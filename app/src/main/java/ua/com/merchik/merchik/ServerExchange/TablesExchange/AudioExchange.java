package ua.com.merchik.merchik.ServerExchange.TablesExchange;


import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class AudioExchange {

    public void import_test() {

        StandartData data = new StandartData();
        data.mod = "audio";
        data.act = "list";
        data.dt_change_from = Clock.today;
        data.dt_change_to = Clock.tomorrow;


        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);


        RetrofitBuilder.getRetrofitInterface()
                .TEST_JSON_UPLOAD_RX(RetrofitBuilder.contentType, convertedObject)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(jsonObject ->{
                    Log.e("RESULT", ">> " + jsonObject);
                }, throwable -> {
                    Log.e("Error","error: " +throwable.getMessage());
                });


    }

    public void export_test(Context context) {

        StandartData data = new StandartData();
        data.mod = "audio";
        data.act = "save";

        AudioReportUpload audioData = new AudioReportUpload();
        audioData.client_id = "123";
        audioData.addr_id = "456";
        audioData.date = "2025-07-09";
        audioData.theme_id = "22";
        audioData.comment = "тестовий аудіозвіт";
        audioData.contacter_id = "789";
        audioData.doc_num = "DOC-001";
        audioData.doc_id = 1;
        audioData.code_dad2 = "3456";
        audioData.tel = "+380991112233";
        List<AudioReportUpload> list = new ArrayList<>();
        list.add(audioData);
        data.data = list;

//        Gson gson = new Gson();
//        String json = gson.toJson(data);
//        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
//
//        File tempAudioFile = new File(context.getCacheDir(), "test.mp3");
//        try (InputStream inputStream = context.getResources().openRawResource(R.raw.test);
//             FileOutputStream outputStream = new FileOutputStream(tempAudioFile)) {
//            byte[] buffer = new byte[4096];
//            int bytesRead;
//            while ((bytesRead = inputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, bytesRead);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        RequestBody audioRequestBody = RequestBody.create(tempAudioFile, MediaType.parse("audio/mpeg"));
//        MultipartBody.Part audioPart = MultipartBody.Part.createFormData("upload_audio[]", tempAudioFile.getName(), audioRequestBody);
//        List<MultipartBody.Part> audioList = Collections.singletonList(audioPart);

        Gson gson = new Gson();

        JsonObject rootObject = new JsonObject();

// Добавляем основные поля
        rootObject.addProperty("act", "save");
        rootObject.addProperty("mod", "audio");

// Берем первый элемент из data (предполагаем, что он один)
        AudioReportUpload audioDat = list.get(0);

// Добавляем все поля из audioData в корень
        rootObject.addProperty("addr_id", audioDat.addr_id);
        rootObject.addProperty("client_id", audioDat.client_id);
        rootObject.addProperty("code_dad2", audioDat.code_dad2);
        rootObject.addProperty("comment", audioDat.comment);
        rootObject.addProperty("contacter_id", audioDat.contacter_id);
        rootObject.addProperty("date", audioDat.date);
        rootObject.addProperty("doc_id", audioDat.doc_id);
        rootObject.addProperty("doc_num", audioDat.doc_num);
        rootObject.addProperty("tel", audioDat.tel);
        rootObject.addProperty("theme_id", audioDat.theme_id);

// Теперь rootObject содержит все поля в корне
        String json = new Gson().toJson(rootObject);
//        String json = gson.toJson(data);

// Создаем MultipartBody.Part для JSON данных
        RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));
        MultipartBody.Part jsonPart = MultipartBody.Part.createFormData("json_data", null, jsonBody);

// Подготовка аудио файла
        File tempAudioFile = new File(context.getCacheDir(), "test.mp3");
        try (InputStream inputStream = context.getResources().openRawResource(R.raw.test);
             FileOutputStream outputStream = new FileOutputStream(tempAudioFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        RequestBody audioRequestBody = RequestBody.create(tempAudioFile, MediaType.parse("audio/mpeg"));
        MultipartBody.Part audioPart = MultipartBody.Part.createFormData("upload_audio[]", tempAudioFile.getName(), audioRequestBody);

// Собираем все части вместе
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(jsonPart)
                .addPart(audioPart);

        RetrofitBuilder.getRetrofitInterface()
                .TEST_AUDIO_UPLOAD_RX(RetrofitBuilder.contentType,
//                        "audio",
//                        "save",
                        builder.build().parts()
                        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(jsonObject ->{
                    Log.e("RESULT", ">> " + jsonObject);
                }, throwable -> {
                    Log.e("Error","error: " +throwable.getMessage());
                });
    }


    private RequestBody createPartFromString(String value) {
        return RequestBody.create(value, MultipartBody.FORM);
    }

    public class AudioReportUpload {
        public String client_id;
        public String addr_id;
        public String date;
        public String theme_id;
        public String comment;
        public String contacter_id;
        public String doc_num;
        public int doc_id;
        public String code_dad2;
        public String tel;
    }

}
