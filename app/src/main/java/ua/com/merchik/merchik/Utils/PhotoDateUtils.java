package ua.com.merchik.merchik.Utils;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/** Photo dates refer to the visit/task, never to the time of an upload retry. */
public final class PhotoDateUtils {
    private static final ZoneId ZONE = ZoneId.of("Europe/Kiev");
    private static final long MAX_SECONDS = 253402300799L;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private PhotoDateUtils() { }

    public static Long resolveSeconds(Long dt, String timeEvent) {
        Long seconds = normalizeSeconds(dt);
        if (seconds != null) return seconds;
        if (timeEvent == null || timeEvent.trim().isEmpty()) return null;
        String value = timeEvent.trim();
        try {
            if (value.length() == 10) {
                LocalDate date = LocalDate.parse(value, DATE);
                return normalizeSeconds(date.atStartOfDay(ZONE).toEpochSecond());
            }
        } catch (DateTimeException ignored) {
            // Older task photos can contain a Unix timestamp in time_event.
        }
        try {
            return normalizeSeconds(Long.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static String uploadDate(Long dt, String timeEvent) {
        Long seconds = resolveSeconds(dt, timeEvent);
        if (seconds == null) throw new IllegalArgumentException("0");
        return DATE.format(Instant.ofEpochSecond(seconds).atZone(ZONE).toLocalDate());
    }

    private static Long normalizeSeconds(Long value) {
        if (value == null || value <= 0) return null;
        long seconds = value > MAX_SECONDS ? value / 1000 : value;
        if (seconds <= 0 || seconds > MAX_SECONDS) return null;
        try {
            int year = Instant.ofEpochSecond(seconds).atZone(ZONE).getYear();
            return year >= 1970 && year <= 9999 ? seconds : null;
        } catch (DateTimeException ignored) {
            return null;
        }
    }
}
