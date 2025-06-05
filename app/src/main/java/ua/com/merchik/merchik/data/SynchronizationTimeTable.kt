package ua.com.merchik.merchik.data

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import ua.com.merchik.merchik.data.synchronization.DownloadStatus
import ua.com.merchik.merchik.data.synchronization.TableName

@Entity(tableName = "synchronization_timetable")
data class SynchronizationTimeTable(
    @PrimaryKey
    val id: Int,

    val tableName: TableName,

    val syncPeriodSeconds: Long = 0L,       // Период синхронизации
    val lastDownloadTime: Long = 0L,        // Время последней успешной загрузки
    val lastUploadTime: Long = 0L,          // Время последней успешной отправки

    val downloadedItems: Int = 0,           // Количество загруженных записей
    val uploadedItems: Int = 0,             // Количество отправленных записей

    val description: String? = null,        // Описание

    val isUserGenerated: Boolean = false,   // true если данные может создать пользователь

    val lastDownloadStatus: DownloadStatus = DownloadStatus.PENDING, // success / error / pending
    val lastUploadStatus: DownloadStatus = DownloadStatus.PENDING     // success / error / pending
)
