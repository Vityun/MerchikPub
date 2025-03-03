package ua.com.merchik.merchik.dialogs.features

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.dialogs.features.indicator.LineSpinFadeLoaderIndicator

class LoadingIndicator(private val composeContainer: FrameLayout) {

    private val isLoadingIndicatorVisible = mutableStateOf(false)

    fun show() {
        // Создаем ComposeView

        val composeView = ComposeView(composeContainer.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Устанавливаем Composable
            setContent {
                MerchikTheme {
                    LineSpinFadeLoaderIndicator(
                        penThickness = 10f,
                        radius = 22f,
                        elementHeight = 15f,
                        color = Color.Green
                    )
                }
            }
        }

        Log.d(
            "Debug!!! 2",
            "composeContainer width: ${composeContainer.width}, height: ${composeContainer.height}"
        )
        // Добавляем в контейнер
        composeContainer.addView(composeView)
        isLoadingIndicatorVisible.value = true
        Log.d(
            "Debug!!!!!!!!!",
            "composeContainer width: ${composeContainer.width}, height: ${composeContainer.height}"
        )

    }

    fun hide() {
        // Удаляем все дочерние элементы (или конкретный ComposeView)
        isLoadingIndicatorVisible.value = false
        composeContainer.removeAllViews()
    }

    fun isLoadingIndicatorShow(): Boolean = isLoadingIndicatorVisible.value

}