package ua.com.merchik.merchik.Utils.text

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/** Native rendering of plain messages with server markers, not an HTML renderer. */
object NativeTextLinks {
    @JvmStatic
    fun linkify(message: CharSequence?): SpannableString = toSpannable(NativeTextParser.parse(message))

    @JvmStatic
    fun setText(view: TextView, message: CharSequence?) {
        view.text = linkify(message)
        view.movementMethod = LinkMovementMethod.getInstance()
    }

    fun toSpannable(
        parsed: ParsedNativeText,
        onAction: (View, NativeTextAction) -> Unit = { view, action -> NativeTextActions.perform(view.context, action) }
    ): SpannableString = SpannableString(parsed.text).apply {
        parsed.styles.forEach { style ->
            val span = when (style.kind) {
                NativeTextStyle.Kind.BOLD -> StyleSpan(Typeface.BOLD)
                NativeTextStyle.Kind.ITALIC -> StyleSpan(Typeface.ITALIC)
                NativeTextStyle.Kind.UNDERLINE -> UnderlineSpan()
                NativeTextStyle.Kind.COLOR -> ForegroundColorSpan(requireNotNull(style.color))
            }
            setSpan(span, style.start, style.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        parsed.links.forEach { link ->
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) = onAction(widget, link.action)
                override fun updateDrawState(ds: TextPaint) {
                    ds.color = Color.BLUE
                    ds.isUnderlineText = true
                }
            }, link.start, link.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    /** Use with Compose Text; the caller owns navigation and any after-edit refresh. */
    fun toAnnotatedString(
        parsed: ParsedNativeText,
        onAction: (NativeTextAction) -> Unit
    ): AnnotatedString = AnnotatedString.Builder(parsed.text).apply {
        parsed.styles.forEach { style ->
            val span = when (style.kind) {
                NativeTextStyle.Kind.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                NativeTextStyle.Kind.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                NativeTextStyle.Kind.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
                NativeTextStyle.Kind.COLOR -> SpanStyle(color = androidx.compose.ui.graphics.Color(requireNotNull(style.color)))
            }
            addStyle(span, style.start, style.end)
        }
        parsed.links.forEachIndexed { index, link ->
            addLink(LinkAnnotation.Clickable(
                tag = "native-text-$index",
                styles = TextLinkStyles(SpanStyle(
                    color = androidx.compose.ui.graphics.Color.Blue,
                    textDecoration = TextDecoration.Underline
                )),
                linkInteractionListener = { onAction(link.action) }
            ), link.start, link.end)
        }
    }.toAnnotatedString()
}
