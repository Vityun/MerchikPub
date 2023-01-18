package ua.com.merchik.merchik.ServerExchange.Constants;

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
 * Loading and saving Reclamation constants from the server.
 */
public class ReclamationPercentageExchange {


    public void downloadAndSaveReclamationPercentage() {
        downloadReclamationPercentageData(new Click() {
            @Override
            public <T> void onSuccess(T data) {
                saveReclamationPercentage((JsonObject) data);
            }

            @Override
            public void onFailure(String error) {
                Log.e("ReclamationPercentage", "Error data: " + error);
            }
        });
    }


    /**
     * 18.01.23.
     * Тут происходит сохранение констант рекламаций в памяти телефона
     * */
    private void saveReclamationPercentage(JsonObject data) {
        Log.e("ReclamationPercentage", "data: " + data);
    }


    /**
     * 18.01.23.
     * Загружаю данные о проценте рекламаций.
     *
     * mod=data_list
     * act=reclamation_percent_avg
     *
     * фильтры:
     * tp - тип (вроде их должно быть 2, один для Киева, другой для областей)
     * dt_change_from
     * dt_change_to
     */
    private void downloadReclamationPercentageData(Click click) {
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "reclamation_percent_avg";

//        data.tp = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("INFO", "downloadReclamationPercentageData", "convertedObject: " + convertedObject);

        Log.e("ReclamationPercentage", "convertedObject: " + convertedObject);

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
