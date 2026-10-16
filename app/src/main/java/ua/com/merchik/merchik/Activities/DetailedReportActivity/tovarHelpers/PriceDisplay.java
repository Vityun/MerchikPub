package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** UI precision only; never use the formatted text to overwrite the stored price. */
public final class PriceDisplay {
    private PriceDisplay() {}

    public static BigDecimal cents(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static String format(String value) {
        if (value == null || value.trim().isEmpty()) return "0.00";
        try {
            return cents(new BigDecimal(value.trim().replace(',', '.'))).toPlainString();
        } catch (NumberFormatException ignored) {
            return value;
        }
    }

    public static String editorResult(String entered, String original) {
        // Saving an untouched editor must preserve even sub-cent precision in the DB.
        return original != null && !original.trim().isEmpty() && format(original).equals(entered)
                ? original : entered;
    }
}
