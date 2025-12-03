package ua.com.merchik.merchik.features.main.Main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.common.rememberImeVisible
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import ua.com.merchik.merchik.features.main.componentsUI.ImageWithText
import ua.com.merchik.merchik.features.main.componentsUI.MessageDialogData
import ua.com.merchik.merchik.features.main.componentsUI.RoundCheckbox
import ua.com.merchik.merchik.features.main.componentsUI.TextFieldInputRounded
import ua.com.merchik.merchik.features.main.componentsUI.TextInStrokeCircle
import ua.com.merchik.merchik.features.main.componentsUI.Tooltip
import ua.com.merchik.merchik.features.main.componentsUI.rememberContextMenuHost
import java.io.File
import kotlin.math.roundToInt


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun MainUI(modifier: Modifier, viewModel: MainViewModel, context: Context) {

    val uiState by viewModel.uiState.collectAsState()

    val focusManager = LocalFocusManager.current

    var isActiveFiltered by remember { mutableStateOf(false) }
    var isActiveSorted by remember { mutableStateOf(false) }
    var isActiveGrouped by remember { mutableStateOf(false) }

    var showAdditionalContent by remember { mutableStateOf(false) }

    var showSettingsDialog by remember { mutableStateOf(false) }

    var showSortingDialog by remember { mutableStateOf(false) }

    var showFilteringDialog by remember { mutableStateOf(false) }

    var showMapsDialog by remember { mutableStateOf(false) }

    var showMessageDialog by remember { mutableStateOf<MessageDialogData?>(null) }

    val offsetSizeFont by viewModel.offsetSizeFonts.collectAsState()

    var maxLinesSubTitle by remember { mutableStateOf(1) }

    val listState = rememberLazyListState()

    // действие по клику
    val menu = rememberContextMenuHost(viewModel, context)

    var searchFocused by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val imeVisible by rememberImeVisible()

//    val dataItemsUI = remember { mutableStateListOf<DataItemUI>() }
//                val dataItemsUI = mutableListOf<DataItemUI>()

//        ## проверим, как будет работать без него
    val dataItemsUI_ by viewModel.dataItems.collectAsState()


    var flying by remember { mutableStateOf<Flying<DataItemUI>?>(null) }
    // === 2) Новый режим: призрак для перетаскивания + сжатие при отпускании ===
    var dragging by remember { mutableStateOf<Dragging<DataItemUI>?>(null) }
    var shrinking by remember { mutableStateOf<Shrinking<DataItemUI>?>(null) }

    // Какой элемент скрываем в списке (во время полёта)
    var disappearingKey by remember { mutableStateOf<Any?>(null) }

    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current

    // Лаунчер на результат
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.updateContent()
        }
    }

    var pendingId by remember { mutableStateOf<Long?>(null) }

    // Ловим запросы скролла и кладём «ожидание» в локальный стейт
    LaunchedEffect(Unit) {
        viewModel.scrollToHash.collectLatest { h -> pendingId = h }
    }

    // сколько элементов сверху не из основного списка (хедеры/stickyHeader и т.п.)
    val headerCount = 0
    // хотим зазор сверху после скролла
    val topOffsetPx = -with(LocalDensity.current) { 38.dp.roundToPx() }

    // На кнопку Back тоже корректно сворачиваем
    BackHandler(enabled = searchFocused) {
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
    }

    // как только клавиатура скрылась — снимаем фокус → панель схлопнется
    LaunchedEffect(imeVisible) {
        if (!imeVisible && searchFocused) {
            focusManager.clearFocus(force = true)
        }
    }

    viewModel.launcher = launcher

    // Карта stableId -> LayoutCoordinates (MutableStateMap чтобы изменения триггерили recomposition при необходимости)
    val coordsMap = remember { mutableStateMapOf<Long, LayoutCoordinates>() }

    // Состояние летающей копии (null = нет анимации)
