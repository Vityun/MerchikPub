package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.window.PopupProperties
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

    Box(modifier = modifier) {
        Box(
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        isVisible = true
                    }
                )
            }
        ) {
            content()
        }

        if (isVisible) {
            Popup(
                onDismissRequest = {
                    isVisible = false
                },
                offset = IntOffset(0, 100),
                alignment = Alignment.Center,
                properties = PopupProperties(
                    focusable = true
                )
            ) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = colorResource(id = R.color.borderContextMenu),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .widthIn(max = 220.dp)
                            .heightIn(max = 360.dp),
                        contentPadding = PaddingValues(7.dp)
                    ) {
                        itemsIndexed(items = itemsMenu) { index, item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isVisible = false
                                        onSelectedMenu(index)
                                    }
                            ) {
                                Text(
                                    text = item,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 8.dp,
                                            vertical = 8.dp
                                        )
                                )

                                if (index < itemsMenu.lastIndex) {
                                    HorizontalDivider(thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}