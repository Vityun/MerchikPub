package ua.com.merchik.merchik.features.main.chat

import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object ChatDateFormatter {
    private val weekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
    private val months = listOf("Січ", "Лют", "Бер", "Кві", "Тра", "Чер", "Лип", "Сер", "Вер", "Жов", "Лис", "Гру")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)
    private val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT)

    fun format(
        timestampSeconds: Long,
        zone: ZoneId = ZoneId.systemDefault(),
        today: LocalDate = LocalDate.now(zone)
    ): String {
        if (timestampSeconds <= 0) return ""
        val time = try {
            Instant.ofEpochSecond(timestampSeconds).atZone(zone)
        } catch (_: DateTimeException) {
            return ""
        }
        val date = time.toLocalDate()
        val weekStart = today.minusDays(today.dayOfWeek.value - 1L)
        return when {
            date == today -> time.format(timeFormat)
            !date.isBefore(weekStart) && date.isBefore(weekStart.plusWeeks(1)) -> weekdays[date.dayOfWeek.value - 1]
            date.year == today.year -> "${date.dayOfMonth} ${months[date.monthValue - 1]}"
            else -> date.format(dateFormat)
        }
    }
}
