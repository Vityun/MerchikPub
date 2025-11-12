package ua.com.merchik.merchik.features.main.Main

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.componentsUI.DatePicker
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import ua.com.merchik.merchik.features.main.componentsUI.TextFieldInputRounded
import ua.com.merchik.merchik.features.main.componentsUI.Tooltip
import java.time.LocalDate

@Composable
fun FilteringDialog(viewModel: MainViewModel,
                    onDismiss: () -> Unit,
                    onChanged: (Filters) -> Unit) {

    val selectedFilterDateStart by viewModel.rangeDataStart.collectAsState()
    val selectedFilterDateEnd by viewModel.rangeDataEnd.collectAsState()
    var searchText = remember { viewModel.filters?.searchText ?: "" }

    var showToolTip by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    val listState = rememberLazyListState()

    ComposableLifecycle { source, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                FilteringDialogDataHolder.instance().filters = uiState.filters?.copy()
            }
            Lifecycle.Event.ON_RESUME -> {
                FilteringDialogDataHolder.instance().filters?.let {
                    viewModel.updateFilters(it)
                }
            }
            else -> {}
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .background(color = Color.Transparent)
        ) {
            Row(modifier = Modifier.align(Alignment.End)) {
                ImageButton(
                    id = R.drawable.ic_question_1,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 22.dp,
                    modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                    onClick = { showToolTip = true }
                )

                ImageButton(
                    id = R.drawable.ic_letter_x,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(color = Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                    onClick = { onDismiss.invoke() }
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = Color.White)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 10.dp, bottom = 7.dp, end = 10.dp),
                        fontWeight = FontWeight.Bold,
                        text = viewModel.uiState.value.filters?.title ?: "Фільтри"
                    )

                    viewModel.uiState.collectAsState().value.filters?.subTitle?.let {
                        Text(
                            text = it,
                            modifier = Modifier
                                .padding(start = 10.dp, bottom = 7.dp, end = 10.dp)
                        )
                    }

                    uiState.filters?.rangeDataByKey?.let {
                        if (it.enabled)
                            ItemDateFilterUI(it, selectedFilterDateStart, selectedFilterDateEnd,

                                onStartDateChanged = { date -> viewModel.setStartDate(date) },
                                onEndDateChanged = { date -> viewModel.setEndDate(date) })
                        else
                            Tooltip(text = viewModel.getTranslateString(stringResource(id = R.string.ui_filter_not_available_edit), 5998)) {
                                ItemDateFilterUI(it, selectedFilterDateStart, selectedFilterDateEnd,
                                    onStartDateChanged = { date -> viewModel.setStartDate(date) },
                                    onEndDateChanged = { date -> viewModel.setEndDate(date) })
                            }
                    }

                    TextFieldInputRounded(
                        viewModel = viewModel,
                        value = uiState.filters?.searchText ?: "",
                        onValueChange = {
                            searchText = it
                            val current = uiState.filters
                            if (current != null) {
                                viewModel.updateFilters(current.copy(searchText = it))
                                FilteringDialogDataHolder.instance().filters = current
                            }
                            else {
                                val filters = Filters(
                                    searchText = it
                                )
                                viewModel.updateFilters(filters)
                                FilteringDialogDataHolder.instance().filters = filters
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .height(40.dp)
                    )

                    uiState.filters?.items?.let {
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
                                items(it) {
                                    if (it.enabled)
                                        ItemFilterUI(viewModel, viewModel.context, it) { changedItemFilter ->
                                            uiState.filters?.items?.map {
                                                if (it.clazz == changedItemFilter.clazz) changedItemFilter
                                                else it
                                            }?.let { it1 ->
                                                uiState.filters?.copy(
                                                    items = it1
                                                )?.let { filters ->
                                                    FilteringDialogDataHolder.instance().filters = filters
                                                    viewModel.updateFilters(filters)
                                                }
                                            }
                                        }
                                    else {
                                        Tooltip(text = viewModel.getTranslateString(stringResource(id = R.string.ui_filter_not_available_edit), 5998)) {
                                            ItemFilterUI(viewModel, viewModel.context, it)
                                        }
                                    }
                                    Spacer(modifier = Modifier.padding(10.dp))
                                }
                            }
                        }
                    }


                    Row {
                        Button(
                            onClick = {
                                FilteringDialogDataHolder.instance().filters?.let {
                                    onChanged.invoke(it)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue)),
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.ui_apply), 5995))
                        }
                        Spacer(modifier = Modifier.padding(10.dp))
                        Button(
                            onClick = {
                                FilteringDialogDataHolder.instance().filters?.let {
                                    onChanged.invoke(it.copy(
                                        rangeDataByKey =
                                        if (it.rangeDataByKey?.enabled == true)
                                            it.rangeDataByKey.copy(
                                                start = LocalDate.now(),
                                                end = LocalDate.now()
                                            )
                                        else
                                            it.rangeDataByKey,
                                        searchText = "",
                                        items = it.items.map {
                                            if (it.enabled)
                                                it.copy(
                                                    rightValuesRaw = emptyList(),
                                                    rightValuesUI = emptyList()
                                                )
                                            else
                                                it
                                        }
                                    ))
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.orange)),
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.ui_clear), 5996))
                        }
                    }
                }
            }
        }
    }
    if (showToolTip) {

        MessageDialog(
            title = "Не доступно",
            status = DialogStatus.ALERT,
            message = "Данный раздел находится в стадии в разработки",
            okButtonName = "Ок",
            onDismiss = {
                showToolTip = false
            },
            onConfirmAction = {
                showToolTip = false
            }
        )
    }
}

