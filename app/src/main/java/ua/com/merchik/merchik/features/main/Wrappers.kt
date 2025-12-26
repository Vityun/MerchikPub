package ua.com.merchik.merchik.features.main

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.Utils.ValidatorEKL
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.Padding
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.EKL.EKLDataHolder
import ua.com.merchik.merchik.features.main.LogMPDBOverride.formatLocationUniqueToDms
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

object LogDBOverride {
    fun getHidedFieldsOnUI(): String =
        "id, tp, author"

    fun getTranslateId(key: String): Long? = when (key) {
        "nm" -> 1813
        "dt_update", "dt_start" -> 4043

        "addr_id" -> 5914
        "author" -> 5916
        "client_id" -> 5913
        "comments" -> 5911
        "dt" -> 5917
        "dt_action" -> 5910
        "id" -> 5909
        "obj_date" -> 5919
        "obj_id" -> 5915
        "session" -> 5918
        "tp" -> 5912
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
                MerchModifier(
                    fontStyle = FontStyle.Italic,
                    background = Color.Red,
                    alignment = Alignment.End,
                    weight = 1f
                )
            else
                MerchModifier(fontStyle = FontStyle.Italic)
        }

        "dt_update", "dt_start", "dt", "dt_action" -> MerchModifier(fontStyle = FontStyle.Italic)
        "comments" -> MerchModifier(fontStyle = FontStyle.Italic)
        else -> null
    }
}

object ThemeDBOverride {
    fun getTranslateId(key: String): Long? = when (key) {
        "nm" -> 5864
        "comment" -> 5862
        "ID" -> 5863

        "dt_update" -> 5984
        "grp_id" -> 5980
        "need_photo" -> 5982
        "need_report" -> 5983
        "tp" -> 5981
        else -> null
    }
}

object AddressSDBOverride {

    fun getTranslateId(key: String): Long? = when (key) {
        "dt_start", "dt", "dt_action" -> 4043

        "city_id" -> 5922
        "dt_update" -> 5926
        "id" -> 5920
        "kol_kass" -> 5929
        "location_xd" -> 5927
        "location_yd" -> 5928
        "nm" -> 5921
        "nomer_tt" -> 5930
        "obl_id" -> 5924
        "tp_id" -> 5923
        "tt_id" -> 5925
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
        "dt_update" -> 5935
        "edrpou" -> 5933
        "id" -> 5931
        "main_tov_grp" -> 5934
        "nm" -> 5932
        "ppa_auto" -> 5937
        "recl_reply_mode" -> 5936
        "work_restart_date" -> 5939
        "work_start_date" -> 5938
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
                SimpleDateFormat("dd MMMM yyyy").format(Date(it))
            } ?: value.toString()
        }

        "dt_start" -> {
            formatDateString(value.toString()) ?: value.toString()
        }

        "dt_end" -> {
            formatDateString(value.toString()) ?: value.toString()
        }

        else -> value.toString()
    }

    fun getTranslateId(key: String): Long? = when (key) {
        "ID" -> 5887
        "nm" -> 5901
        "notes" -> 5902
        "addr_id" -> 5879
        "addr_tt_id" -> 5904
        "author_id" -> 5880
        "client_id" -> 5899
        "color" -> 5900
        "disable_score" -> 5881
        "dt_change" -> 5882
        "dt_start" -> 5898
        "dt_end" -> 5897
        "exam_id" -> 5883
        "grp_id" -> 5884
        "hide_client" -> 5885
        "hide_user" -> 5886
        "not_approve" -> 5888
        "options_id" -> 5889
        "option_id" -> 5896
        "showcase_tp_id" -> 5890
        "site_id" -> 5891
        "summ" -> 5892
        "theme_id" -> 5893
        "tovar_id" -> 5894
        "tovar_manufacturer_id" -> 5903
        "user_id" -> 5895
        else -> null
    }

    fun getContainerModifier(jsonObject: JSONObject): MerchModifier {
        val color =
            try {
                val colorHex = jsonObject.optString("color", "")
                Color(android.graphics.Color.parseColor("#$colorHex"))
            } catch (e: Exception) {
                null
            }
        return MerchModifier(background = color)
    }

    fun getValueModifier(key: String, jsonObject: JSONObject): MerchModifier? = when (key) {
        else -> MerchModifier(padding = Padding(start = 10.dp))
    }
}

