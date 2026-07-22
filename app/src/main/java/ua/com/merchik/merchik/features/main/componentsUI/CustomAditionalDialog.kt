package ua.com.merchik.merchik.features.main.componentsUI

import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CustomAditionalCorner = 8.dp
private val CustomAditionalBorderWidth = 1.dp
private val CustomAditionalFieldMinHeight = 42.dp

@Composable
fun CustomAditionalDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    subTitle: String = "",
    helpTitle: String = title,
    helpMessage: String = subTitle,
    showHelpButton: Boolean = helpMessage.isNotBlank(),
    contentScrollable: Boolean = true,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {

    val addressSubtitle = "Адреса по которым вы можете создать новый визит. Если нужного адреса в списке нет, обратитесь за помощью к своему руководителю или службу поддержки."
    val scrollState = rememberScrollState()
    var subTitleMaxLines by remember { mutableIntStateOf(1) }
    var showToolTip by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogView = LocalView.current

        SideEffect {
            (dialogView.parent as? DialogWindowProvider)
                ?.window
                ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = modifier
                    .statusBarsPadding()
                    .fillMaxSize()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 40.dp,
                        bottom = 40.dp
                    )
                    .imePadding()
                    .background(Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (showHelpButton) {
                        ImageButton(
                            id = R.drawable.ic_question_1,
                            shape = CircleShape,
                            colorImage = ColorFilter.tint(Color.Gray),
                            sizeButton = 40.dp,
                            sizeImage = 22.dp,
                            modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                            onClick = { showToolTip = true }
                        )
                    }

                    ImageButton(
                        id = R.drawable.ic_letter_x,
                        shape = CircleShape,
                        colorImage = ColorFilter.tint(Color.Gray),
                        sizeButton = 40.dp,
                        sizeImage = 25.dp,
                        modifier = Modifier.padding(start = 15.dp, bottom = 10.dp),
                        onClick = onDismiss
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        if (subTitle.isNotBlank()) {
                            Spacer(modifier = Modifier.padding(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                            ) {
                                Text(
                                    text = subTitle,
                                    maxLines = subTitleMaxLines,
                                    overflow = TextOverflow.Ellipsis,
                                    textDecoration = if (subTitleMaxLines == 1) {
                                        TextDecoration.Underline
                                    } else {
                                        null
                                    },
                                    modifier = Modifier
                                        .padding(start = 2.dp, end = 2.dp)
                                        .clickable {
                                            subTitleMaxLines =
                                                if (subTitleMaxLines == 1) 99 else 1
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.padding(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(7.dp)
                                    .then(
                                        if (contentScrollable) {
                                            Modifier.verticalScroll(scrollState)
                                        } else {
                                            Modifier
                                        }
                                    ),
                                content = content
                            )
                        }

                        actions?.let { actionsContent ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                content = actionsContent
                            )
                        }
                    }
                }
            }
        }
    }

    if (showToolTip) {
        MessageDialog(
            title = helpTitle,
            status = DialogStatus.ALERT,
            message = helpMessage,
            okButtonName = "OK",
            onDismiss = { showToolTip = false },
            onConfirmAction = { showToolTip = false }
        )
    }
}

