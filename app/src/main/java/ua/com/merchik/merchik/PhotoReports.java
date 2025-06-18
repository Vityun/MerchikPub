package ua.com.merchik.merchik;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import kotlin.Unit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.UploadPhotoData.ImagesPrepareUploadPhoto;
import ua.com.merchik.merchik.data.UploadPhotoData.Move;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/**
 * 11.06.2021
 * Создан для выгрузки фотоотчётов.
 * Uploading Photo Reports
 */
public class PhotoReports {

    private Context mContext;

    /*Правильно ли это сделано для текущего класса?*/
    private Realm realm = RealmManager.INSTANCE;

    /*Разрешение на выгрузку фотографий*/
    public boolean permission = true;

    /*Список/очередь на выгрузку фотоотчётов*/
    private List<StackPhotoDB> realmResults = new ArrayList<>();

    public enum UploadType {
        SINGLE,
        MULTIPLE,
        AUTO
    }

    //----------------------------------------------------------------------------------------------


    /*Может режим сюда сразу передавать?*/
    public PhotoReports(Context mContext) {
        this.mContext = mContext;
    }

    //==============================================================================================

    /**
     * 11.06.2021
     * Выгрузка фотоотчётов.
     */
    public void uploadPhotoReports(UploadType type) {
        getDataToUpload();  // Подготовка данных к выгрузке

        Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/uploadPhotoReports", "START. Upload type: " + type);
        Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/uploadPhotoReports", "START. size to unload: " + realmResults.size() + " /permission: " + permission);

        if (!realmResults.isEmpty()) {
            switch (type) {
                case MULTIPLE:
                    if (permission) {
                        Globals.writeToMLOG("INFO", "PhotoRиeports/upload_photo/uploadPhotoReports", "start MULTIPLE upload permission true. Начинаю выгружать фотки пакетом при клике на Выгрузить фото");

                        permission = false;

                        String msg = "Сейчас будет выгружено " + realmResults.size() + " фото на сервер. Дождитесь сообщения об окончании работы.";

                        DialogData dialog = new DialogData(mContext);
                        dialog.setTitle("Выгрузка фотоотчётов");
                        dialog.setText(msg);
                        dialog.setOk("Ок", () -> send(type));
                        dialog.setClose(dialog::dismiss);
                        dialog.show();
                    } else {
                        Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/uploadPhotoReports", "start MULTIPLE upload permission false. Я уже нажал на кнопку 'Выгрузить фото' и жду пока выгрузка закончится");

                        DialogData dialogData = new DialogData(mContext);
                        dialogData.setTitle("Выгрузка фото");
                        dialogData.setText("Вы уже начали выгрузку. Осталось выгрузить: " + realmResults.size() + " фото. \nДождитесь пока все фото выгрузятся.");
                        dialogData.show();
                    }

                    break;


                case AUTO:
                    if (permission) {
                        Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/uploadPhotoReports", "start AUTO upload permission false. Начинается Автовыгрузка фотографий");
                        permission = false;
                        send(type);
                    } else {
                        Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/uploadPhotoReports", "start AUTO upload permission false. Уже начата Автовыгрузка, нужно подождать пока она завершится, прежде чем собирать новые данные");
                    }
                    break;
            }
        } else {
            switch (type) {
                case MULTIPLE:
                    new MessageDialogBuilder((Activity) mContext)
                            .setStatus(DialogStatus.NORMAL)
                            .setTitle(mContext.getText(R.string.not_photo_title).toString())
                            .setMessage(mContext.getText(R.string.not_photo).toString())
                            .setOnConfirmAction(() -> Unit.INSTANCE)
                            .show();

//                    Toast.makeText(mContext, "Нет фото для ыгрузки", Toast.LENGTH_SHORT).show();
                    break;
            }
        }


    }


