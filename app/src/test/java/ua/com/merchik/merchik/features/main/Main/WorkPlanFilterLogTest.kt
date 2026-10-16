package ua.com.merchik.merchik.features.main.Main

import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import java.time.LocalDate

class WorkPlanFilterLogTest {
    @Test
    fun reportsCountsDateSearchAndActualFilterModes() {
        val filters = Filters(
            rangeDataByKey = RangeDate("dt", null, null, true),
            searchText = "Market",
            items = listOf(filter("addr_txt", listOf("Address"), true),
                filter("status", listOf("1")), filter("client_txt", emptyList()))
        )
        val json = JsonParser.parseString(buildWorkPlanFilterLog(
            "WP_DATA_IN_CONTAINER", 34, listOf(item(1161026023235061393L)), filters,
            LocalDate.of(2026, 10, 16), LocalDate.of(2026, 10, 20)
        )).asJsonObject
        assertEquals(34, json["before"].asInt)
        assertEquals(1, json["after"].asInt)
        assertEquals("2026-10-16", json.getAsJsonObject("date")["from"].asString)
        assertEquals("2026-10-20", json.getAsJsonObject("date")["to"].asString)
        assertEquals("Market", json["search"].asString)
        assertEquals(2, json.getAsJsonArray("filters").size())
        assertEquals("exclude", json.getAsJsonArray("filters")[0].asJsonObject["mode"].asString)
        assertEquals("include", json.getAsJsonArray("filters")[1].asJsonObject["mode"].asString)
        assertEquals("1161026023235061393", json.getAsJsonArray("visibleDad2")[0].asString)
    }

    @Test
    fun emptyAndUnfilteredResultsAreRepresentedWithoutInventingConditions() {
        val json = JsonParser.parseString(summary(emptyList())).asJsonObject
        assertEquals(0, json["after"].asInt)
        assertEquals(0, json.getAsJsonArray("filters").size())
        assertEquals(0, json["visibleDad2Count"].asInt)
        assertFalse(json.has("date"))
        assertFalse(json.has("error"))
    }

    @Test
    fun identicalResultAndDifferentDisplayOrderHaveSameLogForDeduplication() {
        assertEquals(summary(listOf(item(1), item(2))), summary(listOf(item(2), item(1))))
    }

    @Test
    fun replacementOfVisitIsVisibleEvenWithSameCount() {
        assertNotEquals(summary(listOf(item(1), item(2))), summary(listOf(item(1), item(3))))
    }

    @Test
    fun longListsAreBoundedButChangesOutsidePreviewAreDetectable() {
        val first = (1L..20L).map { item(it) }
        val second = (1L..19L).map { item(it) } + item(21)
        val json = JsonParser.parseString(summary(first)).asJsonObject
        assertEquals(12, json.getAsJsonArray("visibleDad2").size())
        assertEquals(20, json["visibleDad2Count"].asInt)
        assertNotEquals(summary(first), summary(second))
    }

    @Test
    fun valuesAreBoundedAndEscapedAndReadOnlyFiltersStillAppear() {
        val values = (1..20).map { "Address $it\n\"" + "x".repeat(150) }
        val filters = Filters(items = listOf(filter("addr_txt", values)))
        val json = JsonParser.parseString(buildWorkPlanFilterLog(
            "WP_DATA_IN_CONTAINER", 34, emptyList(), filters, null, null
        )).asJsonObject
        val active = json.getAsJsonArray("filters")[0].asJsonObject
        assertEquals(20, active["count"].asInt)
        assertEquals(12, active.getAsJsonArray("values").size())
        assertEquals(120, active.getAsJsonArray("values")[0].asString.length)
        assertTrue(active.getAsJsonArray("values")[0].asString.contains("\n\""))
    }

    @Test
    fun failureIsDistinguishableFromEmptyFilterResult() {
        val json = JsonParser.parseString(buildWorkPlanFilterLog(
            "WP_DATA_IN_CONTAINER", 34, emptyList(), null, null, null, "IllegalStateException"
        )).asJsonObject
        assertEquals("IllegalStateException", json["error"].asString)
    }

    private fun summary(items: List<DataItemUI>) = buildWorkPlanFilterLog(
        "WP_DATA_IN_CONTAINER", 34, items, null, null, null
    )

    private fun item(dad2: Long): DataItemUI {
        val wp = WpDataDB().apply { code_dad2 = dad2 }
        return DataItemUI(listOf(wp), emptyList(), emptyList(), selected = false, stableId = dad2)
    }

    private fun filter(field: String, values: List<String>, exclude: Boolean = false) = ItemFilter(
        field, WpDataDB::class, modeUI = ModeUI.MULTI_SELECT, titleContext = "",
        subTitleContext = "", leftField = field, rightField = field, rightValuesRaw = values,
        rightValuesUI = values, enabled = false, excludeMode = exclude
    )
}
