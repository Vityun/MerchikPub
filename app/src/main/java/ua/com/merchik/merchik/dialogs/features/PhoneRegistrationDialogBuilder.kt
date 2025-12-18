package ua.com.merchik.merchik.dialogs.features



import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.PhoneDialog

class PhoneRegistrationDialogBuilder(private val context: Activity) {

    private var title: String = "Відновлення пароля"
    private var subTitle: String = "Для відновлення пароля введіть свій номер телефону"
    private var initialDigits: String = ""
    private var status: DialogStatus = DialogStatus.NORMAL
    private var okButtonName: String = "Вiдновити"

    private var cancelable: Boolean = true
    private var exitConfirmMessage: String = "Приложение будет закрыто. Завершить работу?"

    private var onDismissListener: (() -> Unit)? = null
    private var onConfirmAction: ((phoneWithCountry: String, rawDigits: String) -> Unit)? = null

    private val isDialogVisible = mutableStateOf(false)
    private var composeView: ComposeView? = null

    fun setTitle(title: String?) = apply { this.title = title ?: "" }
    fun setSubTitle(subTitle: String?) = apply { this.subTitle = subTitle ?: "" }

    /** digits-only (без +38), максимум 10 цифр */
    fun setInitialDigits(digits: String?) = apply {
        this.initialDigits = (digits ?: "").filter(Char::isDigit).take(10)
    }

    fun setStatus(status: DialogStatus) = apply { this.status = status }

    fun setOkButtonName(name: String) = apply { this.okButtonName = name }

    fun setCancelable(cancelable: Boolean) = apply { this.cancelable = cancelable }

    fun setExitConfirmMessage(message: String) = apply { this.exitConfirmMessage = message }

    fun setOnDismissListener(listener: (() -> Unit)?) = apply {
        this.onDismissListener = listener
    }

    fun setOnDismissListener(listener: DialogDismissListener?) = apply {
        this.onDismissListener = { listener?.onDismiss() }
    }

    fun setOnConfirmAction(action: ((String, String) -> Unit)?) = apply {
        this.onConfirmAction = action
    }

    fun setOnConfirmListener(listener: PhoneConfirmListener?) = apply {
        this.onConfirmAction = { phone, raw -> listener?.onConfirm(phone, raw) }
    }

    private fun internalDismiss() {
        if (!isDialogVisible.value) return
        isDialogVisible.value = false

        // убрать ComposeView из иерархии
        val parent = composeView?.parent as? ViewGroup
        if (parent != null && composeView != null) {
            parent.removeView(composeView)
        }
        composeView = null

        onDismissListener?.invoke()
    }

    private fun showExitConfirmDialog() {
        MessageDialogBuilder(context)
            .setTitle("Выход из приложения")
            .setStatus(DialogStatus.ERROR)
            .setMessage(exitConfirmMessage)
            .setOnConfirmAction("Да") { context.finishAffinity() }
            .setOnCancelAction("Отмена") { }
            .show()
    }

    fun show() {
        context.runOnUiThread {
            if (isDialogVisible.value) return@runOnUiThread

            isDialogVisible.value = true

            val root = context.findViewById<ViewGroup>(android.R.id.content)

            val cv = ComposeView(context).apply {
                setContent {
                    MerchikTheme {
                        if (isDialogVisible.value) {
                            PhoneDialog(
                                title = title,
                                subTitle = subTitle,
                                initialDigits = initialDigits,
                                status = status,
                                okButtonName = okButtonName,
                                onDismiss = {
                                    if (cancelable) internalDismiss()
                                },
                                onCloseClick = {
                                    if (cancelable) internalDismiss() else showExitConfirmDialog()
                                },
                                dismissOnBackPress = cancelable,
                                dismissOnClickOutside = cancelable,
                                onConfirmAction = { phoneWithCountry, rawDigits ->
                                    onConfirmAction?.invoke(phoneWithCountry, rawDigits)
                                    internalDismiss()
                                }
                            )
                        }
                    }
                }
            }

            composeView = cv
            root.addView(cv)
        }
    }

    fun isShowing(): Boolean = isDialogVisible.value

    fun dismiss() {
        context.runOnUiThread { internalDismiss() }
    }

    /** Java-friendly */
    fun interface PhoneConfirmListener {
        fun onConfirm(phoneWithCountry: String, rawDigits: String)
    }

    /** Java-friendly */
    interface DialogDismissListener {
        fun onDismiss()
    }
}
