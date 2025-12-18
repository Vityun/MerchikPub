package ua.com.merchik.merchik.Activities;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.database.realm.RealmManager.getAllWorkPlan;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.DynamicRealm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.*;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.SamplePhotoExchange;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.Utils.CodeGenerator;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.WpDataServer;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ShowcaseResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.dialogs.DialogShowcase.DialogShowcase;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;


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
//                    Toast.makeText(this, "Подсказка к данному разделу не готова", Toast.LENGTH_SHORT).show();
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

    public static CharSequence getFormattedMessage() {
        final String text = "Пояснения и нюансы" +
                "\n" +
                "Почему Dialog, а не WindowManager напрямую?\n" +
                "Dialog проще подписывать под жизненный цикл Activity и не требует отдельных разрешений. Мы вручную меняем type на TYPE_APPLICATION_PANEL и «прибиваем» диалог к токену окна активити — так он оказывается выше любых внутренних окон приложения, включая стандартные Dialog/BottomSheet.\n" +
                "\n" +
                "Не перехватывает фокус: флаги FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL пропускают касания к нижним слоям, как у «настоящего» тоста.\n" +
                "\n" +
                "Откуда вызвать: через очередь MutableSharedFlow — можно вызывать из VM/репозитория/сервиса, лишь бы вы один раз сделали AppToast.init(application).\n" +
                "\n" +
                "Очередь и отмена: как у системного тоста — новый показ отменяет предыдущий.\n" +
                "\n" +
                "Compose не обязателен: при желании замените содержимое на обычный XML-layout — API не изменится.\n" +
                "\n" +
                "Ограничения: это «самый верх» в пределах вашего процесса/активити. Над окнами других приложений/системы подниматься не будет (и не должен). Если поверх вашего тоста моментально";

        SpannableStringBuilder ssb = new SpannableStringBuilder(text);

        // 1) Заголовок (до первой новой строки)
        int endTitle = text.indexOf("\n");
        if (endTitle > 0) {
            ssb.setSpan(new ForegroundColorSpan(Color.RED), 0, endTitle, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, endTitle, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 2) Английские слова другим шрифтом
        Pattern engPattern = Pattern.compile("[A-Za-z]+");
        Matcher engMatcher = engPattern.matcher(text);
        while (engMatcher.find()) {
            ssb.setSpan(new TypefaceSpan("monospace"),
                    engMatcher.start(), engMatcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 3) Текст в кавычках (и «русские», и "английские")
        // Текст в кавычках = синим + подчёркивание + кликабельность
        Pattern quotePattern = Pattern.compile("[«\"](.*?)[»\"]");
        Matcher quoteMatcher = quotePattern.matcher(text);
        while (quoteMatcher.find()) {
            final int start = quoteMatcher.start();
            final int end = quoteMatcher.end();

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    // открываем ссылку
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"));
                    widget.getContext().startActivity(browserIntent);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.BLUE);   // цвет текста
                    ds.setUnderlineText(true); // подчёркивание
                }
            };
            ssb.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return ssb;
    }

    private void downloadPhotoInfoById(String stackPhotoDBID, Clicks.clickObjectAndStatus<StackPhotoDB> clickUpdatePhoto) {
        PhotoDownload photoDownloader = new PhotoDownload();

        PhotoTableRequest request = new PhotoTableRequest();
        request.mod = "images_view";
        request.act = "list_image";
        request.nolimit = "1";
        request.id_list = stackPhotoDBID;

        photoDownloader.getPhotoInfoAndSaveItToDB(request, clickUpdatePhoto);
    }

    private void test() {

        Log.e("!!!!!!!!!!!!!!","USER: " + Globals.userId);


        new Translate().uploadNewTranslate();

        SamplePhotoExchange samplePhotoExchange = new SamplePhotoExchange();
//        samplePhotoExchange.downloadSamplePhotoTable(new Clicks.clickObjectAndStatus() {
//            @Override
//            public void onSuccess(Object data) {
//                Globals.writeToMLOG("INFO", "Exchange/SamplePhotoExchange()/onSuccess", "+");
//
////                            List<SamplePhotoSDB> listPhotosToDownload = (List<SamplePhotoSDB>) data;
////                            Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/samplePhotoExchange/onSuccess", "Загрузка ОБРАЗЦОВ ФОТО res: " + res.size());
//
//                try {
//                    RealmManager.INSTANCE.executeTransaction(realm -> {
//                        if (samplePhotoExchange.synchronizationTimetableDB != null) {
//                            samplePhotoExchange.synchronizationTimetableDB.setVpi_app(System.currentTimeMillis() / 1000);
//                            realm.copyToRealmOrUpdate(samplePhotoExchange.synchronizationTimetableDB);
//                        }
//                    });
//                } catch (Exception e) {
//                    Globals.writeToMLOG("ERROR", "SamplePhotoExchange/downloadSamplePhotoTable/onResponse/onComplete/synchronizationTimetableDB", "Exception e: " + e);
//                }
//
//                try {
////                                SamplePhotoExchange samplePhotoExchange = new SamplePhotoExchange();
//                    List<Integer> listPhotosToDownload = samplePhotoExchange.getSamplePhotosToDownload();
//
//                    if (listPhotosToDownload != null && listPhotosToDownload.size() > 0) {
////                                            photoCount = listPhotosToDownload.size();
//                        Log.i("````", "listPhotosToDownload: " + listPhotosToDownload.size());
//
//                        samplePhotoExchange.downloadSamplePhotosByPhotoIds(listPhotosToDownload, new Clicks.clickStatusMsg() {
//                            @Override
//                            public void onSuccess(String data) {
//                                Toast.makeText(MenuMainActivity.this, data, Toast.LENGTH_LONG).show();
//                            }
//
//                            @Override
//                            public void onFailure(String error) {
//
//                            }
//                        });
//                    } else {
//                        TablesLoadingUnloading.readySamplePhotos = true;
//                        Log.i("````", "....1");
//                    }
//                } catch (Exception e) {
//                    TablesLoadingUnloading.readySamplePhotos = true;
//                    Log.e("````", "err", e);
//                }
//            }
//
//            @Override
//            public void onFailure(String error) {
//                Globals.writeToMLOG("ERROR", "Exchange/SamplePhotoExchange()/onFailure", "error: " + error);
//            }
//        });

//        AppToaster.INSTANCE.show("Test", AppToaster.Style.SUCCESS, AppToaster.Length.LONG);


//       Toasty.success(this,
//                       getFormattedMessage(), Toast.LENGTH_LONG, false)
//               .show();


//       Toasty.normal(this,"Test").show();


//        TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading();
//        tablesLoadingUnloading.downloadWPDataWithCords();
//        tablesLoadingUnloading.donwloadPlanBudgetRNO();
//        tablesLoadingUnloading.downloadWPDataWithCordsMy();
//        tablesLoadingUnloading.donwloadPlanBudget();

//
//        WorkManagerHelper.INSTANCE.startSyncWorker(this);

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

    public void downloadWPData() {

        String mod = "plan";
        String act = "list";
        String date_from = Clock.getDatePeriod(-21);
//        String date_from = timeYesterday7;
        String date_to =  Clock.getDatePeriod(5);
        long vpi;

        SynchronizationTimetableDB sTable = RealmManager.getSynchronizationTimetableRowByTable("wp_data");
        if (sTable != null) {
            Globals.writeToMLOG("INFO", "TablesLoadingUnloading/downloadWPData/getSynchronizationTimetableRowByTable", "sTable: " + sTable);
            vpi = sTable.getVpi_app();
            Log.e("updateWpData", "vpi: " + vpi);
        } else
            vpi = 0;
//        vpi = 1758173674;
        vpi = 0;
        try {
            Log.e("TAG_TEST_WP", "RESPONSE_0 T");
            Globals.writeToMLOG("INFO", "TablesLoadingUnloading/downloadWPData", "vpi: " + vpi);

            Call<WpDataServer> call = RetrofitBuilder.getRetrofitInterface().GET_WPDATA_VPI(mod, act, date_from, date_to, vpi);
            call.enqueue(new Callback<WpDataServer>() {
                @Override
                public void onResponse(Call<WpDataServer> call, Response<WpDataServer> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {

//                            downloadWPDataWithCords();
                            if (response.body().getState() && response.body().getList() != null
                                    && !response.body().getList().isEmpty()) {
                                List<WpDataDB> wpDataDBList = response.body().getList();
                                Globals.writeToMLOG("INFO", "TablesLoadingUnloading/downloadWPData/onResponse", "wpDataDBList.size(): " + wpDataDBList.size());
//                            RealmManager.setWpDataAuto2(wpDataDBList);
//                            RealmManager.setWpData(wpDataDBList);

                                HashSet<String> clientName = new HashSet<>();
                                for (WpDataDB wpDataDB: wpDataDBList){
                                    if (wpDataDB.getClient_txt().contains("ОТС"))
                                        Log.e("!!!!!!!!","+++");
                                    clientName.add(wpDataDB.getClient_txt());

                                }
                                if (wpDataDBList.isEmpty()) {

                                    return;
                                }
                                RealmManager.updateWorkPlanFromServer(wpDataDBList);
                                INSTANCE.executeTransaction(realm -> {
                                    if (sTable != null) {
                                        sTable.setVpi_app((System.currentTimeMillis() / 1000) - 60);
                                        realm.copyToRealmOrUpdate(sTable);
                                    }
                                });

                            }
                        }
                    } catch (Exception e) {
                        Log.e("Exception","Error : " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<WpDataServer> call, Throwable t) {
                    Log.e("Exception","Error : " + t.getMessage());

                }
            });
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "TablesLoadingUnloading/downloadWPData", "Exception: " + e.getMessage());

        }

        Log.e("SERVER_REALM_DB_UPDATE", "===================================downloadWPData_END");

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
