package ua.com.merchik.merchik.Options.Controls;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import ua.com.merchik.merchik.Globals;

public final class PremiumPercentagePolicy {
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private PremiumPercentagePolicy() { }

    public static BigDecimal amount(double cashZakaz, int percent) {
        if (Double.isNaN(cashZakaz) || Double.isInfinite(cashZakaz) || cashZakaz <= 0) {
            return ZERO;
        }
        // The existing rate is the amount for a 10% adjustment, not for every percentage.
        return BigDecimal.valueOf(cashZakaz)
                .multiply(BigDecimal.valueOf(Globals.OPTION_CONTROL_PENALTY_RATE))
                .multiply(BigDecimal.valueOf(percent).abs())
                .movePointLeft(1)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal amount(double cashZakaz, String storedPercent) {
        return amount(cashZakaz, readPercent(storedPercent));
    }

    public static String penalty(double cashZakaz, int percent) {
        return (percent < 0 ? amount(cashZakaz, percent) : ZERO).toPlainString();
    }

    public static String counterText(double cashZakaz, String storedPercent, Locale locale) {
        int percent = readPercent(storedPercent);
        String sign = percent > 0 ? "+" : percent < 0 ? "-" : "";
        return "~" + sign + String.format(locale, "%.2f", amount(cashZakaz, percent));
    }

    public static String counterColor(String storedPercent) {
        int percent = readPercent(storedPercent);
        return percent > 0 ? "green" : percent < 0 ? "red" : "gray";
    }

    private static int readPercent(String storedPercent) {
        if (storedPercent == null || storedPercent.trim().isEmpty()) return 0;
        try {
            return new BigDecimal(storedPercent.trim().replace(',', '.')).intValueExact();
        } catch (NumberFormatException | ArithmeticException ignored) {
            return 0;
        }
    }
}
