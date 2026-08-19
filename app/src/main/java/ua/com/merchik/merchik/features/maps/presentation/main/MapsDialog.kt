package ua.com.merchik.merchik.features.maps.presentation.main


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.LaunchOrigin
import ua.com.merchik.merchik.dialogs.features.LoadingDialogWithPercent
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.DBViewModels.AdditionalWorksMapSearchLocationHolder
import ua.com.merchik.merchik.features.main.DBViewModels.AddressSDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.CustomAditionalAddressSelectionHolder
import ua.com.merchik.merchik.features.main.Main.AnchoredAnimatedDialog
import ua.com.merchik.merchik.features.main.Main.FilteringDialog
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.captureBoundsInScreen
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import ua.com.merchik.merchik.features.main.componentsUI.TextFieldInputRounded
import ua.com.merchik.merchik.features.main.componentsUI.TextFieldInputRoundedSuggestion
import ua.com.merchik.merchik.features.maps.domain.isValidLatLon
import ua.com.merchik.merchik.features.maps.domain.parseDoubleSafe
import ua.com.merchik.merchik.features.maps.domain.stringByKey
import ua.com.merchik.merchik.features.maps.presentation.MapActionsBridge
import ua.com.merchik.merchik.features.maps.presentation.MapEffect
import ua.com.merchik.merchik.features.maps.presentation.MapIntent
import ua.com.merchik.merchik.features.maps.presentation.viewModels.BaseMapViewModel
import ua.com.merchik.merchik.features.maps.presentation.viewModels.MapFromMapsViewModel
import ua.com.merchik.merchik.features.maps.presentation.viewModels.MapFromWPdataViewModel
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.UUID


private const val MAP_SEARCH_MAX_SUGGESTIONS = 5
private const val MAP_SEARCH_BIAS_RADIUS_METERS = 50_000.0
private const val MAP_SEARCH_AREA_ACTION_TEXT = "Искать работу в этом районе"

