package ua.com.merchik.merchik.features.main.Main

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.ViewHolders.AkciyaSelectionDataHolder
import ua.com.merchik.merchik.ViewHolders.ErrorSelectionDataHolder
import ua.com.merchik.merchik.data.Database.Room.UsersSDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dataLayer.model.ClickTextAction
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.features.main.DBViewModels.AkciyaDBViewModel
import ua.com.merchik.merchik.features.main.DBViewModels.AkciyaPresence
import ua.com.merchik.merchik.features.main.DBViewModels.ErrorDBViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun Float.toPx() = with(LocalDensity.current) { this@toPx.sp.toPx() }

private const val PRODUCT_CODE_TAG = "PRODUCT_CODE_TAG"


@Composable
fun ItemFieldValue(
    item: DataItemUI,
    fieldValue: FieldValue,
    visibilityField: Int? = null,
    onClickProductCode: ((DataItemUI, FieldValue, ClickTextAction) -> Unit)? = null,
    onLongClickProductCode: ((DataItemUI, FieldValue, ClickTextAction) -> Unit)? = null
) {
    Row(Modifier.fillMaxWidth()) {
        if (visibilityField == View.VISIBLE) {
            ItemTextField(
                textField = fieldValue.field,
                modifier = Modifier.weight(1f)
            )
        }

        ItemTextField(
            textField = fieldValue.value,
            modifier = Modifier.weight(2f),
            onClick = { action ->
                onClickProductCode?.invoke(item, fieldValue, action)
            },
            onLongClick = { action ->
                onLongClickProductCode?.invoke(item, fieldValue, action)
            }
        )
    }
}

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
        textDecoration = it.modifierValue?.textDecoration,
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
private fun ItemTextField(
    textField: TextField,
    modifier: Modifier? = null,
    onClick: ((ClickTextAction) -> Unit)? = null,
    onLongClick: ((ClickTextAction) -> Unit)? = null
) {
    val finalModifier = (modifier ?: Modifier)
        .padding(
            start = textField.modifierValue?.padding?.start ?: 0.dp,
            top = textField.modifierValue?.padding?.top ?: 0.dp,
            end = textField.modifierValue?.padding?.end ?: 0.dp,
            bottom = textField.modifierValue?.padding?.bottom ?: 0.dp,
        )
        .then(
            textField.modifierValue?.alignment?.let {
                Modifier.wrapContentWidth(it)
            } ?: Modifier
        )
        .then(
            textField.modifierValue?.background?.let {
                Modifier.background(color = it)
            } ?: Modifier
        )

    val productCodeText = textField.productCodeText

    if (productCodeText == null) {
        Text(
            text = textField.value,
            fontWeight = textField.modifierValue?.fontWeight,
            fontStyle = textField.modifierValue?.fontStyle,
            color = textField.modifierValue?.textColor ?: Color.Black,
            textDecoration = textField.modifierValue?.textDecoration,
            maxLines = textField.modifierValue?.maxLine ?: 3,
            overflow = TextOverflow.Ellipsis,
            modifier = finalModifier
        )
        return
    }

    val baseTextStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = textField.modifierValue?.fontWeight,
        fontStyle = textField.modifierValue?.fontStyle ?: FontStyle.Normal,
        color = textField.modifierValue?.textColor ?: Color.Black
    )

    val annotated = remember(productCodeText) {
        buildAnnotatedString {
            var cursor = 0

            productCodeText.parts.forEach { part ->
                val start = cursor
                append(part.text)
                cursor += part.text.length
                val end = cursor

                addStyle(
                    style = SpanStyle(
                        color = part.color,
                        textDecoration = if (productCodeText.underline) {
                            TextDecoration.Underline
                        } else {
                            TextDecoration.None
                        }
                    ),
                    start = start,
                    end = end
                )
            }

            productCodeText.clickAction?.let { action ->
                addStringAnnotation(
                    tag = PRODUCT_CODE_TAG,
                    annotation = action.actionId + "|" + (action.argument ?: ""),
                    start = 0,
                    end = length
                )
            }
        }
    }

    val clickAction = productCodeText.clickAction

    if (clickAction != null && (onClick != null || onLongClick != null)) {
        Text(
            text = annotated,
            style = baseTextStyle,
            maxLines = textField.modifierValue?.maxLine ?: 3,
            overflow = TextOverflow.Ellipsis,
            modifier = finalModifier.combinedClickable(
                onClick = {
                    onClick?.invoke(
                        ClickTextAction(
                            actionId = clickAction.actionId,
                            argument = clickAction.argument
                        )
                    )
                },
                onLongClick = {
                    onLongClick?.invoke(
                        ClickTextAction(
                            actionId = clickAction.actionId,
                            argument = clickAction.argument
                        )
                    )
                }
            )
        )
    } else {
        Text(
            text = annotated,
            style = baseTextStyle,
            maxLines = textField.modifierValue?.maxLine ?: 3,
            overflow = TextOverflow.Ellipsis,
            modifier = finalModifier
        )
    }
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

    val user: UsersSDB? = RoomManager.SQL_DB.usersDao().getUserById(Globals.userId)
    val animationTime = when {
        user == null -> 850
        user.reportDate05 == null -> 2650
        user.reportDate20 == null -> 1550
        else -> 850
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

            // 2550 - скорость до 5й отчетности
            // 1550 - до 20й
            // 850 - по умолчанию
            p.animateTo(1f, tween(animationTime, easing = FastOutSlowInEasing))
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

                            val closeScaleX =
                                if (shrinkToZeroOnClose) minScaleToZero else openStartScaleX
                            val closeScaleY =
                                if (shrinkToZeroOnClose) minScaleToZero else openStartScaleY

                            scaleX = closeScaleX + (1f - closeScaleX) * p.value
                            scaleY = closeScaleY + (1f - closeScaleY) * p.value

                            val startTx = (a.center.x - t.center.x)
                            val startTy = (a.center.y - t.center.y)

                            translationX = startTx * (1f - p.value)
                            translationY = startTy * (1f - p.value)
                        }
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                content(requestClose)
            }
        }
    }
}

