package ua.com.merchik.merchik;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgList;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class DownloadPictureService extends Service {

    public static List<TovarImgList> picList;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        List<TovarImgList> data = picList;
        downloadPhoto(data, new Clicks.clickStatusMsg() {
            @Override
            public void onSuccess(String data) {

            }

            @Override
            public void onFailure(String error) {

            }
        }, new Clicks.clickStatusMsgMode() {
            @Override
            public void onSuccess(String data, Clicks.MassageMode mode) {

            }

            @Override
            public void onFailure(String error) {

            }
        });

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
    public static void downloadPhoto(List<TovarImgList> data, Clicks.clickStatusMsg result, Clicks.clickStatusMsgMode result2) {
        long start = System.currentTimeMillis() / 1000;
        final int[] cnt = {0};
        int count = 0;
        notSuccessfulResponse = 0;
        bodyIsNull = 0;
        saveNewTovarPhoto = 0;
        errorSaveTovarPhoto = 0;
        internetError = 0;
        long countTP = 0;
        int desiredPhotoTP = 18; // Значение, которое вы хотите проверить
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            countTP = data.stream()
                    .filter(item -> {
                        try {
                            int photoTP = Integer.parseInt(item.getPhotoTp());
                            return photoTP == desiredPhotoTP;
                        } catch (NumberFormatException e) {
                            return false; // В случае ошибки парсинга числа
                        }
                    })
                    .count();
        }


        for (TovarImgList item : data) {
            int photoTP = Integer.parseInt(item.getPhotoTp());

            if (photoTP == 18) {
                count++;
                retrofit2.Call<ResponseBody> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_PHOTO_BY_URL(item.getPhotoUrl());
                int finalCount = count;
                long finalCountTP = countTP;
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
                });
            }
        }

        result.onSuccess("Фоток с типом 18: " + count);
    }
}
