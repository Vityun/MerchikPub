package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RetrofitResponse.SamplePhotoResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class SamplePhotoExchange {

    public SynchronizationTimetableDB synchronizationTimetableDB;

    public void downloadSamplePhotoTable(Clicks.clickObjectAndStatus click) {

        StandartData data = new StandartData();
        data.mod = "images_view";
        data.act = "list_example";

        synchronizationTimetableDB = RealmManager.getSynchronizationTimetableRowByTable("sample_photo");
        if (synchronizationTimetableDB != null) {
            long time = System.currentTimeMillis() / 1000 - synchronizationTimetableDB.getVpi_app();
            String dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());
            if (dt_change_from.equals("0")) {
                data.dt_change_from = "0";
            } else {
                if (synchronizationTimetableDB.getUpdate_frequency() < time) {
                    return;
                } else {
                    data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());
                }

            }
            Globals.writeToMLOG("INFO", "SamplePhotoExchange/downloadSamplePhotoTable", "synchronizationTimetableDB != null && data.dt_change_from: " + data.dt_change_from);
        } else {
            data.dt_change_from = "0";
            Globals.writeToMLOG("INFO", "SamplePhotoExchange/downloadSamplePhotoTable", "synchronizationTimetableDB == null");
        }

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<SamplePhotoResponse> call = RetrofitBuilder.getRetrofitInterface().GET_SAMPLE_PHOTO(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<SamplePhotoResponse>() {
            @Override
            public void onResponse(Call<SamplePhotoResponse> call, Response<SamplePhotoResponse> response) {
                Log.e("test", "test" + response);
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().state) {
                                if (response.body().list != null && response.body().list.size() > 0) {
                                    SQL_DB.samplePhotoDao().insertAll(response.body().list)
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new DisposableCompletableObserver() {
                                                @Override
                                                public void onComplete() {
                                                    Log.d("test", "test");
                                                    Globals.writeToMLOG("INFO", "SamplePhotoExchange/downloadSamplePhotoTable/onResponse/onComplete", "OK: " + response.body().list.size());
//                                                    click.onSuccess(response.body().list);
                                                }

                                                @Override
                                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                                    Log.d("test", "test");
                                                    Globals.writeToMLOG("ERROR", "SamplePhotoExchange/downloadSamplePhotoTable/onResponse/onError", "Throwable e: " + e);
                                                    click.onFailure("onError SQL_DB.potentialClientDao().insertAll Throwable e: " + e);
                                                }
                                            });

                                    click.onSuccess(response.body().list);
                                }

                            } else {
                                click.onFailure("Ошибка запроса. State=false");
                            }
                        } else {
                            click.onFailure("Ошибка запроса. Тело пришло пустым.");
                        }
                    } else {
                        click.onFailure("Ошибка запроса. Код: " + response.code());
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "SamplePhotoExchange/downloadSamplePhotoTable/onResponse", "Exception e: " + e);
                    click.onFailure("Ошибка запроса. Exception e: " + e);
                }
            }

            @Override
            public void onFailure(Call<SamplePhotoResponse> call, Throwable t) {
                Log.e("test", "test" + t);
                Globals.writeToMLOG("ERROR", "SamplePhotoExchange/downloadSamplePhotoTable/onFailure", "Throwable t: " + t);
                click.onFailure("Ошибка запроса. Throwable t: " + t);
            }
        });
    }

    public void downloadSamplePhotos(List<SamplePhotoSDB> list, Clicks.clickStatusMsg click) {
        List<Integer> dataList = new ArrayList<>();
        for (SamplePhotoSDB item : list) {
            dataList.add(item.photoId);
        }

        // Проверяем какие фотки у нас УЖЕ есть
        List<StackPhotoDB> stack = StackPhotoRealm.getByServerIds(dataList);

        for (StackPhotoDB item : stack) {
            dataList.remove(item.getPhotoServerId());
        }


        new PhotoDownload().downloadPhotoByIds("/Sample", "SAMPLE_", dataList, new Clicks.clickStatusMsg() {
            @Override
            public void onSuccess(String data) {
                Globals.writeToMLOG("INFO", "downloadSamplePhotos", "data: " + data);
                click.onSuccess(data);
            }

            @Override
            public void onFailure(String error) {
                Globals.writeToMLOG("ERROR", "downloadSamplePhotos", "data: " + error);
                click.onFailure(error);
            }
        });
    }
}
