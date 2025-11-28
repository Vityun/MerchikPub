package ua.com.merchik.merchik.features.main.Main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.features.main.componentsUI.ContextMenu
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton


@Composable
fun SortingDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {

    val uiState by viewModel.uiState.collectAsState()

    var maxLinesSubTitle by remember { mutableStateOf(1) }
    var showToolTip by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    // 👇 Список "актуальных" полей для сортировки (как у тебя сейчас)
    val itemsSorting: List<SortingField> = remember(uiState.settingsItems) {
        uiState.settingsItems
            .asSequence()
            .filter { !it.key.equals("column_name", true) }      // служебную колонку исключаем
            .filter { !it.key.equals("group_header", true) }
            .filter { setting ->
                val hiddenByUser = setting.isEnabled
                hiddenByUser
            }
            .map { SortingField(it.key, it.text, 1) }
            .toList()
    }

    fun getSelectedItem(
        itemsSorting: List<SortingField>,
        positionFirst: Int
    ): SortingField {
        val fromState = uiState.sortingFields.getOrNull(positionFirst)

        return itemsSorting.firstOrNull {
            it.key?.equals(fromState?.key, true) == true
        }?.copy(
            order = fromState?.order ?: 1,
            group = fromState?.group ?: false
        ) ?: SortingField()
    }

    // --- ОРИГИНАЛЬНЫЕ значения (как в viewModel на момент открытия диалога) ---
    val originalItemFirst = remember(uiState.sortingFields, itemsSorting) {
        getSelectedItem(itemsSorting, 0)
    }
    val originalItemSecond = remember(uiState.sortingFields, itemsSorting) {
        getSelectedItem(itemsSorting, 1)
    }
    val originalItemThird = remember(uiState.sortingFields, itemsSorting) {
        getSelectedItem(itemsSorting, 2)
    }

    // --- ЛОКАЛЬНОЕ СОСТОЯНИЕ ДЛЯ 3 СТРОК ---
    var selectedItemFirst by remember(uiState.sortingFields, itemsSorting) {
        mutableStateOf(originalItemFirst)
    }
    var selectedItemSecond by remember(uiState.sortingFields, itemsSorting) {
        mutableStateOf(originalItemSecond)
    }
    var selectedItemThird by remember(uiState.sortingFields, itemsSorting) {
        mutableStateOf(originalItemThird)
    }

    // helper: какие ключи уже заняты (для ограничения списков выбора)
    fun itemsForRow(current: SortingField, others: List<SortingField>): List<SortingField> {
        val usedKeys = others.mapNotNull { it.key }.toSet()
        return itemsSorting.filter { sf ->
            val k = sf.key
            // поле либо ещё не занято, либо это уже выбранное поле в текущей строке
            k == null || k !in usedKeys || k == current.key
        }
    }

    // 👉 есть ли несохранённые изменения
    val hasUnsavedChanges by remember {
        derivedStateOf {
            originalItemFirst != selectedItemFirst ||
                    originalItemSecond != selectedItemSecond ||
                    originalItemThird != selectedItemThird
        }
    }

    // Общая функция "применить + закрыть"
    fun applyAndDismiss() {
        val localList: List<SortingField?> = listOf(
            selectedItemFirst.takeIf { it.key != null },
            selectedItemSecond.takeIf { it.key != null },
            selectedItemThird.takeIf { it.key != null }
        )

        localList.forEachIndexed { index, sf ->
            viewModel.updateSorting(sf, index)
        }

        viewModel.saveSettings()
        viewModel.updateContent()
        onDismiss.invoke()
    }

    Dialog(onDismissRequest = onDismiss) {
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
                    onClick = {
                        // 👇 крестик: если есть изменения — спрашиваем, иначе просто закрываем
                        if (hasUnsavedChanges) {
                            showUnsavedDialog = true
                        } else {
                            onDismiss.invoke()
                        }
                    }
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Text(
                            text = viewModel.getTranslateString(
                                "Вы можете выбрать реквизиты по которым будет отсортирована табличная часть. " +
                                        "Для получения дополнительной информации нажмите иконку с изображением знака вопроса в верхней части текущей формы.",
                                9071
                            ),

                            maxLines = maxLinesSubTitle,
                            overflow = TextOverflow.Ellipsis,
                            color = if ((viewModel.typeWindow ?: "").equals(
                                    "container",
                                    true
                                )
                            ) Color.DarkGray else Color.Black,
                            textDecoration = if (maxLinesSubTitle == 1) TextDecoration.Underline else null,
                            modifier = Modifier
                                .padding(start = 2.dp, end = 2.dp)
                                .clickable {
                                    maxLinesSubTitle =
                                        if (maxLinesSubTitle == 1) 99 else 1
                                }
                        )
                    }
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

                            // --- ЛОГИКА ДОПУСТИМОСТИ ГРУППИРОВКИ ---
                            val canGroupFirst = true
                            val canGroupSecond = selectedItemFirst.group
                            val canGroupThird = selectedItemFirst.group && selectedItemSecond.group

                            // --- СПИСКИ ДОСТУПНЫХ ПОЛЕЙ ДЛЯ КАЖДОЙ СТРОКИ ---
                            val itemsFirst = itemsForRow(
                                current = selectedItemFirst,
                                others = listOf(selectedItemSecond, selectedItemThird)
                            )
                            val itemsSecond = itemsForRow(
                                current = selectedItemSecond,
                                others = listOf(selectedItemFirst, selectedItemThird)
                            )
                            val itemsThird = itemsForRow(
                                current = selectedItemThird,
                                others = listOf(selectedItemFirst, selectedItemSecond)
                            )

                            DropDownSortingList(
                                title = "Сортувати за:",
                                selectedItem = selectedItemFirst,
                                onSelectedItem = { new ->
                                    selectedItemFirst = new ?: SortingField()
                                },
                                items = itemsFirst,
                                canGroup = canGroupFirst
                            )

                            if (selectedItemFirst.key != null || selectedItemSecond.key != null) {
                                Spacer(modifier = Modifier.padding(10.dp))

                                DropDownSortingList(
                                    title = "Потім по:",
                                    selectedItem = selectedItemSecond,
                                    onSelectedItem = { new ->
                                        selectedItemSecond = new ?: SortingField()
                                    },
                                    items = itemsSecond,
                                    canGroup = canGroupSecond
                                )
                            }

                            if ((selectedItemFirst.key != null && selectedItemSecond.key != null) ||
                                selectedItemThird.key != null
                            ) {
                                Spacer(modifier = Modifier.padding(10.dp))

                                DropDownSortingList(
                                    title = "Потім по:",
                                    selectedItem = selectedItemThird,
                                    onSelectedItem = { new ->
                                        selectedItemThird = new ?: SortingField()
                                    },
                                    items = itemsThird,
                                    canGroup = canGroupThird
                                )
                            }

                        }
                    }
                    Row {
                        Button(
                            onClick = {
                                // Нажали "Отмена" -> НИЧЕГО не шлём во viewModel
                                onDismiss.invoke()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.blue)
                            ),
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
                                // 👇 ТОЛЬКО ТУТ передаём локальное состояние во viewModel
                                applyAndDismiss()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.orange)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(
                                viewModel.getTranslateString(
                                    stringResource(id = R.string.save)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showToolTip) {
        MessageDialog(
            title = "Не доступно",
            status = DialogStatus.ALERT,
            message = "Данный раздел находится в стадии в разработки",
            okButtonName = "Ок",
            onDismiss = {
                showToolTip = false
            },
            onConfirmAction = {
                showToolTip = false
            }
        )
    }

    // 👇 Диалог про несохранённые изменения при нажатии на крестик
    if (showUnsavedDialog) {
        MessageDialog(
            title = "Изменения не сохранены",
            status = DialogStatus.ALERT,
            message = "Вы изменили настройки сортировки. Сохранить изменения?",
            okButtonName = "Сохранить",
            onDismiss = {
                // Не сохраняем, просто закрываем диалог сортировки
                showUnsavedDialog = false
                onDismiss.invoke()
            },
            onConfirmAction = {
                // Сохраняем и закрываем
                showUnsavedDialog = false
                applyAndDismiss()
            }
        )
    }
}

