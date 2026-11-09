package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.JsonObject;
import org.junit.Test;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import static org.junit.Assert.*;

public class VisitReportDownloadPolicyTest {
    private static final long DAD2 = 1091126039410061639L;

    private static Map<Long, LocalDate> visits(long... ids) {
        Map<Long, LocalDate> dates = new LinkedHashMap<>();
        for (long id : ids) dates.put(id, LocalDate.of(2026, 11, 9));
        return dates;
    }

    @Test public void oneOpenedVisitDoesNotCompleteInitialDownload() {
        assertFalse(VisitReportDownloadPolicy.loaded(false, -1, 119));
        assertEquals(0, VisitReportDownloadPolicy.requestVpi(false, 1794230132));
    }

    @Test public void committedCoverageSurvivesRestart() {
        assertTrue(VisitReportDownloadPolicy.loaded(true, 128, 128));
        assertEquals(1794230132, VisitReportDownloadPolicy.requestVpi(true, 1794230132));
    }

    @Test public void lostRowsRequireRecovery() {
        assertFalse(VisitReportDownloadPolicy.loaded(true, 128, 8));
    }

    @Test public void localAdditionsDoNotInvalidateCoverage() {
        assertTrue(VisitReportDownloadPolicy.loaded(true, 128, 130));
    }

    @Test public void emptySuccessfulResultIsReadyWithoutVpi() {
        assertTrue(VisitReportDownloadPolicy.loaded(true, 0, 0));
        assertFalse(VisitReportDownloadPolicy.loaded(false, 0, 0));
        assertEquals(0, VisitReportDownloadPolicy.nextVpi(0, 1794230132, 0, true));
    }

    @Test public void emptyDeltaPreservesExistingVpi() {
        assertEquals(1000, VisitReportDownloadPolicy.nextVpi(1000, 3000, 0, true));
    }

    @Test public void failedOrDeferredMergeDoesNotAdvanceVpi() {
        assertEquals(1000, VisitReportDownloadPolicy.nextVpi(1000, 3000, 119, false));
        assertEquals(0, VisitReportDownloadPolicy.nextVpi(0, 3000, 119, false));
    }

    @Test public void localPendingRowsCanBeReadyWithoutAdvancingDownloadVpi() {
        assertTrue(VisitReportDownloadPolicy.loaded(true, 119, 119));
        assertEquals(1000, VisitReportDownloadPolicy.nextVpi(1000, 3000, 119, false));
    }

    @Test public void nonemptyProcessedDeltaUsesRequestStartAndOverlap() {
        assertEquals(2880, VisitReportDownloadPolicy.nextVpi(1000, 3000, 119, true));
    }

    @Test public void replayedRowsStillAllowVpiAdvance() {
        assertEquals(2880, VisitReportDownloadPolicy.nextVpi(1000, 3000, 1, true));
    }

    @Test public void clockMovingBackwardsDoesNotRegressVpi() {
        assertEquals(1000, VisitReportDownloadPolicy.nextVpi(1000, 900, 1, true));
    }

    @Test public void scopeIgnoresOrderingAndDuplicates() {
        assertEquals(VisitReportDownloadPolicy.scope(176053, visits(DAD2, 42L)),
                VisitReportDownloadPolicy.scope(176053, visits(42L, DAD2, 42L)));
    }

    @Test public void newVisitsAndOtherUsersCannotReuseCompletion() {
        String first = VisitReportDownloadPolicy.scope(176053, visits(DAD2));
        assertNotEquals(first, VisitReportDownloadPolicy.scope(176053, visits(DAD2, 42L)));
        assertNotEquals(first, VisitReportDownloadPolicy.scope(176054, visits(DAD2)));
    }

    @Test public void longDad2IsNeverRoundedInScopeOrRequest() {
        assertNotEquals(VisitReportDownloadPolicy.scope(1, visits(DAD2)),
                VisitReportDownloadPolicy.scope(1, visits(DAD2 + 1)));
        assertEquals(DAD2, VisitReportDownloadPolicy.request(true, visits(DAD2), 0)
                .getAsJsonArray("code_dad2").get(0).getAsLong());
    }

    @Test public void rpRequestUsesSecondsAndExplicitPlannedDates() {
        JsonObject request = VisitReportDownloadPolicy.request(true, visits(DAD2, 42L), 1794230132);
        assertEquals("report_prepare", request.get("mod").getAsString());
        assertEquals("list_data", request.get("act").getAsString());
        assertEquals("1794230132", request.get("dt_change_from").getAsString());
        assertEquals(2, request.getAsJsonArray("code_dad2").size());
        assertEquals("2026-11-09", request.get("date_from").getAsString());
        assertEquals("2026-11-10", request.get("date_to").getAsString());
    }

