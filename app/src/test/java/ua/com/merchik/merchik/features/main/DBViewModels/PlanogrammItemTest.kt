package ua.com.merchik.merchik.features.main.DBViewModels

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammJOINSDB
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.TextField

class PlanogrammItemTest {
    @Test
    fun imageCaptionAndPhotoCountAreNotShown() {
        val plan = PlanogrammSDB().apply { id = 47; planogrammPhoto = 3 }

        assertEquals("", plan.commentsForImage)
        val hidden = plan.hidedFieldsOnUI.split(",").map { it.trim() }
        assertTrue("comments" in hidden)
        assertTrue("planogrammPhoto" in hidden)
        assertFalse("nm" in hidden)
    }

    @Test
    fun selectionRestrictionsCannotBeEdited() {
        val filters = listOf("addr_id", "client_id", "tp_id", "date").map { key ->
            planogramSelectionFilter(key, PlanogrammSDB::class, key, "value", "Display value")
        }

        assertTrue(filters.all { !it.enabled })
        assertEquals(4, filters.map { it.identityKey }.distinct().size)
        assertTrue(filters.all { it.rightValuesUI == listOf("Display value") })
    }

    @Test
    fun contextFiltersDoNotExcludeUnassignedPlansOrOverwriteTheirClient() {
        val plan = PlanogrammSDB().apply { id = 47 }
        val client = FieldValue("client_id", TextField("client_id", "Client"), TextField("", ""))
        val item = DataItemUI(
            rawObj = listOf(plan), rawFields = listOf(client), fields = emptyList(),
            selected = false, stableId = 47L
        )
        val filters = listOf(
            planogramSelectionFilter("Client", PlanogrammSDB::class, "client_id", "00123", "Client"),
            planogramSelectionFilter("Network", PlanogrammSDB::class, "tp_id", "", "Unassigned")
        )

        val result = listOf(item).withPlanogramSelectionFilters(filters)
            .withPlanogramSelectionFilters(filters).single()

        assertNull(plan.clientId)
        assertEquals(client, result.rawFields.single { it.key == "client_id" })
        assertEquals(item.fields, result.fields)
        assertEquals(3, result.rawFields.size)
        assertTrue(filters.all { filter ->
            result.rawFields.any { field ->
                field.key == filter.leftField && filter.rightValuesRaw.contains(field.value.rawValue.toString())
            }
        })
    }

    @Test
    fun keepsPlanogramAndServerPhotoIdentifiersSeparate() {
        val source = PlanogrammJOINSDB().apply {
            id = 47
            planogrammPhotoId = 59682417
            planogrammName = "Planogram"
        }

        val result = source.toPlanogramItem(2)

        assertEquals(47, result.id.toInt())
        assertEquals(59682417L, result.photoId.toLong())
        assertEquals("Planogram", result.nm)
        assertEquals(2, result.planogrammPhoto)
    }

    @Test
    fun missingImageDoesNotUsePlanogramIdAsPhotoId() {
        val result = PlanogrammJOINSDB().apply { id = 47 }.toPlanogramItem(0)

        assertNull(result.photoId)
        assertEquals(0, result.planogrammPhoto)
    }

    @Test
    fun preservesClientAndCommentFromOriginalSelection() {
        val result = PlanogrammJOINSDB().apply {
            id = 47
            planogrammClientId = 123
            planogrammClientTxt = "Client"
            planogrammComment = "Comment"
        }.toPlanogramItem(0)

        assertEquals("123", result.clientId)
        assertEquals("Client", result.clientTxt)
        assertEquals("Comment", result.comments)
    }
}
