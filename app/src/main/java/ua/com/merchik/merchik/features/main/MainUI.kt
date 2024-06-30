package ua.com.merchik.merchik.features.main

import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.Padding
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.dataLayer.model.TextField
import java.util.Collections
import kotlin.math.roundToInt

@Composable
fun MainUI() {
    MainUI(viewModel = viewModel())
}

@Composable
internal fun MainUI(viewModel: MainViewModel) {

    val uiState by viewModel.uiState.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    var showSortingDialog by remember { mutableStateOf(false) }

    var showFilteringDialog by remember { mutableStateOf(false) }

    var searchStr by remember { mutableStateOf("") }

    var isFocusedSearchView by remember { mutableStateOf(false) }

    Column {

        ImageButton(
            id = R.drawable.ic_10,
            shape = CircleShape,
            colorImage = ColorFilter.tint(color = colorResource(id = R.color.colorInetYellow)),
            sizeButton = 45.dp,
            sizeImage = 25.dp,
            modifier = Modifier.padding(start = 7.dp).align(alignment = Alignment.End),
            onClick = { showSettingsDialog = true }
        )

        Text(text = uiState.title, fontSize = 24.sp, modifier = Modifier
            .padding(7.dp)
            .align(Alignment.CenterHorizontally))

        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            cursorColor = Color.Black,
            focusedIndicatorColor = Color.White,
            unfocusedIndicatorColor = Color.White,
        )

        Row(
            modifier = Modifier
                .padding(10.dp)
        ) {
            TextField(
                value = searchStr,
                onValueChange = { searchStr = it },
                colors = textFieldColors,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                label = {
                    if (!isFocusedSearchView) Text("Пошук", color = colorResource(id = R.color.hintColorDefault))
                },
                trailingIcon = {
                    Image(
                        painter = painterResource(id = com.google.android.material.R.drawable.ic_search_black_24),
                        contentDescription = "")
                },
                modifier = Modifier
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = Color.White)
                    .onFocusChanged { isFocusedSearchView = it.isFocused }
            )

            ImageButton(id = R.drawable.ic_filter,
                sizeButton = 55.dp,
                sizeImage = 25.dp,
                modifier = Modifier.padding(start = 7.dp),
                onClick = { showFilteringDialog = true }
            )

            ImageButton(id = R.drawable.ic_2,
                sizeButton = 55.dp,
                sizeImage = 25.dp,
                modifier = Modifier.padding(start = 7.dp),
                onClick = { showSortingDialog = true }
            )
        }

        LazyColumn {
            val searchStrList = searchStr.split(" ")
            val visibilityField = if (uiState.settingsItems.firstOrNull { it.key == "column_name" }?.isEnabled == true) View.VISIBLE else View.GONE
            var isColored = false
            items(uiState.items.filter { itemUI ->
                itemUI.fields.any { fieldValue ->
                    var isFounded = true
                    searchStrList.forEach {
                        isFounded = if (isFounded) fieldValue.value.value.contains(it, true) else false
                    }
                    isFounded
                }
            }) { item ->
                isColored = !isColored
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp)
                    .shadow(10.dp)
                    .border(1.dp, Color.Black)
                    .background(Color.White)
                ) {
                    Row(Modifier.padding(7.dp)) {
                        Box(modifier = Modifier
                            .padding(7.dp)
                            .border(1.dp, Color.Black)
                            .background(Color.White)
                            .align(alignment = Alignment.CenterVertically)
                        ) {
                            Image(
                                modifier = Modifier.size(100.dp),
                                bitmap = ImageBitmap.imageResource(id = R.mipmap.merchik),
                                contentDescription = null
                            )
                        }
                        Column {
                            item.fields.forEach {
                                ItemFieldValue(it, visibilityField)
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

    if (showSortingDialog) {
        SortingDialog(viewModel, onDismiss = { showSortingDialog = false })
    }

    if (showFilteringDialog) {
        FilteringDialog(viewModel, onDismiss = { showFilteringDialog = false })
    }
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
        text = "${it.value} ",
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

    Column(Modifier.fillMaxSize().padding(16.dp)) {
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
                                val newIndex = (startIndex + offsetY / 60).coerceIn(0, items.size - 1)

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
            modifier = Modifier.clip(shape).size(sizeImage)
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
internal fun FilteringDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp, bottom = 20.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = Color.White)
        ) {
            DragAndDropList()
        }
    }
}
@Composable
internal fun SortingDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp, bottom = 20.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = Color.White)
        ) {
            DragAndDropList()
        }
    }
}

@Composable
internal fun SettingsDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {

    val uiState by viewModel.uiState.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp, bottom = 20.dp)
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
                                TextField(
                                    viewModel.getTranslateString(stringResource(id = R.string.column_name)),
                                    MerchModifier(fontWeight = FontWeight.Bold, padding = Padding(10.dp, 7.dp, 10.dp, 7.dp))
                                ),
                                TextField(
                                    viewModel.getTranslateString(stringResource(id = R.string.visibility)),
                                    MerchModifier(fontWeight = FontWeight.Bold, padding = Padding(10.dp, 7.dp, 10.dp, 7.dp), weight = 1f, alignment = Alignment.End)
                                )
                            ),
                            View.VISIBLE
                        )
                        HorizontalDivider(thickness = 1.dp)

                        LazyColumn {
                            items(uiState.settingsItems) {
                                SettingsItemView(item = it)
                            }
                        }
                    }
                }

                Row {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
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