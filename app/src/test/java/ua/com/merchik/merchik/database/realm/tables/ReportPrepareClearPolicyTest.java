package ua.com.merchik.merchik.database.realm.tables;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.UploadToServ.ReportPrepareServ;

import static org.junit.Assert.*;

public class ReportPrepareClearPolicyTest {
    private static final long DAD2 = 1161126028847061680L;
    private static final long NOW = 1794900000L;

    @Test
    public void clearsEveryEnteredRequisiteAndQueuesUpload() {
        ReportPrepareDB row = row(1, DAD2, "10");
        ReportPrepareClearPolicy.Result result = clear(visit(), row);

        assertEquals(ReportPrepareClearPolicy.Status.CLEARED, result.status);
        assertEquals(Collections.singletonList(1L), result.rowIds);
        assertEquals(Collections.singleton("10"), result.tovarIds);
        assertEquals("0", row.getPrice());
        assertEquals("0", row.getPriceMin());
        assertEquals("0", row.getPriceMax());
        assertEquals("0", row.getFace());
        assertEquals(0, row.getAmount());
        assertEquals("0", row.getUp());
        assertEquals("", row.getDtExpire());
        assertEquals("0", row.getExpireLeft());
        assertEquals("", row.getNotes());
        assertEquals("0", row.getAkciya());
        assertEquals("0", row.getAkciyaId());
        assertEquals("0", row.getOborotvedNum());
        assertEquals("", row.oborotved_num_date);
        assertEquals("0", row.getErrorId());
        assertEquals("", row.getErrorComment());
        assertEquals("0", row.getTovarError());
        assertEquals(1, row.getUploadStatus());
        assertEquals(NOW, row.getDtChange());
    }

    @Test
    public void retainsIdentityPlanAndDocumentMetadata() {
        ReportPrepareDB row = row(1, DAD2, "10");
        row.setKli("9382");
        row.setAddrId("28847");
        row.setIsp("42");
        row.setAuthorId("43");
        row.setDt("2026-11-16");
        row.setDtReport("2026-11-17");
        row.setOtchetNum("DOC-1");
        row.setOtchetUnique("unique-1");
        row.facesPlan = 9;
        row.akciya_plan_id = "301";
        row.buyerOrderId = 44;
        clear(visit(), row);

        assertEquals(Long.valueOf(1), row.getID());
        assertEquals(String.valueOf(DAD2), row.getCodeDad2());
        assertEquals("10", row.getTovarId());
        assertEquals("9382", row.getKli());
        assertEquals("28847", row.getAddrId());
        assertEquals("42", row.getIsp());
        assertEquals("43", row.getAuthorId());
        assertEquals("2026-11-16", row.getDt());
        assertEquals("2026-11-17", row.getDtReport());
        assertEquals("DOC-1", row.getOtchetNum());
        assertEquals("unique-1", row.getOtchetUnique());
        assertEquals(Integer.valueOf(9), row.facesPlan);
        assertEquals("301", row.akciya_plan_id);
        assertEquals(Integer.valueOf(44), row.buyerOrderId);
    }

    @Test
    public void onlySelectedProductsOfThisVisitAreCleared() {
        ReportPrepareDB selected = row(1, DAD2, "10");
        ReportPrepareDB unselected = row(2, DAD2, "20");
        ReportPrepareDB anotherVisit = row(3, DAD2 + 1, "10");
        ReportPrepareDB duplicateProduct = row(4, DAD2, "10");
        ReportPrepareClearPolicy.Result result = ReportPrepareClearPolicy.clear(DAD2, visit(),
                Arrays.asList(selected, unselected, anotherVisit, duplicateProduct),
                Collections.singleton("10"), NOW);

        assertEquals(Arrays.asList(1L, 4L), result.rowIds);
        assertEquals(Collections.singleton("10"), result.tovarIds);
        assertEquals("0", duplicateProduct.getPrice());
        assertUntouched(unselected);
        assertUntouched(anotherVisit);
    }

    @Test
    public void entireSelectedGroupIsCleared() {
        ReportPrepareDB first = row(1, DAD2, "10");
        ReportPrepareDB second = row(2, DAD2, "20");
        ReportPrepareClearPolicy.Result result = ReportPrepareClearPolicy.clear(DAD2, visit(),
                Arrays.asList(first, second), new HashSet<>(Arrays.asList("10", "20")), NOW);
        assertEquals(2, result.tovarIds.size());
        assertEquals("0", first.getPrice());
        assertEquals("0", second.getPrice());
    }

    @Test
    public void visitEndRefusesAllEdits() {
        WpDataDB visit = visit();
        visit.setVisit_end_dt(NOW);
        ReportPrepareDB row = row(1, DAD2, "10");
        assertRefused(ReportPrepareClearPolicy.Status.VISIT_FINISHED, clear(visit, row));
        assertUntouched(row);
    }

