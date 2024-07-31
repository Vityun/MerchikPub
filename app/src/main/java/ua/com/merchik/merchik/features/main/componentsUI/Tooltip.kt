package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun Tooltip(
    text: String,
    modifier: Modifier = Modifier,
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
        content()
        if (isVisible) {
            Popup(
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
                            colorResource(id = R.color.borderToolTip),
                            RoundedCornerShape(8.dp)
                        )
                        .background(
                            colorResource(id = R.color.backgroundToolTip),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            time = 0
                            isVisible = false
                        }
                ) {
                    Text(
                        modifier = Modifier
                            .widthIn(max = 200.dp)
                            .padding(7.dp),
                        text = text,
                        color = Color.Black
                    )
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