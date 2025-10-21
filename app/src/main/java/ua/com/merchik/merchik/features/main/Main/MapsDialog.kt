package ua.com.merchik.merchik.features.main.Main

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Activities.MenuMainActivity.getFormattedMessage
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.Utils.toast.ClickableToast
import ua.com.merchik.merchik.Utils.toast.Toasty
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.toDayMonthFormat
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import ua.com.merchik.merchik.features.main.componentsUI.InfoBalloonText
import kotlin.math.pow
import kotlin.math.roundToInt


@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun MapsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    contextUI: ContextUI,
    onOpenContextMenu: (WpDataDB, ContextUI) -> Unit
) {
    // Безопасно читаем uiState
    val uiState by viewModel.uiState.collectAsState()

    // Читаем offset fonts безопасно (если это StateFlow — collectAsState)
    val offsetSizeFont by viewModel.offsetSizeFonts.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Результат фильтрации/сортировки — пересчитываем только при изменении входных параметров
    val result by remember(
        uiState.items,
        uiState.filters,
        uiState.sortingFields,
        viewModel.rangeDataStart.value,
        viewModel.rangeDataEnd.value,
        uiState.filters?.searchText
    ) {
        mutableStateOf(
            filterAndSortDataItems(
                items = uiState.items,
                filters = uiState.filters,
                sortingFields = uiState.sortingFields,
                rangeStart = viewModel.rangeDataStart.value,
                rangeEnd = viewModel.rangeDataEnd.value,
                searchText = uiState.filters?.searchText
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(vertical = 40.dp)
                .background(Color.Transparent)
        ) {
            Row(modifier = Modifier.align(Alignment.End)) {
                ImageButton(
                    id = R.drawable.ic_settings,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier
                        .padding(start = 15.dp, bottom = 10.dp),
                    onClick = { showSettingsDialog = true }
                )
                ImageButton(
                    id = R.drawable.ic_letter_x,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier
                        .padding(start = 15.dp, bottom = 10.dp),
                    onClick = onDismiss
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Карта",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "## Опис можливих дій. Додасться поступово")

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                    ) {
                        StoresMap(
                            viewModel = viewModel,
                            onDismiss = onDismiss,
                            items = result.items,
                            userLat = Globals.CoordX,
                            userLon = Globals.CoordY,
                            modifier = Modifier.fillMaxSize(),
                            fetchAddressById = { id -> RoomManager.SQL_DB.addressDao().getById(id) },
                            contextUI = contextUI,
                            onOpenContextMenu = onOpenContextMenu
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showSettingsDialog) {
        MessageDialog(
            title = "Не доступно",
            status = DialogStatus.ALERT,
            message = "Даний розділ знаходиться в стадії розробки",
            onDismiss = { showSettingsDialog = false },
            onConfirmAction = { showSettingsDialog = false }
        )
    }
}

// ---------------- StorePoint ----------------
data class StorePoint(
    val id: String?,
    val lat: Double,
    val lon: Double,
    val title: String?,
    val wp: WpDataDB? = null,
    val dataItemsUI: DataItemUI? = null
)

// ----------------- Utilities -----------------
private fun isValidLatLon(lat: Double?, lon: Double?): Boolean =
    lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0 && !(lat == 0.0 && lon == 0.0)

private fun List<FieldValue>.stringByKey(key: String): String? {
    val fv = firstOrNull { it.key == key } ?: return null
    return fv.value.value.takeIf { it.isNotBlank() }
        ?: fv.value.rawValue?.toString()?.takeIf { it.isNotBlank() }
}

typealias AddressFetcher = suspend (Int) -> AddressSDB?

fun String.parseDoubleSafe(): Double? {
    // Убираем NBSP, обрезаем, оставляем только цифры, точку/запятую и минус
    var s = this.replace('\u00A0', ' ')
        .trim()
        .replace(Regex("[^0-9,.-]"), "")
        .replace(" ", "")

    if (s.isEmpty()) return null

    // Если есть запятая, заменим её на точку
    s = s.replace(',', '.')

    // Если точек несколько — оставим последнюю как десятичный разделитель,
    // все предыдущие считаем разделителями тысяч и удалим
    val lastDot = s.lastIndexOf('.')
    if (lastDot > -1) {
        val before = s.substring(0, lastDot).replace(".", "")
        val after = s.substring(lastDot + 1)
        s = if (after.isEmpty()) before else "$before.$after"
    }

    return s.toDoubleOrNull()
}

private fun formatSum(sum: Double?): String =
    sum?.let { "${it.roundToInt()} грн" } ?: ""

private fun List<FieldValue>.addrIdInt(): Int? =
    stringByKey("addr_id")?.trim()?.toIntOrNull()

// ----------------- StoresMap -----------------
@Composable
fun StoresMap(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    items: List<DataItemUI>,
    userLat: Double,
    userLon: Double,
    modifier: Modifier = Modifier,
    fetchAddressById: AddressFetcher,
    contextUI: ContextUI,
    onOpenContextMenu: (WpDataDB, ContextUI) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val initialLat = if (isValidLatLon(userLat, userLon)) userLat else 0.0
    val initialLon = if (isValidLatLon(userLat, userLon)) userLon else 0.0
    val initialZoom = if (isValidLatLon(userLat, userLon)) 14f else 2f

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLon), initialZoom)
    }

    var showPerevopros by remember { mutableStateOf(false) }
    var selectedStableId by remember { mutableStateOf<Long?>(null) }
    var selectedWpDataDB by remember { mutableStateOf<WpDataDB?>(null) }

    val green = colorResource(id = R.color.ufmd_accept_t)

    val addressCache = remember { mutableStateMapOf<Int, AddressSDB?>() }

    // aggregation for badges (unchanged)
    val cashAggByAddrId by remember(items) {
        derivedStateOf {
            items.asSequence()
                .mapNotNull { item ->
                    val id = item.rawFields.addrIdInt() ?: return@mapNotNull null
                    val value = item.rawFields.stringByKey("cash_ispolnitel")?.parseDoubleSafe()
                    id to value
                }
                .filter { it.second != null }
                .groupBy({ it.first }, { it.second!! })
                .mapValues { entry ->
                    val list = entry.value
                    Pair(list.size, list.sum())
                }
        }
    }

    // address cache filling (unchanged)
    val missingIds by remember(items, addressCache.keys) {
        derivedStateOf {
            items.asSequence()
                .mapNotNull { it.rawFields.addrIdInt() }
                .distinct()
                .filter { id ->
                    if (addressCache.containsKey(id)) return@filter false
                    val first = items.firstOrNull { it.rawFields.addrIdInt() == id } ?: return@filter true
                    val f = first.rawFields
                    val lat = f.stringByKey("log_addr_location_xd")?.parseDoubleSafe()
                    val lon = f.stringByKey("log_addr_location_yd")?.parseDoubleSafe()
                    val title = f.stringByKey("log_addr_txt")
                    (lat == null || lon == null || !isValidLatLon(lat, lon) || title.isNullOrBlank())
                }
                .toList()
        }
    }

    LaunchedEffect(missingIds) {
        if (missingIds.isEmpty()) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            missingIds.forEach { id ->
                val addr = try {
                    fetchAddressById(id)
                } catch (e: Exception) {
                    null
                }
                withContext(Dispatchers.Main) {
                    addressCache[id] = addr
                }
            }
        }
    }

    // --- fallbackStores: old behaviour (used when NO log_* center) ---
    val fallbackStores by remember(items, addressCache) {
        derivedStateOf {
            items.mapNotNull { item ->
                val f = item.rawFields
                val idStr = f.stringByKey("addr_id")
                val idInt = idStr?.toIntOrNull()

                // old logic: prefer addr_location_* then fallback to CoordX/CoordY
                var lat = f.stringByKey("addr_location_xd")?.parseDoubleSafe()
                    ?: f.stringByKey("CoordX")?.parseDoubleSafe()
                var lon = f.stringByKey("addr_location_yd")?.parseDoubleSafe()
                    ?: f.stringByKey("CoordY")?.parseDoubleSafe()
                var title = f.stringByKey("addr_txt")?.takeIf { it.isNotBlank() }

                if ((!isValidLatLon(lat, lon) || title.isNullOrBlank()) && idInt != null) {
                    addressCache[idInt]?.let { addr ->
                        if (lat == null) lat = addr.locationXd?.toDouble()
                        if (lon == null) lon = addr.locationYd?.toDouble()
                        if (title.isNullOrBlank()) title = addr.nm
                    }
                }

                if (!isValidLatLon(lat, lon)) return@mapNotNull null

                val wp = item.rawObj.filterIsInstance<WpDataDB>().firstOrNull()
                StorePoint(
                    id = idStr ?: "${lat}_${lon}",
                    lat = lat!!,
                    lon = lon!!,
                    title = title ?: "Результат",
                    wp = wp,
                    dataItemsUI = item
                )
            }.distinctBy { it.id ?: "${it.lat}_${it.lon}" }
        }
    }

    // --- storeCenterPoint: built only from log_* fields (if exist) ---
    val storeCenterPoint by remember(items) {
        derivedStateOf {
            // find first item that has explicit log_* coords
            val candidate = items.firstOrNull { item ->
                val f = item.rawFields
                val lx = f.stringByKey("log_addr_location_xd")?.parseDoubleSafe()
                val ly = f.stringByKey("log_addr_location_yd")?.parseDoubleSafe()
                isValidLatLon(lx, ly)
            } ?: return@derivedStateOf null

            val f = candidate.rawFields
            val lx = f.stringByKey("log_addr_location_xd")!!.parseDoubleSafe()!!
            val ly = f.stringByKey("log_addr_location_yd")!!.parseDoubleSafe()!!
            val title = f.stringByKey("log_addr_txt") ?: candidate.rawObj.filterIsInstance<WpDataDB>().firstOrNull()?.addr_txt
            val addrId = candidate.rawFields.addrIdInt()
            Triple(LatLng(lx, ly), title ?: "Магазин", addrId)
        }
    }

    // --- positionPoints: ALWAYS use CoordX/CoordY for positions when storeCenter exists.
    // If no store center, we'll use fallbackStores to render (old behaviour).
    val positionPoints by remember(items) {
        derivedStateOf {
            items.mapNotNull { item ->
                val f = item.rawFields
                val cx = f.stringByKey("CoordX")?.parseDoubleSafe()
                val cy = f.stringByKey("CoordY")?.parseDoubleSafe()
                if (!isValidLatLon(cx, cy)) return@mapNotNull null
                val idStr = f.stringByKey("addr_id")
                val wp = item.rawObj.filterIsInstance<WpDataDB>().firstOrNull()
                val title = f.stringByKey("addr_txt") ?: wp?.addr_txt ?: "Результат"
                StorePoint(
                    id = idStr ?: "${cx}_${cy}",
                    lat = cx!!,
                    lon = cy!!,
                    title = title,
                    wp = wp,
                    dataItemsUI = item
                )
            }.distinctBy { it.id ?: "${it.lat}_${it.lon}" }
        }
    }

    // haversine
    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2).pow(2.0) +
                kotlin.math.cos(Math.toRadians(lat1)) *
                kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2).pow(2.0)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadius * c
    }

    // store icon
    val storeIcon: BitmapDescriptor? = remember {
        try {
            val bmp = context.renderVector(R.drawable.ic_store, context.dp(48f))
            BitmapDescriptorFactory.fromBitmap(bmp)
        } catch (t: Throwable) {
            null
        }
    }

    // camera includes either storeCenter + positionPoints + user OR fallbackStores + user
    LaunchedEffect(storeCenterPoint, positionPoints.map { it.lat to it.lon }, fallbackStores.map { it.lat to it.lon }, userLat, userLon) {
        delay(80)
        val latLngs = mutableListOf<LatLng>()
        if (storeCenterPoint != null) {
            latLngs.add(storeCenterPoint!!.first)
            positionPoints.forEach { latLngs.add(LatLng(it.lat, it.lon)) }
        } else {
            fallbackStores.forEach { latLngs.add(LatLng(it.lat, it.lon)) }
        }
        if (isValidLatLon(userLat, userLon)) latLngs.add(LatLng(userLat, userLon))

        try {
            when {
                latLngs.size >= 2 -> {
                    val builder = LatLngBounds.builder()
                    latLngs.forEach { builder.include(it) }
                    val bounds = builder.build()
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 80))
                }
                latLngs.size == 1 -> {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLngs.first(), 14f))
                }
                else -> {
                    if (isValidLatLon(userLat, userLon)) {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(userLat, userLon), 14f))
                    }
                }
            }
        } catch (e: Exception) {
            val p = latLngs.firstOrNull()
            p?.let { cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 12f)) }
        }
    }

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
        badgeText = android.graphics.Color.DKGRAY,
        pinTint = ContextCompat.getColor(context, R.color.maps_dark_blue)
    )

    // Drawing
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        // user marker (always)
        if (isValidLatLon(userLat, userLon)) {
            MarkerInfoWindow(
                state = MarkerState(position = LatLng(userLat, userLon)),
                icon = getBadgeYou(1),
            ) {
                InfoBalloonText(
                    title = "Ваше местоположение",
                    tailAlignment = 0.5f,
                    tailOnBottom = true
                )
            }
        }

        if (storeCenterPoint != null) {
            // --- SCENARIO A: there is explicit store center from log_* ---
            val (centerLatLng, centerTitle, _) = storeCenterPoint!!
            val radiusMeters = Globals.distanceMin.toDouble().coerceAtLeast(0.0)

            Circle(
                center = centerLatLng,
                radius = radiusMeters,
                strokeColor = Color.Gray,
                fillColor = Color(0x3300FF00),
                clickable = false,
                strokeWidth = 2f
            )

            MarkerInfoWindow(
                state = MarkerState(position = centerLatLng),
                icon = storeIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            ) {
                InfoBalloonText(title = centerTitle ?: "Магазин", tailAlignment = 0.5f, tailOnBottom = true)
            }

            // draw positionPoints (CoordX/CoordY) colored by distance to center
            positionPoints.forEach { p ->
                val idInt = p.id?.toIntOrNull()
                val agg = idInt?.let { cashAggByAddrId[it] }
                val count = agg?.first ?: 0
                val subtitle = agg?.let { (cnt, sum) -> "${cnt}/${formatSum(sum)}" } ?: ""

                val insideRadius = run {
                    val d = distanceMeters(centerLatLng.latitude, centerLatLng.longitude, p.lat, p.lon)
                    d <= Globals.distanceMin
                }

                val iconDesc: BitmapDescriptor = when {
                    insideRadius && count > 0 -> getBadgePin(count)
                    insideRadius -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    !insideRadius && count > 0 -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                }

                MarkerInfoWindow(
                    state = MarkerState(position = LatLng(p.lat, p.lon)),
                    onInfoWindowClick = {
                        if (count == 1 && p.wp?.user_id != 14041) {
                            p.wp?.let { onOpenContextMenu(it, contextUI) }
                        } else {
                            p.wp?.let {
                                selectedWpDataDB = p.wp
                                selectedStableId = p.dataItemsUI?.stableId
                                showPerevopros = true
                            }
                        }
                    },
                    icon = iconDesc
                ) {
                    InfoBalloonText(
                        title = p.title ?: "Позиция",
                        subtitle = subtitle,
                        tailAlignment = 0.5f,
                        tailOnBottom = true
                    )
                }
            }
        } else {
            // --- SCENARIO B: no log_* store center — fallback to old behavior using fallbackStores ---
            fallbackStores.forEach { s ->
                val idInt = s.id?.toIntOrNull()
                val agg = idInt?.let { cashAggByAddrId[it] }
                val count = agg?.first ?: 0
                val subtitle = agg?.let { (cnt, sum) -> "${cnt}/${formatSum(sum)}" } ?: ""

                // previous logic: insideRadius defaults to true when no store center
                val insideRadius = true

                val iconDesc: BitmapDescriptor =
                    if (insideRadius && count > 0) getBadgePin(count)
                    else if (insideRadius) BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    else BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)

                MarkerInfoWindow(
                    state = MarkerState(position = LatLng(s.lat, s.lon)),
                    onInfoWindowClick = {
                        if (count == 1 && s.wp?.user_id != 14041) {
                            s.wp?.let { onOpenContextMenu(it, contextUI) }
                        } else {
                            s.wp?.let {
                                selectedWpDataDB = s.wp
                                selectedStableId = s.dataItemsUI?.stableId
                                showPerevopros = true
                            }
                        }
                    },
                    icon = iconDesc
                ) {
                    InfoBalloonText(
                        title = s.title ?: "Точнiсть: 13.5132065",
                        subtitle = subtitle,
                        tailAlignment = 0.5f,
                        tailOnBottom = true
                    )
                }
            }
        }
    }

    // confirm dialog (unchanged)
    if (showPerevopros) {
        val sortingFieldAdr = remember { SortingField("addr_txt", viewModel.getTranslateString("Адреса", 1101), 1) }
        val sortingFieldDate = remember { SortingField("dt", viewModel.getTranslateString("Дата", 1100), 1) }

        LaunchedEffect(sortingFieldAdr, sortingFieldDate) {
            viewModel.updateSorting(sortingFieldAdr, 0)
            viewModel.updateSorting(sortingFieldDate, 1)
        }

        MessageDialog(
            title = "Перейти к посещенням",
            status = DialogStatus.NORMAL,
            subTitle = selectedWpDataDB?.addr_txt,
            message = "Показать все работы, по этому адресу за период с ${uiState.filters?.rangeDataByKey?.start?.toDayMonthFormat()} " +
                    "по ${uiState.filters?.rangeDataByKey?.end?.toDayMonthFormat()}?" +
                    "\nНажав 'Применить' система отобразит вам все клиенто-посещения по этому адресу и выделит их цветом",
            onDismiss = { showPerevopros = false },
            onConfirmAction = {
                selectedStableId?.let { id -> viewModel.requestScrollToVisit(id) }
                selectedWpDataDB?.let { viewModel.highlightByAddrId(it.addr_id.toString(), color = green) }
                showPerevopros = false
                onDismiss()
            },
            onCancelAction = { showPerevopros = false }
        )
    }
}

/* ---------- Helpers для иконок ---------- */

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
