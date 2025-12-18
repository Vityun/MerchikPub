package ua.com.merchik.merchik.dialogs.features

import android.app.Activity
import android.content.Context
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.AnnotatedString
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog

class MessageDialogBuilder(private val context: Activity) {

    private var title: String? = ""
    private var subTitle: String? = ""
    private var message: String = ""
    private var messageAnnotated: AnnotatedString? = null
    private var status: DialogStatus = DialogStatus.NORMAL
    private var okButtonName: String = "OK"
    private var onConfirmAction: (() -> Unit)? = null
    private var cancelButtonName: String = "Отмена"
    private var onCancelAction: (() -> Unit?)? = null
    private var showCheckbox: Boolean = false
    private var checkboxPrefKey: String = "not_show_again"

    private val isDialogVisible = mutableStateOf(false)

    private var onDismissListener: (() -> Unit)? = null
    private var cancelable: Boolean = true
    private var exitConfirmMessage: String =
        "Приложение будет закрыто. Завершить работу?"

    fun setTitle(title: String?) = apply { this.title = title }
    fun setSubTitle(subTitle: String?) = apply { this.subTitle = subTitle }

    fun setMessage(message: String) = apply {
        this.message = message
        this.messageAnnotated = null          // строка имеет приоритет, если не задавали спан отдельно
    }

    // NEW: передать SpannableString / Spanned / CharSequence

    fun setAnnotatedMessage(text: AnnotatedString) = apply {
        this.messageAnnotated = text
    }

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

    fun setShowCheckbox(show: Boolean, prefKey: String = "not_show_again") = apply {
        showCheckbox = show
        checkboxPrefKey = prefKey
    }

    fun setCancelable(cancelable: Boolean) = apply {
        this.cancelable = cancelable
    }

    fun setExitConfirmMessage(message: String) = apply {
        this.exitConfirmMessage = message
    }

    fun setOnDismissListener(listener: (() -> Unit)?) = apply {
        this.onDismissListener = listener
    }

    fun setOnDismissListener(listener: DialogDismissListener?) = apply {
        this.onDismissListener = { listener?.onDismiss() }
    }

    private fun internalDismiss() {
        if (!isDialogVisible.value) return
        isDialogVisible.value = false
        onDismissListener?.invoke()
    }

    private fun showExitConfirmDialog() {
        MessageDialogBuilder(context)
            .setTitle("Выход из приложения")
            .setStatus(DialogStatus.ERROR)
            .setMessage(exitConfirmMessage)
            .setOnConfirmAction("Да") {
                context.finishAffinity()
            }
            .setOnCancelAction("Отмена") { }
            .show()
    }

    fun show() {
        val sharedPref = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

        if (showCheckbox && sharedPref.getBoolean(checkboxPrefKey, false)) {
            onConfirmAction?.invoke()
            return
        }

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
                                if (cancelable) {
                                    internalDismiss()
                                } else {
                                    // игнорируем back/клик мимо
                                }
                            },
                            onCloseClick = {
                                if (cancelable) {
                                    internalDismiss()
                                } else {
                                    showExitConfirmDialog()
                                }
                            },
                            okButtonName = okButtonName,
                            onConfirmAction = if (onConfirmAction == null) null else {
                                {
                                    onConfirmAction?.invoke()
                                    internalDismiss()
                                }
                            },
                            cancelButtonName = cancelButtonName,
                            onCancelAction = if (onCancelAction == null) null else {
                                {
                                    onCancelAction?.invoke()
                                    internalDismiss()
                                }
                            },
                            status = status,
                            showCheckbox = showCheckbox,
                            onCheckboxChanged = { checked ->
                                sharedPref.edit().putBoolean(checkboxPrefKey, checked).apply()
                            },
                            dismissOnBackPress = cancelable,
                            dismissOnClickOutside = cancelable
                        )
                    }
                }
            }
        }

        context.findViewById<ViewGroup>(android.R.id.content).addView(composeView)
    }

    fun isShowing(): Boolean = isDialogVisible.value

    fun dismiss() {
        internalDismiss()
    }
}

fun interface DialogDismissListener {
    fun onDismiss()
}

fun interface XButtonClicked {
    fun onDismiss()
}