object TradeMarkDBOverride {
    fun getValueModifier(key: String, jsonObject: JSONObject): MerchModifier? = when (key) {
        else -> MerchModifier(padding = Padding(start = 10.dp))
    }

    fun getTranslateId(key: String): Long? = when (key) {
        "ID" -> 5940
        "nm" -> 5941
        "dt_update" -> 5942
        "sort_type" -> 5943
        else -> null
    }
}

object LogMPDBOverride {
    fun getTranslateId(key: String): Long? = when (key) {
//        "CoordAccuracy" -> 5562
//        "CoordTime" -> 5563
//        "distance" -> 5564
//        "provider" -> 5566

        "address" -> 5957
        "codeDad2" -> 5955
        "CoordAccuracy" -> 5953
        "CoordAltitude" -> 5950
        "CoordSpeed" -> 5952
        "CoordTime" -> 5951
        "CoordX" -> 5948
        "CoordY" -> 5949
        "distance" -> 5958
        "gp" -> 5946
        "id" -> 5944
        "inPlace" -> 5959
        "locationUniqueString" -> 5961
        "mocking" -> 5954
        "provider" -> 5947
        "serverId" -> 5945
        "upload" -> 5960
        "vpi" -> 5956
        else -> null
    }

    fun getValueUI(key: String, value: Any): String = when (key) {
        "CoordTime" ->
            value.toString().toLongOrNull()?.let {
                SimpleDateFormat("dd.MM HH:mm:ss").format(Date(it))
            } ?: value.toString()

        "distance" ->
            (value as? Int)?.let {
                if (it > 1000) "${it / 1000} км." else "$it м."
            } ?: "0 м."

        "provider" -> if (value == 1) "GPS" else "GSM"

        "CoordAccurancy" -> if (value == 1) "GPS" else "GSM"

        "locationUniqueString" -> formatLocationUniqueToDms(value.toString())

        "CoordY" ->
            (value as? Number)?.let { String.format(Locale.US, "%.6f", it.toDouble()) } ?: ""


        "CoordX" ->
            (value as? Number)?.let { String.format(Locale.US, "%.6f", it.toDouble()) } ?: ""


        else -> value.toString()
    }

    fun getValueModifier(key: String, jsonObject: JSONObject): MerchModifier? = when (key) {
        else -> MerchModifier(padding = Padding(start = 10.dp))
    }

    private fun parseLatLonUkraine(raw: String?): Pair<Double, Double>? {
        if (raw.isNullOrBlank()) return null
        val s = raw.removePrefix("1") // убираем возможный префикс

        // собрать индексы точек
        val dotIndexes = s.withIndex().filter { it.value == '.' }.map { it.index }
        if (dotIndexes.size < 2) return null

        // Попробуем все возможные разрезы между первой и второй точкой
        val firstDot = dotIndexes[0]
        val secondDot = dotIndexes[1]

        for (end in (firstDot + 1)..(secondDot - 1)) {
            try {
                val latStr = s.substring(0, end + 1)   // включительно end (точка и дробная часть)
                val lonStr = s.substring(end + 1)      // остаток

                val lat = latStr.toDoubleOrNull() ?: continue
                val lon = lonStr.toDoubleOrNull() ?: continue

                // ограничение для Украины: широта ~ [44,53], долгота ~ [22,40]
                if (lat in 44.0..53.0 && lon in 22.0..40.0) {
                    return Pair(lat, lon)
                }
            } catch (t: Throwable) {
                // игнорируем и пробуем следующую позицию
            }
        }

        // fallback: взять первые два вещественных числа и проверить диапазон
        val regex = Regex("[-+]?[0-9]*\\.?[0-9]+")
        val found = regex.findAll(s).map { it.value }.toList()
        if (found.size >= 2) {
            val lat = found[0].toDoubleOrNull()
            val lon = found[1].toDoubleOrNull()
            if (lat != null && lon != null && lat in 44.0..53.0 && lon in 22.0..40.0) {
                return Pair(lat, lon)
            }
        }

        return null
    }