@Composable
fun ImageCommentFieldsBlock(
    item: DataItemUI,
    fields: List<FieldValue>,
    onClick: (DataItemUI, FieldValue) -> Unit
) {
    if (fields.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp,
                start = 2.dp)
    ) {
        fields.forEach { field ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onClick(item, field) },
                        onLongClick = { onClick(item, field) }
                    )
            ) {
                ItemFieldValue(
                    item = item,
                    fieldValue = field,
                    visibilityField = View.GONE,
                    onClickProductCode = null,
                    onLongClickProductCode = null
                )
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

    fun pulse() {
        trigger++
    }
}

@Composable
fun rememberPulseController() = remember { PulseController() }

@Composable
fun Modifier.pulseOn(controller: PulseController): Modifier {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(controller.trigger) {
        // короткий “пульс”
        if (controller.trigger != 0) {
            scale.snapTo(1f)
            scale.animateTo(0.85f, tween(110))
            scale.animateTo(1f, tween(160))
            scale.animateTo(0.85f, tween(110))
            scale.animateTo(1f, tween(160))
        }
    }

    return this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

@Composable
private fun StepperButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(6.dp))
//            .background(Color(0xFFF4F4F4))
            .background(Color.LightGray)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun NumberInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    var localValue by remember(value) { mutableStateOf(value) }

    BasicTextField(
        value = localValue,
        onValueChange = { input ->
            val digitsOnly = input.filter { it.isDigit() }
            localValue = digitsOnly
            onValueChange(digitsOnly)
        },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        modifier = Modifier
            .width(74.dp)
            .height(38.dp)
            .padding(horizontal = 8.dp)
            .onFocusChanged { focusState ->
                val nowFocused = focusState.isFocused

                if (nowFocused && !isFocused) {
                    if (localValue == "0") {
                        localValue = ""
                        onValueChange("")
                    }
                }

                if (!nowFocused && isFocused) {
                    if (localValue.isBlank()) {
                        localValue = "0"
                        onValueChange("0")
                    }
                }

                isFocused = nowFocused
            },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                innerTextField()
            }
        }
    )
}

