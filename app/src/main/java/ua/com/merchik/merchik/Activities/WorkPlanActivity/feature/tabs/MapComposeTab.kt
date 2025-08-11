package ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.database.realm.RealmManager


@Composable
fun MapComposeTab() {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(Globals.CoordX, Globals.CoordY),
            14f
        )
    }

    val wpList = remember { RealmManager.getAllWorkPlanMAP() }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // Маркер пользователя
        Marker(
            state = MarkerState(position = LatLng(Globals.CoordX, Globals.CoordY)),
            title = "Ваше местоположение",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        )

        // Маркеры магазинов
        wpList.forEach { item ->
            runCatching {
                val lat = item.addr_location_xd.toDouble()
                val lon = item.addr_location_yd.toDouble()
                Marker(
                    state = MarkerState(position = LatLng(lat, lon)),
                    title = item.addr_txt,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            }
        }
    }
}
