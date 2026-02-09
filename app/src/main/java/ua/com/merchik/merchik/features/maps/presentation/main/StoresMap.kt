package ua.com.merchik.merchik.features.maps.presentation.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.features.main.componentsUI.InfoBalloonText
import ua.com.merchik.merchik.features.maps.domain.PointUi
import ua.com.merchik.merchik.features.maps.domain.formatSum
import ua.com.merchik.merchik.features.maps.domain.haversine
import ua.com.merchik.merchik.features.maps.domain.isValidLatLon
import ua.com.merchik.merchik.features.maps.presentation.MapIntent
import ua.com.merchik.merchik.features.maps.presentation.viewModels.BaseMapViewModel

@Composable
fun StoresMap(
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
    vm: BaseMapViewModel
) {
    val s by vm.state.collectAsState()

    val userLat = s.userLat
    val userLon = s.userLon
    val circleR = s.circleRadiusMeters

    val context = LocalContext.current

    // 1) Инициализация Maps SDK (безопасно вызывать многократно)
    LaunchedEffect(context) {
        try {
            com.google.android.gms.maps.MapsInitializer.initialize(
                context.applicationContext,
                com.google.android.gms.maps.MapsInitializer.Renderer.LATEST
            ) { /* no-op */ }
        } catch (_: Throwable) {
        }
    }

    // user LatLng
    val userLatLng = remember(userLat, userLon) {
        if (isValidLatLon(userLat, userLon)) LatLng(userLat!!, userLon!!) else null
    }

    // Icons/badges
    val greenDefault = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
    val redDefault = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)

    val greenDot = rememberDotIcon(
        dotDp = 12f,
        colorInt = ContextCompat.getColor(context, R.color.maps_green)
    )
    val redDot = rememberDotIcon(
        dotDp = 12f,
        colorInt = ContextCompat.getColor(context, R.color.red_error)
    )

    val getBadgePin = rememberBadgePinCache(
        pinRes = R.drawable.ic_3,
        pinHeightDp = 44f,
        badgeDiameter = 22.dp,
        badgeYOffsetK = 0.40f,
        badgeBg = android.graphics.Color.LTGRAY,
        badgeBorder = android.graphics.Color.DKGRAY,
        badgeText = android.graphics.Color.DKGRAY,
        pinTint = ContextCompat.getColor(context, R.color.maps_green)
    )

    val getBadgeYou = rememberBadgePinCache(
        pinRes = R.drawable.ic_3,
        pinHeightDp = 44f,
        badgeDiameter = 22.dp,
        badgeYOffsetK = 0.40f,
        badgeIconRes = R.drawable.ic_60,
        badgeBg = android.graphics.Color.LTGRAY,
        badgeBorder = android.graphics.Color.DKGRAY,
        pinTint = ContextCompat.getColor(context, R.color.maps_dark_blue)
    )

    val storeIcon = rememberBadgePinCache(
        pinRes = R.drawable.ic_3,
        pinHeightDp = 44f,
        badgeDiameter = 22.dp,
        badgeYOffsetK = 0.40f,
        badgeIconRes = R.drawable.ic_store,
        badgeBg = android.graphics.Color.LTGRAY,
        badgeBorder = android.graphics.Color.DKGRAY,
        pinTint = ContextCompat.getColor(context, R.color.selected_item)
    )

    // Route polyline (MapFromMaps)
    val pathPoints = remember(s.pointsUi) {
        s.pointsUi
            .asSequence()
            .filter { it.point.coordTimeMillis != null }
            .sortedBy { it.point.coordTimeMillis }
            .map { LatLng(it.point.lat, it.point.lon) }
            .toList()
    }

    // Fit camera bounds
    LaunchedEffect(s.center, s.pointsUi, userLat, userLon) {
        kotlinx.coroutines.delay(50)
        val latLngs = buildList {
            s.center?.let { add(it.pos) }
            s.pointsUi.forEach { add(LatLng(it.point.lat, it.point.lon)) }
            if (isValidLatLon(userLat, userLon)) add(LatLng(userLat!!, userLon!!))
        }
        if (latLngs.isEmpty()) return@LaunchedEffect
        try {
            if (latLngs.size >= 2) {
                val builder = LatLngBounds.builder()
                latLngs.forEach { builder.include(it) }
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(builder.build(), 80)
                )
            } else {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(latLngs.first(), 14f)
                )
            }
        } catch (_: Throwable) {
            latLngs.firstOrNull()?.let {
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 12f))
            }
        }
    }

    // ===== helpers for Scenario 2 (stable) =====
    data class SelectedMarker(
        val key: String,
        val pos: LatLng,
        val subtitle: String
    )

    fun stableKey(pUi: PointUi): String {
        pUi.point.id?.takeIf { it.isNotBlank() }?.let { return it }
        pUi.point.wp?.id?.let { return "wp_$it" }

        fun r6(x: Double) = kotlin.math.round(x * 1_000_000.0) / 1_000_000.0
        return "ll_${r6(pUi.point.lat)}_${r6(pUi.point.lon)}"
    }

    fun formatDistanceKmM(meters: Double): String {
        return if (meters >= 1000.0) String.format("%.2f км", meters / 1000.0)
        else String.format("%.0f м", meters)
    }

    fun buildSubtitle(pUi: PointUi, distance: String?): String = buildString {
        if (pUi.count > 0) {
            append("${pUi.count} кпс")
            append("\nПремія: ${formatSum(pUi.sum)}")
        }
        if (!distance.isNullOrBlank()) {
            if (isNotEmpty()) append('\n')
            append("Відстань: ")
            append(distance)
        }
    }

    // Scenario 2 selection: держим сразу ГОТОВЫЙ subtitle
    var selected by remember { mutableStateOf<SelectedMarker?>(null) }

    // MarkerState cache (важно для стабильности)
    val markerStates = remember {
        mutableMapOf<String, com.google.maps.android.compose.MarkerState>()
    }

    fun stateFor(key: String, pos: LatLng): com.google.maps.android.compose.MarkerState {
        val st = markerStates.getOrPut(key) { com.google.maps.android.compose.MarkerState(pos) }
        st.position = pos
        return st
    }

    // Чтобы принудительно обновить окно выбранного маркера (bitmap snapshot)
    // делаем один "жёсткий" refresh после установки selected.
    LaunchedEffect(selected?.key, selected?.subtitle) {
        val sel = selected ?: return@LaunchedEffect
        val st = markerStates[sel.key] ?: return@LaunchedEffect
        withFrameNanos { } // дать Compose пересобрать контент окна
        st.hideInfoWindow()
        st.showInfoWindow()
    }

    Box(Modifier.fillMaxSize()) {
        com.google.maps.android.compose.GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // === YOU marker ===
            userLatLng?.let { youPos ->
                com.google.maps.android.compose.MarkerInfoWindow(
                    state = com.google.maps.android.compose.MarkerState(position = youPos),
                    icon = getBadgeYou(1)
                ) {
                    InfoBalloonText(
                        title = "Ваше местоположение",
                        tailAlignment = 0.5f,
                        tailOnBottom = true
                    )
                }
            }

            // === Scenario 1: MapFromMaps ===
            if (s.center != null) {
                val c = s.center!!

                com.google.maps.android.compose.Circle(
                    center = c.pos,
                    radius = Globals.distanceMin.toDouble(),
                    strokeColor = Color.Gray,
                    fillColor = Color(0x3300FF00),
                    strokeWidth = 2f
                )

                com.google.maps.android.compose.MarkerInfoWindow(
                    state = com.google.maps.android.compose.MarkerState(position = c.pos),
                    icon = storeIcon(1)
                ) {
                    InfoBalloonText(
                        title = c.title ?: "Магазин",
                        tailAlignment = 0.5f,
                        tailOnBottom = true
                    )
                }

                s.pointsUi.forEachIndexed { index, pUi ->
                    val iconDesc = if (pUi.insideRadius) greenDot else redDot
                    val markerKey = pUi.point.id ?: "${pUi.point.lat}_${pUi.point.lon}_$index"
                    androidx.compose.runtime.key(markerKey) {
                        com.google.maps.android.compose.MarkerInfoWindow(
                            state = com.google.maps.android.compose.MarkerState(
                                position = LatLng(pUi.point.lat, pUi.point.lon)
                            ),
                            onInfoWindowClick = { vm.process(MapIntent.MarkerClicked(pUi)) },
                            icon = iconDesc,
                            anchor = Offset(0.5f, 0.5f),
                            zIndex = if (pUi.insideRadius) 1f else 0f
                        ) {
                            InfoBalloonText(title = pUi.point.title ?: "Магазин")
                        }
                    }
                }

                if (pathPoints.size >= 2) {
                    com.google.maps.android.compose.Polyline(
                        points = pathPoints,
                        color = Color(0xFF1976D2),
                        width = 6f,
                        geodesic = true
                    )
                }
            } else {
                // === Scenario 2: FromWPdata (distance + line while InfoWindow is open) ===
                val uLat = userLat
                val uLon = userLon
                val isUserRNO = s.pointsUi.any { it.point.wp?.user_id == 14041 }

                if (isUserRNO && isValidLatLon(uLat, uLon) && circleR != null) {
                    com.google.maps.android.compose.Circle(
                        center = LatLng(uLat!!, uLon!!),
                        radius = circleR,
                        strokeColor = Color.Gray,
                        fillColor = Color(0x221E88E5),
                        strokeWidth = 2f
                    )
                }

                // Markers
                var i = 0
                s.pointsUi.forEach { pUi ->
                    val pos = LatLng(pUi.point.lat, pUi.point.lon)
                    val baseKey = stableKey(pUi)
                    val key = "${baseKey}_$i"

                    val iconDesc = when {
                        pUi.insideRadius && pUi.count > 0 -> getBadgePin(pUi.count)
                        pUi.insideRadius -> greenDefault
                        !pUi.insideRadius && pUi.count > 0 -> redDefault
                        else -> redDefault
                    }

                    val alpha = if (isUserRNO && isValidLatLon(uLat, uLon) && circleR != null) {
                        val d = haversine(uLat!!, uLon!!, pUi.point.lat, pUi.point.lon)
                        if (d <= circleR) 1f else 0.4f
                    } else 1f

                    val markerState = stateFor(key, pos)
                    val isSelected = selected?.key == key

                    // IMPORTANT: subtitle для выбранного маркера берём из selected (уже посчитан в onClick)
                    val subtitle = remember(
                        key,
                        pUi.count,
                        pUi.sum,
                        isSelected,
                        selected?.subtitle
                    ) {
                        if (isSelected) selected?.subtitle.orEmpty()
                        else buildSubtitle(pUi, distance = null)
                    }

                    com.google.maps.android.compose.MarkerInfoWindow(
                        state = markerState,
                        icon = iconDesc,
                        alpha = alpha,
                        onClick = {
                            val dist = if (userLatLng != null) {
                                val meters = haversine(
                                    userLatLng.latitude, userLatLng.longitude,
                                    pos.latitude, pos.longitude
                                )
                                formatDistanceKmM(meters)
                            } else null

                            selected = SelectedMarker(
                                key = key,
                                pos = pos,
                                subtitle = buildSubtitle(pUi, dist)
                            )

                            // показываем сразу (а LaunchedEffect выше гарантированно переснимет bitmap)
//                            markerState.showInfoWindow()
                            false
                        },
                        onInfoWindowClick = {
                            vm.process(MapIntent.MarkerClicked(pUi))
                            if (selected?.key == key) selected = null
                        },
                        onInfoWindowClose = {
//                            if (selected?.key == key) selected = null
                        }
                    ) {
                        InfoBalloonText(
                            title = pUi.point.title ?: "Позиция",
                            subtitle = selected?.subtitle ?: subtitle,
                            tailAlignment = 0.5f,
                            tailOnBottom = true
                        )
                    }
                    i++
                }

                // Line + distance label (пока selected != null)
                val you = userLatLng
                val target = selected?.pos
                if (you != null && target != null) {
                    com.google.maps.android.compose.Polyline(
                        points = listOf(you, target),
                        width = 6f,
                        geodesic = true,
                        color = Color(0xFF00ACC1)
                    )

                    // текст на середине линии — используем уже готовую distance строку из selected.subtitle
                    // (если хочешь отдельно — можно достать dist из selected.subtitle, но проще посчитать тут быстро)
                    val meters = haversine(you.latitude, you.longitude, target.latitude, target.longitude)
                    val dist = formatDistanceKmM(meters)

                    val mid = LatLng(
                        (you.latitude + target.latitude) / 2.0,
                        (you.longitude + target.longitude) / 2.0
                    )

                    val textIcon = rememberDistanceTextIcon(dist)

                    com.google.maps.android.compose.Marker(
                        state = com.google.maps.android.compose.MarkerState(position = mid),
                        icon = textIcon,
                        anchor = Offset(0.5f, 0.5f),
                        zIndex = 10f
                    )
                }
            }
        }
    }
}


