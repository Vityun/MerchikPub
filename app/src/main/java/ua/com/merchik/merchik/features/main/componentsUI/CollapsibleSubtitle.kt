package ua.com.merchik.merchik.features.main.componentsUI



import android.text.Spanned
import android.text.style.*
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.core.text.HtmlCompat
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.dialogs.features.dialogMessage.parseAndReplaceVisitText
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.Main.StateUI
import java.util.regex.Pattern

@Composable
fun CollapsibleSubtitle(
    uiState: StateUI,        // где есть subTitle: String?, subTitleLong: String?
    viewModel: MainViewModel         // если нужен для цвета
) {
    var expanded by remember { mutableStateOf(false) }

    val hasBoth = !uiState.subTitle.isNullOrBlank() && !uiState.subTitleLong.isNullOrBlank()

    val textToShow = when {
        hasBoth && !expanded -> uiState.subTitle!!
        hasBoth && expanded  -> uiState.subTitleLong!!
        else                 -> uiState.subTitle ?: uiState.subTitleLong ?: ""
    }

    val styledAnnotatedString: AnnotatedString = remember(textToShow) {
        val pattern = Pattern.compile("\\{([^}]*)\\}")
        val matcher = pattern.matcher(textToShow)
        if (matcher.find()) {
            // parseAndReplaceVisitText — ваша функция, оставляем как есть
            parseAndReplaceVisitText(textToShow) { codeDad2 ->
                // защищённый вызов Realm (если нужно — вынести в ассинхронный код)
                try {
                    RealmManager.getWorkPlanRowByCodeDad2(codeDad2.toLong())
                } catch (t: Throwable) {
                    null
                }
            }
        } else {
            AnnotatedString.fromHtml(textToShow.replace("\n", "<br>"))
        }
    }

    val textColor =
        if ((viewModel.typeWindow ?: "").equals("container", ignoreCase = true)) Color.DarkGray else Color.Black

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Text(
            text = styledAnnotatedString,
            // если у нас оба варианта: в свернутом виде показываем в одну строку с троеточием;
            // если только один вариант — показываем полностью
            maxLines = if (hasBoth && !expanded) 1 else Int.MAX_VALUE,
            overflow = if (hasBoth && !expanded) TextOverflow.Ellipsis else TextOverflow.Clip,
            color = textColor,
            textDecoration = if (hasBoth && !expanded) TextDecoration.Underline else null,
            modifier = Modifier
                .padding(start = 10.dp, bottom = 7.dp, end = 10.dp)
                .then(
                    if (hasBoth) Modifier.clickable { expanded = !expanded } else Modifier
                )
        )
    }
}


private fun htmlToAnnotatedString(html: String): AnnotatedString {
    val sp = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY) as Spanned
    val plain = sp.toString()

    return AnnotatedString.Builder(plain).apply {
        fun addRangeStyle(style: SpanStyle, start: Int, end: Int) {
            if (start in 0..plain.length && end in 0..plain.length && start < end) {
                addStyle(style, start, end)
            }
        }

        sp.getSpans(0, sp.length, Any::class.java).forEach { span ->
            val start = sp.getSpanStart(span)
            val end = sp.getSpanEnd(span)
            if (start < 0 || end < 0) return@forEach

            when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        android.graphics.Typeface.BOLD ->
                            addRangeStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                        android.graphics.Typeface.ITALIC ->
                            addRangeStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                        android.graphics.Typeface.BOLD_ITALIC -> {
                            addRangeStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                            addRangeStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                        }
                    }
                }
                is UnderlineSpan ->
                    addRangeStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                is StrikethroughSpan ->
                    addRangeStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
                is ForegroundColorSpan ->
                    addRangeStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
                is URLSpan -> {
                    addStringAnnotation(tag = "URL", annotation = span.url, start = start, end = end)
                    // Цвет/подчёркивание ссылок часто уже есть в HTML; если нужно — можно добавить стиль тут.
                }
                is TypefaceSpan -> {
                    // Опционально: if (span.family == "monospace") addRangeStyle(SpanStyle(fontFamily = FontFamily.Monospace), start, end)
                }
            }
        }
    }.toAnnotatedString()
}
