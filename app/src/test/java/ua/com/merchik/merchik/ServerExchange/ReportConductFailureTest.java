package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReportConductFailureTest {
    private static final String DOCUMENT = "\u0410\u041e\u0438-03212690";
    private static final String PREFIX = "\u041f\u0440\u043e\u0432\u0435\u0441\u0442\u0438 \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442 \u041e\u0442\u0447\u0435\u0442\u0418\u0441\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044f ";
    private static final String FAILED = " \u043d\u0435 \u0432\u0434\u0430\u043b\u043e\u0441\u044c!";
    private static final String MESSAGE = PREFIX + DOCUMENT + FAILED
            + "  \u041c\u043e\u0436\u043b\u0438\u0432\u043e \u043d\u0435 \u0432\u0438\u043a\u043e\u043d\u0430\u043d\u0456 \u0431\u043b\u043e\u043a\u0443\u044e\u0447\u0456 \u043e\u043f\u0446\u0456\u0457.";

    private static ReportConductFailure parse(String message) {
        return ReportConductFailure.parse(176053, 14214942, 205909, 176053, 1789645922L, message);
    }

    @Test public void recognizesActualReceivedMessage() {
        ReportConductFailure failure = parse(MESSAGE);
        assertNotNull(failure);
        assertEquals(DOCUMENT, failure.document);
        assertEquals(14214942, failure.messageId);
        assertEquals(1789645922L, failure.messageTime);
        assertEquals(0, failure.dad2);
        assertEquals("176053:14214942", failure.key());
    }

    @Test public void recognizesAlternateFailureSpellings() {
        assertNotNull(parse(PREFIX + DOCUMENT + " \u043d\u0435 \u0432\u0434\u0430\u043b\u043e\u0441\u044f!"));
        assertNotNull(parse(PREFIX + DOCUMENT + " \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c."));
    }

    @Test public void whitespaceAndNewlinesAreAccepted() {
        assertNotNull(parse("\n" + MESSAGE.replace(' ', '\u00a0') + "\n"));
        assertNotNull(parse(MESSAGE.replace(" " + DOCUMENT, "\n" + DOCUMENT)));
    }

    @Test public void nativeDocumentLinkRetainsExactDad2() {
        ReportConductFailure failure = parse(PREFIX + "{\u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442|code_dad2:1161126028847061680|" + DOCUMENT + "}" + FAILED);
        assertNotNull(failure);
        assertEquals(1161126028847061680L, failure.dad2);
        assertEquals(DOCUMENT, failure.document);
    }

    @Test public void unrelatedDocumentLinkDoesNotChangeTarget() {
        ReportConductFailure failure = parse(MESSAGE + " {\u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442|123|\u0410\u041e\u0438-99999999}");
        assertNotNull(failure);
        assertEquals(DOCUMENT, failure.document);
        assertEquals(0, failure.dad2);
    }

    @Test public void otherRecipientAndOutgoingMessagesDoNotTriggerRecovery() {
        assertNull(ReportConductFailure.parse(7, 14214942, 205909, 176053, 1789645922L, MESSAGE));
        assertNull(ReportConductFailure.parse(176053, 14214942, 176053, 176053, 1789645922L, MESSAGE));
        assertNull(ReportConductFailure.parse(176053, 14214942, 205909, null, 1789645922L, MESSAGE));
    }

    @Test public void missingMessageIdentityIsNotActionable() {
        assertNull(ReportConductFailure.parse(176053, null, 205909, 176053, 1789645922L, MESSAGE));
        assertNull(ReportConductFailure.parse(176053, 0, 205909, 176053, 1789645922L, MESSAGE));
        assertNull(ReportConductFailure.parse(176053, 14214942, 205909, 176053, null, MESSAGE));
        assertNull(parse(null));
    }

    @Test public void positiveResultOrUnrelatedFailureIsNotActionable() {
        assertNull(parse(PREFIX + DOCUMENT + " \u0432\u0434\u0430\u043b\u043e\u0441\u044c!"));
        assertNull(parse(DOCUMENT + FAILED));
        assertNull(parse(PREFIX.replace("\u041e\u0442\u0447\u0435\u0442\u0418\u0441\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044f", "Other") + DOCUMENT + FAILED));
    }

    @Test public void forwardedOrQuotedMessagesDoNotTriggerRecovery() {
        assertNull(parse("Fwd: " + MESSAGE));
        assertNull(parse("> " + MESSAGE));
        assertNull(parse("\"" + MESSAGE + "\""));
    }

    @Test public void malformedReportNumbersAreNotTruncated() {
        assertNull(parse(MESSAGE.replace(DOCUMENT, DOCUMENT + "1")));
        assertNull(parse(MESSAGE.replace(DOCUMENT, DOCUMENT.substring(0, DOCUMENT.length() - 1))));
        assertNull(parse(MESSAGE.replace(DOCUMENT, "Other-03212690")));
    }

    @Test public void pendingNotificationSurvivesRestartWithoutStoringMessageBody() {
        String json = new Gson().toJson(parse(MESSAGE));
        ReportConductFailure restored = new Gson().fromJson(json, ReportConductFailure.class);
        assertEquals(parse(MESSAGE).key(), restored.key());
        assertEquals(DOCUMENT, restored.document);
        assertFalse(json.contains("msg"));
        assertFalse(json.contains(PREFIX));
    }

    @Test public void receiptsAreScopedToTheRecipient() {
        ReportConductFailure first = parse(MESSAGE);
        ReportConductFailure second = ReportConductFailure.parse(7, 14214942, 205909, 7, 1789645922L, MESSAGE);
        assertNotEquals(first.key(), second.key());
    }

    @Test public void pendingNotificationOnlyBlocksItsReport() {
        ReportConductFailure failure = parse(MESSAGE);
        assertTrue(failure.matchesVisit(DOCUMENT, 123));
        assertFalse(failure.matchesVisit("\u0410\u041e\u0438-99999999", 123));
        assertFalse(failure.matchesVisit(null, 123));
        assertFalse(failure.matchesVisit(DOCUMENT, 0));
    }

    @Test public void nativeDad2MustAgreeWithTheReportNumber() {
        ReportConductFailure failure = parse(MESSAGE);
        failure.dad2 = 123;
        assertTrue(failure.matchesVisit(DOCUMENT, 123));
        assertFalse(failure.matchesVisit(DOCUMENT, 456));
        assertFalse(failure.matchesVisit("\u0410\u041e\u0438-99999999", 123));
    }
}
