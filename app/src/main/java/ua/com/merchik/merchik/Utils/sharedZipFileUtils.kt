package ua.com.merchik.merchik.Utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream



suspend fun compressFile(inputFilePath: String, outputFilePath: String): File = withContext(Dispatchers.IO) {
    val inputFile = File(inputFilePath)
    val outputFile = File(outputFilePath)

    FileInputStream(inputFile).use { input ->
        GZIPOutputStream(FileOutputStream(outputFile)).use { gzipOut ->
            input.copyTo(gzipOut)
        }
    }

    println("Сжатие завершено: $outputFilePath")
    return@withContext outputFile
}


fun createMultipart(file: File, fieldName: String = "file"): MultipartBody.Part {
    val requestBody = RequestBody.create("application/gzip".toMediaTypeOrNull(), file)
    return MultipartBody.Part.createFormData(fieldName, file.name, requestBody)
}