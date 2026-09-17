package ua.com.merchik.merchik.dialogs.features.dialogMessage

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.*
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.dialogs.features.indicator.LineSpinFadeLoaderIndicator
import ua.com.merchik.merchik.features.main.componentsUI.AutoResizeText
import ua.com.merchik.merchik.features.maps.presentation.main.ImageButton
import java.util.regex.Pattern

enum class DialogCloseReason {
    DISMISS,
    CLOSE_BUTTON,
    CONFIRM,
    CANCEL
}

/*
пример использования причин отказа
MessageDialog(
    title = "Видалення",
    message = "Ви дійсно хочете видалити елемент?",
    onDismiss = { showDialog = false },
    onConfirmAction = {
        // логика подтверждения
    },
    onCancelAction = {
        // логика отмены
    },
    onDialogClosed = { reason ->
        when (reason) {
            DialogCloseReason.DISMISS -> {
                Log.d("MessageDialog", "Закрыт по back/outside")
            }
            DialogCloseReason.CLOSE_BUTTON -> {
                Log.d("MessageDialog", "Закрыт по крестику")
            }
            DialogCloseReason.CONFIRM -> {
                Log.d("MessageDialog", "Закрыт после подтверждения")
            }
            DialogCloseReason.CANCEL -> {
                Log.d("MessageDialog", "Закрыт после отмены")
            }
        }
    }
)
 */

