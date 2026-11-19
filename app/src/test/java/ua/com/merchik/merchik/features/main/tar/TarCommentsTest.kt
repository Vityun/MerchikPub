package ua.com.merchik.merchik.features.main.tar

import org.junit.Assert.*
import org.junit.Test
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.features.main.DBViewModels.tarCommentsFilters
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.RangeDate
import ua.com.merchik.merchik.features.main.Main.SortingField
import java.io.File
import java.time.LocalDate
import java.time.ZoneOffset

class TarCommentsTest {
    @Test fun subtitleUsesTaskOrReclamationContext() {
        assertEquals("Текст завдання: Task", tarCommentsSubtitle(1, "Task"))
        assertEquals("Текст рекламації: Claim", tarCommentsSubtitle(0, "Claim"))
    }

    @Test fun nullableFieldsAndMalformedDatesAreSafe() {
        for (date in listOf(null, "", "bad", "-1", "0")) {
            val row = TarCommentItem.from(TARCommentsDB().apply { dt = date }, null, "")
            assertEquals(0L, row.dt)
            assertEquals("", row.time)
            assertEquals("", row.author)
            assertEquals(0, row.hasPhoto)
        }
    }

    @Test fun snapshotPreservesIdentityTextAndAuthor() {
        val source = TARCommentsDB().apply {
            id = "1234567890123"; rId = "42"; dt = "1795086000"; who = "12"
            comment = "<b>Message</b>"; photo = "60044599"; startUpdate = true
        }
        val snapshot = TarCommentItem.from(source, "Author", "Message")
        assertEquals(source.id, snapshot.id)
        assertEquals("Author", snapshot.author)
        assertEquals("12", snapshot.who)
        assertEquals("Message", snapshot.comment)
        assertEquals("60044599", snapshot.photo)
        assertEquals(8, snapshot.time.length)
        assertTrue(source.startUpdate)
        source.comment = "Changed"
        assertEquals("Message", snapshot.comment)
    }

    @Test fun unknownAuthorFallsBackToId() {
        val source = TARCommentsDB().apply { who = "12" }
        assertEquals("12", TarCommentItem.from(source, null, "").author)
        assertEquals("12", TarCommentItem.from(source, " ", "").author)
    }

    @Test fun serverPhotoAndPendingLocalPhotoAreBothAttachments() {
        for ((server, hash) in listOf("60044599" to "", "0" to "local-photo-hash")) {
            val row = TarCommentItem.from(TARCommentsDB().apply { photo = server; photo_hash = hash }, null, "")
            assertEquals(1, row.hasPhoto)
            assertEquals(R.mipmap.merchik_m, row.getIdResImage())
        }
    }

    @Test fun absentPhotoHasCameraAction() {
        for (value in listOf(null, "", "0", "null", " ")) {
            val row = TarCommentItem.from(TARCommentsDB().apply { photo = value; photo_hash = value }, null, "")
            assertEquals(0, row.hasPhoto)
            assertEquals(R.drawable.ic_47, row.getIdResImage())
        }
    }

    @Test fun emptyConversationDoesNotApplyDateOrAuthorRestriction() {
        val filters = tarCommentsFilters(emptyList(), Filters())
        assertNull(filters.rangeDataByKey)
        assertTrue(filters.items.all { it.rightValuesRaw.isEmpty() })
    }

    @Test fun initialConversationIncludesOldCommentsDespiteMainUiDefaultDates() {
        val rows = listOf(
            TarCommentItem(id = "1", dt = timestamp("2026-09-24")),
            TarCommentItem(id = "2", dt = timestamp("2026-10-15")),
            TarCommentItem(id = "3", dt = timestamp("2026-11-19"))
        )
        val filters = tarCommentsFilters(rows, Filters())
        assertEquals(listOf(3L, 2L, 1L), filtered(rows, filters))
    }

    @Test fun oldHiddenDateFilterIsRemovedWithoutClearingExplicitFilters() {
        val rows = listOf(
            TarCommentItem(id = "1", dt = timestamp("2026-09-24"), who = "7", hasPhoto = 1, comment = "Shelf"),
            TarCommentItem(id = "2", dt = timestamp("2026-09-25"), who = "8", hasPhoto = 1, comment = "Shelf")
        )
        val initial = tarCommentsFilters(rows, Filters(searchText = "Shelf"))
        val previous = initial.copy(
            rangeDataByKey = RangeDate("dt", LocalDate.of(2026, 11, 19), LocalDate.of(2026, 11, 23), false),
            items = initial.items.map { filter ->
                filter.selectChoice(filter.choices!!.first { it.key == if (filter.leftField == "who") "7" else "1" })
            }
        )
        val refreshed = tarCommentsFilters(rows, previous)
        assertNull(refreshed.rangeDataByKey)
        assertEquals("Shelf", refreshed.searchText)
        assertEquals(listOf(1L), filtered(rows, refreshed))
    }

