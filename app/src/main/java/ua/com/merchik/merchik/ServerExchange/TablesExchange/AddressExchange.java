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
import ua.com.merchik.merchik.data.RetrofitResponse.tables.AddressResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/**
 * 06.07.2021
 * Загрузка со стороный сайта таблички АДРЕСОВ.
 * */
public class AddressExchange {

    public void downloadAddressTable(ExchangeInterface.ExchangeResponseInterface exchange){
        try {
            StandartData data = new StandartData();
            // Адреса
            data.mod = "data_list";
            data.act = "addr_list";
            SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.getSynchronizationTimetableRowByTable("address_sql");
            String dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());
            if (dt_change_from.equals("0")){
                data.dt_change_from = "0";
            }else {
                data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app()-120);  // минус 2 минуты для "синхрона". Это надо поменять.
            }

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<AddressResponse> call = RetrofitBuilder.getRetrofitInterface().GET_ADDRESS_ROOM(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<AddressResponse>() {
                @Override
                public void onResponse(Call<AddressResponse> call, Response<AddressResponse> response) {
                    try {
                        if (response.body() != null){
                            Log.e("downloadAddressTable", "response.body(): " + response.body());
                            Globals.writeToMLOG("INFO", "downloadAddressTable/call.enqueue/onResponse/response.body()", "response.body(): " + response.body().list.size());
                            RealmManager.INSTANCE.executeTransaction(realm -> {
                                synchronizationTimetableDB.setVpi_app(System.currentTimeMillis()/1000);
                                realm.copyToRealmOrUpdate(synchronizationTimetableDB);
                            });
                            exchange.onSuccess(response.body().list);
                        }else {
                            Globals.writeToMLOG("INFO", "downloadAddressTable/call.enqueue/onResponse/response.body()", "response.body(): NULL");
                            exchange.onFailure("Ошибка при обновлении Адресов(разбор данных). Передайте код ошибки Вашему руководителю. Код ошибки: ");
                        }
                    }catch (Exception e){
                        Globals.writeToMLOG("ERR", "downloadAddressTable/call.enqueue/onResponse/catch", "Exception e: " + e);
                        exchange.onFailure("Ошибка при обновлении Адресов(разбор данных). Передайте код ошибки Вашему руководителю. Код ошибки: " + e);
                    }
                }

                @Override
                public void onFailure(Call<AddressResponse> call, Throwable t) {
                    Globals.writeToMLOG("FAILURE", "downloadAddressTable/call.enqueue/onFailure", "Throwable t: " + t);
                    exchange.onFailure("Ошибка сети. Проверьте интернет или повторите попытку позже. Код ошибки: " + t);
                }
            });

        }catch (Exception e){
            Globals.writeToMLOG("ERR", "downloadAddressTable/catch", "Exception e: " + e);
            exchange.onFailure("Ошибка при обновлении Адресов. Передайте код ошибки Вашему руководителю. Код ошибки: " + e);
        }
    }


}
