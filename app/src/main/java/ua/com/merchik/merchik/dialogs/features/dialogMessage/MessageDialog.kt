package ua.com.merchik.merchik.dialogs.features.dialogMessage

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import java.time.LocalDate


@Composable
fun MessageDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirmAction: (() -> Unit)? = null, // Опциональный параметр для действия на кнопке "OK"
    onCancelAction: (() -> Unit)? = null, // Опциональный параметр для действия на кнопке "Oтмена"
    status: DialogStatus? = DialogStatus.NORMAL
) {
    // Для управления состоянием подтверждающего диалога
//    val isCompleted by remember { derivedStateOf { viewModel.isCompleted } }

    Log.e("STATUS!!!", "status: ${status?.name}")

    var isCompleted by remember { mutableStateOf(false) }

    // Закрываем диалог, если задача завершена
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            onDismiss()
        }
    }

    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .padding(bottom = 44.dp)
                .background(color = Color.Transparent)
        ) {

            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(start = 15.dp, bottom = 15.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically // Для выравнивания текста и кнопки по вертикали
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp),
                    color = Color.White
                )
                ImageButton(
                    id = R.drawable.ic_letter_x,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(color = Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier
                        .padding(end = 10.dp),
//                    .padding(start = 15.dp, bottom = 10.dp)
//                    .align(alignment = Alignment.End),
                    onClick = {
                        isCompleted = true
                    }
                )
            }
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = Color.White)
            )
            {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (status != DialogStatus.NORMAL)
                        Image(
                            modifier = Modifier
                                .size(60.dp)
                                .padding(bottom = 8.dp),
                            painter = if (status == DialogStatus.ALERT) painterResource(id = R.drawable.ic_caution)
                            else painterResource(id = R.drawable.ic_exclamation_mark_in_a_circle),
                            colorFilter = if (status == DialogStatus.ERROR) ColorFilter.tint(color = Color.Red) else null,
                            contentDescription = null
                        )
                    Text(
//                        modifier = Modifier
//                            .padding(bottom = 8.dp),
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
//                        fontFamily = FontFamily.Monospace,
                        color = Color(0xCC1E201D),
                        textAlign = TextAlign.Center
                    )

                    // Если есть действие, показываем кнопку "OK"
                    if (onConfirmAction != null) {
                        Spacer(modifier = Modifier.padding(16.dp))
//                        Box(
//                            modifier = Modifier.fillMaxWidth(),
//                            contentAlignment = Alignment.BottomEnd // Располагаем кнопку в правом нижнем углу
//                        ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (onCancelAction != null) {
                                Button(
                                    onClick = {
                                        onCancelAction()
                                        isCompleted = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorResource(
                                            id = R.color.blue
                                        )
                                    ),
                                    modifier = Modifier
                                        .weight(1f),
                                ) {
                                    Text("Змiниты ПТТ")
                                }
                                Spacer(modifier = Modifier.padding(10.dp))
                            } else {
                                Spacer(modifier = Modifier
                                    .weight(1f)
                                    .padding(10.dp))
                            }

                            Button(
                                onClick = {
                                    onConfirmAction()
                                    isCompleted = true

                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(
                                        id = R.color.orange
                                    )
                                ),
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                if (onCancelAction != null) {
                                    Text("Редагувати ПТТ")
                                } else
                                    Text("Вiдправити")
                            }
                        }
                    }
                }
            }
        }
    }
}