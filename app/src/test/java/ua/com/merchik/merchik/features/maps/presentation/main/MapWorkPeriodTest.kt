package ua.com.merchik.merchik.features.maps.presentation.main

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MapWorkPeriodTest {
    private val start = LocalDate.of(2026, 11, 23)
    private val end = start.plusDays(4)
    private val format = DateTimeFormatter.ofPattern("dd-MMMM")

    @Test fun markerWithoutDateFilterHasNoPeriod() {
        assertEquals("", mapWorkPeriod(null, null))
    }

    @Test fun openEndedFilterShowsOnlyKnownBoundary() {
        assertEquals("з ${start.format(format)}", mapWorkPeriod(start, null))
        assertEquals("до ${end.format(format)}", mapWorkPeriod(null, end))
    }

    @Test fun completeFilterRetainsBothDates() {
        assertEquals("за період з ${start.format(format)} по ${end.format(format)}", mapWorkPeriod(start, end))
    }
}
