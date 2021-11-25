package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.LanguagesResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/**
 * 05.05.2021
 * Прошлая реализация находится в menu_login. Что она там делает, почему и зачем - вопросы скорее к
 * вселенной чем ко мне. Его надо оттуда перенести сюда. На данный момент не критично по этому пока
 * его не трогаю
 *
 * 10.05.2021
 * Языки. Получение списка языков с сайта. Используются для переключения языков и получения переводов.
 * */
public class LanguagesExchange {

    /**
     * Упрощённый функционал без множества проверок. Если будут вопросы - добавлять проверки.
     * */
    public void downloadLanguages(ExchangeInterface.Languages exchangeInterface){
        try {
            StandartData data = new StandartData();
            data.mod = "translation";
            data.act = "lang_list";

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<LanguagesResponse> call = RetrofitBuilder.getRetrofitInterface().GET_LANGUAGES_ROOM(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<LanguagesResponse>() {
                @Override
                public void onResponse(retrofit2.Call<LanguagesResponse> call, retrofit2.Response<LanguagesResponse> response) {
                    try {
                        if (response.body() != null){
                            if (response.body().state){
                                exchangeInterface.onSuccess(response.body().list);
                            }else {
                                // todo ADD M_LOG
                                exchangeInterface.onFailure("LanguagesExchange.Call.onResponse.State: false");
                            }
                        }else {
                            // todo ADD M_LOG
                            exchangeInterface.onFailure("LanguagesExchange.Call.onResponse.response: null");
                        }
                    }catch (Exception e){
                        // todo ADD M_LOG
                        exchangeInterface.onFailure("LanguagesExchange.Call.onResponse: " + e);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<LanguagesResponse> call, Throwable t) {
                    // todo ADD M_LOG
                    exchangeInterface.onFailure("LanguagesExchange.Call.onFailure: " + t);
                }
            });
        }catch (Exception e){
            // todo ADD M_LOG
            exchangeInterface.onFailure("LanguagesExchange.Exception: " + e);
        }

    }

}
