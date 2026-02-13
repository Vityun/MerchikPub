package ua.com.merchik.merchik.dataLayer

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import org.json.JSONObject
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.model.*
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.features.main.componentsUI.CardItemsData
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenuAction
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenuState
import ua.com.merchik.merchik.features.main.componentsUI.MessageDialogData
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import java.util.zip.CRC32

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

    fun getPreferredFieldOrder(): List<String> = emptyList()

}


private fun logException(tag: String, where: String, e: Throwable, extra: String? = null) {
    val sw = StringWriter()
    e.printStackTrace(PrintWriter(sw))
    val stack = sw.toString()
    val extraInfo = extra?.let { " | extra: $it" } ?: ""
    Log.e("ERROR","$tag, $where | Exception ${e::class.java.name}: ${e.message}$extraInfo\n" +
            stack
    )

    Globals.writeToMLOG("ERROR", tag, "$where | Exception ${e::class.java.name}: ${e.message}$extraInfo\n$stack")
}


fun DataObjectUI.toItemUI(
    nameUIRepository: NameUIRepository,
    hideUserFields: String?,
    typePhoto: Int?,
    groupingKeys: List<String> = emptyList()
): DataItemUI {
    val gson = Gson()
    val jsonObject = try {
        JSONObject(gson.toJson(this))
    } catch (e: Throwable) {
        logException(
            "Mappers.DataObjectUI.toItemUI",
            "JSONObject(gson.toJson(this))",
            e,
            "source object: $this"
        )
        JSONObject()
    }

    // ---- получение стабильного реального id ----
    val stableIdFromSource: Long? = try {
        val idKey = jsonObject.keys().asSequence()
            .firstOrNull { it.equals("id", ignoreCase = true) }

        if (idKey != null) {
            val rawValue = jsonObject.opt(idKey)
            val trimmed = rawValue?.toString()?.trim() ?: ""

            val num = trimmed.toLongOrNull()
            if (num != null && num != 0L) {
                num
            } else if (trimmed.isNotEmpty()) {
                stableLongFromString(trimmed)
            } else {
                null
            }
        } else {
            Globals.writeToMLOG(
                "WARN",
                "Mappers.DataObjectUI.toItemUI",
                "ID key not found in json: $jsonObject"
            )
            null
        }
    } catch (e: Throwable) {
        logException("Mappers.DataObjectUI.toItemUI", "extract stableId", e, "json: $jsonObject")
        null
    }
    // ---------------------------------------------------------

    val fields = mutableListOf<FieldValue>()
    val rawFields = mutableListOf<FieldValue>()
    val images = mutableListOf<String>()

    // Скрытые поля
    val hiddenList: Set<String> = (
            (hideUserFields?.split(",") ?: emptyList()) +
                    this.getHidedFieldsOnUI().split(",")
            ).map { it.trim() }
        .filter { it.isNotBlank() }
        .toSet()

    val idResImage = this.getIdResImage()
    if (idResImage != null) {
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
    val imageKeys = getFieldsImageOnUI()
        .split(",").map { it.trim() }.filter { it.isNotEmpty() }

// мапа "id-key -> hash-key"
    val hashByIdKey = mapOf(
        "photoServerId" to "photo_hash",
        "photo_do_id" to "photo_do_hash",
        // добавь остальные пары при необходимости:
        // "photo_posle_id" to "photo_posle_hash",
    )

    for (key in imageKeys) {
        val num = resolvePhotoNum(jsonObject, key, hashByIdKey[key], idResImage)
        if (num != null) images.add(num)   // без дедупа, как раньше по смыслу
    }

//    val imageKeys = this.getFieldsImageOnUI()
//        .split(",")
//        .map { it.trim() }
//        .filter { it.isNotEmpty() }
//
//    for (key in imageKeys) {
//        resolvePhotoNum(jsonObject, key, idResImage)?.let { num ->
//            if (!images.contains(num)) images.add(num)
//        }
//    }

//    val imageKeys = this.getFieldsImageOnUI()
//        .split(",")
//        .map { it.trim() }
//        .filter { it.isNotEmpty() }
//
//    for (key in imageKeys) {
//        val photoId = jsonObject.optString(key, "0")
//        when {
//            photoId != "0" -> {
//                RealmManager.getPhotoByPhotoId(photoId)?.getPhoto_num()?.let { images.add(it) }
//            }
//
//            key == "photo_do_id" -> {
//                val hash = jsonObject.optString("photo_do_hash", "0")
//                if (hash.length > 12) {
//                    RealmManager.getPhotoByHash(hash)?.getPhoto_num()?.let { images.add(it) }
//                } else {
//                    idResImage?.let { images.add(it.toString()) }
//                }
//            }
//
//            else -> idResImage?.let { images.add(it.toString()) }
//        }
//    }

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

    // --- Универсальная логика формирования порядка ключей ---

    // 0) Ключи группировки (если есть и если такие поля реально есть в объекте)
    val groupingOrder: List<String> = try {
        groupingKeys
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    } catch (_: Throwable) {
        emptyList()
    }

    // 1) приоритетные поля из модели
    val preferredOrder: List<String> = try {
        this.getPreferredFieldOrder()
    } catch (_: Throwable) {
        emptyList()
    }

    // 2) поля из конфигурации orderOnUI (если заданы)
    val rawOrderedKeys =
        this.getFieldsForOrderOnUI()?.map { it.trim() }?.toSet() ?: emptySet()

    // 3) все ключи из json
    val allKeys: Set<String> = jsonObject.keys().asSequence().toSet()

    // Собираем итоговый порядок без дубликатов, сохраняя вставку
    val finalOrderedKeys = LinkedHashSet<String>()

    // 0) Сначала — поля группировки (в порядке уровней: 1-я группа, 2-я, 3-я...)
    for (k in groupingOrder) {
        if (k in allKeys) finalOrderedKeys.add(k)
    }

    // 1) затем — приоритетные поля модели
    for (k in preferredOrder) {
        if (k in allKeys) finalOrderedKeys.add(k)
    }

    // 2) затем — поля из orderOnUI
    for (k in rawOrderedKeys) {
        if (k in allKeys) finalOrderedKeys.add(k)
    }

    // 3) в конце — все остальные
    for (k in allKeys) {
        finalOrderedKeys.add(k)
    }

    // Генерация полей в финальном порядке
    for (key in finalOrderedKeys) {
        updateFields(key)
    }

    return DataItemUI(
        rawObj = listOf(this),
        rawFields = rawFields,
        fields = fields,
        images = images,
        modifierContainer = getContainerModifier(jsonObject),
        selected = false,
        stableId = stableIdFromSource ?: DataItemIdGenerator.nextId()
    )
}

private fun resolvePhotoNum(
    json: JSONObject,
    idKey: String,
    hashKey: String?,
    idResImage: Int?
): String? {
    fun norm(s: String) = s.trim().takeIf { it.isNotEmpty() && it != "0" && it != "null" }

    // 1) по id
    val id = norm(json.optString(idKey, ""))
    if (id != null) {
        RealmManager.getPhotoByPhotoId(id)?.getPhoto_num()?.let { return it }
        RealmManager.getPhotoById(null, id)?.getPhoto_num()?.let { return it }
    }

    // 2) по hash (если задан)
    val hash = hashKey?.let { norm(json.optString(it, "")) }
    if (hash != null && hash.length > 12) {
        RealmManager.getPhotoByHash(hash)?.getPhoto_num()?.let { return it }
    }

    // 3) плейсхолдер
    return idResImage?.toString()
}


private fun resolvePhotoNum(
    json: JSONObject,
    idKey: String,
    idResImage: Int?
): String? {

    fun norm(s: String) = s.trim().takeIf { it.isNotEmpty() && it != "0" && it != "null" }

    // 1) пробуем по idKey (photoServerId)
    val id = norm(json.optString(idKey, ""))
    if (id != null) {
        RealmManager.getPhotoByPhotoId(id)?.getPhoto_num()?.let { return it }
        RealmManager.getPhotoById(null, id)?.getPhoto_num()?.let { return it } // если ты используешь именно getPhotoById
    }

    // 2) fallback по hash ТОЛЬКО для этого кейса
    if (idKey == "photoServerId") {
        val hash = norm(json.optString("photo_hash", ""))
        if (hash != null && hash.length > 12) {
            RealmManager.getPhotoByHash(hash)?.getPhoto_num()?.let { return it }
        }
    }

    // 3) fallback на ресурс (логотип) — только если реально ничего не найдено
    return idResImage?.toString()
}


fun DataItemUI.withGroupingOnTop(groupingKeys: List<String>): DataItemUI {
    if (groupingKeys.isEmpty()) return this

    return this.copy(
        rawFields = rawFields.reorderByGrouping(groupingKeys),
        fields = fields.reorderByGrouping(groupingKeys)
    )
}
private fun List<FieldValue>.reorderByGrouping(groupingKeys: List<String>): List<FieldValue> {
    if (groupingKeys.isEmpty()) return this

    // порядок ключей группировки: key -> index (0,1,2...)
    val keyOrder = groupingKeys
        .withIndex()
        .associate { it.value to it.index }

    val grouped = mutableListOf<FieldValue>()
    val others = mutableListOf<FieldValue>()

    for (fv in this) {
        if (keyOrder.containsKey(fv.key)) {
            grouped += fv
        } else {
            others += fv
        }
    }

    // поля группировки сортируем по уровню (1-я группа, 2-я, 3-я...)
    grouped.sortBy { keyOrder[it.key] }

    return grouped + others
}



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
    ContextMenuAction.ShowAllVizitInAdress -> R.drawable.ic_37
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
    TOVAR_FROM_TOVAR_TABS,
    STACK_PHOTO_TO_FROM_ACHIEVEMENT,
    STACK_PHOTO_TO_FROM_PLANOGRAMM_VIZIT,
    STACK_PHOTO_AFTER_FROM_ACHIEVEMENT,
    USERS_SDB_FROM_EKL,
    PLANOGRAMM_VIZIT_SHOWCASE,
    SHOWCASE,
    SHOWCASE_COMPLETED_CHECK,
    WP_DATA_IN_CONTAINER,
    WP_DATA_ADDITIONAL_IN_CONTAINER,
    WP_DATA_IN_CONTAINER_MULT,
    WP_DATA_ADDITIONAL_IN_CONTAINER_MULT,
    SMS_PLAN_DEFAULT,
    JOURNAL_PHOTO_DEFAULT,
    STACK_PHOTO_FROM_OPTION_158605,     // Корпоративный блок (40)
    SAMPLE_PHOTO_FROM_OPTION_135158,    // Фото Остатков Товаров (ФОТ) (4) +
    SAMPLE_PHOTO_FROM_OPTION_141360,    // Фото товара на складе +
    SAMPLE_PHOTO_FROM_OPTION_132969,    // Фото Тележка с Товаром (ФТТ) (10) +
    SAMPLE_PHOTO_FROM_OPTION_135809,    // Фото витрины до начала работ (14) +
    SAMPLE_PHOTO_FROM_OPTION_158309,    // Фото витрины наближене (39) +
    SAMPLE_PHOTO_FROM_OPTION_158604,    // Фото витрины наполненности (41) +
    SAMPLE_PHOTO_FROM_OPTION_157277,    // Фото акционного товра + ценник (28? = 26!)
    SAMPLE_PHOTO_FROM_OPTION_157354,    // Фото ДМП (42)
    SAMPLE_PHOTO_FROM_OPTION_164355,    // Фото планограмы (5)
    SAMPLE_PHOTO_FROM_OPTION_169108,    // фото POS материалов (47) +
    SAMPLE_PHOTO_FROM_OPTION_172100     // Фото вітрини з акційними цінниками (48)
}




