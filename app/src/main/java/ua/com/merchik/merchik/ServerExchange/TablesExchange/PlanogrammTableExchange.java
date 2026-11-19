package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;


public class PlanogrammTableExchange {

    public void planogramDownload(Clicks.clickObjectAndStatus click) {
        CatalogDataExchange.planograms(click);
    }

    public void planogrammAddressDownload(Clicks.clickObjectAndStatus click) {
        CatalogDataExchange.addresses(click);
    }

    public void planogrammGroupDownload(Clicks.clickObjectAndStatus click) {
        CatalogDataExchange.groups(click);
    }

    public void planogrammImagesDownload(Clicks.clickObjectAndStatus click) {
        CatalogDataExchange.images(click);
    }

    public void planorgammType() {
        CatalogDataExchange.types(null);
    }

    public void planogramTypeList(Clicks.clickObjectAndStatus click) {
        CatalogDataExchange.types(click);
    }

    public void planogrammVisitShowcase() {
        CatalogDataExchange.visits(null);
    }


    // выгрузка таблицы визитов планоргам
    public void planogrammVisitShowcaseUploadData() {

        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "vizit_showcase_save";
//        data.nolimit = "1";

//        List<PlanogrammVizitShowcaseSDB> planograms = SQL_DB.planogrammVizitShowcaseDao().getAllUploadedPlanograms();
        List<PlanogrammVizitShowcaseSDB> planogrammVizitShowcaseSDBList = SQL_DB.planogrammVizitShowcaseDao().getAllUploadedPlanograms();

        if (planogrammVizitShowcaseSDBList == null || planogrammVizitShowcaseSDBList.isEmpty())
            return;

        Gson gson = new Gson();
//        String json = gson.toJson(data);
        JsonObject jsonObject = gson.toJsonTree(data).getAsJsonObject();
        JsonElement planogramJsonArray = gson.toJsonTree(planogrammVizitShowcaseSDBList);
//        JsonElement planogramJsonArray = gson.toJsonTree(Collections.singletonList(planogram));
        jsonObject.add("list", planogramJsonArray);

        retrofit2.Call<JsonObject> call1 = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, jsonObject);
        call1.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("planogramDownload", "planogramDownload: " + response.body());
                Globals.writeToMLOG("INFO", "1_D_PlanogrammSDB", "response: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject responseBody = response.body();

                    // Проверяем наличие поля "state" и что оно true
                    if (responseBody.has("state") && responseBody.get("state").getAsBoolean()) {
                        JsonObject list = responseBody.getAsJsonObject("list");

                        if (list != null && !list.entrySet().isEmpty()) {
                            List<Integer> idsToUpdate = new ArrayList<>();

                            for (Map.Entry<String, JsonElement> entry : list.entrySet()) {
                                String idStr = entry.getKey();
                                JsonElement value = entry.getValue();

                                if (value != null && value.getAsBoolean()) {
                                    try {
                                        int id = Integer.parseInt(idStr);
                                        idsToUpdate.add(id);
                                    } catch (NumberFormatException e) {
                                        Log.e("Planogram", "Некорректный ID: " + idStr, e);
                                        Globals.writeToMLOG("INFO", "1_D_PlanogrammSDB.NumberFormatException", "response: " + response.body());
                                    }
                                }
                            }

                            // Если есть что обновлять
                            if (!idsToUpdate.isEmpty()) {
                                SQL_DB.planogrammVizitShowcaseDao().markUploaded(idsToUpdate);
                                Log.d("Planogram", "Обновлено uploadStatus=0 для ID: " + idsToUpdate);
                                Globals.writeToMLOG("INFO", "1_D_PlanogrammSDB", "Обновлено uploadStatus=0 для ID: " + idsToUpdate);

                            }
                        }
                    } else {
                        Log.w("Planogram", "Сервер вернул отрицательный результат: " + responseBody.toString());
                        Globals.writeToMLOG("INFO", "1_D_PlanogrammSDB", "response: " + response.body());
                    }
                } else {
                    Log.e("Planogram", "Ошибка ответа от сервера: " + response.code() + " / " + response.message());
                    Globals.writeToMLOG("INFO", "1_D_PlanogrammSDB", "response: " + response.body());
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("planogramDownload", "planogramDownloadThrowable t: " + t);
                Globals.writeToMLOG("INFO", "1_D_PlanogrammSDB.onFailure", "Throwable t: " + t);
            }
        });


    }

}
