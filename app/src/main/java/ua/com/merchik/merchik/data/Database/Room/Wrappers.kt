package ua.com.merchik.merchik.data.Database.Room

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.Padding
import java.text.SimpleDateFormat
import java.util.Date

object LogDBOverride {
    fun getHidedFieldsOnUI(): String =
        "id, tp, author"

    fun getTranslateId(key: String): Long? = when (key) {
        "nm" -> 1813
        "dt_update", "dt_start", "dt", "dt_action" -> 4043
        "comments" -> 2340
        else -> null
    }

    fun getValueUI(key: String, value: Any): String = when (key) {
        "dt_update", "dt_start", "dt", "dt_action" -> {
            value.toString().toLongOrNull()?.let {
                "${SimpleDateFormat("dd MMMM HH:mm:ss.SSS").format(Date(it))} $value"
            } ?: value.toString()
        }
        else -> value.toString()
    }

    fun getFieldModifier(key: String, jsonObject: JSONObject): MerchModifier? = when (key) {
        "nm" -> MerchModifier(fontWeight = FontWeight.Bold, padding = Padding(end = 10.dp))
        "dt_update", "dt_start", "dt", "dt_action" -> MerchModifier(
            fontWeight = FontWeight.Bold,
            padding = Padding(end = 10.dp)
        )
        "comments" -> {
            if (!jsonObject.get("obj_id").toString().contains("20"))
                MerchModifier(
                    fontWeight = FontWeight.Bold,
                    padding = Padding(end = 10.dp),
                    background = Color.Green
                )
            else
                MerchModifier(fontWeight = FontWeight.Bold, padding = Padding(end = 10.dp))
        }
        else -> null
    }

    fun getValueModifier(key: String, jsonObject: JSONObject): MerchModifier? = when (key) {
        "obj_id" -> {
            if (jsonObject.get("obj_id").toString().contains("20"))
                MerchModifier(fontStyle = FontStyle.Italic, background = Color.Red, alignment = Alignment.End, weight = 1f)
            else
                MerchModifier(fontStyle = FontStyle.Italic)
        }
        "dt_update", "dt_start", "dt", "dt_action" -> MerchModifier(fontStyle = FontStyle.Italic)
        "comments" -> MerchModifier(fontStyle = FontStyle.Italic)
        else -> null
    }
}

object AddressSDBOverride {

    fun getTranslateId(key: String): Long? = when (key) {
        "nm" -> 1813
        "dt_update", "dt_start", "dt", "dt_action" -> 4043
        else -> null
    }

    fun getFieldModifier(
        key: String,
        jsonObject: JSONObject
    ) = when (key) {
        "nm" -> MerchModifier(fontWeight = FontWeight.Bold, padding = Padding(end = 10.dp))
        "dt_update", "dt_start", "dt", "dt_action" -> MerchModifier(
            fontWeight = FontWeight.Bold,
            padding = Padding(end = 10.dp)
        )
        else -> null
    }

}

object CustomerSDBOverride {
    fun getHidedFieldsOnUI(): String =
        "dt_update, ppa_auto, recl_reply_mode, main_tov_grp, client_id"

    fun getTranslateId(key: String): Long? = when (key) {
        "nm" -> 1102
        else -> null
    }

    fun getFieldModifier(key: String, jsonObject: JSONObject): MerchModifier? = when (key) {
        "nm" -> MerchModifier(fontWeight = FontWeight.Bold, padding = Padding(end = 10.dp))
        else -> MerchModifier(fontWeight = FontWeight.Bold, padding = Padding(end = 20.dp))
    }

    fun getValueUI(key: String, value: Any): String = when (key) {
        "dt_update", "dt_start", "dt", "dt_action" -> {
            value.toString().toLongOrNull()?.let {
                "${SimpleDateFormat("dd MMMM HH:mm:ss.SSS").format(Date(it))} $value"
            } ?: value.toString()
        }
        else -> value.toString()
    }
}

object AdditionalRequirementsDBOverride {
    fun getContainerModifier(jsonObject: JSONObject): MerchModifier {
        val color =
            try {
                val colorHex = jsonObject.optString("color", "").ifEmpty { "FFFFFF" }
                Color(android.graphics.Color.parseColor("#$colorHex"))
            } catch (e: Exception){
                Color.White
            }
        return MerchModifier(background = color)
    }

}