@Composable
fun MessageDialog(
    title: String? = "",
    subTitle: String? = "",
    message: String = "",
    onDismiss: () -> Unit,
    onCloseClick: (() -> Unit)? = null,
    okButtonName: String = "Застосувати",
    onConfirmAction: (() -> Unit)? = null,
    cancelButtonName: String = "Скасувати",
    onCancelAction: (() -> Unit)? = null,
    status: DialogStatus? = DialogStatus.NORMAL,
    showCheckbox: Boolean = false,
    onCheckboxChanged: ((Boolean) -> Unit)? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    onTextLinkClick: ((String) -> Unit)? = null,
    singleChoice: MessageDialogSingleChoice? = null,
    fontSizeOffsetSp: Float = 0f,
    onDialogClosed: ((DialogCloseReason) -> Unit)? = null
) {
    Log.e("additionalEarningsDialogData", "MessageDialog ++")
    val scrollState = rememberScrollState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var isChecked by rememberSaveable { mutableStateOf(false) }
    val confirmEnabled = singleChoice?.canConfirm != false
    val confirmButtonColors = ButtonDefaults.buttonColors(
        containerColor = colorResource(R.color.orange),
        disabledContainerColor = Color(0xFFD0D0D0),
        disabledContentColor = Color(0xFF616161)
    )
    val context = LocalContext.current
    // Disabled buttons ignore onClick; only the hint overlay handles these taps.
    val disabledConfirmHintModifier = Modifier.pointerInput(context) {
        detectTapGestures {
            Toast.makeText(context, "Спочатку оберіть потрібний варіант", Toast.LENGTH_SHORT).show()
        }
    }

    fun TextStyle.withDialogFontSize(): TextStyle = if (fontSizeOffsetSp == 0f) this else copy(
        fontSize = if (fontSize.isSp) (fontSize.value + fontSizeOffsetSp).sp else fontSize,
        lineHeight = if (lineHeight.isSp) (lineHeight.value + fontSizeOffsetSp).sp else lineHeight
    )

    fun closeDialog(reason: DialogCloseReason) {
        onDialogClosed?.invoke(reason)
        onDismiss()
    }

    val styledAnnotatedString: AnnotatedString = remember(message, onTextLinkClick) {
        val pattern = Pattern.compile("\\{([^}]*)\\}")
        val matcher = pattern.matcher(message)

        if (matcher.find()) {
            parseAndReplaceVisitText(message) { codeDad2 ->
                try {
                    RealmManager.getWorkPlanRowByCodeDad2(codeDad2.toLong())
                } catch (t: Throwable) {
                    null
                }
            }
        } else {
            AnnotatedString.fromHtml(
                htmlString = message.replace(Regex("\\s*\n\\s*"), " "),
                linkStyles = TextLinkStyles(
                    style = SpanStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                ),
                linkInteractionListener = { link ->
                    val url = (link as? LinkAnnotation.Url)?.url ?: return@fromHtml
                    onTextLinkClick?.invoke(url)
                }
            )
        }
    }

    val composition by rememberLottieComposition(
        when (status) {
            DialogStatus.ERROR -> LottieCompositionSpec.RawRes(R.raw.error)
            DialogStatus.ALERT -> LottieCompositionSpec.RawRes(R.raw.alert)
            else -> LottieCompositionSpec.RawRes(R.raw.status_ok)
        }
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Dialog(
        onDismissRequest = {
            closeDialog(DialogCloseReason.DISMISS)
        },
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .width(screenWidth * 0.9f)
                .padding(bottom = 44.dp)
                .background(color = Color.Transparent)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImageButton(
                    id = R.drawable.ic_letter_x,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier.padding(end = 0.dp),
                    onClick = {
                        onCloseClick?.invoke()
                        closeDialog(DialogCloseReason.CLOSE_BUTTON)
                    }
                )
            }

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .verticalScroll(scrollState)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    title?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium.withDialogFontSize(),
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .scale(1.1f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (status == DialogStatus.LOADING) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .padding(bottom = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LineSpinFadeLoaderIndicator(
                                penThickness = 10f,
                                radius = 22f,
                                elementHeight = 15f,
                                color = Color.Green
                            )
                        }
                    } else if (status != DialogStatus.EMPTY) {
                        LottieAnimation(
                            modifier = Modifier
                                .size(68.dp)
                                .padding(bottom = 4.dp),
                            composition = composition,
                            progress = { progress }
                        )
                    }

                    subTitle?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleSmall.withDialogFontSize(),
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    BasicText(
                        text = styledAnnotatedString,
                        style = MaterialTheme.typography.titleSmall.withDialogFontSize().copy(
                            color = Color(0xCC1E201D),
                            textAlign = TextAlign.Justify
                        ),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .padding(horizontal = if (subTitle.isNullOrEmpty()) 0.dp else 6.dp)
                            .padding(bottom = 2.dp)
                            .pointerInput(styledAnnotatedString) {
                                detectTapGestures { pos ->
                                    val layout = textLayoutResult ?: return@detectTapGestures
                                    val offset = layout.getOffsetForPosition(pos)

                                    val links = styledAnnotatedString.getLinkAnnotations(offset, offset)
                                    val link = links.firstOrNull() ?: return@detectTapGestures

                                    when (val item = link.item) {
                                        is LinkAnnotation.Url -> {
                                            onTextLinkClick?.invoke(item.url)
                                        }
                                        is LinkAnnotation.Clickable -> {
                                            onTextLinkClick?.invoke(item.tag)
                                        }
                                    }
                                }
                            },
                        onTextLayout = {
                            textLayoutResult = it
                        }
                    )

                    singleChoice?.let { choice ->
                        Column(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .padding(top = 4.dp)
                                .selectableGroup()
                        ) {
                            choice.options.forEach { option ->
                                val selected = choice.selectedKey == option.key
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 44.dp)
                                        .selectable(
                                            selected = selected,
                                            role = Role.RadioButton,
                                            onClick = { choice.select(option.key) }
                                        )
                                        .padding(horizontal = 0.dp, vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = colorResource(R.color.blue),
                                            unselectedColor = Color.Gray
                                        )
                                    )
                                    Text(
                                        text = option.title,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 12.dp),
                                        style = MaterialTheme.typography.bodyMedium.withDialogFontSize()
                                    )
                                }
                            }
                        }
                    }

                    if (showCheckbox) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, start = 8.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    isChecked = it
                                    onCheckboxChanged?.invoke(it)
                                }
                            )
                            Text(
                                text = stringResource(id = R.string.not_show_again),
                                modifier = Modifier.padding(start = 4.dp),
                                style = MaterialTheme.typography.bodySmall.withDialogFontSize()
                            )
                        }
                    }

                    if (onConfirmAction != null || onCancelAction != null) {
                        Spacer(modifier = Modifier.padding(4.dp))

                        when {
                            onConfirmAction != null && onCancelAction != null -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onCancelAction()
                                            closeDialog(DialogCloseReason.CANCEL)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorResource(id = R.color.blue)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(cancelButtonName, style = LocalTextStyle.current.withDialogFontSize())
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        Button(
                                            onClick = {
                                                if (confirmEnabled) {
                                                    onConfirmAction()
                                                    closeDialog(DialogCloseReason.CONFIRM)
                                                }
                                            },
                                            enabled = confirmEnabled,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = confirmButtonColors,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(okButtonName, style = LocalTextStyle.current.withDialogFontSize())
                                        }
                                        if (!confirmEnabled) {
                                            Box(Modifier.matchParentSize().then(disabledConfirmHintModifier))
                                        }
                                    }
                                }
                            }

                            onCancelAction != null -> {
                                Row {
                                    Button(
                                        onClick = {
                                            onCancelAction()
                                            closeDialog(DialogCloseReason.CANCEL)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorResource(id = R.color.blue)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    ) {
                                        AutoResizeText(
                                            text = cancelButtonName,
                                            modifier = Modifier.fillMaxWidth(),
                                            style = MaterialTheme.typography.labelLarge.withDialogFontSize(),
                                            maxLines = 1,
                                            minTextSize = (12f + fontSizeOffsetSp).sp,
                                            step = 1.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }

                            onConfirmAction != null -> {
                                Row {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Box(modifier = Modifier.weight(1f)) {
                                        Button(
                                            onClick = {
                                                if (confirmEnabled) {
                                                    onConfirmAction()
                                                    closeDialog(DialogCloseReason.CONFIRM)
                                                }
                                            },
                                            enabled = confirmEnabled,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = confirmButtonColors,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            AutoResizeText(
                                                text = okButtonName,
                                                modifier = Modifier.fillMaxWidth(),
                                                style = MaterialTheme.typography.labelLarge.withDialogFontSize(),
                                                maxLines = 1,
                                                minTextSize = (12f + fontSizeOffsetSp).sp,
                                                step = 1.sp
                                            )
                                        }
                                        if (!confirmEnabled) {
                                            Box(Modifier.matchParentSize().then(disabledConfirmHintModifier))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.padding(2.dp))
                }
            }
        }
    }
    Log.e("additionalEarningsDialogData", "MessageDialog --")
}

//@Composable
//fun MessageDialog(
//    title: String? = "",
//    subTitle: String? = "",
//    message: String = "",
//    onDismiss: () -> Unit,
//    onCloseClick: (() -> Unit)? = null,
//    okButtonName: String = "Застосувати",
//    onConfirmAction: (() -> Unit)? = null,
//    cancelButtonName: String = "Скасувати",
//    onCancelAction: (() -> Unit)? = null,
//    status: DialogStatus? = DialogStatus.NORMAL,
//    showCheckbox: Boolean = false,
//    onCheckboxChanged: ((Boolean) -> Unit)? = null,
//    dismissOnBackPress: Boolean = true,
//    dismissOnClickOutside: Boolean = true,
//    onTextLinkClick: ((String) -> Unit)? = null
//) {
//    val scrollState = rememberScrollState()
//    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
//
//    // Кешируем разбор сообщения, чтобы не парсить на каждой рекомпозиции
//// Кешируем разбор сообщения, чтобы не парсить на каждой рекомпозиции
////    val styledAnnotatedString: AnnotatedString = remember(message, messageAnnotated) {
////        when {
////            // приоритет — уже готовый AnnotatedString с кликами
////            messageAnnotated != null -> messageAnnotated
////
////            else -> {
////                val pattern = Pattern.compile("\\{([^}]*)\\}")
////                val matcher = pattern.matcher(message)
////                if (matcher.find()) {
////                    parseAndReplaceVisitText(message) { codeDad2 ->
////                        try {
////                            RealmManager.getWorkPlanRowByCodeDad2(codeDad2.toLong())
////                        } catch (t: Throwable) {
////                            null
////                        }
////                    }
////                } else {
////                    AnnotatedString.fromHtml(message.replace("\n", "<br>"))
////                }
////            }
////        }
////    }
//
//    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
//
//    val styledAnnotatedString: AnnotatedString = remember(message) {
//        val pattern = Pattern.compile("\\{([^}]*)\\}")
//        val matcher = pattern.matcher(message)
//
//        if (matcher.find()) {
//            parseAndReplaceVisitText(message) { codeDad2 ->
//                try {
//                    RealmManager.getWorkPlanRowByCodeDad2(codeDad2.toLong())
//                } catch (t: Throwable) {
//                    null
//                }
//            }
//        } else {
//            AnnotatedString.fromHtml(
//                htmlString = message.replace(Regex("\\s*\n\\s*"), " "),
//                linkStyles = TextLinkStyles(
//                    style = SpanStyle(
//                        color = Color.Blue,
//                        textDecoration = TextDecoration.Underline
//                    )
//                ),
//                linkInteractionListener = { link ->
//                    val url = (link as? LinkAnnotation.Url)?.url ?: return@fromHtml
//                    onTextLinkClick?.invoke(url)
//                }
//            )
////            AnnotatedString.fromHtml(
////                htmlString = message
////                    .replace("\n", "<br>")
////                    .replace("/n", "<br>"),
////                linkStyles = TextLinkStyles(
////                    style = SpanStyle(
////                        color = Color.Blue,
////                        textDecoration = TextDecoration.Underline
////                    )
////                )
////            )
//        }
//    }
//
////    val styledAnnotatedString: AnnotatedString = remember(message) {
////
////        val pattern = Pattern.compile("\\{([^}]*)\\}")
////        val matcher = pattern.matcher(message)
////        if (matcher.find()) {
////            parseAndReplaceVisitText(message) { codeDad2 ->
////                try {
////                    RealmManager.getWorkPlanRowByCodeDad2(codeDad2.toLong())
////                } catch (t: Throwable) {
////                    null
////                }
////            }
////        } else {
////
////            AnnotatedString.fromHtml(
////                htmlString = message.replace("\n", "<br>").replace("/n", "<br>"),
////                linkStyles = TextLinkStyles(
////                    style = SpanStyle(
////                        color = Color.Blue,
////                        textDecoration = TextDecoration.Underline
////                    )
////                )
////            )
////        }
////    }
//
//
//    // чекбокс сохраняем при пересоздании конфигурации
//    var isChecked by rememberSaveable { mutableStateOf(false) }
//
//    val composition by rememberLottieComposition(
//        when (status) {
//            DialogStatus.ERROR -> LottieCompositionSpec.RawRes(R.raw.error)
//            DialogStatus.ALERT -> LottieCompositionSpec.RawRes(R.raw.alert)
//            else -> LottieCompositionSpec.RawRes(R.raw.status_ok)
//        }
//    )
//    val progress by animateLottieCompositionAsState(
//        composition,
//        iterations = LottieConstants.IterateForever
//    )
//
//    var isCompleted by remember { mutableStateOf(false) }
//
//    // при isCompleted вызываем onDismiss()
//    LaunchedEffect(isCompleted) {
//        if (isCompleted) onDismiss()
//    }
//
//    // Позволяем закрывать диалог тапом вне области — если это НЕ нужно, замените onDismiss на {}
//    Dialog(
//        onDismissRequest = {
//            // сюда прилетает back/клик мимо
//            onDismiss()
//        },
//        properties = DialogProperties(
//            dismissOnBackPress = dismissOnBackPress,
//            dismissOnClickOutside = dismissOnClickOutside
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .wrapContentHeight()
//                .width(screenWidth * 0.9f)
//                .padding(bottom = 44.dp)
//                .background(color = Color.Transparent)
//        ) {
//            // Заголовок и кнопка "X" (кнопка справа)
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
//                horizontalArrangement = Arrangement.End,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                ImageButton(
//                    id = R.drawable.ic_letter_x,
//                    shape = CircleShape,
//                    colorImage = ColorFilter.tint(Color.Gray),
//                    sizeButton = 40.dp,
//                    sizeImage = 25.dp,
//                    modifier = Modifier.padding(end = 0.dp),
//                    onClick = {
//                        if (onCloseClick != null) {
//                            onCloseClick()
//
//                        }
//                        else
//                            isCompleted = true
//                    }
//                )
//            }
//
//            Box(
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .verticalScroll(scrollState)
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(Color.White)
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                ) {
//
//                    // Title
//                    title?.takeIf { it.isNotEmpty() }?.let {
//                        Text(
//                            text = it,
//                            style = MaterialTheme.typography.titleMedium,
//                            modifier = Modifier
//                                .padding(horizontal = 16.dp, vertical = 8.dp)
//                                .scale(1.1f),
//                            textAlign = TextAlign.Center,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//
//                    // Lottie / loader
//                    if (status == DialogStatus.LOADING) {
//                        Box(
//                            modifier = Modifier
//                                .size(68.dp)
//                                .padding(bottom = 4.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            LineSpinFadeLoaderIndicator(
//                                penThickness = 10f,
//                                radius = 22f,
//                                elementHeight = 15f,
//                                color = Color.Green
//                            )
//                        }
//                    } else if (status != DialogStatus.EMPTY) {
//                        LottieAnimation(
//                            modifier = Modifier
//                                .size(68.dp)
//                                .padding(bottom = 4.dp),
//                            composition = composition,
//                            progress = { progress },
//                        )
//                    }
//
//                    // Subtitle
//                    subTitle?.takeIf { it.isNotEmpty() }?.let {
//                        Text(
//                            text = it,
//                            style = MaterialTheme.typography.titleSmall,
//                            color = Color.Gray,
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier
//                                .padding(horizontal = 16.dp, vertical = 4.dp)
//                        )
//                    }
//
//                    BasicText(
//                        text = styledAnnotatedString,
//                        style = MaterialTheme.typography.titleSmall.copy(
//                            color = Color(0xCC1E201D),
//                            textAlign = TextAlign.Justify
//                        ),
//                        modifier = Modifier
//                            .padding(vertical = 4.dp)
//                            .padding(horizontal = if (subTitle.isNullOrEmpty()) 0.dp else 6.dp)
//                            .padding(bottom = 2.dp)
//                            .pointerInput(styledAnnotatedString) {
//                                detectTapGestures { pos ->
//                                    val layout = textLayoutResult ?: return@detectTapGestures
//                                    val offset = layout.getOffsetForPosition(pos)
//
//                                    val links = styledAnnotatedString.getLinkAnnotations(offset, offset)
//                                    val link = links.firstOrNull() ?: return@detectTapGestures
//
//                                    when (val item = link.item) {
//                                        is LinkAnnotation.Url -> {
//                                            onTextLinkClick?.invoke(item.url)
//                                        }
//                                        is LinkAnnotation.Clickable -> {
//                                            onTextLinkClick?.invoke(item.tag)
//                                        }
//                                    }
//                                }
//                            },
//                        onTextLayout = {
//                            textLayoutResult = it
//                        }
//                    )
//
//                    // Checkbox
//                    if (showCheckbox) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 12.dp, start = 8.dp)
//                        ) {
//                            Checkbox(
//                                checked = isChecked,
//                                onCheckedChange = {
//                                    isChecked = it
//                                    onCheckboxChanged?.invoke(it)
//                                }
//                            )
//                            Text(
//                                text = stringResource(id = R.string.not_show_again),
//                                modifier = Modifier.padding(start = 4.dp),
//                                style = MaterialTheme.typography.bodySmall
//                            )
//                        }
//                    }
//
//                    // Buttons: если обе кнопки заданы — делим пространство, если одна — занимает всю ширину
//                    if (onConfirmAction != null || onCancelAction != null) {
//                        Spacer(modifier = Modifier.padding(4.dp))
//
//                        when {
//                            onConfirmAction != null && onCancelAction != null -> {
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                                ) {
//                                    Button(
//                                        onClick = {
//                                            onCancelAction()
//                                            isCompleted = true
//                                        },
//                                        shape = RoundedCornerShape(8.dp),
//                                        colors = ButtonDefaults.buttonColors(
//                                            containerColor = colorResource(id = R.color.blue)
//                                        ),
//                                        modifier = Modifier.weight(1f)
//                                    ) {
//                                        Text(cancelButtonName)
//                                    }
//
//                                    Button(
//                                        onClick = {
//                                            onConfirmAction()
//                                            isCompleted = true
//                                        },
//                                        shape = RoundedCornerShape(8.dp),
//                                        colors = ButtonDefaults.buttonColors(
//                                            containerColor = colorResource(id = R.color.orange)
//                                        ),
//                                        modifier = Modifier.weight(1f)
//                                    ) {
//                                        Text(okButtonName)
//                                    }
//                                }
//                            }
//
//                            onCancelAction != null -> {
//                                Row {
//                                    Button(
//                                        onClick = {
//                                            onCancelAction()
//                                            isCompleted = true
//                                        },
//                                        shape = RoundedCornerShape(8.dp),
//                                        colors = ButtonDefaults.buttonColors(
//                                            containerColor = colorResource(id = R.color.blue)
//                                        ),
//                                        modifier = Modifier
//                                            .weight(1f)
//                                            .fillMaxWidth()
//                                    ) {
//                                        AutoResizeText(
//                                            text = cancelButtonName,
//                                            modifier = Modifier.fillMaxWidth(),
//                                            style = MaterialTheme.typography.labelLarge, // можно подобрать подходящий стиль
//                                            maxLines = 1,
//                                            minTextSize = 12.sp,
//                                            step = 1.sp
//                                        )
//                                    }
//                                    Spacer(modifier = Modifier.weight(1f))
//                                }
//                            }
//
//                            onConfirmAction != null -> {
//                                Row {
//                                    Spacer(modifier = Modifier.weight(1f))
//                                    Button(
//                                        onClick = {
//                                            onConfirmAction()
//                                            isCompleted = true
//                                        },
//                                        shape = RoundedCornerShape(8.dp),
//                                        colors = ButtonDefaults.buttonColors(
//                                            containerColor = colorResource(id = R.color.orange)
//                                        ),
//                                        modifier = Modifier
//                                            .weight(1f)
//                                            .fillMaxWidth()
//                                    ) {
//                                        AutoResizeText(
//                                            text = okButtonName,
//                                            modifier = Modifier.fillMaxWidth(),
//                                            style = MaterialTheme.typography.labelLarge, // можно подобрать подходящий стиль
//                                            maxLines = 1,
//                                            minTextSize = 12.sp,
//                                            step = 1.sp
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.padding(2.dp))
//                }
//            }
//        }
//    }
//}