@Composable
fun CustomAditionalWorkForm(
    executorFirm: String = "",
    customer: String = "",
    date: String = "",
    address: String = "",
    order: String = "",
    onCustomerClick: (() -> Unit)? = null,
    onDateSelected: (displayDate: String, dateYmd: String) -> Unit = { _, _ -> },
    onAddressClick: (() -> Unit)? = null,
    onOrderClick: (() -> Unit)? = null
) {
    val displayDateFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    }
    val minDate = LocalDate.now()
    var selectedDate by remember(date, displayDateFormatter, minDate) {
        val initialDate = parseCustomAditionalDate(date, displayDateFormatter) ?: minDate
        mutableStateOf(if (initialDate.isBefore(minDate)) minDate else initialDate)
    }
    val dateDialog = rememberMaterialDialogState()

    Column(modifier = Modifier.fillMaxWidth()) {
        CustomAditionalSelectField(
            title = "Фирма - исполнитель",
            value = executorFirm,
            tooltipText = "Это фирма (предприятие) исполнитель работ."
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomAditionalSelectField(
            title = "Фирма - заказчик",
            value = executorFirm,
            tooltipText = "Это фирма (предприятие) заказчик работ.",
            onClick = onCustomerClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomAditionalSelectField(
            title = "Сотрудник - исполнитель",
            value = customer,
            tooltipText = "Это сотрудник исполнитель работы.",
            onClick = onCustomerClick
        )
        Spacer(modifier = Modifier.height(8.dp))

//        Box(
//            modifier = Modifier.fillMaxWidth(),
//            contentAlignment = Alignment.CenterEnd
//        ) {
        CustomAditionalSelectField(
            title = "Дата",
            value = date,
            tooltipText = "Выберите дату новой работы.",
            onClick = { dateDialog.show() },
//                modifier = Modifier.width(152.dp)
        )
//        }

        Spacer(modifier = Modifier.height(8.dp))

        CustomAditionalSelectField(
            title = "Адрес",
            value = address,
            tooltipText = "Выбор адреса будет подключен следующим шагом.",
            onClick = onAddressClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomAditionalSelectField(
            title = "Заказ",
            value = order,
            tooltipText = "Выбор заказа будет подключен следующим шагом.",
            onClick = onOrderClick
        )
    }

    MaterialDialog(
        dialogState = dateDialog,
        buttons = {
            positiveButton(
                text = "ОК",
                textStyle = TextStyle(
                    color = colorResource(R.color.blue),
                    fontWeight = FontWeight.Black
                )
            )
            negativeButton(
                text = "Скасувати",
                textStyle = TextStyle(
                    color = colorResource(R.color.orange),
                    fontWeight = FontWeight.Black
                )
            )
        }
    ) {
        datepicker(
            initialDate = selectedDate,
            title = "Дата",
            allowedDateValidator = { date ->
                !date.isBefore(minDate)
            },
            colors = DatePickerDefaults.colors(
                headerBackgroundColor = Color(0xFFB1B1B1),
                headerTextColor = Color.White,
                calendarHeaderTextColor = Color(0xFFB1B1B1),
                dateActiveBackgroundColor = Color(0xFFB1B1B1),
                dateActiveTextColor = Color.White
            )
        ) { newDate ->
            selectedDate = newDate
            onDateSelected(
                newDate.format(displayDateFormatter),
                newDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            )
        }
    }
}

private fun parseCustomAditionalDate(
    value: String,
    displayDateFormatter: DateTimeFormatter
): LocalDate? {
    if (value.isBlank()) return null

    val trimmed = value.trim()
    return runCatching {
        LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrNull() ?: runCatching {
        LocalDate.parse(trimmed, displayDateFormatter)
    }.getOrNull()
}

@Composable
private fun CustomAditionalSelectField(
    title: String,
    value: String,
    tooltipText: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val content: @Composable (Modifier) -> Unit = { fieldModifier ->
        Box(
            modifier = fieldModifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = CustomAditionalFieldMinHeight)
                    .border(
                        BorderStroke(
                            CustomAditionalBorderWidth,
                            colorResource(id = R.color.borderContextMenu)
                        ),
                        RoundedCornerShape(CustomAditionalCorner)
                    )
                    .padding(
                        top = 12.dp,
                        start = 6.dp,
                        end = 6.dp,
                        bottom = 6.dp
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                CustomAditionalValueChip(
                    value = value,
                    placeholder = "Выбрать",
                    onClick = onClick
                )
            }

            Text(
                text = title,
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 13.sp,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 12.dp, y = (-8).dp)
                    .background(Color.White)
                    .padding(horizontal = 6.dp, vertical = 0.dp)
            )
        }
    }

    if (onClick == null) {
        Tooltip(text = tooltipText, modifier = modifier) {
            content(Modifier)
        }
    } else {
        content(modifier)
    }
}

@Composable
private fun CustomAditionalValueChip(
    value: String,
    placeholder: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isEmpty = value.isBlank()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .wrapContentWidth()
            .background(
                color = colorResource(id = R.color.background_item_filter),
                shape = RoundedCornerShape(CustomAditionalCorner)
            )
            .border(
                BorderStroke(
                    CustomAditionalBorderWidth,
                    colorResource(id = R.color.borderContextMenu)
                ),
                RoundedCornerShape(CustomAditionalCorner)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick.invoke() }
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            modifier = Modifier.padding(
                start = 7.dp,
                top = 4.dp,
                bottom = 4.dp,
                end = 7.dp
            ),
            text = value.ifBlank { placeholder },
            color = if (isEmpty) Color.DarkGray else Color.Black,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun CustomAditionalDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @ColorRes colorResId: Int = R.color.blue
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = colorResId)
        ),
        modifier = modifier.padding(top = 12.dp)
    ) {
        Text(text)
    }
}
