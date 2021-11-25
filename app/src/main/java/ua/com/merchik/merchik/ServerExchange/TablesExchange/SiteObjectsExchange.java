package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.SiteObjectsResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

/**
 * 05.05.2021
 * Получение Обьектов Сайта. Обьекты сайта - текстовые значения разнобразных элементов. Они
 * существуют(в данном приложении, на данный момент) для того что б реализовать мультиязычность.
 */
public class SiteObjectsExchange {

    public void downloadSiteObjects(Exchange.ExchangeInt exchangeInterface) {
        try {
            // todo ADD M_LOG
            Log.e("SiteObjectsExchange", "2");

            StandartData data = new StandartData();
            data.mod = "site_objects";
            data.act = "list";
            data.lang_id = String.valueOf(Globals.langId);

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<SiteObjectsResponse> call = RetrofitBuilder.getRetrofitInterface().GET_SITE_OBJECTS_R(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<SiteObjectsResponse>() {
                @Override
                public void onResponse(retrofit2.Call<SiteObjectsResponse> call, retrofit2.Response<SiteObjectsResponse> response) {
                    try {
                        // todo ADD M_LOG
                        Log.e("SiteObjectsExchange", "3");

                        //--------------------------------------------------------------------------
                        SiteObjectsResponse MAKE_JSON = new SiteObjectsResponse();
                        MAKE_JSON = response.body();

                        List<SiteObjectsSDB> dataList = MAKE_JSON.objectSQLList;




                        //--------------------------------------------------------------------------


                        if (response.body().state) {
                            if (response.body().error != null) {
                                // todo ADD M_LOG
                                Log.e("SiteObjectsExchange", "4");

                                exchangeInterface.onFailure("Замечание с сайта: " + response.body().error);
                            } else {
                                // Нормальный функционал
                                if (response.body().objectSQLList != null) {
                                    try {
                                        //==========================================================
                                        int i = 0;
                                        for (SiteObjectsSDB item : dataList) {

                                            Log.e("SiteObjectsExchangeITEM", "item(" + i + "): " + item.id);

                                            Gson gson = new Gson();
                                            String json = gson.toJson(item);
                                            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                                            Log.e("SiteObjectsExchangeROW", "ROW(" + i + "): " + convertedObject);
                                            i++;
                                        }
                                        //==========================================================


                                        SQL_DB.siteObjectsDao().insertAll(response.body().objectSQLList);
                                        Log.e("SiteObjectsExchange", "Ok");
                                        exchangeInterface.onSuccess("Загрузило: " + response.body().objectSQLList.size() + " ОбьектовСайта.");
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
                public void onFailure(retrofit2.Call<SiteObjectsResponse> call, Throwable t) {
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