//@Composable
//fun StoresMap(
//    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
//    vm: BaseMapViewModel
//) {
//    val s by vm.state.collectAsState()
//
//    val userLat = s.userLat
//    val userLon = s.userLon
//    val circleR = s.circleRadiusMeters
//
//    // === Выбор точки для дистанции/линии (живёт пока открыт InfoWindow) ===
//    var selectedKey by remember { mutableStateOf<String?>(null) }
//    var selectedPos by remember { mutableStateOf<LatLng?>(null) }
//    var showDistance by remember { mutableStateOf(false) }
//
//    val context = LocalContext.current
//
//    // 1) Инициализация Maps SDK (безопасно вызывать многократно)
//    LaunchedEffect(context) {
//        try {
//            com.google.android.gms.maps.MapsInitializer.initialize(
//                context.applicationContext,
//                com.google.android.gms.maps.MapsInitializer.Renderer.LATEST
//            ) { /* no-op */ }
//        } catch (_: Throwable) {
//        }
//    }
//
//    // user LatLng
//    val userLatLng = remember(userLat, userLon) {
//        if (isValidLatLon(userLat, userLon)) LatLng(userLat!!, userLon!!) else null
//    }
//
//    // distance text (derived)
//    val distanceText: String? by remember(userLatLng, selectedPos, showDistance) {
//        derivedStateOf {
//            val you = userLatLng ?: return@derivedStateOf null
//            val target = if (showDistance) selectedPos else null ?: return@derivedStateOf null
//
//            val meters = haversine(you.latitude, you.longitude, target!!.latitude, target.longitude)
//            if (meters >= 1000.0) String.format("%.2f км", meters / 1000.0)
//            else String.format("%.0f м", meters)
//        }
//    }
//
//    // Icons/badges
//    val greenDefault = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
//    val redDefault = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
//
//    val greenDot = rememberDotIcon(
//        dotDp = 12f,
//        colorInt = ContextCompat.getColor(context, R.color.maps_green)
//    )
//    val redDot = rememberDotIcon(
//        dotDp = 12f,
//        colorInt = ContextCompat.getColor(context, R.color.red_error)
//    )
//
//    val getBadgePin = rememberBadgePinCache(
//        pinRes = R.drawable.ic_3,
//        pinHeightDp = 44f,
//        badgeDiameter = 22.dp,
//        badgeYOffsetK = 0.40f,
//        badgeBg = android.graphics.Color.LTGRAY,
//        badgeBorder = android.graphics.Color.DKGRAY,
//        badgeText = android.graphics.Color.DKGRAY,
//        pinTint = ContextCompat.getColor(context, R.color.maps_green)
//    )
//
//    val getBadgeYou = rememberBadgePinCache(
//        pinRes = R.drawable.ic_3,
//        pinHeightDp = 44f,
//        badgeDiameter = 22.dp,
//        badgeYOffsetK = 0.40f,
//        badgeIconRes = R.drawable.ic_60,
//        badgeBg = android.graphics.Color.LTGRAY,
//        badgeBorder = android.graphics.Color.DKGRAY,
//        pinTint = ContextCompat.getColor(context, R.color.maps_dark_blue)
//    )
//
//    val storeIcon = rememberBadgePinCache(
//        pinRes = R.drawable.ic_3,
//        pinHeightDp = 44f,
//        badgeDiameter = 22.dp,
//        badgeYOffsetK = 0.40f,
//        badgeIconRes = R.drawable.ic_store,
//        badgeBg = android.graphics.Color.LTGRAY,
//        badgeBorder = android.graphics.Color.DKGRAY,
//        pinTint = ContextCompat.getColor(context, R.color.selected_item)
//    )
//
//    // Route polyline (MapFromMaps)
//    val pathPoints = remember(s.pointsUi) {
//        s.pointsUi
//            .asSequence()
//            .filter { it.point.coordTimeMillis != null }
//            .sortedBy { it.point.coordTimeMillis }
//            .map { LatLng(it.point.lat, it.point.lon) }
//            .toList()
//    }
//
//    // Fit camera bounds
//    LaunchedEffect(s.center, s.pointsUi, userLat, userLon) {
//        kotlinx.coroutines.delay(50)
//        val latLngs = buildList {
//            s.center?.let { add(it.pos) }
//            s.pointsUi.forEach { add(LatLng(it.point.lat, it.point.lon)) }
//            if (isValidLatLon(userLat, userLon)) add(LatLng(userLat!!, userLon!!))
//        }
//        if (latLngs.isEmpty()) return@LaunchedEffect
//        try {
//            if (latLngs.size >= 2) {
//                val builder = LatLngBounds.builder()
//                latLngs.forEach { builder.include(it) }
//                cameraPositionState.animate(
//                    CameraUpdateFactory.newLatLngBounds(builder.build(), 80)
//                )
//            } else {
//                cameraPositionState.animate(
//                    CameraUpdateFactory.newLatLngZoom(latLngs.first(), 14f)
//                )
//            }
//        } catch (_: Throwable) {
//            latLngs.firstOrNull()?.let {
//                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 12f))
//            }
//        }
//    }
//
//    Box(Modifier.fillMaxSize()) {
//        com.google.maps.android.compose.GoogleMap(
//            modifier = Modifier.fillMaxSize(),
//            cameraPositionState = cameraPositionState
//        ) {
//            // === YOU marker ===
//            userLatLng?.let { youPos ->
//                com.google.maps.android.compose.MarkerInfoWindow(
//                    state = com.google.maps.android.compose.MarkerState(position = youPos),
//                    icon = getBadgeYou(1)
//                ) {
//                    InfoBalloonText(
//                        title = "Ваше местоположение",
//                        tailAlignment = 0.5f,
//                        tailOnBottom = true
//                    )
//                }
//            }
//
//            // === Scenario 1: MapFromMaps ===
//            if (s.center != null) {
//                val c = s.center!!
//
//                com.google.maps.android.compose.Circle(
//                    center = c.pos,
//                    radius = Globals.distanceMin.toDouble(),
//                    strokeColor = Color.Gray,
//                    fillColor = Color(0x3300FF00),
//                    strokeWidth = 2f
//                )
//
//                com.google.maps.android.compose.MarkerInfoWindow(
//                    state = com.google.maps.android.compose.MarkerState(position = c.pos),
//                    icon = storeIcon(1)
//                ) {
//                    InfoBalloonText(
//                        title = c.title ?: "Магазин",
//                        tailAlignment = 0.5f,
//                        tailOnBottom = true
//                    )
//                }
//
//                // Small dots (no distance logic here, as per your requirements)
//                s.pointsUi.forEachIndexed { index, pUi ->
//                    val iconDesc = if (pUi.insideRadius) greenDot else redDot
//                    val markerKey = pUi.point.id ?: "${pUi.point.lat}_${pUi.point.lon}_$index"
//                    androidx.compose.runtime.key(markerKey) {
//                        com.google.maps.android.compose.MarkerInfoWindow(
//                            state = com.google.maps.android.compose.MarkerState(
//                                position = LatLng(pUi.point.lat, pUi.point.lon)
//                            ),
//                            onInfoWindowClick = { vm.process(MapIntent.MarkerClicked(pUi)) },
//                            icon = iconDesc,
//                            anchor = Offset(0.5f, 0.5f),
//                            zIndex = if (pUi.insideRadius) 1f else 0f
//                        ) {
//                            InfoBalloonText(title = pUi.point.title ?: "Магазин")
//                        }
//                    }
//                }
//
//                if (pathPoints.size >= 2) {
//                    com.google.maps.android.compose.Polyline(
//                        points = pathPoints,
//                        color = Color(0xFF1976D2),
//                        width = 6f,
//                        geodesic = true
//                    )
//                }
//
//            } else {
//                // === Scenario 2: FromWPdata (distance + line while InfoWindow is open) ===
//                val uLat = userLat
//                val uLon = userLon
//
//                val isUserRNO = s.pointsUi.any { it.point.wp?.user_id == 14041 }
//
//                if (isUserRNO && isValidLatLon(uLat, uLon) && circleR != null) {
//                    com.google.maps.android.compose.Circle(
//                        center = LatLng(uLat!!, uLon!!),
//                        radius = circleR,
//                        strokeColor = Color.Gray,
//                        fillColor = Color(0x221E88E5),
//                        strokeWidth = 2f
//                    )
//                }
//
//                // Markers
//                // Markers
//                s.pointsUi.forEachIndexed { index, pUi ->
//                    val pos = LatLng(pUi.point.lat, pUi.point.lon)
////                    val key = pUi.point.id ?: "${pUi.point.lat}_${pUi.point.lon}_$index"
//
//                    val key = stableKey(pUi)
//
//
//                    val iconDesc = when {
//                        pUi.insideRadius && pUi.count > 0 -> getBadgePin(pUi.count)
//                        pUi.insideRadius -> greenDefault
//                        !pUi.insideRadius && pUi.count > 0 -> redDefault
//                        else -> redDefault
//                    }
//
//                    val alpha = if (isUserRNO && isValidLatLon(uLat, uLon) && circleR != null) {
//                        val d = haversine(uLat!!, uLon!!, pUi.point.lat, pUi.point.lon)
//                        if (d <= circleR) 1f else 0.4f
//                    } else 1f
//
//                    // ВАЖНО: свой MarkerState на каждый маркер
//                    val markerState =
//                        remember(key) { com.google.maps.android.compose.MarkerState(position = pos) }
//                    // если вдруг pos меняется — обновим
//                    markerState.position = pos
//
//                    val isSelected = showDistance && selectedKey == key
//
//                    val subtitle = remember(
//                        key,
//                        pUi.count,
//                        pUi.sum,
//                        isSelected,
//                        distanceText
//                    ) {
//                        buildString {
//                            if (pUi.count > 0) {
//                                append("${pUi.count} кпс")
//                                append("\nпремия: ${formatSum(pUi.sum)}")
//                            }
//                            if (isSelected && !distanceText.isNullOrBlank()) {
//                                if (isNotEmpty()) append('\n')
//                                append("расстояние: ")
//                                append(distanceText)
//                            }
//                        }
//                    }
//
//                    LaunchedEffect(isSelected, distanceText) {
//                        if (isSelected) {
//                            // ждём, чтобы Compose успел пересобрать content
//                            withFrameNanos { }
////                            markerState.hideInfoWindow()
//                            markerState.showInfoWindow()
//                        }
//                    }
//
//
//                    com.google.maps.android.compose.MarkerInfoWindow(
//                        state = markerState,
//                        onClick = {
//                            selectedKey = key
//                            selectedPos = pos
//                            showDistance = true
//
//                            // сами покажем окно после апдейта стейта
////                            markerState.showInfoWindow()
//                            true // <- важно: не даём карте открыть окно "раньше времени"
//                        },
//                        onInfoWindowClick = {
//                            vm.process(MapIntent.MarkerClicked(pUi))
//                            showDistance = false
//                            selectedKey = null
//                            selectedPos = null
//                        },
//                        onInfoWindowClose = {
//                            showDistance = false
//                            selectedKey = null
//                            selectedPos = null
//                        },
//                        icon = iconDesc,
//                        alpha = alpha
//                    ) {
//                        InfoBalloonText(
//                            title = pUi.point.title ?: "Позиция",
//                            subtitle = subtitle,
//                            tailAlignment = 0.5f,
//                            tailOnBottom = true
//                        )
//                    }
//                }
//
//                // Line + distance label over the line (only while InfoWindow is open)
//                val you = userLatLng
//                val target = selectedPos
//                if (showDistance && you != null && target != null && distanceText != null) {
//                    com.google.maps.android.compose.Polyline(
//                        points = listOf(you, target),
//                        width = 6f,
//                        geodesic = true,
//                        color = Color(0xFF00ACC1)
//                    )
//
//                    val mid = LatLng(
//                        (you.latitude + target.latitude) / 2.0,
//                        (you.longitude + target.longitude) / 2.0
//                    )
//
//                    val textIcon = rememberDistanceTextIcon(distanceText!!)
//
//                    com.google.maps.android.compose.Marker(
//                        state = com.google.maps.android.compose.MarkerState(position = mid),
//                        icon = textIcon,
//                        anchor = Offset(0.5f, 0.5f),
//                        zIndex = 10f
//                    )
//                }
//            }
//        }
//    }
//}