    /**
     * 11.06.2021
     * выгрузка и сохранение результатов
     */
    private void send(UploadType type) {
        Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/send/onSuccess", "Start SEND. size to unload: " + realmResults.size());
        Log.e("StartPhotoUpload", "++++++++++");
        if (!realmResults.isEmpty()) {
            StackPhotoDB current = realmResults.get(0);
            buildCall(current, new ExchangeInterface.UploadPhotoReports() {
                @Override
                public void onSuccess(StackPhotoDB photoDB, String s) {
                    Toast.makeText(mContext, "Фото номер: " + photoDB.getId() + " успешно выгружено", Toast.LENGTH_LONG).show();
                    realm.executeTransaction(realm -> {
                        photoDB.setError(null);
                        photoDB.setUpload_to_server(System.currentTimeMillis());
                        realm.insertOrUpdate(photoDB);
                        Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/send/onSuccess/realm.executeTransaction", "onSuccess. Фото выгружено. Отмечено в Realm");
                    });

                    realmResults.remove(photoDB);
                    String filePath = photoDB.getPhoto_num();
                    File file = new File(filePath);
                    Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/send/onSuccess", "successfully upload photo to server photo id: " + photoDB.getId() + ", file.length(): " + file.length() + ", left to unload: " + realmResults.size());
                    send(type);
                }

                @Override
                public void onFailure(StackPhotoDB photoDB, String error) {
                    realm.executeTransaction(realm -> {
                        photoDB.setError(1);
                        photoDB.setErrorTime(System.currentTimeMillis());
                        photoDB.setErrorTxt(error);
                        realm.insertOrUpdate(photoDB);
                        Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/send/onFailure/realm.executeTransaction", "onFailure. Фото с ошибкой. Отмечено в Realm");
                    });

                    realmResults.remove(photoDB);
                    Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/send/onFailure", "failure. photo id: " + photoDB.getId() + " ERROR MSG: " + error + " ///left to unload: " + realmResults.size());
                    send(type);

                    // Путь к файлу (пример: photoDB.getPhoto_num() возвращает путь в виде String)
                    String filePath = photoDB.getPhoto_num();
                    // Создаем объект File
                    File file = new File(filePath);
                    // Проверяем, существует ли файл
                    Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/send/onFailure", "file.length(): " + file.length() + ", file.exists(): " + file.exists() + ", isImageValid: " + isImageValid(file));

                    if (!isImageValid(file))
                        if (photoDB.getPhotoServerId() == null || photoDB.getPhotoServerId().isEmpty()
                                && isPhotoOlderThan10Minutes(photoDB)) {
                            StackPhotoRealm.deleteByPhotoNum(filePath);
                            Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/send/deleteByPhotoNum", "isImageValid: " + isImageValid(file) + ", file delete ");
                        }
                }
            });
        } else {
            switch (type) {
                case MULTIPLE:
                    Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/send/Send", "MULTIPLE/ выгрузка окончена");

                    // ВСЕ ФОТО БЫЛИ ВЫГРУЖЕНЫ
                    DialogData dialogData = new DialogData(mContext);
                    dialogData.setTitle("Выгрузка фото");
                    dialogData.setText("Выгрузка окончена");
                    dialogData.show();
                    permission = true;
                    break;

                case AUTO:
                    Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/send/Send", "AUTO/ выгрузка окончена");

                    permission = true;
                    break;
            }

        }
    }

