package ua.com.merchik.merchik.dialogs.features

import android.app.Activity
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog

class MessageDialogBuilder(private val context: Activity) {

    private var title: String? = ""
    private var subTitle: String? = ""
    private var message: String = ""
    private var status: DialogStatus = DialogStatus.NORMAL
    private var okButtonName: String = "OK"
    private var onConfirmAction: (() -> Unit)? = null
    private var cancelButtonName: String = "Отмена"
    private var onCancelAction: (() -> Unit?)? = null

    private val isDialogVisible = mutableStateOf(false)


    fun setTitle(title: String?) = apply { this.title = title }
    fun setSubTitle(subTitle: String?) = apply { this.subTitle = subTitle }
    fun setMessage(message: String) = apply { this.message = message }

    //    fun setMessage(messageSpanned: Spanned) = apply { this.messageSpanned = messageSpanned }
    fun setStatus(status: DialogStatus) = apply { this.status = status }

    fun setOnConfirmAction(actionConfirm: (() -> Unit)?) =
        apply { this.onConfirmAction = actionConfirm }

    fun setOnConfirmAction(okButtonName: String, action: (() -> Unit)?) =
        apply {
            this.okButtonName = okButtonName
            this.onConfirmAction = action
        }

    fun setOnCancelAction(actionCancel: (() -> Unit?)?) =
        apply { this.onCancelAction = actionCancel }

    fun setOnCancelAction(cancelButtonName: String, action: (() -> Unit)?) =
        apply {
            this.cancelButtonName = cancelButtonName
            this.onCancelAction = action
        }

    fun show() {

        Log.e("MessageDialogBuilder", "onCancelAction: $onCancelAction")

        isDialogVisible.value = true
        val composeView = ComposeView(context).apply {
            setContent {
                MerchikTheme {
                    if (isDialogVisible.value) {
                        MessageDialog(
                            title = title,
                            subTitle = subTitle,
                            message = message,
                            onDismiss = {
                                isDialogVisible.value = false
                                dismiss()
                            },
                            okButtonName = okButtonName,
                            onConfirmAction = if (onConfirmAction == null) null else {
                                {
                                    isDialogVisible.value = false
                                    onConfirmAction?.invoke()
                                }
                            },
                            cancelButtonName = cancelButtonName,
                            onCancelAction = if (onCancelAction == null) null else {
                                {
                                    isDialogVisible.value = false
                                    onCancelAction?.invoke()
                                }
                            },
                            status = status
                        )
                    }
                }
            }
        }
        context.findViewById<ViewGroup>(android.R.id.content).addView(composeView)
    }

    fun isShowing(): Boolean {
        return isDialogVisible.value
    }

    fun dismiss() {
        isDialogVisible.value = false
        // Логика завершения или отмены загрузки
//        Toast.makeText(context, "Загрузка отменена или завершена", Toast.LENGTH_SHORT).show()
    }
}