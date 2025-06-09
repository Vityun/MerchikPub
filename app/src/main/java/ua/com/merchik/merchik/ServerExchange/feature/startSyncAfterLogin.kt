package ua.com.merchik.merchik.ServerExchange.feature

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.dialogs.features.LoadingDialogWithPercent
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel


fun startSyncAfterLogin(
    context: AppCompatActivity,
    syncRepository: DataSyncRepository,
    progress: ProgressViewModel,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
    val loadingDialog = LoadingDialogWithPercent(context, progress)
    loadingDialog.show()

    CoroutineScope(Dispatchers.Main).launch {
        DataSyncRepository().syncStatusFlow.collectLatest { status ->
            when (status) {
                is SyncStatus.Syncing -> {
                    progress.onNextEvent("Загрузка: ${status.table.name}")
                }
                is SyncStatus.Success -> {
                    Globals.writeToMLOG(
                        "INFO", "startSyncAfterLogin/SyncStatus",
                        "Success for: ${status.table.name}"
                    )

                }
                is SyncStatus.Failure -> {
                    Toast.makeText(context, "Ошибка загрузки ${status.table.name}", Toast.LENGTH_SHORT).show()
                    Globals.writeToMLOG(
                        "ERROR", "startSyncAfterLogin/SyncStatus",
                        "Failure for: ${status.table.name}"
                    )
                }
                SyncStatus.Done -> {
                    progress.onCompleted()
                    onSuccess()
                    return@collectLatest
                }
            }
        }
    }

    CoroutineScope(Dispatchers.IO).launch {
        val ok = syncRepository.syncAllIfNeeded()
        if (!ok) {
            withContext(Dispatchers.Main) {
                progress.onCompleted()
                onFailure()
            }
        }
    }
}
