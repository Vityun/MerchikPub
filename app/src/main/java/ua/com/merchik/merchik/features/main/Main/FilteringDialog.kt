package ua.com.merchik.merchik.features.main.Main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.features.main.componentsUI.DatePicker
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import ua.com.merchik.merchik.features.main.componentsUI.TextFieldInputRounded
import java.time.LocalDate

@Composable
fun FilteringDialog(viewModel: MainViewModel,
                    onDismiss: () -> Unit,
                    onChanged: (Filters) -> Unit) {

    var searchStr by remember { mutableStateOf(viewModel.filters?.searchText ?: "") }
    var selectedFilterDateStart by remember { mutableStateOf(viewModel.filters?.rangeDataByKey?.start ?: LocalDate.now()) }
    var selectedFilterDateEnd by remember { mutableStateOf(viewModel.filters?.rangeDataByKey?.end ?: LocalDate.now()) }

    val listState = rememberLazyListState()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
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
                onClick = { onDismiss.invoke() }
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = Color.White)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        fontWeight = FontWeight.Bold,
                        text = viewModel.getTranslateString(stringResource(id = R.string.filters))
                    )

                    if (viewModel.filters?.rangeDataByKey != null) {
                        Row {
                            DatePicker(
                                "Дата з",
                                selectedFilterDateStart
                            ) { selectedFilterDateStart = it }
                            DatePicker(
                                "Дата по",
                                selectedFilterDateEnd
                            ) { selectedFilterDateEnd = it }
                        }
                    }

                    viewModel.filters?.items?.let {
                        LazyColumnScrollbar(
                            modifier = Modifier
                                .padding(start = 10.dp, top = 10.dp, bottom = 7.dp)
                                .weight(1f),
                            state = listState,
                            settings = ScrollbarSettings(
                                scrollbarPadding = 2.dp,
                                alwaysShowScrollbar = true,
                                thumbUnselectedColor = colorResource(id = R.color.scrollbar),
                                thumbSelectedColor = colorResource(id = R.color.scrollbar),
                                thumbShape = CircleShape,
                            ),
                        ) {
                            LazyColumn(
                                state = listState,
                            ) {
                                items(it) {
                                    Column(Modifier.padding(end = 10.dp)) {
                                        Text(
                                            text = it.title,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    BorderStroke(
                                                        1.dp,
                                                        colorResource(id = R.color.borderContextMenu)
                                                    ), RoundedCornerShape(8.dp)
                                                )
                                                .clickable { it.onSelect?.invoke() },
                                        ) {
                                            Text(
                                                modifier = Modifier.padding(7.dp),
                                                text = if (it.rightValuesUI.isEmpty()) "Додати фільтр" else it.rightValuesUI.joinToString(
                                                    ","
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.padding(10.dp))
                                }
                            }
                        }
                    }


                    Row {
                        Button(
                            onClick = {
                                onChanged.invoke(
                                    Filters(
                                        viewModel.filters?.let {
                                            RangeDate(
                                                it.rangeDataByKey?.key,
                                                selectedFilterDateStart,
                                                selectedFilterDateEnd
                                            )
                                        },
                                        searchStr
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue)),
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.apply)))
                        }
                        Spacer(modifier = Modifier.padding(10.dp))
                        Button(
                            onClick = {
                                onChanged.invoke(
                                    Filters(
                                        viewModel.filters?.let {
                                            RangeDate(
                                                it.rangeDataByKey?.key,
                                                LocalDate.now(),
                                                LocalDate.now()
                                            )
                                        },
                                        ""
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.orange)),
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.clear)))
                        }
                    }
                }
            }
        }
    }
}