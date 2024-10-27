package ua.com.merchik.merchik.dataLayer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import org.json.JSONObject
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.Padding
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.database.realm.RealmManager

interface DataObjectUI {
    fun getIdResImage(): Int? {
        return null
    }

    fun getFieldsImageOnUI(): String {
        return ""
    }

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
        return MerchModifier(textColor = Color.Gray, padding = Padding(end = 10.dp))
    }

    fun getValueModifier(key: String, jsonObject: JSONObject): MerchModifier? {
        return null
    }
}

fun DataObjectUI.toItemUI(nameUIRepository: NameUIRepository, hideUserFields: String?, typePhoto: Int?): DataItemUI {
    val jsonObject = JSONObject(Gson().toJson(this))
    val fields = mutableListOf<FieldValue>()
    val rawFields = mutableListOf<FieldValue>()

    this.getIdResImage()?.let {
        val keyIdResImage = "id_res_image"
        if (!("${hideUserFields}").contains(keyIdResImage)) {
            fields.add(
                FieldValue(
                    keyIdResImage,
                    TextField(
                        keyIdResImage,
                        "${nameUIRepository.getTranslateString(keyIdResImage, this.getFieldTranslateId(keyIdResImage))}: ",
                    ),
                    TextField(
                        it,
                        it.toString(),
                    )
                )
            )
        }
    }

    val images = mutableListOf<String>()
    this.getFieldsImageOnUI().split(",").forEach {
        if (it.isNotEmpty()) {
            RealmManager
                .getPhotoByIdAndType(
                    null,
                    jsonObject.get(it.trim()).toString(),
                    typePhoto ?: -1,
                )
                ?.getPhoto_num()?.let { pathPhoto ->
                    images.add(pathPhoto)
                }
        }
    }

    jsonObject.keys().forEach { key ->
        rawFields.add(
            FieldValue(
                key,
                TextField(
                    key,
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
        if (!("${hideUserFields}, ${this.getHidedFieldsOnUI()}").contains(key)) {
            fields.add(
                FieldValue(
                    key,
                    TextField(
                        key,
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

    return DataItemUI(
        rawObj = listOf(this),
        rawFields = rawFields,
        fields = fields,
        images = images,
        modifierContainer = getContainerModifier(jsonObject),
        false
    )
}

enum class ModeUI{
    DEFAULT, ONE_SELECT, MULTI_SELECT
}

enum class ContextUI{
    DEFAULT, ONE_SELECT, MULTI_SELECT,
    ADD_REQUIREMENTS_FROM_OPTIONS,
    ADD_REQUIREMENTS_FROM_ACHIEVEMENT,
    TRADE_MARK_FROM_ACHIEVEMENT,
    THEME_FROM_ACHIEVEMENT,
    TOVAR_FROM_ACHIEVEMENT,
    STACK_PHOTO_TO_FROM_ACHIEVEMENT,
    STACK_PHOTO_AFTER_FROM_ACHIEVEMENT,
    SAMPLE_PHOTO_FROM_OST_TOVARA,
}