    public static boolean isImageValid(File file) {
        try (InputStream is = new FileInputStream(file)) {
            byte[] header = new byte[8];
            if (is.read(header) != header.length) return false;

            // Проверка JPEG (начинается с FF D8 FF)
            if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
                return true;
            }
            // Проверка PNG (начинается с 89 50 4E 47)
            return header[0] == (byte) 0x89 && header[1] == (byte) 0x50 &&
                    header[2] == (byte) 0x4E && header[3] == (byte) 0x47;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isPhotoOlderThan10Minutes(StackPhotoDB photoDB) {
        if (photoDB == null || photoDB.getCreate_time() <= 0) {
            return false; // Некорректные данные
        }

        long photoTimeSeconds = photoDB.getCreate_time(); // Время создания фото в секундах
        long currentTimeSeconds = System.currentTimeMillis() / 1000; // Текущее время в секундах
        long tenMinutesInSeconds = 10 * 60; // 10 минут в секундах

        return (currentTimeSeconds - photoTimeSeconds) > tenMinutesInSeconds;
    }

    /**
     * 11.06.2021
     * Получение с базы данных фоток которые надо выгружать. Запись их в Список/Очередь на выгрузку.
     */
    private void getDataToUpload() {
        List<StackPhotoDB> res = realm.copyFromRealm(RealmManager.getStackPhotoPhotoToUpload());
        for (StackPhotoDB photo : res) {
            if (photo != null && !realmResults.contains(photo) && realmResults.size() < 20) {
                String filePath = photo.getPhoto_num();
                File file = new File(filePath);
                if (file.exists() && file.length() > 1) {
                    Globals.writeToMLOG("INFO", "PhotoReports.getDataToUpload", "realmResults add: " + photo.getPhoto_num());
                    realmResults.add(photo);
                } else
                    Globals.writeToMLOG("INFO", "PhotoReports.getDataToUpload", "realmResults not fit: " + photo.getPhoto_num());

            }
        }

        // Отладочная инфа. В перспективе нужно удалить.
        if (realmResults != null) {
            Globals.writeToMLOG("INFO", "PhotoReports.getDataToUpload", "realmResults(очередь на выгрузку) = " + realmResults.size());
        } else {
            Globals.writeToMLOG("INFO", "PhotoReports.getDataToUpload", "realmResults(очередь на выгрузку) - Пустое.(NULL)");
        }
    }


    /**
     * 11.06.2021
     * Сбор данных для запроса. Запрос на выгрузку фотографии.
     *
     * @return
     */
    private void buildCall(StackPhotoDB photoDB, ExchangeInterface.UploadPhotoReports callback) {
        int photoId = photoDB.getId();
        Globals.writeToMLOG("INFO", "PhotoReports/buildCall/", "START UPLOAD. Photo upload id: " + photoId);

        final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");


        // Запрос
        String mod = "images_prepare";
//        String act = "upload_image";images_prepareupload_photo
        String act = "upload_photo";

        String client_id = "";
        String addr_id = "";
        String date = "";
        String img_type_id = "";
        String photo_user_id = "";
        String client_tovar_group = "";
        String doc_num = "";
        String theme_id = "";
        String comment = "";
        String dvi = "";
        String code_dad2 = "";
        String gp = "";
        String tovar_id = "";

        String img_src_id = "0";
        String showcase_id = "0";
        String planogram_id = "0";
        String planogram_img_id = "0";
        String example_id = "";
        String example_img_id = "";

        // Распаковка данных с БД
        if (photoDB.getClient_id() != null) {
            client_id = photoDB.getClient_id();
        }
        if (photoDB.getAddr_id() != null) {
            addr_id = String.valueOf(photoDB.getAddr_id());
        }
        if (photoDB.getTime_event() != null) {
            date = photoDB.getTime_event();
        }
        if (photoDB.getPhoto_type() != null) {
            img_type_id = String.valueOf(photoDB.getPhoto_type());
        }
        if (photoDB.getPhoto_user_id() != null) {
            photo_user_id = photoDB.getPhoto_user_id();
        }
        if (photoDB.getPhoto_group_id() != null) {
            client_tovar_group = photoDB.getPhoto_group_id();
        }
        if (photoDB.getDoc_id() != null) {
            doc_num = photoDB.getDoc_id();
        }
        if (photoDB.getTheme_id() != null) {
            theme_id = String.valueOf(photoDB.getTheme_id());
        }
        if (photoDB.getComment() != null) {
            comment = photoDB.getComment();
        }
        if (photoDB.getDvi() != null) {
            dvi = String.valueOf(photoDB.getDvi());
        }

        if (photoDB.tovar_id != null && !photoDB.tovar_id.equals("")) {
            tovar_id = photoDB.tovar_id;
        }
        code_dad2 = String.valueOf(photoDB.getCode_dad2()); // todo какая-то странная дичь с этой строчкой.
        if (photoDB.getGp() != null) {
            gp = photoDB.getGp();
        }

        if (photoDB.img_src_id != null && !photoDB.img_src_id.equals("") && !photoDB.img_src_id.equals("null")) {
            img_src_id = photoDB.img_src_id;
        }

        if (photoDB.showcase_id != null && !photoDB.showcase_id.equals("") && !photoDB.showcase_id.equals("null")) {
            showcase_id = photoDB.showcase_id;
        }

        if (photoDB.planogram_id != null && !photoDB.planogram_id.equals("") && !photoDB.planogram_id.equals("null")) {
            planogram_id = photoDB.planogram_id;
        }

        if (photoDB.planogram_img_id != null && !photoDB.planogram_img_id.equals("") && !photoDB.planogram_img_id.equals("null")) {
            planogram_img_id = photoDB.planogram_img_id;
        }

        if (photoDB.example_id != null) {
            example_id = photoDB.example_id;
        }

        if (photoDB.example_img_id != null) {
            example_img_id = photoDB.example_img_id;
        }


        // Запаковка данных для сервера
        RequestBody mod2 = RequestBody.create(MediaType.parse("text/plain"), mod);
        RequestBody act2 = RequestBody.create(MediaType.parse("text/plain"), act);
        RequestBody client_id2 = RequestBody.create(MediaType.parse("text/plain"), client_id);
        RequestBody addr_id2 = RequestBody.create(MediaType.parse("text/plain"), addr_id);
        RequestBody date2 = RequestBody.create(MediaType.parse("text/plain"), date);
        RequestBody img_type_id2 = RequestBody.create(MediaType.parse("text/plain"), img_type_id);
        RequestBody photo_user_id2 = RequestBody.create(MediaType.parse("text/plain"), photo_user_id);
        RequestBody client_tovar_group2 = RequestBody.create(MediaType.parse("text/plain"), client_tovar_group);
        RequestBody doc_num2 = RequestBody.create(MediaType.parse("text/plain"), doc_num);
        RequestBody theme_id2 = RequestBody.create(MediaType.parse("text/plain"), theme_id);
        RequestBody comment2 = RequestBody.create(MediaType.parse("text/plain"), comment);
        RequestBody dvi2 = RequestBody.create(MediaType.parse("text/plain"), dvi);
        RequestBody codeDad2 = RequestBody.create(MediaType.parse("text/plain"), code_dad2);
        RequestBody gp2 = RequestBody.create(MediaType.parse("text/plain"), gp);
        RequestBody tov2 = RequestBody.create(MediaType.parse("text/plain"), tovar_id);

        RequestBody img_src_id2 = RequestBody.create(MediaType.parse("text/plain"), img_src_id);
        RequestBody showcase_id2 = RequestBody.create(MediaType.parse("text/plain"), showcase_id);
        RequestBody planogram_id2 = RequestBody.create(MediaType.parse("text/plain"), planogram_id);
        RequestBody planogram_img_id2 = RequestBody.create(MediaType.parse("text/plain"), planogram_img_id);
        RequestBody example_id2 = RequestBody.create(MediaType.parse("text/plain"), example_id);
        RequestBody example_img_id2 = RequestBody.create(MediaType.parse("text/plain"), example_img_id);

        File file;
        file = new File(photoDB.getPhoto_num());

        Log.e("M_UPLOAD_GALLERY", "\n\nStart");
        Log.e("M_UPLOAD_GALLERY", "id: " + photoDB.getId());
        try {
            Globals.writeToMLOG("INFO", "PhotoReports.buildCall", "file.length()1: " + file.length());
            if (file.length() == 0) {
                Uri uri = Uri.parse(photoDB.getPhoto_num());
                Log.e("M_UPLOAD_GALLERY", "uri: " + uri);
                Log.e("M_UPLOAD_GALLERY", "mContext: " + mContext);

                file = new File(uri.getPath());


                // Проверяем версию Android и выполняем соответствующие действия
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Версия Android 11 и выше
                    if (checkManageExternalStoragePermission()) {
                        try {
                            Log.e("M_UPLOAD_GALLERY", "2");
                            file = new File(Globals.getRealPathFromURITEST(uri, mContext));
                            Log.e("M_UPLOAD_GALLERY", "uri_file.length()2.1: " + file.length());
                            Globals.writeToMLOG("INFO", "PhotoReports.buildCall", "uri_file.length()2.1: " + file.length());
                        } catch (Exception e) {
                            Log.e("M_UPLOAD_GALLERY", "Uri.parse/Exception e: " + e);
                        }
                    } else {
                        // Запрос разрешения MANAGE_EXTERNAL_STORAGE
//                        requestManageExternalStoragePermission();
                        Globals.writeToMLOG("INFO", "PhotoReports.buildCall", "Запрос разрешения MANAGE_EXTERNAL_STORAGE: Нет доступа");

                        Log.e("M_UPLOAD_GALLERY", "2");
                        file = new File(Globals.getRealPathFromURITEST(uri, mContext));
                        Log.e("M_UPLOAD_GALLERY", "uri_file.length()2.1: " + file.length());
                        Globals.writeToMLOG("INFO", "PhotoReports.buildCall", "uri_file.length()2.1.11111: " + file.length());
                    }
                } else {
                    // Версия Android ниже 11
                    try {
                        Log.e("M_UPLOAD_GALLERY", "2");
                        file = new File(Globals.getRealPathFromURITEST(uri, mContext));
                        Log.e("M_UPLOAD_GALLERY", "uri_file.length()2.1: " + file.length());
                        Globals.writeToMLOG("INFO", "PhotoReports.buildCall", "uri_file.length()2.1: " + file.length());
                    } catch (Exception e) {
                        Log.e("M_UPLOAD_GALLERY", "Uri.parse/Exception e: " + e);
                        Globals.writeToMLOG("INFO", "PhotoReports.buildCall", "uri_file.length()2.1: Exception e" + e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("M_UPLOAD_GALLERY", "uri/Exception e: " + e);
            Globals.writeToMLOG("INFO", "PhotoReports.buildCall", "file.length()Exception e: " + e);
        }


        if (file.length() == 0) {
            Globals.writeToMLOG("INFO", "PhotoReports.buildCall", "file.length()");
//            StackPhotoDB sp = StackPhotoRealm.getById(photoDB.getId());
//            sp.deleteFromRealm();
            callback.onFailure(photoDB, "Файл фотографии [id:" + photoDB.getId() + "] равен: " + file.length() + "(пуст)");
            return;
        }


        Log.e("M_UPLOAD_GALLERY", "FILE RES: " + file.length());

        // MultipartBody.Part is used to send also the actual file name
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part photo = MultipartBody.Part.createFormData("photos[]", file.getName(), requestBody);

//        MultipartBody.Part photo = MultipartBody.Part.createFormData("photos[]", file.getName(), RequestBody.create(MEDIA_TYPE_JPG, file));
        Globals.writeToMLOG("INFO", "PhotoReports.buildCall", "photo: " + photo.body());

        // Создание вызова выгрузка фото
        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface()
                .SEND_PHOTO_2_BODY(mod2, act2, client_id2, addr_id2, date2, img_type_id2, photo_user_id2, client_tovar_group2, doc_num2, theme_id2, comment2, dvi2, codeDad2, gp2, tov2, img_src_id2, showcase_id2, planogram_id2, planogram_img_id2,
                        example_id2, example_img_id2,
                        photo);


//        Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL", "call: " + new Gson().toJson(call.request().body()));
        call.enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("M_UPLOAD_GALLERY", "HERE IN onResponse");
                Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody", "HERE IN");

                Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody", "HERE IN call: " + call);

                Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody", "HERE IN response: " + response);

                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody", "" + response.body());
                            ImagesPrepareUploadPhoto info = new Gson().fromJson(new Gson().toJson(response.body()), ImagesPrepareUploadPhoto.class);
                            Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody/info", "" + new Gson().toJson(info));
                            if (info.state) {
                                ImagesPrepareUploadPhoto.DataList data = info.list.get(0);
                                if (data.state) {
                                    callback.onSuccess(photoDB, "test");
                                } else {
                                    if (data.errorType != null) {
                                        Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody/errorType: ", data.errorType);
                                    }
                                    if (data.errorType.equals("photo_already_exist")) {
                                        Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody/info/photo_already_exist", "photo_already_exist");
                                        if (photoDB.photo_num.contains("SCREENSHOT") && photoDB.getPhoto_type() == 4)
                                            new MessageDialogBuilder((Activity) mContext)
                                                    .setStatus(DialogStatus.ALERT)
                                                    .setTitle("Сервер не прийняв фото")
                                                    .setSubTitle("Фото вже є на сервері")
                                                    .setMessage(data.error + ". Ви повинні завантажити нове фото")
                                                    .show();
                                        callback.onSuccess(photoDB, data.error);
                                    } else if (data.errorType.equals("missing_geo_coord")) {
                                        Globals.fixMP(null, null);
                                        new TablesLoadingUnloading().uploadLodMp(new ExchangeInterface.ExchangeRes() {
                                            @Override
                                            public void onSuccess(String ok) {
                                                Log.e("uploadLodMp", "uploadLodMp: " + ok);
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                Log.e("uploadLodMp", "uploadLodMp error: " + error);
                                            }
                                        });
                                        Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody/info/missing_geo_coord", "missing_geo_coord");
                                        callback.onFailure(photoDB, "Ошибка при обработке фото: " + data.error);
                                    } else {
                                        Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody/info/other_error", data.error);
                                        callback.onFailure(photoDB, "Ошибка при обработке фото: " + data.error);
                                    }
                                }
                            } else {
                                try {
                                    if (info.list != null && info.list.size() > 0) {
                                        ImagesPrepareUploadPhoto.DataList data = info.list.get(0);
                                        Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody/info/data", "" + new Gson().toJson(data));
                                        if (data.state) {
                                            callback.onSuccess(photoDB, "При выгрузке фото произошла ошибка1: " + data.error);
                                        } else {
                                            if (data.errorType.equals("photo_already_exist")) {
                                                Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody/info/photo_already_exist", "photo_already_exist");
                                                if (photoDB.photo_num.contains("SCREENSHOT") && photoDB.getPhoto_type() == 4)
                                                    new MessageDialogBuilder((Activity) mContext)
                                                            .setStatus(DialogStatus.ALERT)
                                                            .setTitle("Сервер не прийняв фото")
                                                            .setSubTitle("Фото вже є на сервері")
                                                            .setMessage(data.error + ". Ви повинні завантажити нове фото")
                                                            .show();

                                                callback.onSuccess(photoDB, data.error);
                                            } else if (data.errorType.equals("missing_geo_coord")) {
                                                Globals.fixMP(null, null);
                                                Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody/info/missing_geo_coord", "missing_geo_coord");
                                                callback.onFailure(photoDB, "Ошибка при обработке фото: " + data.error);
                                            } else {
                                                Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody/info/other_error", data.error);
                                                callback.onFailure(photoDB, "Ошибка при обработке фото: " + data.error);
                                            }
                                        }
                                    } else {
                                        callback.onFailure(photoDB, "Список list - пустой!");
                                    }
                                } catch (Exception e) {
                                    callback.onFailure(photoDB, "Ошибка при обработке данных: " + e);
                                }
//                                callback.onFailure(photoDB, "Запрос прошел с ошибкой, ошибка: " + response.body());
                            }
                        } else {
                            callback.onFailure(photoDB, "Запрос прошел с ошибкой, ответ с сервера - пустой. Обратитесь к Вашему руководителю.");
                        }
                    } else {
                        callback.onFailure(photoDB, "Запрос прошел с ошибкой, возможно проблема на сервере, повторите попытку позже. code: " + response.code());
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody", "HEREException e: " + e);
                    callback.onFailure(photoDB, "ВНИМАНИЕ! Передайте эту ошибку Вашему руководителю: " + e);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("M_UPLOAD_GALLERY", "HERE IN onFailure: " + t);
                Globals.writeToMLOG("FAILURE", "PhotoReports/buildCall/CALL/onFailure", "t.toString(): " + t.toString());
                callback.onFailure(photoDB, t.toString());
            }
        });
        Globals.writeToMLOG("INFO", "PhotoReports/buildCall/CALL/onResponse/responseBody", "HERE AFTER");


        Log.e("M_UPLOAD_GALLERY", "End");
    }