private val mapSearchHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .callTimeout(8, TimeUnit.SECONDS)
        .build()
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun MapsDialog(
    mainViewModel: MainViewModel,
    onDismiss: () -> Unit,
    onOpenContextMenu: (WpDataDB, ContextUI, LaunchOrigin?) -> Unit
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val contextUI = mainViewModel.contextUI
    val isAdditionalWorksMap =
        contextUI == ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER ||
                contextUI == ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER_MULT
    val isCustomAditionalAddressMap =
        mainViewModel is AddressSDBViewModel &&
                CustomAditionalAddressSelectionHolder.mapSelectionEnabled
    val customAditionalAddressMapSubtitle =
        CustomAditionalAddressSelectionHolder.mapSubtitle.orEmpty()
    val customAditionalAddressMapDistance =
        CustomAditionalAddressSelectionHolder.mapDistanceMeters

    val distance by mainViewModel.offsetDistanceMeters.collectAsState()

    val highlightColor: Color = colorResource(id = R.color.ufmd_accept_t)

    // 1) генерим id сессии диалога (константа на время жизни composable)
    val sessionId = remember { System.currentTimeMillis() }

    var showToolTip by remember { mutableStateOf(false) }
    var notReadyMenu by remember { mutableStateOf(false) }

    var showToolTipKostil by remember { mutableStateOf(false) }

    var showFilteringDialog by remember { mutableStateOf(false) }
    var filterBtnRect by remember { mutableStateOf<Rect?>(null) }

    var isActiveFiltered by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()  // <-- добавили

    var maxLinesSubTitle by remember { mutableStateOf(1) }
    var mapSearchText by remember { mutableStateOf("") }
    var mapSearchMarker by remember { mutableStateOf<MapSearchMarker?>(null) }
    var mapSearchError by remember { mutableStateOf<String?>(null) }
    var mapSearchSuggestions by remember { mutableStateOf<List<MapSearchPrediction>>(emptyList()) }
    var mapSearchLockedText by remember { mutableStateOf<String?>(null) }
    var mapSearchSessionToken by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var additionalWorksSearchOrigin by remember {
        mutableStateOf(
            AdditionalWorksMapSearchLocationHolder.get()?.let { point ->
                LatLng(point.latitude, point.longitude)
            }
        )
    }
    var pendingAdditionalWorksSearchMarker by remember { mutableStateOf<MapSearchMarker?>(null) }
    var additionalWorksMapSearchInProgress by remember { mutableStateOf(false) }

    val formatterDDmmYYYY = DateTimeFormatter
        .ofPattern("dd MMM yyyy")
        .withLocale(Locale.getDefault())

    val formatterHHdd_DDmmYYYY = DateTimeFormatter
        .ofPattern("HH:mm dd.MM.yyyy")
        .withLocale(Locale.getDefault())

// Decide scenario once per input
    val hasLogCenter by remember(uiState.items) {
        mutableStateOf(
            uiState.items.firstOrNull {
                it.rawFields.stringByKey("log_addr_location_xd")?.parseDoubleSafe() != null &&
                        it.rawFields.stringByKey("log_addr_location_yd")?.parseDoubleSafe() != null
            } != null
        )
    }


    val vm: BaseMapViewModel =
        if (hasLogCenter) hiltViewModel<MapFromMapsViewModel>() else hiltViewModel<MapFromWPdataViewModel>()

    // 1) Подписываемся на состояние карты
    val mapState by vm.state.collectAsState()


    if (notReadyMenu) {
        MessageDialog(
            title = "Додатковий заробіток",
            status = DialogStatus.NORMAL,
            message = "Заявка на выполнение работ создана и передана куратору, в течении нескольких минут вы получите ответ. Если ответ будет положительный это посещение будет перенесено в план работ",
            okButtonName = "Ок",
            onDismiss = { notReadyMenu = false },
            onConfirmAction = { notReadyMenu = false }
        )
    }


    LaunchedEffect(
        vm,
        onDismiss,
        contextUI,
        highlightColor,
        sessionId,
        isCustomAditionalAddressMap
    ) {
        vm.attachBridge(object : MapActionsBridge {
            override val contextUI: ContextUI = contextUI
            override val highlightColor: Color = highlightColor
            override fun requestScrollToVisit(stableId: Long) =
                mainViewModel.requestScrollToVisit(stableId)

            override fun highlightByAddrId(addrId: String, color: Color) =
                mainViewModel.highlightByAddrId(addrId, color)

            override val addressSelectionMode: Boolean = isCustomAditionalAddressMap

            override fun selectAddressFromMap(
                stableId: Long?,
                addressId: String?,
                addressName: String?
            ) {
                val targetStableId = stableId ?: addressId?.toLongOrNull()
                targetStableId?.let { mainViewModel.selectOneItemFromMap(it) }
            }

            override fun dismissHost() = onDismiss()
        })
        vm.process(MapIntent.Init(sessionId))
    }

    // есть ли на карте точки пользователя 14041 (для FromWPdata)
    val isRnoUserOnMap by remember(mapState.pointsUi) {
        derivedStateOf { mapState.pointsUi.any { it.point.wp?.user_id == 14041 } }
    }

    // период из фильтров
    val periodStrt = remember(uiState.filters?.rangeDataByKey) {
        uiState.filters?.rangeDataByKey?.let { range ->
            range.start?.format(formatterDDmmYYYY)
        } ?: "не визначено"
    }


    val periodEnd = mainViewModel.rangeDataEnd.value
        ?.format(formatterDDmmYYYY)
        ?: "не визначено"


    // время для fromMaps
    val validTime = 1_800_000L // 30 минут в миллисекундах

    val wpDataDB: WpDataDB? = try {
        Gson().fromJson(mainViewModel.dataJson, WpDataDB::class.java)
    } catch (e: Exception) {
        null
    }

// определяем время
    val startTime = remember {

        val startMillis =
            if (wpDataDB != null && wpDataDB.visit_start_dt > 0 && wpDataDB.visit_end_dt > 0) {
                wpDataDB.visit_start_dt - validTime
            } else {
                System.currentTimeMillis() - validTime
            }

        // Приводим к миллисекундам, если данные в секундах
        val millis = if (startMillis < 10_000_000_000L) startMillis * 1000 else startMillis

        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .format(formatterHHdd_DDmmYYYY)
    }

    val endTime = remember {

        val endMillis = if (wpDataDB != null && wpDataDB.visit_end_dt > 0) {
            wpDataDB.visit_end_dt
        } else {
            System.currentTimeMillis()
        }

        val millis = if (endMillis < 10_000_000_000L) endMillis * 1000 else endMillis

        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .format(formatterHHdd_DDmmYYYY)
    }


    // --- тексты под конкретный сценарий ---
    // Заголовок -> subTitle диалога "Довідка"
    // Вторая строка -> подзаголовок на карте (вместо "## Опис …")
    // Третья строка -> message в диалоге "Довідка"
    val isFromMaps = hasLogCenter
    val (infoSubtitle, mapSubheaderText, helpMessage) = remember(
        isFromMaps,
        isRnoUserOnMap,
        periodStrt,
        periodEnd
    ) {
        when {
            // 1) FromWPdata + user 14041  — "Додатковий заробіток."
            !isFromMaps && isRnoUserOnMap -> Triple(
                "Додатковий заробіток.",
                String.format(
                    mainViewModel.getTranslateString(
                        text = "Адреса с возможностью дополнительного заработка за период с %s по %s. Для получения дополнительной информации нажмите иконку «?» вверху текущей формы.",
                        translateId = 9069
                    ), periodStrt, periodEnd
                ),

                mainViewModel.getTranslateString(
                    "На карті зеленими маркерами позначено адреси, за якими є можливість виконувати роботи за окрему (додаткову) оплату.\n" +
                            "Цифри всередині маркерів відображають кількість доступних робіт за відповідною адресою.\n" +
                            "Синім маркером позначено ваше поточне місцезнаходження (МП).\n" +
                            "Щоб дізнатися детальнішу інформацію про роботи та оплату за їх виконання за конкретною адресою, натисніть на відповідний маркер.",
                    9056
                )
            )

            // 2) FromMaps — “Опція МП та Відвідування (Головна)”
            isFromMaps -> Triple(
                "Опція МП та Відвідування (Головна).",
                "Історія місцезнаходження користувача за період з $startTime по $endTime.\n" +
                        "Для отримання додаткової інформації натисніть на іконку «?» у верхній частині поточної форми.",
                "На карті:\n" +
                        "— синім маркером позначено поточне місцезнаходження користувача;\n" +
                        "— зеленим маркером позначено адресу — місце виконання робіт;\n" +
                        "— зеленими та червоними точками позначено фактичне місцезнаходження користувача.\n" +
                        "\n" +
                        "Точки з’єднані лінією, що дає змогу відстежити маршрут руху.\n" +
                        "\n" +
                        "— зелені точки — зауваження відсутні (користувач перебував за адресою з урахуванням допустимої похибки);\n" +
                        "— червоні точки — перебування за межами адреси виконання робіт.\n" +
                        "\n" +
                        "Щоб отримати детальнішу інформацію про конкретне місце, натисніть на маркер або відповідну точку."
            )

            // 3) FromWPdata + другой пользователь — “План Робіт”
            else -> Triple(
                "План Робіт.",
                "Адреси, за якими виконуються роботи за період з $periodStrt по $periodEnd." +
                        "Для отримання додаткової інформації натисніть на іконку «?» у верхній частині поточної форми.",
                "На карті зеленими маркерами позначено адреси, за якими заплановано виконання робіт.\n" +
                        "Цифри всередині маркерів відображають кількість запланованих робіт за відповідною адресою.\n" +
                        "Синім маркером позначено ваше поточне місцезнаходження (МП).\n" +
                        "Щоб переглянути детальну інформацію щодо робіт за конкретною адресою, натисніть на відповідний маркер."
            )
        }
    }


    val effectiveMapSubheaderText =
        if (isCustomAditionalAddressMap && customAditionalAddressMapSubtitle.isNotBlank()) {
            customAditionalAddressMapSubtitle
        } else {
            mapSubheaderText
        }

    // Effects handling
    val effects = vm.effects
    val cameraController = rememberCameraPositionState()

    fun setAdditionalWorksSearchMarker(marker: MapSearchMarker) {
        mapSearchMarker = marker
        mapSearchText = marker.title
        mapSearchLockedText = marker.title
        mapSearchSuggestions = emptyList()
        mapSearchError = null
    }

    fun runAdditionalWorksMapSearch(marker: MapSearchMarker) {
        if (!isAdditionalWorksMap || additionalWorksMapSearchInProgress) return

        additionalWorksMapSearchInProgress = true
        additionalWorksSearchOrigin = marker.position
        AdditionalWorksMapSearchLocationHolder.set(
            latitude = marker.position.latitude,
            longitude = marker.position.longitude
        )
        val progress = ProgressViewModel(1)
        val loadingDialog = context.findActivity()?.let { activity ->
            LoadingDialogWithPercent(activity, progress)
        }

        progress.reset("Пошук робіт")
        loadingDialog?.show()
        progress.setProgressPercent(
            progressPercent = 92f,
            message = "Завантаження робіт в обраному районі",
            durationMillis = 15_000L
        )

        try {
            TablesLoadingUnloading().downloadWPDataWithCords(
                marker.position.latitude,
                marker.position.longitude,
                Runnable {
                    additionalWorksSearchOrigin = marker.position
                    AdditionalWorksMapSearchLocationHolder.set(
                        latitude = marker.position.latitude,
                        longitude = marker.position.longitude
                    )
                    mainViewModel.updateContent()
                    progress.setProgressPercent(
                        progressPercent = 98f,
                        message = "Оновлення карти",
                        durationMillis = 600L
                    )
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        progress.onCompleted()
                        additionalWorksMapSearchInProgress = false
                    }, 700L)
                }
            )
        } catch (e: Throwable) {
            Globals.writeToMLOG(
                "ERROR",
                "MapsDialog/runAdditionalWorksMapSearch",
                "lat=${marker.position.latitude}, lon=${marker.position.longitude}, error=${e.message}"
            )
            progress.onCanceled()
            additionalWorksMapSearchInProgress = false
        }
    }

    LaunchedEffect(mapSearchText, context, isAdditionalWorksMap) {
        if (!isAdditionalWorksMap) {
            mapSearchMarker = null
            mapSearchError = null
            mapSearchSuggestions = emptyList()
            mapSearchLockedText = null
            return@LaunchedEffect
        }

        val query = mapSearchText.trim()
        if (query.isBlank()) {
            mapSearchMarker = null
            mapSearchError = null
            mapSearchSuggestions = emptyList()
            mapSearchLockedText = null
            return@LaunchedEffect
        }

        if (mapSearchLockedText == query) {
            mapSearchSuggestions = emptyList()
            mapSearchError = null
            return@LaunchedEffect
        }

        if (query.length < 3) {
            mapSearchMarker = null
            mapSearchError = null
            mapSearchSuggestions = emptyList()
            return@LaunchedEffect
        }

        mapSearchMarker = null
        mapSearchError = null
        delay(650)
        val searchCenter = currentMapSearchCenter(
            cameraTarget = cameraController.position.target,
            mapCenter = mapState.center?.pos,
            userLat = mapState.userLat,
            userLon = mapState.userLon
        )
        val suggestions = findMapSearchSuggestions(
            context = context,
            query = query,
            center = searchCenter,
            sessionToken = mapSearchSessionToken
        )
        if (mapSearchText.trim() != query || mapSearchLockedText == query) return@LaunchedEffect

        mapSearchSuggestions = suggestions
        mapSearchError = if (suggestions.isEmpty()) {
            mainViewModel.getTranslateString("Адресу не знайдено")
        } else {
            null
        }
    }

    if (isAdditionalWorksMap && pendingAdditionalWorksSearchMarker != null) {
        val marker = pendingAdditionalWorksSearchMarker!!
        MessageDialog(
            title = "Пошук робіт",
            subTitle = marker.title,
            status = DialogStatus.NORMAL,
            message = "Встановити цю адресу як точку відліку та шукати роботи в цьому районі?",
            okButtonName = "Так",
            cancelButtonName = "Ні",
            onDismiss = { pendingAdditionalWorksSearchMarker = null },
            onConfirmAction = {
                pendingAdditionalWorksSearchMarker = null
                runAdditionalWorksMapSearch(marker)
            },
            onCancelAction = {
                pendingAdditionalWorksSearchMarker = null
            }
        )
    }


    LaunchedEffect(vm, sessionId) {
        effects.collectLatest { e ->
            when (e) {
                is MapEffect.MoveCamera -> {
                    if (e.sessionId != sessionId) return@collectLatest  // 👈 игнор чужих
                    val latLngs = e.latLngs
                    try {
                        if (latLngs.size >= 2) {
                            val builder = LatLngBounds.builder()
                            latLngs.forEach { builder.include(it) }
                            cameraController.animate(
                                CameraUpdateFactory.newLatLngBounds(builder.build(), e.padding)
                            )
                        } else if (latLngs.size == 1) {
                            cameraController.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLngs.first(),
                                    e.zoomIfSingle ?: 14f
                                )
                            )
                        }
                    } catch (_: Throwable) {
                    }
                }

                is MapEffect.OpenContextMenu -> onOpenContextMenu(e.wp, e.contextUI, e.option)
                is MapEffect.ShowConfirm -> {
// confirm dialog is drawn below from state; nothing to do here
                }
            }
        }
    }
    // Подаём вход — но запрещаем авто-фит на SetInput
    LaunchedEffect(
        uiState.items,
        uiState.filters,
        uiState.sortingFields,
        uiState.groupingFields,
        mainViewModel.rangeDataStart.value,
        mainViewModel.rangeDataEnd.value,
        uiState.filters?.searchText,
        Globals.CoordX,
        Globals.CoordY,
        additionalWorksSearchOrigin,
        isAdditionalWorksMap,
        vm,
        sessionId,
        distance,
        isCustomAditionalAddressMap,
        customAditionalAddressMapDistance
    ) {
        vm.process(
            MapIntent.SetInput(
                items = uiState.items,
                filters = uiState.filters,
                sorting = uiState.sortingFields,
                grouping = uiState.groupingFields,
                rangeStartLocalDate = mainViewModel.rangeDataStart.value,
                rangeEndLocalDate = mainViewModel.rangeDataEnd.value,
                search = uiState.filters?.searchText,
                userLat = if (isAdditionalWorksMap) {
                    additionalWorksSearchOrigin?.latitude ?: Globals.CoordX
                } else {
                    Globals.CoordX
                },
                userLon = if (isAdditionalWorksMap) {
                    additionalWorksSearchOrigin?.longitude ?: Globals.CoordY
                } else {
                    Globals.CoordY
                },
                distanceMeters = if (isCustomAditionalAddressMap) {
                    customAditionalAddressMapDistance
                } else if (isAdditionalWorksMap) {
                    distance
                } else {
                    null
                },
                autoCenterOnSetInput = false
            )
        )
    }

