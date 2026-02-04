package ua.com.merchik.merchik.dialogs.features

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.dialogs.features.dialogMessage.InfoDialog

class InfoDialogBuilder(private val context: Context) {

    private var title: String? = ""
    private var subTitle: String? = ""
    private var message: CharSequence = ""

    private val isDialogVisible = mutableStateOf(false)

    fun setTitle(title: String?) = apply { this.title = title }
    fun setSubTitle(subTitle: String?) = apply { this.subTitle = subTitle }

    fun setMessage(message: CharSequence) = apply {
        Log.e("!!!!!!!!","res: $message")
        this.message = message
    }

    fun show() {

        isDialogVisible.value = true

        val composeView = ComposeView(context).apply {
            setContent {
                MerchikTheme {
                    if (isDialogVisible.value) {
                        InfoDialog(
                            title = title,
                            subTitle = subTitle,
                            message = message,
                            onDismiss = {
                                internalDismiss()
                            }
                        )
                    }
                }
            }
        }
        if (context is Activity)
            context.findViewById<ViewGroup>(android.R.id.content).addView(composeView)

    }


    private fun internalDismiss() {
        if (!isDialogVisible.value) return
        isDialogVisible.value = false
    }

}