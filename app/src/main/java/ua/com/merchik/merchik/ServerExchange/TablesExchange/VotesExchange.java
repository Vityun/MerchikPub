package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.VoteSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.PhotoInfoResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoInformation;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoInformationData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class VotesExchange {

    public void uploadVotes(Clicks.clickObjectAndStatus click) {
        PhotoInformation res = new PhotoInformation();
        List<PhotoInformationData> data = new ArrayList<>();

        List<VoteSDB> votes = SQL_DB.votesDao().getAllToUpload();

        res.mod = "images_view";
        res.act = "set_score";
        for (VoteSDB item : votes) {
            PhotoInformationData info = new PhotoInformationData();
            info.element_id = String.valueOf(item.id);
            info.id = String.valueOf(item.photoId);
            info.score = String.valueOf(item.score);
            info.vote_class = item.voteClass;

            info.dt = item.dt;
            info.isp = item.isp;
            info.theme_id = item.themeId;
            info.code_dad2 = item.codeDad2;
            info.addr_id = item.addrId;
            info.kli = item.kli;
            info.merchik = item.merchik;

            data.add(info);
        }
        res.data = data;

        String json = new Gson().toJson(res);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<PhotoInfoResponse> call = RetrofitBuilder.getRetrofitInterface().SEND_PHOTO_INFO(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new retrofit2.Callback<PhotoInfoResponse>() {
            @Override
            public void onResponse(retrofit2.Call<PhotoInfoResponse> call, retrofit2.Response<PhotoInfoResponse> response) {
                if (response.isSuccessful()) {
                    Log.e("uploadVotes", "response: " + response);
                    Log.e("uploadVotes", "response.body(): " + response.body());

                    Globals.writeToMLOG("INFO", "uploadVotes.onResponse", "response: " + response);

                    if (response.body() != null) {
                        Globals.writeToMLOG("INFO", "uploadVotes.onResponse", "response.body(): " + response.body());
                        if (response.body().list != null && response.body().list.size() > 0) {
                            Globals.writeToMLOG("INFO", "uploadVotes.onResponse", "response.body().list.size(): " + response.body().list.size());

                            for (VoteSDB item : votes) {
                                item.dtUpload = System.currentTimeMillis() / 1000;
                            }
                            SQL_DB.votesDao().insertAllCompletable(votes).subscribeOn(Schedulers.io())
                                    .subscribe(new DisposableCompletableObserver() {
                                        @Override
                                        public void onComplete() {
                                            Globals.writeToMLOG("OK", "uploadVotes.onResponse/onComplete", "OK");
                                        }

                                        @Override
                                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                            Globals.writeToMLOG("ERROR", "uploadVotes.onResponse/onError", "Throwable e: " + e);
                                        }
                                    });


                            click.onSuccess(response.body().list);
                        }
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<PhotoInfoResponse> call, Throwable t) {
                Log.e("uploadVotes", "t:" + t);
                Globals.writeToMLOG("INFO", "uploadVotes.onFailure", "Throwable t: " + t);
            }
        });
    }

}
