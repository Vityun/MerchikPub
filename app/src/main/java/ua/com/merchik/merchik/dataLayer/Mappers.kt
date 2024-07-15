package ua.com.merchik.merchik.dataLayer

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import org.json.JSONObject
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.ItemUI
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.Padding
import ua.com.merchik.merchik.dataLayer.model.TextField

interface DataObjectUI {
    fun getHidedFieldsOnUI(): String {
        return ""
    }

    fun getFieldTranslateId(key: String): Long? {
        return null
    }

    fun getValueUI(key: String, value: Any): String {
        return value.toString()
    }

    fun getContainerModifier(jsonObject: JSONObject): MerchModifier? {
        return null
    }

    fun getFieldModifier(key: String, jsonObject: JSONObject): MerchModifier? {
        return MerchModifier(fontWeight = FontWeight.Bold, padding = Padding(end = 10.dp))
    }

    fun getValueModifier(key: String, jsonObject: JSONObject): MerchModifier? {
        return null
    }
}

fun DataObjectUI.toItemUI(nameUIRepository: NameUIRepository, hideUserFields: String?): ItemUI {
    val jsonObject = JSONObject(Gson().toJson(this))
    val fields = mutableListOf<FieldValue>()
    jsonObject.keys().forEach { key ->
        if (!("${hideUserFields}, ${this.getHidedFieldsOnUI()}").contains(key)) {
            fields.add(
                FieldValue(
                    key,
                    TextField(
                        jsonObject.get(key),
                        "${nameUIRepository.getTranslateString(key, this.getFieldTranslateId(key))}: ",
                        this.getFieldModifier(key, jsonObject)
                    ),
                    TextField(
                        jsonObject.get(key),
                        this.getValueUI(key, jsonObject.get(key)),
                        this.getValueModifier(key, jsonObject)
                    )
                )
            )
        }
    }

    return ItemUI(
        rawObj = listOf(this),
        fields = fields,
        modifierContainer = getContainerModifier(jsonObject)
    )
}

enum class ContextUI{
    MAIN, DEFAULT
}



