package ua.com.merchik.merchik.features.main.componentsUI

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.R

data class YouTubePlayerDialogUiState(
    val lessonId: Int,
    val url: String,
    val videoId: String,
    val title: String
)

@Composable
fun YouTubePlayerDialog(
    state: YouTubePlayerDialogUiState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var isPreparing by remember(state.lessonId, state.videoId) { mutableStateOf(true) }
    var playerError by remember(state.lessonId, state.videoId) { mutableStateOf<String?>(null) }
    var userFullscreen by remember(state.lessonId, state.videoId) { mutableStateOf(false) }
    val isFullscreen = isLandscape || userFullscreen

    val playerView = remember(state.lessonId, state.videoId) {
        YouTubePlayerView(context).apply {
            enableAutomaticInitialization = false
        }
    }

    DisposableEffect(playerView, lifecycleOwner, state.lessonId, state.videoId) {
        fun postToUi(block: () -> Unit) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                block()
            } else {
                mainHandler.post { block() }
            }
        }

        val listener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                postToUi {
                    isPreparing = false
                    playerError = null
                }
                youTubePlayer.loadOrCueVideo(
                    lifecycleOwner.lifecycle,
                    state.videoId,
                    0f
                )
            }

            override fun onError(
                youTubePlayer: YouTubePlayer,
                error: PlayerConstants.PlayerError
            ) {
                val message = buildYouTubePlayerErrorMessage(error)
                Globals.writeToMLOG(
                    "ERROR",
                    "YouTubePlayerDialog/onError",
                    "lessonId=${state.lessonId}, videoId=${state.videoId}, error=${error.name}"
                )
                postToUi {
                    isPreparing = false
                    playerError = message
                }
            }
        }

        runCatching {
            lifecycleOwner.lifecycle.addObserver(playerView)
            val origin = "https://${context.packageName}"
            val optionsBuilder = IFramePlayerOptions.Builder()
                .controls(1)
                .rel(0)

            applyOriginIfSupported(optionsBuilder, origin, state)

            val options = optionsBuilder.build()
            playerView.initialize(listener, true, options)
        }.onFailure {
            Globals.writeToMLOG(
                "ERROR",
                "YouTubePlayerDialog/initialize",
                "lessonId=${state.lessonId}, videoId=${state.videoId}, exception=$it"
            )
            postToUi {
                isPreparing = false
                playerError = "Не удалось открыть плеер YouTube."
            }
        }

        onDispose {
            runCatching {
                lifecycleOwner.lifecycle.removeObserver(playerView)
                playerView.release()
            }.onFailure {
                Globals.writeToMLOG(
                    "ERROR",
                    "YouTubePlayerDialog/release",
                    "lessonId=${state.lessonId}, videoId=${state.videoId}, exception=$it"
                )
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isFullscreen) {
                        Color.Black
                    } else {
                        Color.Black.copy(alpha = 0.35f)
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .then(
                        if (isFullscreen) {
                            Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 18.dp,
                                    end = 18.dp,
                                    top = 28.dp,
                                    bottom = 28.dp
                                )
                        }
                    )
                    .align(Alignment.Center)
            ) {
                YouTubeDialogActionRow(
                    isFullscreen = isFullscreen,
                    showFullscreenButton = !isLandscape,
                    onFullscreenClick = {
                        userFullscreen = !userFullscreen
                    },
                    onOpenExternal = {
                        openYouTubeExternal(context, state.url, state.videoId)
                    },
                    onDismiss = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(2f)
                )

                Column(
                    modifier = if (isFullscreen) {
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Black)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(12.dp)
                    }
                ) {
                    if (!isFullscreen) {
                    Text(
                        text = state.title.ifBlank { "Відеоурок" },
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    }

                    Box(
                        modifier = (
                                if (isFullscreen) {
                                    Modifier.fillMaxSize()
                                } else {
                                    Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                }
                                )
                            .clip(
                                if (isFullscreen) {
                                    RoundedCornerShape(0.dp)
                                } else {
                                    RoundedCornerShape(6.dp)
                                }
                            )
                            .background(Color.Black)
                    ) {
                        AndroidView(
                            factory = { playerView },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isPreparing) {
                            YouTubePlayerOverlay(
                                text = "Загрузка видео...",
                                showProgress = true,
                                onOpenExternal = null
                            )
                        }

                        playerError?.let { errorText ->
                            YouTubePlayerOverlay(
                                text = errorText,
                                showProgress = false,
                                onOpenExternal = {
                                    openYouTubeExternal(context, state.url, state.videoId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YouTubeDialogActionRow(
    isFullscreen: Boolean,
    showFullscreenButton: Boolean,
    onFullscreenClick: () -> Unit,
    onOpenExternal: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End
    ) {
        if (showFullscreenButton) {
            ImageButton(
                id = if (isFullscreen) R.drawable.ic_fullscreen_exit else R.drawable.ic_fullscreen,
                shape = CircleShape,
                colorImage = ColorFilter.tint(Color.Gray),
                sizeButton = 40.dp,
                sizeImage = 22.dp,
                modifier = Modifier.padding(end = 8.dp, bottom = 10.dp),
                onClick = onFullscreenClick
            )
        }

        ImageButton(
            id = R.drawable.ic_74,
            shape = CircleShape,
            colorImage = ColorFilter.tint(Color.Gray),
            sizeButton = 40.dp,
            sizeImage = 22.dp,
            modifier = Modifier.padding(end = 8.dp, bottom = 10.dp),
            onClick = onOpenExternal
        )

        ImageButton(
            id = R.drawable.ic_letter_x,
            shape = CircleShape,
            colorImage = ColorFilter.tint(Color.Gray),
            sizeButton = 40.dp,
            sizeImage = 24.dp,
            modifier = Modifier.padding(bottom = 10.dp),
            onClick = onDismiss
        )
    }
}

@Composable
private fun YouTubePlayerOverlay(
    text: String,
    showProgress: Boolean,
    onOpenExternal: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.orange)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (onOpenExternal != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOpenExternal,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.orange)
                    )
                ) {
                    Text(
                        text = "Открыть на YouTube",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private fun buildYouTubePlayerErrorMessage(error: PlayerConstants.PlayerError): String {
    return when (error.name) {
        "VIDEO_NOT_FOUND" -> "Видео не найдено или закрыто настройками доступа."
        "VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER" ->
            "YouTube запретил воспроизведение этого видео во встроенном плеере."

        "HTML_5_PLAYER" -> "YouTube не смог воспроизвести видео в HTML5-плеере."
        "ERROR_REQUEST_MISSING_HTTP_REFERER" ->
            "YouTube отклонил встроенный плеер из-за отсутствия идентификации приложения."

        else -> "YouTube вернул ошибку воспроизведения: ${error.name}."
    }
}

private fun applyOriginIfSupported(
    optionsBuilder: IFramePlayerOptions.Builder,
    origin: String,
    state: YouTubePlayerDialogUiState
) {
    runCatching {
        optionsBuilder.javaClass
            .getMethod("origin", String::class.java)
            .invoke(optionsBuilder, origin)
    }.onFailure {
        Globals.writeToMLOG(
            "ERROR",
            "YouTubePlayerDialog/applyOrigin",
            "origin is not supported, lessonId=${state.lessonId}, videoId=${state.videoId}, exception=$it"
        )
    }
}

private fun openYouTubeExternal(
    context: Context,
    url: String,
    videoId: String
) {
    val targetUrl = url.takeIf { it.isNotBlank() }
        ?: "https://www.youtube.com/watch?v=$videoId"

    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    }.onFailure {
        Globals.writeToMLOG(
            "ERROR",
            "YouTubePlayerDialog/openExternal",
            "url=$targetUrl, exception=$it"
        )
    }
}
