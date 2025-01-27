package ua.com.merchik.merchik.dialogs.features

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog

class AlertDialogOneButton(
    val context: Activity,
    private val title: String,
    private val message: String,
    private val onConfirmAction: (() -> Unit)? = null // Значение по умолчанию
) {

    private val isDialogVisible = mutableStateOf(false)

    fun show() {
        isDialogVisible.value = true
        val composeView = ComposeView(context).apply {
            setContent {
                if (isDialogVisible.value) {
                    MessageDialog(
                        title = title,
                        message = message,
                        onDismiss = {
                            isDialogVisible.value = false
                            onDialogDismissed()
                        },
                        onConfirmAction = {
                            isDialogVisible.value = false
                            onConfirmAction?.invoke() // Выполняется только если передано действие
                        }
                    )
                }
            }
        }
        context.findViewById<ViewGroup>(android.R.id.content).addView(composeView)
    }

    fun isShowing(): Boolean {
        return isDialogVisible.value
    }

    private fun onDialogDismissed() {
        // Логика при закрытии
    }
}
