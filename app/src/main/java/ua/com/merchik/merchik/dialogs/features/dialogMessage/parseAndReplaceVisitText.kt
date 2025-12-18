package ua.com.merchik.merchik.dialogs.features.dialogMessage

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.core.text.HtmlCompat
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.database.realm.RealmManager

import java.util.regex.Pattern

fun parseAndReplaceVisitText(
    serverResponse: String,
    getWpData: (codeDad2: String) -> WpDataDB? // Функция для получения данных из Realm
): AnnotatedString {

    val pattern = Pattern.compile("\\{([^}]*)\\}")
    val matcher = pattern.matcher(serverResponse)

    return buildAnnotatedString {
        var lastIndex = 0

        while (matcher.find()) {
            // Добавляем HTML-текст до совпадения
            appendHtml(serverResponse.substring(lastIndex, matcher.start()))

            // Обрабатываем содержимое фигурных скобок
            val matchText = matcher.group(1) ?: continue
            val parts = matchText.split("|")
            if (parts.size >= 2) {
                val codeDad2 = parts[1].removePrefix("code_dad2:")
                val wp = getWpData(codeDad2)

                val replacementText = if (wp != null) {
                    "${wp.addr_txt}, ${wp.client_txt}"
                } else {
                    "дані не знайдені"
                }

                // Добавляем стилизованный текст
                withStyle(
                    style = SpanStyle(
                        color = androidx.compose.ui.graphics.Color.Red,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(replacementText)
                }

                // Добавляем аннотацию для клика
                addStringAnnotation(
                    tag = "CLICKABLE",
                    annotation = codeDad2,
                    start = length - replacementText.length,
                    end = length
                )
            }

            lastIndex = matcher.end()
        }

        // Добавляем оставшийся HTML-текст
        appendHtml(serverResponse.substring(lastIndex))
    }
}

private fun AnnotatedString.Builder.appendHtml(html: String) {

    html.replace("\n", "<br>")
    val htmlSpanned = HtmlCompat.fromHtml(
        html,
        HtmlCompat.FROM_HTML_MODE_COMPACT
    )

    // Простейшая конвертация Spanned в AnnotatedString
    append(AnnotatedString(htmlSpanned.toString()))

    // Здесь можно добавить более сложную обработку HTML-стилей,
    // если нужно сохранить жирный текст, курсив и т.д.
}
