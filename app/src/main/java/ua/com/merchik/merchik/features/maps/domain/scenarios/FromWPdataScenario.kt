package ua.com.merchik.merchik.features.maps.domain.scenarios

import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.maps.domain.*

class FromWPdataScenario : MapScenario {
    override fun deriveStoreCenter(items: List<DataItemUI>): StoreCenter? = null


    override suspend fun buildPoints(
        items: List<DataItemUI>,
        addressResolver: suspend (Int) -> AddressSDB?
    ): List<StorePoint> = items.mapNotNull { item ->
        val f = item.rawFields
        val idStr = f.stringByKey("addr_id")
        val idInt = idStr?.toIntOrNull()


        var lat = f.stringByKey("addr_location_xd")?.parseDoubleSafe()
            ?: f.stringByKey("CoordX")?.parseDoubleSafe()
        var lon = f.stringByKey("addr_location_yd")?.parseDoubleSafe()
            ?: f.stringByKey("CoordY")?.parseDoubleSafe()
        var title = f.stringByKey("addr_txt")?.takeIf { it.isNotBlank() }


        if ((!isValidLatLon(lat, lon) || title.isNullOrBlank()) && idInt != null) {
            addressResolver(idInt)?.let { addr ->
                if (lat == null) lat = addr.locationXd?.toDouble()
                if (lon == null) lon = addr.locationYd?.toDouble()
                if (title.isNullOrBlank()) title = addr.nm
            }
        }
        if (!isValidLatLon(lat, lon)) return@mapNotNull null


        val wp = item.rawObj.filterIsInstance<WpDataDB>().firstOrNull()
        StorePoint(
            id = idStr ?: "${'$'}lat_${'$'}lon",
            lat = lat!!,
            lon = lon!!,
            title = title ?: "Результат",
            wp = wp,
            dataItemsUI = item
        )
    }.distinctBy { it.id ?: "${'$'}{it.lat}_${'$'}{it.lon}" }


    override fun isInsideRadius(center: StoreCenter?, point: StorePoint, radiusMeters: Double): Boolean = true


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