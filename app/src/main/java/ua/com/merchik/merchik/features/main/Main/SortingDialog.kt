package ua.com.merchik.merchik.features.main.Main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenu
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton

@Composable
fun SortingDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {

    val uiState by viewModel.uiState.collectAsState()

    var selectedItemFirst by remember { mutableStateOf(SortingField()) }
    var selectedItemSecond by remember { mutableStateOf(SortingField()) }
    var selectedItemThird by remember { mutableStateOf(SortingField()) }


    fun getSelectedItem(
        itemsSorting: List<SortingField>,
        positionFirst: Int
    ) = itemsSorting.firstOrNull {
        it.key?.equals(
            uiState.sortingFields.getOrNull(positionFirst)?.key,
            true
        ) == true
    }?.copy(order = uiState.sortingFields.getOrNull(positionFirst)?.order ?: 1)
        ?: SortingField()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
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
                onClick = { onDismiss.invoke() }
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
                    Text(
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally),
                        text = viewModel.getTranslateString(stringResource(id = R.string.ui_setting_table))
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = viewModel.getTranslateString(stringResource(id = R.string.ui_setting_column_visibility_desc))
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
                        Column(
                            modifier = Modifier.padding(7.dp)
                        ) {

                            val itemsSorting = uiState.settingsItems.filter { !it.key.equals("column_name", true) }.map { SortingField(it.key, it.text, 1) }

                            selectedItemFirst = getSelectedItem(itemsSorting, 0)
                            selectedItemSecond = getSelectedItem(itemsSorting, 1)
                            selectedItemThird = getSelectedItem(itemsSorting, 2)

                            DropDownSortingList(
                                title = "Сортировать по:",
                                selectedItem = selectedItemFirst,
                                onSelectedItem = {
                                    viewModel.updateSorting(it, 0)
                                    selectedItemFirst = it ?: SortingField()
                                },
                                items = itemsSorting
                            )

                            if (selectedItemFirst.key != null || selectedItemSecond.key != null) {
                                Spacer(modifier = Modifier.padding(10.dp))

                                DropDownSortingList(
                                    title = "Затем по:",
                                    selectedItem = selectedItemSecond,
                                    onSelectedItem = {
                                        viewModel.updateSorting(it, 1)
                                        selectedItemSecond = it ?: SortingField()
                                    },
                                    items = itemsSorting
                                )
                            }

                            if ((selectedItemFirst.key != null && selectedItemSecond.key != null) || selectedItemThird.key != null) {
                                Spacer(modifier = Modifier.padding(10.dp))

                                DropDownSortingList(
                                    title = "Затем по:",
                                    selectedItem = selectedItemThird,
                                    onSelectedItem = {
                                        viewModel.updateSorting(it, 2)
                                        selectedItemThird = it ?: SortingField()
                                    },
                                    items = itemsSorting
                                )
                            }

                        }
                    }
                    Row {
                        Button(
                            onClick = {
                                onDismiss.invoke()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.ui_cancel)))
                        }

                        Button(
                            onClick = {
                                viewModel.saveSettings()
                                viewModel.updateContent()
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
    }
}

@Composable
fun DropDownSortingList(
    title: String,
    selectedItem: SortingField,
    onSelectedItem: (SortingField?) -> Unit,
    items: List<SortingField>
) {
    var selectedItem by remember { mutableStateOf(selectedItem) }

    Column {
        Text(text = title, color = Color.Black)
        Row {
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .weight(1f)
                    .border(
                        BorderStroke(
                            1.dp,
                            colorResource(id = R.color.borderContextMenu)
                        ), RoundedCornerShape(8.dp)
                    )
            ) {
                ContextMenu(
                    onSelectedMenu = {
                        selectedItem = items[it]
                        onSelectedItem.invoke(items[it])
                    },
                    itemsMenu = items.mapNotNull { it.title }
                ) {
                    Row {
                        Text(
                            text = selectedItem.title ?: "",
                            modifier = Modifier
                                .padding(7.dp)
                                .weight(1f)
                        )
                        Image(
                            painter = painterResource(R.drawable.ic_arrow_down_1),
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 7.dp)
                                .align(Alignment.CenterVertically),
                            contentScale = ContentScale.Inside,
                            contentDescription = null
                        )
                    }
                }
            }
            ImageButton(id = R.drawable.ic_letter_x,
                sizeButton = 40.dp,
                sizeImage = 20.dp,
                colorImage = ColorFilter.tint(color = Color.Gray),
                modifier = Modifier.padding(start = 7.dp),
                onClick = {
                    selectedItem = SortingField()
                    onSelectedItem.invoke(null)
                }
            )
            ImageButton(
                id = if ((selectedItem.order?: 1) == 1) R.drawable.ic_arrow_down_2
                else R.drawable.ic_arrow_up_2,
                sizeButton = 40.dp,
                sizeImage = 20.dp,
                colorImage = ColorFilter.tint(color = Color.Gray),
                modifier = Modifier.padding(start = 7.dp),
                onClick = {
                    selectedItem =selectedItem.copy(order = if (selectedItem.order == 1) -1 else 1)
                    onSelectedItem.invoke(selectedItem)
                }
            )
        }
    }
}