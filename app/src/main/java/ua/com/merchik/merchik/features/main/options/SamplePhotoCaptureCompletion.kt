package ua.com.merchik.merchik.features.main.options

import com.google.gson.JsonObject
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.MakePhoto.PhotoReferenceSelection
import ua.com.merchik.merchik.Utils.PhotoEvidencePolicy
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm

internal object SamplePhotoCaptureCompletion {
    fun shouldClose(contextName: String?, source: JsonObject?, savedPhotoId: Int?): Boolean {
        if (source == null || savedPhotoId == null ||
            !PhotoReportCountPolicy.isReportSession(
                contextName, PhotoReferenceSelection.enabled(source.toString()),
                source.text("tovarId")
            )
        ) return false

        val visitId = source.text("wpDataDBId")?.toLongOrNull() ?: return false
        val optionRowId = source.text("optionDBId") ?: return false
        val visit = WpDataRealm.getWpDataRowById(visitId) ?: return false
        val option = OptionsRealm.getOptionById(optionRowId) ?: return false
        val photo = StackPhotoRealm.getById(savedPhotoId) ?: return false
        if (option.codeDad2?.toLongOrNull() != visit.code_dad2) return false
        val type = PhotoReportCountPolicy.reportPhotoType(option.optionId)
        if (type < 0 || photo.code_dad2 != visit.code_dad2 || photo.photo_type != type) return false

        val saved = PhotoEvidencePolicy.isReadyPhoto(photo.photoServerId, photo.photo_num)
        val minimum = PhotoReportMinimum.requiredCount(visit, visit.code_dad2, option, null, true)
        val photos = StackPhotoRealm.getPhotosByDAD2(visit.code_dad2, type)
        val ready = photos.count { PhotoEvidencePolicy.isReadyPhoto(it.photoServerId, it.photo_num) }
        val close = PhotoReportCountPolicy.shouldClose(
            saved, visit.code_dad2, photo.code_dad2, type, photo.photo_type, ready, minimum
        )
        Globals.writeToMLOG(
            "INFO", "SamplePhotoCapture/completion",
            "dad2=${visit.code_dad2}, optionRow=$optionRowId, option=${option.optionId}, " +
                "photoId=$savedPhotoId, type=$type, rows=${photos.size}, ready=$ready, " +
                "minimum=$minimum, close=$close"
        )
        return close
    }

    private fun JsonObject.text(key: String): String? =
        get(key)?.takeUnless { it.isJsonNull }?.asString?.trim()
}
