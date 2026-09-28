package ua.com.merchik.merchik.features.main.Main

import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import ua.com.merchik.merchik.dataLayer.LaunchOrigin
import kotlin.math.roundToInt

private fun LayoutCoordinates.screenBounds(view: View): Rect {
    val screen = IntArray(2)
    val window = IntArray(2)
    view.getLocationOnScreen(screen)
    view.getLocationInWindow(window)
    // Compose bounds already include the host's position inside its window.
    return boundsInWindow().translate(
        Offset((screen[0] - window[0]).toFloat(), (screen[1] - window[1]).toFloat())
    )
}

@Composable
fun Modifier.captureLaunchOrigin(onCaptured: (LaunchOrigin?) -> Unit): Modifier {
    val view = LocalView.current
    return onGloballyPositioned { coordinates ->
        val rect = coordinates.screenBounds(view)
        onCaptured(if (rect.isEmpty) null else LaunchOrigin(
            rect.left.roundToInt(), rect.top.roundToInt(),
            rect.width.roundToInt().coerceAtLeast(1), rect.height.roundToInt().coerceAtLeast(1)
        ))
    }
}

// Shared by MainUI dialogs and FeaturesActivity opened from option counters.
fun Modifier.anchoredTransform(
    anchorRect: Rect?,
    targetRect: Rect?,
    progress: () -> Float
): Modifier = graphicsLayer {
    transformOrigin = TransformOrigin(0.5f, 0.5f)
    alpha = if (anchorRect != null && targetRect == null) 0f else 1f
    if (anchorRect != null && targetRect != null && !targetRect.isEmpty) {
        val p = progress()
        scaleX = 0.001f + (1f - 0.001f) * p
        scaleY = scaleX
        translationX = (anchorRect.center.x - targetRect.center.x) * (1f - p)
        translationY = (anchorRect.center.y - targetRect.center.y) * (1f - p)
    }
}

/** The same anchored motion without creating another window over FeaturesActivity. */
@Composable
fun AnchoredAnimatedContent(
    anchorRect: Rect,
    closeRequested: State<Boolean>,
    durationMillis: Int,
    onClosed: () -> Unit,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    var targetRect by remember { mutableStateOf<Rect?>(null) }
    val progress = remember { Animatable(0f) }
    val latestOnClosed by rememberUpdatedState(onClosed)

    LaunchedEffect(closeRequested.value) {
        if (closeRequested.value) {
            if (targetRect != null) {
                progress.animateTo(0f, tween(750, easing = FastOutSlowInEasing))
            }
            latestOnClosed()
        } else {
            snapshotFlow { targetRect }.filterNotNull().first()
            progress.animateTo(1f, tween(durationMillis, easing = FastOutSlowInEasing))
        }
    }

    // Measure the stationary parent, not the transformed child.
    Box(Modifier.fillMaxSize().onGloballyPositioned { targetRect = it.screenBounds(view) }) {
        Box(Modifier.matchParentSize().anchoredTransform(anchorRect, targetRect) { progress.value }) {
            content()
        }
        if (closeRequested.value) {
            Box(Modifier.matchParentSize().pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) awaitPointerEvent().changes.forEach { it.consume() }
                }
            })
        }
    }
}
