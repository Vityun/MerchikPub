package ua.com.merchik.merchik.ServerExchange.Constants;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ReclamationPercentage.ReclamationPercentageResponse;
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
                saveReclamationPercentage((ReclamationPercentageResponse) data);
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
     *
     * @param data
     */
    private void saveReclamationPercentage(ReclamationPercentageResponse data) {

        Log.e("ReclamationPercentage", "data: " + new Gson().toJson(data));
        Log.e("ReclamationPercentage", "dataS: " + data.list.size());

        if (data != null && data.state && data.list != null && data.list.size() > 0) {
            SQL_DB.reclamationPercentageDao().insertAll(data.list)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {
                            Log.e("ReclamationPercentage", "onComplete OK");
                            Globals.writeToMLOG("OK", "ReclamationPercentage/onResponse/onComplete", "OK");
                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                            Log.e("ReclamationPercentage", "onError e: " + e);
                            Globals.writeToMLOG("ERROR", "ReclamationPercentage/onResponse/onError", "Throwable e: " + e);
                        }
                    });
        }
    }


    /**
     * 18.01.23.
     * Загружаю данные о проценте рекламаций.
     * <p>
     * mod=data_list
     * act=reclamation_percent_avg
     * <p>
     * фильтры:
     * tp - тип (вроде их должно быть 2, один для Киева, другой для областей)
     * dt_change_from
     * dt_change_to
     */
    private void downloadReclamationPercentageData(Click click) {
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "reclamation_percent_avg";

//        data.dt_change_from = Clock.getHumanTimeSecPattern(Clock.getDatePeriodLong(System.currentTimeMillis(), -60) / 1000, "yyyy-MM-dd");
//        data.dt_change_to = Clock.getHumanTimeSecPattern(Clock.getDatePeriodLong(System.currentTimeMillis(), 1) / 1000, "yyyy-MM-dd");
        data.dt_change_from = String.valueOf(Clock.getDatePeriodLong(System.currentTimeMillis(), -60) / 1000);
        data.dt_change_to = String.valueOf(Clock.getDatePeriodLong(System.currentTimeMillis(), 1) / 1000);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("INFO", "downloadReclamationPercentageData", "convertedObject: " + convertedObject);

        Log.e("ReclamationPercentage", "convertedObject: " + convertedObject);

        retrofit2.Call<ReclamationPercentageResponse> call = RetrofitBuilder.getRetrofitInterface().ReclamationPercentage_DOWNLOAD_TABLE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<ReclamationPercentageResponse>() {
            @Override
            public void onResponse(Call<ReclamationPercentageResponse> call, Response<ReclamationPercentageResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        click.onSuccess(response.body());
                    } else {
                        click.onFailure("Что-то пошло не по плану!: " + response);
                    }
                } else {
                    click.onFailure("Что-то пошло не по плану: " + response);
                }

            }

            @Override
            public void onFailure(Call<ReclamationPercentageResponse> call, Throwable t) {
                click.onFailure(t.getMessage());
            }
        });
    }
}
