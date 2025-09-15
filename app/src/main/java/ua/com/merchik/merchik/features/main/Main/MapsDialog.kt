package ua.com.merchik.merchik.features.main.Main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
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
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
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
    val id: String,
    val lat: Double,
    val lon: Double,
    val title: String,
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

private fun formatSum(sum: Double): String = "${sum.roundToInt()} грн"

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

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(userLat, userLon), 14f)
    }

    val context = LocalContext.current
    var showPerevopros by remember { mutableStateOf(false) }
    var selectedStableId by remember { mutableStateOf<Long?>(null) } // <-- новый state
    var selectedWpDataDB by remember { mutableStateOf<WpDataDB?>(null) } // <-- новый state


    val green = colorResource(id = R.color.ufmd_accept_t)

    // --- Агрегация по addr_id ---
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

    // Кеш адресов
    val addressCache = remember { mutableStateMapOf<Int, AddressSDB?>() }

    // Собираем список уникальных addr_id, для которых может понадобиться подгрузка
    val missingIds by remember(items, addressCache.keys) {
        derivedStateOf {
            items.asSequence()
                .mapNotNull { it.rawFields.addrIdInt() }
                .distinct()
                .filter { id ->
                    if (addressCache.containsKey(id)) return@filter false
                    // проверяем наличие валидных coords/title в первой записи с этим id
                    val first = items.firstOrNull { it.rawFields.addrIdInt() == id } ?: return@filter true
                    val f = first.rawFields
                    val lat = f.stringByKey("addr_location_xd")?.parseDoubleSafe()
                    val lon = f.stringByKey("addr_location_yd")?.parseDoubleSafe()
                    val title = f.stringByKey("addr_txt")
                    (lat == null || lon == null || !isValidLatLon(lat, lon) || title.isNullOrBlank())
                }
                .toList()
        }
    }

    // Загружаем недостающие адреса
    LaunchedEffect(missingIds) {
        if (missingIds.isEmpty()) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            missingIds.forEach { id ->
                val addr = try {
                    fetchAddressById(id)
                } catch (e: Exception) {
                    // обработать ошибку (лог/Crashlytics) при необходимости
                    null
                }
                withContext(Dispatchers.Main) {
                    addressCache[id] = addr
                }
            }
        }
    }

    // Формируем список StorePoint — берём данные из поля, а если чего-то нет — из кэша
    val stores by remember(items, addressCache) {
        derivedStateOf {
            items.mapNotNull { item ->
                val f = item.rawFields
                val idStr = f.stringByKey("addr_id") ?: return@mapNotNull null
                val idInt = idStr.toIntOrNull()

                var lat = f.stringByKey("addr_location_xd")?.parseDoubleSafe()
                var lon = f.stringByKey("addr_location_yd")?.parseDoubleSafe()
                var title = f.stringByKey("addr_txt")

                if ((lat == null || lon == null || !isValidLatLon(lat, lon) || title.isNullOrBlank()) && idInt != null) {
                    addressCache[idInt]?.let { addr ->
                        if (lat == null) lat = addr.locationXd?.toDouble()
                        if (lon == null) lon = addr.locationYd?.toDouble()
                        if (title.isNullOrBlank()) title = addr.nm
                    }
                }

                if (!isValidLatLon(lat, lon)) return@mapNotNull null

                val wp = item.rawObj.filterIsInstance<WpDataDB>().firstOrNull()
                StorePoint(
                    id = idStr,
                    lat = lat!!,
                    lon = lon!!,
                    title = title ?: "ТТ №$idStr",
                    wp = wp,
                    dataItemsUI = item
                )
            }.distinctBy { it.id }
        }
    }

    // При изменении магазинов — перемещаем камеру
    LaunchedEffect(stores) {
        val points = stores.map { LatLng(it.lat, it.lon) }
        when {
            points.size >= 2 -> {
                val builder = LatLngBounds.builder()
                points.forEach { builder.include(it) }
                if (isValidLatLon(userLat, userLon)) builder.include(LatLng(userLat, userLon))
                val bounds = builder.build()
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 80))
            }
            points.size == 1 -> {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(points.first(), 14f))
            }
            else -> {
                if (isValidLatLon(userLat, userLon)) {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(userLat, userLon), 14f))
                }
            }
        }
    }

    // prepare badge factories (remember внутри)
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

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        // маркер пользователя
        if (isValidLatLon(userLat, userLon)) {
            MarkerInfoWindow(
                state = MarkerState(position = LatLng(userLat, userLon)),
                icon = getBadgeYou(1),
                onInfoWindowClick = {
                    Toasty.normal(context, getFormattedMessage()).show()
                }
            ) {
                InfoBalloonText(
                    title = "Ваше местоположение",
                    tailAlignment = 0.5f,
                    tailOnBottom = true
                )
            }
        }

        // маркеры магазинов
        stores.forEach { s ->
            val idInt = s.id.toIntOrNull()
            val agg = idInt?.let { cashAggByAddrId[it] }
            val count = agg?.first ?: 0
            val iconDesc: BitmapDescriptor =
                if (count > 0) getBadgePin(count) else BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)

            val subtitle = agg?.let { (cnt, sum) -> "$cnt/${formatSum(sum)}" }

            MarkerInfoWindow(
                state = MarkerState(position = LatLng(s.lat, s.lon)),
                onInfoWindowClick = {
                    // если у записи есть объект wp и пользователь соответствует условию — открыт контекст
                    if (count == 1 && s.wp?.user_id != 14041) {
                        s.wp?.let { onOpenContextMenu(it, contextUI) }
                    } else {

                        selectedWpDataDB = s.wp
                        // сохраняем stableId локально, но НЕ эмитим scroll пока пользователь не подтвердит
                        selectedStableId = s.dataItemsUI?.stableId

                        showPerevopros = true
                    }
                },
                icon = iconDesc
            ) {
                InfoBalloonText(
                    title = s.title,
                    subtitle = subtitle,
                    tailAlignment = 0.5f,
                    tailOnBottom = true
                )
            }
        }
    }

    // Диалог перехода к посещениям
    if (showPerevopros) {
        val sortingFieldAdr = remember {
            SortingField("addr_txt", viewModel.getTranslateString("Адреса", 1101), 1)
        }
        val sortingFieldDate = remember {
            SortingField("dt", viewModel.getTranslateString("Дата", 1100), 1)
        }

        LaunchedEffect(sortingFieldAdr, sortingFieldDate) {
            viewModel.updateSorting(sortingFieldAdr, 0)
            viewModel.updateSorting(sortingFieldDate, 1)
        }

        MessageDialog(
            title = "Перейти к посещениям",
            status = DialogStatus.NORMAL,
            subTitle = selectedWpDataDB?.addr_txt,
            message = "Показать все работы, по этому адресу за период с ${uiState.filters?.rangeDataByKey?.start?.toDayMonthFormat()} " +
                    "по ${uiState.filters?.rangeDataByKey?.end?.toDayMonthFormat()}?" +
                    "\nНажав 'Применить' система отобразит вам все клиенто-посещения по этому адресу и выделит их цветом",
            onDismiss = { showPerevopros = false },
            onConfirmAction = {
                // эмитим запрос на скролл (если есть сохранённый stableId)
                selectedStableId?.let { id ->
                    viewModel.requestScrollToVisit(id)
                }

                selectedWpDataDB?.let {
                    viewModel.highlightByAddrId(it.addr_id.toString(), color = green)
                }

                showPerevopros = false
                onDismiss()
//                Toasty.normal(context, getFormattedMessage()).show()
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
private fun rememberBadgePinCache(
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

    return remember(pinRes, pinHeightDp, badgeDiameter, badgeYOffsetK, badgeBg, badgeBorder, badgeText, badgeIconRes, pinTint) {
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
