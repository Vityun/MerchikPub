package ua.com.merchik.merchik.features.main.Main

import android.util.Log
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.dataLayer.model.TextField

@Composable
fun Float.toPx() = with(LocalDensity.current) { this@toPx.sp.toPx() }


@Composable
fun ItemFieldValue(it: FieldValue, visibilityField: Int? = null) {
//    Globals.writeToMLOG("INFO", "MainUI.ItemUI.ItemFieldValue","visibilityField: ${visibilityField == View.VISIBLE} | " +
//            "it.field.value: ${it.field.value} | it.value.value: ${it.value.value}, it.value.rawValue: ${it.value.rawValue}")
//    Log.e("INFO", "MainUI.ItemUI.ItemFieldValue visibilityField: ${visibilityField == View.VISIBLE} | " +
//            "it.field.value: ${it.field.value} | it.value.value: ${it.value.value}, it.value.rawValue: ${it.value.rawValue}")
    Row(Modifier.fillMaxWidth()) {
        if (visibilityField == View.VISIBLE) {
            ItemTextField(it.field, Modifier.weight(1f))
        }
//        ###########################
        ItemTextField(it.value, Modifier.weight(2f))
    }
}

@Composable
private fun ItemTextField(it: TextField, modifier: Modifier? = null) {
    Text(
        text = it.value,
        fontWeight = it.modifierValue?.fontWeight,
        fontStyle = it.modifierValue?.fontStyle,
        color = it.modifierValue?.textColor ?: Color.Black,
        maxLines = it.modifierValue?.maxLine ?: 3,
        overflow = TextOverflow.Ellipsis,
        modifier = (modifier ?: Modifier)
            .padding(
                start = it.modifierValue?.padding?.start ?: 0.dp,
                top = it.modifierValue?.padding?.top ?: 0.dp,
                end = it.modifierValue?.padding?.end ?: 0.dp,
                bottom = it.modifierValue?.padding?.bottom ?: 0.dp,
            )
            .then(it.modifierValue?.alignment?.let {
                Modifier.wrapContentWidth(it)
            } ?: Modifier)
            .then(it.modifierValue?.background?.let {
                Modifier.background(color = it)
            } ?: Modifier)
    )
}

@Composable
fun SettingsItemView(item: SettingsItemUI) {
    var isChecked by remember { mutableStateOf(item.isEnabled) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.text, modifier = Modifier
                .padding(start = 10.dp)
                .align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.weight(1f))

        Checkbox(
            checked = isChecked,
            onCheckedChange = { checked ->
                isChecked = checked
                item.isEnabled = checked
            }
        )
    }
}

@Composable
fun FontSizeSlider(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    size: Float,
    onChanged: (Float) -> Unit
) {
    var fontSize by remember { mutableStateOf(size) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "А",
                    style = TextStyle(fontSize = 14.sp)
                )

                Text(
                    modifier = Modifier.weight(1f),
                    text = viewModel.getTranslateString(
                        stringResource(id = R.string.ui_font_size),
                        5999
                    ),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = fontSize.sp)
                )

                Text(
                    text = "А",
                    style = TextStyle(fontSize = 30.sp)
                )
            }

            Slider(
                modifier = Modifier.padding(top = 18.dp),
                value = fontSize,
                enabled = false,
                onValueChange = {
                    fontSize = it
                    onChanged(it)
                },
                valueRange = 14f..30f,
            )
        }
    }
}


