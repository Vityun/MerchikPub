package ua.com.merchik.merchik.features.main.componentsUI

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import ua.com.merchik.merchik.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DatePicker(
    title: String,
    enabled: Boolean,
    date: LocalDate?,
    dateChange: (LocalDate) -> Unit
) {
    var selectedDate by remember { mutableStateOf(date ?: LocalDate.now()) }
    val dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

    // Материал диалог
    val dateDialog = rememberMaterialDialogState()

    Column(
        modifier = Modifier.padding(bottom = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "$title:")

        Box(
            modifier = Modifier
                .border(
                    BorderStroke(1.dp, colorResource(id = R.color.borderContextMenu)),
                    RoundedCornerShape(8.dp)
                )
                .then(if (enabled) Modifier.clickable { dateDialog.show() } else Modifier)
        ) {
            Text(
                text = selectedDate.format(dateFormat),
                modifier = Modifier.padding(7.dp)
            )
        }

        val formattedDate = selectedDate.format(
            DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.getDefault())
        )
        // Компонент выбора даты
        MaterialDialog(
            dialogState = dateDialog,
            buttons = {
                positiveButton(
                    text = "ОК",
                    textStyle = TextStyle(color = colorResource(R.color.blue),
                        fontWeight = FontWeight.Black) // зелёный OK
                )
                negativeButton(
                    text = "Скасувати",
                    textStyle = TextStyle(color = colorResource(R.color.orange),
                        fontWeight = FontWeight.Black) // красный Cancel
                )
            }
        ) {
            datepicker(
                initialDate = selectedDate,
                title = title,
                colors =  DatePickerDefaults.colors(
                    headerBackgroundColor = Color(0xFFB1B1B1),     // фон хедера
                    headerTextColor = Color.White,                 // текст хедера
                    calendarHeaderTextColor = Color(0xFFB1B1B1),   // названия дней недели
                    dateActiveBackgroundColor = Color(0xFFB1B1B1), // выбранная дата
                    dateActiveTextColor = Color.White              // текст выбранной даты
                )
            ) { newDate ->
                selectedDate = newDate
                dateChange(newDate)
            }
        }
    }
}

