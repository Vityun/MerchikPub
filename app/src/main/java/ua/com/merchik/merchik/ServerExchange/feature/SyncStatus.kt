package ua.com.merchik.merchik.ServerExchange.feature

import ua.com.merchik.merchik.data.synchronization.TableName



sealed class SyncStatus {
    data class Syncing(val table: TableName) : SyncStatus()
    data class Success(val table: TableName) : SyncStatus()
    data class Failure(val table: TableName) : SyncStatus()
    data object Done : SyncStatus()
}
