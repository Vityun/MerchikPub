package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.data.synchronization.StartupPolicy;

import static org.junit.Assert.*;

public class VisitDataResponsePolicyTest {
    private static final String EMPTY_LIST = "\u0421\u043f\u0438\u0441\u043e\u043a \u043f\u0443\u0441\u0442";

    @Test public void normalEmptySuccessIsStillAccepted() {
        assertTrue(VisitDataResponsePolicy.requireList(200, true, null, Collections.emptyList()).isEmpty());
    }

    @Test public void legacyEmptyCanOmitStateAndRows() {
        for (Boolean state : new Boolean[]{null, false, true}) {
            assertTrue(VisitDataResponsePolicy.requireList(200, state, EMPTY_LIST, null).isEmpty());
            assertTrue(VisitDataResponsePolicy.requireList(200, state, EMPTY_LIST, Collections.emptyList()).isEmpty());
        }
    }

    @Test public void legacyEmptyAllowsSurroundingWhitespaceOnly() {
        assertTrue(VisitDataResponsePolicy.requireList(200, false, " \n" + EMPTY_LIST + " ", null).isEmpty());
        assertRejected(200, false, EMPTY_LIST + ": access denied", null);
        assertRejected(200, false, "Error: " + EMPTY_LIST, null);
    }

    @Test public void httpErrorsNeverCompleteReadiness() {
        for (int status : new int[]{302, 400, 401, 403, 404, 429, 500, 503}) {
            assertRejected(status, false, EMPTY_LIST, null);
            assertRejected(status, true, null, Collections.emptyList());
        }
    }

    @Test public void missingBodyOrUnexplainedMissingListIsNotAnEmptySuccess() {
        assertRejected(200, null, null, null);
        assertRejected(204, null, null, null);
        assertRejected(200, true, null, null);
        assertRejected(200, false, null, Collections.emptyList());
        assertRejected(200, null, null, Collections.emptyList());
    }

    @Test public void realServerErrorsAreNotSwallowed() {
        for (String error : new String[]{"Unauthorized", "Access denied", "Database error", "Timeout"}) {
            assertRejected(200, false, error, null);
            assertRejected(200, true, error, Collections.emptyList());
        }
    }

    @Test public void contradictoryEmptyErrorWithRowsIsRejected() {
        for (Boolean state : new Boolean[]{null, false, true}) {
            assertRejected(200, state, EMPTY_LIST, Collections.singletonList("row"));
        }
    }

    @Test public void nonemptySuccessReturnsAllRowsUnmodified() {
        List<String> rows = new ArrayList<>(Arrays.asList("one", "two"));
        assertSame(rows, VisitDataResponsePolicy.requireList(200, true, null, rows));
        assertEquals(Arrays.asList("one", "two"), rows);
    }

    @Test public void failedAndMalformedNonemptyResultsRemainErrors() {
        assertRejected(200, false, null, Collections.singletonList("row"));
        assertRejected(200, null, null, Collections.singletonList("row"));
        assertRejected(200, true, null, Arrays.asList("row", null));
    }

    @Test public void emptyReportCanUnlockStartupWithDownloadedDictionariesAndProducts() {
        List<String> rows = VisitDataResponsePolicy.requireList(200, false, EMPTY_LIST, null);
        boolean reportReady = VisitReportDownloadPolicy.loaded(true, rows.size(), 0);
        boolean ready = StartupPolicy.areRequiredTablesReady(true, true, true, true, true, true, reportReady, true);
        assertTrue(ready);
        assertFalse(StartupPolicy.shouldShowLoading(ready, false));
        assertFalse(StartupPolicy.areRequiredTablesReady(true, true, true, true, true, true, reportReady, false));
    }

    @Test public void confirmedEmptyPlanDoesNotRequireAnAssignedVisitForReadiness() {
        List<String> rows = VisitDataResponsePolicy.requireList(200, false, EMPTY_LIST, null);
        assertTrue(VisitReportDownloadPolicy.canUseSavedPlan(true, rows.size()));
        assertFalse(VisitReportDownloadPolicy.canUseSavedPlan(false, rows.size()));
    }

