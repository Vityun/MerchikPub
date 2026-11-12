package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Options.ReportConductState;
import ua.com.merchik.merchik.data.QuestionAnswerDB;
import ua.com.merchik.merchik.data.UploadToServ.QuestionAnswerUpload;
import ua.com.merchik.merchik.data.UploadToServ.QuestionAnswerUploadResponse;

import static org.junit.Assert.*;

public class QuestionAnswerAckPolicyTest {
    private static final long FIRST = 1793625964568L;
    private static final long SECOND = FIRST + 1;
    private static final long THIRD = FIRST + 2;
    private static final long BASE = 10_000_000L;

    @Test public void localComplaintWaitsForAcknowledgement() {
        assertEquals(Collections.singletonList(FIRST), pending(BASE, answer(FIRST, 610)));
        assertTrue(pending(FIRST, answer(FIRST, 610)).isEmpty());
    }

    @Test public void downloadedComplaintDoesNotNeedAnotherUpload() {
        assertTrue(pending(BASE, answer(53539, 610)).isEmpty());
        assertTrue(QuestionAnswerAckPolicy.isConfirmed(20_000_000, BASE));
    }

    @Test public void oneConfirmedQualifyingAnswerIsEnough() {
        assertTrue(pending(BASE, answer(FIRST, 610), answer(53539, 6)).isEmpty());
        assertTrue(pending(FIRST, answer(SECOND, 612), answer(FIRST, 607)).isEmpty());
    }

    @Test public void unrelatedPendingAnswersDoNotHoldAReport() {
        assertTrue(QuestionAnswerAckPolicy.pendingReleasingIds(Collections.emptyList(), BASE).isEmpty());
    }

    @Test public void temporaryNoComplaintsExceptionAlsoWaitsForAck() {
        assertEquals(Collections.singletonList(FIRST), pending(BASE, answer(FIRST, 600)));
        assertTrue(pending(FIRST, answer(FIRST, 600)).isEmpty());
    }

    @Test public void actualServerErrorDoesNotAcknowledgeListedElement() {
        QuestionAnswerUploadResponse response = response("{\"state\":false,\"error\":\"Cannot convert 70.00 to int\","
                + "\"list\":[{\"element_id\":\"" + FIRST + "\"}]}");
        long after = QuestionAnswerAckPolicy.acknowledgedThrough(BASE, sent(FIRST), response);
        assertEquals(BASE, after);
        assertEquals(Collections.singletonList(FIRST), pending(after, answer(FIRST, 610)));
    }

    @Test public void successAcknowledgesOnlyReturnedRequestIds() {
        QuestionAnswerUploadResponse response = response("{\"state\":true,\"list\":["
                + "{\"element_id\":\"" + SECOND + "\",\"id\":\"123\"},"
                + "{\"element_id\":\"" + FIRST + "\",\"id\":\"122\"},"
                + "{\"element_id\":\"" + THIRD + "\",\"id\":\"124\"}]}");
        assertEquals(SECOND, QuestionAnswerAckPolicy.acknowledgedThrough(BASE, sent(FIRST, SECOND), response));
    }

    @Test public void partialAckCannotJumpOverMissingRow() {
        QuestionAnswerUploadResponse response = response("{\"state\":true,\"list\":["
                + "{\"element_id\":\"" + FIRST + "\"},{\"element_id\":\"" + THIRD + "\"}]}");
        assertEquals(FIRST, QuestionAnswerAckPolicy.acknowledgedThrough(BASE, sent(FIRST, SECOND, THIRD), response));
    }

    @Test public void perRowRejectionOrErrorPreventsAcknowledgement() {
        for (String failure : Arrays.asList("\"state\":false", "\"error\":\"failed\"")) {
            QuestionAnswerUploadResponse response = response("{\"state\":true,\"list\":["
                    + "{\"element_id\":\"" + FIRST + "\"," + failure + "}]}");
            assertEquals(BASE, QuestionAnswerAckPolicy.acknowledgedThrough(BASE, sent(FIRST), response));
        }
    }

