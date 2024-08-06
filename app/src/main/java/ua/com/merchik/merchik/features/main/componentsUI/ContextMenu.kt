package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.R

@Composable
fun ContextMenu(
    itemsMenu: List<String>,
    modifier: Modifier = Modifier,
    onSelectedMenu: (Int) -> Unit,
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
        Column {
            content()
            if (isVisible) {
                Popup(
                    onDismissRequest = {
                        time = 0
                        isVisible = false
                    },
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
                                colorResource(id = R.color.borderContextMenu),
                                RoundedCornerShape(8.dp)
                            )
                            .background(
                                Color.White,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        LazyColumn(contentPadding = PaddingValues(7.dp)) {
                            itemsIndexed(items = itemsMenu) { k, item ->
                                Column(
                                    modifier = Modifier
                                        .clickable {
                                            time = 0
                                            isVisible = false
                                            onSelectedMenu.invoke(k)
                                        }
                                        .widthIn(max = 200.dp)
                                ) {
                                    Text(
                                        text = item,
                                        modifier = Modifier
                                            .padding(top = 5.dp, bottom = 5.dp)
                                    )
                                    if (k < itemsMenu.size - 1) HorizontalDivider(thickness = 1.dp)
                                }
                            }
                        }
                    }
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