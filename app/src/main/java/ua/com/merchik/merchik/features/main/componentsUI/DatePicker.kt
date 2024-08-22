package ua.com.merchik.merchik.features.main.componentsUI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import ua.com.merchik.merchik.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DatePicker(title: String, date: LocalDate?, dateChange: (date: LocalDate) -> Unit) {
    var selectedDate by remember { mutableStateOf(date ?: LocalDate.now()) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${title}:",
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .clickable { showDialog = true }
                .border(
                    BorderStroke(
                        1.dp,
                        colorResource(id = R.color.borderContextMenu)
                    ), RoundedCornerShape(8.dp)
                )
        ) {
            Text(
                text = selectedDate.format(dateFormat),
                modifier = Modifier.padding(7.dp)
            )
        }

        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val datePicker = android.widget.DatePicker(context).apply {
                            init(selectedDate.year, selectedDate.month.value, selectedDate.dayOfMonth) { _, year, month, dayOfMonth ->
                                selectedDate = LocalDate.of(year, month, dayOfMonth)
                                dateChange.invoke(selectedDate)
                            }
                        }

                        AndroidView(
                            factory = { datePicker },
                            modifier = Modifier.wrapContentWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                // Confirm date selection
                                showDialog = false
                            }) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }
}