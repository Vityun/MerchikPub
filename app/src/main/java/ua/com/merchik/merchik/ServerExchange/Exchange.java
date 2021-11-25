package ua.com.merchik.merchik.ServerExchange;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.AddressExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.CityExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.CustomerExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.LanguagesExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.OblastExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.ReclamationPointExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.SiteObjectsExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.StandartExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.TranslationsExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.UsersExchange;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CitySDB;
import ua.com.merchik.merchik.data.Database.Room.ContentSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.LanguagesSDB;
import ua.com.merchik.merchik.data.Database.Room.OblastSDB;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.Database.Room.StandartSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;
import ua.com.merchik.merchik.data.RealmModels.TasksAndReclamationsDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgList;
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.ImagesViewListImageList;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.ImagesViewListImageResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.PhotoInfoResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.PhotoInfoResponseList;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ChatResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.update.wpdata.WpDataUpdateResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.update.wpdata.WpDataUpdateResponseList;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirementsMarks.AdditionalRequirementsMarksListServerData;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirementsMarks.AdditionalRequirementsSendMarksServerData;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.TARCommentData.TARCommentsServerData;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoInformation;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoInformationData;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartDataClasses.MarkData;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartDataClasses.TARUpload;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartDataClasses.TARUploadData;
import ua.com.merchik.merchik.data.TestJsonUpload.StratEndWork.UploadDataSEWork;
import ua.com.merchik.merchik.data.TestJsonUpload.TARCommentDataListUpload;
import ua.com.merchik.merchik.data.TestJsonUpload.TARCommentDataUpload;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.TARCommentsRealm;
import ua.com.merchik.merchik.database.realm.tables.TasksAndReclamationsRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogEKL;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

/**
 * 26.02.2021
 * Этот класс создан на замену текущего TablesLoadingUnloading потому что в последнем скопилось
 * много нехорошего кода и написан не совсем адекватно в принципе.
 * <p>
 * Класс Обмена. Буду по возможности сюда перетаскивать адевкатный функционал по обмену данными с
 * сервером.
 */
public class Exchange {

    private Globals globals = new Globals();

    public Context context;
    private static long exchange = 0;
    //    private int retryTime = 120000;   // 2
    private int retryTime = 600000;     // 10
//    private int retryTime = 60000;     // 1

    /**
     * 26.02.2021
     * Енум для опозначения какие данные мы будем отправлять на всервер.
     */
    enum UploadPhotoInfo {
        DVI, RATING, COMMENT, PRIZE
    }

    public interface ExchangeInt {
        // Отработка успешного результата
        void onSuccess(String msg);

        // Отработка ошибки
        void onFailure(String error);
    }


    /**
     * 26.02.2021
     * Начало Обмена. Внутри находятся все Обмены
     */
    public void startExchange() {
        try {
            Log.e("startExchange", "start");

            if (exchange + retryTime < System.currentTimeMillis()) {
                Log.e("startExchange", "start/Время обновлять наступило");
                exchange = System.currentTimeMillis();

//                String login = PreferenceManager.getDefaultSharedPreferences(context)
//                        .getString("login", "");
//
//                String password = PreferenceManager.getDefaultSharedPreferences(context)
//                        .getString("password", "");
//
//                server.sessionCheckAndLogin(context, login, password);   // Проверка активности сессии и логин, если сессия протухла
//                internetStatus = server.internetStatus();       // Обновление статуса интеренета
//                pingServer(1);                            // ОБМЕН ЦВЕТ



                globals.fixMP();    //


                planogram(new ExchangeInterface.ExchangeResponseInterface() {
                    @Override
                    public <T> void onSuccess(List<T> data) {
                        try {
                            List<ImagesViewListImageList> datalist = (List<ImagesViewListImageList>) data;
                            PhotoDownload.savePhotoToDB2(datalist);
                            Globals.writeToMLOG("INFO", "startExchange/planogram.onSuccess", "OK");
                        } catch (Exception e) {
                            Globals.writeToMLOG("ERROR", "startExchange/planogram.onSuccess", "Exception e: " + e);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Globals.writeToMLOG("FAIL", "startExchange/planogram/onFailure", error);
                    }
                }); // Получение планограмм

                chatExchange();

                if (true) {
                    new AddressExchange().downloadAddressTable(new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {
                            try {
                                Log.e("AddressExchange", "START");
                                SQL_DB.addressDao().insertData((List<AddressSDB>) data)
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
                                Log.e("AddressExchange", "END");

                            } catch (Exception e) {

                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("HEUTE", "1error." + error);
                        }
                    });
                    new CustomerExchange().downloadCustomerTable(new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {
                            try {
                                SQL_DB.customerDao().insertData((List<CustomerSDB>) data)
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(new DisposableCompletableObserver() {
                                            @Override
                                            public void onComplete() {
                                            }

                                            @Override
                                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                            }
                                        });

                            } catch (Exception e) {
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("HEUTE", "2error." + error);
                        }
                    });
                    new UsersExchange().downloadUsersTable(new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {
                            Log.e("downloadUsersTable", "onSuccess: " + data);
                            try {
                                SQL_DB.usersDao().insertData((List<UsersSDB>) data)
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(new DisposableCompletableObserver() {
                                            @Override
                                            public void onComplete() {
                                            }

                                            @Override
                                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                            }
                                        });
                            } catch (Exception e) {

                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("HEUTE", "3error." + error);
                            Log.e("downloadUsersTable", "onFailure: " + error);
                        }
                    });
                    new CityExchange().downloadCityTable(new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {
                            try {
                                SQL_DB.cityDao().insertData((List<CitySDB>) data)
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(new DisposableCompletableObserver() {
                                            @Override
                                            public void onComplete() {
                                            }

                                            @Override
                                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                            }
                                        });
                            } catch (Exception e) {

                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("HEUTE", "4error." + error);
                        }
                    });
                    new OblastExchange().downloadOblastTable(new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {
                            try {
                                SQL_DB.oblastDao().insertData((List<OblastSDB>) data)
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(new DisposableCompletableObserver() {
                                            @Override
                                            public void onComplete() {
                                            }

                                            @Override
                                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                            }
                                        });
                            } catch (Exception e) {

                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("HEUTE", "5error." + error);
                        }
                    });

                } else {
                }