    @Test fun missingDateDoesNotHideComment() {
        val rows = listOf(TarCommentItem(id = "1", dt = 0, comment = "Undated"))
        assertEquals(listOf(1L), filtered(rows, tarCommentsFilters(rows, Filters())))
    }

    @Test fun authorChoicesAreUniqueAndRefreshPreservesSelectionAndSearch() {
        val rows = listOf(TarCommentItem(who = "1", author = "First"), TarCommentItem(who = "1", author = "First"))
        val initial = tarCommentsFilters(rows, Filters(searchText = "text"))
        val author = initial.items.first()
        assertEquals(2, author.choices!!.size)
        val selected = initial.copy(items = listOf(author.selectChoice(author.choices!!.last())))
        val refreshed = tarCommentsFilters(rows + TarCommentItem(who = "2", author = "Next"), selected)
        assertEquals(listOf("1"), refreshed.items.first().rightValuesRaw)
        assertEquals(3, refreshed.items.first().choices!!.size)
        assertEquals("text", refreshed.searchText)
    }

    @Test fun searchFindsTextAndAuthor() {
        val rows = listOf(TarCommentItem(id = "1", author = "Iryna", comment = "Shelf photo"),
            TarCommentItem(id = "2", author = "Olena", comment = "Stock"))
        assertEquals(listOf(1L), filtered(rows, Filters(searchText = "shelf")))
        assertEquals(listOf(2L), filtered(rows, Filters(searchText = "Olena")))
    }

    @Test fun authorAndPhotoFiltersWorkTogether() {
        val rows = listOf(TarCommentItem(id = "1", who = "7", hasPhoto = 1),
            TarCommentItem(id = "2", who = "7", hasPhoto = 0),
            TarCommentItem(id = "3", who = "8", hasPhoto = 1))
        val initial = tarCommentsFilters(rows, Filters())
        val selected = initial.copy(items = initial.items.map { filter ->
            filter.selectChoice(filter.choices!!.first { it.key == if (filter.leftField == "who") "7" else "1" })
        })
        assertEquals(listOf(1L), filtered(rows, selected))
    }

    @Test fun timestampsSortNumericallyNewestFirst() {
        val rows = listOf(TarCommentItem(id = "1", dt = 99), TarCommentItem(id = "2", dt = 100))
        assertEquals(listOf(2L, 1L), filtered(rows, Filters()))
    }

    @Test fun addActionIsOutsideFilteringAndGroupingAndUsesSameHandlerAsPlus() {
        val source = File("app/src/main/java/ua/com/merchik/merchik/features/main/Main/MainUI.kt").readText()
        val header = source.indexOf("item(key = \"tar_add_comment\")")
        assertTrue(header > 0)
        assertTrue(source.indexOf("if (!isActiveGrouped || groups.isEmpty())", header) > header)
        assertTrue(source.contains("TarAddCommentItem(viewModel::onClickAdditionalContent)"))
    }

    @Test fun photoAttachmentUsesStableCommentIdAndPreservesUploadFlags() {
        val source = File("app/src/main/java/ua/com/merchik/merchik/Activities/TaskAndReclamations/TasksActivity/Tab3Fragment.java").readText()
        assertTrue(source.contains("getTARCommentById(pendingPhotoCommentId)"))
        assertTrue(source.contains("tarCommentsDB.startUpdate = true"))
        assertTrue(source.contains("row.startUpdate = true"))
        assertFalse(source.contains("dataComments.get(tarCommentIndex)"))
        assertTrue(source.contains("isPhotoRequiredForTheme()"))
    }

    private fun filtered(rows: List<TarCommentItem>, filters: Filters) = filterAndSortDataItems(
        items = rows.map { row ->
            val fields = listOf("dt" to row.dt, "who" to row.who, "author" to row.author,
                "comment" to row.comment, "hasPhoto" to row.hasPhoto).map { (key, value) ->
                FieldValue(key, TextField(key, key), TextField(value, value.toString()))
            }
            DataItemUI(listOf(row), fields, fields, selected = false, stableId = row.id.toLong())
        }, filters = filters, sortingFields = listOf(SortingField("dt", "Дата", -1)),
        groupingFields = emptyList(), rangeStart = LocalDate.of(2026, 11, 19),
        rangeEnd = LocalDate.of(2026, 11, 23), searchText = filters.searchText
    ).items.map { it.stableId }

    private fun timestamp(date: String) = LocalDate.parse(date).atTime(12, 0).toEpochSecond(ZoneOffset.UTC)
}