    @Test public void contradictoryRowsAreNotAccepted() {
        QuestionAnswerUploadResponse response = response("{\"state\":true,\"list\":["
                + "{\"element_id\":\"" + FIRST + "\"},"
                + "{\"element_id\":\"" + FIRST + "\",\"state\":false}]}");
        assertEquals(BASE, QuestionAnswerAckPolicy.acknowledgedThrough(BASE, sent(FIRST), response));
    }

    @Test public void emptyOrMissingResponseLeavesCheckpointUnchanged() {
        for (QuestionAnswerUploadResponse response : Arrays.asList(null, response("{}"),
                response("{\"state\":true}"), response("{\"state\":true,\"list\":[]}"))) {
            assertEquals(BASE, QuestionAnswerAckPolicy.acknowledgedThrough(BASE, sent(FIRST), response));
        }
    }

    @Test public void oldUploadCheckpointIsPreservedAndDownloadVpiIsNotAnAck() {
        assertEquals(FIRST, QuestionAnswerAckPolicy.uploadedThrough(FIRST, 0));
        assertEquals(SECOND, QuestionAnswerAckPolicy.uploadedThrough(FIRST, SECOND));
        assertEquals(FIRST, QuestionAnswerAckPolicy.uploadedThrough(1793625964, FIRST));
        assertEquals(BASE, QuestionAnswerAckPolicy.uploadedThrough(1793625964, 0));
    }

    @Test public void failedThenSuccessfulRetryReleasesTheSameComplaint() {
        List<QuestionAnswerUpload> sent = sent(FIRST);
        long checkpoint = QuestionAnswerAckPolicy.acknowledgedThrough(BASE, sent, response("{\"state\":false}"));
        assertFalse(pending(checkpoint, answer(FIRST, 610)).isEmpty());
        checkpoint = QuestionAnswerAckPolicy.acknowledgedThrough(checkpoint, sent,
                response("{\"state\":true,\"list\":[{\"element_id\":\"" + FIRST + "\"}]}"));
        assertTrue(pending(checkpoint, answer(FIRST, 610)).isEmpty());
    }

    @Test public void conductStaysReadyAcrossRestartUntilComplaintIsAcknowledged() {
        ReportConductState command = new ReportConductState();
        command.dad2 = 1121126039896061547L;
        command.approve();
        long checkpoint = QuestionAnswerAckPolicy.acknowledgedThrough(BASE, sent(FIRST),
                response("{\"state\":false,\"list\":[{\"element_id\":\"" + FIRST + "\"}]}"));
        assertTrue(command.canSend(0));
        assertFalse(pending(checkpoint, answer(FIRST, 610)).isEmpty());

        ReportConductState restored = new Gson().fromJson(new Gson().toJson(command), ReportConductState.class);
        assertEquals(ReportConductState.Stage.READY, restored.stage);
        assertEquals(command.revision, restored.revision);
        assertFalse(restored.resultPending);
        assertFalse(pending(checkpoint, answer(FIRST, 610)).isEmpty());

        checkpoint = QuestionAnswerAckPolicy.acknowledgedThrough(checkpoint, sent(FIRST),
                response("{\"state\":true,\"list\":[{\"element_id\":\"" + FIRST + "\"}]}"));
        assertTrue(restored.canSend(0));
        assertTrue(pending(checkpoint, answer(FIRST, 610)).isEmpty());
    }

    private static List<Long> pending(long checkpoint, QuestionAnswerDB... answers) {
        return QuestionAnswerAckPolicy.pendingReleasingIds(Arrays.asList(answers), checkpoint);
    }

    private static QuestionAnswerDB answer(long id, int theme) {
        QuestionAnswerDB row = new QuestionAnswerDB();
        row.setId(id);
        row.setIdQuest(theme);
        return row;
    }

    private static List<QuestionAnswerUpload> sent(Long... ids) {
        java.util.ArrayList<QuestionAnswerUpload> rows = new java.util.ArrayList<>();
        for (Long id : ids) rows.add(QuestionAnswerUpload.fromDb(answer(id, 610)));
        return rows;
    }

    private static QuestionAnswerUploadResponse response(String json) {
        return new Gson().fromJson(json, QuestionAnswerUploadResponse.class);
    }
}