    /**
    градусов-минут-секунд (DMS)
     **/
    fun toDmsString(lat: Double, lon: Double): String {
        fun convert(coord: Double, isLat: Boolean): String {
            val hemi = if (isLat) if (coord >= 0) "N" else "S" else if (coord >= 0) "E" else "W"
            val absCoord = abs(coord)
            val degrees = floor(absCoord).toInt()
            val minutesFloat = (absCoord - degrees) * 60.0
            val minutes = floor(minutesFloat).toInt()
            val seconds = (minutesFloat - minutes) * 60.0
            // форматируем секунды с одной десятичной
            val secondsStr = String.format(Locale.US, "%.1f", seconds)
            return "%d°%d'%s\"%s".format(degrees, minutes, secondsStr, hemi)
        }

        val latStr = convert(lat, true)
        val lonStr = convert(lon, false)
        // между ними пробел
        return "$latStr\n$lonStr"
    }

    /** Вывод координат в десятичном формате с 6 знаками после запятой */
    fun toDecimalString(lat: Double, lon: Double): String {
        return String.format(Locale.US, "%.6f, %.6f", lat, lon)
    }

    /** Удобная оболочка: принимает строку из БД и возвращает DMS либо пустую строку */
    fun formatLocationUniqueToDms(raw: String?): String {
        val latLon = parseLatLonUkraine(raw) ?: return ""
        return toDecimalString(latLon.first, latLon.second)
    }
}

object SamplePhotoSDBOverride {
    fun getFieldsForOrderOnUI(): List<String> = "nm, about".split(",").map { it.trim() }

    fun getValueModifier(key: String, jsonObject: JSONObject): MerchModifier {
        return MerchModifier(
            maxLine = 50
        )
    }

    fun getHidedFieldsOnUI(): String =
        "photoServerId, example_id, commentUpload, doc_id"
}

object TovarDBOverride {
    fun getHidedFieldsOnUI(): String =
        "barcode, client_id, client_id2, deleted, depth, dt_update, expire_period, group_id, " +
                "height, ID, manufacturer_id, photo_id, related_tovar_id, width, sortcol"

    fun getFieldFirstImageUI(): String = "photo_id"

    fun getTranslateId(key: String): Long? = when (key) {
        "barcode" -> 5970
        "client_id" -> 5963
        "client_id2" -> 5964
        "deleted" -> 5978
        "depth" -> 5977
        "dt_update" -> 5972
        "expire_period" -> 5979
        "group_id" -> 5968
        "height" -> 5975
        "ID" -> 5962
        "manufacturer_id" -> 5969
        "nm" -> 5965
        "photo_id" -> 5974
        "related_tovar_id" -> 5971
        "sortcol" -> 5973
        "weight" -> 5966
        "weight_gr" -> 5967
        "width" -> 5976
        else -> null
    }

    fun getValueUI(key: String, value: Any): String = when (key) {
        "weight_gr" -> "ФП/ЦВКДОШАНП"
        else -> value.toString()
    }

}

object UsersSDBOverride {

    fun getHidedFieldsOnUI(): String =
//        ""
        "user_id, author_id, report_date_01, report_date_05, report_date_20, report_date_40, report_date_200, " +
                "img_personal_photo_thumb, img_personal_photo, img_personal_photo_path, " +
                "department, dt_update, city_id, inn, send_sms, fired, fired_reason, fired_dt, report_count, " +
                "tel_corp, " +
                "tel2_corp, " +
//                "tel, tel2" +
                "flag" +
                ""

