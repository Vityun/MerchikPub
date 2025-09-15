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
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
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
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB
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

    var showAdditionalContent by remember { mutableStateOf(false) }

    var showSettingsDialog by remember { mutableStateOf(false) }

    var showSortingDialog by remember { mutableStateOf(false) }

    var showFilteringDialog by remember { mutableStateOf(false) }

    var showMapsDialog by remember { mutableStateOf(false) }

    var showMessageDialog by remember { mutableStateOf<MessageDialogData?>(null) }

    val offsetSizeFont by viewModel.offsetSizeFonts.collectAsState()

    var maxLinesSubTitle by remember { mutableStateOf(1) }

    val listState = rememberLazyListState()

//    var selectedItem by remember { mutableStateOf<ContextMenuState?>(null) }
    val menu = rememberContextMenuHost(viewModel, context)

    var searchFocused by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val imeVisible by rememberImeVisible()

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

//                val searchStrList = uiState.filters?.searchText?.split(" ")
//
//                var _isActiveFiltered = uiState.filters?.searchText?.isNotEmpty() == true
//
//                fun getSortValue(it: DataItemUI, sortingField: SortingField) =
//                    it.fields.firstOrNull { fieldValue ->
//                        fieldValue.key.equals(sortingField.key, true)
//                    }?.value?.value
//
//                fun comparator(sortingField: SortingField?): Comparator<DataItemUI> =
//                    if (sortingField?.order == 1)
//                        compareBy { getSortValue(it, sortingField) }
//                    else if (sortingField?.order == -1)
//                        compareByDescending { getSortValue(it, sortingField) }
//                    else
//                        compareBy { 0 }

                val dataItemsUI = mutableListOf<DataItemUI>()

                dataItemsUI.addAll(uiState.itemsHeader)

                val result = filterAndSortDataItems(
                    items = uiState.items,
                    filters = uiState.filters,
                    sortingFields = uiState.sortingFields,
                    rangeStart = viewModel.rangeDataStart.value,
                    rangeEnd = viewModel.rangeDataEnd.value,
                    searchText = uiState.filters?.searchText
                )
                dataItemsUI.addAll(result.items)
                dataItemsUI.addAll(uiState.itemsFooter)

//                val dataItemsUI by remember(
//                    uiState.items,
//                    uiState.filters,
//                    uiState.sortingFields,
//                    viewModel.rangeDataStart.value,
//                    viewModel.rangeDataEnd.value
//                ) {
//                    mutableStateOf(
//                        filterAndSortDataItems(
//                            items = uiState.items,
//                            filters = uiState.filters,
//                            sortingFields = uiState.sortingFields,
//                            rangeStart = viewModel.rangeDataStart.value,
//                            rangeEnd = viewModel.rangeDataEnd.value,
//                            searchText = uiState.filters?.searchText
//                        ).items
//                    )
//                }

