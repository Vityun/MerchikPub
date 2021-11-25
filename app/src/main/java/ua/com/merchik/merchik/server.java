package ua.com.merchik.merchik;


import android.content.Context;
import android.util.Log;

import ua.com.merchik.merchik.data.RetrofitResponse.Login;
import ua.com.merchik.merchik.data.RetrofitResponse.ServerConnection;
import ua.com.merchik.merchik.data.ServerLogin.SessionCheck;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;


public class server{




    /**
     * Создает тело запроса на основе полученных значений HashMap.
     *
     * @return FormBody для отправки на сервер
     * */
/*    static RequestBody createFormBody(HashMap<String, String> params){

        FormBody.Builder formBuilder = new FormBody.Builder();

        for ( Map.Entry<String, String> entry : params.entrySet() ) {
            formBuilder.add( entry.getKey(), entry.getValue() );
        }

        return formBuilder.build();
    }*/


    /**
     * Используется для перевода кодов в читабельное для человека состояние
     *
     * Коды: Клиент, Адрес, Тип Товара, Тип Фото
     * */
/*
    static String[] customerAddressInformation(String customer, String address, String customerTovGrp, String photoType){

        JsonObject JSON = null;
        JsonObject JSONCoordinates = null;
        String[] customerAndAddress = new String[6];    // Хранится расшифрованное: (client_id)(addr_id)(client_tovar_group)(images_type_list) + (latitude)(longitude)

        //http://DOMEN/mobile_app.php?mod=filter_list&act=menu_list&client_id=03693&addr_id=22332&only_selected=1
        HashMap<String, String> params = new HashMap<>();
        params.put("mod",  "filter_list" );
        params.put("act",  "menu_list");
        params.put("client_id", customer);
        params.put("addr_id", "");
        params.put("client_tovar_group", "");
        params.put("images_type_list", "");
//        params.put("only_selected", "1");

        RequestBody formBody = createFormBody(params);
        String sJSON = null;//ServerPOST("mobile_app.php", formBody);

        if(sJSON != null && !sJSON.equals("")) {
            JSON = new JsonParser().parse(sJSON).getAsJsonObject();
        }
//---
        HashMap<String, String> params1 = new HashMap<>();
        params1.put("mod",  "filter_list" );
        params1.put("act",  "menu_list");
        params1.put("addr_id", address);

        RequestBody formBody1 = createFormBody(params1);
        String sJSONCoordinates = null;//ServerPOST("mobile_app.php", formBody1);

        if(sJSONCoordinates != null && !sJSONCoordinates.equals("")) {
            JSONCoordinates = new JsonParser().parse(sJSONCoordinates).getAsJsonObject();
        }

        Log.e("TAG_TXT_INFO", "JSON: " + JSON);


        if (!(JSON == null)) {

            if (!JSON.getAsJsonObject("menu_list").getAsJsonArray("client_id").isJsonNull()) {
                JsonArray arr = JSON.getAsJsonObject("menu_list").getAsJsonArray("client_id");
                for (int i = 0; i < arr.size(); i++) {
                    if (arr.get(i).getAsJsonObject().get("id").getAsString().equals(customer)) {
                        customerAndAddress[0] = arr.get(i).getAsJsonObject().get("nm").getAsString();
                    }
                }
            }else{
                customerAndAddress[0] = "";
            }

            if (!JSON.getAsJsonObject("menu_list").getAsJsonArray("addr_id").isJsonNull()) {
                JsonArray arr1 = JSON.getAsJsonObject("menu_list").getAsJsonArray("addr_id");
                for (int i = 0; i < arr1.size(); i++) {
                    if (arr1.get(i).getAsJsonObject().get("id").getAsString().equals(address)) {
                        customerAndAddress[1] = arr1.get(i).getAsJsonObject().get("nm").getAsString();
                    }
                }
            }else {
                customerAndAddress[1] = "";
            }

            if (!JSON.getAsJsonObject("menu_list").getAsJsonArray("client_tovar_group").isJsonNull()) {
                JsonArray arr2 = JSON.getAsJsonObject("menu_list").getAsJsonArray("client_tovar_group");
                for (int i = 0; i < arr2.size(); i++) {
                    if (arr2.get(i).getAsJsonObject().get("id").getAsString().equals(customerTovGrp)) {
                        customerAndAddress[2] = arr2.get(i).getAsJsonObject().get("nm").getAsString();
                    }
                }
            }else{
                customerAndAddress[2] = "";
            }

            if (!JSON.getAsJsonObject("menu_list").getAsJsonArray("images_type_list").isJsonNull()) {
                JsonArray arr3 = JSON.getAsJsonObject("menu_list").getAsJsonArray("images_type_list");
                for (int i = 0; i < arr3.size(); i++) {
                    if (arr3.get(i).getAsJsonObject().get("id").getAsString().equals(photoType)) {
                        customerAndAddress[3] = arr3.get(i).getAsJsonObject().get("nm").getAsString();
                    }
                }
            }else {
                customerAndAddress[3] = "";
            }

            if (!(JSONCoordinates == null)){
                if (!JSONCoordinates.getAsJsonObject("menu_list").getAsJsonArray("addr_id").isJsonNull()) {
                    JsonArray arr = JSONCoordinates.getAsJsonObject("menu_list").getAsJsonArray("addr_id");
                    for (int i = 0; i < arr.size(); i++) {
                        if (arr.get(i).getAsJsonObject().get("id").getAsString().equals(address)) {
                            customerAndAddress[4] = arr.get(i).getAsJsonObject().get("lat").getAsString();
                            customerAndAddress[5] = arr.get(i).getAsJsonObject().get("lon").getAsString();
                        }
                    }
                }else{
                    customerAndAddress[4] = "";
                    customerAndAddress[5] = "";
                }
            }

        }

        return customerAndAddress;
    }
*/


