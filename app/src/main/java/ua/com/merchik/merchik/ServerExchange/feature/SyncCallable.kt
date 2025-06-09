package ua.com.merchik.merchik.ServerExchange.feature

interface SyncCallable {
    // Основные методы
    fun onSuccess(dataSize: Int)
    fun onFailure(error: String)

}