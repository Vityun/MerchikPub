package ua.com.merchik.merchik.Options;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** All comparisons use seconds; duration_plan and avg_time.duration are supplied in minutes. */
public final class OvertimeOfferPolicy {
    public static final String VITMARK = "9382";
    public static final int SUPPR_THEME = 1132;
    public static final int ACHIEVEMENT_THEME = 1378;
    public static final int DEFAULT_OVERTIME_PERCENT = 30;
    static final int MIN_OVERTIME_PERCENT = 10;
    private static final int MAX_AVERAGE_SHORTFALL_PERCENT = 30;
    private static final BigDecimal SECONDS_PER_MINUTE = BigDecimal.valueOf(60);
    private static final BigDecimal ONE_HOUR_SECONDS = SECONDS_PER_MINUTE.multiply(BigDecimal.valueOf(60));
    private static final BigDecimal TWO_HOURS_SECONDS = SECONDS_PER_MINUTE.multiply(BigDecimal.valueOf(120));

    private OvertimeOfferPolicy() { }

    public static BigDecimal plannedSeconds(String minutes) {
        try {
            return positiveMinutesToSeconds(new BigDecimal(minutes.trim().replace(',', '.')));
        } catch (RuntimeException e) { return null; }
    }

    public static BigDecimal averageSeconds(JsonObject response) {
        try {
            if (response == null || !response.get("state").getAsBoolean()) return null;
            return positiveMinutesToSeconds(response.get("duration").getAsBigDecimal());
        } catch (RuntimeException e) { return null; }
    }

    private static BigDecimal positiveMinutesToSeconds(BigDecimal minutes) {
        return minutes.signum() > 0 ? minutes.multiply(SECONDS_PER_MINUTE) : null;
    }

    static int overtimePercentForAverage(BigDecimal averageSeconds) {
        if (averageSeconds.compareTo(TWO_HOURS_SECONDS) > 0) return MIN_OVERTIME_PERCENT;
        if (averageSeconds.compareTo(ONE_HOUR_SECONDS) >= 0) return 20;
        return DEFAULT_OVERTIME_PERCENT;
    }

    public static boolean exceedsPlan(long actualSeconds, BigDecimal planSeconds, int overtimePercent) {
        return actualSeconds > 0 && isPositive(planSeconds)
                && BigDecimal.valueOf(actualSeconds).compareTo(planSeconds.multiply(
                        BigDecimal.valueOf(100L + overtimePercent).movePointLeft(2))) > 0;
    }

    public static boolean acceptsAverage(BigDecimal planSeconds, BigDecimal averageSeconds) {
        return isPositive(planSeconds) && isPositive(averageSeconds)
                && averageSeconds.compareTo(planSeconds.multiply(
                        BigDecimal.valueOf(100 - MAX_AVERAGE_SHORTFALL_PERCENT).movePointLeft(2))) >= 0;
    }

    private static boolean isPositive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    public static JsonObject averageRequest(String clientId, int addrId, int themeId) {
        JsonObject request = new JsonObject();
        request.addProperty("mod", "plan");
        request.addProperty("act", "avg_time");
        request.addProperty("client_id", clientId);
        request.addProperty("addr_id", addrId);
        request.addProperty("theme_id", themeId);
        return request;
    }

    public static JsonObject averageRequest(String clientId, int addrId, int themeId, long dad2) {
        JsonObject request = averageRequest(clientId, addrId, themeId);
        request.addProperty("code_dad2_exclude", dad2);
        return request;
    }

    public static String minutes(BigDecimal seconds) {
        return seconds.divide(SECONDS_PER_MINUTE, 0, RoundingMode.HALF_UP).toPlainString();
    }
}
