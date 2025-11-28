package ua.com.merchik.merchik.features.main.componentsUI


import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.StateUI



@Composable
fun FullList(
    dataItemsUI: List<DataItemUI>,
    listState: LazyListState,
    visibilityColumName: Int,
    uiState: StateUI,
    viewModel: MainViewModel,
    context: Context
) {
    LazyColumnScrollbar(
        modifier = Modifier
            .padding(start = 10.dp, top = 10.dp, bottom = 7.dp)
            .fillMaxSize(),
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
                ItemRow(
                    item = item,
                    visibilityColumName = visibilityColumName,
                    uiState = uiState,
                    viewModel = viewModel,
                    context = context
                )
            }
        }
    }
}
