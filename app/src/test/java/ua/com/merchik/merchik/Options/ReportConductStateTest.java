package ua.com.merchik.merchik.Options;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReportConductStateTest {
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
}
