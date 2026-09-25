package ua.com.merchik.merchik.dialogs.features.masterCode

import android.annotation.SuppressLint
import android.app.Activity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import kotlin.math.abs

class MasterCodeDialogLauncher(private val activity: Activity) : View.OnTouchListener,
    View.OnAttachStateChangeListener {
    private var versionView: View? = null
    private var composeView: ComposeView? = null
    private var pressing = false
    private var longPressStarted = false
    private var downX = 0f
    private var downY = 0f
    private val touchSlop = ViewConfiguration.get(activity).scaledTouchSlop
    private val startLongPress = Runnable {
        val view = versionView
        if (pressing && view?.isAttachedToWindow == true && view.isShown && activity.hasWindowFocus()) {
            longPressStarted = true
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    private val openDialog = Runnable {
        val view = versionView
        if (pressing && view?.isAttachedToWindow == true && view.isShown && activity.hasWindowFocus()) {
            cancelPendingPress()
            show()
        }
    }

    fun attachTo(view: View) {
        if (versionView === view) return
        detach()
        versionView = view
        view.setOnTouchListener(this)
        view.addOnAttachStateChangeListener(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                cancelPendingPress()
                pressing = true
                downX = event.x
                downY = event.y
                view.isPressed = true
                view.postDelayed(startLongPress, ViewConfiguration.getLongPressTimeout().toLong())
                view.postDelayed(openDialog, HOLD_DURATION_MS)
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(event.x - downX) > touchSlop || abs(event.y - downY) > touchSlop ||
                    event.x < 0 || event.x > view.width || event.y < 0 || event.y > view.height
                ) cancelPressWithFeedback(view)
            }
            MotionEvent.ACTION_UP -> {
                val shortClick = pressing && !longPressStarted
                cancelPressWithFeedback(view)
                if (shortClick) view.performClick()
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_DOWN -> cancelPressWithFeedback(view)
        }
        return true
    }

    fun cancelPendingPress() {
        pressing = false
        longPressStarted = false
        versionView?.removeCallbacks(startLongPress)
        versionView?.removeCallbacks(openDialog)
        versionView?.isPressed = false
    }

    private fun cancelPressWithFeedback(view: View) {
        val wasLongPress = pressing && longPressStarted
        cancelPendingPress()
        if (wasLongPress && view.isAttachedToWindow && view.isShown && activity.hasWindowFocus()) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    private fun detach() {
        cancelPendingPress()
        versionView?.setOnTouchListener(null)
        versionView?.removeOnAttachStateChangeListener(this)
        versionView = null
    }

    override fun onViewAttachedToWindow(view: View) = Unit

    override fun onViewDetachedFromWindow(view: View) {
        cancelPendingPress()
    }

    private fun show() {
        if (composeView != null || activity.isFinishing || activity.isDestroyed) return
        val root = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        versionView?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        val view = ComposeView(activity)
        composeView = view
        view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = Unit

            override fun onViewDetachedFromWindow(v: View) {
                if (composeView === v) dismiss()
            }
        })
        view.setContent {
            MerchikTheme {
                MasterCodeDialog(onDismiss = ::dismiss)
            }
        }
        root.addView(view, ViewGroup.LayoutParams(0, 0))
    }

    private fun dismiss() {
        val view = composeView ?: return
        composeView = null
        view.disposeComposition()
        (view.parent as? ViewGroup)?.removeView(view)
    }

    fun dispose() {
        detach()
        dismiss()
    }

    companion object {
        private const val HOLD_DURATION_MS = 3_000L
    }
}
