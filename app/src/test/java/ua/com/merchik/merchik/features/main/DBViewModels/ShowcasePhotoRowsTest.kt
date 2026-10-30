package ua.com.merchik.merchik.features.main.DBViewModels

import org.junit.Assert.*
import org.junit.Test
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB

class ShowcasePhotoRowsTest {
    private val visit = WpDataDB().apply {
        addr_id = 36097
        client_id = "92217"
        code_dad2 = 1301026036097061393L
        code_iza = "visit-iza"
    }

    private fun showcase(id: Int, photoId: Int?) = ShowcaseSDB().apply {
        this.id = id
        this.photoId = photoId
        nm = "Showcase $id"
        mainOptionId = 574
        status = 1
    }

    private fun photo(serverId: String, path: String = "sample.jpg") = StackPhotoDB().apply {
        id = 100
        photoServerId = serverId
        photo_num = path
        photo_size = "Small"
        photo_hash = "sample-hash"
    }

    @Test fun keepsAllThreeShowcasesWhenOneHasNoSampleId() {
        val rows = ShowcasePhotoRows.build(
            listOf(showcase(68384, 57873221), showcase(68385, 0), showcase(68386, 57873218)),
            listOf(photo("57873221"), photo("57873218")), visit
        )
        assertEquals(listOf(68384, 68385, 68386), rows.map { it.showcaseId })
        assertEquals(listOf(-68384, -68385, -68386), rows.map { it.id })
        assertEquals("Showcase 68385", rows[1].showcaseName)
        assertEquals("", rows[1].photoServerId)
        assertNull(rows[1].photo_num)
    }

    @Test fun nullAndNegativeSampleIdsDoNotHideShowcases() {
        val rows = ShowcasePhotoRows.build(listOf(showcase(1, null), showcase(2, -5)), emptyList(), visit)
        assertEquals(2, rows.size)
        assertTrue(rows.all { it.photoServerId == "" && it.photo_num == null })
    }

    @Test fun unloadedPhotoKeepsNameMetadataAndStableIdentity() {
        val item = showcase(7, 81).apply { nm = ""; selectName = "Fallback name" }
        val unloaded = ShowcasePhotoRows.build(listOf(item), emptyList(), visit).single()
        val loaded = ShowcasePhotoRows.build(listOf(item), listOf(photo("81")), visit).single()
        assertEquals(unloaded.id, loaded.id)
        assertEquals("Fallback name", unloaded.showcaseName)
        assertEquals("81", unloaded.photoServerId)
        assertEquals(574, unloaded.mainOption)
        assertEquals(1, unloaded.statusShowcase)
        assertEquals(visit.code_dad2, unloaded.code_dad2)
        assertEquals(visit.addr_id, unloaded.addr_id)
        assertEquals(visit.client_id, unloaded.client_id)
        assertEquals(visit.code_iza, unloaded.code_iza)
    }

    @Test fun sharedSampleProducesSeparateRowsWithoutMutatingPhoto() {
        val sample = photo("81").apply { showcaseId = 999; specialCol = 7 }
        val rows = ShowcasePhotoRows.build(listOf(showcase(1, 81), showcase(2, 81)), listOf(sample), visit)
        assertEquals(2, rows.size)
        assertNotSame(rows[0], rows[1])
        assertNotEquals(rows[0].id, rows[1].id)
        assertTrue(rows.all { it.photo_num == "sample.jpg" && it.photoServerId == "81" })
        rows[0].specialCol = 1
        assertEquals(0, rows[1].specialCol)
        assertEquals(999, sample.showcaseId)
        assertEquals(7, sample.specialCol)
    }

    @Test fun duplicatePhotoRowsUseFirstPhotoAndDuplicateShowcaseIdsProduceOneRow() {
        val rows = ShowcasePhotoRows.build(
            listOf(showcase(1, 81), showcase(1, 81)),
            listOf(photo("81", "first.jpg"), photo("81", "second.jpg")), visit
        )
        assertEquals(1, rows.size)
        assertEquals("first.jpg", rows.single().photo_num)
    }

    @Test fun completedPhotoWithoutExampleMarksShowcaseWithoutSample() {
        val rows = ShowcasePhotoRows.build(listOf(showcase(1, null), showcase(2, 81)), emptyList(), visit)
        val completed = StackPhotoDB().apply { showcase_id = "1"; example_img_id = "0" }
        ShowcasePhotoRows.markCompleted(rows, listOf(completed))
        assertEquals(listOf(1, 2), rows.map { it.specialCol })
    }

    @Test fun sharedOrOutdatedSampleDoesNotMarkAnotherShowcase() {
        val rows = ShowcasePhotoRows.build(listOf(showcase(1, 81), showcase(2, 81)), emptyList(), visit)
        val completed = StackPhotoDB().apply { showcase_id = "2"; example_img_id = "80" }
        ShowcasePhotoRows.markCompleted(rows, listOf(completed, completed))
        assertEquals(listOf(2, 1), rows.map { it.specialCol })
    }

    @Test fun photoWithoutShowcaseAndUnrelatedShowcaseCannotMarkRows() {
        val rows = ShowcasePhotoRows.build(listOf(showcase(1, 81)), emptyList(), visit)
        val completed = listOf(null, "", "0", "-1", "oops", "99").map { id ->
            StackPhotoDB().apply { showcase_id = id; example_img_id = "81" }
        }
        ShowcasePhotoRows.markCompleted(rows, completed)
        assertEquals(2, rows.single().specialCol)
    }
}
