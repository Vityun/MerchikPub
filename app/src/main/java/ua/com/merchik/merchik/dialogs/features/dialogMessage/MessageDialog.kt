package ua.com.merchik.merchik.dialogs.features.dialogMessage

import android.content.Intent
import android.net.Uri
import android.text.SpannedString
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.tabs.PHONE_TAG
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.dialogs.features.indicator.LineSpinFadeLoaderIndicator
import ua.com.merchik.merchik.features.main.componentsUI.AutoResizeText
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import java.util.regex.Pattern

@Composable
fun MessageDialog(
    title: String? = "",
    subTitle: String? = "",
    message: String = "",
    onDismiss: () -> Unit,
    onCloseClick: (() -> Unit)? = null,
    okButtonName: String = "Застосувати",
    onConfirmAction: (() -> Unit)? = null,
    cancelButtonName: String = "Скасувати",
    onCancelAction: (() -> Unit)? = null,
    status: DialogStatus? = DialogStatus.NORMAL,
    showCheckbox: Boolean = false,
    onCheckboxChanged: ((Boolean) -> Unit)? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true
) {
    val scrollState = rememberScrollState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // Кешируем разбор сообщения, чтобы не парсить на каждой рекомпозиции
// Кешируем разбор сообщения, чтобы не парсить на каждой рекомпозиции
//    val styledAnnotatedString: AnnotatedString = remember(message, messageAnnotated) {
//        when {
//            // приоритет — уже готовый AnnotatedString с кликами
//            messageAnnotated != null -> messageAnnotated
//
//            else -> {
//                val pattern = Pattern.compile("\\{([^}]*)\\}")
//                val matcher = pattern.matcher(message)
//                if (matcher.find()) {
//                    parseAndReplaceVisitText(message) { codeDad2 ->
//                        try {
//                            RealmManager.getWorkPlanRowByCodeDad2(codeDad2.toLong())
//                        } catch (t: Throwable) {
//                            null
//                        }
//                    }
//                } else {
//                    AnnotatedString.fromHtml(message.replace("\n", "<br>"))
//                }
//            }
//        }
//    }
    val styledAnnotatedString: AnnotatedString = remember(message) {

        val pattern = Pattern.compile("\\{([^}]*)\\}")
        val matcher = pattern.matcher(message)
        if (matcher.find()) {
            parseAndReplaceVisitText(message) { codeDad2 ->
                try {
                    RealmManager.getWorkPlanRowByCodeDad2(codeDad2.toLong())
                } catch (t: Throwable) {
                    null
                }
            }
        } else {

            AnnotatedString.fromHtml(
                htmlString = message.replace("\n", "<br>").replace("/n", "<br>"),
                linkStyles = TextLinkStyles(
                    style = SpanStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        }
    }


    // чекбокс сохраняем при пересоздании конфигурации
    var isChecked by rememberSaveable { mutableStateOf(false) }

    val composition by rememberLottieComposition(
        when (status) {
            DialogStatus.ERROR -> LottieCompositionSpec.RawRes(R.raw.error)
            DialogStatus.ALERT -> LottieCompositionSpec.RawRes(R.raw.alert)
            else -> LottieCompositionSpec.RawRes(R.raw.status_ok)
        }
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    var isCompleted by remember { mutableStateOf(false) }

    // при isCompleted вызываем onDismiss()
    LaunchedEffect(isCompleted) {
        if (isCompleted) onDismiss()
    }

    // Позволяем закрывать диалог тапом вне области — если это НЕ нужно, замените onDismiss на {}
    Dialog(
        onDismissRequest = {
            // сюда прилетает back/клик мимо
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .width(screenWidth * 0.9f)
                .padding(bottom = 44.dp)
                .background(color = Color.Transparent)
        ) {
            // Заголовок и кнопка "X" (кнопка справа)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImageButton(
                    id = R.drawable.ic_letter_x,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier.padding(end = 0.dp),
                    onClick = {
                        if (onCloseClick != null) {
                            onCloseClick()

                        }
                        else
                            isCompleted = true
                    }
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

                    // Title
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

                    // Lottie / loader
                    if (status == DialogStatus.LOADING) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .padding(bottom = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LineSpinFadeLoaderIndicator(
                                penThickness = 10f,
                                radius = 22f,
                                elementHeight = 15f,
                                color = Color.Green
                            )
                        }
                    } else if (status != DialogStatus.EMPTY) {
                        LottieAnimation(
                            modifier = Modifier
                                .size(68.dp)
                                .padding(bottom = 4.dp),
                            composition = composition,
                            progress = { progress },
                        )
                    }

                    // Subtitle
                    subTitle?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = styledAnnotatedString,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xCC1E201D),
                        textAlign = TextAlign.Justify,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .padding(horizontal = if (subTitle.isNullOrEmpty()) 0.dp else 6.dp)
                            .padding(bottom = 2.dp)
                    )


                    // Checkbox
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
                                text = stringResource(id = R.string.not_show_again),
                                modifier = Modifier.padding(start = 4.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Buttons: если обе кнопки заданы — делим пространство, если одна — занимает всю ширину
                    if (onConfirmAction != null || onCancelAction != null) {
                        Spacer(modifier = Modifier.padding(4.dp))

                        when {
                            onConfirmAction != null && onCancelAction != null -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onCancelAction()
                                            isCompleted = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorResource(id = R.color.blue)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(cancelButtonName)
                                    }

                                    Button(
                                        onClick = {
                                            onConfirmAction()
                                            isCompleted = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorResource(id = R.color.orange)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(okButtonName)
                                    }
                                }
                            }

                            onCancelAction != null -> {
                                Row {
                                    Button(
                                        onClick = {
                                            onCancelAction()
                                            isCompleted = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorResource(id = R.color.blue)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    ) {
                                        AutoResizeText(
                                            text = cancelButtonName,
                                            modifier = Modifier.fillMaxWidth(),
                                            style = MaterialTheme.typography.labelLarge, // можно подобрать подходящий стиль
                                            maxLines = 1,
                                            minTextSize = 12.sp,
                                            step = 1.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }

                            onConfirmAction != null -> {
                                Row {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(
                                        onClick = {
                                            onConfirmAction()
                                            isCompleted = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorResource(id = R.color.orange)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    ) {
                                        AutoResizeText(
                                            text = okButtonName,
                                            modifier = Modifier.fillMaxWidth(),
                                            style = MaterialTheme.typography.labelLarge, // можно подобрать подходящий стиль
                                            maxLines = 1,
                                            minTextSize = 12.sp,
                                            step = 1.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.padding(2.dp))
                }
            }
        }
    }
}
