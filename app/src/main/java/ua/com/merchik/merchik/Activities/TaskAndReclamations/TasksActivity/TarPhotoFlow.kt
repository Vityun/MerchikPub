package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.JsonObject
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARActivity
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.MakePhoto.MakePhoto
import ua.com.merchik.merchik.MakePhoto.PhotoReferenceSelection
import ua.com.merchik.merchik.MakePhoto.PlanogramPhotoSelection
import ua.com.merchik.merchik.MakePhoto.ShowcasePhotoMetadata
import ua.com.merchik.merchik.Utils.PhotoPickerUtils
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager.SQL_DB
import ua.com.merchik.merchik.features.main.DBViewModels.PlanogrammSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.SamplePhotoSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.ShowcaseDBViewModel
import java.util.UUID

/** Owns TAR capture metadata across selectors, camera/gallery and Activity recreation. */
class TarPhotoFlow(private val activity: TARActivity) {
    private val preferences = activity.getSharedPreferences("tar_photo_flow", Context.MODE_PRIVATE)
    private val gson = Gson()

    private fun pending(): TarPhotoCaptureState? = try {
        preferences.getString("pending", null)?.let { gson.fromJson(it, TarPhotoCaptureState::class.java) }
    } catch (e: Exception) {
        Globals.writeToMLOG("ERROR", "TarPhotoFlow/restore", "Invalid capture state: $e")
        clear()
        null
    }

    private fun save(state: TarPhotoCaptureState) {
        preferences.edit().putString("pending", gson.toJson(state)).apply()
    }

    fun clear() { preferences.edit().remove("pending").apply() }

    fun isActive(): Boolean = preferences.contains("pending")

    fun pendingTask(): TasksAndReclamationsSDB? = pending()?.let { SQL_DB.tarDao().getById(it.tarId) }

    fun start(tarId: Int, photoType: Int, gallery: Boolean) {
        try {
            activity.clearPendingPhotoTypeId()
            require(photoType >= 0)
            val task = requireNotNull(SQL_DB.tarDao().getById(tarId)) { "TAR $tarId not found" }
            val networkId = SQL_DB.addressDao().getById(task.addr)?.tpId ?: 0
            val state = TarPhotoCaptureState(tarId, photoType, gallery, UUID.randomUUID().toString(), networkId)
            activity.setPendingPhotoTypeId(photoType)
            save(state)
            when {
                usesShowcase(photoType) -> {
                    requireVisit(task)
                    openSelector(state, task, PhotoReferenceSelection.SHOWCASE)
                }
                SamplePhotoSDBViewModel.supportsPhotoType(photoType) ||
                    SQL_DB.samplePhotoDao().getPhotoLogActiveAndTp(1, photoType, networkId).isNotEmpty() ->
                    openSelector(state, task, PhotoReferenceSelection.SAMPLE)
                else -> openSource(state)
            }
        } catch (e: Exception) {
            fail(e)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != REQUEST_REFERENCE) return false
        try {
            val state = pending() ?: return true
            if (resultCode != Activity.RESULT_OK) {
                activity.clearPendingPhotoTypeId()
                return true
            }
            if (data == null || data.getStringExtra(PhotoReferenceSelection.TOKEN) != state.token ||
                data.getStringExtra(PhotoReferenceSelection.KIND) != state.stage) {
                Globals.writeToMLOG("ERROR", "TarPhotoFlow/result", "Ignored stale selection, tar=${state.tarId}, stage=${state.stage}")
                return true
            }
            val id = data.getIntExtra(PhotoReferenceSelection.ID, Int.MIN_VALUE)
            val task = requireNotNull(pendingTask()) { "TAR ${state.tarId} not found" }
            when (state.stage) {
                PhotoReferenceSelection.SAMPLE -> {
                    val sample = requireNotNull(SQL_DB.samplePhotoDao().getById(id)) { "Sample $id not found" }
                    require(sample.active == 1 && sample.photoTp == state.photoType &&
                        (sample.grpId == 0 || sample.grpId == state.networkId)) { "Sample does not match TAR" }
                    state.setSample(sample)
                    openSource(state)
                }
                PhotoReferenceSelection.SHOWCASE -> {
                    val showcase = if (id == 0) ShowcaseSDB().apply { this.id = 0 }
                        else requireNotNull(SQL_DB.showcaseDao().getById(id)) { "Showcase $id not found" }
                    val metadata = ShowcasePhotoMetadata.load(task.codeDad2, state.photoType.toString(), showcase)
                    state.showcaseId = metadata.showcaseId
                    state.imageId = metadata.imageId
                    state.exampleImageId = metadata.imageId
                    state.planogramId = metadata.planogramId
                    state.planogramImageId = metadata.planogramImageId
                    state.productGroupId = showcase.tovarGrp?.takeIf { it > 0 }?.toString().orEmpty()
                    if (metadata.needsPlanogram) openSelector(state, task, PhotoReferenceSelection.PLANOGRAM)
                    else openSource(state)
                }
                PhotoReferenceSelection.PLANOGRAM -> {
                    if (id == -999) {
                        state.withoutPlanogram()
                    } else {
                        val visit = requireVisit(task)
                        val plans = PlanogramPhotoSelection.forVisit(visit).getPlans()
                        val plan = requireNotNull(plans.firstOrNull { it.id == id }) { "Planogram $id is unavailable" }
                        state.planogramId = plan.id.toString()
                        state.planogramImageId = plan.planogrammPhotoId?.toString().orEmpty()
                    }
                    openSource(state)
                }
                else -> error("Unexpected photo selection: ${state.stage}")
            }
        } catch (e: Exception) {
            fail(e)
        }
        return true
    }