                // Синхронизация стандартов
                StandartExchange standartExchange = new StandartExchange();
                standartExchange.downloadStandartTable(new ExchangeInterface.ExchangeResponseInterface() {
                    @Override
                    public <T> void onSuccess(List<T> data) {
                        Log.e("MerchikTest", "data: " + data);
//                        List<StandartSDB> save = (List<StandartSDB>) data;
                        SQL_DB.standartDao().insertData((List<StandartSDB>) data)
                                .subscribeOn(Schedulers.io())
                                .subscribe(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                        Globals.writeToMLOG("INFO", "Exchange.downloadStandartTable.onComplete", "Успешно сохранило Стандарты (" + data.size() + ")шт в БД");
                                    }

                                    @Override
                                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                        Globals.writeToMLOG("INFO", "Exchange.downloadStandartTable.onError", "Ошибка при сохранении в БД: " + e);
                                    }
                                });
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("MerchikTest", "error: " + error);
                    }
                });
                standartExchange.downloadContentTable(new ExchangeInterface.ExchangeResponseInterface() {
                    @Override
                    public <T> void onSuccess(List<T> data) {
                        Log.e("MerchikTest", "data: " + data);
//                        List<ContentSDB> save = (List<ContentSDB>) data;
                        SQL_DB.contentDao().insertData((List<ContentSDB>) data)
                                .subscribeOn(Schedulers.io())
                                .subscribe(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                        Globals.writeToMLOG("INFO", "Exchange.downloadContentTable.onComplete", "Успешно сохранило Контенты (" + data.size() + ")шт в БД");
                                    }

                                    @Override
                                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                        Globals.writeToMLOG("INFO", "Exchange.downloadContentTable.onError", "Ошибка при сохранении в БД: " + e);
                                    }
                                });
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("MerchikTest", "error: " + error);
                    }
                });


                // Чудо, что ты тут делаешь?
                new DialogEKL(context, null).responseCheckEKLList();


                try {
                    getPhotoFromSite(); // Получаем ВСЕ фотки за сегодняшний день с сайта. Какие именно и сколько фото - зависит от пользователя.
                    sendPhotoInformation(getPhotoInfoToUpload(UploadPhotoInfo.DVI), new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {
                            try {
                                List<PhotoInfoResponseList> photo = (List<PhotoInfoResponseList>) data;
                                if (photo.size() > 0) {
                                    Integer[] ids = new Integer[photo.size()];
                                    int count = 0;
                                    for (PhotoInfoResponseList item : photo) {
                                        ids[count++] = item.elementId;
                                    }

                                    List<StackPhotoDB> stackPhoto = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getByIds(ids));

                                    for (StackPhotoDB item : stackPhoto) {
                                        for (PhotoInfoResponseList listItem : photo) {
                                            if (listItem.elementId.equals(item.getId())) {
                                                if (listItem.state) {
                                                    item.setDviUpload(false);
                                                } else {
                                                    item.setDviUpload(false);
                                                    item.setComment(listItem.error);
                                                }
                                                StackPhotoRealm.setAll(Collections.singletonList(item));
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "sendPhotoInformation(getPhotoInfoToUpload(UploadPhotoInfo.DVI)", "Exception e: " + e);
                            }
                        }

                        @Override
                        public void onFailure(String error) {

                        }
                    });    // Выгрузка изменённых ДВИ
                    sendPhotoInformation(getPhotoInfoToUpload(UploadPhotoInfo.COMMENT), new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {
                            try {
                                List<PhotoInfoResponseList> photo = (List<PhotoInfoResponseList>) data;
                                if (photo.size() > 0) {
                                    Integer[] ids = new Integer[photo.size()];
                                    int count = 0;
                                    for (PhotoInfoResponseList item : photo) {
                                        ids[count++] = item.elementId;
                                    }

                                    List<StackPhotoDB> stackPhoto = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getByIds(ids));

                                    for (StackPhotoDB item : stackPhoto) {
                                        for (PhotoInfoResponseList listItem : photo) {
                                            if (listItem.elementId.equals(item.getId())) {
                                                if (listItem.state) {
                                                    item.setCommentUpload(false);
                                                } else {
                                                    item.setCommentUpload(false);
                                                    item.setComment(listItem.error);
                                                }
                                                StackPhotoRealm.setAll(Collections.singletonList(item));
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "sendPhotoInformation(getPhotoInfoToUpload(UploadPhotoInfo.COMMENT)", "Exception e: " + e);
                            }
                        }

                        @Override
                        public void onFailure(String error) {

                        }
                    });    // Выгрузка изменённых комментариев
                    sendPhotoInformation(getPhotoInfoToUpload(UploadPhotoInfo.RATING), new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {
                            try {
                                List<PhotoInfoResponseList> photo = (List<PhotoInfoResponseList>) data;
                                if (photo.size() > 0) {
                                    Integer[] ids = new Integer[photo.size()];
                                    int count = 0;
                                    for (PhotoInfoResponseList item : photo) {
                                        ids[count++] = item.elementId;
                                    }

                                    List<StackPhotoDB> stackPhoto = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getByIds(ids));

                                    for (StackPhotoDB item : stackPhoto) {
                                        for (PhotoInfoResponseList listItem : photo) {
                                            if (listItem.elementId.equals(item.getId())) {
                                                if (listItem.state) {
                                                    item.setMarkUpload(false);
                                                } else {
                                                    item.setMarkUpload(false);
                                                    item.setComment(listItem.error);
                                                }
                                                StackPhotoRealm.setAll(Collections.singletonList(item));
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "sendPhotoInformation(getPhotoInfoToUpload(UploadPhotoInfo.RATING)", "Exception e: " + e);
                            }
                        }

                        @Override
                        public void onFailure(String error) {

                        }
                    });    // Выгрузка Рейтингов фоток
                    globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.startExchange.Успех.1." + "\n");
                } catch (Exception e) {
                    globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.startExchange.Ошибка.1." + e + "\n");
                }


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
                    });     // Загрузка Задач и Рекламаций

                    sendTAR();              // Выгрузка на сервер ЗИР-а
                    uploadTARComments();    // Выгрузка ЗИР переписки(коммнетариев)
                    globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.startExchange.Успех.2." + "\n");
                } catch (Exception e) {
                    globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.startExchange.Ошибка.2." + e + "\n");
                }


                try {
                    //sendingChangedWPData(); // Выгрузка обновленных полей в плане работ
                    sendWpData2();
                    globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.startExchange.Успех.3." + "\n");
                } catch (Exception e) {
                    globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.startExchange.Ошибка.3." + e + "\n");
                }

                sendARMark();   // ОТПРАВКА ТЕСТ ОЦЕНОК

