package ua.com.merchik.merchik.features.main.tar

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.features.main.DBViewModels.tarCommentsFilters
import ua.com.merchik.merchik.features.main.DBViewModels.tarHomeFilters
import ua.com.merchik.merchik.features.main.Main.Filters
import java.io.File

class TarModelSerializationTest {
    private val gson = Gson()

    @Test fun shrinkerProtectsAllMainUiModelNamesAndMembers() {
        val rules = File("app/proguard-rules.pro").readLines()
            .joinToString(" ") { it.substringBefore('#') }
            .replace(Regex("\\s+"), " ")
        assertTrue(rules.contains(
            "-keep class ua.com.merchik.merchik.** implements " +
                "ua.com.merchik.merchik.dataLayer.DataObjectUI { *; }"
        ))
    }

    @Test fun modelNamesRemainStableForSavedTableSettings() {
        assertEquals("TarListItem", TarListItem::class.java.simpleName)
        assertEquals("TarCommentItem", TarCommentItem::class.java.simpleName)
    }

    @Test fun listSerializationPreservesCardFilterAndPhotoKeys() {
        assertKeys(TarListItem(), setOf(
            "id", "dt", "id1c", "state", "tp", "addr", "address", "client",
            "customer", "responsible", "amount", "comment", "photo", "photoHash", "theme"
        ))
    }

    @Test fun commentSerializationPreservesCardFilterAndPhotoKeys() {
        assertKeys(TarCommentItem(), setOf(
            "id", "dt", "time", "who", "author", "comment", "photo", "photoHash", "hasPhoto"
        ))
    }

    @Test fun populatedModelsSurviveGsonRoundTrip() {
        val task = TarListItem(12, 123L, "T-12", 0, 1, 7, "Address", "42",
            "Customer", "User", "10 / 20", "Task text", 99, "hash", theme = "Shelf layout")
        val comment = TarCommentItem("1234567890123", 123L, "12:00:00", "7", "Author",
            "Comment text", "99", "hash", 1)
        assertEquals(task, gson.fromJson(gson.toJson(task), TarListItem::class.java))
        assertEquals(comment, gson.fromJson(gson.toJson(comment), TarCommentItem::class.java))
    }

    @Test fun gsonFieldsFeedActiveTaskAndReclamationFilters() {
        for (type in 0..1) {
            val rows = listOf(TarListItem(id = 1, tp = type, state = 0),
                TarListItem(id = 2, tp = type, state = 1))
            assertEquals(listOf(1L), filtered(rows, tarHomeFilters(type, 0, Filters())))
        }
    }

    @Test fun gsonFieldsFeedCommentAuthorAndPhotoFilters() {
        val rows = listOf(TarCommentItem(id = "1", who = "7", hasPhoto = 1),
            TarCommentItem(id = "2", who = "7", hasPhoto = 0),
            TarCommentItem(id = "3", who = "8", hasPhoto = 1))
        val initial = tarCommentsFilters(rows, Filters())
        val selected = initial.copy(items = initial.items.map { filter ->
            filter.selectChoice(filter.choices!!.first {
                it.key == if (filter.leftField == "who") "7" else "1"
            })
        })
        assertEquals(listOf(1L), filtered(rows, selected))
    }

    private fun assertKeys(row: DataObjectUI, expected: Set<String>) {
        val actual = gson.toJsonTree(row).asJsonObject.entrySet().map { it.key }.toSet()
        assertEquals(expected, actual)
        assertTrue(actual.containsAll(row.getPreferredFieldOrder()))
        assertTrue(actual.containsAll(row.getHidedFieldsOnUI().split(',')))
        assertTrue(actual.containsAll(row.getFieldsImageOnUI().split(',').filter { it.isNotBlank() }))
    }

    private fun filtered(rows: List<DataObjectUI>, filters: Filters): List<Long> {
        // Use real Gson keys, unlike fixtures that bypass serialization with hard-coded fields.
        val items = rows.map { row ->
            val json = gson.toJsonTree(row).asJsonObject
            val fields = json.entrySet().map { (key, value) ->
                FieldValue(key, TextField(key, key), TextField(value.asString, value.asString))
            }
            DataItemUI(listOf(row), fields, fields, selected = false, stableId = json["id"].asLong)
        }
        return filterAndSortDataItems(items, filters, emptyList(), emptyList(),
            null, null, "").items.map { it.stableId }
    }
}
