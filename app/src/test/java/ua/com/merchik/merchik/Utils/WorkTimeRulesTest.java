package ua.com.merchik.merchik.Utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class WorkTimeRulesTest {
    private WorkTimeRules.Times times(long start, long end) {
        return WorkTimeRules.normalize(start, start, end, end);
    }

    @Test public void zeroPairsStayZero() {
        assertTrue(times(0, 0).matches(0, 0, 0, 0));
    }

    @Test public void missingClientTimesAreCopiedFromVisit() {
        assertTrue(WorkTimeRules.normalize(100, 0, 200, 0).matches(100, 100, 200, 200));
    }

    @Test public void missingVisitTimesAreCopiedFromClient() {
        assertTrue(WorkTimeRules.normalize(0, 100, 0, 200).matches(100, 100, 200, 200));
    }

    @Test public void conflictingPositiveTimesPreferVisit() {
        assertTrue(WorkTimeRules.normalize(100, 110, 200, 210).matches(100, 100, 200, 200));
    }

    @Test public void realBrokenVisitRecoversRecordedEndNotNewDuration() {
        WorkTimeRules.Times t = WorkTimeRules.normalize(1792344750L, 1792344750L, 0, 1792346400L);
        assertEquals(1792346400L, t.end);
        assertEquals(1650, t.duration());
    }

    @Test public void millisecondsBecomeSeconds() {
        assertTrue(WorkTimeRules.normalize(1792344750000L, 1792344750L, 1792346400000L, 0)
                .matches(1792344750L, 1792344750L, 1792346400L, 1792346400L));
    }

    @Test public void consistentRejectsRawMillisecondsAndHalfPairs() {
        assertFalse(WorkTimeRules.consistent(100, 0, 0, 0));
        assertFalse(WorkTimeRules.consistent(100, 100, 0, 200));
        assertFalse(WorkTimeRules.consistent(1792344750000L, 1792344750000L, 0, 0));
        assertTrue(WorkTimeRules.consistent(100, 100, 200, 200));
    }

    @Test(expected = IllegalArgumentException.class) public void negativeIsInvalid() {
        WorkTimeRules.normalize(-1, 100, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class) public void noInventedStart() {
        WorkTimeRules.normalize(0, 0, 200, 200);
    }

    @Test(expected = IllegalArgumentException.class) public void endBeforeStartIsInvalid() {
        WorkTimeRules.normalize(100, 100, 99, 99);
    }

    @Test public void pendingLocalCompletionSurvivesServerReply() {
        assertSameMerge(times(100, 200), times(100, 0), true, false, false, false, 100, 200);
    }

    @Test public void pendingLocalStartIsNotFinishedByStaleReply() {
        assertSameMerge(times(100, 0), times(100, 200), true, false, false, false, 100, 0);
    }

    @Test public void olderServerCannotReplacePairedLocalTimes() {
        assertSameMerge(times(100, 200), times(110, 210), false, true, false, false, 100, 200);
    }

    @Test public void serverZerosCannotResetFinishedWork() {
        assertSameMerge(times(100, 200), times(0, 0), false, false, false, false, 100, 200);
    }

    @Test public void contradictoryServerPairsCannotReplacePairedLocalTimes() {
        assertSameMerge(times(100, 200), times(110, 210), false, false, true, true, 100, 200);
    }

    @Test public void freshConsistentServerCorrectionIsAccepted() {
        assertSameMerge(times(100, 200), times(110, 210), false, false, false, false, 110, 210);
    }

    @Test public void newVisitTakesNormalizedServerTimes() {
        assertSameMerge(null, times(100, 200), false, false, true, true, 100, 200);
    }

    @Test public void serverCompletionFillsEmptyLocalEnd() {
        assertSameMerge(times(100, 0), times(100, 200), false, false, false, true, 100, 200);
    }

    @Test public void mixingChronologicallyIncompatiblePairsKeepsLocal() {
        assertSameMerge(times(100, 200), times(250, 0), false, false, false, false, 100, 200);
    }

    private void assertSameMerge(WorkTimeRules.Times local, WorkTimeRules.Times server,
                                boolean dirty, boolean older, boolean startConflict, boolean endConflict,
                                long start, long end) {
        assertTrue(WorkTimeRules.merge(local, server, dirty, older, startConflict, endConflict)
                .matches(start, start, end, end));
    }
}
