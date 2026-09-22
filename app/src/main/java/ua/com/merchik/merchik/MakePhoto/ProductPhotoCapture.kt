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
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.ViewHolders.Clicks
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm
import ua.com.merchik.merchik.database.realm.tables.PhotoTypeRealm
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.SamplePhotoPreview
import ua.com.merchik.merchik.features.main.DBViewModels.SamplePhotoSDBViewModel

object ProductPhotoCapture {
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
        val photoTypeName = PhotoTypeRealm.getPhotoTypeById(photoType)?.nm ?: "Тип фото $photoType"
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
            putExtra("subTitle", photoTypeName)
        }
        activity.startActivityForResult(intent, DetailedReportActivity.NEED_UPDATE_UI_REQUEST)
    }

    fun getSamples(photoType: Int, tradeMarkId: Int): List<SamplePhotoSDB> =
        RoomManager.SQL_DB.samplePhotoDao().getPhotoLogActiveAndTp(1, photoType, tradeMarkId)

    fun takePhoto(
        activity: Activity,
        visitId: Long,
        optionId: String,
        photoType: Int,
        tovarId: String,
        sample: SamplePhotoSDB?
    ): Boolean {
        if (activity.isFinishing || activity.isDestroyed) return false
        return try {
            val visit = WpDataRealm.getWpDataRowById(visitId)
                ?: error("Visit not found: $visitId")
            val option = OptionsRealm.getOptionById(optionId)
                ?: error("Option not found: $optionId")
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
                RealmManager.INSTANCE.copyFromRealm(option),
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
