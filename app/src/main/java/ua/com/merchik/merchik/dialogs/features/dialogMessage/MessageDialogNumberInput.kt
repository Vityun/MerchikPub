package ua.com.merchik.merchik.dialogs.features.dialogMessage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.com.merchik.merchik.R

data class MessageDialogNumberInput(
    val label: String,
    val value: String = "",
    val maxValue: Long,
    val validationMessage: String,
    val suffix: String = "",
    val maxValueMessage: String = validationMessage,
    val onValueChange: (String) -> Unit
) {
    val number: Long?
        get() = value.toLongOrNull()?.takeIf { it in 1..maxValue }

    val canConfirm: Boolean
        get() = number != null

    val validationError: String?
        get() {
            if (canConfirm) return null
            if (value.isNotEmpty() && value.all { it in '0'..'9' }) {
                val parsed = value.toLongOrNull()
                if (parsed == null || parsed > maxValue) return maxValueMessage
            }
            return validationMessage
        }

    fun updateValue(value: String) {
        if (value.all { it in '0'..'9' }) onValueChange(value)
    }
}

@Composable
internal fun MessageDialogNumberInputText(
    text: AnnotatedString,
    input: MessageDialogNumberInput,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val inlineText = remember(text, input.label, input.suffix) {
        buildAnnotatedString {
            append(text)
            append(" ")
            appendInlineContent("number_input", input.label)
            if (input.suffix.isNotEmpty()) {
                append("\u00A0")
                append(input.suffix)
            }
        }
    }
    BasicText(
        text = inlineText,
        inlineContent = mapOf(
            "number_input" to InlineTextContent(
                Placeholder(
                    width = 90.sp,
                    height = 30.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                MessageDialogInlineNumberInput(input, style)
            }
        ),
        style = style.copy(
            lineHeight = 34.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun MessageDialogInlineNumberInput(input: MessageDialogNumberInput, style: TextStyle) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val inputColor = colorResource(if (input.canConfirm) R.color.normalColor else R.color.red_error)
    val inputStyle = style.copy(
        color = inputColor,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
    val shape = RoundedCornerShape(8.dp)

    LaunchedEffect(focusRequester) {
        withFrameNanos { }
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    BasicTextField(
        value = input.value,
        onValueChange = input::updateValue,
        singleLine = true,
        textStyle = inputStyle,
        cursorBrush = SolidColor(inputColor),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .semantics {
                contentDescription = input.label
                input.validationError?.let { error(it) }
            },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(BorderStroke(1.dp, inputColor), shape)
                    .background(Color.White, shape)
                    .padding(horizontal = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                if (input.value.isEmpty()) Text("_____", style = inputStyle)
                innerTextField()
            }
        }
    )
}
