package ua.com.merchik.merchik.Options;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** All comparisons use seconds; wpdata.duration_plan is supplied in minutes. */
public final class OvertimeOfferPolicy {
    public static final String VITMARK = "9382";
    public static final int SUPPR_THEME = 1132;
    public static final int ACHIEVEMENT_THEME = 1378;

    private OvertimeOfferPolicy() { }

    public static BigDecimal plannedSeconds(String minutes) {
        try {
            BigDecimal value = new BigDecimal(minutes.trim().replace(',', '.'));
            return value.signum() > 0 ? value.multiply(BigDecimal.valueOf(60)) : null;
        } catch (RuntimeException e) { return null; }
    }

    public static BigDecimal averageSeconds(JsonObject response) {
        try {
            if (response == null || !response.get("state").getAsBoolean()) return null;
            BigDecimal value = response.get("duration").getAsBigDecimal();
            return value.signum() > 0 ? value : null;
        } catch (RuntimeException e) { return null; }
    }

    public static boolean exceeds(long actualSeconds, BigDecimal baselineSeconds) {
        return actualSeconds > 0 && baselineSeconds != null && baselineSeconds.signum() > 0
                && BigDecimal.valueOf(actualSeconds).compareTo(
                        baselineSeconds.multiply(new BigDecimal("1.5"))) > 0;
    }

    public static JsonObject averageRequest(String clientId, int addrId, int themeId, long dad2) {
        JsonObject request = new JsonObject();
        request.addProperty("mod", "plan");
        request.addProperty("act", "avg_time");
        request.addProperty("client_id", clientId);
        request.addProperty("addr_id", addrId);
        request.addProperty("theme_id", themeId);
        request.addProperty("code_dad2_exclude", dad2);
        return request;
    }

    public static String minutes(BigDecimal seconds) {
        return seconds.divide(BigDecimal.valueOf(60), 0, RoundingMode.HALF_UP).toPlainString();
    }
}
