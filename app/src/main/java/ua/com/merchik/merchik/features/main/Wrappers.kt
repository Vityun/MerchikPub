package ua.com.merchik.merchik.features.main

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
                MerchModifier(fontStyle = FontStyle.Italic, background = Color.Red, alignment = Alignment.End, weight = 1f)
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
                SimpleDateFormat("dd MMMM YYYY").format(Date(it))
            } ?: value.toString()
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
        "dt_end" -> 5897
        "dt_start" -> 5898
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
            } catch (e: Exception){
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
}