package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.math.roundToInt

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