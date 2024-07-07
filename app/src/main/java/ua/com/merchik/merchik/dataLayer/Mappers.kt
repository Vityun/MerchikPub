package ua.com.merchik.merchik.dataLayer

import com.google.gson.Gson
import org.json.JSONObject
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.ItemUI
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.TextField

interface DataObjectUI {
    fun getHidedFieldsOnUI(): String {
        return ""
    }

    fun getTranslateId(key: String): Long? {
        return null
    }

    fun getValueUI(key: String, value: Any): String {
        return value.toString()
    }

    fun getFieldModifier(key: String, jsonObject: JSONObject): MerchModifier? {
        return null
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
                        nameUIRepository.getTranslateString(key, this.getTranslateId(key)),
                        this.getFieldModifier(key, jsonObject)
                    ),
                    TextField(
                        this.getValueUI(key, jsonObject.get(key)),
                        this.getValueModifier(key, jsonObject)
                    )
                )
            )
        }
    }

    return ItemUI(
        rawObj = listOf(this),
        fields = fields
    )
}

enum class ContextUI{
    MAIN, DEFAULT
}