@Composable
fun AnchoredAnimatedDialog(
    visible: Boolean,
    anchorRect: Rect?,                 // Rect кнопки (boundsInWindow)
    onDismissRequest: () -> Unit,       // меняем твой showXxxDialog=false ТОЛЬКО тут
    padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
    onClosed: (() -> Unit)? = null,
    content: @Composable (requestClose: () -> Unit) -> Unit
) {
    var hostVisible by remember { mutableStateOf(false) }
    var targetRect by remember { mutableStateOf<Rect?>(null) }

    val shrinkToZeroOnClose: Boolean = true
    val minScaleToZero: Float = 0.001f // чтобы не было 0 и глюков матрицы

    val scope = rememberCoroutineScope()
    val p = remember { Animatable(0f) } // 0 -> в кнопке, 1 -> на месте

    fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    suspend fun playClose() {
        // если anchorRect ещё неизвестен — просто закрываем как есть
        p.animateTo(0f, tween(750, easing = FastOutSlowInEasing))
        hostVisible = false
        onDismissRequest()
        onClosed?.invoke()
    }

    val requestClose: () -> Unit = {
        scope.launch { playClose() }
    }

    LaunchedEffect(visible) {
        if (visible) {
            hostVisible = true
            p.snapTo(0f)

            // Ждём промера контента, чтобы старт был строго "из кнопки", без рывков
            // ✅ ждём и targetRect, и anchorRect
            snapshotFlow { anchorRect to targetRect }
                .filter { it.first != null && it.second != null }
                .first()

            p.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
        } else if (hostVisible) {
            playClose()
        }
    }

    if (!hostVisible) return

    Dialog(
        onDismissRequest = requestClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        val dialogView = LocalView.current

        Box(Modifier.fillMaxSize()) {
            // Прозрачный слой для клика "мимо" (затемнение даёт сам Dialog)
            Box(
                modifier = Modifier
                    .matchParentSize()
            )

            // Контент диалога, который мы анимируем
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .onGloballyPositioned { coords ->
                        val r = coords.boundsInWindow()
                        val loc = IntArray(2)
                        dialogView.getLocationOnScreen(loc)
                        targetRect = Rect(
                            left = r.left + loc[0],
                            top = r.top + loc[1],
                            right = r.right + loc[0],
                            bottom = r.bottom + loc[1]
                        )
                    }
                    .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    alpha = 1f

                    val a = anchorRect
                    val t = targetRect

                    if (a != null && t != null && t.width > 0f && t.height > 0f) {
                        val openStartScaleX = (a.width / t.width).coerceIn(0.03f, 1f)
                        val openStartScaleY = (a.height / t.height).coerceIn(0.03f, 1f)

                        val closeScaleX = if (shrinkToZeroOnClose) minScaleToZero else openStartScaleX
                        val closeScaleY = if (shrinkToZeroOnClose) minScaleToZero else openStartScaleY

                        scaleX = closeScaleX + (1f - closeScaleX) * p.value
                        scaleY = closeScaleY + (1f - closeScaleY) * p.value

                        val startTx = (a.center.x - t.center.x)
                        val startTy = (a.center.y - t.center.y)

                        translationX = startTx * (1f - p.value)
                        translationY = startTy * (1f - p.value)
                    }
                }
                ,
                contentAlignment = Alignment.TopCenter
            ) {
                content(requestClose)
            }
        }
    }
}


@Stable
data class Flying<T>(
    val item: T,
    val startOffset: IntOffset,
    val size: IntSize
)

@Stable
data class Dragging<T>(
    val item: T,
    val size: IntSize,
    val offset: MutableState<Offset> // Текущая позиция «призрака»
)

@Stable
data class Shrinking<T>(
    val item: T,
    val startOffset: IntOffset,
    val size: IntSize
)

@Stable
private data class ItemRect(val offset: IntOffset, val size: IntSize)




@Composable
fun Modifier.captureBoundsInWindow(onCaptured: (Rect) -> Unit): Modifier =
    onGloballyPositioned { coords -> onCaptured(coords.boundsInWindow()) }


@Composable
fun Modifier.captureBoundsInScreen(onCaptured: (Rect) -> Unit): Modifier {
    val view = LocalView.current
    return onGloballyPositioned { coords ->
        val r = coords.boundsInWindow()
        val loc = IntArray(2)
        view.getLocationOnScreen(loc) // <-- сдвиг окна на экране
        onCaptured(
            Rect(
                left = r.left + loc[0],
                top = r.top + loc[1],
                right = r.right + loc[0],
                bottom = r.bottom + loc[1]
            )
        )
    }
}



@Stable
class PulseController {
    internal var trigger by mutableIntStateOf(0)
        private set
    fun pulse() { trigger++ }
}

@Composable
fun rememberPulseController() = remember { PulseController() }

@Composable
fun Modifier.pulseOn(controller: PulseController): Modifier {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(controller.trigger) {
        // короткий “пульс”
        scale.snapTo(1f)
        scale.animateTo(0.85f, tween(110))
        scale.animateTo(1f, tween(160))
        scale.animateTo(0.85f, tween(110))
        scale.animateTo(1f, tween(160))
    }

    return this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}