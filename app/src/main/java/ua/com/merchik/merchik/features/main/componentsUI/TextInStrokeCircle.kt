package ua.com.merchik.merchik.features.main.componentsUI

import android.graphics.Paint
import android.text.TextPaint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TextInStrokeCircle(
    modifier: Modifier = Modifier,
    text: String,
    circleColor: Color,
    textColor: Color,
    aroundColor: Color,
    circleSize: Dp,
    textSize: Float,
) {
    Box(
        modifier = Modifier
            .size(45.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f) // 55% от 45.dp
                .clip(CircleShape)
                .background(aroundColor)
        ) {
            Canvas(modifier = modifier.size(circleSize)) {
                drawCircle(
                    color = Color.White,
                )
                drawCircle(
                    color = circleColor,
                    style = Stroke(width = 1.dp.toPx())
                )

                drawIntoCanvas { canvas ->
                    val paint = TextPaint().apply {
                        this.color = textColor.toArgb()
                        this.textSize = textSize
                        this.textAlign = Paint.Align.CENTER
                    }
                    val x = center.x
                    val y = center.y - (paint.descent() + paint.ascent()) / 2
                    canvas.nativeCanvas.drawText(text, x, y, paint)
                }
            }
        }
    }
}

@Composable
fun TextInCircle(
    modifier: Modifier = Modifier,
    text: String,
    circleColor: Color,
    textColor: Color,
    circleSize: Dp,
    textSize: Float
) {
    Box(modifier = modifier.size(circleSize)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = circleColor,
                radius = size.minDimension / 2
            )

            drawIntoCanvas { canvas ->
                val paint = TextPaint().apply {
                    this.color = textColor.toArgb()
                    this.textSize = textSize
                    this.textAlign = android.graphics.Paint.Align.CENTER
                }
                val x = size.width / 2
                val y = size.height / 2 - (paint.descent() + paint.ascent()) / 2
                canvas.nativeCanvas.drawText(text, x, y, paint)
            }
        }
    }
}