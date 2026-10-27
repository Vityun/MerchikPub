package ua.com.merchik.merchik.Options;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReportConductStateTest {
    @Test public void unfinishedVisitWithEitherOperatorCanFinishWithoutConduct() {
        assertTrue(ReportConductState.canFinishWithoutConduct(0, 0, 123, 0));
        assertTrue(ReportConductState.canFinishWithoutConduct(0, 0, 0, 123));
        assertTrue(ReportConductState.canFinishWithoutConduct(0, 0, 123, 456));
    }

    @Test public void missingOperatorsHideFinishWithoutConduct() {
        assertFalse(ReportConductState.canFinishWithoutConduct(0, 0, 0, 0));
        assertFalse(ReportConductState.canFinishWithoutConduct(0, 0, -1, 0));
        assertFalse(ReportConductState.canFinishWithoutConduct(0, 0, 0, -1));
    }

    @Test public void eitherSavedEndHidesFinishWithoutConduct() {
        assertFalse(ReportConductState.canFinishWithoutConduct(200, 200, 123, 456));
        assertFalse(ReportConductState.canFinishWithoutConduct(200, 0, 123, 456));
        assertFalse(ReportConductState.canFinishWithoutConduct(0, 200, 123, 456));
    }

    @Test public void finishWithoutConductSavesMatchingTimesAndEndsDecision() {
        ReportConductState state = unfinished(null);
        state.prepareWorkEndWithoutConduct(250);
        assertFalse(state.canSend(Long.MAX_VALUE));
        state.workEndSaved();
        assertEquals(250, state.visitEnd);
        assertEquals(250, state.clientEnd);
        assertEquals(ReportConductState.Stage.FINISHED_WITHOUT_CONDUCT, state.stage);
        assertFalse(state.workEndRequested);
        assertFalse(state.conductAfterWorkEnd);
        assertFalse(state.awaitingDecision());
        assertFalse(state.canSend(Long.MAX_VALUE));
        assertFalse(state.resultPending);
    }

    @Test public void finishWithoutConductRetainsPreviouslyEnteredDuration() {
        ReportConductState state = unfinished(2080L);
        state.stage = ReportConductState.Stage.WAITING_CONFIRMATION;
        state.prepareWorkEndWithoutConduct(5000);
        state.workEndSaved();
        assertEquals(2080, state.visitEnd);
        assertEquals(2080, state.clientEnd);
    }

    @Test public void finishWithoutConductRecoversCrashBeforeCommitWithoutAuthorizingCommand() {
        ReportConductState state = unfinished(null);
        state.prepareWorkEndWithoutConduct(250);
        ReportConductState loaded = restored(state);
        assertTrue(loaded.finishWithoutConduct);
        assertTrue(loaded.canApplyWorkEnd(100, 0, 100, 0));
        assertFalse(loaded.canSend(Long.MAX_VALUE));
        loaded.workEndSaved();
        assertFalse(loaded.canSend(Long.MAX_VALUE));
        assertFalse(loaded.awaitingDecision());
    }

    @Test public void finishWithoutConductRecoversCrashAfterRealmCommitIdempotently() {
        ReportConductState state = unfinished(250L);
        state.prepareWorkEndWithoutConduct(1000);
        ReportConductState loaded = restored(state);
        assertTrue(loaded.canApplyWorkEnd(100, 250, 100, 250));
        assertFalse(loaded.canApplyWorkEnd(100, 260, 100, 260));
        loaded.workEndSaved();
        ReportConductState finished = restored(loaded);
        assertEquals(ReportConductState.Stage.FINISHED_WITHOUT_CONDUCT, finished.stage);
        assertFalse(finished.awaitingDecision());
        assertFalse(finished.canSend(Long.MAX_VALUE));
        assertFalse(finished.canApplyWorkEnd(100, 250, 100, 250));
    }

    @Test(expected = IllegalStateException.class)
    public void finishedWithoutConductCannotBeApprovedByOldCallback() {
        ReportConductState state = unfinished(null);
        state.prepareWorkEndWithoutConduct(250);
        state.workEndSaved();
        state.approve();
    }

    @Test(expected = IllegalStateException.class)
    public void repeatedFinishWithoutConductDoesNotChangeEnd() {
        ReportConductState state = unfinished(null);
        state.prepareWorkEndWithoutConduct(250);
        state.prepareWorkEndWithoutConduct(300);
    }

    @Test public void explicitNewDecisionCanConductPreviouslyFinishedVisit() {
        ReportConductState finished = unfinished(null);
        finished.prepareWorkEndWithoutConduct(250);
        finished.workEndSaved();
        ReportConductState next = new ReportConductState();
        next.dad2 = finished.dad2;
        next.visitStart = next.clientStart = 100;
        next.visitEnd = next.clientEnd = 250;
        next.approve();
        assertNotEquals(finished.revision, next.revision);
        assertTrue(next.canSend(0));
        assertFalse(finished.canSend(Long.MAX_VALUE));
    }

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

    @Test public void formerReplayVisitKeepsItsSavedEndWhenOldDecisionResumes() {
        long start = 1792936745L;
        long savedEnd = 1792940072L;
        ReportConductState state = new ReportConductState();
        state.dad2 = 1261026024655061457L;
        state.requestWorkEnd(start, start, start + 3600);
        assertFalse(state.matchesUnfinishedWork(start, savedEnd, start, savedEnd));
        state.prepareWorkEnd(start + 7200, true);
        ReportConductState loaded = restored(state);
        assertFalse(loaded.canApplyWorkEnd(start, savedEnd, start, savedEnd));
        assertFalse(loaded.canSend(Long.MAX_VALUE));
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
