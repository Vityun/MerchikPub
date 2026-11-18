package ua.com.merchik.merchik.Options.Controls;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import ua.com.merchik.merchik.Globals;

public final class PremiumPercentagePolicy {
    static final int PERCENT_STEP = 5;
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private PremiumPercentagePolicy() { }

    public static BigDecimal amount(double cashZakaz, int percent) {
        if (Double.isNaN(cashZakaz) || Double.isInfinite(cashZakaz) || cashZakaz <= 0) {
            return ZERO;
        }
        return BigDecimal.valueOf(cashZakaz)
                .multiply(adjustmentRate(percent))
                .setScale(2, RoundingMode.HALF_UP);
    }

    static BigDecimal multiplier(int percent) {
        return BigDecimal.valueOf(percent).abs().divide(BigDecimal.valueOf(PERCENT_STEP));
    }

    public static BigDecimal adjustmentRate(int percent) {
        // The shared rate already accounts for the performer's share, for one 5% step.
        return BigDecimal.valueOf(Globals.OPTION_CONTROL_PENALTY_RATE).multiply(multiplier(percent));
    }

    public static BigDecimal effectivePercent(int percent) {
        return adjustmentRate(percent).movePointRight(2)
                .multiply(BigDecimal.valueOf(Integer.signum(percent)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static String detailPercentText(int percent, Locale locale) {
        return String.format(locale, "%.2f%%", effectivePercent(percent));
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

    public static String counterColor(String storedPercent, String negativeColor) {
        int percent = readPercent(storedPercent);
        return percent > 0 ? "green" : percent < 0 ? negativeColor : "gray";
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
