package ua.com.merchik.merchik.data.synchronization;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class ResultsDynamicSyncPolicy {
    public static final int SYNC_PERIOD_SECONDS = 86400;
    private static final ZoneId ZONE = ZoneId.of("Europe/Kiev");

    private ResultsDynamicSyncPolicy() { }

    public static boolean isDownloadDue(long lastDownloadSeconds, long nowSeconds) {
        if (lastDownloadSeconds <= 0 || lastDownloadSeconds > nowSeconds) return true;

        ZonedDateTime now = Instant.ofEpochSecond(nowSeconds).atZone(ZONE);
        ZonedDateTime refresh = now.toLocalDate().atTime(1, 0).atZone(ZONE);
        if (now.isBefore(refresh)) refresh = refresh.minusDays(1);
        return lastDownloadSeconds < refresh.toEpochSecond();
    }
}
