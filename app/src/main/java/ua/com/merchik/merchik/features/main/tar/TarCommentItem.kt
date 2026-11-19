package ua.com.merchik.merchik.features.main.tar

import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.Utils.PhotoEvidencePolicy
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Detached presentation snapshot; Realm objects never enter the filtering pipeline.
data class TarCommentItem(
    val id: String = "",
    val dt: Long = 0,
    val time: String = "",
    val who: String = "",
    val author: String = "",
    val comment: String = "",
    val photo: String = "",
    val photoHash: String = "",
    val hasPhoto: Int = 0
) : DataObjectUI {
    override fun getHidedFieldsOnUI() = "who,photo,photoHash,hasPhoto"
    override fun getIdResImage() = if (hasPhoto == 1) R.mipmap.merchik_m else R.drawable.ic_47
    override fun getPreferredFieldOrder() = listOf("dt", "time", "author", "comment", "id")

    fun fieldTitle(key: String): String = when (key) {
        "id" -> "ID"
        "dt" -> "Дата"
        "time" -> "Час"
        "author" -> "Автор коментаря"
        "comment" -> "Коментар"
        "id_res_image" -> "Фото"
        else -> key
    }

    override fun getValueUI(key: String, value: Any): String = when (key) {
        "dt" -> value.toString().toLongOrNull()?.takeIf { it > 0 }
            ?.let { Clock.getHumanTimeDDMMYYYY(it) }.orEmpty()
        else -> value.toString()
    }

    companion object {
        fun from(row: TARCommentsDB, author: String?, plainComment: String): TarCommentItem {
            val seconds = row.dt?.toLongOrNull()?.takeIf { it > 0 } ?: 0
            val hash = row.photo_hash.orEmpty().trim().takeUnless { it.equals("null", true) }.orEmpty()
            return TarCommentItem(
                id = row.id.orEmpty(), dt = seconds,
                time = if (seconds > 0) SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(Date(seconds * 1000)) else "",
                who = row.who.orEmpty(), author = author?.takeIf { it.isNotBlank() } ?: row.who.orEmpty(),
                comment = plainComment, photo = row.photo.orEmpty().trim(),
                photoHash = hash,
                hasPhoto = if (PhotoEvidencePolicy.hasServerPhotoId(row.photo)
                    || PhotoEvidencePolicy.isUsableHash(hash)) 1 else 0
            )
        }
    }
}

internal fun tarCommentsSubtitle(type: Int, text: String): String =
    "${if (type == 1) "Текст завдання" else "Текст рекламації"}: $text"
