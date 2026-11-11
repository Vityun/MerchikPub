package ua.com.merchik.merchik.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Whole quantities: missing is zero; malformed values must not silently become zero. */
public final class CountValue {
    private CountValue() { }

    public static int rounded(String value) {
        try {
            return decimal(value).setScale(0, RoundingMode.HALF_UP).intValueExact();
        } catch (ArithmeticException e) {
            throw new NumberFormatException("Quantity is out of range: " + value);
        }
    }

    public static int nonNegative(String value) {
        if (decimal(value).signum() < 0) throw new NumberFormatException("Negative quantity: " + value);
        return rounded(value);
    }

    public static String input(String value) {
        return value == null || value.trim().isEmpty() ? "" : Integer.toString(nonNegative(value));
    }

    public static String draft(String value) {
        String result = value == null ? "" : value.trim().replace(',', '.');
        if (result.equals(".")) result = "0.";
        if (!result.isEmpty() && !result.matches("[0-9]+(\\.[0-9]*)?")) {
            throw new NumberFormatException("Invalid quantity: " + value);
        }
        nonNegative(result);
        return result;
    }

    public static String step(String value, int delta) {
        return Integer.toString(Math.max(0, Math.addExact(nonNegative(draft(value)), delta)));
    }

    private static BigDecimal decimal(String value) {
        return value == null || value.trim().isEmpty() ? BigDecimal.ZERO
                : new BigDecimal(value.trim().replace(',', '.'));
    }
}
