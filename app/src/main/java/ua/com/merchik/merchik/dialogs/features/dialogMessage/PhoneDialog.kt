package ua.com.merchik.merchik.dialogs.features.dialogMessage


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.*
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dialogs.features.indicator.LineSpinFadeLoaderIndicator
import ua.com.merchik.merchik.features.main.componentsUI.AutoResizeText
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton

@Composable
fun PhoneDialog(
    title: String = "Відновлення пароля",
    subTitle: String = "Для відновлення пароля введіть свій номер телефону",
    initialDigits: String = "",
    onDismiss: () -> Unit,
    onCloseClick: (() -> Unit)? = null,
    okButtonName: String = "Продовжити",
    onConfirmAction: (phoneWithCountry: String, rawDigits: String) -> Unit,
    status: DialogStatus = DialogStatus.NORMAL,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true
) {
    val scrollState = rememberScrollState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    var phoneDigits by rememberSaveable {
        mutableStateOf(initialDigits.filter(Char::isDigit).take(9))
    }
    val isValid = phoneDigits.length == 9

    val composition by rememberLottieComposition(
        when (status) {
            DialogStatus.ERROR -> LottieCompositionSpec.RawRes(R.raw.error)
            DialogStatus.ALERT -> LottieCompositionSpec.RawRes(R.raw.alert)
            else -> LottieCompositionSpec.RawRes(R.raw.status_ok) // ✅ NORMAL
        }
    )
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    var isCompleted by remember { mutableStateOf(false) }
    LaunchedEffect(isCompleted) { if (isCompleted) onDismiss() }

    Dialog(
        onDismissRequest = { onDismiss() },
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
                .background(Color.Transparent)
        ) {
            // X
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
                        onCloseClick?.invoke() ?: run { isCompleted = true }
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
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    // Icon (NORMAL)
                    if (status == DialogStatus.LOADING) {
                        Box(
                            modifier = Modifier.size(68.dp).padding(bottom = 4.dp),
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
                            modifier = Modifier.size(68.dp).padding(bottom = 4.dp),
                            composition = composition,
                            progress = { progress },
                        )
                    }

                    // Subtitle
                    Text(
                        text = subTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // ✅ Phone label + field (как на скрине)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Телефон",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFF2B2B2B),
                            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                        )

                        val mask = "+38(0##) ###-##-##"
                        val maxDigits = remember(mask) { mask.count { it == '#' } } // будет 9

                        OutlinedTextField(
                            value = phoneDigits,
                            onValueChange = { input ->
                                phoneDigits = input.filter(Char::isDigit).take(maxDigits) // ✅ 9
                            },
                            singleLine = true,
                            placeholder = { Text("+38(0__) ___-__-__") },
                            visualTransformation = PhoneMaskVisualTransformation(mask),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Gray,
                                unfocusedBorderColor = Color.LightGray,
                                cursorColor = Color.DarkGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.DarkGray,
                                unfocusedTextColor = Color.DarkGray
                            )
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ✅ One orange button (как на скрине)
                    val enabled = isValid && status != DialogStatus.LOADING

                    Button(
                        onClick = {
                            val phone = "+380$phoneDigits"
                            onConfirmAction(phone, phoneDigits)
                            isCompleted = true
                        },
                        enabled = enabled,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.orange),
                            disabledContainerColor = Color(0xFF8F8F8F),     // ✅ темнее серый
                            contentColor = Color.White,
                            disabledContentColor = Color(0xFFEAEAEA)       // ✅ чуть серее
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        AutoResizeText(
                            text = okButtonName,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            minTextSize = 12.sp,
                            step = 1.sp
                        )
                    }


                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

/**
 * Маска для ввода телефона: value хранит ТОЛЬКО цифры (0..9),
 * на экране показывает формат "+38(0XX) XXX-XX-XX".
 */
class PhoneMaskVisualTransformation(
    private val mask: String,
    private val maskChar: Char = '#'
) : VisualTransformation {

    private val maxDigits = mask.count { it == maskChar }

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter(Char::isDigit).take(maxDigits)

        // ✅ чтобы placeholder показывался, когда ничего не введено
        if (raw.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val out = StringBuilder()
        val o2t = IntArray(raw.length + 1)
        val t2o = ArrayList<Int>(mask.length + 1)

        var rawIndex = 0
        t2o.add(0)
        o2t[0] = 0

        for (ch in mask) {
            if (ch == maskChar) {
                if (rawIndex >= raw.length) break
                out.append(raw[rawIndex])
                rawIndex++
            } else {
                out.append(ch)
            }
            o2t[rawIndex] = out.length
            t2o.add(rawIndex)
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                o2t[offset.coerceIn(0, raw.length)]

            override fun transformedToOriginal(offset: Int): Int =
                t2o[offset.coerceIn(0, t2o.lastIndex)]
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}