    @Test
    public void clientEndAloneAlsoRefusesAllEdits() {
        WpDataDB visit = visit();
        visit.setClient_end_dt(NOW);
        ReportPrepareDB row = row(1, DAD2, "10");
        assertRefused(ReportPrepareClearPolicy.Status.VISIT_FINISHED, clear(visit, row));
        assertUntouched(row);
    }

    @Test
    public void missingOrDifferentVisitRefusesAllEdits() {
        ReportPrepareDB row = row(1, DAD2, "10");
        assertRefused(ReportPrepareClearPolicy.Status.VISIT_NOT_FOUND, clear(null, row));
        WpDataDB other = visit();
        other.setCode_dad2(DAD2 + 1);
        assertRefused(ReportPrepareClearPolicy.Status.VISIT_NOT_FOUND, clear(other, row));
        assertUntouched(row);
    }

    @Test
    public void startedButUnfinishedVisitAllowsClearingPendingEdits() {
        WpDataDB visit = visit();
        visit.setVisit_start_dt(NOW - 1000);
        visit.setClient_start_dt(NOW - 1000);
        ReportPrepareDB row = row(1, DAD2, "10");
        row.setUploadStatus(1);
        assertEquals(ReportPrepareClearPolicy.Status.CLEARED, clear(visit, row).status);
        assertEquals("0", row.getPrice());
        assertEquals(1, row.getUploadStatus());
    }

    @Test
    public void absentProductsDoNotCreateOrChangeRows() {
        ReportPrepareDB row = row(1, DAD2, "20");
        ReportPrepareClearPolicy.Result result = clear(visit(), row);
        assertTrue(result.rowIds.isEmpty());
        assertTrue(result.tovarIds.isEmpty());
        assertUntouched(row);
    }

    @Test
    public void uploadSnapshotContainsResetsNotOldValues() {
        ReportPrepareDB row = row(1, DAD2, "10");
        clear(visit(), row);
        ReportPrepareServ upload = ReportPrepareRealm.uploadSnapshot(row);
        assertEquals("1", upload.getElement_id());
        assertEquals(String.valueOf(DAD2), upload.getCode_dad2());
        assertEquals("0", upload.getPrice());
        assertEquals("0", upload.getPrice_min());
        assertEquals("0", upload.getPrice_max());
        assertEquals("0", upload.getFace());
        assertEquals(0, upload.getAmount());
        assertEquals("", upload.getDt_expire());
        assertEquals("0", upload.getExpire_left());
        assertEquals("0", upload.getUp());
        assertEquals("0", upload.getAkciya());
        assertEquals("0", upload.getAkciya_id());
        assertEquals("0", upload.getOborotved_num());
        assertEquals("0", upload.getError_id());
        assertEquals("", upload.getError_comment());
        assertEquals("", upload.getNotes());
        assertEquals(NOW, upload.getDt());
    }

    private static ReportPrepareClearPolicy.Result clear(WpDataDB visit, ReportPrepareDB row) {
        return ReportPrepareClearPolicy.clear(DAD2, visit, Collections.singletonList(row),
                Collections.singleton("10"), NOW);
    }

    private static void assertRefused(ReportPrepareClearPolicy.Status status,
                                      ReportPrepareClearPolicy.Result result) {
        assertEquals(status, result.status);
        assertTrue(result.rowIds.isEmpty());
        assertTrue(result.tovarIds.isEmpty());
    }

    private static void assertUntouched(ReportPrepareDB row) {
        assertEquals("103", row.getPrice());
        assertEquals("9", row.getFace());
        assertEquals("1", row.getAkciya());
        assertEquals("note", row.getNotes());
        assertEquals(0, row.getUploadStatus());
        assertEquals(123, row.getDtChange());
    }

    private static WpDataDB visit() {
        WpDataDB visit = new WpDataDB();
        visit.setCode_dad2(DAD2);
        return visit;
    }

    private static ReportPrepareDB row(long id, long dad2, String tovarId) {
        ReportPrepareDB row = new ReportPrepareDB();
        row.setID(id);
        row.setCodeDad2(String.valueOf(dad2));
        row.setTovarId(tovarId);
        row.setPrice("103");
        row.setPriceMin("120");
        row.setPriceMax("120");
        row.setFace("9");
        row.setAmount(18);
        row.setUp("6");
        row.setDtExpire("2027-01-01");
        row.setExpireLeft("2");
        row.setNotes("note");
        row.setAkciya("1");
        row.setAkciyaId("301");
        row.setOborotvedNum("27");
        row.oborotved_num_date = "2026-11-17";
        row.setErrorId("4");
        row.setErrorComment("comment");
        row.setTovarError("1");
        row.setDtChange(123);
        return row;
    }
}