@Composable
fun DropDownSortingList(
    title: String,
    selectedItem: SortingField,
    onSelectedItem: (SortingField?) -> Unit,
    items: List<SortingField>,
    canGroup: Boolean
) {
    // Локальное состояние, синхронизированное с входящим selectedItem
    var localItem by remember(
        selectedItem.key,
        selectedItem.title,
        selectedItem.order,
        selectedItem.group
    ) {
        mutableStateOf(selectedItem)
    }

    val groupOptions = listOf("Не группировать", "Группировать")

    // Если группировка недоступна, но в локальном состоянии она включена — сбросим
    LaunchedEffect(canGroup) {
        if (!canGroup && localItem.group) {
            localItem = localItem.copy(group = false)
            onSelectedItem.invoke(localItem)
        }
    }

    Column {
        Spacer(modifier = Modifier.height(4.dp))

        // --------- Ряд заголовков над полями ---------
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.DarkGray,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(40.dp))

            Text(
                text = "Группировка",
                color = Color.DarkGray,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.width(40.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --------- Ряд с контролами ---------
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Dropdown выбора поля сортировки ---
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContextMenu(
                    onSelectedMenu = { index ->
                        val base = items[index]
                        localItem = localItem.copy(
                            key = base.key,
                            title = base.title
                        ).let {
                            if (it.order == null) it.copy(order = 1) else it
                        }
                        onSelectedItem.invoke(localItem)
                    },
                    itemsMenu = items.mapNotNull { it.title }
                ) {
                    Row {
                        Text(
                            text = localItem.title ?: "",
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

            // --- Кнопка смены порядка сортировки (ASC/DESC) ---
            ImageButton(
                id = if ((localItem.order ?: 1) == 1)
                    R.drawable.ic_arrow_down_2
                else
                    R.drawable.ic_arrow_up_2,
                sizeButton = 40.dp,
                sizeImage = 20.dp,
                colorImage = ColorFilter.tint(color = Color.Gray),
                modifier = Modifier.padding(start = 7.dp),
                onClick = {
                    if (localItem.key != null) {
                        val newOrder = if ((localItem.order ?: 1) == 1) -1 else 1
                        localItem = localItem.copy(order = newOrder)
                        onSelectedItem.invoke(localItem)
                    }
                }
            )

            // --- Dropdown / заглушка группировки ---
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .weight(1f)
                    .padding(start = 7.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canGroup) {
                    ContextMenu(
                        onSelectedMenu = { index ->
                            if (localItem.key == null) return@ContextMenu

                            val groupEnabled = index == 1
                            localItem = localItem.copy(group = groupEnabled)
                            onSelectedItem.invoke(localItem)
                        },
                        itemsMenu = groupOptions
                    ) {
                        Row {
                            Text(
                                text = if (localItem.group) "Группировать" else "-",
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
                } else {
                    Row {
                        Text(
                            text = "-",
                            color = Color.LightGray,
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
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color.LightGray)
                        )
                    }
                }
            }

            // --- Крестик для сброса всей строки ---
            ImageButton(
                id = R.drawable.ic_letter_x,
                sizeButton = 40.dp,
                sizeImage = 20.dp,
                colorImage = ColorFilter.tint(color = Color.Gray),
                modifier = Modifier.padding(start = 7.dp),
                onClick = {
                    localItem = SortingField()
                    onSelectedItem.invoke(null)
                }
            )
        }
    }
}

