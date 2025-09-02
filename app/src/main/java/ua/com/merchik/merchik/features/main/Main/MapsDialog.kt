package ua.com.merchik.merchik.features.main.Main

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import ua.com.merchik.merchik.features.main.componentsUI.InfoBalloonText


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MapsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onOpenContextMenu: (WpDataDB) -> Unit   // новый параметр
) {

    val uiState by viewModel.uiState.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    var offsetSizeFont by remember { mutableStateOf(viewModel.offsetSizeFonts.value) }
    val dataItemsUI = mutableListOf<DataItemUI>()

    val result = filterAndSortDataItems(
        items = uiState.items,
        filters = uiState.filters,
        sortingFields = uiState.sortingFields,
        rangeStart = viewModel.rangeDataStart.value,
        rangeEnd = viewModel.rangeDataEnd.value,
        searchText = uiState.filters?.searchText
    )
    dataItemsUI.addAll(result.items)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 40.dp)
                .background(color = Color.Transparent)
        ) {
            Row(
                modifier = Modifier.align(alignment = Alignment.End)
            ) {
                ImageButton(
                    id = R.drawable.ic_settings,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(color = Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier
                        .padding(start = 15.dp, bottom = 10.dp),
                    onClick = {
                        showSettingsDialog = true
                    }
                )
                ImageButton(
                    id = R.drawable.ic_letter_x,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(color = Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier
                        .padding(start = 15.dp, bottom = 10.dp),
                    onClick = { onDismiss.invoke() }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally),
                        text = "Карта"
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = "## Опис можливих дій. Додасться поступово"
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(color = Color.White)
                    ) {
                        StoresMap(
                            items = result.items, // <-- Подаём актуальный список
                            userLat = Globals.CoordX,
                            userLon = Globals.CoordY,
                            modifier = Modifier.fillMaxSize(),
                            fetchAddressById = { id ->
                                // Если ваш DAO suspend — просто вызов:
                                RoomManager.SQL_DB.addressDao().getById(id)
                            },
                            onOpenContextMenu = onOpenContextMenu   // прокидываем дальше
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
//                        Button(
//                            onClick = {
//                                viewModel.updateContent()
//                                onDismiss.invoke()
//                            },
//                            shape = RoundedCornerShape(8.dp),
//                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue)),
//                            modifier = Modifier
//                                .weight(1f)
//                                .padding(5.dp)
//                        ) {
//                            Text(
//                                viewModel.getTranslateString(
//                                    "Ок"
//                                )
//                            )
//                        }

//                        Button(
//                            onClick = {
//                                viewModel.saveSettings()
//                                viewModel.updateContent()
//                                viewModel.updateOffsetSizeFonts(offsetSizeFont)
//                                onDismiss.invoke()
//                            },
//                            shape = RoundedCornerShape(8.dp),
//                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.orange)),
//                            modifier = Modifier
//                                .weight(1f)
//                                .padding(5.dp)
//                        ) {
//                            Text(viewModel.getTranslateString("Застосувати"))
//                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
            MessageDialog(
                title = "Не доступно",
                status = DialogStatus.ALERT,
                message = "Данный раздел находится в стадии в разработки",
                onDismiss = {
                    showSettingsDialog = false
                },
                onConfirmAction = {
                    showSettingsDialog = false
                }
            )
    }
}

// Модель точки магазина
data class StorePoint(
    val id: String,
    val lat: Double,
    val lon: Double,
    val title: String,
    val wp: WpDataDB? = null
) {
}

// ================== Helpers ==================

private fun String.parseDoubleSafe(): Double? {
    val cleaned = trim().replace(',', '.')
    return cleaned.toDoubleOrNull()
}

private fun isValidLatLon(lat: Double, lon: Double): Boolean =
    lat in -90.0..90.0 && lon in -180.0..180.0 && !(lat == 0.0 && lon == 0.0)

private fun List<FieldValue>.stringByKey(key: String): String? {
    val fv = firstOrNull { it.key == key } ?: return null
    // value.value — ваш нормализованный текст; если пуст — попробуем rawValue
    return fv.value.value.takeIf { it.isNotBlank() }
        ?: (fv.value.rawValue?.toString()?.takeIf { it.isNotBlank() })
}

private fun List<FieldValue>.toStorePointOrNull(): StorePoint? {
    val id = stringByKey("addr_id") ?: return null
    val latStr = stringByKey("addr_location_xd") ?: return null
    val lonStr = stringByKey("addr_location_yd") ?: return null
    val title = stringByKey("addr_txt") ?: "ТТ №$id"

    val lat = latStr.parseDoubleSafe() ?: return null
    val lon = lonStr.parseDoubleSafe() ?: return null
    if (!isValidLatLon(lat, lon)) return null

    return StorePoint(id = id, lat = lat, lon = lon, title = title)
}

// Удобный тип: как получить AddressSDB по id (suspend)
typealias AddressFetcher = suspend (Int) -> AddressSDB?

@Composable
fun StoresMap(
    items: List<DataItemUI>,
    userLat: Double,
    userLon: Double,
    modifier: Modifier = Modifier,
    fetchAddressById: AddressFetcher,
    onOpenContextMenu: (WpDataDB) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(userLat, userLon), 14f)
    }

    val context = LocalContext.current

    // Собираем магазины из uiState.items
//    val stores by remember(items) {
//        derivedStateOf {
//            items.mapNotNull { it.rawFields.toStorePointOrNull() }
//                .distinctBy { it.id }
//        }
//    }
    // Кэш подгруженных из Room адресов
    val addressCache = remember { mutableStateMapOf<Int, AddressSDB?>() }

    // Какие id нужно дотянуть (у элементов нет валидных lat/lon или title)
    val missingIds = remember(items) {
        items.asSequence()
            .mapNotNull { it.rawFields.stringByKey("addr_id")?.toIntOrNull() }
            .filter { id ->
                val f = items.first {
                    it.rawFields.stringByKey("addr_id")?.toIntOrNull() == id
                }.rawFields
                val lat = f.stringByKey("addr_location_xd")?.parseDoubleSafe()
                val lon = f.stringByKey("addr_location_yd")?.parseDoubleSafe()
                val title = f.stringByKey("addr_txt")
                // если нет чего-то важного — понадобится из Room
                (lat == null || lon == null || !isValidLatLon(lat, lon) || title.isNullOrBlank())
                        && !addressCache.containsKey(id)
            }
            .distinct()
            .toList()
    }

    // Подтягиваем недостающие адреса (IO) и кладём в кэш
    // Подтягиваем недостающие адреса (IO) и кладём в кэш
    LaunchedEffect(missingIds) {
        if (missingIds.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                missingIds.forEach { id ->
                    val addr = fetchAddressById(id) // suspend
                    withContext(Dispatchers.Main) {
                        addressCache[id] = addr
                    }
                }
            }
        }
    }


    // Формируем список точек: берём из rawFields, а недостающее — из кэша Room
    val stores by remember(items, addressCache) {
        derivedStateOf {
            items.mapNotNull { item ->
                val f = item.rawFields
                val idStr = f.stringByKey("addr_id") ?: return@mapNotNull null
                val idInt = idStr.toIntOrNull()

                var lat = f.stringByKey("addr_location_xd")?.parseDoubleSafe()
                var lon = f.stringByKey("addr_location_yd")?.parseDoubleSafe()
                var title = f.stringByKey("addr_txt")

                if ((lat == null || lon == null || !isValidLatLon(
                        lat,
                        lon
                    ) || title.isNullOrBlank()) && idInt != null
                ) {
                    addressCache[idInt]?.let { addr ->
                        if (lat == null) lat = addr.locationXd?.toDouble()
                        if (lon == null) lon = addr.locationYd?.toDouble()
                        if (title.isNullOrBlank()) title = addr.nm
                    }
                }

                if (lat == null || lon == null || !isValidLatLon(
                        lat!!,
                        lon!!
                    )
                ) return@mapNotNull null
                val wp = item.rawObj.filterIsInstance<WpDataDB>().firstOrNull()

                StorePoint(
                    id = idStr,
                    lat = lat!!,
                    lon = lon!!,
                    title = title ?: "ТТ №$idStr",
                    wp = wp
                )
            }.distinctBy { it.id }
        }
    }

    // При изменении списка магазинов — двигаем камеру
    LaunchedEffect(stores) {
        val points = stores.map { LatLng(it.lat, it.lon) }
        when {
            points.size >= 2 -> {
                val builder = LatLngBounds.builder()
                points.forEach { builder.include(it) }
                // дополнительно включим пользователя, если координаты валидны
                if (isValidLatLon(userLat, userLon)) builder.include(LatLng(userLat, userLon))
                val bounds = builder.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, /*padding*/ 80)
                )
            }

            points.size == 1 -> {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(points.first(), 14f)
                )
            }

            else -> {
                // Нет магазинов — центрируемся на пользователе, если координаты валидны
                if (isValidLatLon(userLat, userLon)) {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(LatLng(userLat, userLon), 14f)
                    )
                }
            }
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        // Маркер пользователя (если координаты валидны)
        if (isValidLatLon(userLat, userLon)) {
            Marker(
                state = MarkerState(position = LatLng(userLat, userLon)),
                title = "Ваше местоположение",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            )
        }


        // Маркеры магазинов
        stores.forEach { s ->
            MarkerInfoWindow(
                state = MarkerState(position = LatLng(s.lat, s.lon)),
//                title = "${s.title}",
//                snippet = "Подробности",         // это и будет второй строкой
                onInfoWindowClick = {
                    Toast.makeText(context, "## Будет стандартное меню", Toast.LENGTH_SHORT).show()
                    s.wp?.let { onOpenContextMenu(it) }
                },
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            ) {
                InfoBalloonText(
                    title = s.title,
                    subtitle = "Подробнее",
                    tailAlignment = 0.5f,      // можно сдвинуть влево/вправо 0f..1f
                    tailOnBottom = true        // хвостик снизу (как у стандартного окна)
                )
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    // прямоугольная часть
//                    Surface(
//                        shape = RoundedCornerShape(12.dp),
//                        color = Color.White,
//                        border = BorderStroke(1.dp, Color(0x33000000)),
//                        shadowElevation = 8.dp
//                    ) {
//                        Column(Modifier.padding(8.dp)) {
//                            Text(s.title, style = MaterialTheme.typography.titleMedium)
//                            Text("Подробности", style = MaterialTheme.typography.bodyMedium)
//                        }
//                    }
//                    // Треугольник-хвостик
//                    Canvas(Modifier.size(width = 20.dp, height = 10.dp)) {
//                        val path = Path().apply {
//                            moveTo(size.width / 2f, size.height) // нижняя точка
//                            lineTo(0f, 0f)
//                            lineTo(size.width, 0f)
//                            close()
//                        }
//                        drawPath(path, color = Color.White)
//                        drawPath(path, color = Color(0x33000000), style = Stroke(width = 2f))
//                    }
//                }
            }
        }
    }
}


