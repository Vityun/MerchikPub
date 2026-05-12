package ua.com.merchik.merchik.features.main.componentsUI

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import ua.com.merchik.merchik.R
import java.io.File


enum class TovarPhotoQuality {
    NONE,
    PREVIEW,
    FULL
}


data class TovarPhotoDialogUiState(
    val tovarId: String,
    val photoId: String?,
    val imagePath: String?,
    val imageQuality: TovarPhotoQuality,
    val barcode: String,
    val article: String,
    val isLoadingFull: Boolean,
    val imageVersion: Long = System.currentTimeMillis(),
    val loadToken: Long = System.nanoTime()
)


@Composable
fun TovarPhotoDialog(
    state: TovarPhotoDialogUiState,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 40.dp,
                    bottom = 40.dp
                )
                .background(Color.Transparent)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ImageButton(
                    id = R.drawable.ic_letter_x,
                    shape = CircleShape,
                    colorImage = ColorFilter.tint(Color.Gray),
                    sizeButton = 40.dp,
                    sizeImage = 25.dp,
                    modifier = Modifier.padding(bottom = 10.dp),
                    onClick = { onDismiss() }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BarcodeImageBlock(
                        barcode = state.barcode
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CopyableBarcodeText(
                        barcode = state.barcode
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = buildString {
                            append("Штрихкод: ")
                            append(state.barcode)
                            append("\n")
                            append("Артикул: ")
                            append(state.article)
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    ) {
                        GlideTovarImage(
                            imagePath = state.imagePath,
                            imageVersion = state.imageVersion,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (state.isLoadingFull) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                            )
                        }

                        val qualityText = when (state.imageQuality) {
                            TovarPhotoQuality.FULL -> "Повне фото"
                            TovarPhotoQuality.PREVIEW -> "Попереднє фото"
                            TovarPhotoQuality.NONE -> "Фото відсутнє"
                        }

                        Text(
                            text = qualityText,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.55f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlideTovarImage(
    imagePath: String?,
    imageVersion: Long,
    modifier: Modifier = Modifier
) {
    val hasImage = !imagePath.isNullOrBlank()

    if (!hasImage) {
        Image(
            painter = painterResource(R.mipmap.merchik_m),
            contentDescription = "Фото товару відсутнє",
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            contentScale = ContentScale.Fit
        )
        return
    }

    val model = remember(imagePath, imageVersion) {
        buildGlideTovarModel(imagePath)
    }

    val signatureKey = remember(imagePath, imageVersion) {
        buildGlideSignatureKey(
            imagePath = imagePath,
            imageVersion = imageVersion
        )
    }

    key(signatureKey) {
        GlideImage(
            model = model,
            contentDescription = "Фото товару",
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            contentScale = ContentScale.Fit,

            // Важно:
            // loading НЕ ставим, иначе при каждом обновлении будет мигать merchik_m.
            loading = null,

            // merchik_m показываем только если реальная картинка не загрузилась.
            failure = placeholder(R.mipmap.merchik_m),

            transition = CrossFade
        ) { requestBuilder ->
            requestBuilder
                .signature(ObjectKey(signatureKey))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(1200, 1200)
        }
    }
}

private fun buildGlideTovarModel(imagePath: String?): Any {
    if (imagePath.isNullOrBlank()) {
        return R.mipmap.merchik_m
    }

    val path = imagePath.trim()

    return when {
        path.startsWith("content://", ignoreCase = true) -> Uri.parse(path)
        path.startsWith("file://", ignoreCase = true) -> Uri.parse(path)
        path.startsWith("http://", ignoreCase = true) -> path
        path.startsWith("https://", ignoreCase = true) -> path

        else -> File(path)
    }
}

private fun buildGlideSignatureKey(
    imagePath: String?,
    imageVersion: Long
): String {
    if (imagePath.isNullOrBlank()) {
        return "empty_$imageVersion"
    }

    val path = imagePath.trim()

    val fileInfo = when {
        path.startsWith("file://", ignoreCase = true) -> {
            val uri = Uri.parse(path)
            val file = uri.path?.let { File(it) }
            "${file?.lastModified() ?: 0}_${file?.length() ?: 0}"
        }

        path.startsWith("content://", ignoreCase = true) -> {
            "content_$imageVersion"
        }

        path.startsWith("http://", ignoreCase = true) ||
                path.startsWith("https://", ignoreCase = true) -> {
            "remote_$imageVersion"
        }

        else -> {
            val file = File(path)
            "${file.lastModified()}_${file.length()}"
        }
    }

    return "${path}_${imageVersion}_$fileInfo"
}

@Composable
fun BarcodeImageBlock(
    barcode: String
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val barcodeBitmap = remember(barcode) {
        createBarcodeBitmapOrNull(barcode)
    }

    if (barcodeBitmap != null) {
        Image(
            bitmap = barcodeBitmap.asImageBitmap(),
            contentDescription = "Штрихкод",
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clickable {
                    copyBarcodeToClipboard(
                        context = context,
                        clipboard = clipboard,
                        barcode = barcode
                    )
                }
                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                .padding(2.dp),
            contentScale = ContentScale.FillBounds
        )
    } else {
        Text(
            text = "Штрихкод $barcode розпізнати не вдалося",
            color = Color.Red,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    copyBarcodeToClipboard(
                        context = context,
                        clipboard = clipboard,
                        barcode = barcode
                    )
                }
                .padding(8.dp)
        )
    }
}

private fun createBarcodeBitmapOrNull(
    barcode: String
): Bitmap? {
    return try {
        val value = barcode.trim()
        if (value.isBlank()) return null

        val format = if (value.all { it.isDigit() } && value.length == 13) {
            BarcodeFormat.EAN_13
        } else {
            BarcodeFormat.CODE_128
        }

        val bitMatrix = MultiFormatWriter().encode(
            value,
            format,
            900,
            220
        )

        BarcodeEncoder().createBitmap(bitMatrix)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun CopyableBarcodeText(
    barcode: String
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Text(
        text = barcode,
        color = Color(0xFF1565C0),
        textDecoration = TextDecoration.Underline,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                copyBarcodeToClipboard(
                    context = context,
                    clipboard = clipboard,
                    barcode = barcode
                )
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

private fun copyBarcodeToClipboard(
    context: Context,
    clipboard: ClipboardManager,
    barcode: String
) {
    if (barcode.isBlank()) return

    clipboard.setText(AnnotatedString(barcode))

    Toast.makeText(
        context,
        "Штрихкод скопійовано: $barcode",
        Toast.LENGTH_SHORT
    ).show()
}



