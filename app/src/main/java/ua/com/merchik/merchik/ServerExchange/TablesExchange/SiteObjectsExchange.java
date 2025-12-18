package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SiteObjectsResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/**
 * 05.05.2021
 * Получение Обьектов Сайта. Обьекты сайта - текстовые значения разнобразных элементов. Они
 * существуют(в данном приложении, на данный момент) для того что б реализовать мультиязычность.
 */
public class SiteObjectsExchange {

    public void downloadSiteObjects(Exchange.ExchangeInt exchangeInterface) {
        try {
            Globals.writeToMLOG("INFO", "SiteObjectsExchange/downloadSiteObjects/", " +");

            StandartData data = new StandartData();
            data.mod = "site_objects";
            data.act = "list";
            data.lang_id = String.valueOf(Globals.langId);

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            Call<SiteObjectsResponse> call = RetrofitBuilder.getRetrofitInterface().GET_SITE_OBJECTS_R(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<SiteObjectsResponse>() {
                @Override
                public void onResponse(Call<SiteObjectsResponse> call, Response<SiteObjectsResponse> response) {
                    try {
                        // todo ADD M_LOG
                        Log.e("SiteObjectsExchange", "3");
//                        Globals.writeToMLOG("INFO", "SiteObjectsExchange/downloadSiteObjects/Response", " response: " + (response.body() != null ? response.body().objectSQLList : "null"));
                        if (response.body().state) {
                            if (response.body().error != null) {
                                // todo ADD M_LOG
                                Log.e("SiteObjectsExchange", "4");

                                exchangeInterface.onFailure("Замечание с сайта: " + response.body().error);
                                Globals.writeToMLOG("INFO", "SiteObjectsExchange/downloadSiteObjects/Response_error", " response: " + response.body().error);

                            } else {
                                // Нормальный функционал
                                if (response.body().objectSQLList != null) {
                                    try {
                                        SQL_DB.siteObjectsDao().insertAll(response.body().objectSQLList)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new DisposableCompletableObserver() {
                                                    @Override
                                                    public void onComplete() {
                                                        exchangeInterface.onSuccess("Загрузило: " + response.body().objectSQLList.size() + " ОбьектовСайта.");

                                                    }

                                                    @Override
                                                    public void onError(@NonNull Throwable e) {
                                                        Globals.writeToMLOG("INFO", "SiteObjectsExchange/downloadSiteObjects/Response_error", ".Throwable: " + e.getMessage());
                                                    }
                                                });
                                        Log.e("SiteObjectsExchange", "Ok");
                                        Globals.writeToMLOG("INFO", "SiteObjectsExchange/downloadSiteObjects/Response_error", " response: " + "Загрузило: " + response.body().objectSQLList.size() + " ОбьектовСайта.");
                                    } catch (Exception e) {
                                        Log.e("SiteObjectsExchange", "ERR");
                                        exchangeInterface.onFailure("Запись в БД: " + e);
                                    }
                                } else {
                                    // todo ADD M_LOG
                                    Log.e("SiteObjectsExchange", "5");

                                    exchangeInterface.onFailure("Список ОбектовСайта пуст.");
                                }
                            }
                        } else {
                            // todo ADD M_LOG
                            Log.e("SiteObjectsExchange", "6");

                            exchangeInterface.onFailure("State на получение ОбьектовСайта - отрицательный.");
                        }

                    } catch (Exception e) {
                        // todo ADD M_LOG
                        Log.e("SiteObjectsExchange", "7");

                        exchangeInterface.onFailure("onResponse.Exception.: " + e.toString());
                    }
                }

                @Override
                public void onFailure(Call<SiteObjectsResponse> call, Throwable t) {
                    // todo ADD M_LOG
                    Log.e("SiteObjectsExchange", "8");

                    exchangeInterface.onFailure("onFailure.Throwable: " + t.toString());
                }
            });

        } catch (Exception e) {
            // todo ADD M_LOG
            Log.e("SiteObjectsExchange", "9");

            exchangeInterface.onFailure("Загрузка ОбьектовСайта.Exception: " + e.toString());
        }
    }


}
