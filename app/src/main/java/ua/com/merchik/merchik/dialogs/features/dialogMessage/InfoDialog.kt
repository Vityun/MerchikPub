package ua.com.merchik.merchik.dialogs.features.dialogMessage

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton


@Composable
fun InfoDialog(
    title: String? = "",
    subTitle: String? = "",
    message: CharSequence = "",
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    var isCompleted by remember { mutableStateOf(false) }

    val annotated = remember(message) {
        when (message) {
            is android.text.Spanned -> message.toAnnotatedString()
            else -> AnnotatedString(message.toString())
        }
    }

    // при isCompleted вызываем onDismiss()
    LaunchedEffect(isCompleted) {
        if (isCompleted) onDismiss()
    }

    Dialog(
        onDismissRequest = {
            // сюда прилетает back/клик мимо
            onDismiss()
        }
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .width(screenWidth * 0.9f)
                .padding(bottom = 44.dp)
                .background(color = Color.Transparent)
        ) {
            // Заголовок и кнопка "X" (кнопка справа)
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
                        isCompleted = true
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

                    // Title
                    title?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .scale(1.1f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }


                    // Subtitle
                    subTitle?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(color = Color.White)
                    ) {
                        Text(
                            text = annotated,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xCC1E201D),
                            textAlign = TextAlign.Justify,
                            modifier = Modifier
                                .padding(vertical = 7.dp)
                                .padding(horizontal = 10.dp)
                        )
                    }
                }
            }
        }
    }
}


fun Spanned.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    append(this@toAnnotatedString.toString())

    val spans = getSpans(0, length, Any::class.java)
    spans.forEach { span ->
        val start = getSpanStart(span).coerceAtLeast(0)
        val end = getSpanEnd(span).coerceAtMost(length)
        if (start >= end) return@forEach

        when (span) {
            is StyleSpan -> {
                when (span.style) {
                    Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                    Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                    Typeface.BOLD_ITALIC -> addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                        start, end
                    )
                }
            }

            is UnderlineSpan ->
                addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)

            is StrikethroughSpan ->
                addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)

            is ForegroundColorSpan ->
                addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)

            is BackgroundColorSpan ->
                addStyle(SpanStyle(background = Color(span.backgroundColor)), start, end)

            is RelativeSizeSpan ->
                addStyle(SpanStyle(fontSize = (span.sizeChange * 14f).sp), start, end) // 14sp база

            is AbsoluteSizeSpan -> {
                val px = span.size
                // если span в dip -> можно конвертить через density; но чаще size в px.
                addStyle(SpanStyle(fontSize = px.sp), start, end)
            }

            is URLSpan -> {
                // для Text() просто пометим как ссылку (цвет/подчерк)
                addStyle(
                    SpanStyle(
                        color = Color(0xFF1E88E5),
                        textDecoration = TextDecoration.Underline
                    ),
                    start, end
                )
                addStringAnnotation(tag = "URL", annotation = span.url, start = start, end = end)
            }
        }
    }
}
