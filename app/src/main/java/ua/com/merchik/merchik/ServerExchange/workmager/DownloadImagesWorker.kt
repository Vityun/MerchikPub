package ua.com.merchik.merchik.ServerExchange.workmager

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgList
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.realm.tables.TovarRealm
import ua.com.merchik.merchik.retrofit.RetrofitBuilder
import java.io.IOException

class DownloadImagesWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Получаем список изображений для загрузки
            val photoList = getPhotoListToDownload()
            Log.d("DownloadImagesWorker", "Starting download of ${photoList.size} photos")

            // Загружаем изображения
            var successfulDownloads = 0
            for (photo in photoList) {
                val success = downloadPhoto(photo)
                if (success) {
                    successfulDownloads++
                }
            }

            Log.d(
                "DownloadImagesWorker",
                "Finished downloading $successfulDownloads/${photoList.size} photos"
            )
            Result.success() // Успешное завершение задачи
        } catch (e: Exception) {
            Log.e("DownloadImagesWorker", "Error in doWork", e)
            Result.failure() // Ошибка
        }
    }


    private suspend fun getPhotoListToDownload(): List<TovarImgList> {
        // 1. Получаем ID товаров пачками
        val tovarIdsList = TovarRealm.getTovarIdsInBatches()

        // 2. Находим ID товаров, которых нет в StackPhotoDB
        val tovarsPhotoToDownload = StackPhotoRealm.findTovarIds(tovarIdsList)

        // 3. Формируем запрос и получаем список фотографий
        return getTovarPhotoInfoFromServer(tovarsPhotoToDownload)
    }

    private val downloadSemaphore = Semaphore(10) // Ограничиваем до 10 одновременных запросов

    private suspend fun downloadPhoto(photo: TovarImgList): Boolean {
        return withContext(Dispatchers.IO) {
            downloadSemaphore.acquire() // Ограничиваем количество одновременных запросов
            try {
                // Загружаем изображение по URL
                val response: Response<ResponseBody> = RetrofitBuilder.getRetrofitInterface()
                    .DOWNLOAD_PHOTO_BY_URL_WORKER(photo.photoUrl)
                    .execute()

                if (response.isSuccessful && response.body() != null) {
                    // Сохраняем фото и информацию в базу данных
                    savePhotoToDatabase(photo, response.body()!!)
                    true // Успешная загрузка
                } else {
                    // Логируем ошибку, но продолжаем загрузку следующих изображений
                    Log.e("DownloadImagesWorker", "Failed to download photo: ${photo.photoUrl}")
                    false
                }
            } catch (e: IOException) {
                // Логируем ошибку сети, но продолжаем загрузку следующих изображений
                Log.e(
                    "DownloadImagesWorker",
                    "Network error while downloading photo: ${photo.photoUrl}",
                    e
                )
                false
            } finally {
                downloadSemaphore.release() // Освобождаем семафор
            }
        }
    }

    private suspend fun savePhotoToDatabase(photo: TovarImgList, responseBody: ResponseBody) {
        return withContext(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            try {
                // Декодируем Bitmap из ResponseBody
                val bitmap = BitmapFactory.decodeStream(responseBody.byteStream())

                // Сохраняем изображение на устройство
                val path = Globals.saveImage1(bitmap, "TOVAR_${photo.tovarId}_SID${photo.id}")

                // Сохраняем информацию о фото в базу данных
                realm.executeTransactionAsync { bgRealm ->
                    try {
                        // Генерация нового ID
                        val lastId = bgRealm.where(StackPhotoDB::class.java)
                            .sort("id", Sort.DESCENDING)
                            .findFirst()
                            ?.id ?: 0
                        val newId = lastId + 1

                        // Создание объекта StackPhotoDB
                        val stackPhotoDB = StackPhotoDB().apply {
                            id = newId
                            photoServerId = photo.id
                            object_id = photo.tovarId.toInt()
                            addr_id = photo.addrId.toInt()
                            approve = photo.approve.toInt()
                            dvi = photo.dvi?.toInt() ?: 0
                            code_iza = photo.codeIZA
                            vpi = 0
                            create_time = photo.dt.toLong() * 1000
                            upload_to_server = 0
                            get_on_server = 0
                            photo_num = path
                            photo_hash = photo.hash
                            photo_type = photo.photoTp.toInt()
                            comment = "small"
                            upload_time = 0
                            upload_status = 0
                            status = false
                        }

                        // Сохранение в Realm
                        bgRealm.copyToRealmOrUpdate(stackPhotoDB)
                        Log.d("DownloadImagesWorker", "Photo saved: ID=$newId, Path=$path")
                    } catch (e: Exception) {
                        Log.e("DownloadImagesWorker", "Error saving photo to database", e)
                    }
                }
                bitmap.recycle() // Освобождаем память
            } catch (e: Exception) {
                Log.e("DownloadImagesWorker", "Error decoding or saving photo", e)
            } finally {
                realm.close() // Закрываем Realm
            }

        }
    }

    private suspend fun getTovarPhotoInfoFromServer(tovarIds: List<Int>): List<TovarImgList> {
        return withContext(Dispatchers.IO) {
            try {
                // Формируем запрос
                val data = StandartData<Any?>().apply {
                    mod = "images_view"
                    act = "list_image"
                    nolimit = "1"
                    image_type = "small"
                    photo_tovar_id = tovarIds
                }

                // Формирование тела запроса
                val gson = Gson()
                val json = gson.toJson(data)
                val convertedObject = Gson().fromJson(json, JsonObject::class.java)

                // Выполняем запрос
                val response = RetrofitBuilder.getRetrofitInterface()
                    .GET_TOVAR_PHOTO_INFO_JSON(RetrofitBuilder.contentType, convertedObject)
                    .execute()

                if (response.isSuccessful && response.body() != null) {
                    // Возвращаем список фотографий
                    response.body()!!.list ?: emptyList()
                } else {
                    // Логируем ошибку и возвращаем пустой список
                    Log.e("API_ERROR", "Response not successful: ${response.message()}")
                    emptyList()
                }
            } catch (e: IOException) {
                // Логируем ошибку сети и возвращаем пустой список
                Log.e("API_ERROR", "Network error: ${e.message}")
                emptyList()
            }
        }
    }

}