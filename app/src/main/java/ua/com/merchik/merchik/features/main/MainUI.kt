package ua.com.merchik.merchik.features.main

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.TextPaint
import android.view.View
import android.widget.DatePicker
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.Padding
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenu
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Collections
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun MainUI(viewModel: MainViewModel, context: Context) {

    val uiState by viewModel.uiState.collectAsState()

    var isActiveFiltered by remember { mutableStateOf(false) }

    var showSettingsDialog by remember { mutableStateOf(false) }

    var showSortingDialog by remember { mutableStateOf(false) }

    var showFilteringDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                onClick = { showSettingsDialog = true }
            )

            ImageButton(
                id = R.drawable.ic_refresh,
                shape = CircleShape,
                colorImage = ColorFilter.tint(color = Color.Gray),
                sizeButton = 40.dp,
                sizeImage = 25.dp,
                modifier = Modifier
                    .padding(start = 15.dp, bottom = 10.dp),
                onClick = { viewModel.updateContent() }
            )

            ImageButton(
                id = R.drawable.ic_letter_x,
                shape = CircleShape,
                colorImage = ColorFilter.tint(color = Color.Gray),
                sizeButton = 40.dp,
                sizeImage = 25.dp,
                modifier = Modifier
                    .padding(start = 15.dp, bottom = 10.dp),
                onClick = { (context as? Activity)?.finish() }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(color = colorResource(id = R.color.main_form))
        ) {
            Column {

                val searchStrList = uiState.filters?.searchText?.split(" ")
                val visibilityField =
                    if (uiState.settingsItems.firstOrNull { it.key == "column_name" }?.isEnabled == true) View.VISIBLE else View.GONE

                var _isActiveFiltered = false

                val itemsUI = uiState.items.filter { itemUI ->
                    uiState.filters?.let { filters ->
                        filters.rangeDataByKey?.let { rangeDataByKey ->
                            itemUI.fields.forEach { fieldValue ->
                                if (fieldValue.key.equals(rangeDataByKey.key, true)) {
                                    if (((fieldValue.value.rawValue as? Long)?: 0) < (rangeDataByKey.start?.atStartOfDay(ZoneId.systemDefault())
                                            ?.toInstant()?.toEpochMilli() ?: 0)
                                        || ((fieldValue.value.rawValue as? Long)?: 0) > (rangeDataByKey.end?.atTime(LocalTime.MAX)
                                            ?.atZone(ZoneId.systemDefault())?.toInstant()
                                            ?.toEpochMilli() ?: 0)
                                    ) {
                                        _isActiveFiltered = true
                                        return@filter false
                                    }
                                }
                            }
                        }
                    }

                    var isFound: Boolean
                    searchStrList?.forEach {
                        isFound = false
                        itemUI.fields.forEach inner@{ fieldValue ->
                            if (fieldValue.value.value.contains(it, true)) {
                                isFound = true
                                return@inner
                            }
                        }
                        if (!isFound) {
                            _isActiveFiltered = true
                            return@filter false
                        }
                    }
                    return@filter true
                }

                isActiveFiltered = _isActiveFiltered

                uiState.title?.let {
                    Text(
                        text = it, fontSize = 16.sp, modifier = Modifier
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
                    Text(
                        text = it,
                        modifier = Modifier
                            .padding(start = 10.dp, bottom = 7.dp, end = 10.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(start = 10.dp, bottom = 10.dp, end = 10.dp)
                ) {

                    TextFieldInputRounded(
                        value = uiState.filters?.searchText ?: "",
                        onValueChange = {
                            val filters = Filters(
                                uiState.filters?.rangeDataByKey,
                                it
                            )
                            viewModel.updateFilters(filters)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)

                    )

                    ImageButton(id = R.drawable.ic_plus,
                        sizeButton = 40.dp,
                        sizeImage = 20.dp,
                        modifier = Modifier.padding(start = 7.dp),
                        onClick = {  }
                    )

                    ImageButton(id = R.drawable.ic_sort_down,
                        sizeButton = 40.dp,
                        sizeImage = 20.dp,
                        modifier = Modifier.padding(start = 7.dp),
                        onClick = { showSortingDialog = true }
                    )

                    ImageButton(id = if (isActiveFiltered) R.drawable.ic_filterbold else R.drawable.ic_filter,
                        sizeButton = 40.dp,
                        sizeImage = 20.dp,
                        modifier = Modifier.padding(start = 7.dp),
                        onClick = { showFilteringDialog = true }
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
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
                            items(itemsUI) { item ->
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.onClickItem(
                                                item,
                                                context
                                            )
                                        }
                                        .fillMaxWidth()
                                        .padding(end = 10.dp, bottom = 7.dp)
                                        .shadow(4.dp, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.LightGray)
                                        .then(item.modifierContainer?.background?.let {
                                            Modifier.background(it)
                                        } ?: Modifier.background(
                                            if (item.selected) colorResource(id = R.color.selected_item) else Color.White
                                        ))
                                ) {
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
                                                Image(
                                                    painter = painterResource(idResImage),
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.FillWidth,
                                                    contentDescription = null
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(2f)) {
                                            item.fields.forEachIndexed { index, field ->
                                                if (!field.key.equals("id_res_image",true)) {
                                                    ItemFieldValue(field, visibilityField)
                                                    if (index < item.fields.size - 1) HorizontalDivider()
                                                }
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.align(Alignment.TopEnd)) {

                                        if (viewModel.contextUI == ContextUI.ONE_SELECT || viewModel.contextUI == ContextUI.MULTI_SELECT) {
                                            RoundCheckbox(
                                                modifier = Modifier.padding(
                                                    top = 3.dp,
                                                    end = 3.dp
                                                ),
                                                checked = item.selected,
                                                aroundColor = if (item.selected) colorResource(
                                                    id = R.color.selected_item
                                                ) else Color.White,
                                                onCheckedChange = { checked ->
                                                    viewModel.updateItemSelect(checked, item)
                                                }
                                            )
                                        }

                                        item.rawObj.firstOrNull { it is AdditionalRequirementsMarkDB }
                                            ?.let {
                                                it as AdditionalRequirementsMarkDB
                                                val text = it.score ?: "0"
                                                TextInStrokedCircle(
                                                    modifier = Modifier.padding(
                                                        top = 3.dp,
                                                        end = 3.dp
                                                    ),
                                                    text = text,
                                                    circleColor = if (text == "0") Color.Red else Color.Gray,
                                                    textColor = if (text == "0") Color.Red else Color.Gray,
                                                    aroundColor = if (item.selected) colorResource(
                                                        id = R.color.selected_item
                                                    ) else Color.White,
                                                    circleSize = 30.dp,
                                                    textSize = 20f.toPx(),
                                                )
                                            }
                                    }
                                }
                            }
                        }
                    }

                    Row {
                        Tooltip(text = viewModel.getTranslateString(stringResource(id = R.string.total_number_selected, itemsUI.size))) {
                            Text(
                                text = "\u2211 ${itemsUI.size}",
                                fontSize = 16.sp,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
                            )
                        }

                        Tooltip(text = viewModel.getTranslateString(stringResource(id = R.string.total_number_selected, 0))) {
                            Text(
                                text = "⚲ ${0}",
                                fontSize = 16.sp,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
                            )
                        }

                        Tooltip(text = viewModel.getTranslateString(stringResource(id = R.string.total_number_selected, 0))) {
                            Text(
                                text = "\u2207 ${0}",
                                fontSize = 16.sp,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (viewModel.contextUI == ContextUI.ONE_SELECT || viewModel.contextUI == ContextUI.MULTI_SELECT) {
                            val selectedCount = itemsUI.filter { it.selected }.size
                            Tooltip(text = viewModel.getTranslateString(stringResource(id = R.string.total_number_marked, selectedCount))) {
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

                if (viewModel.contextUI == ContextUI.ONE_SELECT || viewModel.contextUI == ContextUI.MULTI_SELECT) {
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
                            Text(viewModel.getTranslateString(stringResource(id = R.string.cancel)))
                        }

                        val selectedItems = itemsUI.filter { it.selected }
                        Button(
                            onClick = {
                                if (selectedItems.isNotEmpty()) {
                                    viewModel.onSelectedItemsUI(selectedItems)
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
                                "${viewModel.getTranslateString(stringResource(id = R.string.choice))} " +
                                        if (selectedItems.isNotEmpty()) "(${selectedItems.size})" else ""
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(viewModel, onDismiss = { showSettingsDialog = false })
    }

    if (showSortingDialog) {
        SortingDialog(viewModel, onDismiss = { showSortingDialog = false })
    }

    if (showFilteringDialog) {
        FilteringDialog(viewModel,
            onDismiss = { showFilteringDialog = false },
            onChanged = {
                viewModel.updateFilters(it)
                showFilteringDialog = false
            }
        )
    }
}

@Composable
fun Tooltip(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var time by remember { mutableIntStateOf(5) }

    Box(modifier = modifier.pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                time = 5
                isVisible = true
            },
        )
    }) {
        content()
        if (isVisible) {
            Popup(
                offset = IntOffset(0, -100),
                alignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            1.dp,
                            colorResource(id = R.color.borderToolTip),
                            RoundedCornerShape(8.dp)
                        )
                        .background(
                            colorResource(id = R.color.backgroundToolTip),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            time = 0
                            isVisible = false
                        }
                ) {
                    Text(
                        modifier = Modifier
                            .widthIn(max = 200.dp)
                            .padding(7.dp),
                        text = text,
                        color = Color.Black
                    )
                }
            }
        }
    }

    LaunchedEffect(time) {
        delay(1000L)
        if (time <= 0) isVisible = false
        else time--
    }
}

@Composable
fun Float.toPx() = with(LocalDensity.current) { this@toPx.sp.toPx() }

@Composable
private fun TextFieldInputRounded(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit
) {
    var isFocusedSearchView by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .onFocusChanged { isFocusedSearchView = it.isFocused }
    ) {
        if (!isFocusedSearchView)
            Text(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 7.dp),
                text = "Пошук",
                fontSize = 16.sp,
                color = colorResource(id = R.color.hintColorDefault),
            )

        Row{
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle.Default.copy(color = Color.Black, fontSize = 16.sp),
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 7.dp)
                    .weight(1f)
            )

            Image(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 7.dp, start = 7.dp)
                    .fillMaxHeight(),
                painter = painterResource(id = com.google.android.material.R.drawable.ic_search_black_24),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorResource(id = R.color.hintColorDefault))
            )
        }
    }
}

