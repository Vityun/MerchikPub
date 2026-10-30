package ua.com.merchik.merchik.MakePhoto

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB

/** Selector data, not a synthetic visit to be saved or used by option controls. */
@Keep
data class PhotoReferenceContext(
    val codeDad2: Long,
    val addressId: Int,
    val clientId: String,
    val addressName: String,
    val clientName: String,
    val codeIza: String = ""
) {
    fun addTo(data: JsonObject) {
        data.add(JSON_KEY, Gson().toJsonTree(this))
    }

    companion object {
        private const val JSON_KEY = "tarPhotoContext"

        fun fromVisit(visit: WpDataDB) = PhotoReferenceContext(
            visit.code_dad2, visit.addr_id, visit.client_id.orEmpty(),
            visit.addr_txt.orEmpty(), visit.client_txt.orEmpty(), visit.code_iza.orEmpty()
        )

        fun fromTaskWithoutVisit(task: TasksAndReclamationsSDB): PhotoReferenceContext {
            require(sourceVisitDad2(task) == null) { "Use the source visit when it is specified" }
            return PhotoReferenceContext(
                task.codeDad2 ?: 0, task.addr ?: 0, task.client.orEmpty(),
                task.addrNm.orEmpty(), task.clientNm.orEmpty()
            ).also { it.validate() }
        }

        fun sourceVisitDad2(task: TasksAndReclamationsSDB): Long? = task.codeDad2SrcDoc?.takeIf { it > 0 }

        fun fromReferenceJson(dataJson: String?): PhotoReferenceContext? {
            if (dataJson.isNullOrBlank()) return null
            val root = JsonParser.parseString(dataJson)
            if (!root.isJsonObject) return null
            val data = root.asJsonObject
            if (data.get(PhotoReferenceSelection.TOKEN)?.takeUnless { it.isJsonNull }?.asString.isNullOrBlank()) return null
            val context = data.get(JSON_KEY)?.takeUnless { it.isJsonNull } ?: return null
            return Gson().fromJson(context, PhotoReferenceContext::class.java).also { it.validate() }
        }
    }

    private fun validate() {
        require(codeDad2 > 0 && addressId > 0 && clientId.isNotBlank()) {
            "Invalid TAR photo context: dad2=$codeDad2, addr=$addressId, client=$clientId"
        }
    }
}
