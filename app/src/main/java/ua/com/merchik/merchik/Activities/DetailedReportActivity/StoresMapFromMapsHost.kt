package ua.com.merchik.merchik.Activities.DetailedReportActivity



// ComposeHosts.kt


import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.compose.rememberCameraPositionState
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.RealmModels.LogMPDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.MainViewModelImpl
import ua.com.merchik.merchik.database.realm.tables.LogMPRealm
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.maps.domain.StoreCenter
import ua.com.merchik.merchik.features.maps.domain.StorePoint
import ua.com.merchik.merchik.features.maps.domain.isValidLatLon
import ua.com.merchik.merchik.features.maps.domain.parseDoubleSafe
import ua.com.merchik.merchik.features.maps.presentation.MainMapActionsBridge
import ua.com.merchik.merchik.features.maps.presentation.MapIntent
import ua.com.merchik.merchik.features.maps.presentation.main.MapsDialog
import ua.com.merchik.merchik.features.maps.presentation.main.StoresMap
import ua.com.merchik.merchik.features.maps.presentation.viewModels.BaseMapViewModel
import ua.com.merchik.merchik.features.maps.presentation.viewModels.MapFromMapsViewModel
import androidx.compose.runtime.collectAsState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val LOG_MP_VALID_TIME_SEC = 1_800L
private const val MAX_LOG_MP_MAP_POINTS = 2_000

@Composable
private fun StoresMapFromMapsHost(wpData: WpDataDB) {
    val mainVm: MainViewModel = hiltViewModel<MainViewModelImpl>()
    mainVm.dataJson = Gson().toJson(wpData)
    val mapVm: MapFromMapsViewModel = hiltViewModel()

    val camera = rememberCameraPositionState()

    val highlightColor = colorResource(id = R.color.selected_item)
    val contextUI = mainVm.contextUI

    var showMapsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(mapVm, mainVm, highlightColor, contextUI) {
        mapVm.attachBridge(
            MainMapActionsBridge(
                mainVm = mainVm,
                onDismiss = { },
                contextUI = contextUI,
                highlightColor = highlightColor
            )
        )
    }

    val distance by mainVm.offsetDistanceMeters.collectAsState()

    LaunchedEffect(
        wpData,
        Globals.CoordX,
        Globals.CoordY,
        mapVm,
        distance
    ) {
        try {
            val input = loadLogMpMapInput(wpData)
            mapVm.process(
                MapIntent.SetPointsInput(
                    center = input.center,
                    points = input.points,
                    userLat = Globals.CoordX,
                    userLon = Globals.CoordY,
                    distanceMeters = distance,
                    autoCenterOnSetInput = false
                )
            )
        } catch (e: Throwable) {
            Log.e("StoresMapFromMapsHost", "loadLogMpMapInput failed", e)
            mapVm.process(
                MapIntent.SetPointsInput(
                    center = buildStoreCenter(wpData),
                    points = emptyList(),
                    userLat = Globals.CoordX,
                    userLon = Globals.CoordY,
                    distanceMeters = distance,
                    autoCenterOnSetInput = false
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        StoresMap(
            cameraPositionState = camera,
            vm = mapVm as BaseMapViewModel
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            showMapsDialog = true
                        },
                        onPress = {
                            showMapsDialog = true
                        }
                    )
                }
        )
    }

//    if (showMapsDialog) {
//        MapsDialog(
//            mainViewModel = mainVm,
//            onDismiss = { showMapsDialog = false },
//            onOpenContextMenu = { wp, ctxUI, _ ->
//                mainVm.openContextMenu(wp, ctxUI)
//            }
//        )
//    }
}

private data class LogMpMapInput(
    val center: StoreCenter?,
    val points: List<StorePoint>
)

private data class LogMpMapRequest(
    val startMillis: Long,
    val endMillis: Long,
    val center: StoreCenter?
)

