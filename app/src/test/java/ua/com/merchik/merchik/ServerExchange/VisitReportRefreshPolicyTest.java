package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.JsonObject;
import org.junit.Test;
import java.time.LocalDate;

import static org.junit.Assert.*;

public class VisitReportRefreshPolicyTest {
    @Test public void onlyUnstartedUnfinishedVisitsAreEligible() {
        assertTrue(VisitReportRefreshPolicy.notStarted(0, 0, 0, 0));
        assertFalse(VisitReportRefreshPolicy.notStarted(1, 0, 0, 0));
        assertFalse(VisitReportRefreshPolicy.notStarted(0, 1, 0, 0));
        assertFalse(VisitReportRefreshPolicy.notStarted(0, 0, 1, 0));
        assertFalse(VisitReportRefreshPolicy.notStarted(0, 0, 0, 1));
    }

    @Test public void firstOpenAndExactHourAreDue() {
        assertTrue(VisitReportRefreshPolicy.due(10000, 0, true));
        assertFalse(VisitReportRefreshPolicy.due(13599, 10000, true));
        assertTrue(VisitReportRefreshPolicy.due(13600, 10000, true));
    }

    @Test public void repeatedOpenUsesTheSamePersistedReceipt() {
        long saved = VisitReportRefreshPolicy.completedAt(0, 10000, 98, true);
        for (long now = 10000; now < 13600; now += 10)
            assertFalse(VisitReportRefreshPolicy.due(now, saved, true));
    }

    @Test public void missingLocalDataAndClockRollbackAllowRefresh() {
        assertTrue(VisitReportRefreshPolicy.due(10001, 10000, false));
        assertTrue(VisitReportRefreshPolicy.due(9999, 10000, true));
    }

    @Test public void receiptsAreScopedToUserAndVisit() {
        assertEquals("256140:1131126040397061639", VisitReportRefreshPolicy.scope(256140, 1131126040397061639L));
        assertNotEquals(VisitReportRefreshPolicy.scope(1, 10), VisitReportRefreshPolicy.scope(2, 10));
        assertNotEquals(VisitReportRefreshPolicy.scope(1, 10), VisitReportRefreshPolicy.scope(1, 11));
    }

    @Test public void fullRequestUsesOnlyThisVisitAndNoIncrementalCursor() {
        JsonObject request = VisitReportRefreshPolicy.request(1131126038653061639L, LocalDate.of(2026, 11, 13));
        assertEquals("report_prepare", request.get("mod").getAsString());
        assertEquals("list_data", request.get("act").getAsString());
        assertEquals("1131126038653061639", request.get("code_dad2").getAsString());
        assertEquals("0", request.get("dt_change_from").getAsString());
        assertEquals("2026-11-13", request.get("date_from").getAsString());
        assertEquals("2026-11-14", request.get("date_to").getAsString());
    }

    @Test public void emptyAndFailedResponsesNeverCreateOrAdvanceReceipt() {
        assertEquals(0, VisitReportRefreshPolicy.completedAt(0, 10000, 0, true));
        assertEquals(5000, VisitReportRefreshPolicy.completedAt(5000, 10000, 0, true));
        assertEquals(0, VisitReportRefreshPolicy.completedAt(0, 10000, 98, false));
        assertEquals(5000, VisitReportRefreshPolicy.completedAt(5000, 10000, 98, false));
    }

    @Test public void successfullyReplayedRowsAlsoRefreshReceipt() {
        assertEquals(10000, VisitReportRefreshPolicy.completedAt(5000, 10000, 98, true));
    }

    @Test(expected = IllegalArgumentException.class) public void invalidVisitCannotProduceRequest() {
        VisitReportRefreshPolicy.request(0, LocalDate.of(2026, 11, 13));
    }
}