    public static boolean test;
    public static boolean serverIsOn(){
        String mod = "ping";
        long unixTimeToServer = System.currentTimeMillis();

        retrofit2.Call<ServerConnection> call = RetrofitBuilder.getRetrofitInterface().takeState(mod, unixTimeToServer);
        call.enqueue(new retrofit2.Callback<ServerConnection>() {
            @Override
            public void onResponse(retrofit2.Call<ServerConnection> call, retrofit2.Response<ServerConnection> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if(response.body().getState()){
                        test = response.body().getState();
                        RetrofitBuilder.setServerStatusUI(response.body().getState());
                        RetrofitBuilder.setServerTime(response.body().getServer_time());
                        Globals.serverGetTime = System.currentTimeMillis();
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ServerConnection> call, Throwable t) {
                test = false;
                RetrofitBuilder.setServerStatusUI(false);
                RetrofitBuilder.setServerTime(0);
            }
        });

        System.out.println("RETROFIT_RESP_RES: " + test);
        return test;
    }




    /**
     * Тестируется пингами сеть и возвращается цвет сети
     *
     * (1)Зелёный - Есть интернет и сервер
     * (2)Жёлтый - Есть интернет, сервера нет
     * (3)Красный - Нет интернета
     *
     * */
    public static int internetStatus(){
        if (serverIsOn()){
            return 1;
        }else{
            return 2;
        }
    }



    //==============================================================================================






    // На снос. Переписать.
    public boolean loginToOnline(String l, String p){
/*        String inputStram;
        // Попытка залогиниться
        RequestBody fB = new FormBody.Builder()
                .add("mod", "auth")
                .add("act", "sotr_auth")
                .add("username", l)
                .add("password", p)
                .build();

        inputStram = ServerPOST("mobile_app.php", fB);

        if (inputStram == null || inputStram.equals("")) {
            return false;
        }

        JsonObject login_output = new JsonParser().parse(inputStram).getAsJsonObject();     // Получаем JSON-ответ на логин
        System.out.println("ЛОГИНИМСЯ В ОФЛАЙНЕ (ответ сервера): " + login_output);
        if (!login_output.get("state").getAsBoolean()){ // Вывод ошибки, если пришла с сервера
            String error = login_output.get("error").getAsString();
            return false;
        }else return login_output.get("state").getAsBoolean();*/
        return true;
    }




    /**
     * 15.07.2020
     * Проверка "протухлости" сессии и логин. если сессия протухла
     *
     * */
    public static void sessionCheckAndLogin(Context context, String login, String password){
        String modAuth = "auth";

        Log.e("sessionCheckAndLogin", "Проверка онлайн я или нет." + login + password);

        // проверка "протухлости" сессии
        retrofit2.Call<SessionCheck> call = RetrofitBuilder.getRetrofitInterface().CHECK_SESSION(modAuth, Globals.getAppInfoToSession(context));
        call.enqueue(new retrofit2.Callback<SessionCheck>() {
            @Override
            public void onResponse(retrofit2.Call<SessionCheck> call, retrofit2.Response<SessionCheck> response) {

                if (response.isSuccessful() && response.body() != null) {
                    SessionCheck resp = response.body();
                    Log.e("sessionCheckAndLogin", "AUTH: " + resp.getAuth());

                    if (resp.getState()){
                        if (!resp.getAuth()){   // Если сессия протухла - логинимся
                            Log.e("sessionCheckAndLogin", "Сессия протухла - я пробую выполнить вход.");
                            Globals.onlineStatus = false;
                            loginOnServer(context, login, password);
                        }else {
                            Globals.onlineStatus = true;
                            Log.e("sessionCheckAndLogin", "Сессия нормальная - работаем дальше" + " /Кто залогинен, если все ок:" + resp.getUserInfo().getFio());
                        }
                    }else {
                        Globals.onlineStatus = false;
                        Log.e("sessionCheckAndLogin", "State = false");
                    }
                }else {
                    Globals.onlineStatus = false;
                }
            }

            @Override
            public void onFailure(retrofit2.Call<SessionCheck> call, Throwable t) {
                Globals.onlineStatus = false;
                Log.e("sessionCheckAndLogin", "FAILURE: " + t);
            }
        });

    }


    /**
     * 15.07.2020
     *
     * Ретрфитовский логин на сервере.
     *
     * Передаю логин и пароль для этого.
     * */
    public static void loginOnServer(Context context, String login, String password){
        String mod = "auth";
        String act = "sotr_auth";

        Log.e("loginOnServer", "LOGIN: " + login + " pass: " + password);

        retrofit2.Call<Login> callLogin = RetrofitBuilder.getRetrofitInterface().LOGIN(mod, act, login, password, Globals.getAppInfoToSession(context));
        callLogin.enqueue(new retrofit2.Callback<Login>() {
            @Override
            public void onResponse(retrofit2.Call<Login> callLogin, retrofit2.Response<Login> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Login resp = response.body();

                    // Разбираем ответ на логин
                    Log.e("loginOnServer", "LOGIN STATE: " + resp.getState());
                    if (resp.getState()){
                        Globals.onlineStatus = true;
                    }else {
                        Globals.onlineStatus = false;
                    }
                }else {
                    Globals.onlineStatus = false;
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Login> callLogin, Throwable t) {
                Log.e("loginOnServer", "FAILURE: " + t);
                Globals.onlineStatus = false;
            }
        });

    }





}//END CLASS..


// для больших строк
/*int maxLogSize = 1000;
for(int i = 0; i <= veryLongString.length() / maxLogSize; i++) {
    int start = i * maxLogSize;
    int end = (i+1) * maxLogSize;
    end = end > veryLongString.length() ? veryLongString.length() : end;
    Log.v(TAG, veryLongString.substring(start, end));
}*/