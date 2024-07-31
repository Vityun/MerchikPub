package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun RoundCheckbox(
    modifier: Modifier = Modifier,
    aroundColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .size(45.dp)
            .clip(CircleShape)
            .background(aroundColor)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = modifier.size(30.dp)) {
            drawCircle(
                color = Color.White,
            )
            drawCircle(
                color = Color.Gray,
                style = Stroke(width = 1.dp.toPx())
            )
            // Draw checkmark if checked
            if (checked) {
                drawLine(
                    color = Color.Blue,
                    start = center - Offset(6.dp.toPx(), 0.dp.toPx()),
                    end = center + Offset(-2.dp.toPx(), 6.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = Color.Blue,
                    start = center + Offset(-2.dp.toPx(), 6.dp.toPx()),
                    end = center + Offset(6.dp.toPx(), -6.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}