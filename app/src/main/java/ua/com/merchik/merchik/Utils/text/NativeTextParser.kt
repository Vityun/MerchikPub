package ua.com.merchik.merchik.Utils.text

import java.util.Locale
import java.util.regex.Pattern

data class NativeTextAction(val kind: Kind, val value: String) {
    enum class Kind {
        COPY_UNLOCK_CODE, VISIT_BY_NUMBER, VISIT_BY_DAD2, PHOTO_BY_SERVER_ID,
        TASK_BY_ID, RECLAMATION_BY_ID, TASK_BY_NUMBER, RECLAMATION_BY_NUMBER, DIAL
    }
}

data class NativeTextLink(val start: Int, val end: Int, val action: NativeTextAction)
data class NativeTextStyle(val start: Int, val end: Int, val kind: Kind, val color: Int? = null) {
    enum class Kind { BOLD, ITALIC, UNDERLINE, COLOR }
}
data class ParsedNativeText(
    val text: String,
    val links: List<NativeTextLink>,
    val styles: List<NativeTextStyle>
)

/** Parses server markers and legacy plain-text links without Android, HTML or database access. */
object NativeTextParser {
    @JvmField
    val REPORT_DOCUMENT_PATTERN: Pattern = Pattern.compile(
        "(?<![\\p{L}\\p{N}_])\u0410\u041e\u0438-[0-9]{8}(?![\\p{L}\\p{N}_])"
    )
    @JvmField
    val UNLOCK_CODE_PATTERN: Pattern = Pattern.compile(
        "(?<![\\p{L}\\p{N}_])\u043a\u043e\u0434[\\s\\u00A0]+(?:\u0440\u0430\u0437\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u043a\u0438|\u0440\u043e\u0437\u0431\u043b\u043e\u043a\u0443\u0432\u0430\u043d\u043d\u044f)[\\s\\u00A0]+([\\p{L}\\p{N}]{4})(?![\\p{L}\\p{N}_])",
        Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
    )

    private const val DOCUMENT = "\u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442"
    private const val PHOTO = "\u0444\u043e\u0442\u043e"
    private const val TASK = "\u0437\u0430\u0434\u0430\u0447\u0430"
    private const val RECLAMATION = "\u0440\u0435\u043a\u043b\u0430\u043c\u0430\u0446\u0438\u044f"
    private const val ONE_C = "_1\u0441"
    private const val PHONE = "\u0442\u0435\u043b\u0435\u0444\u043e\u043d"
    private const val BOLD = "\u0442\u0435\u043a\u0441\u0442_\u0436\u0438\u0440\u043d\u044b\u0439"
    private const val ITALIC = "\u0442\u0435\u043a\u0441\u0442_\u043a\u0443\u0440\u0441\u0438\u0432"
    private const val UNDERLINE = "\u0442\u0435\u043a\u0441\u0442_\u043f\u043e\u0434\u0447\u0435\u0440\u043a\u043d\u0443\u0442\u044b\u0439"
    private const val COLOR = "\u0442\u0435\u043a\u0441\u0442_\u0446\u0432\u0435\u0442"
    // Android's ICU regex engine requires the literal closing brace to be escaped too.
    private val markers = Regex("\\{+([^{}]*)\\}")
    private val positiveId = Regex("[0-9]+")
    private val documentNumber = Regex("[\\p{L}\\p{N}._-]+")
    private val phone = Regex("\\+?[0-9 ()-]+")

    @JvmStatic
    fun parse(message: CharSequence?): ParsedNativeText {
        val source = message?.toString().orEmpty()
        val text = StringBuilder()
        val links = mutableListOf<NativeTextLink>()
        val styles = mutableListOf<NativeTextStyle>()

        fun appendPlain(value: String) {
            val offset = text.length
            text.append(value)
            val reports = REPORT_DOCUMENT_PATTERN.matcher(value)
            while (reports.find()) {
                links += NativeTextLink(offset + reports.start(), offset + reports.end(),
                    NativeTextAction(NativeTextAction.Kind.VISIT_BY_NUMBER, reports.group()))
            }
            val codes = UNLOCK_CODE_PATTERN.matcher(value)
            while (codes.find()) {
                links += NativeTextLink(offset + codes.start(1), offset + codes.end(1),
                    NativeTextAction(NativeTextAction.Kind.COPY_UNLOCK_CODE, codes.group(1)))
            }
        }

        var cursor = 0
        for (match in markers.findAll(source)) {
            appendPlain(source.substring(cursor, match.range.first))
            val marker = parseMarker(match.groupValues[1])
            if (marker == null) {
                // Unknown markers stay literal; never reinterpret their IDs or labels as links.
                text.append(match.value)
            } else {
                val start = text.length
                text.append(marker.label)
                marker.action?.let { links += NativeTextLink(start, text.length, it) }
                marker.style?.let { styles += NativeTextStyle(start, text.length, it, marker.color) }
            }
            cursor = match.range.last + 1
        }
        appendPlain(source.substring(cursor))
        return ParsedNativeText(text.toString(), links.sortedBy { it.start }, styles.toList())
    }

