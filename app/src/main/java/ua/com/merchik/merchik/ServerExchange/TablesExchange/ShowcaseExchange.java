package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.realm.RealmManager.getAllWorkPlan;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ShowcaseResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class ShowcaseExchange {

    public void downloadShowcaseTable(ExchangeInterface.ExchangeResponseInterface exchange) {
        try {
//            List<Integer> addr_id = new ArrayList<>();
//            addr_id.add(27710);
            List<WpDataDB> wpDataDBList = getWorkPlanList();
            List<ShowcaseSDB> showcaseSDBSList = SQL_DB.showcaseDao().getAll();

            // Используем Set для автоматического удаления дубликатов
            Set<String> uniqueClientIds = new HashSet<>();
            Set<String> uniqueAdressId = new HashSet<>();
            Set<String> uniqueShowcaseId = new HashSet<>();

            // Проходим по каждому элементу списка wpDataDBList
            if (wpDataDBList.isEmpty())
                return;

            for (WpDataDB wpDataDB : wpDataDBList) {
                // Добавляем client_id в Set (дубликаты игнорируются)
                uniqueClientIds.add(wpDataDB.getClient_id());
                uniqueAdressId.add(String.valueOf(wpDataDB.getAddr_id()));
            }

            if (!showcaseSDBSList.isEmpty())
                for (ShowcaseSDB sdb : showcaseSDBSList){
                    uniqueShowcaseId.add(sdb.selectId);
                }
            uniqueShowcaseId.remove("444");
            uniqueShowcaseId.remove("1028");


            StandartData data = new StandartData();
            data.mod = "rack";
            data.act = "list";
            data.active_only = "1";
            data.client_id = new ArrayList<>(uniqueClientIds);
            data.addr_id = new ArrayList<>(uniqueAdressId);
            data.id_exclude = new ArrayList<>(uniqueShowcaseId);

                    // #### TODO
//            SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.INSTANCE.copyFromRealm(RealmManager.getSynchronizationTimetableRowByTable("photo_showcase"));
//            data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());

                    // добавить время отправки,что бы не передавать лишнее

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
                        if (response.body() != null && response.body().state != null && response.body().state && response.body().list != null && response.body().list.size() > 0) {
                            Globals.writeToMLOG("INFO", "downloadShowcaseTable/onResponse/isSuccessful", "response.body().list.size(): " + response.body().list.size());
                            List<ShowcaseSDB> serv = response.body().list;
                            exchange.onSuccess(serv);

//                            List<ShowcaseSDB> filteredServ = serv.stream()
//                                    .filter(showcase -> wpDataDBList.stream()
//                                            .anyMatch(wpData ->
//                                                    Objects.equals(wpData.getClient_id(), showcase.clientId) &&
//                                                            String.valueOf(wpData.getAddr_id()).equals(showcase.addrId)
//                                            )
//                                    )
//                                    .collect(Collectors.toList());


//                            List<ShowcaseSDB> db = SQL_DB.showcaseDao().getAll();
//
//                            List<ShowcaseSDB> res = new ArrayList<>();
//                            Set<Integer> dbIds = new HashSet<>();
//
//                            // Заполняем HashSet dbIds числовыми идентификаторами из списка db
//                            for (ShowcaseSDB itemDB : db) {
//                                dbIds.add(itemDB.id); // Здесь предполагается, что у класса есть метод getId()
//                            }

                            // 11.02.2025 были случае когда лезли 10к+ витрин, теперь фильтрую по клиентам и адресам из впдаты. Зачем нам левые фото витрины даже для менеджера? Для этого есть сайт
//                            for (ShowcaseSDB itemServ : serv) {
//                                if (!dbIds.contains(itemServ.id)) {
//                                    res.add(itemServ);
//                                }
//                            }

//                            if (!serv.isEmpty())
//                                RealmManager.INSTANCE.executeTransaction(realm -> {
//                                    synchronizationTimetableDB.setVpi_app(System.currentTimeMillis() / 1000);
//                                    realm.copyToRealmOrUpdate(synchronizationTimetableDB);
//                                });
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
        PhotoDownload photoDownload = new PhotoDownload();
        for (ShowcaseSDB item : data) {
//            if (item.photoId == 51528478 ||
//                    item.photoId == 52657208 ||
//                    item.photoId == 52657201 ||
//                    item.photoId == 52657328 ||
//                    item.photoId == 52657480 ||
//                    item.photoId == 52657384 ||
//                    item.photoId == 52657669
//            ) {

            StackPhotoDB stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId2(String.valueOf(item.photoId));
            if (stackPhotoDB == null) {
                Log.e("checkRequest", "downloadShowcasePhoto/item: " + item.id);
                photoDownload.downloadPhoto(item.photoBig, new ExchangeInterface.ExchangePhoto() {
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

//                        photoDB.setPhoto_num(Globals.savePhotoToPhoneMemory("/Showcase", "" + item.id, bitmap));

                            photoDB.setPhotoServerURL(item.photoBig);


                            photoDB.img_src_id = String.valueOf(item.photoId);
                            photoDB.showcase_id = String.valueOf(item.id);
                            photoDB.planogram_id = Objects.toString(item.planogramId, "");
                            photoDB.planogram_img_id = String.valueOf(item.photoPlanogramId);

                            photoDownload.savePhotoAndUpdateStackPhotoDB("/Showcase", "" + item.photoId, bitmap, photoDB);

//                        RealmManager.stackPhotoSavePhoto(photoDB);

                            Log.e("checkRequest", "downloadShowcasePhoto/photoDB: " + photoDB.getId());
                            Globals.writeToMLOG("INFO", "savePhotoToDB2/downloadPhoto/ShowcaseSDB/photoDB", "photoDB.getId(): " + photoDB.getId());
                        } catch (Exception e) {
                            Log.e("checkRequest", "downloadShowcasePhoto/Exception e: " + e);
                            Globals.writeToMLOG("ERR", "savePhotoToDB2/downloadPhoto/ShowcaseSDB", "Exception e: " + e);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("checkRequest", "downloadShowcasePhoto/String error: " + error);
                        Globals.writeToMLOG("ERR", "savePhotoToDB2/downloadPhoto/ShowcaseSDB/onFailure", "error: " + error);
                    }
                });
            } else if (stackPhotoDB.getPhoto_num() == null) {
                photoDownload.downloadPhoto(item.photoBig, new ExchangeInterface.ExchangePhoto() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        try {
                            long dt = System.currentTimeMillis() / 1000;

                            stackPhotoDB.setDt(dt);

                            stackPhotoDB.setCreate_time(dt * 1000);// реквизиты что б фотки не выгружались обратно на сервер
                            stackPhotoDB.setUpload_to_server(dt);// реквизиты что б фотки не выгружались обратно на сервер
                            stackPhotoDB.setGet_on_server(dt);// реквизиты что б фотки не выгружались обратно на сервер

//                                stackPhotoDB.img_src_id = String.valueOf(item.photoId);
//                                stackPhotoDB.showcase_id = String.valueOf(item.id);
//                                stackPhotoDB.planogram_id = Objects.toString(item.planogramId, "");
//                                stackPhotoDB.planogram_img_id = String.valueOf(item.photoPlanogramId);

                            photoDownload.savePhotoAndUpdateStackPhotoDB("/Showcase", "" + item.photoId, bitmap, stackPhotoDB);

                            Log.e("checkRequest", "downloadShowcasePhoto/photoDB: " + stackPhotoDB.getId());
                            Globals.writeToMLOG("INFO", "savePhotoToDB2/downloadPhoto/ShowcaseSDB/photoDB", "photoDB.getId(): " + stackPhotoDB.getId());
                        } catch (Exception e) {
                            Log.e("checkRequest", "downloadShowcasePhoto/Exception e: " + e);
                            Globals.writeToMLOG("ERR", "savePhotoToDB2/downloadPhoto/ShowcaseSDB", "Exception e: " + e);
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
    }

    public List<ShowcaseSDB> getSamplePhotosToDownload() {
        List<Integer> list = SQL_DB.showcaseDao().getAllPhotosIds();
        List<StackPhotoDB> stack = StackPhotoRealm.getByServerIds(list);
        for (StackPhotoDB item : stack) {
            list.remove(item.getPhotoServerId());
        }
        List<ShowcaseSDB> res = SQL_DB.showcaseDao().getAllByPhotosIds(list);
        return res;
    }

    private List<WpDataDB> getWorkPlanList() {
        RealmResults<WpDataDB> realmResults = getAllWorkPlan(); // Получаем RealmResults
        return realmResults != null ? new ArrayList<>(realmResults) : new ArrayList<>(); // Преобразуем в List
    }

}
