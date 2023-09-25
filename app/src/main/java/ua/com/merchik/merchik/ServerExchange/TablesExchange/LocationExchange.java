package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RetrofitResponse.Location.LocationResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class LocationExchange {
    public void downloadLocationTable(ExchangeInterface.ExchangeResponseInterface exchange){
        try {
            StandartData data = new StandartData();
            data.mod = "location";
            data.act = "list";
//            data.dt_change_from = String.valueOf(System.currentTimeMillis()/1000 - 3600);
            SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.getSynchronizationTimetableRowByTable("location");
            try {
                if (synchronizationTimetableDB != null){
                    String dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());
                    if (dt_change_from.equals("0")){
                        data.dt_change_from = "0";
                    }else {
                        data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app()-120);  // минус 2 минуты для "синхрона". Это надо поменять.
                    }
                }else {
                    data.dt_change_from = "0";
                }
            }catch (Exception e){
                data.dt_change_from = "0";
            }

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<LocationResponse> call = RetrofitBuilder.getRetrofitInterface().LOCATION_RESPONSE_CALL(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<LocationResponse>() {
                @Override
                public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                    try {
                        if (response.body() != null){
                            Log.e("downloadLocationTable", "response.body(): " + response.body());
                            Globals.writeToMLOG("INFO", "downloadLocationTable/call.enqueue/onResponse/response.body()", "response.body(): " + response.body().list.size());
                            RealmManager.INSTANCE.executeTransaction(realm -> {
                                if (synchronizationTimetableDB != null){
                                    synchronizationTimetableDB.setVpi_app(System.currentTimeMillis()/1000);
                                    realm.copyToRealmOrUpdate(synchronizationTimetableDB);
                                }

                            });
                            exchange.onSuccess(response.body().list);
                        }else {
                            Globals.writeToMLOG("INFO", "downloadLocationTable/call.enqueue/onResponse/response.body()", "response.body(): NULL");
                            exchange.onFailure("Ошибка при обновлении Location(разбор данных). Передайте код ошибки Вашему руководителю. Код ошибки: ");
                        }
                    }catch (Exception e){
                        Globals.writeToMLOG("ERR", "downloadLocationTable/call.enqueue/onResponse/catch", "Exception e: " + e);
                        exchange.onFailure("Ошибка при обновлении Location(разбор данных). Передайте код ошибки Вашему руководителю. Код ошибки: " + e);
                    }
                }

                @Override
                public void onFailure(Call<LocationResponse> call, Throwable t) {
                    Globals.writeToMLOG("FAILURE", "downloadLocationTable/call.enqueue/onFailure", "Throwable t: " + t);
                    exchange.onFailure("Ошибка сети. Проверьте интернет или повторите попытку позже. Код ошибки: " + t);
                }
            });

        }catch (Exception e){
            Globals.writeToMLOG("ERR", "downloadLocationTable/catch", "Exception e: " + e);
            exchange.onFailure("Ошибка при обновлении Location. Передайте код ошибки Вашему руководителю. Код ошибки: " + e);
        }
    }
}
