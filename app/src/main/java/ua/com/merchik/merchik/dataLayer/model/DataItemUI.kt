package ua.com.merchik.merchik.dataLayer.model

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    val stableId: Long = DataItemIdGenerator.nextId()
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
    val modifierValue: MerchModifier? = null
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
    var index: Int
)


// генератор id для DataItemUI — потокобезопасный
object DataItemIdGenerator {
    // Инициализируем из currentTime, чтобы при перезапуске не начинать с нуля
    private val counter = AtomicLong(System.currentTimeMillis())

    fun nextId(): Long = counter.incrementAndGet()
}