    private data class Marker(
        val label: String,
        val action: NativeTextAction? = null,
        val style: NativeTextStyle.Kind? = null,
        val color: Int? = null
    )

    private fun parseMarker(body: String): Marker? {
        val parts = body.split('|', limit = 5)
        if (parts.size !in 2..4) return null
        val type = parts[0].trim().lowercase(Locale.ROOT)
        val id = parts[1].trim()
        val label = parts.getOrNull(2)?.takeIf { it.isNotBlank() } ?: id
        // The optional web platform does not change native navigation.
        val style = when (type) {
            BOLD -> NativeTextStyle.Kind.BOLD
            ITALIC -> NativeTextStyle.Kind.ITALIC
            UNDERLINE -> NativeTextStyle.Kind.UNDERLINE
            COLOR -> NativeTextStyle.Kind.COLOR
            else -> null
        }
        if (style != null) {
            if (label.isEmpty()) return null
            val color = if (style == NativeTextStyle.Kind.COLOR) parseColor(id) ?: return null else null
            return Marker(label, style = style, color = color)
        }

        val kind = when (type) {
            DOCUMENT -> NativeTextAction.Kind.VISIT_BY_DAD2
            PHOTO -> NativeTextAction.Kind.PHOTO_BY_SERVER_ID
            TASK -> NativeTextAction.Kind.TASK_BY_ID
            RECLAMATION -> NativeTextAction.Kind.RECLAMATION_BY_ID
            TASK + ONE_C -> NativeTextAction.Kind.TASK_BY_NUMBER
            RECLAMATION + ONE_C -> NativeTextAction.Kind.RECLAMATION_BY_NUMBER
            PHONE -> NativeTextAction.Kind.DIAL
            else -> return null
        }
        val value = if (type == DOCUMENT) id.removePrefix("code_dad2:").trim() else id
        val valid = when (kind) {
            NativeTextAction.Kind.DIAL -> phone.matches(value) && value.any { it in '0'..'9' }
            NativeTextAction.Kind.TASK_BY_NUMBER, NativeTextAction.Kind.RECLAMATION_BY_NUMBER ->
                documentNumber.matches(value)
            NativeTextAction.Kind.TASK_BY_ID, NativeTextAction.Kind.RECLAMATION_BY_ID ->
                positiveId.matches(value) && (value.toIntOrNull() ?: 0) > 0
            else -> positiveId.matches(value) && (value.toLongOrNull() ?: 0L) > 0L
        }
        if (!valid) return null
        val displayLabel = if (type == DOCUMENT && (label.trim() == id || label.trim() == value)) DOCUMENT else label
        return Marker(displayLabel, NativeTextAction(kind, value))
    }

    private fun parseColor(value: String): Int? {
        val hex = when {
            Regex("#[0-9a-fA-F]{3}").matches(value) -> value.drop(1).map { "$it$it" }.joinToString("")
            Regex("#[0-9a-fA-F]{6}").matches(value) -> value.drop(1)
            else -> namedColors[value.lowercase(Locale.ROOT)] ?: return null
        }
        return (0xFF000000L or hex.toLong(16)).toInt()
    }

    private val namedColors = mapOf(
        "black" to "000000", "white" to "ffffff", "red" to "ff0000", "green" to "008000",
        "blue" to "0000ff", "yellow" to "ffff00", "gray" to "808080", "grey" to "808080",
        "silver" to "c0c0c0", "maroon" to "800000", "purple" to "800080", "fuchsia" to "ff00ff",
        "lime" to "00ff00", "olive" to "808000", "navy" to "000080", "teal" to "008080", "aqua" to "00ffff"
    )
}
