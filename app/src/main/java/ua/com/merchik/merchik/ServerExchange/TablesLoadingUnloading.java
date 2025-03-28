package ua.com.merchik.merchik.ServerExchange;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.database.realm.RealmManager.getAllWorkPlan;
import static ua.com.merchik.merchik.database.realm.RealmManager.getSynchronizationTimetable;
import static ua.com.merchik.merchik.database.realm.tables.PPARealm.setPPA;
import static ua.com.merchik.merchik.database.realm.tables.WpDataRealm.getWpDataAddresses;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.ReclamationPointExchange;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHints;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjects;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.PPAonResponse;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.MenuItemFromWebDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.UsersDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.AddressTableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.ArticleTableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.CustomerGroups;
import ua.com.merchik.merchik.data.RetrofitResponse.CustomerTableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.CustomerTableResponseList;
import ua.com.merchik.merchik.data.RetrofitResponse.ErrorTableResponce;
import ua.com.merchik.merchik.data.RetrofitResponse.ImageTypes;
import ua.com.merchik.merchik.data.RetrofitResponse.OptionsServer;
import ua.com.merchik.merchik.data.RetrofitResponse.PPATableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.PromoTableResponce;
import ua.com.merchik.merchik.data.RetrofitResponse.ReportHint;
import ua.com.merchik.merchik.data.RetrofitResponse.ReportPrepareServer;
import ua.com.merchik.merchik.data.RetrofitResponse.SotrTable;
import ua.com.merchik.merchik.data.RetrofitResponse.SotrTableList;
import ua.com.merchik.merchik.data.RetrofitResponse.TARCommentsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.TasksAndReclamationsResponce;
import ua.com.merchik.merchik.data.RetrofitResponse.ThemeTableRespose;
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgList;
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.TovarTableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.TradeMarkResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.WpDataServer;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.OborotVedResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.OpinionResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.OpinionThemeResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ReportPrepare.ReportPrepareUploadList;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ReportPrepare.ReportPrepareUploadResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.TovarGroupClientResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.TovarGroupResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.update.reportprepare.ReportPrepareUpdateResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.update.reportprepare.ReportPrepareUpdateResponseList;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirements.AdditionalRequirementsServerData;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirementsMarksServerData;
import ua.com.merchik.merchik.data.TestJsonUpload.PPARequest;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.data.TestJsonUpload.TasksAndReclamationsRequest;
import ua.com.merchik.merchik.data.UploadToServ.LogUploadToServ;
import ua.com.merchik.merchik.data.UploadToServ.ReportPrepareServ;
import ua.com.merchik.merchik.data.UploadToServ.WpDataUploadToServ;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TARCommentsRealm;
import ua.com.merchik.merchik.database.realm.tables.TasksAndReclamationsRealm;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;
import ua.com.merchik.merchik.dialogs.BlockingProgressDialog;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;


/**
 * 26/02/2021
 * Начинаю считать этот класс устаревающим.
 * Надо переносить с него обмены в Exchange и архитектурить Там уже нормально
 */
/**MERCHIK_1
 * НА 99% працює із базою данних РЕАЛМ*/
public class TablesLoadingUnloading {

    private String timeYesterday7 = Clock.today_7;
    private String timeYesterday = Clock.yesterday;
    private String timeToday = Clock.today;
    //    private String timeTomorrow = Clock.tomorrow;
    private String timeTomorrow = Clock.getDatePeriod(5);
    private String timeTomorrow7 = Clock.tomorrow7;

//        private String timeYesterday = "2021-09-15";
//    private String timeTomorrow   = "2021-09-15";

    private Globals globals = new Globals();

    public static boolean sync = false;
    public static boolean syncInternetError = false;
    public static boolean readyWPData = false;
    public static boolean readyImagesTp = false;
    public static boolean readyTypeGrp = false;
    public static boolean readyOptions = false;
    public static boolean readyReportPrepare = false;
    public static boolean readyCustomerTable = false;
    public static boolean readyAddressTable = false;
    public static boolean readySotrTable = false;
    public static boolean readyTovarTable = false;
    public static boolean readyErrorTable = false;
    public static boolean readyAkciyTable = false;
    public static boolean readyTradeMarksTable = false;

    public static boolean readySamplePhotos = false;


    /**
     * 18.08.2020
     * <p>
     * Загрузка старым методом всех таблиц
     */
    public void downloadAllTables(Context context) {
        sync = true;

        try {
            Exchange.sendWpData2();
            updateWpData();

            downloadWPData();

            downloadOptions();
            Log.e("uploadRP", "start");
            uploadRP(new ExchangeInterface.ExchangeResponseInterface() {
                @Override
                public <T> void onSuccess(List<T> data) {
                    Log.e("uploadRP", "onSuccess data: " + data);
                    if (data != null) {
                        Log.e("uploadRP", "onSuccess: " + data.size());

                        if (data != null) {
                            List<ReportPrepareUpdateResponseList> list = (List<ReportPrepareUpdateResponseList>) data;

                            Long[] ids = new Long[list.size()];
                            int count = 0;
                            for (ReportPrepareUpdateResponseList item : list) {
                                ids[count++] = item.elementId;
                            }

                            List<ReportPrepareDB> rp = INSTANCE.copyFromRealm(ReportPrepareRealm.getByIds(ids));

                            for (ReportPrepareDB item : rp) {
                                for (ReportPrepareUpdateResponseList listItem : list) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        if (Objects.equals(listItem.elementId, item.getID())) {
                                            if (listItem.state) {
                                                item.setUploadStatus(0);
                                                ReportPrepareRealm.setAll(Collections.singletonList(item));
                                            }
                                        }
                                    }
                                }
                            }

                            //  TODO Вернул загрузку RP
                            downloadReportPrepare(0);
                        }

                    } else {
                        downloadReportPrepare(0);
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.d("uploadRP", "error: " + error);
                    downloadReportPrepare(0);
                }
            });
//            downloadTovarTable(context, null);
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLoadingUnloading.class.downloadAllTables.Успех.Обязательные таблици." + "\n");
        } catch (Exception e) {
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLoadingUnloading.class.downloadAllTables.Ошибка.Обязательные таблици: " + e + "\n");
        }


        try {
            downloadImagesTp();
            downloadTypeGrp();

            downloadCustomerTable();
            downloadAddressTable();
            downloadSotrTable();

            downloadErrorTable();
            downloadAkciyTable();
            downloadTradeMarksTable();
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLoadingUnloading.class.downloadAllTables.Успех.Не обязательные таблици." + "\n");
        } catch (Exception e) {
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLoadingUnloading.class.downloadAllTables.Ошибка.Не обязательные таблици: " + e + "\n");
        }


        try {
            downloadPPA(); // Загрузка ППА по нажатию на синхронизацию
//            downloadTAR(); // Загрузка Затач и Рекламаций

            downloadTARComments();  // Загрузка таблици Переписки (Комментарии к Рекламациям)
            downloadTheme();    // Загрузка таблички Тем

            downloadAdditionalRequirements();   // Загрузка таблички Доп Требований

            downloadAdditionalRequirementsMarks();    // Загрузка таблички Оценок Доп Требований


            downloadOpinions();
            downloadOpinions2();

            downloadOborotVed();

            downloadtovar_grp_client();


            downloadTovarGroupTable(new ExchangeInterface.ExchangeResponseInterface() {
                @Override
                public <T> void onSuccess(List<T> data) {
                    List<TovarGroupSDB> list = (List<TovarGroupSDB>) data;

                    try {
                        Globals.writeToMLOG("INFO", "downloadTovarGroupTable.onSuccess", "list: " + list.size());
                    }catch (Exception e){
                        Globals.writeToMLOG("ERROR", "downloadTovarGroupTable.onSuccess", "list Exception e: " + e);
                    }

                    SQL_DB.tovarGroupDao().insertData(list)
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    Globals.writeToMLOG("INFO", "downloadTovarGroupTable.onComplete", "OK");
                                }

                                @Override
                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                    Globals.writeToMLOG("ERROR", "downloadTovarGroupTable.onError", "Throwable e: " + e);
                                }
                            });
                }

                @Override
                public void onFailure(String error) {
                    Log.e("TablesLoadUpload", "downloadTovarGroupTable.onFailure: " + error);
                    Globals.writeToMLOG("FAIL", "downloadTovarGroupTable.onFailure", "onFailure: " + error);
                }
            }); // Загрузка таблици. ОСНОВА. Группы Товаров.

            // Загрузка Задач и Рекламаций
            try {
                ReclamationPointExchange tarExchange = new ReclamationPointExchange();
                tarExchange.downloadTaR(new ExchangeInterface.ExchangeResponseInterface() {
                    @Override
                    public <T> void onSuccess(List<T> data) {
                        SQL_DB.tarDao().insertData((List<TasksAndReclamationsSDB>) data)
                                .subscribeOn(Schedulers.io())
                                .subscribe(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                        Globals.writeToMLOG("INFO", "Exchange.ReclamationPointExchange/downloadTaR.onComplete", "Успешно сохранило Задачи и Рекламации (" + data.size() + ")шт в БД");
                                    }

                                    @Override
                                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                        Globals.writeToMLOG("INFO_ERR", "Exchange.ReclamationPointExchange/downloadTaR.onError", "Ошибка при сохранении в БД: " + e);
                                    }
                                });
                    }

                    @Override
                    public void onFailure(String error) {
                        Globals.writeToMLOG("INFO_ERR", "Exchange.ReclamationPointExchange/downloadTaR.onFailure", "String error: " + error);
                    }
                });
            } catch (Exception e) {
            }


            /*Загрузка подсказок, Вообще надо нормально прописать время их синхронизации*/
