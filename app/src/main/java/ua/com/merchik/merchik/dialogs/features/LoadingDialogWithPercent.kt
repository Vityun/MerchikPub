package ua.com.merchik.merchik.dialogs.features

import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import ua.com.merchik.merchik.dialogs.features.dialogLoading.LoadingDialog
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel


class LoadingDialogWithPercent(val context: AppCompatActivity,
                               private val progressViewModel: ProgressViewModel) {

    private val isDialogVisible = mutableStateOf(false)

    fun show() {
        isDialogVisible.value = true
        val composeView = ComposeView(context).apply {
            setContent {
                if (isDialogVisible.value) {
                    LoadingDialog(
                        viewModel = progressViewModel,
                        onDismiss = {
                            isDialogVisible.value = false
                            onDialogDismissed() // Обрабатываем завершение загрузки
                        }
                    )
                }
            }
        }
        // Вставляем ComposeView в корневой layout
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