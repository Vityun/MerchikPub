package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.Utils.Ui.CounterBadgeView

@Composable
fun CounterBadge(
    count: Int,
    modifier: Modifier = Modifier,
    diameter: Dp = dimensionResource(R.dimen.counter_badge_size),
    background: Color = Color.Red,
    borderAndTextColor: Color = Color.White,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxCount: Int? = 9
) = CounterBadge(count.toLong(), modifier, diameter, background, borderAndTextColor, fontSize, maxCount)

@Composable
fun CounterBadge(
    count: Long,
    modifier: Modifier = Modifier,
    diameter: Dp = dimensionResource(R.dimen.counter_badge_size),
    background: Color = Color.Red,
    borderAndTextColor: Color = Color.White,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxCount: Int? = 9
) {
    val defaultFontSize = dimensionResource(R.dimen.counter_badge_text_size)
    val diameterPx = with(LocalDensity.current) { diameter.toPx() }
    val fontSizePx = with(LocalDensity.current) {
        if (fontSize == TextUnit.Unspecified) defaultFontSize.toPx() else fontSize.toPx()
    }
    AndroidView(
        factory = { CounterBadgeView(it) },
        modifier = modifier,
        update = {
            it.setBadgeStyle(diameterPx, fontSizePx, background.toArgb(),
                borderAndTextColor.toArgb(), borderAndTextColor.toArgb())
            it.setCount(count, maxCount)
        }
    )
}
