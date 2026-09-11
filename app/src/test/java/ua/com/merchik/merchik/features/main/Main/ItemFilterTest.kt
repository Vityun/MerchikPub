package ua.com.merchik.merchik.features.main.Main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.features.main.DBViewModels.OptionsDBViewModel

class ItemFilterTest {
    private val all = ItemFilterChoice("all", "All", emptyList())
    private val active = ItemFilterChoice("active", "Active",
        listOf("active", "active_violation", "active_green"))
    private val green = ItemFilterChoice("green", "No signal",
        listOf("active_green", "inactive_green"))
    private val violations = ItemFilterChoice("violations", "Violations",
        listOf("active_violation", "inactive_violation"))

    private fun dropdown(defaultKey: String = "active") = ItemFilter(
        title = "Display",
        clazz = OptionsDB::class,
        modeUI = ModeUI.ONE_SELECT,
        titleContext = "",
        subTitleContext = "",
        leftField = "option_display_state",
        rightField = "option_display_state",
        rightValuesRaw = emptyList(),
        rightValuesUI = emptyList(),
        enabled = true,
        choices = listOf(all, active, green, violations),
        defaultChoiceKey = defaultKey
    )

    @Test
    fun selectionReplacesPreviousValueWithoutAddingRows() {
        val filter = dropdown().selectChoice(active).selectChoice(violations)

        assertEquals(violations.rawValues, filter.rightValuesRaw)
        assertEquals(listOf("Violations"), filter.rightValuesUI)
        assertEquals(violations, filter.selectedChoice)
        assertNull(filter.clazzViewModel)
    }

    @Test
    fun selectingAllRemovesConstraintButKeepsItsLabel() {
        val filter = dropdown().selectChoice(active).selectChoice(all)

        assertEquals(emptyList<String>(), filter.rightValuesRaw)
        assertEquals(listOf("All"), filter.rightValuesUI)
    }

    @Test
    fun clearRestoresConfiguredDefaultRatherThanFirstChoice() {
        val filter = dropdown(defaultKey = "active").selectChoice(violations).clearValues()

        assertEquals(active.rawValues, filter.rightValuesRaw)
        assertEquals(listOf("Active"), filter.rightValuesUI)
    }

    @Test
    fun initializingFilterWithDefaultSelectsActiveIncludingBothSignalColors() {
        val filter = dropdown().clearValues()

        assertEquals(active, filter.selectedChoice)
        assertEquals(listOf("active", "active_violation", "active_green"), filter.rightValuesRaw)
    }

    @Test
    fun ordinaryFilterStillClearsToEmptyLists() {
        val filter = dropdown().copy(
            choices = null,
            clazzViewModel = OptionsDBViewModel::class,
            rightValuesRaw = listOf("123"),
            rightValuesUI = listOf("Visit")
        ).clearValues()

        assertEquals(emptyList<String>(), filter.rightValuesRaw)
        assertEquals(emptyList<String>(), filter.rightValuesUI)
        assertEquals(OptionsDBViewModel::class, filter.clazzViewModel)
    }

    @Test
    fun sameTableFiltersHaveIndependentKeysAndKeepVisitConstraint() {
        val display = dropdown()
        val visit = ItemFilter(
            "Visit", OptionsDB::class, OptionsDBViewModel::class, ModeUI.MULTI_SELECT,
            "Visit", "", "code_dad2", "code_dad2", listOf("123"), listOf("123"), false
        )
        val changed = display.selectChoice(active)
        val filters = listOf(visit, display).map { if (it.key == changed.key) changed else it }

        assertNotEquals(visit.key, display.key)
        assertSame(visit, filters.first())
        assertEquals(listOf("123"), filters.first().rightValuesRaw)
        assertEquals(active, filters.last().selectedChoice)
    }

    @Test
    fun selectingUnknownChoiceDoesNotChangeFilter() {
        val filter = dropdown().selectChoice(active)

        assertSame(filter, filter.selectChoice(ItemFilterChoice("unknown", "Unknown")))
    }

    @Test
    fun pinToggleChangesOnlyUiStateAndCanBeUndone() {
        val filter = dropdown().clearValues().copy(isPinned = false)
        val pinned = filter.onPinChanged(true)

        assertEquals(true, pinned.isPinned)
        assertEquals(filter, pinned.copy(isPinned = false))
        assertEquals(filter, pinned.onPinChanged(false))
    }

    @Test
    fun pinStateSurvivesChangingAndClearingSelection() {
        val pinned = dropdown().copy(isPinned = false).onPinChanged(true)

        assertEquals(true, pinned.selectChoice(green).isPinned)
        assertEquals(true, pinned.selectChoice(green).clearValues().isPinned)
    }

    @Test
    fun pinHandlerIgnoresFiltersWithoutPinControlAndDisabledFilters() {
        val ordinary = dropdown().copy(choices = null)
        val withoutPin = dropdown()
        val disabled = dropdown().copy(isPinned = false, enabled = false)

        assertSame(ordinary, ordinary.onPinChanged(true))
        assertSame(withoutPin, withoutPin.onPinChanged(true))
        assertSame(disabled, disabled.onPinChanged(true))
    }

    @Test
    fun choicesAreResolvedByStableKeyNotStaleTranslatedLabel() {
        val filter = dropdown().selectChoice(active.copy(title = "Old translation"))

        assertEquals(listOf("Active"), filter.rightValuesUI)
    }

    @Test
    fun refreshingDefinitionsCanRestoreChoiceByRawValues() {
        val selected = dropdown().selectChoice(violations)
        val fresh = dropdown()
        val choice = fresh.choices.orEmpty().first { it.rawValues == selected.rightValuesRaw }

        assertEquals(selected.rightValuesRaw, fresh.selectChoice(choice).rightValuesRaw)
        assertEquals(selected.rightValuesUI, fresh.selectChoice(choice).rightValuesUI)
    }

    @Test
    fun explicitAllSelectionIsNotReplacedByActiveDefaultOnRefresh() {
        val selected = dropdown().selectChoice(all)
        val fresh = dropdown()
        val choice = fresh.choices.orEmpty().first { it.rawValues == selected.rightValuesRaw }

        assertEquals(all, fresh.selectChoice(choice).selectedChoice)
    }

    @Test
    fun dropdownChoicesUseNormalRawFieldFiltering() {
        val rows = listOf("active", "inactive", "active_violation", "inactive_violation",
            "active_green", "inactive_green")
            .mapIndexed { index, state ->
                DataItemUI(
                    rawObj = emptyList(),
                    rawFields = listOf(FieldValue(
                        "option_display_state",
                        TextField("option_display_state", "Display"),
                        TextField(state, state)
                    )),
                    fields = emptyList(),
                    selected = false,
                    stableId = index.toLong()
                )
            }
        fun filteredIds(choice: ItemFilterChoice) = filterAndSortDataItems(
            items = rows,
            filters = Filters(items = listOf(dropdown().selectChoice(choice))),
            sortingFields = emptyList(),
            groupingFields = emptyList(),
            rangeStart = null,
            rangeEnd = null,
            searchText = null
        ).items.map { it.stableId }

        assertEquals(listOf(0L, 1L, 2L, 3L, 4L, 5L), filteredIds(all))
        assertEquals(listOf(0L, 2L, 4L), filteredIds(active))
        assertEquals(listOf(4L, 5L), filteredIds(green))
        assertEquals(listOf(2L, 3L), filteredIds(violations))
    }
}
