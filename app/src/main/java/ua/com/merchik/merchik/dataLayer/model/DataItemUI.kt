package ua.com.merchik.merchik.dataLayer.model

import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import java.util.concurrent.atomic.AtomicLong


data class DataItemUI(
    val rawObj: List<DataObjectUI>,
    val rawFields: List<FieldValue>,
    val fields: List<FieldValue>,
    val images: List<String>? = null,
    val modifierContainer: MerchModifier? = null,
    val selected: Boolean,
    // уникальный стабильный идентификатор — присваивается при создании
    val stableId: Long
)

data class MerchModifier(
    val hide: Boolean? = null,
    val fontStyle: FontStyle? = null,
    val fontWeight: FontWeight? = null,
    val padding: Padding? = null,
    val textColor: Color? = null,
    val background: Color? = null,
    val alignment: Alignment.Horizontal? = null,
    val weight: Float? = null,
    val textDecoration: TextDecoration? = null,
    val visibility: Int? = null,
    val maxLine: Int? = null
)

data class Padding(
    val start: Dp? = 0.dp,
    val top: Dp? = 0.dp,
    val end: Dp? = 0.dp,
    val bottom: Dp? = 0.dp,
)

data class TextField(
    val rawValue: Any,
    val value: String,
    val modifierValue: MerchModifier? = null,
    val productCodeText: ProductCodeText? = null
)

data class FieldValue(
    val key: String,
    val field: TextField,
    val value: TextField
)

data class SettingsItemUI(
    val key: String,
    val text: String,
    var isEnabled: Boolean,
    val index: Int
)

@Immutable
data class ClickTextAction(
    val actionId: String,
    val argument: String? = null
)

@Immutable
data class ColoredPart(
    val text: String,
    val color: Color
)


@Immutable
data class ProductCodeText(
    val parts: List<ColoredPart>,
    val underline: Boolean = true,
    val clickAction: ClickTextAction? = null
) {
    val fullText: String
        get() = parts.joinToString(separator = "") { it.text }
}


// генератор id для DataItemUI — потокобезопасный
object DataItemIdGenerator {
    // Инициализируем из currentTime, чтобы при перезапуске не начинать с нуля
    private val counter = AtomicLong(System.currentTimeMillis())

    fun nextId(): Long = counter.incrementAndGet()
}

fun DataItemUI.addOrReplaceField(fieldValue: FieldValue): DataItemUI {
    val newFields = fields.toMutableList()
    val newRawFields = rawFields.toMutableList()

    val fieldIndex = newFields.indexOfFirst {
        it.key.equals(fieldValue.key, ignoreCase = true)
    }
    if (fieldIndex >= 0) newFields[fieldIndex] = fieldValue
    else newFields.add(fieldValue)

    val rawFieldIndex = newRawFields.indexOfFirst {
        it.key.equals(fieldValue.key, ignoreCase = true)
    }
    if (rawFieldIndex >= 0) newRawFields[rawFieldIndex] = fieldValue
    else newRawFields.add(fieldValue)

    return copy(
        fields = newFields,
        rawFields = newRawFields
    )
}

fun buildOptionCodeField(
    optionHtmlOrText: String,
    key: String,
    title: String = "Шифр",
    actionId: String = "open_option_code"
): FieldValue {
    val productCode = optionHtmlOrText.toProductCodeText(
        action = ClickTextAction(
            actionId = actionId,
            argument = optionHtmlOrText
        )
    )

    return FieldValue(
        key = key,
        field = TextField(
            rawValue = title,
            value = title,
            modifierValue = MerchModifier(
                textColor = Color.Gray,
                padding = Padding(end = 10.dp)
            )
        ),
        value = TextField(
            rawValue = optionHtmlOrText,
            value = productCode.fullText,
            productCodeText = productCode
        )
    )
}


private fun String.toProductCodeText(
    action: ClickTextAction? = null,
    defaultColor: Color = Color.Black
): ProductCodeText {
    val spanned: Spanned = HtmlCompat.fromHtml(
        this,
        HtmlCompat.FROM_HTML_MODE_LEGACY
    )

    val plainText = spanned.toString()

    if (plainText.isEmpty()) {
        return ProductCodeText(
            parts = emptyList(),
            underline = true,
            clickAction = action
        )
    }

    val spans = spanned.getSpans(0, plainText.length, ForegroundColorSpan::class.java)

    val charsWithColor = plainText.mapIndexed { index, ch ->
        val colorInt = spans.firstOrNull { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            index in start until end
        }?.foregroundColor

        ch.toString() to (colorInt?.let { Color(it) } ?: defaultColor)
    }

    val parts = mutableListOf<ColoredPart>()
    var currentText = StringBuilder()
    var currentColor: Color? = null

    charsWithColor.forEach { (charText, color) ->
        if (currentColor == null || currentColor == color) {
            currentText.append(charText)
            currentColor = color
        } else {
            parts.add(
                ColoredPart(
                    text = currentText.toString(),
                    color = currentColor!!
                )
            )
            currentText = StringBuilder(charText)
            currentColor = color
        }
    }

    if (currentText.isNotEmpty() && currentColor != null) {
        parts.add(
            ColoredPart(
                text = currentText.toString(),
                color = currentColor!!
            )
        )
    }

    return ProductCodeText(
        parts = parts,
        underline = true,
        clickAction = action
    )
}