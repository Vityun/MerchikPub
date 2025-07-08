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
import ua.com.merchik.merchik.data.RetrofitResponse.tables.CustomerResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class  CustomerExchange <T>{

    public void downloadCustomerTable(ExchangeInterface.ExchangeResponseInterface exchange){
        try {
            Log.e("downloadCustomerTable", "S: ");
            StandartData data = new StandartData();
            // Клиенты
            data.mod = "data_list";
            data.act = "client_list";
            SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.getSynchronizationTimetableRowByTable("clients_sql");
            String dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());
            if (dt_change_from.equals("0")){
                data.dt_change_from = "0";
            }else {
                data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app()-120);  // минус 2 минуты для "синхрона". Это надо поменять.
            }

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<T> call = (Call<T>) RetrofitBuilder.getRetrofitInterface().GET_CUSTOMER_ROOM(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    try {
                        if (response.body() != null){
                            CustomerResponse cus = (CustomerResponse) response.body();

                            Globals.writeToMLOG("INFO", "downloadCustomerTable/call.enqueue/onResponse/response.body()", "response.body() size: " + ((CustomerResponse) response.body()).list.size());

                            RealmManager.INSTANCE.executeTransaction(realm -> {
                                synchronizationTimetableDB.setVpi_app(System.currentTimeMillis()/1000);
                                realm.copyToRealmOrUpdate(synchronizationTimetableDB);
                            });
                            exchange.onSuccess(cus.list);
                        }else {
                            Globals.writeToMLOG("INFO", "downloadCustomerTable/call.enqueue/onResponse/response.body()", "response.body(): NULL");
                            exchange.onFailure("Ошибка при обновлении Клиентов(разбор данных). Передайте код ошибки Вашему руководителю. Код ошибки: ");
                        }
                    }catch (Exception e){
                        Log.e("downloadCustomerTable", "SE: " + e);

                        Globals.writeToMLOG("ERR", "downloadCustomerTable/call.enqueue/onResponse/catch", "Exception e: " + e);
                        exchange.onFailure("Ошибка при обновлении Клиентов(разбор данных). Передайте код ошибки Вашему руководителю. Код ошибки: " + e);
                    }
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {
                    Log.e("downloadCustomerTable", "SF: " + t);

                    Globals.writeToMLOG("FAILURE", "downloadCustomerTable/call.enqueue/onFailure", "Throwable t: " + t);
                    exchange.onFailure("Ошибка сети. Проверьте интернет или повторите попытку позже. Код ошибки: " + t);
                }
            });

        }catch (Exception e){
            Log.e("downloadCustomerTable", "SEE: " + e);

            Globals.writeToMLOG("ERR", "downloadCustomerTable/catch", "Exception e: " + e);
            exchange.onFailure("Ошибка при обновлении Клиентов. Передайте код ошибки Вашему руководителю. Код ошибки: " + e);
        }
    }
}