//                dataItemsUI.addAll(
//                    uiState.items.filter { dataItemUI ->
//                        uiState.filters?.let { filters ->
//                            filters.rangeDataByKey?.let { rangeDataByKey ->
//                                val star = viewModel.rangeDataStart.value
//                                val end = viewModel.rangeDataEnd.value
//                                dataItemUI.fields.forEach { fieldValue ->
//                                    if (fieldValue.key.equals(rangeDataByKey.key, true)) {
//                                        val rawValue = fieldValue.value.rawValue
//                                        val timestamp = when (rawValue) {
//                                            is Long -> rawValue
//                                            is Date -> rawValue.time
//                                            is String -> {
//                                                try {
//                                                    val formatter = SimpleDateFormat(
//                                                        "MMM d, yyyy hh:mm:ss a",
//                                                        Locale.US
//                                                    )
//                                                    val parsedDate = formatter.parse(rawValue)
//                                                    parsedDate?.time ?: return@filter false
//                                                } catch (e: Exception) {
//                                                    return@filter false // строка не распарсилась — фильтруем
//                                                }
//                                            }
//
//                                            else -> return@filter false // если ни Long, ни Date — исключаем
//                                        }
//// Получаем границы диапазона в миллисекундах
//                                        val startMillis = star
//                                            ?.atStartOfDay(ZoneId.systemDefault())
//                                            ?.toInstant()?.toEpochMilli() ?: Long.MIN_VALUE
//
//                                        val endMillis = end
//                                            ?.atTime(LocalTime.MAX)
//                                            ?.atZone(ZoneId.systemDefault())
//                                            ?.toInstant()?.toEpochMilli() ?: Long.MAX_VALUE
//
//// Фильтрация по диапазону
//                                        if (timestamp < startMillis || timestamp > endMillis) {
//                                            _isActiveFiltered = true
//                                            return@filter false
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                        var isFound: Boolean
//                        searchStrList?.forEach {
//                            isFound = false
//                            dataItemUI.fields.forEach inner@{ fieldValue ->
//                                if (fieldValue.value.value.contains(it, true)) {
//                                    isFound = true
//                                    return@inner
//                                }
//                            }
//                            if (!isFound) {
//                                return@filter false
//                            }
//                        }
//
//                        uiState.filters?.items?.let { filters ->
//                            filters.forEach { filter ->
//                                isFound = false
//                                if (filter.rightValuesRaw.isNotEmpty()) {
//                                    dataItemUI.rawFields.forEach inner@{ fieldValue ->
//                                        if (fieldValue.key.equals(filter.leftField, true)) {
//                                            val rawVal = filter.rightValuesRaw
//                                            val fieldVal = fieldValue.value.rawValue.toString()
//                                            if (filter.rightValuesRaw.isNotEmpty()) _isActiveFiltered =
//                                                true
//                                            if (filter.rightValuesRaw.contains(fieldValue.value.rawValue.toString())) {
//                                                isFound = true
//                                                return@inner
//                                            }
//                                        }
//                                    }
//                                }
//                                if (!isFound) {
//                                    return@filter false
//                                }
//                            }
//                        }
//
//                        return@filter true
//
//                    }.sortedWith(
//                        comparator(uiState.sortingFields.getOrNull(0))
//                            .thenComparing(comparator(uiState.sortingFields.getOrNull(1)))
//                            .thenComparing(comparator(uiState.sortingFields.getOrNull(2)))
//                    )
//                )


                isActiveFiltered = result.isActiveFiltered
                isActiveSorted = result.isActiveSorted

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

                uiState.subTitle?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Text(
                            text = it,
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
                            val filters = Filters(
                                rangeDataByKey = uiState.filters?.rangeDataByKey,
                                searchText = it
                            )
                            viewModel.updateFilters(filters)
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
                            if ((viewModel.typeWindow ?: "").equals("container", true)) {
                                ImageButton(
                                    id = R.drawable.ic_maps,
                                    shape = RoundedCornerShape(2.dp),
                                    sizeButton = 40.dp,
                                    sizeImage = 24.dp,
                                    modifier = Modifier.padding(start = 7.dp),
                                    onClick = {
                                        showMapsDialog = true
//                                        Toast.makeText(context, "Карта в разработке", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                ImageButton(
                                    id = R.drawable.ic_settings_empt,
                                    shape = RoundedCornerShape(2.dp),
                                    sizeButton = 40.dp, sizeImage = 24.dp,
                                    modifier = Modifier.padding(start = 7.dp),
                                    onClick = { showSettingsDialog = true }
                                )

                                ImageButton(
                                    id = R.drawable.ic_refresh,
                                    shape = RoundedCornerShape(2.dp),
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
                                shape = RoundedCornerShape(2.dp)
                            )

                            ImageButton(
                                id = if (isActiveSorted) R.drawable.ic_sort_down_checked else R.drawable.ic_sort_down,
                                sizeButton = 40.dp, sizeImage = 24.dp,
                                modifier = Modifier.padding(start = 7.dp),
                                onClick = { showSortingDialog = true },
                                shape = RoundedCornerShape(2.dp)
                            )

                            ImageButton(
                                id = if (isActiveFiltered) R.drawable.ic_filterbold else R.drawable.ic_filter,
                                sizeButton = 40.dp, sizeImage = 24.dp,
                                modifier = Modifier.padding(start = 7.dp),
                                onClick = { showFilteringDialog = true },
                                shape = RoundedCornerShape(2.dp)
                            )
                        }
                    }
                }