    private boolean checkManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return false;
        }
    }


    /**
     * 11.06.2021
     * Разбор ответа с сервера.
     *
     * @param response -- Ответ с сервера
     * @param photoDB  -- Строчка с базы данных Фото
     * @param callback -- Что делать по завершению запроса
     */
    private void responseTEST(Response<JsonObject> response, StackPhotoDB photoDB, ExchangeInterface.UploadPhotoReports callback) {

        JsonObject jsonR = response.body();

        Log.e("UPLOAD_PHOTO_R", "response: " + jsonR);
        // todo надо удалить, избыточно в логе
        Globals.writeToMLOG("INFO", "PhotoReports.responseTEST", "response: " + jsonR);

        if (response.isSuccessful() && response.body() != null) {
            try {
                if (jsonR != null) {
                    if (!jsonR.get("state").isJsonNull() && jsonR.get("state").getAsBoolean()) {
                        if (!jsonR.get("move").isJsonNull()) {
                            try {
                                // ОБРАБОТКА УСПЕШНОГО ОТВЕТА С СЕРВЕРА
                                JSONObject j = new JSONObject(jsonR.get("move").toString());
                                Iterator keys = j.keys();
                                Move obj = new Gson().fromJson(jsonR.get("move").getAsJsonObject().get(keys.next().toString()), Move.class);

                                if (obj.getRes().equals("true") || obj.getRes().equals("1")) {
                                    Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/responseTEST", "Success. StackPhotoDB id: " + photoDB.getId());
                                    callback.onSuccess(photoDB, "test text");
                                } else {
                                    // ОШИБКА.
                                    Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/responseTEST", "Err in MOVE response data: " + response.body().toString());
                                    callback.onFailure(photoDB, response.body().toString());
                                }

                            } catch (Exception e) {

                                // ЗАПРОС ПРОШЕЛ С ОШИБКОЙ
                                String msg = Arrays.toString(e.getStackTrace());
                                Globals.writeToMLOG("ERROR", "PhotoReports/upload_photo/responseTEST", "in MOVE Exception e: " + e);
                                callback.onFailure(photoDB, msg + response.body().toString());
                            }
                        } else {
                            Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/responseTEST", "move is NULL");
                            callback.onFailure(photoDB, response.body().toString());
                        }


                    } else if (!jsonR.get("state").isJsonNull() && !jsonR.get("state").getAsBoolean()) {
                        try {
                            if (!jsonR.get("error").isJsonNull() || jsonR.get("error") != null) {
                                String error = jsonR.get("error").getAsString();

                                Globals.writeToMLOG("INFO", "PhotoReports/upload_photo/responseTEST", "response from site. error: " + error);


                                // Такое фото уже было загружено ранее: JPG_20210216_091646_-1173842094.jpg
                                // This photo has already been uploaded earlier. T.P.H.A.B.U.E.
                                String crutch = error.substring(0, 35);

                                if (crutch.equals("Такое фото уже было загружено ранее:")) {

//                                    try {
//                                        photoDB.setUpload_to_server(System.currentTimeMillis());    // todo. Должна быть ошибка с транзакцией
//                                        RealmManager.stackPhotoSavePhoto(photoDB);
//                                    } catch (Exception e) {
//                                        Log.e("UPLOAD_PHOTO_R", "1_T.P.H.A.B.U.E.Exception e: " + e);
//                                        Globals.writeToMLOG("ERROR", "PhotoReports.responseTEST/T.P.H.A.B.U.E.1", "Exception e: " + e);
//                                    }
                                    callback.onSuccess(photoDB, "test text");
                                } else {
//                                    try {
//                                        photoDB.setUpload_to_server(System.currentTimeMillis());    // todo. Должна быть ошибка с транзакцией
//                                        RealmManager.stackPhotoSavePhoto(photoDB);
//                                    } catch (Exception e) {
//                                        Log.e("UPLOAD_PHOTO_R", "2_T.P.H.A.B.U.E.Exception e: " + e);
//                                        Globals.writeToMLOG("ERROR", "PhotoReports.responseTEST/T.P.H.A.B.U.E.2", "Exception e: " + e);
//                                    }

                                    callback.onSuccess(photoDB, "test text");
//                                    callback.onFailure(photoDB, "(Выгрузка фото)Возникла ошибка: " + error + response.body().toString());
                                }


                            } else {
                                callback.onFailure(photoDB, "Фото не выгружено. Сообщите об этом руководителю. Ответ от сервера: " + jsonR);
                            }
                        } catch (Exception e) {
                            callback.onFailure(photoDB, "Фото не выгружено." + e + response.body().toString());
                        }
                    } else {
                        callback.onFailure(photoDB, "Ошибка: " + jsonR + response.body().toString());
                    }
                } else {
                    callback.onFailure(photoDB, "Пустой ответ от сервера: " + response);
                }
            } catch (Exception e) {
                callback.onFailure(photoDB, "Ошибка при выгрузке фото - повторите попытку позже или обратитесь к Вашему руководителю. \nОшибка: " + e + response.body().toString());
            }
        }
    }


}
