package ua.com.merchik.merchik.features.maps.presentation.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
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
import ua.com.merchik.merchik.features.maps.domain.formatSum
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


// provide icons/badges in UI as before
    val context = LocalContext.current


    // 1) Инициализируем SDK (безопасно вызывать многократно)
    LaunchedEffect(context) {
        try {
            com.google.android.gms.maps.MapsInitializer.initialize(
                context.applicationContext,
                com.google.android.gms.maps.MapsInitializer.Renderer.LATEST
            ) { /* no-op */ }
        } catch (_: Throwable) {
        }
    }

    val greenDefault = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
    val redDefault = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)

    // ---- НОВОЕ: маленькие круглые «точки» для MapFromMaps ----
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


    com.google.maps.android.compose.GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // user marker
        if (isValidLatLon(userLat, userLon)) {
            com.google.maps.android.compose.MarkerInfoWindow(
                state = com.google.maps.android.compose.MarkerState(position = LatLng(userLat!!, userLon!!)),
                icon = getBadgeYou(1),
            ) {
                InfoBalloonText(title = "Ваше местоположение", tailAlignment = 0.5f, tailOnBottom = true)
            }
        }

        // ===== СЦЕНАРИЙ MapFromMaps: рисуем центр + маленькие кружки по всем точкам =====
        if (s.center != null) {
            val c = s.center!!

            // Радиус + пин центра (как было)
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
                InfoBalloonText(title = c.title ?: "Магазин", tailAlignment = 0.5f, tailOnBottom = true)
            }

            Log.e("!!!MAP!!!", "s point size: ${s.pointsUi.size}")

            // ---- маленькие круглые маркеры для КАЖДОЙ точки ----
            s.pointsUi.forEachIndexed { index, pUi ->
                val iconDesc = if (pUi.insideRadius) greenDot else redDot

                // уникальный ключ, чтобы compose не переиспользовал один и тот же узел
                val markerKey = pUi.point.id ?: "${pUi.point.lat}_${pUi.point.lon}_$index"

                Log.e("!!!MAP!!!", "pt[$index] id=${pUi.point.id} lat=${pUi.point.lat} lon=${pUi.point.lon}")

                androidx.compose.runtime.key(markerKey) {
                    // NB: используем MarkerInfoWindow, как в твоей «старой» версии
                    com.google.maps.android.compose.MarkerInfoWindow(
                        state = com.google.maps.android.compose.MarkerState(
                            position = LatLng(pUi.point.lat, pUi.point.lon)
                        ),
                        // маленькая точка — инфо-окно не рисуем, но клик сработает
                        onInfoWindowClick = {
                            vm.process(MapIntent.MarkerClicked(pUi))
                        },
                        icon = iconDesc,
                        // якорим по центру круга, чтобы точка не «смещалась»
                        anchor = Offset(0.5f, 0.5f),   // <-- правильный anchor для compose
                        // чуть выше zIndex для «внутренних» точек, чтобы не прятались под внешними
                        zIndex = if (pUi.insideRadius) 1f else 0f
                    ) {
                        InfoBalloonText(title = pUi.point.title ?: "Магазин")
                        // Можно ничего не рисовать в InfoWindow содержимом (оставим пусто)
                    }
                }
            }
        } else {
            // FromWPdata — твой текущий код с бейджами (без изменений)
            s.pointsUi.forEach { pUi ->
                val iconDesc = when {
                    pUi.insideRadius && pUi.count > 0 -> getBadgePin(pUi.count)
                    pUi.insideRadius -> greenDefault
                    !pUi.insideRadius && pUi.count > 0 -> redDefault
                    else -> redDefault
                }
                val subtitle = if (pUi.count > 0) "${pUi.count} КПС ${formatSum(pUi.sum)}" else ""

                com.google.maps.android.compose.MarkerInfoWindow(
                    state = com.google.maps.android.compose.MarkerState(
                        position = LatLng(pUi.point.lat, pUi.point.lon)
                    ),
                    onInfoWindowClick = { vm.process(MapIntent.MarkerClicked(pUi)) },
                    icon = iconDesc
                ) {
                    InfoBalloonText(
                        title = pUi.point.title ?: "Позиция",
                        subtitle = subtitle,
                        tailAlignment = 0.5f,
                        tailOnBottom = true
                    )
                }
            }
        }
    }

    LaunchedEffect(s.center, s.pointsUi, userLat, userLon) {
        kotlinx.coroutines.delay(50) // ждём, пока карта примонтируется
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
                    CameraUpdateFactory.newLatLngBounds(builder.build(), /*padding*/ 80)
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
}


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

private fun Context.renderVector(@DrawableRes resId: Int, heightPx: Int, @ColorInt tint: Int? = null): Bitmap {
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
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textAlign = Paint.Align.CENTER
        textSize = sp(fontSizeSp)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val cx = size / 2f
    val cy = size / 2f
    val radius = cx - border.strokeWidth / 2f

    canvas.drawCircle(cx, cy, radius, fill)
    canvas.drawCircle(cx, cy, radius, border)

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
