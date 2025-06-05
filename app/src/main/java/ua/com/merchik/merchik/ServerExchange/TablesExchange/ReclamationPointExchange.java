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

            StandartData.Filter filter = new StandartData.Filter();
            filter.date_from = Clock.getHumanTimeYYYYMMDD(Clock.getDateLong(-20).getTime()/1000);
            filter.date_to = Clock.getHumanTimeYYYYMMDD(Clock.getDateLong(20).getTime()/1000);

//            // Нужно получить ВПИ с таблички синхронизаций
            SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.getSynchronizationTimetableRowByTable("task_and_reclamations");
            if (synchronizationTimetableDB != null){
                String dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());
                if (dt_change_from.equals("0")){
                    data.dt_change_from = "0";
                }else {
                    data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());
//                    filter.dt_change_from =
                }
                Globals.writeToMLOG("INFO", "ReclamationPointExchange/downloadTaR", "synchronizationTimetableDB != null && data.dt_change_from: " + data.dt_change_from);
            }else {
                data.dt_change_from = "0";
                Globals.writeToMLOG("INFO", "ReclamationPointExchange/downloadTaR", "synchronizationTimetableDB == null");
            }

            data.filter = filter;

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            Globals.writeToMLOG("INFO", "ReclamationPointExchange/downloadTaR", "convertedObject: " + convertedObject);

            retrofit2.Call<TasksAndReclamationsSDBResponce> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_TasksAndReclamationsSDB(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<TasksAndReclamationsSDBResponce>() {
                @Override
                public void onResponse(Call<TasksAndReclamationsSDBResponce> call, Response<TasksAndReclamationsSDBResponce> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null){
                            if (response.body().list != null && response.body().list.size()>0){
                                RealmManager.INSTANCE.executeTransaction(realm -> {
                                    if (synchronizationTimetableDB != null) {
                                        synchronizationTimetableDB.setVpi_app(System.currentTimeMillis()/1000);
                                        realm.copyToRealmOrUpdate(synchronizationTimetableDB);
                                    }
                                });

                                exchange.onSuccess(response.body().list);
                            }else {
                                exchange.onFailure("Dанных нет"); // данных нет
                            }
                        }else {
                            exchange.onFailure("Запрос вернулся пустым.");   //Запрос вернулся пустым.
                        }
                    }catch (Exception e){
                        exchange.onFailure("Запрос завершён с ошибкой: " + e);   //Запрос завершён с ошибкой
                    }
                }

                @Override
                public void onFailure(Call<TasksAndReclamationsSDBResponce> call, Throwable t) {
                    Globals.writeToMLOG("INFO", "ReclamationPointExchange/downloadTaR/onFailure", "t: " + t);
                    exchange.onFailure("onFailure: " + t);
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
