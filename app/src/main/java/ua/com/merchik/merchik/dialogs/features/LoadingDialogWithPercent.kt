package ua.com.merchik.merchik.dialogs.features

import android.app.Activity
import android.view.ViewGroup
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
    private var composeView: ComposeView? = null

    fun show() {
        if (isDialogVisible.value) return

        isDialogVisible.value = true
        val view = ComposeView(context).apply {
            setContent {
                MerchikTheme {
                    if (isDialogVisible.value) {
                        LoadingDialog(
                            viewModel = progressViewModel,
                            onDismiss = {
                                dismiss()
                                listener?.onDialogDismissed() // Вызываем слушатель
                            }
                        )
                    }
                }
            }
        }
        // Вставляем ComposeView в корневой layout
        composeView = view
        context.findViewById<ViewGroup>(android.R.id.content).addView(view)
    }

    fun dismiss() {
        if (!isDialogVisible.value && composeView == null) return

        isDialogVisible.value = false
        composeView?.let { view ->
            (view.parent as? ViewGroup)?.removeView(view)
        }
        composeView = null
    }

    fun isShowing(): Boolean {
        return isDialogVisible.value
    }

    fun setOnDismissListener(listener: DialogDismissedListener) {
        this.listener = listener
    }

}
