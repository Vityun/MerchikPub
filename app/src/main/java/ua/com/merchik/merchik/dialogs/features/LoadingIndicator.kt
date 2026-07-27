package ua.com.merchik.merchik.dialogs.features

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.dialogs.features.indicator.LineSpinFadeLoaderIndicator

class LoadingIndicator(private val composeContainer: FrameLayout) {

    private var isLoadingIndicatorVisible = false
    private var composeView: ComposeView? = null
    private var isAttachListenerAdded = false

    private val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            attachComposeViewIfNeeded()
        }

        override fun onViewDetachedFromWindow(v: View) {
            detachComposeView()
        }
    }

    fun show() {
        if (isLoadingIndicatorVisible) return

        isLoadingIndicatorVisible = true
        ensureAttachListener()
        attachComposeViewIfNeeded()
    }

    fun hide() {
        isLoadingIndicatorVisible = false
        detachComposeView()
        removeAttachListener()
    }

    fun isLoadingIndicatorShow(): Boolean = isLoadingIndicatorVisible

    private fun ensureAttachListener() {
        if (isAttachListenerAdded) return
        composeContainer.addOnAttachStateChangeListener(attachListener)
        isAttachListenerAdded = true
    }

    private fun removeAttachListener() {
        if (!isAttachListenerAdded) return
        composeContainer.removeOnAttachStateChangeListener(attachListener)
        isAttachListenerAdded = false
    }

    private fun attachComposeViewIfNeeded() {
        if (!isLoadingIndicatorVisible || composeView != null || !composeContainer.isAttachedToWindow) {
            return
        }

        val view = ComposeView(composeContainer.context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        composeContainer.addView(view)
        composeView = view

        view.setContent {
            MerchikTheme {
                LineSpinFadeLoaderIndicator(
                    penThickness = 10f,
                    radius = 22f,
                    elementHeight = 15f,
                    color = Color.Green
                )
            }
        }

        Log.d(
            "Debug!!! 2",
            "composeContainer width: ${composeContainer.width}, height: ${composeContainer.height}"
        )
        Log.d(
            "Debug!!!!!!!!!",
            "composeContainer width: ${composeContainer.width}, height: ${composeContainer.height}"
        )
    }

    private fun detachComposeView() {
        composeView?.let { view ->
            view.disposeComposition()
            composeContainer.removeView(view)
        } ?: composeContainer.removeAllViews()
        composeView = null
    }
}
