package ua.com.merchik.merchik.features.maps.presentation.main


import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.SortingField
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import ua.com.merchik.merchik.features.maps.domain.parseDoubleSafe
import ua.com.merchik.merchik.features.maps.domain.stringByKey
import ua.com.merchik.merchik.features.maps.presentation.MapActionsBridge
import ua.com.merchik.merchik.features.maps.presentation.MapEffect
import ua.com.merchik.merchik.features.maps.presentation.MapIntent
import ua.com.merchik.merchik.features.maps.presentation.viewModels.BaseMapViewModel
import ua.com.merchik.merchik.features.maps.presentation.viewModels.MapFromMapsViewModel
import ua.com.merchik.merchik.features.maps.presentation.viewModels.MapFromWPdataViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun MapsDialog(
    mainViewModel: MainViewModel,
    onDismiss: () -> Unit,
    onOpenContextMenu: (WpDataDB, ContextUI) -> Unit
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val contextUI = mainViewModel.contextUI

    val distance = mainViewModel.offsetDistanceMeters.value

    val highlightColor: Color = colorResource(id = R.color.ufmd_accept_t)

    // 1) генерим id сессии диалога (константа на время жизни composable)
    val sessionId = remember { System.currentTimeMillis() }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showToolTip by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()  // <-- добавили

    var maxLinesSubTitle by remember { mutableStateOf(1) }

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

    if (mainViewModel.contextUI == ContextUI.WP_DATA_ADDITIONAL_IN_CONTAINER)
        vm.setDistance(distance)

    // 1) Подписываемся на состояние карты
    val mapState by vm.state.collectAsState()


    val allUserIs14041 by remember(mapState.pointsUi) {
        derivedStateOf {
            val pts = mapState.pointsUi
            pts.isNotEmpty() && pts.all { it.point.wp?.user_id == 14041 }
        }
    }

    LaunchedEffect(vm, onDismiss, contextUI, highlightColor, sessionId) {
        vm.attachBridge(object : MapActionsBridge {
            override val contextUI: ContextUI = contextUI
            override val highlightColor: Color = highlightColor
            override fun requestScrollToVisit(stableId: Long) =
                mainViewModel.requestScrollToVisit(stableId)

            override fun highlightByAddrId(addrId: String, color: Color) =
                mainViewModel.highlightByAddrId(addrId, color)

            override fun dismissHost() = onDismiss()
        })
        vm.process(MapIntent.Init(sessionId)) // 👈 важный вызов
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

    val periodEnd = remember(uiState.filters?.rangeDataByKey) {
        uiState.filters?.rangeDataByKey?.let { range ->
            range.end?.format(formatterDDmmYYYY)
        } ?: "не визначено"
    }

    // время для fromMaps
    val validTime = 1_800_000L // 30 минут в миллисекундах

    val wpDataDB: WpDataDB? = try {
        Gson().fromJson(mainViewModel.dataJson, WpDataDB::class.java)
    } catch (e: Exception) {
        null
    }

// определяем время
    val startTime = remember {
//        if (!isRnoUserOnMap) return@remember uiState.filters?.rangeDataByKey?.start?.let {
//            Instant.ofEpochMilli(it.toEpochDay())
//                .atZone(ZoneId.systemDefault())
//                .format(formatterHHdd_DDmmYYYY)
//        }


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
//        if (!isRnoUserOnMap) return@remember uiState.filters?.rangeDataByKey?.end?.let {
//            Instant.ofEpochMilli(it.toEpochDay())
//                .atZone(ZoneId.systemDefault())
//                .format(formatterHHdd_DDmmYYYY)
//        }

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

                "На карте, зелеными маркерами помечены адреса, в которых есть возможность выполнять работы за отдельную (дополнительную) оплату. " +
                        "Числа внутри маркеров обозначают количество доступных работ по указанному адресу. " +
                        "Синим отмечено ваше текущее местоположение (МП). " +
                        "Чтобы узнать подробнее про работы (и оплату за их выполнение) по конкретному адресу, кликните на соответствующем маркере."
            )

            // 2) FromMaps — “Опція МП та Відвідування (Головна)”
            isFromMaps -> Triple(
                "Опція МП та Відвідування (Головна).",
                "История местоположений пользователя за период с $startTime по $endTime. Для получения дополнительной информации нажмите иконку «?» вверху текущей формы.",
                "На карте, синим маркером отмечено текущее местоположение пользователя. " +
                        "Зеленым маркером отмечен адрес — место работ. " +
                        "Зелёными (красными) точками отмечено РЕАЛЬНОЕ местоположение пользователя. " +
                        "Точки соединены линией, чтобы можно было проследить маршрут движения. " +
                        "Зеленые точки — нет замечаний (находился по адресу с учётом погрешности), " +
                        "красные — за пределами адреса выполнения работ. " +
                        "Чтобы узнать больше по конкретному месту, кликните на маркер (или точку)."
            )

            // 3) FromWPdata + другой пользователь — “План Робіт”
            else -> Triple(
                "План Робіт.",
                "Адреса, в которых выполняются работы за период с $periodStrt по $periodEnd. Для получения дополнительной информации нажмите иконку «?» вверху текущей формы.",
                "На карте зелёными маркерами помечены адреса, в которых запланировано выполнение работ. " +
                        "Числа внутри маркеров обозначают количество запланированных работ по указанному адресу. " +
                        "Синим отмечено ваше текущее местоположение (МП). " +
                        "Чтобы узнать подробнее о работах по конкретному адресу, кликните на соответствующем маркере."
            )
        }
    }


    // Effects handling
    val effects = vm.effects
    val cameraController = rememberCameraPositionState()


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

                is MapEffect.OpenContextMenu -> onOpenContextMenu(e.wp, e.contextUI)
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
        vm,
        sessionId
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
                userLat = Globals.CoordX,
                userLon = Globals.CoordY,
                autoCenterOnSetInput = false      // <-- ключевая строчка
            )
        )
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
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

                ImageButton(
                    id = R.drawable.ic_settings_empt,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                    onClick = { showSettingsDialog = true }
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
                            text = mapSubheaderText,
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


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                    ) {
                        StoresMap(
                            cameraPositionState = cameraController,
                            vm = vm
                        )
                    }


                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
    // Confirm Dialog (bound to VM state)
    val state by vm.state.collectAsState()
    if (state.pendingWp != null) {
        val wp = state.pendingWp
        val sortingFieldAdr = remember {
            SortingField(
                "addr_txt",
                mainViewModel.getTranslateString("Адреса", 1101),
                1
            )
        }
        val sortingFieldDate =
            remember { SortingField("dt", mainViewModel.getTranslateString("Дата", 1100), 1) }
        val periodDate = remember(uiState.filters?.rangeDataByKey) {
            uiState.filters?.rangeDataByKey?.let { range ->
                val start =
                    range.start?.format(formatterDDmmYYYY)
                        ?: "?"
                val end =
                    range.end?.format(formatterDDmmYYYY)
                        ?: "?"
                "$start по $end"
            } ?: "не определено"
        }

        MessageDialog(
            title = "Перейти к посещенням",
            status = DialogStatus.NORMAL,
            subTitle = wp?.addr_txt,
            message = "Показать все работы по этому адресу за период с $periodDate?",
            onDismiss = { vm.process(MapIntent.DismissConfirm) },
            onConfirmAction = {

                scope.launch {
                    // 1) Сначала отправляем изменения в MainViewModel
                    mainViewModel.updateSorting(sortingFieldAdr, 0)
                    mainViewModel.updateSorting(sortingFieldDate, 1)
                    // (если нужно, можно подождать кадр)
//                     yield() // kotlinx.coroutines.yield()

                    // 2) Потом закрываем карту и запускаем подсветку/скролл
                    vm.process(MapIntent.ConfirmJump)
                }
            },
            onCancelAction = { vm.process(MapIntent.DismissConfirm) }
        )
    }

    if (showSettingsDialog) {
        if (allUserIs14041)
            MapRadiusDialog(
                vm = vm,                          // твой BaseMapViewModel
                mainViewModel = mainViewModel,    // чтобы использовать переводы/цвета, как в SettingsDialog
                onDismiss = { showSettingsDialog = false }
            )
        else
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