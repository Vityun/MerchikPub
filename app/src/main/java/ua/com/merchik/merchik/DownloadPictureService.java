package ua.com.merchik.merchik;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.ResponseBody;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgList;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class DownloadPictureService extends Service {

    public Context context;
    public static List<TovarImgList> picList;


    @Override
    public void onCreate() {
        super.onCreate();
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        context = this;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1000")
                .setSmallIcon(R.mipmap.merchik)
                .setContentTitle("Завантаження")
                .setContentText("Завантажую фото Товарів")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel(
                    "1000",
                    "1000",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(nc);
        }

        nm.notify(1000, builder.build());
        startForeground(1000, builder.build());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            List<TovarImgList> data = picList;
            downloadPhoto(data, new Clicks.click() {
                @Override
                public <T> void click(T data) {
//                    try {
//                        Log.e("DownloadPictureService", "onStartCommand: " + data);
//                        RealmManager.stackPhotoSavePhoto((List<StackPhotoDB>)data);
//                    }catch (Exception e){
//                        Log.e("DownloadPictureService", "Exception e: " + e);
//                    }
                }
            });
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "DownloadPictureService/onStartCommand/", "Exception e: " + Arrays.toString(e.getStackTrace()));
        }

        return Service.START_REDELIVER_INTENT;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    static int notSuccessfulResponse;
    static int bodyIsNull;
    static int saveNewTovarPhoto;
    static int errorSaveTovarPhoto;
    static int internetError;

    static List<StackPhotoDB> savePhotoToDB = new ArrayList<>();


    static int count = 0;
    public void downloadPhoto(List<TovarImgList> data, Clicks.click click) {
        long start = System.currentTimeMillis() / 1000;
        final int[] cnt = {0};
        count = 0;
        notSuccessfulResponse = 0;
        bodyIsNull = 0;
        saveNewTovarPhoto = 0;
        errorSaveTovarPhoto = 0;
        internetError = 0;


        Observable.fromIterable(data)
                .filter(tovarImgList -> tovarImgList.getPhotoTp().equals("18") && tovarImgList.getPhotoUrl() != null && tovarImgList.getPhotoUrl().length() > 1)
                .flatMap(tovarImgList ->
                        RetrofitBuilder.getRetrofitInterface()
                                .DOWNLOAD_PHOTO_BY_URL_TEST(tovarImgList.getPhotoUrl())
                                .map(responseBody -> new SumTestObj(responseBody, tovarImgList))
                                .toObservable()

                ).doOnNext(sumTestObj -> {
                    try {
                        Bitmap bmp = BitmapFactory.decodeStream(sumTestObj.observable.byteStream());
                        TovarImgList item = sumTestObj.tovarImgList;

                        String path = Globals.saveImage1(bmp, "TOVAR_" + item.getTovarId() + "_SID" + item.getID());


                        Realm realm = Realm.getDefaultInstance();
                        realm.executeTransaction(innerRealm -> {
                            try {
                                int id;
                                RealmResults<StackPhotoDB> realmResults = innerRealm.where(StackPhotoDB.class)
                                        .sort("id")
                                        .findAll();

                                try {
                                    StackPhotoDB s = innerRealm.copyFromRealm(Objects.requireNonNull(realmResults.last()));
                                    int idPhoto = s.getId();
                                    Log.e("DownloadPictureService", "s.getId(): " + s.getId());
                                    String idServerPhoto = s.getPhotoServerId();
                                    Log.e("DownloadPictureService", "idServerPhoto: " + idServerPhoto);
                                    id = idPhoto + 1;
                                } catch (Exception e) {
                                    id = 0;
                                    Log.e("DownloadPictureService", "Objects.requireNonNullException e: " + e);
                                }


                                Log.e("DownloadPictureService", "Objects.requireNonNull: " + id);
                                StackPhotoDB stackPhotoDB = new StackPhotoDB();
                                stackPhotoDB.setId(id);
                                stackPhotoDB.setPhotoServerId(item.getID());
                                stackPhotoDB.setObject_id(Integer.valueOf(item.getTovarId()));

                                stackPhotoDB.addr_id = Integer.valueOf(item.getAddrId());
                                stackPhotoDB.approve = Integer.valueOf(item.getApprove());
                                stackPhotoDB.dvi = Integer.valueOf(item.getDvi());

                                stackPhotoDB.setVpi(0);
                                stackPhotoDB.setCreate_time(Long.parseLong(item.getDt()) * 1000);
                                stackPhotoDB.setUpload_to_server(0);
                                stackPhotoDB.setGet_on_server(0);
                                stackPhotoDB.setPhoto_num(path);
                                stackPhotoDB.setPhoto_hash(item.getHash());
                                stackPhotoDB.setPhoto_type(Integer.valueOf(item.getPhotoTp()));
                                stackPhotoDB.setComment("small");
                                stackPhotoDB.setUpload_time(0);
                                stackPhotoDB.setUpload_status(0);
                                stackPhotoDB.setStatus(false);

                                innerRealm.copyToRealmOrUpdate(stackPhotoDB);

                            } catch (Exception e) {
                                Log.e("DownloadPictureService", "onNext/RealmManager Exception e" + e);
                            }
                        });
                        realm.close();


//                        try {
//                            Log.e("DownloadPictureService", "onNext/RealmManager HERE?");
//                            Realm realm = Realm.getDefaultInstance();
//                            realm.executeTransaction(innerRealm -> {
//                                try {
//                                    Log.e("DownloadPictureService", "onNext" + "photo: " + new Gson().toJson(stackPhotoDB));
//                                    innerRealm.copyToRealmOrUpdate(stackPhotoDB);
//                                } catch (Exception e) {
//                                    Log.e("DownloadPictureService", "onNext/RealmManager Exception e" + e);
//                                }
//                            });
//                            realm.close();
//                        }catch (Exception e){
//                            Log.e("DownloadPictureService", "doOnNext/RealmManager Exception eee" + e);
//                        }

                        saveNewTovarPhoto++;
                    } catch (Exception e) {
                        errorSaveTovarPhoto++;
                        Log.e("DownloadPictureService", "doOnNext/Exception e: " + e);
                    }
                })
                .observeOn(Schedulers.io())
                .subscribe(new Observer<SumTestObj>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.e("DownloadPictureService", "onSubscribe" + "d: " + d);
                    }

                    @Override
                    public void onNext(@NonNull SumTestObj sumTestObj) {
                        Log.e("DownloadPictureService", "onNext" + "sumTestObj: " + new Gson().toJson(sumTestObj));
                        count++;
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("DownloadPictureService", "onError: Throwable e" + e);
                        Globals.writeToMLOG("ERROR", "DownloadPictureService/downloadPhoto/onError", "Throwable e: " + Arrays.toString(e.getStackTrace()));
                    }

                    @Override
                    public void onComplete() {
                        try {
                            Log.e("DownloadPictureService", "onComplete" + "OK");
                            Log.e("DownloadPictureService", "onComplete" + "savePhotoToDB: " + savePhotoToDB.size());
                            Globals.writeToMLOG("ERROR", "DownloadPictureService/downloadPhoto/onComplete", "ОК: " + count);
                            click.click(savePhotoToDB);
                            Log.e("DownloadPictureService", "onComplete" + "OK1");
                        }catch (Exception e){
                            Log.e("DownloadPictureService", "onComplete/Exception e" + e);
                            Globals.writeToMLOG("ERROR", "DownloadPictureService/downloadPhoto/onComplete", "Exception e: " + e);
                        }
                    }
                });


    }//downloadPhoto

    class SumTestObj {
        public ResponseBody observable;
        public TovarImgList tovarImgList;

        public SumTestObj(ResponseBody observable, TovarImgList tovarImgList) {
            this.observable = observable;
            this.tovarImgList = tovarImgList;
        }
    }

}// service





    /*
                                  try {
                            Bitmap bmp = BitmapFactory.decodeStream(sumTestObj.observable.byteStream());
                            TovarImgList item = sumTestObj.tovarImgList;

                            String path = Globals.saveImage1(bmp, "TOVAR_" + item.getTovarId() + "_SID" + item.getID());

                            int id = RealmManager.stackPhotoGetLastId();
                            id++;

                            StackPhotoDB stackPhotoDB = new StackPhotoDB();
                            stackPhotoDB.setId(id);
                            stackPhotoDB.setPhotoServerId(item.getID());
                            stackPhotoDB.setObject_id(Integer.valueOf(item.getTovarId()));

                            stackPhotoDB.addr_id = Integer.valueOf(item.getAddrId());
                            stackPhotoDB.approve = Integer.valueOf(item.getApprove());
                            stackPhotoDB.dvi = Integer.valueOf(item.getDvi());

                            stackPhotoDB.setVpi(0);
                            stackPhotoDB.setCreate_time(Long.parseLong(item.getDt()) * 1000);
                            stackPhotoDB.setUpload_to_server(0);
                            stackPhotoDB.setGet_on_server(0);
                            stackPhotoDB.setPhoto_num(path);
                            stackPhotoDB.setPhoto_hash(item.getHash());
                            stackPhotoDB.setPhoto_type(Integer.valueOf(item.getPhotoTp()));
                            stackPhotoDB.setComment("small");
                            stackPhotoDB.setUpload_time(0);
                            stackPhotoDB.setUpload_status(0);
                            stackPhotoDB.setStatus(false);

                            // 30.01
                            RealmManager.stackPhotoSavePhoto(stackPhotoDB);
                            saveNewTovarPhoto++;
                        } catch (Exception e) {
                            errorSaveTovarPhoto++;
                        }





          call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    try {
                                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                                        String path = Globals.saveImage1(bmp, "TOVAR_" + item.getTovarId() + "_SID" + item.getID());

                                        int id = RealmManager.stackPhotoGetLastId();
                                        id++;

                                        StackPhotoDB stackPhotoDB = new StackPhotoDB();
                                        stackPhotoDB.setId(id);
                                        stackPhotoDB.setPhotoServerId(item.getID());
                                        stackPhotoDB.setObject_id(Integer.valueOf(item.getTovarId()));

                                        stackPhotoDB.addr_id = Integer.valueOf(item.getAddrId());
                                        stackPhotoDB.approve = Integer.valueOf(item.getApprove());
                                        stackPhotoDB.dvi = Integer.valueOf(item.getDvi());

                                        stackPhotoDB.setVpi(0);
                                        stackPhotoDB.setCreate_time(Long.parseLong(item.getDt()) * 1000);
                                        stackPhotoDB.setUpload_to_server(0);
                                        stackPhotoDB.setGet_on_server(0);
                                        stackPhotoDB.setPhoto_num(path);
                                        stackPhotoDB.setPhoto_hash(item.getHash());
                                        stackPhotoDB.setPhoto_type(Integer.valueOf(item.getPhotoTp()));
                                        stackPhotoDB.setComment("small");
                                        stackPhotoDB.setUpload_time(0);
                                        stackPhotoDB.setUpload_status(0);
                                        stackPhotoDB.setStatus(false);

                                        // 30.01
                                        RealmManager.stackPhotoSavePhoto(stackPhotoDB);
                                        saveNewTovarPhoto++;
                                    } catch (Exception e) {
                                        errorSaveTovarPhoto++;
                                    }
                                } else {
                                    bodyIsNull++;
                                }
                            } else {
                                notSuccessfulResponse++;
                            }

                            if (cnt[0] < finalCount) {
                                cnt[0]++;
                                result2.onSuccess("Завантажено " + cnt[0] + " фото з " + finalCountTP, Clicks.MassageMode.SHOW);
                            } else if (cnt[0] == finalCount) {
//                            result.onSuccess("S/Закончил работу, обработал(всего/с типом 18/загружено): " + data.size() + "/" + finalCount + "/" + cnt[0] + "\n\n(Код не 200(1)/Тело пустое(2)/Сохранило новую фотку товара(3)/Ошибка при сохранении фото(4)/Ошибка интернета(5))\n\n" + notSuccessfulResponse + "(1)/" + bodyIsNull + "(2)/" + saveNewTovarPhoto + "(3)/" + errorSaveTovarPhoto + "(4)/" + internetError + "(5)/");
//                            result.onSuccess("Завантажено " + cnt[0] + " фото з " + data.size());
                                result2.onSuccess("Закінчив завантаження, завантажено: " + cnt[0] + " фото.", Clicks.MassageMode.CLOSE);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            internetError++;
                            if (cnt[0] < finalCount) {
                                cnt[0]++;
                                result2.onSuccess("Завантажено " + cnt[0] + " фото з " + finalCountTP + "\nПомилка: " + t, Clicks.MassageMode.SHOW);
                            } else if (cnt[0] == finalCount) {
//                            result.onSuccess("F/Закончил работу, обработал(всего/с типом 18/загружено): " + data.size() + "/" + finalCount + "/" + cnt[0]);
//                            result.onSuccess("Завантажено " + cnt[0] + " фото з " + data.size());
                                result2.onSuccess("Закінчив завантаження, завантажено: " + cnt[0] + " фото." + "\nПомилка: " + t, Clicks.MassageMode.CLOSE);
                            }
                        }
                    });*/
