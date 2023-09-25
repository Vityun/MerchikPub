package ua.com.merchik.merchik.ServerExchange;


import static ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading.downloadSiteHints;
import static ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading.downloadVideoLessons;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.realm.RealmResults;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.Constants.ReclamationPercentageExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.AddressExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.CityExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.CustomerExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.EKLExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.FragmentsExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.LanguagesExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.OblastExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.PotentialClientTableExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.SamplePhotoExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.ShelfSizeExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.ShowcaseExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.SiteObjectsExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.StandartExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.TranslationsExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.UsersExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.VideoViewExchange;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CitySDB;
import ua.com.merchik.merchik.data.Database.Room.ContentSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;
import ua.com.merchik.merchik.data.Database.Room.LanguagesSDB;
import ua.com.merchik.merchik.data.Database.Room.OblastSDB;
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.Database.Room.StandartSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.Database.Room.ViewListSDB;
import ua.com.merchik.merchik.data.Database.Room.VoteSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.AdditionalMaterialsAddressResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.AdditionalMaterialsGroupsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.AdditionalMaterialsLinksResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.AdditionalMaterialsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.ConductWpDataResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgList;
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.ImagesViewListImageList;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.ImagesViewListImageResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.PhotoInfoResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.PhotoInfoResponseList;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.AchievementsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ArticleResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ChatGrp.ChatGrpResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ChatResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.VoteResponse;
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
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.BlockingProgressDialog;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogEKL;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/**
 * 26.02.2021
 * Этот класс создан на замену текущего TablesLoadingUnloading потому что в последнем скопилось
 * много нехорошего кода и написан не совсем адекватно в принципе.
 * <p>
 * Класс Обмена. Буду по возможности сюда перетаскивать адевкатный функционал по обмену данными с
 * сервером.
 */
public class Exchange {

    private final Globals globals = new Globals();
    public Context context;
    private static long exchange = 0;
    //    private int retryTime = 120000;   // 2
    private final int retryTime = 600000;     // 10
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

                try {
                    globals.fixMP(null);    //
                    Globals.writeToMLOG("ERROR", "startExchange/globals.fixMP();", "locationGPS: " + Globals.locationGPS);
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/globals.fixMP();", "Exception e: " + e);
                }

                try {
                    TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading();
                    tablesLoadingUnloading.uploadLodMp(new ExchangeInterface.ExchangeRes() {
                        @Override
                        public void onSuccess(String ok) {
                            Log.e("uploadLodMp", "uploadLodMp: " + ok);
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("uploadLodMp", "uploadLodMp error: " + error);
                        }
                    });
                } catch (Exception e) {
                    Log.e("uploadLodMp", "uploadLodMp Exception e: " + e);
                }


                try {
                    downloadAdditionalMaterials();
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/downloadAdditionalMaterials", "Exception e: " + e);
                }

