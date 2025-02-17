package ua.com.merchik.merchik.ServerExchange.workmager

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
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
            .setInitialDelay(2, TimeUnit.MINUTES) // Задержка перед первым запуском
            .setConstraints(constraints)
            .build()

        // Запуск задачи
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PhotoDownloadWork", // Уникальное имя задачи
            ExistingPeriodicWorkPolicy.UPDATE, // Заменить существующую задачу
            photoDownloadWork
        )
    }
}