@Composable
fun rememberBadgePinCache(
    @DrawableRes pinRes: Int,
    pinHeightDp: Float = 44f,
    badgeDiameter: Dp = 22.dp,
    badgeYOffsetK: Float = 0.40f,
    badgeBg: Int = android.graphics.Color.RED,
    badgeBorder: Int = android.graphics.Color.WHITE,
    badgeText: Int = android.graphics.Color.WHITE,
    @DrawableRes badgeIconRes: Int? = null,
    pinTint: Int? = null
): (Int) -> BitmapDescriptor {
    val ctx = LocalContext.current
    val cache = remember { mutableMapOf<Int, BitmapDescriptor>() }

    return remember(
        pinRes,
        pinHeightDp,
        badgeDiameter,
        badgeYOffsetK,
        badgeBg,
        badgeBorder,
        badgeText,
        badgeIconRes,
        pinTint
    ) {
        { n: Int ->
            val key = n.coerceIn(1, 9)
            cache.getOrPut(key) {
                if (badgeIconRes != null) {
                    ctx.makePinWithBadgeIcon(
                        pinRes = pinRes,
                        badgeIconRes = badgeIconRes,
                        pinHeightDp = pinHeightDp,
                        badgeDiameter = badgeDiameter,
                        badgeYOffsetK = badgeYOffsetK,
                        badgeBg = badgeBg,
                        badgeBorder = badgeBorder,
                        pinTint = pinTint
                    )
                } else {
                    ctx.makePinWithBadge(
                        count = key,
                        pinRes = pinRes,
                        pinHeightDp = pinHeightDp,
                        badgeDiameter = badgeDiameter,
                        badgeYOffsetK = badgeYOffsetK,
                        badgeBg = badgeBg,
                        badgeBorder = badgeBorder,
                        badgeText = badgeText,
                        pinTint = pinTint
                    )
                }
            }
        }
    }
}