//@Composable
//private fun ItemDateFilterUI(
//    it: RangeDate,
//    selectedFilterDateStart: LocalDate?,
//    selectedFilterDateEnd: LocalDate?
//) {
//    var selectedFilterDateStart1 = selectedFilterDateStart
//    var selectedFilterDateEnd1 = selectedFilterDateEnd
//    Row {
//        DatePicker(
//            "Дата з",
//            it.enabled,
//            selectedFilterDateStart1
//        ) { selectedFilterDateStart1 = it }
//        DatePicker(
//            "Дата по",
//            it.enabled,
//            selectedFilterDateEnd1
//        ) { selectedFilterDateEnd1 = it }
//    }
//}
@Composable
private fun ItemDateFilterUI(
    it: RangeDate,
    selectedFilterDateStart: LocalDate?,
    selectedFilterDateEnd: LocalDate?,
    onStartDateChanged: (LocalDate) -> Unit,
    onEndDateChanged: (LocalDate) -> Unit
) {
    Row {
        DatePicker(
            title = "Дата з",
            enabled = it.enabled,
            date = selectedFilterDateStart
        ) { newDate -> onStartDateChanged(newDate) }

        DatePicker(
            title = "Дата по",
            enabled = it.enabled,
            date = selectedFilterDateEnd
        ) { newDate -> onEndDateChanged(newDate) }
    }
}

@Composable
private fun ItemFilterUI(viewModel: MainViewModel, context: Context?, itemFilter: ItemFilter, onChanged: ((ItemFilter) -> Unit)? = null) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(end = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = itemFilter.title)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier
                    .clickable { if (itemFilter.rightValuesUI.size > 3) isExpanded = !isExpanded },
                text = if (isExpanded) "${itemFilter.rightValuesUI.size} элементов"
                else "${if(itemFilter.rightValuesUI.size >= 3) 3 else itemFilter.rightValuesUI.size} элемента из ${itemFilter.rightValuesUI.size}",
                style = TextStyle(
                    color = colorResource(id = R.color.blue),
                    textDecoration = TextDecoration.Underline
                )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        1.dp,
                        colorResource(id = R.color.borderContextMenu)
                    ), RoundedCornerShape(8.dp)
                )
                .then(if (itemFilter.enabled) Modifier.clickable {
                    (context as? Activity)?.let { itemFilter.onSelect(it) }
                } else Modifier),
        ) {
            Column {
                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(start = 3.dp, end = 25.dp)
                        .background(
                            color = colorResource(id = R.color.background_item_filter),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                colorResource(id = R.color.borderContextMenu)
                            ), RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(
                                start = 7.dp,
                                top = 3.dp,
                                bottom = 3.dp,
                                end = 7.dp
                            ),
                        text = viewModel.getTranslateString(stringResource(id = R.string.ui_text_add), 6000)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                itemFilter.rightValuesUI.forEachIndexed { index, item ->
                    if (!isExpanded && index > 3) return@forEachIndexed

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(start = 3.dp, end = 25.dp)
                            .background(
                                color = colorResource(id = R.color.background_item_filter),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    colorResource(id = R.color.borderContextMenu)
                                ), RoundedCornerShape(8.dp)
                            )
                    ) {
                        if (isExpanded || index < 3) {
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(
                                        start = 7.dp,
                                        top = 3.dp,
                                        bottom = 3.dp,
                                        end = 7.dp
                                    ),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2,
                                text = item ?: itemFilter.rightValuesRaw[index] ?: ""
                            )
                            if (itemFilter.enabled) {
                                Image(
                                    modifier = Modifier
                                        .size(25.dp)
                                        .padding(top = 7.dp, bottom = 7.dp, end = 7.dp)
                                        .clickable {
                                            onChanged?.invoke(
                                                itemFilter.copy(
                                                    rightValuesRaw = itemFilter.rightValuesRaw.filterIndexed { index1, _ -> index != index1 },
                                                    rightValuesUI = itemFilter.rightValuesUI.filterIndexed { index1, _ -> index != index1 }
                                                )
                                            )
                                            if (itemFilter.rightValuesUI.size <= 3+1) isExpanded = false
                                        },
                                    contentScale = ContentScale.Inside,
                                    painter = painterResource(id = R.drawable.ic_letter_x),
                                    contentDescription = "",
                                    colorFilter = ColorFilter.tint(color = colorResource(id = R.color.hintColorDefault))
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                }
            }

        }
    }
}