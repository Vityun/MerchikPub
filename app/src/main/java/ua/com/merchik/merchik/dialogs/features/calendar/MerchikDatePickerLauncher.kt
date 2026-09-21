package ua.com.merchik.merchik.dialogs.features.calendar

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

/** Hosts the shared Compose calendar for legacy View dialogs. */
class MerchikDatePickerLauncher(private val context: Context) {
    private var composeView: ComposeView? = null

    fun interface DateSelectedListener {
        fun onDateSelected(value: String)
    }

    fun show(value: String?, title: String?, listener: DateSelectedListener) {
        if (composeView != null) return
        val activity = findActivity(context) ?: return
        if (activity.isFinishing || activity.isDestroyed) return
        val root = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        val initialDate = parseDate(value) ?: LocalDate.now()
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
                MerchikDatePickerDialog(
                    visible = true,
                    initialDate = initialDate,
                    title = title?.takeIf { it.isNotBlank() } ?: "Оберіть дату",
                    onDateSelected = { listener.onDateSelected(it.toString()) },
                    onDismiss = ::dismiss
                )
            }
        }
        root.addView(view, ViewGroup.LayoutParams(0, 0))
    }

    fun dismiss() {
        val view = composeView ?: return
        composeView = null
        view.disposeComposition()
        (view.parent as? ViewGroup)?.removeView(view)
    }

    companion object {
        private val storageFormatter = DateTimeFormatter.ofPattern("uuuu-M-d", Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT)
        private val displayFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT)

        @JvmStatic
        fun formatForDisplay(value: String?): String =
            parseDate(value)?.format(displayFormatter).orEmpty()

        @JvmStatic
        fun toStorageDate(value: String?): String =
            parseDate(value)?.toString() ?: "0000-00-00"

        private fun parseDate(value: String?): LocalDate? {
            if (value.isNullOrBlank()) return null
            for (formatter in listOf(storageFormatter, displayFormatter)) {
                try {
                    return LocalDate.parse(value.trim(), formatter).takeIf { it.year in 1..9999 }
                } catch (_: DateTimeParseException) {
                    // Legacy values may use either format or the empty-date sentinel.
                }
            }
            return null
        }

        private fun findActivity(context: Context): Activity? {
            var current = context
            while (current is ContextWrapper) {
                if (current is Activity) return current
                val base = current.baseContext
                if (base === current) break
                current = base
            }
            return null
        }
    }
}
