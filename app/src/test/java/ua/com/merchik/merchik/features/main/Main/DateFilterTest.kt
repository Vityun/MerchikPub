package ua.com.merchik.merchik.features.main.Main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.TextField
import java.io.File
import java.time.LocalDate
import java.time.ZoneOffset

class DateFilterTest {
    private val today = LocalDate.of(2026, 11, 19)
    private val old = item(1, "2026-09-19")
    private val current = item(2, "2026-11-19")
    private val fourthDay = item(3, "2026-11-23")
    private val future = item(4, "2026-11-24")
    private val undated = item(5, null)
    private val rows = listOf(old, current, fourthDay, future)

    @Test fun unboundedRangeDoesNotHideHistoricalOrUndatedRows() {
        val result = filter(Filters(rangeDataByKey = RangeDate(key = "dt", enabled = true)),
            rows + undated, null, null)

        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), result.items.map { it.stableId })
        assertFalse(result.isActiveFiltered)
    }

    @Test fun disabledDateControlWithoutBoundsDoesNotHideUndatedRows() {
        val result = filter(Filters(rangeDataByKey = RangeDate(key = "dt", enabled = false)),
            rows + undated, null, null)

        assertEquals(5, result.items.size)
        assertFalse(result.isActiveFiltered)
    }

    @Test fun sectionsWithoutDateFilterIgnoreEvenOldDefaultBounds() {
        val result = filter(Filters(), rows + undated, today, today.plusDays(4))

        assertEquals(5, result.items.size)
        assertFalse(result.isActiveFiltered)
    }

    @Test fun workPlanStillFiltersTodayThroughFourthDayInclusive() {
        val result = filter(range(), rows, today, today.plusDays(4))

        assertEquals(listOf(2L, 3L), result.items.map { it.stableId })
        assertTrue(result.isActiveFiltered)
    }

    @Test fun explicitlySelectedHistoricalPeriodStillApplies() {
        val result = filter(range(), rows, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))

        assertEquals(listOf(1L), result.items.map { it.stableId })
    }

    @Test fun lockedDateRangeRemainsAnActiveConstraint() {
        val result = filter(Filters(rangeDataByKey = RangeDate(key = "dt", enabled = false)),
            rows, today, today.plusDays(4))

        assertEquals(listOf(2L, 3L), result.items.map { it.stableId })
        assertTrue(result.isActiveFiltered)
    }

    @Test fun startOnlyRangeIsSupported() {
        assertEquals(listOf(2L, 3L, 4L), filter(range(), rows, today, null).items.map { it.stableId })
    }

    @Test fun endOnlyRangeIncludesOldRows() {
        assertEquals(listOf(1L, 2L), filter(range(), rows, null, today).items.map { it.stableId })
    }

    @Test fun clearingOrdinaryFiltersRemovesDateBoundsAndSearch() {
        val cleared = range().copy(searchText = "test").clearEditableValues()

        assertNull(cleared.rangeDataByKey!!.start)
        assertNull(cleared.rangeDataByKey!!.end)
        assertEquals("", cleared.searchText)
        assertEquals(5, filter(cleared, rows + undated,
            cleared.rangeDataByKey!!.start, cleared.rangeDataByKey!!.end).items.size)
    }

    @Test fun workPlanResetKeepsItsPreviousTodayValues() {
        val cleared = range().clearEditableValues(resetDate = today)

        assertEquals(today, cleared.rangeDataByKey!!.start)
        assertEquals(today, cleared.rangeDataByKey!!.end)
    }

    @Test fun clearingDoesNotRemoveLockedDateRange() {
        val locked = RangeDate("dt", today.minusDays(41), today.plusDays(3), false)
        val cleared = Filters(rangeDataByKey = locked).clearEditableValues()

        assertSame(locked, cleared.rangeDataByKey)
    }

    @Test fun workPlanAndPhotoJournalOptIntoAutomaticTodayBounds() {
        val base = source("Main/MainViewModel.kt")
        assertTrue(base.contains("initialStartDate: LocalDate? = null"))
        assertTrue(base.contains("initialEndDate: LocalDate? = null"))
        assertTrue(base.contains("_rangeDataStart = MutableStateFlow(initialStartDate)"))
        assertTrue(base.contains("_rangeDataEnd = MutableStateFlow(initialEndDate)"))
        val workPlan = source("DBViewModels/WpDataDBViewModel.kt")
        assertTrue(workPlan.contains("initialStartDate = LocalDate.now()"))
        assertTrue(workPlan.contains("initialEndDate = LocalDate.now().plusDays(4)"))
        val photoJournal = source("DBViewModels/JournalPhotoSDBViewModel.kt")
        assertTrue(photoJournal.contains("initialStartDate = LocalDate.now()"))
        assertTrue(photoJournal.contains("initialEndDate = LocalDate.now().plusDays(4)"))
        assertTrue(photoJournal.contains("?: Long.MIN_VALUE"))
        assertTrue(photoJournal.contains("?: Long.MAX_VALUE"))
    }

    @Test fun resetClearsQueryBoundsOnlyOutsideWorkPlan() {
        val dialog = source("Main/FilteringDialog.kt")
        assertTrue(dialog.contains("val isWorkPlan = viewModel is WpDataDBViewModel"))
        assertTrue(dialog.contains("if (!isWorkPlan && it.rangeDataByKey?.enabled == true) {\n" +
            "                                    viewModel.setStartDate(null)\n" +
            "                                    viewModel.setEndDate(null)"))
    }

    @Test fun achievementsKeepTheirVisitBasedPeriod() {
        val achievements = source("DBViewModels/AchievementsSDBViewModel.kt")
        assertTrue(achievements.contains("Clock.getDatePeriodLong(visitMillis(wpDataDB), -41)"))
        assertTrue(achievements.contains("Clock.getDatePeriodLong(visitMillis(wpDataDB), 3)"))
        assertTrue(achievements.contains("existingRange?.start ?: defaultStart"))
        assertTrue(achievements.contains("existingRange?.end ?: defaultEnd"))
    }

    private fun range() = Filters(rangeDataByKey = RangeDate("dt", today, today.plusDays(4), true))

    private fun item(id: Long, date: String?): DataItemUI {
        val raw = date?.let { LocalDate.parse(it).atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC) }
        val fields = listOf(FieldValue("dt", TextField("dt", "Date"), TextField(raw ?: "", date.orEmpty())))
        return DataItemUI(emptyList(), fields, fields, selected = false, stableId = id)
    }

    private fun filter(filters: Filters, items: List<DataItemUI>, start: LocalDate?, end: LocalDate?) =
        filterAndSortDataItems(items, filters, emptyList(), emptyList(), start, end,
            filters.searchText, ZoneOffset.UTC)

    private fun source(path: String) =
        File("app/src/main/java/ua/com/merchik/merchik/features/main/$path").readText().replace("\r\n", "\n")
}