fun LocalDate.toDayMonthFormat(locale: Locale = Locale.getDefault()): String {
    val monthName = this.month.getDisplayName(TextStyle.FULL, locale)
    return "${this.dayOfMonth} $monthName"
}


/** Конвертирует строку в стабильный Long через CRC32 (детерминированно). */
private fun stableLongFromString(s: String?): Long {
    if (s == null) return 0L
    val bytes = s.toByteArray(Charsets.UTF_8)
    val crc = CRC32()
    crc.update(bytes)
    // возвращаем положительное значение
    return crc.value.toLong()
}

/**
 * Пытается найти реальный id в JSONObject.
 * Приоритет:
 * 1) ключ, равный "id" (игнорируя регистр)
 * 2) ключи, заканчивающиеся на "_id" (игнорируя регистр)
 * 3) ключи, содержащие "id" (как крайняя мера)
 * Возвращает Pair(foundKey, stringValue) или null если не найдено.
 */
private fun findIdKeyAndValue(jsonObject: JSONObject): Pair<String, String>? {
    val keys = jsonObject.keys().asSequence().toList()

    // 1) точное совпадение "id" (независимо от регистра)
    val exactIdKey = keys.firstOrNull { it.equals("id", ignoreCase = true) }
    if (exactIdKey != null) return Pair(exactIdKey, jsonObject.optString(exactIdKey, ""))

    // 2) ключи, заканчивающиеся на "_id" (игнорируем регистр), предпочитаем более короткие имена
    val underscoreId = keys
        .filter { it.endsWith("_id", ignoreCase = true) }
        .sortedBy { it.length } // короткие имена выше по приоритету
        .firstOrNull()
    if (underscoreId != null) return Pair(underscoreId, jsonObject.optString(underscoreId, ""))

    // 3) ключи, заканчивающиеся на "id" (без подчеркивания), например "Id", "UserId"
    val suffixId = keys
        .filter { it.length >= 2 && it.endsWith("id", ignoreCase = true) }
        .sortedBy { it.length }
        .firstOrNull()
    if (suffixId != null) return Pair(suffixId, jsonObject.optString(suffixId, ""))

    // 4) крайняя мера — любой ключ, содержащий "id" как подстроку (игнорир.)
    val containsId = keys.firstOrNull { it.contains("id", ignoreCase = true) }
    if (containsId != null) return Pair(containsId, jsonObject.optString(containsId, ""))

    return null
}

