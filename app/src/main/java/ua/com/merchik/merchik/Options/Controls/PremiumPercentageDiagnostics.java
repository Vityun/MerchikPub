package ua.com.merchik.merchik.Options.Controls;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ua.com.merchik.merchik.Globals;

final class PremiumPercentageDiagnostics {
    private PremiumPercentageDiagnostics() { }

    static JsonObject result(Double cashZakaz, int percent, boolean complete, String decision) {
        JsonObject result = new JsonObject();
        if (cashZakaz != null && (Double.isNaN(cashZakaz) || Double.isInfinite(cashZakaz))) {
            result.addProperty("cash_zakaz", cashZakaz.toString());
        } else {
            result.addProperty("cash_zakaz", cashZakaz);
        }
        result.addProperty("calculation_complete", complete);
        result.addProperty("decision", decision);
        result.addProperty("percent", complete ? percent : null);
        result.addProperty("rate_for_5_percent", Globals.OPTION_CONTROL_PENALTY_RATE);
        result.addProperty("percent_step", PremiumPercentagePolicy.PERCENT_STEP);
        result.addProperty("multiplier", complete ? PremiumPercentagePolicy.multiplier(percent) : null);
        result.addProperty("adjustment_rate", complete ? PremiumPercentagePolicy.adjustmentRate(percent) : null);
        result.addProperty("effective_percent", complete ? PremiumPercentagePolicy.effectivePercent(percent) : null);
        result.addProperty("formula", "cash_zakaz * rate_for_5_percent * abs(percent) / 5");
        result.addProperty("effective_percent_formula", "rate_for_5_percent * percent / 5 * 100");
        result.addProperty("rounding", "HALF_UP, scale=2");
        JsonArray reasons = new JsonArray();
        result.add("zero_reasons", reasons);
        if (!complete) {
            result.addProperty("amount", (Number) null);
            result.addProperty("unavailable_reason", decision);
            return result;
        }

        BigDecimal amount = PremiumPercentagePolicy.amount(cashZakaz != null ? cashZakaz : 0, percent);
        result.addProperty("amount", amount);
        result.addProperty("adjustment", percent > 0 ? "BONUS" : percent < 0 ? "REDUCTION" : "UNCHANGED");
        if (amount.signum() == 0) {
            if (percent == 0) {
                reasons.add("ZERO_PERCENT");
                reasons.add(decision);
            }
            if (cashZakaz == null) reasons.add("CASH_ZAKAZ_MISSING");
            else if (Double.isNaN(cashZakaz) || Double.isInfinite(cashZakaz)) reasons.add("CASH_ZAKAZ_NON_FINITE");
            else if (cashZakaz == 0) reasons.add("CASH_ZAKAZ_ZERO");
            else if (cashZakaz < 0) reasons.add("CASH_ZAKAZ_NEGATIVE");
            else if (percent != 0) reasons.add("ROUNDED_TO_ZERO");
        }
        return result;
    }

    static JsonObject date(Date date) {
        JsonObject result = new JsonObject();
        TimeZone zone = TimeZone.getDefault();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US);
        format.setTimeZone(zone);
        result.addProperty("local", date != null ? format.format(date) : null);
        result.addProperty("epoch_ms", date != null ? date.getTime() : null);
        result.addProperty("time_zone", zone.getID());
        return result;
    }
}
