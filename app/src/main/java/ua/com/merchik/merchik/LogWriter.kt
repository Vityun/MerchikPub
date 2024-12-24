package ua.com.merchik.merchik

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Activities.MyApplication
import java.io.File
import java.io.IOException

object LogWriter {
    private val logQueue = ArrayDeque<String>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val logFile = File(MyApplication.getAppContext().cacheDir, "M_LOG.txt")
    private val maxQueueSize = 1000 // Максимальное количество записей в очереди перед сбросом на диск.

    init {
        scope.launch {
            processLogs()
        }
    }

    fun writeToLog(type: String, place: String, msg: String) {
        val time = Clock.getHumanTime()
        val logEntry = "$time $type $place $msg\n"

        synchronized(logQueue) {
            logQueue.addLast(logEntry)
            if (logQueue.size >= maxQueueSize) {
                scope.launch { flushLogs() }
            }
        }
    }

    private suspend fun processLogs() {
        while (true) {
            flushLogs()
            delay(5000) // Периодический сброс каждые 5 секунд.
        }
    }

    private suspend fun flushLogs() {
        val logsToWrite: List<String>
        synchronized(logQueue) {
            if (logQueue.isEmpty()) return
            logsToWrite = ArrayList(logQueue)
            logQueue.clear()
        }

        withContext(Dispatchers.IO) {
            try {
                logFile.parentFile?.mkdirs() // Убедиться, что папка существует.
                logFile.appendText(logsToWrite.joinToString(separator = ""))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
