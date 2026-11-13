package ua.com.merchik.merchik.database.realm.tables;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.google.gson.JsonObject;

import ua.com.merchik.merchik.ServerExchange.ReportPrepareUploadPolicy;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;

import static org.junit.Assert.*;

public class ReportPrepareVisitMergeTest {
    private static final long VISIT = 1131126038653061639L;

    private static ReportPrepareDB row(long id, String product, boolean pending) {
        ReportPrepareDB row = new ReportPrepareDB();
        row.setID(id);
        row.setCodeDad2(String.valueOf(VISIT));
        row.setTovarId(product);
        row.setUploadStatus(pending ? 1 : 0);
        return row;
    }

    private static ReportPrepareVisitMerge.Change change(ReportPrepareDB local, ReportPrepareDB server) {
        return ReportPrepareVisitMerge.plan(VISIT, Collections.singletonList(local),
                Collections.singletonList(server)).get(0);
    }

    @Test public void incidentIdsReconcileAndCleanValuesComeFromServer() {
        ReportPrepareDB local = row(683658465, "151651", false);
        local.setFace("0");
        ReportPrepareDB server = row(684411812, "151651", false);
        server.setFace("2");
        ReportPrepareVisitMerge.Change change = change(local, server);
        assertEquals(Collections.singletonList(683658465L), change.obsoleteIds);
        ReportPrepareDB accepted = change.apply();
        assertEquals(Long.valueOf(684411812), accepted.getID());
        assertEquals("2", accepted.getFace());
        assertEquals(0, accepted.getUploadStatus());
    }

    @Test public void pendingPayloadMovesIntactToNewIdAndStaysPending() {
        ReportPrepareDB local = row(683658465, "151651", true);
        local.setDtChange(1794560000L);
        local.setDtReport("2026-11-13");
        local.setKli("9382");
        local.setAddrId("38653");
        local.setPrice("27.9");
        local.setFace("2");
        local.setAmount(3);
        local.setDtExpire("2027-01-01");
        local.setExpireLeft("10");
        local.setNotes("local notes");
        local.setUp("4");
        local.setAkciya("1");
        local.setAkciyaId("5");
        local.setOborotvedNum("6");
        local.setErrorId("7");
        local.setErrorComment("local error");
        local.buyerOrderId = 8;
        local.setPriceMin("32.9");
        local.setPriceMax("32.9");
        ReportPrepareDB server = row(684411812, "151651", false);
        server.setFace("0");
        server.facesPlan = 9;
        server.akciya_plan_id = "new plan";
        JsonObject expected = ReportPrepareUploadPolicy.snapshot(ReportPrepareRealm.uploadSnapshot(local)).getAsJsonObject();
        ReportPrepareDB accepted = change(local, server).apply();
        JsonObject actual = ReportPrepareUploadPolicy.snapshot(ReportPrepareRealm.uploadSnapshot(accepted)).getAsJsonObject();
        String idField = expected.keySet().stream()
                .filter(key -> expected.get(key).isJsonPrimitive() && "683658465".equals(expected.get(key).getAsString()))
                .findFirst().get();
        expected.addProperty(idField, "684411812");
        assertEquals(expected, actual);
        assertEquals(1, accepted.getUploadStatus());
        assertEquals(Integer.valueOf(9), accepted.facesPlan);
        assertEquals("new plan", accepted.akciya_plan_id);
    }

    @Test public void unchangedIdAlsoPreservesPendingValues() {
        ReportPrepareDB local = row(10, "1", true);
        local.setFace("3");
        ReportPrepareVisitMerge.Change change = change(local, row(10, "1", false));
        assertTrue(change.obsoleteIds.isEmpty());
        assertEquals("3", change.apply().getFace());
        assertEquals(1, change.server.getUploadStatus());
    }

    @Test public void newProductIsInserted() {
        List<ReportPrepareVisitMerge.Change> changes = ReportPrepareVisitMerge.plan(VISIT,
                Collections.emptyList(), Collections.singletonList(row(10, "1", false)));
        assertNull(changes.get(0).pending);
        assertTrue(changes.get(0).obsoleteIds.isEmpty());
        assertEquals(Long.valueOf(10), changes.get(0).apply().getID());
    }

    @Test public void onePendingDuplicateWinsAndOtherOldIdsAreRemoved() {
        ReportPrepareDB pending = row(10, "1", true);
        pending.setFace("4");
        ReportPrepareVisitMerge.Change change = ReportPrepareVisitMerge.plan(VISIT,
                Arrays.asList(pending, row(11, "1", false)), Collections.singletonList(row(12, "1", false))).get(0);
        assertEquals(Arrays.asList(10L, 11L), change.obsoleteIds);
        assertEquals("4", change.apply().getFace());
    }

    @Test public void localRowsNotInResponseAreUntouched() {
        ReportPrepareDB other = row(11, "2", true);
        ReportPrepareVisitMerge.Change change = ReportPrepareVisitMerge.plan(VISIT,
                Arrays.asList(row(10, "1", false), other), Collections.singletonList(row(12, "1", false))).get(0);
        assertEquals(Collections.singletonList(10L), change.obsoleteIds);
        assertEquals(1, other.getUploadStatus());
    }

    @Test public void emptyResponseChangesNothing() {
        assertTrue(ReportPrepareVisitMerge.plan(VISIT, Collections.singletonList(row(10, "1", true)),
                Collections.emptyList()).isEmpty());
    }

    @Test(expected = IllegalArgumentException.class) public void foreignVisitIdCollisionIsRejected() {
        ReportPrepareDB foreign = row(12, "1", true);
        foreign.setCodeDad2("123");
        change(foreign, row(12, "1", false));
    }

    @Test(expected = IllegalArgumentException.class) public void otherProductIdCollisionIsRejected() {
        change(row(12, "2", true), row(12, "1", false));
    }

    @Test(expected = IllegalArgumentException.class) public void multiplePendingCopiesAreNotDiscarded() {
        ReportPrepareVisitMerge.plan(VISIT, Arrays.asList(row(10, "1", true), row(11, "1", true)),
                Collections.singletonList(row(12, "1", false)));
    }

    @Test(expected = IllegalArgumentException.class) public void duplicateServerProductIsRejected() {
        ReportPrepareVisitMerge.plan(VISIT, Collections.emptyList(), Arrays.asList(row(10, "1", false), row(11, "1", false)));
    }

    @Test(expected = IllegalArgumentException.class) public void duplicateServerIdIsRejected() {
        ReportPrepareVisitMerge.plan(VISIT, Collections.emptyList(), Arrays.asList(row(10, "1", false), row(10, "2", false)));
    }

    @Test(expected = IllegalArgumentException.class) public void outOfScopeResponseIsRejected() {
        ReportPrepareDB wrong = row(10, "1", false);
        wrong.setCodeDad2("123");
        ReportPrepareVisitMerge.plan(VISIT, Collections.emptyList(), Collections.singletonList(wrong));
    }

    @Test public void invalidLaterRowDoesNotModifyEarlierPendingPayload() {
        ReportPrepareDB local = row(10, "1", true);
        local.setFace("5");
        ReportPrepareDB server = row(12, "1", false);
        server.setFace("0");
        try {
            ReportPrepareVisitMerge.plan(VISIT, Collections.singletonList(local), Arrays.asList(server, null));
            fail("Expected invalid response");
        } catch (IllegalArgumentException expected) {
            assertEquals("0", server.getFace());
            assertEquals("5", local.getFace());
        }
    }
}
