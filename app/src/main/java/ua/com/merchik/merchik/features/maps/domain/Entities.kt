package ua.com.merchik.merchik.features.maps.domain

import com.google.android.gms.maps.model.LatLng
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.model.DataItemUI

data class StorePoint(
    val id: String?,
    val lat: Double,
    val lon: Double,
    val title: String?,
    val wp: WpDataDB? = null,
    val dataItemsUI: DataItemUI? = null,
    val coordTimeMillis: Long? = null        // 👈 добавили
)


data class StoreCenter(
    val pos: LatLng,
    val title: String?,
    val addrId: Int?
)

data class PointUi(
    val point: StorePoint,
    val count: Int, // aggregated visits per addr
    val sum: Double, // aggregated sum per addr
    val insideRadius: Boolean // for FromMaps depends on distance; for FromWPdata always true
)