package ua.com.merchik.merchik.features.main.Main

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.Padding
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import ua.com.merchik.merchik.features.main.componentsUI.Tooltip

@Composable
fun SettingsDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {

    val uiState by viewModel.uiState.collectAsState()

    var offsetSizeFont by remember { mutableStateOf(viewModel.offsetSizeFonts.value) }

    var showToolTip by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .padding(top = 40.dp, bottom = 40.dp)
            .background(color = Color.Transparent)
    ) {
        Row(modifier = Modifier.align(Alignment.End)) {
            ImageButton(
                id = R.drawable.ic_question_1,
                shape = CircleShape,
                colorImage = ColorFilter.tint(Color.Gray),
                sizeButton = 40.dp,
                sizeImage = 22.dp,
                modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                onClick = { showToolTip = true }
            )

            ImageButton(
                id = R.drawable.ic_letter_x,
                shape = CircleShape,
                colorImage = ColorFilter.tint(color = Color.Gray),
                sizeButton = 40.dp,
                sizeImage = 25.dp,
                modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                onClick = { onDismiss.invoke() }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(color = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally),
                    text = viewModel.getTranslateString(
                        stringResource(id = R.string.ui_setting_table),
                        5990
                    )
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = viewModel.getTranslateString(
                        stringResource(id = R.string.ui_setting_column_visibility_desc),
                        5991
                    )
                )
                Spacer(modifier = Modifier.padding(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(color = Color.White)
                ) {
                    Column {
                        ItemFieldValue(
                            FieldValue(
                                key = "",
                                TextField(
                                    "",
                                    viewModel.getTranslateString(
                                        stringResource(id = R.string.ui_column_name),
                                        5992
                                    ),
                                    MerchModifier(
                                        fontWeight = FontWeight.Bold,
                                        padding = Padding(10.dp, 7.dp, 10.dp, 7.dp)
                                    )
                                ),
                                TextField(
                                    "",
                                    viewModel.getTranslateString(
                                        stringResource(id = R.string.ui_visibility),
                                        5993
                                    ),
                                    MerchModifier(
                                        fontWeight = FontWeight.Bold,
                                        padding = Padding(10.dp, 7.dp, 10.dp, 7.dp),
                                        weight = 1f,
                                        alignment = Alignment.End
                                    )
                                )
                            ),
                            View.VISIBLE
                        )
                        HorizontalDivider(thickness = 1.dp)

                        val items = uiState.settingsItems

// индекс последнего элемента из HEADER_KEYS (учитывая что id_res_image может отсутствовать)
                        val lastHeaderIndex = remember(items) {
                            items.indexOfLast { it.key in HEADER_KEYS }
                        }

                        LazyColumn {
                            itemsIndexed(
                                items = items,
                                key = { _, it -> it.key } // ключ стабильный
                            ) { index, itemSettingsUI ->

                                SettingsItemView(item = itemSettingsUI)

                                if (index == lastHeaderIndex && lastHeaderIndex != -1 && index != items.lastIndex) {
                                    HorizontalDivider(thickness = 1.dp,
                                        color = Color.LightGray,
                                        modifier = Modifier.padding(vertical = 2.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Tooltip(
                    text = viewModel.getTranslateString(
                        stringResource(id = R.string.ui_option_in_progress),
                        6001
                    )
                ) {
                    FontSizeSlider(viewModel, size = 16 + offsetSizeFont) {
                        offsetSizeFont = it - 16
                    }
                }

                Row {
                    Button(
                        onClick = {
                            viewModel.updateContent()
                            onDismiss.invoke()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue)),
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp)
                    ) {
                        Text(
                            viewModel.getTranslateString(
                                stringResource(id = R.string.ui_cancel),
                                5994
                            )
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.saveSettings()
                            viewModel.updateContent()
                            viewModel.updateOffsetSizeFonts(offsetSizeFont)
                            onDismiss.invoke()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.orange)),
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp)
                    ) {
                        Text(viewModel.getTranslateString(stringResource(id = R.string.save)))
                    }
                }
            }
        }

    }
    if (showToolTip) {

        MessageDialog(
            title = "Недоступно",
            status = DialogStatus.ALERT,
            message = "Цей розділ перебуває у стадії розробки",
            okButtonName = "Ок",
            onDismiss = {
                showToolTip = false
            },
            onConfirmAction = {
                showToolTip = false
            }
        )
    }
}

private val HEADER_KEYS = setOf(
    "column_name",
    "group_header",
    "filter_select",
    "id_res_image",
)