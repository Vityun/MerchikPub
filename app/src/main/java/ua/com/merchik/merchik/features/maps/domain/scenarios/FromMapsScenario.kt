package ua.com.merchik.merchik.features.maps.domain.scenarios

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.maps.domain.*
import java.util.*

class FromMapsScenario : MapScenario {
    override fun deriveStoreCenter(items: List<DataItemUI>): StoreCenter? {
        val candidate = items.firstOrNull { item ->
            val f = item.rawFields
            val lx = f.stringByKey("log_addr_location_xd")?.parseDoubleSafe()
            val ly = f.stringByKey("log_addr_location_yd")?.parseDoubleSafe()
            isValidLatLon(lx, ly)
        } ?: return null
        val f = candidate.rawFields
        val lx = f.stringByKey("log_addr_location_xd")!!.parseDoubleSafe()!!
        val ly = f.stringByKey("log_addr_location_yd")!!.parseDoubleSafe()!!
        val title =
            f.stringByKey("log_addr_txt") ?: candidate.rawObj.filterIsInstance<WpDataDB>().firstOrNull()?.addr_txt
        return StoreCenter(LatLng(lx, ly), title ?: "Магазин", f.addrIdInt())
    }


    override suspend fun buildPoints(
        items: List<DataItemUI>,
        addressResolver: suspend (Int) -> AddressSDB?
    ): List<StorePoint> =
        items.mapNotNull { item ->
            val f = item.rawFields
            val cx = f.stringByKey("CoordX")?.parseDoubleSafe()
            val cy = f.stringByKey("CoordY")?.parseDoubleSafe()
            if (!isValidLatLon(cx, cy)) return@mapNotNull null

            val idStr = f.stringByKey("addr_id")  // НЕ используем для distinct
            val wp = item.rawObj.filterIsInstance<WpDataDB>().firstOrNull()
//            val title = f.stringByKey("addr_txt") ?: wp?.addr_txt ?: "Результат"
            // 🔹 Формируем title из всех полей: "ключ значение\n"
            val title = buildString {
                item.fields.forEach { field ->
                    append("${field.field.value} ${field.value.value}\n")
                }
            }.trimEnd()

// Универсальный поиск CoordTime во всех полях
            val tMillis = (
                    item.fields.firstOrNull { it.key.equals("coord_time", true) || it.key.equals("CoordTime", true) }?.value?.rawValue
                        ?: f.firstOrNull { it.key.equals("coord_time", true) || it.key.equals("CoordTime", true) }?.value?.rawValue
                    )
                ?.toString()
                ?.trim()
                ?.toLongOrNull()

            Log.e("!!!",">>> time: $tMillis")
            StorePoint(
                id = idStr,                 // можно оставить, но не для distinct
                lat = cx!!,
                lon = cy!!,
                title = title,
                wp = wp,
                dataItemsUI = item,
                coordTimeMillis = tMillis
            )
        }
            // либо вообще без distinct:
            //.toList()

            // либо distinct только по координатам (с нормализацией до 6 знаков):
//            .distinctBy { "%.6f_%.6f".format(Locale.getDefault(), it.lat, it.lon) }


    override fun isInsideRadius(center: StoreCenter?, point: StorePoint, radiusMeters: Double): Boolean {
        center ?: return true
        val d = haversine(center.pos.latitude, center.pos.longitude, point.lat, point.lon)
        return d <= radiusMeters
    }


    override fun aggregateCash(items: List<DataItemUI>): Map<Int, Pair<Int, Double>> =
        items.asSequence()
            .mapNotNull { item ->
                val id = item.rawFields.addrIdInt() ?: return@mapNotNull null
                val value = item.rawFields.stringByKey("cash_ispolnitel")?.parseDoubleSafe()
                id to value
            }
            .filter { it.second != null }
            .groupBy({ it.first }, { it.second!! })
            .mapValues { (_, list) -> list.size to list.sum() }
}