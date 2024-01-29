package ua.com.merchik.merchik.ServerExchange.TablesExchange;

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
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ShelfSizeResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/**
 * 18.01.23.
 * Таблица БД. (загрузка, очевидно же)
 * Длин Полочного пространства.
 */
public class ShelfSizeExchange {

    public void downloadShelfSize() {
        downloadShelfSizeData(new Click() {
            @Override
            public <T> void onSuccess(T data) {
                saveShelfSize((ShelfSizeResponse) data);
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
     *
     * @param data
     */
    private void saveShelfSize(ShelfSizeResponse data) {
        Log.e("ShelfSizeExchange", "data: " + data);


        Log.e("ShelfSizeExchange", "data: " + new Gson().toJson(data));
        try {
            Log.e("ShelfSizeExchange", "dataS: " + data.list.size());


            if (data != null && data.state && data.list != null && data.list.size() > 0) {
                Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/ShelfSizeExchange/onSuccess", " data.list: " + data.list);

                SQL_DB.shelfSizeDao().insertAll(data.list)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Log.e("ShelfSizeExchange", "onComplete OK");
                                Globals.writeToMLOG("OK", "ShelfSizeExchange/onResponse/onComplete", "OK");
                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                Log.e("ShelfSizeExchange", "onError e: " + e);
                                Globals.writeToMLOG("ERROR", "ShelfSizeExchange/onResponse/onError", "Throwable e: " + e);
                            }
                        });
            }

        } catch (Exception e) {
            Log.e("ShelfSizeExchange", "Exception e: " + e);
        }
    }

    /**
     * 18.01.23.
     * Загружаю таблицы Длин Полочного пространства.
     * <p>
     * mod=data_list
     * act=shelf_size
     * <p>
     * фильтры:
     * client_id - клиент
     * addr_id - адрес
     * <p>
     * клиент и адрес могут быть как числом, так и массивом чисел, если вообще будет в них
     * необходимость
     * dt_from / dt_to- дата на которую актуальна длина в формате YYYY-MM-DD
     * это не ВПИ если что
     */
    private void downloadShelfSizeData(Click click) {
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "shelf_size";

        data.dt_from = Clock.getHumanTimeSecPattern(Clock.getDatePeriodLong(System.currentTimeMillis(), -20) / 1000, "yyyy-MM-dd");
        data.dt_to = Clock.getHumanTimeSecPattern(Clock.getDatePeriodLong(System.currentTimeMillis(), 1) / 1000, "yyyy-MM-dd");

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("INFO", "downloadShelfSizeData", "convertedObject: " + convertedObject);

        retrofit2.Call<ShelfSizeResponse> call = RetrofitBuilder.getRetrofitInterface().ShelfSize_DOWNLOAD_TABLE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<ShelfSizeResponse>() {
            @Override
            public void onResponse(Call<ShelfSizeResponse> call, Response<ShelfSizeResponse> response) {
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
            public void onFailure(Call<ShelfSizeResponse> call, Throwable t) {
                click.onFailure(t.getMessage());
            }
        });
    }
}
