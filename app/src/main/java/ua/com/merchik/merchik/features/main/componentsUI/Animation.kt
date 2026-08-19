package ua.com.merchik.merchik.features.main.componentsUI

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.ServerExchange.workmager.DownloadImagesWorker
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgList
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.realm.tables.TovarRealm
import ua.com.merchik.merchik.retrofit.RetrofitBuilder
import java.io.IOException


interface PhotoListCallback {

    fun onSuccess(list: List<TovarImgList>)

    fun onError(error: Throwable)
}

object PhotoManager {

    suspend fun getPhotoListToDownload(): List<TovarImgList> {
        val tovarIdsList = TovarRealm.getTovarIdsInBatches()
        Log.e("test_photo_oo", "tovarIdsList Количество: " + tovarIdsList.size)

//        val tovarsPhotoToDownload =
//            StackPhotoRealm.findTovarIds(tovarIdsList)

//        Log.e("test_photo_oo", "tovarsPhotoToDownload Количество: " + tovarsPhotoToDownload.size)

//        return getTovarPhotoInfoFromServer(tovarIdsList)
        return getTovarPhotoInfoFromServer(emptyList())
    }

    @JvmStatic
    fun getPhotoListToDownloadAsync(
        callback: PhotoListCallback
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = getPhotoListToDownload()

                withContext(Dispatchers.Main) {
                    callback.onSuccess(result)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError(e)
                }
            }
        }
    }
}




suspend fun getTovarPhotoInfoFromServer(tovarIds: List<Int>): List<TovarImgList> {
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

            Log.e("test_photo_oo", "convertedObject: $convertedObject")

            // Выполняем запрос
            val response = RetrofitBuilder.getRetrofitInterface()
                .GET_TOVAR_PHOTO_INFO_JSON(RetrofitBuilder.contentType, convertedObject)
                .execute()

            if (response.isSuccessful && response.body() != null) {
                // Возвращаем список фотографий

                Log.e("test_photo_oo", "response.body() Количество: " + response.body()!!.list.size)
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
