package ua.com.merchik.merchik.features.main.chat

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ChatDateFormatterTest {
    private val zone = ZoneId.of("Europe/Kiev")
    private val today = LocalDate.of(2026, 9, 29)

    private fun format(dateTime: String, currentDate: LocalDate = today): String =
        ChatDateFormatter.format(LocalDateTime.parse(dateTime).atZone(zone).toEpochSecond(), zone, currentDate)

    @Test
    fun todayShowsTimeOnly() {
        assertEquals("08:05", format("2026-09-29T08:05:00"))
    }

    @Test
    fun currentWeekStartsOnMondayAndUsesUkrainianWeekdays() {
        assertEquals("Пн", format("2026-09-28T12:00:00"))
        assertEquals("Нд", format("2026-09-27T12:00:00", LocalDate.of(2026, 9, 26)))
        assertEquals("27 Вер", format("2026-09-27T12:00:00"))
    }

    @Test
    fun olderDatesInCurrentYearUseDayAndShortMonth() {
        assertEquals("2 Бер", format("2026-03-02T12:00:00"))
        assertEquals("24 Вер", format("2026-09-24T12:00:00"))
    }

    @Test
    fun olderYearsUseFullDateUnlessTheyBelongToCurrentWeek() {
        assertEquals("29.02.2024", format("2024-02-29T12:00:00"))
        assertEquals("Пн", format("2025-12-29T12:00:00", LocalDate.of(2026, 1, 1)))
        assertEquals("20.12.2025", format("2025-12-20T12:00:00", LocalDate.of(2026, 1, 1)))
    }

    @Test
    fun timestampsUseLocalDateNotUtcDate() {
        val timestamp = Instant.parse("2026-09-28T22:30:00Z").epochSecond
        assertEquals("01:30", ChatDateFormatter.format(timestamp, zone, today))
        assertEquals("Пн", ChatDateFormatter.format(timestamp, ZoneId.of("UTC"), today))
    }

    @Test
    fun missingOrInvalidTimestampIsEmpty() {
        assertEquals("", ChatDateFormatter.format(0, zone, today))
        assertEquals("", ChatDateFormatter.format(-1, zone, today))
        assertEquals("", ChatDateFormatter.format(Long.MAX_VALUE, zone, today))
    }
}
