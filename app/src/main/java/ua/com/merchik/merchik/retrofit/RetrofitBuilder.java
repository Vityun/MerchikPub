package ua.com.merchik.merchik.retrofit;

import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();


    private RetrofitBuilder() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd")
                .create();


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
