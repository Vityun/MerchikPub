package ua.com.merchik.merchik.MakePhoto

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.gson.JsonParser

/** Select references without starting the visit's camera/save flow. */
object PhotoReferenceSelection {
    const val TOKEN = "referenceSelectionToken"
    const val KIND = "referenceSelectionKind"
    const val ID = "referenceSelectionId"
    const val SAMPLE = "sample"
    const val SHOWCASE = "showcase"
    const val PLANOGRAM = "planogram"

    fun enabled(dataJson: String?): Boolean = !text(dataJson, TOKEN).isNullOrBlank()

    fun gallery(dataJson: String?): Boolean = text(dataJson, "galleryAction") == "true"

    fun complete(context: Context, dataJson: String?, kind: String, id: Int): Boolean {
        val activity = context as? Activity ?: return false
        val token = text(dataJson, TOKEN)?.takeIf { it.isNotBlank() } ?: return false
        if (activity.isFinishing || activity.isDestroyed) return false
        activity.setResult(Activity.RESULT_OK, Intent()
            .putExtra(TOKEN, token).putExtra(KIND, kind).putExtra(ID, id))
        activity.finish()
        return true
    }

    private fun text(dataJson: String?, key: String): String? = runCatching {
        JsonParser.parseString(dataJson).asJsonObject.get(key)?.takeUnless { it.isJsonNull }?.asString
    }.getOrNull()
}
