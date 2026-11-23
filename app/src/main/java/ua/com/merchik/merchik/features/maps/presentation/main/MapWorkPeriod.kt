package ua.com.merchik.merchik.features.maps.presentation.main

import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal fun mapWorkPeriod(start: LocalDate?, end: LocalDate?): String {
    val format = DateTimeFormatter.ofPattern("dd-MMMM")
    return when {
        start != null && end != null -> "за період з ${start.format(format)} по ${end.format(format)}"
        start != null -> "з ${start.format(format)}"
        end != null -> "до ${end.format(format)}"
        else -> ""
    }
}