    private fun requireVisit(task: TasksAndReclamationsSDB) =
        requireNotNull(RealmManager.getWorkPlanRowByCodeDad2Detached(task.codeDad2)) {
            "Відвідування для цієї задачі не завантажено. Виконайте обмін даними."
        }.also {
            require(it.addr_id == task.addr && it.client_id == task.client) { "TAR visit does not match address/customer" }
        }

    private fun openSelector(state: TarPhotoCaptureState, task: TasksAndReclamationsSDB, kind: String) {
        if (kind == PhotoReferenceSelection.PLANOGRAM &&
            PlanogramPhotoSelection.forVisit(requireVisit(task)).getPlans().isEmpty()) {
            state.withoutPlanogram()
            openSource(state)
            return
        }

        val (model, context, title) = when (kind) {
            PhotoReferenceSelection.SAMPLE -> Triple(SamplePhotoSDBViewModel::class.java, ContextUI.SAMPLE_PHOTO_FROM_OPTION_GENERIC, "Зразки фотозвітів")
            PhotoReferenceSelection.SHOWCASE -> Triple(ShowcaseDBViewModel::class.java, ContextUI.SHOWCASE_MAKE_PHOTO, "Вітрини")
            else -> Triple(PlanogrammSDBViewModel::class.java, ContextUI.PLANOGRAM_MAKE_PHOTO, "Планограми")
        }
        state.stage = kind
        save(state)
        val data = JsonObject().apply {
            addProperty(PhotoReferenceSelection.TOKEN, state.token)
            addProperty("galleryAction", state.gallery)
            addProperty("photoType", state.photoType)
            addProperty("tradeMarkDBId", state.networkId)
            addProperty("clientName", task.clientNm)
            if (kind != PhotoReferenceSelection.SAMPLE) addProperty("wpDataDBId", task.codeDad2.toString())
        }
        activity.startActivityForResult(Intent(activity, FeaturesActivity::class.java).apply {
            putExtra("viewModel", model.canonicalName)
            putExtra("contextUI", context.toString())
            putExtra("modeUI", ModeUI.ONE_SELECT.toString())
            putExtra("typeWindow", "container")
            putExtra("dataJson", data.toString())
            putExtra("title", title)
            putExtra("subTitle", when (kind) {
                PhotoReferenceSelection.SAMPLE -> "Оберіть зразок для фото"
                PhotoReferenceSelection.SHOWCASE -> "Оберіть вітрину для фото"
                else -> "Оберіть планограму для фото"
            })
        }, REQUEST_REFERENCE)
    }

    private fun openSource(state: TarPhotoCaptureState) {
        state.stage = "capture"
        save(state)
        Globals.writeToMLOG("INFO", "TarPhotoFlow", "tar=${state.tarId}, type=${state.photoType}, gallery=${state.gallery}, " +
            "example=${state.exampleId}/${state.exampleImageId}, showcase=${state.showcaseId}, planogram=${state.planogramId}/${state.planogramImageId}")
        if (state.gallery) {
            activity.startActivityForResult(PhotoPickerUtils.createSingleImageChooser(), MakePhoto.PICK_GALLERY_IMAGE_REQUEST)
        } else {
            val camera = requireNotNull(MakePhoto().createCameraIntent(activity)) { "Камера недоступна" }
            state.cameraPath = MakePhoto.getOpenCameraPhotoPath(activity).orEmpty()
            save(state)
            activity.startActivityForResult(camera, MakePhoto.CAMERA_REQUEST_TAKE_PHOTO)
        }
    }

    fun applyTo(photo: StackPhotoDB, task: TasksAndReclamationsSDB) {
        val state = pending() ?: return
        check(state.tarId == task.id && state.stage == "capture") { "Photo does not belong to the pending TAR capture" }
        state.applyTo(photo)
    }

    private fun fail(error: Exception) {
        Globals.writeToMLOG("ERROR", "TarPhotoFlow", "${error}")
        val cameraPath = pending()?.cameraPath?.takeIf { it.isNotBlank() }
        if (cameraPath != null && cameraPath == MakePhoto.getPendingPhotoNum(activity)) {
            MakePhoto.deletePendingPhotoFileIfExists(activity)
            MakePhoto.clearPendingPhoto(activity)
        }
        activity.clearPendingPhotoTypeId()
        Toast.makeText(activity, "Не вдалося підготувати фото. ${error.message.orEmpty()}", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val REQUEST_REFERENCE = 21341
        internal fun usesShowcase(photoType: Int) = photoType == 0 || photoType == 45
    }
}

@Keep
internal data class TarPhotoCaptureState(
    val tarId: Int,
    val photoType: Int,
    val gallery: Boolean,
    val token: String,
    val networkId: Int,
    var stage: String = "",
    var exampleId: String = "",
    var exampleImageId: String = "",
    var showcaseId: String = "",
    var imageId: String = "",
    var planogramId: String = "",
    var planogramImageId: String = "",
    var productGroupId: String = "",
    var cameraPath: String = ""
) {
    fun withoutPlanogram() {
        planogramId = "0"
        planogramImageId = "0"
    }

    fun setSample(sample: SamplePhotoSDB) {
        require((sample.id1c ?: 0) > 0 && (sample.photoId ?: 0) > 0) { "Зразок не має коду або фото. Оновіть довідник зразків." }
        exampleId = sample.id1c.toString()
        exampleImageId = sample.photoId.toString()
    }

    fun applyTo(photo: StackPhotoDB) {
        photo.photo_type = photoType
        photo.example_id = exampleId
        photo.example_img_id = exampleImageId
        photo.showcase_id = showcaseId
        photo.img_src_id = imageId
        photo.planogram_id = planogramId
        photo.planogram_img_id = planogramImageId
        photo.photo_group_id = productGroupId
    }
}
