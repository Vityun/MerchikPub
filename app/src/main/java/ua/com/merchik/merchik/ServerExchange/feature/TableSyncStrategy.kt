package ua.com.merchik.merchik.ServerExchange.feature


import ua.com.merchik.merchik.data.synchronization.TableName



interface TableSyncStrategy {
    val tableName: TableName
    suspend fun sync(lastSyncTime: Long): SyncResult
}
