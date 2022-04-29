package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.TasksAndReclamationsSDBResponce;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class ReclamationPointExchange {

    /**
     * Получение в SQL Задач и Рекламаций
     * */
    public void downloadTaR(ExchangeInterface.ExchangeResponseInterface exchange){
        try {
            StandartData data = new StandartData();
            data.mod = "reclamation";
            data.act = "list";

//            // Нужно получить ВПИ с таблички синхронизаций
            SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.getSynchronizationTimetableRowByTable("task_and_reclamations");
            String dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());
            if (dt_change_from.equals("0")){
                data.dt_change_from = "0";
            }else {
                data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app()-120);  // минус 2 минуты для "синхрона". Это надо поменять.
            }

            StandartData.Filter filter = new StandartData.Filter();
            filter.date_from = Clock.getHumanTimeYYYYMMDD(Clock.getDateLong(-7).getTime()/1000);
            filter.date_to = Clock.getHumanTimeYYYYMMDD(Clock.getDateLong(2).getTime()/1000);

            data.filter = filter;


            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

/*            retrofit2.Call<JsonObject> callTest = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
            callTest.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.e("test", "response");
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e("test", "err");
                }
            });*/

            Globals.writeToMLOG("INFO", "ReclamationPointExchange/downloadTaR", "convertedObject: " + convertedObject);

            retrofit2.Call<TasksAndReclamationsSDBResponce> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_TasksAndReclamationsSDB(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<TasksAndReclamationsSDBResponce>() {
                @Override
                public void onResponse(Call<TasksAndReclamationsSDBResponce> call, Response<TasksAndReclamationsSDBResponce> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null){

                            Gson gson = new Gson();
                            String json = gson.toJson(response.body());
                            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                            Globals.writeToMLOG("INFO", "ReclamationPointExchange/downloadTaR/onResponse", "convertedObject: " + convertedObject);

                            if (response.body().list != null && response.body().list.size()>0){
                                RealmManager.INSTANCE.executeTransaction(realm -> {
                                    synchronizationTimetableDB.setVpi_app(System.currentTimeMillis()/1000);
                                    realm.copyToRealmOrUpdate(synchronizationTimetableDB);
                                });

//                                SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.getSynchronizationTimetableRowByTable("task_and_reclamations");

                                exchange.onSuccess(response.body().list);
                            }else {
                                exchange.onFailure(null); // данных нет
                            }
                        }else {
                            exchange.onFailure(null);   //Запрос вернулся пустым.
                        }
                    }catch (Exception e){
                        exchange.onFailure(null);   //Запрос завершён с ошибкой
                    }
                }

                @Override
                public void onFailure(Call<TasksAndReclamationsSDBResponce> call, Throwable t) {
                    Globals.writeToMLOG("INFO", "ReclamationPointExchange/downloadTaR/onFailure", "t: " + t);
                    exchange.onFailure(null);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
            Log.e("test", "e:" +e);
            Globals.writeToMLOG("ERROR", "ReclamationPointExchange/downloadTaR", "Exception e: " + e);
            exchange.onFailure(null);
        }
    }

}
