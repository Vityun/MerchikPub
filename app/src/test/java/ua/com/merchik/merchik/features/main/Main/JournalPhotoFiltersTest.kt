package ua.com.merchik.merchik.features.main.Main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.com.merchik.merchik.dataLayer.SelectedMode
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.features.main.DBViewModels.journalPhotoFilters
import java.time.LocalDate

class JournalPhotoFiltersTest {
    private val today = LocalDate.of(2026, 11, 19)

    @Test fun firstOpeningSelectsOnlyCurrentPerformerAndKeepsDateRange() {
        val filters = defaults()
        assertEquals(listOf("user_id"), filters.items.filter { it.rightValuesRaw.isNotEmpty() }.map { it.leftField })
        assertEquals(listOf("7"), filters.items.last().rightValuesRaw)
        assertEquals(listOf("Current user"), filters.items.last().rightValuesUI)
        assertEquals(today, filters.rangeDataByKey!!.start)
        assertEquals(today.plusDays(4), filters.rangeDataByKey!!.end)
        assertTrue(filters.items.all { it.enabled })
    }

    @Test fun ownPhotosWithAnyAddressClientOrTypeRemainVisible() {
        val rows = listOf(photo(1, 7, "unknown-address", "unknown-client", "94"),
            photo(2, 7, "different-address", "different-client", "31"),
            photo(3, 8, "unknown-address", "unknown-client", "94"))
        assertEquals(listOf(1L, 2L), filter(defaults(), rows))
    }

    @Test fun missingPerformerDictionaryRowDoesNotRemoveIdConstraint() {
        val filters = journalPhotoFilters(7, null, today, today.plusDays(4))
        assertEquals(listOf("7"), filters.items.last().rightValuesRaw)
        assertEquals(listOf("7"), filters.items.last().rightValuesUI)
        assertEquals(listOf(1L), filter(filters, listOf(photo(1, 7), photo(2, 8))))
    }

    @Test fun refreshPreservesManuallySelectedPerformer() {
        val selected = defaults().copy(items = defaults().items.map {
            if (it.leftField == "user_id") it.copy(rightValuesRaw = listOf("8"), rightValuesUI = listOf("Other user"))
            else it
        })
        val refreshed = refresh(selected)
        assertEquals(listOf("8"), refreshed.items.last().rightValuesRaw)
        assertEquals(listOf(2L), filter(refreshed, listOf(photo(1, 7), photo(2, 8))))
    }

    @Test fun refreshPreservesSearchSelectionAndManuallyAddedFilters() {
        val selected = defaults().copy(searchText = "shelf", selectedMode = SelectedMode.ALL,
            items = defaults().items.map {
                if (it.leftField == "user_id") it else it.copy(
                    rightValuesRaw = listOf("chosen"), rightValuesUI = listOf("Chosen"), excludeMode = true)
            })
        val refreshed = refresh(selected)
        assertEquals(selected.searchText, refreshed.searchText)
        assertEquals(selected.selectedMode, refreshed.selectedMode)
        assertEquals(selected.items, refreshed.items)
    }

    @Test fun clearingFiltersDoesNotRestoreDefaultPerformerOnReload() {
        val cleared = defaults().clearEditableValues()
        val refreshed = journalPhotoFilters(7, "Current user", null, null, cleared)
        assertTrue(refreshed.items.all { it.rightValuesRaw.isEmpty() })
        assertNull(refreshed.rangeDataByKey!!.start)
        assertNull(refreshed.rangeDataByKey!!.end)
        assertEquals(listOf(1L, 2L), filter(refreshed, listOf(photo(1, 7), photo(2, 8))))
    }

    @Test fun selectedDateRangeIsNotResetToTodayOnReload() {
        val from = today.minusMonths(1)
        val to = from.plusDays(2)
        val filters = journalPhotoFilters(7, "Current user", from, to, defaults())
        assertEquals(from, filters.rangeDataByKey!!.start)
        assertEquals(to, filters.rangeDataByKey!!.end)
        assertFalse(filters.items.last().excludeMode)
    }

    private fun defaults() = journalPhotoFilters(7, "Current user", today, today.plusDays(4))

    private fun refresh(previous: Filters) =
        journalPhotoFilters(7, "Current user", today, today.plusDays(4), previous)

    private fun photo(id: Long, user: Int, address: String = "1", client: String = "1", type: String = "31"): DataItemUI {
        val fields = listOf("user_id" to user.toString(), "addr_id" to address,
            "client_id" to client, "photo_type" to type).map { (key, value) ->
            FieldValue(key, TextField(key, key), TextField(value, value))
        }
        return DataItemUI(emptyList(), fields, fields, selected = false, stableId = id)
    }

    private fun filter(filters: Filters, rows: List<DataItemUI>) = filterAndSortDataItems(
        rows, filters, emptyList(), emptyList(), filters.rangeDataByKey?.start,
        filters.rangeDataByKey?.end, filters.searchText
    ).items.map { it.stableId }
}
