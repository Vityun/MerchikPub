package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CounterBadge(
    count: Int,
    modifier: Modifier = Modifier,
    diameter: Dp = 22.dp,                 // подберите под ваш шрифт (20–24.dp обычно ок)
    background: Color = Color.Red,
    borderAndTextColor: Color = Color.White,
    fontSize: TextUnit = 11.sp,
    maxCount: Int? = 9
) = CounterBadge(count.toLong(), modifier, diameter, background, borderAndTextColor, fontSize, maxCount)

@Composable
fun CounterBadge(
    count: Long,
    modifier: Modifier = Modifier,
    diameter: Dp = 22.dp,
    background: Color = Color.Red,
    borderAndTextColor: Color = Color.White,
    fontSize: TextUnit = 11.sp,
    maxCount: Int? = 9
) {
    val unlimited = maxCount == null
    val horizontalPadding = 6.dp
    val minWidth = if (unlimited) {
        val textMeasurer = rememberTextMeasurer()
        val style = LocalTextStyle.current.copy(fontSize = fontSize)
        val twoDigitWidth = with(LocalDensity.current) {
            textMeasurer.measure("00", style = style, maxLines = 1, softWrap = false)
                .size.width.toDp()
        }
        maxOf(diameter, twoDigitWidth + horizontalPadding * 2)
    } else diameter
    Surface(
        modifier = modifier.then(
            if (unlimited) Modifier.defaultMinSize(minWidth = minWidth, minHeight = diameter)
            else Modifier.size(diameter)
        ),
        shape = CircleShape,
        color = background,
        border = BorderStroke(1.dp, borderAndTextColor)
    ) {
        Box(
            modifier = if (unlimited) Modifier.padding(horizontal = horizontalPadding, vertical = 1.dp) else Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (maxCount != null && count > maxCount) "$maxCount+" else count.toString(),
                color = borderAndTextColor,
                fontSize = fontSize,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
            )
        }
    }
}
