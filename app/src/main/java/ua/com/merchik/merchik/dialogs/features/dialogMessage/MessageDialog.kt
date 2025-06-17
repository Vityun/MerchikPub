package ua.com.merchik.merchik.dialogs.features.dialogMessage

import android.annotation.SuppressLint
import android.text.Html
import android.text.Spanned
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import java.time.LocalDate

@Composable
fun MessageDialog(
    title: String? = "",
    subTitle: String? = "",
    message: String = "",
    onDismiss: () -> Unit,
    okButtonName: String = "Ok",
    onConfirmAction: (() -> Unit)? = null,
    cancelButtonName: String = "Отмена",
    onCancelAction: (() -> Unit)? = null,
    status: DialogStatus? = DialogStatus.NORMAL,
    showCheckbox: Boolean = false,
    onCheckboxChanged: ((Boolean) -> Unit)? = null,
) {
    val scrollState = rememberScrollState()
    val styledAnnotatedString = AnnotatedString.fromHtml(message)
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    var isChecked by remember { mutableStateOf(false) }

    val composition by rememberLottieComposition(
        when (status) {
            DialogStatus.ERROR -> LottieCompositionSpec.RawRes(R.raw.error)
            DialogStatus.ALERT -> LottieCompositionSpec.RawRes(R.raw.alert)
            else -> LottieCompositionSpec.RawRes(R.raw.status_ok)
        }
    )
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    var isCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(isCompleted) {
        if (isCompleted) onDismiss()
    }

    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .width(screenWidth * 0.9f)
                .padding(bottom = 44.dp)
                .background(color = Color.Transparent)
        ) {
            // Заголовок и кнопка "X"
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(start = 15.dp, bottom = 15.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                ImageButton(
                    id = R.drawable.ic_letter_x,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier.padding(end = 10.dp),
                    onClick = { isCompleted = true }
                )
            }

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .verticalScroll(scrollState)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    title?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .scale(1.1f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (status != DialogStatus.EMPTY) {
                        LottieAnimation(
                            modifier = Modifier.size(68.dp).padding(bottom = 4.dp),
                            composition = composition,
                            progress = { progress },
                        )
                    }

                    subTitle?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .scale(1.1f)
                        )
                    }

                    Text(
                        text = styledAnnotatedString,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xCC1E201D),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .padding(bottom = 2.dp)
                    )

                    // ✅ Чекбокс "Не показывать больше"
                    if (showCheckbox) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, start = 8.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    isChecked = it
                                    onCheckboxChanged?.invoke(it)
                                }
                            )
                            Text(
                                text = stringResource(id = R.string.not_show_again), // строка из ресурсов
                                modifier = Modifier.padding(start = 4.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Кнопки
                    if (onConfirmAction != null || onCancelAction != null) {
                        Spacer(modifier = Modifier.padding(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            onCancelAction?.let {
                                Button(
                                    onClick = {
                                        it()
                                        isCompleted = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue)),
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .weight(1f)
                                ) {
                                    Text(cancelButtonName)
                                }
                            } ?: Spacer(modifier = Modifier.weight(1f).padding(16.dp))

                            onConfirmAction?.let {
                                Button(
                                    onClick = {
                                        it()
                                        isCompleted = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.orange)),
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .weight(1f)
                                ) {
                                    Text(okButtonName)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
