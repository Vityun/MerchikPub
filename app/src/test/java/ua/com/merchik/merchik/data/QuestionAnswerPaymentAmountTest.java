package ua.com.merchik.merchik.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class QuestionAnswerPaymentAmountTest {
    @Test public void roundsMathematicallyWithoutFloatingPointLoss() {
        assertEquals("70", QuestionAnswerPaymentAmount.roundToWhole("70.49"));
        assertEquals("71", QuestionAnswerPaymentAmount.roundToWhole("70.50"));
        assertEquals("71", QuestionAnswerPaymentAmount.roundToWhole("70.99"));
        assertEquals("70", QuestionAnswerPaymentAmount.roundToWhole("70.49999999999999999"));
    }

    @Test public void acceptsCommaAndTrimsWhitespace() {
        assertEquals("71", QuestionAnswerPaymentAmount.roundToWhole(" 70,50 "));
        assertEquals("70", QuestionAnswerPaymentAmount.roundToWhole("70,49"));
    }

    @Test public void removesFractionalZerosAndKeepsIntegerValues() {
        assertEquals("70", QuestionAnswerPaymentAmount.roundToWhole("70.00"));
        assertEquals("70", QuestionAnswerPaymentAmount.roundToWhole("70"));
        assertEquals("0", QuestionAnswerPaymentAmount.roundToWhole("0.00"));
    }

    @Test public void doesNotOverflowAnInt() {
        assertEquals("2147483648", QuestionAnswerPaymentAmount.roundToWhole("2147483647.50"));
    }

    @Test public void preservesMissingAndInvalidLegacyValues() {
        assertNull(QuestionAnswerPaymentAmount.roundToWhole(null));
        assertEquals("", QuestionAnswerPaymentAmount.roundToWhole(""));
        assertEquals(" ", QuestionAnswerPaymentAmount.roundToWhole(" "));
        assertEquals("unknown", QuestionAnswerPaymentAmount.roundToWhole("unknown"));
    }

    @Test public void uploadOnlyRoundsPaymentIncreaseAnswers() {
        assertEquals("71", QuestionAnswerPaymentAmount.forUpload(610, "70.50"));
        assertEquals("70.50", QuestionAnswerPaymentAmount.forUpload(600, "70.50"));
        assertEquals("70.50", QuestionAnswerPaymentAmount.forUpload(null, "70.50"));
    }
}