private fun Context.dp(dp: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

private fun Context.sp(sp: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

private fun Context.renderVector(
    @DrawableRes resId: Int,
    heightPx: Int,
    @ColorInt tint: Int? = null
): Bitmap {
    val d: Drawable = requireNotNull(ContextCompat.getDrawable(this, resId))
    val ratio = if (d.intrinsicHeight > 0) d.intrinsicWidth.toFloat() / d.intrinsicHeight else 1f
    val widthPx = (heightPx * ratio).coerceAtLeast(1f).toInt()
    val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    d.setBounds(0, 0, widthPx, heightPx)
    tint?.let { d.setTint(it) }
    d.draw(canvas)
    return bmp
}

private fun Context.drawCounterBadgeBitmap(
    count: Int,
    diameter: Dp = 22.dp,
    @ColorInt background: Int = android.graphics.Color.RED,
    @ColorInt borderColor: Int = android.graphics.Color.WHITE,
    @ColorInt textColor: Int = android.graphics.Color.WHITE,
    fontSizeSp: Float = 11f
): Bitmap {
    val size = dp(diameter.value)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)

    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = background
        style = Paint.Style.FILL
    }
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = borderColor
        style = Paint.Style.STROKE
        strokeWidth = dp(1f).toFloat()
    }
    val outerBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = dp(2f).toFloat()
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textAlign = Paint.Align.CENTER
        textSize = sp(fontSizeSp)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val cx = size / 2f
    val cy = size / 2f
    val radius = cx - outerBorder.strokeWidth / 2f

    canvas.drawCircle(cx, cy, radius, fill)
    canvas.drawCircle(cx, cy, radius, outerBorder)
    canvas.drawCircle(cx, cy, radius - outerBorder.strokeWidth / 2f, border) // внутренняя рамка

    val text = if (count > 9) "9+" else count.toString()
    val fm = textPaint.fontMetrics
    val baseline = cy - (fm.ascent + fm.descent) / 2f
    canvas.drawText(text, cx, baseline, textPaint)

    return bmp
}

