package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.feature.SyncCallable;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammTypeSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammAddressResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammGroupResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammImagesResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class PlanogrammTableExchange {

    public void planogramDownload(SyncCallable click) {
        Set<Integer> uniquePlanogrammId = new HashSet<>();

        List<PlanogrammSDB> planogrammList = SQL_DB.planogrammDao().getAll();
        if (!planogrammList.isEmpty())
            for (PlanogrammSDB p : planogrammList) {
                uniquePlanogrammId.add(p.id);
            }

        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "list";
        data.nolimit = "1";
        data.exclude_id = new ArrayList<>(uniquePlanogrammId);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogramDownload", "convertedObject: " + convertedObject);
        retrofit2.Call<PlanogrammResponse> call = RetrofitBuilder.getRetrofitInterface().Planogramm_RESPONSE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<PlanogrammResponse>() {
            @Override
            public void onResponse(Call<PlanogrammResponse> call, Response<PlanogrammResponse> response) {

                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().state) {
                                if (response.body().list != null && !response.body().list.isEmpty()) {

                                    for (PlanogrammSDB item : response.body().list) {
                                        if (item.dtStart.getTime() < 0) {
                                            item.dtStart = null;
                                        }
                                        if (item.dtEnd.getTime() < 0) {
                                            item.dtEnd = null;
                                        }
                                    }

                                    SQL_DB.planogrammDao().insertAll(response.body().list)
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new DisposableCompletableObserver() {
                                                @Override
                                                public void onComplete() {
                                                    Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogramDownload/onResponse/onComplete", "OK: " + response.body().list.size());
                                                    click.onSuccess(response.body().list.size());
                                                }

                                                @Override
                                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                                    Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogramDownload/onResponse/onError", "Throwable e: " + e);
                                                    click.onFailure("onError SQL_DB.planogramDownload().insertAll Throwable e: " + e);
                                                }
                                            });

//                                    click.onSuccess(response.body().list);
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
                    Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogramDownload/onResponse", "Exception e: " + e);
                    click.onFailure("Ошибка запроса. Exception e: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<PlanogrammResponse> call, Throwable t) {
                Log.e("MAIN_test", "planogramDownload: " + t);
                click.onFailure("Ошибка запроса. Exception e: " + t.getMessage());
                Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogramDownload/onFailure", "Exception e: " + t.getMessage());

            }
        });
    }

    // Загрузка Планограмм Адресов
    public void planogrammAddressDownload(SyncCallable click) {
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "addr_list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogrammAddressDownload", "convertedObject: " + convertedObject);
        retrofit2.Call<PlanogrammAddressResponse> call = RetrofitBuilder.getRetrofitInterface().Planogramm_ADDRESS_RESPONSE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<PlanogrammAddressResponse>() {
            @Override
            public void onResponse(Call<PlanogrammAddressResponse> call, Response<PlanogrammAddressResponse> response) {
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().state) {
                                if (response.body().list != null && !response.body().list.isEmpty()) {
                                    SQL_DB.planogrammAddressDao().insertAll(response.body().list)
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new DisposableCompletableObserver() {
                                                @Override
                                                public void onComplete() {
                                                    Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogrammAddressDownload/onResponse/onComplete", "OK: " + response.body().list.size());
                                                    click.onSuccess(response.body().list.size());
                                                }

                                                @Override
                                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                                    Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammAddressDownload/onResponse/onError", "Throwable e: " + e);
                                                    click.onFailure("onError SQL_DB.planogrammAddressDownload().insertAll Throwable e: " + e);
                                                }
                                            });
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
                    Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammAddressDownload/onResponse", "Exception e: " + e);
                    click.onFailure("Ошибка запроса. Exception e: " + e);
                }
            }

            @Override
            public void onFailure(Call<PlanogrammAddressResponse> call, Throwable t) {
                Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammAddressDownload/onFailure", "Exception e: " + t.getMessage());
                click.onFailure("Ошибка запроса. Exception e: " + t.getMessage());
            }
        });
    }


    // Загрузка Планограмм Групп
    public void planogrammGroupDownload(SyncCallable click) {
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "group_list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "planogrammGroupDownload convertedObject: " + convertedObject);
        Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogrammGroupDownload", "convertedObject: " + convertedObject);
        retrofit2.Call<PlanogrammGroupResponse> call = RetrofitBuilder.getRetrofitInterface().Planogramm_GROUP_RESPONSE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<PlanogrammGroupResponse>() {
            @Override
            public void onResponse(Call<PlanogrammGroupResponse> call, Response<PlanogrammGroupResponse> response) {

                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().state) {
                                if (response.body().list != null && response.body().list.size() > 0) {
                                    SQL_DB.planogrammGroupDao().insertAll(response.body().list)
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new DisposableCompletableObserver() {
                                                @Override
                                                public void onComplete() {
                                                    click.onSuccess(response.body().list.size());
                                                    Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogrammGroupDownload/onResponse/onComplete", "OK: " + response.body().list.size());
                                                }

                                                @Override
                                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                                    Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammGroupDownload/onResponse/onError", "Throwable e: " + e);
                                                    click.onFailure("onError SQL_DB.planogrammGroupDownload().insertAll Throwable e: " + e);
                                                }
                                            });
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
                    Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammGroupDownload/onResponse", "Exception e: " + e);
                    click.onFailure("Ошибка запроса. Exception e: " + e);
                }
            }

            @Override
            public void onFailure(Call<PlanogrammGroupResponse> call, Throwable t) {
                Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammGroupDownload/onResponse", "Exception e: " + t.getMessage());
                click.onFailure("Ошибка запроса. Exception e: " + t.getMessage());
            }
        });
    }


    // Загрузка Планограмм Витрин
    public void planogrammImagesDownload(SyncCallable click) {
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "img_list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogrammImagesDownload", "convertedObject: " + convertedObject);
        retrofit2.Call<PlanogrammImagesResponse> call = RetrofitBuilder.getRetrofitInterface().Planogramm_IMAGES_RESPONSE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<PlanogrammImagesResponse>() {
            @Override
            public void onResponse(Call<PlanogrammImagesResponse> call, Response<PlanogrammImagesResponse> response) {
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().state) {
                                if (response.body().list != null && response.body().list.size() > 0) {
                                    SQL_DB.planogrammImagesDao().insertAll(response.body().list)
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new DisposableCompletableObserver() {
                                                @Override
                                                public void onComplete() {
                                                    click.onSuccess(response.body().list.size());
                                                    Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogrammImagesDownload/onResponse/onComplete", "OK: " + response.body().list.size());
                                                }

                                                @Override
                                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                                    Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammImagesDownload/onResponse/onError", "Throwable e: " + e);
                                                    click.onFailure("onError SQL_DB.PlanogrammTableExchange().insertAll Throwable e: " + e);
                                                }
                                            });
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
                    Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammImagesDownload/onResponse", "Exception e: " + e);
                    click.onFailure("Ошибка запроса. Exception e: " + e);
                }
            }

            @Override
            public void onFailure(Call<PlanogrammImagesResponse> call, Throwable t) {
                Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammImagesDownload/onResponse", "Exception e: " + t.getMessage());
                click.onFailure("Ошибка запроса. Exception e: " + t.getMessage());
            }
        });
    }

    public void planorgammType(SyncCallable click) {
        Set<String> uniquePlanogrammId = new HashSet<>();

        List<PlanogrammTypeSDB> planogrammList = SQL_DB.planogrammTypeDao().getAllPlanogramsm();
        if (!planogrammList.isEmpty())
            for (PlanogrammTypeSDB p : planogrammList) {
                uniquePlanogrammId.add(p.getPlanogram_id());
            }

        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "tt_type_list";
        data.nolimit = "1";
        data.id_exclude_old = new ArrayList<>(uniquePlanogrammId);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        RetrofitBuilder.getRetrofitInterface()
                .Planogramm_TYPE_RESPONSE(RetrofitBuilder.contentType, convertedObject)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapCompletable(planorgammType -> {
                    if (planorgammType.state) {
                        if (planorgammType.list != null && !planorgammType.list.isEmpty()) {
                            return SQL_DB.planogrammTypeDao()
                                    .insertAll(planorgammType.list)
                                    .doOnComplete(() -> {
                                        Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogrammType",
                                                "Data inserted successfully. Count: " + planorgammType.list.size());
                                        click.onSuccess(planorgammType.list.size());
                                    });
                        } else {
                            String errorMsg = "Ошибка запроса. Пустой список.";
                            Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammType", errorMsg);
                            throw new RuntimeException(errorMsg);
                        }
                    } else {
                        String errorMsg = "Ошибка запроса. State=false";
                        Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammType", errorMsg);
                        throw new RuntimeException(errorMsg);
                    }
                })
                .subscribe(
                        () -> {
                        },
                        throwable -> {
                            String errorMsg = "Ошибка запроса: " + throwable.getMessage();
                            Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammType", errorMsg);
                            click.onFailure(errorMsg);
                        }
                );
