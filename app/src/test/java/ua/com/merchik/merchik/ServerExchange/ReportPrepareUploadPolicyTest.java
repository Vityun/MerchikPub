package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.JsonElement;
import org.junit.Test;

import java.util.Collections;

import ua.com.merchik.merchik.data.UploadToServ.ReportPrepareServ;

import static org.junit.Assert.*;

public class ReportPrepareUploadPolicyTest {
    @Test public void waitsFiveSecondsFromFirstObservedPendingRow() {
        ReportPrepareUploadPolicy policy = new ReportPrepareUploadPolicy();
        ReportPrepareServ row = row("1", "10");
        assertEquals(5000, policy.remaining(row, 100));
        assertEquals(1, policy.remaining(row, 5099));
        assertEquals(0, policy.remaining(row, 5100));
    }

    @Test public void everyDigitRestartsOnlyThisRowsDelay() {
        ReportPrepareUploadPolicy policy = new ReportPrepareUploadPolicy();
        ReportPrepareServ first = row("1", "1");
        ReportPrepareServ other = row("2", "50");
        policy.remaining(first, 0);
        policy.remaining(other, 0);
        first.setPrice("12");
        assertEquals(5000, policy.remaining(first, 1000));
        first.setPrice("123");
        assertEquals(5000, policy.remaining(first, 2000));
        assertEquals(0, policy.remaining(other, 5000));
        assertEquals(2000, policy.remaining(first, 5000));
        assertEquals(0, policy.remaining(first, 7000));
    }

    @Test public void draftInputAlsoRestartsDelayWithoutSavingInvalidValue() {
        ReportPrepareUploadPolicy policy = new ReportPrepareUploadPolicy();
        ReportPrepareServ row = row("1", "10");
        policy.remaining(row, 0);
        policy.edited("1", 4900);
        assertEquals(4900, policy.remaining(row, 5000));
        assertEquals(0, policy.remaining(row, 9900));
    }

    @Test public void changingOtherFieldOfSameRowRestartsDelay() {
        ReportPrepareUploadPolicy policy = new ReportPrepareUploadPolicy();
        ReportPrepareServ row = row("1", "10");
        policy.remaining(row, 0);
        row.setPrice_min("20");
        row.setPrice_max("20");
        assertEquals(5000, policy.remaining(row, 4000));
    }

    @Test public void bothSuccessFlagsAndExactSnapshotAreRequired() {
        ReportPrepareServ row = row("671918923", "10");
        JsonElement sent = ReportPrepareUploadPolicy.snapshot(row);
        assertTrue(ReportPrepareUploadPolicy.canAcknowledge(true, true, sent, row));
        assertFalse(ReportPrepareUploadPolicy.canAcknowledge(false, true, sent, row));
        assertFalse(ReportPrepareUploadPolicy.canAcknowledge(true, false, sent, row));
        assertFalse(ReportPrepareUploadPolicy.canAcknowledge(true, true, null, row));
        assertFalse(ReportPrepareUploadPolicy.canAcknowledge(true, true, sent, null));
    }

    @Test public void oldAcknowledgementCannotClearNewPriceEvenWithinSameSecond() {
        ReportPrepareServ row = row("671918923", "10");
        row.setDt(1791890644L);
        JsonElement sent = ReportPrepareUploadPolicy.snapshot(row);
        row.setPrice("20");
        assertFalse(ReportPrepareUploadPolicy.canAcknowledge(true, true, sent, row));
        assertTrue(ReportPrepareUploadPolicy.canAcknowledge(true, true,
                ReportPrepareUploadPolicy.snapshot(row), row));
    }

    @Test public void detectsChangesOutsidePriceFields() {
        ReportPrepareServ row = row("1", "10");
        JsonElement sent = ReportPrepareUploadPolicy.snapshot(row);
        row.setFace("3");
        assertFalse(ReportPrepareUploadPolicy.canAcknowledge(true, true, sent, row));
        row.setFace("0");
        row.setNotes("new comment");
        assertFalse(ReportPrepareUploadPolicy.canAcknowledge(true, true, sent, row));
        row.setNotes(null);
        row.buyer_order_id = "9";
        assertFalse(ReportPrepareUploadPolicy.canAcknowledge(true, true, sent, row));
    }

    @Test public void snapshotCannotBeMutatedThroughOutgoingDto() {
        ReportPrepareServ row = row("1", "10");
        JsonElement sent = ReportPrepareUploadPolicy.snapshot(row);
        row.setPrice("99");
        assertEquals("10", sent.getAsJsonObject().get("price").getAsString());
    }

    @Test public void duplicateProductWithDifferentRowIdIsNotAcknowledged() {
        ReportPrepareServ first = row("1", "10");
        ReportPrepareServ duplicate = row("2", "10");
        assertFalse(ReportPrepareUploadPolicy.canAcknowledge(true, true,
                ReportPrepareUploadPolicy.snapshot(first), duplicate));
    }

    @Test public void cleanupAndRestartDoNotLosePersistentPendingRows() {
        ReportPrepareUploadPolicy policy = new ReportPrepareUploadPolicy();
        ReportPrepareServ row = row("1", "10");
        policy.remaining(row, 0);
        policy.retain(Collections.emptySet());
        assertEquals(5000, policy.remaining(row, 6000));
        policy.clear();
        assertEquals(5000, policy.remaining(row, 7000));
    }

    private static ReportPrepareServ row(String id, String price) {
        ReportPrepareServ row = new ReportPrepareServ();
        row.setElement_id(id);
        row.setCode_dad2("1131026021456061377");
        row.setTovar_id("175133");
        row.setPrice(price);
        row.setPrice_min("0");
        row.setPrice_max("0");
        row.setFace("0");
        return row;
    }
}
