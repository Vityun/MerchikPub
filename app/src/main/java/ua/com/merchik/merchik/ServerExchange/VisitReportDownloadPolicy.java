package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Completion receipts describe coverage; they are not download watermarks. */
public final class VisitReportDownloadPolicy {
    public static final long RETRY_MS = 30_000L;
    private static final ZoneId VISIT_ZONE = ZoneId.of("Europe/Kiev");
    private static final String COVERAGE_VERSION = "dated-v2";

    private VisitReportDownloadPolicy() { }

    public static String scope(int user, Map<Long, LocalDate> visits) {
        StringBuilder value = new StringBuilder().append(user);
        for (Map.Entry<Long, LocalDate> visit : new TreeMap<>(visits).entrySet())
            value.append(':').append(visit.getKey()).append('@').append(Objects.requireNonNull(visit.getValue()));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.toString().getBytes(StandardCharsets.UTF_8));
            // Earlier receipts covered a request without dates and can miss future visits.
            StringBuilder key = new StringBuilder().append(user).append(':').append(COVERAGE_VERSION).append(':');
            for (byte b : digest) key.append(String.format(java.util.Locale.ROOT, "%02x", b & 255));
            return key.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean loaded(boolean receipt, long savedCount, long localCount) {
        return receipt && savedCount >= 0 && localCount >= savedCount;
    }

    public static long requestVpi(boolean covered, long storedVpi) {
        return covered ? Math.max(0, storedVpi) : 0;
    }

    public static LocalDate visitDate(Date date) {
        return Instant.ofEpochMilli(Objects.requireNonNull(date, "Visit date missing").getTime())
                .atZone(VISIT_ZONE).toLocalDate();
    }

    public static boolean canUseSavedPlan(boolean planLoaded, long savedVisits) {
        return planLoaded || savedVisits > 0;
    }

    public static JsonObject request(boolean rp, Map<Long, LocalDate> visits, long vpi) {
        if (visits.isEmpty()) throw new IllegalArgumentException("Visit scope is empty");
        JsonObject request = new JsonObject();
        request.addProperty("mod", rp ? "report_prepare" : "data_list");
        request.addProperty("act", rp ? "list_data" : "tovar_list");
        request.addProperty(rp ? "dt_change_from" : "dt", String.valueOf(vpi));
        JsonArray ids = new JsonArray();
        for (long id : new TreeMap<>(visits).keySet()) ids.add(id);
        request.add("code_dad2", ids);
        if (rp) {
            request.addProperty("date_from", Collections.min(visits.values()).toString());
            // Include the entire last planned day even if the backend compares with midnight.
            request.addProperty("date_to", Collections.max(visits.values()).plusDays(1).toString());
        }
        return request;
    }

    public static long nextVpi(long previous, long requestStartedSec, int rows, boolean fullySaved) {
        return fullySaved && rows > 0 ? Math.max(previous, Math.max(0, requestStartedSec - 120)) : previous;
    }

    public static boolean retryDue(long nowMs, long lastAttemptMs) {
        return lastAttemptMs < 0 || nowMs - lastAttemptMs >= RETRY_MS;
    }
}
