package ua.com.merchik.merchik.retrofit;

import android.os.Build;
import android.util.Log;

import com.chuckerteam.chucker.api.ChuckerInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.ByteString;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.com.merchik.merchik.Activities.MyApplication;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.WebSocketData.Selector;
import ua.com.merchik.merchik.data.WebSocketData.WebSocketData;
import ua.com.merchik.merchik.data.WebSocketData.WebsocketParam;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class RetrofitBuilder {

    private static RetrofitBuilder INSTANCE = new RetrofitBuilder();
    private static final String BASE_URL = "https://merchik.net/";  // android 8+
    private static final String BASE_URL_OLD = "http://merchik.net/";   // old android

    // test
//    private static final String BASE_URL_OLD = "http://merchik.net/";
//    private static final String BASE_URL = "https://merchik.com.ua/";
//    private static final String BASE_URL = "http://merchik.alien/";merchik.com.ua/matest.php

    public static String contentType = "application/json";

    private MyCookieJar cookie = new MyCookieJar();

    private RetrofitInterface interfaceAPI;

    private static boolean serverStatus;//todo add int interConnection
    private static long serverTime;

    private RetrofitBuilder() {
        // было 29.11.23.
//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .setDateFormat("yyyy-MM-dd")
//                .create();

        // стало
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd");

        // Регистрация пользовательского адаптера для Date
        gsonBuilder.registerTypeAdapter(Date.class, new CustomDateTypeAdapter());

        Gson gson = gsonBuilder.create();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Логирует тела запросов и ответов

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(new TimeoutInterceptor());
        httpClientBuilder.addInterceptor(new ChuckerInterceptor(MyApplication.getAppContext()));

        httpClientBuilder.addInterceptor(loggingInterceptor);

        httpClientBuilder.cookieJar(cookie)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);

        OkHttpClient client = httpClientBuilder.build();

        String url = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            url = BASE_URL;
        }else {
            url = BASE_URL_OLD;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(client)
                .build();

        interfaceAPI = retrofit.create(RetrofitInterface.class);
    }


    public static WebSocket webSocket() {
        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        Request request = new Request.Builder()
                .url("ws.merchik.com.ua")
                .build();

        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                Log.e("MYwebSocket", "onOpenresponse: " + response);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                Log.e("MYwebSocket", "onMessage.text: " + text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                Log.e("MYwebSocket", "onMessage.bytes: " + bytes);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                Log.e("MYwebSocket", "onClosing.reason: " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                Log.e("MYwebSocket", "onClosed.code: " + code);
                Log.e("MYwebSocket", "onClosed.reason: " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                Log.e("MYwebSocket", "onFailure.response: " + response);
            }
        });

        return webSocket;
    }

    public static WebSocket startWebSocket(Clicks.click click) {
        WebSocket webSocket;
        OkHttpClient client;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        client = builder.build();
        Request request = new Request.Builder()
                .url("wss://ws.merchik.com.ua")
                .build();
        Log.i("WebSockets", "Headers: " + request.headers().toString());

        if (Globals.userId == 0) {
            AppUsersDB appUsersDB = RealmManager.getAppUser();
            if (appUsersDB != null) {
                Globals.userId = appUsersDB.getUserId();
                Globals.userOwnership = appUsersDB.user_work_plan_status.equals("our");
            }
        }

        Globals.writeToMLOG("INFO", "WebSocket/Headers", "Headers: " + request.headers().toString());

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            private static final int NORMAL_CLOSURE_STATUS = 1000;

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                WebsocketParam websocketParam = new WebsocketParam();
                websocketParam.act = "auth";
                websocketParam.mod = "auth";
                websocketParam.userId = Globals.userId;
                websocketParam.token = Globals.token;

                Selector selector = new Selector();
                selector.platformId = 5;

                websocketParam.selector = selector;

                JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(websocketParam), JsonObject.class);

                Globals.writeToMLOG("INFO", "WebSocket/onOpen/convertedObject", "convertedObject: " + convertedObject);

                String str1 = convertedObject.toString() + "\u0001";
                Log.i("WebSockets", "Connection str1: " + str1);
                webSocket.send(str1); // Добавили спецсимвол - разделитель, что б сервер нормально воспринимал меня.

                Globals.writeToMLOG("INFO", "WebSocket/onOpen/Connection accepted!", "Connection accepted!");
                Log.i("WebSockets", "Connection accepted!");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.i("WebSockets", "Receiving : " + text);
                Globals.writeToMLOG("INFO", "WebSocket/onMessage/String", "Receiving: " + text);

                JsonObject convertedObject = new Gson().fromJson(text, JsonObject.class);

                WebSocketData data = new Gson().fromJson(new Gson().toJson(convertedObject), WebSocketData.class);
                click.click(data);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.i("WebSockets", "Receiving bytes : " + bytes.hex());
                Globals.writeToMLOG("INFO", "WebSocket/onMessage/ByteString", "Receiving bytes : " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(NORMAL_CLOSURE_STATUS, null);
                Log.i("WebSockets", "Closing : " + code + " / " + reason);
                Globals.writeToMLOG("INFO", "WebSocket/onClosing", "Closing : " + code + " / " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.i("WebSockets", "Error : " + t.getMessage());
                Globals.writeToMLOG("INFO", "WebSocket/onFailure/Throwable", "Throwable t: " + t.getMessage());
                if (response != null && response.body() != null) {
                    Globals.writeToMLOG("INFO", "WebSocket/onFailure/Response", "Response response: " + response.body());
                }
                click.click("error");
            }
        });
        return webSocket;
    }

    public static RetrofitInterface getRetrofitInterface() {
        return INSTANCE.interfaceAPI;
    }


    public static RetrofitInterface getRetrofitInterfaceUploadPhoto() {
        return INSTANCE.interfaceAPI;
    }

    //==============================================================================================

    public static void setServerStatusUI(boolean status) {

        serverStatus = status;
    }

    public static boolean getServerStatusUI() {
        return serverStatus;
    }

    public static void setServerTime(long time) {
        serverTime = time * 1000;
    }

    public static long getServerTime() {
        return serverTime;
    }




    /*
     * 17.03.2021
     * Конвертация POJO в JSON для дальнейшей отправки на сервер
     * */
//    new Gson().fromJson(new Gson().toJson(data), JsonObject.class);


}
