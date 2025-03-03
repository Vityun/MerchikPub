package ua.com.merchik.merchik.dialogs.features

import android.app.Activity
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.dialogs.features.dialogLoading.DialogDismissedListener
import ua.com.merchik.merchik.dialogs.features.dialogLoading.LoadingDialog
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel


class LoadingDialogWithPercent(val context: Activity,
                               private val progressViewModel: ProgressViewModel) {

    private val isDialogVisible = mutableStateOf(false)
    private var listener: DialogDismissedListener? = null

    fun show() {
        isDialogVisible.value = true
        val composeView = ComposeView(context).apply {
            setContent {
                MerchikTheme {
                    if (isDialogVisible.value) {
                        LoadingDialog(
                            viewModel = progressViewModel,
                            onDismiss = {
                                isDialogVisible.value = false
                                listener?.onDialogDismissed() // Вызываем слушатель
                            }
                        )
                    }
                }
            }
        }
        // Вставляем ComposeView в корневой layout
        context.findViewById<ViewGroup>(android.R.id.content).addView(composeView)
    }

    fun isShowing(): Boolean {
        return isDialogVisible.value
    }

    fun setOnDismissListener(listener: DialogDismissedListener) {
        this.listener = listener
    }

}