//                updateLanguages();  // Обновление языков
//                updateSiteObj();    // Обновление Обьектов Сайта
//                updateTranslates();  // Обновление Переводов
            } else {
                long time = (System.currentTimeMillis() - exchange) / 1000;
                Log.e("startExchange", "start/Время обновлять НЕ наступило. После обновления прошло: " + time + "секунд.");
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "startExchange", "Exception e: " + e);
        }

    }

    // ====================================^=^=^=^=^================================================

    private void sendTAR() {
        TARUpload data = new TARUpload();
        data.mod = "reclamation";
        data.act = "create";

        // Получаю на выгрузку ЗИР (таблицу)
        List<TasksAndReclamationsDB> list = TasksAndReclamationsRealm.getToUnload();

        // Создаю данные на выгрузку (запрос)
        List<TARUploadData> dataList = new ArrayList<>();

        for (TasksAndReclamationsDB item : list) {

            TARUploadData el = new TARUploadData();
            el.tp = item.getTp();
            el.element_id = item.getID();
            el.addr_id = item.getAddr();
            el.date = Clock.getDateString(Integer.parseInt(item.getDt()));
            el.photo_id = item.getPhoto();
            el.theme_id = item.getThemeId();
            el.comment = item.getComment();

            dataList.add(el);
        }

        data.data = dataList;


        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Log.e("sendTAR", "convertedObject: " + convertedObject);
        Log.e("sendTAR", "list.size(): " + list.size());


        if (list.size() > 0) {
            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<JsonObject>() {
                @Override
                public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    try {
                        Gson gson = new Gson();
                        String json = gson.toJson(response.body());
                        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                        Log.e("sendTAR", "convertedObjectResponse: " + convertedObject);
                        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.sendTAR.onResponse.response: " + convertedObject + "\n");


                    } catch (Exception e) {
                        Log.e("sendTAR", "e: " + e);
                        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.sendTAR.onResponse.ERROR_1: " + e + "\n");
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                    Log.e("sendTAR", "t: " + t);
                    globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.sendTAR.onFailure.ERR: " + t + "\n");
                }
            });
        } else {

        }


    }


    /**
     * 10.05.2021
     * Обновление таблички Языки. Запись идёт в SQL
     */
    private void updateLanguages() {
        try {
            Log.e("updateLanguages", "OK");
            // Отображение прогресса обена таблиц.
            ProgressDialog progressDialog = ProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: " + "Языки", true, true);
            new LanguagesExchange().downloadLanguages(new ExchangeInterface.Languages() {
                @Override
                public void onSuccess(List<LanguagesSDB> data) {

                    // Сохранение данных в БД
                    SQL_DB.langListDao().insertAll(data);

                    // Скрытие текущего прогресса
                    if (progressDialog != null) {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }


                    // ОПЦИОНАЛЬНО! Отображение сообщения пользователю
//                    DialogData dialog = new DialogData(context);
//                    dialog.setTitle("Таблица Языков");
//                    dialog.setText("Синхронизовало " + data.size() + " Языков");
//                    dialog.setClose(dialog::dismiss);
//                    dialog.show();
                }

                @Override
                public void onFailure(String error) {

                    // Скрытие текущего прогресса
                    if (progressDialog != null) {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }

                    // ОПЦИОНАЛЬНО! Отображение сообщения пользователю
                    DialogData dialog = new DialogData(context);
                    dialog.setTitle("Таблица Языков");
                    dialog.setText(error);
                    dialog.setClose(dialog::dismiss);
                    dialog.show();
                }
            });

        } catch (Exception e) {
            // todo ADD M_LOG
            Log.e("updateLanguages", "ERR: " + e);
        }
    }


    /**
     * 10.05.2021
     * Обновление таблички Обьекты сайта. Запись идёт в SQL
     */
    private void updateSiteObj() {
        List<SiteObjectsSDB> data = SQL_DB.siteObjectsDao().getAll();

        // Отобрадение прогресса
        ProgressDialog progressDialog = ProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: " + "ОбьектыСайта", true, true);

        new SiteObjectsExchange().downloadSiteObjects(new ExchangeInt() {
            @Override
            public void onSuccess(String msg) {

                Log.e("SiteObjectsExchange", "S");


                if (progressDialog != null) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }

//                DialogData dialog = new DialogData(context);
//                dialog.setTitle("ОбьектыСайта");
//                dialog.setText(msg);
//                dialog.setClose(dialog::dismiss);
//                dialog.show();
            }

            @Override
            public void onFailure(String error) {

                Log.e("SiteObjectsExchange", "F");


                if (progressDialog != null) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }

                DialogData dialog = new DialogData(context);
                dialog.setTitle("ОбьектыСайта");
                dialog.setText(error);
                dialog.setClose(dialog::dismiss);
                dialog.show();
            }
        });
