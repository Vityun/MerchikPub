package ua.com.merchik.merchik.ServerExchange;


import static androidx.core.content.ContextCompat.startForegroundService;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.DownloadPictureService;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RetrofitResponse.ModImagesView;
import ua.com.merchik.merchik.data.RetrofitResponse.ModImagesViewList;
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgList;
import ua.com.merchik.merchik.data.RetrofitResponse.TovarImgResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.ImagesViewListImageList;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/**
 * 23.02.2021
 * Загрузка фоток.
 * <p>
 * Класс создан с целью получения от сервера фотографий по разным фильтрам. И сохранением их
 * в БД StackPhotoDB.
 */
public class PhotoDownload {
//    public final Globals globals = new Globals();

    /**
     * 09.09.20022
     * Загрузка фотографий по ID.
     */
    @SuppressWarnings("rawtypes")
    public void downloadPhotoByIds(String photoDir, String photoTypeDir, List<Integer> dataIds, Clicks.clickStatusMsg result) {

        // Ничего не делаю, если список ID-шников у нас пришел пустым
        if (dataIds == null || dataIds.size() == 0) {
            result.onFailure("Список ID-шников - пуст");
            return;
        }

        String idStringList = createPhotoIdList(dataIds);   // Формируем ID фотографий через Запятую

        // Если список ID меня не устраивает
        if (idStringList == null || idStringList.length() == 0) {
            result.onFailure("По какой-то причине не смогли сформировать список ID-шников");
            return;
        }

        // Формирую запрос
        StandartData data = new StandartData();
        data.mod = "images_view";
        data.act = "list_image";
        data.nolimit = "1";
//        data.image_type = "full";   // small (То шо тут было)
        data.image_type = "small";   // small (То шо тут было)

        data.photo_type = "35";

        data.id_list = idStringList;

        // Формирование тела запроса
        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Globals.writeToMLOG("INFO", "downloadPhotoByIds", "convertedObject: " + convertedObject);

        retrofit2.Call<TovarImgResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_PHOTO_INFO_JSON(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<TovarImgResponse>() {
            @Override
            public void onResponse(Call<TovarImgResponse> call, Response<TovarImgResponse> response) {
                Globals.writeToMLOG("INFO", "downloadPhotoByIds", "response: " + new Gson().toJson(response.body()));
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().getState()) {
                            int photoListUrlSize;
                            try {
                                photoListUrlSize = response.body().getList().size();
                            } catch (Exception e) {
                                photoListUrlSize = -1;
                            }

                            result.onSuccess("Данные о фото (" + photoListUrlSize + "шт) успешно получены. \nНачинаю загрузку фотографий.. \n\nЭТО МОЖЕТ ЗАНЯТЬ МНОГО ВРЕМЕНИ И ТРАФИКА!");

                            downloadPhoto(photoDir, photoTypeDir, response.body().getList(), result);
                        } else {
                            result.onFailure("Не получилось загрузить фото. Обратитесь к руководителю. Ошибка:\n\n(URL)state = false");
                        }
                    } else {
                        result.onFailure("Не получилось загрузить фото. Обратитесь к руководителю. Ошибка:\n\n(URL)Тело запроса вернулось пустым.");
                    }
                } else {
                    result.onFailure("Не получилось загрузить фото. Обратитесь к руководителю. Ошибка:\n\n(URL)Ошибка запроса: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TovarImgResponse> call, Throwable t) {
                Globals.writeToMLOG("INFO", "downloadPhotoByIds", "Throwable t: " + t);
                result.onFailure("Возникли проблемы с сетью. Проверьте интернет соединение, повторите попытку позже. Если проблема повторяется - обратитесь к Руководителю.\n\n(URL)Ошибка: " + t);
            }
        });
    }

    /**
     * 09.09.2022
     * Загрузка фотографий по ссылкам
     *
     * @param list
     * @param result
     */
    public void downloadPhoto(String photoDir, String photoTypeDir, List<TovarImgList> list, Clicks.clickStatusMsg result) {
        for (TovarImgList item : list) {
            String url = item.getPhotoUrl().replace("thumb_", "");
            retrofit2.Call<ResponseBody> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_PHOTO_BY_URL(url);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            try {
                                Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                                String path = Globals.saveImageHD(bmp, photoDir, photoTypeDir + item.getID());

                                if (StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(item.getID()) == null) {
                                    int id = RealmManager.stackPhotoGetLastId();
                                    id++;

                                    StackPhotoDB stackPhotoDB = new StackPhotoDB();
                                    stackPhotoDB.setId(id);
                                    stackPhotoDB.setPhotoServerId(item.getID());
                                    stackPhotoDB.setVpi(0);
                                    stackPhotoDB.setCreate_time(Long.parseLong(item.getDt()) * 1000);
                                    stackPhotoDB.setUpload_to_server(System.currentTimeMillis());
                                    stackPhotoDB.setGet_on_server(System.currentTimeMillis());
                                    stackPhotoDB.setPhoto_num(path);
                                    stackPhotoDB.setPhoto_type(Integer.valueOf(item.getPhotoTp()));
                                    stackPhotoDB.setUpload_time(0);
                                    stackPhotoDB.setUpload_status(0);
                                    stackPhotoDB.setStatus(false);

                                    RealmManager.stackPhotoSavePhoto(stackPhotoDB);
                                    result.onSuccess("photo id = " + id +
                                            ", PhotoServerId: " + item.getID() +
                                            ", hash: " + item.getHash());
                                }
                            } catch (Exception e) {
                                result.onFailure("downloadPhotoTest/onResponse/Exception e: " + e);
                            }
                        } else {
                            result.onFailure("downloadPhotoTest/onResponse/response.body() - тело пустое.");
                        }
                    } else {
                        result.onFailure("downloadPhotoTest/onResponse/response.isSuccessful(): " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    result.onFailure("downloadPhotoTest/onFailure/Throwable t: " + t);
                }
            });
        }
    }

    /**
     * 09.09.2022
     * Записываю ID фотографий через запятую в строку.
     * Это нужно для того что б сраобтал запрос на получение фоток.
     */
    public String createPhotoIdList(List<Integer> dataIds) {
        StringBuilder res = new StringBuilder();
        try {
            if (dataIds != null && dataIds.size() > 0) {
                for (Integer item : dataIds) {
                    res.append(item).append(", ");
                }
                res = new StringBuilder(res.substring(0, res.length() - 2));   // Убираю последний ", " - для того что б на сервере не произошло каких-то приколов.
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "PhotoDownload/createPhotoIdList", "Exception e: " + e);
        }

        return res.toString();
    }


    // -------------------  Я СЧИТАЮ ЧТО НИЖЕ - МУСОР ----------------------------------------------

    /**
     * 12.02.2022
     * Формирование запроса на получение ссылок для скачивания фотографий.
     */
    public static void getPhotoURLFromServer(List<TovarDB> tovars, Clicks.clickStatusMsg result, Clicks.clickStatusMsgMode result2, Context context) {
        List<Integer> tovarIdsList = getTovarIds(tovars);
//        List<Integer> tovarIdsList = getTovarIds(TovarRealm.getAllTov());

        Globals.writeToMLOG("INFO", "getPhotoURLFromServer", "ЗАГРУЗКА ФОТО. ПОЛУЧЕНО ФОТО: " + tovars.size());

        List<Integer> tovarsPhotoToDownload = StackPhotoRealm.findTovarIds(tovarIdsList); // Записываю сюда список ID-шников которых ещё нет на моей сторне

        Globals.writeToMLOG("INFO", "getPhotoURLFromServer", "ЗАГРУЗКА ФОТО. НУЖНО СКАЧАТЬ СТОЛЬКО ФОТО: " + tovarsPhotoToDownload.size());

//        result.onSuccess("Треба Дозавантажити " + tovarsPhotoToDownload.size() + " фото Товарів");


        // Разбивка на группі
        // Нужно для того что б не сразу все 8000 фоток загружалось на сторону приложения
        int batchSize = 50; // Размер каждой группы
        List<List<Integer>> batches = new ArrayList<>(); // Список, который будет содержать группы

        for (int i = 0; i < tovarsPhotoToDownload.size(); i += batchSize) {
            int end = Math.min(tovarsPhotoToDownload.size(), i + batchSize);
            List<Integer> batch = tovarsPhotoToDownload.subList(i, end);
            batches.add(batch);
        }

        StandartData data = new StandartData();
        data.mod = "images_view";
        data.act = "list_image";
        data.nolimit = "1";
        data.image_type = "small";
//        data.tovar_id = tovarsPhotoToDownload;    // Должен сюда записать список ID-шников Товаров которые я хочу загрузить на свою сторону.
//        data.photo_tovar_id = batches.get(0);    // Должен сюда записать список ID-шников Товаров которые я хочу загрузить на свою сторону.
        data.photo_tovar_id = tovarsPhotoToDownload;    // Должен сюда записать список ID-шников Товаров которые я хочу загрузить на свою сторону.

        // Формирование тела запроса
        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

//        Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/getPhotoURLFromServer/onSuccess", "convertedObject: " + convertedObject);


        // Отладочная инфа
        long start = System.currentTimeMillis() / 1000;

        retrofit2.Call<TovarImgResponse> call = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_PHOTO_INFO_JSON(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<TovarImgResponse>() {
            @Override
            public void onResponse(Call<TovarImgResponse> call, Response<TovarImgResponse> response) {
                int photoListUrlSize;

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().getState()) {
                            try {
                                photoListUrlSize = response.body().getList().size();
                            } catch (Exception e) {
                                photoListUrlSize = -1;
                            }

//                            Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/getPhotoURLFromServer/onSuccess/photoListUrlSize", "photoListUrlSize: " + photoListUrlSize);

                            long end = System.currentTimeMillis() / 1000 - start;
                            result.onSuccess("Данные о фото товаров(" + photoListUrlSize + "шт) успешно получены. Это заняло " + end + " секунд! \nНачинаю загрузку фотографий.. \n\nЭТО МОЖЕТ ЗАНЯТЬ МНОГО ВРЕМЕНИ И ТРАФИКА!");
                            // TODO Начинаем загрузку + сохранение на телефон фоток Товаров.
//                            downloadPhoto(response.body().getList(), result, result2);

                            try {
                                Intent serviceIntent = new Intent(context, DownloadPictureService.class);
                                DownloadPictureService.picList = response.body().getList();
                                startForegroundService(context, serviceIntent);
                            }catch (Exception e){
                                Globals.writeToMLOG("ERROR", "DownloadPictureService", "Exception e: " + e);
                                downloadPhoto(response.body().getList(), result, result2);
                            }
                        } else {
                            result.onFailure("Не получилось загрузить фото Товаров. Обратитесь к руководителю. Ошибка:\n\n(URL)state = false");
                        }
                    } else {
                        result.onFailure("Не получилось загрузить фото Товаров. Обратитесь к руководителю. Ошибка:\n\n(URL)Тело запроса вернулось пустым.");
                    }
                } else {
                    result.onFailure("Не получилось загрузить фото Товаров. Обратитесь к руководителю. Ошибка:\n\n(URL)Ошибка запроса: " + response.code());
                }

                Log.d("test", "test" + convertedObject);
            }

            @Override
            public void onFailure(Call<TovarImgResponse> call, Throwable t) {
                result.onFailure("Возникли проблемы с сетью. Проверьте интернет соединение, повторите попытку позже. Если проблема повторяется - обратитесь к Руководителю.\n\n(URL)Ошибка: " + t);

                Log.d("test", "test");
            }
        });


