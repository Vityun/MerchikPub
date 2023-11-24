package ua.com.merchik.merchik.Activities;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.LocationExchange;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RetrofitResponse.Location.LocationList;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ShowcaseResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogShowcase.DialogShowcase;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;
import ua.com.merchik.merchik.toolbar_menus;


public class MenuMainActivity extends toolbar_menus {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent();


        try {

            findViewById(R.id.fab).setOnClickListener(v -> {
                Toast.makeText(this, "Подсказка к данному разделу не готова", Toast.LENGTH_SHORT).show();
                test();
            });

            findViewById(R.id.fab).setOnLongClickListener(v -> {
                Toast.makeText(this, "Отладочная информация!\nДолгий клик по подсказке.", Toast.LENGTH_SHORT).show();
//                test2(v.getContext());
                return true;
            });

            initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_home);
        } catch (Exception e) {
            globals.alertDialogMsg(this, "ОшибкаMain: " + e);
        }
    }

    private void test() {
        try {
            new LocationExchange().downloadLocationTable(new ExchangeInterface.ExchangeResponseInterface() {
                @Override
                public <T> void onSuccess(List<T> data) {
                    try {
                        List<LocationList> newDataList = (List<LocationList>) data;
                        List<LogMPDB> allLogMPListDB = RealmManager.INSTANCE.copyFromRealm(RealmManager.getAllLogMPDB());

                        // Создаем множество для быстрого поиска всех серверных ID из allLogMPListDB
                        Set<Long> serverIdsInDB = new HashSet<>();
                        for (LogMPDB log : allLogMPListDB) {
                            serverIdsInDB.add(log.serverId);
                        }

                        List<LogMPDB> logList = new ArrayList<>();
                        int id = RealmManager.logMPGetLastId();
                        // Перебираем элементы newDataList и добавляем в newDataListToSave только те, которых нет в базе данных
                        for (LocationList location : newDataList) {
                            // Проверяем, есть ли текущий ID в списке серверных ID из базы данных
                            if (!serverIdsInDB.contains(location.id)) {
                                LogMPDB logMPDB = new LogMPDB();
                                logMPDB.id = ++id;
                                logMPDB.serverId = location.id;
                                logMPDB.provider = location.sourceId;
                                logMPDB.CoordX = location.lat;
                                logMPDB.CoordY = location.lon;
                                logMPDB.CoordAltitude = location.altitude;
                                logMPDB.CoordTime = location.dtDevice*1000;
                                logMPDB.CoordSpeed = location.speed;
                                logMPDB.CoordAccuracy = location.accuracy;
                                logMPDB.mocking = location.locationIsFake != null && location.locationIsFake != 0;
                                logMPDB.vpi = System.currentTimeMillis() / 1000;

                                logList.add(logMPDB);
                            }
                        }


//                        List<LogMPDB> logList = new ArrayList<>();
//                        int id = RealmManager.logMPGetLastId();
//                        for (LocationList item : newDataList) {
//                            LogMPDB logMPDB = new LogMPDB();
//                            logMPDB.id = ++id;
//                            logMPDB.serverId = item.id;
//                            logMPDB.provider = item.sourceId;
//                            logMPDB.CoordX = item.lat;
//                            logMPDB.CoordY = item.lon;
//                            logMPDB.CoordAltitude = item.altitude;
//                            logMPDB.CoordTime = item.dtDevice;
//                            logMPDB.CoordSpeed = item.speed;
//                            logMPDB.CoordAccuracy = item.accuracy;
//                            logMPDB.mocking = item.locationIsFake != null && item.locationIsFake != 0;
//                            logMPDB.vpi = System.currentTimeMillis() / 1000;
//
//                            logList.add(logMPDB);
//                        }

                        RealmManager.INSTANCE.executeTransaction(realm -> {
                            List<LogMPDB> testList = realm.copyToRealmOrUpdate(logList);
                            Log.e("downloadLocationTable", "testList: " + testList);
                        });

                        Globals.writeToMLOG("INFO", "startExchange/downloadLocationTable/onFailure", "OK: ");
                    } catch (Exception e) {
                        Log.e("downloadLocationTable", "Exception e: " + e);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    Globals.writeToMLOG("INFO", "startExchange/downloadLocationTable/onFailure", "String error: " + error);
                }
            });
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "startExchange/downloadLocationTable", "Exception e: " + e);
        }
    }


    /*        String mod = "location";
        String act = "track";

        List<LogMPDB> logMp = RealmManager.getNOTUploadLogMPDBTEST(31, 32);

            Log.e("LogMp", "LogMpUploadText. LogSize: " + logMp.size());

            HashMap<String, String> map = new HashMap<>();
            for (LogMPDB list : logMp) {
                map.put("gp[" + list.getId() + "]", list.getGp());
            }

            Globals.writeToMLOG("INFO", "uploadLodMp", "Количество ЛОГ МП на выгрузку: " + logMp.size());

            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().UPLOAD_LOG_MP(mod, act, map);
            call.enqueue(new retrofit2.Callback<JsonObject>() {
                @Override
                public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    Log.e("LogMp", "RESPONSE: " + response.body());

                    // TODO Тут очень много раз в минуту дёргаю это место. Нужно проверить - нужно ли в таком количестве.
//                    Globals.writeToMLOG("INFO", "uploadLodMp/onResponse", "response.body(): " + response.body());

                    try {
                        JsonObject resp = response.body();
                        if (resp != null) {
                            if (!resp.get("state").isJsonNull() && resp.get("state").getAsBoolean()) {
                                JsonObject arr = resp.get("geo_result").getAsJsonObject();
                                if (arr != null) {
                                    for (LogMPDB list : logMp) {
                                        JsonObject geoInfo = arr.getAsJsonObject(String.valueOf(list.getId()));
                                        if (geoInfo != null && geoInfo.get("state").getAsBoolean()) {
                                            try {
                                                RealmManager.INSTANCE.executeTransaction(realm -> {
//                                                    list.deleteFromRealm();
                                                    list.upload = System.currentTimeMillis()/1000;  // 27.08.23 Вместо удаления, пишу воемя когда координаты были выгружены
                                                });

                                                res.onSuccess("ОК");
                                            } catch (Exception e) {
                                                Globals.writeToMLOG("ERROR", "uploadLodMp/onResponse/executeTransaction", "Exception e: " + e);
                                                res.onFailure("Exception e: " + e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "uploadLodMp/onResponse/onResponse", "Exception e: " + e);
                        res.onFailure("2_Exception e: " + e);
                    }

                }

                @Override
                public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                    Globals.writeToMLOG("ERROR", "uploadLodMp/onFailure", "Throwable t: " + t);
                    Log.e("LogMp", "FAILURE_E: " + t.getMessage());
                    Log.e("LogMp", "FAILURE_E2: " + t);
                    res.onFailure("onFailure: " + t);
                }
            });*/


    /*        try {
                StandartData data = new StandartData();
                data.mod = "data_list";
                data.act = "tovar_vendor_code_list";

                Gson gson = new Gson();
                String json = gson.toJson(data);
                JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        Log.e("test", "response" + response.body());
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e("test", "test" + t);
                    }
                });


            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "startExchange/ShowcaseExchange/downloadShowcaseTable", "Exception e: " + e);
            }*/
    public void swoeDialogSW() {
        DialogShowcase dialog = new DialogShowcase(this);
        dialog.setClose(dialog::dismiss);
        dialog.show();
    }

    public void checkRequest() {
        StandartData data = new StandartData();
        data.mod = "rack";
        data.act = "list";

//        data.dt_change_from = "";
//        data.dt_change_to = "";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Log.e("checkRequest", "checkRequest: " + convertedObject);

        retrofit2.Call<ShowcaseResponse> call = RetrofitBuilder.getRetrofitInterface().SHOWCASE_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<ShowcaseResponse>() {
            @Override
            public void onResponse(Call<ShowcaseResponse> call, Response<ShowcaseResponse> response) {
                Log.e("checkRequest", "response: " + response);
                Log.e("checkRequest", "response.body(): " + response.body());
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().state && response.body().list != null && response.body().list.size() > 0) {
                        SQL_DB.showcaseDao().insertAll(response.body().list);
                    }
                }
            }

            @Override
            public void onFailure(Call<ShowcaseResponse> call, Throwable t) {
                Log.e("checkRequest", "Throwable t: " + t);
            }
        });
    }

    private void planogram() {
        // Просто планограммы
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "Просто планограммы convertedObject: " + convertedObject);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("MAIN_test", "Просто планограммы: " + response);
                Log.e("MAIN_test", "Просто планограммы body: " + response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MAIN_test", "Просто планограммы: " + t);
            }
        });
    }

    private void planogramAddr() {
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "addr_list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "Адреса планограммы convertedObject: " + convertedObject);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("MAIN_test", "Адреса планограммы: " + response);
                Log.e("MAIN_test", "Адреса планограммы body: " + response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MAIN_test", "Адреса планограммы: " + t);
            }
        });
    }

    private void planogramGrp() {
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "group_list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "Группы планограммы convertedObject: " + convertedObject);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("MAIN_test", "Группы планограммы: " + response);
                Log.e("MAIN_test", "Группы планограммы body: " + response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MAIN_test", "Группы планограммы: " + t);
            }
        });
    }

    private void planogramImg() {
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "img_list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "Фото планограммы convertedObject: " + convertedObject);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("MAIN_test", "Фото планограммы: " + response);
                Log.e("MAIN_test", "Фото планограммы body: " + response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MAIN_test", "Фото планограммы: " + t);
            }
        });
    }
    // =================================== --- onCreate --- ========================================

    private void setActivityContent() {
        setContentView(R.layout.toolbar_new);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        TextView activity_title = (TextView) findViewById(R.id.activity_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title.setText(getString(R.string.title_activity_menu_main));
    }


}
