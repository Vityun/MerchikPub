package ua.com.merchik.merchik.Utils;

import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class PhotoDateUtilsTest {
    private static final long MIDNIGHT = Instant.parse("2026-10-18T21:00:00Z").getEpochSecond();

    @Test
    public void missingDtUsesPersistedVisitDate() {
        assertEquals(Long.valueOf(MIDNIGHT), PhotoDateUtils.resolveSeconds(null, "2026-10-19"));
        assertEquals("2026-10-19", PhotoDateUtils.uploadDate(null, "2026-10-19"));
    }

    @Test
    public void validDtWinsOverConflictingTimeEvent() {
        assertEquals(Long.valueOf(MIDNIGHT), PhotoDateUtils.resolveSeconds(MIDNIGHT, "2026-10-20"));
        assertEquals("2026-10-19", PhotoDateUtils.uploadDate(MIDNIGHT, "2026-10-20"));
    }

    @Test
    public void validDtDoesNotRequireTimeEvent() {
        assertEquals("2026-10-19", PhotoDateUtils.uploadDate(MIDNIGHT, null));
        assertEquals("2026-10-19", PhotoDateUtils.uploadDate(MIDNIGHT, "invalid"));
    }

    @Test
    public void millisecondsAreConvertedToSeconds() {
        assertEquals(Long.valueOf(MIDNIGHT), PhotoDateUtils.resolveSeconds(MIDNIGHT * 1000 + 987, null));
        assertEquals("2026-10-19", PhotoDateUtils.uploadDate(MIDNIGHT * 1000, null));
    }

    @Test
    public void legacyTaskTimeEventMayContainSeconds() {
        assertEquals(Long.valueOf(MIDNIGHT), PhotoDateUtils.resolveSeconds(null, String.valueOf(MIDNIGHT)));
        assertEquals("2026-10-19", PhotoDateUtils.uploadDate(null, String.valueOf(MIDNIGHT)));
    }

    @Test
    public void legacyTaskTimeEventMayContainMilliseconds() {
        assertEquals(Long.valueOf(MIDNIGHT), PhotoDateUtils.resolveSeconds(null, String.valueOf(MIDNIGHT * 1000)));
    }

    @Test
    public void invalidDtFallsBackToTimeEvent() {
        for (Long dt : Arrays.asList(0L, -1L, Long.MIN_VALUE, Long.MAX_VALUE)) {
            assertEquals(Long.valueOf(MIDNIGHT), PhotoDateUtils.resolveSeconds(dt, "2026-10-19"));
        }
    }

    @Test
    public void malformedDatesDoNotBecomeTodayOrAnotherDay() {
        for (String value : Arrays.asList(null, "", " ", "null", "0000-00-00", "2026-02-29",
                "2026-02-30", "2026-13-01", "2026-10-32", "19.10.2026", "not-a-date")) {
            assertNull(value, PhotoDateUtils.resolveSeconds(null, value));
        }
    }

    @Test
    public void invalidNumericDatesAreRejected() {
        for (String value : Arrays.asList("0", "-1", String.valueOf(Long.MAX_VALUE), "999999999999999999999")) {
            assertNull(value, PhotoDateUtils.resolveSeconds(null, value));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void uploadRejectsMissingDateInsteadOfUnboxingNull() {
        PhotoDateUtils.uploadDate(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void uploadRejectsInvalidDateInsteadOfInventingOne() {
        PhotoDateUtils.uploadDate(0L, "0000-00-00");
    }

    @Test
    public void validLeapDayIsPreserved() {
        assertEquals("2024-02-29", PhotoDateUtils.uploadDate(null, "2024-02-29"));
    }

    @Test
    public void whitespaceAroundPersistedDateIsAccepted() {
        assertEquals("2026-10-19", PhotoDateUtils.uploadDate(null, " 2026-10-19 "));
    }

    @Test
    public void midnightUsesKyivDayRatherThanUtcDay() {
        assertEquals("2026-10-19", PhotoDateUtils.uploadDate(MIDNIGHT + 1800, null));
        assertEquals("2026-10-18", PhotoDateUtils.uploadDate(MIDNIGHT - 1, null));
    }

    @Test
    public void deviceTimeZoneDoesNotChangeVisitDay() {
        TimeZone saved = TimeZone.getDefault();
        try {
            for (String zone : Arrays.asList("UTC", "America/Lima", "Pacific/Kiritimati")) {
                TimeZone.setDefault(TimeZone.getTimeZone(zone));
                assertEquals("2026-10-19", PhotoDateUtils.uploadDate(MIDNIGHT, null));
                assertEquals(Long.valueOf(MIDNIGHT), PhotoDateUtils.resolveSeconds(null, "2026-10-19"));
            }
        } finally {
            TimeZone.setDefault(saved);
        }
    }

    @Test
    public void winterDateUsesWinterOffset() {
        long midnight = Instant.parse("2026-12-18T22:00:00Z").getEpochSecond();
        assertEquals(Long.valueOf(midnight), PhotoDateUtils.resolveSeconds(null, "2026-12-19"));
    }
}
