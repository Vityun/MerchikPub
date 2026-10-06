package ua.com.merchik.merchik.dialogs.DialodTAR

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialog
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialogChoice
import ua.com.merchik.merchik.dialogs.features.dialogMessage.MessageDialogSingleChoice

private const val TAR_PHOTO_SOURCE_CAMERA = "camera"
private const val TAR_PHOTO_SOURCE_GALLERY = "gallery"

object TarPhotoSourceDialog {
    @JvmStatic
    fun show(
        activity: Activity,
        onCamera: Runnable,
        onGallery: Runnable,
        onDismiss: Runnable
    ) {
        Launcher(activity, onCamera, onGallery, onDismiss).show()
    }

    private class Launcher(
        private val activity: Activity,
        private val onCamera: Runnable,
        private val onGallery: Runnable,
        private val onDismiss: Runnable
    ) {
        private var composeView: ComposeView? = null
        private var dismissed = false
        private var pendingAction: Runnable? = null

        fun show() {
            if (activity.isFinishing || activity.isDestroyed) {
                onDismiss.run()
                return
            }

            val root = activity.findViewById<ViewGroup>(android.R.id.content)
            if (root == null) {
                onDismiss.run()
                return
            }

            val view = ComposeView(activity)
            composeView = view
            view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) = Unit

                override fun onViewDetachedFromWindow(v: View) {
                    if (composeView === v) finish()
                }
            })
            view.setContent {
                MerchikTheme {
                    val selectedChoice = remember { mutableStateOf<String?>(null) }
                    MessageDialog(
                        title = "Добавить фотографию",
                        message = "Создайте новую фотографию или выберите уже готовую из галереи",
                        status = DialogStatus.NORMAL,
                        onDismiss = ::finish,
                        okButtonName = "ОК",
                        onConfirmAction = {
                            pendingAction = when (selectedChoice.value) {
                                TAR_PHOTO_SOURCE_CAMERA -> onCamera
                                TAR_PHOTO_SOURCE_GALLERY -> onGallery
                                else -> null
                            }
                        },
                        singleChoice = MessageDialogSingleChoice(
                            options = listOf(
                                MessageDialogChoice(TAR_PHOTO_SOURCE_CAMERA, "Сфотографировать"),
                                MessageDialogChoice(TAR_PHOTO_SOURCE_GALLERY, "Выбрать из галереи")
                            ),
                            selectedKey = selectedChoice.value,
                            onSelected = { selectedChoice.value = it }
                        )
                    )
                }
            }
            root.addView(view, ViewGroup.LayoutParams(0, 0))
        }

        private fun finish() {
            if (dismissed) return
            val action = pendingAction
            pendingAction = null
            dismiss()
            if (action == null) {
                onDismiss.run()
            } else {
                action.run()
            }
        }

        private fun dismiss() {
            if (dismissed) return
            dismissed = true
            val view = composeView
            composeView = null
            view?.disposeComposition()
            (view?.parent as? ViewGroup)?.removeView(view)
        }
    }
}
