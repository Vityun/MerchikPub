package ua.com.merchik.merchik.dataLayer

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import org.json.JSONObject
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.Padding
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenuAction

interface DataObjectUI {
    fun getIdResImage(): Int? {
        return null
    }

    fun getFieldsImageOnUI(): String {
        return ""
    }

    fun getFieldsForOrderOnUI(): List<String>? {
        return null
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

//// Простой holder для состояния диалога
//data class MessageDialogData(
//    val title: String,
//    val message: String,
//    val status: DialogStatus = DialogStatus.NORMAL,
//    val clickAction: (() -> Unit)? = null
//)

fun DataObjectUI.toItemUI(
    nameUIRepository: NameUIRepository,
    hideUserFields: String?,
    typePhoto: Int?
): DataItemUI {
    val gson = Gson()
    val jsonObject = JSONObject(gson.toJson(this))

    val fields = mutableListOf<FieldValue>()
    val rawFields = mutableListOf<FieldValue>()
    val images = mutableListOf<String>()

    val hiddenList: Set<String> = (
            (hideUserFields?.split(",") ?: emptyList()) +
                    this.getHidedFieldsOnUI().split(",")
            ).map { it.trim() }
        .filter { it.isNotBlank() }
        .toSet()

    val idResImage = this.getIdResImage()
    if (idResImage != null && "id_res_image" !in hiddenList) {
        val label = nameUIRepository.getTranslateString(
            "id_res_image",
            this.getFieldTranslateId("id_res_image")
        )
        fields.add(
            FieldValue(
                "id_res_image",
                TextField("id_res_image", "$label:"),
                TextField(idResImage, idResImage.toString())
            )
        )
    }

    // Обработка изображений
    val imageKeys = this.getFieldsImageOnUI()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    for (key in imageKeys) {
        val photoId = jsonObject.optString(key, "0")
        when {
            photoId != "0" -> {
                RealmManager.getPhotoByPhotoId(photoId)?.getPhoto_num()?.let { images.add(it) }
            }

            key == "photo_do_id" -> {
                val hash = jsonObject.optString("photo_do_hash", "0")
                if (hash.length > 12) {
                    RealmManager.getPhotoByHash(hash)?.getPhoto_num()?.let { images.add(it) }
                } else {
                    idResImage?.let { images.add(it.toString()) }
                }
            }

            else -> idResImage?.let { images.add(it.toString()) }
        }
    }

    fun updateFields(key: String) {
        val valueRaw = jsonObject.opt(key) ?: return
        val label = nameUIRepository.getTranslateString(key, this.getFieldTranslateId(key))
        val fieldModifier = this.getFieldModifier(key, jsonObject)
        val valueText = this.getValueUI(key, valueRaw)
        val valueModifier = this.getValueModifier(key, jsonObject)

        val field = FieldValue(
            key,
            TextField(key, "$label:", fieldModifier),
            TextField(valueRaw, valueText, valueModifier)
        )

        rawFields.add(field)
        if (key !in hiddenList) {
            fields.add(field)
        }
    }

    val orderedKeys = this.getFieldsForOrderOnUI()?.map { it.trim() }?.toSet() ?: emptySet()
    val allKeys = jsonObject.keys().asSequence().toSet()

    orderedKeys.forEach { if (it in allKeys) updateFields(it) }
    (allKeys - orderedKeys).forEach { updateFields(it) }

    return DataItemUI(
        rawObj = listOf(this),
        rawFields = rawFields,
        fields = fields,
        images = images,
        modifierContainer = getContainerModifier(jsonObject),
        false
    )
}

//
//fun DataObjectUI.toItemUI(
//    nameUIRepository: NameUIRepository,
//    hideUserFields: String?,
//    typePhoto: Int?
//): DataItemUI {
//    Log.e("!!!!!!TEST!!!!!!","toItemUI: start 2")
//    val jsonObject = JSONObject(Gson().toJson(this))
//    val fields = mutableListOf<FieldValue>()
//    val rawFields = mutableListOf<FieldValue>()
//
//    this.getIdResImage()?.let {
//        val keyIdResImage = "id_res_image"
//        if (!("${hideUserFields}").contains(keyIdResImage)) {
//            fields.add(
//                FieldValue(
//                    keyIdResImage,
//                    TextField(
//                        keyIdResImage,
//                        "${
//                            nameUIRepository.getTranslateString(
//                                keyIdResImage,
//                                this.getFieldTranslateId(keyIdResImage)
//                            )
//                        }: ",
//                    ),
//                    TextField(
//                        it,
//                        it.toString(),
//                    )
//                )
//            )
//        }
//    }
//
//    val images = mutableListOf<String>()
//    this.getFieldsImageOnUI().split(",").forEach {
//        if (it.isNotEmpty()) {
//            val photo = jsonObject.optString(it.trim(), "0") // "0" — значение по умолчанию
//            if (photo != "0")
//                RealmManager.getPhotoByPhotoId(photo)
//                    ?.getPhoto_num()?.let { pathPhoto ->
//                        images.add(pathPhoto)
//                    }
//            else {
//                /*хреновый костыль
//                 */
//                if (it.trim() == "photo_do_id") {
//                    val hash = jsonObject.optString("photo_do_hash", "0")
//                    if (hash.length > 12)
//                        RealmManager.getPhotoByHash(jsonObject.optString("photo_do_hash", "0"))
//                            ?.getPhoto_num()?.let { pathPhoto ->
//                                images.add(pathPhoto)
//                            }
//                    else
//                        images.add(this.getIdResImage().toString())
//                } else
//                    images.add(this.getIdResImage().toString())
//
//            }
////            val photo = jsonObject.get(it.trim()).toString()
////            if (photo != "0")
////                RealmManager.getPhotoByPhotoId(photo)
////                    ?.getPhoto_num()?.let { pathPhoto ->
////                        images.add(pathPhoto)
////                    }
////            else
////                images.add(this.getIdResImage().toString())
//        }
//    }
//
//    fun updateFields(key: String) {
//        rawFields.add(
//            FieldValue(
//                key,
//                TextField(
//                    key,
//                    "${nameUIRepository.getTranslateString(key, this.getFieldTranslateId(key))}: ",
//                    this.getFieldModifier(key, jsonObject)
//                ),
//                TextField(
//                    jsonObject.get(key),
//                    this.getValueUI(key, jsonObject.get(key)),
//                    this.getValueModifier(key, jsonObject)
//                )
//            )
//        )
//
//        val hiddenList = ((hideUserFields?.split(", ") ?: emptyList()) + this.getHidedFieldsOnUI().split(", "))
//            .map { it.trim() }
//            .filter { it.isNotBlank() }
//
//        if (key !in hiddenList) {
//            fields.add(
//                FieldValue(
//                    key,
//                    TextField(
//                        key,
//                        "${
//                            nameUIRepository.getTranslateString(
//                                key,
//                                this.getFieldTranslateId(key)
//                            )
//                        }: ",
//                        this.getFieldModifier(key, jsonObject)
//                    ),
//                    TextField(
//                        jsonObject.get(key),
//                        this.getValueUI(key, jsonObject.get(key)),
//                        this.getValueModifier(key, jsonObject)
//                    )
//                )
//            )
//        }
//    }
//
//    this.getFieldsForOrderOnUI()?.forEach { key ->
//        if (jsonObject.keys().asSequence().toList().contains(key)) updateFields(key)
//    }
//
//    jsonObject.keys().forEach { key ->
//        if (this.getFieldsForOrderOnUI()?.contains(key) != true) updateFields(key)
//    }
//    Log.e("!!!!!!TEST!!!!!!","toItemUI: end")
//
//    return DataItemUI(
//        rawObj = listOf(this),
//        rawFields = rawFields,
//        fields = fields,
//        images = images,
//        modifierContainer = getContainerModifier(jsonObject),
//        false
//    )
//}

enum class ModeUI {
    DEFAULT, ONE_SELECT, MULTI_SELECT
}

@DrawableRes
fun ContextMenuAction.iconResOrNull(): Int? = when (this) {
    ContextMenuAction.AcceptOrder -> R.drawable.ic_37   // твои ресурсы
    ContextMenuAction.AcceptAllAtAddress -> R.drawable.ic_37
    ContextMenuAction.RejectOrder -> null
    ContextMenuAction.RejectAddress -> null
    ContextMenuAction.RejectClient -> null
    ContextMenuAction.RejectByType -> null
    ContextMenuAction.OpenVisit -> R.drawable.ic_37
    ContextMenuAction.OpenOrder -> R.drawable.ic_37
    ContextMenuAction.AskMoreMoney -> null
    ContextMenuAction.Feedback -> null
    ContextMenuAction.ConfirmAcceptOneTime -> R.drawable.ic_37
    ContextMenuAction.ConfirmAcceptInfinite -> R.drawable.ic_37
    ContextMenuAction.Close -> R.drawable.ic_37
    ContextMenuAction.ConfirmAllAcceptOneTime -> R.drawable.ic_37
    ContextMenuAction.ConfirmAllAcceptInfinite -> R.drawable.ic_37
    ContextMenuAction.OpenSMSPlanDirectory -> R.drawable.ic_37
}

enum class ContextUI {
    DEFAULT, ONE_SELECT, MULTI_SELECT,
    ADD_REQUIREMENTS_FROM_OPTIONS,
    ADD_REQUIREMENTS_FROM_ACHIEVEMENT,
    ADD_OPINION_FROM_DETAILED_REPORT,
    TRADE_MARK_FROM_ACHIEVEMENT,
    THEME_FROM_ACHIEVEMENT,
    TOVAR_FROM_ACHIEVEMENT,
    STACK_PHOTO_TO_FROM_ACHIEVEMENT,
    STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT,
    STACK_PHOTO_AFTER_FROM_ACHIEVEMENT,
    USERS_SDB_FROM_EKL,
    PLANOGRAMM_VIZIT_SHOWCASE,
    SHOWCASE,
    SHOWCASE_COMPLETED_CHECK,
    WP_DATA_IN_CONTAINER,
    WP_DATA_ADDITIONAL_IN_CONTAINER,
    SMS_PLAN_DEFAULT,
    STACK_PHOTO_FROM_OPTION_158605,     // Корпоративный блок (40)
    SAMPLE_PHOTO_FROM_OPTION_135158,    // Фото Остатков Товаров (ФОТ) (4) +
    SAMPLE_PHOTO_FROM_OPTION_141360,    // Фото товара на складе +
    SAMPLE_PHOTO_FROM_OPTION_132969,    // Фото Тележка с Товаром (ФТТ) (10) +
    SAMPLE_PHOTO_FROM_OPTION_135809,    // Фото витрины до начала работ (14) + TODO проверить где используется! №№№
    SAMPLE_PHOTO_FROM_OPTION_158309,    // Фото витрины наближене (39) +
    SAMPLE_PHOTO_FROM_OPTION_158604,    // Фото витрины наполненности (41) +
    SAMPLE_PHOTO_FROM_OPTION_157277,    // Фото акционного товра + ценник (28? = 26!)
    SAMPLE_PHOTO_FROM_OPTION_157354,    // Фото ДМП (42)
    SAMPLE_PHOTO_FROM_OPTION_164355,    // Фото планограмы (5)
    SAMPLE_PHOTO_FROM_OPTION_169108    // фото POS материалов (47) +

}



