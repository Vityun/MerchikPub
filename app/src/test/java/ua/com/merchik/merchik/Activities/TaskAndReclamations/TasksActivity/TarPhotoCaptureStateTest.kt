package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB

class TarPhotoCaptureStateTest {
    @Test
    fun cameraAndGalleryKeepSampleCodeAndServerImageIdAfterRecreation() {
        val sample = SamplePhotoSDB().apply { id = 78; id1c = 70078; photoId = 59682417 }
        for (gallery in listOf(false, true)) {
            val state = TarPhotoCaptureState(2670971, 31, gallery, "selection", 12)
            state.setSample(sample)
            state.stage = "capture"
            val restored = Gson().fromJson(Gson().toJson(state), TarPhotoCaptureState::class.java)
            val photo = StackPhotoDB()
            restored.applyTo(photo)

            assertEquals(31, photo.photo_type.toInt())
            assertEquals("70078", photo.example_id)
            assertEquals("59682417", photo.example_img_id)
            assertEquals(gallery, restored.gallery)
            assertEquals("selection", restored.token)
            assertEquals("capture", restored.stage)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun sampleWithoutServerImageCannotStartCapture() {
        val sample = SamplePhotoSDB().apply { id = 78; id1c = 70078; photoId = 0 }
        TarPhotoCaptureState(1, 31, false, "selection", 0).setSample(sample)
    }

    @Test
    fun galleryKeepsShowcaseAndPlanogramReferences() {
        val state = TarPhotoCaptureState(1, 0, true, "selection", 12).apply {
            showcaseId = "17"
            imageId = "90001"
            exampleImageId = "90001"
            planogramId = "25"
            planogramImageId = "90002"
            productGroupId = "42"
        }
        val photo = StackPhotoDB()
        state.applyTo(photo)
        assertEquals("17", photo.showcase_id)
        assertEquals("90001", photo.img_src_id)
        assertEquals("90001", photo.example_img_id)
        assertEquals("25", photo.planogram_id)
        assertEquals("90002", photo.planogram_img_id)
        assertEquals("42", photo.photo_group_id)
    }

    @Test
    fun withoutPlanogramClearsPreviousPlanButKeepsShowcaseForCameraAndGallery() {
        for (gallery in listOf(false, true)) {
            val state = TarPhotoCaptureState(1, 0, gallery, "selection", 12).apply {
                showcaseId = "17"
                imageId = "90001"
                exampleImageId = "90001"
                planogramId = "25"
                planogramImageId = "90002"
                productGroupId = "42"
                withoutPlanogram()
            }
            val restored = Gson().fromJson(Gson().toJson(state), TarPhotoCaptureState::class.java)
            val photo = StackPhotoDB()
            restored.applyTo(photo)
            assertEquals("0", photo.planogram_id)
            assertEquals("0", photo.planogram_img_id)
            assertEquals("17", photo.showcase_id)
            assertEquals("90001", photo.img_src_id)
            assertEquals("90001", photo.example_img_id)
            assertEquals("42", photo.photo_group_id)
            assertEquals(gallery, restored.gallery)
        }
    }

    @Test
    fun nextCaptureDoesNotInheritReferences() {
        val photo = StackPhotoDB().apply {
            example_id = "78"; example_img_id = "90001"; showcase_id = "17"
            img_src_id = "90002"; planogram_id = "25"; planogram_img_id = "90003"
            photo_group_id = "42"
        }
        TarPhotoCaptureState(2, 18, false, "next", 0).applyTo(photo)
        assertEquals(18, photo.photo_type.toInt())
        assertTrue(listOf(photo.example_id, photo.example_img_id, photo.showcase_id, photo.img_src_id,
            photo.planogram_id, photo.planogram_img_id, photo.photo_group_id).all { it.isEmpty() })
    }

    @Test
    fun panoramaAndAdditionalShowcasePhotosUseShowcasesNotSamples() {
        assertTrue(TarPhotoFlow.usesShowcase(0))
        assertTrue(TarPhotoFlow.usesShowcase(45))
        assertFalse(TarPhotoFlow.usesShowcase(31))
        assertFalse(TarPhotoFlow.usesShowcase(4))
    }
}