    fun getValueUI(key: String, value: Any): String = when (key) {
//        "tel" -> {
//            value.toString().takeIf { it.isNotEmpty() }?.let {
//                it.replace(Regex(".(?=.{4})"), "*")
//            } ?: value.toString()
//        }
//
//        "tel2" -> {
//            value.toString().takeIf { it.isNotEmpty() }?.let {
//                it.replace(Regex(".(?=.{4})"), "*")
//            } ?: value.toString()
//        }


        "otdel_id" -> {
            RoomManager.SQL_DB.tovarGroupDao().getById(value as Int)?.nm
                ?: "Відділ не визначено ($value)"
        }

        else -> value.toString()
    }

    fun getTranslateId(key: String): Long? = when (key) {
        "fio" -> 7789
        "tel" -> 7790
        "tel2" -> 7791
        "otdel_id" -> 7792
        "work_addr_id" -> 5879
        else -> null
    }

    fun getContainerModifier(jsonObject: JSONObject): MerchModifier {
        val color = try {
            val otdelId =
                jsonObject.optInt("otdel_id", -1) // Получаем значение "otdel_id", по умолчанию -1
//            Log.d(
//                "getContainerModifier",
//                "otdel_id: $otdelId usersPTTtovarIdList: ${EKLDataHolder.instance().usersPTTtovarIdList}"
//            )
//            Log.d(
//                "getContainerModifier",
//                "JSONObject: $jsonObject"
//            )
            if (otdelId == 0 || otdelId == -1) {
                Color(android.graphics.Color.parseColor("#FFC4C4")) // Цвет для несовпадения
            } else
            // Проверка на пустоту или наличие только одного элемента 0
                if (EKLDataHolder.instance().usersPTTtovarIdList.isEmpty() || (EKLDataHolder.instance().usersPTTtovarIdList.size == 1 && EKLDataHolder.instance().usersPTTtovarIdList[0] == 0)) {
//                    Log.d("getContainerModifier", "usersPTTtovarIdList is empty or contains only 0")
//                Color(android.graphics.Color.parseColor("#FFC4C4")) // Цвет для несовпадения
                    Color(android.graphics.Color.parseColor("#00FF77")) // Цвет для совпадения
                } else if (otdelId in EKLDataHolder.instance().usersPTTtovarIdList) {
//                    Log.d("getContainerModifier", "otdel_id in usersPTTtovarIdList: $otdelId")
                    Color(android.graphics.Color.parseColor("#00FF77")) // Цвет для совпадения
                } else {
//                    Log.d("getContainerModifier", "otdel_id out usersPTTtovarIdList: $otdelId")
                    if (ValidatorEKL.controlEKL().result)
                        Color(android.graphics.Color.parseColor("#00FF77")) // Цвет для совпадения
                    else
                        Color(android.graphics.Color.parseColor("#FFC4C4")) // Цвет для несовпадения
                }
        } catch (e: Exception) {
            Log.e("getContainerModifier", "getContainerModifier Error: ${e.message}", e)
            null // Обработка ошибок
        }
//        Log.d("getContainerModifier", "Color: $color")
        return MerchModifier(background = color)
    }

    fun getValueModifier(key: String, jsonObject: JSONObject): MerchModifier? = when (key) {

        "tel", "tel2" -> {
            MerchModifier(fontStyle = FontStyle.Italic)
        }

        "notes" -> {
            MerchModifier(
                maxLine = 50
            )
        }

        else -> null
    }

//    fun getFieldsForOrderOnUI(jsonObject: JSONObject): List<String> {
//         val js = jsonObject.keys().forEach { key ->
//            if (this.getFieldsForOrderOnUI()?.contains(key) != true) updateFields(key)
//        }
//        return js
//    }

}

object OpinionSDBOverride {

    fun getHidedFieldsOnUI(): String =
        "dt_change, grp_id, ID"

}

object PlanogrammVizitShowcaseSDBOverride {

