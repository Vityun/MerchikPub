package ua.com.merchik.merchik.ServerExchange.workmager

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkManagerHelper {

    fun schedulePhotoDownloadTask(context: Context) {
        // Ограничения для задачи (например, только при подключении к интернету)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Требуется интернет
            .build()

        // Создание периодической задачи
        val photoDownloadWork = PeriodicWorkRequest.Builder(
            DownloadImagesWorker::class.java,
            15, // Интервал повторения (в минутах)
            TimeUnit.MINUTES
        )
            .setInitialDelay(   2, TimeUnit.MINUTES) // Задержка перед первым запуском
            .setConstraints(constraints)
            .build()

        // Запуск задачи
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PhotoDownloadWork", // Уникальное имя задачи
            ExistingPeriodicWorkPolicy.UPDATE, // Заменить существующую задачу
            photoDownloadWork
        )
    }

    fun scheduleWpDataSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = PeriodicWorkRequest.Builder(
            SyncWorker::class.java,
            15, // Интервал повторения (в минутах)
            TimeUnit.MINUTES
        )
            .setInitialDelay(0, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "SyncWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            syncWorkRequest
        )
    }

    fun startSyncWorker(context: Context) {
        val syncRequest = OneTimeWorkRequestBuilder<DownloadImagesWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
    }
}