@Composable
fun MapWithLoading(
    url: String = "https://merchik.net/",
    modifier: Modifier
) {
    var committed by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }
    var minTimePassed by remember { mutableStateOf(false) }
    var epoch by remember { mutableStateOf(0) } // ключ новой загрузки
    val minShowMillis = 1800L

    // Таймер минимального показа лоадера
    LaunchedEffect(epoch) {
        minTimePassed = false
        delay(minShowMillis)
        minTimePassed = true
    }

    val showLoading by remember {
        derivedStateOf { !committed || progress < 95 || !minTimePassed }
    }

    Box(
        modifier = modifier
    ) {
        val context = LocalContext.current
        val webView = remember { WebView(context) }

        DisposableEffect(Unit) {
            onDispose { webView.destroy() }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                webView.apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    // важно присвоить!
                    settings.userAgentString = settings.userAgentString.replace("; wv", "")

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(v: WebView?, url: String?, ico: Bitmap?) {
                            committed = false
                            progress = 0
                            epoch++ // перезапуск таймера минимального показа
                        }

                        override fun onPageCommitVisible(view: WebView?, url: String?) {
                            committed = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            if (progress == 100) committed = true // fallback
                        }

                        override fun onReceivedError(
                            view: WebView?, request: WebResourceRequest?, error: WebResourceError?
                        ) {
                            // можно показать экран ошибки; для простоты — скрываем лоадер
                            committed = true
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                        }
                    }

                    loadUrl(url)
                }
            }
        )

        // Лоадер поверх WebView
        AnimatedVisibility(
            visible = showLoading,
            enter = fadeIn(),
            exit = fadeOut(tween(350))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.maps)
                )
                val lottieProgress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    speed = 1.5f
                )
                LottieAnimation(
                    composition = composition,
                    progress = { lottieProgress },
                    modifier = Modifier.fillMaxSize()
                )

                if (progress in 1..99) {
                    Text(
                        text = "$progress%",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        color = Color.Gray
                    )
                }
            }
        }

        // back по истории WebView
        BackHandler(enabled = webView.canGoBack()) {
            webView.goBack()
        }
