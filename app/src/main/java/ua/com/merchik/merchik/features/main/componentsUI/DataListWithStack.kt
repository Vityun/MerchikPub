package ua.com.merchik.merchik.features.main.componentsUI

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
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
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.StateUI


@Composable
fun DataListWithStack(
    dataItemsUI: List<DataItemUI>,
    listState: LazyListState,
    visibilityColumName: Int, // или твой тип
    uiState: StateUI,                          // твой uiState с settingsItems
    viewModel: MainViewModel,
    context: Context
) {
    var isCollapsed by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .focusable()
            .background(colorResource(id = R.color.main_form_list))
    ) {
        // Верхняя часть – анимируемая область (стопка / полный список)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .animateContentSize( // плавное изменение высоты контейнера
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
        ) {
            AnimatedContent(
                targetState = isCollapsed,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    if (!targetState) {
                        // Свернуто -> Развернуто: список выезжает из позиции стопки
                        (slideInVertically(
                            initialOffsetY = { fullHeight -> -fullHeight / 4 } // слегка сверху
                        ) + fadeIn()) togetherWith
                                (slideOutVertically(
                                    targetOffsetY = { fullHeight -> fullHeight / 4 }
                                ) + fadeOut())
                    } else {
                        // Развернуто -> Свернуто: список «съезжает» в область стопки
                        (slideInVertically(
                            initialOffsetY = { fullHeight -> fullHeight / 4 }
                        ) + fadeIn()) togetherWith
                                (slideOutVertically(
                                    targetOffsetY = { fullHeight -> -fullHeight / 4 }
                                ) + fadeOut())
                    }
                },
                label = "stack_expand_collapse"
            ) { collapsed ->
                if (collapsed) {
                    StackCard(
                        items = dataItemsUI,
                        visibilityColumName = visibilityColumName,
                        uiState = uiState,
                        viewModel = viewModel,
                        context = context,
                        onExpand = { isCollapsed = false }
                    )
                } else {
                    FullList(
                        dataItemsUI = dataItemsUI,
                        listState = listState,
                        visibilityColumName = visibilityColumName,
                        uiState = uiState,
                        viewModel = viewModel,
                        context = context
                    )
                }
            }
        }

        // НИЖНЯЯ ПАНЕЛЬ – твой Row c суммами/выбранными
        BottomSummaryRow(
            dataItemsUI = dataItemsUI,
            viewModel = viewModel
        )
    }
}
