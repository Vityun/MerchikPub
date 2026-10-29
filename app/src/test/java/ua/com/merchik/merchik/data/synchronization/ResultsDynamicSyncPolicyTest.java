package ua.com.merchik.merchik.data.synchronization;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResultsDynamicSyncPolicyTest {
    private static long time(String value) {
        return LocalDateTime.parse(value).atZone(ZoneId.of("Europe/Kiev")).toEpochSecond();
    }

    private static boolean due(String last, String now) {
        return ResultsDynamicSyncPolicy.isDownloadDue(time(last), time(now));
    }

    @Test public void defaultPeriodIsOneDay() {
        assertEquals(86400, ResultsDynamicSyncPolicy.SYNC_PERIOD_SECONDS);
    }

    @Test public void firstDownloadRunsImmediately() {
        assertTrue(ResultsDynamicSyncPolicy.isDownloadDue(0, time("2026-10-29T09:00:00")));
    }

    @Test public void firstDownloadBeforeRefreshAlsoRuns() {
        assertTrue(ResultsDynamicSyncPolicy.isDownloadDue(0, time("2026-10-29T00:30:00")));
    }

    @Test public void tenMinuteExchangeDoesNotRedownload() {
        assertFalse(due("2026-10-29T09:00:00", "2026-10-29T09:10:00"));
    }

    @Test public void laterInSameDayDoesNotRedownload() {
        assertFalse(due("2026-10-29T01:00:00", "2026-10-29T23:59:59"));
    }

    @Test public void midnightDoesNotStartNewSnapshot() {
        assertFalse(due("2026-10-28T10:00:00", "2026-10-29T00:59:59"));
    }

    @Test public void refreshAtOneMakesPreviousSnapshotDue() {
        assertTrue(due("2026-10-29T00:59:59", "2026-10-29T01:00:00"));
    }

    @Test public void downloadAtRefreshBelongsToNewSnapshot() {
        assertFalse(due("2026-10-29T01:00:00", "2026-10-29T01:00:01"));
    }

    @Test public void nextMorningDoesNotWaitTwentyFourHours() {
        assertTrue(due("2026-10-28T18:00:00", "2026-10-29T08:00:00"));
    }

    @Test public void offlineDaysAreCaughtUpOnNextExchange() {
        assertTrue(due("2026-10-25T10:00:00", "2026-10-29T10:00:00"));
    }

    @Test public void previousSuccessfulTimeStillAllowsRetry() {
        long lastSuccess = time("2026-10-28T10:00:00");
        assertTrue(ResultsDynamicSyncPolicy.isDownloadDue(lastSuccess, time("2026-10-29T10:00:00")));
        assertTrue(ResultsDynamicSyncPolicy.isDownloadDue(lastSuccess, time("2026-10-29T10:10:00")));
    }

    @Test public void responseCrossingRefreshDoesNotSuppressNewDownload() {
        assertTrue(due("2026-10-29T00:59:50", "2026-10-29T01:00:10"));
    }

    @Test public void futureTimestampDoesNotBlockDownloadsIndefinitely() {
        assertTrue(due("2026-11-01T10:00:00", "2026-10-29T10:00:00"));
    }

    @Test public void springClockChangeUsesKyivCalendarDay() {
        assertFalse(due("2026-03-29T01:00:00", "2026-03-30T00:59:59"));
        assertTrue(due("2026-03-29T01:00:00", "2026-03-30T01:00:00"));
    }

    @Test public void autumnClockChangeUsesKyivCalendarDay() {
        assertFalse(due("2026-10-25T01:00:00", "2026-10-26T00:59:59"));
        assertTrue(due("2026-10-25T01:00:00", "2026-10-26T01:00:00"));
    }

    @Test public void yearBoundaryUsesPreviousRefreshBeforeOne() {
        assertFalse(due("2026-12-31T18:00:00", "2027-01-01T00:30:00"));
        assertTrue(due("2026-12-31T18:00:00", "2027-01-01T01:00:00"));
    }

    @Test public void deviceTimezoneDoesNotMoveRefreshBoundary() {
        TimeZone previous = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
            assertTrue(due("2026-10-29T00:59:59", "2026-10-29T01:00:00"));
            assertFalse(due("2026-10-29T01:00:00", "2026-10-29T08:00:00"));
        } finally {
            TimeZone.setDefault(previous);
        }
    }
}
