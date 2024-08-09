package ua.com.merchik.merchik.features.main.Main

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.model.ItemUI
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import ua.com.merchik.merchik.features.main.componentsUI.RoundCheckbox
import ua.com.merchik.merchik.features.main.componentsUI.TextFieldInputRounded
import ua.com.merchik.merchik.features.main.componentsUI.TextInStrokeCircle
import ua.com.merchik.merchik.features.main.componentsUI.Tooltip
import java.io.File
import java.time.LocalTime
import java.time.ZoneId

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

                fun getSortValue(it: ItemUI, sortingField: SortingField) =
                    it.fields.firstOrNull { fieldValue ->
                        fieldValue.key.equals(sortingField.key, true)
                    }?.value?.value

                fun comparator(sortingField: SortingField?): Comparator<ItemUI> =
                    if (sortingField?.order == 1)
                        compareBy { getSortValue(it, sortingField) }
                    else if (sortingField?.order == -1)
                        compareByDescending { getSortValue(it, sortingField) }
                    else
                        compareBy { 0 }

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
                }.sortedWith(
                    comparator(uiState.sortingFields.getOrNull(0))
                        .thenComparing(comparator(uiState.sortingFields.getOrNull(1)))
                        .thenComparing(comparator(uiState.sortingFields.getOrNull(2)))
                )

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
                                                        }
                                                    }
                                                }

                                                if (images.size <= 1) {
                                                    Image(
                                                        painter = images[0],
                                                        modifier = Modifier
                                                            .padding(5.dp)
                                                            .size(100.dp),
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
                                                                    .size(100.dp),
                                                                contentScale = ContentScale.FillWidth,
                                                                contentDescription = null
                                                            )
                                                        }
                                                    }
                                                }
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
                                                TextInStrokeCircle(
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

fun setContentInComposeView(composeView: ComposeView, viewModel: MainViewModel, context: Context) {
    composeView.setContent {
        MainUI(viewModel, context)
    }
}