//                .subscribe(planorgammType -> {
//                    if (planorgammType.state && planorgammType.list != null && !planorgammType.list.isEmpty()) {
//                        SQL_DB.planogrammTypeDao()
//                                .insertAll(planorgammType.list)
//                                .subscribeOn(Schedulers.io())
//                                .subscribe();
//                        Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogrammType", "Data inserted successfully");
//                    }
//                }, throwable -> Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planorgammType", "exeption: " + throwable.getMessage()));

    }

//    public void planogramTypeList(Clicks.clickObjectAndStatus click) {
//        StandartData data = new StandartData();
//        data.mod = "planogram";
//        data.act = "tt_type_list";
//        data.nolimit = "1";
//
//        Gson gson = new Gson();
//        String json = gson.toJson(data);
//        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
//        Log.e("MAIN_test", "planogramDownload convertedObject: " + convertedObject);
//
////        retrofit2.Call<JsonObject> call1 = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
////        call1.enqueue(new Callback<JsonObject>() {
////            @Override
////            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
////                Log.e("planogramDownload", "planogramDownload: " + response.body());
////                Globals.writeToMLOG("INFO", "1_D_PlanogrammSDB", "response: " + response.body());
////            }
////
////            @Override
////            public void onFailure(Call<JsonObject> call, Throwable t) {
////                Log.e("planogramDownload", "planogramDownloadThrowable t: " + t);
////                Globals.writeToMLOG("INFO", "1_D_PlanogrammSDB", "Throwable t: " + t);
////            }
////        });
//
//        retrofit2.Call<PlanogrammResponse> call = RetrofitBuilder.getRetrofitInterface().Planogramm_RESPONSE(RetrofitBuilder.contentType, convertedObject);
//        call.enqueue(new Callback<PlanogrammResponse>() {
//            @Override
//            public void onResponse(Call<PlanogrammResponse> call, Response<PlanogrammResponse> response) {
//                Log.e("MAIN_test", "planogramDownload: " + response);
//                Log.e("MAIN_test", "planogramDownload body: " + response.body());
//                try {
//                    if (response.isSuccessful()) {
//                        if (response.body() != null) {
//                            if (response.body().state) {
//                                if (response.body().list != null && response.body().list.size() > 0) {
//
//                                    for (PlanogrammSDB item : response.body().list) {
//                                        if (item.dtStart.getTime() < 0) {
//                                            item.dtStart = null;
//                                        }
//                                        if (item.dtEnd.getTime() < 0) {
//                                            item.dtEnd = null;
//                                        }
//                                    }
//
//                                    SQL_DB.planogrammDao().insertAll(response.body().list)
//                                            .subscribeOn(Schedulers.io())
//                                            .subscribe(new DisposableCompletableObserver() {
//                                                @Override
//                                                public void onComplete() {
//                                                    Log.d("test", "test");
//                                                    Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogramDownload/onResponse/onComplete", "OK: " + response.body().list.size());
////                                                    click.onSuccess(response.body().list);
//                                                }
//
//                                                @Override
//                                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
//                                                    Log.d("test", "test");
//                                                    Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogramDownload/onResponse/onError", "Throwable e: " + e);
//                                                    click.onFailure("onError SQL_DB.planogramDownload().insertAll Throwable e: " + e);
//                                                }
//                                            });
//

    /// /                                    click.onSuccess(response.body().list);