private fun Context.makePinWithBadge(
    count: Int,
    @DrawableRes pinRes: Int,
    pinHeightDp: Float = 44f,
    badgeDiameter: Dp = 22.dp,
    badgeYOffsetK: Float = 0.40f,
    @ColorInt badgeBg: Int = android.graphics.Color.RED,
    @ColorInt badgeBorder: Int = android.graphics.Color.WHITE,
    @ColorInt badgeText: Int = android.graphics.Color.WHITE,
    @ColorInt pinTint: Int? = null
): BitmapDescriptor {
    val pinBitmap = renderVector(pinRes, dp(pinHeightDp), pinTint)
    val canvas = Canvas(pinBitmap)

    val safe = count.coerceAtLeast(1)
    val badge = drawCounterBadgeBitmap(
        count = safe,
        diameter = badgeDiameter,
        background = badgeBg,
        borderColor = badgeBorder,
        textColor = badgeText
    )

    val cx = pinBitmap.width / 2f
    val cy = pinBitmap.height * badgeYOffsetK
    val left = cx - badge.width / 2f
    val top = cy - badge.height / 2f

    canvas.drawBitmap(badge, left, top, null)
    return BitmapDescriptorFactory.fromBitmap(pinBitmap)
}

private fun Context.makePinWithBadgeIcon(
    @DrawableRes pinRes: Int,
    @DrawableRes badgeIconRes: Int,
    pinHeightDp: Float = 44f,
    badgeDiameter: Dp = 22.dp,
    badgeYOffsetK: Float = 0.40f,
    @ColorInt badgeBg: Int = android.graphics.Color.RED,
    @ColorInt badgeBorder: Int = android.graphics.Color.WHITE,
    @ColorInt pinTint: Int? = null
): BitmapDescriptor {
    val pinBitmap = renderVector(pinRes, dp(pinHeightDp), pinTint)
    val canvas = Canvas(pinBitmap)

    val size = dp(badgeDiameter.value)
    val badgeBmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val badgeCanvas = Canvas(badgeBmp)

    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = badgeBg
        style = Paint.Style.FILL
    }
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = badgeBorder
        style = Paint.Style.STROKE
        strokeWidth = dp(1f).toFloat()
    }

    val cx = size / 2f
    val cy = size / 2f
    val r = cx - border.strokeWidth / 2f

    badgeCanvas.drawCircle(cx, cy, r, fill)
    badgeCanvas.drawCircle(cx, cy, r, border)

    val d: Drawable = requireNotNull(ContextCompat.getDrawable(this, badgeIconRes))
    val inset = (size * 0.15f).toInt()
    d.setBounds(inset, inset, size - inset, size - inset)
    d.setTint(badgeBorder)
    d.draw(badgeCanvas)

    val pinCx = pinBitmap.width / 2f
    val pinCy = pinBitmap.height * badgeYOffsetK
    val left = pinCx - badgeBmp.width / 2f
    val top = pinCy - badgeBmp.height / 2f
    canvas.drawBitmap(badgeBmp, left, top, null)

    return BitmapDescriptorFactory.fromBitmap(pinBitmap)
}


