package ua.com.merchik.merchik.Options.Controls;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Explicit metadata allowlist: message text, names and contact details must not enter MLOG. */
final class SmsControlDiagnostics {
    static final int MAX_MATCHES = 5;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Europe/Kiev"));
    private static final String[] FIELDS = {"id", "ID", "code_dad2", "sender", "addr_id", "client_to",
            "tp", "state", "scnt", "dt", "dt_act", "dt_check", "var_id", "gw_type", "msg_type", "msg_type_code"};

    private SmsControlDiagnostics() { }

    static JsonObject metadata(JsonObject row, long visitDad2, int visitUserId, int currentUserId) {
        JsonObject result = new JsonObject();
        for (String field : FIELDS) {
            if (row.has(field)) result.add(field, row.get(field));
        }
        result.addProperty("sameVisit", equalsNumber(row.get("code_dad2"), visitDad2));
        result.addProperty("sameVisitUser", equalsNumber(row.get("sender"), visitUserId));
        result.addProperty("sameCurrentUser", equalsNumber(row.get("sender"), currentUserId));
        for (String field : new String[]{"dt", "dt_act", "dt_check"}) {
            if (row.has(field)) result.addProperty(field + "_kyiv", time(row.get(field)));
        }
        if (row.has("sms_err")) {
            JsonElement error = row.get("sms_err");
            result.addProperty("hasSmsErrorText", error != null && !error.isJsonNull()
                    && error.isJsonPrimitive() && !error.getAsString().trim().isEmpty());
        }
        return result;
    }

    private static boolean equalsNumber(JsonElement value, long expected) {
        try { return value != null && !value.isJsonNull() && value.getAsBigDecimal()
                .compareTo(java.math.BigDecimal.valueOf(expected)) == 0; }
        catch (RuntimeException e) { return false; }
    }

    private static String time(JsonElement value) {
        try { return time(value.getAsLong()); }
        catch (RuntimeException e) { return "unknown"; }
    }

    static String time(long seconds) {
        if (seconds <= 0) return "unset";
        try { return TIME.format(Instant.ofEpochSecond(seconds)); }
        catch (RuntimeException e) { return "invalid"; }
    }
}