//            downloadSiteHints("2");
//            downloadVideoLessons();

            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLoadingUnloading.class.downloadAllTables.Успех.Новые таблици." + "\n");
        } catch (Exception e) {
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLoadingUnloading.class.downloadAllTables.Ошибка.Новые таблици: " + e + "\n");
        }


        globals.testMSG(context);
    }


    /**
     * 18.08,2020
     * <p>
     * Выгрузка таблиц
     */
    public void uploadAllTables(Context context) {
        try {
            uploadReportPrepareToServer();


            updateWpData();
        } catch (Exception e) {
            // Запись в лог
        }

    }


    public void downloadWPData() {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================downloadWPData_START");

        String mod = "plan";
        String act = "list";
        String date_from = Clock.getDatePeriod(-21);
//        String date_from = timeYesterday7;
        String date_to = timeTomorrow;

//        BlockingProgressDialog pg = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: " + "План работ");

//        try {
//            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().GET_WPDATA_VPI_JSON(mod, act, date_from, date_to, 0);
//            call.enqueue(new retrofit2.Callback<JsonObject>() {
//                @Override
//                public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
//                    Log.e("TAG_TEST_WP", "RESPONSE_JSON: " + response.body());
//                }
//
//                @Override
//                public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
//                }
//            });
//        } catch (Exception e) {
//        }

        try {
            Log.e("TAG_TEST_WP", "RESPONSE_0 T");
            retrofit2.Call<WpDataServer> call = RetrofitBuilder.getRetrofitInterface().wpData(mod, act, date_from, date_to);
            call.enqueue(new retrofit2.Callback<WpDataServer>() {
                @Override
                public void onResponse(retrofit2.Call<WpDataServer> call, retrofit2.Response<WpDataServer> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.e("SERVER_REALM_DB_UPDATE", "===================================downloadWPData_:" + response.body().getState() + "/" + response.body().getError());
                        if (response.body().getList() != null) {
                            Log.e("SERVER_REALM_DB_UPDATE", "===================================WPDataSIZE: " + response.body().getList().size());
                        } else {
                            Log.e("SERVER_REALM_DB_UPDATE", "===================================WPDataSIZE: NuLL");
                        }

                        if (response.body().getState()) {
                            List<WpDataDB> wpDataDBList = response.body().getList();
                            RealmManager.setWpData(wpDataDBList);

                            downloadTovarTable(null,wpDataDBList);
//                            Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/downloadWPData/onSuccess", "(response.body().getList(): " + response.body().getList().size());
//                            if (RealmManager.setWpData(response.body().getList())) {
//                                if (pg != null)
//                                    if (pg.isShowing())
//                                        pg.dismiss();
//                            } else {
//                                if (pg != null)
//                                    if (pg.isShowing())
//                                        pg.dismiss();
//                            }

//                            long currentTime = System.currentTimeMillis() / 1000;

                        }
//                        else {
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();
//                        }
                    }
                    readyWPData = true;
                }

                @Override
                public void onFailure(retrofit2.Call<WpDataServer> call, Throwable t) {
//                    if (pg != null)
//                        if (pg.isShowing())
//                            pg.dismiss();
                    readyWPData = false;
                    syncInternetError = true;
                }
            });
        } catch (Exception e) {
            readyWPData = true;
        }

        Log.e("SERVER_REALM_DB_UPDATE", "===================================downloadWPData_END");
    }


    public void downloadImagesTp() {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================downloadImagesTp_START");
        String mod = "filter_list";
        String act = "menu_list";
        String images_type_list = "";

//        BlockingProgressDialog pg = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: Тип фото");

        retrofit2.Call<ImageTypes> call = RetrofitBuilder.getRetrofitInterface().IMAGE_TYPES_CALL(mod, act, images_type_list);
        call.enqueue(new retrofit2.Callback<ImageTypes>() {
            @Override
            public void onResponse(retrofit2.Call<ImageTypes> call, retrofit2.Response<ImageTypes> response) {
                Log.e("TAG_TEST", "RESPONSE_1" + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getMenuList() != null && response.body().getMenuList().getImagesTypeList() != null) {


                        if (response.body().getMenuList().getImagesTypeList() != null) {
                            Log.e("SERVER_REALM_DB_UPDATE", "===================================ImagesTpSIZE: " + response.body().getMenuList().getImagesTypeList().size());
                        } else {
                            Log.e("SERVER_REALM_DB_UPDATE", "===================================ImagesTpSIZE: NuLL");
                        }

                        RealmManager.setImagesTp(response.body().getMenuList().getImagesTypeList());
//                        if (RealmManager.setImagesTp(response.body().getMenuList().getImagesTypeList())) {
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();
//                        } else {
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();
//                        }

//                        long currentTime = System.currentTimeMillis() / 1000;
//                        RealmManager.setToSynchronizationTimetableDB(new SynchronizationTimetableDB(2, "image_tp", 36000, currentTime, currentTime, 0, 0));

                    }
//                    else {
//                        Toast.makeText(context, "Типы фото обновить не получилось. Повторите попытку позже.", Toast.LENGTH_SHORT).show();
//                        if (pg != null)
//                            if (pg.isShowing())
//                                pg.dismiss();
//                    }
                }
                readyImagesTp = true;
            }

            @Override
            public void onFailure(retrofit2.Call<ImageTypes> call, Throwable t) {
//                if (pg != null)
//                    if (pg.isShowing())
//                        pg.dismiss();
                readyImagesTp = false;
                syncInternetError = true;
                Log.e("TAG_TEST", "FAILURE_1 E: " + t.getMessage());
            }
        });

        Log.e("SERVER_REALM_DB_UPDATE", "===================================downloadImagesTp_END");
    }


    public void downloadTypeGrp() {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadTypeGrp.START");


        Log.e("TAG_TEST_S", "RESPONSE_2" + "ЗАШОЛ)))");
        String mod = "data_list";
        String act = "client_group_list_plain";
//        String act = "client_group_list";

//        BlockingProgressDialog pg = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: Групы товаров");

        retrofit2.Call<CustomerGroups> call = RetrofitBuilder.getRetrofitInterface().GROUP_TYPE(mod, act);
        call.enqueue(new retrofit2.Callback<CustomerGroups>() {
            @Override
            public void onResponse(retrofit2.Call<CustomerGroups> call, retrofit2.Response<CustomerGroups> response) {
                Log.e("TAG_TEST", "RESPONSE_2: " + response.body());
                try {
                    if (response.isSuccessful() && response.body().getState()) {


                        if (response.body().getList() != null) {
                            Log.e("SERVER_REALM_DB_UPDATE", "===================================.TypeGrp.SIZE: " + response.body().getList().size());
                        } else {
                            Log.e("SERVER_REALM_DB_UPDATE", "===================================.TypeGrp.SIZE: NuLL");
                        }

                        RealmManager.setGroupTypeV2(response.body().getList());
//                        if (RealmManager.setGroupTypeV2(response.body().getList())) {
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();
//                        } else {
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();
//                        }

//                        RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Группы товаров с сервера: " + response.body().getList(), 1097, null, null, null, Globals.userId, null, Globals.session, null)));

                        long currentTime = System.currentTimeMillis() / 1000;
//                        RealmManager.setToSynchronizationTimetableDB(new SynchronizationTimetableDB(3, "client_group_tp", 600, currentTime, currentTime, 0, 0));

                    }
                } catch (Exception e) {
//                    RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Ошибка при обмене групп товаров(сервер что-то отправил): " + e, 1097, null, null, null, Globals.userId, null, Globals.session, null)));
                }
                readyTypeGrp = true;
            }

            @Override
            public void onFailure(retrofit2.Call<CustomerGroups> call, Throwable t) {
//                if (pg != null)
//                    if (pg.isShowing())
//                        pg.dismiss();
                readyTypeGrp = false;
                syncInternetError = true;
//                RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Ошибка при обмене групп товаров(ошика интернета): " + t, 1097, null, null, null, Globals.userId, null, Globals.session, null)));
                Log.e("TAG_TEST", "FAILURE_2 E: " + t);
            }
        });

        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadTypeGrp.END");
    }

    public void downloadOptionsByDAD2(long dad2, Clicks.click click) {
        StandartData data = new StandartData();
        data.mod = "plan";
        data.act = "options_list";
        data.code_dad2 = String.valueOf(dad2);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("INFO", "downloadOptionsByDAD2/convertedObject", "convertedObject: " + convertedObject);

        retrofit2.Call<OptionsServer> call = RetrofitBuilder.getRetrofitInterface().GET_OPTIONS(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<OptionsServer>() {
            @Override
            public void onResponse(Call<OptionsServer> call, Response<OptionsServer> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().getState()) {
                            RealmManager.saveDownloadedOptions(response.body().getList());
                            click.click("Данные успешно загружены и сохранены. (" + response.body().getList().size() + ")шт. Опций.");
                        } else {
                            click.click("Обновить данные не получилось. Обратитесь к своему руководителю.");
                        }
                    } else {
                        click.click("Ошибка. При повторении обратитесь с ней к своему руководителю. Код запроса: " + response.code());
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "downloadOptionsByDAD2/onResponse", "Exception e: " + e);
                    click.click("Ошибка при обработке данных: " + e);
                }
            }

            @Override
            public void onFailure(Call<OptionsServer> call, Throwable t) {
                Globals.writeToMLOG("ERROR", "downloadOptionsByDAD2/onFailure", "Throwable t: " + t);
                click.click("Ошибка связи. Повторите попытку позже. При повторении проблемы - обратитесь к своему руководителю. Ошибка: " + t);
            }
        });


    }

    public void downloadOptions() {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadOptions.START");
        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLU.class.downloadOptions.ENTER\n");

        String mod = "plan";
        String act = "options_list";
        String date_from = timeYesterday7;
//        String date_from = Clock.getDatePeriod(-14);
        String date_to = timeTomorrow;

//        BlockingProgressDialog pg = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: Опции");

        retrofit2.Call<OptionsServer> call = RetrofitBuilder.getRetrofitInterface().OPTIONS_CALL(mod, act, date_from, date_to);
        call.enqueue(new retrofit2.Callback<OptionsServer>() {
            @Override
            public void onResponse(retrofit2.Call<OptionsServer> call, retrofit2.Response<OptionsServer> response) {
                globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLU.class.downloadOptions.onResponse.ENTER\n");
                Log.e("TAG_TEST", "RESPONSE_3");
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("SERVER_REALM_DB_UPDATE", "===================================downloadOptions_:" + response.body().getState() + "/" + response.body().getError());

                    try {
                        try {
                            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLU.class.downloadOptions.размер ответа: " + response.body().getList().size() + "\n");
                        } catch (Exception e) {
                            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLU.class.downloadOptions.ответ от сервера.ERROR1: " + e + "\n");
                        }
                    } catch (Exception e) {
                        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLU.class.downloadOptions.ответ от сервера.ERROR: " + e + "\n");
                    }


                    if (response.body().getList() != null) {
                        Log.e("SERVER_REALM_DB_UPDATE", "===================================.Options.SIZE: " + response.body().getList().size());
//                        Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/downloadOptions/onSuccess", "(response.body().getList(): " + response.body().getList().size());
                    } else {
                        Log.e("SERVER_REALM_DB_UPDATE", "===================================.Options.SIZE: NuLL");
                    }

                    RealmManager.setOptions(response.body().getList());
//                    if (response.body().getState()) {
//                        if (RealmManager.setOptions(response.body().getList())) {
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();
//                        } else {
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();
//
//                        }
//                    } else {
//                        if (pg != null)
//                            if (pg.isShowing())
//                                pg.dismiss();
//
//                    }

                }
                readyOptions = true;
            }

            @Override
            public void onFailure(retrofit2.Call<OptionsServer> call, Throwable t) {
                globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLU.class.downloadOptions.onFailure.ENTER\n");
//                if (pg != null)
//                    if (pg.isShowing())
//                        pg.dismiss();
                readyOptions = false;
                syncInternetError = true;
                Log.e("TAG_TEST", "FAILURE_3 E: " + t);
            }
        });
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadOptions.END");

    }


    /**
     * ЗАГРУЗКА ТАБЛИЦИ report_prepare С СЕРВЕРА
     *
     * @param context -- Контекст где будет отображаться окно прогресса загрузки таблици
     * @param mode    -- Режим работы. Если 0 - всё затераем, 1 - "умная" загрузка(обновление)
     */
    public void downloadReportPrepare(int mode) {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadReportPrepare.START");

        String mod = "report_prepare";
        String act = "list_data";
        String date_from = timeYesterday7;
        String date_to = timeTomorrow;

        // Получение значения ВПО (время последнего обмена) для того что б с сервера отправились новые данные
        long lastUpdate = 0;
        SynchronizationTimetableDB realmResults = RealmManager.getSynchronizationTimetableRowByTable("report_prepare");
        if (realmResults != null) lastUpdate = realmResults.getVpo_export();

        // 22.08.23. Получаю с Плана Работ список Адресов так что б они не повторялись.
        List<Integer> addressIds = getWpDataAddresses();

        Log.e("downloadReportPrepare", "addressIds size: " + addressIds.size());
        Log.e("downloadReportPrepare", "addressIds: " + addressIds);

        // 22.08.23. Перепиал запрос на получение RP
        StandartData data = new StandartData();
        data.mod = "report_prepare";
        data.act = "list_data";
        data.date_from = date_from;
        data.date_to = date_to;
        if (lastUpdate != 0) data.vpo = lastUpdate;
        if (addressIds != null && addressIds.size() > 0) data.addr_id = addressIds;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<ReportPrepareServer> call = RetrofitBuilder.getRetrofitInterface().ReportPrepareServer_RESPONSE(RetrofitBuilder.contentType, convertedObject);
//        retrofit2.Call<JsonObject> callT = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);

//        retrofit2.Call<ReportPrepareServer> call;
//        if (lastUpdate == 0) {
//            call = RetrofitBuilder.getRetrofitInterface().REPORT_PREPARE_CALL_ALL(mod, act, date_from, date_to);
//        } else {
//            call = RetrofitBuilder.getRetrofitInterface().REPORT_PREPARE_CALL_PIECE(mod, act, date_from, date_to, lastUpdate);
//        }

//        BlockingProgressDialog pg = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: Дет. отчёт");

//        callT.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                Log.e("TAG_TEST_callT", "response: " + response);
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.e("TAG_TEST_callT", "Throwable t: " + t);
//            }
//        });


        call.enqueue(new retrofit2.Callback<ReportPrepareServer>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<ReportPrepareServer> call, @NonNull retrofit2.Response<ReportPrepareServer> response) {
                Log.e("TAG_TEST", "RESPONSE_4");
                Log.e("downloadReportPrepare", "downloadReportPrepare.response: " + response);

                Globals.writeToMLOG("INFO", "downloadReportPrepare/onResponse", "response.isSuccessful(): " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getState()) {

                        Globals.writeToMLOG("INFO", "downloadReportPrepare/onResponse", "response.body().getState(): " + response.body().getState());

                        if (response.body().getList() != null) {
//                            Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/downloadReportPrepare/onSuccess", "(response.body().getList(): " + response.body().getList().size());
                            Log.e("SERVER_REALM_DB_UPDATE", "===================================.ReportPrepare.SIZE: " + response.body().getList().size());
                            Globals.writeToMLOG("INFO", "downloadReportPrepare/onResponse", "response.body().getList().size(): " + response.body().getList().size());
                        } else {
                            Log.e("SERVER_REALM_DB_UPDATE", "===================================.ReportPrepare.SIZE: NuLL");
                        }
                        RealmManager.setReportPrepare(response.body().getList());

//                        if (RealmManager.setReportPrepare(response.body().getList())) {
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();
//                        } else {
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();
//
//                        }
                    }
//                    else {
//                        if (pg != null)
//                            if (pg.isShowing())
//                                pg.dismiss();
//
//                    }
                }
                readyReportPrepare = true;
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<ReportPrepareServer> call, @NonNull Throwable t) {
//                if (pg != null)
//                    if (pg.isShowing())
//                        pg.dismiss();
                readyReportPrepare = false;
                syncInternetError = true;
                Log.e("TAG_TEST", "FAILURE_4 E: " + t);
                Globals.writeToMLOG("ERR", "downloadReportPrepare/onFailure", "Throwable t: " + t);
            }
        });
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadReportPrepare.END");

    }


    /**
     * Обновление таблицы Клиентов
     */
    public void downloadCustomerTable() {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadCustomerTable.START");


        String mod = "data_list";
        String act = "client_list";

//        RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Обновление таблицы Клиенты", 1094, null, null, null, null, null, Globals.session, null)));

//        BlockingProgressDialog pg = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: Клиенты");

        retrofit2.Call<CustomerTableResponse> call = RetrofitBuilder.getRetrofitInterface().GET_CUSTOMER_T(mod, act);
        call.enqueue(new retrofit2.Callback<CustomerTableResponse>() {
            @Override
            public void onResponse(retrofit2.Call<CustomerTableResponse> call, retrofit2.Response<CustomerTableResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSECustomerTable: " + response.body());

                    if (response.body().getState()) {
                        if (!response.body().getList().isEmpty()) {
                            Log.e("TAG_TABLE", "SiteHintsDB: 200");

                            ArrayList<CustomerDB> list = new ArrayList<CustomerDB>();
                            List<CustomerTableResponseList> responseList = response.body().getList();

                            for (int i = 0; i < responseList.size(); i++) {
                                list.add(i, new CustomerDB(responseList.get(i).getClientId(), responseList.get(i).getNm(), null, null, responseList.get(i).getEdrpou()));
                            }


                            if (list != null) {
                                Log.e("SERVER_REALM_DB_UPDATE", "===================================.CustomerTable.SIZE: " + list.size());
                            } else {
                                Log.e("SERVER_REALM_DB_UPDATE", "===================================.CustomerTable.SIZE: NuLL");
                            }


                            // Запись в БД
                            RealmManager.setRowToCustomer(list);
//                            if (RealmManager.setRowToCustomer(list)) {
//                                if (pg != null)
//                                    if (pg.isShowing())
//                                        pg.dismiss();``
//                            } else {
//                                if (pg != null)
//                                    if (pg.isShowing())
//                                        pg.dismiss();
//                            }

                        }
                    }
//                    else {
//                        if (pg != null)
//                            if (pg.isShowing())
//                                pg.dismiss();
//
//                    }
                }
                readyCustomerTable = true;
            }

            @Override
            public void onFailure(retrofit2.Call<CustomerTableResponse> call, Throwable t) {
//                if (pg != null)
//                    if (pg.isShowing())
//                        pg.dismiss();

                readyCustomerTable = false;
                syncInternetError = true;
//                RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Ошибка при обмене т.Клиенты: " + t, 1094, null, null, null, null, null, Globals.session, null)));
                Log.e("TAG_TABLE", "FAILURECustomerTable: " + t);
            }
        });

        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadCustomerTable.END");

    }


    /**
     * Обновление таблицы Адресов
     */
    public void downloadAddressTable() {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadAddressTable.START");

        String mod = "data_list";
        String act = "addr_list";

//        BlockingProgressDialog pg = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: Адреса");

        retrofit2.Call<AddressTableResponse> call = RetrofitBuilder.getRetrofitInterface().GET_ADDRESS_T(mod, act);
        call.enqueue(new retrofit2.Callback<AddressTableResponse>() {
            @Override
            public void onResponse(retrofit2.Call<AddressTableResponse> call, retrofit2.Response<AddressTableResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSEAddressTable: " + response.body());
                    if (response.body().getState()) {
                        if (!response.body().getList().isEmpty()) {
                            // Запись в БД
                            RealmManager.setRowToAddress(response.body().getList());
//                            if (RealmManager.setRowToAddress(response.body().getList())) {
//                                if (pg != null)
//                                    if (pg.isShowing())
//                                        pg.dismiss();
//                            } else {
//                                if (pg != null)
//                                    if (pg.isShowing())
//                                        pg.dismiss();
//
//                            }
                        }
//                        else {
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();
//
//                        }
                    }
//                    else {
//                        if (pg != null)
//                            if (pg.isShowing())
//                                pg.dismiss();
//
//                    }
                }
                readyAddressTable = true;
            }

            @Override
            public void onFailure(retrofit2.Call<AddressTableResponse> call, Throwable t) {
//                if (pg != null)
//                    if (pg.isShowing())
//                        pg.dismiss();
                readyAddressTable = false;
                syncInternetError = true;
                Log.e("TAG_TABLE", "FAILUREAddressTable: " + t);
            }
        });
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadAddressTable.END");

    }


    /**
     * Обновление таблицы Сотрудники
     */
    public void downloadSotrTable() {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadSotrTable.START");

        String mod = "data_list";
        String act = "sotr_list";

//        RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Обмен таблицы Сотрудники", 1095, null, null, null, null, null, Globals.session, null)));

//        BlockingProgressDialog pg = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: Сотрудники");

        retrofit2.Call<SotrTable> call = RetrofitBuilder.getRetrofitInterface().GET_SOTR_T(mod, act);
        call.enqueue(new retrofit2.Callback<SotrTable>() {
            @Override
            public void onResponse(retrofit2.Call<SotrTable> call, retrofit2.Response<SotrTable> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSESotrTable: " + response.body());
                    if (response.body().getState()) {
//                        RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Обмен таблицы Сотрудники. Успех.", 1095, null, null, null, null, null, Globals.session, null)));
                        if (!response.body().getList().isEmpty()) {
                            Log.e("TAG_TABLE", "ListS: 200");

                            ArrayList<UsersDB> list = new ArrayList<UsersDB>();
                            List<SotrTableList> responseList = response.body().getList();

                            for (int i = 0; i < responseList.size(); i++) {
                                list.add(i, new UsersDB(
                                        responseList.get(i).getUser_id(),
                                        responseList.get(i).getFio(),
                                        null,
                                        null,
                                        Integer.parseInt(responseList.get(i).getCityId()),
                                        responseList.get(i).getInn(),
                                        Integer.parseInt(responseList.get(i).getWorkAddrId()),
                                        responseList.get(i).getClientId()
                                ));
//                                Log.e("TAG_TABLE", "ListUSERS: " + list.get(i));
                            }


                            if (list != null) {
                                Log.e("SERVER_REALM_DB_UPDATE", "===================================.SotrTable.SIZE: " + list.size());
                            } else {
                                Log.e("SERVER_REALM_DB_UPDATE", "===================================.SotrTable.SIZE: NuLL");
                            }


                            // Запись в БД
                            RealmManager.setRowToUsers(list);
//                            if (RealmManager.setRowToUsers(list)) {
//                                if (pg != null)
//                                    if (pg.isShowing())
//                                        pg.dismiss();
//                            } else {
//                                if (pg != null)
//                                    if (pg.isShowing())
//                                        pg.dismiss();
//                            }
                        }
                        else {
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();

//                            RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Обмен таблицы Сотрудники. Сотрудники пустые.", 1095, null, null, null, null, null, Globals.session, null)));
                            Log.e("TAG_TABLE", "ListS: empty");
                        }
                    }
                    else {
//                        if (pg != null)
//                            if (pg.isShowing())
//                                pg.dismiss();

//                        RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Обмен таблицы Сотрудники. Ошибка от Вовы: ", 1095, null, null, null, null, null, Globals.session, null)));
                        Log.e("TAG_TABLE", "ListS: ERROR");
                    }
                }
                readySotrTable = true;
            }

            @Override
            public void onFailure(retrofit2.Call<SotrTable> call, Throwable t) {
//                if (pg != null)
//                    if (pg.isShowing())
//                        pg.dismiss();
                readySotrTable = false;
                syncInternetError = true;
//                RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Обмен таблицы Сотрудники. Ошибка сети: " + t, 1095, null, null, null, null, null, Globals.session, null)));
                Log.e("TAG_TABLE", "FAILURESotrTable: " + t);
            }
        });
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadSotrTable.END");

    }


    /**
     * Получене таблицы Городов
     */
    public void downloadCityTable(Context context) {
        String mod = "data_list";
        String act = "city_list";

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().GET_CITY_T(mod, act);
        call.enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSECityTable: " + response.body());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                Log.e("TAG_TABLE", "FAILURECityTable: " + t);
            }
        });
    }


    /**
     * Получение таблички областей
     */
    public void downloadOblTable(Context context) {
        String mod = "data_list";
        String act = "obl_list";

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().GET_OBL_T(mod, act);
        call.enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSEOblTable: " + response.body());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                Log.e("TAG_TABLE", "FAILUREOblTable: " + t);
            }
        });
    }


    /**
     * Обновление таблицы: Торговых точек
     */
    public void downloadAddressTTTable(Context context) {
        String mod = "data_list";
        String act = "addr_tt_list";

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().GET_ADDRESS_TT_T(mod, act);
        call.enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSEAddressTTTable: " + response.body());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                Log.e("TAG_TABLE", "FAILUREAddressTTTable: " + t);
            }
        });
    }


    /**
     * Обновление таблицы: Товаров
     */

    public void downloadTovarTable(ArrayList<String> listId, List<WpDataDB> wpDataDBList) {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadTovarTable.START");

        String mod = "data_list";
        String act = "tovar_list";


        String date_from = Clock.getDatePeriod(-30);
        String date_to = Clock.getDatePeriod(1);


        // Используем Set для автоматического удаления дубликатов
        Set<String> uniqueClientIds = new HashSet<>();

        // Проходим по каждому элементу списка wpDataDBList
        for (WpDataDB wpDataDB : wpDataDBList) {
            // Добавляем client_id в Set (дубликаты игнорируются)
            uniqueClientIds.add(wpDataDB.getClient_id());
        }


        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "tovar_list";
        data.date_from = date_from;
        data.date_to = date_to;
        data.client_id = new ArrayList<>(uniqueClientIds);
//                Arrays.asList("38283","9382"); //9382

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<TovarTableResponse> call;
        if (listId != null) {
            call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_T_ID(mod, act, listId);
        } else {
//            call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_T(mod, act);
            call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_TABLE(RetrofitBuilder.contentType, convertedObject);
        }

//        BlockingProgressDialog finalPg = pg;
//        BlockingProgressDialog finalTovarProgressDialog = tovarProgressDialog;
        call.enqueue(new retrofit2.Callback<TovarTableResponse>() {
            @Override
            public void onResponse(retrofit2.Call<TovarTableResponse> call, retrofit2.Response<TovarTableResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.e("TAG_TABLE", "RESPONSETovarTable: " + response.body());
                        if (response.body().getState()) {
                            List<TovarDB> list = response.body().getList();

                            try {
                                try {
                                    globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLU.class.downloadTovarTable.размер ответа: " + list.size() + "\n");
                                } catch (Exception e) {
                                    globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLU.class.downloadTovarTable.ответ от сервера.ERROR1: " + e + "\n");
                                }
                            } catch (Exception e) {
                                globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLU.class.downloadTovarTable.ответ от сервера.ERROR: " + e + "\n");
                            }

                            if (list != null) {
//                                Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/downloadTovarTable/onSuccess", "list.size(): " + list.size());

                                Log.e("SERVER_REALM_DB_UPDATE", "===================================.TovarTable.SIZE: " + list.size());
                            } else {
                                Log.e("SERVER_REALM_DB_UPDATE", "===================================.TovarTable.SIZE: NuLL");
                            }

//                            list.stream()
//                                    .filter(tovarDB -> wpDataDBList.stream()
//                                            .anyMatch(wpDataDB -> Objects.equals(tovarDB.getClientId(), wpDataDB.getClient_id())));


                            RealmManager.setTovarAsync(list);

                                // 24/01/2024 Закоментил что б при синхронизации не заваливало фотками обмен
//                                PhotoDownload.getPhotoURLFromServer(list, new Clicks.clickStatusMsg() {
//                                    @Override
//                                    public void onSuccess(String data) {
//                                        Log.e("test", "String data: " + data);
//                                    }
//
//                                    @Override
//                                    public void onFailure(String error) {
//                                        Log.e("test", "String error: " + error);
//                                    }
//                                }, new Clicks.clickStatusMsgMode() {
//                                    @Override
//                                    public void onSuccess(String data, Clicks.MassageMode mode) {
//
//                                    }
//
//                                    @Override
//                                    public void onFailure(String error) {
//
//                                    }
//                                }, context);

//                                if (finalPg != null)
//                                    if (finalPg.isShowing())
//                                        finalPg.dismiss();

                        }
                    } else {
//                        if (finalPg != null)
//                            if (finalPg.isShowing())
//                                finalPg.dismiss();

                    }
                    readyTovarTable = true;
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "downloadTovarTable/onResponse/Exception", "Exception: " + e);
//                    if (finalPg != null)
//                        if (finalPg.isShowing())
//                            finalPg.dismiss();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<TovarTableResponse> call, Throwable t) {
//                if (finalPg != null)
//                    if (finalPg.isShowing())
//                        finalPg.dismiss();
                readyTovarTable = false;
                syncInternetError = true;
                Log.e("TAG_TABLE", "FAILURETovarTable: " + t);
            }
        });
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadTovarTable.END");

    }


    /**
     * Обновление таблицы: Группы товаров
     */
    public void downloadTovarGroupTable(ExchangeInterface.ExchangeResponseInterface exchange) {
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "tovar_group_list";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("INFO", "downloadTovarGroupTable.convertedObject", "convertedObject: " + convertedObject);

        retrofit2.Call<TovarGroupResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_TOVAR_GROUP(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new retrofit2.Callback<TovarGroupResponse>() {
            @Override
            public void onResponse(retrofit2.Call<TovarGroupResponse> call, retrofit2.Response<TovarGroupResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TovarGroupResponse resp = response.body();

                    if (resp.state) {
                        if (resp.list.size() > 0) {
//                            Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/downloadTovarGroupTable/onSuccess", "resp.list.size(): " + resp.list.size());

                            exchange.onSuccess(resp.list);
                        } else {
                            exchange.onFailure("Данных для обработки нет.");
                        }
                    } else {
                        exchange.onFailure("Запрос прошел с ошибкой: " + resp.error);
                    }
                } else {
                    exchange.onFailure("Ответ от сервера пустой");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<TovarGroupResponse> call, Throwable t) {
                exchange.onFailure("Данные от сервера получить не удалось. Повторите попытку познее или обратитесь в службу поддержки (кнопка '?' в правом нижнем углу) за помощью. \n\nКод ошибки: " + t);
            }
        });
    }


    /**
     * Обновление таблицы: Торговые Марки
     */
    public void downloadTradeMarksTable() {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadTradeMarksTable.START");

        String mod = "data_list";
        String act = "tovar_manufacturer_list";

//        BlockingProgressDialog pg = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: Торговые Марки");

        retrofit2.Call<TradeMarkResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TRADE_MARKS_T(mod, act);
        call.enqueue(new retrofit2.Callback<TradeMarkResponse>() {
            @Override
            public void onResponse(retrofit2.Call<TradeMarkResponse> call, retrofit2.Response<TradeMarkResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getState()) {


                        if (response.body().getList() != null) {
//                            Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/downloadTradeMarksTable/onSuccess", "response.body().getList(): " + response.body().getList().size());

                            Log.e("SERVER_REALM_DB_UPDATE", "===================================.TradeMarksTable.SIZE: " + response.body().getList().size());
                        } else {
                            Log.e("SERVER_REALM_DB_UPDATE", "===================================.TradeMarksTable.SIZE: NuLL");
                        }

                        RealmManager.setTradeMarks(response.body().getList());
//                        if (RealmManager.setTradeMarks(response.body().getList()))
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();
                    }
                }
                readyTradeMarksTable = true;
//                if (pg != null)
//                    if (pg.isShowing())
//                        pg.dismiss();

            }

            @Override
            public void onFailure(retrofit2.Call<TradeMarkResponse> call, Throwable t) {
//                if (pg != null)
//                    if (pg.isShowing())
//                        pg.dismiss();
                readyTradeMarksTable = false;
                syncInternetError = true;
                Log.e("TAG_TABLE", "FAILURETradeMarksTable: " + t);
            }
        });
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadTradeMarksTable.END");

    }


    /**
     * Обновление таблицы: План По Ассортименту
     */
    public void downloadPPATable(Context context) {
        String mod = "data_list";
        String act = "ppa_list";

        retrofit2.Call<PPATableResponse> call = RetrofitBuilder.getRetrofitInterface().GET_PPA_T(mod, act);
        call.enqueue(new retrofit2.Callback<PPATableResponse>() {
            @Override
            public void onResponse(retrofit2.Call<PPATableResponse> call, retrofit2.Response<PPATableResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSEPPATable: " + response.body());
                    if (response.body().getState()) {
                        RealmManager.setPPA(response.body().getList());
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<PPATableResponse> call, Throwable t) {
                Log.e("TAG_TABLE", "FAILUREPPATable: " + t);
            }
        });
    }


    /**
     * Обновление таблицы: Артикула
     */
    public void downloadArticleTable(Context context) {
        String mod = "data_list";
        String act = "tovar_vendor_code_list";

        retrofit2.Call<ArticleTableResponse> call = RetrofitBuilder.getRetrofitInterface().GET_ARTICLE_T(mod, act);
        call.enqueue(new retrofit2.Callback<ArticleTableResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ArticleTableResponse> call, retrofit2.Response<ArticleTableResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSEArticleTable: " + response.body());
                    if (response.body().getState()) {
                        RealmManager.setArticle(response.body().getList());
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ArticleTableResponse> call, Throwable t) {
                Log.e("TAG_TABLE", "FAILUREArticleTable: " + t);
            }
        });
    }


    /**
     * Обновление таблицы: Ошибки
     */
    public void downloadErrorTable() {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadErrorTable.START");

        String mod = "data_list";
        String act = "report_error_list";

//        BlockingProgressDialog pg = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: Ошибки");

        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "report_error_list";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

//        retrofit2.Call<JsonObject> callTest = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
//        callTest.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                Log.e("test", "test" + response);
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.e("test", "test");
//            }
//        });

        retrofit2.Call<ErrorTableResponce> call = RetrofitBuilder.getRetrofitInterface().GET_ERROR_LIST(mod, act);
        call.enqueue(new retrofit2.Callback<ErrorTableResponce>() {
            @Override
            public void onResponse(retrofit2.Call<ErrorTableResponce> call, retrofit2.Response<ErrorTableResponce> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSEdownloadErrorTable: " + response.body());
                    if (response.body().getState()) {
                        RealmManager.setError(response.body().getList());
//                        if (RealmManager.setError(response.body().getList()))
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();
                    }
                }
                readyErrorTable = true;
//                if (pg != null)
//                    if (pg.isShowing())
//                        pg.dismiss();

            }

            @Override
            public void onFailure(retrofit2.Call<ErrorTableResponce> call, Throwable t) {
//                if (pg != null)
//                    if (pg.isShowing())
//                        pg.dismiss();
                readyErrorTable = false;
                syncInternetError = true;
                Log.e("TAG_TABLE", "FAILUREdownloadErrorTable: " + t);
            }
        });
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadErrorTable.END");

    }


    /**
     * Обновление таблицы: Акции
     */
    public void downloadAkciyTable() {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadAkciyTable.START");

        String mod = "data_list";
        String act = "report_promo_list";

//        BlockingProgressDialog pg = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: Акции");

        retrofit2.Call<PromoTableResponce> call = RetrofitBuilder.getRetrofitInterface().GET_PROMO_LIST(mod, act);
        call.enqueue(new retrofit2.Callback<PromoTableResponce>() {
            @Override
            public void onResponse(retrofit2.Call<PromoTableResponce> call, retrofit2.Response<PromoTableResponce> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSEdownloadAkciyTable: " + response.body());
                    if (response.body().getState()) {
                        RealmManager.setPromo(response.body().getList());
//                        if (RealmManager.setPromo(response.body().getList()))
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();
                    }
                }
                readyAkciyTable = true;
            }

            @Override
            public void onFailure(retrofit2.Call<PromoTableResponce> call, Throwable t) {
//                if (pg != null)
//                    if (pg.isShowing())
//                        pg.dismiss();
                readyAkciyTable = false;
                syncInternetError = true;
                Log.e("TAG_TABLE", "FAILUREdownloadAkciyTable: " + t);
            }
        });
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadAkciyTable.END");

    }


    /**
     * Получение фото товаров
     */
    public void getTovarImg(List<TovarDB> list, String imageType) {

        Log.e("DetailedReportA", "list: " + list.size());
        Log.e("getTovarImg", "list.size: " + list.size());
        Log.e("getTovarImg", "imageType: " + imageType);

        String mod = "images_view";
        String act = "list_image";
        String tovarOnly = "1";
        String nolimit = "1";

        if (imageType.equals("") && imageType == null) {
            imageType = "small";
        }


        // todo убрать это нафиг отсюда, что я курил когда это писал?
        ArrayList<String> listId = new ArrayList<>();
        for (TovarDB tov : list) {
            try {
                if (!RealmManager.stackPhotoExistByObjectId(Integer.parseInt(tov.getiD()), imageType)) {
                    listId.add(tov.getiD());
                }
            } catch (Exception e) {
                // ЛОГ ошибки
            }
        }

        Log.e("TAG_TABLE", "PHOTO_TOVAR_ID_TO_SEND: " + listId);
        Log.e("TAG_TABLE", "PHOTO_TOVAR_ID_TO_SEND: " + listId.size());

        retrofit2.Call<TovarImgResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_PHOTO_INFO(mod, act, tovarOnly, nolimit, imageType, listId);
        String finalImageType = imageType;
        call.enqueue(new retrofit2.Callback<TovarImgResponse>() {
            @Override
            public void onResponse(retrofit2.Call<TovarImgResponse> call, retrofit2.Response<TovarImgResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Log.e("TAG_TABLE", "PHOTO_TOVAR_RESPONSE: " + response);
                        Log.e("TAG_TABLE", "PHOTO_TOVAR_RESPONSE_BODY: " + response.body().getState());
                        List<TovarImgList> list = response.body().getList();

                        for (TovarImgList item : list) {
                            Log.e("PHOTO_TOV_data", "-------------------------------");
                            Log.e("PHOTO_TOV_data", "item.getID(): " + item.getID());
                            Log.e("PHOTO_TOV_data", "item.getPhotoTp(): " + item.getPhotoTp());
                            Log.e("PHOTO_TOV_data", "item.getPhotoTpTxt(): " + item.getPhotoTpTxt());
                        }


                        if (list != null) {
                            Log.e("TAG_TABLE", "PHOTO_TOVAR_LIST_SIZE: " + list.size());
                            downloadTovarImg(list, finalImageType);
                        }
                    } catch (Exception e) {
                        Log.e("LOG", "SAVE_TO_LOG");
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<TovarImgResponse> call, Throwable t) {
                Log.e("TAG_TABLE", "PHOTO_TOVAR_ERROR: " + t);
            }
        });
    }


    /**
     * Скачивание фото Товаров
     */
    public void downloadTovarImg(List<TovarImgList> list, String imageType) {
        Globals globals = new Globals();

        Log.e("TAG_TABLE", "PHOTO_TOVAR_DOWNLOAD_LIST_SIZE: " + list.size());

        for (int i = 0; i < list.size(); i++) {
            Log.e("TAG_TABLE", "PHOTO_TOVAR_URL: " + list.get(i).getPhotoUrl());

            Log.e("downloadTovarImg", "-------------------------------");
            Log.e("downloadTovarImg", "item.getID(): " + list.get(i).getID());
            Log.e("downloadTovarImg", "item.getPhotoTp(): " + list.get(i).getPhotoTp());
            Log.e("downloadTovarImg", "item.getPhotoTpTxt(): " + list.get(i).getPhotoTpTxt());

            // 01.03.2021 Костыль. Нужен для того что б с Сайта не заваливалось что-то кроме фото товаров
            int tp = Integer.parseInt(list.get(i).getPhotoTp());

            Log.e("downloadTovarImg", "TP: " + tp);

            if (tp == 18) {
                retrofit2.Call<ResponseBody> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_PHOTO_BY_URL(list.get(i).getPhotoUrl());
                int finalI = i;
                call.enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.e("TAG_TABLE", "PHOTO_TOVAR_URL_res: " + response.body().byteStream());

                            Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                            String path = globals.saveImage1(bmp, imageType + "-" + list.get(finalI).getTovarId());

                            Log.e("TESTING", "2_SAVE PHOTO");
                            Log.e("TESTING", "2_SAVE PHOTO/path: " + path);

                            int id = RealmManager.stackPhotoGetLastId();
                            id++;
                            StackPhotoDB stackPhotoDB = new StackPhotoDB(
                                    id,
                                    list.get(finalI).getID(),
                                    Integer.parseInt(list.get(finalI).getTovarId()),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    0,
                                    System.currentTimeMillis(),
                                    0,
                                    0,
                                    0,
                                    path,
                                    null,
                                    Integer.parseInt(list.get(finalI).getPhotoTp()),
                                    null,
                                    null,
                                    null,
                                    imageType,
                                    null,
                                    0,
                                    0,
                                    false,
                                    null,
                                    null,
                                    null);
                            stackPhotoDB.setCode_iza(list.get(finalI).codeIZA);

                            RealmManager.stackPhotoSavePhoto(stackPhotoDB);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                        Log.e("TAG_TABLE", "PHOTO_TOVAR_URL: " + t);
                    }
                });
            } else {
                Log.e("downloadTovarImg", "--------------ЭТО ФОТО НЕ СОХРАНЕНО-----------------");
            }
        }
    }


    public JSONArray toJson(List<LogDB> log) {
        JSONArray array = new JSONArray();

        for (int i = 0; i < log.size(); i++) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", log.get(i).getId());
                obj.put("dt_action", log.get(i).getDt_action());
                obj.put("comments", log.get(i).getComments());
                obj.put("tp", log.get(i).getTp());
                obj.put("client_id", log.get(i).getClient_id());
                obj.put("addr_id", log.get(i).getAddr_id());
                obj.put("obj_id", log.get(i).getObj_id());
                obj.put("author", log.get(i).getAuthor());
                obj.put("dt", log.get(i).getDt());
                obj.put("session", log.get(i).getSession());
                obj.put("obj_date", log.get(i).getObj_date());

                array.put(i, obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        return array;
    }


    /**
     * 13.08.2020
     * <p>
     * Загрузка плана работ по ВПИ
     * (для автообмена)
     */
    public void updateWpData() {

        Log.e("TEST_ERROR_WP", "ENTER");

        String mod = "plan";
        String act = "list";
        String date_from = timeYesterday;
        String date_to = timeTomorrow;
        long vpi = 0;

        SynchronizationTimetableDB sTable = RealmManager.getSynchronizationTimetableRowByTable("wp_data");
        if (sTable != null) {
            Globals.writeToMLOG("INFO", "TablesLoadingUnloading/updateWpData/getSynchronizationTimetableRowByTable", "sTable: " + sTable);
            vpi = sTable.getVpi_app();
            Log.e("updateWpData", "vpi: " + vpi);

//            if (timeToUpdate(vpi, sTable.getUpdate_frequency())) {
            // TEST Узнаю весь JSON. Надо удалять потом как отлажу.
//            try {
//                retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().GET_WPDATA_VPI_JSON(mod, act, date_from, date_to, vpi);
//                call.enqueue(new retrofit2.Callback<JsonObject>() {
//                    @Override
//                    public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
//                        Log.e("TAG_TEST_WP", "RESPONSE_JSON: " + response.body());
//                    }
//
//                    @Override
//                    public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
//                    }
//                });
//            } catch (Exception e) {
//            }

            // Начинаю синхронизацию Плана работ
            try {
                // Получаю изменённые данные с плана работ
                retrofit2.Call<WpDataServer> call = RetrofitBuilder.getRetrofitInterface().GET_WPDATA_VPI(mod, act, date_from, date_to, vpi);
                call.enqueue(new retrofit2.Callback<WpDataServer>() {
                    @Override
                    public void onResponse(retrofit2.Call<WpDataServer> call, retrofit2.Response<WpDataServer> response) {
                        Log.e("TAG_TEST_WP", "RESPONSE_0");
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                if (response.body().getState()) {
                                    Log.e("TAG_TEST_WP", "RESPONSE_OK");
                                    if (response.body().getList() != null) {
                                        ArrayList<WpDataDB> wpToUpload = RealmManager.setWpDataAuto(response.body().getList()); // Получаем данные для выгрузки
                                        RealmManager.INSTANCE.executeTransaction(realm -> {
                                            if (sTable != null) {
                                                long vpiApp = System.currentTimeMillis() / 1000;
                                                Log.e("updateWpData", "ЗАписал когда обновился" + vpiApp);

                                                sTable.setVpi_app(vpiApp);

                                                Log.e("updateWpData", "sTable: " + sTable);
                                                realm.insertOrUpdate(sTable);
                                            }
                                        }); //
                                        if (wpToUpload != null) {
                                            Log.e("updateWpData", "wpToUploadSize: " + wpToUpload.size());
                                        }
                                    }
                                }
                            }
                            Log.e("TAG_TEST_WP", "RESPONSE_OK?");
                            Globals.writeToMLOG("OK", "TablesLoadingUnloading/updateWpData/onResponse", "Вот столько обновилось" + response.body().getList().size());
                        } catch (Exception e) {
                            Globals.writeToMLOG("ERROR", "TablesLoadingUnloading/updateWpData/onResponse/Exception", "Exception e" + e);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<WpDataServer> call, Throwable t) {
                        Log.e("TAG_TEST_WP", "FAILURE_0 E: " + t);
                        Globals.writeToMLOG("ERROR", "TablesLoadingUnloading/updateWpData/onFailure", "Throwable t: " + t);
                    }
                });
            } catch (Exception e) {
                Log.e("TAG_TEST_WP", "FAILURE_0 CATCH: " + e.getMessage());
                Globals.writeToMLOG("ERROR", "TablesLoadingUnloading/updateWpData/Exception2", "Exception e: " + e);
            }
            /*} else {
                Log.e("updateWpData", "Пока РАНО синхронизовать");
            }*/
        }
    }

    private boolean timeToUpdate(long lastUpdate, int updateFrequency) {
        long time = (System.currentTimeMillis() / 1000) - (lastUpdate + updateFrequency);
        Log.e("timeToUpdate", "Время до обмена Плана работ: " + time + "секунд.");
        return System.currentTimeMillis() / 1000 > lastUpdate + updateFrequency;
    }

    //----------------------------------------------------------------------------------------------

    /***/
    public void sendAndUpdateLog() {
        String mod = "log";
        String act = "save";
        ArrayList<LogUploadToServ> data = RealmManager.getLogToSend();

        Log.e("UPLOAD_DATA", "LOG. (" + data.size() + ")");

        Log.e("LOG_SEND", "call: " + data.size());

        if (data.size() > 0) {
            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().LOG(mod, act, data);
            Log.e("LOG_SEND", "call: " + call);
            call.enqueue(new retrofit2.Callback<JsonObject>() {
                @Override
                public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    Log.e("LOG_SEND", "response: " + response.body());

                    if (response.isSuccessful() && response.body() != null) {
                        JsonObject obj = response.body();
                        JsonObject log = obj.getAsJsonObject("log");

                        for (LogUploadToServ el : data) {
                            if (log.getAsJsonObject(el.getElement_id()) != null) {
                                Log.e("LOG_SEND", "JSON: " + log.getAsJsonObject(el.getElement_id()));
                                LogDB logDB = RealmManager.getLogRowById(el.getElement_id());
                                RealmManager.INSTANCE.executeTransaction(realm -> {
                                    logDB.setDt(System.currentTimeMillis() / 1000); // Надо сменить на DT из запроса
                                    INSTANCE.copyToRealmOrUpdate(logDB);
                                });
                            } else {

                            }

                        }

                    }
                }

                @Override
                public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                    Log.e("LOG_SEND", "FAILURE_E: " + t.getMessage());
                    Log.e("LOG_SEND", "FAILURE_E2: " + t);
                }
            });
        }
    }

    /*

     */
