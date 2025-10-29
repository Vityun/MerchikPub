package ua.com.merchik.merchik.features.maps.presentation.main

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import ua.com.merchik.merchik.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.Padding
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.features.main.Main.ItemFieldValue
import ua.com.merchik.merchik.features.main.Main.MainViewModel
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import ua.com.merchik.merchik.features.maps.presentation.MapIntent
import ua.com.merchik.merchik.features.maps.presentation.viewModels.BaseMapViewModel
import androidx.compose.material3.TextFieldDefaults



@Composable
fun MapRadiusDialog(
    vm: BaseMapViewModel,
    mainViewModel: MainViewModel, // если нужно для переводов и цветов как в SettingsDialog
    onDismiss: () -> Unit
) {
    val state by vm.state.collectAsState()

    // текущее значение из VM (в метрах)
    val currentMeters = state.circleRadiusMeters
    var radiusText by remember(currentMeters) { mutableStateOf(currentMeters?.toInt()?.toString().orEmpty()) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 40.dp)
                .background(color = Color.Transparent)
        ) {
            ImageButton(
                id = R.drawable.ic_letter_x,
                shape = CircleShape,
                colorImage = ColorFilter.tint(color = Color.Gray),
                sizeButton = 40.dp,
                sizeImage = 25.dp,
                modifier = Modifier
                    .padding(start = 15.dp, bottom = 10.dp)
                    .align(alignment = Alignment.End),
                onClick = { onDismiss() }
            )

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
                    // Заголовок
                    Text(
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                        text = mainViewModel.getTranslateString(
                            stringResource(id = R.string.ui_setting_table),
                            5990
                        )
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    // Описание (замена "Видимость" -> "Радиус")
                    Text(
                        text = mainViewModel.getTranslateString(
                            text = "Радиус — ограничивающий круг для точек на карте (FromWPdata, user_id=14041).",
                        )
                    )
                    Spacer(modifier = Modifier.padding(8.dp))

                    // Контентная карточка (как у тебя)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(color = Color.White)
                    ) {
                        Column(Modifier.fillMaxWidth()) {

                            // Шапка как в SettingsDialog, но "Радиус"
                            ItemFieldValue(
                                FieldValue(
                                    key = "",
                                    TextField(
                                        "",
                                        mainViewModel.getTranslateString(
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
                                        mainViewModel.getTranslateString(
                                            text = "Радиус"
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

                            // Единственное поле "Радиус работы (м)"
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = mainViewModel.getTranslateString(
                                        text = "Радиус работы (м)",
                                    ),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(6.dp))

                                // Поле ввода чисел
                                androidx.compose.material3.OutlinedTextField(
                                    value = radiusText,
                                    onValueChange = { new ->
                                        // оставляем только цифры
                                        radiusText = new.filter { it.isDigit() }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    placeholder = {
                                        Text(
                                            text = mainViewModel.getTranslateString(
                                                text = "в метрах, например 500",
                                            )
                                        )
                                    },
//                                    colors = TextFieldDefaults.colors(
//                                        focusedTextColor = Color.DarkGray
//                                    ),
                                    supportingText = {
                                        Text(
                                            text = mainViewModel.getTranslateString(
                                                text = "Круг строится от вашего текущего местоположения. " +
                                                        "Точки вне радиуса отображаются полупрозрачными.",
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // нижние кнопки (как в SettingsDialog)
                    Row {
                        Button(
                            onClick = { onDismiss() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(mainViewModel.getTranslateString(stringResource(id = R.string.ui_cancel), 5994))
                        }

                        Button(
                            onClick = {
                                val meters = radiusText.toDoubleOrNull()
                                // null — чтобы можно было "сбросить" кастомный радиус, если поле пустое
                                vm.process(MapIntent.SetCircleRadius(meters))
                                onDismiss()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.orange)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(mainViewModel.getTranslateString(stringResource(id = R.string.save)))
                        }
                    }
                }
            }
        }
    }
}
