package ua.com.merchik.merchik.features.main.componentsUI

import android.content.Context
import android.view.View
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.ItemUI
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.StateUI

@Composable
fun StackCard(
    items: List<DataItemUI>,
    visibilityColumName: Int,
    uiState: StateUI,
    viewModel: MainViewModel,
    context: Context,
    onExpand: () -> Unit
) {
//    val visibilityColumName =
//        if (uiState.settingsItems.firstOrNull { it.key == "column_name" }?.isEnabled == true) View.VISIBLE else View.GONE

    val previewItems = remember(items) { items.take(3) } // достаточно нескольких

    val elevation by animateDpAsState(targetValue = 10.dp, label = "stack_elevation")
    val offsetY by animateDpAsState(targetValue = (-8).dp, label = "stack_offset_y")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
            .offset(y = offsetY)
            .clickable { onExpand() } // клик по стопке -> разворачиваем
    ) {
        previewItems.forEachIndexed { index, item ->
            val cardOffset = (index * 6).dp     // смещение вниз
            val cardAlpha = 1f - index * 0.18f // дальние карточки более прозрачные

            Card(
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = elevation - index.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .offset(y = cardOffset)
                    .zIndex((previewItems.size - index).toFloat())
                    .graphicsLayer { alpha = cardAlpha }
            ) {
                // Внутри можно показать компактный вид элемента
                ItemUI(
                    item = item,
                    visibilityColumName = visibilityColumName,
                    settingsItemUI = uiState.settingsItems,
                    contextUI = viewModel.modeUI,
                    onClickItem = {
                        // При клике по конкретному элементу тоже можно разворачивать
                        onExpand()
                        viewModel.onClickItem(it, context)
                    },
                    onLongClickItem = {
                        onExpand()
                        viewModel.onLongClickItem(it, context)
                    },
                    onClickItemImage = { viewModel.onClickItemImage(it, context) },
                    onMultipleClickItemImage = { dataItem, idx ->
                        viewModel.onClickItemImage(dataItem, context, idx)
                    },
                    onCheckItem = { checked, dataItem ->
                        viewModel.updateItemSelect(checked, dataItem)
                    }
                )
            }
        }
    }
}