/*        // test новых приколов с товарами
        StandartData data1 = new StandartData();
        data1.mod = "images_view";
        data1.act = "list_image";
        data1.nolimit = "1";
        data1.image_type = "small";
//        data1.photo_tovar_id = tovarsPhotoToDownload;    // Должен сюда записать список ID-шников Товаров которые я хочу загрузить на свою сторону.
        data1.photo_tovar_id = batches.get(0);    // Должен сюда записать список ID-шников Товаров которые я хочу загрузить на свою сторону.

        // Формирование тела запроса
        Gson gson1 = new Gson();
        String json1 = gson1.toJson(data1);
        JsonObject convertedObject1 = new Gson().fromJson(json1, JsonObject.class);

        // Отладочная инфа
        long start1 = System.currentTimeMillis() / 1000;

        retrofit2.Call<TovarImgResponse> call1 = RetrofitBuilder.getRetrofitInterface().GET_TOVAR_PHOTO_INFO_JSON(RetrofitBuilder.contentType, convertedObject1);
        call1.enqueue(new Callback<TovarImgResponse>() {
            @Override
            public void onResponse(Call<TovarImgResponse> call, Response<TovarImgResponse> response) {
                int photoListUrlSize;

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().getState()) {
                            try {
                                photoListUrlSize = response.body().getList().size();
                            } catch (Exception e) {
                                photoListUrlSize = -1;
                            }

                            long end = System.currentTimeMillis() / 1000 - start1;
                            result.onSuccess("Данные о фото товаров(" + photoListUrlSize + "шт) успешно получены. Это заняло " + end + " секунд! \nНачинаю загрузку фотографий.. \n\nЭТО МОЖЕТ ЗАНЯТЬ МНОГО ВРЕМЕНИ И ТРАФИКА!");
                            // TODO Начинаем загрузку + сохранение на телефон фоток Товаров.
//                            downloadPhoto(response.body().getList(), result, result2);
                        } else {
                            result.onFailure("Не получилось загрузить фото Товаров. Обратитесь к руководителю. Ошибка:\n\n(URL)state = false");
                        }
                    } else {
                        result.onFailure("Не получилось загрузить фото Товаров. Обратитесь к руководителю. Ошибка:\n\n(URL)Тело запроса вернулось пустым.");
                    }
                } else {
                    result.onFailure("Не получилось загрузить фото Товаров. Обратитесь к руководителю. Ошибка:\n\n(URL)Ошибка запроса: " + response.code());
                }

                Log.d("test", "test" + convertedObject);
            }

            @Override
            public void onFailure(Call<TovarImgResponse> call, Throwable t) {
                result.onFailure("Возникли проблемы с сетью. Проверьте интернет соединение, повторите попытку позже. Если проблема повторяется - обратитесь к Руководителю.\n\n(URL)Ошибка: " + t);

                Log.d("test", "test");
            }
        });*/
    }

    private static List<Integer> getTovarIds(List<TovarDB> tovars) {
        ArrayList<Integer> result = new ArrayList<>();
        for (TovarDB tovar : tovars) {
            result.add(Integer.valueOf(tovar.getiD()));
        }
        return result;
    }

    /**
     * 12.02.2022
     * Загрузка фоток Товаров.
     */
    static int notSuccessfulResponse;
    static int bodyIsNull;
    static int saveNewTovarPhoto;
    static int errorSaveTovarPhoto;
    static int internetError;

    public static void downloadPhoto(List<TovarImgList> data, Clicks.clickStatusMsg result, Clicks.clickStatusMsgMode result2) {
        long start = System.currentTimeMillis() / 1000;
        final int[] cnt = {0};
        int count = 0;
        notSuccessfulResponse = 0;
        bodyIsNull = 0;
        saveNewTovarPhoto = 0;
        errorSaveTovarPhoto = 0;
        internetError = 0;
        long countTP = 0;
//        notSuccessfulResponse + "(1)/" + bodyIsNull + "(2)/" + saveNewTovarPhoto + "(3)/" + errorSaveTovarPhoto + "(4)/" + internetError + "(5)/"

        int desiredPhotoTP = 18; // Значение, которое вы хотите проверить
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            countTP = data.stream()
                    .filter(item -> {
                        try {
                            int photoTP = Integer.parseInt(item.getPhotoTp());
                            return photoTP == desiredPhotoTP;
                        } catch (NumberFormatException e) {
                            return false; // В случае ошибки парсинга числа
                        }
                    })
                    .count();
        }


        for (TovarImgList item : data) {
            int photoTP = Integer.parseInt(item.getPhotoTp());

            if (photoTP == 18) {
                count++;
//                Globals.writeToMLOG("INFO", "getPhotoURLFromServer.onResponse.downloadPhoto", "convertedObject: " + new Gson().toJson(item));
                retrofit2.Call<ResponseBody> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_PHOTO_BY_URL(item.getPhotoUrl());
                int finalCount = count;
                long finalCountTP = countTP;
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                try {
                                    Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                                    String path = Globals.saveImage1(bmp, "TOVAR_" + item.getTovarId() + "_SID" + item.getID());

                                    int id = RealmManager.stackPhotoGetLastId();
                                    id++;

                                    StackPhotoDB stackPhotoDB = new StackPhotoDB();
                                    stackPhotoDB.setId(id);
                                    stackPhotoDB.setPhotoServerId(item.getID());
                                    stackPhotoDB.setObject_id(Integer.valueOf(item.getTovarId()));

                                    stackPhotoDB.addr_id = Integer.valueOf(item.getAddrId());
                                    stackPhotoDB.approve = Integer.valueOf(item.getApprove());
                                    stackPhotoDB.dvi = Integer.valueOf(item.getDvi());

                                    stackPhotoDB.setVpi(0);
                                    stackPhotoDB.setCreate_time(Long.parseLong(item.getDt()) * 1000);
                                    stackPhotoDB.setUpload_to_server(0);
                                    stackPhotoDB.setGet_on_server(0);
                                    stackPhotoDB.setPhoto_num(path);
                                    stackPhotoDB.setPhoto_hash(item.getHash());
                                    stackPhotoDB.setPhoto_type(Integer.valueOf(item.getPhotoTp()));
                                    stackPhotoDB.setComment("small");
                                    stackPhotoDB.setUpload_time(0);
                                    stackPhotoDB.setUpload_status(0);
                                    stackPhotoDB.setStatus(false);

                                    // 30.01
                                    RealmManager.stackPhotoSavePhoto(stackPhotoDB);
                                    saveNewTovarPhoto++;
                                } catch (Exception e) {
                                    errorSaveTovarPhoto++;
                                }
                            } else {
                                bodyIsNull++;
                            }
                        } else {
                            notSuccessfulResponse++;
                        }

                        if (cnt[0] < finalCount) {
                            cnt[0]++;
                            result2.onSuccess("Завантажено " + cnt[0] + " фото з " + finalCountTP, Clicks.MassageMode.SHOW);
                        } else if (cnt[0] == finalCount) {
//                            result.onSuccess("S/Закончил работу, обработал(всего/с типом 18/загружено): " + data.size() + "/" + finalCount + "/" + cnt[0] + "\n\n(Код не 200(1)/Тело пустое(2)/Сохранило новую фотку товара(3)/Ошибка при сохранении фото(4)/Ошибка интернета(5))\n\n" + notSuccessfulResponse + "(1)/" + bodyIsNull + "(2)/" + saveNewTovarPhoto + "(3)/" + errorSaveTovarPhoto + "(4)/" + internetError + "(5)/");
//                            result.onSuccess("Завантажено " + cnt[0] + " фото з " + data.size());
                            result2.onSuccess("Закінчив завантаження, завантажено: " + cnt[0] + " фото.", Clicks.MassageMode.CLOSE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        internetError++;
                        if (cnt[0] < finalCount) {
                            cnt[0]++;
                            result2.onSuccess("Завантажено " + cnt[0] + " фото з " + finalCountTP + "\nПомилка: " + t, Clicks.MassageMode.SHOW);
                        } else if (cnt[0] == finalCount) {
//                            result.onSuccess("F/Закончил работу, обработал(всего/с типом 18/загружено): " + data.size() + "/" + finalCount + "/" + cnt[0]);
//                            result.onSuccess("Завантажено " + cnt[0] + " фото з " + data.size());
                            result2.onSuccess("Закінчив завантаження, завантажено: " + cnt[0] + " фото." + "\nПомилка: " + t, Clicks.MassageMode.CLOSE);
                        }
                    }
                });
            }
        }

