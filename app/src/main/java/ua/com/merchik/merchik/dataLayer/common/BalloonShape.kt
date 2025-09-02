package ua.com.merchik.merchik.dataLayer.common

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class BalloonShape(
    private val cornerRadius: Dp = 12.dp,
    private val tailWidth: Dp = 20.dp,
    private val tailHeight: Dp = 10.dp,
    private val tailAlignment: Float = 0.5f,
    private val tailOnBottom: Boolean = true
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = with(density) {
        val r = cornerRadius.toPx()
        val tw = tailWidth.toPx().coerceAtLeast(1f)
        val th = tailHeight.toPx().coerceAtLeast(1f)

        val rect = if (tailOnBottom)
            Rect(0f, 0f, size.width, size.height - th)
        else
            Rect(0f, th, size.width, size.height)

        val body = Path().apply {
            addRoundRect(RoundRect(rect, CornerRadius(r, r)))
        }

        val baseY = if (tailOnBottom) size.height - th else th
        val cx = (size.width * tailAlignment).coerceIn(tw / 2f, size.width - tw / 2f)

        val tail = Path().apply {
            if (tailOnBottom) {
                moveTo(cx - tw / 2f, baseY)
                lineTo(cx, baseY + th)
                lineTo(cx + tw / 2f, baseY)
            } else {
                moveTo(cx - tw / 2f, baseY)
                lineTo(cx, baseY - th)
                lineTo(cx + tw / 2f, baseY)
            }
            close()
        }

        val union = Path.combine(PathOperation.Union, body, tail)
        Outline.Generic(union)
    }
}
