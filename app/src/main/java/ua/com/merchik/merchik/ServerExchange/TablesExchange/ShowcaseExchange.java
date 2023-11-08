package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ShowcaseResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class ShowcaseExchange {

    public void downloadShowcaseTable(ExchangeInterface.ExchangeResponseInterface exchange) {
        try {
            StandartData data = new StandartData();
            data.mod = "rack";
            data.act = "list";
            data.active_only = "1";

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
                        Globals.writeToMLOG("INFO", "downloadShowcaseTable/onResponse/isSuccessful", "isSuccessful");
                        if (response.body() != null && response.body().state && response.body().list != null && response.body().list.size() > 0) {
                            Globals.writeToMLOG("INFO", "downloadShowcaseTable/onResponse/isSuccessful", "response.body().list.size(): " + response.body().list.size());
                            List<ShowcaseSDB> serv = response.body().list;
                            List<ShowcaseSDB> db = SQL_DB.showcaseDao().getAll();

                            List<ShowcaseSDB> res = new ArrayList<>();
                            Set<Integer> dbIds = new HashSet<>();

                            // Заполняем HashSet dbIds числовыми идентификаторами из списка db
                            for (ShowcaseSDB itemDB : db) {
                                dbIds.add(itemDB.id); // Здесь предполагается, что у класса есть метод getId()
                            }

                            // Выполняем сравнение
                            for (ShowcaseSDB itemServ : serv) {
                                if (!dbIds.contains(itemServ.id)) {
                                    res.add(itemServ);
                                }
                            }

                            exchange.onSuccess(res);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ShowcaseResponse> call, Throwable t) {
                    Log.e("checkRequest", "Throwable t: " + t);
                    Globals.writeToMLOG("ERR", "downloadShowcaseTable/onFailure", "Throwable t: " + t);
                }
            });

        } catch (Exception e) {
            Globals.writeToMLOG("ERR", "downloadShowcaseTable/catch", "Exception e: " + e);
//            exchange.onFailure("Ошибка при обновлении Сотрудников. Передайте код ошибки Вашему руководителю. Код ошибки: " + e);
        }
    }

    public void downloadShowcasePhoto(List<ShowcaseSDB> data) {
        for (ShowcaseSDB item : data) {
            Log.e("checkRequest", "downloadShowcasePhoto/item: " + item.id);
            PhotoDownload.downloadPhoto(item.photoBig, new ExchangeInterface.ExchangePhoto() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    try {
                        long dt = System.currentTimeMillis() / 1000;

                        StackPhotoDB photoDB = new StackPhotoDB();
                        photoDB.setId(RealmManager.stackPhotoGetLastId() + 1);
                        photoDB.setPhotoServerId(String.valueOf(item.photoId));
                        photoDB.setDt(dt);
                        photoDB.setClient_id(item.clientId);
                        photoDB.setAddr_id(Integer.valueOf(item.addrId));
                        photoDB.setUser_id(null);
                        photoDB.setPhoto_type(0);

                        photoDB.setCreate_time(dt * 1000);// реквизиты что б фотки не выгружались обратно на сервер
                        photoDB.setUpload_to_server(dt);// реквизиты что б фотки не выгружались обратно на сервер
                        photoDB.setGet_on_server(dt);// реквизиты что б фотки не выгружались обратно на сервер

                        photoDB.setPhoto_num(Globals.savePhotoToPhoneMemory("/Showcase", "" + item.id, bitmap));

                        photoDB.setPhotoServerURL(item.photoBig);

                        photoDB.img_src_id = String.valueOf(item.photoId);
                        photoDB.showcase_id = String.valueOf(item.id);
                        photoDB.planogram_id = "";  // Почему нет планограммы?
                        photoDB.planogram_img_id = String.valueOf(item.photoPlanogramId);


                        RealmManager.stackPhotoSavePhoto(photoDB);

                        Log.e("checkRequest", "downloadShowcasePhoto/photoDB: " + photoDB.getId());
                        Globals.writeToMLOG("INFO", "savePhotoToDB2/downloadPhoto/ShowcaseSDB/photoDB", "photoDB.getId(): " + photoDB.getId());
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERR", "savePhotoToDB2/downloadPhoto/ShowcaseSDB", "Exception e: " + e);
                        Log.e("checkRequest", "downloadShowcasePhoto/Exception e: " + e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e("checkRequest", "downloadShowcasePhoto/String error: " + error);
                    Globals.writeToMLOG("ERR", "savePhotoToDB2/downloadPhoto/ShowcaseSDB/onFailure", "error: " + error);
                }
            });
        }
    }

    public List<ShowcaseSDB> getSamplePhotosToDownload(){
        List<Integer> list = SQL_DB.showcaseDao().getAllPhotosIds();
        List<StackPhotoDB> stack = StackPhotoRealm.getByServerIds(list);
        for (StackPhotoDB item : stack) {
            list.remove(item.getPhotoServerId());
        }
        List<ShowcaseSDB> res = SQL_DB.showcaseDao().getAllByPhotosIds(list);
        return res;
    }

}
