package ua.com.merchik.merchik.features.main.tar

import org.junit.Assert.*
import org.junit.Test
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.database.realm.tables.TasksAndReclamationsRealm
import ua.com.merchik.merchik.features.main.DBViewModels.tarHomeFilters
import ua.com.merchik.merchik.features.main.DBViewModels.TarHomeHeading
import ua.com.merchik.merchik.features.main.DBViewModels.tarHomeHeading
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.SortingField
import ua.com.merchik.merchik.features.main.formatDateString
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TarHomeTest {
    @Test fun taskHeadingHasOnlyActiveCount() {
        val rows = List(6) { TarListItem(state = 0) } + List(17) { TarListItem(state = 1) }
        assertEquals(TarHomeHeading("Завдання", 6), tarHomeHeading(1, rows))
    }

    @Test fun reclamationHeadingExcludesAllInactiveStatuses() {
        val rows = (0..3).map { TarListItem(tp = 0, state = it) }
        assertEquals(TarHomeHeading("Рекламації", 1), tarHomeHeading(0, rows))
    }

    @Test fun emptyListHasZeroCountersInBothContexts() {
        assertEquals(TarHomeHeading("Завдання", 0), tarHomeHeading(1, emptyList()))
        assertEquals(TarHomeHeading("Рекламації", 0), tarHomeHeading(0, emptyList()))
    }

    @Test fun headingReflectsStatusChangesAndDeletions() {
        val rows = listOf(TarListItem(id = 1, state = 0), TarListItem(id = 2, state = 0))
        assertEquals(TarHomeHeading("Завдання", 2), tarHomeHeading(1, rows))
        assertEquals(TarHomeHeading("Завдання", 1), tarHomeHeading(1, rows.drop(1)))
        val completed = rows.map { it.copy(state = 1) }
        assertEquals(TarHomeHeading("Завдання", 0), tarHomeHeading(1, completed))
        assertEquals(TarHomeHeading("Завдання", 0), tarHomeHeading(1, completed.drop(1)))
    }

    @Test fun nullableServerFieldsAreSafe() {
        val row = TarListItem.from(TasksAndReclamationsSDB())
        assertEquals(0, row.id)
        assertEquals("", row.address)
        assertEquals("", row.customer)
        assertEquals("", row.comment)
    }

    @Test fun joinedNamesAndOriginalIdentifiersArePreserved() {
        val source = TasksAndReclamationsSDB().apply {
            id = 12; dt = 123L; id1c = "T-12"; tp = 1; state = 0
            addr = 7; addrNm = "Address"; client = "42"; clientNm = "Customer"
            vinovnik = 8; sortNm = "User"; comment = "Task text"; photo = 99
        }
        val row = TarListItem.from(source)
        assertEquals(12, row.id)
        assertEquals("T-12", row.id1c)
        assertEquals("Address", row.address)
        assertEquals(7, row.addr)
        assertEquals("Customer", row.customer)
        assertEquals("42", row.client)
        assertEquals("User", row.responsible)
        assertEquals(99, row.photo)
        assertEquals("Task text", row.comment)
    }

    @Test fun missingNamesFallBackToIdentifiers() {
        val source = TasksAndReclamationsSDB().apply {
            addr = 7; client = "42"; vinovnik = 8
            addrNm = " "; clientNm = ""; sortNm = null
        }
        val row = TarListItem.from(source)
        assertEquals("7", row.address)
        assertEquals("42", row.customer)
        assertEquals("8", row.responsible)
    }

    @Test fun taskAmountsKeepLegacyRules() {
        val source = TasksAndReclamationsSDB().apply {
            tp = 1; sumPenalty = "10"; sumPremiya = "20"; state = 0
        }
        assertEquals("10 / 20", TarListItem.from(source).amount)
        source.state = 1
        assertEquals("10 / 0", TarListItem.from(source).amount)
        assertEquals("20", source.sumPremiya)
    }

    @Test fun reclamationAmountsKeepLegacyRules() {
        val source = TasksAndReclamationsSDB().apply { tp = 0; sumPenalty = "10"; state = 0 }
        assertEquals("10 / -10", TarListItem.from(source).amount)
        source.state = 1
        assertEquals("10 / 0", TarListItem.from(source).amount)
        source.state = 2
        assertEquals("", TarListItem.from(source).amount)
    }

    @Test fun taskStatusesAreUkrainianAcrossCardsAndFilters() {
        assertStatusLabels(1, listOf("Активні", "Виконані", "Не виконані", "Скасовані"))
    }

    @Test fun reclamationStatusesAreUkrainianAcrossCardsAndFilters() {
        assertStatusLabels(0, listOf("Активні", "Виправлені", "Скасовані", "Строк виконання минув"))
    }

    @Test fun unknownStatusesHaveUkrainianFallbacks() {
        for (type in 0..1) {
            assertEquals("Невідомий статус", TarListItem(tp = type, state = 99).getValueUI("state", 99))
        }
        assertEquals("Не вдалося визначити", TasksAndReclamationsRealm.getStatusTxt(99, 0))
    }

    @Test fun datesMatchWorkPlanFormatInLocalTimeZone() {
        val originalLocale = Locale.getDefault()
        val originalZone = TimeZone.getDefault()
        try {
            val seconds = Instant.parse("2026-11-18T22:30:00Z").epochSecond
            val row = TarListItem(dt = seconds)
            for (locale in listOf(Locale("uk", "UA"), Locale.US)) {
                Locale.setDefault(locale)
                for (zone in listOf("Europe/Kiev", "UTC")) {
                    TimeZone.setDefault(TimeZone.getTimeZone(zone))
                    val actual = row.getValueUI("dt", seconds)
                    assertEquals(formatDateString(Date(seconds * 1000).toString()), actual)
                    assertTrue(actual.endsWith("2026"))
                    assertTrue(actual.startsWith(if (zone == "UTC") "18 " else "19 "))
                    assertEquals(seconds, row.dt)
                }
            }
        } finally {
            Locale.setDefault(originalLocale)
            TimeZone.setDefault(originalZone)
        }
    }

    @Test fun missingAndInvalidDatesRemainEmpty() {
        for (value in listOf(0L, -1L, "", "invalid")) {
            assertEquals("", TarListItem().getValueUI("dt", value))
        }
    }

    private fun assertStatusLabels(type: Int, expected: List<String>) {
        val rows = expected.indices.map { TarListItem(tp = type, state = it) }
        assertEquals(expected, rows.map { it.getValueUI("state", it.state) })
        val choices = tarHomeFilters(type, 0, Filters()).items.first().choices!!
        assertEquals(listOf("Усі") + expected, choices.map { it.title })
        assertEquals(expected.indices.map { listOf(it.toString()) }, choices.drop(1).map { it.rawValues })
        val legacyStatuses = TasksAndReclamationsRealm.getTaRStatus(type)
        assertEquals(expected, legacyStatuses.map { it.nm })
        assertEquals(expected.indices.toList(), legacyStatuses.map { it.id })
    }

    @Test fun activeIsDefaultForBothLists() {
        for (type in 0..1) {
            val filter = tarHomeFilters(type, 0, Filters()).items.first()
            assertEquals(listOf("0"), filter.rightValuesRaw)
            assertEquals(5, filter.choices!!.size)
        }
    }

    @Test fun explicitTaskLinkIsNotHiddenByActiveFilter() {
        val filter = tarHomeFilters(0, 12, Filters()).items.first()
        assertTrue(filter.rightValuesRaw.isEmpty())
        assertEquals("all", filter.selectedChoice!!.key)
    }

    @Test fun refreshingPreservesSearchAndChosenFilters() {
        val original = tarHomeFilters(1, 0, Filters())
        val changed = original.copy(searchText = "test", items = original.items.map {
            when (it.leftField) {
                "state" -> it.selectChoice(it.choices!!.first { choice -> choice.key == "all" })
                "client" -> it.copy(rightValuesRaw = listOf("42"), rightValuesUI = listOf("Customer"))
                else -> it
            }
        })
        assertEquals(changed, tarHomeFilters(1, 0, changed))
    }

    @Test fun filtersCombineStatusClientAndAddress() {
        val filters = tarHomeFilters(1, 0, Filters()).let { base -> base.copy(items = base.items.map {
            when (it.leftField) {
                "client" -> it.copy(rightValuesRaw = listOf("42"))
                "addr" -> it.copy(rightValuesRaw = listOf("7"))
                else -> it
            }
        }) }
        val result = filterAndSortDataItems(
            listOf(item(1, 0, "42", 7), item(2, 1, "42", 7), item(3, 0, "43", 7), item(4, 0, "42", 8)),
            filters, emptyList(), emptyList(), null, null, ""
        )
        assertEquals(listOf(1L), result.items.map { it.stableId })
    }

    @Test fun newestRowsFirstAndAllStatusSelection() {
        val filters = tarHomeFilters(1, 0, Filters()).let { base -> base.copy(items = base.items.map {
            if (it.leftField == "state") it.selectChoice(it.choices!!.first()) else it
        }) }
        val result = filterAndSortDataItems(
            listOf(item(1, 0), item(2, 1)), filters,
            listOf(SortingField(key = "dt", order = -1)), emptyList(), null, null, ""
        )
        assertEquals(listOf(2L, 1L), result.items.map { it.stableId })
    }

    private fun item(id: Long, state: Int, client: String = "42", addr: Int = 7): DataItemUI {
        val fields = listOf("state" to state, "client" to client, "addr" to addr, "dt" to id)
            .map { (key, value) -> FieldValue(key, TextField(key, key), TextField(value, value.toString())) }
        return DataItemUI(emptyList(), fields, fields, selected = false, stableId = id)
    }
}
