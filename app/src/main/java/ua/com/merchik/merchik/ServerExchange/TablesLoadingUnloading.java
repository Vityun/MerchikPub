package ua.com.merchik.merchik.ServerExchange;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.database.realm.RealmManager.getSynchronizationTimetable;
import static ua.com.merchik.merchik.database.realm.tables.PPARealm.setPPA;
import static ua.com.merchik.merchik.database.realm.tables.WpDataRealm.getWpDataAddresses;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.trecker.imHereGPS;
import static ua.com.merchik.merchik.trecker.imHereNET;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.realm.RealmList;
import io.realm.RealmResults;
import kotlin.Unit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Activities.CronchikViewModel;
import ua.com.merchik.merchik.Activities.WorkPlanActivity.feature.helpers.ScrollDataHolder;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.ReclamationPointExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.SamplePhotoExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.ShowcaseExchange;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB;
import ua.com.merchik.merchik.data.Database.Room.WPDataAdditional;
import ua.com.merchik.merchik.data.HashElements;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHints;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjects;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.PPAonResponse;
import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.MenuItemFromWebDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.UsersDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgList;
import ua.com.merchik.merchik.data.RetrofitResponse.models.AddressTableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ArticleTableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.CustomerGroups;
import ua.com.merchik.merchik.data.RetrofitResponse.models.CustomerTableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.CustomerTableResponseList;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ErrorTableResponce;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ImageTypes;
import ua.com.merchik.merchik.data.RetrofitResponse.models.OptionsServer;
import ua.com.merchik.merchik.data.RetrofitResponse.models.PPATableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.PromoTableResponce;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportHint;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SotrTable;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SotrTableList;
import ua.com.merchik.merchik.data.RetrofitResponse.models.TARCommentsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.TasksAndReclamationsResponce;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ThemeTableRespose;
import ua.com.merchik.merchik.data.RetrofitResponse.models.TovarImgResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.TovarTableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.TradeMarkResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.WpDataServer;
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
import ua.com.merchik.merchik.data.UploadToServ.UploadResponse;
import ua.com.merchik.merchik.data.UploadToServ.WPDataAdditionalMapper;
import ua.com.merchik.merchik.data.UploadToServ.WPDataAdditionalServ;
import ua.com.merchik.merchik.data.UploadToServ.WpDataUploadToServ;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TARCommentsRealm;
import ua.com.merchik.merchik.database.realm.tables.TasksAndReclamationsRealm;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;
import ua.com.merchik.merchik.database.room.DaoInterfaces.WPDataAdditionalDao;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;
import ua.com.merchik.merchik.dialogs.features.LoadingDialogWithPercent;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;
import ua.com.merchik.merchik.features.main.Main.MainViewModel;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;


/**
 * 26/02/2021
 * Начинаю считать этот класс устаревающим.
 * Надо переносить с него обмены в Exchange и архитектурить Там уже нормально
 */