    fun getValueModifier(key: String, jsonObject: JSONObject): MerchModifier? = when (key) {
        "comments" -> {
            val score = jsonObject.optString("score", "") // Безопасное получение score
//            val commentsText = if (score == "0") {
//                "Вкажіть оцінку планограми від 1 до 9"
//            } else {
//                "Поточна оцінка планограми:"
//            }

            // Обновляем значение comments в JSONObject
            jsonObject.put(
                "comments",
                "Фото ще не завантажилися. Зачекайте кілька хвилин і спробуйте ще раз. Або зробіть примусову синхронізацію"
            )

            // Возвращаем модификатор в зависимости от условия
            if (score == "0") {
                MerchModifier(
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal,
                    weight = 1f
                )
            } else {
                MerchModifier()
            }
        }

        else -> null
    }

    fun getContainerModifier(jsonObject: JSONObject): MerchModifier {
        val color =
            try {
                val colorHex = jsonObject.optString("color", "")
                Color(android.graphics.Color.parseColor("#$colorHex"))
            } catch (e: Exception) {
                null
            }
        return MerchModifier(background = color)
    }
}

object StackPhotoDBOverride {

    fun getContainerModifier(jsonObject: JSONObject): MerchModifier {
        try {
            val specialCol = jsonObject.optInt("specialCol", 0)
            if (specialCol == 2)
                return MerchModifier(background = Color(android.graphics.Color.parseColor("#FFC4C4")))
            if (specialCol == 1)
                return MerchModifier(background = Color(android.graphics.Color.parseColor("#00FF77")))


        } catch (_: Exception) {

        }
        return MerchModifier()
    }

    fun getTranslateId(key: String): Long? = when (key) {
        "dt" -> 1100
        "user_txt" -> 1103
        "addr_txt" -> 1101
        "client_txt" -> 1102
        "theme_id" -> 8724
        "status" -> 3167
        "main_option_id" -> 8725
        "cash_ispolnitel" -> 8751
        //группа 2340

        "dt_update" -> 5926
        "nomer_tt" -> 5930
        "obl_id" -> 5924
        "tp_id" -> 5923
        "tt_id" -> 5925
        "client_start_dt" -> 9062
        "client_end_dt" -> 9063
        "sku" -> 9065
        "duration" -> 9064
        "smeta" -> 9154
        "doc_num_otchet" -> 9155

        "comment" -> 5911

        else -> null
    }

    fun getValueUI(key: String, value: Any): String = when (key) {

        "create_time" -> {
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy",  Locale.getDefault())
            if (value.toString() == "0")
                "-"
            else
                try {
                    val millis = value.toString().toLong()
                    Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .format(formatter)
                } catch (e: Exception) {
                    "Робота не розпочата"
                }
        }
        "" -> {""}

        else -> value.toString()
    }

//
}


object LogMPDBDBOverride {

    fun getContainerModifier(jsonObject: JSONObject): MerchModifier {
        try {
            val distance = jsonObject.optInt("distance", 0)
            if (distance == 0)
                return MerchModifier()
            return if (distance > Globals.distanceMin)
                MerchModifier(background = Color(android.graphics.Color.parseColor("#FFC4C4"))) //error
            else
                MerchModifier(background = Color(android.graphics.Color.parseColor("#00FF77"))) //good


        } catch (_: Exception) {

        }
        return MerchModifier()
    }
}


object WPDataBDOverride {
    fun getValueUI(key: String, value: Any, wpDataDB: WpDataDB): String = when (key) {
        "dt" -> {
            formatDateString(value.toString()) ?: value.toString()
        }

        "client_start_dt" -> {
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm",  Locale.getDefault())
            if (value.toString() == "0")
                "-"
            else
                try {
                    val millis = value.toString().toLong()
                    Instant.ofEpochMilli(millis * 1000)
                        .atZone(ZoneId.systemDefault())
                        .format(formatter)
                } catch (e: Exception) {
                    "Робота не розпочата"
                }
        }

        "client_end_dt" -> {
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.getDefault())
            if (value.toString() == "0")
                "-"
            else
                try {
                    val millis = value.toString().toLong()
                    Instant.ofEpochMilli(millis * 1000)
                        .atZone(ZoneId.systemDefault())
                        .format(formatter).toString()
                } catch (e: Exception) {
                    "Робота не закінчена"
                }
        }

