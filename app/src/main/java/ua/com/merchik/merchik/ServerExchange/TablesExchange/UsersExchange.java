package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.UsersResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class UsersExchange {

    public void downloadUsersTable(ExchangeInterface.ExchangeResponseInterface exchange){
        try {
            StandartData data = new StandartData();
            // Сотрудники
            data.mod = "data_list";
            data.act = "sotr_list";
            SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.getSynchronizationTimetableRowByTable("users_sql");
            try {
                String dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());
                if (dt_change_from.equals("0")){
                    data.dt_change_from = "0";
                }else {
                    data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app()-120);  // минус 2 минуты для "синхрона". Это надо поменять.
                }
            }catch (Exception e){
                data.dt_change_from = "0";
                Globals.writeToMLOG("ERR", "downloadUsersTable/SynchronizationTimetableDB", "Exception e: " + e);
            }


            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

//            retrofit2.Call<JsonObject> callTest = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
//            callTest.enqueue(new Callback<JsonObject>() {
//                @Override
//                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                    Log.e("downloadUsersTable", "response: " + response.body());
//                }
//
//                @Override
//                public void onFailure(Call<JsonObject> call, Throwable t) {
//                    Log.e("downloadUsersTable", "Throwable t: " + t);
//                }
//            });

            retrofit2.Call<UsersResponse> call = RetrofitBuilder.getRetrofitInterface().GET_USERS_ROOM(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<UsersResponse>() {
                @Override
                public void onResponse(Call<UsersResponse> call, Response<UsersResponse> response) {
                    try {
                        if (response.body() != null){
                            Globals.writeToMLOG("INFO", "downloadUsersTable/call.enqueue/onResponse/response.body()", "response.body(): " + response.body().list.size());

                            try {
                                RealmManager.INSTANCE.executeTransaction(realm -> {
                                    synchronizationTimetableDB.setVpi_app(System.currentTimeMillis()/1000);
                                    realm.copyToRealmOrUpdate(synchronizationTimetableDB);
                                });
                            }catch (Exception e){
                                Globals.writeToMLOG("ERR", "downloadUsersTable/call.enqueue/onResponse/RealmManager.INSTANCE.executeTransaction", "Exception e: " + e);
                            }

                            exchange.onSuccess(response.body().list);
                        }else {
                            Globals.writeToMLOG("INFO", "downloadUsersTable/call.enqueue/onResponse/response.body()", "response.body(): NULL");
                            exchange.onFailure("Ошибка при обновлении Сотрудников(разбор данных). Передайте код ошибки Вашему руководителю. Код ошибки: ");
                        }
                    }catch (Exception e){
                        Globals.writeToMLOG("ERR", "downloadUsersTable/call.enqueue/onResponse/catch", "Exception e: " + e);
                        exchange.onFailure("Ошибка при обновлении Сотрудников(разбор данных). Передайте код ошибки Вашему руководителю. Код ошибки: " + e);
                    }
                }

                @Override
                public void onFailure(Call<UsersResponse> call, Throwable t) {
                    Globals.writeToMLOG("FAILURE", "downloadUsersTable/call.enqueue/onFailure", "Throwable t: " + t);
                    exchange.onFailure("Ошибка сети. Проверьте интернет или повторите попытку позже. Код ошибки: " + t);
                }
            });

        }catch (Exception e){
            Globals.writeToMLOG("ERR", "downloadUsersTable/catch", "Exception e: " + e);
            exchange.onFailure("Ошибка при обновлении Сотрудников. Передайте код ошибки Вашему руководителю. Код ошибки: " + e);
        }
    }
}
