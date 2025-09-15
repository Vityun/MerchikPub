package ua.com.merchik.merchik.dataLayer

import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import org.json.JSONObject
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.Padding
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenuAction
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

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

private fun logException(tag: String, where: String, e: Throwable, extra: String? = null) {
    val sw = StringWriter()
    e.printStackTrace(PrintWriter(sw))
    val stack = sw.toString()
    val extraInfo = extra?.let { " | extra: $it" } ?: ""
    Globals.writeToMLOG("ERROR", tag, "$where | Exception ${e::class.java.name}: ${e.message}$extraInfo\n$stack")
}


fun DataObjectUI.toItemUI(
    nameUIRepository: NameUIRepository,
    hideUserFields: String?,
    typePhoto: Int?
): DataItemUI {
    val gson = Gson()
    val jsonObject = try {
        JSONObject(gson.toJson(this))
    } catch (e: Throwable) {
        logException("Mappers.DataObjectUI.toItemUI", "JSONObject(gson.toJson(this))", e, "source object: $this")
        // если не получилось распарсить — создаём пустой JSON, чтобы дальше код не падал
        JSONObject()
    }

    val fields = mutableListOf<FieldValue>()
    val rawFields = mutableListOf<FieldValue>()
    val images = mutableListOf<String>()

    // Сформируем множество скрытых ключей (trim + убрать пустые)
    val hiddenList: Set<String> = (
            (hideUserFields?.split(",") ?: emptyList()) +
                    this.getHidedFieldsOnUI().split(",")
            ).map { it.trim() }
        .filter { it.isNotBlank() }
        .toSet()

    val idResImage = this.getIdResImage()
    // id_res_image обрабатываем только если он не скрыт
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

    // Обработка изображений — пропускаем ключи, которые скрыты
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
//        if (key in hiddenList) return

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

//        // rawFields тоже теперь фильтрованы — мы добавляем только если ключ не в hiddenList (уже проверено)
//        rawFields.add(field)
//        // fields содержит только видимые (не скрытые) элементы
//        fields.add(field)

        rawFields.add(field)
        if (key !in hiddenList) {
            fields.add(field)
        }
    }

    val orderedKeys = this.getFieldsForOrderOnUI()?.map { it.trim() }?.toSet() ?: emptySet()
    val allKeys = jsonObject.keys().asSequence().toSet()

    orderedKeys.forEach { if (it in allKeys) updateFields(it) }
    (allKeys - orderedKeys).forEach { updateFields(it) }

    Globals.writeToMLOG("INFO",  "Mappers.DataObjectUI.toItemUI ", "fields: ${fields.size} | rawFields: ${rawFields.size}")

    return DataItemUI(
        rawObj = listOf(this),
        rawFields = rawFields,
        fields = fields,
        images = images,
        modifierContainer = getContainerModifier(jsonObject),
        false
    )
}


