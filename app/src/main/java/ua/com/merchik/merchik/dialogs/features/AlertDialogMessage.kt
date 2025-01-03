package ua.com.merchik.merchik.dialogs.features

import android.app.Activity
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import ua.com.merchik.merchik.dialogs.features.dialogLoading.LoadingDialog
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog


class AlertDialogMessage(val context: Activity,
                         private val title: String,
                         private val message: String) {

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

    fun onDialogDismissed() {


        // Логика завершения или отмены загрузки
//        Toast.makeText(context, "Загрузка отменена или завершена", Toast.LENGTH_SHORT).show()

    }
}