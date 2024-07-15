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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.Padding
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.dataLayer.model.TextField
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
                id = R.drawable.ic_10,
                shape = CircleShape,
                colorImage = ColorFilter.tint(color = colorResource(id = R.color.colorInetYellow)),
                sizeButton = 40.dp,
                sizeImage = 25.dp,
                modifier = Modifier
                    .padding(start = 15.dp, bottom = 10.dp),
                onClick = { showSettingsDialog = true }
            )

            ImageButton(
                id = R.drawable.ic_letter_x,
                shape = CircleShape,
                colorImage = ColorFilter.tint(color = Color.Black),
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
                .background(color = Color.White)
        ) {
            Column {

                val searchStrList = uiState.filters?.searchText?.split(" ")
                val visibilityField =
                    if (uiState.settingsItems.firstOrNull { it.key == "column_name" }?.isEnabled == true) View.VISIBLE else View.GONE

                var _isActiveFiltered = false
                val itemsUI = uiState.items.filter { itemUI ->
                    uiState.filters?.let { filters ->
                        itemUI.fields.forEach { fieldValue ->
                            if (fieldValue.key.equals(filters.rangeDataByKey.key, true)) {
                                if (((fieldValue.value.rawValue as? Long)?: 0) < (filters.rangeDataByKey.start?.atStartOfDay(ZoneId.systemDefault())
                                        ?.toInstant()?.toEpochMilli() ?: 0)
                                    || ((fieldValue.value.rawValue as? Long)?: 0) > (filters.rangeDataByKey.end?.atTime(LocalTime.MAX)
                                        ?.atZone(ZoneId.systemDefault())?.toInstant()
                                        ?.toEpochMilli() ?: 0)
                                ) {
                                    _isActiveFiltered = true
                                    return@filter false
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

                Text(
                    text = uiState.title, fontSize = 16.sp, modifier = Modifier
                        .padding(7.dp)
                        .align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .padding(10.dp)
                ) {

                    TextFieldInputRounded(
                        value = uiState.filters?.searchText ?: "",
                        onValueChange = {
                            val filters = Filters(
                                RangeDate(
                                    uiState.filters?.rangeDataByKey?.key,
                                    uiState.filters?.rangeDataByKey?.start,
                                    uiState.filters?.rangeDataByKey?.end
                                ),
                                it
                            )
                            viewModel.updateFilters(filters)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ImageButton(id = if (isActiveFiltered) R.drawable.ic_filterbold else R.drawable.ic_filter,
                        sizeButton = 55.dp,
                        sizeImage = 25.dp,
                        modifier = Modifier.padding(start = 7.dp),
                        onClick = { showFilteringDialog = true }
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(7.dp)
                        .shadow(4.dp)
                        .background(Color.White)
                ) {
                    LazyColumnScrollbar(
                        modifier = Modifier.padding(5.dp),
                        state = listState,
                        settings = ScrollbarSettings(
                            alwaysShowScrollbar = true,
                            thumbUnselectedColor = Color.Gray,
                            thumbSelectedColor = Color.Gray,
                            thumbShape = CircleShape,
                        ),
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(end = 15.dp),
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
                                        .padding(7.dp)
                                        .shadow(4.dp)
                                        .border(1.dp, Color.LightGray)
                                        .then(item.modifierContainer?.background?.let {
                                            Modifier.background(it)
                                        } ?: Modifier.background(Color.White))
                                ) {
                                    Row(Modifier.padding(7.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .padding(7.dp)
                                                .border(1.dp, Color.Black)
                                                .background(Color.White)
                                                .align(alignment = Alignment.CenterVertically)
                                        ) {
                                            viewModel.idResImage?.let {
                                                Image(
                                                    painter = painterResource(it),
                                                    modifier = Modifier
                                                        .size(100.dp),
                                                    contentDescription = null
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            item.fields.forEach {
                                                ItemFieldValue(it, visibilityField)
                                            }
                                        }

                                        item.rawObj.firstOrNull{ it is AdditionalRequirementsMarkDB }?.let {
                                            it as AdditionalRequirementsMarkDB
                                            val text = it.score ?: "0"
                                            TextInStrokedCircle(
                                                modifier = Modifier.padding(2.dp),
                                                text = text,
                                                circleColor = if (text == "0") Color.Red else Color.Gray,
                                                textColor = if (text == "0") Color.Red else Color.Gray,
                                                circleSize = 30.dp,
                                                textSize = 16f.toPx(),
                                                strokeWidth = 2f.toPx()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(viewModel, onDismiss = { showSettingsDialog = false })
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
fun Float.toPx() = with(LocalDensity.current) { this@toPx.sp.toPx() }

@Composable
private fun TextFieldInputRounded(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit
) {
    var isFocusedSearchView by remember { mutableStateOf(false) }

    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        cursorColor = Color.Black,
        focusedIndicatorColor = Color.White,
        unfocusedIndicatorColor = Color.White,
    )

    TextField(
        value = value,
        onValueChange = onValueChange,
        colors = textFieldColors,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        label = {
            if (!isFocusedSearchView) Text(
                "Пошук",
                color = colorResource(id = R.color.hintColorDefault)
            )
        },
        trailingIcon = {
            Image(
                painter = painterResource(id = com.google.android.material.R.drawable.ic_search_black_24),
                contentDescription = ""
            )
        },
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .onFocusChanged { isFocusedSearchView = it.isFocused }
    )
}

@Composable
private fun ItemFieldValue(it: FieldValue, visibilityField: Int? = null) {
    Row(Modifier.fillMaxWidth()) {
        if (visibilityField == View.VISIBLE) {
            ItemTextField(it.field, it.field.modifierValue?.weight?.let { Modifier.weight(it) })
        }
        ItemTextField(it.value, it.value.modifierValue?.weight?.let{ Modifier.weight(it) })
    }
}

@Composable
private fun ItemTextField(it: TextField, modifier: Modifier? = null) {
    Text(
        text = it.value,
        fontWeight = it.modifierValue?.fontWeight,
        fontStyle = it.modifierValue?.fontStyle,
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
    circleSize: Dp,
    textSize: Float,
    strokeWidth: Float
) {
    Canvas(modifier = modifier.size(circleSize)) {
        val radius = size.minDimension / 2
        drawCircle(
            color = circleColor,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
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
                    onValueChange = { searchStr = it }
                )

                Row {
                    DatePickerExample("Дата з:", selectedFilterDateStart) { selectedFilterDateStart = it }
                    DatePickerExample("Дата по:", selectedFilterDateEnd) { selectedFilterDateEnd = it}
                }

                Row {
                    Button(
                        onClick = {
                            onChanged.invoke(Filters(
                                RangeDate(
                                    viewModel.filters?.rangeDataByKey?.key,
                                    selectedFilterDateStart,
                                    selectedFilterDateEnd
                                ),
                                searchStr
                            ))
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
                            onChanged.invoke(Filters(
                                RangeDate(
                                    viewModel.filters?.rangeDataByKey?.key,
                                    LocalDate.now(),
                                    LocalDate.now()
                                ),
                                ""
                            ))
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
fun SettingsDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {

    val uiState by viewModel.uiState.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 40.dp)
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
                    fontWeight = FontWeight.Bold,
                    text = viewModel.getTranslateString(stringResource(id = R.string.setting_column_visibility))
                )
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
                                    MerchModifier(fontWeight = FontWeight.Bold, padding = Padding(10.dp, 7.dp, 10.dp, 7.dp))
                                ),
                                TextField(
                                    "",
                                    viewModel.getTranslateString(stringResource(id = R.string.visibility)),
                                    MerchModifier(fontWeight = FontWeight.Bold, padding = Padding(10.dp, 7.dp, 10.dp, 7.dp), weight = 1f, alignment = Alignment.End)
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