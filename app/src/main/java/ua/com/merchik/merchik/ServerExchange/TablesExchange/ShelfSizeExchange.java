package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/**
 * 18.01.23.
 * Таблица БД. (загрузка, очевидно же)
 * Длин Полочного пространства.
 * */
public class ShelfSizeExchange {

    public void downloadShelfSize() {
        downloadShelfSizeData(new Click() {
            @Override
            public <T> void onSuccess(T data) {
                saveShelfSize((JsonObject) data);
            }

            @Override
            public void onFailure(String error) {
                Log.e("ShelfSizeExchange", "Error data: " + error);
            }
        });
    }

    /**
     * 18.01.23.
     * Тут происходит сохранение Длин Полочного пространства
     * */
    private void saveShelfSize(JsonObject data) {
        Log.e("ShelfSizeExchange", "data: " + data);
    }

    /**
     * 18.01.23.
     * Загружаю таблицы Длин Полочного пространства.
     *
     * mod=data_list
     * act=shelf_size
     *
     * фильтры:
     * client_id - клиент
     * addr_id - адрес
     *
     * клиент и адрес могут быть как числом, так и массивом чисел, если вообще будет в них
     * необходимость
     * dt_from / dt_to- дата на которую актуальна длина в формате YYYY-MM-DD
     * это не ВПИ если что
     */
    private void downloadShelfSizeData(Click click) {
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "shelf_size";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("INFO", "downloadShelfSizeData", "convertedObject: " + convertedObject);

        Log.e("ShelfSizeExchange", "convertedObject: " + convertedObject);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                click.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                click.onFailure(t.getMessage());
            }
        });
    }
}