    @Test public void confirmedEmptyNeverCreatesOrAdvancesDownloadWatermarks() {
        List<String> rows = VisitDataResponsePolicy.requireList(200, false, EMPTY_LIST, null);
        for (long previous : new long[]{0, 1000}) {
            assertEquals(previous, VisitReportDownloadPolicy.nextVpi(previous, 3000, rows.size(), true));
            assertEquals(previous, SyncResponsePolicy.downloadWatermark(previous, 3000, rows.size()));
        }
    }

    @Test public void laterNonemptyResultIsDownloadedFromUnchangedWatermark() {
        int emptyCount = VisitDataResponsePolicy.requireList(200, false, EMPTY_LIST, null).size();
        long vpi = VisitReportDownloadPolicy.nextVpi(1000, 3000, emptyCount, true);
        assertEquals(1000, VisitReportDownloadPolicy.requestVpi(true, vpi));
        List<String> rows = VisitDataResponsePolicy.requireList(200, true, null, Collections.singletonList("new RP"));
        assertEquals(3880, VisitReportDownloadPolicy.nextVpi(vpi, 4000, rows.size(), true));
    }

    @Test public void newlyAssignedVisitCannotReuseEmptyPlanCoverage() {
        String emptyScope = VisitReportDownloadPolicy.scope(260523, Collections.emptyMap());
        String assignedScope = VisitReportDownloadPolicy.scope(260523,
                Collections.singletonMap(1181126039410061639L, LocalDate.of(2026, 11, 18)));
        assertNotEquals(emptyScope, assignedScope);
        assertEquals(0, VisitReportDownloadPolicy.requestVpi(assignedScope.equals(emptyScope), 1000));
    }

    @Test public void emptyDeltaKeepsReadinessForExistingLocalRowsWithoutAdvancingVpi() {
        int received = VisitDataResponsePolicy.requireList(200, false, EMPTY_LIST, null).size();
        assertTrue(VisitReportDownloadPolicy.loaded(true, 17, 17));
        assertEquals(1000, VisitReportDownloadPolicy.nextVpi(1000, 3000, received, true));
    }

    @Test public void otherIncrementalDownloadsRetainStrictValidation() {
        try {
            SyncResponsePolicy.requireList(false, EMPTY_LIST, null);
            fail("The legacy exception must stay scoped to visit lists");
        } catch (IllegalStateException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test public void emptyDictionaryDeltaIsAcceptedOnlyWhenLocalDictionaryExists() {
        assertTrue(VisitDataResponsePolicy.requireDictionary(200, true, null, Collections.emptyList(), 4).isEmpty());
        assertTrue(VisitDataResponsePolicy.requireDictionary(200, false, EMPTY_LIST, null, 4).isEmpty());
    }

    @Test public void emptyInitialDictionaryCannotPretendToBeReady() {
        for (boolean legacy : new boolean[]{false, true}) {
            try {
                VisitDataResponsePolicy.requireDictionary(200, !legacy, legacy ? EMPTY_LIST : null,
                        legacy ? null : Collections.emptyList(), 0);
                fail("Missing dictionary was accepted");
            } catch (IllegalStateException expected) {
                assertEquals("Empty initial dictionary", expected.getMessage());
            }
        }
    }

    @Test public void nonemptyInitialDictionaryCanBeSaved() {
        List<String> rows = Collections.singletonList("theme");
        assertSame(rows, VisitDataResponsePolicy.requireDictionary(200, true, null, rows, 0));
    }

    @Test(expected = IllegalStateException.class) public void cachedDictionaryDoesNotTurnAnErrorIntoSuccess() {
        VisitDataResponsePolicy.requireDictionary(200, false, "Access denied", null, 4);
    }

    private static void assertRejected(int status, Boolean state, String error, List<String> rows) {
        try {
            VisitDataResponsePolicy.requireList(status, state, error, rows);
            fail("Unexpected success: HTTP " + status + ", state=" + state + ", error=" + error);
        } catch (IllegalStateException expected) {
            assertNotNull(expected.getMessage());
        }
    }
}
