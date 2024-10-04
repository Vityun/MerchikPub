package ua.com.merchik.merchik.features.main.Main

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.SettingsItemUI
import ua.com.merchik.merchik.dataLayer.model.TextField

@Composable
fun Float.toPx() = with(LocalDensity.current) { this@toPx.sp.toPx() }

@Composable
fun ItemFieldValue(it: FieldValue, visibilityField: Int? = null) {
    Row(Modifier.fillMaxWidth()) {
        if (visibilityField == View.VISIBLE) {
            ItemTextField(it.field, Modifier.weight(1f))
        }
        ItemTextField(it.value, Modifier.weight(1f))
    }
}

@Composable
private fun ItemTextField(it: TextField, modifier: Modifier? = null) {
    Text(
        text = it.value,
        fontWeight = it.modifierValue?.fontWeight,
        fontStyle = it.modifierValue?.fontStyle,
        color = it.modifierValue?.textColor ?: Color.Black,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = (modifier ?: Modifier)
            .padding(
                start = it.modifierValue?.padding?.start ?: 0.dp,
                top = it.modifierValue?.padding?.top ?: 0.dp,
                end = it.modifierValue?.padding?.end ?: 0.dp,
                bottom = it.modifierValue?.padding?.bottom ?: 0.dp,
            )
            .then(it.modifierValue?.alignment?.let {
                Modifier.wrapContentWidth(it)
            } ?: Modifier)
            .then(it.modifierValue?.background?.let {
                Modifier.background(color = it)
            } ?: Modifier)
    )
}

@Composable
fun SettingsItemView(item: SettingsItemUI) {
    var isChecked by remember { mutableStateOf(item.isEnabled) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = item.text, modifier = Modifier
            .padding(start = 10.dp)
            .align(Alignment.CenterVertically))

        Spacer(modifier = Modifier.weight(1f))

        Checkbox(
            checked = isChecked,
            onCheckedChange = { checked ->
                isChecked = checked
                item.isEnabled = checked
            }
        )
    }
}

@Composable
fun FontSizeSlider(viewModel: MainViewModel, modifier: Modifier = Modifier, size: Float, onChanged: (Float) -> Unit) {
    var fontSize by remember { mutableStateOf(size) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "А",
                    style = TextStyle(fontSize = 14.sp)
                )

                Text(
                    modifier = Modifier.weight(1f),
                    text = viewModel.getTranslateString(stringResource(id = R.string.font_size)),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = fontSize.sp)
                )

                Text(
                    text = "А",
                    style = TextStyle(fontSize = 30.sp)
                )
            }

            Slider(
                modifier = Modifier.padding(top = 18.dp),
                value = fontSize,
                enabled = false,
                onValueChange = {
                    fontSize = it
                    onChanged(it)
                },
                valueRange = 14f..30f,
            )
        }
    }
}