//                                }
//
//                            } else {
//                                click.onFailure("Ошибка запроса. State=false");
//                            }
//                        } else {
//                            click.onFailure("Ошибка запроса. Тело пришло пустым.");
//                        }
//                    } else {
//                        click.onFailure("Ошибка запроса. Код: " + response.code());
//                    }
//                } catch (Exception e) {
//                    Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogramDownload/onResponse", "Exception e: " + e);
//                    click.onFailure("Ошибка запроса. Exception e: " + e);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<PlanogrammResponse> call, Throwable t) {
//                Log.e("MAIN_test", "planogramDownload: " + t);
//            }
//        });
//    }


    // Загрузка таблицы визитов планоргам
    public void planogrammVisitShowcase(SyncCallable click) {

        Set<Integer> uniquePlanogrammId = new HashSet<>();

        List<PlanogrammVizitShowcaseSDB> planogrammList = SQL_DB.planogrammVizitShowcaseDao().getAll();
        if (!planogrammList.isEmpty())
            for (PlanogrammVizitShowcaseSDB p : planogrammList) {
                uniquePlanogrammId.add(p.planogram_id);
            }
//test
//        uniquePlanogrammId.remove(606);
//        uniquePlanogrammId.remove(182);

        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "vizit_showcase_list";
        data.nolimit = "1";
        data.id_exclude_old = new ArrayList<>(uniquePlanogrammId);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        RetrofitBuilder.getRetrofitInterface()
                .PLANOGRAMM_VIZIT_SHOWCASE_RESPONSE(RetrofitBuilder.contentType, convertedObject)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response.state && response.list != null && !response.list.isEmpty()) {
                                SQL_DB.planogrammVizitShowcaseDao()
                                        .insertAll(response.list)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new DisposableCompletableObserver() {
                                            @Override
                                            public void onComplete() {
                                                Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogrammVisitShowcase",
                                                        "Data inserted. Size: " + response.list.size());
                                                click.onSuccess(response.list.size());
                                            }

                                            @Override
                                            public void onError(@NonNull Throwable e) {
                                                Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammVisitShowcase",
                                                        "DB error: " + e.getMessage());
                                                click.onFailure("DB error: " + e.getMessage());
                                            }
                                        });
                            } else {
                                String errorMsg = !response.state ? "State=false" : "Empty list";
                                Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammVisitShowcase", errorMsg);
                                click.onFailure(errorMsg);
                            }
                        },
                        throwable -> {
                            Globals.writeToMLOG("ERROR", "PlanogrammTableExchange/planogrammVisitShowcase",
                                    "Network error: " + throwable.getMessage());
                            click.onFailure("Network error: " + throwable.getMessage());
                        }
                );

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