private suspend fun loadLogMpMapInput(wpData: WpDataDB): LogMpMapInput {
    val request = buildLogMpMapRequest(wpData)

    return withContext(Dispatchers.IO) {
        val realm = Realm.getDefaultInstance()
        try {
            val results = realm.where(LogMPDB::class.java)
                .greaterThanOrEqualTo("CoordTime", request.startMillis)
                .lessThanOrEqualTo("CoordTime", request.endMillis)
                .notEqualTo("CoordX", 0.0)
                .notEqualTo("CoordY", 0.0)
                .sort("CoordTime", Sort.DESCENDING)
                .findAll()

            val size = results.size
            val step = if (size <= MAX_LOG_MP_MAP_POINTS) {
                1
            } else {
                (size + MAX_LOG_MP_MAP_POINTS - 1) / MAX_LOG_MP_MAP_POINTS
            }

            val points = ArrayList<StorePoint>(minOf(size, MAX_LOG_MP_MAP_POINTS))
            results.forEachIndexed { index, log ->
                if (index % step != 0 && index != size - 1) return@forEachIndexed
                if (!isValidLatLon(log.CoordX, log.CoordY)) return@forEachIndexed

                points.add(
                    StorePoint(
                        id = log.id.toString(),
                        lat = log.CoordX,
                        lon = log.CoordY,
                        title = buildLogPointTitle(log),
                        wp = null,
                        dataItemsUI = null,
                        coordTimeMillis = log.CoordTime
                    )
                )
            }

            LogMpMapInput(
                center = request.center,
                points = points
            )
        } finally {
            realm.close()
        }
    }
}

private fun buildLogMpMapRequest(wpData: WpDataDB): LogMpMapRequest {
    val nowSec = System.currentTimeMillis() / 1_000L
    val startSec = if (wpData.visit_start_dt > 0 && wpData.visit_end_dt > 0) {
        wpData.visit_start_dt - LOG_MP_VALID_TIME_SEC
    } else {
        nowSec - LOG_MP_VALID_TIME_SEC
    }
    val endSec = if (wpData.visit_end_dt > 0) wpData.visit_end_dt else nowSec

    return LogMpMapRequest(
        startMillis = LogMPRealm.normalizeToMillis(startSec),
        endMillis = LogMPRealm.normalizeToMillis(endSec),
        center = buildStoreCenter(wpData)
    )
}

private fun buildStoreCenter(wpData: WpDataDB): StoreCenter? {
    val lat = wpData.addr_location_xd?.parseDoubleSafe()
    val lon = wpData.addr_location_yd?.parseDoubleSafe()
    if (!isValidLatLon(lat, lon)) return null

    return StoreCenter(
        pos = LatLng(lat!!, lon!!),
        title = wpData.addr_txt?.takeIf { it.isNotBlank() } ?: "Store",
        addrId = wpData.addr_id
    )
}

private fun buildLogPointTitle(log: LogMPDB): String {
    val time = SimpleDateFormat("dd.MM HH:mm:ss", Locale.getDefault()).format(Date(log.CoordTime))
    val provider = when (log.provider) {
        1 -> "GPS"
        2 -> "GSM"
        else -> log.provider?.toString().orEmpty()
    }

    return buildString {
        append("CoordTime: ").append(time)
        append("\nCoordX: ").append(String.format(Locale.US, "%.6f", log.CoordX))
        append("\nCoordY: ").append(String.format(Locale.US, "%.6f", log.CoordY))
        append("\nAccuracy: ").append(log.CoordAccuracy)
        append("\nDistance: ").append(formatDistance(log.distance))
        if (provider.isNotBlank()) append("\nProvider: ").append(provider)
    }
}

private fun formatDistance(distance: Int): String =
    if (distance >= 1_000) {
        String.format(Locale.US, "%.2f km", distance / 1_000.0)
    } else {
        "$distance m"
    }

/** Расширение для удобного вызова из Java. */
@JvmOverloads
fun attachStoresMapFromMaps(
    composeView: ComposeView,
    wpData: WpDataDB
) {

    composeView.setViewCompositionStrategy(
        ViewCompositionStrategy.DisposeOnDetachedFromWindow
    )
    composeView.setContent {
        StoresMapFromMapsHost(wpData)
    }
}
