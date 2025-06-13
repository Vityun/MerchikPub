package ua.com.merchik.merchik.Activities;

import static ua.com.merchik.merchik.database.realm.RealmManager.getAllWorkPlan;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.DynamicRealm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.workmager.WorkManagerHelper;
import ua.com.merchik.merchik.Translate;
import ua.com.merchik.merchik.Utils.CodeGenerator;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ShowcaseResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.dialogs.DialogShowcase.DialogShowcase;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;
import ua.com.merchik.merchik.toolbar_menus;


public class MenuMainActivity extends toolbar_menus {

    CronchikViewModel cronchikViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent();

        cronchikViewModel = new ViewModelProvider(this).get(CronchikViewModel.class);

        try {

            findViewById(R.id.fab).setOnClickListener(v -> {
                try {
                    Toast.makeText(this, "Подсказка к данному разделу не готова", Toast.LENGTH_SHORT).show();
                    test();
                } catch (Exception e) {
                    Log.e("MenuMainActivity", "Exception e: " + e);
                }
            });

            findViewById(R.id.fab).setOnLongClickListener(v -> {
//                Toast.makeText(this, "Отладочная информация!\nДолгий клик по подсказке.", Toast.LENGTH_SHORT).show();
                testLong();
                return true;
            });

            initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_home);
        } catch (Exception e) {
            globals.alertDialogMsg(this, "ОшибкаMain: " + e);
        }
    }

    private void testLong() {
        AppUsersDB appUsersDB = AppUserRealm.getAppUser();
        if (appUsersDB != null && appUsersDB.getUserId() == 172906) {
            String res = CodeGenerator.getCode();
            Toast.makeText(getApplicationContext(), "" + res, Toast.LENGTH_LONG).show();
        }
    }

    public long getRealmSchemaVersion() {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("myrealm.realm")
                .allowWritesOnUiThread(true)
                .allowQueriesOnUiThread(true)
                .deleteRealmIfMigrationNeeded()
                .build();

        DynamicRealm realm = DynamicRealm.getInstance(config);
        long schemaVersion = realm.getVersion();
        realm.close();
        return schemaVersion;
    }

    public static List<WpDataDB> getWorkPlanList() {
        RealmResults<WpDataDB> realmResults = getAllWorkPlan(); // Получаем RealmResults
        return realmResults != null ? new ArrayList<>(realmResults) : new ArrayList<>(); // Преобразуем в List
    }

    public static List<WpDataDB> getUniqueClientAndAddrId(List<WpDataDB> workPlanList) {
        Set<String> uniqueClientAddrSet = new HashSet<>(); // Храним уникальные комбинации client_id и addr_id
        List<WpDataDB> uniqueList = new ArrayList<>(); // Результирующий список
        // Список для формирования строки для лога
        List<String> logEntries = new ArrayList<>();

        for (WpDataDB wpDataDB : workPlanList) {
            String clientId = wpDataDB.getClient_id();
            int addrId = wpDataDB.getAddr_id();
            String uniqueKey = clientId + "_" + addrId; // Создаем уникальный ключ для пары (client_id, addr_id)

            if (!uniqueClientAddrSet.contains(uniqueKey)) {  // Если такая пара еще не встречалась
                uniqueClientAddrSet.add(uniqueKey);  // Добавляем в Set
                uniqueList.add(wpDataDB);  // Добавляем элемент в результирующий список

                // Добавляем запись для лога в формате "client_id-addr_id"
                logEntries.add(clientId + "-" + addrId);
            }
        }

        // Формируем строку для лога с уникальными клиентами и адресами через запятую
        String logMessage = String.join(", ", logEntries);

        // Выводим строку в лог
        Log.d("UniqueClientAddrLog", "Unique clients and addresses: " + logMessage);

        return uniqueList;
    }

    private void test() {


//        WorkManagerHelper.INSTANCE.startSyncWorker(this);

//        Exchange.exchangeTime = 0;


//        realm.close();

//        PlanogrammTableExchange planogrammTableExchange = new PlanogrammTableExchange();
//        planogrammTableExchange.planogrammVisitShowcaseUploadData();

//        String clientId = "9295";
//        Integer addressId = 31987;
//        String ttId = "32";
//
//
//        Intent intent = new Intent(this, FeaturesActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putString("viewModel", PlanogrammVizitShowcaseViewModel.class.getCanonicalName());
//        bundle.putString("contextUI", ContextUI.PLANOGRAMM_VIZIT_SHOWCASE.toString());
//        bundle.putString("modeUI", ModeUI.DEFAULT.toString());
//        JsonObject dataJson = new JsonObject();
//        dataJson.addProperty("clientId", String.valueOf(clientId));
//        dataJson.addProperty("addressId", addressId);
//        dataJson.addProperty("ttId", String.valueOf(ttId));
//        bundle.putString("dataJson", new Gson().toJson(dataJson));
//
//        bundle.putString("title", "##Панограмма посещения");
//        bundle.putString(
//                "subTitle",
//                "##subTitle"
//        );
//        intent.putExtras(bundle);
//
//        ActivityCompat.startActivityForResult(this, intent, NEED_UPDATE_UI_REQUEST, null);



//        new TablesLoadingUnloading().downloadWPData(this);

        new Translate().uploadNewTranslate();

//        Exchange exchange = new Exchange();
//        exchange.updateAverageSalary();
//        exchange.updateSiteObjкрон);

        // Обучение
//        String title = SQL_DB.siteObjectsDao().getObjectsByRealId(2599).comments;
//        String subtitle = SQL_DB.siteObjectsDao().getObjectsByRealId(6941).comments + ": " + ImagesTypeListRealm.getByID(14).getNm();

//        Log.e("!!!!!!!!",">>> " + subtitle);
//
//        new SiteObjectsExchange().downloadSiteObjects(new Exchange.ExchangeInt() {
//            @Override
//            public void onSuccess(String msg) {
//
//            }
//
//            @Override
//            public void onFailure(String error) {
//
//            }
//        });
    }