fun DataObjectUI.toItemUI_(
    nameUIRepository: NameUIRepository,
    hideUserFields: String?,
    typePhoto: Int?
): DataItemUI {
    try {
        val gson = Gson()
        val jsonObject = try {
            JSONObject(gson.toJson(this))
        } catch (e: Throwable) {
            logException("Mappers.DataObjectUI.toItemUI", "JSONObject(gson.toJson(this))", e, "source object: $this")
            // если не получилось распарсить — создаём пустой JSON, чтобы дальше код не падал
            JSONObject()
        }

        val fields = mutableListOf<FieldValue>()
        val rawFields = mutableListOf<FieldValue>()
        val images = mutableListOf<String>()

        // Сформируем множество скрытых ключей (trim + убрать пустые)
        val hiddenList: Set<String> = (
                (hideUserFields?.split(",") ?: emptyList()) +
                        this.getHidedFieldsOnUI().split(",")
                ).map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

        val idResImage = try {
            this.getIdResImage()
        } catch (e: Throwable) {
            logException("Mappers.DataObjectUI.toItemUI", "getIdResImage()", e)
            null
        }

        if (idResImage != null && "id_res_image" !in hiddenList) {
            try {
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
            } catch (e: Throwable) {
                logException("Mappers.DataObjectUI.toItemUI", "add id_res_image field", e)
            }
        }

        // Обработка изображений
        val imageKeys = try {
            this.getFieldsImageOnUI()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } catch (e: Throwable) {
            logException("Mappers.DataObjectUI.toItemUI", "getFieldsImageOnUI()", e)
            emptyList()
        }

        for (key in imageKeys) {
            try {
                val photoId = jsonObject.optString(key, "0")
                when {
                    photoId != "0" -> {
                        try {
                            RealmManager.getPhotoByPhotoId(photoId)?.getPhoto_num()?.let { images.add(it) }
                        } catch (e: Throwable) {
                            logException("Mappers.DataObjectUI.toItemUI", "getPhotoByPhotoId for key=$key photoId=$photoId", e)
                        }
                    }

                    key == "photo_do_id" -> {
                        val hash = jsonObject.optString("photo_do_hash", "0")
                        if (hash.length > 12) {
                            try {
                                RealmManager.getPhotoByHash(hash)?.getPhoto_num()?.let { images.add(it) }
                            } catch (e: Throwable) {
                                logException("Mappers.DataObjectUI.toItemUI", "getPhotoByHash for hash=$hash", e)
                            }
                        } else {
                            idResImage?.let { images.add(it.toString()) }
                        }
                    }

                    else -> idResImage?.let { images.add(it.toString()) }
                }
            } catch (e: Throwable) {
                logException("Mappers.DataObjectUI.toItemUI", "processing image key=$key", e, "jsonSnippet=${jsonObject.optString(key)}")
            }
        }

        // локальная функция обновления полей — оборачиваем критичные операции в try/catch
        fun updateFields(key: String) {
            try {
                val valueRaw = jsonObject.opt(key) ?: return
                val label = try {
                    nameUIRepository.getTranslateString(key, this.getFieldTranslateId(key))
                } catch (e: Throwable) {
                    logException("Mappers.DataObjectUI.toItemUI", "getTranslateString for key=$key", e)
                    key
                }

                val fieldModifier = try {
                    this.getFieldModifier(key, jsonObject)
                } catch (e: Throwable) {
                    logException("Mappers.DataObjectUI.toItemUI", "getFieldModifier for key=$key", e)
                    null
                }

                val valueText = try {
                    this.getValueUI(key, valueRaw)
                } catch (e: Throwable) {
                    logException("Mappers.DataObjectUI.toItemUI", "getValueUI for key=$key", e, "raw=$valueRaw")
                    valueRaw?.toString() ?: ""
                }

                val valueModifier = try {
                    this.getValueModifier(key, jsonObject)
                } catch (e: Throwable) {
                    logException("Mappers.DataObjectUI.toItemUI", "getValueModifier for key=$key", e)
                    null
                }

                val field = FieldValue(
                    key,
                    TextField(key, "$label:", fieldModifier),
                    TextField(valueRaw, valueText, valueModifier)
                )

                rawFields.add(field)
                if (key !in hiddenList) {
                    fields.add(field)
                }
            } catch (e: Throwable) {
                logException("Mappers.DataObjectUI.toItemUI", "updateFields for key=$key", e, "jsonValue=${jsonObject.optString(key)}")
            }
        }

        val orderedKeys = try {
            this.getFieldsForOrderOnUI()?.map { it.trim() }?.toSet() ?: emptySet()
        } catch (e: Throwable) {
            logException("Mappers.DataObjectUI.toItemUI", "getFieldsForOrderOnUI()", e)
            emptySet()
        }

        val allKeys = try {
            jsonObject.keys().asSequence().toSet()
        } catch (e: Throwable) {
            logException("Mappers.DataObjectUI.toItemUI", "jsonObject.keys()", e)
            emptySet<String>()
        }

        try {
            orderedKeys.forEach { if (it in allKeys) updateFields(it) }
            (allKeys - orderedKeys).forEach { updateFields(it) }
        } catch (e: Throwable) {
            logException("Mappers.DataObjectUI.toItemUI", "iterating keys", e)
        }

        return DataItemUI(
            rawObj = listOf(this),
            rawFields = rawFields,
            fields = fields,
            images = images,
            modifierContainer = getContainerModifier(jsonObject),
            false
        )
    } catch (e: Throwable) {
        // глобальная ошибка — логируем всё и возвращаем безопасный пустой DataItemUI
        logException("Mappers.DataObjectUI.toItemUI", "top-level", e, "object=$this")
        return DataItemUI(
            rawObj = listOf(this),
            rawFields = emptyList(),
            fields = emptyList(),
            images = emptyList(),
            modifierContainer = null,
            false
        )
    }
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

// безопасно ставим/сбрасываем фон контейнера
fun DataItemUI.withContainerBackground(color: Color?): DataItemUI =
    copy(modifierContainer = (modifierContainer ?: MerchModifier()).copy(background = color))

// достать addr_id из rawFields как строку
fun DataItemUI.addrIdOrNull(): String? =
    rawFields.firstOrNull { it.key.equals("addr_id", ignoreCase = true) }
        ?.value?.rawValue
        ?.toString()
        ?.trim()


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




fun LocalDate.toDayMonthFormat(locale: Locale = Locale.getDefault()): String {
    val monthName = this.month.getDisplayName(TextStyle.FULL, locale)
    return "${this.dayOfMonth} $monthName"
}
