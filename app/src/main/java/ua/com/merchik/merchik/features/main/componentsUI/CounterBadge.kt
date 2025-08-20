package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    borderColor: Color = Color.White,
    fontSize: TextUnit = 11.sp
) {
    Surface(
        modifier = modifier.size(diameter),
        shape = CircleShape,
        color = background,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 9) "9+" else count.toString(),
                color = Color.White,
                fontSize = fontSize,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
