package ua.com.merchik.merchik.Utils;

import java.math.BigDecimal;

/** Converts legacy decimal face counts to whole counts without truncation or silent fallback. */
public final class FaceValue {
    public static final int MAX_INPUT = 999;

    private FaceValue() { }

    /** Input-only bound, checked before rounding. Format validation remains in CountValue. */
    public static boolean exceedsInputLimit(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        try {
            return new BigDecimal(value.trim().replace(',', '.'))
                    .compareTo(BigDecimal.valueOf(MAX_INPUT)) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int rounded(String value) {
        return CountValue.rounded(value);
    }

    public static String input(String value) {
        return CountValue.input(value);
    }

    public static String draft(String value) {
        return CountValue.draft(value);
    }

    public static String display(String value) {
        try {
            return input(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    public static String step(String value, int delta) {
        return CountValue.step(value, delta);
    }

    public static boolean resetsToZero(String previous, String next) {
        return CountValue.nonNegative(next) == 0 && rounded(previous) != 0;
    }
}
