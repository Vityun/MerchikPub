package ua.com.merchik.merchik.ServerExchange.workmager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgList
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.realm.tables.TovarRealm
import ua.com.merchik.merchik.database.room.RoomManager.SQL_DB
import ua.com.merchik.merchik.retrofit.RetrofitBuilder
import java.io.IOException

class DownloadImagesWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {

            // 1. Дозагрузка отсутствующих фотографий
            val thumbnailsToDownload = getMissingThumbnails()
            setProgress(workDataOf("status" to "start"))
            Log.d(
                "DownloadImagesWorker",
                "Starting thumbnail download: ${thumbnailsToDownload.size} items"
            )

            var successfulThumbnails = 0
            thumbnailsToDownload.forEach { stackPhoto ->
                if (downloadAndUpdateThumbnail(stackPhoto)) successfulThumbnails++
            }

            // 2.Получаем список изображений для загрузки
            val photoList = getPhotoListToDownload()
            Log.d("DownloadImagesWorker", "Starting download of ${photoList.size} photos")

            // Загружаем изображения
            var successfulDownloads = 0
            photoList.forEach { photo ->
                if (downloadPhoto(photo)) successfulDownloads++
            }

            setProgress(workDataOf("status" to "end"))
            delay(1000)
            Result.success() // Успешное завершение задачи
        } catch (e: Exception) {
            Log.e("DownloadImagesWorker", "Error in doWork", e)
            Result.failure() // Ошибка
        } finally {
            delay(1000)
            setProgress(workDataOf("status" to "end"))
//            setProgress(workDataOf("status" to WorkInfo.State.SUCCEEDED))
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

    // Новый метод для получения списка всех отсутствующих фотографий за 7 дней
    private suspend fun getMissingThumbnails(): List<StackPhotoDB> {
        return withContext(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            val sevenDaysAgo = System.currentTimeMillis() / 1000 - 7 * 24 * 60 * 60
            try {

                // 1. Получаем данные Realm
                val realmPhotos = realm.copyFromRealm(
                    realm.where(StackPhotoDB::class.java)
                        .isNotNull("photoServerURL")
                        .isNull("photo_num")
                        .greaterThanOrEqualTo("dt", sevenDaysAgo)
                        .findAll()
                )

                // 2. Получаем хеши из Room в отдельной транзакции
                val hashes = withContext(Dispatchers.IO) {
                    SQL_DB.achievementsDao().getAll()
                        .flatMap { listOfNotNull(it.img_before_hash, it.img_after_hash) }
                        .toSet()
                }

                // 3. Получаем Realm-фото по хешам в основном потоке Realm
                val achievementPhotos = realm.use { r ->
                    r.where(StackPhotoDB::class.java)
                        .`in`("photo_hash", hashes.toTypedArray())
                        .findAll()
                        .let { r.copyFromRealm(it) }
                }

                // 4. Объединяем и удаляем дубликаты
                (realmPhotos + achievementPhotos)
                    .distinctBy { it.getPhotoServerId() } // или другой уникальный идентификатор
                    .sortedByDescending { it.dt }

            } catch (e: Exception) {
                Log.e("DownloadImagesWorker", "Error getting thumbnails", e)
                emptyList()
            } finally {
                realm.close()
            }
        }
    }


    private val downloadSemaphore = Semaphore(8) // Ограничиваем до 8 одновременных запросов

    private suspend fun downloadPhoto(photo: TovarImgList): Boolean {
        return withContext(Dispatchers.IO) {
            downloadSemaphore.acquire() // Ограничиваем количество одновременных запросов
            try {
                // Загружаем изображение по URL
                val response: Response<ResponseBody> = RetrofitBuilder.getRetrofitInterfaceForImage()
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

//                val newId = realm.where(StackPhotoDB::class.java)
//                    .max("id")?.toInt()?.plus(1) ?: 1

                // Сохраняем информацию о фото в базу данных
                realm.executeTransaction { bgRealm ->
                    try {
                        // Генерация нового ID
//                        val newId = PrimaryKeyGenerator.nextId(bgRealm, StackPhotoDB::class.java)
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

    // Метод для загрузки и обновления thumbnail'ов
    private suspend fun downloadAndUpdateThumbnail(stackPhoto: StackPhotoDB): Boolean {
        return withContext(Dispatchers.IO) {
            downloadSemaphore.acquire()
            try {
                val originalUrl =
                    stackPhoto.photoServerURL?.replace("thumb_", "") ?: return@withContext false

                val response = RetrofitBuilder.getRetrofitInterfaceForImage()
                    .DOWNLOAD_PHOTO_BY_URL(originalUrl)
                    .execute()

                if (response.isSuccessful && response.body() != null) {
                    saveThumbnailToDatabase(stackPhoto, response.body()!!)
                    true
                } else {
                    false
                }
            } catch (e: IOException) {
                Log.e("DownloadImagesWorker", "Network error for thumbnail", e)
                false
            } finally {
                downloadSemaphore.release()
            }
        }
    }

    // Сохранение thumbnail'а
    private suspend fun saveThumbnailToDatabase(
        stackPhoto: StackPhotoDB,
        responseBody: ResponseBody
    ) {
        withContext(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            var bitmap: Bitmap? = null
            try {
                bitmap = BitmapFactory.decodeStream(responseBody.byteStream())

                val path =
                    Globals.savePhotoToPhoneMemory("/Manager", stackPhoto.photoServerId, bitmap);
//                    .saveImage1(bitmap, "THUMB_${stackPhoto.photoServerId}")

                // Получаем ID из объекта stackPhoto
//                val photoId = stackPhoto.photoServerId ?: run {
//                    Log.e("DownloadImagesWorker", "PhotoServerId is null")
//                    return@withContext
//                }


                realm.executeTransaction { r ->
                    stackPhoto.apply {
                        photo_num = path
                    }
                    r.insertOrUpdate(stackPhoto)
//                    val dbPhoto = r.where(StackPhotoDB::class.java)
//                        .equalTo("photoServerId", photoId)
//                        .findFirst()
//                        ?: return@executeTransaction
//
//                    dbPhoto.photo_num = path
//                    Log.d("DownloadImagesWorker", "Updated thumbnail: ${dbPhoto.id}")
                }
            } catch (e: Exception) {
                Log.e("DownloadImagesWorker", "Error saving thumbnail", e)
            } finally {
                realm.close()
                bitmap?.recycle()
            }
        }
    }

}