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

    private fun row(id: Long, vararg states: String) = DataItemUI(
        rawObj = emptyList(),
        rawFields = states.map { state -> FieldValue(
            "option_display_state", TextField("option_display_state", "Display"), TextField(state, state)
        ) },
        fields = emptyList(),
        selected = false,
        stableId = id
    )

    private fun applyFilters(rows: List<DataItemUI>, vararg filters: ItemFilter) = filterAndSortDataItems(
        items = rows,
        filters = Filters(items = filters.toList()),
        sortingFields = emptyList(),
        groupingFields = emptyList(),
        rangeStart = null,
        rangeEnd = null,
        searchText = null
    )

    @Test
    fun ordinaryExclusionRemovesAnySelectedValue() {
        val filter = dropdown().copy(
            choices = null,
            excludeMode = true,
            rightValuesRaw = listOf("active", "inactive"),
            rightValuesUI = listOf("Active", "Inactive")
        )
        val result = applyFilters(listOf(row(1, "active"), row(2, "inactive"), row(3, "other")), filter)

        assertEquals(listOf(3L), result.items.map { it.stableId })
        assertEquals(true, result.isActiveFiltered)
    }

    @Test
    fun emptyExclusionDoesNotHideAnythingOrMarkFilteringActive() {
        val filter = dropdown().copy(choices = null, excludeMode = true)
        val rows = listOf(row(1, "active"), row(2, "inactive"), row(3))
        val result = applyFilters(rows, filter)

        assertEquals(rows, result.items)
        assertEquals(false, result.isActiveFiltered)
    }

    @Test
    fun inclusionAndExclusionAreBothApplied() {
        val include = dropdown().selectChoice(active)
        val exclude = dropdown().selectChoice(green).copy(excludeMode = true, key = "exclude")
        val rows = listOf(row(1, "active"), row(2, "active_green"), row(3, "inactive_green"))

        assertEquals(listOf(1L), applyFilters(rows, include, exclude).items.map { it.stableId })
        assertEquals(emptyList<DataItemUI>(), applyFilters(rows, include, include.copy(excludeMode = true)).items)
    }

    @Test
    fun exclusionKeepsMissingFieldsButRemovesRowsWithAnyMatchingField() {
        val filter = dropdown().selectChoice(active)
        val rows = listOf(row(1), row(2, "other"), row(3, "other", "active"))

        assertEquals(listOf(1L, 2L), applyFilters(rows, filter.copy(excludeMode = true)).items.map { it.stableId })
        assertEquals(listOf(3L), applyFilters(rows, filter).items.map { it.stableId })
    }

    @Test
    fun exclusionUsesCaseInsensitiveFieldKeysAndRawValues() {
        val filter = dropdown().copy(
            choices = null, excludeMode = true, leftField = "OPTION_DISPLAY_STATE", rightValuesRaw = listOf("42")
        )
        val numeric = row(1, "placeholder").let { item ->
            item.copy(rawFields = item.rawFields.map { it.copy(value = TextField(42, "Translated name")) })
        }

        assertEquals(listOf(2L), applyFilters(listOf(numeric, row(2, "other")), filter).items.map { it.stableId })
    }

    @Test
    fun selectingAndClearingValuesPreservesExclusionMode() {
        val filter = dropdown().copy(excludeMode = true)
        val selected = filter.selectChoice(green)

        assertEquals(true, selected.excludeMode)
        assertEquals(true, selected.clearValues().excludeMode)
        assertEquals(active.rawValues, selected.clearValues().rightValuesRaw)
        val ordinaryCleared = selected.copy(choices = null).clearValues()
        assertEquals(true, ordinaryCleared.excludeMode)
        assertEquals(emptyList<String>(), ordinaryCleared.rightValuesRaw)
    }

    @Test
    fun exclusionWorksWithDropdownAndEmptyChoice() {
        val rows = listOf(row(1, "active_green"), row(2, "inactive_green"), row(3, "active_violation"))
        val filter = dropdown().copy(excludeMode = true).selectChoice(green)

        assertEquals(listOf(3L), applyFilters(rows, filter).items.map { it.stableId })
        assertEquals(rows, applyFilters(rows, filter.selectChoice(all)).items)
    }

    @Test
    fun exclusionIsAppliedBeforeSortingAndGrouping() {
        val rows = listOf(row(1, "active"), row(2, "inactive"), row(3, "active_green"))
            .map { it.copy(fields = it.rawFields) }
        val result = filterAndSortDataItems(
            items = rows,
            filters = Filters(items = listOf(dropdown().selectChoice(green).copy(excludeMode = true))),
            sortingFields = listOf(SortingField(key = "option_display_state", order = -1)),
            groupingFields = listOf(GroupingField(key = "option_display_state")),
            rangeStart = null,
            rangeEnd = null,
            searchText = null
        )

        assertEquals(setOf(1L, 2L), result.items.map { it.stableId }.toSet())
        assertEquals(2, result.groups.size)
        assertEquals(true, result.isActiveSorted)
        assertEquals(true, result.isActiveGrouped)
    }

    @Test
    fun includeAndExcludeFiltersHaveDifferentIdentitiesEvenAfterCopy() {
        val include = dropdown()
        val exclude = include.copy(excludeMode = true)

        assertEquals(include.key, include.identityKey)
        assertEquals(include.key, exclude.key)
        assertNotEquals(include.identityKey, exclude.identityKey)
    }

    @Test
    fun removingExcludedVisitDoesNotRenameOrClearTheLockedIncludeFilter() {
        val include = dropdown().copy(
            title = "Visit", choices = null, enabled = false,
            rightValuesRaw = listOf("123"), rightValuesUI = listOf("Visit 123")
        )
        val exclude = include.copy(title = "Exclude visit", enabled = true, excludeMode = true)
        val changed = exclude.clearValues()
        val updated = listOf(include, exclude).map {
            if (it.identityKey == changed.identityKey) changed else it
        }

        assertSame(include, updated.first())
        assertEquals("Visit", updated.first().title)
        assertEquals(false, updated.first().enabled)
        assertEquals(listOf("123"), updated.first().rightValuesRaw)
        assertEquals(listOf("Visit 123"), updated.first().rightValuesUI)
        assertEquals("Exclude visit", updated.last().title)
        assertEquals(true, updated.last().excludeMode)
        assertEquals(emptyList<String>(), updated.last().rightValuesRaw)
        val rows = listOf(row(1, "123"), row(2, "456"))
        assertEquals(emptyList<DataItemUI>(), applyFilters(rows, include, exclude).items)
        assertEquals(listOf(1L), applyFilters(rows, *updated.toTypedArray()).items.map { it.stableId })
    }

    @Test
    fun identitySurvivesChangingLabelsAndClearingValues() {
        val exclude = dropdown().copy(excludeMode = true).selectChoice(green)

        assertEquals(exclude.identityKey, exclude.copy(title = "Translated title").identityKey)
        assertEquals(exclude.identityKey, exclude.clearValues().identityKey)
    }

    @Test
    fun pickerReadsSelectionFromTheRequestedFilterNotFirstOfSameTable() {
        val include = dropdown().selectChoice(active)
        val exclude = dropdown().copy(excludeMode = true).selectChoice(green)
        val filters = listOf(include, exclude)
        val selectedFilter = filters.firstOrNull {
            it.isSelectionTarget(OptionsDB::class, exclude.identityKey)
        }

        assertSame(exclude, selectedFilter)
        assertEquals(green.rawValues, selectedFilter?.rightValuesRaw)
    }

    @Test
    fun pickerResultUpdatesOnlyTheRequestedFilter() {
        val include = dropdown().copy(choices = null, rightValuesRaw = listOf("123"), enabled = false)
        val exclude = include.copy(excludeMode = true, enabled = true)
        val filters = listOf(include, exclude)
        val updated = filters.map {
            if (it.isSelectionTarget(OptionsDB::class, exclude.identityKey)) {
                it.copy(rightValuesRaw = listOf("456"), rightValuesUI = listOf("Visit 456"))
            } else it
        }

        assertSame(include, updated.first())
        assertEquals(listOf("456"), updated.last().rightValuesRaw)
        assertEquals(true, updated.last().excludeMode)
    }

    @Test
    fun pickerTargetDoesNotFallbackWhenAnExplicitKeyIsMissing() {
        val include = dropdown()
        val exclude = include.copy(excludeMode = true)

        assertEquals(false, include.isSelectionTarget(OptionsDB::class, "missing"))
        assertEquals(false, exclude.isSelectionTarget(OptionsDB::class, "missing"))
        assertEquals(true, include.isSelectionTarget(OptionsDB::class, null))
    }

    @Test
    fun customFilterKeysRemainIndependent() {
        val first = dropdown().copy(key = "first", excludeMode = true)
        val second = first.copy(key = "second")

        assertNotEquals(first.identityKey, second.identityKey)
        assertEquals(false, first.isSelectionTarget(OptionsDB::class, second.identityKey))
    }
}
