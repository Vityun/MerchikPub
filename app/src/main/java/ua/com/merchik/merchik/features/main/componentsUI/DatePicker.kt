package ua.com.merchik.merchik.features.main.componentsUI

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dialogs.features.calendar.MerchikDatePickerDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DatePicker(
    title: String,
    enabled: Boolean,
    date: LocalDate?,
    dateChange: (LocalDate) -> Unit
) {
    val dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")
    var showDateDialog by remember { mutableStateOf(false) }

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
                .then(if (enabled) Modifier.clickable { showDateDialog = true } else Modifier)
        ) {
            Text(
                text = date?.format(dateFormat) ?: stringResource(R.string.ui_date_unbounded),
                modifier = Modifier.padding(7.dp)
            )
        }

        MerchikDatePickerDialog(
            visible = showDateDialog,
            initialDate = date ?: LocalDate.now(),
            title = title,
            onDateSelected = { newDate ->
                dateChange(newDate)
            },
            onDismiss = { showDateDialog = false }
        )
    }
}

