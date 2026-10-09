package ua.com.merchik.merchik.MakePhoto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB

class ShowcasePhotoMetadataTest {
    private val showcase = ShowcaseSDB().apply { id = 17; photoId = 90001; planogramId = 25 }

    @Test
    fun visitLinkTakesPriorityOverShowcasePlanogram() {
        val link = PlanogrammVizitShowcaseSDB().apply {
            showcase_id = 17; planogram_id = 30; planogram_photo_id = 90003
        }
        val fallback = PlanogrammSDB().apply { id = 25; photoId = 90002L }
        val result = ShowcasePhotoMetadata.resolve("0", showcase, link, fallback)
        assertEquals("17", result.showcaseId)
        assertEquals("90001", result.imageId)
        assertEquals("30", result.planogramId)
        assertEquals("90003", result.planogramImageId)
        assertFalse(result.needsPlanogram)
    }

    @Test
    fun withoutVisitLinkKeepsExistingPlanogramSelectionRule() {
        val plan = PlanogrammSDB().apply { id = 25; photoId = 90002L }
        for (type in listOf("0", "45")) {
            val result = ShowcasePhotoMetadata.resolve(type, showcase, null, plan)
            assertTrue(result.needsPlanogram)
            assertEquals("25", result.planogramId)
            assertEquals("90002", result.planogramImageId)
        }
    }

    @Test
    fun panoramaWithoutShowcaseStillOffersPlanogramSelection() {
        val empty = ShowcaseSDB().apply { id = 0 }
        assertTrue(ShowcasePhotoMetadata.resolve("0", empty, null, null).needsPlanogram)
        assertFalse(ShowcasePhotoMetadata.resolve("45", empty, null, null).needsPlanogram)
    }
}
