package ua.com.merchik.merchik.ServerExchange.feature

import androidx.appcompat.app.AppCompatActivity
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel

object KotlinBridge {
    @JvmStatic
    fun startSyncAfterLogin(
        context: AppCompatActivity,
        syncRepository: DataSyncRepository,
        progress: ProgressViewModel,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        startSyncAfterLogin(context, syncRepository, progress, onSuccess, onFailure)
    }
}
