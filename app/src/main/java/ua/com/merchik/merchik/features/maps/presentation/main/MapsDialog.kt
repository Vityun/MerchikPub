package ua.com.merchik.merchik.features.maps.presentation.main


import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest
import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.database.room.factory.WPDataAdditionalFactory
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.Main.AnchoredAnimatedDialog
import ua.com.merchik.merchik.features.main.Main.FilteringDialog
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.captureBoundsInScreen
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import ua.com.merchik.merchik.features.maps.data.mappers.WpSelectionDataHolder
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

    var showToolTip by remember { mutableStateOf(false) }
    var notReadyMenu by remember { mutableStateOf(false) }

    var showToolTipKostil by remember { mutableStateOf(false) }

    var showFilteringDialog by remember { mutableStateOf(false) }
    var filterBtnRect by remember { mutableStateOf<Rect?>(null) }

    var isActiveFiltered by remember { mutableStateOf(true) }

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

    val holder = WpSelectionDataHolder.instance()
    val version = holder.version
    val wpDataList = remember { mutableStateListOf<WpDataDB>() }


    LaunchedEffect(version) {
        val wpList = holder.consumePendingSelected()
        if (wpList.isNotEmpty()) {
            wpDataList.clear()
            wpDataList.addAll(wpList)

            Toast.makeText(
                mainViewModel.context,
                "Знайдено результатів: ${wpDataList.size}",
                Toast.LENGTH_LONG
            ).show()

            if (wpDataList.size == 1)
                MessageDialogBuilder(mainViewModel.context as Activity)
                    .setTitle("Додатковий заробіток")
                    .setStatus(DialogStatus.NORMAL)
                    .setSubTitle(wpDataList.first().addr_txt)
                    .setMessage(String.format(
                        "Подать заявку на выполнение этих работ\n" +
                                "<font color='gray'>Відвідування від</font> %s" +
                                "<br><font color='gray'>Клієнт:</font> %s" +
                                "<br><font color='gray'>Адреса:</font> %s" +
                                "<br><font color='gray'>Премія (план):</font> %s грн." +
                                "<br><font color='gray'>СКЮ (кількість товарних позицій):</font> %s" +
                                "<br><font color='gray'>Середній час роботи:</font> %s хв",
                        Clock.getHumanTime_dd_MMMM(wpDataList.first().dt.time),
                        wpDataList.first().client_txt,
                        wpDataList.first().addr_txt,
                        wpDataList.first().cash_ispolnitel,
                        wpDataList.first().sku,
                        wpDataList.first().duration
                    ))
                    .setOnConfirmAction("Выполнять всегда") {
                        notReadyMenu = true
                    }
                    .setOnCancelAction("Выполнить один раз") {
                        mainViewModel.doAcceptOneTime(wp = wpDataList.first())
                    }
                    .show()
            else
                MessageDialogBuilder(mainViewModel.context as Activity)
                    .setTitle("Додатковий заробіток")
                    .setStatus(DialogStatus.NORMAL)
                    .setSubTitle(wpDataList.first().addr_txt)
                    .setMessage( "Подати заявку на виконання обранних ${wpDataList.size} робiт за цією адресою?"
                    )
                    .setOnConfirmAction("Выполнять всегда") {
                        notReadyMenu = true
                    }
                    .setOnCancelAction("Выполнить один раз") {
                        notReadyMenu = true
                    }
                    .show()
        }
    }

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

//    val end by mainViewModel.rangeDataEnd.collectAsState()
//    val periodEnd = end?.format(formatterDDmmYYYY) ?: "не визначено"

    val periodEnd = mainViewModel.rangeDataEnd.value
        ?.format(formatterDDmmYYYY)
        ?: "не визначено"

//    val periodEnd = remember { mainViewModel.rangeDataEnd.value?.let {
//        it.format(formatterDDmmYYYY) ?:  "не визначено"}
//    } ?: "не визначено"
//    val periodEnd = remember(uiState.filters?.rangeDataByKey) {
//        uiState.filters?.rangeDataByKey?.let { range ->
//            range.end?.format(formatterDDmmYYYY)
//        } ?: "не визначено"
//    }

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
//    }
    // Confirm Dialog (bound to VM state)
    val state by vm.state.collectAsState()
//    if (state.pendingWp != null) {
//        val wp = state.pendingWp
//        val sortingFieldAdr = remember {
//            SortingField(
//                "addr_txt",
//                mainViewModel.getTranslateString("Адреса", 1101),
//                1
//            )
//        }
//        val groupingFieldAdr = remember {
//            GroupingField(
//                key = "addr_txt",
//                title = mainViewModel.getTranslateString("Адреса", 1101),
//                priority = 1,
//                collapsedByDefault = false
//            )
//        }
//        val sortingFieldDate =
//            remember {
//                SortingField("dt", mainViewModel.getTranslateString("Дата", 1100), 1)
//            }
//        val groupingFieldDate = remember {
//            GroupingField(
//                key = "dt",
//                title = mainViewModel.getTranslateString("Дата", 1100),
//                priority = 1,
//                collapsedByDefault = false
//            )
//        }
//        val periodDate = remember(uiState.filters?.rangeDataByKey) {
//            uiState.filters?.rangeDataByKey?.let { range ->
//                val start =
//                    range.start?.format(formatterDDmmYYYY)
//                        ?: "?"
//                val end =
//                    range.end?.format(formatterDDmmYYYY)
//                        ?: "?"
//                "$start по $end"
//            } ?: "не определено"
//        }
//
//        MessageDialog(
//            title = "Перейти к посещенням",
//            status = DialogStatus.NORMAL,
//            subTitle = wp?.addr_txt,
//            message = "Показать все работы по этому адресу за период с $periodDate?",
//            onDismiss = { vm.process(MapIntent.DismissConfirm) },
//            onConfirmAction = {
//
//                scope.launch {
//                    // 1) Сначала отправляем изменения в MainViewModel
//                    mainViewModel.updateSorting(sortingFieldAdr, 0)
//                    mainViewModel.updateSorting(sortingFieldDate, 1)
//                    mainViewModel.updateGrouping(groupingFieldAdr, 0)
//                    mainViewModel.updateGrouping(groupingFieldDate, 1)
//                    wp?.let {
//                        val updated = uiState.filters?.copy(searchText = it.addr_txt)
//                            ?: Filters(searchText = it.addr_txt)
//                        mainViewModel.updateFilters(updated)
//                    }
//                    vm.process(MapIntent.ConfirmJump)
//                    delay(500)
//                    showToolTipKostil = true
//                    mainViewModel.showKostilDialog()
//                }
//            },
//            onCancelAction = { vm.process(MapIntent.DismissConfirm) }
//        )
//    }

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
