package ua.com.merchik.merchik.dataLayer.model

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ItemUI(
    val id: Long,
    val fields: List<FieldValue>
)

data class MerchModifier(
    val hide: Boolean? = null,
    val fontStyle: FontStyle? = null,
    val fontWeight: FontWeight? = null,
    val padding: Padding? = null,
    val background: Color? = null,
    val alignment: Alignment.Horizontal? = null,
    val weight: Float? = null,
    val visibility: Int? = null,
)

data class Padding(
    val start: Dp? = 0.dp,
    val top: Dp? = 0.dp,
    val end: Dp? = 0.dp,
    val bottom: Dp? = 0.dp,
)

data class TextField(
    val value: String,
    val modifierValue: MerchModifier? = null
)

data class FieldValue(
    val field: TextField,
    val value: TextField
)

data class SettingsItemUI(
    val key: String,
    val text: String,
    var isEnabled: Boolean
)
