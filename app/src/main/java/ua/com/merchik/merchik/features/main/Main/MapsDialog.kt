package ua.com.merchik.merchik.features.main.Main

import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType.Companion.Uri
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.dataLayer.model.FieldValue
import ua.com.merchik.merchik.dataLayer.model.MerchModifier
import ua.com.merchik.merchik.dataLayer.model.Padding
import ua.com.merchik.merchik.dataLayer.model.TextField
import ua.com.merchik.merchik.features.main.componentsUI.ImageButton
import ua.com.merchik.merchik.features.main.componentsUI.Tooltip


@Composable
fun MapsDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {

    val uiState by viewModel.uiState.collectAsState()

    var offsetSizeFont by remember { mutableStateOf(viewModel.offsetSizeFonts.value) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 40.dp)
                .background(color = Color.Transparent)
        ) {
            ImageButton(
                id = R.drawable.ic_letter_x,
                shape = CircleShape,
                colorImage = ColorFilter.tint(color = Color.Gray),
                sizeButton = 40.dp,
                sizeImage = 25.dp,
                modifier = Modifier
                    .padding(start = 15.dp, bottom = 10.dp)
                    .align(alignment = Alignment.End),
                onClick = { onDismiss.invoke() }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally),
                        text = "Карта"
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = "## Опис можливих дій. Додасться поступово"
                    )
                    Spacer(modifier = Modifier.padding(8.dp))

                    MapWithLoading(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(color = Color.White))
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f)
//                            .shadow(4.dp, RoundedCornerShape(8.dp))
//                            .clip(RoundedCornerShape(8.dp))
//                            .background(color = Color.White)
//                    ) {
//                        AndroidView(
//                            factory = { context ->
//                                WebView(context).apply {
//                                    settings.javaScriptEnabled = true
//                                    settings.domStorageEnabled = true
//                                    settings.databaseEnabled = true
//                                    settings.userAgentString.replace("; wv", "")
//                                    webViewClient = WebViewClient()
//                                    webChromeClient = WebChromeClient()
//                                    loadUrl("https://merchik.net/")
//                                }
//                            },
//                            modifier = Modifier.fillMaxSize()
//                        )
//                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        Button(
                            onClick = {
                                viewModel.updateContent()
                                onDismiss.invoke()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.blue)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.ui_cancel), 5994))
                        }

                        Button(
                            onClick = {
                                viewModel.saveSettings()
                                viewModel.updateContent()
                                viewModel.updateOffsetSizeFonts(offsetSizeFont)
                                onDismiss.invoke()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.orange)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(viewModel.getTranslateString(stringResource(id = R.string.save)))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MapWithLoading(
    url: String = "https://merchik.net/",
    modifier: Modifier
) {
    var committed by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }
    var minTimePassed by remember { mutableStateOf(false) }
    var epoch by remember { mutableStateOf(0) } // ключ новой загрузки
    val minShowMillis = 1800L

    // Таймер минимального показа лоадера
    LaunchedEffect(epoch) {
        minTimePassed = false
        delay(minShowMillis)
        minTimePassed = true
    }

    val showLoading by remember {
        derivedStateOf { !committed || progress < 95 || !minTimePassed }
    }

    Box(
        modifier = modifier
    ) {
        val context = LocalContext.current
        val webView = remember { WebView(context) }

        DisposableEffect(Unit) {
            onDispose { webView.destroy() }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                webView.apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    // важно присвоить!
                    settings.userAgentString = settings.userAgentString.replace("; wv", "")

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(v: WebView?, url: String?, ico: Bitmap?) {
                            committed = false
                            progress = 0
                            epoch++ // перезапуск таймера минимального показа
                        }

                        override fun onPageCommitVisible(view: WebView?, url: String?) {
                            committed = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            if (progress == 100) committed = true // fallback
                        }

                        override fun onReceivedError(
                            view: WebView?, request: WebResourceRequest?, error: WebResourceError?
                        ) {
                            // можно показать экран ошибки; для простоты — скрываем лоадер
                            committed = true
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                        }
                    }

                    loadUrl(url)
                }
            }
        )

        // Лоадер поверх WebView
        AnimatedVisibility(
            visible = showLoading,
            enter = fadeIn(),
            exit = fadeOut(tween(350))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.maps)
                )
                val lottieProgress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    speed = 1.5f
                )
                LottieAnimation(
                    composition = composition,
                    progress = { lottieProgress },
                    modifier = Modifier.fillMaxSize()
                )

                if (progress in 1..99) {
                    Text(
                        text = "$progress%",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        color = Color.Gray
                    )
                }
            }
        }

        // back по истории WebView
        BackHandler(enabled = webView.canGoBack()) {
            webView.goBack()
        }
//        AndroidView(
//            modifier = Modifier.fillMaxSize(),
//            factory = { context ->
//                WebView(context).apply {
//                    settings.javaScriptEnabled = true
//                    settings.domStorageEnabled = true
//                    settings.databaseEnabled = true
//                    // важно: присвоить результат
//                    settings.userAgentString = settings.userAgentString.replace("; wv", "")
//
//                    webViewClient = object : WebViewClient() {
//                        override fun onPageStarted(
//                            view: WebView?, url: String?, favicon: android.graphics.Bitmap?
//                        ) {
//                            isLoading = true
//                        }
//
//                        // Когда контент реально отрисован (API 23+)
//                        override fun onPageCommitVisible(view: WebView?, url: String?) {
//                            isLoading = false
//                        }
//
//                        override fun onPageFinished(view: WebView?, url: String?) {
//                            if (progress == 10000) isLoading = false
//                        }
//
//                        override fun onReceivedError(
//                            view: WebView?,
//                            request: WebResourceRequest?,
//                            error: WebResourceError?
//                        ) {
//                            isLoading = false
//                        }
//                    }
//
//                    webChromeClient = object : WebChromeClient() {
//                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
//                            progress = newProgress
//                        }
//                    }
//
//                    loadUrl(url)
//                }
//            }
//        )
//
//        if (isLoading) {
//            // Оверлей с анимацией Lottie
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.White),
//                contentAlignment = Alignment.Center
//            ) {
//                val composition by rememberLottieComposition(
//                    LottieCompositionSpec.RawRes(R.raw.maps)
//                )
//                val lottieProgress by animateLottieCompositionAsState(
//                    composition = composition,
//                    iterations = LottieConstants.IterateForever,
//                    speed = 1.5f
//                )
//                LottieAnimation(
//                    composition = composition,
//                    progress = { lottieProgress },
//                    modifier = Modifier.fillMaxSize()
//                )
//
//                if (progress in 1..99) {
//                    Text(
//                        text = "$progress%",
//                        modifier = Modifier
//                            .align(Alignment.BottomCenter)
//                            .padding(bottom = 16.dp),
//                        color = Color.Gray
//                    )
//                }
//            }
//        }
    }
}