/**
 * MERCHIK_1
 * НА 99% працює із базою данних РЕАЛМ
 */
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

    private CronchikViewModel cronchikViewModel;


    /**
     * 18.08.2020
     * <p>
     * Загрузка старым методом всех таблиц
     */
    public void downloadAllTables(Context context, CronchikViewModel viewModel) {
        this.cronchikViewModel = viewModel;
        downloadAllTables(context);
    }

    public void downloadAllTables(Context context) {
        sync = true;

//if (false)
        if (Globals.userId != 172906)
            if (Globals.userId != 19653)
                try {
//            Exchange.sendWpData2();
//            updateWpData();

                    downloadWPData();
//                    downloadWPDataWithCords();
//            donwloadPlanBudgetRNO();
//            donwloadPlanBudget();

//            downloadWPDataRx().subscribe();

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
                    RealmResults<WpDataDB> wpDataDBS = RealmManager.getAllWorkPlan();
                    if (wpDataDBS != null && !wpDataDBS.isEmpty()) {
                        List<WpDataDB> wpDataDBList = INSTANCE.copyFromRealm(wpDataDBS);
                        downloadTovarTable(null, wpDataDBList);
                    }
                    globals.writeToMLOG("_INFO.TablesLoadingUnloading.class.downloadAllTables.Успех.Обязательные таблици." + "\n");
                } catch (Exception e) {
                    globals.writeToMLOG("_INFO.TablesLoadingUnloading.class.downloadAllTables.Ошибка.Обязательные таблици: " + e + "\n");
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
            globals.writeToMLOG("_INFO.TablesLoadingUnloading.class.downloadAllTables.Успех.Не обязательные таблици." + "\n");
        } catch (Exception e) {
            globals.writeToMLOG("_INFO.TablesLoadingUnloading.class.downloadAllTables.Ошибка.Не обязательные таблици: " + e + "\n");
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
                    } catch (Exception e) {
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

//            downloadWPDataWithCords();

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

            globals.writeToMLOG("_INFO.TablesLoadingUnloading.class.downloadAllTables.Успех.Новые таблици." + "\n");
        } catch (Exception e) {
            globals.writeToMLOG("_INFO.TablesLoadingUnloading.class.downloadAllTables.Ошибка.Новые таблици: " + e + "\n");
        }


        globals.testMSG(context);
    }

    private static boolean isdownloadWPData = false;

    public void downloadWPData() {
        if (isdownloadWPData) return;
        isdownloadWPData = true;

        String mod = "plan";
        String act = "list";
        String date_from = Clock.getDatePeriod(-21);
//        String date_from = timeYesterday7;
        String date_to = timeTomorrow;
        long vpi;

        SynchronizationTimetableDB sTable = RealmManager.getSynchronizationTimetableRowByTable("wp_data");
        if (sTable != null) {
            Globals.writeToMLOG("INFO", "TablesLoadingUnloading/downloadWPData/getSynchronizationTimetableRowByTable", "sTable: " + sTable);
            vpi = sTable.getVpi_app();
            Log.e("updateWpData", "vpi: " + vpi);
        } else
            vpi = 0;
//        vpi = 1758173674;
        try {
            Log.e("TAG_TEST_WP", "RESPONSE_0 T");
            Globals.writeToMLOG("INFO", "TablesLoadingUnloading/downloadWPData", "vpi: " + vpi);

            Call<WpDataServer> call = RetrofitBuilder.getRetrofitInterface().GET_WPDATA_VPI(mod, act, date_from, date_to, vpi);
            call.enqueue(new Callback<WpDataServer>() {
                @Override
                public void onResponse(Call<WpDataServer> call, Response<WpDataServer> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {

                            downloadWPDataWithCords();
                            if (response.body().getState() && response.body().getList() != null
                                    && !response.body().getList().isEmpty()) {
                                List<WpDataDB> wpDataDBList = response.body().getList();
                                Globals.writeToMLOG("INFO", "TablesLoadingUnloading/downloadWPData/onResponse", "wpDataDBList.size(): " + wpDataDBList.size());
//                            RealmManager.setWpDataAuto2(wpDataDBList);
//                            RealmManager.setWpData(wpDataDBList);
                                if (wpDataDBList.isEmpty()) {
                                    isdownloadWPData = false;
                                    readyWPData = true;
                                    return;
                                }
                                RealmManager.updateWorkPlanFromServer(wpDataDBList);
                                downloadTovarTable(null, wpDataDBList);
                                INSTANCE.executeTransaction(realm -> {
                                    if (sTable != null) {
                                        sTable.setVpi_app((System.currentTimeMillis() / 1000) - 60);
                                        realm.copyToRealmOrUpdate(sTable);
                                    }
                                });
                                RoomManager.SQL_DB.initStateDao().markWpLoaded();
                            } else {
                                RoomManager.SQL_DB.initStateDao().markWpLoaded();
                                isdownloadWPData = false;
                            }

                        }
                        isdownloadWPData = false;
                        readyWPData = true;
                    } catch (Exception e) {
                        isdownloadWPData = false;
                        readyWPData = true;
                    }
                    isdownloadWPData = false;
                }

                @Override
                public void onFailure(Call<WpDataServer> call, Throwable t) {
//                    if (pg != null)
//                        if (pg.isShowing())
//                            pg.dismiss();
                    readyWPData = false;
                    syncInternetError = true;
                    isdownloadWPData = false;
                }
            });
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "TablesLoadingUnloading/downloadWPData", "Exception: " + e.getMessage());
            readyWPData = true;
            isdownloadWPData = false;
        }

        Log.e("SERVER_REALM_DB_UPDATE", "===================================downloadWPData_END");

    }

    public void downloadWPDataWithCords() {

        StandartData data = new StandartData();

        data.mod = "plan";
        data.act = "list";
        data.date_from = Clock.getDatePeriod(-3);
        data.date_to = Clock.getDatePeriod(3);

        long vpi;
//        SynchronizationTimetableDB sTable = RealmManager.getSynchronizationTimetableRowByTable("wp_data");
//        if (sTable != null) {
//            Globals.writeToMLOG("INFO", "TablesLoadingUnloading/downloadWPData/getSynchronizationTimetableRowByTable", "sTable: " + sTable);
//            vpi = sTable.getVpi_app();
//            Log.e("updateWpData", "vpi: " + vpi);
//        } else
        vpi = 0;

//        data.dt_change_from = String.valueOf(vpi);


        try {

            double coordX, coordY;
            // X (широта)
            if (Globals.CoordX == 0) {
                if (imHereGPS != null) {
                    coordX = imHereGPS.getLatitude();
                } else if (imHereNET != null) {
                    coordX = imHereNET.getLatitude();
                } else {
                    coordX = 0; // fallback если нет координат
                }
            } else {
                coordX = Globals.CoordX;
            }

// Y (долгота)
            if (Globals.CoordY == 0) {
                if (imHereGPS != null) {
                    coordY = imHereGPS.getLongitude();
                } else if (imHereNET != null) {
                    coordY = imHereNET.getLongitude();
                } else {
                    coordY = 0;
                }
            } else {
                coordY = Globals.CoordY;
            }

            if (coordX == 0)
                data.additional_works_location_x = "50.454698130193506";
            else
                data.additional_works_location_x = String.valueOf(coordX);
            if (coordY == 0)
                data.additional_works_location_y = "30.593419371718536";
            else
                data.additional_works_location_y = String.valueOf(coordY);

//            data.additional_works_location_x = "50.454698130193506";
//            data.additional_works_location_y = "30.593419371718536";
            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            Globals.writeToMLOG("INFO", "TablesLoadingUnloading/downloadWPData", "convertedObject: " + convertedObject);

            Call<JsonObject> test = RetrofitBuilder.getRetrofitInterface().SEND_WP_DATA_JSON(RetrofitBuilder.contentType, convertedObject);
            test.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    JsonObject object = response.body();
                    Log.e("Result", "result: " + object);
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });

            Call<WpDataServer> call = RetrofitBuilder.getRetrofitInterface().GET_WPDATA_VPI(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<WpDataServer>() {
                @Override
                public void onResponse(Call<WpDataServer> call, Response<WpDataServer> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {

                            if (response.body().getState() && response.body().getList() != null
                                    && !response.body().getList().isEmpty()) {
                                List<WpDataDB> wpDataDBList = response.body().getList();
//                                List<WpDataDB> wpDataDBListRNO = new ArrayList<>();
//                                for (WpDataDB wpDataDB : wpDataDBList) {
//                                    if (wpDataDB.getUser_id() == 176053)
//                                        Log.e("!!!!!!!!!!", "+++++++++++");
//                                }
                                HashElements he = response.body().getHashElements();
                                Map<String, String> addrMap = he != null ? he.getAddrId() : null;
                                Map<String, String> clientMap = he != null ? he.getClientId() : null;
                                Map<String, String> dad2Map = he != null ? he.getCodeDad2() : null;
                                downloadAddressTableByHash(addrMap);
                                downloadClientTableByHash(clientMap);
                                downloadOptionTableByHash(dad2Map);
                                downloadReportPrepearByHash(dad2Map);

                                List<String> clientId = new ArrayList<>(clientMap.size());
                                List<String> dad2 = new ArrayList<>(dad2Map.size());
                                for (Map.Entry<String, String> e : clientMap.entrySet()) {
                                    clientId.add(e.getKey());
                                }
                                for (Map.Entry<String, String> e : dad2Map.entrySet()) {
                                    dad2.add(e.getKey());
                                }
                                downloadTovarTableDad2(dad2);
                                Globals.writeToMLOG("INFO", "TablesLoadingUnloading/downloadWPData/onResponse", "wpDataDBList.size(): " + wpDataDBList.size());
//                            RealmManager.setWpDataAuto2(wpDataDBList);
//                            RealmManager.setWpData(wpDataDBList);
                                RealmManager.updateWorkPlanFromServer(wpDataDBList);
                                downloadTovarTable(null, wpDataDBList);
//                                RealmManager.INSTANCE.executeTransaction(realm -> {
//                                    if (sTable != null) {
//                                        sTable.setVpi_app((System.currentTimeMillis() / 1000) + 5);
//                                        realm.copyToRealmOrUpdate(sTable);
//                                    }
//                                });

                            }
                        }
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onFailure(Call<WpDataServer> call, Throwable t) {

                }
            });
        } catch (Exception e) {
        }

        Log.e("SERVER_REALM_DB_UPDATE", "===================================downloadWPData_END");

    }

    public void downloadWPDataWithCordsMy() {

        StandartData data = new StandartData();

        data.mod = "plan";
        data.act = "list";
        data.date_from = Clock.getDatePeriod(-1);
        data.date_to = Clock.getDatePeriod(1);


        try {

            double coordX, coordY;
            // X (широта)
            if (Globals.CoordX == 0) {
                if (imHereGPS != null) {
                    coordX = imHereGPS.getLatitude();
                } else if (imHereNET != null) {
                    coordX = imHereNET.getLatitude();
                } else {
                    coordX = 0; // fallback если нет координат
                }
            } else {
                coordX = Globals.CoordX;
            }

// Y (долгота или высота — уточни!)
            if (Globals.CoordY == 0) {
                if (imHereGPS != null) {
                    coordY = imHereGPS.getLongitude(); // ⚠️ лучше использовать getLongitude()
                } else if (imHereNET != null) {
                    coordY = imHereNET.getLongitude();
                } else {
                    coordY = 0;
                }
            } else {
                coordY = Globals.CoordY;
            }

//            data.additional_works_location_x = String.valueOf(coordX);
//            data.additional_works_location_y = String.valueOf(coordY);
            data.additional_works_location_x = "50.45478805335162";
            data.additional_works_location_y = "30.593345204219315";
            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            Globals.writeToMLOG("INFO", "TablesLoadingUnloading/downloadWPData", "convertedObject: " + convertedObject);

            Call<WpDataServer> call = RetrofitBuilder.getRetrofitInterface().GET_WPDATA_VPI(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<WpDataServer>() {
                @Override
                public void onResponse(Call<WpDataServer> call, Response<WpDataServer> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {

                            if (response.body().getState() && response.body().getList() != null
                                    && !response.body().getList().isEmpty()) {
                                List<WpDataDB> wpDataDBList = response.body().getList();
//                                List<WpDataDB> wpDataDBListRNO = new ArrayList<>();
//                                for (WpDataDB wpDataDB : wpDataDBList) {
//                                    if (wpDataDB.getUser_id() == 14041)
//                                        wpDataDBListRNO.add(wpDataDB);
//                                }

                                Globals.writeToMLOG("INFO", "TablesLoadingUnloading/downloadWPData/onResponse", "wpDataDBList.size(): " + wpDataDBList.size());
//                            RealmManager.setWpDataAuto2(wpDataDBList);
//                            RealmManager.setWpData(wpDataDBList);
                                RealmManager.updateWorkPlanFromServer(wpDataDBList);
                                downloadTovarTable(null, wpDataDBList);
//                                RealmManager.INSTANCE.executeTransaction(realm -> {
//                                    if (sTable != null) {
//                                        sTable.setVpi_app((System.currentTimeMillis() / 1000) + 5);
//                                        realm.copyToRealmOrUpdate(sTable);
//                                    }
//                                });

                            }
                        }
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onFailure(Call<WpDataServer> call, Throwable t) {

                }
            });
        } catch (Exception e) {
        }

        Log.e("SERVER_REALM_DB_UPDATE", "===================================downloadWPData_END");

    }

    //    04.08.2025 новый метод для получения таблицы доп заработков
    public void donwloadPlanBudget() {

        StandartData data = new StandartData();

        data.mod = "plan_budget";
        data.act = "wp_data_request_list";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        RetrofitBuilder.getRetrofitInterface()
                .GET_WP_DATA_ADDITIONAL(RetrofitBuilder.contentType, convertedObject)
                .subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    boolean ok = result != null
                            && Boolean.TRUE.equals(result.state)
                            && result.list != null
                            && !result.list.isEmpty();

                    if (!ok) {
                        return Single.error(new NoSuchElementException("Empty/invalid data"));
                    }

                    // используем DAO Completable напрямую
                    return SQL_DB.wpDataAdditionalDao()
                            .insertAll(result.list)              // Completable от Room
                            .andThen(Single.just(result.list.size())); // после вставки вернуть размер

                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        size -> {
                            Log.e("!!!!!!!!!!!!!", "inserted size: " + size);
                            Globals.writeToMLOG("INFO",
                                    "PlanogrammTableExchange.donwloadPlanBudget",
                                    "Data inserted successfully. Size: " + size);
                        },
                        throwable -> {
                            Globals.writeToMLOG("ERROR",
                                    "PlanogrammTableExchange.donwloadPlanBudget",
                                    "exception: " + throwable.getMessage());
                        }
                );

    }

    private static final AtomicBoolean DOWNLOAD_PLAN_BUDGET_RUNNING = new AtomicBoolean(false);

    public void donwloadPlanBudgetForConfirmDecision(Activity context, Runnable onFinish) {

        // Если уже выполняется — выходим
        if (!DOWNLOAD_PLAN_BUDGET_RUNNING.compareAndSet(false, true)) {
            Log.i("donwloadPlanBudget", "Already running — skipped.");
            return;
        }

        // Сначала проверим, есть ли записи с confirm_decision = 0
        Single.fromCallable(() -> SQL_DB.wpDataAdditionalDao().getNotConfirmDecision())
                .subscribeOn(Schedulers.io())
                .flatMap(listBefore -> {
                    if (listBefore == null || listBefore.isEmpty()) {
                        Log.e("!!!!!!!!!!!!!", "donwloadPlanBudget: no not-confirmed items -> skipping network request");
                        Globals.writeToMLOG("INFO", "TablesLoadingUnloading.donwloadPlanBudget",
                                "No not-confirmed items, skipping download.");
                        return Single.just(0); // ничего не делаем
                    }

                    // Соберём id тех, кто был в состоянии confirm_decision = 0 до запроса
                    final Set<Long> beforeIds = new HashSet<>();
                    for (WPDataAdditional w : listBefore) beforeIds.add(w.ID);

                    // Есть незаконченные/неподтвержденные — делаем сетевой запрос
                    StandartData data = new StandartData();
                    data.mod = "plan_budget";
                    data.act = "wp_data_request_list";

                    Gson gson = new Gson();
                    String json = gson.toJson(data);
                    JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                    // Выполняем сетевой запрос
                    return RetrofitBuilder.getRetrofitInterface()
                            .GET_WP_DATA_ADDITIONAL(RetrofitBuilder.contentType, convertedObject)
                            .subscribeOn(Schedulers.io())
                            .flatMap(result -> {
                                boolean ok = result != null
                                        && Boolean.TRUE.equals(result.state)
                                        && result.list != null
                                        && !result.list.isEmpty();

                                if (!ok) {
                                    return Single.error(new NoSuchElementException("Empty/invalid data"));
                                }

                                // --- NEW: вычисляем, у каких пришедших записей поменялся исполнитель ---
                                List<WPDataAdditional> incoming = result.list;
                                List<Long> incomingIds = new ArrayList<>(incoming.size());
                                for (WPDataAdditional w : incoming) {
                                    // предполагаем что есть getId()
                                    incomingIds.add(w.ID);
                                }

                                // Сохраняем пришедшие записи в БД
                                return SQL_DB.wpDataAdditionalDao()
                                        .insertAll(result.list) // Completable
                                        .andThen(Single.fromCallable(() -> {
                                            // После вставки читаем количество не подтверждённых записей
                                            List<WPDataAdditional> listAfter = SQL_DB.wpDataAdditionalDao().getNotConfirmDecision();
                                            Set<Long> afterIds = new HashSet<>();
                                            if (listAfter != null) {
                                                for (WPDataAdditional w : listAfter)
                                                    afterIds.add(w.ID);
                                            }

                                            // Найдём id которые были в beforeIds, а теперь отсутствуют -> это подтверждённые (0 -> 1)
                                            Set<Long> confirmedIds = new HashSet<>(beforeIds);
                                            confirmedIds.removeAll(afterIds); // оставшиеся — подтвердившиеся


                                            int confirmedCount = confirmedIds.size();

                                            Log.e("donwloadPlanBudget", "inserted size: " + result.list.size());
                                            Log.e("donwloadPlanBudget", "not-confirm before=" + beforeIds.size() + " after=" + afterIds.size());
                                            Globals.writeToMLOG("INFO",
                                                    "TablesLoadingUnloading.donwloadPlanBudget",
                                                    "Inserted: " + result.list.size() + ". Not-confirm before: " + beforeIds.size() + ", after: " + afterIds.size()
                                                            + ". confirmedCount: " + confirmedCount);

                                            // Если есть confirmedIds — положим их в ScrollDataHolder и вызовем downloadWPData()
                                            if (confirmedCount > 0) {
                                                List<WPDataAdditional> wpDataAdditionalListConfirmed =
                                                        SQL_DB.wpDataAdditionalDao().getByIds(new ArrayList<>(confirmedIds)
                                                        );
                                                Set<Long> goodIds = new HashSet<>();

                                                for (WPDataAdditional wpDataAdditional : wpDataAdditionalListConfirmed) {
                                                    if (wpDataAdditional.action == 1)
                                                        goodIds.add(wpDataAdditional.ID);
                                                }

                                                List<Long> confirmedList = new ArrayList<>(confirmedIds);
                                                ScrollDataHolder.Companion.instance().addIds(confirmedList);

                                                Log.e("donwloadPlanBudget", "some items became confirmed -> launching downloadWPData()");
                                                Globals.writeToMLOG("INFO",
                                                        "TablesLoadingUnloading.donwloadPlanBudget",
                                                        "Detected confirmed items: " + confirmedList);

//                                                try {
//                                                    downloadWPData();
//                                                } catch (Exception e) {
//                                                    Log.e("donwloadPlanBudget", "downloadWPData() threw", e);
//                                                    Globals.writeToMLOG("ERROR",
//                                                            "TablesLoadingUnloading.donwloadPlanBudget",
//                                                            "downloadWPData() exception: " + e.getMessage());
//                                                }
                                            }

                                            // Возвращаем количество подтверждённых элементов — UI будет реагировать только на >0
                                            return confirmedCount;

//                                            int beforeCount = listBefore.size();
//                                            int afterCount = listAfter == null ? 0 : listAfter.size();
//
//                                            Log.e("!!!!!!!!!!!!!", "donwloadPlanBudget: inserted size: " + result.list.size());
//                                            Log.e("!!!!!!!!!!!!!", "donwloadPlanBudget: not-confirm before=" + beforeCount + " after=" + afterCount);
//                                            Globals.writeToMLOG("INFO",
//                                                    "TablesLoadingUnloading.donwloadPlanBudget",
//                                                    "Inserted: " + result.list.size() + ". Not-confirm before: " + beforeCount + ", after: " + afterCount);
//
//                                            // Если уменьшилось — значит кто-то стал confirm_decision = 1 → запускаем downloadWPData()
//                                            if (afterCount < beforeCount) {
//                                                Log.e("!!!!!!!!!!!!!", "donwloadPlanBudget: some items became confirmed -> launching downloadWPData()");
//                                                Globals.writeToMLOG("INFO",
//                                                        "TablesLoadingUnloading.donwloadPlanBudget",
//                                                        "Detected confirmed items, calling downloadWPData()");
//                                                try {
//                                                    // Вызываем метод — он сам должен управлять своими Scheduler'ами
//                                                    downloadWPData();
//                                                } catch (Exception e) {
//                                                    Log.e("!!!!!!!!!!!!!", "donwloadPlanBudget: downloadWPData() threw", e);
//                                                    Globals.writeToMLOG("ERROR",
//                                                            "TablesLoadingUnloading.donwloadPlanBudget",
//                                                            "downloadWPData() exception: " + e.getMessage());
//                                                }
//                                            }
//
//                                            return result.list.size();
                                        }));
                            });
                })

                .doFinally(() -> {
                    // Сбрасываем флаг выполнения в любом случае
                    DOWNLOAD_PLAN_BUDGET_RUNNING.set(false);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        size -> {
                            if (size > 0) {

                                ProgressViewModel progress = new ProgressViewModel(1);
                                LoadingDialogWithPercent loadingDialog = new LoadingDialogWithPercent(context, progress);

                                try {
//                                    Exchange exchange = new Exchange();
//                                    exchange.setContext(context);
//                                    Exchange.exchangeTime = 0;
//                                    exchange.startExchange();
                                    new TablesLoadingUnloading().downloadAllTables(context);

                                    loadingDialog.show();
                                    progress.onNextEvent("Виконую Синхронізацію з сервером", 7_700);
//                        exchange.uploadTARComments(null);
                                } catch (Exception e) {
                                    Log.d("test", "test" + e);
                                    Globals.writeToMLOG("ERROR", "TOOBAR/CLICK_EXCHANGE/Exchange", "Exception e: " + e);
                                    Globals.writeToMLOG("ERROR", "TOOBAR/CLICK_EXCHANGE/Exchange", "Exception e: " + Arrays.toString(e.getStackTrace()));
                                }

                                try {
                                    SamplePhotoExchange samplePhotoExchange = new SamplePhotoExchange();
                                    List<Integer> listPhotosToDownload = samplePhotoExchange.getSamplePhotosToDownload();
                                    if (listPhotosToDownload != null && !listPhotosToDownload.isEmpty()) {
                                        Globals.writeToMLOG("INFO", "TOOBAR/CLICK_EXCHANGE/SamplePhotoExchange", "listPhotosToDownload: " + listPhotosToDownload.size());
//                            BlockingProgressDialog progress = new BlockingProgressDialog(this, "Ідентифікатори фото", "Починаю завантажувати " + listPhotosToDownload.size() + " ідентифікаторів фото. Це може зайняти деякий час.");
//                            progress.show();
                                        samplePhotoExchange.downloadSamplePhotosByPhotoIds(listPhotosToDownload, new Clicks.clickStatusMsg() {
                                            @Override
                                            public void onSuccess(String data) {
                                                Globals.writeToMLOG("INFO", "TOOBAR/CLICK_EXCHANGE/SamplePhotoExchange", "data: " + data);
//                                    progress.dismiss();
                                                progress.onNextEvent("");
                                                progress.onCompleted();
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                Globals.writeToMLOG("INFO", "TOOBAR/CLICK_EXCHANGE/SamplePhotoExchange", "error: " + error);
                                                progress.onNextEvent("");
                                                progress.onCompleted();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    Globals.writeToMLOG("ERROR", "TOOBAR/CLICK_EXCHANGE/SamplePhotoExchange", "Exception e: " + e);
                                    Globals.writeToMLOG("ERROR", "TOOBAR/CLICK_EXCHANGE/SamplePhotoExchange", "Exception e: " + Arrays.toString(e.getStackTrace()));
                                }

                                // 08.11.23. Загрузка принудительная Образцов фото.
                                try {
                                    /**MERCHIK_1
                                     * Зверни увагу на примусове завантаження і цих типів фото
                                     * Мені прям дуже кажется що сюди треба дивитись в першу чергу*/
                                    ShowcaseExchange showcaseExchange = new ShowcaseExchange();
                                    List<ShowcaseSDB> list = showcaseExchange.getSamplePhotosToDownload();
                                    if (list != null && list.size() > 0) {
                                        Globals.writeToMLOG("INFO", "TOOBAR/CLICK_EXCHANGE/ShowcaseExchange", "list: " + list.size());
                                        showcaseExchange.downloadShowcasePhoto(list);
                                    } else {
                                        Toast.makeText(context, "Всі вітрини вже завантажені!", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Globals.writeToMLOG("ERROR", "TOOBAR/CLICK_EXCHANGE/ShowcaseExchange", "Exception e: " + e);
                                    Globals.writeToMLOG("ERROR", "TOOBAR/CLICK_EXCHANGE/ShowcaseExchange", "Exception e: " + Arrays.toString(e.getStackTrace()));
                                }

                                try {
                                    /**MERCHIK_1
                                     * А це наче завантажуються фото за минулі роботи які виконував мерчандайзер,
                                     * але перевстановив додаток та загубив ці фото*/
//                        PhotoMerchikExchange photoMerchikExchange = new PhotoMerchikExchange();
//                        photoMerchikExchange.getPhotoFromSite();
                                } catch (Exception e) {
                                    Globals.writeToMLOG("ERROR", "TOOBAR/CLICK_EXCHANGE/ShowcaseExchange", "Exception e: " + e);
                                    Globals.writeToMLOG("ERROR", "TOOBAR/CLICK_EXCHANGE/ShowcaseExchange", "Exception e: " + Arrays.toString(e.getStackTrace()));
                                }
                                loadingDialog.setOnDismissListener(() -> {
                                    new MessageDialogBuilder(context)
                                            .setTitle("Зміни в планi робіт")
                                            .setStatus(DialogStatus.NORMAL)
                                            .setSubTitle("Вам підтвердили роботу")
                                            .setMessage("Надійшло підтвердження за заявкою, яку ви подали. Щоб переглянути нову роботу, натисніть 'Ок'.")
                                            .setOnCancelAction(context.getText(R.string.ui_cancel).toString(), () -> Unit.INSTANCE)
                                            .setOnConfirmAction(() -> {

                                                onFinish.run();
//                                                mainViewModel.updateContent();

//                                                Intent intent = new Intent(context, WPDataActivity.class);
//                                                intent.putExtra("initialOpent", false);
//                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                context.startActivity(intent);
                                                return Unit.INSTANCE;
                                            })
                                            .show();
                                });
                                Log.e("!!!!!!!!!!!!!", "donwloadPlanBudget: Data inserted successfully. Size: " + size);
                                Globals.writeToMLOG("INFO",
                                        "PlanogrammTableExchange.donwloadPlanBudget",
                                        "Data inserted successfully. Size: " + size);
                            } else {
                                Log.e("!!!!!!!!!!!!!", "donwloadPlanBudget: No network action performed (no not-confirmed items).");
                            }
                        },
                        throwable -> {
                            Globals.writeToMLOG("ERROR",
                                    "TablesLoadingUnloading.donwloadPlanBudget",
                                    "exception: " + throwable.getMessage());
                            Log.e("!!!!!!!!!!!!!", "donwloadPlanBudget: exception", throwable);
                        }
                );
    }

    // class fields (добавьте в ваш класс)
    private final AtomicBoolean uploadRunning = new AtomicBoolean(false);
    private final AtomicInteger uploadPending = new AtomicInteger(0);
    private final CompositeDisposable disposables = new CompositeDisposable();

    public static class UploadPlanBudgetResult {
        public final int updatedCount;
        public final int autoApprovedCount;

        public UploadPlanBudgetResult(int updatedCount, int autoApprovedCount) {
            this.updatedCount = updatedCount;
            this.autoApprovedCount = autoApprovedCount;
        }
    }

    private final AtomicReference<SingleSubject<UploadPlanBudgetResult>> inFlight =
            new AtomicReference<>(null);

    private final CompositeDisposable disposables2 = new CompositeDisposable();

    public Single<UploadPlanBudgetResult> uploadPlanBudgetRx() {
        SingleSubject<UploadPlanBudgetResult> current = inFlight.get();
        if (current != null) {
            return current; // уже выполняется — подписываемся на тот же результат
        }

        SingleSubject<UploadPlanBudgetResult> subject = SingleSubject.create();
        if (!inFlight.compareAndSet(null, subject)) {
            // кто-то успел раньше
            return inFlight.get();
        }

        Disposable d = startUploadChainRx()
                .doFinally(() -> inFlight.set(null))
                .subscribe(subject::onSuccess, subject::onError);

        disposables.add(d);
        return subject;
    }

    private Single<UploadPlanBudgetResult> startUploadChainRx() {
        WPDataAdditionalDao dao = SQL_DB.wpDataAdditionalDao();

        return Single
                .fromCallable(() -> dao.getUploadToServer())  // список берём в IO
                .subscribeOn(Schedulers.io())
                .flatMap(wpDataAdditionals -> {
                    if (wpDataAdditionals == null || wpDataAdditionals.isEmpty()) {
                        return Single.just(new UploadPlanBudgetResult(0, 0));
                    }

                    List<WPDataAdditionalServ> servs =
                            WPDataAdditionalMapper.mapAll(wpDataAdditionals, Globals.userId);

                    StandartData data = new StandartData();
                    data.mod = "plan_budget";
                    data.act = "wp_data_request_add";
                    data.data = servs;

                    JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(data), JsonObject.class);

                    return RetrofitBuilder.getRetrofitInterface()
                            .UPLOAD_WP_DATA_ADDITIONAL(RetrofitBuilder.contentType, convertedObject)
                            .map(resp -> {
                                List<Pair<Long, Long>> mapping = new ArrayList<>();
                                List<Long> autoApprovedServerIds = new ArrayList<>();

                                if (resp != null && resp.data != null) {
                                    for (UploadResponse.Item it : resp.data) {
                                        if (it == null || !it.state || it.elementId == null || it.serverId == null) continue;

                                        try {
                                            long local = Long.parseLong(it.elementId);
                                            long server = Long.parseLong(it.serverId);
                                            mapping.add(new Pair<>(local, server));

                                            if (it.autoApproved) {
                                                autoApprovedServerIds.add(server);
                                            }
                                        } catch (NumberFormatException ignore) {}
                                    }
                                }

                                return new Object[]{ mapping, autoApprovedServerIds };
                            })
                            .flatMap(arr -> {
                                @SuppressWarnings("unchecked")
                                List<Pair<Long, Long>> mapping = (List<Pair<Long, Long>>) arr[0];
                                @SuppressWarnings("unchecked")
                                List<Long> autoApprovedIds = (List<Long>) arr[1];

                                if (mapping == null || mapping.isEmpty()) {
                                    return Single.just(new UploadPlanBudgetResult(0, 0));
                                }

                                return Completable
                                        .fromAction(() -> {
                                            dao.replaceLocalIdsWithServerIdsSync(mapping);

                                            // Если хочешь прямо тут отметить auto-approved:
                                            // dao.markAutoApprovedSync(autoApprovedIds, System.currentTimeMillis());
                                            // (нужен метод в DAO, см. ниже)
                                        })
                                        .subscribeOn(Schedulers.io())
                                        .andThen(Single.just(new UploadPlanBudgetResult(mapping.size(), autoApprovedIds.size())));
                            });
                });
    }



    public void uploadPlanBudget() {
        // Если уже выполняется — пометим, что есть "отложенный" вызов и выйдем
        if (!uploadRunning.compareAndSet(false, true)) {
            int pending = uploadPending.incrementAndGet();
            Log.i("UPLOAD", "Upload already running — queued pending count = " + pending);
            return;
        }
        // Запускаем реальную цепочку
        startUploadChain();
    }

    private void startUploadChain() {
        List<WPDataAdditional> wpDataAdditionals = SQL_DB.wpDataAdditionalDao().getUploadToServer();
        if (wpDataAdditionals.isEmpty()) {
            // ничего не загружать — сразу сбросим флаг и посмотрим pending
            uploadRunning.set(false);
            int pending = uploadPending.getAndSet(0);
            if (pending > 0 && uploadRunning.compareAndSet(false, true)) {
                // запустить ещё раз
                startUploadChain();
            }
            return;
        }

        List<WPDataAdditionalServ> servs = WPDataAdditionalMapper.mapAll(wpDataAdditionals, Globals.userId);

        StandartData data = new StandartData();
        data.mod = "plan_budget";
        data.act = "wp_data_request_add";
        data.data = servs;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        WPDataAdditionalDao dao = SQL_DB.wpDataAdditionalDao();

        Disposable d = RetrofitBuilder.getRetrofitInterface()
                .UPLOAD_WP_DATA_ADDITIONAL(RetrofitBuilder.contentType, convertedObject) // Single<UploadResponse>
                .map(resp -> {
                    List<Pair<Long, Long>> mapping = new ArrayList<>();
                    if (resp != null && resp.data != null) {
                        for (UploadResponse.Item it : resp.data) {
                            if (it != null && it.state && it.elementId != null && it.serverId != null) {
                                try {
                                    long local = Long.parseLong(it.elementId);
                                    long server = Long.parseLong(it.serverId);
                                    mapping.add(new Pair<>(local, server));
                                } catch (NumberFormatException ignore) {
                                }
                            }
                        }
                    }
                    return mapping;
                })
                .flatMap(mapping -> {
                    if (mapping.isEmpty()) return Single.just(0);
                    return Completable.fromAction(() -> {
                                dao.replaceLocalIdsWithServerIdsSync(mapping);
                            })
                            .subscribeOn(Schedulers.io())
                            .andThen(Single.just(mapping.size()));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
                    // Всегда сбрасываем флаг выполнения. Затем — если были отложенные вызовы — запускаем ещё один цикл.
                    uploadRunning.set(false);

                    int pending = uploadPending.getAndSet(0);
                    if (pending > 0) {
                        Log.i("UPLOAD", "Restarting upload because pending was " + pending);
                        // Если никто не работает — стартуем. (маловероятен race, но защищаемся)
                        if (uploadRunning.compareAndSet(false, true)) {
                            startUploadChain();
                        }
                    }
                })
                .subscribe(
                        updatedCount -> {
                            Log.e("UPLOAD", "uploadStatus=0 проставлен для: " + updatedCount);
                            Globals.writeToMLOG("INFO", "TablesLoading.startUploadChain",
                                    "Updated: " + updatedCount);
                        },
                        throwable -> {
                            Globals.writeToMLOG("ERROR", "TablesLoading.startUploadChain",
                                    "exception: " + throwable.getMessage());
                            Log.e("UPLOAD", "upload exception", throwable);
                        }
                );

        disposables.add(d);
    }


    //    04.08.2025 новый метод для для получения количества доп работ по РНО
    public void donwloadPlanBudgetRNO() {

        StandartData data = new StandartData();

        data.mod = "plan_budget";
        data.act = "additional_works_count";
        data.location_x = String.valueOf(Globals.CoordX);
        data.location_y = String.valueOf(Globals.CoordY);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        RetrofitBuilder.getRetrofitInterface()
                .GET_ADDITIONAL_WORK_COUNT(RetrofitBuilder.contentType, convertedObject)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {

                    if (result != null && result.state
                            && result.count != null && cronchikViewModel != null) {
                        Log.e("!!!!!!!!!!!!!", "result: " + result);

//                        if (cronchikViewModel != null)
//                            cronchikViewModel.updateBadge(1, result.count);

//                        SQL_DB.wpDataAdditionalDao().insertAll(result.list);
                        Globals.writeToMLOG("INFO", "PlanogrammTableExchange.donwloadPlanBudget", "Data inserted successfully. Size: " + "result.list.size()");
                    } else
                        Globals.writeToMLOG("INFO", "PlanogrammTableExchange.donwloadPlanBudget", "data is empty");

                }, throwable -> Globals.writeToMLOG("ERROR", "PlanogrammTableExchange.donwloadPlanBudget", "exeption: " + throwable.getMessage()));
    }


    public void downloadImagesTp() {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================downloadImagesTp_START");
        String mod = "filter_list";
        String act = "menu_list";
        String images_type_list = "";

//        BlockingProgressDialog pg = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: Тип фото");

        Call<ImageTypes> call = RetrofitBuilder.getRetrofitInterface().IMAGE_TYPES_CALL(mod, act, images_type_list);
        call.enqueue(new Callback<ImageTypes>() {
            @Override
            public void onResponse(Call<ImageTypes> call, Response<ImageTypes> response) {
                Log.e("TAG_TEST", "RESPONSE_1" + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getMenuList() != null && response.body().getMenuList().getImagesTypeList() != null) {

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
            public void onFailure(Call<ImageTypes> call, Throwable t) {
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

        Log.e("TAG_TEST_S", "RESPONSE_2" + "ЗАШОЛ)))");
        String mod = "data_list";
        String act = "client_group_list_plain";

        Call<CustomerGroups> call = RetrofitBuilder.getRetrofitInterface().GROUP_TYPE(mod, act);
        call.enqueue(new Callback<CustomerGroups>() {
            @Override
            public void onResponse(Call<CustomerGroups> call, Response<CustomerGroups> response) {
                Log.e("TAG_TEST", "RESPONSE_2: " + response.body());
                try {
                    if (response.isSuccessful() && response.body().getState()) {

                        RealmManager.setGroupTypeV2(response.body().getList());

                    }
                } catch (Exception e) {
//                    RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Ошибка при обмене групп товаров(сервер что-то отправил): " + e, 1097, null, null, null, Globals.userId, null, Globals.session, null)));
                }
                readyTypeGrp = true;
            }

            @Override
            public void onFailure(Call<CustomerGroups> call, Throwable t) {
                readyTypeGrp = false;
                syncInternetError = true;
                Log.e("TAG_TEST", "FAILURE_2 E: " + t);
            }
        });
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

        Call<OptionsServer> call = RetrofitBuilder.getRetrofitInterface().GET_OPTIONS(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<OptionsServer>() {
            @Override
            public void onResponse(Call<OptionsServer> call, Response<OptionsServer> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().getState() && response.body().getList() != null && !response.body().getList().isEmpty()) {
                            RealmManager.setOptions2(response.body().getList());
                            click.click(response.body().getList());
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

    private static boolean isDownloadOptions = false;

    public void downloadOptions() {
        if (isDownloadOptions) return;
        isDownloadOptions = true;
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadOptions.START");
        globals.writeToMLOG("_INFO.TablesLU.class.downloadOptions.ENTER\n");

//        StandartData standartData = new StandartData();
//        standartData.mod = "report_prepare";
//        standartData.act = "list_data";

        StandartData standartData = new StandartData();
        standartData.mod = "plan";
        standartData.act = "options_list";
        standartData.date_from = timeYesterday7;
        standartData.date_to = timeTomorrow;

//        String mod = "plan";
//        String act = "options_list";
//        String date_from = Clock.getDatePeriod(-14);

//        #### TODO dt_change_from
//        SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.INSTANCE.copyFromRealm(RealmManager.getSynchronizationTimetableRowByTable("options_list"));
//        standartData.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());

        Gson gson = new Gson();
        String json = gson.toJson(standartData);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Call<OptionsServer> call = RetrofitBuilder.getRetrofitInterface()
                .GET_OPTIONS(RetrofitBuilder.contentType, convertedObject);
//                .OPTIONS_CALL(mod, act, date_from, date_to);
        call.enqueue(new Callback<OptionsServer>() {
            @Override
            public void onResponse(Call<OptionsServer> call, Response<OptionsServer> response) {
                try {
                    globals.writeToMLOG("_INFO.TablesLU.class.downloadOptions.onResponse.ENTER\n");
                    if (response.isSuccessful() && response.body() != null) {
                        RoomManager.SQL_DB.initStateDao().markOptionsLoaded();

                        Log.e("SERVER_REALM_DB_UPDATE", "===================================downloadOptions_:" + response.body().getState() + "/" + response.body().getError());
                        globals.writeToMLOG("_INFO.TablesLU.class.downloadOptions.response.isSuccessful(): " + response.isSuccessful());

                        if (response.body().getList() != null && !response.body().getList().isEmpty()) {
                            globals.writeToMLOG("_INFO.TablesLU.class.downloadOptions.размер ответа: " + response.body().getList().size());
                            RealmManager.setOptions(response.body().getList());


//                            RealmManager.setOptions2(response.body().getList());
//                            RealmManager.INSTANCE.executeTransaction(realm -> {
//                                synchronizationTimetableDB.setVpi_app((System.currentTimeMillis() / 1000) + 10);
//                                realm.copyToRealmOrUpdate(synchronizationTimetableDB);
//                            });
                        }

                    }
                } finally {
                    readyOptions = true;
                    isDownloadOptions = false;
                }
            }

            @Override
            public void onFailure(Call<OptionsServer> call, Throwable t) {
                globals.writeToMLOG("_INFO.TablesLU.class.downloadOptions.onFailure.ENTER\n");
//                if (pg != null)
//                    if (pg.isShowing())
//                        pg.dismiss();
                isDownloadOptions = false;
                readyOptions = false;
                syncInternetError = true;
                Log.e("TAG_TEST", "FAILURE_3 E: " + t);
            }
        });
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadOptions.END");

    }


    /**
     * ЗАГРУЗКА ТАБЛИЦИ report_prepare С СЕРВЕРА
     * <p>
     * //     * @param context -- Контекст где будет отображаться окно прогресса загрузки таблици
     *
     * @param mode -- Режим работы. Если 0 - всё затераем, 1 - "умная" загрузка(обновление)
     */
    public void downloadReportPrepare(int mode) {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadReportPrepare.START");

        String date_from = timeYesterday7;
        String date_to = timeTomorrow;

        // Получение значения ВПО (время последнего обмена) для того что б с сервера отправились новые данные
        long lastUpdate = 0;
        SynchronizationTimetableDB realmResults = INSTANCE.copyFromRealm(RealmManager.getSynchronizationTimetableRowByTable("report_prepare"));
//        SynchronizationTimetableDB realmResults = RealmManager.getSynchronizationTimetableRowByTable("report_prepare");
//        if (realmResults != null) lastUpdate = realmResults.getVpo_export();

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
//        if (lastUpdate != 0) data.vpo = lastUpdate;
        if (addressIds != null && addressIds.size() > 0)
            data.addr_id = addressIds;

        data.dt_change_from = String.valueOf(realmResults != null ? realmResults.getVpi_app() : 0);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Call<ReportPrepareServer> call = RetrofitBuilder.getRetrofitInterface().ReportPrepareServer_RESPONSE(RetrofitBuilder.contentType, convertedObject);

        call.enqueue(new Callback<ReportPrepareServer>() {
            @Override
            public void onResponse(@NonNull Call<ReportPrepareServer> call, @NonNull Response<ReportPrepareServer> response) {
                Log.e("TAG_TEST", "RESPONSE_4");
                Log.e("downloadReportPrepare", "downloadReportPrepare.response: " + response);

                Globals.writeToMLOG("INFO", "downloadReportPrepare/onResponse", "response.isSuccessful(): " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null && response.body().getState()
                        && response.body().getList() != null && !response.body().getList().isEmpty()) {

                    Globals.writeToMLOG("INFO", "downloadReportPrepare/onResponse", "response.body().getState(): " + response.body().getState());

                    if (response.body().getList() != null) {
//                            Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/downloadReportPrepare/onSuccess", "(response.body().getList(): " + response.body().getList().size());
                        Log.e("SERVER_REALM_DB_UPDATE", "===================================.ReportPrepare.SIZE: " + response.body().getList().size());
                        Globals.writeToMLOG("INFO", "downloadReportPrepare/onResponse", "response.body().getList().size(): " + response.body().getList().size());
                        INSTANCE.executeTransaction(realm -> {
                            realmResults.setVpi_app(System.currentTimeMillis() / 1000);
                            realm.copyToRealmOrUpdate(realmResults);
                        });
                    } else {
                        Log.e("SERVER_REALM_DB_UPDATE", "===================================.ReportPrepare.SIZE: NuLL");
                    }
                    RealmManager.setReportPrepare(response.body().getList());
                }
                readyReportPrepare = true;
            }

            @Override
            public void onFailure(@NonNull Call<ReportPrepareServer> call, @NonNull Throwable t) {
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

        Call<CustomerTableResponse> call = RetrofitBuilder.getRetrofitInterface().GET_CUSTOMER_T(mod, act);
        call.enqueue(new Callback<CustomerTableResponse>() {
            @Override
            public void onResponse(Call<CustomerTableResponse> call, Response<CustomerTableResponse> response) {
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
            public void onFailure(Call<CustomerTableResponse> call, Throwable t) {
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

    public void downloadReportPrepearByHash(Map<String, String> addrIdToHash) {
        if (addrIdToHash == null || addrIdToHash.isEmpty()) return;

        StandartData data = new StandartData();

        data.mod = "report_prepare";
        data.act = "list_data";

        List<String> ids = new ArrayList<>(addrIdToHash.size());
        List<String> hashes = new ArrayList<>(addrIdToHash.size());

        for (Map.Entry<String, String> e : addrIdToHash.entrySet()) {
            ids.add(e.getKey());
            hashes.add(e.getValue());
        }

        data.code_dad2 = ids;
        data.code_dad2_hash = hashes;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("INFO", "downloadOptionsByDAD2/convertedObject", "convertedObject: " + convertedObject);

        RetrofitBuilder.getRetrofitInterface()
                .GET_REPORT_PREPEAR_RX(RetrofitBuilder.contentType, convertedObject)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(reportPrepareServer -> {
                    Log.e("!", "__ " + reportPrepareServer);

                    if (reportPrepareServer.getState() && reportPrepareServer.getList() != null
                            && !reportPrepareServer.getList().isEmpty())
                        RealmManager.setReportPrepare(reportPrepareServer.getList());
                });

    }

    public void downloadOptionTableByHash(Map<String, String> addrIdToHash) {
        if (addrIdToHash == null || addrIdToHash.isEmpty()) return;

        StandartData data = new StandartData();

        data.mod = "plan";
        data.act = "options_list";

        List<String> ids = new ArrayList<>(addrIdToHash.size());
        List<String> hashes = new ArrayList<>(addrIdToHash.size());

        for (Map.Entry<String, String> e : addrIdToHash.entrySet()) {
            ids.add(e.getKey());
            hashes.add(e.getValue());
        }

        data.code_dad2 = ids;
        data.code_dad2_hash = hashes;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("INFO", "downloadOptionsByDAD2/convertedObject", "convertedObject: " + convertedObject);

        RetrofitBuilder.getRetrofitInterface()
                .GET_OPTIONS_RX(RetrofitBuilder.contentType, convertedObject)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(optionsServer -> {
                    Log.e("!", "__ " + optionsServer);
                    if (optionsServer.getState() && optionsServer.getList() != null
                            && !optionsServer.getList().isEmpty())
                        RealmManager.setOptions2(optionsServer.getList());

                });

    }

    public void downloadAddressTableByHash(Map<String, String> addrIdToHash) {
        if (addrIdToHash == null || addrIdToHash.isEmpty()) return;

        StandartData data = new StandartData();

        data.mod = "data_list";
        data.act = "addr_list";

        List<String> ids = new ArrayList<>(addrIdToHash.size());
        List<String> hashes = new ArrayList<>(addrIdToHash.size());

        for (Map.Entry<String, String> e : addrIdToHash.entrySet()) {
            ids.add(e.getKey());
            hashes.add(e.getValue());
        }

        data.id = ids;
        data.hash = hashes;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("INFO", "downloadOptionsByDAD2/convertedObject", "convertedObject: " + convertedObject);

        RetrofitBuilder.getRetrofitInterface()
                .GET_ADDRESS_RX(RetrofitBuilder.contentType, convertedObject)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(addressTableResponse -> {
                    if (addressTableResponse.state && addressTableResponse.list != null
                            && !addressTableResponse.list.isEmpty()) {
//                        RealmManager.setRowToAddress(addressTableResponse.getList());
                        SQL_DB.addressDao().insertData(addressTableResponse.list)
                                .subscribeOn(Schedulers.io())
                                .subscribe(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                        Log.e("AddressExchange", "END1");
                                    }

                                    @Override
                                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                        Log.e("AddressExchange", "END1: " + e);
                                    }
                                });
                    }
                });

    }

    public void downloadClientTableByHash(Map<String, String> clientIdToHash) {
        if (clientIdToHash == null || clientIdToHash.isEmpty()) return;

        StandartData data = new StandartData();

        data.mod = "data_list";
        data.act = "client_list";

        List<String> ids = new ArrayList<>(clientIdToHash.size());
        List<String> hashes = new ArrayList<>(clientIdToHash.size());

        for (Map.Entry<String, String> e : clientIdToHash.entrySet()) {
            ids.add(e.getKey());
            hashes.add(e.getValue());
        }

        data.id = ids;
        data.hash = hashes;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("INFO", "downloadOptionsByDAD2/convertedObject", "convertedObject: " + convertedObject);

        RetrofitBuilder.getRetrofitInterface()
                .GET_CLIENT_RX(RetrofitBuilder.contentType, convertedObject)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(clientResponce -> {
                    Log.e("!", "__ " + clientResponce);
                    if (clientResponce != null && clientResponce.state
                            && clientResponce.list != null && !clientResponce.list.isEmpty()) {
//                        CustomerRealm.setAddressTable(clientResponce.list);
                        SQL_DB.customerDao().insertData(clientResponce.list)
                                .subscribeOn(Schedulers.io())
                                .subscribe(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                        Log.e("CustomerExchange", "onComplete OK");
                                    }

                                    @Override
                                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                        Log.e("CustomerExchange", "Throwable e: " + e);
                                    }
                                });
                    }

                });

    }

    /**
     * Обновление таблицы Адресов
     */
    public void downloadAddressTable() {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadAddressTable.START");

        String mod = "data_list";
        String act = "addr_list";

//        BlockingProgressDialog pg = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: Адреса");

        Call<AddressTableResponse> call = RetrofitBuilder.getRetrofitInterface().GET_ADDRESS_T(mod, act);
        call.enqueue(new Callback<AddressTableResponse>() {
            @Override
            public void onResponse(Call<AddressTableResponse> call, Response<AddressTableResponse> response) {
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
            public void onFailure(Call<AddressTableResponse> call, Throwable t) {
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

        Call<SotrTable> call = RetrofitBuilder.getRetrofitInterface().GET_SOTR_T(mod, act);
        call.enqueue(new Callback<SotrTable>() {
            @Override
            public void onResponse(Call<SotrTable> call, Response<SotrTable> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSESotrTable: " + response.body());
                    if (response.body().getState()) {
//                        RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Обмен таблицы Сотрудники. Успех.", 1095, null, null, null, null, null, Globals.session, null)));
                        if (!response.body().getList().isEmpty()) {
                            Log.e("TAG_TABLE", "ListS: 200");

                            ArrayList<UsersDB> list = new ArrayList<UsersDB>();
                            List<SotrTableList> responseList = response.body().getList();

                            for (int i = 0; i < responseList.size(); i++) {
                                if (responseList.get(i).getFio().contains("Примак")) {
                                    SotrTableList sotr = responseList.get(i);
                                    Log.e("!!!!!!!!!!!", "sotr: " + sotr.getFio());
                                }
                                if (responseList.get(i).getWork_start_date() != null && !responseList.get(i).getWork_start_date().isEmpty())
                                    Log.e("!!!!!!!!!!", "sotr_list: " + responseList.get(i).getWork_start_date());
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
                        } else {
//                            if (pg != null)
//                                if (pg.isShowing())
//                                    pg.dismiss();

//                            RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Обмен таблицы Сотрудники. Сотрудники пустые.", 1095, null, null, null, null, null, Globals.session, null)));
                            Log.e("TAG_TABLE", "ListS: empty");
                        }
                    } else {
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
            public void onFailure(Call<SotrTable> call, Throwable t) {
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

        Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().GET_CITY_T(mod, act);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSECityTable: " + response.body());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
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

        Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().GET_OBL_T(mod, act);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSEOblTable: " + response.body());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
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

        Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().GET_ADDRESS_TT_T(mod, act);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSEAddressTTTable: " + response.body());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("TAG_TABLE", "FAILUREAddressTTTable: " + t);
            }
        });
    }


    /**
     * Обновление таблицы: Товаров
     */
    public void downloadTovarTableDad2(List<String> wpDataDBList) {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadTovarTable.START");

        String mod = "data_list";
        String act = "tovar_list";

        ArrayList<String> listId = null;
//##################################
//        тут некорректный фильтр по дате, нужно убрать date_from / date_to
//нужно добавить dt - unixtime (аналог dt_change_from)
        String date_from = Clock.getDatePeriod(-30);
        String date_to = Clock.getDatePeriod(1);


        // Используем Set для автоматического удаления дубликатов
        Set<String> uniqueClientIds = new HashSet<>();
        Set<Long> uniqueDad2 = new HashSet<>();
        Set<String> uniqueTovarRemove = new HashSet<>();

        RealmResults<TovarDB> results = INSTANCE.where(TovarDB.class).findAll();
        if (results != null && !results.isEmpty()) {
            List<TovarDB> tovarDBList = INSTANCE.copyFromRealm(results);
            for (TovarDB tovar : tovarDBList) {
                uniqueTovarRemove.add(tovar.getiD());
            }
        }

        // Проходим по каждому элементу списка wpDataDBList
//        for (WpDataDB wpDataDB : wpDataDBList) {
//            // Добавляем client_id в Set (дубликаты игнорируются)
//            uniqueClientIds.add(wpDataDB.getClient_id());
//            uniqueDad2.add(wpDataDB.getCode_dad2());
//        }

        long vpi;
        SynchronizationTimetableDB realmResults = RealmManager.getSynchronizationTimetableRowByTable("tovar_list");
        if (realmResults != null) {
            Globals.writeToMLOG("INFO", "TablesLoadingUnloading/updateWpData/getSynchronizationTimetableRowByTable", "sTable: " + realmResults);
            vpi = realmResults.getVpi_app();
        } else
            vpi = 0;

        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "tovar_list";
        data.dt = String.valueOf(vpi);
//        data.date_from = date_from;
//        data.date_to = date_to;
//        data.client_id = new ArrayList<>(uniqueClientIds);
        data.code_dad2 = new ArrayList<>(wpDataDBList);
//        if (!uniqueTovarRemove.isEmpty())
//            data.exclude_id = new ArrayList<>(uniqueTovarRemove);

//                Arrays.asList("38283","9382"); //9382

        /*
        фильтры:
id - число или массив кодов товаров
client_id - массив клиентов
dt - ВПИ
deleted - признак удаления (0 - только неудалённые (по умолчанию если не передано, то значение фильтра считает нулём), 1 - только удалённые, 2 - любые)
code_dad2 - число или массив чисел
фильтр id_exclude с массивом кодов товаров для исключения
id_exclude - иди товаров которые есть в приложении
         */

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Call<TovarTableResponse> call;
        if (listId != null) {
            call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_T_ID(mod, act, listId);
        } else {
//            call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_T(mod, act);
            call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_TABLE(RetrofitBuilder.contentType, convertedObject);
        }

//        BlockingProgressDialog finalPg = pg;
//        BlockingProgressDialog finalTovarProgressDialog = tovarProgressDialog;
        call.enqueue(new Callback<TovarTableResponse>() {
            @Override
            public void onResponse(Call<TovarTableResponse> call, Response<TovarTableResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.e("TAG_TABLE", "RESPONSETovarTable: " + response.body());
                        if (response.body().getState()) {
                            List<TovarDB> list = response.body().getList();

                            try {
                                try {
                                    globals.writeToMLOG("_INFO.TablesLU.class.downloadTovarTable.размер ответа: " + list.size() + "\n");
                                } catch (Exception e) {
                                    globals.writeToMLOG("_INFO.TablesLU.class.downloadTovarTable.ответ от сервера.ERROR1: " + e + "\n");
                                }
                            } catch (Exception e) {
                                globals.writeToMLOG("_INFO.TablesLU.class.downloadTovarTable.ответ от сервера.ERROR: " + e + "\n");
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

                            INSTANCE.executeTransaction(realm -> {
                                realmResults.setVpi_app(System.currentTimeMillis() / 1000);
                                realm.copyToRealmOrUpdate(realmResults);
                            });

//                            getTovarImg(list, "small");
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
            public void onFailure(Call<TovarTableResponse> call, Throwable t) {
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

    public void downloadTovarTable(ArrayList<String> listId, List<WpDataDB> wpDataDBList) {
        Log.e("SERVER_REALM_DB_UPDATE", "===================================.downloadTovarTable.START");

        String mod = "data_list";
        String act = "tovar_list";

//##################################
//        тут некорректный фильтр по дате, нужно убрать date_from / date_to
//нужно добавить dt - unixtime (аналог dt_change_from)
        String date_from = Clock.getDatePeriod(-30);
        String date_to = Clock.getDatePeriod(1);


        // Используем Set для автоматического удаления дубликатов
        Set<String> uniqueClientIds = new HashSet<>();
        Set<Long> uniqueDad2 = new HashSet<>();
        Set<String> uniqueTovarRemove = new HashSet<>();

        RealmResults<TovarDB> results = INSTANCE.where(TovarDB.class).findAll();
        if (results != null && !results.isEmpty()) {
            List<TovarDB> tovarDBList = INSTANCE.copyFromRealm(results);
            for (TovarDB tovar : tovarDBList) {
                uniqueTovarRemove.add(tovar.getiD());
            }
        }

        // Проходим по каждому элементу списка wpDataDBList
        for (WpDataDB wpDataDB : wpDataDBList) {
            // Добавляем client_id в Set (дубликаты игнорируются)
            uniqueClientIds.add(wpDataDB.getClient_id());
            uniqueDad2.add(wpDataDB.getCode_dad2());
        }

        long vpi;
        SynchronizationTimetableDB realmResults = RealmManager.getSynchronizationTimetableRowByTable("tovar_list");
        if (realmResults != null) {
            Globals.writeToMLOG("INFO", "TablesLoadingUnloading/updateWpData/getSynchronizationTimetableRowByTable", "sTable: " + realmResults);
            vpi = realmResults.getVpi_app();
        } else
            vpi = 0;

        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "tovar_list";
        data.dt = String.valueOf(vpi);
//        data.date_from = date_from;
//        data.date_to = date_to;
//        data.client_id = new ArrayList<>(uniqueClientIds);
        data.code_dad2 = new ArrayList<>(uniqueDad2);
//        if (!uniqueTovarRemove.isEmpty())
//            data.exclude_id = new ArrayList<>(uniqueTovarRemove);

//                Arrays.asList("38283","9382"); //9382

        /*
        фильтры:
id - число или массив кодов товаров
client_id - массив клиентов
dt - ВПИ
deleted - признак удаления (0 - только неудалённые (по умолчанию если не передано, то значение фильтра считает нулём), 1 - только удалённые, 2 - любые)
code_dad2 - число или массив чисел
фильтр id_exclude с массивом кодов товаров для исключения
id_exclude - иди товаров которые есть в приложении
         */

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Call<TovarTableResponse> call;
        if (listId != null) {
            call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_T_ID(mod, act, listId);
        } else {
//            call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_T(mod, act);
            call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_TABLE(RetrofitBuilder.contentType, convertedObject);
        }

//        BlockingProgressDialog finalPg = pg;
//        BlockingProgressDialog finalTovarProgressDialog = tovarProgressDialog;
        call.enqueue(new Callback<TovarTableResponse>() {
            @Override
            public void onResponse(Call<TovarTableResponse> call, Response<TovarTableResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.e("TAG_TABLE", "RESPONSETovarTable: " + response.body());
                        if (response.body().getState()) {
                            List<TovarDB> list = response.body().getList();

                            try {
                                try {
                                    globals.writeToMLOG("_INFO.TablesLU.class.downloadTovarTable.размер ответа: " + list.size() + "\n");
                                } catch (Exception e) {
                                    globals.writeToMLOG("_INFO.TablesLU.class.downloadTovarTable.ответ от сервера.ERROR1: " + e + "\n");
                                }
                            } catch (Exception e) {
                                globals.writeToMLOG("_INFO.TablesLU.class.downloadTovarTable.ответ от сервера.ERROR: " + e + "\n");
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

                            INSTANCE.executeTransaction(realm -> {
                                realmResults.setVpi_app(System.currentTimeMillis() / 1000);
                                realm.copyToRealmOrUpdate(realmResults);
                            });

//                            getTovarImg(list, "small");
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
            public void onFailure(Call<TovarTableResponse> call, Throwable t) {
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


    public void downloadTovarTableWhithResult(List<WpDataDB> wpDataDBList, Click click) {

        //##################################
//        тут некорректный фильтр по дате, нужно убрать date_from / date_to
//нужно добавить dt - unixtime (аналог dt_change_from)

        String date_from = Clock.getDatePeriod(-30);
        String date_to = Clock.getDatePeriod(1);


        // Используем Set для автоматического удаления дубликатов
        Set<String> uniqueClientIds = new HashSet<>();
        Set<String> uniqueTovarRemove = new HashSet<>();

        for (WpDataDB dataDB : wpDataDBList) {
            RealmResults<TovarDB> results = RealmManager.getTovarListFromReportPrepareByDad2(dataDB.getCode_dad2());
            if (results != null && !results.isEmpty()) {
                List<TovarDB> tovarDBList = INSTANCE.copyFromRealm(results);
                for (TovarDB tovar : tovarDBList) {
                    uniqueTovarRemove.add(tovar.getiD());
                }
            }
        }


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
        data.id_exclude = new ArrayList<>(uniqueTovarRemove);

        /*
        фильтры:
id - число или массив кодов товаров
client_id - массив клиентов
dt - ВПИ
deleted - признак удаления (0 - только неудалённые (по умолчанию если не передано, то значение фильтра считает нулём), 1 - только удалённые, 2 - любые)
code_dad2 - число или массив чисел
фильтр id_exclude с массивом кодов товаров для исключения
id_exclude - иди товаров которые есть в приложении
         */

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Call<TovarTableResponse> call;
        call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_TABLE(RetrofitBuilder.contentType, convertedObject);


        call.enqueue(new Callback<TovarTableResponse>() {
            @Override
            public void onResponse(Call<TovarTableResponse> call, Response<TovarTableResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.e("TAG_TABLE", "RESPONSETovarTable: " + response.body());
                        if (response.body().getState()) {
                            List<TovarDB> list = response.body().getList();
                            try {
                                try {
                                    globals.writeToMLOG("_INFO.TablesLU.class.downloadTovarTable.размер ответа: " + list.size() + "\n");
                                } catch (Exception e) {
                                    globals.writeToMLOG("_INFO.TablesLU.class.downloadTovarTable.ответ от сервера.ERROR1: " + e + "\n");
                                }
                            } catch (Exception e) {
                                globals.writeToMLOG("_INFO.TablesLU.class.downloadTovarTable.ответ от сервера.ERROR: " + e + "\n");
                            }

                            click.onSuccess(list);

                        } else {
                            click.onFailure("На сервере нет товаров которые можно добавить");
                        }
                    } else {

                        click.onFailure("На сервере нет товаров которые можно добавить");

                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "downloadTovarTable/onResponse/Exception", "Exception: " + e);

                    click.onFailure("Ошибка при получении списка товара, передайте скриншот этой ошибки руководителю: " + e.getMessage());

                }
            }

            @Override
            public void onFailure(Call<TovarTableResponse> call, Throwable t) {

                click.onFailure("Ошибка при получении списка товара, передайте скриншот этой ошибки руководителю: " + t.getMessage());

                Log.e("TAG_TABLE", "FAILURETovarTable: " + t);
            }
        });

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

        Call<TovarGroupResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_TOVAR_GROUP(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<TovarGroupResponse>() {
            @Override
            public void onResponse(Call<TovarGroupResponse> call, Response<TovarGroupResponse> response) {
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
            public void onFailure(Call<TovarGroupResponse> call, Throwable t) {
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

        Call<TradeMarkResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TRADE_MARKS_T(mod, act);
        call.enqueue(new Callback<TradeMarkResponse>() {
            @Override
            public void onResponse(Call<TradeMarkResponse> call, Response<TradeMarkResponse> response) {
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
            public void onFailure(Call<TradeMarkResponse> call, Throwable t) {
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

        Call<PPATableResponse> call = RetrofitBuilder.getRetrofitInterface().GET_PPA_T(mod, act);
        call.enqueue(new Callback<PPATableResponse>() {
            @Override
            public void onResponse(Call<PPATableResponse> call, Response<PPATableResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSEPPATable: " + response.body());
                    if (response.body().getState()) {
                        RealmManager.setPPA(response.body().getList());
                    }
                }
            }

            @Override
            public void onFailure(Call<PPATableResponse> call, Throwable t) {
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

        Call<ArticleTableResponse> call = RetrofitBuilder.getRetrofitInterface().GET_ARTICLE_T(mod, act);
        call.enqueue(new Callback<ArticleTableResponse>() {
            @Override
            public void onResponse(Call<ArticleTableResponse> call, Response<ArticleTableResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TAG_TABLE", "RESPONSEArticleTable: " + response.body());
                    if (response.body().getState()) {
                        RealmManager.setArticle(response.body().getList());
                    }
                }
            }

            @Override
            public void onFailure(Call<ArticleTableResponse> call, Throwable t) {
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

        Call<ErrorTableResponce> call = RetrofitBuilder.getRetrofitInterface().GET_ERROR_LIST(mod, act);
        call.enqueue(new Callback<ErrorTableResponce>() {
            @Override
            public void onResponse(Call<ErrorTableResponce> call, Response<ErrorTableResponce> response) {
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
            public void onFailure(Call<ErrorTableResponce> call, Throwable t) {
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

        Call<PromoTableResponce> call = RetrofitBuilder.getRetrofitInterface().GET_PROMO_LIST(mod, act);
        call.enqueue(new Callback<PromoTableResponce>() {
            @Override
            public void onResponse(Call<PromoTableResponce> call, Response<PromoTableResponce> response) {
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
            public void onFailure(Call<PromoTableResponce> call, Throwable t) {
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

        if (imageType == null || imageType.isEmpty()) {
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

        Call<TovarImgResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_PHOTO_INFO(mod, act, tovarOnly, nolimit, imageType, listId);
        String finalImageType = imageType;
        call.enqueue(new Callback<TovarImgResponse>() {
            @Override
            public void onResponse(Call<TovarImgResponse> call, Response<TovarImgResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getList() != null
                        && !response.body().getList().isEmpty()) {
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

//                            downloadTovarImg(list, finalImageType);
                        }
                    } catch (Exception e) {
                        Log.e("LOG", "SAVE_TO_LOG");
                    }
                }
            }

            @Override
            public void onFailure(Call<TovarImgResponse> call, Throwable t) {
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
                Call<ResponseBody> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_PHOTO_BY_URL(list.get(i).getPhotoUrl());
                int finalI = i;
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
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
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
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

        if (true)
            return;

        String mod = "plan";
        String act = "list";
        String date_from = timeYesterday;
        String date_to = timeTomorrow;
        long vpi;

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
                Call<WpDataServer> call = RetrofitBuilder.getRetrofitInterface().GET_WPDATA_VPI(mod, act, date_from, date_to, vpi);
                call.enqueue(new Callback<WpDataServer>() {
                    @Override
                    public void onResponse(Call<WpDataServer> call, Response<WpDataServer> response) {
                        Log.e("TAG_TEST_WP", "RESPONSE_0");
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                if (response.body().getState()) {
                                    Log.e("TAG_TEST_WP", "RESPONSE_OK");
                                    if (response.body().getList() != null && !response.body().getList().isEmpty()) {
                                        RealmManager.updateWorkPlanFromServer(response.body().getList()); // Получаем данные для выгрузки
                                        INSTANCE.executeTransaction(realm -> {
                                            long vpiApp = System.currentTimeMillis() / 1000;
                                            sTable.setVpi_app(vpiApp - 60);
                                            realm.copyToRealmOrUpdate(sTable);
                                        }); //

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
                    public void onFailure(Call<WpDataServer> call, Throwable t) {
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

        if (!data.isEmpty()) {
            Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().LOG(mod, act, data);
            Log.e("LOG_SEND", "call: " + call);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.e("LOG_SEND", "response: " + response.body());

                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JsonObject obj = response.body();
                            JsonObject log = obj.getAsJsonObject("log");
                            if (log != null)
                                for (LogUploadToServ el : data) {
                                    if (log.getAsJsonObject(el.getElement_id()) != null) {
                                        Log.e("LOG_SEND", "JSON: " + log.getAsJsonObject(el.getElement_id()));
                                        LogDB logDB = RealmManager.getLogRowById(el.getElement_id());
                                        INSTANCE.executeTransaction(realm -> {
                                            logDB.setDt(System.currentTimeMillis() / 1000); // Надо сменить на DT из запроса
                                            INSTANCE.copyToRealmOrUpdate(logDB);
                                        });
                                    } else {

                                    }

                                }
                        } catch (Exception e) {
                            Globals.writeToMLOG("ERR", "sendAndUpdateLog", "Exception e: " + e);
                        }

                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
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

        Call<ReportHint> call = RetrofitBuilder.getRetrofitInterface().GET_REPORT_HINT(mod, act, tovarId, codeDad2, clientId);
        call.enqueue(new Callback<ReportHint>() {
            @Override
            public void onResponse(Call<ReportHint> call, Response<ReportHint> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("REPORT_HINT", "" + response.body());

                }
            }

            @Override
            public void onFailure(Call<ReportHint> call, Throwable t) {

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
//            downloadSiteHints("2");
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

                            List<ReportPrepareDB> rp = INSTANCE.copyFromRealm(ReportPrepareRealm.getByIds(ids));

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

                                    List<ReportPrepareDB> rp = INSTANCE.copyFromRealm(ReportPrepareRealm.getByIds(ids));

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
            List<ReportPrepareServ> prepareDBList = RealmManager.getReportPrepareToUpload();
            if (prepareDBList.isEmpty()) {
                exchange.onFailure("uploadRP: Данных на выгрузку нет");
                return;
            }

            StandartData data = new StandartData();
            data.mod = "report_prepare";
            data.act = "set_report_data";
            data.data = prepareDBList;

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            Log.e("uploadRP", "convertedObject: " + convertedObject);
            Globals.writeToMLOG("INFO", "uploadRP().Start", "Size: " + data.data.size());

            if (data.data.size() > 0) {
                Call<ReportPrepareUpdateResponse> call = RetrofitBuilder.getRetrofitInterface().SEND_RP_INFO(RetrofitBuilder.contentType, convertedObject);
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

        if (!data.isEmpty()) {
            StandartData standartData = new StandartData();
            standartData.mod = "report_prepare";
            standartData.act = "set_report_data";
            standartData.data = data;

            Gson gson = new Gson();
            String json = gson.toJson(standartData);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            Call<ReportPrepareUploadResponse> call = RetrofitBuilder.getRetrofitInterface().SEND_RP(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<ReportPrepareUploadResponse>() {
                @Override
                public void onResponse(Call<ReportPrepareUploadResponse> call, Response<ReportPrepareUploadResponse> response) {
                    long currentTime = System.currentTimeMillis() / 1000;
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                if (response.body().state) {
                                    if (response.body().data != null && !response.body().data.isEmpty()) {
                                        for (ReportPrepareUploadList item : response.body().data) {
                                            ReportPrepareDB reportPrepareDB = INSTANCE.copyFromRealm(RealmManager.getReportPrepareRowById(item.elementId));
                                            INSTANCE.executeTransaction(realm -> {
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
                public void onFailure(Call<ReportPrepareUploadResponse> call, Throwable t) {
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

        List<LogMPDB> logMp = INSTANCE.copyFromRealm(RealmManager.getNOTUploadLogMPDB());
//        List<LogMPDB> logMp = RealmManager.INSTANCE.copyFromRealm(RealmManager.getNOTUploadLogMPDBTEST());
        if (logMp != null && !logMp.isEmpty()) {
            Log.e("uploadLodMp", "LogMpUploadText. LogSize: " + logMp.size());

            HashMap<String, String> map = new HashMap<>();
            for (LogMPDB list : logMp) {
                if (list.getGp() != null) {
                    map.put("gp[" + list.getId() + "]", list.getGp());
                }
            }

            Globals.writeToMLOG("INFO", "uploadLodMp", "Количество ЛОГ МП на выгрузку: " + logMp.size());
//            Globals.writeToMLOG("INFO", "uploadLodMp", "Данные на выгрузку: " + map);

            Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().UPLOAD_LOG_MP(mod, act, map/*, debug_param_1*/);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
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
                                                INSTANCE.executeTransaction(realm -> {
                                                    list.serverId = id;
                                                    list.upload = System.currentTimeMillis() / 1000;  // 27.08.23 Вместо удаления, пишу воемя когда координаты были выгружены
                                                    realm.insertOrUpdate(list);
                                                });

                                                Globals.writeToMLOG("INFO", "uploadLodMp/onResponse/executeTransaction.onSuccess", "OK: " + list.serverId);
                                                res.onSuccess("ОК");
                                            } catch (Exception e) {
                                                Globals.writeToMLOG("ERROR", "uploadLodMp/onResponse/executeTransaction", "Exception e: " + e);
                                                res.onFailure("Exception e: " + e);
                                            }
                                        } else {
                                            try {
                                                long id = geoInfo.get("geo_id").getAsLong();
                                                INSTANCE.executeTransaction(realm -> {
                                                    list.serverId = id;
                                                    list.upload = System.currentTimeMillis() / 1000;  // 27.08.23 Вместо удаления, пишу воемя когда координаты были выгружены
                                                    realm.insertOrUpdate(list);
                                                });
                                            } catch (Exception e) {
                                                Globals.writeToMLOG("INFO", "uploadLodMp/onResponse/geoInfo.get(\"state\")", "Exception e: " + e);
                                            }
                                            Globals.writeToMLOG("INFO", "uploadLodMp/onResponse/geoInfo.get(\"state\")", "response.body(): " + response.body());
                                        }
                                    }
                                }
                            } else {
                                Globals.writeToMLOG("INFO", "uploadLodMp/onResponse/state=false", "response.body(): " + response.body());
                            }
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "uploadLodMp/onResponse/onResponse", "Exception e: " + e);
                        res.onFailure("2_Exception e: " + e);
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
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

    public static Map<String, Object> decodeBase64ToMap(String base64) {
        try {
            // Декодируем из base64
            byte[] decodedBytes = Base64.decode(base64, 0);
            String decodedUrl = new String(decodedBytes, "UTF-8");

            // Декодируем URL-encoded строку
            String urlQuery = URLDecoder.decode(decodedUrl, "UTF-8");

            // Парсим query string обратно в Map
            Map<String, Object> resultMap = new HashMap<>();
            String[] pairs = urlQuery.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    resultMap.put(keyValue[0], keyValue[1]);
                }
            }

            return resultMap;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
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

        Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_MENU(mod);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
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
            public void onFailure(Call<JsonObject> call, Throwable t) {
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

    //  ##MenuItemFromWebDB
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

            Call<SiteObjects> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_SITE_HINTS(mod, act, langId);
            call.enqueue(new Callback<SiteObjects>() {
                @Override
                public void onResponse(Call<SiteObjects> call, Response<SiteObjects> response) {
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null && response.body().getState() && response.body().getObjectList() != null && !response.body().getObjectList().isEmpty()) {
//                                Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/downloadSiteHints/onSuccess", "response.body().getObjectList().size(): " + response.body().getObjectList().size());
                                saveSiteObjectsDB(response.body().getObjectList());
                                RoomManager.SQL_DB.initStateDao().markSiteLoaded();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("downloadSiteHints", "Exception e: " + e);
                    }

                }

                @Override
                public void onFailure(Call<SiteObjects> call, Throwable t) {
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

        Call<SiteHints> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_VIDEO_LESSONS(mod, act);
        call.enqueue(new Callback<SiteHints>() {
            @Override
            public void onResponse(Call<SiteHints> call, Response<SiteHints> response) {
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
            public void onFailure(Call<SiteHints> call, Throwable t) {
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


            Call<PPAonResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_PPA(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<PPAonResponse>() {
                @Override
                public void onResponse(Call<PPAonResponse> call, Response<PPAonResponse> response) {
                    try {
//                    Log.e("MenuMainTest", "res/list/size: " + response.body().getList().size());
                        Log.e("MenuMainTest", "test");
                        if (response.isSuccessful())
                            if (response.body() != null && response.body().getList() != null && !response.body().getList().isEmpty())
                                setPPA(response.body().getList());
                    } catch (Exception e) {
                        Log.e("MenuMainTest", "Exception e: " + e);
                    }
                }

                @Override
                public void onFailure(Call<PPAonResponse> call, Throwable t) {
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

            Call<TasksAndReclamationsResponce> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_TasksAndReclamations(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<TasksAndReclamationsResponce>() {
                @Override
                public void onResponse(Call<TasksAndReclamationsResponce> call, Response<TasksAndReclamationsResponce> response) {
                    try {
                        TasksAndReclamationsRealm.setTasksAndReclamations(response.body().getList());
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onFailure(Call<TasksAndReclamationsResponce> call, Throwable t) {
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
            data.date_to = Clock.getHumanTimeYYYYMMDD(Clock.getDateLong(20).getTime() / 1000);

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            Call<TARCommentsResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_ReclamationComments(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<TARCommentsResponse>() {
                @Override
                public void onResponse(Call<TARCommentsResponse> call, Response<TARCommentsResponse> response) {
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
                public void onFailure(Call<TARCommentsResponse> call, Throwable t) {
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

            // #### TODO
            SynchronizationTimetableDB synchronizationTimetableDB = INSTANCE.copyFromRealm(RealmManager.getSynchronizationTimetableRowByTable("theme_list"));
            data.dt = String.valueOf(synchronizationTimetableDB.getVpi_app());

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            Call<ThemeTableRespose> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_Theme(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<ThemeTableRespose>() {
                @Override
                public void onResponse(Call<ThemeTableRespose> call, Response<ThemeTableRespose> response) {
                    try {
                        RoomManager.SQL_DB.initStateDao().markThemeLoaded();
                        if (response.body() != null && response.body().getList() != null && !response.body().getList().isEmpty()) {
                            ThemeRealm.setThemeDBTable(response.body().getList());
                            INSTANCE.executeTransaction(realm -> {
                                synchronizationTimetableDB.setVpi_app(System.currentTimeMillis() / 1000);
                                realm.copyToRealmOrUpdate(synchronizationTimetableDB);
                            });
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERR", "downloadTheme/onResponse", "Exception e: " + e);
                    }
                }

                @Override
                public void onFailure(Call<ThemeTableRespose> call, Throwable t) {
                    Globals.writeToMLOG("ERR", "downloadTheme/onFailure", "onFailure e: " + t.getMessage());
                }
            });
        } catch (Exception e) {
        }
    }

    /**
     * 12.04.2021
     * Скачивание Дополнительных Требований
     */
    private static boolean isDownloadAdditionalRequirements = false;

    public void downloadAdditionalRequirements() {
        if (isDownloadAdditionalRequirements) return;
        isDownloadAdditionalRequirements = true;

        try {
            StandartData data = new StandartData();
            data.mod = "additional_requirements";
            data.act = "list";
//            data.client_id = Можно передать список клиентов с которыми работает пользователь
            data.date_from = Clock.getDatePeriod(-120);
            data.date_to = Clock.tomorrow7;

//            SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.INSTANCE.copyFromRealm(RealmManager.getSynchronizationTimetableRowByTable("additional_requirements"));
//            data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());

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

            Call<AdditionalRequirementsServerData> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_AdditionalRequirementsDB(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<AdditionalRequirementsServerData>() {
                @Override
                public void onResponse(Call<AdditionalRequirementsServerData> call, Response<AdditionalRequirementsServerData> response) {
                    try {
                        if (response.isSuccessful())
                            if (response.body() != null && response.body().getList() != null
                                    && !response.body().getList().isEmpty()) {

                                AdditionalRequirementsRealm.setDataToDB(response.body().getList());

//                                RealmManager.INSTANCE.executeTransaction(realm -> {
//                                    synchronizationTimetableDB.setVpi_app(System.currentTimeMillis() / 1000);
//                                    realm.copyToRealmOrUpdate(synchronizationTimetableDB);
//                                });
                                Globals.writeToMLOG("ERR", "downloadAdditionalRequirements/onResponse", "response.body().getList(): " + response.body().getList().size());

                            }
//                        Set<Integer> uniqueIds = new HashSet<>();
//                        List<AdditionalRequirementsDB> uniqueData = new ArrayList<>();
//                        for (AdditionalRequirementsDB item : response.body().getList()) {
//                            if (!uniqueIds.contains(item.getId())) {
//                                uniqueIds.add(item.getId());
//                                uniqueData.add(item);
//                            }
//                        }
//                        AdditionalRequirementsRealm.setDataToDB(uniqueData);

                        Globals.writeToMLOG("ERR", "downloadAdditionalRequirements/onResponse", "response +");
                        isDownloadAdditionalRequirements = false;
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERR", "downloadAdditionalRequirements/onResponse", "Exception e: " + e);
                        isDownloadAdditionalRequirements = false;
                    }
                }

                @Override
                public void onFailure(Call<AdditionalRequirementsServerData> call, Throwable t) {
                    isDownloadAdditionalRequirements = false;
                    Globals.writeToMLOG("ERR", "downloadAdditionalRequirements/onFailure", "Throwable t: " + t);
                }
            });
        } catch (Exception e) {
            isDownloadAdditionalRequirements = false;
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

            Call<AdditionalRequirementsMarksServerData> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_AdditionalRequirementsMarksDB(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<AdditionalRequirementsMarksServerData>() {
                @Override
                public void onResponse(Call<AdditionalRequirementsMarksServerData> call, Response<AdditionalRequirementsMarksServerData> response) {
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
                        globals.writeToMLOG("_INFO.TablesLU.class.downloadAdditionalRequirementsMarks.onResponse.catch. ошибка в данных что вернулись: " + e + "\n");
                    }
                }

                @Override
                public void onFailure(Call<AdditionalRequirementsMarksServerData> call, Throwable t) {
                    Log.e("downloadAddReqMarks", "Throwable: " + t);
                    globals.writeToMLOG("_INFO.TablesLU.class.downloadAdditionalRequirementsMarks.onFailure. ошибка в выполнении запроса: " + t + "\n");
                }
            });

        } catch (Exception e) {
            Log.e("downloadAddReqMarks", "Exception: " + e);
            globals.writeToMLOG("_INFO.TablesLU.class.downloadAdditionalRequirementsMarks.catch ошибка всего метода: " + e + "\n");
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

        Call<OpinionResponse> call = RetrofitBuilder.getRetrofitInterface().GET_OPINION_ROOM(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<OpinionResponse>() {
            @Override
            public void onResponse(Call<OpinionResponse> call, Response<OpinionResponse> response) {
                Log.e("downloadOpinions", "Response: " + response.body());

                if (response.body() != null && response.body().list != null
                        && response.body().state && !response.body().list.isEmpty())
                    SQL_DB.opinionDao().insertAll(response.body().list);
            }

            @Override
            public void onFailure(Call<OpinionResponse> call, Throwable t) {
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

        Call<OpinionThemeResponse> call = RetrofitBuilder.getRetrofitInterface().GET_OPINION_THEME_ROOM(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<OpinionThemeResponse>() {
            @Override
            public void onResponse(Call<OpinionThemeResponse> call, Response<OpinionThemeResponse> response) {
                Log.e("downloadOpinions2", "Response: " + response.body());
                if (response.body() != null && response.body().list != null
                        && response.body().state && !response.body().list.isEmpty())
                    SQL_DB.opinionThemeDao().insertAll(response.body().list);
            }

            @Override
            public void onFailure(Call<OpinionThemeResponse> call, Throwable t) {

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
//        ########################
        data.dt_change_from = String.valueOf(Clock.getDateLong(-30).getTime() / 1000);
        data.dt_change_to = String.valueOf(Clock.getDateLong(7).getTime() / 1000);
//        data.

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Log.e("downloadOborotVed", "convertedObject: " + convertedObject);

        Call<OborotVedResponse> call = RetrofitBuilder.getRetrofitInterface().GET_OBOROT_VED_ROOM(RetrofitBuilder.contentType, convertedObject);

        Log.e("downloadOborotVed", "call.request(): " + call.request());
        Log.e("downloadOborotVed", "call.request(): " + call.request().body());

        call.enqueue(new Callback<OborotVedResponse>() {
            @Override
            public void onResponse(Call<OborotVedResponse> call, Response<OborotVedResponse> response) {
                try {
                    if (response.body() != null && response.body().list != null
                            && response.body().state && !response.body().list.isEmpty())
                        SQL_DB.oborotVedDao().insertData(response.body().list);
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "FUNC: downloadOborotVed/onResponse", "Exception e: " + e);
                }

            }

            @Override
            public void onFailure(Call<OborotVedResponse> call, Throwable t) {
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

        Call<TovarGroupClientResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_TOVAR_GROUP_CLIENT(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<TovarGroupClientResponse>() {
            @Override
            public void onResponse(Call<TovarGroupClientResponse> call, Response<TovarGroupClientResponse> response) {
                try {
                    if (response.body() != null && response.body().list != null
                            && response.body().state && !response.body().list.isEmpty())
                        SQL_DB.tovarGroupClientDao().insertData(response.body().list).subscribe(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                try {
                                    Globals.writeToMLOG("INFO", "downloadtovar_grp_client", "OK");
                                    Globals.writeToMLOG("INFO", "downloadtovar_grp_client", "response.body().list: " + response.body().list.size());
                                } catch (Exception e) {
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
            public void onFailure(Call<TovarGroupClientResponse> call, Throwable t) {
                Globals.writeToMLOG("ERR", "downloadtovar_grp_client/onFailure", "Throwable t: " + t);
            }
        });
    }

//    private List<WpDataDB> getWorkPlanList() {
//        RealmResults<WpDataDB> realmResults = getAllWorkPlan(); // Получаем RealmResults
//        return realmResults != null ? new ArrayList<>(realmResults) : new ArrayList<>(); // Преобразуем в List
//    }

}
