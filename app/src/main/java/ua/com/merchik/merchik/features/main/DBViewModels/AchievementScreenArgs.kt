package ua.com.merchik.merchik.features.main.DBViewModels

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

internal data class AchievementScreenArgs(
    val visitDad2: Long? = null,
    val achievementServerId: Int? = null
) {
    fun toJson(): String = JsonObject().apply {
        visitDad2?.let { addProperty(VISIT_DAD2, it) }
        achievementServerId?.let { addProperty(ACHIEVEMENT_SERVER_ID, it) }
    }.toString()

    companion object {
        private const val VISIT_DAD2 = "wpDataDBId"
        private const val ACHIEVEMENT_SERVER_ID = "achievementServerId"

        fun fromJson(raw: String?): AchievementScreenArgs {
            val element = raw?.let { runCatching { JsonParser.parseString(it) }.getOrNull() }
            val args = element?.takeIf { it.isJsonObject }?.asJsonObject
            val legacyArgs = args?.get("nameValuePairs")?.takeIf { it.isJsonObject }?.asJsonObject
            val dad2 = args?.visitDad2OrNull() ?: legacyArgs?.visitDad2OrNull()
                ?: element?.asLongOrNull()
            val serverId = args?.get(ACHIEVEMENT_SERVER_ID)?.asLongOrNull()
                ?.takeIf { it in 1L..Int.MAX_VALUE.toLong() }?.toInt()
            return AchievementScreenArgs(dad2?.takeIf { it > 0 }, serverId)
        }

        private fun JsonObject.visitDad2OrNull(): Long? =
            get(VISIT_DAD2)?.asLongOrNull() ?: get("codeDad2")?.asLongOrNull()
                ?: get("code_dad2")?.asLongOrNull()

        private fun JsonElement.asLongOrNull(): Long? =
            if (isJsonPrimitive) asString.toLongOrNull() else null
    }
}
