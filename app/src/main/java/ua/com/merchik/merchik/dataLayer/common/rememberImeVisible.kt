package ua.com.merchik.merchik.dataLayer.common



import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsAnimationCompat

@Composable
fun rememberImeVisible(): State<Boolean> {
    val view = LocalView.current
    val imeVisible = remember { mutableStateOf(false) }

    DisposableEffect(view) {
        // начальное значение
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            imeVisible.value = insets.isVisible(WindowInsetsCompat.Type.ime())
            insets
        }
        // обновления в процессе анимации (свайпами и т.п.)
        val animCb = object : WindowInsetsAnimationCompat.Callback(
            WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
        ) {
            override fun onProgress(
                insets: WindowInsetsCompat,
                runningAnimations: MutableList<WindowInsetsAnimationCompat>
            ): WindowInsetsCompat {
                imeVisible.value = insets.isVisible(WindowInsetsCompat.Type.ime())
                return insets
            }
        }
        ViewCompat.setWindowInsetsAnimationCallback(view, animCb)

        onDispose {
            ViewCompat.setOnApplyWindowInsetsListener(view, null)
            ViewCompat.setWindowInsetsAnimationCallback(view, null)
        }
    }
    return imeVisible
}