/*


            PlanogrammTableExchange planogrammTableExchange = new PlanogrammTableExchange();
            planogrammTableExchange.planogramDownload(new Clicks.clickObjectAndStatus() {
                @Override
                public void onSuccess(Object data) {

                }

                @Override
                public void onFailure(String error) {

                }
            });



try {
        PlanogrammTableExchange planogrammTableExchange = new PlanogrammTableExchange();
        planogrammTableExchange.planogramDownload(new Clicks.clickObjectAndStatus() {
            @Override
            public void onSuccess(Object data) {

            }

            @Override
            public void onFailure(String error) {

            }
        });
        planogrammTableExchange.planogrammAddressDownload(new Clicks.clickObjectAndStatus() {
            @Override
            public void onSuccess(Object data) {

            }

            @Override
            public void onFailure(String error) {

            }
        });
        planogrammTableExchange.planogrammGroupDownload(new Clicks.clickObjectAndStatus() {
            @Override
            public void onSuccess(Object data) {

            }

            @Override
            public void onFailure(String error) {

            }
        });
        planogrammTableExchange.planogrammImagesDownload(new Clicks.clickObjectAndStatus() {
            @Override
            public void onSuccess(Object data) {

            }

            @Override
            public void onFailure(String error) {

            }
        });
    }catch (Exception e){
        Globals.writeToMLOG("ERROR", "startExchange/PlanogrammExchange/planogrammDownload", "Exception e: " + e);
    }*/

/*

        Globals.writeToMLOG("INFO", "uploadAchievemnts", "test");
        new Exchange().uploadAchievemnts();

        new Exchange().planogram(new ExchangeInterface.ExchangeResponseInterface() {
            @Override
            public <T> void onSuccess(List<T> data) {
                try {
                    List<ImagesViewListImageList> datalist = (List<ImagesViewListImageList>) data;
                    PhotoDownload.savePhotoToDB2(datalist);
                    Globals.writeToMLOG("INFO", "startExchange/planogram.onSuccess", "OK: " + datalist.size());
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/planogram.onSuccess", "Exception e: " + e);
                }
            }

            @Override
            public void onFailure(String error) {
                Globals.writeToMLOG("FAIL", "startExchange/planogram/onFailure", error);
            }
        }); // Получение планограмм


new PlanogrammTableExchange().planogramDownload(new Clicks.clickObjectAndStatus() {
        @Override
        public void onSuccess(Object data) {

        }

        @Override
        public void onFailure(String error) {

        }
    });*/

/*    // Просто планограммы
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
    });*/




/*                try {
        StandartData data = new StandartData();
        data.mod = "additional_requirements";
        data.act = "list";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<AdditionalRequirementsServerData> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_AdditionalRequirementsDB(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new retrofit2.Callback<AdditionalRequirementsServerData>() {
            @Override
            public void onResponse(retrofit2.Call<AdditionalRequirementsServerData> call, retrofit2.Response<AdditionalRequirementsServerData> response) {
                try {
                    AdditionalRequirementsRealm.setDataToDB(response.body().getList());
                    Globals.writeToMLOG("ERR", "downloadAdditionalRequirements/onResponse", "response.body().getList(): " + response.body().getList().size());
                } catch (Exception e) {
                    Globals.writeToMLOG("ERR", "downloadAdditionalRequirements/onResponse", "Exception e: " + e);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<AdditionalRequirementsServerData> call, Throwable t) {
                Globals.writeToMLOG("ERR", "downloadAdditionalRequirements/onFailure", "Throwable t: " + t);
            }
        });
    } catch (Exception e) {
        Globals.writeToMLOG("ERR", "downloadAdditionalRequirements", "Exception e: " + e);
    }*/



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

    // ВИТРИНЫ
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
