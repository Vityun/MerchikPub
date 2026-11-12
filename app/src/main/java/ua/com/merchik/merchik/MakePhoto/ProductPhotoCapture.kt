package ua.com.merchik.merchik.MakePhoto

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.TovarRequisites
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.ViewHolders.Clicks
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.model.ImageDisplayMode
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm
import ua.com.merchik.merchik.database.realm.tables.PhotoTypeRealm
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialodTAR.TarPhotoSourceDialog
import ua.com.merchik.merchik.dialogs.SamplePhotoPreview
import ua.com.merchik.merchik.features.main.DBViewModels.SamplePhotoSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.StackPhotoDBViewModel

object ProductPhotoCapture {
    const val EXTRA_PROMOTION_PRODUCT_ID = "promotion_photo_product_id"
    const val EXTRA_REMAINING_GOODS_PRODUCT_ID = "remaining_goods_photo_product_id"

    fun openRemainingGoodsPhotos(activity: Activity, wp: WpDataDB, tovarId: String) {
        val photos = StackPhotoRealm.getReadyProductPhotos(wp.code_dad2, StackPhotoDB.PHOTO_TOV_LEFT, tovarId)
        if (photos.isEmpty()) {
            openRemainingGoodsSamples(activity, wp, tovarId)
            return
        }
        openPhotoList(activity, wp, tovarId, StackPhotoDB.PHOTO_TOV_LEFT,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158, EXTRA_REMAINING_GOODS_PRODUCT_ID)
    }

    fun openRemainingGoodsSamples(activity: Activity, wp: WpDataDB, tovarId: String) {
        val visitId = wp.id
        val networkId = RoomManager.SQL_DB.addressDao().getById(wp.addr_id)?.tpId ?: 0
        val optionId = OptionsRealm.getOption(wp.code_dad2.toString(), "135158")?.getID()
        TarPhotoSourceDialog.show(
            activity,
            onCamera = {
                openSampleList(activity, visitId, StackPhotoDB.PHOTO_TOV_LEFT, tovarId, networkId,
                    ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158, optionId)
            },
            onGallery = {
                openSampleList(activity, visitId, StackPhotoDB.PHOTO_TOV_LEFT, tovarId, networkId,
                    ContextUI.SAMPLE_PHOTO_FOR_PRODUCT_GALLERY, optionId)
            },
            onDismiss = {}
        )
    }

    fun openPromotionPhotos(activity: Activity, wp: WpDataDB, tovarId: String) {
        val photos = StackPhotoRealm.getReadyProductPhotos(wp.code_dad2, StackPhotoDB.PHOTO_PROMOTION_TOV, tovarId)
        if (photos.isEmpty()) {
            openPromotionSamples(activity, wp, tovarId)
            return
        }
        openPhotoList(activity, wp, tovarId, StackPhotoDB.PHOTO_PROMOTION_TOV,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277, EXTRA_PROMOTION_PRODUCT_ID)
    }

    private fun openPhotoList(
        activity: Activity,
        wp: WpDataDB,
        tovarId: String,
        photoType: Int,
        contextUI: ContextUI,
        productIdExtra: String
    ) {
        val intent = Intent(activity, FeaturesActivity::class.java).apply {
            putExtra("viewModel", StackPhotoDBViewModel::class.java.name)
            putExtra("contextUI", contextUI.name)
            putExtra("dataJson", wp.code_dad2.toString())
            putExtra(productIdExtra, tovarId)
            putExtra(FeaturesActivity.EXTRA_INITIAL_IMAGE_DISPLAY_MODE, ImageDisplayMode.TWO_COLUMNS.name)
            putExtra("title", "Перелік фото звітів")
            putExtra("subTitle", photoTypeName(photoType))
        }
        activity.startActivityForResult(intent, DetailedReportActivity.NEED_UPDATE_UI_REQUEST)
    }

    fun openPromotionSamples(activity: Activity, wp: WpDataDB, tovarId: String) {
        val networkId = RoomManager.SQL_DB.addressDao().getById(wp.addr_id)?.tpId ?: 0
        val option = OptionsRealm.getOption(wp.code_dad2.toString(), "157277")
        openSampleList(activity, wp.id, StackPhotoDB.PHOTO_PROMOTION_TOV, tovarId, networkId,
            ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277, option?.getID())
    }