@Composable
private fun ItemFieldValue(it: FieldValue, visibilityField: Int? = null) {
    Row(Modifier.fillMaxWidth()) {
        if (visibilityField == View.VISIBLE) {
            ItemTextField(it.field, Modifier.weight(1f))
        }
        ItemTextField(it.value, Modifier.weight(1f))
    }
}

@Composable
private fun ItemTextField(it: TextField, modifier: Modifier? = null) {
    Text(
        text = it.value,
        fontWeight = it.modifierValue?.fontWeight,
        fontStyle = it.modifierValue?.fontStyle,
        color = it.modifierValue?.textColor ?: Color.Black,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = (modifier ?: Modifier)
            .padding(
                start = it.modifierValue?.padding?.start ?: 0.dp,
                top = it.modifierValue?.padding?.top ?: 0.dp,
                end = it.modifierValue?.padding?.end ?: 0.dp,
                bottom = it.modifierValue?.padding?.bottom ?: 0.dp,
            )
            .then(it.modifierValue?.alignment?.let {
                Modifier.wrapContentWidth(it)
            } ?: Modifier)
            .then(it.modifierValue?.background?.let {
                Modifier.background(color = it)
            } ?: Modifier)
    )
}

@Composable
fun RoundCheckbox(
    modifier: Modifier = Modifier,
    aroundColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .size(45.dp)
            .clip(CircleShape)
            .background(aroundColor)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = modifier.size(30.dp)) {
            drawCircle(
                color = Color.White,
            )
            drawCircle(
                color = Color.Gray,
                style = Stroke(width = 1.dp.toPx())
            )
            // Draw checkmark if checked
            if (checked) {
                drawLine(
                    color = Color.Blue,
                    start = center - Offset(6.dp.toPx(), 0.dp.toPx()),
                    end = center + Offset(-2.dp.toPx(), 6.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = Color.Blue,
                    start = center + Offset(-2.dp.toPx(), 6.dp.toPx()),
                    end = center + Offset(6.dp.toPx(), -6.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun DragAndDropList() {
    val items = remember { mutableStateListOf("Item 1", "Item 2", "Item 3", "Item 4") }
    val coroutineScope = rememberCoroutineScope()
    var draggedIndex by remember { mutableStateOf(-1) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var startIndex by remember { mutableStateOf(-1) }
    val elevation = remember { Animatable(0f) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)) {
        items.forEachIndexed { index, item ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(50.dp)
                    .zIndex(if (index == draggedIndex) 1f else 0f)
                    .offset {
                        if (index == draggedIndex) {
                            IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt())
                        } else {
                            IntOffset(0, 0)
                        }
                    }
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .shadow(elevation.value.dp, RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                draggedIndex = index
                                startIndex = index
                                coroutineScope.launch {
                                    elevation.animateTo(4f, tween(durationMillis = 300))
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += Offset(dragAmount.x, dragAmount.y)

                                val offsetY = dragOffset.y.roundToInt()
                                val newIndex =
                                    (startIndex + offsetY / 60).coerceIn(0, items.size - 1)

                                if (newIndex != draggedIndex) {
                                    coroutineScope.launch {
                                        Collections.swap(items, draggedIndex, newIndex)
                                        draggedIndex = newIndex
                                    }
                                }
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    elevation.animateTo(0f, tween(durationMillis = 300))
                                }
                                draggedIndex = -1
                                dragOffset = Offset.Zero
                            }
                        )
                    }
            ) {
                Text(
                    text = item,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun TextInStrokedCircle(
    modifier: Modifier = Modifier,
    text: String,
    circleColor: Color,
    textColor: Color,
    aroundColor: Color,
    circleSize: Dp,
    textSize: Float,
) {
    Box(
        modifier = Modifier
            .size(45.dp)
            .clip(CircleShape)
            .background(aroundColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = modifier.size(circleSize)) {
            drawCircle(
                color = Color.White,
            )
            drawCircle(
                color = circleColor,
                style = Stroke(width = 1.dp.toPx())
            )

            drawIntoCanvas { canvas ->
                val paint = TextPaint().apply {
                    this.color = textColor.toArgb()
                    this.textSize = textSize
                    this.textAlign = android.graphics.Paint.Align.CENTER
                }
                val x = center.x
                val y = center.y - (paint.descent() + paint.ascent()) / 2
                canvas.nativeCanvas.drawText(text, x, y, paint)
            }
        }
    }
}

@Composable
fun TextInCircle(
    modifier: Modifier = Modifier,
    text: String,
    circleColor: Color,
    textColor: Color,
    circleSize: Dp,
    textSize: Float
) {
    Box(modifier = modifier.size(circleSize)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = circleColor,
                radius = size.minDimension / 2
            )

            drawIntoCanvas { canvas ->
                val paint = TextPaint().apply {
                    this.color = textColor.toArgb()
                    this.textSize = textSize
                    this.textAlign = android.graphics.Paint.Align.CENTER
                }
                val x = size.width / 2
                val y = size.height / 2 - (paint.descent() + paint.ascent()) / 2
                canvas.nativeCanvas.drawText(text, x, y, paint)
            }
        }
    }
}

@Composable
fun ImageButton(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    @DrawableRes id: Int,
    sizeButton: Dp,
    sizeImage: Dp,
    backGround: ButtonColors = ButtonDefaults.buttonColors(containerColor = Color.White),
    colorImage: ColorFilter = ColorFilter.tint(color = colorResource(id = R.color.black)),
    onClick: () -> Unit
) {
    Button(
        onClick = { onClick.invoke() },
        shape = shape,
        colors = backGround,
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .size(sizeButton)
            .shadow(4.dp, shape = shape)
            .clip(shape)
    ) {
        Image(
            painter = painterResource(id),
            contentDescription = "",
            contentScale = ContentScale.Inside,
            colorFilter = colorImage,
            modifier = Modifier
                .clip(shape)
                .size(sizeImage)
        )
    }
}

@Composable
fun SettingsItemView(item: SettingsItemUI) {
    var isChecked by remember { mutableStateOf(item.isEnabled) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = item.text, modifier = Modifier
            .padding(start = 10.dp)
            .align(Alignment.CenterVertically))

        Spacer(modifier = Modifier.weight(1f))

        Checkbox(
            checked = isChecked,
            onCheckedChange = { checked ->
                isChecked = checked
                item.isEnabled = checked
            }
        )
    }
}

@Composable
fun FilteringDialog(viewModel: MainViewModel,
                             onDismiss: () -> Unit,
                             onChanged: (Filters) -> Unit) {

    var searchStr by remember { mutableStateOf(viewModel.filters?.searchText ?: "") }
    var selectedFilterDateStart by remember { mutableStateOf(viewModel.filters?.rangeDataByKey?.start ?: LocalDate.now()) }
    var selectedFilterDateEnd by remember { mutableStateOf(viewModel.filters?.rangeDataByKey?.end ?: LocalDate.now()) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(color = Color.Transparent)
        ) {
            ImageButton(
                id = R.drawable.ic_letter_x,
                shape = CircleShape,
                colorImage = ColorFilter.tint(color = Color.Gray),
                sizeButton = 40.dp,
                sizeImage = 25.dp,
                modifier = Modifier
                    .padding(start = 15.dp, bottom = 10.dp)
                    .align(alignment = Alignment.End),
                onClick = { onDismiss.invoke() }
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = Color.White)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        fontWeight = FontWeight.Bold,
                        text = viewModel.getTranslateString(stringResource(id = R.string.search))
                    )
                    TextFieldInputRounded(
                        value = searchStr,
                        onValueChange = { searchStr = it },
                        modifier = Modifier
                            .height(45.dp)
                            .padding(5.dp)
                    )

                    if (viewModel.filters?.rangeDataByKey != null) {
                        Row {
                            DatePickerExample(
                                "Дата з:",
                                selectedFilterDateStart
                            ) { selectedFilterDateStart = it }
                            DatePickerExample(
                                "Дата по:",
                                selectedFilterDateEnd
                            ) { selectedFilterDateEnd = it }
                        }
                    }

                    Row {
                        Button(
                            onClick = {
                                onChanged.invoke(
                                    Filters(
                                        viewModel.filters?.let {
                                            RangeDate(
                                                it.rangeDataByKey?.key,
                                                selectedFilterDateStart,
                                                selectedFilterDateEnd
                                            )
                                        },
                                        searchStr
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.apply)))
                        }

                        Button(
                            onClick = {
                                onChanged.invoke(
                                    Filters(
                                        viewModel.filters?.let {
                                            RangeDate(
                                                it.rangeDataByKey?.key,
                                                LocalDate.now(),
                                                LocalDate.now()
                                            )
                                        },
                                        ""
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.orange)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.clear)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DatePickerExample(title: String, date: LocalDate?, dateChange: (date: LocalDate) -> Unit) {
    var selectedDate by remember { mutableStateOf(date ?: LocalDate.now()) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${title}:\n${selectedDate.format(dateFormat)}",
            modifier = Modifier.clickable { showDialog = true }
        )

        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val datePicker = DatePicker(context).apply {
                            init(selectedDate.year, selectedDate.month.value, selectedDate.dayOfMonth) { _, year, month, dayOfMonth ->
                                selectedDate = LocalDate.of(year, month, dayOfMonth)
                                dateChange.invoke(selectedDate)
                            }
                        }

                        AndroidView(
                            factory = { datePicker },
                            modifier = Modifier.wrapContentWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                // Confirm date selection
                                showDialog = false
                            }) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SortingDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
//    val sortingFieldFirst by remember {
//
//    }


    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 40.dp)
                .background(color = Color.Transparent)
        ) {
            ImageButton(
                id = R.drawable.ic_letter_x,
                shape = CircleShape,
                colorImage = ColorFilter.tint(color = Color.Gray),
                sizeButton = 40.dp,
                sizeImage = 25.dp,
                modifier = Modifier
                    .padding(start = 15.dp, bottom = 10.dp)
                    .align(alignment = Alignment.End),
                onClick = { onDismiss.invoke() }
            )

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
                        text = viewModel.getTranslateString(stringResource(id = R.string.setting_table))
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = viewModel.getTranslateString(stringResource(id = R.string.setting_column_visibility_desc))
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
                        Column(
                            modifier = Modifier.padding(7.dp)
                        ) {

                            val itemsSorting = uiState.settingsItems.filter { !it.key.equals("column_name", true) }.map { it.text }

                            DropDownSortingList(
                                "Сортировать по:",
                                {  },
                                itemsSorting.map { SortingField(it, Order.ASC) }
                            )

                            Spacer(modifier = Modifier.padding(10.dp))

                            DropDownSortingList(
                                "Затем по:",
                                {
                                
                                },
                                itemsSorting.map { SortingField(it, Order.ASC) }
                            )

                            Spacer(modifier = Modifier.padding(10.dp))

                            DropDownSortingList(
                                "Затем по:",
                                {  },
                                itemsSorting.map { SortingField(it, Order.ASC) }
                            )

                        }
                    }
                    Row {
                        Button(
                            onClick = {
                                onDismiss.invoke()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.cancel)))
                        }

                        Button(
                            onClick = {
                                onDismiss.invoke()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.orange)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.save)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DropDownSortingList(
    title: String,
    onSelectedItemIndex: (Int) -> Unit,
    items: List<SortingField>
) {
    var selectedItem by remember { mutableStateOf(SortingField()) }

    Text(text = title)
    Row {
        Row(
            modifier = Modifier
                .height(40.dp)
                .weight(1f)
                .border(
                    BorderStroke(
                        1.dp,
                        colorResource(id = R.color.borderContextMenu)
                    ), RoundedCornerShape(8.dp)
                )
        ) {
            ContextMenu(
                onSelectedMenu = {
                    selectedItem = items[it]
                    onSelectedItemIndex.invoke(it)
                },
                itemsMenu = items.mapNotNull { it.title }
            ) {
                Row {
                    Text(
                        text = selectedItem.title ?: "",
                        modifier = Modifier
                            .padding(7.dp)
                            .weight(1f)
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_arrow_down_1),
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 7.dp)
                            .align(Alignment.CenterVertically),
                        contentScale = ContentScale.Inside,
                        contentDescription = null
                    )
                }
            }
        }
        ImageButton(id = R.drawable.ic_letter_x,
            sizeButton = 40.dp,
            sizeImage = 20.dp,
            colorImage = ColorFilter.tint(color = Color.Gray),
            modifier = Modifier.padding(start = 7.dp),
            onClick = {
                selectedItem = SortingField()
                onSelectedItemIndex.invoke(-1)
            }
        )
        ImageButton(id = if ((selectedItem.order ?: Order.ASC) == Order.ASC) R.drawable.ic_arrow_down_2 else R.drawable.ic_arrow_up_2,
            sizeButton = 40.dp,
            sizeImage = 20.dp,
            colorImage = ColorFilter.tint(color = Color.Gray),
            modifier = Modifier.padding(start = 7.dp),
            onClick = {
                selectedItem = selectedItem.copy(order = if (selectedItem.order == Order.ASC) Order.DESC else Order.ASC)
                onSelectedItemIndex.invoke(items.indexOf(selectedItem))
            }
        )
    }
}

@Composable
fun SettingsDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {

    val uiState by viewModel.uiState.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 40.dp)
                .background(color = Color.Transparent)
        ) {
            ImageButton(
                id = R.drawable.ic_letter_x,
                shape = CircleShape,
                colorImage = ColorFilter.tint(color = Color.Gray),
                sizeButton = 40.dp,
                sizeImage = 25.dp,
                modifier = Modifier
                    .padding(start = 15.dp, bottom = 10.dp)
                    .align(alignment = Alignment.End),
                onClick = { onDismiss.invoke() }
            )

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
                        text = viewModel.getTranslateString(stringResource(id = R.string.setting_table))
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = viewModel.getTranslateString(stringResource(id = R.string.setting_column_visibility_desc))
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
                        Column {
                            ItemFieldValue(
                                FieldValue(
                                    key = "",
                                    TextField(
                                        "",
                                        viewModel.getTranslateString(stringResource(id = R.string.column_name)),
                                        MerchModifier(
                                            fontWeight = FontWeight.Bold,
                                            padding = Padding(10.dp, 7.dp, 10.dp, 7.dp)
                                        )
                                    ),
                                    TextField(
                                        "",
                                        viewModel.getTranslateString(stringResource(id = R.string.visibility)),
                                        MerchModifier(
                                            fontWeight = FontWeight.Bold,
                                            padding = Padding(10.dp, 7.dp, 10.dp, 7.dp),
                                            weight = 1f,
                                            alignment = Alignment.End
                                        )
                                    )
                                ),
                                View.VISIBLE
                            )
                            HorizontalDivider(thickness = 1.dp)

                            LazyColumn {
                                items(uiState.settingsItems) { itemSettingsUI ->
                                    SettingsItemView(item = itemSettingsUI)
                                }
                            }
                        }
                    }

                    Row {
                        Button(
                            onClick = {
                                viewModel.updateContent()
                                onDismiss.invoke()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.cancel)))
                        }

                        Button(
                            onClick = {
                                viewModel.saveSettings()
                                viewModel.updateContent()
                                onDismiss.invoke()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.orange)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.save)))
                        }
                    }
                }
            }
        }
    }
}