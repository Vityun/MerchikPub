package ua.com.merchik.merchik.dialogs.features

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView

class LoadingIndicator(private val composeContainer: FrameLayout) {

    fun show() {
        // Создаем ComposeView
        Log.d(
            "Debug!!! 1",
            "composeContainer width: ${composeContainer.width}, height: ${composeContainer.height}"
        )

        val composeView = ComposeView(composeContainer.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Устанавливаем Composable
            setContent {

//                test()
//                PacmanIndicator(color = Color.Black, ballDiameter = 50f, canvasSize = 60.dp, animationDuration = 10000)

//                LineSpinFadeLoaderIndicator(
//                    penThickness = 10f,
//                    radius = 22f,
//                    elementHeight = 15f,
//                    color = Color.Green
//                )
            }
        }

        Log.d(
            "Debug!!! 2",
            "composeContainer width: ${composeContainer.width}, height: ${composeContainer.height}"
        )
        // Добавляем в контейнер
        composeContainer.addView(composeView)

        Log.d(
            "Debug!!!!!!!!!",
            "composeContainer width: ${composeContainer.width}, height: ${composeContainer.height}"
        )

    }

    fun hide() {
        // Удаляем все дочерние элементы (или конкретный ComposeView)
        composeContainer.removeAllViews()
    }

}