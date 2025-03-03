package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammAddressResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammGroupResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammImagesResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class PlanogrammTableExchange {

    public void planogramDownload(Clicks.clickObjectAndStatus click) {
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "planogramDownload convertedObject: " + convertedObject);

        retrofit2.Call<JsonObject> call1 = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call1.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("planogramDownload", "planogramDownload: " + response.body());
                Globals.writeToMLOG("INFO", "1_D_PlanogrammSDB", "response: " + response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("planogramDownload", "planogramDownloadThrowable t: " + t);
                Globals.writeToMLOG("INFO", "1_D_PlanogrammSDB", "Throwable t: " + t);
            }
        });

        retrofit2.Call<PlanogrammResponse> call = RetrofitBuilder.getRetrofitInterface().Planogramm_RESPONSE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<PlanogrammResponse>() {
            @Override
            public void onResponse(Call<PlanogrammResponse> call, Response<PlanogrammResponse> response) {
                Log.e("MAIN_test", "planogramDownload: " + response);
                Log.e("MAIN_test", "planogramDownload body: " + response.body());

                Log.e("test", "test" + response);
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().state) {
                                if (response.body().list != null && response.body().list.size() > 0) {

                                    for (PlanogrammSDB item : response.body().list){
                                        if (item.dtStart.getTime() < 0){
                                            item.dtStart = null;
                                        }
                                        if (item.dtEnd.getTime() < 0){
                                            item.dtEnd = null;
                                        }
                                    }

                                    SQL_DB.planogrammDao().insertAll(response.body().list)
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new DisposableCompletableObserver() {
                                                @Override
                                                public void onComplete() {
                                                    Log.d("test", "test");
                                                    Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogramDownload/onResponse/onComplete", "OK: " + response.body().list.size());
//                                                    click.onSuccess(response.body().list);
                                                }

                                                @Override
                                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                                    Log.d("test", "test");
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
                    click.onFailure("Ошибка запроса. Exception e: " + e);
                }
            }

            @Override
            public void onFailure(Call<PlanogrammResponse> call, Throwable t) {
                Log.e("MAIN_test", "planogramDownload: " + t);
            }
        });
    }

    // Загрузка Планограмм Адресов
    public void planogrammAddressDownload(Clicks.clickObjectAndStatus click) {
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "addr_list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "planogrammAddressDownload convertedObject: " + convertedObject);

        retrofit2.Call<PlanogrammAddressResponse> call = RetrofitBuilder.getRetrofitInterface().Planogramm_ADDRESS_RESPONSE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<PlanogrammAddressResponse>() {
            @Override
            public void onResponse(Call<PlanogrammAddressResponse> call, Response<PlanogrammAddressResponse> response) {
                Log.e("MAIN_test", "planogrammAddressDownload: " + response);
                Log.e("MAIN_test", "planogrammAddressDownload body: " + response.body());

                Log.e("test", "test" + response);
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().state) {
                                if (response.body().list != null && response.body().list.size() > 0) {
                                    SQL_DB.planogrammAddressDao().insertAll(response.body().list)
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new DisposableCompletableObserver() {
                                                @Override
                                                public void onComplete() {
                                                    Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogrammAddressDownload/onResponse/onComplete", "OK: " + response.body().list.size());
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
                Log.e("MAIN_test", "planogrammAddressDownload: " + t);
            }
        });
    }


    // Загрузка Планограмм Групп
    public void planogrammGroupDownload(Clicks.clickObjectAndStatus click) {
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "group_list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "planogrammGroupDownload convertedObject: " + convertedObject);

        retrofit2.Call<PlanogrammGroupResponse> call = RetrofitBuilder.getRetrofitInterface().Planogramm_GROUP_RESPONSE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<PlanogrammGroupResponse>() {
            @Override
            public void onResponse(Call<PlanogrammGroupResponse> call, Response<PlanogrammGroupResponse> response) {
                Log.e("MAIN_test", "planogrammGroupDownload: " + response);
                Log.e("MAIN_test", "planogrammGroupDownload body: " + response.body());

                Log.e("test", "test" + response);
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
                Log.e("MAIN_test", "planogrammGroupDownload: " + t);
            }
        });
    }


    // Загрузка Планограмм Витрин
    public void planogrammImagesDownload(Clicks.clickObjectAndStatus click) {
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "img_list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "planogrammImagesDownload convertedObject: " + convertedObject);

        retrofit2.Call<PlanogrammImagesResponse> call = RetrofitBuilder.getRetrofitInterface().Planogramm_IMAGES_RESPONSE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<PlanogrammImagesResponse>() {
            @Override
            public void onResponse(Call<PlanogrammImagesResponse> call, Response<PlanogrammImagesResponse> response) {
                Log.e("MAIN_test", "planogrammImagesDownload: " + response);
                Log.e("MAIN_test", "planogrammImagesDownload body: " + response.body());

                Log.e("test", "test" + response);
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
                Log.e("MAIN_test", "planogrammImagesDownload: " + t);
            }
        });
    }

    public void planogramTypeList(Clicks.clickObjectAndStatus click) {
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "tt_type_list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "planogramDownload convertedObject: " + convertedObject);

        retrofit2.Call<JsonObject> call1 = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call1.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("planogramDownload", "planogramDownload: " + response.body());
                Globals.writeToMLOG("INFO", "1_D_PlanogrammSDB", "response: " + response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("planogramDownload", "planogramDownloadThrowable t: " + t);
                Globals.writeToMLOG("INFO", "1_D_PlanogrammSDB", "Throwable t: " + t);
            }
        });

        retrofit2.Call<PlanogrammResponse> call = RetrofitBuilder.getRetrofitInterface().Planogramm_RESPONSE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<PlanogrammResponse>() {
            @Override
            public void onResponse(Call<PlanogrammResponse> call, Response<PlanogrammResponse> response) {
                Log.e("MAIN_test", "planogramDownload: " + response);
                Log.e("MAIN_test", "planogramDownload body: " + response.body());

                Log.e("test", "test" + response);
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().state) {
                                if (response.body().list != null && response.body().list.size() > 0) {

                                    for (PlanogrammSDB item : response.body().list){
                                        if (item.dtStart.getTime() < 0){
                                            item.dtStart = null;
                                        }
                                        if (item.dtEnd.getTime() < 0){
                                            item.dtEnd = null;
                                        }
                                    }

                                    SQL_DB.planogrammDao().insertAll(response.body().list)
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new DisposableCompletableObserver() {
                                                @Override
                                                public void onComplete() {
                                                    Log.d("test", "test");
                                                    Globals.writeToMLOG("INFO", "PlanogrammTableExchange/planogramDownload/onResponse/onComplete", "OK: " + response.body().list.size());
//                                                    click.onSuccess(response.body().list);
                                                }

                                                @Override
                                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                                    Log.d("test", "test");
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
                    click.onFailure("Ошибка запроса. Exception e: " + e);
                }
            }

            @Override
            public void onFailure(Call<PlanogrammResponse> call, Throwable t) {
                Log.e("MAIN_test", "planogramDownload: " + t);
            }
        });
    }

}