    @Test public void productsUseTheirOwnDtFilter() {
        JsonObject request = VisitReportDownloadPolicy.request(false, visits(DAD2), 1234);
        assertEquals("tovar_list", request.get("act").getAsString());
        assertEquals("1234", request.get("dt").getAsString());
        assertFalse(request.has("dt_change_from"));
        assertFalse(request.has("date_from"));
        assertFalse(request.has("date_to"));
    }

    @Test public void retriesWaitThirtySecondsButRestartCanRetryImmediately() {
        assertTrue(VisitReportDownloadPolicy.retryDue(10, -1));
        assertFalse(VisitReportDownloadPolicy.retryDue(30009, 10));
        assertTrue(VisitReportDownloadPolicy.retryDue(30010, 10));
    }

    @Test public void futureVisitsAreCoveredThroughTheWholeLastPlannedDay() {
        Map<Long, LocalDate> dates = visits(DAD2);
        dates.put(1111126039410061674L, LocalDate.of(2026, 11, 11));
        dates.put(1131126039410061639L, LocalDate.of(2026, 11, 13));
        JsonObject request = VisitReportDownloadPolicy.request(true, dates, 0);
        assertEquals("2026-11-09", request.get("date_from").getAsString());
        assertEquals("2026-11-14", request.get("date_to").getAsString());
        assertEquals(3, request.getAsJsonArray("code_dad2").size());
        assertEquals(1111126039410061674L, request.getAsJsonArray("code_dad2").get(1).getAsLong());
    }

    @Test public void datesComeFromWpDataNotDad2OrTodaysFixedWindow() {
        Map<Long, LocalDate> dates = visits(DAD2);
        dates.put(DAD2, LocalDate.of(2026, 9, 1));
        dates.put(42L, LocalDate.of(2027, 1, 31));
        JsonObject request = VisitReportDownloadPolicy.request(true, dates, 0);
        assertEquals("2026-09-01", request.get("date_from").getAsString());
        assertEquals("2027-02-01", request.get("date_to").getAsString());
    }

    @Test public void dateConversionUsesKievRegardlessOfDeviceTimeZone() {
        TimeZone previous = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
            assertEquals(LocalDate.of(2026, 11, 11), VisitReportDownloadPolicy.visitDate(
                    Date.from(Instant.parse("2026-11-10T22:00:00Z"))));
            assertEquals(LocalDate.of(2026, 10, 19), VisitReportDownloadPolicy.visitDate(
                    Date.from(Instant.parse("2026-10-18T21:00:00Z"))));
        } finally { TimeZone.setDefault(previous); }
    }

    @Test public void correctedPlannedDateInvalidatesOldCoverage() {
        Map<Long, LocalDate> before = visits(DAD2);
        Map<Long, LocalDate> after = visits(DAD2);
        after.put(DAD2, LocalDate.of(2026, 11, 11));
        assertNotEquals(VisitReportDownloadPolicy.scope(176053, before),
                VisitReportDownloadPolicy.scope(176053, after));
    }

    @Test public void undatedCompletionCannotReusePreviouslyAdvancedVpi() {
        String legacyScope = "176053:fe8f3384df162baf2f3439e0f321b969aaf9e4b3b0abea7b17663742b4e4b084";
        String scope = VisitReportDownloadPolicy.scope(176053, visits(DAD2));
        assertTrue(scope.startsWith("176053:dated-v2:"));
        assertNotEquals(legacyScope, scope);
        long from = VisitReportDownloadPolicy.requestVpi(scope.equals(legacyScope), 1794234039L);
        assertEquals("0", VisitReportDownloadPolicy.request(true, visits(DAD2), from)
                .get("dt_change_from").getAsString());
        assertEquals(1794234039L, VisitReportDownloadPolicy.nextVpi(1794234039L, 1794240000L, 0, true));
    }

    @Test public void cachedPlanCanBeUsedEvenWithoutSuccessfulInitializationFlag() {
        assertTrue(VisitReportDownloadPolicy.canUseSavedPlan(false, 120));
        assertTrue(VisitReportDownloadPolicy.canUseSavedPlan(true, 120));
    }

    @Test public void failedFirstPlanWithoutAnySavedVisitsCannotStartUnscopedDownload() {
        assertFalse(VisitReportDownloadPolicy.canUseSavedPlan(false, 0));
    }

    @Test public void successfullyLoadedEmptyPlanCanCompleteWithoutRequests() {
        assertTrue(VisitReportDownloadPolicy.canUseSavedPlan(true, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyScopeCannotCreateAnUnfilteredRequest() {
        VisitReportDownloadPolicy.request(true, Collections.emptyMap(), 0);
    }

    @Test public void productDeltaUsesOwnVpiEvenIfReportDownloadFailed() {
        assertEquals(1000, VisitReportDownloadPolicy.nextVpi(1000, 3000, 0, false));
        assertEquals(2880, VisitReportDownloadPolicy.nextVpi(900, 3000, 860, true));
    }
}
