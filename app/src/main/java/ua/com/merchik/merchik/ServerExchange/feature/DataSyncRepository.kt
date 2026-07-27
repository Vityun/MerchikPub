package ua.com.merchik.merchik.ServerExchange.feature

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import ua.com.merchik.merchik.ServerExchange.feature.strategy.WpDataSyncStrategy
import ua.com.merchik.merchik.Utils.TrustedTime
import ua.com.merchik.merchik.data.SynchronizationTimeTable
import ua.com.merchik.merchik.data.synchronization.DownloadStatus
import ua.com.merchik.merchik.database.room.DaoInterfaces.SynchronizationTimetableDao
import ua.com.merchik.merchik.database.room.RoomManager


class DataSyncRepository {

    private val _syncStatusFlow = MutableSharedFlow<SyncStatus>(replay = 1)
    val syncStatusFlow: SharedFlow<SyncStatus> = _syncStatusFlow

    private val syncTimetableDao: SynchronizationTimetableDao =
        RoomManager.SQL_DB.synchronizationTimetableDao()

    private val strategies: List<TableSyncStrategy> = listOf(
        WpDataSyncStrategy()
//        PlanogramAddressSync(),
//        PlanogramGroupSync(),
//        PlanogramImagesSync(),
//        PlanogramTypeSync(),
//        PlanogramVisitShowcaseSync()
        // другие
    )

    suspend fun syncAllIfNeeded(): Boolean {
        for (strategy in strategies) {
            val info = syncTimetableDao.getByTableName(strategy.tableName) ?: continue

            if (!isTimeToSync(info)) continue

            _syncStatusFlow.emit(SyncStatus.Syncing(strategy.tableName))

            val result = strategy.sync(info.lastDownloadTime)
            val updated = info.copy(
                lastDownloadTime = if (result.success) {
                    TrustedTime.syncWatermarkSec(info.lastDownloadTime, 120)
                } else {
                    info.lastDownloadTime
                },
                lastDownloadStatus = if (result.success) DownloadStatus.SUCCESS else DownloadStatus.ERROR,
                downloadedItems = result.downloadedItems
            )
            syncTimetableDao.update(updated)

            if (!result.success) {
                _syncStatusFlow.emit(SyncStatus.Failure(strategy.tableName))
                return false
            } else {
                _syncStatusFlow.emit(SyncStatus.Success(strategy.tableName))
            }
        }

        _syncStatusFlow.emit(SyncStatus.Done)
        return true
    }

    private fun isTimeToSync(info: SynchronizationTimeTable): Boolean {
        return TrustedTime.isSyncDue(info.lastDownloadTime, info.syncPeriodSeconds)
    }
}
