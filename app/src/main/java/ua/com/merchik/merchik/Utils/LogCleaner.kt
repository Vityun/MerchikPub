package ua.com.merchik.merchik.Utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogCleaner {

    private const val LOG_FILE_NAME = "M_LOG.txt"
    private const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS" // Формат времени в логах

    /**
     * Асинхронная очистка старых логов
     */
    suspend fun cleanOldLogs(cacheDir: File) = withContext(Dispatchers.IO) {
        val logFile = File(cacheDir, LOG_FILE_NAME)
        if (!logFile.exists()) return@withContext

        val dateFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
        val daysAgo = Date(System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000) // 2 дня назад
//        val daysAgo = Date(System.currentTimeMillis() - 2L * 60 * 60 * 1000) // тест 2 часа

        val tempFile = File(cacheDir, "TEMP_LOG.txt")

        var foundFirstLog = false

        logFile.bufferedReader().use { reader ->
            tempFile.bufferedWriter().use { writer ->
                reader.lineSequence().forEach { line ->
                    val timestamp = extractTimestamp(line)

                    if (!foundFirstLog) {
                        if (timestamp != null) {
                            val logDate = try {
                                dateFormat.parse(timestamp)
                            } catch (e: Exception) {
                                null
                            }

                            if (logDate == null || logDate.after(daysAgo)) {
                                // Первая подходящая строка — с неё начинаем писать
                                foundFirstLog = true
                                writer.write(line)
                                writer.newLine()
                            }
                        }
                        // До первой подходящей строки — пропускаем
                        return@forEach
                    } else {
                        // После первой логовой строки — пишем как раньше
                        writer.write(line)
                        writer.newLine()
                    }
                }
            }
        }

        // Заменяем оригинальный файл новым
        if (logFile.delete()) {
            tempFile.renameTo(logFile)
        }
    }


//    suspend fun cleanOldLogs(cacheDir: File) = withContext(Dispatchers.IO) {
//        val logFile = File(cacheDir, LOG_FILE_NAME)
//        if (!logFile.exists()) return@withContext
//
//        val dateFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
//        val daysAgo = Date(System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000) // 2 дня назад
//        val daysAgo = Date(System.currentTimeMillis() - 2L * 60 * 60 * 1000) // тест 2 часа

//        val tempFile = File(cacheDir, "TEMP_LOG.txt") // Временный файл для записи актуальных логов
//
//        logFile.bufferedReader().use { reader ->
//            tempFile.bufferedWriter().use { writer ->
//                reader.lineSequence().forEach { line ->
//                    val timestamp = extractTimestamp(line)
//                    if (timestamp != null) {
//                        val logDate = try {
//                            dateFormat.parse(timestamp)
//                        } catch (e: Exception) {
//                            null
//                        }
//
//                        // Оставляем записи с датой >= чем 2 дня назад или записи без даты
//                        if (logDate == null || logDate.after(daysAgo)) {
//                            writer.write(line)
//                            writer.newLine()
//                        }
//                    } else {
//                        // Запись без времени — оставляем
//                        writer.write(line)
//                        writer.newLine()
//                    }
//                }
//            }
//        }
//
//        // Заменяем исходный файл временным
//        if (logFile.delete()) {
//            tempFile.renameTo(logFile)
//        }
//    }

    /**
     * Извлечение временной метки из строки
     */
    private fun extractTimestamp(logLine: String): String? {
        // Логи могут быть в формате "2025-01-23 12:19:59:543" или "12:19:59:543_INFO"
        val parts = logLine.split(" ")
        if (parts.isNotEmpty()) {
            // Если лог начинается с даты
            if (parts[0].matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                return "${parts[0]} ${parts.getOrNull(1)}"
            }
        }

        // Если лог начинается с времени (например, через "_")
        if (logLine.matches(Regex("\\d{2}:\\d{2}:\\d{2}:\\d{3}_.*"))) {
            return logLine.split("_")[0]
        }

        return null // Время не найдено
    }
}
