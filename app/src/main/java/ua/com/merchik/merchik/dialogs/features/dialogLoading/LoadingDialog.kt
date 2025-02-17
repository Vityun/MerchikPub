package ua.com.merchik.merchik.dialogs.features.dialogLoading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton


@Composable
fun LoadingDialog(viewModel: ProgressViewModel,onDismiss: () -> Unit) {
    // Для управления состоянием подтверждающего диалога
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val isCompleted by remember { derivedStateOf { viewModel.isCompleted } }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

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
                .width(screenWidth * 0.9f)
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
                    text = viewModel.currentMessage.value,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
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
                    onClick = { showConfirmationDialog = true }
                )
            }
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = Color.White)
            )
            {
                AnimatedLoadingBar(viewModel)
            }
        }
    }

    // Диалог подтверждения
    if (showConfirmationDialog) {
        ConfirmationDialog(
            title = "Вы действительно хотите отменить загрузку?",
            onConfirm = {
                showConfirmationDialog = false
                onDismiss() // Закрываем основной диалог и отменяем загрузку
            },
            onDismiss = { showConfirmationDialog = false } // Закрываем только подтверждающий диалог
        )
    }
}