    @JvmStatic
    @JvmOverloads
    fun open(
        activity: Activity,
        wp: WpDataDB,
        option: OptionsDB,
        photoType: Int,
        tovarId: String,
        contextUI: ContextUI = ContextUI.SAMPLE_PHOTO_FOR_PRODUCT
    ) {
        val owner = activity as? LifecycleOwner ?: return
        val visitId = wp.id
        val addressId = wp.addr_id
        val optionId = option.getID()
        owner.lifecycleScope.launch {
            try {
                val (tradeMarkId, samples) = withContext(Dispatchers.IO) {
                    val networkId = RoomManager.SQL_DB.addressDao().getById(addressId)?.tpId ?: 0
                    networkId to getSamples(photoType, networkId)
                }
                if (activity.isFinishing || activity.isDestroyed) return@launch
                when (samples.size) {
                    0 -> takePhoto(activity, visitId, optionId, photoType, tovarId, null)
                    1 -> {
                        val sample = samples.single()
                        SamplePhotoPreview.showSample(activity, sample) { onStarted ->
                            if (takePhoto(activity, visitId, optionId, photoType, tovarId, sample)) {
                                onStarted()
                            }
                        }
                    }
                    else -> {
                        openSampleList(activity, visitId, photoType, tovarId, tradeMarkId, contextUI, optionId)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Globals.writeToMLOG("ERROR", "ProductPhotoCapture/open", "visitId=$visitId, tovarId=$tovarId: $e")
                Toast.makeText(activity, "Не вдалося відкрити зразки фото", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @JvmStatic
    fun openRemainingGoodsGallery(activity: Activity, wp: WpDataDB, tovarId: String, onPick: Clicks.clickVoid) {
        val owner = activity as? LifecycleOwner ?: return
        val visitId = wp.id
        val addressId = wp.addr_id
        owner.lifecycleScope.launch {
            try {
                val (tradeMarkId, samples) = withContext(Dispatchers.IO) {
                    val networkId = RoomManager.SQL_DB.addressDao().getById(addressId)?.tpId ?: 0
                    networkId to getSamples(4, networkId)
                }
                if (activity.isFinishing || activity.isDestroyed) return@launch
                when (samples.size) {
                    0 -> {
                        Globals.writeToMLOG("INFO", "ProductPhotoCapture/openRemainingGoodsGallery",
                            "No active sample: photoType=4, tradeMarkId=$tradeMarkId, addressId=$addressId")
                        Toast.makeText(activity, "Не знайдено зразок фото залишків для цієї мережі", Toast.LENGTH_SHORT).show()
                    }
                    1 -> SamplePhotoPreview.showSample(activity, samples.single(), galleryAction = true) { onStarted ->
                        onPick.click()
                        onStarted()
                    }
                    else -> openSampleList(
                        activity, visitId, 4, tovarId, tradeMarkId,
                        ContextUI.SAMPLE_PHOTO_FOR_PRODUCT_GALLERY
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Globals.writeToMLOG("ERROR", "ProductPhotoCapture/openRemainingGoodsGallery", "$e")
                Toast.makeText(activity, "Не вдалося відкрити зразок фото залишків", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openSampleList(
        activity: Activity,
        visitId: Long,
        photoType: Int,
        tovarId: String,
        tradeMarkId: Int,
        contextUI: ContextUI,
        optionId: String? = null
    ) {
        val data = JsonObject().apply {
            addProperty("wpDataDBId", visitId.toString())
            optionId?.let { addProperty("optionDBId", it) }
            addProperty("photoType", photoType)
            addProperty("tovarId", tovarId)
            addProperty("tradeMarkDBId", tradeMarkId)
        }
        val intent = Intent(activity, FeaturesActivity::class.java).apply {
            putExtra("viewModel", SamplePhotoSDBViewModel::class.java.canonicalName)
            putExtra("contextUI", contextUI.toString())
            putExtra("modeUI", ModeUI.ONE_SELECT.toString())
            putExtra("dataJson", data.toString())
            putExtra("title", activity.getString(R.string.title_samplephotosdb))
            putExtra("subTitle", photoTypeName(photoType))
        }
        activity.startActivityForResult(intent, DetailedReportActivity.NEED_UPDATE_UI_REQUEST)
    }

    private fun photoTypeName(photoType: Int): String =
        if (photoType == StackPhotoDB.PHOTO_PROMOTION_TOV) TovarRequisites.promotionPhotoTitle()
        else PhotoTypeRealm.getPhotoTypeById(photoType)?.nm ?: "Тип фото $photoType"

    fun getSamples(photoType: Int, tradeMarkId: Int): List<SamplePhotoSDB> =
        RoomManager.SQL_DB.samplePhotoDao().getPhotoLogActiveAndTp(1, photoType, tradeMarkId)

    fun takePhoto(
        activity: Activity,
        visitId: Long,
        optionId: String?,
        photoType: Int,
        tovarId: String,
        sample: SamplePhotoSDB?
    ): Boolean {
        if (activity.isFinishing || activity.isDestroyed) return false
        return try {
            val visit = WpDataRealm.getWpDataRowById(visitId)
                ?: error("Visit not found: $visitId")
            // Product actions remain available without a separate photo option in the visit.
            val option = optionId?.let { OptionsRealm.getOptionById(it) ?: error("Option not found: $it") }
            require(option != null || photoType == StackPhotoDB.PHOTO_PROMOTION_TOV ||
                photoType == StackPhotoDB.PHOTO_TOV_LEFT) { "Option ID is missing" }
            require(tovarId.isNotBlank() && tovarId != "0") { "Product ID is missing" }
            require(sample == null || sample.photoTp == photoType) { "Sample photo type does not match" }

            // Set capture metadata only when taking the photo, not while browsing samples.
            MakePhoto.img_src_id = ""
            MakePhoto.showcase_id = ""
            MakePhoto.planogram_id = ""
            MakePhoto.planogram_img_id = ""
            MakePhoto.example_id = sample?.id1c?.takeIf { it > 0 }?.toString().orEmpty()
            MakePhoto.example_img_id = sample?.photoId?.takeIf { it > 0 }?.toString().orEmpty()
            MakePhoto().pressedMakePhoto(
                activity,
                RealmManager.INSTANCE.copyFromRealm(visit),
                option?.let { RealmManager.INSTANCE.copyFromRealm(it) },
                photoType.toString(),
                tovarId
            ) {}
            true
        } catch (e: Exception) {
            Globals.writeToMLOG("ERROR", "ProductPhotoCapture/takePhoto", "visitId=$visitId, tovarId=$tovarId: $e")
            Toast.makeText(activity, "Не вдалося відкрити камеру для цього товару", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
