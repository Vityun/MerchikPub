package ua.com.merchik.merchik.dialogs.features.dialogLoading

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


@Composable
fun GradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(
        Color(0xFF336699), Color(0xFF6CA0DC), Color(0xCC4682B4),
        Color(0xFF336699), Color(0xFF87CEFA), Color(0xCC5B9BD5)
    ),
    animationDuration: Int = 4000 // Длительность анимации
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val progressWidth = progress * width

        // Создаём градиент
        val gradient = Brush.linearGradient(
            colors = gradientColors,
            start = Offset(x = -animatedOffset * width, y = 0f),
            end = Offset(x = width - animatedOffset * width, y = 0f)
        )

        // Рисуем фон прогресс-бара
        drawRect(
            color = Color.Gray.copy(alpha = 0.3f),
            size = Size(width, height)
        )

        // Рисуем прогресс с градиентом
        drawRect(
            brush = gradient,
            size = Size(progressWidth, height)
        )
    }
}
