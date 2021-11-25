package ua.com.merchik.merchik.ServerExchange.TablesExchange;


import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.TranslatesResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/**
 * 10.05.2021
 * Переводы.
 * */
public class TranslationsExchange {

    public void downloadTranslations(ExchangeInterface.Translates exchangeInterface){
        try {
            StandartData data = new StandartData();
            data.mod = "translation";
            data.act = "translation_list";
            data.lang_id = String.valueOf(Globals.langId);

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<TranslatesResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TRANSLATES_ROOM(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<TranslatesResponse>() {
                @Override
                public void onResponse(retrofit2.Call<TranslatesResponse> call, retrofit2.Response<TranslatesResponse> response) {
                    try {
                        if (response.body() != null){
                            if (response.body().state){
                                exchangeInterface.onSuccess(response.body().list);
                            }else {
                                // todo ADD M_LOG
                                exchangeInterface.onFailure("TranslationsExchange.Call.onResponse.State: false");
                            }
                        }else {
                            // todo ADD M_LOG
                            exchangeInterface.onFailure("TranslationsExchange.Call.onResponse.response: null");
                        }
                    }catch (Exception e){
                        // todo ADD M_LOG
                        exchangeInterface.onFailure("TranslationsExchange.Call.onResponse: " + e);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<TranslatesResponse> call, Throwable t) {
                    // todo ADD M_LOG
                    exchangeInterface.onFailure("TranslationsExchange.Call.onFailure: " + t);
                }
            });

        }catch (Exception e){
            // todo ADD M_LOG
            exchangeInterface.onFailure("TranslationsExchange.Exception: " + e);
        }
    }
}
