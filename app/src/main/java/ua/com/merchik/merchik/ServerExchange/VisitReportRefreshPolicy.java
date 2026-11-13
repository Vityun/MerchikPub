package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.util.Objects;

/** The per-visit success receipt is independent of the incremental download VPI. */
public final class VisitReportRefreshPolicy {
    public static final long INTERVAL_SEC = 60 * 60;

    private VisitReportRefreshPolicy() { }

    public static boolean notStarted(long clientStart, long visitStart, long clientEnd, long visitEnd) {
        return clientStart == 0 && visitStart == 0 && clientEnd == 0 && visitEnd == 0;
    }

    public static boolean due(long now, long lastSuccess, boolean hasRows) {
        return !hasRows || lastSuccess <= 0 || now < lastSuccess || now - lastSuccess >= INTERVAL_SEC;
    }

    public static String scope(int user, long dad2) {
        if (user <= 0 || dad2 <= 0) throw new IllegalArgumentException("Invalid visit scope");
        return user + ":" + dad2;
    }

    public static JsonObject request(long dad2, LocalDate date) {
        if (dad2 <= 0) throw new IllegalArgumentException("Invalid dad2");
        Objects.requireNonNull(date, "Visit date missing");
        JsonObject request = new JsonObject();
        request.addProperty("mod", "report_prepare");
        request.addProperty("act", "list_data");
        request.addProperty("code_dad2", String.valueOf(dad2));
        // Zero requests the full visit, never the global incremental cursor.
        request.addProperty("dt_change_from", "0");
        request.addProperty("date_from", date.toString());
        request.addProperty("date_to", date.plusDays(1).toString());
        return request;
    }

    public static long completedAt(long previous, long now, int rowCount, boolean applied) {
        return applied && rowCount > 0 ? now : previous;
    }
}
