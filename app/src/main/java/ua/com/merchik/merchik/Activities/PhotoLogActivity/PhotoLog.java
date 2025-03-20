package ua.com.merchik.merchik.Activities.PhotoLogActivity;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.SearchView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.UploadPhotoData.ImagesPrepareUploadPhoto;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.retrofit.MyCookieJar;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class PhotoLog {

    private Dialog dialog;
    public Context mContext;

    /**
     * Отображение Диалогового окна с фотками
     */
    public void viewPhotoLog(Context context) {
        try {
            dialog = new Dialog(context);
            dialog.setContentView(R.layout.alertdialog_photo_log);
            dialog.setTitle("Журнал фото");
            dialog.setCancelable(true);

            SearchView searchView = (SearchView) dialog.findViewById(R.id.searchViewPhotoLog);

            RecyclerView recyclerView = (RecyclerView) dialog.findViewById(R.id.recyclerViewPhotoLog);
            PhotoLogAdapter recycleViewPLAdapter = new PhotoLogAdapter(context, RealmManager.getStackPhoto(), 0, 0, false, null, null);
            recyclerView.setAdapter(recycleViewPLAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String text) {
                    Log.e("TAG_SEARCH", "SEARCH(0): " + text);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String text) {
                    Log.e("TAG_SEARCH", "SEARCH(1): " + text);
                    recycleViewPLAdapter.getFilter().filter(text);
                    return true;
                }
            });

            Button button_update = (Button) dialog.findViewById(R.id.buttonPhotoLogClose);
            button_update.setOnClickListener(v -> dialog.cancel());
            dialog.show();
        } catch (Exception e) {
            DialogData dialog = new DialogData(context);
            dialog.setTitle("Ошибка");
            dialog.setText("Журнал фото: " + e);
            dialog.setClose(dialog::dismiss);
            dialog.show();
        }
    }


    /**
     * 14.08.2020
     * <p>
     * upload photo to server
     * <p>
     * 06.05.2024
     */
    public void sendPhotoOnServer(Context context, StackPhotoDB photoDB, ExchangeInterface.UploadPhotoReports callback) {
        new MyCookieJar();
        Globals globals = new Globals();

        final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");

        int photoId = photoDB.getId();
        String mod = "images_prepare";
        String act = "upload_photo";

        String client_id = "";
        String addr_id = "";
        String date = "";
        String img_type_id = "";
        String tovar_id = "";
        String photo_user_id = "";
        String client_tovar_group = "";
        String doc_num = "";
        String theme_id = "";
        String comment = "";
        String code_dad2 = "";
        String dvi = "";
        String gp = "";

        String img_src_id = "0";
        String showcase_id = "0";
        String planogram_id = "0";
        String planogram_img_id = "0";

        String example_id = "";
        String example_img_id = "";

        if (photoDB.getClient_id() != null) {
            client_id = String.valueOf(photoDB.getClient_id());
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
            photo_user_id = String.valueOf(photoDB.getPhoto_user_id());
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

        try {
            code_dad2 = String.valueOf(photoDB.getCode_dad2());
        } catch (Exception e) {
            // Запись ошибки
            code_dad2 = "";
        }


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


        //pass it like this
        File file = new File(photoDB.getPhoto_num());

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part photo =
                MultipartBody.Part.createFormData("photos[]", file.getName(), RequestBody.create(MEDIA_TYPE_JPG, file));


        Log.e("TAG_SEND_PHOTO", "Data: \n"
                + "\n mod:" + mod
                + "\n act:" + act
                + "\n client_id:" + client_id
                + "\n addr_id:" + addr_id
                + "\n date:" + date
                + "\n img_type_id:" + img_type_id
                + "\n photo_user_id:" + photo_user_id
                + "\n client_tovar_group:" + client_tovar_group
                + "\n doc_num:" + doc_num
                + "\n theme_id:" + theme_id
                + "\n comment:" + comment
                + "\n code_dad2:" + code_dad2
                + "\n gp:" + null
                + "\n photo:" + photo);

        String data = "" + "mod:" + mod + " act:" + act + " client_id:" + client_id + " addr_id:" + addr_id + " date:" + date + " img_type_id:" + img_type_id + " photo_user_id:" + photo_user_id + " client_tovar_group:" + client_tovar_group + " doc_num:" + doc_num + " theme_id:" + theme_id + " comment:" + comment + " code_dad2:" + code_dad2 + " gp:" + gp + " photo:" + file.toString();
        String logMsg1 = "(Журнал фото) Данные фото: " + data;
//        RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, logMsg1 , 1088, null, null, null, null, null, Globals.session, null)));


        String info = " UPLOAD.PHOTO.PHOTOLOG.PHOTODATA:  photoId: " + photoId;
        globals.writeToMLOG(Clock.getHumanTime() + info + data + "\n");

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface()
                .SEND_PHOTO_2_BODY(mod2, act2, client_id2, addr_id2, date2, img_type_id2, photo_user_id2, client_tovar_group2, doc_num2, theme_id2, comment2, dvi2, codeDad2, gp2, tov2, img_src_id2, showcase_id2, planogram_id2, planogram_img_id2,
                        example_id2, example_img_id2,
                        photo);

        try {

            call.enqueue(new retrofit2.Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Globals.writeToMLOG("INFO", "Long/PhotoReports/buildCall/CALL/onResponse/responseBody", "HERE IN");
                    Globals.writeToMLOG("INFO", "Long/PhotoReports/buildCall/CALL/onResponse/responseBody", "HERE IN call: " + call);
                    Globals.writeToMLOG("INFO", "Long/PhotoReports/buildCall/CALL/onResponse/responseBody", "HERE IN response: " + response);
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                Globals.writeToMLOG("INFO", "Long/PhotoReports/buildCall/CALL/onResponse/responseBody", "" + response.body());
                                ImagesPrepareUploadPhoto info = new Gson().fromJson(new Gson().toJson(response.body()), ImagesPrepareUploadPhoto.class);
                                Globals.writeToMLOG("INFO", "Long/PhotoReports/buildCall/CALL/onResponse/responseBody/info", "" + new Gson().toJson(info));
                                if (info.state) {
                                    ImagesPrepareUploadPhoto.DataList data = info.list.get(0);
                                    if (data.state) {
                                        callback.onSuccess(photoDB, "test");
                                    } else {
                                        if (data.errorType.equals("photo_already_exist")) {
                                            Globals.writeToMLOG("INFO", "Long/PhotoReports/buildCall/CALL/onResponse/responseBody/info/photo_already_exist", "photo_already_exist");
                                            callback.onSuccess(photoDB, "Фото уже было загружено");
                                        } else {
                                            callback.onFailure(photoDB, "Ошибка при обработке фото: " + data.error);
                                        }
                                    }
                                } else {
                                    try {
                                        if (info.list != null && info.list.size() > 0) {
                                            ImagesPrepareUploadPhoto.DataList data = info.list.get(0);
                                            Globals.writeToMLOG("INFO", "Long/PhotoReports/buildCall/CALL/onResponse/responseBody/info/data", "" + new Gson().toJson(data));
                                            if (data.state) {
                                                callback.onSuccess(photoDB, "При выгрузке фото произошла ошибка1: " + data.error);
                                            } else {
                                                if (data.errorType.equals("photo_already_exist")) {
                                                    Globals.writeToMLOG("INFO", "Long/PhotoReports/buildCall/CALL/onResponse/responseBody/info/photo_already_exist", "photo_already_exist");
                                                    callback.onSuccess(photoDB, "Фото уже было загружено");
                                                } else {
                                                    callback.onFailure(photoDB, "Ошибка при обработке фото: " + data.error);
                                                }
                                            }
                                        } else {
                                            callback.onFailure(photoDB, "Список list - пустой!");
                                        }
                                    } catch (Exception e) {
                                        callback.onFailure(photoDB, "Ошибка при обработке данных: " + e);
                                    }
                                }
                            } else {
                                callback.onFailure(photoDB, "Запрос прошел с ошибкой, ответ с сервера - пустой. Обратитесь к Вашему руководителю.");
                            }
                        } else {
                            callback.onFailure(photoDB, "Запрос прошел с ошибкой, возможно проблема на сервере, повторите попытку позже. code: " + response.code());
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("INFO", "Long/PhotoReports/buildCall/CALL/onResponse/responseBody", "HEREException e: " + e);
                        callback.onFailure(photoDB, "ВНИМАНИЕ! Передайте эту ошибку Вашему руководителю: " + e);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e("M_UPLOAD_GALLERY", "HERE IN onFailure: " + t);
                    Globals.writeToMLOG("FAILURE", "Long/PhotoReports/buildCall/CALL/onFailure", "t.toString(): " + t.toString());
                    callback.onFailure(photoDB, t.toString());
                }
            });


            /*call.enqueue(new retrofit2.Callback<JsonObject>() {
                @Override
                public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                    globals.writeToMLOG(Clock.getHumanTime() + "PHOTOLOG.onResponse.Успешный ответ: id фото выгрузки: " + photoId + " Ответ с сервера: " + response.body() + "\n");


                    Log.e("TAG_REALM_LOG", "SUCCESS: " + response.body());

                    JsonObject jsonR = response.body();
                    Log.e("TAG_SEND_PHOTO", "RESPONSE: " + response.body());
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            if (jsonR != null) {
                                if (!jsonR.get("state").isJsonNull() && jsonR.get("state").getAsBoolean()) {
                                    if (!jsonR.get("move").isJsonNull()) {
                                        try {
                                            Log.e("TAG_REALM_LOG", "ФОТО ВЫГРУЖЕНО с ID: " + photoDB.getId());

                                            JSONObject j = new JSONObject(jsonR.get("move").toString());
                                            Iterator keys = j.keys();
                                            Move obj = new Gson().fromJson(jsonR.get("move").getAsJsonObject().get(keys.next().toString()), Move.class);

                                            Log.e("photoUploadToServer", "jsonR.get(\"move\"): " + jsonR.get("move"));
                                            Log.e("photoUploadToServer", "obj.getRes(): " + obj.getRes());

                                            if (obj.getRes().equals("true") || obj.getRes().equals("1")) {
                                                RealmManager.INSTANCE.executeTransaction(realm -> {
                                                    photoDB.setUpload_to_server(System.currentTimeMillis());
                                                    RealmManager.INSTANCE.copyToRealmOrUpdate(photoDB);
                                                });
                                                Toast.makeText(context, "Фото " + photoId + " выгружено на сервер.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                globals.alertDialogMsg(context, "(Выгрузка фото)Возникла ошибка. Ответ от сервера: " + response.body().toString());
                                            }

                                        } catch (Exception e) {
                                            String msg = Arrays.toString(e.getStackTrace());
                                            globals.alertDialogMsg(context, "(Выгрузка фото)Возникла ошибка. Ответ от сервера: " + msg + response.body().toString());
                                        }
                                    } else {
                                        globals.alertDialogMsg(context, "(Выгрузка фото)Возникла ошибка. Ответ от сервера: " + response.body().toString());
                                    }


//                                    try {
//                                        RealmManager.INSTANCE.executeTransaction(realm -> photoDB.setUpload_to_server(System.currentTimeMillis()));
//                                        RealmManager.stackPhotoSavePhoto(photoDB);
//
//                                        Toast.makeText(context, "Фото " + photoId + " выгружено на сервер.", Toast.LENGTH_LONG).show();
//                                    }catch (Exception e){
//                                        String msg = Arrays.toString(e.getStackTrace());
//                                        globals.alertDialogMsg(context, msg);
//                                    }
                                } else if (!jsonR.get("state").isJsonNull() && !jsonR.get("state").getAsBoolean()) {
                                    try {
                                        if (!jsonR.get("error").isJsonNull() || jsonR.get("error") != null) {
                                            String error = jsonR.get("error").getAsString();
                                            globals.alertDialogMsg(context, "(Выгрузка фото)Возникла ошибка: " + error);
                                        } else {
                                            globals.alertDialogMsg(context, "Фото не выгружено. Сообщите об этом руководителю. Ответ от сервера: " + "NULL");
                                        }
                                    } catch (Exception e) {
                                        // error с ошибкой
                                        globals.alertDialogMsg(context, "При выгрузке произошла ошибка: " + e + ". Попробуйте перелогиниться и повторить попытку. Если ошибка будет повторяться - обратитесь к Вашему администатору");
                                    }
                                    globals.alertDialogMsg(context, "При выгрузке произошла ошибка. Попробуйте перелогиниться и повторить попытку. Если ошибка будет повторяться - обратитесь к Вашему администатору");
                                } else {
                                    globals.alertDialogMsg(context, "Ошибка: " + jsonR);//Toast toast = Toast.makeText(this, "Данные сохранить не получилось, повторите попытку", Toast.LENGTH_SHORT);toast.show();
                                }
                            } else {
                                globals.alertDialogMsg(context, "Не удалось получить ответ от сервера. Скорее всего отсутствует интернет. Проверьте связь и повторите попытку или обратитесь к Ващему руководителю.");//Toast toast = Toast.makeText(this, "Не удалось получить ответ от сервера. Скорее всего отсутствует интернет. Проверьте связь и повторите попытку или обратитесь к Ващему руководителю.", Toast.LENGTH_SHORT);toast.show();
                            }
                        } catch (Exception e) {
//                            RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "(Журнал фото) Ошибка при разборе ответа с сервера: " + e, 1088, null, null, null, null, null, Globals.session, null)));
                            globals.alertDialogMsg(context, "Ошибка при выгрузке фото - повторите попытку позже или обратитесь к Вашему руководителю. \nОшибка: " + e);
                            Log.e("TAG_SEND_PHOTO", "ERROR: " + e);
                        }
                    } else {
                        globals.alertDialogMsg(context, "Запрос прошел НЕ УДАЧНО. Повторите попытку позже. Ошибка: " + response.errorBody() + "\n\n\n" + response.toString());
                        globals.alertDialogMsg(context, "Запрос прошел НЕ УДАЧНО. Повторите попытку позже. Ошибка: " + response.body());
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {

                    globals.writeToMLOG(Clock.getHumanTime() + "PHOTOLOG.onFailure.НЕ Успешный ответ: id фото выгрузки: " + photoId + " Код ошибки: " + t.toString() + "\n");


                    Log.e("TAG_REALM_LOG", "FAILURE");
                    Log.e("TAG_SEND_PHOTO", "FAILURE: " + t.getMessage());

                    try {
                        Log.e("TAG_REALM_LOG", "ЗАПИСЬ 5");
//                        RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "(Журнал фото) Ошибка при выгрузке фото(FAILURE): " + t, 1088, null, null, null, null, null, Globals.session, null)));
                    } catch (Exception e) {
                        Log.e("TAG_REALM_LOG", "Ошибка(5): " + e);
                    }

                    Log.e("TAG_SEND_PHOTO", "FAILURE: " + t.getMessage());
                }
            });*/
        } catch (Exception e) {
//            RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "(Журнал фото) Ошибка при выгрузке фото: " + e, 1088, null, null, null, null, null, Globals.session, null)));
        }


    }


    public void sendPhotoOnServer2(Context context, StackPhotoDB photoDB) {

    }


}