//        }

    }

    /**
     * 10.05.2021
     * Обновление таблички Переводов. Запись идёт в SQL
     */
    private void updateTranslates() {
        try {
            Log.e("updateTranslates", "OK");

            // Отображение прогресса обена таблиц.
            ProgressDialog progressDialog = ProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: " + "Переводы", true, true);

            new TranslationsExchange().downloadTranslations(new ExchangeInterface.Translates() {
                @Override
                public void onSuccess(List<TranslatesSDB> data) {

                    // Сохранение данных в БД
                    SQL_DB.translatesDao().insertAll(data);

                    // Скрытие текущего прогресса
                    if (progressDialog != null) {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }


                    // ОПЦИОНАЛЬНО! Отображение сообщения пользователю
//                    DialogData dialog = new DialogData(context);
//                    dialog.setTitle("Таблица Переводов");
//                    dialog.setText("Синхронизовало " + data.size() + " Переводов");
//                    dialog.setClose(dialog::dismiss);
//                    dialog.show();
                }

                @Override
                public void onFailure(String error) {

                    // Скрытие текущего прогресса
                    if (progressDialog != null) {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }

                    // ОПЦИОНАЛЬНО! Отображение сообщения пользователю
                    DialogData dialog = new DialogData(context);
                    dialog.setTitle("Таблица Переводов");
                    dialog.setText(error);
                    dialog.setClose(dialog::dismiss);
                    dialog.show();
                }
            });

        } catch (Exception e) {
            // todo ADD M_LOG
            Log.e("updateTranslates", "ERR: " + e);
        }
    }


    /**/
    public void sendPhotoInformation(PhotoInformation data, ExchangeInterface.ExchangeResponseInterface exchange) {
        if (data != null) {
            if (data.data != null && data.data.size() > 0) {
                String json = new Gson().toJson(data); // TODO: УЗНАТЬ У МЕНТОРА: Можно ли эти 2 строчки выделить в отдельный метод как-то и где это сделать? в утильном Globals?
                JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                Log.e("sendPhotoInformation", "json: " + json);

                Globals.writeToMLOG("INFO", "sendPhotoInformation", "json: " + json);

                retrofit2.Call<PhotoInfoResponse> call = RetrofitBuilder.getRetrofitInterface().SEND_PHOTO_INFO(RetrofitBuilder.contentType, convertedObject);
                call.enqueue(new retrofit2.Callback<PhotoInfoResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<PhotoInfoResponse> call, retrofit2.Response<PhotoInfoResponse> response) {
                        if (response.isSuccessful()) {
                            Log.e("sendPhotoInformation", "response: " + response);
                            Log.e("sendPhotoInformation", "response.body(): " + response.body());

                            Globals.writeToMLOG("INFO", "sendPhotoInformation.onResponse", "response: " + response);

                            if (response.body() != null) {
                                Globals.writeToMLOG("INFO", "sendPhotoInformation.onResponse", "response.body(): " + response.body());
                                if (response.body().list != null && response.body().list.size() > 0) {
                                    Globals.writeToMLOG("INFO", "sendPhotoInformation.onResponse", "response.body().list.size(): " + response.body().list.size());
                                    exchange.onSuccess(response.body().list);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<PhotoInfoResponse> call, Throwable t) {
                        Log.e("sendPhotoInformation", "t:" + t);
                    }
                });
            }
        } else {
            // Данных на выгрузку ДВИ/Комментариев ... НЕТ
            Log.e("sendPhotoInformation", "data: " + "Данных нет");
        }
    }

    /**/
    public PhotoInformation getPhotoInfoToUpload(UploadPhotoInfo enumData) {
        PhotoInformation res = new PhotoInformation();

        // Создаю обьект с ДАТОЙ для отправки на сервер
        List<PhotoInformationData> data = new ArrayList<>();

        Log.e("getPhotoInfoToUpload", "enumData: " + enumData);

        List<StackPhotoDB> stackPhotoDB = new ArrayList<>();

        switch (enumData) {
            case DVI:
                stackPhotoDB = RealmManager.stackPhotoDBListGetDVIToUpload();
                Log.e("getPhotoInfoToUpload", "DVI DB size: " + stackPhotoDB.size());
                break;

            case COMMENT:
                stackPhotoDB = RealmManager.stackPhotoDBListGetCommentToUpload();
                Log.e("getPhotoInfoToUpload", "COMMENT DB size: " + stackPhotoDB.size());
                break;

            case RATING:
                stackPhotoDB = RealmManager.stackPhotoDBListGetRatingToUpload();
                Log.e("getPhotoInfoToUpload", "RATING DB size: " + stackPhotoDB.size());
                break;
        }

//        stackPhotoDB = RealmManager.stackPhotoDBListGetDVIToUpload();
//        Log.e("getPhotoInfoToUpload", "size: " + stackPhotoDB.size());


        // 01.03.2021 Костыль. Если данных нет - сразу возвращаем пустоту.
        if (stackPhotoDB.size() == 0) {
            return null;
        }


        res.mod = "images_view";

        // Наверно стоит вынести в отдельную функцию
        switch (enumData) {
            case DVI:
                res.act = "set_dvi";
                for (StackPhotoDB item : stackPhotoDB) {
                    PhotoInformationData info = new PhotoInformationData();
                    info.element_id = String.valueOf(item.getId());
                    info.id = item.getPhotoServerId();
                    info.state = String.valueOf(item.isDvi());

                    data.add(info);
                }
                res.data = data;
                break;

            case COMMENT:
                res.act = "set_comment";
                for (StackPhotoDB item : stackPhotoDB) {
                    PhotoInformationData info = new PhotoInformationData();
                    info.element_id = String.valueOf(item.getId());
                    info.id = item.getPhotoServerId();
                    info.text = item.getComment();

                    data.add(info);
                }
                res.data = data;
                break;

            case RATING:
                res.act = "set_score";
                for (StackPhotoDB item : stackPhotoDB) {
                    PhotoInformationData info = new PhotoInformationData();
                    info.element_id = String.valueOf(item.getId());
                    info.id = item.getPhotoServerId();
                    info.score = item.getMark();

                    data.add(info);
                }
                res.data = data;
        }


        return res;
    }


    /**
     * 03.03.2021 (ДОПОЛНИТЬ)
     * Получение со стороны сервера фотографий.
     * <p>
     * Задумка функции в том что Руководители смогут по полученным фоткам подчинённых ставить
     * оценки, комментарии, дви. Получаем ВСЕ фотки за сегодняшний день с сайта.
     * Какие именно и сколько фото - зависит от пользователя. Подчинённый должен получить только
     * свои фото, а руководитель - Все
     */
    private void getPhotoFromSite() {
        PhotoDownload server = new PhotoDownload();
        PhotoTableRequest data = new PhotoTableRequest();
        data.mod = "images_view";
        data.act = "list_image";
        data.nolimit = "1";
        data.dt_upload = String.valueOf(RealmManager.getSynchronizationTimetableRowByTable("stack_photo").getVpi_app());
        data.date_from = Clock.yesterday;
        data.date_to = Clock.today;


        WpDataRealm.UserPostRes info = WpDataRealm.userPost(Globals.userId);
        switch (info) {
            case EMPTY:
                Log.e("getPhotoFromSite", "EMPTY");
                globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.getPhotoFromSite: " + "EMPTY" + "\n");
                break;

            case SUBORDINATE:
                Log.e("getPhotoFromSite", "SUBORDINATE");
//                data.sotr_id = String.valueOf(Globals.userId);
                globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.getPhotoFromSite: " + "SUBORDINATE" + "\n");
//                server.getPhotoFromServer(data);
                break;

            case MANAGER:
                Log.e("getPhotoFromSite", "MANAGER");
                globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.getPhotoFromSite: " + "MANAGER" + "\n");
                server.getPhotoFromServer(data);
                break;
        }
    }


    /**
     * 09.03.2021
     * Получение с Сайта данных(ссылок) для загрузки фото товаров в приложение.
     */
    public void getTovarImg(List<TovarDB> list, String imageType, Globals.OperationResult operationResult) {

        Log.e("getTovarImg", "list: " + list.get(0).getiD());
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
//                Log.e("TAG_TABLE", "PHOTO_TOVAR_ID_TO_SEND: " + tov.getiD());
                if (!RealmManager.stackPhotoExistByObjectId(Integer.parseInt(tov.getiD()), imageType)) {
//                if (!RealmManager.stackPhotoExistByObjectId(Integer.parseInt(tov.getiD()))) {
//                    Log.e("TAG_TABLE", "PHOTO_TOVAR_ID_TO_SEND_NOTEXIST: " + tov.getiD());
                    listId.add(tov.getiD());
                } else {
//                    Log.e("TAG_TABLE", "PHOTO_TOVAR_ID_TO_SEND_EXIST: " + tov.getiD());
                }

            } catch (Exception e) {
                // ЛОГ ошибки
            }
        }

