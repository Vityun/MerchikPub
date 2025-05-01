package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ContentResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.StandartResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/**
 * 11.08.2021
 * Получение с сервера таблички Стандартов
 *
 * Точка входа: standart
 * */
public class StandartExchange {

    public void downloadStandartTable(ExchangeInterface.ExchangeResponseInterface exchange){
        try {
            StandartData data = new StandartData();
            data.mod = "standart";
            data.act = "list";

            // #### TODO
            SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.INSTANCE.copyFromRealm(RealmManager.getSynchronizationTimetableRowByTable("stack_photo"));
            data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());

//            data.dt_change_from = String.valueOf(System.currentTimeMillis()/1000 - 142);
//            data.dt_change_from = "0";

//            data.date_from = "";
//            data.date_to = "";

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<StandartResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_STANDART(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<StandartResponse>() {
                @Override
                public void onResponse(Call<StandartResponse> call, Response<StandartResponse> response) {
                    Log.e("test", "response: " + response);
                    exchange.onSuccess(response.body().list);
                }

                @Override
                public void onFailure(Call<StandartResponse> call, Throwable t) {
                    Log.e("test", "Throwable: " + t);
                    exchange.onFailure(null);
                }
            });


//            {
//                retrofit2.Call<JsonObject> call1 = RetrofitBuilder.getRetrofitInterface().MOD_IMAGES_VIEW_CALL_JSON(RetrofitBuilder.contentType, convertedObject);
//                call1.enqueue(new Callback<JsonObject>() {
//                    @Override
//                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                        Log.d("smarti", "onResponse: ");
//                    }
//
//                    @Override
//                    public void onFailure(Call<JsonObject> call, Throwable t) {
//                        Log.d("smarti", "onResponse: ");
//                    }
//                });
//            }
        }catch (Exception e){

        }
    }


    public void downloadContentTable(ExchangeInterface.ExchangeResponseInterface exchange){
        try {
            StandartData data = new StandartData();
            data.mod = "standart";
            data.act = "content_list";

            // #### TODO
            SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.INSTANCE.copyFromRealm(RealmManager.getSynchronizationTimetableRowByTable("stack_photo"));
            data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());

//            data.dt_change_from = "0";
//            data.dt_change_from = String.valueOf(System.currentTimeMillis()/1000 - 142);

//            data.date_from = "";
//            data.date_to = "";
//            data.active = "0/1";
//            data.theme_id = "";

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<ContentResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_CONTENT(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<ContentResponse>() {
                @Override
                public void onResponse(Call<ContentResponse> call, Response<ContentResponse> response) {
                    Log.e("test", "response: " + response);
                    exchange.onSuccess(response.body().list);
                }

                @Override
                public void onFailure(Call<ContentResponse> call, Throwable t) {
                    Log.e("test", "Throwable: " + t);
                    exchange.onFailure(null);
                }
            });
        }catch (Exception e){

        }
    }
}
