package ua.com.merchik.merchik.ServerExchange.download;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.feature.SyncCallable;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.WpDataServer;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class MainExchange {

    private String timeTomorrow = Clock.getDatePeriod(5);


    public void downloadWPData(SyncCallable click, long vpi) {

        try {
            StandartData data = new StandartData();
            data.mod = "plan";
            data.act = "list";
            data.date_from =  Clock.getDatePeriod(-21);
            data.date_to = timeTomorrow;
            data.dt_change_from = String.valueOf(vpi);
            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
            Globals.writeToMLOG("INFO", "MainExchange/downloadWPData", "convertedObject: " + convertedObject);
            retrofit2.Call<WpDataServer> call = RetrofitBuilder.getRetrofitInterface().GET_WPDATA_VPI(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<>() {
                @Override
                public void onResponse(retrofit2.Call<WpDataServer> call, retrofit2.Response<WpDataServer> response) {
                    if (response.isSuccessful() && response.body() != null) {

                        if (response.body().getState() && response.body().getList() != null
                                && !response.body().getList().isEmpty()) {
                            List<WpDataDB> wpDataDBList = response.body().getList();
                            Globals.writeToMLOG("INFO", "MainExchange/downloadWPData/onResponse", "wpDataDBList.size(): " + wpDataDBList.size());
                            RealmManager.updateWorkPlanFromServer(wpDataDBList);
                            click.onSuccess(wpDataDBList.size());

                        } else
                            click.onSuccess(0);
                    } else
                        click.onFailure("response error, headers: " + response.headers());
                }

                @Override
                public void onFailure(retrofit2.Call<WpDataServer> call, Throwable t) {

                    click.onFailure(t.getMessage());
                }
            });
        } catch (Exception e) {
            click.onFailure(e.getMessage());
        }

        Log.e("SERVER_REALM_DB_UPDATE", "===================================downloadWPData_END");
    }

}