/**
 * 17.08.2020
 * <p>
 * Выгрузка Плана работ на Сервер
 * <p>
 * send WpData in data object(JSON).
 * Data mast min exist: element_id, code_dad2, user_id, client_id, isp + changing fields
 *//*

    public void sendWpData() {
        String mod = "plan";
        String act = "update_data";
        ArrayList<WpDataUploadToServ> data = RealmManager.getWpDataToSend();   // wp_data which must sending on server. passed data to send.


        JsonArray convertedObject = new Gson().fromJson(new Gson().toJson(data), JsonArray.class);
        Log.e("WPDATA_SEND", "convertedObject.json: " + convertedObject);


        Log.e("WPDATA_SEND", "Data: " + data.size());

        if (data != null && data.size() > 0) {
//            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().SEND_WP_DATA(mod, act, data);
            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().SEND_WP_DATA(mod, act, data);
            call.enqueue(new retrofit2.Callback<JsonObject>() {
                @Override
                public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    Log.e("WPDATA_SEND", "RESPONSE: " + response);
                    Log.e("WPDATA_SEND", "RESPONSE.body: " + response.body());
                }

                @Override
                public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                    Log.e("WPDATA_SEND", "FAILURE_E: " + t.getMessage());
                    Log.e("WPDATA_SEND", "FAILURE_E2: " + t);
                }
            });
        }
    }


    */
