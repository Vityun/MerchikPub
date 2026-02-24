package ua.com.merchik.merchik.features.main.componentsUI

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.ItemUI
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.StateUI


@Composable
fun ItemRow(
    item: DataItemUI,
    visibilityColumName: Int,
    uiState: StateUI,
    viewModel: MainViewModel,
    context: Context
) {
    val key = item.stableId
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val haptics = LocalHapticFeedback.current

    // Если используешь disappearingKey – можно пробросить его изнаружи
    var isVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = isVisible,
        modifier = Modifier
            .fillMaxWidth(),
//            .animateItemPlacement(animationSpec = tween(300)), // плавное смещение соседей
        exit = shrinkVertically(
            animationSpec = tween(250),
            shrinkTowards = Alignment.Top
        ) + fadeOut(tween(180))
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { coords = it }
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { _ ->
                            coords?.let { c ->
                                val pos = c.positionInRoot()
                                val size = c.size

                                // TODO: сюда возвращаешь свою логику dragging/Flying/shrinking
                                // dragging = Dragging(
                                //     item = item,
                                //     size = size,
                                //     offset = mutableStateOf(Offset(pos.x, pos.y))
                                // )

                                haptics.performHapticFeedback(
                                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                                )
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // TODO: dragging?.offset?.let { it.value += dragAmount }
                        },
                        onDragEnd = {
                            // TODO: dragging -> shrinking и т.п.
                            // dragging = null
                        },
                        onDragCancel = {
                            // TODO: аналогично onDragEnd
                            // dragging = null
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
                },
                onLongClickItem = {
                    viewModel.onLongClickItem(it, context)
                },
                onClickItemImage = {
                    viewModel.onClickItemImage(it, context)
                },
                onMultipleClickItemImage = { dataItem, index ->
                    viewModel.onClickItemImage(dataItem, context, index)
                },
                onCheckItem = { checked, dataItem ->
                    viewModel.updateItemSelect(checked, dataItem)
                }
            )
        }
    }
}