@Composable
private fun rememberDotIcon(dotDp: Float = 12f, colorInt: Int): BitmapDescriptor {
    val ctx = LocalContext.current
    return remember(dotDp, colorInt) {
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dotDp, ctx.resources.displayMetrics
        ).toInt().coerceAtLeast(6)

        val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
            style = Paint.Style.FILL
        }
        val r = px / 2f
        canvas.drawCircle(r, r, r, paint)
        BitmapDescriptorFactory.fromBitmap(bmp)
    }
}


@Composable
fun rememberDistanceTextIcon(text: String): BitmapDescriptor {
    val context = LocalContext.current
    return remember(text) {
        val density = context.resources.displayMetrics.density

        val padH = (10 * density).toInt()
        val padV = (6 * density).toInt()
        val textSize = 14 * density

        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            this.textSize = textSize
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val textW = paint.measureText(text).toInt()
        val fm = paint.fontMetrics
        val textH = (fm.descent - fm.ascent).toInt()

        val w = textW + padH * 2
        val h = textH + padV * 2

        val bmp =
            android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        val c = android.graphics.Canvas(bmp)

        // фон
        val bg = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#AA000000") // полупрозрачный чёрный
        }
        val r = 16f * density
        c.drawRoundRect(
            android.graphics.RectF(0f, 0f, w.toFloat(), h.toFloat()),
            r, r, bg
        )

        // текст
        val x = padH.toFloat()
        val y = padV.toFloat() - fm.ascent
        c.drawText(text, x, y, paint)

        BitmapDescriptorFactory.fromBitmap(bmp)
    }
}


private fun stableKey(pUi: PointUi): String {
    val id = pUi.point.id
    if (!id.isNullOrBlank()) return id

    val wpId = pUi.point.wp?.id
    if (wpId != null) return "wp_$wpId"

    fun r6(x: Double) = kotlin.math.round(x * 1_000_000.0) / 1_000_000.0
    return "ll_${r6(pUi.point.lat)}_${r6(pUi.point.lon)}"
}