private const val MIN_COMMENT_MEANINGFUL_CHARS = 10

private fun String.meaningfulCharsCount(): Int {
    return count { it.isLetterOrDigit() }
}

@Composable
fun TextEditorRow(
    title: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        TextInput(
            value = value,
            onValueChange = onValueChange,
            placeholder = "Комментарий к товару",
            minMeaningfulChars = MIN_COMMENT_MEANINGFUL_CHARS,
            tooShortHint = "Слишком короткий комментарий"
        )
    }
}

@Composable
fun TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    minMeaningfulChars: Int = 0,
    tooShortHint: String = ""
) {
    var localValue by remember(value) { mutableStateOf(value) }

    val meaningfulCount = remember(localValue) {
        localValue.meaningfulCharsCount()
    }

    val hasValidation = minMeaningfulChars > 0
    val isEmpty = localValue.isBlank()
    val isTooShort = hasValidation && !isEmpty && meaningfulCount < minMeaningfulChars

    val borderColor = if (isTooShort) Color.Red else Color.LightGray

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        BasicTextField(
            value = localValue,
            onValueChange = { input ->
                localValue = input

                val count = input.meaningfulCharsCount()
                val validToSave = minMeaningfulChars <= 0 ||
                        input.isBlank() ||
                        count >= minMeaningfulChars

                if (validToSave) {
                    onValueChange(input)
                }
            },
            singleLine = false,
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Start,
                color = Color.Black
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 46.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 46.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (localValue.isBlank() && placeholder.isNotBlank()) {
                        Text(
                            text = placeholder,
                            color = Color.Gray
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (isTooShort) {
            Text(
                text = "$tooShortHint: минимум $minMeaningfulChars символов",
                color = Color.Red,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun NumberEditorRow(
    title: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            maxLines = 2
        )

        StepperButton(text = "−", onClick = onMinus)

        NumberInput(
            value = value,
            onValueChange = onValueChange
        )
        StepperButton(text = "+", onClick = onPlus)
    }
}


@Composable
fun DateEditorRow(
    title: String,
    value: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val formatter = remember {
        java.text.SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            maxLines = 2
        )

        Box(
            modifier = Modifier
                .height(38.dp)
                .width(152.dp)
//                .padding(horizontal = 16.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                .clickable {
                    val calendar = Calendar.getInstance()

                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val picked = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth)
                            }
                            onDateSelected(formatter.format(picked.time))
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.ifBlank { "Оберiть дату" },
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = if (value.isBlank()) Color.Gray else LocalContentColor.current
            )
        }
    }
}

@Composable
fun SelectEditorRow(
    title: String,
    selectedValue: String,
    choices: List<InlineChoiceUi>,
    onSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(text = title, modifier = Modifier.padding(bottom = 6.dp))

        ChoiceDropDown(
            selectedId = selectedValue,
            choices = choices,
            onSelected = onSelected
        )
    }
}