@SuppressLint("SimpleDateFormat")
private fun formatDateString(raw: String?): String {
    if (raw.isNullOrBlank()) return ""

    val possiblePatterns = listOf(
        "EEE MMM dd HH:mm:ss zzz yyyy",   // Wed Oct 09 18:42:15 GMT+03:00 2024
        "MMM dd, yyyy hh:mm:ss a",        // Oct 09, 2025 12:00:00 AM
        "MMM dd yyyy HH:mm:ss",           // Oct 09 2025 00:00:00
        "yyyy-MM-dd'T'HH:mm:ss",          // ISO без зоны
        "yyyy-MM-dd HH:mm:ss"             // частый формат SQL
    )

    val date = possiblePatterns.firstNotNullOfOrNull { pattern ->
        try {
            SimpleDateFormat(pattern, Locale.ENGLISH).parse(raw)
        } catch (_: Exception) {
            null
        }
    } ?: return ""

    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("uk")) // или Locale("ru")
    return formatter.format(date)
}


sealed interface MainEvent {
    data class ShowContextMenu(val menuState: ContextMenuState) : MainEvent
    data class ShowCardItemsDialog(val cardItemsData: CardItemsData) : MainEvent
    data class ShowLoading(val title: String, val durationMs: Long = 28_700L) : MainEvent
    data object LoadingCompleted : MainEvent
    data object LoadingCanceled : MainEvent
    data class ShowMessageDialog(val data: MessageDialogData) : MainEvent
    data object JumpToVizitAndCloseMaps : MainEvent

}


sealed interface PendingAction {
    data class JumpToAddressVisits(
        val addrText: String,
        val addrTitle: String?,      // например wp.addr_txt для subtitle
        val periodText: String       // уже готовая строка "12.01.2026 по 18.01.2026"
    ) : PendingAction
}