                try {
                    planogram(new ExchangeInterface.ExchangeResponseInterface() {
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
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/planogram", "Exception e: " + e);
                }

                try {
                    chatExchange();
                    chatGroupExchange();
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/chatExchange/chatGroupExchange", "Exception e: " + e);
                }


                try {
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
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/AddressExchange,CustomerExchange,UsersExchange,CityExchange,OblastExchange", "Exception e: " + e);
                }

                try {
                    new EKLExchange().downloadEKLTable(new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {
                            try {
                                Log.e("AddressExchange", "START");
                                SQL_DB.eklDao().insertData((List<EKL_SDB>) data)
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(new DisposableCompletableObserver() {
                                            @Override
                                            public void onComplete() {
                                                Globals.writeToMLOG("ERROR", "Exchange/new EKLExchange().downloadEKLTable/onSuccess/onComplete", "OK");
                                            }

                                            @Override
                                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                                Globals.writeToMLOG("ERROR", "Exchange/new EKLExchange().downloadEKLTable/onSuccess/onError", "Throwable e: " + e);
                                            }
                                        });
                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "Exchange/new EKLExchange().downloadEKLTable/onSuccess", "Exception e: " + e);
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            Globals.writeToMLOG("ERROR", "Exchange/new EKLExchange().downloadEKLTable/onFailure", "error: " + error);
                        }
                    });
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "Exchange/new EKLExchange().downloadEKLTable", "Exception e: " + e);
                }


                try {
                    // Синхронизация стандартов
                    StandartExchange standartExchange = new StandartExchange();
                    standartExchange.downloadStandartTable(new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {
                            Log.e("MerchikTest", "data: " + data);
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
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/StandartExchange", "Exception e: " + e);
                }


                try {
                    // Чудо, что ты тут делаешь?
                    new DialogEKL(context, null).responseCheckEKLList();
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/DialogEKL", "Exception e: " + e);
                }


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

                                    Log.e("sendPhotoInformation", "photo.size(): " + photo.size());

                                    // Формируем ID шники для Стэк Фото
//                                    String[] photoIds = new String[photo.size()];
//                                    for (int i = 0; i < photo.size(); i++) {
//                                        String id = String.valueOf(photo.get(i).elementId);
//                                        Log.e("sendPhotoInformation", "id: " + id);
//                                        photoIds[i] = id;
//                                    }

                                    Log.e("sendPhotoInformation", "photoIds: " + Arrays.toString(ids));

                                    List<StackPhotoDB> stackPhoto = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getById(ids));
                                    Log.e("sendPhotoInformation", "stackPhoto: " + stackPhoto.size());

                                    for (StackPhotoDB item : stackPhoto) {
                                        for (PhotoInfoResponseList listItem : photo) {
                                            if (listItem.elementId.equals(item.getId())) {
                                                if (listItem.state) {
                                                    item.setCommentUpload(false);
                                                    Log.e("sendPhotoInformation", "listItem.state: " + listItem.state);
                                                } else {
                                                    item.setCommentUpload(false);
                                                    item.setComment(listItem.error);
                                                    Log.e("sendPhotoInformation", "listItem.state: " + listItem.state);
                                                    Log.e("sendPhotoInformation", "listItem.error: " + listItem.error);
                                                }
                                                Log.e("sendPhotoInformation", "stackPhoto item save: " + item.photoServerId);
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
                    /*                    ReclamationPointExchange tarExchange = new ReclamationPointExchange();
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
                    });     // Загрузка Задач и Рекламаций*/
                    sendTAR();              // Выгрузка на сервер ЗИР-а
                    uploadTARComments(null);    // Выгрузка ЗИР переписки(коммнетариев)
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

                try {
                    sendARMark();   // ОТПРАВКА ТЕСТ ОЦЕНОК
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/ReclamationPercentageExchange", "Exception e: " + e);
                }


//                updateLanguages();  // Обновление языков
//                updateSiteObj();    // Обновление Обьектов Сайта
//                updateTranslates();  // Обновление Переводов


                try {
                    new PotentialClientTableExchange().downloadPotentialClientTable(new Clicks.clickStatusMsg() {
                        @Override
                        public void onSuccess(String data) {
                            Globals.writeToMLOG("INFO", "Exchange/downloadPotentialClientTable/onSuccess", "data: " + data);
                        }

                        @Override
                        public void onFailure(String error) {
                            Globals.writeToMLOG("INFO", "Exchange/downloadPotentialClientTable/onFailure", "error: " + error);
                        }
                    });
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/PotentialClientTableExchange", "Exception e: " + e);
                }


                try {
                    /*Загрузка ОБРАЗЦОВ ФОТО*/
                    SamplePhotoExchange samplePhotoExchange = new SamplePhotoExchange();
                    samplePhotoExchange.downloadSamplePhotoTable(new Clicks.clickObjectAndStatus() {
                        @Override
                        public void onSuccess(Object data) {
                            Globals.writeToMLOG("INFO", "Exchange/SamplePhotoExchange()/onSuccess", "data: " + data);

                            List<SamplePhotoSDB> res = (List<SamplePhotoSDB>) data;

                            try {
                                RealmManager.INSTANCE.executeTransaction(realm -> {
                                    if (samplePhotoExchange.synchronizationTimetableDB != null) {
                                        samplePhotoExchange.synchronizationTimetableDB.setVpi_app(System.currentTimeMillis() / 1000);
                                        realm.copyToRealmOrUpdate(samplePhotoExchange.synchronizationTimetableDB);
                                    }
                                });
                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "SamplePhotoExchange/downloadSamplePhotoTable/onResponse/onComplete/synchronizationTimetableDB", "Exception e: " + e);
                            }

                            samplePhotoExchange.downloadSamplePhotos(res, new Clicks.clickStatusMsg() {
                                @Override
                                public void onSuccess(String data) {
                                    Globals.writeToMLOG("INFO", "2Exchange/SamplePhotoExchange()/onSuccess", "data: " + data);
                                }

                                @Override
                                public void onFailure(String error) {
                                    Globals.writeToMLOG("ERROR", "2Exchange/SamplePhotoExchange()/onFailure", "error: " + error);
                                }
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            Globals.writeToMLOG("ERROR", "Exchange/SamplePhotoExchange()/onFailure", "error: " + error);
                        }
                    });
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/SamplePhotoExchange", "Exception e: " + e);
                }


                try {
                    downloadAchievements();
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/downloadAchievements", "Exception e: " + e);
                }

                try {
                    downloadVoteTable();
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/downloadVoteTable", "Exception e: " + e);
                }


                try {
                    downloadArticleTable();
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/downloadArticleTable", "Exception e: " + e);
                }


                try {
                    // Загрузка констант: процент рекламаций Киев, процент рекламаций Регионы
                    new ReclamationPercentageExchange().downloadAndSaveReclamationPercentage();
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/ReclamationPercentageExchange", "Exception e: " + e);
                }


                try {
                    // Загрузка таблички Длин Полочного пространства
                    new ShelfSizeExchange().downloadShelfSize();
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/ShelfSizeExchange", "Exception e: " + e);
                }


                try {
                    updateTAR(SQL_DB.tarDao().getByUploadStatusVotes());
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/updateTAR", "Exception e: " + e);
                }

                try {
                    new FragmentsExchange().downloadFragmentsTable(new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {

                        }

                        @Override
                        public void onFailure(String error) {

                        }
                    });
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "FragmentsExchange/downloadFragmentsTable", "Exception e: " + e);
                }

                try {
                    downloadSiteHints("2");
                    downloadVideoLessons();
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/downloadSiteHints/downloadVideoLessons", "Exception e: " + e);
                }

                try {
                    new VideoViewExchange().downloadVideoViewTable(new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {
                            SQL_DB.videoViewDao().insertAll((List<ViewListSDB>) data);
                        }

                        @Override
                        public void onFailure(String error) {

                        }
                    });
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/VideoViewExchange/downloadVideoLessons", "Exception e: " + e);
                }


                try {
                    new ShowcaseExchange().downloadShowcaseTable(new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {
                            SQL_DB.showcaseDao().insertAll((List<ShowcaseSDB>) data)
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(new DisposableCompletableObserver() {
                                        @Override
                                        public void onComplete() {
                                            Log.e("ShowcaseExchange", "OK");
                                            new ShowcaseExchange().downloadShowcasePhoto((List<ShowcaseSDB>) data);
                                        }

                                        @Override
                                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                            Log.e("ShowcaseExchange", "Throwable e: " + e);
                                        }
                                    });
                            Globals.writeToMLOG("ERROR", "startExchange/ShowcaseExchange/downloadShowcaseTable/onSuccess", "data: " + data.size());
                        }

                        @Override
                        public void onFailure(String error) {
                            Globals.writeToMLOG("ERROR", "startExchange/ShowcaseExchange/downloadShowcaseTable/onFailure", "error: " + error);
                        }
                    });
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "startExchange/ShowcaseExchange/downloadShowcaseTable", "Exception e: " + e);
                }

                // --------------------------------------------------------------
            } else {
                long time = (System.currentTimeMillis() - exchange) / 1000;
                Log.e("startExchange", "start/Время обновлять НЕ наступило. После обновления прошло: " + time + "секунд.");
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "startExchange", "Exception e: " + e);
        }

    }

    // ====================================^=^=^=^=^================================================

    public void sendTAR() {
        TARUpload data = new TARUpload();
        data.mod = "reclamation";
        data.act = "create";

        // Получаю на выгрузку ЗИР (таблицу)
//        List<TasksAndReclamationsDB> list = TasksAndReclamationsRealm.getToUnload();
        List<TasksAndReclamationsSDB> tarList = SQL_DB.tarDao().getByUploadStatus(1);

        // Создаю данные на выгрузку (запрос)
        List<TARUploadData> dataList = new ArrayList<>();

        for (TasksAndReclamationsSDB item : tarList) {

            TARUploadData el = new TARUploadData();
            el.tp = item.tp;
            el.element_id = item.id;
            el.addr_id = item.addr;
            el.user_id = item.author;
            el.client_id = item.client;
            el.vinovnik_id = item.vinovnik;
//            el.vinovnik_id = 14041;     // TODO КРИТИЧНО, ТОЛЬКО ТЕСТЫ !!!
            el.date = Clock.getDateString(item.dt);
            el.photo_id = item.photo;
            el.photo_hash = item.photoHash;
            el.theme_id = item.themeId;
            el.comment = item.comment;

            dataList.add(el);
        }

        data.data = dataList;


        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Log.e("sendTAR", "convertedObject: " + convertedObject);
        Log.e("sendTAR", "list.size(): " + tarList.size());


        if (tarList.size() > 0) {
            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<JsonObject>() {
                @Override
                public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    try {
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
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.sendTAR.Failure.DataList empty" + "\n");
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
            BlockingProgressDialog progressDialog = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: " + "Языки");
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
        BlockingProgressDialog progressDialog = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: " + "ОбьектыСайта");

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
            BlockingProgressDialog progressDialog = BlockingProgressDialog.show(context, "Обмен данными с сервером.", "Обновление таблицы: " + "Переводы");

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
                        Globals.writeToMLOG("INFO", "sendPhotoInformation.onFailure", "Throwable t: " + t);
                    }
                });
            }
        } else {
            // Данных на выгрузку ДВИ/Комментариев ... НЕТ
            Log.e("sendPhotoInformation", "data: " + "Данных нет");
            Globals.writeToMLOG("INFO", "sendPhotoInformation.onResponse", "Данных нет");
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
                Globals.writeToMLOG("INFO", "getPhotoInfoToUpload.DVI", "stackPhotoDB.size(): " + stackPhotoDB.size());
                break;

            case COMMENT:
                stackPhotoDB = RealmManager.stackPhotoDBListGetCommentToUpload();
                Log.e("getPhotoInfoToUpload", "COMMENT DB size: " + stackPhotoDB.size());
                Globals.writeToMLOG("INFO", "getPhotoInfoToUpload.COMMENT", "stackPhotoDB.size(): " + stackPhotoDB.size());
                break;

            case RATING:
                stackPhotoDB = RealmManager.stackPhotoDBListGetRatingToUpload();
                Log.e("getPhotoInfoToUpload", "RATING DB size: " + stackPhotoDB.size());
                Globals.writeToMLOG("INFO", "getPhotoInfoToUpload.RATING", "stackPhotoDB.size(): " + stackPhotoDB.size());
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

        Globals.writeToMLOG("INFO", "getPhotoInfoToUpload.RES", "res: " + res);

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

        SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.INSTANCE.copyFromRealm(RealmManager.getSynchronizationTimetableRowByTable("stack_photo"));

        PhotoDownload server = new PhotoDownload();
        PhotoTableRequest data = new PhotoTableRequest();
        data.mod = "images_view";
        data.act = "list_image";
        data.nolimit = "1";
        data.dt_upload = String.valueOf(synchronizationTimetableDB.getVpi_app());
        data.date_from = Clock.today_7;
        data.date_to = Clock.today;

        // Типо говорю что я уже обновился, что б мне постоянно не прилетали фотки и я не задалбівал сервер
        synchronizationTimetableDB.setVpi_app(System.currentTimeMillis() / 1000);
        RealmManager.setToSynchronizationTimetableDB(synchronizationTimetableDB);

        WpDataRealm.UserPostRes info = WpDataRealm.userPost(Globals.userId);
        switch (info) {
            case EMPTY:
                Log.e("getPhotoFromSite", "EMPTY");
                globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.getPhotoFromSite: " + "EMPTY" + "\n");
                break;

            case SUBORDINATE:
                Log.e("getPhotoFromSite", "SUBORDINATE");
                data.sotr_id = String.valueOf(Globals.userId);
                globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.getPhotoFromSite: " + "SUBORDINATE: " + data.sotr_id + "\n");
                server.getPhotoFromServer(data);
                break;

            case MANAGER:
                Log.e("getPhotoFromSite", "MANAGER");
                globals.writeToMLOG(Clock.getHumanTime() + "_INFO.Exchange.class.getPhotoFromSite: " + "MANAGER" + "\n");
//                server.getPhotoFromServer(data);
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
                if (!RealmManager.stackPhotoExistByObjectId(Integer.parseInt(tov.getiD()), imageType)) {
                    listId.add(tov.getiD());
                }

            } catch (Exception e) {
                // ЛОГ ошибки
            }
        }

        if (listId.size() == 0) {
            operationResult.onFailure("Пусто?");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("mod: ").append(mod).append("\n");
        sb.append("act: ").append(act).append("\n");
        sb.append("tovarOnly: ").append(tovarOnly).append("\n");
        sb.append("imageType: ").append(imageType).append("\n");
        sb.append("listId: ").append(listId).append("\n");

        Globals.writeToMLOG("INFO", "getTovarImg", sb.toString());

        retrofit2.Call<TovarImgResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_PHOTO_INFO(mod, act, tovarOnly, nolimit, imageType, listId);
        String finalImageType = imageType;
        call.enqueue(new retrofit2.Callback<TovarImgResponse>() {
            @Override
            public void onResponse(retrofit2.Call<TovarImgResponse> call, retrofit2.Response<TovarImgResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        List<TovarImgList> list = response.body().getList();

                        Globals.writeToMLOG("INFO", "getTovarImg/onResponse", new Gson().toJson(list));

                        if (list != null) {
                            downloadTovarImg(list, finalImageType, operationResult);
                        }
                    } catch (Exception e) {
                        Log.e("LOG", "SAVE_TO_LOG");
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
                            String path = Globals.saveImage1(bmp, imageType + "-" + list.get(finalI).getTovarId());

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


    public void uploadTARComments(TARCommentsDB tarCommentsDB) {

        Globals.writeToMLOG("INFO", "uploadTARComments", "Start uploadTARComments");

        List<TARCommentsDB> list = new ArrayList<>();
        if (tarCommentsDB != null) {
            list.add(RealmManager.INSTANCE.copyFromRealm(tarCommentsDB));
        } else {
            list = RealmManager.INSTANCE.copyFromRealm(TARCommentsRealm.getTARCommentToUpload());
        }

        Globals.writeToMLOG("INFO", "uploadTARComments", "list comments to upload(" + (list != null ? list.size() + "): " : "null"));

        if (list != null && list.size() > 0) {

            Log.e("uploadTARComments", "List not null: " + list.size());

            TARCommentDataUpload data = new TARCommentDataUpload();
            data.mod = "reclamation";
            data.act = "set_comment";

            List<TARCommentDataListUpload> dataList = new ArrayList<>();
            for (TARCommentsDB item : list) {

                Globals.writeToMLOG("INFO", "uploadTARComments/ОбновилФотоУКомментария", "tarCommentsDB: " + new Gson().toJson(item));

                TARCommentDataListUpload dataItem = new TARCommentDataListUpload();
                dataItem.id = item.getRId();
                dataItem.comment = item.getComment();

                if (item.commentId != null && item.dtUpdate != null && item.dtUpdate != 0) {
                    dataItem.comment_id = String.valueOf(item.commentId);
                }

                if (item.getPhoto() != null && !item.getPhoto().equals("")) {
                    dataItem.photo_id = item.getPhoto();
                }
                if (item.photo_hash != null && !item.photo_hash.equals("")) {
                    dataItem.photo_hash = item.photo_hash;
                }

                dataList.add(dataItem);
            }

            data.data = dataList;


            JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(data), JsonObject.class);
            Log.e("uploadTARComments", "convertedObject: " + convertedObject);

            Globals.writeToMLOG("INFO", "uploadTARComments", "convertedObject: " + convertedObject);


            retrofit2.Call<TARCommentsServerData> call = RetrofitBuilder.getRetrofitInterface().UPLOAD_TAR_COMMENT(RetrofitBuilder.contentType, convertedObject);
            List<TARCommentsDB> finalList = list;
            call.enqueue(new retrofit2.Callback<TARCommentsServerData>() {
                @Override
                public void onResponse(retrofit2.Call<TARCommentsServerData> call, retrofit2.Response<TARCommentsServerData> response) {
                    try {
                        List<TARCommentsDB> saveToDb = new ArrayList<>();
                        List<TARCommentsDB> deleteFromDb = new ArrayList<>();

                        TARCommentsServerData res = response.body();
                        if (res != null) {
                            if (res.getState() != null && res.getState()) {
                                if (res.getList() != null && res.getList().size() > 0) {
                                    Log.d("test", "test");
                                    int size = res.getList().size();
                                    for (int i = 0; i < size; i++) {
                                        Log.d("test", "test0");
                                        if (res.getList().get(i).getState() != null && res.getList().get(i).getState()) {
                                            TARCommentsDB recreateComment = new TARCommentsDB();  // Для того что б "обновлять" по первичному ключу данные/ copy
                                            recreateComment.setID(res.getList().get(i).getInfo().getCommentId());
                                            recreateComment.commentId = Integer.valueOf(res.getList().get(i).getInfo().getCommentId());
                                            recreateComment.setTp(finalList.get(i).getTp());
                                            recreateComment.setDt(finalList.get(i).getDt());
                                            recreateComment.setWho(finalList.get(i).getWho());
                                            recreateComment.setComment(finalList.get(i).getComment());
                                            recreateComment.setPhoto(finalList.get(i).getPhoto());
                                            recreateComment.photo_hash = finalList.get(i).photo_hash;
                                            recreateComment.setRId(finalList.get(i).getRId());
                                            recreateComment.setDvi(finalList.get(i).getDvi());
                                            recreateComment.setFrom1c(finalList.get(i).getFrom1c());
                                            recreateComment.setReportId(finalList.get(i).getReportId());
                                            recreateComment.setResponceId(finalList.get(i).getResponceId());
                                            recreateComment.startUpdate = false;

                                            saveToDb.add(recreateComment);
                                            deleteFromDb.add(finalList.get(i));
                                        } else {
                                            Log.d("test", "test1");
                                        }
                                    }
//                                    RealmManager.INSTANCE.executeTransaction((realm) -> {
//                                        realm.copyToRealmOrUpdate(finalList);
//                                    });

                                    // Удаление Старых ID
                                    String[] ids = new String[deleteFromDb.size()];
                                    int i = 0;
                                    for (TARCommentsDB item : deleteFromDb) {
                                        ids[i++] = item.getID();
                                    }

                                    RealmManager.INSTANCE.executeTransaction((realm) -> {
                                        Log.e("uploadTARComments", "ids: " + ids);
                                        RealmResults<TARCommentsDB> dell = realm.where(TARCommentsDB.class)
                                                .in("id", ids)
                                                .findAll();
                                        dell.deleteAllFromRealm();


                                        // Сохранение новых
                                        Log.e("uploadTARComments", "saveToDb: " + saveToDb.size());
                                        List<TARCommentsDB> result = realm.copyToRealmOrUpdate(saveToDb);
                                        Globals.writeToMLOG("INFO", "uploadTARComments/onResponse", "result list comments to seve(" + (result != null ? result.size() + "): .." : "null"));
                                    });
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("uploadTARComments", "Exception e: " + e);
                        Globals.writeToMLOG("ERROR", "uploadTARComments/onResponse", "Exception e: " + e);
                    }

                    Globals.writeToMLOG("INFO", "uploadTARComments/onResponse", "End uploadTARComments");
                }

                @Override
                public void onFailure(retrofit2.Call<TARCommentsServerData> call, Throwable t) {
                    Log.e("uploadTARComments", "t:" + t);
                    Globals.writeToMLOG("ERROR", "uploadTARComments/onFailure", "Throwable t: " + t);
                    Globals.writeToMLOG("INFO", "uploadTARComments/onFailure", "End uploadTARComments");
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
        try {
            StandartData standartData = new StandartData();
            standartData.mod = "reclamation";
            standartData.act = "update_data";

            StandartData.StandartDataTARUpload data = new StandartData.StandartDataTARUpload();

            data.element_id = 1;
            data.code_dad2 = uploadData.codeDad2;
            data.vote_score = uploadData.voteScore;
            data.vinovnik_score = uploadData.vinovnikScore;
            data.vinovnik_score_comment = uploadData.vinovnikScoreComment;
            data.sotr_opinion_id = uploadData.sotrOpinionId;

            data.dt_start_fact = uploadData.dt_start_fact;
            data.dt_end_fact = uploadData.dt_end_fact;

            standartData.data = Collections.singletonList(data);

            JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(standartData), JsonObject.class);
            Globals.writeToMLOG("INGO", "updateTAR", "convertedObject:" + convertedObject);

            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.e("updateTAR", "response:" + response.body());
                    Globals.writeToMLOG("INGO", "updateTAR", "response.body():" + response.body());
                    uploadData.uploadStatus = 0;
                    SQL_DB.tarDao().insertData(Collections.singletonList(uploadData));
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e("updateTAR", "t:" + t);
                    Globals.writeToMLOG("INGO", "updateTAR", "t:" + t);
                }
            });
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "updateTAR", "Exception e:" + e);
        }
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
        Globals.writeToMLOG("INFO", "Exchange.sendWpData2.JsonObject.convertedObject", "convertedObject" + convertedObject);

        if (data != null && data.data.size() > 0) {
            retrofit2.Call<WpDataUpdateResponse> call = RetrofitBuilder.getRetrofitInterface().SEND_WP_DATA(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<WpDataUpdateResponse>() {
                @Override
                public void onResponse(retrofit2.Call<WpDataUpdateResponse> call, retrofit2.Response<WpDataUpdateResponse> response) {
                    try {
                        Globals.writeToMLOG("INFO", "Exchange.sendWpData2.onResponse", "response" + response);
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().state) {
                                if (response.body().data != null && response.body().data.size() > 0) {
                                    // TODO Вынести это в нормальную функцию и отдельный вызов.
                                    Long[] ids = new Long[response.body().data.size()];
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
     * 27.01.22
     * Нужно перекотиться на неё
     */
    public void sendWpDataToServer(Click result) {
        UploadDataSEWork data = new UploadDataSEWork();
        data.mod = "plan";
        data.act = "update_data";
        data.data = RealmManager.getWpDataStartEndWork();

        JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(data), JsonObject.class);

        Globals.writeToMLOG("INFO", "Exchange.sendWpDataToServer", "convertedObject" + convertedObject);

        if (data != null && data.data.size() > 0) {
            retrofit2.Call<WpDataUpdateResponse> call = RetrofitBuilder.getRetrofitInterface().SEND_WP_DATA(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<WpDataUpdateResponse>() {
                @Override
                public void onResponse(retrofit2.Call<WpDataUpdateResponse> call, retrofit2.Response<WpDataUpdateResponse> response) {
                    try {
                        Globals.writeToMLOG("INFO", "Exchange.sendWpDataToServer.onResponse", "response" + response);
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                if (response.body().state) {
                                    if (response.body().data != null && response.body().data.size() > 0) {
                                        saveWpDataResult(response.body().data);
                                        result.onSuccess("Данные о проведении обработаны успешно.");
                                    } else if (response.body().error != null && !response.body().error.equals("")) {
                                        Globals.writeToMLOG("ERROR", "Exchange.sendWpDataToServer.onResponse.response.body().error", "Error: " + response.body().error);
                                        result.onFailure("Возникла проблемма с обработкой данных на сервере по причине: " + response.body().error);
                                    } else if (response.body().data == null) {
                                        result.onSuccess("Запрос на проведение прошел успешно, но данных для обработки сервер не вернул.");
                                    } else {
                                        result.onSuccess("Запрос на проведение прошел успешно.");
                                    }
                                } else {
                                    result.onFailure("Данных для обработки с сервера не вернулось. Повторите попытку позже или обратитесь к своему руководителю.");
                                }
                            } else {
                                result.onFailure("Данных для обработки с сервера не вернулось. Повторите попытку позже или обратитесь к своему руководителю.");
                            }
                        } else {
                            result.onFailure("Ошибка сервера. Повторите попытку позже или обратитесь к руководителю. \nОшибка: " + response.code());
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "Exchange.sendWpDataToServer.onResponse", "Exception e: " + e);
                        result.onFailure("Произошла ошибка в анализе данных. \nОшибка: " + e);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<WpDataUpdateResponse> call, Throwable t) {
                    Globals.writeToMLOG("ERROR", "Exchange.sendWpData2.onFailure", "Throwable t: " + t);
                    result.onFailure("Возникла ошибка связи. Проверьте состояние интернета и повторите попытку позже. \nОшибка: " + t);
                }
            });
        }
    }

    /**
     * 27.01.22.
     * Сохранение обновлённых данных в БД
     */
    public void saveWpDataResult(List<WpDataUpdateResponseList> data) {
        try {
            Globals.writeToMLOG("INFO", "Exchange.saveWpDataResult.data", "data: " + data.size());
            Long[] ids = new Long[data.size()];
            int count = 0;
            for (WpDataUpdateResponseList item : data) {
                ids[count++] = item.elementId;
            }

            List<WpDataDB> wp = RealmManager.INSTANCE.copyFromRealm(WpDataRealm.getWpDataRowByIds(ids));
            List<WpDataDB> saveWp = new ArrayList<>();

            for (WpDataDB item : wp) {
                for (WpDataUpdateResponseList itm : data) {
                    if (itm.elementId.equals(item.getId())) {
                        if (itm.data.visitStartDt || itm.data.visitEndDt) {
                            item.startUpdate = false;
                        } else {
                            item.setSetStatus(0);
                        }
                        saveWp.add(item);
                    }
                }
            }
            WpDataRealm.setWpData(saveWp);
        } catch (Exception e) {
            // Exchange.saveWpDataResult.ERROR Exception e: java.lang.IllegalArgumentException: Invalid query: field 'id' not found in class 'WpDataDB'.
            Globals.writeToMLOG("ERROR", "Exchange.saveWpDataResult.ERROR", "Exception e: " + e);
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

        List<AdditionalRequirementsMarkDB> realmList = AdditionalRequirementsMarkRealm.getToUpload();
        List<AdditionalRequirementsMarkDB> list = RealmManager.INSTANCE.copyFromRealm(realmList);
        List<MarkData> markLIST = new ArrayList<>();

        if (list == null) {
            Globals.writeToMLOG("INFO", "sendARMark", "list to download: NULL");
            return;
        }
        Globals.writeToMLOG("INFO", "sendARMark", "list: " + list.size());


        for (AdditionalRequirementsMarkDB item : list) {
            MarkData mark = new MarkData();

            mark.id = String.valueOf(item.getItemId());
            mark.element_id = item.getItemId();
            mark.score = item.getScore();
            mark.tp_id = item.getTp();
            mark.comment = item.comment;

            markLIST.add(mark);
        }

        data.data = markLIST;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Log.e("sendARMark", "convertedObject: " + convertedObject);
        Globals.writeToMLOG("INFO", "sendARMark", "convertedObject: " + convertedObject);

//        retrofit2.Call<JsonObject> testCall = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
//        testCall.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                Log.e("sendARMarktestCall", "response: " + response);
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.e("sendARMarktestCall", "Throwable: " + t);
//            }
//        });

        retrofit2.Call<AdditionalRequirementsSendMarksServerData> call = RetrofitBuilder.getRetrofitInterface().SEND_ADDREP_MARKS(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new retrofit2.Callback<AdditionalRequirementsSendMarksServerData>() {
            @Override
            public void onResponse(retrofit2.Call<AdditionalRequirementsSendMarksServerData> call, retrofit2.Response<AdditionalRequirementsSendMarksServerData> response) {
                try {
                    try {

                        if (response.body().getList() != null && response.body().getList().size() > 0) {
                            List<AdditionalRequirementsMarksListServerData> info = response.body().getList();

                            for (AdditionalRequirementsMarksListServerData item : info) {
                                if (item.state) {
                                    for (AdditionalRequirementsMarkDB ARMark : list) {
                                        if (item.elementId.equals(ARMark.getItemId())) {
                                            ARMark.setUploadStatus(String.valueOf(System.currentTimeMillis()));
                                        }
                                    }
                                }
                            }
                            AdditionalRequirementsMarkRealm.setDataToDB(list);
                        }

                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "Exchange.class.sendARMark.onResponse", "Exception(set to DB) e: " + e);
                    }

                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "Exchange.class.sendARMark.onResponse", "Exception(response) e: " + e);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<AdditionalRequirementsSendMarksServerData> call, Throwable t) {
                Globals.writeToMLOG("ERROR", "Exchange.class.sendARMark.onFailure", "Throwable t: " + t);
            }
        });
    }


    /**
     * 20.05.2021
     * Отправка ЛОГА местоположения
     */
/*    public void sendLogMp() {
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

    }*/


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
     * 14.11.2022
     * Получение групп чатов
     */
    public static void chatGroupExchange() {
        StandartData data = new StandartData();
        data.mod = "chat";
        data.act = "list";
//        data.dt_change_from = Clock.today;
//        data.dt_change_to = Clock.tomorrow;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<ChatGrpResponse> call = RetrofitBuilder.getRetrofitInterface().CHAT_GRP_DOWNLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<ChatGrpResponse>() {
            @Override
            public void onResponse(Call<ChatGrpResponse> call, Response<ChatGrpResponse> response) {
                Log.e("chatExchange", "response: " + response);
                SQL_DB.chatGrpDao().insertData(response.body().list)
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
            public void onFailure(Call<ChatGrpResponse> call, Throwable t) {
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
//        if (StackPhotoRealm.checkByType5()) {
//            exchange.onFailure("Все данные уже загружены");
//            return;
//        }

        StandartData data = new StandartData();
        data.mod = "images_view";
        data.act = "list_image";
        data.date_from = Clock.getDatePeriod(-180);
        data.date_to = Clock.today;
        data.photo_type = "5";
        data.nolimit = "1";

        // ВПИ с таблички синхронизаций
        SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.getSynchronizationTimetableRowByTable("planogram");
        String dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());
        if (dt_change_from.equals("0")) {
            data.dt_change_from = "0";
        } else {
            data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app() - 120);  // минус 2 минуты для "синхрона". Это надо поменять.
        }

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<ImagesViewListImageResponse> call = RetrofitBuilder.getRetrofitInterface().GET_PHOTOS(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<ImagesViewListImageResponse>() {
            @Override
            public void onResponse(Call<ImagesViewListImageResponse> call, Response<ImagesViewListImageResponse> response) {
                try {
                    if (response.isSuccessful() && response.code() == 200) {
                        if (response.body() != null && response.body().state) {
                            RealmManager.INSTANCE.executeTransaction(realm -> {
                                synchronizationTimetableDB.setVpi_app(System.currentTimeMillis() / 1000);
                                realm.copyToRealmOrUpdate(synchronizationTimetableDB);
                            });

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
     * <p>
     * Сюда нужно воткнуть обработку (прогресс).
     * Добавить профайлер
     * Обработка результата
     * Обновление Пинга
     */
    public List<String> exchangeListInfo;

    public void exchangeTableWpData() {
        Globals.setProfiler(); // Установка профайлера


        // Должна быть сама Синхронизация Плана работ.

    }


    public void downloadAdditionalMaterials() {
        getAdditionalMaterialsAddress();
        getAdditionalMaterials();
        getAdditionalMaterialsGroups();
//        getAdditionalMaterialsLinks(0);
    }


    /**
     * 22.02.2022
     * Получения списка переводов фамилий
     */
    public void getSotrTranslates() {
//        mod=data_list
//        act=sn_sotr_list
//        фильтры
//        dt_change_from - впи с
//        dt_change_to - впи до

        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "sn_sotr_list";
        data.dt_change_from = "";
        data.dt_change_to = "";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("test", "test" + response);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("test", "test" + t);
            }
        });
    }

    /**
     * 22.02.2022
     * Получения списка адресов для доп материалов
     */
    public void getAdditionalMaterialsAddress() {
        /*mod=additional_materials
        act=addr_list

        фильтры

        dt_change_from - впи с
        dt_change_to - впи по*/

        StandartData data = new StandartData();
        data.mod = "additional_materials";
        data.act = "addr_list";
        data.dt_change_from = "";
        data.dt_change_to = "";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<AdditionalMaterialsAddressResponse> call = RetrofitBuilder.getRetrofitInterface().GET_ADDITIONAL_MATERIAL_ADDRESS(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<AdditionalMaterialsAddressResponse>() {
            @Override
            public void onResponse(Call<AdditionalMaterialsAddressResponse> call, Response<AdditionalMaterialsAddressResponse> response) {
                Log.e("test", "test" + response);
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        try {
                            SQL_DB.additionalMaterialsAddressDao().insertAll(response.body().list);
                        } catch (Exception e) {
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AdditionalMaterialsAddressResponse> call, Throwable t) {
                Log.e("test", "test" + t);
            }
        });
    }

    /**
     * 22.02.2022
     * Получения списка групп для доп материалов
     */
    public void getAdditionalMaterialsGroups() {
        /*mod=additional_materials
        act=group_list

        фильтры

        dt_change_from - впи с
        dt_change_to - впи по*/

        StandartData data = new StandartData();
        data.mod = "additional_materials";
        data.act = "group_list";
//        data.dt_change_from = "";
//        data.dt_change_to = "";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<AdditionalMaterialsGroupsResponse> call = RetrofitBuilder.getRetrofitInterface().AdditionalMaterialsGroupsResponse_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<AdditionalMaterialsGroupsResponse>() {
            @Override
            public void onResponse(Call<AdditionalMaterialsGroupsResponse> call, Response<AdditionalMaterialsGroupsResponse> response) {
                Log.e("test", "test" + response);

                try {
                    SQL_DB.additionalMaterialsGroupsDao().insertAll(response.body().list);
                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(Call<AdditionalMaterialsGroupsResponse> call, Throwable t) {
                Log.e("test", "test" + t);
            }
        });
    }

    /**
     * 22.02.2022
     * Получения списка доп материалов
     */
    public void getAdditionalMaterials() {
        /*mod=additional_materials
        act=list

        фильтры

        dt_change_from - впи с
        dt_change_to - впи по*/

        StandartData data = new StandartData();
        data.mod = "additional_materials";
        data.act = "list";
        data.dt_change_from = "";
        data.dt_change_to = "";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<AdditionalMaterialsResponse> call = RetrofitBuilder.getRetrofitInterface().GET_ADDITIONAL_MATERIAL(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<AdditionalMaterialsResponse>() {
            @Override
            public void onResponse(Call<AdditionalMaterialsResponse> call, Response<AdditionalMaterialsResponse> response) {
                Log.e("test", "test" + response);

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().state) {
                            SQL_DB.additionalMaterialsDao().insertAll(response.body().list);
                        }
                    }
                } else {
                    //
                }
            }

            @Override
            public void onFailure(Call<AdditionalMaterialsResponse> call, Throwable t) {
                Log.e("test", "test" + t);
            }
        });
    }

    /**
     * 22.02.2022
     * Получения списка ссылки на скачивание доп материала
     */
    public void getAdditionalMaterialsLinks(Integer id, Click result) {
        /*mod=additional_materials
        act=download_url
        id= ID файла, который требуется скачать

        если в ответ на запрос в поле state возвращается false, то в поле error будет содержаться описание ошибки
        если в ответ на запрос в поле state возвращается true, то в поле url будет содержаться ссылка на скачивание файла, которую нужно открыть из приложения в браузере*/

        StandartData data = new StandartData();
        data.mod = "additional_materials";
        data.act = "download_url";
        data.id = String.valueOf(id);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<AdditionalMaterialsLinksResponse> call = RetrofitBuilder.getRetrofitInterface().GET_ADDITIONAL_MATERIAL_LINK(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<AdditionalMaterialsLinksResponse>() {
            @Override
            public void onResponse(Call<AdditionalMaterialsLinksResponse> call, Response<AdditionalMaterialsLinksResponse> response) {
                Log.e("test", "test" + response);

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().state) {
                            result.onSuccess(response.body().url);
                        } else {
                            result.onFailure("Ошибка: Запрос прошел не успешно.");
                        }
                    } else {
                        result.onFailure("Ошибка: Пустое тело запроса.");
                    }
                } else {
                    result.onFailure("Ошибка: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AdditionalMaterialsLinksResponse> call, Throwable t) {
                Log.e("test", "test" + t);
                result.onFailure("Ошибка: " + t);
            }
        });
    }


    //групп товаров по клиентам:

    /**
     * 08.03.2022
     * TV для получения групп товаров по клиентам
     */
    public void getGroupTovByClient() {
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "client_tovar_group_list";

        data.dt_change_from = "";
        data.dt_change_to = "";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("test", "test" + response);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("test", "test" + t);
            }
        });
    }


    /**
     * 08.03.2022
     * Добавление нового ПТТ-шника
     */
    public void changeOrAddNewPTT() {

        StandartData data = new StandartData();

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("test", "test" + response);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("test", "test" + t);
            }
        });
    }


    public void downloadAchievements() {
        StandartData data = new StandartData();
        data.mod = "images_achieve";
        data.act = "list";

//        StandartData.Filter filter = new StandartData.Filter();
//        filter.date_from = "2022-09-01";
//        filter.date_to = "2022-10-19";
//        filter.confirm = "";
//        filter.is_view = "";
//        data.filter = filter;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<AchievementsResponse> call = RetrofitBuilder.getRetrofitInterface().ACHIEVEMENTS_DOWNLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<AchievementsResponse>() {
            @Override
            public void onResponse(Call<AchievementsResponse> call, Response<AchievementsResponse> response) {
                Log.e("test", "test" + response);
                try {
                    Globals.writeToMLOG("INFO", "downloadAchievements/onResponse", "response: " + response.body().list.size());

                    SQL_DB.achievementsDao().insertAllCompletable(response.body().list)
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    Globals.writeToMLOG("OK", "downloadAchievements/onResponse/onComplete", "OK");
                                }

                                @Override
                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                    Globals.writeToMLOG("ERROR", "downloadAchievements/onResponse/onError", "Throwable e: " + e);
                                }
                            });
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "downloadAchievements/onResponse", "Exception e: " + e);
                }
            }

            @Override
            public void onFailure(Call<AchievementsResponse> call, Throwable t) {
                Log.e("test", "test" + t);
                Globals.writeToMLOG("ERROR", "downloadAchievements/onFailure", "Throwable t: " + t);
            }
        });
    }


    /**
     * 17.10.2022
     * Получение Таблички Оценок
     */
    public void downloadVoteTable() {
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "images_vote";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<VoteResponse> call = RetrofitBuilder.getRetrofitInterface().VOTES_DOWNLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<VoteResponse>() {
            @Override
            public void onResponse(Call<VoteResponse> call, Response<VoteResponse> response) {
                Log.e("test", "test" + response);

                try {
                    Globals.writeToMLOG("INFO", "downloadVoteTable/onResponse", "response: " + response.body().list.size());

                    SQL_DB.votesDao().insertAllCompletable(response.body().list)
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    Globals.writeToMLOG("OK", "downloadVoteTable/onResponse/onComplete", "OK");
                                }

                                @Override
                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                    Globals.writeToMLOG("ERROR", "downloadVoteTable/onResponse/onError", "Throwable e: " + e);
                                }
                            });
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "downloadVoteTable/onResponse", "Exception e: " + e);
                }
            }

            @Override
            public void onFailure(Call<VoteResponse> call, Throwable t) {
                Log.e("test", "test" + t);
                Globals.writeToMLOG("ERROR", "downloadVoteTable/onFailure", "Throwable t: " + t);
            }
        });
    }


    /**
     * 18.10.2022
     * <p>
     * Выгрузка Оценок на сторону сервера.
     * - На этапе 18.10.22. используется (всё ещё нет ибо я на своей стороне оценки не ставлю) для выгрузки оценок Достижений
     */
    public void sendVote(List<VoteSDB> votes) {

        if (votes != null && votes.size() > 0) {
            Globals.writeToMLOG("INFO", "sendVote", "have " + votes.size() + " votes to send");

            StandartData data = new StandartData();
            data.mod = "images_achieve";
            data.act = "set_score";

            List<StandartData.ImagesAchieve> votesToSend = new ArrayList<>();

            for (VoteSDB item : votes) {
                StandartData.ImagesAchieve vote = new StandartData.ImagesAchieve();
                vote.id = item.serverId;
                vote.score = item.score;
                vote.comment = item.comments;
                vote.element_id = item.id;
            }

            data.data = votesToSend;

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.e("test", "test" + response);
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e("test", "test" + t);
                }
            });

        } else {
            // Мне нечего выгружать
            Globals.writeToMLOG("INFO", "sendVote", "have not votes to send");
        }
    }


    public void downloadArticleTable() {
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "tovar_vendor_code_list";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<ArticleResponse> call = RetrofitBuilder.getRetrofitInterface().ARTICLE_DOWNLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<ArticleResponse>() {
            @Override
            public void onResponse(Call<ArticleResponse> call, Response<ArticleResponse> response) {
                Log.e("test", "test" + response);
                try {
                    Globals.writeToMLOG("INFO", "downloadArticleTable/onResponse", "response: " + response.body().list.size());
                    SQL_DB.articleDao().insertAllCompletable(response.body().list)
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    Globals.writeToMLOG("OK", "downloadArticleTable/onResponse/onComplete", "OK");
                                }

                                @Override
                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                    Globals.writeToMLOG("ERROR", "downloadArticleTable/onResponse/onError", "Throwable e: " + e);
                                }
                            });

                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "downloadArticleTable/onResponse", "Exception e: " + e);
                }
            }

            @Override
            public void onFailure(Call<ArticleResponse> call, Throwable t) {
                Log.e("test", "test" + t);
                Globals.writeToMLOG("ERROR", "downloadArticleTable/onFailure", "Throwable t: " + t);
            }
        });
    }


    /**
     * 29.11.22.
     * Создание прямого запроса на Проведение документа.
     */
    public static void conductingOnServerWpData(WpDataDB wp, long codeDad2, Click click) {
        StandartData data = new StandartData();
        data.mod = "plan";
        data.act = "document_complete";
        data.code_dad2 = String.valueOf(codeDad2);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("INFO", "conductingOnServerWpData", "convertedObject: " + convertedObject);

        retrofit2.Call<ConductWpDataResponse> call = RetrofitBuilder.getRetrofitInterface().CONDUCT_WP_DATA(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<ConductWpDataResponse>() {
            @Override
            public void onResponse(Call<ConductWpDataResponse> call, Response<ConductWpDataResponse> response) {
                Log.e("conductingOnServer", "response: " + response.body());
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().state) {
                            // Пока пусть будет, я не знаю что им там в голову бахнет
                            if (response.body().document_complete && wp.getClient_id().equals(wp.getIsp())) {
                                click.onSuccess(response.body().notice);
                            } else {
                                click.onSuccess(response.body().notice);
//                                click.onFailure("Не можу провести документ, причина: " + response.body().notice);
                            }
                        } else {
                            click.onFailure("Не можу обробити документ, причина: " + response.body().error);
                        }
                    } else {
                        click.onFailure("Нема даних для обробки.");
                    }
                } else {
                    click.onFailure("Код запиту до сервера: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ConductWpDataResponse> call, Throwable t) {
                Log.e("conductingOnServer", "Throwable t: " + t);
                click.onFailure("Нема зв'язку. Помилка: " + t);
            }
        });
    }

}
