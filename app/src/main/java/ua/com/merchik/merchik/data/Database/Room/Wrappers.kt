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
        "dt_update, ppa_auto, recl_reply_mode, main_tov_grp"

    fun getTranslateId(key: String): Long? = when (key) {
        "nm" -> 1102
        else -> null
    }

    fun getFieldModifier(key: String, jsonObject: JSONObject): MerchModifier? = when (key) {
//        "nm" -> MerchModifier(fontWeight = FontWeight.Bold, padding = Padding(end = 10.dp))
        else -> MerchModifier(padding = Padding(end = 10.dp))
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
    fun getValueUI(key: String, value: Any): String = when (key) {
        "dt_change" -> {
            value.toString().toLongOrNull()?.let {
                SimpleDateFormat("dd MMMM YYYY").format(Date(it))
            } ?: value.toString()
        }
        else -> value.toString()
    }

    fun getContainerModifier(jsonObject: JSONObject): MerchModifier {
        val color =
            try {
                val colorHex = jsonObject.optString("color", "")
                Color(android.graphics.Color.parseColor("#$colorHex"))
            } catch (e: Exception){
                null
            }
        return MerchModifier(background = color)
    }

    fun getValueModifier(key: String, jsonObject: JSONObject): MerchModifier? = when (key) {
        else -> MerchModifier(padding = Padding(start = 10.dp))
    }
}

object TradeMarkDB {
    fun getValueModifier(key: String, jsonObject: JSONObject): MerchModifier? = when (key) {
        else -> MerchModifier(padding = Padding(start = 10.dp))
    }
}

object LogMPDBOverride {
    fun getTranslateId(key: String): Long? = when (key) {
        "CoordAccuracy" -> 5562
        "CoordTime" -> 5563
        "distance" -> 5564
        "provider" -> 5566
        else -> null
    }

    fun getValueUI(key: String, value: Any): String = when (key) {
        "CoordTime" ->
            value.toString().toLongOrNull()?.let {
                SimpleDateFormat("dd.MM HH:mm:ss").format(Date(it))
            } ?: value.toString()

        "distance" ->
            (value as? Int)?.let {
                if (it > 1000) "${it / 1000} км." else  "$it м."
            } ?: "0 м."

        "provider" -> if (value == 1) "GPS" else "GSM"

        "CoordAccurancy" -> if (value == 1) "GPS" else "GSM"

        else -> value.toString()
    }

    fun getValueModifier(key: String, jsonObject: JSONObject): MerchModifier? = when (key) {
        else -> MerchModifier(padding = Padding(start = 10.dp))
    }
}

object TovarDBOverride {
    fun getHidedFieldsOnUI(): String =
        "barcode, client_id, client_id2, deleted, depth, dt_update, expire_period, group_id, " +
                "height, ID, manufacturer_id, photo_id, related_tovar_id, width, sortcol"

    fun getFieldFirstImageUI(): String = "photo_id"
}
