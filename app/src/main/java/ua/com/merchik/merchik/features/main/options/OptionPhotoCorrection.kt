package ua.com.merchik.merchik.features.main.options

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.gson.Gson
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.model.ImageDisplayMode
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm
import ua.com.merchik.merchik.dialogs.DialogFullPhoto
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR
import ua.com.merchik.merchik.features.main.DBViewModels.OptionsDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.StackPhotoDBViewModel

object OptionPhotoCorrection {
    private const val EXTRA_SESSION = "option_photo_correction_session"

    data class Session(
        val visitId: Long,
        val optionRowId: String,
        val dad2: Long,
        val wasViolation: Boolean,
        val before: String
    )

    private data class PhotoState(val id: Int, val comment: String?, val exampleId: String?, val dvi: Int?)

    // Upload acknowledgements, server IDs and cached image paths are not user corrections.
    private fun snapshot(dad2: Long): String = Gson().toJson(
        StackPhotoRealm.getPhotosByDAD2(dad2, 31).sortedBy { it.id }.map {
            PhotoState(it.id, it.comment, it.example_id, it.dvi)
        }
    )

    private fun session(visitId: Long, optionRowId: String): Session {
        val wp = WpDataRealm.getWpDataRowById(visitId) ?: error("Visit not found: $visitId")
        val option = OptionsRealm.getOptionById(optionRowId) ?: error("Option not found: $optionRowId")
        check(option.codeDad2 == wp.code_dad2.toString())
        return Session(visitId, optionRowId, wp.code_dad2, option.isSignal == "1", snapshot(wp.code_dad2))
    }

    private fun optionsViewModel(context: Context): OptionsDBViewModel? =
        (context as? ViewModelStoreOwner)?.let { ViewModelProvider(it)[OptionsDBViewModel::class.java] }

    @JvmStatic
    fun openPhoto(context: Context, visitId: Long, optionRowId: String, photoServerId: String?, localPhotoId: Int) {
        try {
            val source = session(visitId, optionRowId)
            val serverId = photoServerId?.trim()?.takeUnless { it.isEmpty() || it == "0" }
            val query = StackPhotoRealm.getPhotosByDAD2(source.dad2, 31).where()
            // Local IDs are only for photos that had not yet been uploaded when the link was created.
            if (serverId != null) query.equalTo("photoServerId", serverId)
            else query.equalTo("id", localPhotoId)
            val photo = query.findFirst()
                ?: error("Photo not found: serverId=$serverId, localId=$localPhotoId, dad2=${source.dad2}")
            // The query returns a managed object; detach it once for the photo dialog.
            val photos = listOf(RealmManager.INSTANCE.copyFromRealm(photo))
            val dialog = DialogFullPhoto(context)
            dialog.setPhotos(0, photos, { _, selected ->
                val fullSize = DialogFullPhotoR(context)
                fullSize.setPhoto(selected)
                selected.comment?.let(fullSize::setComment)
                fullSize.setClose { fullSize.dismiss() }
                fullSize.show()
            }, {})
            dialog.setClose { dialog.dismiss() }
            dialog.setOnDismissListener {
                if (complete(source)) optionsViewModel(context)?.refreshOptions(true)
            }
            optionsViewModel(context)?.cancelPhotoFeedback()
            dialog.show()
        } catch (error: Exception) {
            showError(context, error)
        }
    }

    @JvmStatic
    fun openJournal(context: Context, visitId: Long, optionRowId: String) {
        try {
            val source = session(visitId, optionRowId)
            val intent = Intent(context, FeaturesActivity::class.java).apply {
                putExtra("viewModel", StackPhotoDBViewModel::class.java.canonicalName)
                putExtra("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360.toString())
                putExtra("modeUI", ModeUI.DEFAULT.toString())
                putExtra("dataJson", Gson().toJson(source.dad2))
                putExtra("title", "Перелік фото звітів")
                putExtra("subTitle", "Фото товару на складі (ФТС)")
                putExtra(FeaturesActivity.EXTRA_INITIAL_IMAGE_DISPLAY_MODE, ImageDisplayMode.TWO_COLUMNS.name)
                // The original snapshot survives recreation of the journal activity.
                putExtra(EXTRA_SESSION, Gson().toJson(source))
            }
            optionsViewModel(context)?.cancelPhotoFeedback()
            context.startActivity(intent)
        } catch (error: Exception) {
            showError(context, error)
        }
    }

    fun readSession(intent: Intent): Session? = try {
        intent.getStringExtra(EXTRA_SESSION)?.let { Gson().fromJson(it, Session::class.java) }
    } catch (error: Exception) {
        Globals.writeToMLOG("ERROR", "OptionPhotoCorrection/readSession", "error=$error")
        null
    }

    fun complete(source: Session?): Boolean {
        if (source == null) return false
        return try {
            val changed = snapshot(source.dad2) != source.before
            if (changed && source.wasViolation) {
                SamplePhotoCaptureResult.publishCorrection(source.visitId, source.optionRowId)
            }
            changed
        } catch (error: Exception) {
            Globals.writeToMLOG("ERROR", "OptionPhotoCorrection/complete", "error=$error")
            false
        }
    }

    private fun showError(context: Context, error: Exception) {
        Globals.writeToMLOG("ERROR", "OptionPhotoCorrection/open", "error=$error")
        Toast.makeText(context, "Не вдалося відкрити фото. Оновіть дані та повторіть спробу.", Toast.LENGTH_LONG).show()
    }
}
