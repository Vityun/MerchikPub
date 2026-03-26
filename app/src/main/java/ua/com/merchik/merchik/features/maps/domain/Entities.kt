package ua.com.merchik.merchik.features.maps.domain

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import android.view.View
import com.google.android.gms.maps.GoogleMap
import ua.com.merchik.merchik.dataLayer.LaunchOrigin


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


fun GoogleMap.latLngToLaunchOrigin(
    latLng: LatLng,
    anchorView: View,
    widthPx: Int = 56,
    heightPx: Int = 56,
    yOffsetPx: Int = 32
): LaunchOrigin {
    val point = projection.toScreenLocation(latLng)

    val loc = IntArray(2)
    anchorView.getLocationOnScreen(loc)
    val anchorX = loc[0]
    val anchorY = loc[1]

    val localX = point.x - anchorX
    val localY = point.y - anchorY

    Log.e("!!!!!!KORD!!!!!", "latLngToLaunchOrigin point.x: ${point.x} |  point.y:${point.y} | anchorX: ${anchorX} | height: ${anchorY}" )

    return LaunchOrigin(
//        x = localX - widthPx / 2,
//        y = localY - heightPx / 2 - yOffsetPx,
        x = point.x + (point.x / 9),
        y = point.y + (point.y / 5),
        width = 1,
        height = 1
    )
}