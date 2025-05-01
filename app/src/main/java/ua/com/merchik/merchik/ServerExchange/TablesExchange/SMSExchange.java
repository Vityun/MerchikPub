package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RetrofitResponse.SMSLogResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.SMSPlanResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class SMSExchange {

    public void smsPlanExchange(Clicks.clickObjectAndStatus click) {
        StandartData data = new StandartData();
        data.mod = "sms";
        data.act = "get_plan";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

//        retrofit2.Call<JsonObject> call1 = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
//        call1.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                Log.e("SMSExchange", "smsPlanExchange: " + response);
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.e("SMSExchange", "smsPlanExchange: " + t);
//            }
//        });

        retrofit2.Call<SMSPlanResponse> call = RetrofitBuilder.getRetrofitInterface().SMSPlan_RESPONSE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<SMSPlanResponse>() {
            @Override
            public void onResponse(Call<SMSPlanResponse> call, Response<SMSPlanResponse> response) {
                Log.e("SMSExchange", "smsPlanExchange: " + response);
                Log.e("SMSExchange", "smsPlanExchange body: " + response.body());

                Log.e("test", "test" + response);
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().state) {
                                if (response.body().list != null && response.body().list.size() > 0) {
                                    SQL_DB.smsPlanDao().insertAllCompletable(response.body().list)
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new DisposableCompletableObserver() {
                                                @Override
                                                public void onComplete() {
                                                    Log.d("test", "test");
                                                    Globals.writeToMLOG("INFO", "SMSExchange/smsPlanExchange/onResponse/onComplete", "OK: " + response.body().list.size());
//                                                    click.onSuccess(response.body().list);
                                                }

                                                @Override
                                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                                    Log.d("test", "test");
                                                    Globals.writeToMLOG("ERROR", "SMSExchange/smsPlanExchange/onResponse/onError", "Throwable e: " + e);
                                                    click.onFailure("onError SQL_DB.smsPlanExchange().insertAll Throwable e: " + e);
                                                }
                                            });
                                }

                            } else {
                                click.onFailure("Ошибка запроса. State=false");
                            }
                        } else {
                            click.onFailure("Ошибка запроса. Тело пришло пустым.");
                        }
                    } else {
                        click.onFailure("Ошибка запроса. Код: " + response.code());
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "SMSExchange/smsPlanExchange/onResponse", "Exception e: " + e);
                    click.onFailure("Ошибка запроса. Exception e: " + e);
                }
            }

            @Override
            public void onFailure(Call<SMSPlanResponse> call, Throwable t) {
                Log.e("SMSExchange", "smsPlanExchange: " + t);
            }
        });
    }


    public void smsLogExchange(Clicks.clickObjectAndStatus click) {
        StandartData data = new StandartData();
        data.mod = "sms";
        data.act = "get_log";
        data.nolimit = "1";

        // #### TODO
        SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.INSTANCE.copyFromRealm(RealmManager.getSynchronizationTimetableRowByTable("errorsList"));
        data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());


        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

//        retrofit2.Call<JsonObject> call1 = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
//        call1.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                Log.e("SMSExchange", "smsPlanExchange: " + response);
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.e("SMSExchange", "smsPlanExchange: " + t);
//            }
//        });


        retrofit2.Call<SMSLogResponse> call = RetrofitBuilder.getRetrofitInterface().SMSLog_RESPONSE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<SMSLogResponse>() {
            @Override
            public void onResponse(Call<SMSLogResponse> call, Response<SMSLogResponse> response) {
                Log.e("SMSExchange", "smsPlanExchange: " + response);
                Log.e("SMSExchange", "smsPlanExchange body: " + response.body());
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().state) {
                                if (response.body().list != null && response.body().list.size() > 0) {
                                    SQL_DB.smsLogDao().insertAllCompletable(response.body().list)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new DisposableCompletableObserver() {
                                                @Override
                                                public void onComplete() {
                                                    Log.d("test", "test");
                                                    Globals.writeToMLOG("INFO", "SMSExchange/smsLogExchange/onResponse/onComplete", "OK: " + response.body().list.size());
//                                                    click.onSuccess(response.body().list);

                                                    RealmManager.INSTANCE.executeTransaction(realm -> {
                                                        synchronizationTimetableDB.setVpi_app(System.currentTimeMillis() / 1000);
                                                        realm.copyToRealmOrUpdate(synchronizationTimetableDB);
                                                    });
                                                    Log.d("test", "test");

                                                }

                                                @Override
                                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                                    Log.d("test", "test");
                                                    Globals.writeToMLOG("ERROR", "SMSExchange/smsLogExchange/onResponse/onError", "Throwable e: " + e);
                                                    click.onFailure("onError SQL_DB.smsLogExchange().insertAll Throwable e: " + e);
                                                }
                                            });
                                }

                            } else {
                                click.onFailure("Ошибка запроса. State=false");
                            }
                        } else {
                            click.onFailure("Ошибка запроса. Тело пришло пустым.");
                        }
                    } else {
                        click.onFailure("Ошибка запроса. Код: " + response.code());
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "SMSExchange/smsLogExchange/onResponse", "Exception e: " + e);
                    click.onFailure("Ошибка запроса. Exception e: " + e);
                }
            }

            @Override
            public void onFailure(Call<SMSLogResponse> call, Throwable t) {
                Log.e("SMSExchange", "smsLogExchange: " + t);
            }
        });
    }
}
