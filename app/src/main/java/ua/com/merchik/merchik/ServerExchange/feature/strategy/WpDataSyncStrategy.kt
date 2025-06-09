package ua.com.merchik.merchik.ServerExchange.feature.strategy

import ua.com.merchik.merchik.ServerExchange.download.MainExchange
import ua.com.merchik.merchik.ServerExchange.feature.SyncCallable
import ua.com.merchik.merchik.ServerExchange.feature.SyncResult
import ua.com.merchik.merchik.ServerExchange.feature.TableSyncStrategy
import ua.com.merchik.merchik.data.synchronization.TableName
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class WpDataSyncStrategy : TableSyncStrategy {
    override val tableName = TableName.WP_DATA

    override suspend fun sync(lastSyncTime: Long): SyncResult {
        return suspendCoroutine { continuation ->
            MainExchange().downloadWPData(object : SyncCallable {
                override fun onSuccess(data: Int) {
                    continuation.resume(SyncResult(true, data as? Int ?: 0))
                }

                override fun onFailure(error: String) {
                    continuation.resume(SyncResult(false, 0))
                }
            }, lastSyncTime)
        }
    }
}
