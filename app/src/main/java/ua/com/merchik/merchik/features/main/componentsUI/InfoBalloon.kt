package ua.com.merchik.merchik.features.main.componentsUI


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ua.com.merchik.merchik.dataLayer.common.BalloonShape


/** Базовый, «слотовый» вариант — можно класть любой контент. */
@Composable
fun InfoBalloon(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    tailWidth: Dp = 20.dp,
    tailHeight: Dp = 10.dp,
    tailAlignment: Float = 0.5f,
    tailOnBottom: Boolean = true,
    background: Color = Color.White,
    borderColor: Color = Color(0x33000000),
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    contentArrangement: Arrangement.Vertical = Arrangement.Center,
    content: @Composable ColumnScope.() -> Unit
) {
    // 1) Тот же shape, чтобы использовать и в Surface, и при обводке
    val shape = remember(cornerRadius, tailWidth, tailHeight, tailAlignment, tailOnBottom) {
        BalloonShape(cornerRadius, tailWidth, tailHeight, tailAlignment, tailOnBottom)
    }

    // 2) Компенсируем хвостик: добавляем внутренний паддинг на сторону хвоста
    val innerPadding = remember(contentPadding, tailOnBottom, tailHeight) {
        fun sum(a: PaddingValues, b: PaddingValues) = PaddingValues(
            start = a.calculateStartPadding(LayoutDirection.Ltr) + b.calculateStartPadding(
                LayoutDirection.Ltr
            ),
            top = a.calculateTopPadding() + b.calculateTopPadding(),
            end = a.calculateEndPadding(LayoutDirection.Ltr) + b.calculateEndPadding(LayoutDirection.Ltr),
            bottom = a.calculateBottomPadding() + b.calculateBottomPadding()
        )

        val tailPad = if (tailOnBottom)
            PaddingValues(bottom = tailHeight)
        else
            PaddingValues(top = tailHeight)
        sum(contentPadding, tailPad)
    }

    // 3) Рисуем границу вручную (на некоторых устройствах border у Surface «теряется»)
    val borderStrokePx = with(LocalDensity.current) { borderWidth.toPx() }

    Surface(
        modifier = modifier
            .drawWithContent {
                drawContent()
                // Получаем тот же контур и обводим
                val path = when (val outline = shape.createOutline(size, layoutDirection, this)) {
                    is Outline.Generic -> outline.path
                    is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
                    is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
                }
                drawPath(path, color = borderColor, style = Stroke(width = borderStrokePx))
            },
        shape = shape,
        color = background,
        // оставим border у Surface пустым — всё равно рисуем сами поверх
        shadowElevation = shadowElevation
    ) {
        Column(
            modifier = Modifier.padding(innerPadding),
            horizontalAlignment = contentAlignment,
            verticalArrangement = contentArrangement
        ) {
            content()
        }
    }
}

@Composable
fun InfoBalloonText(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    tailAlignment: Float = 0.5f,
    tailOnBottom: Boolean = true
) {
    InfoBalloon(
        modifier = modifier,
        tailAlignment = tailAlignment,
        tailOnBottom = tailOnBottom
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
            )
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(1.dp))

            // Подзаголовок у левого края
            Row(
                modifier = Modifier
                    .align(Alignment.Start),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
//                Text(
//                    text = subtitle,
//                    style = MaterialTheme.typography.bodyMedium
//                )
//                Spacer(Modifier.width(6.dp))
                Text(
                    text = buildAnnotatedString {
                        append(subtitle)
                        append(" ")
                        withStyle(
                            SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)
                        ) { append("подробнее..") }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