//        AndroidView(
//            modifier = Modifier.fillMaxSize(),
//            factory = { context ->
//                WebView(context).apply {
//                    settings.javaScriptEnabled = true
//                    settings.domStorageEnabled = true
//                    settings.databaseEnabled = true
//                    // важно: присвоить результат
//                    settings.userAgentString = settings.userAgentString.replace("; wv", "")
//
//                    webViewClient = object : WebViewClient() {
//                        override fun onPageStarted(
//                            view: WebView?, url: String?, favicon: android.graphics.Bitmap?
//                        ) {
//                            isLoading = true
//                        }
//
//                        // Когда контент реально отрисован (API 23+)
//                        override fun onPageCommitVisible(view: WebView?, url: String?) {
//                            isLoading = false
//                        }
//
//                        override fun onPageFinished(view: WebView?, url: String?) {
//                            if (progress == 10000) isLoading = false
//                        }
//
//                        override fun onReceivedError(
//                            view: WebView?,
//                            request: WebResourceRequest?,
//                            error: WebResourceError?
//                        ) {
//                            isLoading = false
//                        }
//                    }
//
//                    webChromeClient = object : WebChromeClient() {
//                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
//                            progress = newProgress
//                        }
//                    }
//
//                    loadUrl(url)
//                }
//            }
//        )
//
//        if (isLoading) {
//            // Оверлей с анимацией Lottie
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.White),
//                contentAlignment = Alignment.Center
//            ) {
//                val composition by rememberLottieComposition(
//                    LottieCompositionSpec.RawRes(R.raw.maps)
//                )
//                val lottieProgress by animateLottieCompositionAsState(
//                    composition = composition,
//                    iterations = LottieConstants.IterateForever,
//                    speed = 1.5f
//                )
//                LottieAnimation(
//                    composition = composition,
//                    progress = { lottieProgress },
//                    modifier = Modifier.fillMaxSize()
//                )
//
//                if (progress in 1..99) {
//                    Text(
//                        text = "$progress%",
//                        modifier = Modifier
//                            .align(Alignment.BottomCenter)
//                            .padding(bottom = 16.dp),
//                        color = Color.Gray
//                    )
//                }
//            }
//        }
    }
}
