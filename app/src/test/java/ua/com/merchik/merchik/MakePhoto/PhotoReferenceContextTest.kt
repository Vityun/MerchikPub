package ua.com.merchik.merchik.MakePhoto

import com.google.gson.JsonObject
import org.junit.Assert.*
import org.junit.Test
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.features.main.DBViewModels.ShowcasePhotoRows

class PhotoReferenceContextTest {
    private fun task(source: Long? = 0) = TasksAndReclamationsSDB().apply {
        codeDad2 = 1221026015545279537L
        codeDad2SrcDoc = source
        addr = 15545
        client = "05327"
        addrNm = "Task address"
        clientNm = "Task client"
    }

    @Test
    fun taskWithoutVisitUsesItsAddressClientAndDocument() {
        for (source in listOf(null, 0L, -1L)) {
            val context = PhotoReferenceContext.fromTaskWithoutVisit(task(source))
            assertEquals(1221026015545279537L, context.codeDad2)
            assertEquals(15545, context.addressId)
            assertEquals("05327", context.clientId)
            assertEquals("Task address", context.addressName)
            assertEquals("Task client", context.clientName)
            assertEquals("", context.codeIza)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun specifiedSourceCannotSilentlyFallBackToTask() {
        PhotoReferenceContext.fromTaskWithoutVisit(task(1301026015545061512L))
    }

    @Test
    fun actualVisitKeepsItsSelectionContext() {
        val visit = WpDataDB().apply {
            code_dad2 = 1301026015545061512L
            addr_id = 15545
            client_id = "05327"
            addr_txt = "Visit address"
            client_txt = "Visit client"
            code_iza = "12345"
        }
        val context = PhotoReferenceContext.fromVisit(visit)
        assertEquals(visit.code_dad2, context.codeDad2)
        assertEquals(visit.addr_id, context.addressId)
        assertEquals(visit.client_id, context.clientId)
        assertEquals(visit.addr_txt, context.addressName)
        assertEquals(visit.client_txt, context.clientName)
        assertEquals(visit.code_iza, context.codeIza)
    }

    @Test
    fun contextSurvivesSelectorTransportForBothTypesAndSources() {
        for (type in listOf(0, 45)) for (gallery in listOf(false, true)) {
            val context = PhotoReferenceContext.fromTaskWithoutVisit(task())
            val data = JsonObject().apply {
                addProperty(PhotoReferenceSelection.TOKEN, "selection")
                addProperty("photoType", type)
                addProperty("galleryAction", gallery)
            }
            context.addTo(data)
            assertEquals(context, PhotoReferenceContext.fromReferenceJson(data.toString()))
            assertFalse(data.has("wpDataDBId"))
            assertTrue(PhotoReferenceSelection.enabled(data.toString()))
            assertEquals(gallery, PhotoReferenceSelection.gallery(data.toString()))
        }
    }

    @Test
    fun normalVisitAndSampleSelectorsHaveNoTaskFallback() {
        assertNull(PhotoReferenceContext.fromReferenceJson(null))
        assertNull(PhotoReferenceContext.fromReferenceJson("1301026015545061512"))
        assertNull(PhotoReferenceContext.fromReferenceJson("{\"wpDataDBId\":\"1301026015545061512\"}"))
        val data = JsonObject().apply { addProperty(PhotoReferenceSelection.TOKEN, "selection") }
        assertNull(PhotoReferenceContext.fromReferenceJson(data.toString()))
    }

    @Test
    fun contextCannotOverrideNormalVisitOutsideReferenceSelector() {
        val data = JsonObject()
        PhotoReferenceContext.fromTaskWithoutVisit(task()).addTo(data)
        assertNull(PhotoReferenceContext.fromReferenceJson(data.toString()))
    }

    @Test
    fun invalidTaskCannotProduceUnrestrictedSelection() {
        val invalid = listOf(
            task().apply { addr = null }, task().apply { addr = 0 },
            task().apply { client = null }, task().apply { client = " " },
            task().apply { codeDad2 = null }, task().apply { codeDad2 = 0 }
        )
        for (task in invalid) {
            assertTrue(runCatching { PhotoReferenceContext.fromTaskWithoutVisit(task) }.exceptionOrNull() is IllegalArgumentException)
        }
    }

    @Test
    fun showcaseRowsDoNotRequireVisitOrSamplePhoto() {
        val context = PhotoReferenceContext.fromTaskWithoutVisit(task())
        val showcases = listOf(
            ShowcaseSDB().apply { id = 1; nm = "First"; photoId = 0 },
            ShowcaseSDB().apply { id = 2; nm = "Second"; photoId = 90001 }
        )
        val rows = ShowcasePhotoRows.build(showcases, emptyList(), context)
        assertEquals(listOf(1, 2), rows.map { it.showcaseId })
        assertTrue(rows.all { it.addr_id == context.addressId && it.client_id == context.clientId })
        assertTrue(rows.all { it.code_dad2 == context.codeDad2 && it.photo_num == null })
        assertEquals(listOf("First", "Second"), rows.map { it.showcaseName })
    }

    @Test
    fun planogramContextKeepsAddressClientNetworkAndDateWithoutVisit() {
        val context = PhotoReferenceContext.fromTaskWithoutVisit(task())
        val selection = PlanogramPhotoSelection(context, 12, "2026-10-30")
        assertEquals("05327", selection.context.clientId)
        assertEquals(15545, selection.context.addressId)
        assertEquals(12, selection.networkId)
        assertEquals("2026-10-30", selection.date)
    }
}