//        Log.e("TAG_TABLE", "PHOTO_TOVAR_ID_TO_SEND: " + listId);
//        Log.e("TAG_TABLE", "PHOTO_TOVAR_ID_TO_SEND: " + listId.size());

        if (listId.size() == 0) {
            operationResult.onFailure("Пусто?");
            return;
        }

        retrofit2.Call<TovarImgResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_PHOTO_INFO(mod, act, tovarOnly, nolimit, imageType, listId);
        String finalImageType = imageType;
        call.enqueue(new retrofit2.Callback<TovarImgResponse>() {
            @Override
            public void onResponse(retrofit2.Call<TovarImgResponse> call, retrofit2.Response<TovarImgResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
//                        Log.e("TAG_TABLE", "PHOTO_TOVAR_RESPONSE: " + response);
//                        Log.e("TAG_TABLE", "PHOTO_TOVAR_RESPONSE_BODY: " + response.body().getState());
                        List<TovarImgList> list = response.body().getList();

//                        for (TovarImgList item : list) {
//                            Log.e("PHOTO_TOV_data", "-------------------------------");
//                            Log.e("PHOTO_TOV_data", "item.getID(): " + item.getID());
//                            Log.e("PHOTO_TOV_data", "item.getPhotoTp(): " + item.getPhotoTp());
//                            Log.e("PHOTO_TOV_data", "item.getPhotoTpTxt(): " + item.getPhotoTpTxt());
//                        }


                        if (list != null) {
//                            Log.e("TAG_TABLE", "PHOTO_TOVAR_LIST_SIZE: " + list.size());
                            downloadTovarImg(list, finalImageType, operationResult);
                        }
                    } catch (Exception e) {
//                        Log.e("LOG", "SAVE_TO_LOG");
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<TovarImgResponse> call, Throwable t) {
//                Log.e("TAG_TABLE", "PHOTO_TOVAR_ERROR: " + t);
                operationResult.onFailure("" + t);
            }
        });
    }


    /**
     * 09.03.2021
     * Загрузка фотографий
     */
    public void downloadTovarImg(List<TovarImgList> list, String imageType, Globals.OperationResult operationResult) {
        Globals globals = new Globals();

//        Log.e("TAG_TABLE", "PHOTO_TOVAR_DOWNLOAD_LIST_SIZE: " + list.size());

        for (int i = 0; i < list.size(); i++) {
//            Log.e("TAG_TABLE", "PHOTO_TOVAR_URL: " + list.get(i).getPhotoUrl());

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

                            Log.e("TESTING", "1_SAVE PHOTO");
                            Log.e("TESTING", "1_SAVE PHOTO/path: " + path);

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

                            RealmManager.stackPhotoSavePhoto(stackPhotoDB);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                        Log.e("TAG_TABLE", "PHOTO_TOVAR_URL: " + t);

                        operationResult.onFailure(t.toString());
                    }
                });
            } else {
                Log.e("downloadTovarImg", "--------------ЭТО ФОТО НЕ СОХРАНЕНО-----------------");
            }
        }

        operationResult.onSuccess();
    }


    private void uploadTARComments() {

        Log.e("uploadTARComments", "enter here");

        List<TARCommentsDB> list = TARCommentsRealm.getTARCommentToUpload();

        if (list != null && list.size() > 0) {

            Log.e("uploadTARComments", "List not null: " + list.size());

            TARCommentDataUpload data = new TARCommentDataUpload();
            data.mod = "reclamation";
            data.act = "set_comment";

            List<TARCommentDataListUpload> dataList = new ArrayList<>();
            for (TARCommentsDB item : list) {
                TARCommentDataListUpload dataItem = new TARCommentDataListUpload();
                dataItem.id = item.getRId();
                dataItem.comment = item.getComment();
                dataItem.photo_id = item.getPhoto();

                dataList.add(dataItem);
            }

            data.data = dataList;


            JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(data), JsonObject.class);
            Log.e("uploadTARComments", "convertedObject: " + convertedObject);


            retrofit2.Call<TARCommentsServerData> call = RetrofitBuilder.getRetrofitInterface().UPLOAD_TAR_COMMENT(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<TARCommentsServerData>() {
                @Override
                public void onResponse(retrofit2.Call<TARCommentsServerData> call, retrofit2.Response<TARCommentsServerData> response) {
                    Log.e("uploadTARComments", "response: " + response);
                    Log.e("uploadTARComments", "response.body(): " + response.body());

                    JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(response.body()), JsonObject.class);
                    Log.e("uploadTARComments", "response.body().json: " + convertedObject);

                    try {
                        TARCommentsServerData res = response.body();
                        if (res != null) {
                            if (res.getState() != null && res.getState()) {
                                if (res.getList() != null && res.getList().size() > 0) {
                                    for (int i = 0; i == res.getList().size(); i++) {
                                        if (res.getList().get(i).getState() != null && res.getList().get(i).getState()) {
                                            list.get(i).setID(res.getList().get(i).getInfo().getCommentId());
                                        }
                                    }
                                    RealmManager.INSTANCE.executeTransaction((realm) -> {
                                        INSTANCE.copyToRealmOrUpdate(list);
                                    });
                                }
                            }
                        }
                    } catch (Exception e) {

                    }


                }

                @Override
                public void onFailure(retrofit2.Call<TARCommentsServerData> call, Throwable t) {
                    Log.e("uploadTARComments", "t:" + t);
                }
            });


        }
    }


    /**
     * 29.10.21
     * Выгрузка на сервер обновлённых данных о ЗиРах
     * (время, мнение, комменты)
     */
    public static void updateTAR(TasksAndReclamationsSDB uploadData) {
        // Подготовка данных на выгрузку
//        List<TasksAndReclamationsSDB> uploadList = new ArrayList<TasksAndReclamationsSDB>();

//        if (uploadList != null && uploadList.size() > 0){

        StandartData standartData = new StandartData();
        standartData.mod = "reclamation";
        standartData.act = "update_data";

        StandartData.StandartDataTARUpload data = new StandartData.StandartDataTARUpload();

        data.element_id = 1;
        data.code_dad2 = uploadData.codeDad2;
        data.vote_score = uploadData.voteScore;
        data.vinovnik_score = uploadData.vinovnikScore;

        standartData.data = Collections.singletonList(data);


        JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(standartData), JsonObject.class);
        Log.e("updateTAR", "convertedObject:" + convertedObject);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("updateTAR", "response:" + response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("updateTAR", "t:" + t);
            }
        });

    }


    /**
     * 31.03.2021
     * Новая отправка на сервер данных о Начале/Конце работы
     */
    public static void sendWpData2() {
        UploadDataSEWork data = new UploadDataSEWork();
        data.mod = "plan";
        data.act = "update_data";
        data.data = RealmManager.getWpDataStartEndWork();

        JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(data), JsonObject.class);
        Log.e("sendWpData2", "convertedObject.json: " + convertedObject);

        if (data != null && data.data.size() > 0) {
            retrofit2.Call<WpDataUpdateResponse> call = RetrofitBuilder.getRetrofitInterface().SEND_WP_DATA(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<WpDataUpdateResponse>() {
                @Override
                public void onResponse(retrofit2.Call<WpDataUpdateResponse> call, retrofit2.Response<WpDataUpdateResponse> response) {
                    try {
//                        Log.e("sendWpData2", "RESPONSE: " + response);
//                        Log.e("sendWpData2", "RESPONSE.body: " + response.body());
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().state) {
                                if (response.body().data != null && response.body().data.size() > 0) {
                                    // TODO Вынести это в нормальную функцию и отдельный вызов.
                                    Integer[] ids = new Integer[response.body().data.size()];
                                    int count = 0;
                                    for (WpDataUpdateResponseList item : response.body().data) {
                                        ids[count++] = item.elementId;
                                    }

                                    List<WpDataDB> wp = RealmManager.INSTANCE.copyFromRealm(WpDataRealm.getWpDataRowByIds(ids));
                                    List<WpDataDB> saveWp = new ArrayList<>();

                                    for (WpDataDB item : wp) {
                                        for (WpDataUpdateResponseList data : response.body().data) {
                                            if (data.elementId.equals(item.getId())) {
                                                item.startUpdate = false;
                                                saveWp.add(item);
                                            }
                                        }
                                    }
                                    WpDataRealm.setWpData(saveWp);

                                }
                                if (response.body().error != null && !response.body().error.equals("")) {
                                    Globals.writeToMLOG("ERROR", "Exchange.sendWpData2.onResponse.response.body().error", "Error: " + response.body().error);
                                }
                            }
                        }

                        Globals.writeToMLOG("INFO", "Exchange.sendWpData2.onResponse", "response" + response);

                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "Exchange.sendWpData2.onResponse", "Exception e: " + e);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<WpDataUpdateResponse> call, Throwable t) {
//                    Log.e("sendWpData2", "FAILURE_E: " + t.getMessage());
//                    Log.e("sendWpData2", "FAILURE_E2: " + t);
                    Globals.writeToMLOG("ERROR", "Exchange.sendWpData2.onFailure", "Throwable t: " + t);
                }
            });
        }
    }


    /**
     * 20.04.2021
     * Отправка Оценок Доп. Требований
     */
    public void sendARMark() {

        Log.e("sendARMark", "sendARMark: START");

        StandartData data = new StandartData();
        data.mod = "additional_requirements";
        data.act = "set_score";

        List<AdditionalRequirementsMarkDB> list = AdditionalRequirementsMarkRealm.getToUpload();

        List<MarkData> markLIST = new ArrayList<>();

        Log.e("sendARMark", "list.size(): " + list.size());
        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.sendARMark.list: " + list.size() + "\n");

        for (AdditionalRequirementsMarkDB item : list) {
            MarkData mark = new MarkData();
            Log.e("sendARMark", "?");

            mark.id = String.valueOf(item.getItemId());
            mark.score = item.getScore();
            mark.tp_id = item.getTp();

            markLIST.add(mark);

            Log.e("sendARMark", "??");
            Log.e("sendARMark", "???");
        }

        data.data = markLIST;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Log.e("sendARMark", "convertedObject: " + convertedObject);


        if (list.size() > 0) {
            retrofit2.Call<AdditionalRequirementsSendMarksServerData> call = RetrofitBuilder.getRetrofitInterface().SEND_ADDREP_MARKS(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<AdditionalRequirementsSendMarksServerData>() {
                @Override
                public void onResponse(retrofit2.Call<AdditionalRequirementsSendMarksServerData> call, retrofit2.Response<AdditionalRequirementsSendMarksServerData> response) {
                    try {
                        Gson gson = new Gson();
                        String json = gson.toJson(response.body());
                        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.sendARMark.onResponse.response: " + convertedObject + "\n");

                        Log.e("sendARMark", "convertedObject: " + convertedObject);

                        List<AdditionalRequirementsMarksListServerData> info = response.body().getList();

                        try {
                            if (info != null && info.size() > 0) {
                                for (int i = 0; i < info.size() - 1; i++) {
                                    if (info.get(i).getState()) {
                                        Log.e("sendARMark", "HERE_1");
                                        int finalI = i;
                                        INSTANCE.executeTransaction(realm -> {
                                            list.get(finalI).setUploadStatus(String.valueOf(System.currentTimeMillis()));
                                        });
                                        AdditionalRequirementsMarkRealm.setDataToDB(Collections.singletonList(list.get(finalI)));
                                    } else {
                                        Log.e("sendARMark", "HERE_2");
                                        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.sendARMark.onResponse.response.body().getError(): " + response.body().getError() + "\n");
                                        // Нужно логировать ошибки
                                    }
                                }
                            }

                        } catch (Exception e) {
                            Log.e("sendARMark", "HERE_3");
                            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.sendARMark.onResponse.ERROR.HERE_3: " + e + "\n");
                        }

                    } catch (Exception e) {
                        Log.e("sendARMark", "e: " + e);
                        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.sendARMark.onResponse.ERROR.HERE_4: " + e + "\n");
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<AdditionalRequirementsSendMarksServerData> call, Throwable t) {
                    Log.e("sendARMark", "t: " + t);
                    globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.sendARMark.onFailure.ERR: " + t + "\n");
                }
            });
        } else {

        }


    }


    /**
     * 20.05.2021
     * Отправка ЛОГА местоположения
     */
    public void sendLogMp() {
        String mod = "location";
        String act = "track";

        List<LogMPDB> logMp = RealmManager.getAllLogMPDB();
        if (logMp != null && logMp.size() > 0) {
            Log.e("LogMp", "LogMpUploadText. LogSize: " + logMp.size());

            HashMap<String, String> map = new HashMap<>();
            for (LogMPDB list : logMp) {
                map.put("gp[" + list.getId() + "]", list.getGp());
            }


            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().UPLOAD_LOG_MP(mod, act, map);
            call.enqueue(new retrofit2.Callback<JsonObject>() {
                @Override
                public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    Log.e("LogMp", "RESPONSE: " + response.body());

                    try {
                        JsonObject resp = response.body();
                        if (resp != null) {
                            if (!resp.get("state").isJsonNull() && resp.get("state").getAsBoolean()) {
                                JsonObject arr = resp.get("geo_result").getAsJsonObject();
                                if (arr != null) {
                                    for (LogMPDB list : logMp) {
                                        JsonObject geoInfo = arr.getAsJsonObject(String.valueOf(list.getId()));
                                        if (!geoInfo.isJsonNull() && geoInfo.get("state").getAsBoolean()) {
                                            try {
                                                RealmManager.INSTANCE.executeTransaction(realm -> {
                                                    list.deleteFromRealm();
                                                });
                                            } catch (Exception e) {
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                    }

                }

                @Override
                public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                    Log.e("LogMp", "FAILURE_E: " + t.getMessage());
                    Log.e("LogMp", "FAILURE_E2: " + t);
                }
            });
        } else {
            Log.e("LogMp", "LogMpUploadText. LogSize: " + null);
        }

    }


    /**
     * 05.08.2021
     * Получение чатов и сообщений
     */
    public static void chatExchange() {
        StandartData data = new StandartData();
        data.mod = "chat";
        data.act = "list_message";
        data.dt_change_from = Clock.today;
        data.dt_change_to = Clock.tomorrow;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<ChatResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TABLE_CHAT(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                Log.e("chatExchange", "response: " + response);
                SQL_DB.chatDao().insertData(response.body().list)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Log.e("chatExchange", "onComplete()");
                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                Log.e("chatExchange", "Throwable e: " + e);
                            }
                        });
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Log.e("chatExchange", "Throwable t: " + t);
            }
        });
    }

    /**
     * 05.08.2021
     * Передача на сторону сервера Чатов (прочитанности)
     */
    public static void chatMarkRead(StandartData.StandartDataChat dataChat, ExchangeInterface.ExchangeResponseInterfaceSingle exchange) {
        StandartData data = new StandartData();
        data.mod = "chat";
        data.act = "mark_read";
        data.dt_change_from = Clock.today;
        data.dt_change_to = Clock.tomorrow;

        // Должен быть DATA с инфой на передачу
        data.data = Collections.singletonList(dataChat);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("chatMarkRead", "response: " + response);
                exchange.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("chatMarkRead", "Throwable t: " + t);
                exchange.onFailure(t.toString());
            }
        });
    }


