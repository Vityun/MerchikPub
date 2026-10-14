package ua.com.merchik.merchik.database.realm.tables;

import androidx.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;

import static org.junit.Assert.*;

public class ReportPrepareDuplicateTest {
    private Realm realm;
    private final List<String> logs = new ArrayList<>();
    private final List<String> deferred = new ArrayList<>();

    @Before
    public void setUp() {
        Realm.init(InstrumentationRegistry.getTargetContext());
        realm = Realm.getInstance(new RealmConfiguration.Builder()
                .name("rp-duplicates-" + UUID.randomUUID()).inMemory().build());
        realm.executeTransaction(r -> r.copyToRealm(row(1L, "100", "10", "20")));
    }

    @After
    public void tearDown() {
        if (realm != null) realm.close();
    }

    @Test
    public void cleanLocalRowWinsOverNewerDuplicateWithoutStallingWatermark() {
        ReportPrepareDB duplicate = row(2L, "100", "10", "0");
        duplicate.setDtChange(Long.MAX_VALUE);
        assertTrue(merge(duplicate));
        assertEquals(1, realm.where(ReportPrepareDB.class).count());
        assertEquals("20", byId(1).getPrice());
        assertEquals("30", byId(1).getPriceMin());
        assertEquals("30", byId(1).getPriceMax());
        assertEquals("3", byId(1).getFace());
        assertEquals("local notes", byId(1).getNotes());
        assertEquals(1, logs.size());
        assertTrue(logs.get(0).contains("incomingId=2, keptId=1"));
        assertTrue(deferred.isEmpty());
    }

    @Test
    public void dirtyLocalRowAlsoWinsOverDuplicate() {
        realm.executeTransaction(r -> byId(1).setUploadStatus(1));
        assertTrue(merge(row(2L, "100", "10", "0")));
        assertEquals(1, byId(1).getUploadStatus());
        assertEquals("20", byId(1).getPrice());
        assertNull(byId(2));
        assertTrue(deferred.isEmpty());
    }

    @Test
    public void duplicateWithinOneBatchIsSkippedButOtherProductsAndVisitsAreSaved() {
        assertTrue(merge(row(2L, "100", "20", "25"), row(3L, "100", "20", "0"),
                row(4L, "200", "20", "45")));
        assertEquals(3, realm.where(ReportPrepareDB.class).count());
        assertEquals("25", byId(2).getPrice());
        assertNull(byId(3));
        assertEquals("45", byId(4).getPrice());
        assertEquals(1, logs.size());
    }

    @Test
    public void repeatedDownloadsCannotCreateDuplicate() {
        for (int i = 0; i < 3; i++) assertTrue(merge(row(2L, "100", "10", "0")));
        assertEquals(1, realm.where(ReportPrepareDB.class).count());
        assertEquals("20", byId(1).getPrice());
        assertEquals(3, logs.size());
    }

    @Test
    public void sameIdUpdatesCleanRowButDefersDirtyRow() {
        assertTrue(merge(row(1L, "100", "10", "25")));
        assertEquals("25", byId(1).getPrice());
        realm.executeTransaction(r -> byId(1).setUploadStatus(1));
        assertFalse(merge(row(1L, "100", "10", "0")));
        assertEquals("25", byId(1).getPrice());
        assertEquals(1, byId(1).getUploadStatus());
        assertEquals(Collections.singletonList("100"), deferred);
        assertTrue(logs.isEmpty());
    }

    @Test
    public void existingIdCannotBeReassignedToAnotherVisitOrProduct() {
        assertTrue(merge(row(1L, "200", "10", "0"), row(1L, "100", "20", "0")));
        assertEquals("100", byId(1).getCodeDad2());
        assertEquals("10", byId(1).getTovarId());
        assertEquals("20", byId(1).getPrice());
        assertEquals(2, logs.size());
    }

    @Test
    public void malformedIdentityIsLoggedAndNotSaved() {
        assertFalse(merge(null, row(null, "100", "20", "0"), row(2L, "", "20", "0")));
        assertEquals(1, realm.where(ReportPrepareDB.class).count());
        assertEquals(3, logs.size());
    }

    @Test
    public void localCreationReusesLatestValuesAndReturnsDetachedRow() {
        ReportPrepareDB result = ReportPrepareRealm.getOrCreateLocal(realm, row(null, "100", "10", "0"), logs::add);
        assertEquals(Long.valueOf(1), result.getID());
        assertEquals("20", result.getPrice());
        assertFalse(RealmObject.isManaged(result));
        assertFalse(realm.isInTransaction());
        assertEquals(1, realm.where(ReportPrepareDB.class).count());
        assertTrue(logs.get(0).contains("action=REUSE_EXISTING"));
    }

    @Test
    public void localCreationAllocatesIdInTransactionAndSecondAttemptReusesIt() {
        ReportPrepareDB first = ReportPrepareRealm.getOrCreateLocal(realm, row(null, "100", "20", "25"), logs::add);
        ReportPrepareDB second = ReportPrepareRealm.getOrCreateLocal(realm, row(null, "100", "20", "0"), logs::add);
        assertEquals(Long.valueOf(2), first.getID());
        assertEquals(first.getID(), second.getID());
        assertEquals("25", second.getPrice());
        assertEquals(2, realm.where(ReportPrepareDB.class).count());
    }

    @Test
    public void legacySingleRowWriteCannotInsertDuplicateOrChangeIdentity() {
        realm.executeTransaction(r -> {
            ReportPrepareRealm.saveRowInTransaction(r, row(2L, "100", "10", "0"), logs::add);
            ReportPrepareRealm.saveRowInTransaction(r, row(1L, "100", "20", "0"), logs::add);
        });
        assertEquals(1, realm.where(ReportPrepareDB.class).count());
        assertEquals("20", byId(1).getPrice());
        assertEquals(2, logs.size());
    }

    private boolean merge(ReportPrepareDB... rows) {
        return ReportPrepareRealm.mergeServerRows(realm, Arrays.asList(rows), deferred::add, logs::add);
    }

    private ReportPrepareDB byId(long id) {
        return realm.where(ReportPrepareDB.class).equalTo("iD", id).findFirst();
    }

    private ReportPrepareDB row(Long id, String dad2, String tovarId, String price) {
        ReportPrepareDB row = new ReportPrepareDB();
        row.setID(id);
        row.setCodeDad2(dad2);
        row.setTovarId(tovarId);
        row.setPrice(price);
        row.setPriceMin("30");
        row.setPriceMax("30");
        row.setFace("3");
        row.setNotes("local notes");
        return row;
    }
}