//    var flying by remember { mutableStateOf<Flying?>(null) }

    // Слушаем запросы на полёт от ViewModel
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // Collect fly requests from ViewModel
            viewModel.flyRequests.collect { stableId ->
                // Найти координаты по stableId
                val coords = coordsMap[stableId]
                coords?.let { c ->
                    if (c.isAttached) {
                        // получаем позицию и размер
                        val pos = coords.positionInRoot()
                        val size = coords.size

                        // создаём Flying (можно модифицировать параметры)
                        flying = Flying(
                            item = dataItemsUI_.firstOrNull { it.stableId == stableId }
                                ?: return@collect,
                            startOffset = IntOffset(pos.x.roundToInt(), pos.y.roundToInt()),
                            size = size
                        )
                    } else {
                        // Если координат нет (элемент не отрисован), можно:
                        // 1) игнорировать, 2) отложить и попробовать позже, 3) скроллить к позиции (если ты знаешь индекс).
                        // Для простоты — игнорируем. Можно логировать:
                        Log.d("MainUI", "requestFly no coords for stableId=$stableId")
                    }
                }

            }
        }
    }


    var isCollapsed by rememberSaveable { mutableStateOf(true) }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
    ) {

        if (!(viewModel.typeWindow ?: "").equals("full", true) &&
            !(viewModel.typeWindow ?: "").equals("container", true)
        ) {
            TopButton(
                modifier = Modifier
                    .statusBarsPadding()
                    .align(alignment = Alignment.End),
                onSettings = { showSettingsDialog = true },
                onRefresh = { viewModel.updateContent() },
                onClose = { (context as? Activity)?.finish() }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(
                    if (!(viewModel.typeWindow ?: "").equals(
                            "container",
                            true
                        )
                    ) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
                )
                .background(color = colorResource(id = R.color.main_form))
        ) {
            val visibilityColumName =
                if (uiState.settingsItems.firstOrNull { it.key == "column_name" }?.isEnabled == true) View.VISIBLE else View.GONE

            val visibilityHeaderGroupName =
                uiState.settingsItems.firstOrNull { it.key == "group_header" }?.isEnabled == true

            Column {

                if ((viewModel.typeWindow ?: "").equals("full", true)) {
                    TopButton(
                        modifier = Modifier
                            .align(alignment = Alignment.End)
                            .padding(top = 10.dp, end = 10.dp),
                        onSettings = { showSettingsDialog = true },
                        onRefresh = { viewModel.updateContent() },
                        onClose = { (context as? Activity)?.finish() }
                    )
                    HorizontalDivider()
                } else if ((viewModel.typeWindow ?: "").equals("container", true)) {
                    Spacer(modifier = Modifier.padding(4.dp))
                }

//                val dataItemsUI = mutableListOf<DataItemUI>()
//                dataItemsUI.addAll(uiState.itemsHeader)
//                dataItemsUI.addAll(result.items)
//                dataItemsUI.addAll(uiState.itemsFooter)

                // 1. Берём шапку/подвал из uiState
                val headerItems = uiState.itemsHeader
                val footerItems = uiState.itemsFooter

// 2. Гоним ТОЛЬКО тело в filterAndSortDataItems
                val result = filterAndSortDataItems(
                    items = uiState.items,                 // <-- только body
                    filters = uiState.filters,
                    sortingFields = uiState.sortingFields,
                    groupingFields = uiState.groupingFields,
                    rangeStart = viewModel.rangeDataStart.value,
                    rangeEnd = viewModel.rangeDataEnd.value,
                    searchText = uiState.filters?.searchText
                )

                isActiveFiltered = result.isActiveFiltered
                isActiveSorted = result.isActiveSorted
                isActiveGrouped = result.isActiveGrouped

// 3. Собираем итоговый список для LazyColumn: header + сгруппированное тело + footer
                val dataItemsUI = remember(headerItems, result.items, footerItems) {
                    buildList {
                        addAll(headerItems)
                        addAll(result.items)
                        addAll(footerItems)
                    }
                }

// 4. Сдвигаем индексы групп на размер header
                val groups: List<GroupMeta> = remember(result.groups, headerItems.size) {
                    val headerSize = headerItems.size
                    result.groups.map { g ->
                        g.copy(
                            startIndex = g.startIndex + headerSize,
                            endIndexExclusive = g.endIndexExclusive + headerSize
                        )
                    }
                }


                uiState.title?.let {
                    Text(
                        text = it, fontSize = (16 + offsetSizeFont).sp, modifier = Modifier
                            .padding(start = 10.dp, bottom = 7.dp, end = 10.dp, top = 10.dp)
                            .align(Alignment.CenterHorizontally),
                        fontWeight = FontWeight.Bold
                    )
                }

                uiState.idResImage?.let {
                    Image(
                        painter = painterResource(it),
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Inside,
                        contentDescription = null
                    )
                }

//                uiState.let {
//                    CollapsibleSubtitle(uiState = it, viewModel = viewModel)
//                }
                uiState.subTitle?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Text(
                            text = if (viewModel.contextUI == ContextUI.WP_DATA_IN_CONTAINER
                                && maxLinesSubTitle == 99
                            ) uiState.subTitleLong ?: it else it,


                            maxLines = maxLinesSubTitle,
                            overflow = TextOverflow.Ellipsis,
                            color = if ((viewModel.typeWindow ?: "").equals(
                                    "container",
                                    true
                                )
                            ) Color.DarkGray else Color.Black,
                            textDecoration = if (maxLinesSubTitle == 1) TextDecoration.Underline else null,
                            modifier = Modifier
                                .padding(start = 10.dp, bottom = 7.dp, end = 10.dp)
                                .clickable {
                                    maxLinesSubTitle = if (maxLinesSubTitle == 1) 99 else 1
                                }
                        )
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, bottom = 10.dp, end = 10.dp)
                        // плавная перестройка строки при появлении/исчезновении кнопок
                        .animateContentSize(animationSpec = tween(durationMillis = 220))
                ) {

                    // Поле ввода — всегда стоит первым, а при фокусе перекрывает остальных
                    TextFieldInputRounded(
                        viewModel = viewModel,
                        value = uiState.filters?.searchText ?: "",
                        onValueChange = {
                            val current = uiState.filters
                            if (current != null)
                                viewModel.updateFilters(current.copy(searchText = it))
                            else {
                                val filters = Filters(
                                    searchText = it
                                )
                                viewModel.updateFilters(filters)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            // при фокусе поле поднимаем над соседями
                            .zIndex(if (searchFocused) 2f else 0f),
                        onFocusChangedParent = { focused -> searchFocused = focused }
                    )

                    // Все кнопки показываем только когда поле НЕ в фокусе
                    AnimatedVisibility(
                        visible = !searchFocused,
                        enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
                        exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut()
                    ) {
                        Row {
                            ImageButton(
                                id = R.drawable.ic_maps,
                                shape = RoundedCornerShape(4.dp),
                                sizeButton = 40.dp,
                                sizeImage = 24.dp,
                                modifier = Modifier.padding(start = 7.dp),
                                onClick = {
                                    showMapsDialog = true
//                                        Toast.makeText(context, "Карта в разработке", Toast.LENGTH_SHORT).show()
                                }
                            )

                            if ((viewModel.typeWindow ?: "").equals("container", true)) {
                                ImageButton(
                                    id = R.drawable.ic_settings_empt,
                                    shape = RoundedCornerShape(4.dp),
                                    sizeButton = 40.dp, sizeImage = 24.dp,
                                    modifier = Modifier.padding(start = 7.dp),
                                    onClick = { showSettingsDialog = true }
                                )

                                ImageButton(
                                    id = R.drawable.ic_refresh,
                                    shape = RoundedCornerShape(4.dp),
                                    sizeButton = 40.dp, sizeImage = 24.dp,
                                    modifier = Modifier.padding(start = 7.dp),
                                    onClick = { viewModel.updateContent() }
                                )
                            }

                            ImageButton(
                                id = R.drawable.ic_plus,
                                sizeButton = 40.dp, sizeImage = 24.dp,
                                modifier = Modifier.padding(start = 7.dp),
                                onClick = {
//                                    ##
                                    showAdditionalContent = true
                                },
                                shape = RoundedCornerShape(4.dp)
                            )

                            ImageButton(
                                id = if (isActiveSorted) R.drawable.ic_sort_down_checked else R.drawable.ic_sort_down,
                                sizeButton = 40.dp, sizeImage = 24.dp,
                                modifier = Modifier.padding(start = 7.dp),
                                onClick = { showSortingDialog = true },
                                shape = RoundedCornerShape(4.dp)
                            )

                            ImageButton(
                                id = if (isActiveFiltered) R.drawable.ic_filterbold else R.drawable.ic_filter,
                                sizeButton = 40.dp, sizeImage = 24.dp,
                                modifier = Modifier.padding(start = 7.dp),
                                onClick = { showFilteringDialog = true },
                                shape = RoundedCornerShape(4.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .focusable()
                        .background(colorResource(id = R.color.main_form_list))
                ) {
                    LazyColumnScrollbar(
                        modifier = Modifier
                            .padding(start = 2.dp, top = 10.dp, bottom = 10.dp)
                            .weight(1f),
                        state = listState,
                        settings = ScrollbarSettings(
                            scrollbarPadding = 2.dp,
                            alwaysShowScrollbar = true,
                            thumbUnselectedColor = colorResource(id = R.color.scrollbar),
                            thumbSelectedColor = colorResource(id = R.color.scrollbar),
                            thumbShape = CircleShape,
                        ),
                    ) {

                        LazyColumn(
                            state = listState,
                        ) {
                            if (!isActiveGrouped || groups.isEmpty()) {
                                // ----- обычный режим, как сейчас -----
                                itemsIndexed(
                                    items = dataItemsUI,
                                    key = { _, item -> item.stableId }
                                ) { index, item ->
                                    ItemRowCard(
                                        item = item,
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        context = context,
                                        visibilityColumName = visibilityColumName
                                    )
                                }
                            } else {
                                // ----- режим колод -----
                                items(
                                    items = groups,
                                    key = { it.groupKey } // groupKey или другой стабильный ключ группы
                                ) { groupMeta ->
                                    val groupItems = dataItemsUI.subList(
                                        groupMeta.startIndex,
                                        groupMeta.endIndexExclusive
                                    )

                                    GroupDeck(
                                        groupMeta = if (visibilityHeaderGroupName) groupMeta else null,
                                        items = groupItems,
                                        visibilityColumName = visibilityColumName,
                                        settingsItems = uiState.settingsItems,
                                        viewModel = viewModel,
                                        context = context,
                                        groupingFields = uiState.groupingFields, // 👈 список всех группировок
                                        level = 0                                // 👈 верхний уровень
                                    )
                                }
                            }
                        }

//                        LazyColumn(
//                            state = listState,
//                        ) {
//                            itemsIndexed(
//                                items = dataItemsUI,
//                                key = { _, item -> item.stableId }
//                            ) { index, item ->
//                                val key = item.stableId
//                                var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
//                                // Анимированный уход «в ноль» для исходного элемента
//                                AnimatedVisibility(
//                                    visible = disappearingKey != key,
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .animateItemPlacement(animationSpec = tween(300)), // плавное смещение соседей
//                                    exit = shrinkVertically(
//                                        animationSpec = tween(250),
//                                        shrinkTowards = Alignment.Top
//                                    ) + fadeOut(tween(180))
//                                ) {
//                                    Box(
//                                        modifier = Modifier
//                                            .onGloballyPositioned {
//                                                // Сохраняем coords в map
//                                                coordsMap[key] = it
//                                                coords = it
//                                            }
//                                            // Долгий тап -> «призрак» для перетаскивания
//                                            .pointerInput(Unit) {
//                                                detectDragGesturesAfterLongPress(
//                                                    onDragStart = { _ ->
////                                                        val cronchikViewModel =
////                                                            ViewModelProvider(context as AppCompatActivity).get<CronchikViewModel>(CronchikViewModel::class.java)
////                                                        cronchikViewModel.updateBadge(0, 1)
////
////                                                        coords?.let { c ->
////                                                            val pos = c.positionInRoot()
////                                                            val size = c.size
////                                                            flying = Flying(
////                                                                item = item,
////                                                                startOffset = IntOffset(
////                                                                    pos.x.roundToInt(),
////                                                                    pos.y.roundToInt()
////                                                                ),
////                                                                size = size
////                                                            )
////                                                        }
////                                                        disappearingKey = key
//                                                        coords?.let { c ->
//                                                            val pos = c.positionInRoot()
//                                                            dragging = Dragging(
//                                                                item = item,
//                                                                size = c.size,
//                                                                offset = mutableStateOf(
//                                                                    Offset(
//                                                                        pos.x,
//                                                                        pos.y
//                                                                    )
//                                                                )
//                                                            )
//                                                            // лёгкая вибрация на старте
//                                                            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
//                                                        }
//                                                    },
//                                                    onDrag = { change, dragAmount ->
//                                                        change.consume() // блокируем скролл списка во время перетаскивания
//                                                        dragging?.offset?.let { it.value += dragAmount }
//                                                    },
//                                                    onDragEnd = {
//                                                        dragging?.let { d ->
//                                                            shrinking = Shrinking(
//                                                                item = d.item,
//                                                                startOffset = IntOffset(
//                                                                    d.offset.value.x.roundToInt(),
//                                                                    d.offset.value.y.roundToInt()
//                                                                ),
//                                                                size = d.size
//                                                            )
//                                                        }
//                                                        dragging = null
//                                                    },
//                                                    onDragCancel = {
//                                                        // Отменили — тоже «сожмём и скроем» из текущей позиции
//                                                        dragging?.let { d ->
//                                                            shrinking = Shrinking(
//                                                                item = d.item,
//                                                                startOffset = IntOffset(
//                                                                    d.offset.value.x.roundToInt(),
//                                                                    d.offset.value.y.roundToInt()
//                                                                ),
//                                                                size = d.size
//                                                            )
//                                                        }
//                                                        dragging = null
//                                                    }
//                                                )
//                                            }
//                                    ) {
//                                        ItemUI(
//                                            item = item,
//                                            visibilityColumName = visibilityColumName,
//                                            settingsItemUI = uiState.settingsItems,
//                                            contextUI = viewModel.modeUI,
//                                            onClickItem = {
//                                                viewModel.onClickItem(it, context)
////                                                TODO Код анимации перенести в remember
//                                                // запускаем анимацию через viewModel, чтобы единая логика для внешних вызовов и кликов
////                                                viewModel.requestFlyByStableId(key)
////                                                coords?.let { c ->
////                                                    val pos = c.positionInRoot()
////                                                    val size = c.size
////                                                    flying = Flying(
////                                                        item = item,
////                                                        startOffset = IntOffset(
////                                                            pos.x.roundToInt(),
////                                                            pos.y.roundToInt()
////                                                        ),
////                                                        size = size
////                                                    )
////                                                }
////                                                disappearingKey = key
//                                            },
//                                            onClickItemImage = {
//                                                viewModel.onClickItemImage(
//                                                    it,
//                                                    context
//                                                )
//                                            },
//                                            onMultipleClickItemImage = { dataItem, index ->
//                                                viewModel.onClickItemImage(dataItem, context, index)
//                                            },
//                                            onCheckItem = { checked, it ->
//                                                viewModel.updateItemSelect(
//                                                    checked,
//                                                    it
//                                                )
//                                            }
//                                        )
//                                    }
//                                }
//                            }
//                        }
                    }

                    Row {
                        Tooltip(
                            text = viewModel.getTranslateString(
                                stringResource(
                                    id = R.string.total_number_selected,
                                    dataItemsUI.size
                                )
                            )
                        ) {
                            Text(
                                text = "\u2211 ${dataItemsUI.size}",
                                fontSize = 16.sp,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
                            )
                        }

                        Tooltip(
                            text = viewModel.getTranslateString(
                                stringResource(
                                    id = R.string.total_number_selected,
                                    0
                                )
                            )
                        ) {
                            Text(
                                text = "⚲ ${0}",
                                fontSize = 16.sp,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
                            )
                        }

                        Tooltip(
                            text = viewModel.getTranslateString(
                                stringResource(
                                    id = R.string.total_number_selected,
                                    0
                                )
                            )
                        ) {
                            Text(
                                text = "\u2207 ${0}",
                                fontSize = 16.sp,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (viewModel.modeUI == ModeUI.ONE_SELECT || viewModel.modeUI == ModeUI.MULTI_SELECT) {
                            val selectedCount = dataItemsUI.filter { it.selected }.size
                            Tooltip(
                                text = viewModel.getTranslateString(
                                    stringResource(
                                        id = R.string.total_number_marked,
                                        selectedCount
                                    )
                                )
                            ) {
                                Text(
                                    text = "\u2713 $selectedCount",
                                    fontSize = 16.sp,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier
                                        .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
                                )
                            }
                        }

                    }
                }

//                Column(
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
//                        .shadow(4.dp, RoundedCornerShape(8.dp))
//                        .clip(RoundedCornerShape(8.dp))
//                        .focusable()
//                        .background(colorResource(id = R.color.main_form_list))
//                ) {
//                    // Верхняя часть – анимируем область, где либо стопка, либо полный список
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .fillMaxWidth()
//                            .animateContentSize(
//                                animationSpec = spring(
//                                    dampingRatio = Spring.DampingRatioNoBouncy,
//                                    stiffness = Spring.StiffnessMedium
//                                )
//                            )
//                    ) {
//                        AnimatedContent(
//                            targetState = isCollapsed,
//                            modifier = Modifier.fillMaxSize(),
//                            transitionSpec = {
//                                if (!targetState) {
//                                    // Свернуто -> развернуто (лента выезжает из области стопки)
//                                    (slideInVertically(
//                                        initialOffsetY = { fullHeight -> -fullHeight / 4 } // слегка сверху
//                                    ) + fadeIn()) togetherWith
//                                            (slideOutVertically(
//                                                targetOffsetY = { fullHeight -> fullHeight / 4 }
//                                            ) + fadeOut())
//                                } else {
//                                    // Развернуто -> свернуто (список стягивается в область стопки)
//                                    (slideInVertically(
//                                        initialOffsetY = { fullHeight -> fullHeight / 4 }
//                                    ) + fadeIn()) togetherWith
//                                            (slideOutVertically(
//                                                targetOffsetY = { fullHeight -> -fullHeight / 4 }
//                                            ) + fadeOut())
//                                }
//                            },
//                            label = "stack_expand_collapse"
//                        ) { collapsed ->
//                            if (collapsed) {
//                                // --- СВЕРНУТОЕ СОСТОЯНИЕ: СТОПКА КАРТОЧЕК ---
//                                val previewItems = remember(dataItemsUI) { dataItemsUI.take(3) }
//
//                                val elevation by animateDpAsState(targetValue = 10.dp, label = "stack_elevation")
//                                val offsetY by animateDpAsState(targetValue = (-8).dp, label = "stack_offset_y")
//
//                                Box(
//                                    modifier = Modifier
//                                        .fillMaxSize()
//                                        .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
//                                        .offset(y = offsetY)
//                                        .clickable {
//                                            // по клику разворачиваем список
//                                            isCollapsed = false
//                                        }
//                                ) {
//                                    previewItems.forEachIndexed { index, item ->
//                                        val cardOffset = (index * 6).dp
//                                        val cardAlpha = 1f - index * 0.18f
//
//                                        Card(
//                                            shape = RoundedCornerShape(8.dp),
//                                            elevation = CardDefaults.cardElevation(
//                                                defaultElevation = elevation - index.dp
//                                            ),
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .align(Alignment.TopCenter)
//                                                .offset(y = cardOffset)
//                                                .zIndex((previewItems.size - index).toFloat())
//                                                .graphicsLayer { alpha = cardAlpha }
//                                        ) {
//                                            // внутри используем тот же ItemUI (можно считать превью)
//                                            ItemUI(
//                                                item = item,
//                                                visibilityColumName = visibilityColumName,
//                                                settingsItemUI = uiState.settingsItems,
//                                                contextUI = viewModel.modeUI,
//                                                onClickItem = {
//                                                    // разворачиваем и обрабатываем клик как раньше
//                                                    isCollapsed = false
////                                                    viewModel.onClickItem(it, context)
//                                                },
//                                                onClickItemImage = {
//                                                    viewModel.onClickItemImage(it, context)
//                                                },
//                                                onMultipleClickItemImage = { dataItem, idx ->
//                                                    viewModel.onClickItemImage(dataItem, context, idx)
//                                                },
//                                                onCheckItem = { checked, it2 ->
//                                                    viewModel.updateItemSelect(checked, it2)
//                                                }
//                                            )
//                                        }
//                                    }
//                                }
//                            } else {
//                                // --- РАЗВЁРНУТОЕ СОСТОЯНИЕ: ТВОЙ ОРИГИНАЛЬНЫЙ СПИСОК ---
//                                LazyColumnScrollbar(
//                                    modifier = Modifier
//                                        .padding(start = 10.dp, top = 10.dp, bottom = 7.dp)
//                                        .fillMaxSize(),
//                                    state = listState,
//                                    settings = ScrollbarSettings(
//                                        scrollbarPadding = 2.dp,
//                                        alwaysShowScrollbar = true,
//                                        thumbUnselectedColor = colorResource(id = R.color.scrollbar),
//                                        thumbSelectedColor = colorResource(id = R.color.scrollbar),
//                                        thumbShape = CircleShape,
//                                    ),
//                                ) {
//                                    LazyColumn(
//                                        state = listState,
//                                    ) {
//                                        itemsIndexed(
//                                            items = dataItemsUI,
//                                            key = { _, item -> item.stableId }
//                                        ) { index, item ->
//                                            val key = item.stableId
//                                            var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
//                                            // Анимированный уход «в ноль» для исходного элемента
//                                            this@Column.AnimatedVisibility(
//                                                visible = disappearingKey != key,
//                                                label = "",
//                                                modifier = Modifier
//                                                    .fillMaxWidth()
//                                                    .animateItemPlacement(animationSpec = tween(300)), // плавное смещение соседей
//                                                exit = shrinkVertically(
//                                                    animationSpec = tween(250),
//                                                    shrinkTowards = Alignment.Top
//                                                ) + fadeOut(tween(180))
//                                            ) {
//                                                Box(
//                                                    modifier = Modifier
//                                                        .onGloballyPositioned {
//                                                            // Сохраняем coords в map
//                                                            coordsMap[key] = it
//                                                            coords = it
//                                                        }
//                                                        // Долгий тап -> «призрак» для перетаскивания
//                                                        .pointerInput(Unit) {
//                                                            detectDragGesturesAfterLongPress(
//                                                                onDragStart = { _ ->
//                                                                    coords?.let { c ->
//                                                                        val pos = c.positionInRoot()
//                                                                        dragging = Dragging(
//                                                                            item = item,
//                                                                            size = c.size,
//                                                                            offset = mutableStateOf(
//                                                                                Offset(
//                                                                                    pos.x,
//                                                                                    pos.y
//                                                                                )
//                                                                            )
//                                                                        )
//                                                                        // лёгкая вибрация на старте
//                                                                        haptics.performHapticFeedback(
//                                                                            androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
//                                                                        )
//                                                                    }
//                                                                },
//                                                                onDrag = { change, dragAmount ->
//                                                                    change.consume() // блокируем скролл списка во время перетаскивания
//                                                                    dragging?.offset?.let { it.value += dragAmount }
//                                                                },
//                                                                onDragEnd = {
//                                                                    dragging?.let { d ->
//                                                                        shrinking = Shrinking(
//                                                                            item = d.item,
//                                                                            startOffset = IntOffset(
//                                                                                d.offset.value.x.roundToInt(),
//                                                                                d.offset.value.y.roundToInt()
//                                                                            ),
//                                                                            size = d.size
//                                                                        )
//                                                                    }
//                                                                    dragging = null
//                                                                },
//                                                                onDragCancel = {
//                                                                    // Отменили — тоже «сожмём и скроем» из текущей позиции
//                                                                    dragging?.let { d ->
//                                                                        shrinking = Shrinking(
//                                                                            item = d.item,
//                                                                            startOffset = IntOffset(
//                                                                                d.offset.value.x.roundToInt(),
//                                                                                d.offset.value.y.roundToInt()
//                                                                            ),
//                                                                            size = d.size
//                                                                        )
//                                                                    }
//                                                                    dragging = null
//                                                                }
//                                                            )
//                                                        }
//                                                ) {
//                                                    ItemUI(
//                                                        item = item,
//                                                        visibilityColumName = visibilityColumName,
//                                                        settingsItemUI = uiState.settingsItems,
//                                                        contextUI = viewModel.modeUI,
//                                                        onClickItem = {
//                                                            viewModel.onClickItem(it, context)
//                                                        },
//                                                        onClickItemImage = {
//                                                            viewModel.onClickItemImage(
//                                                                it,
//                                                                context
//                                                            )
//                                                        },
//                                                        onMultipleClickItemImage = { dataItem, indexImg ->
//                                                            viewModel.onClickItemImage(dataItem, context, indexImg)
//                                                        },
//                                                        onCheckItem = { checked, it3 ->
//                                                            viewModel.updateItemSelect(
//                                                                checked,
//                                                                it3
//                                                            )
//                                                        }
//                                                    )
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    // --- НИЖНИЙ ROW ОСТАВЛЯЕМ КАК БЫЛ ---
//                    Row {
//                        Tooltip(
//                            text = viewModel.getTranslateString(
//                                stringResource(
//                                    id = R.string.total_number_selected,
//                                    dataItemsUI.size
//                                )
//                            )
//                        ) {
//                            Text(
//                                text = "\u2211 ${dataItemsUI.size}",
//                                fontSize = 16.sp,
//                                textDecoration = TextDecoration.Underline,
//                                modifier = Modifier
//                                    .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
//                            )
//                        }
//
//                        Tooltip(
//                            text = viewModel.getTranslateString(
//                                stringResource(
//                                    id = R.string.total_number_selected,
//                                    0
//                                )
//                            )
//                        ) {
//                            Text(
//                                text = "⚲ ${0}",
//                                fontSize = 16.sp,
//                                textDecoration = TextDecoration.Underline,
//                                modifier = Modifier
//                                    .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
//                            )
//                        }
//
//                        Tooltip(
//                            text = viewModel.getTranslateString(
//                                stringResource(
//                                    id = R.string.total_number_selected,
//                                    0
//                                )
//                            )
//                        ) {
//                            Text(
//                                text = "\u2207 ${0}",
//                                fontSize = 16.sp,
//                                textDecoration = TextDecoration.Underline,
//                                modifier = Modifier
//                                    .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.weight(1f))
//
//                        if (viewModel.modeUI == ModeUI.ONE_SELECT || viewModel.modeUI == ModeUI.MULTI_SELECT) {
//                            val selectedCount = dataItemsUI.count { it.selected }
//                            Tooltip(
//                                text = viewModel.getTranslateString(
//                                    stringResource(
//                                        id = R.string.total_number_marked,
//                                        selectedCount
//                                    )
//                                )
//                            ) {
//                                Text(
//                                    text = "\u2713 $selectedCount",
//                                    fontSize = 16.sp,
//                                    textDecoration = TextDecoration.Underline,
//                                    modifier = Modifier
//                                        .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
//                                )
//                            }
//                        }
//                    }
//                }

                if (viewModel.modeUI == ModeUI.ONE_SELECT || viewModel.modeUI == ModeUI.MULTI_SELECT) {
                    Row {
                        Button(
                            onClick = {
                                (context as? Activity)?.finish()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                        ) {
                            Text(
                                viewModel.getTranslateString(
                                    stringResource(id = R.string.ui_cancel),
                                    5994
                                )
                            )
                        }

                        val selectedItems = dataItemsUI.filter { it.selected }
                        Button(
                            onClick = {
                                if (selectedItems.isNotEmpty()) {
                                    viewModel.onSelectedItemsUI(selectedItems)
                                    (context as? Activity)?.setResult(Activity.RESULT_OK)
                                    (context as? Activity)?.finish()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors =
                                if (selectedItems.isNotEmpty())
                                    ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.orange))
                                else
                                    ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                        ) {
                            Text(
                                "${
                                    viewModel.getTranslateString(
                                        stringResource(id = R.string.ui_choice),
                                        5997
                                    )
                                } " +
                                        if (selectedItems.isNotEmpty()) "(${selectedItems.size})" else ""
                            )
                        }
                    }
                }

                // Как только пришёл pendingId И список обновился — скроллим к индексу
                LaunchedEffect(pendingId, dataItemsUI) {
                    val target = pendingId ?: return@LaunchedEffect
                    // Находим индекс по stableId
                    val indexInData = dataItemsUI.indexOfFirst { it.stableId == target }
                    if (indexInData >= 0) {
                        // ждём один кадр, чтобы LazyColumn промерилась
                        withFrameNanos { }
                        val indexToScroll = headerCount + indexInData
                        // Анимируем скролл
                        listState.animateScrollToItem(indexToScroll, scrollOffset = topOffsetPx)
                        // сбрасываем ожидание
                        pendingId = null
                    }
                }

            }
// Оверлейная «летающая» копия
            flying?.let { f ->
                // animatables в пикселях
                val y = remember { Animatable(f.startOffset.y.toFloat()) }
                val x = remember { Animatable(f.startOffset.x.toFloat()) }
                val alpha = remember { Animatable(1f) }
                val scale = remember { Animatable(1f) }

                LaunchedEffect(f.item, f.startOffset) {
                    // цель по Y — улететь за верх экрана (в пикселях)
                    val targetY = -(f.startOffset.y + f.size.height).toFloat()

                    // Вариант A: фиксированное смещение влево, например 80.dp
                    val deltaPxA = with(density) { 180.dp.toPx() }

                    // Вариант B: относительное смещение, например 20% от начальной позиции X
                    val deltaPxB = (f.startOffset.x * 0.2f)

                    // Выбери delta (A или B)
                    val deltaX = with(density) { 180.dp.toPx() }

                    // Если хочешь, чтобы объект мог улететь полностью за экран влево, можно убрать coerceAtLeast:
                    val targetX =
                        (f.startOffset.x - deltaX) // .coerceAtLeast(0f) // убери coerceAtLeast если нужно уход за левый край

                    // Параллельная анимация
                    coroutineScope {
                        launch {
                            y.animateTo(
                                targetValue = targetY,
                                animationSpec = tween(
                                    durationMillis = 2600,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                        launch {
                            x.animateTo(
                                targetValue = targetX,
                                animationSpec = tween(
                                    durationMillis = 2600,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                        launch {
                            alpha.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 1900, delayMillis = 1200)
                            )
                        }
                        launch {
                            scale.animateTo(0.01f, tween(1500))
                        }
                    }

                    // после завершения анимации — убрать копию
                    flying = null
                }

                Box(
                    modifier = Modifier
                        .zIndex(100f)
                        // <- здесь нужно использовать текущие значения animatable (x.value и y.value)
                        .offset { IntOffset(x.value.roundToInt(), y.value.roundToInt()) }
                        .requiredSize(
                            with(density) { f.size.width.toDp() },
                            with(density) { f.size.height.toDp() }
                        )
                        .alpha(alpha.value)
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                ) {
                    ItemUI(
                        item = f.item,
                        visibilityColumName = visibilityColumName,
                        settingsItemUI = uiState.settingsItems,
                        contextUI = viewModel.modeUI,
                        onClickItem = {},
                        onClickItemImage = {},
                        onMultipleClickItemImage = { _, _ -> },
                        onCheckItem = { _, _ -> }
                    )
                }
                disappearingKey = f.item.stableId
            }

            // === Оверлей «призрак», который двигаем пальцем ===
            dragging?.let { d ->
                Box(
                    modifier = Modifier
                        .zIndex(100f)
                        .offset {
                            IntOffset(
                                d.offset.value.x.roundToInt(),
                                d.offset.value.y.roundToInt()
                            )
                        }
                        .requiredSize(
                            with(density) { d.size.width.toDp() },
                            with(density) { d.size.height.toDp() }
                        )
                        .graphicsLayer {
                            // лёгкое «поднятие» в воздух
                            scaleX = 1.03f
                            scaleY = 1.03f
                            shadowElevation = 10f
                        }
                ) {
                    ItemUI(
                        item = d.item,
                        visibilityColumName = visibilityColumName,
                        settingsItemUI = uiState.settingsItems,
                        contextUI = viewModel.modeUI,
                        onClickItem = {},
                        onClickItemImage = {},
                        onMultipleClickItemImage = { _, _ -> },
                        onCheckItem = { _, _ -> }
                    )
                }
            }

            // === Оверлей «сжатие и исчезновение» после отпускания ===
            shrinking?.let { s ->
                val scale = remember { Animatable(1f) }
                val alpha = remember { Animatable(1f) }

                LaunchedEffect(s.item, s.startOffset) {
                    coroutineScope {
                        launch { scale.animateTo(0.1f, tween(550)) }
                        launch { alpha.animateTo(0f, tween(550)) }
                    }
                    shrinking = null
                }

                Box(
                    modifier = Modifier
                        .zIndex(199f)
                        .offset { IntOffset(s.startOffset.x, s.startOffset.y) }
                        .requiredSize(
                            with(density) { s.size.width.toDp() },
                            with(density) { s.size.height.toDp() }
                        )
                        .graphicsLayer {
                            this.alpha = alpha.value
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                ) {
                    ItemUI(
                        item = s.item,
                        visibilityColumName = visibilityColumName,
                        settingsItemUI = uiState.settingsItems,
                        contextUI = viewModel.modeUI,
                        onClickItem = {},
                        onClickItemImage = {},
                        onMultipleClickItemImage = { _, _ -> },
                        onCheckItem = { _, _ -> }
                    )
                }
            }
        }
    }



    if (showAdditionalContent) {
        Log.e("showAdditionalContent", "+")
        Log.e("showAdditionalContent", "showAdditionalContent: ${uiState.items.size}")
        viewModel.onClickAdditionalContent()
        showAdditionalContent = false

        viewModel.requestFlyByStableId(4060381953L)

        if ((viewModel.typeWindow ?: "").equals("full", true))
            MessageDialog(
                title = "Не доступно",
                status = DialogStatus.ALERT,
                message = "Данный раздел находится в стадии в разработки",
                onDismiss = {
                    showMapsDialog = false
                },
                onConfirmAction = {
                    showMapsDialog = false
                }
            )
    }

    if (showSettingsDialog) {
        SettingsDialog(viewModel, onDismiss = { showSettingsDialog = false })
    }

    if (showSortingDialog) {
        SortingDialog(viewModel, onDismiss = { showSortingDialog = false })
    }

    if (showFilteringDialog) {
        FilteringDialog(
            viewModel,
            onDismiss = { showFilteringDialog = false },
            onChanged = {
                viewModel.updateFilters(it)
                showFilteringDialog = false
            }
        )
    }

    if (showMapsDialog) {
        ua.com.merchik.merchik.features.maps.presentation.main.MapsDialog(
            mainViewModel = viewModel,
            onDismiss = { showMapsDialog = false },
            contextUI = viewModel.contextUI,
            onOpenContextMenu = { wp, ctxUI ->
                viewModel.openContextMenu(wp, ctxUI)
            }
        )

//        MapsDialog(
//            viewModel,
//            onDismiss = { showMapsDialog = false },
//            contextUI = viewModel.contextUI, // откуда у тебя он берётся
//            onOpenContextMenu = { wp, ctxUI ->
//                viewModel.openContextMenu(wp, ctxUI)
//            }
//        )
    }

}

var index = 0

//@Stable
//@Composable
//fun ItemUI(
//    item: DataItemUI,
//    settingsItemUI: List<SettingsItemUI>,
//    visibilityColumName: Int,
//    contextUI: ModeUI,
//    onClickItem: (DataItemUI) -> Unit,
//    onClickItemImage: (DataItemUI) -> Unit,
//    onMultipleClickItemImage: (DataItemUI, Int) -> Unit, // Теперь принимает и индекс
//    onCheckItem: (Boolean, DataItemUI) -> Unit
//) {
//    index++
//    Globals.writeToMLOG("INFO", "MainUI.ItemUI", "index: $index")
//
//    Box(
//        modifier = Modifier
//            .clickable { onClickItem(item) }
//            .fillMaxWidth()
////            .padding(end = 10.dp, bottom = 7.dp)
//            .clip(RoundedCornerShape(8.dp))
//            .border(1.dp, Color.LightGray)
//            .shadow(6.dp, RoundedCornerShape(8.dp))
//            .then(
//                Modifier.background(
//                    if (item.selected) colorResource(id = R.color.selected_item)
//                    else item.modifierContainer?.background ?: Color.White
//                )
//            )
//    ) {
//        if (item.images?.size == 3)
//        // Новый блок для трех изображений в ряд
//            Column(
//                Modifier
//                    .fillMaxWidth()
//                    .padding(7.dp)
//            ) {
//                item.fields.firstOrNull { it.key.equals("id_res_image", true) }?.let {
//                    val images = item.images.take(3)
//                    val defaultImage = painterResource(R.drawable.merchik)
//
//                    Row(
//                        modifier = Modifier
////                            .padding(end = 5.dp)
//                    ) {
//                        repeat(3) { index ->
//                            Box(
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .padding(2.dp)
//                                    .aspectRatio(1f)
//                                    .border(1.dp, Color.LightGray)
//                                    .background(Color.White)
//                            ) {
//                                val painter = when {
//                                    index < images.size -> {
//                                        val file = File(images[index])
//                                        if (file.exists()) {
//                                            rememberAsyncImagePainter(model = file)
//                                        } else {
//                                            defaultImage
//                                        }
//                                    }
//
//                                    else -> defaultImage
//                                }
//
//                                val fields = item.rawObj.firstOrNull()?.getFieldsForOrderOnUI()
//                                when {
//                                    fields.isNullOrEmpty() || index >= fields.size || fields[index].isNullOrEmpty() -> {
//                                        Image(
//                                            painter = painter,
//                                            contentDescription = null,
//                                            modifier = Modifier
//                                                .fillMaxSize()
//                                                .clickable {
//                                                    onMultipleClickItemImage(
//                                                        item,
//                                                        index
//                                                    )
//                                                },
//                                            contentScale = ContentScale.Crop
//                                        )
//                                    }
//
//                                    else -> {
//                                        // Получаем базовый текст
//                                        val baseText = fields[index].orEmpty()
//                                        val modifiedText = if (baseText == "Планограма") {
//                                            item.rawObj
//                                                .filterIsInstance<PlanogrammVizitShowcaseSDB>()
//                                                .firstOrNull()
//                                                ?.planogram_id
//                                                ?.let { "$baseText $it" }
//                                                ?: baseText
//                                        } else {
//                                            baseText
//                                        }
//
//                                        ImageWithText(
//                                            item = item,
//                                            index = index,
//                                            painter = painter,
//                                            imageText = modifiedText
//                                        )
//                                        { clickedItem, clickedIndex ->
//                                            onMultipleClickItemImage(clickedItem, clickedIndex)
//                                        }
//
//                                    }
//                                }
//                                if (index == 0) {
//                                    item.rawObj.firstOrNull { it is PlanogrammVizitShowcaseSDB }
//                                        ?.let {
//                                            it as PlanogrammVizitShowcaseSDB
//                                            val text = it.score
//                                            Box(
//                                                modifier = Modifier.align(Alignment.TopEnd)
//                                            )
//                                            {
//                                                TextInStrokeCircle(
//                                                    modifier = Modifier
//                                                        .clickable { onClickItem(item) }
//                                                        .align(Alignment.Center),
//                                                    text = text,
//                                                    circleColor = Color.Gray,
//                                                    textColor = Color.Gray,
//                                                    aroundColor =
//                                                        if (item.selected) colorResource(id = R.color.selected_item)
//                                                        else item.modifierContainer?.background
//                                                            ?: Color.White.copy(alpha = 0.5f),
//                                                    circleSize = 30.dp,
//                                                    textSize = 20f.toPx(),
//                                                )
//                                            }
//                                        }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        else
//            Row(Modifier.padding(7.dp)) {
//                item.fields.firstOrNull {
//                    it.key.equals(
//                        "id_res_image",
//                        true
//                    )
//                }?.let {
//                    val idResImage = (it.value.rawValue as? Int)
//                        ?: R.drawable.merchik
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .padding(end = 5.dp)
//                            .border(1.dp, Color.LightGray)
//                            .background(Color.White)
//                            .align(alignment = Alignment.Top)
//                    ) {
//                        val images = mutableListOf<Painter>()
//                        if (item.images.isNullOrEmpty()) {
//                            images.add(painterResource(idResImage))
//                        } else {
//                            item.images.forEach { pathImage ->
//                                val file = File(pathImage)
//                                if (file.exists()) {
//                                    images.add(
//                                        rememberAsyncImagePainter(model = file)
//                                    )
//                                } else
//                                    images.add(painterResource(idResImage))
//                            }
//                        }
//
//                        if (images.size <= 1) {
//                            Image(
//                                painter = images[0],
//                                modifier = Modifier
//                                    .padding(5.dp)
//                                    .size(100.dp)
//                                    .clickable { onClickItemImage(item) },
//                                contentScale = ContentScale.FillWidth,
//                                contentDescription = null
//                            )
//                        } else {
//                            LazyRow {
//                                items(images) { image ->
//                                    Image(
//                                        painter = image,
//                                        modifier = Modifier
//                                            .padding(5.dp)
//                                            .size(100.dp)
//                                            .clickable { onClickItemImage(item) },
//                                        contentScale = ContentScale.FillWidth,
//                                        contentDescription = null
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//
//                Column(
//                    modifier = Modifier
//                        .weight(if (item.images?.size == 3) 1f else 2f)
//                ) {
//                    item.fields.forEachIndexed { index, field ->
//                        if (settingsItemUI.firstOrNull {
//                                it.key.equals(
//                                    field.key,
//                                    true
//                                )
//                            }?.isEnabled == false) {
//                        } else {
//                            if (!field.key.equals("id_res_image", true)) {
//                                ItemFieldValue(field, visibilityColumName)
//                                if (index < item.fields.size - 1)
//                                    HorizontalDivider(color = Color.LightGray)
//                            }
//                        }
//                    }
//                }
//            }
//
//        Column(modifier = Modifier.align(Alignment.TopEnd)) {
//
//            if (contextUI == ModeUI.ONE_SELECT || contextUI == ModeUI.MULTI_SELECT) {
//                RoundCheckbox(
//                    modifier = Modifier.padding(
//                        top = 3.dp,
//                        end = 3.dp
//                    ),
//                    checked = item.selected,
//                    aroundColor =
//                        if (item.selected) colorResource(id = R.color.selected_item)
//                        else item.modifierContainer?.background ?: Color.White,
//                    onCheckedChange = { onCheckItem(it, item) }
//                )
//            }
//
//            item.rawObj.firstOrNull { it is AdditionalRequirementsMarkDB }
//                ?.let {
//                    it as AdditionalRequirementsMarkDB
//                    val text = it.score ?: "0"
//                    TextInStrokeCircle(
//                        modifier = Modifier.padding(
//                            top = 3.dp,
//                            end = 3.dp
//                        ),
//                        text = text,
//                        circleColor = if (text == "0") Color.Red else Color.Gray,
//                        textColor = if (text == "0") Color.Red else Color.Gray,
//                        aroundColor =
//                            if (item.selected) colorResource(id = R.color.selected_item)
//                            else item.modifierContainer?.background ?: Color.White,
//                        circleSize = 30.dp,
//                        textSize = 20f.toPx(),
//                    )
//                }
//        }
//    }
//}

@Stable
@Composable
fun ItemUI(
    item: DataItemUI,
    settingsItemUI: List<SettingsItemUI>,
    visibilityColumName: Int,
    contextUI: ModeUI,
    onClickItem: (DataItemUI) -> Unit,
    onClickItemImage: (DataItemUI) -> Unit,
    onMultipleClickItemImage: (DataItemUI, Int) -> Unit, // Теперь принимает и индекс
    onCheckItem: (Boolean, DataItemUI) -> Unit
) {
    index++
    Globals.writeToMLOG("INFO", "MainUI.ItemUI", "index: $index")

    // ✅ всегда считаем актуальный список видимых полей
    val visibleFields = item.fields.filter { field ->
        val setting = settingsItemUI.firstOrNull {
            it.key.equals(field.key, ignoreCase = true)
        }
        setting?.isEnabled == true
    }

    Box(
        modifier = Modifier
            .clickable { onClickItem(item) }
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .then(
                Modifier.background(
                    if (item.selected) colorResource(id = R.color.selected_item)
                    else item.modifierContainer?.background ?: Color.White
                )
            )
    ) {
        if (item.images?.size == 3)
        // Новый блок для трех изображений в ряд
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(7.dp)
            ) {
                item.fields.firstOrNull { it.key.equals("id_res_image", true) }?.let {
                    val images = item.images.take(3)
                    val defaultImage = painterResource(R.drawable.merchik)

                    Row(
                        modifier = Modifier
//                            .padding(end = 5.dp)
                    ) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                                    .aspectRatio(1f)
                                    .border(1.dp, Color.LightGray)
                                    .background(Color.White)
                            ) {
                                val painter = when {
                                    index < images.size -> {
                                        val file = File(images[index])
                                        if (file.exists()) {
                                            rememberAsyncImagePainter(model = file)
                                        } else {
                                            defaultImage
                                        }
                                    }

                                    else -> defaultImage
                                }

                                val fields = item.rawObj.firstOrNull()?.getFieldsForOrderOnUI()
                                when {
                                    fields.isNullOrEmpty() || index >= fields.size || fields[index].isNullOrEmpty() -> {
                                        Image(
                                            painter = painter,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable {
                                                    onMultipleClickItemImage(
                                                        item,
                                                        index
                                                    )
                                                },
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    else -> {
                                        // Получаем базовый текст
                                        val baseText = fields[index].orEmpty()
                                        val modifiedText = if (baseText == "Планограма") {
                                            item.rawObj
                                                .filterIsInstance<PlanogrammVizitShowcaseSDB>()
                                                .firstOrNull()
                                                ?.planogram_id
                                                ?.let { "$baseText $it" }
                                                ?: baseText
                                        } else {
                                            baseText
                                        }

                                        ImageWithText(
                                            item = item,
                                            index = index,
                                            painter = painter,
                                            imageText = modifiedText
                                        )
                                        { clickedItem, clickedIndex ->
                                            onMultipleClickItemImage(clickedItem, clickedIndex)
                                        }

                                    }
                                }
                                if (index == 0) {
                                    item.rawObj.firstOrNull { it is PlanogrammVizitShowcaseSDB }
                                        ?.let {
                                            it as PlanogrammVizitShowcaseSDB
                                            val text = it.score
                                            Box(
                                                modifier = Modifier.align(Alignment.TopEnd)
                                            )
                                            {
                                                TextInStrokeCircle(
                                                    modifier = Modifier
                                                        .clickable { onClickItem(item) }
                                                        .align(Alignment.Center),
                                                    text = text,
                                                    circleColor = Color.Gray,
                                                    textColor = Color.Gray,
                                                    aroundColor =
                                                        if (item.selected) colorResource(id = R.color.selected_item)
                                                        else item.modifierContainer?.background
                                                            ?: Color.White.copy(alpha = 0.5f),
                                                    circleSize = 30.dp,
                                                    textSize = 20f.toPx(),
                                                )
                                            }
                                        }
                                }
                            }
                        }
                    }
                }
            }
        else
            Row(Modifier.padding(7.dp)) {
                item.fields.firstOrNull {
                    it.key.equals(
                        "id_res_image",
                        true
                    )
                }?.let {
                    val idResImage = (it.value.rawValue as? Int)
                        ?: R.drawable.merchik
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 5.dp)
                            .border(1.dp, Color.LightGray)
                            .background(Color.White)
                            .align(alignment = Alignment.Top)
                    ) {
                        val images = mutableListOf<Painter>()
                        if (item.images.isNullOrEmpty()) {
                            images.add(painterResource(idResImage))
                        } else {
                            item.images.forEach { pathImage ->
                                val file = File(pathImage)
                                if (file.exists()) {
                                    images.add(
                                        rememberAsyncImagePainter(model = file)
                                    )
                                } else
                                    images.add(painterResource(idResImage))
                            }
                        }

                        if (images.size <= 1) {
                            Image(
                                painter = images[0],
                                modifier = Modifier
                                    .padding(5.dp)
                                    .size(100.dp)
                                    .clickable { onClickItemImage(item) },
                                contentScale = ContentScale.FillWidth,
                                contentDescription = null
                            )
                        } else {
                            LazyRow {
                                items(images) { image ->
                                    Image(
                                        painter = image,
                                        modifier = Modifier
                                            .padding(5.dp)
                                            .size(100.dp)
                                            .clickable { onClickItemImage(item) },
                                        contentScale = ContentScale.FillWidth,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(if (item.images?.size == 3) 1f else 2f)
                ) {
                    if (contextUI == ModeUI.ONE_SELECT || contextUI == ModeUI.MULTI_SELECT)

                        item.fields.forEachIndexed { index, field ->
                            if (settingsItemUI.firstOrNull {
                                    it.key.equals(
                                        field.key,
                                        true
                                    )
                                }?.isEnabled == false) {
                            } else {
                                if (!field.key.equals("id_res_image", true)) {
                                    ItemFieldValue(field, visibilityColumName)
                                    if (index < visibleFields.size - 1) {
                                        val bg = item.modifierContainer?.background
                                        val color = when {
                                            // если контейнера нет → LightGray
                                            bg == null -> Color.LightGray
                                            // если фон светлее, чем LightGray → LightGray
                                            bg.isLighterThan(Color.LightGray) -> Color.LightGray
                                            // если такой же или темнее → White
                                            else -> Color.White
                                        }
                                        HorizontalDivider(
                                            color = color
                                        )
                                    }
                                }
                            }
                        }
                    else
                        visibleFields.forEachIndexed { index, field ->
                            if (!field.key.equals("id_res_image", true)) {
                                ItemFieldValue(field, visibilityColumName)
                                if (index < visibleFields.lastIndex) {
                                    val bg = item.modifierContainer?.background
                                    val color = when {
                                        // если контейнера нет → LightGray
                                        bg == null -> Color.LightGray
                                        // если фон светлее, чем LightGray → LightGray
                                        bg.isLighterThan(Color.LightGray) -> Color.LightGray
                                        // если такой же или темнее → White
                                        else -> Color.White
                                    }
                                    HorizontalDivider(color = color)
                                }
                            }
                        }
                }
            }

        Column(modifier = Modifier.align(Alignment.TopEnd)) {

            if (contextUI == ModeUI.ONE_SELECT || contextUI == ModeUI.MULTI_SELECT) {
                RoundCheckbox(
                    modifier = Modifier.padding(
                        top = 3.dp,
                        end = 3.dp
                    ),
                    checked = item.selected,
                    aroundColor =
                        if (item.selected) colorResource(id = R.color.selected_item)
                        else item.modifierContainer?.background ?: Color.White,
                    onCheckedChange = { onCheckItem(it, item) }
                )
            }

            item.rawObj.firstOrNull { it is AdditionalRequirementsMarkDB }
                ?.let {
                    it as AdditionalRequirementsMarkDB
                    val text = it.score ?: "0"
                    TextInStrokeCircle(
                        modifier = Modifier.padding(
                            top = 3.dp,
                            end = 3.dp
                        ),
                        text = text,
                        circleColor = if (text == "0") Color.Red else Color.Gray,
                        textColor = if (text == "0") Color.Red else Color.Gray,
                        aroundColor =
                            if (item.selected) colorResource(id = R.color.selected_item)
                            else item.modifierContainer?.background ?: Color.White,
                        circleSize = 30.dp,
                        textSize = 20f.toPx(),
                    )
                }
        }
    }
}

fun Color.isLighterThan(other: Color): Boolean =
    this.luminance() < other.luminance()

@Composable
fun TopButton(
    modifier: Modifier,
    onSettings: () -> Unit,
    onRefresh: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = modifier
    ) {
        ImageButton(
            id = R.drawable.ic_settings,
            shape = CircleShape,
            colorImage = ColorFilter.tint(color = Color.Gray),
            sizeButton = 40.dp,
            sizeImage = 25.dp,
            modifier = Modifier
                .padding(start = 15.dp, bottom = 10.dp),
            onClick = { onSettings.invoke() }
        )

        ImageButton(
            id = R.drawable.ic_refresh,
            shape = CircleShape,
            colorImage = ColorFilter.tint(color = Color.Gray),
            sizeButton = 40.dp,
            sizeImage = 25.dp,
            modifier = Modifier
                .padding(start = 15.dp, bottom = 10.dp),
            onClick = { onRefresh.invoke() }
        )

        ImageButton(
            id = R.drawable.ic_letter_x,
            shape = CircleShape,
            colorImage = ColorFilter.tint(color = Color.Gray),
            sizeButton = 40.dp,
            sizeImage = 25.dp,
            modifier = Modifier
                .padding(start = 15.dp, bottom = 10.dp),
            onClick = { onClose.invoke() }
        )
    }
}

@Composable
fun ComposableLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}