//    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(vertical = 40.dp)
            .background(Color.Transparent)
    ) {
        Row(modifier = Modifier.align(Alignment.End)) {

            ImageButton(
                id = R.drawable.ic_question_1,
                shape = CircleShape,
                colorImage = ColorFilter.tint(Color.Gray),
                sizeButton = 40.dp,
                sizeImage = 23.dp,
                modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                onClick = { showToolTip = true }
            )

            ua.com.merchik.merchik.features.maps.presentation.main.ImageButton(
                id = if (isActiveFiltered) R.drawable.ic_filterbold else R.drawable.ic_filter,
                shape = CircleShape,
                colorImage = ColorFilter.tint(Color.Gray),
                sizeButton = 40.dp,
                sizeImage = 23.dp,
                modifier = Modifier
                    .padding(start = 15.dp, bottom = 10.dp)
                    .captureBoundsInScreen { filterBtnRect = it },
                onClick = { showFilteringDialog = true }
            )
            ImageButton(
                id = R.drawable.ic_letter_x,
                shape = CircleShape,
                colorImage = ColorFilter.tint(Color.Gray),
                sizeButton = 40.dp,
                sizeImage = 25.dp,
                modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
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
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = mainViewModel.getTranslateString("Карта", 8763),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Text(
                        text = effectiveMapSubheaderText,
                        maxLines = maxLinesSubTitle,
                        overflow = TextOverflow.Ellipsis,
                        color = if ((mainViewModel.typeWindow ?: "").equals(
                                "container",
                                true
                            )
                        ) Color.DarkGray else Color.Black,
                        textDecoration = if (maxLinesSubTitle == 1) TextDecoration.Underline else null,
                        modifier = Modifier
                            .padding(start = 1.dp, bottom = 4.dp, end = 1.dp)
                            .clickable {
                                maxLinesSubTitle = if (maxLinesSubTitle == 1) 99 else 1
                            }
                    )
                }


//                    CollapsibleSubtitle(text = mapSubheaderText)
                Spacer(Modifier.height(8.dp))

                if (isAdditionalWorksMap) {
                    TextFieldInputRounded(
                        viewModel = mainViewModel,
                        value = mapSearchText,
                        onValueChange = {
                            mapSearchLockedText = null
                            mapSearchText = it
                        },
                        suggestions = mapSearchSuggestions.map { suggestion ->
                            TextFieldInputRoundedSuggestion(
                                id = suggestion.id,
                                title = suggestion.title,
                                subtitle = suggestion.subtitle
                            )
                        },
                        onSuggestionClick = { selectedUiSuggestion ->
                            val selectedSuggestion = mapSearchSuggestions
                                .firstOrNull { it.id == selectedUiSuggestion.id }
                            if (selectedSuggestion != null) {
                                val selectedText = selectedSuggestion.fullText

                                mapSearchLockedText = selectedText
                                mapSearchText = selectedText
                                mapSearchSuggestions = emptyList()
                                mapSearchError = null

                                scope.launch {
                                    val searchCenter = currentMapSearchCenter(
                                        cameraTarget = cameraController.position.target,
                                        mapCenter = mapState.center?.pos,
                                        userLat = mapState.userLat,
                                        userLon = mapState.userLon
                                    )
                                    val marker = resolveMapSearchSuggestion(
                                        context = context,
                                        suggestion = selectedSuggestion,
                                        center = searchCenter,
                                        sessionToken = mapSearchSessionToken
                                    )
                                    if (mapSearchLockedText != selectedText) return@launch

                                    if (marker != null) {
                                        setAdditionalWorksSearchMarker(marker)
                                    } else {
                                        mapSearchMarker = null
                                        mapSearchError =
                                            mainViewModel.getTranslateString("Адресу не знайдено")
                                    }
                                    mapSearchSessionToken = UUID.randomUUID().toString()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(8.dp))
                    )

                    mapSearchError?.takeIf { it.isNotBlank() }?.let { errorText ->
                        Text(
                            text = errorText,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(top = 6.dp, start = 2.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                ) {
                    StoresMap(
                        cameraPositionState = cameraController,
                        vm = vm,
                        searchMarker = if (isAdditionalWorksMap) mapSearchMarker else null,
                        onMapClick = if (isAdditionalWorksMap) {
                            { latLng ->
                                val clickedMarker = createAdditionalWorksSearchMarker(
                                    position = latLng,
                                    title = formatMapPoint(latLng)
                                )
                                setAdditionalWorksSearchMarker(clickedMarker)
                                scope.launch {
                                    val markerWithAddress = createAdditionalWorksSearchMarker(
                                        position = latLng,
                                        title = findAddressTitleByLatLng(context, latLng)
                                            ?: formatMapPoint(latLng)
                                    )
                                    if (mapSearchMarker?.position.sameLatLng(latLng)) {
                                        setAdditionalWorksSearchMarker(markerWithAddress)
                                    }
                                }
                            }
                        } else {
                            null
                        },
                        onSearchAreaClick = if (isAdditionalWorksMap) {
                            { marker ->
                                pendingAdditionalWorksSearchMarker = marker
                            }
                        } else {
                            null
                        },
                        focusUserRadiusMeters = if (isCustomAditionalAddressMap) {
                            customAditionalAddressMapDistance
                        } else {
                            null
                        }
                    )
                }


                Spacer(Modifier.height(8.dp))
            }
        }
    }

    AnchoredAnimatedDialog(
        visible = showFilteringDialog,
        anchorRect = filterBtnRect,
        onDismissRequest = { showFilteringDialog = false }
    ) { requestClose ->
        FilteringDialog(
            mainViewModel,
            onDismiss = requestClose,
            onChanged = {
                mainViewModel.updateFilters(it)
                showFilteringDialog = false
                mainViewModel.updateContent()

            },
            true
        )
    }


    if (showToolTip) {

        // Диалог «Довідка»: subTitle/Message — динамические
        MessageDialog(
            title = "Довідка",
            status = DialogStatus.NORMAL,
            subTitle = infoSubtitle,   // ← заголовок-сабтайтл по сценарию
            message = helpMessage,     // ← тело подсказки по сценарию
            onDismiss = { showToolTip = false },
            okButtonName = "Ok",
            onConfirmAction = { showToolTip = false }
        )
    }

}

private data class MapSearchPrediction(
    val id: String,
    val title: String,
    val subtitle: String?,
    val fullText: String,
    val placeId: String? = null,
    val position: LatLng? = null
)

private suspend fun findMapSearchSuggestions(
    context: Context,
    query: String,
    center: LatLng?,
    sessionToken: String
): List<MapSearchPrediction> = withContext(Dispatchers.IO) {
    val placesSuggestions = findPlacesAutocompleteSuggestions(
        context = context,
        query = query,
        center = center,
        sessionToken = sessionToken
    )
    if (placesSuggestions.isNotEmpty()) return@withContext placesSuggestions

    findGeocoderSuggestions(
        context = context,
        query = query,
        center = center
    )
}

private suspend fun resolveMapSearchSuggestion(
    context: Context,
    suggestion: MapSearchPrediction,
    center: LatLng?,
    sessionToken: String
): MapSearchMarker? = withContext(Dispatchers.IO) {
    suggestion.position?.let { position ->
        return@withContext createAdditionalWorksSearchMarker(
            position = position,
            title = suggestion.fullText
        )
    }

    val placeMarker = suggestion.placeId?.let { placeId ->
        findPlaceDetailsMarker(
            context = context,
            placeId = placeId,
            fallbackTitle = suggestion.fullText,
            sessionToken = sessionToken
        )
    }
    if (placeMarker != null) return@withContext placeMarker

    findGeocoderSuggestions(
        context = context,
        query = suggestion.fullText,
        center = center
    ).firstOrNull()?.position?.let { position ->
        createAdditionalWorksSearchMarker(
            position = position,
            title = suggestion.fullText
        )
    }
}

private fun findPlacesAutocompleteSuggestions(
    context: Context,
    query: String,
    center: LatLng?,
    sessionToken: String
): List<MapSearchPrediction> {
    val apiKey = readGoogleMapsApiKey(context) ?: return emptyList()

    return try {
        val urlBuilder = "https://maps.googleapis.com/maps/api/place/autocomplete/json"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter("input", query)
            .addQueryParameter("key", apiKey)
            .addQueryParameter("types", "geocode")
            .addQueryParameter("language", mapSearchLanguage())
            .addQueryParameter("sessiontoken", sessionToken)

        center?.takeIf { it.isUsefulMapSearchPoint() }?.let {
            urlBuilder
                .addQueryParameter("location", "${it.latitude},${it.longitude}")
                .addQueryParameter("radius", MAP_SEARCH_BIAS_RADIUS_METERS.toInt().toString())
        }

        mapSearchHttpClient.newCall(
            Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build()
        ).execute().use { response ->
            if (!response.isSuccessful) {
                Globals.writeToMLOG(
                    "ERROR",
                    "MapsDialog/findPlacesAutocompleteSuggestions",
                    "http=${response.code}, query=$query"
                )
                return emptyList()
            }

            val body = response.body?.string().orEmpty()
            val root = JsonParser.parseString(body).asJsonObject
            val status = root.optString("status")
            if (status != "OK") {
                if (status != "ZERO_RESULTS") {
                    Globals.writeToMLOG(
                        "ERROR",
                        "MapsDialog/findPlacesAutocompleteSuggestions",
                        "status=$status, error=${root.optString("error_message")}, query=$query"
                    )
                }
                return emptyList()
            }

            root.optArray("predictions")
                .take(MAP_SEARCH_MAX_SUGGESTIONS)
                .mapNotNull { item ->
                    val prediction = item.asJsonObjectOrNull() ?: return@mapNotNull null
                    val placeId = prediction.optString("place_id") ?: return@mapNotNull null
                    val description = prediction.optString("description") ?: return@mapNotNull null
                    val structured = prediction.optObject("structured_formatting")
                    val title = structured?.optString("main_text") ?: description
                    val subtitle = structured?.optString("secondary_text")

                    MapSearchPrediction(
                        id = "places_$placeId",
                        title = title,
                        subtitle = subtitle,
                        fullText = description,
                        placeId = placeId
                    )
                }
        }
    } catch (e: Throwable) {
        Globals.writeToMLOG(
            "ERROR",
            "MapsDialog/findPlacesAutocompleteSuggestions",
            "query=$query, error=${e.message}"
        )
        emptyList()
    }
}

private fun findPlaceDetailsMarker(
    context: Context,
    placeId: String,
    fallbackTitle: String,
    sessionToken: String
): MapSearchMarker? {
    val apiKey = readGoogleMapsApiKey(context) ?: return null

    return try {
        val url = "https://maps.googleapis.com/maps/api/place/details/json"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter("place_id", placeId)
            .addQueryParameter("fields", "geometry,formatted_address,name")
            .addQueryParameter("key", apiKey)
            .addQueryParameter("language", mapSearchLanguage())
            .addQueryParameter("sessiontoken", sessionToken)
            .build()

        mapSearchHttpClient.newCall(
            Request.Builder()
                .url(url)
                .get()
                .build()
        ).execute().use { response ->
            if (!response.isSuccessful) {
                Globals.writeToMLOG(
                    "ERROR",
                    "MapsDialog/findPlaceDetailsMarker",
                    "http=${response.code}, placeId=$placeId"
                )
                return null
            }

            val root = JsonParser.parseString(response.body?.string().orEmpty()).asJsonObject
            val status = root.optString("status")
            if (status != "OK") {
                Globals.writeToMLOG(
                    "ERROR",
                    "MapsDialog/findPlaceDetailsMarker",
                    "status=$status, error=${root.optString("error_message")}, placeId=$placeId"
                )
                return null
            }

            val result = root.optObject("result") ?: return null
            val location = result
                .optObject("geometry")
                ?.optObject("location")
                ?: return null
            val lat = location.optDouble("lat") ?: return null
            val lon = location.optDouble("lng") ?: return null

            createAdditionalWorksSearchMarker(
                position = LatLng(lat, lon),
                title = result.optString("formatted_address")
                    ?: result.optString("name")
                    ?: fallbackTitle
            )
        }
    } catch (e: Throwable) {
        Globals.writeToMLOG(
            "ERROR",
            "MapsDialog/findPlaceDetailsMarker",
            "placeId=$placeId, error=${e.message}"
        )
        null
    }
}

private fun findGeocoderSuggestions(
    context: Context,
    query: String,
    center: LatLng?
): List<MapSearchPrediction> {
    if (!Geocoder.isPresent()) return emptyList()

    try {
        val geocoder = Geocoder(context.applicationContext, Locale.getDefault())
        @Suppress("DEPRECATION")
        val boundedAddresses = center
            ?.takeIf { it.isUsefulMapSearchPoint() }
            ?.let { usefulCenter ->
                val bounds = usefulCenter.toSearchBounds(MAP_SEARCH_BIAS_RADIUS_METERS)
                geocoder.getFromLocationName(
                    query,
                    MAP_SEARCH_MAX_SUGGESTIONS,
                    bounds.south,
                    bounds.west,
                    bounds.north,
                    bounds.east
                )
            }

        @Suppress("DEPRECATION")
        val addresses = boundedAddresses
            ?.takeIf { it.isNotEmpty() }
            ?: geocoder.getFromLocationName(query, MAP_SEARCH_MAX_SUGGESTIONS)

        return addresses
            .orEmpty()
            .mapIndexedNotNull { index, address ->
                val position = LatLng(address.latitude, address.longitude)
                if (!position.isUsefulMapSearchPoint()) return@mapIndexedNotNull null

                val addressLine = runCatching { address.getAddressLine(0) }.getOrNull()
                    ?.takeIf { it.isNotBlank() }
                val title = address.featureName
                    ?.takeIf { it.isNotBlank() }
                    ?: address.thoroughfare
                        ?.takeIf { it.isNotBlank() }
                    ?: addressLine
                    ?: query
                val subtitle = addressLine
                    ?.takeIf { it.isNotBlank() && it != title }
                    ?: listOfNotNull(address.locality, address.adminArea, address.countryName)
                        .filter { it.isNotBlank() && it != title }
                        .distinct()
                        .joinToString(", ")
                        .ifBlank { null }

                MapSearchPrediction(
                    id = "geocoder_${position.latitude}_${position.longitude}_$index",
                    title = title,
                    subtitle = subtitle,
                    fullText = addressLine ?: title,
                    position = position
                )
            }
            .distinctBy { "${it.position?.latitude}_${it.position?.longitude}_${it.fullText}" }
    } catch (e: Throwable) {
        Globals.writeToMLOG(
            "ERROR",
            "MapsDialog/findGeocoderSuggestions",
            "query=$query, error=${e.message}"
        )
        return emptyList()
    }
}

private fun currentMapSearchCenter(
    cameraTarget: LatLng?,
    mapCenter: LatLng?,
    userLat: Double?,
    userLon: Double?
): LatLng? = when {
    cameraTarget.isUsefulMapSearchPoint() -> cameraTarget
    mapCenter.isUsefulMapSearchPoint() -> mapCenter
    isValidLatLon(userLat, userLon) -> LatLng(userLat!!, userLon!!)
    else -> null
}

private fun createAdditionalWorksSearchMarker(
    position: LatLng,
    title: String
): MapSearchMarker =
    MapSearchMarker(
        position = position,
        title = title,
        subtitle = null,
        actionText = MAP_SEARCH_AREA_ACTION_TEXT
    )

private suspend fun findAddressTitleByLatLng(
    context: Context,
    position: LatLng
): String? = withContext(Dispatchers.IO) {
    if (!Geocoder.isPresent()) return@withContext null

    try {
        val geocoder = Geocoder(context.applicationContext, Locale.getDefault())
        @Suppress("DEPRECATION")
        val address = geocoder
            .getFromLocation(position.latitude, position.longitude, 1)
            ?.firstOrNull()
            ?: return@withContext null

        runCatching { address.getAddressLine(0) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(
                address.thoroughfare,
                address.featureName,
                address.locality,
                address.countryName
            )
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(", ")
                .ifBlank { null }
    } catch (e: Throwable) {
        Globals.writeToMLOG(
            "ERROR",
            "MapsDialog/findAddressTitleByLatLng",
            "lat=${position.latitude}, lon=${position.longitude}, error=${e.message}"
        )
        null
    }
}

private fun formatMapPoint(position: LatLng): String =
    String.format(Locale.US, "%.6f, %.6f", position.latitude, position.longitude)

private fun LatLng?.sameLatLng(other: LatLng): Boolean {
    val point = this ?: return false
    return kotlin.math.abs(point.latitude - other.latitude) < 0.000001 &&
            kotlin.math.abs(point.longitude - other.longitude) < 0.000001
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private data class SearchBounds(
    val south: Double,
    val west: Double,
    val north: Double,
    val east: Double
)

private fun LatLng.toSearchBounds(radiusMeters: Double): SearchBounds {
    val latDelta = radiusMeters / 111_320.0
    val cosLat = kotlin.math.abs(kotlin.math.cos(Math.toRadians(latitude))).coerceAtLeast(0.01)
    val lonDelta = radiusMeters / (111_320.0 * cosLat)
    return SearchBounds(
        south = (latitude - latDelta).coerceIn(-90.0, 90.0),
        west = (longitude - lonDelta).coerceIn(-180.0, 180.0),
        north = (latitude + latDelta).coerceIn(-90.0, 90.0),
        east = (longitude + lonDelta).coerceIn(-180.0, 180.0)
    )
}

private fun LatLng?.isUsefulMapSearchPoint(): Boolean {
    val point = this ?: return false
    return point.latitude in -90.0..90.0 &&
            point.longitude in -180.0..180.0 &&
            !(point.latitude == 0.0 && point.longitude == 0.0)
}

private fun mapSearchLanguage(): String =
    Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "uk"

private fun readGoogleMapsApiKey(context: Context): String? =
    runCatching {
        @Suppress("DEPRECATION")
        context.applicationContext.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData
            ?.getString("com.google.android.geo.API_KEY")
    }.getOrNull()
        ?.takeIf { it.isNotBlank() }

private fun JsonObject.optString(name: String): String? =
    get(name)
        ?.takeIf { !it.isJsonNull }
        ?.let { runCatching { it.asString }.getOrNull() }
        ?.takeIf { it.isNotBlank() }

private fun JsonObject.optDouble(name: String): Double? =
    get(name)
        ?.takeIf { !it.isJsonNull }
        ?.let { runCatching { it.asDouble }.getOrNull() }

private fun JsonObject.optObject(name: String): JsonObject? =
    get(name)
        ?.takeIf { !it.isJsonNull }
        ?.asJsonObjectOrNull()

private fun JsonObject.optArray(name: String) =
    get(name)
        ?.takeIf { !it.isJsonNull }
        ?.let { runCatching { it.asJsonArray.toList() }.getOrDefault(emptyList()) }
        ?: emptyList()

private fun com.google.gson.JsonElement.asJsonObjectOrNull(): JsonObject? =
    runCatching { asJsonObject }.getOrNull()


@Composable
fun ImageButton(
    @DrawableRes id: Int,
    shape: Shape = CircleShape,
    colorImage: ColorFilter? = null,
    sizeButton: Dp = 40.dp,
    sizeImage: Dp = 25.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(sizeButton)          // размер области клика/кнопки
            .clip(shape)
            .background(color = Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id),
            contentDescription = null,
            colorFilter = colorImage,
            modifier = Modifier.requiredSize(sizeImage), // <-- ключевое: фиксируем размер картинки
            contentScale = ContentScale.Fit,             // без искажений
            alignment = Alignment.Center
        )
    }
}