/**
 * 31.03.2021
 * Новая отправка на сервер данных о Начале/Конце работы
 *//*

    public void sendWpData2() {
        UploadDataSEWork data = new UploadDataSEWork();
        data.mod = "plan";
        data.act = "update_data";
        data.data = RealmManager.getWpDataStartEndWork();

        JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(data), JsonObject.class);
        Log.e("sendWpData2", "convertedObject.json: " + convertedObject);
//        SEND_WP_DATA


        if (data != null && data.data.size() > 0) {
            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().SEND_WP_DATA(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<JsonObject>() {
                @Override
                public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    Log.e("sendWpData2", "RESPONSE: " + response);
                    Log.e("sendWpData2", "RESPONSE.body: " + response.body());
                }

                @Override
                public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                    Log.e("sendWpData2", "FAILURE_E: " + t.getMessage());
                    Log.e("sendWpData2", "FAILURE_E2: " + t);
                }
            });
        }
    }

*/

    /**
     * 17.08.2020
     * <p>
     * Сбор данных(План Работ) для выгрузки на сервер
     * Формирование данных. Раньше было в Реалме (29.10.2020)
     */
    private ArrayList<WpDataUploadToServ> getWpDataToSend(List<WpDataDB> list) {
/*        SiteHintsDB<WpDataDB> list = INSTANCE.where(WpDataDB.class)
                .isNull("dt")
                .findAll();*/

        ArrayList<WpDataUploadToServ> wpDataList = new ArrayList<>();
        if (list != null) {
            for (WpDataDB l : list) {
                wpDataList.add(new WpDataUploadToServ(
                        String.valueOf(l.getId()),
                        String.valueOf(l.getCode_dad2()),
                        String.valueOf(l.getUser_id()),
                        String.valueOf(l.getClient_id()),
                        String.valueOf(l.getIsp()),

                        String.valueOf(l.getVisit_start_dt()),
                        String.valueOf(l.getVisit_end_dt()),
                        String.valueOf(l.getClient_start_dt()),
                        String.valueOf(l.getClient_end_dt()),

                        String.valueOf(l.getSetStatus())
                ));
            }
        } else {
            // Данных нет, нужно логировать.
        }
        return wpDataList;
    }


    /**
     * 20.07.2020
     * ДЭМО. Подумать как его б лучше использовать
     * <p>
     * Скачать
     * Получение подсказки для ввода Опций ТПЛов
     */
    public void getReportPrepareTovarOptionsTPLHint(String tovarId, String codeDad2, String clientId) {
        String mod = "report_prepare";
        String act = "get_param_stats";

        retrofit2.Call<ReportHint> call = RetrofitBuilder.getRetrofitInterface().GET_REPORT_HINT(mod, act, tovarId, codeDad2, clientId);
        call.enqueue(new retrofit2.Callback<ReportHint>() {
            @Override
            public void onResponse(retrofit2.Call<ReportHint> call, retrofit2.Response<ReportHint> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("REPORT_HINT", "" + response.body());

                }
            }

            @Override
            public void onFailure(retrofit2.Call<ReportHint> call, Throwable t) {

            }
        });
    }


    public void cronUpdateTables() {
        Log.e("cronUpdateTables", "ALL START");

        long currentTime = System.currentTimeMillis() / 1000; // Текущее время

//        SynchronizationTimetableDB synchTableWp = RealmManager.getSynchronizationTimetableRowByTable("wp_data");


        SynchronizationTimetableDB synchTableWp = INSTANCE.where(SynchronizationTimetableDB.class)
                .equalTo("table_name", "wp_data")
                .findFirst();


        long l = 0;
        if (synchTableWp != null) {
            l = synchTableWp.getVpi_app() + synchTableWp.getUpdate_frequency();
        }

        Log.e("cronUpdateTables", "l: " + l);
        Log.e("cronUpdateTables", "currentTime: " + currentTime);

        long sync = l - currentTime;
        Log.e("cronUpdateTables", "До синхронизации: " + sync);


        if (l < currentTime) {
            downloadSiteHints("2");
            downloadVideoLessons();
            downloadOborotVed();

            Log.e("cronUpdateTables", "Выгрузка данных");
            try {
//                uploadReportPrepareToServer();
                uploadRP(new ExchangeInterface.ExchangeResponseInterface() {
                    @Override
                    public <T> void onSuccess(List<T> data) {
                        if (data != null) {
                            List<ReportPrepareUpdateResponseList> list = (List<ReportPrepareUpdateResponseList>) data;

                            Long[] ids = new Long[list.size()];
                            int count = 0;
                            for (ReportPrepareUpdateResponseList item : list) {
                                ids[count++] = item.elementId;
                            }

                            List<ReportPrepareDB> rp = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getByIds(ids));

                            for (ReportPrepareDB item : rp) {
                                for (ReportPrepareUpdateResponseList listItem : list) {
                                    if (listItem.elementId.equals(item.getID())) {
                                        if (listItem.state) {
                                            item.setUploadStatus(0);
                                            ReportPrepareRealm.setAll(Collections.singletonList(item));
                                        }
                                    }
                                }
                            }

                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.d("uploadRP", "error: " + error);
                    }
                });

            } catch (Exception e) {
                Globals.writeToMLOG("ERR", "uploadRP", "Exception e: " + e);
            }


            // Сохранение последнего обмена
            try {
                INSTANCE.executeTransaction(
                        realm -> {
                            if (synchTableWp != null) {
                                synchTableWp.setVpi_app(System.currentTimeMillis() / 1000);
                                realm.insertOrUpdate(synchTableWp);
                                Log.d("updateRealm", "here");
                            } else {
                                Log.d("updateRealm", "?");
                            }
                        }/*,
                        () -> {

                            if (synchTableWp != null) {
                                INSTANCE.insertOrUpdate(synchTableWp);
                            }
                        },
                        error -> Log.d("updateRealm", "error: " + error)*/
                );


//                INSTANCE.insertOrUpdate(synchTableWp);
            } catch (Exception e) {
                Log.d("updateRealm", "SAVE LAST UPDATE ERROR: " + e);
                Log.e("cronUpdateTables", "SAVE LAST UPDATE ERROR: " + e);
            }

        } else {
            Log.e("cronUpdateTables", "Ещё рано выгружать данные");
        }
    }


    public void updateTables(Context context) {
        Log.e("TAG_TEST_WP", "ALL START");
        long currentTime = System.currentTimeMillis() / 1000; // Текущее время
        RealmResults<SynchronizationTimetableDB> realmResults = getSynchronizationTimetable();

        for (int i = 0; i < realmResults.size(); i++) {
            long l = realmResults.get(i).getVpi_server() + realmResults.get(i).getUpdate_frequency();
            if (l < currentTime) {
                switch (realmResults.get(i).getId()) {
                    case 1:
//                        downloadWPData(context);
                        Log.e("TAG_TEST_WP", "ALL START/ GET WP");
//                        updateWpData();

                        downloadOptions();
                        uploadRP(new ExchangeInterface.ExchangeResponseInterface() {
                            @Override
                            public <T> void onSuccess(List<T> data) {
                                Log.e("uploadRP1", "data: " + data);
                                downloadReportPrepare(1); // Тут мод 1 ибо будет обмен автоматический

                                if (data != null) {
                                    List<ReportPrepareUpdateResponseList> list = (List<ReportPrepareUpdateResponseList>) data;

                                    Long[] ids = new Long[list.size()];
                                    int count = 0;
                                    for (ReportPrepareUpdateResponseList item : list) {
                                        ids[count++] = item.elementId;
                                    }

                                    List<ReportPrepareDB> rp = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getByIds(ids));

                                    for (ReportPrepareDB item : rp) {
                                        for (ReportPrepareUpdateResponseList listItem : list) {
                                            if (listItem.elementId.equals(item.getID())) {
                                                if (listItem.state) {
                                                    item.setUploadStatus(0);
                                                    ReportPrepareRealm.setAll(Collections.singletonList(item));
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.d("test", "err" + error);
                            }
                        });

                        downloadAddressTable();
                        downloadCustomerTable();
                        downloadSotrTable();

//                        sendAndUpdateLog(context);
                        uploadReportPrepareToServer();

//                        RealmManager.setToSynchronizationTimetableDB(new SynchronizationTimetableDB(1, "wp_data", 600, currentTime, currentTime, 0, 0));
                    case 2:
                        downloadImagesTp();
//                        RealmManager.setToSynchronizationTimetableDB(new SynchronizationTimetableDB(2, "image_tp", 36000, currentTime, currentTime, 0, 0));
                    case 3:
                        downloadTypeGrp();
//                        RealmManager.setToSynchronizationTimetableDB(new SynchronizationTimetableDB(3, "client_group_tp", 36000, currentTime, currentTime, 0, 0));
                    case 4:
                        // Пока делать нечего
                }
            }
        }
    }


    // =============== U_P_L_O_A_D TABLE TO SERVER ===============

    public void uploadRP(ExchangeInterface.ExchangeResponseInterface exchange) {
        try {
            StandartData data = new StandartData();
            data.mod = "report_prepare";
            data.act = "set_report_data";
            data.data = RealmManager.getReportPrepareToUpload();

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            Log.e("uploadRP", "convertedObject: " + convertedObject);
            Globals.writeToMLOG("INFO", "uploadRP().Start", "Size: " + data.data.size());

            if (data.data.size() > 0) {
                retrofit2.Call<ReportPrepareUpdateResponse> call = RetrofitBuilder.getRetrofitInterface().SEND_RP_INFO(RetrofitBuilder.contentType, convertedObject);
                call.enqueue(new Callback<ReportPrepareUpdateResponse>() {
                    @Override
                    public void onResponse(Call<ReportPrepareUpdateResponse> call, Response<ReportPrepareUpdateResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().data != null && response.body().data.size() > 0) {
                                exchange.onSuccess(response.body().data);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ReportPrepareUpdateResponse> call, Throwable t) {
                        Log.e("uploadRP", "Throwable t: " + t);
                        exchange.onFailure("uploadRP: " + t);
                    }
                });
            } else {
                exchange.onFailure("uploadRP: Данных на выгрузку нет");
            }
        } catch (Exception e) {
            exchange.onFailure("uploadRP: Exception e: " + e);
            Globals.writeToMLOG("INFO", "uploadRP()", "Exception e: " + e);
        }
    }

    // upload REPORT_PREPARE
    public boolean uploadReportPrepareToServer() {
        boolean res = false;

        // Получаем последние данные из REPORT_PREPARE для выгрузки на сервер (время изменения dtChange)
        List<ReportPrepareServ> data = RealmManager.getReportPrepareToUpload();
        Log.e("UPLOAD_DATA", "REPORT_PREPARE. (" + data.size() + ")");

        if (data != null && data.size() > 0) {
            StandartData standartData = new StandartData();
            standartData.mod = "report_prepare";
            standartData.act = "set_report_data";
            standartData.data = data;

            Gson gson = new Gson();
            String json = gson.toJson(standartData);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<ReportPrepareUploadResponse> call = RetrofitBuilder.getRetrofitInterface().SEND_RP(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<ReportPrepareUploadResponse>() {
                @Override
                public void onResponse(retrofit2.Call<ReportPrepareUploadResponse> call, retrofit2.Response<ReportPrepareUploadResponse> response) {
                    long currentTime = System.currentTimeMillis() / 1000;
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                if (response.body().state) {
                                    if (response.body().data != null && response.body().data.size() > 0) {
                                        for (ReportPrepareUploadList item : response.body().data) {
                                            ReportPrepareDB reportPrepareDB = INSTANCE.copyFromRealm(RealmManager.getReportPrepareRowById(item.elementId));
                                            RealmManager.INSTANCE.executeTransaction(realm -> {
                                                reportPrepareDB.setUploadStatus(0);
                                                reportPrepareDB.setDtChange(currentTime);
                                                realm.copyToRealmOrUpdate(reportPrepareDB);
                                            });
                                        }
                                    } else {
                                        Globals.writeToMLOG("INFO", "onResponse/uploadReportPrepareToServer/", "response.body().data == null || size == 0");
                                    }
                                } else {
                                    Globals.writeToMLOG("INFO", "onResponse/uploadReportPrepareToServer/", "response.body().state: " + false);
                                }
                            } else {
                                Globals.writeToMLOG("INFO", "onResponse/uploadReportPrepareToServer/", "response.body() != null");
                            }
                        } else {
                            Globals.writeToMLOG("INFO", "onResponse/uploadReportPrepareToServer/", "response.body() != null");
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "onResponse/uploadReportPrepareToServer", "Resp not successful. response.code(): " + response.code());
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ReportPrepareUploadResponse> call, Throwable t) {
                    Globals.writeToMLOG("ERROR", "onFailure/uploadReportPrepareToServer", "Throwable t: " + t);
                }
            });

        } else {
            // print massage NO DATA
            return res;
        }

        return res;
    }


    /**
     * 30.09.2020
     * <p>
     * Выгрузка на сервер ЛОГА МЕСТОПОЛОЖЕНИЯ
     */
    public void uploadLodMp(ExchangeInterface.ExchangeRes res) {
        String mod = "location";
        String act = "track";
//        String debug_param_1 = "test_something";

        List<LogMPDB> logMp = RealmManager.INSTANCE.copyFromRealm(RealmManager.getNOTUploadLogMPDB());
//        List<LogMPDB> logMp = RealmManager.INSTANCE.copyFromRealm(RealmManager.getNOTUploadLogMPDBTEST());
        if (logMp != null && logMp.size() > 0) {
            Log.e("uploadLodMp", "LogMpUploadText. LogSize: " + logMp.size());

            HashMap<String, String> map = new HashMap<>();
            for (LogMPDB list : logMp) {
                if (list.getGp() != null){
                    map.put("gp[" + list.getId() + "]", list.getGp());
                }
            }

            Globals.writeToMLOG("INFO", "uploadLodMp", "Количество ЛОГ МП на выгрузку: " + logMp.size());
//            Globals.writeToMLOG("INFO", "uploadLodMp", "Данные на выгрузку: " + map);

            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().UPLOAD_LOG_MP(mod, act, map/*, debug_param_1*/);
            call.enqueue(new retrofit2.Callback<JsonObject>() {
                @Override
                public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    Log.e("uploadLodMp", "RESPONSE: " + response.body());

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
                                                long id = geoInfo.get("geo_id").getAsLong();
                                                RealmManager.INSTANCE.executeTransaction(realm -> {
                                                    list.serverId = id;
                                                    list.upload = System.currentTimeMillis()/1000;  // 27.08.23 Вместо удаления, пишу воемя когда координаты были выгружены
                                                    realm.insertOrUpdate(list);
                                                });

                                                Globals.writeToMLOG("INFO", "uploadLodMp/onResponse/executeTransaction.onSuccess", "OK: " + list.serverId);
                                                res.onSuccess("ОК");
                                            } catch (Exception e) {
                                                Globals.writeToMLOG("ERROR", "uploadLodMp/onResponse/executeTransaction", "Exception e: " + e);
                                                res.onFailure("Exception e: " + e);
                                            }
                                        }else {
                                            try {
                                                long id = geoInfo.get("geo_id").getAsLong();
                                                RealmManager.INSTANCE.executeTransaction(realm -> {
                                                    list.serverId = id;
                                                    list.upload = System.currentTimeMillis()/1000;  // 27.08.23 Вместо удаления, пишу воемя когда координаты были выгружены
                                                    realm.insertOrUpdate(list);
                                                });
                                            }catch (Exception e){
                                                Globals.writeToMLOG("INFO", "uploadLodMp/onResponse/geoInfo.get(\"state\")", "Exception e: " + e);
                                            }
                                            Globals.writeToMLOG("INFO", "uploadLodMp/onResponse/geoInfo.get(\"state\")", "response.body(): " + response.body());
                                        }
                                    }
                                }
                            }else {
                                Globals.writeToMLOG("INFO", "uploadLodMp/onResponse/state=false", "response.body(): " + response.body());
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
                    Log.e("uploadLodMp", "FAILURE_E: " + t.getMessage());
                    Log.e("uploadLodMp", "FAILURE_E2: " + t);
                    res.onFailure("onFailure: " + t);
                }
            });
        } else {
            Globals.writeToMLOG("INFO", "uploadLodMp", "Данных Лога МП на выгрузку нет");
            Log.e("uploadLodMp", "LogMpUploadText. LogSize: " + null);
        }
    }

    // =============== D_O_W_N_L_O_A_D ===============

    /**
     * 19.10.2020
     * Качаем меню сайта
     * <p>
     * //https://merchik.net/mobile_app.php?mod=menu
     */
    public void downloadMenu() {
        String mod = "menu";

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_MENU(mod);
        call.enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                Log.e("downloadMenu", "RESPONSE: " + response);
                Log.e("downloadMenu", "RESPONSE.BODY: " + response.body());
                try {
                    JSONObject j = new JSONObject(response.body().toString());
                    saveMenuDB(parseJsonMenu(j, false));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                Log.e("downloadMenu", "FAILURE_E: " + t.getMessage());
                Log.e("downloadMenu", "FAILURE_E2: " + t);
            }
        });
    }


    private ArrayList<MenuItemFromWebDB> parseJsonMenu(JSONObject jsonObject, boolean submenu) {
//        Log.e("parseJsonMenu", "===========START============");
        ArrayList<MenuItemFromWebDB> data = new ArrayList<>();  // Таблица меню на сохранение в БД
        Iterator keys = jsonObject.keys();
        while (keys.hasNext()) {
            MenuItemFromWebDB item = new MenuItemFromWebDB();
            String currentDynamicKey = (String) keys.next();
//            Log.e("parseJsonMenu", "currentDynamicKey: " + currentDynamicKey);
            try {
                JSONObject menuItem = jsonObject.getJSONObject(currentDynamicKey);
//                Log.e("parseJsonMenu", "currentDynamicValue: " + menuItem);

                item.setID(menuItem.getInt("ID"));
                item.setNm(menuItem.getString("nm"));
                item.setUrl(menuItem.getString("url"));
                item.setModule(menuItem.getString("module"));
                item.setInternalName(menuItem.getString("internal_name"));
                item.setParent(menuItem.getInt("parent"));

                if (menuItem.has("submenu")) {
                    JSONObject obj = menuItem.getJSONObject("submenu");
                    RealmList<Integer> a = new RealmList<>();
                    Iterator k = obj.keys();
                    while (k.hasNext()) {
                        String submenuItemId = (String) k.next();
//                        Log.e("parseJsonMenu", "submenuItemId: " + submenuItemId);
                        a.add(Integer.valueOf(submenuItemId));
                    }
                    item.setSubmenu(a);

//                    Log.e("parseJsonMenu", "-------------currentDynamicKey--------------");
//                    Log.e("parseJsonMenu", "menuItem.getString(\"submenu\"): " + menuItem.getString("submenu"));
//                    Log.e("parseJsonMenu", "-------------currentDynamicKey--------------");
                    data.addAll(parseJsonMenu(obj, true));
                }

                item.setImg(menuItem.getString("img"));
                item.setComment(menuItem.getString("comment"));

                data.add(item);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        Log.e("parseJsonMenu", "===========END============");
        return data;
    }

    private void saveMenuDB(ArrayList<MenuItemFromWebDB> data) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }


    /**
     * 12.01.2021
     * Получение списка объектов сайта
     * <p>
     * Это что-то типа подсказок.
     * <p>
     * в ответ на запрос должен вернуться массив в object_list, в котором
     * содержатся следующие данные
     * ID - код объекта
     * ID_1c - код объекта во внутреннем формате 1с (для приложения не актуально)
     * nm - Название объекта (используется, если для него нет перевода в
     * выбранном языке)
     * comments - Комментарий к объекту (используется, если для него нет
     * перевода в выбранном языке)
     * script_mod - модуль (пока мало где используется)
     * script_act - действие (пока мало где используется)
     * lesson_id - код видеоурока
     * platform_id - код платформы
     * object_type - тип объекта (на стороне сайта используется для того, чтобы
     * различать формат подсказки)
     * dt_change - впи
     * author_id - код автора
     * nm_translation - Название объекта на выбранном языке (в случае, если
     * перевода нет, значение будет пустым)
     * comments_translation - Комментарий к объекту на выбранном языке (в
     * случае, если перевода нет, значение будет пустым)
     *
     * @param langId
     */
    public static void downloadSiteHints(String langId) {
        try {
            Log.e("downloadSiteHints", "String langId: " + langId);

            String mod = "site_objects";
            String act = "list";
//        String lang_id = "2"; //(переменная, в которой передаётся код языка, для которого требуется получить список объектов)

            retrofit2.Call<SiteObjects> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_SITE_HINTS(mod, act, langId);
            call.enqueue(new retrofit2.Callback<SiteObjects>() {
                @Override
                public void onResponse(retrofit2.Call<SiteObjects> call, retrofit2.Response<SiteObjects> response) {
                    try {
                        if (response.isSuccessful()){
                            if (response.body() != null && response.body().getState() && response.body().getObjectList() != null && response.body().getObjectList().size() > 0) {
//                                Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/downloadSiteHints/onSuccess", "response.body().getObjectList().size(): " + response.body().getObjectList().size());
                                saveSiteObjectsDB(response.body().getObjectList());
                            }
                        }
                    } catch (Exception e) {
                        Log.e("downloadSiteHints", "Exception e: " + e);
                    }

                }

                @Override
                public void onFailure(retrofit2.Call<SiteObjects> call, Throwable t) {
                    Log.e("downloadSiteHints", "FAILURE_E: " + t.getMessage());
                    Log.e("downloadSiteHints", "FAILURE_E2: " + t);
                }
            });
        } catch (Exception e) {
            //todo Делать запись в лог ошибки
        }
    }


    private static void saveSiteObjectsDB(List<SiteObjectsDB> data) {
        if (data != null) {
            Log.e("saveSiteObjectsDB", "data+ : " + data.size());
            INSTANCE.executeTransaction(realm -> {
                INSTANCE.delete(SiteObjectsDB.class);
                INSTANCE.copyToRealmOrUpdate(data);
            });
        } else {
            Log.e("saveSiteObjectsDB", "data-");
        }

    }


    /**
     * 12.01.2021
     * Получение списка видеоуроков
     * <p>
     * Нужно для того что б отображать видеоуроки в диалоговых окошках
     * <p>
     * "в ответ на запрос должен вернуться массив в list, в котором содержатся
     * следующие данные:
     * <p>
     * ID - код видеоурока
     * nm - название видеоурока
     * w_all - признак "показывать всем"
     * w_kli - признак "показывать клиента"
     * w_our - признак "показывать своим"
     * doljnosti - список должностей строкой с разделителем "," , для которых
     * данный видеоурок предназначен
     * url - ссылка на видеоурок для просмотра
     * title - подсказка
     * html - поле с html кодом, если нужно вывести видеоурок в нестандартном
     * виде (скорее всего уже не актуально)
     * platform_id - код платформы
     * dt - дата"
     */
    public static void downloadVideoLessons() {
        String mod = "lesson";
        String act = "list";

        retrofit2.Call<SiteHints> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_VIDEO_LESSONS(mod, act);
        call.enqueue(new retrofit2.Callback<SiteHints>() {
            @Override
            public void onResponse(retrofit2.Call<SiteHints> call, retrofit2.Response<SiteHints> response) {
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().getState() != null && response.body().getState()) {
                                if (response.body().getList() != null && response.body().getList().size() > 0) {
//                                    Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/downloadVideoLessons/onSuccess", "response.body().getList().size(): " + response.body().getList().size());
                                    saveSiteHintsDB(response.body().getList());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "downloadVideoLessons", "Exception e: " + e);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<SiteHints> call, Throwable t) {
                Log.e("downloadVideoLessons", "FAILURE_E: " + t.getMessage());
                Log.e("downloadVideoLessons", "FAILURE_E2: " + t);
            }
        });
    }

    public static void saveSiteHintsDB(List<SiteHintsDB> data) {
        Log.e("downloadVideoLessons", "saveSiteHintsDB/data: " + data);

        try {
            if (data != null) {
                Log.e("downloadVideoLessons", "saveSiteHintsDB/data.size(): " + data.size());
                INSTANCE.executeTransaction(realm -> {
                    INSTANCE.delete(SiteHintsDB.class);
                    for (SiteHintsDB item : data) {
                        Log.e("saveSiteHintsDB", "data id: " + item.getID() + " |nm: " + item.getNm());
                        INSTANCE.copyToRealmOrUpdate(item);
                    }
                });
            }
        } catch (Exception e) {
        }
    }


    private void downloadPPA() {
        try {
            PPARequest data = new PPARequest();
            data.mod = "ppa";
            data.act = "list";
//            data.code_iza = getIZAList();

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);


            retrofit2.Call<PPAonResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_PPA(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<PPAonResponse>() {
                @Override
                public void onResponse(retrofit2.Call<PPAonResponse> call, retrofit2.Response<PPAonResponse> response) {
                    try {
//                    Log.e("MenuMainTest", "res/list/size: " + response.body().getList().size());
                        Log.e("MenuMainTest", "test");
                        setPPA(response.body().getList());
                    } catch (Exception e) {
                        Log.e("MenuMainTest", "Exception e: " + e);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<PPAonResponse> call, Throwable t) {
                    Log.e("MenuMainTest", "test.t:" + t);
                }
            });
        } catch (Exception e) {
            Log.e("MenuMainTest", "Exception e.t:" + e);
        }
    }


    private void downloadTAR() {
        try {
            TasksAndReclamationsRequest data = new TasksAndReclamationsRequest();
            data.mod = "reclamation";
            data.act = "list";

            data.test = "1";

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<TasksAndReclamationsResponce> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_TasksAndReclamations(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<TasksAndReclamationsResponce>() {
                @Override
                public void onResponse(retrofit2.Call<TasksAndReclamationsResponce> call, retrofit2.Response<TasksAndReclamationsResponce> response) {
                    try {
                        TasksAndReclamationsRealm.setTasksAndReclamations(response.body().getList());
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<TasksAndReclamationsResponce> call, Throwable t) {
                }
            });
        } catch (Exception e) {
        }
    }


    private void downloadTARComments() {
        try {
            TasksAndReclamationsRequest data = new TasksAndReclamationsRequest();
            data.mod = "reclamation";
            data.act = "list_comment";
// добавил 1 день!
            data.date_from = Clock.getHumanTimeYYYYMMDD(Clock.getDateLong(-20).getTime() / 1000);
            data.date_to = Clock.getHumanTimeYYYYMMDD(Clock.getDateLong(3).getTime() / 1000);

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<TARCommentsResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_ReclamationComments(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<TARCommentsResponse>() {
                @Override
                public void onResponse(retrofit2.Call<TARCommentsResponse> call, retrofit2.Response<TARCommentsResponse> response) {
                    try {
                        Log.e("downloadTARComments", "response" + response.body());
                        if (response.body() != null && response.body().getList() != null && response.body().getList().size() > 0) {
                            Globals.writeToMLOG("ERROR", "downloadTARComments/onResponse", "response.body().getList(): " + response.body().getList().size());
                            TARCommentsRealm.setTARCommentsDB(response.body().getList());
                        }
                    } catch (Exception e) {
                        Log.e("downloadTARComments", "onResponse/Exception e" + e);
                        Globals.writeToMLOG("ERROR", "downloadTARComments/onResponse/catch", "Exception e: " + e);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<TARCommentsResponse> call, Throwable t) {
                    Log.e("downloadTARComments", "onFailure/Throwable t" + t);
                    Globals.writeToMLOG("ERROR", "downloadTARComments/onFailure", "Throwable t: " + t);
                }
            });
        } catch (Exception e) {
            Log.e("downloadTARComments", "Exception e" + e);
            Globals.writeToMLOG("ERROR", "downloadTARComments/catch", "Exception e: " + e);
        }
    }


    /**
     * 25.03.2021
     * Скачивание Таблички Тем
     */
    private void downloadTheme() {
        try {
            StandartData data = new StandartData();
            data.mod = "data_list";
            data.act = "theme_list";

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<ThemeTableRespose> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_Theme(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<ThemeTableRespose>() {
                @Override
                public void onResponse(retrofit2.Call<ThemeTableRespose> call, retrofit2.Response<ThemeTableRespose> response) {
                    try {
                        ThemeRealm.setThemeDBTable(response.body().getList());
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ThemeTableRespose> call, Throwable t) {
                }
            });
        } catch (Exception e) {
        }
    }

    /**
     * 12.04.2021
     * Скачивание Дополнительных Требований
     */
    public void downloadAdditionalRequirements() {
        try {
            StandartData data = new StandartData();
            data.mod = "additional_requirements";
            data.act = "list";
//            data.client_id = Можно передать список клиентов с которыми работает пользователь
//            data.date_from = Clock.getDatePeriod(-180);
//            data.date_to = Clock.today;

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            // Pika Пример того как получать сырой объект JSON точнее response чтоб понять что возвращается в результате запроса к Володе
            // также потом можно этот текст обработать на https://www.jsonschema2pojo.org/
            // Еще есть возможность после авторизации выполнить https://merchik.com.ua/mobile_app.php?mod=additional_requirements&act=list
            // то есть получить все данные которые должны прийти этому пользователю (сам запрос еще можно донастроить командной строкой - это как обычный GET)
//            retrofit2.Call<JsonObject> callTest = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
//            callTest.enqueue(new Callback<JsonObject>() {
//                @Override
//                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                    Response<JsonObject> s = response;
//                }
//
//                @Override
//                public void onFailure(Call<JsonObject> call, Throwable t) {
//
//                }
//            });

            retrofit2.Call<AdditionalRequirementsServerData> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_AdditionalRequirementsDB(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<AdditionalRequirementsServerData>() {
                @Override
                public void onResponse(retrofit2.Call<AdditionalRequirementsServerData> call, retrofit2.Response<AdditionalRequirementsServerData> response) {
                    try {
                        AdditionalRequirementsRealm.setDataToDB(response.body().getList());

//                        Set<Integer> uniqueIds = new HashSet<>();
//                        List<AdditionalRequirementsDB> uniqueData = new ArrayList<>();
//                        for (AdditionalRequirementsDB item : response.body().getList()) {
//                            if (!uniqueIds.contains(item.getId())) {
//                                uniqueIds.add(item.getId());
//                                uniqueData.add(item);
//                            }
//                        }
//                        AdditionalRequirementsRealm.setDataToDB(uniqueData);

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
        }
    }


    /**
     * 16.04.2021
     * Скачивание таблички Оценок Доп. Требований
     */
    private void downloadAdditionalRequirementsMarks() {
        try {
            StandartData data = new StandartData();
            data.mod = "additional_requirements";
            data.act = "log";

            data.sotr_id = String.valueOf(Globals.userId);

            data.date_from = String.valueOf(Clock.getDateLong(-60).getTime() / 1000);
            data.date_to = String.valueOf(Clock.getDateLong(0).getTime() / 1000);

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            Log.e("downloadAddReqMarks", "convertedObject: " + convertedObject);

            retrofit2.Call<AdditionalRequirementsMarksServerData> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_AdditionalRequirementsMarksDB(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<AdditionalRequirementsMarksServerData>() {
                @Override
                public void onResponse(retrofit2.Call<AdditionalRequirementsMarksServerData> call, retrofit2.Response<AdditionalRequirementsMarksServerData> response) {
//                    Log.e("downloadAddReqMarks", "Ответ: " + response.body().getList().size());
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                if (response.body().getList() != null && response.body().getList().size() > 0) {
                                    AdditionalRequirementsMarkRealm.setDataToDB(response.body().getList());
                                }
                            }
                        }
                    } catch (Exception e) {
                        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLU.class.downloadAdditionalRequirementsMarks.onResponse.catch. ошибка в данных что вернулись: " + e + "\n");
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<AdditionalRequirementsMarksServerData> call, Throwable t) {
                    Log.e("downloadAddReqMarks", "Throwable: " + t);
                    globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLU.class.downloadAdditionalRequirementsMarks.onFailure. ошибка в выполнении запроса: " + t + "\n");
                }
            });

        } catch (Exception e) {
            Log.e("downloadAddReqMarks", "Exception: " + e);
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.TablesLU.class.downloadAdditionalRequirementsMarks.catch ошибка всего метода: " + e + "\n");
        }
    }


    /**
     * Таблица Мнений
     */
    private void downloadOpinions() {
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "sotr_opinion_list";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<OpinionResponse> call = RetrofitBuilder.getRetrofitInterface().GET_OPINION_ROOM(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new retrofit2.Callback<OpinionResponse>() {
            @Override
            public void onResponse(retrofit2.Call<OpinionResponse> call, retrofit2.Response<OpinionResponse> response) {
                Log.e("downloadOpinions", "Response: " + response.body());

                SQL_DB.opinionDao().insertAll(response.body().list);
            }

            @Override
            public void onFailure(retrofit2.Call<OpinionResponse> call, Throwable t) {
                Log.e("downloadOpinions", "Throwable t: " + t);
            }
        });
    }


    private void downloadOpinions2() {
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "sotr_opinion_list_themes";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<OpinionThemeResponse> call = RetrofitBuilder.getRetrofitInterface().GET_OPINION_THEME_ROOM(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new retrofit2.Callback<OpinionThemeResponse>() {
            @Override
            public void onResponse(retrofit2.Call<OpinionThemeResponse> call, retrofit2.Response<OpinionThemeResponse> response) {
                Log.e("downloadOpinions2", "Response: " + response.body());

                SQL_DB.opinionThemeDao().insertAll(response.body().list);
            }

            @Override
            public void onFailure(retrofit2.Call<OpinionThemeResponse> call, Throwable t) {

            }
        });
    }


    /**
     * 22.06.2021
     * Загрузка в приложение Оборотной ведомости
     */
    private void downloadOborotVed() {
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "oborotved_data";

//        data.

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Log.e("downloadOborotVed", "convertedObject: " + convertedObject);

        retrofit2.Call<OborotVedResponse> call = RetrofitBuilder.getRetrofitInterface().GET_OBOROT_VED_ROOM(RetrofitBuilder.contentType, convertedObject);

        Log.e("downloadOborotVed", "call.request(): " + call.request());
        Log.e("downloadOborotVed", "call.request(): " + call.request().body());

        call.enqueue(new retrofit2.Callback<OborotVedResponse>() {
            @Override
            public void onResponse(retrofit2.Call<OborotVedResponse> call, retrofit2.Response<OborotVedResponse> response) {
                try {
                    SQL_DB.oborotVedDao().insertData(response.body().list);
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "FUNC: downloadOborotVed/onResponse", "Exception e: " + e);
                }

            }

            @Override
            public void onFailure(retrofit2.Call<OborotVedResponse> call, Throwable t) {
                Log.e("downloadOborotVed", "Throwable t: " + t);
            }
        });
    }


    /**
     * 19.07.2021
     * Загрузка в приложение Отделов
     */
    private void downloadtovar_grp_client() {
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "client_tovar_group_list";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("INFO", "downloadtovar_grp_client", "convertedObject: " + convertedObject);

        retrofit2.Call<TovarGroupClientResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_TOVAR_GROUP_CLIENT(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new retrofit2.Callback<TovarGroupClientResponse>() {
            @Override
            public void onResponse(retrofit2.Call<TovarGroupClientResponse> call, retrofit2.Response<TovarGroupClientResponse> response) {
                try {
                    SQL_DB.tovarGroupClientDao().insertData(response.body().list).subscribe(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {
                            try {
                                Globals.writeToMLOG("INFO", "downloadtovar_grp_client", "OK");
                                Globals.writeToMLOG("INFO", "downloadtovar_grp_client", "response.body().list: " + response.body().list.size());
                            }catch (Exception e){
                                Globals.writeToMLOG("ERROR", "downloadtovar_grp_client", "Exception e: " + e);
                            }
                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                            Globals.writeToMLOG("ERR", "downloadtovar_grp_client", "Throwable e: " + e);
                        }
                    });
                } catch (Exception e) {
                    Globals.writeToMLOG("ERR", "downloadtovar_grp_client", "Exception e: " + e);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<TovarGroupClientResponse> call, Throwable t) {
                Globals.writeToMLOG("ERR", "downloadtovar_grp_client/onFailure", "Throwable t: " + t);
            }
        });
    }

//    private List<WpDataDB> getWorkPlanList() {
//        RealmResults<WpDataDB> realmResults = getAllWorkPlan(); // Получаем RealmResults
//        return realmResults != null ? new ArrayList<>(realmResults) : new ArrayList<>(); // Преобразуем в List
//    }

}