//    private void

    private void planogram(ExchangeInterface.ExchangeResponseInterface exchange) {
        if (StackPhotoRealm.checkByType5()) {
            exchange.onFailure("Все данные уже загружены");
            return;
        }

        StandartData data = new StandartData();
        data.mod = "images_view";
        data.act = "list_image";
        data.date_from = Clock.getDatePeriod(-180);
        data.date_to = Clock.today;
        data.photo_type = "5";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<ImagesViewListImageResponse> call = RetrofitBuilder.getRetrofitInterface().GET_PHOTOS(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<ImagesViewListImageResponse>() {
            @Override
            public void onResponse(Call<ImagesViewListImageResponse> call, Response<ImagesViewListImageResponse> response) {
                try {
//                    Log.e("test", "response: " + response);
                    if (response.isSuccessful() && response.code() == 200) {
                        if (response.body() != null && response.body().state) {
                            if (response.body().list != null && response.body().list.size() > 0) {
                                exchange.onSuccess(response.body().list);
                            }
                        }
                    } else {
                        exchange.onFailure("Данных нет");
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "Exchange.planogram", "Exception e: " + e);
                }

            }

            @Override
            public void onFailure(Call<ImagesViewListImageResponse> call, Throwable t) {
                Log.e("test", "Throwable t: " + t);
            }
        });
    }


    /**
     * 09.11.2021
     * Обновление Плана работ для Exchange
     *
     * Сюда нужно воткнуть обработку (прогресс).
     * Добавить профайлер
     * Обработка результата
     * Обновление Пинга
     */
    public List<String> exchangeListInfo;
    public void exchangeTableWpData(){
        Globals.setProfiler(); // Установка профайлера



        // Должна быть сама Синхронизация Плана работ.

    }


}
