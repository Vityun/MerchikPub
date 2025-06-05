package ua.com.merchik.merchik.ServerExchange.workmager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading
import ua.com.merchik.merchik.ServerExchange.download.MainExchange
import ua.com.merchik.merchik.data.SynchronizationTimeTable
import ua.com.merchik.merchik.data.synchronization.DownloadStatus
import ua.com.merchik.merchik.data.synchronization.TableName
import ua.com.merchik.merchik.database.room.DaoInterfaces.SynchronizationTimetableDao
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.dialogs.DialogFilter.Click
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val syncTimetableDao: SynchronizationTimetableDao =
        RoomManager.SQL_DB.synchronizationTimetableDao()

    override suspend fun doWork(): Result {
        return try {
            val syncInfo = syncTimetableDao.getByTableName(TableName.WP_DATA)

            if (syncInfo != null
//                && !isTimeToSync(syncInfo)
//                && syncInfo.lastDownloadStatus != DownloadStatus.PENDING
                ) {
//                updateSyncStatus(TableName.WP_DATA, DownloadStatus.PENDING)

                val result = syncWpData(syncInfo)
                val updatedSyncInfo = syncInfo.copy(
                    lastDownloadTime = System.currentTimeMillis() / 1000,
                    lastDownloadStatus = if (result.success) DownloadStatus.SUCCESS else DownloadStatus.ERROR,
                    downloadedItems = result.downloadedItems
                )

                syncTimetableDao.update(updatedSyncInfo)

                if (result.success) Result.success() else Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            updateSyncStatus(TableName.WP_DATA, DownloadStatus.ERROR)
            Result.failure()
        }
    }

    private suspend fun syncWpData(syncInfo: SynchronizationTimeTable): SyncResult {
        val main = MainExchange()
        return suspendCoroutine { continuation ->
            main.downloadWPData(object : Click {
                override fun <T> onSuccess(data: T) {
                    val itemsCount = if (data is Int) data else 0
                    continuation.resume(SyncResult(true, itemsCount))
                }

                override fun onFailure(error: String) {
                    continuation.resume(SyncResult(false, 0))
                }
            }, syncInfo.lastDownloadTime)
        }

    }

    private fun isTimeToSync(syncInfo: SynchronizationTimeTable): Boolean {
        val currentTime = System.currentTimeMillis() / 1000
        val lastSyncTime = syncInfo.lastDownloadTime
        val syncPeriod = syncInfo.syncPeriodSeconds

        return (currentTime - lastSyncTime) >= syncPeriod
    }

    private suspend fun updateSyncStatus(tableName: TableName, status: DownloadStatus) {
        val syncInfo = syncTimetableDao.getByTableName(tableName)
        syncInfo?.let {
            val updated = it.copy(lastDownloadStatus = status)
            syncTimetableDao.update(updated)
        }
    }

    private data class SyncResult(val success: Boolean, val downloadedItems: Int)
}