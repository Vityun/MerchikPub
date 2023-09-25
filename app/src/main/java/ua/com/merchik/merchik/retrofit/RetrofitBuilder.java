package ua.com.merchik.merchik.retrofit;

import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.WebSocketData.Selector;
import ua.com.merchik.merchik.data.WebSocketData.WebSocketData;
import ua.com.merchik.merchik.data.WebSocketData.WebsocketParam;
import ua.com.merchik.merchik.database.realm.RealmManager;
import okhttp3.logging.HttpLoggingInterceptor;

public class RetrofitBuilder{

    private static RetrofitBuilder INSTANCE = new RetrofitBuilder();
    private static final String BASE_URL = "https://merchik.net/";
    private static final String BASE_URL_OLD = "http://merchik.net/";
//    private static final String BASE_URL = "https://merchik.com.ua/";
//    private static final String BASE_URL = "http://merchik.alien/";merchik.com.ua/matest.php

    public static String contentType = "application/json";

    private MyCookieJar cookie = new MyCookieJar();

    private RetrofitInterface interfaceAPI;

    private static boolean serverStatus;//todo add int interConnection
    private static long serverTime;


//    private  CertificatePinner certificatePinner = new CertificatePinner.Builder()
//            .add("merchik.net", "sha256/qWqcoj9jnvn6/bXwFtZ64WLeikP+sikpS5z8HecfrKQ=")
//            .add("merchik.net", "sha256/YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg=")
//            .add("merchik.net", "sha256/Vjs8r4z+80wjNcr1YKepWQboSIRi63WsWXhIMN+eWys=")
//            .build();

    private OkHttpClient client = new OkHttpClient.Builder()
//            .certificatePinner(certificatePinner)
            .cookieJar(cookie)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
//            .readTimeout(40, TimeUnit.SECONDS)
//            .writeTimeout(30, TimeUnit.SECONDS)

//            .connectTimeout(5, TimeUnit.SECONDS)
//            .readTimeout(5, TimeUnit.SECONDS)
//            .writeTimeout(5, TimeUnit.SECONDS)
            .build();

    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
        @Override
        public void log(String message) {
            Log.e("M_UPLOAD_GALLERY", "RETROFIT: " + truncateString(message, 200));
            Globals.writeToMLOG("INFO", "HttpLoggingInterceptor", "RETROFIT: " + truncateString(message, 200));
        }
    });

    private static String truncateString(String string, int maxLength) {
        if (string.length() <= maxLength) {
            return string;
        } else {
            return string.substring(0, maxLength);
        }
    }




    private RetrofitBuilder() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd")
                .create();

//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
//        httpClientBuilder.addInterceptor(loggingInterceptor);
        httpClientBuilder.cookieJar(cookie)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        OkHttpClient client = httpClientBuilder.build();


        Retrofit retrofit;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }else {
//            retrofit = new Retrofit.Builder()
//                    .baseUrl(BASE_URL_OLD)
//                    .addConverterFactory(GsonConverterFactory.create(gson))
//                    .client(client)
//                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }

        interfaceAPI = retrofit.create(RetrofitInterface.class);
    }



    public static WebSocket webSocket(){
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

    public static WebSocket startWebSocket(Clicks.click click){
        WebSocket webSocket;
        OkHttpClient client;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        client = builder.build();
        Request request = new Request.Builder()
                .url("wss://ws.merchik.com.ua")
                .build();
        Log.i("WebSockets", "Headers: " + request.headers().toString());

        if (Globals.userId == 0){
            AppUsersDB appUsersDB = RealmManager.getAppUser();
            if (appUsersDB != null){
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

                webSocket.send(convertedObject.toString());

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
                if (response != null && response.body() != null){
                    Globals.writeToMLOG("INFO", "WebSocket/onFailure/Response", "Response response: " + response.body());
                }
                click.click("error");
            }});
        return webSocket;
    }

    public static RetrofitInterface getRetrofitInterface(){
        return INSTANCE.interfaceAPI;
    }


    public static RetrofitInterface getRetrofitInterfaceUploadPhoto(){
        return INSTANCE.interfaceAPI;
    }

    //==============================================================================================

    public static void setServerStatusUI(boolean status){

        serverStatus = status;
    }

    public static boolean getServerStatusUI(){
        return serverStatus;
    }

    public static void setServerTime(long time){
        serverTime = time*1000;
    }

    public static long getServerTime(){
        return serverTime;
    }




    /*
     * 17.03.2021
     * Конвертация POJO в JSON для дальнейшей отправки на сервер
     * */
//    new Gson().fromJson(new Gson().toJson(data), JsonObject.class);





}
