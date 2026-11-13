package ua.com.merchik.merchik.data;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VisitReassignmentStoreTest {
    private static final int USER = 256140;
    private static final long DAD2 = 1131126043056061639L;
    private static final long ID = 4635525924L;
    private final Map<String, Long> data = new HashMap<>();
    private final Set<SharedPreferences.OnSharedPreferenceChangeListener> listeners = new HashSet<>();
    private SharedPreferences preferences;
    private VisitReassignmentStore store;
    private int editCount;

    @Before
    public void setUp() {
        preferences = (SharedPreferences) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{SharedPreferences.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getLong": return data.getOrDefault(args[0], (Long) args[1]);
                        case "contains": return data.containsKey(args[0]);
                        case "edit":
                            editCount++;
                            return editor();
                        case "registerOnSharedPreferenceChangeListener":
                            listeners.add((SharedPreferences.OnSharedPreferenceChangeListener) args[0]);
                            return null;
                        case "unregisterOnSharedPreferenceChangeListener":
                            listeners.remove(args[0]);
                            return null;
                        default: throw new AssertionError(method.getName());
                    }
                });
        store = new VisitReassignmentStore(preferences);
    }

    @Test
    public void recordsBothStableIdentityAndLegacyId() {
        store.record(USER, DAD2, ID);
        assertTrue(store.wasReassigned(USER, 0, DAD2));
        assertTrue(store.wasReassigned(USER, ID, 0));
    }

    @Test
    public void doesNotCallAnyMissingVisitReassigned() {
        assertFalse(store.wasReassigned(USER, ID, DAD2));
        assertFalse(store.wasReassigned(USER, ID, 0));
    }

    @Test
    public void isolatesUsers() {
        store.record(USER, DAD2, ID);
        assertFalse(store.wasReassigned(USER + 1, ID, DAD2));
        assertFalse(store.wasReassigned(USER + 1, ID, 0));
    }

    @Test
    public void dad2TakesPriorityOverReusedId() {
        store.record(USER, DAD2, ID);
        assertFalse(store.wasReassigned(USER, ID, DAD2 + 1));
    }

    @Test
    public void repeatedServerDeletionPreservesOriginalLocalId() {
        store.record(USER, DAD2, ID);
        store.record(USER, DAD2, 0);
        assertTrue(store.wasReassigned(USER, ID, 0));
    }

    @Test
    public void remembersReasonAcrossStoreRecreation() {
        store.record(USER, DAD2, ID);
        assertTrue(new VisitReassignmentStore(preferences).wasReassigned(USER, ID, DAD2));
    }

    @Test
    public void returnedAssignmentClearsBothKeys() {
        store.record(USER, DAD2, ID);
        store.clear(USER, DAD2);
        assertFalse(store.wasReassigned(USER, ID, DAD2));
        assertFalse(store.wasReassigned(USER, ID, 0));
    }

    @Test
    public void returnedAssignmentDoesNotClearAnotherUser() {
        store.record(USER, DAD2, ID);
        store.record(USER + 1, DAD2, ID);
        store.clear(USER, DAD2);
        assertTrue(store.wasReassigned(USER + 1, ID, DAD2));
    }

    @Test
    public void returnedAssignmentDoesNotClearReusedIdOfAnotherVisit() {
        store.record(USER, DAD2, ID);
        store.record(USER, DAD2 + 1, ID);
        store.clear(USER, DAD2);
        assertTrue(store.wasReassigned(USER, ID, 0));
        assertTrue(store.wasReassigned(USER, ID, DAD2 + 1));
    }

    @Test
    public void rejectsInvalidScope() {
        store.record(0, DAD2, ID);
        store.record(USER, 0, ID);
        assertFalse(store.wasReassigned(0, ID, DAD2));
        assertFalse(store.wasReassigned(USER, ID, 0));
    }

    @Test
    public void recordsDad2EvenWhenRowWasAlreadyAbsent() {
        store.record(USER, DAD2, 0);
        assertTrue(store.wasReassigned(USER, 0, DAD2));
        assertFalse(store.wasReassigned(USER, 0, 0));
    }

    @Test
    public void changedLocalIdRemovesObsoleteAlias() {
        store.record(USER, DAD2, ID);
        store.record(USER, DAD2, ID + 1);
        assertFalse(store.wasReassigned(USER, ID, 0));
        assertTrue(store.wasReassigned(USER, ID + 1, 0));
    }

    @Test
    public void filtersPreferenceEventsByUserAndVisitKey() {
        assertTrue(VisitReassignmentStore.isVisitChange(USER + ":dad2:" + DAD2, USER));
        assertFalse(VisitReassignmentStore.isVisitChange(USER + ":id:" + ID, USER));
        assertFalse(VisitReassignmentStore.isVisitChange((USER + 1) + ":dad2:" + DAD2, USER));
        assertFalse(VisitReassignmentStore.isVisitChange(null, USER));
        assertFalse(VisitReassignmentStore.isVisitChange(USER + ":notified:" + DAD2, USER));
    }

    @Test
    public void repeatedSyncDoesNotWriteOrNotifyAgain() {
        store.record(USER, DAD2, ID);
        assertTrue(store.claimNotification(USER, ID, DAD2));
        int writes = editCount;
        for (int i = 0; i < 50; i++) {
            store.record(USER, DAD2, 0);
            store.record(USER, DAD2, ID);
            assertFalse(store.claimNotification(USER, ID, DAD2));
        }
        assertEquals(writes, editCount);
    }

    @Test
    public void notificationIsSharedByLegacyAndDad2LinksAndSurvivesRecreation() {
        store.record(USER, DAD2, ID);
        assertTrue(store.claimNotification(USER, ID, 0));
        assertFalse(new VisitReassignmentStore(preferences).claimNotification(USER, 0, DAD2));
        assertTrue(store.wasReassigned(USER, ID, DAD2));
    }

    @Test
    public void silentDeletionDoesNotConsumeNotification() {
        store.record(USER, DAD2, ID);
        store.record(USER, DAD2, 0);
        assertTrue(new VisitReassignmentStore(preferences).claimNotification(USER, ID, DAD2));
    }

    @Test
    public void notificationDoesNotRefreshCardsOrNotifyOtherScreens() {
        store.record(USER, DAD2, ID);
        AtomicInteger visitChanges = new AtomicInteger();
        store.addListener((prefs, key) -> {
            if (VisitReassignmentStore.isVisitChange(key, USER)) visitChanges.incrementAndGet();
        });
        assertTrue(store.claimNotification(USER, ID, DAD2));
        assertEquals(0, visitChanges.get());
    }

    @Test
    public void anotherVisitAndAnotherUserCanEachShowTheirOwnNotice() {
        store.record(USER, DAD2, ID);
        store.record(USER, DAD2 + 1, ID + 1);
        store.record(USER + 1, DAD2, ID);
        assertTrue(store.claimNotification(USER, ID, DAD2));
        assertTrue(store.claimNotification(USER, ID + 1, DAD2 + 1));
        assertTrue(store.claimNotification(USER + 1, ID, DAD2));
    }

    @Test
    public void onlyConfirmedReassignmentCanConsumeNotice() {
        assertFalse(store.claimNotification(USER, ID, DAD2));
        store.record(USER, DAD2, ID);
        assertFalse(store.claimNotification(USER, ID, DAD2 + 1));
        assertFalse(store.claimNotification(0, ID, DAD2));
        assertTrue(store.claimNotification(USER, ID, DAD2));
    }

    @Test
    public void changedLocalIdDoesNotAllowSecondNotice() {
        store.record(USER, DAD2, ID);
        assertTrue(store.claimNotification(USER, ID, 0));
        store.record(USER, DAD2, ID + 1);
        assertFalse(store.claimNotification(USER, ID + 1, 0));
    }

    @Test
    public void restoredThenReassignedVisitStartsNewNotificationCycle() {
        store.record(USER, DAD2, ID);
        assertTrue(store.claimNotification(USER, ID, DAD2));
        store.clear(USER, DAD2);
        assertFalse(store.claimNotification(USER, ID, DAD2));
        store.record(USER, DAD2, ID + 1);
        assertTrue(store.claimNotification(USER, ID + 1, DAD2));
    }

    @Test
    public void deletionListenerImmediatelyRemovesCardWithoutConsumingNotice() {
        Set<Long> visibleDad2 = new HashSet<>();
        visibleDad2.add(DAD2);
        visibleDad2.add(DAD2 + 1);
        AtomicInteger removals = new AtomicInteger();
        store.addListener((prefs, key) -> {
            if (VisitReassignmentStore.isVisitChange(key, USER) && prefs.contains(key)) {
                if (visibleDad2.removeIf(dad2 -> store.wasReassigned(USER, 0, dad2))) {
                    removals.incrementAndGet();
                }
            }
        });
        store.record(USER, DAD2, ID);
        assertFalse(visibleDad2.contains(DAD2));
        assertTrue(visibleDad2.contains(DAD2 + 1));
        store.record(USER, DAD2, 0);
        assertEquals(1, removals.get());
        assertTrue(store.claimNotification(USER, ID, DAD2));
    }

    private SharedPreferences.Editor editor() {
        Map<String, Long> writes = new HashMap<>();
        Set<String> removals = new HashSet<>();
        return (SharedPreferences.Editor) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{SharedPreferences.Editor.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "putLong":
                            writes.put((String) args[0], (Long) args[1]);
                            return proxy;
                        case "remove":
                            removals.add((String) args[0]);
                            return proxy;
                        case "apply":
                            Map<String, Long> before = new HashMap<>(data);
                            removals.forEach(data::remove);
                            data.putAll(writes);
                            Set<String> changed = new HashSet<>(removals);
                            changed.addAll(writes.keySet());
                            for (String key : changed) {
                                if (!Objects.equals(before.get(key), data.get(key))) {
                                    for (SharedPreferences.OnSharedPreferenceChangeListener listener : new HashSet<>(listeners)) {
                                        listener.onSharedPreferenceChanged(preferences, key);
                                    }
                                }
                            }
                            return null;
                        default: throw new AssertionError(method.getName());
                    }
                });
    }

    @Test
    public void listenerSeesCompletedRecordOnceAndIgnoresRepeatedDelta() {
        AtomicInteger events = new AtomicInteger();
        store.addListener((prefs, key) -> {
            if (VisitReassignmentStore.isVisitChange(key, USER)) {
                assertTrue(store.wasReassigned(USER, ID, 0));
                assertTrue(store.wasReassigned(USER, ID, DAD2));
                events.incrementAndGet();
            }
        });
        store.record(USER, DAD2, ID);
        store.record(USER, DAD2, 0);
        store.record(USER + 1, DAD2, ID);
        assertEquals(1, events.get());
    }

    @Test
    public void detachedScreenNoLongerReceivesDeletionEvents() {
        AtomicInteger events = new AtomicInteger();
        SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs, key) -> events.incrementAndGet();
        store.addListener(listener);
        store.removeListener(listener);
        store.record(USER, DAD2, ID);
        assertEquals(0, events.get());
    }

    @Test
    public void restorationEventAlreadySeesClearedReason() {
        store.record(USER, DAD2, ID);
        AtomicInteger events = new AtomicInteger();
        store.addListener((prefs, key) -> {
            if (VisitReassignmentStore.isVisitChange(key, USER)) {
                assertFalse(store.wasReassigned(USER, ID, DAD2));
                assertFalse(store.wasReassigned(USER, ID, 0));
                events.incrementAndGet();
            }
        });
        store.clear(USER, DAD2);
        assertEquals(1, events.get());
    }
}
