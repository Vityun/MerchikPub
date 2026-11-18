package ua.com.merchik.merchik.data.synchronization;

import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class StartupPolicyTest {
    @Test public void offlineLoginNeverShowsMandatoryLoading() {
        boolean offline = StartupPolicy.isOffline(true, true, true, true);
        assertTrue(offline);
        assertFalse(StartupPolicy.shouldShowLoading(false, offline));
    }

    @Test public void failedServerAndDisconnectedNetworkAreOffline() {
        assertTrue(StartupPolicy.isOffline(false, true, true, false));
        assertTrue(StartupPolicy.isOffline(false, false, true, true));
    }

    @Test public void unknownServerStatusIsNotMistakenForAnOutage() {
        assertFalse(StartupPolicy.isOffline(false, true, false, false));
        assertTrue(StartupPolicy.shouldShowLoading(false, false));
    }

    @Test public void readyLocalDataNeverRequiresMandatoryLoading() {
        assertFalse(StartupPolicy.shouldShowLoading(true, false));
        assertFalse(StartupPolicy.shouldShowLoading(true, true));
    }

    @Test public void incompleteOnlineStartupKeepsLoadingUntilRequiredTablesArrive() {
        for (int poll = 0; poll < 120; poll++) {
            assertTrue(StartupPolicy.shouldShowLoading(false, false));
        }
        assertFalse(StartupPolicy.shouldShowLoading(true, false));
    }

    @Test public void recoveredNetworkResumesInitialLoading() {
        assertTrue(StartupPolicy.shouldShowLoading(false, false));
        assertFalse(StartupPolicy.shouldShowLoading(false, true));
        assertTrue(StartupPolicy.shouldShowLoading(false, false));
    }

    @Test public void planAloneNeverMakesInitialDataReady() {
        assertFalse(StartupPolicy.areRequiredTablesReady(true, false, false, false, false, false, true, true));
        assertFalse(StartupPolicy.areRequiredTablesReady(true, false, true, true, false, true, true, true));
    }

    @Test public void staleFlagsCannotReplaceMissingServerSiteObjects() {
        assertFalse(StartupPolicy.areRequiredTablesReady(true, true, true, true, false, true, true, true));
    }

    @Test public void missingRoomThemesAlsoPreventReadiness() {
        assertFalse(StartupPolicy.areRequiredTablesReady(true, true, true, true, true, false, true, true));
    }

    @Test public void everyRequiredFlagAndDictionaryMustBeReady() {
        for (int mask = 0; mask < 256; mask++) {
            assertEquals("state=" + mask, mask == 255, StartupPolicy.areRequiredTablesReady(
                    (mask & 1) != 0, (mask & 2) != 0, (mask & 4) != 0,
                    (mask & 8) != 0, (mask & 16) != 0, (mask & 32) != 0,
                    (mask & 64) != 0, (mask & 128) != 0));
        }
    }

    @Test public void successfulEmptyPlanCanStillBeReadyWithDownloadedDictionaries() {
        // wpLoaded/optionsLoaded are set by a successful exchange, even for an empty list.
        assertTrue(StartupPolicy.areRequiredTablesReady(true, true, true, true, true, true, true, true));
    }

    @Test public void reportPrepareAndProductsAreBothRequired() {
        assertFalse(StartupPolicy.areRequiredTablesReady(true, true, true, true, true, true, false, true));
        assertFalse(StartupPolicy.areRequiredTablesReady(true, true, true, true, true, true, true, false));
    }

    @Test public void temporaryHttpFailuresAllowOfflineFallback() {
        for (int code : new int[]{408, 429, 500, 502, 503, 504}) {
            assertTrue("HTTP " + code, StartupPolicy.isTemporaryHttpFailure(code));
        }
    }

    @Test public void authenticationRejectionsAreNotTemporaryFailures() {
        for (int code : new int[]{200, 400, 401, 403, 404}) {
            assertFalse("HTTP " + code, StartupPolicy.isTemporaryHttpFailure(code));
        }
    }

    @Test public void retrySelectsOnlyMissingRequiredTables() {
        for (int mask = 0; mask < 16; mask++) {
            EnumSet<StartupPolicy.InitialTable> missing = StartupPolicy.missingTables(true,
                    (mask & 1) != 0, (mask & 2) != 0, (mask & 4) != 0, (mask & 8) != 0);
            assertEquals((mask & 1) == 0, missing.contains(StartupPolicy.InitialTable.PLAN));
            assertEquals((mask & 2) == 0, missing.contains(StartupPolicy.InitialTable.OPTIONS));
            assertEquals((mask & 4) == 0, missing.contains(StartupPolicy.InitialTable.SITE_OBJECTS));
            assertEquals((mask & 8) == 0, missing.contains(StartupPolicy.InitialTable.THEMES));
        }
    }

    @Test public void completedEmptyPlanAndOptionsAreNotRetriedAsMissingData() {
        assertTrue(StartupPolicy.missingTables(true, true, true, true, true).isEmpty());
    }

    @Test public void specialAccountsStillRetryDictionariesButNotVisitLists() {
        assertEquals(EnumSet.of(StartupPolicy.InitialTable.SITE_OBJECTS, StartupPolicy.InitialTable.THEMES),
                StartupPolicy.missingTables(false, false, false, false, false));
        assertTrue(StartupPolicy.missingTables(false, false, false, true, true).isEmpty());
    }

    @Test public void optionsAreRetriedEvenWhenAllOtherTablesAreReady() {
        assertEquals(EnumSet.of(StartupPolicy.InitialTable.OPTIONS),
                StartupPolicy.missingTables(true, true, false, true, true));
    }

    @Test public void startupPollingDoesNotRequestTheSameTablesEverySecond() {
        StartupPolicy.RetryGate gate = new StartupPolicy.RetryGate();
        assertTrue(gate.tryStart("account", 0));
        for (int second = 1; second < 30; second++) assertFalse(gate.tryStart("account", second * 1000));
        assertTrue(gate.tryStart("account", 30000));
        assertFalse(gate.tryStart("account", 30001));
        assertTrue(gate.tryStart("account", 60000));
    }

    @Test public void accountSessionLanguageAndDatabaseChangesResetRetryDelay() {
        for (int index = 0; index < 5; index++) {
            StartupPolicy.RetryGate gate = new StartupPolicy.RetryGate();
            Object[] scope = {260523, "session", 2, new Object(), new Object()};
            assertTrue(gate.tryStart(Arrays.asList(scope), 100));
            assertFalse(gate.tryStart(Arrays.asList(scope.clone()), 101));
            Object[] changed = scope.clone();
            changed[index] = new Object();
            assertTrue(gate.tryStart(Arrays.asList(changed), 102));
        }
    }

    @Test public void elapsedClockResetDoesNotSuppressRetriesForever() {
        StartupPolicy.RetryGate gate = new StartupPolicy.RetryGate();
        assertTrue(gate.tryStart("account", 60000));
        assertTrue(gate.tryStart("account", 10));
    }
}
