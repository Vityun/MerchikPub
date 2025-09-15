package ua.com.merchik.merchik.features.main.componentsUI


import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.isUnspecified

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AutoResizeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = 1,
    minTextSize: TextUnit = 12.sp,
    step: TextUnit = 1.sp
) {
    // начальный размер берем из переданного стиля или дефолта
    val initialSize = style.fontSize.takeIf { it.isUnspecified.not() } ?: MaterialTheme.typography.labelLarge.fontSize
    var currentSize by remember { mutableStateOf(if (initialSize.isUnspecified) 16.sp else initialSize) }
    var wasMeasured by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier) {
        Text(
            text = text,
            maxLines = maxLines,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            style = style.copy(fontSize = currentSize),
            textAlign = TextAlign.Center,              // выравнивание текста
            modifier = Modifier.fillMaxWidth(),        // обязательно для центровки
            onTextLayout = { layoutResult: TextLayoutResult ->
                // если текст визуально переполняет строку и можно уменьшить размер — уменьшаем
                if (layoutResult.hasVisualOverflow && currentSize > minTextSize) {
                    val next = (currentSize.value - step.value).coerceAtLeast(minTextSize.value)
                    currentSize = next.sp
                    wasMeasured = true
                } else {
                    wasMeasured = true
                }
            }
        )

    }
}