//        Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/getPhotoURLFromServer/downloadPhoto", "Фоток с типом 18: " + count);


        result.onSuccess("Фоток с типом 18: " + count);
    }

    /**
     * 23.02.2021
     * Получение списка фотографий(таблички) для дальнейшего скачивания.
     */
    public void getPhotoFromServer(PhotoTableRequest data) {
        String contentType = "application/json";
        JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(data), JsonObject.class);

        Globals.writeToMLOG("INFO", "" + getClass().getName() + "/getPhotoFromServer/convertedObject", "convertedObject: " + convertedObject);

        {
            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().MOD_IMAGES_VIEW_CALL_JSON(contentType, convertedObject);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.d("smarti", "onResponse: ");
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("smarti", "onResponse: ");
                }
            });
        }

        retrofit2.Call<ModImagesView> call = RetrofitBuilder.getRetrofitInterface().MOD_IMAGES_VIEW_CALL(contentType, convertedObject);
        call.enqueue(new retrofit2.Callback<ModImagesView>() {
            @Override
            public void onResponse(retrofit2.Call<ModImagesView> call, retrofit2.Response<ModImagesView> response) {
                try {
                    int size = 0;
                    if (response.body() != null && response.body().getState() && response.body().getList() != null) {
                        size = response.body().getList().size();
//                        Globals.writeToMLOG("INFO", "PetrovExchangeTest/startExchange/getPhotoFromServer/onSuccess", "(фото юзеров которые надо закачать)size: " + size);
                    }
                    Globals.writeToMLOG("INFO", "" + getClass().getName() + "/getPhotoFromServer/onResponse", "size: " + size);
                    savePhotoToDB(response.body().getList());

                    Log.e("getPhotoFromServer", "response.body().getTotal(): " + response.body().getTotalPages());
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "" + getClass().getName() + "/getPhotoFromServer/onResponse", "Exception e: " + e);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ModImagesView> call, Throwable t) {
                Log.e("getPhotoFromServer", "test.t:" + t);
                Globals.writeToMLOG("ERROR", "" + getClass().getName() + "/getPhotoFromServer/onFailure", "Throwable t: " + t);
            }
        });
    }


    private void savePhotoToDB(List<ModImagesViewList> list) {
        try {
            Globals.writeToMLOG("INFO", getClass().getName() + "savePhotoToDB", "List<ModImagesViewList> list: " + list.size());

            List<StackPhotoDB> stackList = new ArrayList<>();

            for (ModImagesViewList item : list) {
//                if (StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(item.getID()) == null) {
                if (StackPhotoRealm.stackPhotoDBGetPhotoByHASH(item.imgHash) == null) {
                    try {
                        downloadPhoto(item.getPhotoUrl(), new ExchangeInterface.ExchangePhoto() {
                            @Override
                            public void onSuccess(Bitmap bitmap) {
                                StackPhotoDB stackPhotoDB = new StackPhotoDB();
                                stackPhotoDB.setId(RealmManager.stackPhotoGetLastId() + 1);
                                stackPhotoDB.setObject_id(1);   // Добавлено что б эти фотки не пытались выгружаться обычным обменом

                                stackPhotoDB.code_dad2 = Long.parseLong(item.codeDad2);

                                stackPhotoDB.dt = item.getDt();
                                stackPhotoDB.setTime_event(Clock.getHumanTime3(item.getDt()));
                                stackPhotoDB.setCreate_time(item.getDt() * 1000);// реквизиты что б фотки не выгружались обратно на сервер
                                stackPhotoDB.setUpload_to_server(System.currentTimeMillis());// реквизиты что б фотки не выгружались обратно на сервер
                                stackPhotoDB.setGet_on_server(System.currentTimeMillis());// реквизиты что б фотки не выгружались обратно на сервер

                                stackPhotoDB.setPhotoServerId(item.getID());
                                stackPhotoDB.setPhotoServerURL(item.getPhotoUrl());
                                stackPhotoDB.setPhoto_num(Globals.savePhotoToPhoneMemory("/Manager", item.getID() + "_" + "small", bitmap));

                                stackPhotoDB.setUser_id(Integer.valueOf(item.getMerchikId()));
                                stackPhotoDB.setAddr_id(Integer.valueOf(item.getAddrId()));
                                stackPhotoDB.setClient_id(item.getClientId());
                                stackPhotoDB.setPhoto_type(Integer.valueOf(item.getPhotoTp()));
                                stackPhotoDB.photo_hash = item.imgHash;
                                stackPhotoDB.tovar_id = item.getTovarId();

                                stackPhotoDB.showcase_id = item.showcase_id;

                                stackPhotoDB.code_iza = item.codeIZA;

                                stackPhotoDB.setDvi(Integer.valueOf(item.getDvi()));

                                try {
                                    Globals.writeToMLOG("INFO", "PhotoMerchikExchange/getPhotoFromSite/savePhotoToDB", "stackPhotoDB: " + new Gson().toJson(stackPhotoDB));
                                } catch (Exception e) {
                                    Globals.writeToMLOG("INFO", "PhotoMerchikExchange/getPhotoFromSite/savePhotoToDB", "Exception e: " + e);
                                }


                                RealmManager.stackPhotoSavePhoto(stackPhotoDB);

                                stackList.add(stackPhotoDB);
                            }

                            @Override
                            public void onFailure(String error) {
                                Globals.writeToMLOG("ERR", getClass().getName() + "savePhotoToDB/onFailure", "String error: " + error);
                            }
                        });

                    } catch (Exception e) {
                        Globals.writeToMLOG("ERR", getClass().getName() + "savePhotoToDB", "Create new data Exception e: " + e);
                    }
                }
            }

            Globals.writeToMLOG("INFO", getClass().getName() + "savePhotoToDB", "stackList: " + stackList.size());
//            RealmManager.stackPhotoSavePhoto(stackList);

            // Сохранение Впемени последнего изменения таблички
            SynchronizationTimetableDB sync = RealmManager.getSynchronizationTimetableRowByTable("stack_photo");

            Log.e("getPhotoFromServer", "savePhotoToDB.sync.START: " + sync.getVpi_app());

            RealmManager.INSTANCE.executeTransaction((realm) -> {
                sync.setVpi_app(System.currentTimeMillis() / 1000);
            });

            Log.e("getPhotoFromServer", "savePhotoToDB.sync.END: " + sync.getVpi_app());

            RealmManager.setToSynchronizationTimetableDB(sync);
        } catch (Exception e) {
            Globals.writeToMLOG("ERR", getClass().getName() + "savePhotoToDB", "Exception e: " + e);
        }
    }

    public interface downloadPhotoInterface {
        void onSuccess(StackPhotoDB data);

        void onFailure(String s);
    }

    public void downloadPhoto(boolean size, StackPhotoDB data, downloadPhotoInterface downloadPhotoInterface) {

        Log.e("FULL_PHOTO", "size: " + size);

        String url = data.getPhotoServerURL();
        String photoSize;   // Думаю это стоит на енумчик зменить

        Log.e("FULL_PHOTO", "url: " + url);


        if (size) {
            url = url.replace("thumb_", "");
            photoSize = "Full";
        } else {
            photoSize = "Small";
        }

        Log.e("FULL_PHOTO", "url2: " + url);

        retrofit2.Call<ResponseBody> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_PHOTO_BY_URL(url);
        String finalUrl = url;
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("FULL_PHOTO", "onResponse");
                if (response.isSuccessful() && response.body() != null) {
                    Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                    String path = Globals.savePhotoToPhoneMemory("", "UserPhoto" + photoSize + "-" + data.getPhotoServerId(), bmp);

                    Log.e("TESTING", "3_SAVE PHOTO");
                    Log.e("TESTING", "3_SAVE PHOTO/path: " + path);

                    RealmManager.INSTANCE.executeTransaction((realm -> {
                        data.setPhoto_num(path);
                        data.setPhoto_size(photoSize);
                    }));
                    RealmManager.stackPhotoSavePhoto(data);

                    downloadPhotoInterface.onSuccess(data);
                } else {
                    downloadPhotoInterface.onFailure("Запрос не совсем удачный. Требуется проверить ЛОГ. Отпишите своему руководителю.");

                    String msg = "Пришла пустота. Запрос: " + finalUrl;

                    if (response.body() != null) {
                        msg += "response.isSuccessful(): " + response.isSuccessful() + "\n\t\t\t" +
                                "response.body(): " + response.body() + "\n\t\t\t" +
                                "response.body().byteStream()LENGTH: " + response.body().byteStream().toString().length();
                    }

                    Globals.writeToMLOG("INFO", "PhotoDownload.downloadPhoto.onResponse", msg);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("FULL_PHOTO", "onFailure");
                downloadPhotoInterface.onFailure(t.toString());
            }
        });
    }


    //... посмотрел я на код который писал ранее.. это конечно жесть. Сейчас попробую написать
    // более гибкий, но не менее жёсткий код )). Этот раздел в любом случае надо будет
    // пересматривать и редактировать

    /**
     * 17.03.2021
     * Получение списка фотографий с сайта.
     * <p>
     * Вернувший в этот раздел в очередной раз я понял что он требует больших изменений и
     * нормального описания что тут происходит + нужно настраивать как и куда я хочу
     * сохранять фотографии.
     * <p>
     * Данная функция будет заниматься тем что в зависимости от переданных в неё данных - она будет
     * загружать с сайта информацию о фотках(и сохранять их в БД) для дальнейшей работы с ними.
     *
     * @param data - JSON запрос на сайт для получения списка фоток.
     */
    public void getPhotoInfoAndSaveItToDB(PhotoTableRequest data, Clicks.clickObjectAndStatus<StackPhotoDB> clickUpdatePhoto) {
        Log.e("getPhotoInfo2", "HERE");
        JsonObject object = new Gson().fromJson(new Gson().toJson(data), JsonObject.class);

        {
            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().MOD_IMAGES_VIEW_CALL_JSON(RetrofitBuilder.contentType, object);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.d("smarti", "onResponse: ");
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("smarti", "onResponse: ");
                }
            });
        }

        retrofit2.Call<ModImagesView> call = RetrofitBuilder.getRetrofitInterface().MOD_IMAGES_VIEW_CALL(RetrofitBuilder.contentType, object);
        call.enqueue(new retrofit2.Callback<ModImagesView>() {
            @Override
            public void onResponse(retrofit2.Call<ModImagesView> call, retrofit2.Response<ModImagesView> response) {
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().getState() && response.body().getList() != null && response.body().getList().size() > 0) {
                                savePhotoInfoToDB(response.body().getList(), clickUpdatePhoto);
                            } else {
                                clickUpdatePhoto.onFailure("Проблема с загрузкой фото. Обратитесь к руководителю.");
                                Globals.writeToMLOG("INFO", "getPhotoInfoAndSaveItToDB",
                                        "response.body().getState(): " + response.body().getState() +
                                                "response.body().getList() == NULL OR 0");
                            }
                        } else {
                            clickUpdatePhoto.onFailure("Проблема с загрузкой фото. Обратитесь к руководителю.");
                        }
                    } else {
                        clickUpdatePhoto.onFailure("Проблема с загрузкой фото. Обратитесь к руководителю.");
                    }

                } catch (Exception e) {
                    clickUpdatePhoto.onFailure("Проблема с загрузкой фото. Обратитесь к руководителю.");
                    Globals.writeToMLOG("ERROR", "getPhotoInfoAndSaveItToDB", "Не удалось сохранить фото в БД. Exception e: " + e);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ModImagesView> call, Throwable t) {
                Log.e("getPhotoInfo2", "test.t:" + t);
            }
        });
    }

    public void getPhotoInfoAndSaveItToDB(PhotoTableRequest data) {
        Log.e("getPhotoInfo2", "HERE");
        JsonObject object = new Gson().fromJson(new Gson().toJson(data), JsonObject.class);

        {
            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().MOD_IMAGES_VIEW_CALL_JSON(RetrofitBuilder.contentType, object);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.d("smarti", "onResponse: ");
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("smarti", "onResponse: ");
                }
            });
        }

        retrofit2.Call<ModImagesView> call = RetrofitBuilder.getRetrofitInterface().MOD_IMAGES_VIEW_CALL(RetrofitBuilder.contentType, object);
        call.enqueue(new retrofit2.Callback<ModImagesView>() {
            @Override
            public void onResponse(retrofit2.Call<ModImagesView> call, retrofit2.Response<ModImagesView> response) {
                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().getState() && response.body().getList() != null && response.body().getList().size() > 0) {
                                savePhotoInfoToDB(response.body().getList());
                            }
                        }
                    }

                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "getPhotoInfoAndSaveItToDB", "Не удалось сохранить фото в БД. Exception e: " + e);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ModImagesView> call, Throwable t) {
                Log.e("getPhotoInfo2", "test.t:" + t);
            }
        });
    }


    /**
     * 17.03.2021
     * Сохранение записей о фотках в БД.
     * <p>
     * Спустя куча костылей стало очевидно что что б фотки не выгружались на сервер - надо им
     * поставить признак что они уже выгружались.
     * <p>
     * Эта функция в перспективе будет расширяться как и стэкфото
     * <p>
     * 28.02.23. Добавил Clicks.clickObjectAndStatus<StackPhotoDB> clickUpdatePhoto для того что б
     * была возможность вернуть фотку которую только что загрузили. На момент написания коммента,
     * понимаю что надо будет потом сделать адекватнее и возращать список.
     */
    public void savePhotoInfoToDB(List<ModImagesViewList> list, Clicks.clickObjectAndStatus<StackPhotoDB> clickUpdatePhoto) {
        List<StackPhotoDB> stackList = new ArrayList<>();   // Создаём список для записи в БД
        int id = RealmManager.stackPhotoGetLastId() + 1;    // Для новой записи добавляем ID

        // Перебираем полученные от сервера данные и формируем список для записи.
        for (ModImagesViewList item : list) {

            // Если у меня в БД нет записи с таким `photo site ID` - создаю новую
            if (StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(item.getID()) == null) {
                StackPhotoDB stackPhotoDB = new StackPhotoDB();
                stackPhotoDB.setId(id);

                stackPhotoDB.setDt(item.getDt());

                stackPhotoDB.setCreate_time(item.getDt() * 1000);// реквизиты что б фотки не выгружались обратно на сервер
                stackPhotoDB.setUpload_to_server(item.getDt() * 1000);// реквизиты что б фотки не выгружались обратно на сервер
                stackPhotoDB.setGet_on_server(item.getDt() * 1000);// реквизиты что б фотки не выгружались обратно на сервер

                stackPhotoDB.setPhotoServerId(item.getID());
                stackPhotoDB.setPhotoServerURL(item.getPhotoUrl());

                stackPhotoDB.setUser_id(Integer.valueOf(item.getMerchikId()));
                stackPhotoDB.setUserTxt(item.getMerchikIdTxt());

                stackPhotoDB.setAddr_id(Integer.valueOf(item.getAddrId()));
                stackPhotoDB.setAddressTxt(item.getAddrIdTxt());

                stackPhotoDB.setClient_id(item.getClientId());
                stackPhotoDB.setCustomerTxt(item.getClientIdTxt());

                stackPhotoDB.setPhoto_type(Integer.valueOf(item.getPhotoTp()));
                stackPhotoDB.setPhoto_typeTxt(String.valueOf(item.getPhotoTpTxt()));

                stackPhotoDB.setDvi(Integer.valueOf(item.getDvi()));
                stackList.add(stackPhotoDB);

                id++;
            }
        }
        RealmManager.stackPhotoSavePhoto(stackList);
        clickUpdatePhoto.onSuccess(stackList.get(0));   // TODO Это стоит сделать адекватнее. Сделано это только для частного случая.
    }

    public void savePhotoInfoToDB(List<ModImagesViewList> list) {
        List<StackPhotoDB> stackList = new ArrayList<>();   // Создаём список для записи в БД
        int id = RealmManager.stackPhotoGetLastId() + 1;    // Для новой записи добавляем ID

        // Перебираем полученные от сервера данные и формируем список для записи.
        for (ModImagesViewList item : list) {

            // Если у меня в БД нет записи с таким `photo site ID` - создаю новую
            if (StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(item.getID()) == null) {
                StackPhotoDB stackPhotoDB = new StackPhotoDB();
                stackPhotoDB.setId(id);

                stackPhotoDB.setDt(item.getDt());

                stackPhotoDB.setCreate_time(item.getDt() * 1000);// реквизиты что б фотки не выгружались обратно на сервер
                stackPhotoDB.setUpload_to_server(item.getDt() * 1000);// реквизиты что б фотки не выгружались обратно на сервер
                stackPhotoDB.setGet_on_server(item.getDt() * 1000);// реквизиты что б фотки не выгружались обратно на сервер

                stackPhotoDB.setPhotoServerId(item.getID());
                stackPhotoDB.setPhotoServerURL(item.getPhotoUrl());

                stackPhotoDB.setUser_id(Integer.valueOf(item.getMerchikId()));
                stackPhotoDB.setUserTxt(item.getMerchikIdTxt());

                stackPhotoDB.setAddr_id(Integer.valueOf(item.getAddrId()));
                stackPhotoDB.setAddressTxt(item.getAddrIdTxt());

                stackPhotoDB.setClient_id(item.getClientId());
                stackPhotoDB.setCustomerTxt(item.getClientIdTxt());

                stackPhotoDB.setPhoto_type(Integer.valueOf(item.getPhotoTp()));
                stackPhotoDB.setPhoto_typeTxt(String.valueOf(item.getPhotoTpTxt()));

                stackPhotoDB.setDvi(Integer.valueOf(item.getDvi()));

                stackPhotoDB.code_iza = item.codeIZA;

                stackList.add(stackPhotoDB);

                id++;
            }
        }
        RealmManager.stackPhotoSavePhoto(stackList);
    }


    /**
     * 17.03.2021
     * Физическая загрузка конкретной фотографии.
     * <p>
     * Загрузка со стороны сайта в приложение полноценной фотографии по данным из `StackPhotoDB`.
     * Из стэка фото берём URL путь хранения фотки на сайте(для её получения)
     *
     * @param photoSize              - Размер загружаемой фотографии. True(большая фотка) False(маленькая).
     * @param dbRow                  - Строка с таблици `StackPhotoDB`. Информация о фотке которую будем загружать.
     * @param folderName             - Папка в которую будем осуществлять загрузку фотки. (/FolderName).
     * @param downloadPhotoInterface - Обработка успешной/провальной загрузки фотографии.
     */
    public void downloadPhoto(boolean photoSize, StackPhotoDB dbRow, String folderName, downloadPhotoInterface downloadPhotoInterface) {
        String url = dbRow.getPhotoServerURL();
        String size;   // Думаю это стоит на енумчик зменить

        if (photoSize) {
            url = url.replace("thumb_", "");
            size = "Full";
        } else {
            size = "Small";
        }

        retrofit2.Call<ResponseBody> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_PHOTO_BY_URL(url);
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {

                    Globals.writeToMLOG("INFO", "newPhotoDownload().downloadPhoto(", "response.body(): " + response.body());

                    Log.e("downloadPhoto", "response.body(): " + response.body().byteStream());

                    Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                    String path = Globals.savePhotoToPhoneMemory(folderName, dbRow.getPhotoServerId() + "_" + size, bmp);

                    Log.e("TESTING", "4_SAVE PHOTO");
                    Log.e("TESTING", "4_SAVE PHOTO/path: " + path);


                    RealmManager.INSTANCE.executeTransaction((realm -> {
                        dbRow.setPhoto_num(path);
                        dbRow.setPhoto_size(size);
                    }));
                    RealmManager.stackPhotoSavePhoto(dbRow);

                    downloadPhotoInterface.onSuccess(dbRow);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                downloadPhotoInterface.onFailure(t.toString());
            }
        });
    }


    /**
     * 27.07.2021
     * Скачивание фото по ссылке.
     * <p>
     * Отправляю на сервер ссылку, в ответ фотографию, возвращаю дальше в приложение фото
     */
    public static void downloadPhoto(String photoUrl, ExchangeInterface.ExchangePhoto exchange) {
        try {
            retrofit2.Call<ResponseBody> call = RetrofitBuilder.getRetrofitInterface().DOWNLOAD_PHOTO_BY_URL(photoUrl.replace("thumb_", ""));
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                Globals.writeToMLOG("INFO", "downloadPhoto/onResponse", "response.body(): " + response.body());
                                InputStream data = response.body().byteStream(); // <--- TODO BUG    java.lang.NullPointerException: Attempt to invoke virtual method 'java.io.InputStream okhttp3.ResponseBody.byteStream()' on a null object reference at ua.com.merchik.merchik.ServerExchange.PhotoDownload$8.onResponse(PhotoDownload.java:574)

                                if (data.toString().length() > 0) {
                                    Bitmap bmp = BitmapFactory.decodeStream(data);
                                    if (bmp != null) {
                                        exchange.onSuccess(bmp);
                                    } else {
                                        exchange.onFailure("Фото нет");
                                    }
                                } else {
                                    exchange.onFailure("Фото нет");
                                }
                            } else {
                                exchange.onFailure("response.body() == 0");
                            }
                        } else {
                            exchange.onFailure("Код: " + response.code());
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "downloadPhoto/onResponse", "Exception e: " + e);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    exchange.onFailure("Ошибка: " + t);
                }
            });
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "downloadPhoto", "Exception e: " + e);
        }
    }

    public static void savePhotoToDB2(List<ImagesViewListImageList> data) {
        for (ImagesViewListImageList item : data) {
            if (StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(String.valueOf(item.id)) == null) {
                downloadPhoto(item.photoUrl, new ExchangeInterface.ExchangePhoto() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        Globals.writeToMLOG("INFO", "savePhotoToDB2/downloadPhoto/Planogram", "String.valueOf(item.id): " + String.valueOf(item.id));
                        try {
                            StackPhotoDB photoDB = new StackPhotoDB();
                            photoDB.setId(RealmManager.stackPhotoGetLastId() + 1);
                            photoDB.setPhotoServerId(String.valueOf(item.id));
                            photoDB.setDt(item.dt);
                            photoDB.setClient_id(item.clientId);
                            photoDB.setAddr_id(item.addrId);
                            photoDB.setUser_id(item.merchikId);
                            photoDB.setPhoto_type(item.photoTp);

                            photoDB.setCreate_time(item.dt * 1000);// реквизиты что б фотки не выгружались обратно на сервер
                            photoDB.setUpload_to_server(item.dt);// реквизиты что б фотки не выгружались обратно на сервер
                            photoDB.setGet_on_server(item.dt);// реквизиты что б фотки не выгружались обратно на сервер

                            photoDB.setPhoto_num(Globals.savePhotoToPhoneMemory("/Planogram", "" + item.id, bitmap));
                            photoDB.setApprove(item.approve);

                            photoDB.setDvi(item.dvi);
                            photoDB.setPhotoServerURL(item.photoUrl);

//                        Globals.writeToMLOG("INFO", "savePhotoToDB2/downloadPhoto/Planogram", "photoDB: " + new Gson().toJson(photoDB));

                            RealmManager.stackPhotoSavePhoto(photoDB);
                        } catch (Exception e) {
                            Globals.writeToMLOG("ERR", "savePhotoToDB2/downloadPhoto/Planogram", "Exception e: " + e);
                        }

                    }

                    @Override
                    public void onFailure(String error) {
                        Globals.writeToMLOG("ERR", "savePhotoToDB2/downloadPhoto/Planogram", "error: " + error);
                    }
                });
            }
        }

    }


}
