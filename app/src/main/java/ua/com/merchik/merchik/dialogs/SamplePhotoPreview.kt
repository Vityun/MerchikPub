package ua.com.merchik.merchik.dialogs

import android.content.Context
import android.util.Log
import android.widget.Toast
import ua.com.merchik.merchik.ServerExchange.PhotoDownload
import ua.com.merchik.merchik.ViewHolders.Clicks
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest
import ua.com.merchik.merchik.database.realm.RealmManager
import java.io.File

object SamplePhotoPreview {
    private const val HIDE_INFORMATION_ON_OPEN = "hide_information_on_open"

    fun showSample(
        context: Context,
        sample: SamplePhotoSDB,
        galleryAction: Boolean = false,
        onCamera: (() -> Unit) -> Unit
    ) {
        val photoId = sample.photoId?.takeIf { it > 0 }?.toString()
        val photo = photoId?.let { RealmManager.getPhotoById(null, it) }
        val comment = listOfNotNull(sample.nm, sample.about)
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
        show(context, photo, comment, sample.id, photoId, galleryAction, onCamera)
    }

    fun show(
        context: Context,
        photo: StackPhotoDB?,
        comment: String?,
        sampleId: Int?,
        photoId: String? = photo?.photoServerId,
        galleryAction: Boolean = false,
        onCamera: (() -> Unit) -> Unit
    ) {
        val preferences = context.getSharedPreferences("sample_photo_settings", Context.MODE_PRIVATE)
        val dialog = DialogFullPhotoR(context)
        dialog.setTitle(sampleId?.let { "Зразок ($it)" } ?: "Зразок")
        dialog.setCommentTitle("Інформація")
        dialog.setComment(comment?.takeIf { it.isNotBlank() } ?: "Інформація відсутня")
        dialog.commentOn = !preferences.getBoolean(HIDE_INFORMATION_ON_OPEN, false)
        dialog.setCommentDialogSetup { informationDialog ->
            informationDialog.setBottomCheckbox(
                "Більше не показувати",
                preferences.getBoolean(HIDE_INFORMATION_ON_OPEN, false)
            ) { _, checked ->
                preferences.edit().putBoolean(HIDE_INFORMATION_ON_OPEN, checked).apply()
                dialog.commentOn = !checked
            }
        }
        if (galleryAction) {
            dialog.setGallery { onCamera { dialog.dismiss() } }
        } else {
            dialog.setCamera { onCamera { dialog.dismiss() } }
        }
        dialog.setClose { dialog.dismiss() }
        dialog.show()

        fun showLoadError(error: String) {
            Log.e("SamplePhotoPreview", "sampleId=$sampleId, photoId=$photoId: $error")
            if (dialog.isShowing) {
                Toast.makeText(context, "Не вдалося завантажити фото зразка", Toast.LENGTH_SHORT).show()
            }
        }

        fun displayPhoto(data: StackPhotoDB) {
            if (!dialog.isShowing) return
            val hasLocalFile = data.photo_num?.takeIf { it.isNotBlank() }
                ?.let { File(it).isFile } == true
            if (hasLocalFile) dialog.setPhoto(data)
            if (!hasLocalFile || data.photo_size == "Small") {
                PhotoDownload().downloadPhoto(true, data,
                    object : PhotoDownload.downloadPhotoInterface {
                        override fun onSuccess(data: StackPhotoDB) {
                            if (dialog.isShowing) dialog.setPhoto(data)
                        }

                        override fun onFailure(error: String) = showLoadError(error)
                    })
            }
        }

        if (photo != null) {
            displayPhoto(photo)
        } else if (!photoId.isNullOrBlank() && photoId != "0") {
            val request = PhotoTableRequest().apply {
                mod = "images_view"
                act = "list_image"
                nolimit = "1"
                id_list = photoId
            }
            PhotoDownload().getPhotoInfoAndSaveItToDB(request,
                object : Clicks.clickObjectAndStatus<StackPhotoDB> {
                    override fun onSuccess(data: StackPhotoDB) = displayPhoto(data)
                    override fun onFailure(error: String) = showLoadError(error)
                })
        } else {
            showLoadError("Sample has no photo ID")
        }
    }
}
