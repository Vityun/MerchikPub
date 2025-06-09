package ua.com.merchik.merchik.ServerExchange.feature.strategy

import kotlinx.coroutines.suspendCancellableCoroutine
import ua.com.merchik.merchik.ServerExchange.TablesExchange.PlanogrammTableExchange
import ua.com.merchik.merchik.ServerExchange.feature.SyncCallable
import ua.com.merchik.merchik.ServerExchange.feature.SyncResult
import ua.com.merchik.merchik.ServerExchange.feature.TableSyncStrategy
import ua.com.merchik.merchik.ViewHolders.Clicks.clickObjectAndStatus
import ua.com.merchik.merchik.data.synchronization.TableName
import kotlin.coroutines.resume


abstract class PlanogrammSyncStrategy(
    override val tableName: TableName
) : TableSyncStrategy {

    override suspend fun sync(lastDownloadTime: Long): SyncResult {
        return suspendCancellableCoroutine { continuation ->
            val planogramm = PlanogrammTableExchange()

            getSyncFunction(planogramm).invoke(object : SyncCallable {
                override fun onSuccess(data: Int) {
                    continuation.resume(SyncResult(true, data as? Int ?: 0))
                }
                override fun onFailure(error: String) {
                    continuation.resume(SyncResult(false, 0))
                }
            })
        }
    }
    abstract fun getSyncFunction(planogramm: PlanogrammTableExchange): (SyncCallable) -> Unit


}