        "theme_id" -> try {
            val tt = ThemeRealm.getThemeById(value.toString()).nm
            tt
        } catch (e: Exception) {
            "Тема не виявлена"
        }

        "cash_ispolnitel" -> try {
            "$value грн"
        } catch (e: Exception) {
            "Дані відсутні"
        }

        "duration" -> try {
            "$value хв."
        } catch (e: Exception) {
            value.toString()
        }

        "status" -> try {
            when (value) {
                0 -> "Робота не розпочата (звiт не проведено)"
                1 -> "Роботу виконано (звiт проведено)"
                2 -> "Роботу виконано (звiт не проведено)"
                3 -> "Робота виконується (звiт не проведено)"
                else -> "Дані відсутні"
            }

        } catch (e: Exception) {
            "Дані відсутні"
        }

        "user_txt" -> try {
            if (wpDataDB.user_id == 14041)
                "Нет исполнителя" else
                value.toString()
        } catch (e: Exception) {
            ""
        }

        "sku" -> try {
            "$value шт."
        } catch (e: Exception) {
            ""
        }

        else -> value.toString()
    }

    fun getTranslateId(key: String): Long? = when (key) {
        "dt" -> 1100
        "user_txt" -> 1103
        "addr_txt" -> 1101
        "client_txt" -> 1102
        "theme_id" -> 8724
        "status" -> 3167
        "main_option_id" -> 8725
        "cash_ispolnitel" -> 8751
        //группа 2340

        "dt_update" -> 5926
        "nomer_tt" -> 5930
        "obl_id" -> 5924
        "tp_id" -> 5923
        "tt_id" -> 5925
        "client_start_dt" -> 9062
        "client_end_dt" -> 9063
        "sku" -> 9065
        "duration" -> 9064
        "smeta" -> 9154
        "doc_num_otchet" -> 9155

        else -> null
    }

    fun getFieldsForOrderOnUI(): List<String> =
        "theme_id, main_option_id".split(",").map { it.trim() }


    fun getValueModifier(key: String, jsonObject: JSONObject): MerchModifier? = when (key) {
        "status" -> {
            val status: String = jsonObject.get("status").toString()
            when (status) {
                "0" -> {
                    val dtStart = jsonObject.getLong("client_start_dt")
                    if (dtStart > 0)
                        MerchModifier(
                            textColor = Color(android.graphics.Color.parseColor("#FF6D00"))
                        )
                    else
                        MerchModifier(
                            textColor = Color.Red
                        )
                }
                "1" -> {
                    MerchModifier(
                        textColor = Color(android.graphics.Color.parseColor("#00FF00"))
                    )
                }
                else ->          MerchModifier(
                    textColor = Color(android.graphics.Color.parseColor("#FF6D00"))
                )
            }
        }

        else -> null
    }
}

@SuppressLint("SimpleDateFormat")
fun formatDateString(raw: String?): String {
    if (raw.isNullOrBlank()) return ""

    val possiblePatterns = listOf(
        "EEE MMM dd HH:mm:ss zzz yyyy",   // Wed Oct 09 18:42:15 GMT+03:00 2024
        "MMM dd, yyyy hh:mm:ss a",        // Oct 09, 2025 12:00:00 AM
        "MMM dd yyyy HH:mm:ss",           // Oct 09 2025 00:00:00
        "MMM d, yyyy HH:mm:ss",
        "MMM d yyyy HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss",          // ISO без зоны
        "yyyy-MM-dd HH:mm:ss",            // частый формат SQL
        "dd.MM.yyyy"
    )

    val date = possiblePatterns.firstNotNullOfOrNull { pattern ->
        try {
            SimpleDateFormat(pattern, Locale.ENGLISH).parse(raw)
        } catch (_: Exception) {
            null
        }
    } ?: return raw

    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(date)
}