//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(start = 10.dp, bottom = 10.dp, end = 10.dp)
//                ) {
//
//
//                    TextFieldInputRounded(
//                        viewModel,
//                        value = uiState.filters?.searchText ?: "",
//                        onValueChange = {
//                            val filters = Filters(
//                                rangeDataByKey = uiState.filters?.rangeDataByKey,
//                                searchText = it
//                            )
//                            viewModel.updateFilters(filters)
//                        },
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(40.dp)
//
//                    )
//
//                    if ((viewModel.typeWindow ?: "").equals("container", true)) {
//                        ImageButton(
//                            id = R.drawable.ic_maps,
//                            shape = RoundedCornerShape(2.dp),
//                            sizeButton = 40.dp,
//                            sizeImage = 24.dp,
//                            modifier = Modifier
//                                .padding(start = 7.dp),
//                            onClick = {
//                                Toast.makeText(context, "Карта в разработке", Toast.LENGTH_SHORT)
//                                    .show()
//                            }
//                        )
//
//                        ImageButton(
//                            id = R.drawable.ic_settings_empt,
//                            shape = RoundedCornerShape(2.dp),
//                            sizeButton = 40.dp,
//                            sizeImage = 24.dp,
//                            modifier = Modifier
//                                .padding(start = 7.dp),
//                            onClick = { showSettingsDialog = true }
//                        )
//
//                        ImageButton(
//                            id = R.drawable.ic_refresh,
//                            shape = RoundedCornerShape(2.dp),
//                            sizeButton = 40.dp,
//                            sizeImage = 24.dp,
//                            modifier = Modifier
//                                .padding(start = 7.dp),
//                            onClick = { viewModel.updateContent() }
//                        )
//                    }
//                    ImageButton(
//                        id = R.drawable.ic_plus,
//                        sizeButton = 40.dp,
//                        sizeImage = 24.dp,
//                        modifier = Modifier.padding(start = 7.dp),
//                        onClick = { showAdditionalContent = true },
//                        shape = RoundedCornerShape(2.dp)
//                    )
//
//                    ImageButton(
//                        id = R.drawable.ic_sort_down,
//                        sizeButton = 40.dp,
//                        sizeImage = 24.dp,
//                        modifier = Modifier.padding(start = 7.dp),
//                        onClick = { showSortingDialog = true },
//                        shape = RoundedCornerShape(2.dp)
//                    )
//
//                    ImageButton(
//                        id = if (isActiveFiltered) R.drawable.ic_filterbold else R.drawable.ic_filter,
//                        sizeButton = 40.dp,
//                        sizeImage = 24.dp,
//                        modifier = Modifier.padding(start = 7.dp),
//                        onClick = { showFilteringDialog = true },
//                        shape = RoundedCornerShape(2.dp)
//                    )
//                }

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
                            .padding(start = 10.dp, top = 10.dp, bottom = 7.dp)
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
                            itemsIndexed(
                                items = dataItemsUI,
                                key = { _, item -> item.stableId }
                            ) { index, item ->
                                val key = item.stableId
//                                val isTarget = pendingHash != null && item.hashCode() == pendingHash
//                                // как только «наш» айтем отрисовался — скроллим к его индексу (он 100% точный)
//                                LaunchedEffect(isTarget) {
//                                    if (isTarget) {
//                                        withFrameNanos { } // дождаться измерения
//                                        // если у тебя есть stickyHeader/заголовки сверху — добавь их count
//                                        val headerCount = 0
//                                        listState.animateScrollToItem(headerCount + index, scrollOffset = topOffsetPx)
//                                        pendingHash = null
//                                    }
//                                }

                                var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
                                // Анимированный уход «в ноль» для исходного элемента
                                AnimatedVisibility(
                                    visible = disappearingKey != key,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItemPlacement(animationSpec = tween(300)), // плавное смещение соседей
                                    exit = shrinkVertically(
                                        animationSpec = tween(250),
                                        shrinkTowards = Alignment.Top
                                    ) + fadeOut(tween(180))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .onGloballyPositioned { coords = it }
                                            // Долгий тап -> «призрак» для перетаскивания
                                            .pointerInput(Unit) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = { _ ->
                                                        coords?.let { c ->
                                                            val pos = c.positionInRoot()
                                                            dragging = Dragging(
                                                                item = item,
                                                                size = c.size,
                                                                offset = mutableStateOf(
                                                                    Offset(
                                                                        pos.x,
                                                                        pos.y
                                                                    )
                                                                )
                                                            )
                                                            // лёгкая вибрация на старте
                                                            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                        }
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume() // блокируем скролл списка во время перетаскивания
                                                        dragging?.offset?.let { it.value += dragAmount }
                                                    },
                                                    onDragEnd = {
                                                        dragging?.let { d ->
                                                            shrinking = Shrinking(
                                                                item = d.item,
                                                                startOffset = IntOffset(
                                                                    d.offset.value.x.roundToInt(),
                                                                    d.offset.value.y.roundToInt()
                                                                ),
                                                                size = d.size
                                                            )
                                                        }
                                                        dragging = null
                                                    },
                                                    onDragCancel = {
                                                        // Отменили — тоже «сожмём и скроем» из текущей позиции
                                                        dragging?.let { d ->
                                                            shrinking = Shrinking(
                                                                item = d.item,
                                                                startOffset = IntOffset(
                                                                    d.offset.value.x.roundToInt(),
                                                                    d.offset.value.y.roundToInt()
                                                                ),
                                                                size = d.size
                                                            )
                                                        }
                                                        dragging = null
                                                    }
                                                )
                                            }
                                    ) {
                                        ItemUI(
                                            item = item,
                                            visibilityColumName = visibilityColumName,
                                            settingsItemUI = uiState.settingsItems,
                                            contextUI = viewModel.modeUI,
                                            onClickItem = {
                                                viewModel.onClickItem(it, context)
//                                                TODO Код анимации перенести в remember
//                                                coords?.let { c ->
//                                                    val pos = c.positionInRoot()
//                                                    val size = c.size
//                                                    flying = Flying(
//                                                        item = item,
//                                                        startOffset = IntOffset(
//                                                            pos.x.roundToInt(),
//                                                            pos.y.roundToInt()
//                                                        ),
//                                                        size = size
//                                                    )
//                                                }
//                                                disappearingKey = key
                                            },
                                            onClickItemImage = {
                                                viewModel.onClickItemImage(
                                                    it,
                                                    context
                                                )
                                            },
                                            onMultipleClickItemImage = { dataItem, index ->
                                                viewModel.onClickItemImage(dataItem, context, index)
                                            },
                                            onCheckItem = { checked, it ->
                                                viewModel.updateItemSelect(
                                                    checked,
                                                    it
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
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
                val y = remember { Animatable(f.startOffset.y.toFloat()) }
                val x = remember { Animatable(f.startOffset.x.toFloat()) }
                val alpha = remember { Animatable(1f) }
                val scale = remember { Animatable(1f) }

                LaunchedEffect(f.item, f.startOffset) {
                    // Цель: улететь за верх экрана
                    val targetY = -(f.startOffset.y + f.size.height).toFloat()
                    // Параллельно: перемещение и угасание
                    coroutineScope {
                        launch {
                            y.animateTo(
                                targetValue = targetY,
                                animationSpec = tween(
                                    durationMillis = 1600,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                        launch {
                            x.animateTo(
                                targetValue = 0.9f,
                                animationSpec = tween(
                                    durationMillis = 1600,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                        launch {
                            alpha.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 1500, delayMillis = 1200)
                            )
                        }
                        launch { scale.animateTo(0.01f, tween(1000)) }

                    }
                    flying = null
                }

                Box(
                    modifier = Modifier
                        .zIndex(100f)
                        .offset { IntOffset(f.startOffset.x, y.value.roundToInt()) }
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
                    // Рисуем ту же разметку, что и у исходного элемента
                    ItemUI(
                        item = f.item,
                        visibilityColumName = visibilityColumName,
                        settingsItemUI = uiState.settingsItems,
                        contextUI = viewModel.modeUI,
                        // Клики у копии глушим
                        onClickItem = {},
                        onClickItemImage = {},
                        onMultipleClickItemImage = { _, _ -> },
                        onCheckItem = { _, _ -> }
                    )
                }
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
        viewModel.onClickAdditionalContent()
        showAdditionalContent = false

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
        MapsDialog(
            viewModel,
            onDismiss = { showMapsDialog = false },
            contextUI = viewModel.contextUI, // откуда у тебя он берётся
            onOpenContextMenu = { wp, ctxUI ->
                viewModel.openContextMenu(wp, ctxUI)
            }
        )
    }

    showMessageDialog?.let { d ->
        MessageDialog(
            title = d.title,
            subTitle = d.subTitle,
            message = d.message,
            status = d.status,
            onDismiss = {
                showMessageDialog = null
                viewModel.cancelPending()
            },
            okButtonName = d.positivText ?: "Ok",
            onConfirmAction = {
                showMessageDialog = null
                viewModel.performPending()
            },
            onCancelAction = if (d.status == DialogStatus.NORMAL) {
                {
                    showMessageDialog = null
                    viewModel.cancelPending()
                }
            } else null
        )
    }

//    LaunchedEffect(Unit) {
//        viewModel.scrollToHash.collectLatest { targetHash ->
//            // ждём, пока список после фильтра/сортировки обновится и появится нужный элемент
//            val index = snapshotFlow { uiState.items }
//                .mapLatest { list -> list.indexOfFirst { it.hashCode() == targetHash } }
//                .filter { it >= 0 }
//                .first()
//
//            val topOffsetPx = with(density) { 8.dp.toPx().toInt() }
//
//            listState.animateScrollToItem(index
//                , -topOffsetPx
////                , scrollOffset = -topOffsetPx
//            )
//        }
//    }

}

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
    Box(
        modifier = Modifier
            .clickable { onClickItem(item) }
            .fillMaxWidth()
            .padding(end = 10.dp, bottom = 7.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color.LightGray)
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
                                if (index < item.fields.size - 1) HorizontalDivider(color = Color.LightGray)
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