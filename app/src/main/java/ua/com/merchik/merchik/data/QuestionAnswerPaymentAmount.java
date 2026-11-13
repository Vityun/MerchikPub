package ua.com.merchik.merchik.data;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class QuestionAnswerPaymentAmount {
    private static final int PAYMENT_INCREASE_QUESTION_ID = 610;

    private QuestionAnswerPaymentAmount() {}

    public static String forUpload(Integer questionId, String answer) {
        return questionId != null && questionId == PAYMENT_INCREASE_QUESTION_ID
                ? roundToWhole(answer) : answer;
    }

    public static String roundToWhole(String amount) {
        if (amount == null || amount.trim().isEmpty()) return amount;
        try {
            return new BigDecimal(amount.trim().replace(',', '.'))
                    .setScale(0, RoundingMode.HALF_UP).toPlainString();
        } catch (NumberFormatException e) {
            // Preserve malformed legacy values instead of turning them into zero.
            return amount;
        }
    }
}
