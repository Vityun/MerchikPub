package ua.com.merchik.merchik.features.main.Main

import android.content.Intent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.runtime.collectAsState
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
                        text = "## Опис можливих дій"
                    )
                    Spacer(modifier = Modifier.padding(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(color = Color.White)
                    ) {
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.databaseEnabled = true
                                    settings.userAgentString.replace("; wv", "")
                                    webViewClient = WebViewClient()
//                                    webChromeClient = WebChromeClient()
                                    loadUrl("https://google.com")
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

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
fun GoogleMapsBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        val context = LocalContext.current

        // Один экземпляр WebView на всё время жизни композиции
        val webView = remember {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.setSupportMultipleWindows(false)
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                // Принудительно "десктопный" user-agent, чтобы не насильно открывало приложение
                settings.userAgentString = settings.userAgentString
                    .replace("Mobile", "")
                    .replace("Android", "")

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString().orEmpty()

                        // Блокируем открытие внешних intent-ссылок и пытаемся найти веб-фоллбэк
                        if (url.startsWith("intent:") ||
                            url.startsWith("maps:") ||
                            url.startsWith("google.navigation:")
                        ) {
                            // Пытаемся достать browser_fallback_url из intent
                            try {
                                val intent = Intent.parseUri(url, 0)
                                val fallback = intent.getStringExtra("browser_fallback_url")
                                if (!fallback.isNullOrBlank()) {
                                    view?.loadUrl(fallback)
                                }
                            } catch (_: Exception) { /* ignore */ }
                            return true // сами обработали
                        }

                        // mailto:, tel:, geo: и т.п. — лучше гасить внутри, чтобы не прыгало наружу
                        if (url.startsWith("mailto:") || url.startsWith("tel:") || url.startsWith("geo:")) {
                            return true
                        }

                        // Всё остальное — грузим внутри WebView
                        return false
                    }


                }

                // Лёгкая веб-версия карт без агрессивных редиректов в app
                loadUrl("https://www.google.com/maps?force=lite&hl=ru")
                // можно: loadUrl("https://www.google.com/maps/search/?api=1&query=Kyiv&hl=ru")
            }
        }

        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize(),
            update = { /* nothing */ }
        )

        // Назад по истории WebView
        androidx.activity.compose.BackHandler(enabled = webView.canGoBack()) {
            webView.goBack()
        }
    }
}
