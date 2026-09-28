package ua.com.merchik.merchik.dataLayer.common

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun rememberImeVisible(): State<Boolean> {
    // Do not replace the host View's inset listeners: Compose uses them for safeDrawing.
    return rememberUpdatedState(WindowInsets.isImeVisible)
}