@Composable
fun AkciyaSelectorRow(
    title: String = "Акції",
    presence: AkciyaPresence,
    selectedAkciyaId: String?,
    selectedAkciyaName: String,
    onPresenceChanged: (AkciyaPresence) -> Unit,
    onSelected: (id: String?, title: String) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val id = AkciyaSelectionDataHolder.selectedId
            val name = AkciyaSelectionDataHolder.selectedName.orEmpty()

            if (name.isNotBlank()) {
                onSelected(id, name)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = presence == AkciyaPresence.HAS,
                    onCheckedChange = { checked ->
                        onPresenceChanged(
                            if (checked) AkciyaPresence.HAS else AkciyaPresence.UNSET
                        )
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF1565C0),
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.White
                    )
                )
                Text(text = "є",
                    color = if (presence == AkciyaPresence.HAS) Color.Black else Color.Black.copy(alpha = 0.45f)
                )

                Spacer(Modifier.width(8.dp))

                Checkbox(
                    checked = presence == AkciyaPresence.NONE,
                    onCheckedChange = { checked ->
                        onPresenceChanged(
                            if (checked) AkciyaPresence.NONE else AkciyaPresence.UNSET
                        )
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF1565C0),
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.White
                    )
                )
                Text(text = "немає",
                    color = if (presence == AkciyaPresence.NONE) Color.Black else Color.Black.copy(alpha = 0.45f))
            }
        }

        val displayText = when {
            selectedAkciyaName.isNotBlank() -> selectedAkciyaName
            else -> "Оберіть тип акції"
        }

        Text(
            text = displayText,
            color = Color(0xFF1565C0),
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .padding(top = 2.dp)
                .clickable {
                    AkciyaSelectionDataHolder.set(
                        id = selectedAkciyaId,
                        name = selectedAkciyaName
                    )

                    val intent = Intent(activity, FeaturesActivity::class.java)
                    val bundle = Bundle().apply {
                        putString("viewModel", AkciyaDBViewModel::class.java.canonicalName)
                        putString("contextUI", ContextUI.AKCIYA_FROM_TEXT_EDITOR.toString())
                        putString("modeUI", ModeUI.ONE_SELECT.toString())
                        putString("title", "Тип акції")
                        putString("subTitle", "## Оберіть тип акції")
                    }
                    intent.putExtras(bundle)
                    launcher.launch(intent)
                }
        )
    }
}

@Composable
fun DoubleSelectEditorRow(
    title: String,
    value: String,
    value2: String,
    choices: List<InlineChoiceUi>,
    choices2: List<InlineChoiceUi>,
    onSelected1: (String) -> Unit,
    onSelected2: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(text = title, modifier = Modifier.padding(bottom = 6.dp))

        ChoiceDropDown(
            selectedId = value,
            choices = choices,
            onSelected = onSelected1
        )

        Spacer(modifier = Modifier.height(8.dp))

        ChoiceDropDown(
            selectedId = value2,
            choices = choices2,
            onSelected = onSelected2
        )
    }
}

@Composable
fun TextAndSelectEditorRow(
    title: String,
    text: String,
    selectedId: String?,
    selectedValue: String,
    emptySelectionText: String,
    onTextChanged: (String) -> Unit,
    onSelected: (id: String?, title: String) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val id = ErrorSelectionDataHolder.selectedId
            val name = ErrorSelectionDataHolder.selectedName.orEmpty()

            if (name.isNotBlank()) {
                onSelected(id, name)
//                onTextChanged(name)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        val displayText = selectedValue.ifBlank {
            emptySelectionText
        }

        Text(
            text = displayText,
            color = Color(0xFF1565C0),
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clickable {
                    ErrorSelectionDataHolder.set(
                        id = selectedId,
                        name = selectedValue
                    )

                    val intent = Intent(activity, FeaturesActivity::class.java)
                    val bundle = Bundle().apply {
                        putString("viewModel", ErrorDBViewModel::class.java.canonicalName)
                        putString("contextUI", ContextUI.ERROR_FROM_TEXT_EDITOR.toString())
                        putString("modeUI", ModeUI.ONE_SELECT.toString())
                        putString("title", "Ошибка товара")
                        putString("subTitle", "## описание что делать с *Ошибкой товара*")
                    }
                    intent.putExtras(bundle)

                    launcher.launch(intent)
                }
        )

        TextInput(
            value = text,
            onValueChange = onTextChanged,
            minMeaningfulChars = 1,
            placeholder = "Добавить описание ошибки"
        )
    }
}

@Composable
fun ChoiceDropDown(
    selectedId: String,
    choices: List<InlineChoiceUi>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedTitle = choices
        .firstOrNull { it.id == selectedId }
        ?.title
        ?: "Оберiть значення"

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedTitle,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Text("▼")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            choices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(choice.title) },
                    onClick = {
                        expanded = false
                        onSelected(choice.id)
                    }
                )
            }
        }
    }
}

@Composable
fun PhotoEditorRow(
    title: String = "Фото остатков товара",
    onTakePhoto: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onTakePhoto,
            modifier = Modifier
                .width(152.dp)
                .height(38.dp),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Text(
                text = "Фото",
                maxLines = 1
            )
        }
    }
}
