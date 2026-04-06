package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import ua.com.merchik.merchik.dataLayer.model.ClickTextAction
import ua.com.merchik.merchik.dataLayer.model.ProductCodeText

private const val PRODUCT_CODE_TAG = "PRODUCT_CODE_TAG"


@Composable
fun ProductCodeClickableText(
    productCodeText: ProductCodeText,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    onClick: (ClickTextAction) -> Unit
) {
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

    ClickableText(
        text = annotated,
        modifier = modifier,
        style = textStyle,
        onClick = { offset ->
            val annotation = annotated
                .getStringAnnotations(
                    tag = PRODUCT_CODE_TAG,
                    start = offset,
                    end = offset
                )
                .firstOrNull()

            if (annotation != null) {
                val split = annotation.item.split("|", limit = 2)
                val action = ClickTextAction(
                    actionId = split.getOrNull(0).orEmpty(),
                    argument = split.getOrNull(1)?.takeIf { it.isNotEmpty() }
                )
                onClick(action)
            }
        }
    )
}