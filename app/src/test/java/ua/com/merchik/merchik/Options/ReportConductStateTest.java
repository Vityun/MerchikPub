package ua.com.merchik.merchik.Options;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReportConductStateTest {
    @Test(expected = IllegalStateException.class)
    public void inconsistentStartCannotAuthorizeEnd() {
        new ReportConductState().requestWorkEnd(100, 110, null);
    }

    @Test(expected = IllegalStateException.class)
    public void missingClientStartCannotAuthorizeEnd() {
        new ReportConductState().requestWorkEnd(100, 0, null);
    }

    @Test(expected = IllegalStateException.class)
    public void millisecondsCannotBecomeWorkEnd() {
        unfinished(1792346400000L).prepareWorkEnd(1792346400L, true);
    }

    @Test public void repairDoesNotGrantConductConsent() {
        ReportConductState state = new ReportConductState();
        ua.com.merchik.merchik.Utils.WorkTimeRules.Times times =
                ua.com.merchik.merchik.Utils.WorkTimeRules.normalize(100, 100, 0, 200);
        state.visitStart = state.clientStart = times.start;
        state.visitEnd = state.clientEnd = times.end;
        assertFalse(state.canSend(Long.MAX_VALUE));
        assertFalse(state.canApplyWorkEnd(100, 200, 100, 200));
    }

    private final Gson gson = new Gson();

    private ReportConductState restored(ReportConductState state) {
        return gson.fromJson(gson.toJson(state), ReportConductState.class);
    }

    @Test public void workEndAloneDoesNotAuthorizeDelivery() {
        ReportConductState state = new ReportConductState();
        assertTrue(state.awaitingDecision());
        assertFalse(state.canSend(Long.MAX_VALUE));
    }

    @Test public void processRestartDoesNotBypassPenaltyConfirmation() {
        ReportConductState state = new ReportConductState();
        state.stage = ReportConductState.Stage.WAITING_CONFIRMATION;
        ReportConductState loaded = restored(state);
        assertTrue(loaded.awaitingDecision());
        assertFalse(loaded.canSend(Long.MAX_VALUE));
    }

    @Test public void approvedCommandSurvivesRestartWithExactDad2AndAccount() {
        ReportConductState state = new ReportConductState();
        state.userId = 252634;
        state.dad2 = 1021026039574061414L;
        state.visitStart = state.clientStart = 1790854761L;
        state.visitEnd = state.clientEnd = 1790855764L;
        state.approve();
        ReportConductState loaded = restored(state);
        assertEquals(state.dad2, loaded.dad2);
        assertEquals(state.userId, loaded.userId);
        assertEquals(state.revision, loaded.revision);
        assertTrue(loaded.matchesWorkTimes(1790854761L, 1790855764L, 1790854761L, 1790855764L));
        assertTrue(loaded.canSend(0));
        assertFalse(loaded.awaitingDecision());
    }

    @Test public void transientFailureDelaysButDoesNotDiscardDelivery() {
        ReportConductState state = new ReportConductState();
        state.approve();
        state.retryAfter = 60_000L;
        ReportConductState loaded = restored(state);
        assertFalse(loaded.canSend(59_999L));
        assertTrue(loaded.canSend(60_000L));
    }

    @Test public void serverAcceptanceStopsDeliveryWithoutWaitingForDocumentCompletion() {
        ReportConductState state = new ReportConductState();
        state.approve();
        state.complete(true, "Command accepted; report processing is pending");
        ReportConductState loaded = restored(state);
        assertEquals(ReportConductState.Stage.ACCEPTED, loaded.stage);
        assertFalse(loaded.canSend(Long.MAX_VALUE));
        assertTrue(loaded.resultPending);
    }

    @Test public void explicitRejectionIsNotRetriedAutomatically() {
        ReportConductState state = new ReportConductState();
        state.approve();
        state.complete(false, "Blocking option");
        ReportConductState loaded = restored(state);
        assertEquals(ReportConductState.Stage.REJECTED, loaded.stage);
        assertFalse(loaded.canSend(Long.MAX_VALUE));
        assertFalse(loaded.awaitingDecision());
        assertTrue(loaded.resultPending);
    }

    @Test public void newDecisionHasNewRevisionForLateCallbackProtection() {
        assertNotEquals(new ReportConductState().revision, new ReportConductState().revision);
    }

    @Test public void changedWorkEndRequiresNewChecks() {
        ReportConductState state = new ReportConductState();
        state.visitStart = 100;
        state.clientStart = 100;
        state.visitEnd = 200;
        state.clientEnd = 200;
        state.approve();
        assertTrue(state.matchesWorkTimes(100, 200, 100, 200));
        assertFalse(state.matchesWorkTimes(100, 201, 100, 200));
        assertFalse(state.matchesWorkTimes(100, 200, 100, 201));
        assertFalse(state.matchesWorkTimes(101, 200, 100, 200));
        assertFalse(state.matchesWorkTimes(100, 200, 101, 200));
    }

    @Test public void uploadReceiptSurvivesRestartIndependentlyOfWorkPlanUploadFlag() {
        ReportConductState state = new ReportConductState();
        state.approve();
        state.workDataUploaded = true;
        ReportConductState loaded = restored(state);
        assertTrue(loaded.workDataUploaded);
        assertTrue(loaded.canSend(0));
    }

    private ReportConductState unfinished(Long requestedEnd) {
        ReportConductState state = new ReportConductState();
        state.userId = 216646;
        state.dad2 = 1141026037940061377L;
        state.requestWorkEnd(100, 100, requestedEnd);
        return state;
    }

    @Test public void waitingForPenaltyConsentCannotSaveOrSendWorkEnd() {
        ReportConductState state = unfinished(null);
        state.stage = ReportConductState.Stage.WAITING_CONFIRMATION;
        ReportConductState loaded = restored(state);
        assertTrue(loaded.workEndRequested);
        assertTrue(loaded.awaitingDecision());
        assertEquals(0, loaded.visitEnd);
        assertEquals(0, loaded.clientEnd);
        assertFalse(loaded.canApplyWorkEnd(100, 0, 100, 0));
        assertFalse(loaded.canSend(Long.MAX_VALUE));
    }

    @Test public void previewDoesNotAuthorizeCompletionOrFreezeCurrentTime() {
        ReportConductState state = unfinished(null);
        assertEquals(200, state.resolveWorkEnd(200));
        assertEquals(0, state.visitEnd);
        state.prepareWorkEnd(250, true);
        assertEquals(250, state.visitEnd);
        assertEquals(250, state.clientEnd);
    }

    @Test public void explicitDurationSurvivesBackgroundAndUsesCalculatedEnd() {
        ReportConductState state = unfinished(700L);
        state.stage = ReportConductState.Stage.WAITING_CONFIRMATION;
        ReportConductState loaded = restored(state);
        assertEquals(Long.valueOf(700), loaded.requestedWorkEnd);
        loaded.prepareWorkEnd(1000, true);
        assertEquals(700, loaded.visitEnd);
    }

    @Test public void consentSurvivesCrashBeforeRealmCommitButCannotSendYet() {
        ReportConductState state = unfinished(null);
        state.prepareWorkEnd(250, true);
        ReportConductState loaded = restored(state);
        assertEquals(ReportConductState.Stage.FINALIZING_WORK_END, loaded.stage);
        assertTrue(loaded.canApplyWorkEnd(100, 0, 100, 0));
        assertFalse(loaded.awaitingDecision());
        assertFalse(loaded.canSend(Long.MAX_VALUE));
        loaded.workEndSaved();
        assertFalse(loaded.workEndRequested);
        assertTrue(loaded.canSend(0));
        assertFalse(loaded.workDataUploaded);
    }

    @Test public void recoveryAfterRealmCommitIsIdempotent() {
        ReportConductState state = unfinished(null);
        state.prepareWorkEnd(250, true);
        ReportConductState loaded = restored(state);
        assertTrue(loaded.canApplyWorkEnd(100, 250, 100, 250));
        loaded.workEndSaved();
        ReportConductState ready = restored(loaded);
        assertTrue(ready.matchesWorkTimes(100, 250, 100, 250));
        assertEquals(ReportConductState.Stage.READY, ready.stage);
        assertFalse(ready.canApplyWorkEnd(100, 250, 100, 250));
    }

    @Test public void recoveryCannotOverwriteChangedStartOrOtherCompletion() {
        ReportConductState state = unfinished(null);
        state.prepareWorkEnd(250, true);
        assertFalse(state.canApplyWorkEnd(101, 0, 100, 0));
        assertFalse(state.canApplyWorkEnd(100, 0, 101, 0));
        assertFalse(state.canApplyWorkEnd(100, 260, 100, 260));
        assertFalse(state.canApplyWorkEnd(100, 250, 100, 0));
    }

    @Test public void noPenaltiesWithPhotoWarningStillNeedsSeparateConductConsent() {
        ReportConductState state = unfinished(null);
        state.prepareWorkEnd(250, false);
        ReportConductState loaded = restored(state);
        loaded.workEndSaved();
        assertFalse(loaded.workEndRequested);
        assertTrue(loaded.awaitingDecision());
        assertFalse(loaded.canSend(Long.MAX_VALUE));
        assertEquals(250, loaded.visitEnd);
        loaded.approve();
        assertTrue(loaded.canSend(0));
    }

    @Test public void legacyStateDoesNotInventWorkEndIntent() {
        ReportConductState legacy = gson.fromJson("{\"stage\":\"WAITING_CONFIRMATION\","
                + "\"visitStart\":100,\"visitEnd\":250,\"clientStart\":100,\"clientEnd\":250}",
                ReportConductState.class);
        assertFalse(legacy.workEndRequested);
        legacy.approve();
        assertEquals(250, legacy.visitEnd);
        assertTrue(legacy.canSend(0));
    }

    @Test(expected = IllegalStateException.class)
    public void repeatedConsentCannotChangeFinalTimestamp() {
        ReportConductState state = unfinished(null);
        state.prepareWorkEnd(250, true);
        state.prepareWorkEnd(300, true);
    }

    @Test(expected = IllegalStateException.class)
    public void pendingWorkEndCannotBecomeReadyWithoutCommit() {
        unfinished(null).approve();
    }

    @Test(expected = IllegalStateException.class)
    public void waitingDecisionCannotPretendThatWorkEndWasSaved() {
        unfinished(null).workEndSaved();
    }

    @Test(expected = IllegalStateException.class)
    public void invalidEndDoesNotAuthorizeNegativeDuration() {
        unfinished(50L).prepareWorkEnd(250, true);
    }
}
