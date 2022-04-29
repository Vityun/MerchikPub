package ua.com.merchik.merchik;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import androidx.exifinterface.media.ExifInterface;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLog;
import ua.com.merchik.merchik.Activities.WorkPlanActivity.WPDataActivity;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.data.AppData.AppData;
import ua.com.merchik.merchik.data.AppData.Browser;
import ua.com.merchik.merchik.data.AppData.Device;
import ua.com.merchik.merchik.data.AppData.Os;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RetrofitResponse.PhotoHash;
import ua.com.merchik.merchik.data.RetrofitResponse.PhotoHashList;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class menu_main extends AppCompatActivity {
    Context mContext = this;



//    server server = new server();
    VersionApp VersionApp = new VersionApp();
    Globals globals = new Globals();
    URL URL = new URL();// Класс для преобразования HashMap-ов в URLData строку.
    private Menu menu;
    File image; //Имя фотки
    File photoToDB; // Буфер для сохранения фото в БД (был баг где записывается не та фотка)
    JsonObject JSON;
    Uri photo_uri_test;

    ProgressDialog progressDialogWPData = null;

    TextView activity_title;
    TextView textView_comment, textViewInfoCustomerAddress;
    TextView textCartItemCount; // Текст иконки кол-ва фоток в toolbar
    Button button_photo, button_comment, button_make_photo, button_close;
    ImageView imageView, imageViewMerch;

    long delayGPS, delayNET, unixTime = System.currentTimeMillis();
    boolean logFromOffline; // Залогинились ли мы или нет
    boolean exec;   // Решает - выгружатьфото или нет. Присваевается в зависимости от наличия данных к фото пришедших от Web
    double lat, lon;    // Координаты магазина

    int distanceMin=500, wp_data_id;
    int internetStatus;
    float distanceAB;
    private long mLastClickTime = 0; // Время нажатия на кнопку Сделать фото
    private long mLastClickTime2 = 0; // Время нажатия на кнопку Сделать ещё фото

    String mCurrentPhotoPath, testPhotoPath; // Путь к фотографии
    String comment = "";
    String exifInfo; // EXIF-информация о фотографии
    String currentDate, currentDatePlusOneDay;

//    private String dateMinus1, dateCurrent, datePlus1;

    String date, customer_id, address_id, photo_type, customerTypeGrpURI, customerTypeGrp, doc_num, theme_id, photo_user_id;
    Map<Integer, String> customerTypeGrpArr;
    String customer_id_txt = null, address_id_txt = null, customerTypeGrp_txt, photo_type_txt;
    String[] customrAndAdress;  // Хранятся переведенные id елементов в человеческий язык
    String GP;  // base64 данные для отправки на сервер
    String lastGPSData, lastGPSTime, lastNETData, lastNETTime;
    String measure = "м";
    String user_id, login, password; // Переменные между активностями
    String inputStream = "", inputStreamPhotoList = "";
    String tableName;


//    String[] result;

    Map<Integer, String> mapSpinner = new HashMap<>();
    Map<Integer, String> mapCustomerType = new HashMap<>();

    // Нужны для работы метода Coordinates()
    double CoordX, CoordY, CoordAltitude;
    long CoordTime;
    long dad2;
    float CoordSpeed, CoordAccuracy;
    int mocking;

    private static final int CAMERA_REQUEST = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    private static final int PERMISSION_REQUEST = 1;
    private static final long MIN_CLICK_INTERVAL=1000;

    // ---------------------------------------------------------------------------------------------



    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (trecker.switchedOff) {
            trecker.SetUpLocationListener(this);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_main);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title = (TextView) findViewById(R.id.activity_title);
        activity_title.setText(getResources().getString(R.string.activity_title_photo_report));

        testApiData();

        //----- Установка Дата и Дата+1 день -----
//        setDate();

        Log.e("updateDataBase", "2");

        currentDate = Clock.yesterday;
        currentDatePlusOneDay = Clock.tomorrow;
        //----------------------------------------

        // Установка портретной ориентации (ибо фотки на сервер загружались не правильно)
//        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final Handler handler1 = new Handler();
        handler1.removeCallbacksAndMessages(null);

        //-----     Проверка версии     -----
        checkAPPVer();
        //-----------------------------------

        //----------    "Кроны"    ----------)
        globals.handlerCount.postDelayed(runnableCron10, 100);
        //-----------------------------------

        button_photo = findViewById(R.id.button_photo);
        button_make_photo = findViewById(R.id.button_make_photo);
        button_comment = findViewById(R.id.button_comment);
        button_close = findViewById(R.id.button_close);
        imageViewMerch = findViewById(R.id.imageViewMerchik2);imageViewMerch.setAlpha(0.8f); // Лого и его прозрачность
        imageView = findViewById(R.id.imageView);
        textView_comment = findViewById(R.id.textView_comment);
        textViewInfoCustomerAddress = findViewById(R.id.textInfo);


        wp_data_id = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt("wp_data_id", 0);

        user_id = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("user_id", null);

        login = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("login", null);

        password = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("password", null);

        Log.e("APP_LOGIN", "MAIN ACT LOGIN: " + login + " pass: " + password);

        logFromOffline = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("logFromOffline", false);

        String UriToParseFromSite = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("UriToParseFromSite", "");


        Log.e("updateDataBase", "3");
        // 18.08.2020
        // Если таблици не обновлялись - предлагает сразу их обновить.
        updateDataBase();

        // URI from site
        if(!UriToParseFromSite.equals("")) {
            exec = true;
            JSON = URL.ParsingURLtoJSON(UriToParseFromSite);
            Log.e("TAG_SITE_URL", "JSON: " + JSON);
            // Боже, не смотри сюда.
            // ДАТА

            if (JSON != null && !JSON.getAsJsonObject("params").isJsonNull()) {
                Log.e("TAG_SITE_URL", "JSON(2): " + JSON);
//                globals.alertDialogMsg("Ответ от сервера: " + JSON, this);

                if (!JSON.getAsJsonObject("params").get("date").isJsonNull()) {
                    date = JSON.getAsJsonObject("params").get("date").getAsString();
                } else {
                    //date = JSON.getAsJsonObject("params").get("date").toString();
                    date = Clock.today;
                }

                // КЛИЕНТ_ID
                if (!JSON.getAsJsonObject("params").get("client_id").isJsonNull()) {
                    customer_id = JSON.getAsJsonObject("params").get("client_id").getAsString();
                } else {
                    //customer_id = JSON.getAsJsonObject("params").get("client_id").toString();
                    customer_id = null;
                }

                // АДРЕС
                if (!JSON.getAsJsonObject("params").get("addr_id").isJsonNull()) {
                    address_id = JSON.getAsJsonObject("params").get("addr_id").getAsString();
                } else {
                    //address_id = JSON.getAsJsonObject("params").get("addr_id").toString();
                    address_id = null;
                }

                // ТИПО ФОТО
                if (!JSON.getAsJsonObject("params").get("img_type_id").isJsonNull()) {
                    photo_type = JSON.getAsJsonObject("params").get("img_type_id").getAsString();
                } else {
                    //photo_type = JSON.getAsJsonObject("params").get("img_type_id").toString();
                    photo_type = null;
                }
                Log.e("TAG_SITE_URL", "photo_type(2): " + photo_type);

                // ГРУППА ТОВАРОВ
                Log.e("MVSAboutPhotoU", "URI : client_tovar_group: " + JSON);
                if (!JSON.getAsJsonObject("params").get("client_tovar_group").isJsonNull()) {
                    customerTypeGrpURI = JSON.getAsJsonObject("params").get("client_tovar_group").getAsString();
                } else {
                    //customerTypeGrp = JSON.getAsJsonObject("params").get("client_tovar_group").toString();
                    customerTypeGrpURI = null;
                }

                // НОМЕР ДОКУМЕНТА
                if (!JSON.getAsJsonObject("params").get("doc_num").isJsonNull()) {
                    doc_num = JSON.getAsJsonObject("params").get("doc_num").getAsString();
                } else {
                    //doc_num = JSON.getAsJsonObject("params").get("doc_num").toString();
                    doc_num = null;
                }

                // ТЕМА
                if (!JSON.getAsJsonObject("params").get("theme_id").isJsonNull()) {
                    theme_id = JSON.getAsJsonObject("params").get("theme_id").getAsString(); // Ver26 Caused by: java.lang.NullPointerException:
                } else {
                    //theme_id = JSON.getAsJsonObject("params").get("theme_id").toString();
                    theme_id = null;
                }

                // photo_user_id
                if (!JSON.getAsJsonObject("params").get("photo_user_id").isJsonNull()) {
                    photo_user_id = JSON.getAsJsonObject("params").get("photo_user_id").getAsString();

                    // Проверка типа "Фото пользователя". Если > 0 - сохранить в БД как пустоту
                    if (photo_user_id.equals("") || Integer.parseInt(photo_user_id) < 0) {
                        photo_user_id = null;
                    }
                } else {
                    photo_user_id = null;
                }

                Log.e("TAG_SITE_URL", "photo_type: " + photo_type);


//                customrAndAdress = server.customerAddressInformation(customer_id, address_id, customerTypeGrpURI, photo_type);


                //----------------
                Log.e("MVSAboutPhotoC", "Client: " + customer_id);

                String mod = "filter_list";
                String act = "menu_list";
                String client_id = customer_id;
                String addr_id = "";
                String client_tovar_group = "";
                String images_type_list = "";
                String only_selected = "";

                retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().MVS_DATA_CLI_JSON(mod, act, client_id, addr_id, client_tovar_group, images_type_list, only_selected);
                call.enqueue(new retrofit2.Callback<JsonObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                        Log.e("MVSAboutPhotoC", "SUCCESS_C: " + response.body());
/*                        String jsonMVS = String.valueOf(response.body());
                        int maxLogSize = 1000;
                        for(int i = 0; i <= jsonMVS.length() / maxLogSize; i++) {
                            int start = i * maxLogSize;
                            int end = (i+1) * maxLogSize;
                            end = end > jsonMVS.length() ? jsonMVS.length() : end;
                            Log.e("MVSAboutPhotoC", jsonMVS.substring(start, end));
                        }*/

                        JsonObject JSON = response.body();
                        try {
                            if (!JSON.getAsJsonObject("menu_list").getAsJsonArray("client_id").isJsonNull()) {
                                JsonArray arr = JSON.getAsJsonObject("menu_list").getAsJsonArray("client_id");
                                for (int i = 0; i < arr.size(); i++) {
                                    if (arr.get(i).getAsJsonObject().get("id").getAsString().equals(customer_id)) {
                                        customer_id_txt = arr.get(i).getAsJsonObject().get("nm").getAsString();
                                    }
                                }
                            }else{
                                customer_id_txt = "не визначений";
                            }
                        }catch (Exception e){
                            customer_id_txt = "не визначений";
                        }


                        try {
                            if (!JSON.getAsJsonObject("menu_list").getAsJsonArray("addr_id").isJsonNull()) {
                                JsonArray arr1 = JSON.getAsJsonObject("menu_list").getAsJsonArray("addr_id");
                                for (int i = 0; i < arr1.size(); i++) {
                                    if (arr1.get(i).getAsJsonObject().get("id").getAsString().equals(address_id)) {
                                        address_id_txt = arr1.get(i).getAsJsonObject().get("nm").getAsString();
                                    }
                                }
                            }else {
                                address_id_txt = "не визначений";
                            }
                        }catch (Exception e){
                            address_id_txt = "не визначений";
                        }


                        Log.e("MVSAboutPhotoC", "customerTypeGrpURI: " + customerTypeGrpURI);
                        try {
                            if (!JSON.getAsJsonObject("menu_list").getAsJsonArray("client_tovar_group").isJsonNull()) {
                                JsonArray arr2 = JSON.getAsJsonObject("menu_list").getAsJsonArray("client_tovar_group");
                                for (int i = 0; i < arr2.size(); i++) {
                                    if (arr2.get(i).getAsJsonObject().get("id").getAsString().equals(customerTypeGrpURI)) {
                                        customerTypeGrp_txt = arr2.get(i).getAsJsonObject().get("nm").getAsString();
                                    }
                                }
                            }else{
                                customerTypeGrp_txt = "не визначений";
                            }
                        }catch (Exception e){
                            customerTypeGrp_txt = "не визначений";
                        }


                        try {
                            if (!JSON.getAsJsonObject("menu_list").getAsJsonArray("images_type_list").isJsonNull()) {
                                JsonArray arr3 = JSON.getAsJsonObject("menu_list").getAsJsonArray("images_type_list");
                                for (int i = 0; i < arr3.size(); i++) {
                                    if (arr3.get(i).getAsJsonObject().get("id").getAsString().equals(photo_type)) {
                                        photo_type_txt = arr3.get(i).getAsJsonObject().get("nm").getAsString();
                                    }
                                }
                            }else {
                                photo_type_txt = "не визначений";
                            }
                        }catch (Exception e){
                            photo_type_txt = "не визначений";
                        }



                        customerTypeGrpArr = new WorkPlan().getCustomerGroups(customer_id);
/*                        if(customrAndAdress[0] == null) {customer_id_txt = "не визначений";}else {customer_id_txt = customrAndAdress[0];}
                        if(customrAndAdress[1] == null) {address_id_txt = "не визначений";}else {address_id_txt = customrAndAdress[1];}
                        if(customrAndAdress[2] == null) {customerTypeGrp_txt = "не визначений";}else {customerTypeGrp_txt = customrAndAdress[2];}
                        if(customrAndAdress[3] == null) {photo_type_txt = "не визначений";}else {photo_type_txt = customrAndAdress[3];}*/


                        Log.e("KPS_FROM_SERV", ""+ date + address_id_txt + customer_id_txt+ customerTypeGrp_txt);
                        textViewInfoCustomerAddress.setText("Дата: " + date + "\n");
                        textViewInfoCustomerAddress.append("Адреса: " + address_id_txt + "\n");
                        textViewInfoCustomerAddress.append("Клієнт: " + customer_id_txt + "\n");
                        textViewInfoCustomerAddress.append("Група товару: " + customerTypeGrp_txt + "\n");
                        Log.e("KPS_FROM_SERV", "" + textViewInfoCustomerAddress.getText());
                    }

                    @Override
                    public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                        Log.e("MVSAboutPhotoC", "FAILURE_C_E: " + t.getMessage());
                        Log.e("MVSAboutPhotoC", "FAILURE_C_E2: " + t);
                    }
                });



                String mod2 = "filter_list";
                String act2 = "menu_list";
                String addr_id2 = address_id;
                Log.e("MVSAboutPhotoA", "address_id: " + address_id);
                retrofit2.Call<JsonObject> call2 = RetrofitBuilder.getRetrofitInterface().MVS_DATA_ADD_JSON(mod2, act2, addr_id2);
                call2.enqueue(new retrofit2.Callback<JsonObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                        Log.e("MVSAboutPhotoA", "SUCCESS_A: " + response.body());
/*                        String jsonMVS = String.valueOf(response.body());
                        int maxLogSize = 1000;
                        for(int i = 0; i <= jsonMVS.length() / maxLogSize; i++) {
                            int start = i * maxLogSize;
                            int end = (i+1) * maxLogSize;
                            end = end > jsonMVS.length() ? jsonMVS.length() : end;
                            Log.e("MVSAboutPhotoA", jsonMVS.substring(start, end));
                        }*/

                        if (!(response.body() == null)){
                            try {
                                if (!response.body().getAsJsonObject("menu_list").getAsJsonArray("addr_id").isJsonNull()) {
                                    JsonArray arr = response.body().getAsJsonObject("menu_list").getAsJsonArray("addr_id");
                                    for (int i = 0; i < arr.size(); i++) {
                                        if (arr.get(i).getAsJsonObject().get("id").getAsString().equals(address_id)) {
                                            lat = Double.parseDouble(arr.get(i).getAsJsonObject().get("lat").getAsString());
                                            lon = Double.parseDouble(arr.get(i).getAsJsonObject().get("lon").getAsString());
                                        }
                                    }
                                }else{
                                    lat = 0;
                                    lon = 0;
                                }
                            }catch (Exception e){
                                lat = 0;
                                lon = 0;
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                        Log.e("MVSAboutPhotoA", "FAILURE_A_E: " + t.getMessage());
                        Log.e("MVSAboutPhotoA", "FAILURE_A_E2: " + t);
                    }
                });



                //----------------

/*                customerTypeGrpArr = new WorkPlan().getCustomerGroups(customer_id);

                if(customrAndAdress[0] == null) {customer_id_txt = "не визначений";}else {customer_id_txt = customrAndAdress[0];}
                if(customrAndAdress[1] == null) {address_id_txt = "не визначений";}else {address_id_txt = customrAndAdress[1];}
                if(customrAndAdress[2] == null) {customerTypeGrp_txt = "не визначений";}else {customerTypeGrp_txt = customrAndAdress[2];}
                if(customrAndAdress[3] == null) {photo_type_txt = "не визначений";}else {photo_type_txt = customrAndAdress[3];}*/

/*                if (customrAndAdress[4] == null && customrAndAdress[5] == null){
                    lat = 0;
                    lon = 0;
                }else{
                    lon = Float.parseFloat(customrAndAdress[5]);
                    lat = Float.parseFloat(customrAndAdress[4]);
                }*/

            }else {
                globals.alertDialogMsg(this, "Данные о посещении с сервера не получены. Обратитесь к своему руководителю или воспользуйтесь Планом работ в приложении. Ответ от сервера: " + JSON);
            }



        } else if (wp_data_id > 0){// WPDATA
            exec = true;
            Intent intent = getIntent();

            if ((WPDataObj) intent.getSerializableExtra("dataFromWPObj") != null) {
                WPDataObj wp = (WPDataObj)intent.getSerializableExtra("dataFromWPObj");

                if (wp != null) {
                    int wpId = wp.getId();
                    date = wp.getDate();
                    customer_id = wp.getCustomerId();
                    address_id = String.valueOf(wp.getAddressId());
                    photo_type = wp.getPhotoType();
                    customerTypeGrpArr = wp.getCustomerTypeGrp();

                    Log.e("TAG_TEST_GRP", "GROUP: " + customerTypeGrpArr);
                    doc_num = wp.getDocNum();
                    theme_id = String.valueOf(wp.getThemeId());
                    photo_user_id = wp.getPhotoUserId();
                    dad2 = wp.getDad2();
                    customer_id_txt = wp.getCustomerIdTxt();
                    address_id_txt = wp.getAddressIdTxt();
                    lat = wp.getLatitude();
                    lon = wp.getLongitude();

//                    globals.alertDialogMsg("КПС: \nID: " + wpId + "\nГруппа: " + customerTypeGrpArr + "\nТема: " + theme_id, this);
                }
            }

            if(customer_id_txt == null || customer_id_txt.equals("")) customer_id_txt = "не визначений";
            if(address_id_txt == null || address_id_txt.equals("")) address_id_txt = "не визначений";
            if (customerTypeGrpArr == null){
                customerTypeGrp_txt = "не визначений";
            }else{
                if (customerTypeGrpArr.size() == 1) customerTypeGrp_txt = customerTypeGrpArr.values().toArray(new String[0])[0];
                else if (customerTypeGrpArr.size() > 1) customerTypeGrp_txt = "" + customerTypeGrpArr.size() + " групп"; else customerTypeGrp_txt = "не визначений";
            }
            photo_type_txt = "Фото витрины";

        }else{
            exec = false;
            globals.uriNonElementMassage = globals.alertMassage("Не найдены реквизиты посещения: Дата, адрес, клиент...\n\nЗакройте это приложение и откройте его снова из детализированного отчёта в мобильной версии сайта.", globals.uriNonElementMassage,this);
        }


        // Заполнение реквизитов
        textViewInfoCustomerAddress.setText("Дата: " + date + "\n");
        textViewInfoCustomerAddress.append("Адреса: " + address_id_txt + "\n");
        textViewInfoCustomerAddress.append("Клієнт: " + customer_id_txt + "\n");
        textViewInfoCustomerAddress.append("Група товару: " + customerTypeGrp_txt + "\n");
        //textViewInfoCustomerAddress.append("Тип фотографії: " + photo_type_txt + "\n"); // ТУт будет нолик

        Spinner sPT = photoTypeSpinner();   // Получаем спиннер Типа фото
        try {
            sPT.setOnItemSelectedListener(new MyOnItemSelectedListener());
        }catch (Exception e){
            //Toast.makeText(menu_main.this, "Err", Toast.LENGTH_LONG).show();
        }


        // Получение ID пользователя
/*        SharedPreferences pref = getApplicationContext().getSharedPreferences("LocationPrefs", MODE_PRIVATE);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            user_id = extras.getString("user_id");
            login = extras.getString("login");
            password = extras.getString("password");

*//*            String user = extras.getString("user_id");
            String log = extras.getString("login");
            String pas = extras.getString("password");

            SharedPreferences.Editor editor = pref.edit();
            editor.putString("user_id", user);
            editor.putString("login", log);
            editor.putString("password", pas);
            editor.apply();*//*
        }

*//*        user_id = pref.getString("user_id", null);
        login = pref.getString("login", null);
        password = pref.getString("password", null);*/

        System.out.println("TEST.USER_ID: " + user_id);

        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("address", address_id_txt).apply();

        // "загружаем" значение Автоотправки
/*        globals.autoSend = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("checkAutoSend", true);*/




    }//--------------------------------------------------------------------- /ON CREATE ---------------------------------------------------------------------

    private void testApiData() {

        String osVerApi = String.valueOf(Build.VERSION.SDK_INT);
        String brovVer = "";
        try {
            brovVer = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            brovVer = brovVer.replaceAll("\\.", "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String getRadioVersion = Build.getRadioVersion();

        Os os = new Os("Android", Build.VERSION.RELEASE, osVerApi);
        Browser browser = new Browser("MerchikApp", brovVer, "mobile_app", "2020-10-08", "1");
        Device device = new Device("smartphone", Build.BRAND, Build.MODEL, getRadioVersion);
        AppData appData = new AppData(os, browser, device);

        Gson gson = new Gson();
        String json = gson.toJson(appData);

        Log.e("testApiData", "appData: " + json);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_API_DATA_OBJ(json);

        Log.e("testApiData", "call: " + call.request().toString());

        call.enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("testApiData", "testApiData: " + response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("testApiData", "testApiData_ERROR: " + t);
            }
        });
    }


//        retrofit2.Call<String> call = RetrofitBuilder.getRetrofitInterface().TEST_API_DATA(osName, osVer, browName, brovVer);

    //        retrofit2.Call<String> call = RetrofitBuilder.getRetrofitInterface().TEST_API_DATA_JSON(appData);



    /** 18.08.2020
     * Диалог который делает невозможным начать работу, если не обновлены таблици.
     * */
    private void updateDataBase() {
/*        try {
            if (RealmManager.getAllWorkPlan().size() == 0){
                TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading();
                AlertDialog.Builder builder = new AlertDialog.Builder(menu_main.this);
                builder.setCancelable(false);
                builder.setTitle("Синхронизация");
                builder.setMessage("Для начала работы нужно провести синхронизацию. Она может занять от одной минуты до нескольких минут.");
                builder.setPositiveButton("Синхронизовать сейчас!", (dialog, which) -> {
                    tablesLoadingUnloading.downloadAllTables(menu_main.this);
                });
                builder.setNegativeButton("X", ((dialog, which) -> Toast.makeText(menu_main.this, "Синхронизация отменена", Toast.LENGTH_SHORT).show()));

                builder.create().show();
            }
        }catch (Exception e){
            // запись в ЛОГ
        }*/


/*        Log.e("updateDataBase", "1");
        RealmResults<SynchronizationTimetableDB> synchronizationTimetable = RealmManager.getSynchronizationTimetable();
        if (synchronizationTimetable != null){
            Log.e("updateDataBase", "ТАБЛИЦА НЕ 0");
            for (SynchronizationTimetableDB row : synchronizationTimetable){
                Log.e("updateDataBase", "МЫ В ЦИКЛЕ");
                if (row != null){
                    Log.e("updateDataBase", "СТРОКА НЕ 0 ");
                    Log.e("updateDataBase", "ВПИ: " + row.getVpi_app() );
                    Log.e("updateDataBase", "ВПИ(2): " + row.getVpi_server());
                    if (row.getVpi_app() == 0 && row.getVpi_server() == 0){
                        Log.e("updateDataBase", "НАДО ОБНОВЛЯТЬ");
                        // Вызываем диалог который заставит обновится
                        TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading();
                        AlertDialog.Builder builder = new AlertDialog.Builder(menu_main.this);
                        builder.setCancelable(false);
                        builder.setTitle("Синхронизация");
                        builder.setMessage("Для начала работы нужно провести синхронизацию. Она может занять от одной минуты до нескольких минут.");
                        builder.setPositiveButton("Синхронизовать сейчас!", (dialog, which) -> {
                            tablesLoadingUnloading.downloadAllTables(menu_main.this);
                        });

                        builder.create().show();
                        Log.e("updateDataBase", "СОЗДАТЬ");
                    }

                    break;
                }else{
                    // Запись в Лог
                    Log.e("updateDataBase", "СТРОКА ПУСТАЯ");
                }
            }
        }else {
            // Запись в Лог
            Log.e("updateDataBase", "ТАБЛИЦА ПУСТАЯ");
        }*/

    }

    @Override
    protected void onResume() {
        globals.handlerCount.removeCallbacks(runnableCron10);
        globals.handlerCount.postDelayed(runnableCron10, 100);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        globals.handlerCount.removeCallbacks(runnableCron10);
    }

    @Override
    protected void onStop() {
        super.onStop();
        globals.handlerCount.removeCallbacks(runnableCron10);
    }

/*    @Override
    protected void onDestroy() {
        super.onDestroy();
        globals.handlerCount.removeCallbacks(runnableCron10);
    }*/




    // Заполняем спинер типов фото
    private Spinner photoTypeSpinner(){
        Spinner s = findViewById(R.id.spinnerPhotoType);
        try {
            RealmResults<ImagesTypeListDB> imagesTypeList = RealmManager.getAllImagesTypeList();
            for (int i=0; i<imagesTypeList.size(); i++){
                if (imagesTypeList.get(i).getNm() != null && !imagesTypeList.get(i).getNm().equals("")){
                    mapSpinner.put(imagesTypeList.get(i).getId(), imagesTypeList.get(i).getNm());
                }
            }

            String[] result = mapSpinner.values().toArray(new String[0]);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, result);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            s.setAdapter(adapter);

            // Выставляю значение по умолчанию
            try {
                int spinnerPosition = adapter.getPosition(mapSpinner.get(Integer.parseInt(photo_type)));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                s.setAdapter(adapter);
                s.setSelection(spinnerPosition);
            }catch (Exception e){

            }


        }catch (Exception e){
            globals.alertDialogMsg(this, "Список типов фото получить не удалось, попробуйте нажать в меню 3х точек на \"Перейти на главную\". Если после этого ошибка повторится - обратитесь к Вашему руководителю." + e);
        }
        return s;
    }


    /**
     * Создание меню в toolbox-е
     *
     * 1. Иконка связи с инетом
     * 2. Троеточие
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        this.menu = menu;

        MenuItem itemExchange = menu.findItem(R.id.exchange);
        itemExchange.setVisible(false);

        MenuItem item = menu.findItem(R.id.action_photo_count);
        item.setVisible(true);
        MenuItemCompat.setActionView(item, R.layout.feed_update_count);
        final MenuItem menuItem = menu.findItem(R.id.action_photo_count);
        View actionView = MenuItemCompat.getActionView(menuItem);
        textCartItemCount = (TextView) actionView.findViewById(R.id.button_lighter_photo_count);
        setupBadge(0);

        MenuItem autoSendCheck = menu.findItem(R.id.action_autosend);
        autoSendCheck.setChecked(Globals.autoSend);

        return true;
    }


    void setupBadge(int countPhoto) {
        if (textCartItemCount != null) {
            if (countPhoto == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {
                textCartItemCount.setText(String.valueOf(Math.min(countPhoto, 99)));
                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

//------------------------------------------------------


    /**
     * Обработка функцтонала в ToolBox-е
     * */
    @SuppressLint("DefaultLocale")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // ... "На главную"
        if (id == R.id.action_to_main){
            refreshActivity();
        }

        // ... "План работ"
        if (id == R.id.action_to_wpdata){
            try {
//                Intent intent = new Intent(menu_main.this, menu_wp_data.class);
                Intent intent = new Intent(menu_main.this, WPDataActivity.class);
                startActivity(intent);
            }catch (Exception e){
                globals.alertDialogMsg(this, "Не получилось перейти в активность \"План работ\" изза ошибки: " + e);
            }

        }

        // ... Отобразить подсказку
        if (id == R.id.action_info){
            showHelpMassage();
        }

        if (id == R.id.action_autosend){
            boolean change = !Globals.autoSend;
            item.setChecked(change);
            Globals.autoSend = change;

            String stat = "включен";    // stat = статус в который перевели Автовыгрузку
            if (!Globals.autoSend){
                stat = "выключен";
            }
            globals.alertDialogMsg(this, "Обмен данными с сервером в автоматическом режиме " + stat);

/*            // Сохранение состояния автовыгрузки
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean("checkAutoSend", globals.autoSend).apply();*/
        }

        // ... Журнал фото
        if (id == R.id.action_photo_log){
            PhotoLog photoLog = new PhotoLog();
            photoLog.viewPhotoLog(this);
        }

        // ... "Выход"
        if (id == R.id.action_exit) {
            String msg = "Для экономии заряда батареи рекомендую выключить GPS в настройках телефона.\n\nСпасибо за сотрудничество )";
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage(msg);
            builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        finishAffinity();
                    }
                    System.exit(0);
                }
            });
            builder.create().show();
            return true;
        }


        // Светофор
        // inet
/*
        if (id == R.id.check_internet){
            int status = lightsStatusInternet();
            if (status == 1){
                Globals.alertDialogMsg("С интернетом всё в порядке.", this);
            }else if (status == 2){
                Globals.alertDialogMsg("Интернета нет. Проверьте соединение с провайдером или наличие WiFi", this);
            }else if (status == 0){
                Globals.alertDialogMsg("Данные о наличии интернета ещё не получены. Повторите попытку позже.", this);
            }
            return true;
        }
*/

        // serv
        if (id == R.id.check_sever){
            int status = lightsStatusServer();
            if (status == 1){
                globals.alertDialogMsg(this, "С сервером всё впорядке.");
            }else if (status == 2){
                globals.alertDialogMsg(this, "Сервер не доступен. Повторите попытку позже.");
            }else if (status == 0){
                globals.alertDialogMsg(this, "Данные о наличии сервера ещё не получены. Повторите попытку позже.");
            }
            return true;
        }

/*        // online
        if (id == R.id.check_online){
            int status = lightsStatusOnline();
            if (status == 1){
                globals.alertDialogMsg("Вы онлайн.", this);
            }else if (status == 0){
                globals.alertDialogMsg("Вы офлайн. Выйдите из приложения и попробуйте повторно залогиниться в нём или запустите приложение через МВС", this);
            }
            return true;
        }*/

        // gps
        if (id == R.id.check_GPS){
            int status = lightsStatusGPS();
            if (status == 1){
                globals.alertDialogMsg(this, "Координаты GPS актуальны по состоянию на:\nДата: " + lastGPSData + "\nВремя: " + lastGPSTime + "\n\nКоординаты: \nlat: " + CoordX + "\nlon: " + CoordY);
            }else if (status == 2){
                globals.alertDialogMsg(this, "Координаты GPS актуальны по состоянию на:\nДата: " + lastGPSData + "\nВремя: " + lastGPSTime + "\n\nКоординаты: \nlat: " + CoordX + "\nlon: " + CoordY + "\n\nДанные просрочены на " + delayGPS + " минут.\n\nВам необходимо получить свежие данные о местоположении данного устройства.");
            }else if (status == 3){
                globals.alertDialogMsg(this, "GPS не может определить местоположение данного устройства.\nВыйдите из помещения и повторите попытку определения местоположения.");
            }
            return true;
        }

        // net
        if (id == R.id.check_NET){
            int status = lightsStatusNET();
            if (status == 1){
                globals.alertDialogMsg(this, "Координаты NET актуальны по состоянию на:\nДата: " + lastNETData + "\nВремя: " + lastNETTime);
            }else if (status == 2){
                globals.alertDialogMsg(this, "Координаты NET актуальны по состоянию на:\nДата: " + lastNETData + "\nВремя: " + lastNETTime + "\n\nДанные просрочены на " + delayNET + " минут.\n\nВам необходимо получить свежие данные о местоположении данного устройства.");
            }else if (status == 3){
                globals.alertDialogMsg(this, "NET не может определить местоположение данного устройства.\nВыйдите из помещения и повторите попытку определения местоположения.");
            }
            return true;
        }

/*        // Запрос MP
        if (id == R.id.check_MP){
//            recordToLogMP();

            locationRequest();



*//*            if (status == 1){
                Globals.alertDialogMsg("Ваше местоположение определено в " + String.format("%.1f", distanceAB) + measure + " от ТТ " + address_id_txt + ".", this);
            }else if (status == 2){
                Globals.alertDialogMsg("Ваше местоположение определено в " + String.format("%.1f", distanceAB) + measure + " от ТТ " + address_id_txt + " что больше " + distanceMin + " метров." + "\n\nЕсли Вы находитесь на ТТ - проверьте данные GPS в меню Provider. Данные должны быть максимально свежими и сигнал дожлен быть зелёным. Если сигнал зелёный, но данные не свежие - получите свежие данные GPS(для этого надо выйти на улицу/подойти к окну, подождать минуту и нажать на GPS) после чего данные в скобочках должны обновиться.\n\nЕсли проблема продолжается - обратитесь к Вашему руководителю.", this);
            }else if (status == 0){
                Globals.alertDialogMsg("Данные о наличии МП ещё не получены. Повторите попытку позже.\nВозможно Вы не выбрали посещение - выберите его и повторите попытку\n\nЕсли проблема повторяется - обратитесь к Вашему руководителю.", this);
            }*//*
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }


    /* Storing the file url as it'll be null after returning from camera app */
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
    }


    // Обновление текущей активности
    public void refreshActivity(){
        Intent i = new Intent(this, this.getClass());
        finish();
        this.startActivity(i);
    }


    /**Проверка версии приложения*/
    private void checkAPPVer(){
/*        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            int count = 0;
            @Override
            public void run() {
                //if (server.pingInternet()) {
                    boolean bool = VersionApp.checkVer(menu_main.this);
                    if(!bool && count < 1){
                        count++;
                        handler.postDelayed(this, 2000);
                    }
                //}
            }
        }, 5000);*/
    }


    // Выполнить проверку включённости GPS, МП и запустить фотоаппарат для фотографирования
    private void takePhoto(){
        if (trecker.enabledGPS) {
            if (lat > 0 && lon > 0) {
                if (CoordX > 0 && CoordY > 0) {
                    double d = trecker.coordinatesDistanse(lat, lon, CoordX, CoordY);
                    if (d > 500) {
                        String title = "Нарушение по Местоположению.";
                        String msg = String.format("По данным системы вы находитесь на расстоянии %.1f метров от ТТ %s, что больше допустимых 500 метров.\n\nВы не сможете использовать фото которые выполните в таком состоянии системы.\n\nЕсли в действительности Вы находитесь в ТТ - обратитесь к своему руководителю за помощью.", d, address_id_txt);
                        String trueButton = "<font color='#000000'>Всё равно сделать фото</font>";
                        String falseButton = "<font color='#000000'>Отказаться от изготовления фото</font>";
                        String title2 = "ВНИМАНИЕ!";
                        String msg2 = "Система не обнаружила вас в ТТ. \n\nФотографии выполненные в этом режиме могут быть признаны не действительными.\n\nОтказаться от изготовления фото?";
                        String trueButton2 = "<font color='#000000'>Да</font>";
                        String falseButton2 = "<font color='#000000'>Нет</font>";

                        alertMassageMP(1, title, msg, trueButton, falseButton, title2, msg2, trueButton2, falseButton2);
                    } else if (serverTimeControl()){
                        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis());
                        String timeStamp2 = new SimpleDateFormat("HH:mm:ss").format(RetrofitBuilder.getServerTime());
                        String timeDifference = "" + (Globals.serverGetTime - RetrofitBuilder.getServerTime()) / 1000;

                        String t1 = "Ошибка синхронизации времени.";
                        String m1 = String.format("Время установленное на: \n" +
                                "Вашем телефоне: %s \n" +
                                "Нашем сервере:\t\t %s \n\n" +
                                "Разница во времени больше %s секунд\n\n" +
                                "Установите на своём телефоне время аналогичное с сервером и повторите попытку.", timeStamp, timeStamp2, timeDifference);
                        String bt1 = "<font color='#000000'>Всё равно сделать фото</font>";
                        String bf1 = "<font color='#000000'>Отказаться от изготовления фото</font>";
                        String t2 = "ВНИМАНИЕ!";
                        String m2 = "Фотографии выполненные в этом режиме могут быть признаны не действительными.\n\nОтказаться от изготовления фото?";
                        String bt2 = "<font color='#000000'>Да</font>";
                        String bf2 = "<font color='#000000'>Нет</font>";

                        alertMassageMP(1, t1, m1, bt1, bf1, t2, m2, bt2, bf2);
                    }else{
                        dispatchTakePictureIntent(); // Метод который запускает камеру и создаёт файл фото.
                    }
                }else{
                    Log.e("Проверка координат", "X: " + CoordX + " Y: " + CoordY);
                    String t1 = "Координаты не определены";
                    String m1 = "GPS на Вашем телефоне включен, но по какой-то причине не смог определить Ваши координаты.\n" +
                            "1. Выйдите на улицу\n" +
                            "2. Перезагрузите (Выключите/Включите) GPS\n" +
                            "3. В меню приложения нажмите на \"Перейти на главную\"\n" +
                            "4. Повторите попытку \n" +
                            "\n" +
                            "Если ошибка повторится - обратитесь за помощью к Вашему руководителю.";
                    String bt1 = "";
                    String bf1 = "Ок" +
                            "" +
                            "";

                    alertMassageMP(2, t1, m1, bt1, bf1, "", "", "", "");
                }
            }
        } else {
            String title = "Выключеный GPS";
            String msg = "Не могу определить Ваше местоположение, возможно выключен GPS.\n\n" +
                    "Вы не сможете использовать фото которые выполните в таком состоянии системы.\n\n" +
                    "Если в действительности у Вас включён GPS - обратитесь к своему руководителю за помощью.";
            String trueButton = "<font color='#000000'>У меня всё работает</font>";
            String falseButton = "<font color='#000000'>Закрыть сообщение</font>";
            String title2 = "ВНИМАНИЕ!";
            String msg2 = "Система не обнаружила GPS. \n\n" +
                    "Фотографии выполненные в этом режиме могут быть признаны не действительными.\n\n" +
                    "Отказаться от изготовления фото?";
            String trueButton2 = "<font color='#000000'>Да</font>";
            String falseButton2 = "<font color='#000000'>Нет</font>";

            alertMassageMP(1, title, msg, trueButton, falseButton, title2, msg2, trueButton2, falseButton2);
        }
    }




    // BUTTON --- Запуск камеры для фотографии
    public void makePhoto(View view){
        try {
            if (exec) {
                choiceCustomerGroup();
            }else{
                globals.uriNonElementMassage = globals.alertMassage("Не указаны реквизиты посещения: Дата, адрес, клиент...\n\nЗапустите приложение с МВС или получите данные с Плана работ", globals.uriNonElementMassage,this);
            }
        }catch (Exception e){
            globals.alertDialogMsg(this, "ОШИБКА ПРИ ВЫПОЛНЕНИИ ФОТО: " + e);
        }
    }


    // BUTTON --- Продолжить сьемку
    public void makePhotoOffline(View view){
        if (!savePhotoToDB()){
            Toast toast = Toast.makeText(this, "Фото сохранить не удалось! Повторите попытку и обновите страничку.\n\nЕсли ошибка повторяется - обратитесь к руководителю.", Toast.LENGTH_LONG);toast.show();
        }else {
            Toast toast = Toast.makeText(this, "Фото сохранено и готово к отправке.", Toast.LENGTH_SHORT);toast.show();
        }

        choiceCustomerGroup();
    }


    // BUTTON --- Сохранить состояние фото и закрыть(обновить активность)
    public void close(View view) {
        if (!savePhotoToDB()){
            Toast toast = Toast.makeText(this, "Фото сохранить не удалось! Повторите попытку и обновите страничку.\n\nЕсли ошибка повторяется - обратитесь к руководителю.", Toast.LENGTH_LONG);toast.show();
        }else {
            Toast toast = Toast.makeText(this, "Фото сохранено и готово к отправке.", Toast.LENGTH_SHORT);toast.show();
        }

        button_photo.setVisibility(View.VISIBLE); // Скрываем кнопки для Фото
        button_make_photo.setVisibility(View.GONE);
        button_close.setVisibility(View.GONE);
        imageViewMerch.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        button_comment.setVisibility(View.GONE); // Отображение кнопки комментария

        setupBadge(stackPhotoTableCount());
    }


    // BUTTON --- Кнопка загрузки фотографии
    /**Кнопочка обмена. Реализует:
     *
     * 1. Выгрузку фото
     * 2. Обмен таблиц с сервером(План работа, Группы товаров, Тип Фото, Опции, report_prepare)
     *
     * */

    public void uploadPhoto(View view){

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.alertdialog_exchange);
        dialog.setTitle("Обмен.");
        dialog.setCancelable(false);

        TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading();
        Context context = dialog.getContext();  // Получение контекста для отображения прогреса загрузки

        //set up button update tables
        Button button_update_tables = (Button) dialog.findViewById(R.id.button_ad_exchange_update_tables);
        button_update_tables.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Log.e("TAG_REALM_LOG", "ЗАПИСЬ 4");
                    RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "Нажатие на кнопку \"Синхронизовать\"", 1089, null, null, null, Integer.parseInt(user_id), null, Globals.session, null)));
                }catch (Exception e){
                    Log.e("TAG_REALM_LOG", "Ошибка(4): " + e);
                }

                try {
                    tablesLoadingUnloading.downloadWPData(context);                     // План работ
                    tablesLoadingUnloading.downloadImagesTp(context);                   // Типы Фото
                    tablesLoadingUnloading.downloadTypeGrp(context);                    // Группы товаров
                    tablesLoadingUnloading.downloadOptions(context);                    // Опции
                    tablesLoadingUnloading.downloadReportPrepare(context, 0);     // Отчёт RP

                    tablesLoadingUnloading.downloadCustomerTable(context);              // Клиенты
                    tablesLoadingUnloading.downloadAddressTable(context);               // Адреса
                    tablesLoadingUnloading.downloadSotrTable(context);                  // Сотрудники
                    tablesLoadingUnloading.downloadTovarTable(context);                 // Товары

                    tablesLoadingUnloading.downloadErrorTable(context);                 // Таблица Ошибок
                    tablesLoadingUnloading.downloadAkciyTable(context);                 // Тфблица Акций

                    tablesLoadingUnloading.downloadTradeMarksTable(context);            // Таблица Торговых марок

                }catch (Exception e){
                    // запись в ЛОг
                }

                dialog.cancel();
            }
        });

        Button buttonSendData = (Button) dialog.findViewById(R.id.button_ad_send_data);
        buttonSendData.setOnClickListener(v -> {
            Log.e("PRESS_BUTTON", "UPLOAD");
//            tablesLoadingUnloading.uploadReportPrepareToServer();
//            tablesLoadingUnloading.sendAndUpdateLog(context);
//            tablesLoadingUnloading.uploadLodMp();
            tablesLoadingUnloading.updateWpData();
            dialog.dismiss();
        });

        //set up button send photos
        Button button_send_photos = (Button) dialog.findViewById(R.id.button_ad_exchange_send_photos);
        button_send_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Запись в Лог
                try {
                    Log.e("TAG_REALM_LOG", "ЗАПИСЬ 5");
                    RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "Нажатие на кнопку \"выгрузка фото\"", 1088, customer_id, Integer.parseInt(address_id), null, Integer.parseInt(user_id), null, Globals.session, date)));
                }catch (Exception e){
                    Log.e("TAG_REALM_LOG", "Ошибка(5): " + e);
                }

                int countPhoto = stackPhotoTableCount();
                if (countPhoto > 0){
                    String msg = "Сейчас будет выгружено " + countPhoto + " фото на сервер. Дождитесь сообщения об окончании работы.";

                    AlertDialog.Builder builder = new AlertDialog.Builder(menu_main.this);
                    builder.setCancelable(false);
                    builder.setMessage(msg);
                    builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getPhotoAndUpload(1);
//                            cronSendPhoto(1);
                        }
                    });
                    builder.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create().show();
                }
                dialog.cancel();
            }
        });


        //set up button close
        Button button = (Button) dialog.findViewById(R.id.button_ad_exchange_close);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }


    // BUTTON --- Кнопка добавления комментария
    public void addComment(View v) {
        if (textView_comment != null && !comment.equals("")){
            button_comment.setText("Изменить комментарий");
        }
        COMMENTS();
    }


    /**Сохраняет данные о фото и само фото в БД
     * @return  true    - если данные сохранить удалось
     *          false   - есил произошла какая-то ошибка*/
    private boolean savePhotoToDB(){
        try {
            int coord = Coordinates();// Получение кординат и типа данных

            // Вывод сообщения что координаты получить не вышло
            if (coord == 0) {
                globals.alertDialogMsg(this, "Определить координаты вашего местоположения не удалось. Фотоотчёт будет отправлен на сервер без координат. И может быть не принят. За помощью обратитесь к своему руководителю.");
            }

            // Предупреждение: Как давно получены данные GPS, если они устарели.
            if (coord == 6) {
                long sec = System.currentTimeMillis() - trecker.imHereGPS.getTime();
                sec = (sec / 1000 - 1800) / 60;
                if (sec < 0) {
                    globals.alertDialogMsg(this, "Данные о местоположении устарели. Они получены " + sec + " минут назад. Обновите эти данные и продолжите работу.\n\n" +
                            "Что б обновить актуальность данных МП следует:\n" +
                            "1. Иметь включённый GPS\n" +
                            "2. Выйти с ТТ на улицу или подойти к окну с включённым приложением\n" +
                            "3. Подождать минуту пока данные не обновятся \n\n" +
                            "Если Вам что-то не понятно - обратитесь к своему руководителю.");
                    Toast toast = Toast.makeText(this, "Данные о местоположении устарели. Они получены " + sec + " минут назад. Обновите эти данные и продолжите работу.\n\nЕсли Вам что-то не понятно - обратитесь к своему руководителю.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }

            // Предупреждение: Как далеко от ТТ.
            if (lat > 0 && lon > 0) {
                double d = trecker.coordinatesDistanse(lat, lon, CoordX, CoordY);
                if (d > 500) {
                    globals.alertDialogMsg(this, "По данным системы вы находитесь на расстоянии " + String.format("%.1f", d) + measure + " метров от ТТ " + address_id_txt + ", что больше допустимых 500 метров.\n\nЕсли в действительности Вы находитесь на ТТ - проверьте актуальность гео-данных в меню светофора \"Provider\". Если данные получены недавно и актуальны - обратитесь к своему руководителю за помощью.");
                }
            }

            // mod 1 = "Устаревший" вариант для сбора информации об устройстве и координатах
            GP = POST_10(1);// Запись пост данных в переменную для БД для последущей отправки на сервер



            if (RealmManager.chechPhotoExist(photoToDB.getAbsolutePath())) {
                //if (!SQL.checkIsDataAlreadyInDBorNot("stack_photo", "photo_num", photoToDB.getAbsolutePath(), 1)) {   // Проверка на наличие фото в таблице

                File file = null;

                try {
                    file = resizeImageFile(photoToDB);
                } catch (Exception e) {
                    globals.alertDialogMsg(this, "Ошибка В ужатии " + e);
                }

                if (file == null) {
                    globals.alertDialogMsg(this, "Не удалось ужать фото - оно будет сохранено и отправлено в полном объеме. Сообщите об этом своему руководителю.\n(Это влияет на скорость отправки фото и трафик)");
                    file = photoToDB;
                }

                /*Сохранение даты фотки*/
                file = exifPhotoData(file);

                System.out.println("PHOTOCHKA: " + getExifCreateData(file));

                String hash = "";
                String path = file.getAbsolutePath();

                try {
                    hash = globals.getHashMD5FromFilePath(path, this);
                } catch (Exception e) {
                    globals.alertDialogMsg(this, "Ошибка в расчёте hash(тип 1) фото: " + e);
                }

                if (hash == null || hash.equals("")) {
                    try {
                        hash = globals.getHashMD5FromFile(file, this);
                    } catch (Exception e) {
                        globals.alertDialogMsg(this, "Ошибка в расчёте hash(тип 2) фото: " + e);
                    }
                }

                try{
                    int id = RealmManager.stackPhotoGetLastId();
                    id++;

                    Integer userId = null;
                    Integer addrId = null;
                    Integer themId = null;
                    Integer photoType = null;

                    if (user_id != null && !user_id.equals("")){userId = Integer.parseInt(user_id);}
                    if (address_id != null && !address_id.equals("")){addrId = Integer.parseInt(address_id);}
                    if (theme_id != null && !theme_id.equals("")){themId = Integer.parseInt(theme_id);}
                    if (photo_type != null && !photo_type.equals("")){photoType = Integer.parseInt(photo_type);}


                    String userNmText = "";
                    String customerNmText = "";
                    String addressNmText = "";

                    try {
                        if (RealmManager.getUsersNm(Integer.valueOf(user_id)) != null)
                            userNmText = RealmManager.getUsersNm(Integer.valueOf(user_id));

                        if (RealmManager.getCustomerNm(customer_id) != null)
                            customerNmText = RealmManager.getCustomerNm(customer_id);

                        if (RealmManager.getAddressNm(Integer.valueOf(address_id)) != null)
                            addressNmText = RealmManager.getAddressNm(Integer.valueOf(address_id));

                    }catch (Exception e){
                        // Ошибка NPE при получении имени пользователя с БД
                        globals.alertDialogMsg(this, "Фото сохранено, но возникли некоторые проблемы: " + e);
                    }

                StackPhotoDB stackPhotoDB = new StackPhotoDB(
                                    id,
                                    "",
                                    null,
                                    userId,
                                    addrId,
                                    customer_id,
                                    themId,
                                    date,
                                    unixTime,
                                    System.currentTimeMillis(),
                                    0,
                                    0,
                                    dad2,
                                    path,
                                    hash,
                                    photoType,
                                    photo_user_id,
                                    customerTypeGrp,
                                    doc_num,
                                    comment,
                                    GP,
                                    0,
                                    0,
                                    false,
                                    userNmText,
                                    customerNmText,
                                    addressNmText
                                    );

                            // Проверка - есть ли что-то NULL для сохранения в БД
                                RealmManager.stackPhotoSavePhoto(stackPhotoDB);
                                try {
                                    Log.e("TAG_REALM_LOG", "ЗАПИСЬ 3");
                                    RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "Успешное сохранение фото в БД", 1087, customer_id, addrId, null, Integer.parseInt(user_id), null, Globals.session, date)));
                                }catch (Exception e){
                                    Log.e("TAG_REALM_LOG", "Ошибка(3): " + e);
                                }
                                return true;
                    } catch (Exception e) {
                        globals.alertDialogMsg(this, "Ошибка сохранения в БД: " + e);
                    }

            } else {
                globals.alertDialogMsg(this, "Такое фото уже существует. Если ошибка повторяется - обратитесь к Вашему руководителю");
                return false;
            }

        }catch (Exception e){
            globals.alertDialogMsg(this, "Ошибка при сохранении фото. При возникновении этой ошибки - обратитесь к руководителю. Код ошибки: " + e);
        }
        return false;
    }


    /*Alert dialog для выбора групы товара клиента*/
    private void choiceCustomerGroup(){
        if (customerTypeGrpArr != null){
            final String[] result = customerTypeGrpArr.values().toArray(new String[0]);
            if (customerTypeGrpArr.size() > 1){
                new AlertDialog.Builder(this)
                        .setTitle("Выберите группу товара для следующего фото: ")
                        .setItems(result, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast t = Toast.makeText(menu_main.this, "Выбрана группа товара: " + result[which], Toast.LENGTH_LONG);t.show();
                                customerTypeGrp = globals.getKeyForValue(result[which], customerTypeGrpArr);
                                takePhoto();
                            }
                        })
                        .show();
            }else if (customerTypeGrpArr.size() == 1){
                customerTypeGrp = globals.getKeyForValue(result[0], customerTypeGrpArr);
                Toast.makeText(this, "Выбрана группа товара: " + result[0], Toast.LENGTH_LONG).show();
                takePhoto();
            }else{
                globals.alertDialogMsg(this, "Не обнаружено ни одной группы товаров по данному клиенту. Сообщите об этом Администратору!");
                customerTypeGrp = "";
                takePhoto();
            }
        }else {
            globals.alertDialogMsg(this, "Не выбрано посещение\n\nЗайдите в раздел План работ, выберите посещение и повторите попытку.");
        }
    }


    // Размещение фотки по URI адрессу для пользователя, отображение фотографии загрузки
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

        try {
        // Если отменили сьемку:
        if(resultCode == Activity.RESULT_CANCELED && requestCode == 1){
            image.delete();
        }

        // Если сьемка успешная:
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK){
            if (image != null && image.exists()) {
                if (image.length() > 0) {       //

                    final int rotation = getImageOrientation(image.getPath()); //Проверка на сколько градусов повёрнуто изображение
                    if (rotation > 0){
                        photoToDB = resaveBitmap(image, rotation);  // ДляСамсунгов и тп.. Разворачиваем как надо.
                    }else{
                        photoToDB = image;  // Для нормальных телефонов - оставляем как есть
                    }

                    setupBadge(stackPhotoTableCount());

                    textView_comment.setText(null);comment = "";  // Обнуление коментария для новой фото
                    button_comment.setText("Добавить комментарий"); // "Возвращение" к исходной кнопки "Добавить комментарий"

                    button_photo.setVisibility(View.GONE); // Скрываем кнопки для Фото
                    button_make_photo.setVisibility(View.VISIBLE);
                    button_close.setVisibility(View.VISIBLE);
                    imageViewMerch.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);

                    Bitmap b = decodeSampledBitmapFromResource(image, 200, 200);
                    if (b != null) {
                        imageView.setImageBitmap(b);
                    }else{
                        globals.alertDialogMsg(this, "Фото ужать не получилось");
                    }
                    button_comment.setVisibility(View.VISIBLE); // Отображение кнопки комментария

                    Toast t = Toast.makeText(this, "Фото сделано, не забудьте его сохранить!", Toast.LENGTH_LONG);t.show();

                }else { // Если фото получилось нулевым - файлик удаляется
                    deleteImageFile(image, image);
                }
            }else {
                globals.alertDialogMsg(this, "Фото не было создано, повторите попытку");
            }
        }

        }catch (Exception e){
            globals.alertDialogMsg(this, "Ошибка при выполнении фото: " + e);
        }
    }

    // Создание фото и пути к ней
    public void dispatchTakePictureIntent() {
        try{
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPG_" + timeStamp + "_";

                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);  // Для прилы
                try {
                    image = File.createTempFile(
                            imageFileName,
                            ".jpg",
                            storageDir
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Uri contentUri;
                if (Build.VERSION_CODES.N <= android.os.Build.VERSION.SDK_INT) {
                    contentUri = FileProvider.getUriForFile(this, "ua.com.merchik.merchik.provider", image);
                } else {
                    contentUri = Uri.fromFile(image);
                }

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }catch (Exception e){
            globals.alertDialogMsg(this, "Ошибка при создании фото: " + e);
        }
    }

    // Создание файла для фотографии
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);  // Для прилы
        //File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "MERCHIK");   // Для пользователя

//        MediaScannerConnection.scanFile(this, new String[] { storageDir.getPath() }, new String[] { "image/jpeg" }, null);


        image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }


    /**
     * Разрешение на использование хра
     *
     * */
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST); // Возможно не надо
                return true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }


    // Удаление файла
    private void deleteImageFile(File fileOld, File fileNew){

        fileNew.delete();
        fileOld.delete();

        if(fileOld.exists()){
            try {
                fileOld.getCanonicalFile().delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(fileOld.exists()){
                getApplicationContext().deleteFile(fileOld.getName());
            }
        }

        if(fileNew.exists()){
            try {
                fileNew.getCanonicalFile().delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(fileNew.exists()){
                getApplicationContext().deleteFile(fileNew.getName());
            }
        }
    }


    // Создание уменьшеного файла
    public File resizeImageFile(File image){
        File f = null;
        Bitmap res = null;
        int origWidth, origHeight;
        Bitmap readyToDecode;

        readyToDecode = decodeSampledBitmapFromResource(image, 1500, 1500);

        try {
            origWidth = readyToDecode.getWidth();
            origHeight = readyToDecode.getHeight();
        }catch (NullPointerException e){
            globals.alertDialogMsg(this, "Фото ужать не вышло. Обратитесь к руководителю. (Ошибка №1)");
            return image;
        }

        final int destWidth = 1500;//width you need

        if(origWidth > destWidth){
            int destHeight = origHeight/( origWidth / destWidth ) ;
            Bitmap b2 = Bitmap.createScaledBitmap(readyToDecode, destWidth, destHeight, false);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            b2.compress(Bitmap.CompressFormat.JPEG,90 , outStream);
            res = b2;

            try {
                f = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //write the bytes in file
            FileOutputStream fo = null;
            try {
                fo = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                if (fo != null) {
                    fo.write(outStream.toByteArray());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // remember close de FileOutput
            try {
                if (fo != null) {
                    fo.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            //Globals.alertDialogMsg("Фото ужать не вышло. Оно слишком маленькое. (Ошибка №0)", this);
            return image;
        }

        image.delete(); //Удаление полноразмерной фотографии
        return f;
    }


    // Получение информации о фотографии
    public File exifPhotoData (File file){
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif != null) {
            exif.setAttribute(ExifInterface.TAG_DATETIME, new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").format(new Date()));
        }

        try {
            if (exif != null) {
                exif.saveAttributes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }


    // Получение даты создания фото
    private int getExifCreateData (File file){
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif != null) {
            return exif.getAttributeInt(ExifInterface.TAG_DATETIME_ORIGINAL, 0);
        }else {
            return 0;
        }
    }


    // Вызов диалогового окна для комментария
    public void COMMENTS (){

        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_comment, null);

        final EditText editTextComment = (EditText) dialogView.findViewById(R.id.edt_comment);
        Button button1 = (Button) dialogView.findViewById(R.id.buttonSubmit);

        if (!comment.equals("")){
            editTextComment.append(comment);
        }

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comment = String.valueOf(editTextComment.getText());
                textView_comment.setText(comment);

                if (textView_comment != null) {
                    button_comment.setText("Изменить комментарий");
                }
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }


    // Вызов диалогового окна "Фото загружено"
    public void photoUpload(String msg, boolean err){

        if (!err){
            SpannableString redSpannable= new SpannableString(msg);
            redSpannable.setSpan(new ForegroundColorSpan(Color.RED), 0, msg.length(), 0);
        }

        if (globals.viewMassage || err) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage(msg);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    refreshActivity();
                }
            })
                    .setNegativeButton("Больше не показывать", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Переменная уходит в 0
                            globals.viewMassage = false;
                            finish();
                            refreshActivity();
                        }
                    });

            builder.create().show();
        }else {
            globals.refresh = true;
            refreshActivity();
        }

    }


    // Определение заряда батареи
    public static int getBatteryPercentage(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }


    /**POST.. для 10 минутной отправки на сервер данных
     * Формирование данных для отправки на сервер
     *
     * return - POST String in base64
     * */
    public String POST_10 (int mod){

        if (mod == 1) {
            //Coordinates();

            String M_to_URL; //
            String sURL;
            String base64 = null; // Строка для гранения закодированного пакета переменных на сервер
            int width = 0;
            int height = 0;

            // Информация о дисплее для отправки
            try {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                width = size.x;
                height = size.y;
            }catch (Exception e){

            }

            SiteObjectsSDB objectsSDB = new SiteObjectsSDB();

            String s;


            Map<String, Object> DataMap = new HashMap<String, Object>();                                // Общая hashmap. Второе значение Obj, потому что вторым параметром может быть hashmap.
            Map<String, Object> battery = new HashMap<String, Object>();
            Map<String, Object> connection_info = new HashMap<String, Object>();
            Map<String, Object> browser_info = new HashMap<String, Object>();
            Map<String, Object> screen_info = new HashMap<String, Object>();
            Map<String, Object> coords = new HashMap<String, Object>();

            battery.put("battery_level", getBatteryPercentage(this));                           // Уровень зар¤да батареи в процентах или -1 если данных нет
            DataMap.put("battery", battery);

            DataMap.put("device_time", CoordTime / 1000);                                                      // unixtime момента получения данных геокоординат

            connection_info.put("downlink", "");                                                  // скорость соединени¤ в мегабитах
            connection_info.put("downlinkMax", "");
            connection_info.put("effectiveType", "");                                                  // тип сети
            connection_info.put("rtt", "");                                                  // задержка
            connection_info.put("type", "");
            DataMap.put("connection_info", connection_info);                                            // информаци¤ о подключении к сети

            browser_info.put("hardwareConcurrency", Runtime.getRuntime().availableProcessors());        // количество ¤дер
            browser_info.put("maxTouchPoints", "");                                                // количество одновременно обрабатываемых точек касания
            browser_info.put("platform", Build.VERSION.SDK_INT);                             // платформа
            browser_info.put("version_app", VersionApp.currentVersion);
            DataMap.put("browser_info", browser_info);                                      // информаци¤ о браузере и железе

            screen_info.put("availHeight", "");
            screen_info.put("availWidth", "");
            screen_info.put("height", height);
            screen_info.put("width", width);
            screen_info.put("keepAwake", "");
            screen_info.put("orientation_angle", "");
            screen_info.put("orientation_type", "");
            DataMap.put("screen_info", screen_info);                                                    // информаци¤ об экране

            coords.put("latitude", CoordX);
            coords.put("longitude", CoordY);
            coords.put("altitude", CoordAltitude);                                             // высота над уровнем моря
            coords.put("accuracy", CoordAccuracy);                                             // точность обязательно
            coords.put("altitudeAccuracy", "");
            coords.put("heading", "");
            coords.put("speed", CoordSpeed);
            coords.put("trusted_location", mocking);
            DataMap.put("coords", coords);                                                              // географические координаты

            DataMap.put("timestamp", unixTime); // unixtime текущего времени, когда был отправлен запрос с данными с точностью до тысячных (если не сможешь настолько точное врем¤ получить, бери текущий unixtime и умножай на 1000)

            M_to_URL = URL.httpBuildQuery(DataMap, "UTF-8");

            try {
                sURL = URLEncoder.encode(M_to_URL, "UTF-8"); //Кодирование URLData в конечную.
                base64 = Base64.encodeToString(sURL.getBytes(), 0);  //Кодирование конечной URLData в base64 для качества передачи.
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return base64;
        }
        else{   //--- mod 2

            return "mod2";
        }
    }

    /** Метод для получения геоданных сети в случае если GPS-данные не доступны
     * */
    public int Coordinates(){

        int res;
        int minGPSTime = 1200;
        long ut = unixTime/1000;

        // Передача параметра с подделкой координат
        if(trecker.isMockGPS || trecker.isMockNET){
            mocking = 0;
        }else{mocking = 1;}

        if (true){
            res = 4;
        }else if (trecker.imHereNET == null){
            res = 5;
        }


        if (trecker.imHereGPS != null && ut - trecker.imHereGPS.getTime()/1000 < 1800) {

            CoordX = trecker.imHereGPS.getLatitude();
            CoordY = trecker.imHereGPS.getLongitude();
            CoordTime = trecker.imHereGPS.getTime();
            CoordSpeed = trecker.imHereGPS.getSpeed();
            CoordAltitude = trecker.imHereGPS.getAltitude();
            CoordAccuracy = trecker.imHereGPS.getAccuracy();

            res = 1;

        }else if(trecker.imHereNET != null){

            CoordX = trecker.imHereNET.getLatitude();
            CoordY = trecker.imHereNET.getLongitude();
            CoordTime = trecker.imHereNET.getTime();
            CoordSpeed = trecker.imHereNET.getSpeed();
            CoordAltitude = trecker.imHereNET.getAltitude();
            CoordAccuracy = trecker.imHereNET.getAccuracy();

            res = 2;

            if (trecker.imHereGPS != null){
                res = 6;
            }

        }else {
            res = 0;
//            Toast toast = Toast.makeText(this, "Приложение не может получить данные о местоположении данного устройства. Попробуйте нажать в меню 3х точек на кнопку \"Перейти на главную\". \n\nЕсли ошибка повторяется - сообщите об этом своему руководителю.", Toast.LENGTH_SHORT);toast.show();
        }


        RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "Координаты "+ "res:" + res + " (X: "+ CoordX +")" + "(Y: "+ CoordY +")" + " Time: " + CoordTime, 1126, null, null, null, null, null, Globals.session, null)));

        return res;
    }


    /**
     * Метод с документации.
     * Расчёт оптимального размера файла фотографии для загрузки в память
     * */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    /**РАЗОБРАТЬСЯ
     * Метод с документации.
     * Декодирует обьект(фото) что б не вылетала ошибка OutOfMemoryError
     *
     * зачем и на что влияет resId
     * разобраться с методами: BitmapFactory.decodeResource, мне вроде нужен был декодирование файла.
     * */
    public static Bitmap decodeSampledBitmapFromResource(File res,int reqWidth, int reqHeight) {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(res.getAbsolutePath(), options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(res.getAbsolutePath(), options);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //------------ РАБОТА ПО ЗАПИСИФОТОК В БД


    // НОВАЯ ВЫГРУЗКА ФОТО
    // ---------------------------------------------------------------------------------------------
    /**
     * Получение и выгрузка фоток.
     *
     * mod = 1 -- Выгрузка в ручном режиме
     * mod = 2 -- Выгрузка вызвана из крона (Автовыгрузка)
     * */
    private void getPhotoAndUpload(int mod){
        if (internetStatus == 1){// inet+
            RealmResults<StackPhotoDB> results = RealmManager.getStackPhotoPhotoToUpload();

            for (int i=0; i<results.size(); i++){
                if (results.get(i) != null){
                    photoUploadToServer(mod, results.get(i));
                }
            }

        } else if (internetStatus == 2) {// inet-

        }else{

        }
    }

    // Выгрузка фоток
    private void photoUploadToServer(int mode, StackPhotoDB photoDB){

        final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");

        int photoId = photoDB.getId();
        String mod              = "images_prepare";
//        String act              = "upload_image";
        String act              = "upload_photo";

        String client_id            = "";
        String addr_id              = "";
        String date                 = "";
        String img_type_id          = "";
        String photo_user_id        = "";
        String client_tovar_group   = "";
        String doc_num              = "";
        String theme_id             = "";
        String comment              = "";
        String dvi              = "";
        String code_dad2            = "";
        String gp                   = "";

        if (photoDB.getClient_id() != null){
            client_id = String.valueOf(photoDB.getClient_id());
        }

        if (photoDB.getAddr_id() != null){
            addr_id = String.valueOf(photoDB.getAddr_id());
        }

        if (photoDB.getTime_event() != null){
            date = photoDB.getTime_event();
        }

        if (photoDB.getPhoto_type() != null){
            img_type_id = String.valueOf(photoDB.getPhoto_type());
        }

        if (photoDB.getPhoto_user_id() != null){
            photo_user_id = String.valueOf(photoDB.getPhoto_user_id());
        }

        if (photoDB.getPhoto_group_id() != null){
            client_tovar_group = photoDB.getPhoto_group_id();
        }

        if (photoDB.getDoc_id() != null){
            doc_num = photoDB.getDoc_id();
        }

        if (photoDB.getTheme_id() != null){
            theme_id = String.valueOf(photoDB.getTheme_id());
        }

        if (photoDB.getComment() != null){
            comment = photoDB.getComment();
        }

        if (photoDB.getDvi() != null){
            dvi = String.valueOf(photoDB.getDvi());
        }

        try{
            code_dad2 = String.valueOf(photoDB.getCode_dad2());
        }catch (Exception e){
            // Запись ошибки
            code_dad2 = "";
        }

        if (photoDB.getGp() != null){
            gp = photoDB.getGp();
        }


        RequestBody mod2 = RequestBody.create(MediaType.parse("text/plain"), mod);
        RequestBody act2                 = RequestBody.create(MediaType.parse("text/plain"), act              );
        RequestBody client_id2           = RequestBody.create(MediaType.parse("text/plain"), client_id        );
        RequestBody addr_id2             = RequestBody.create(MediaType.parse("text/plain"), addr_id          );
        RequestBody date2                = RequestBody.create(MediaType.parse("text/plain"), date             );
        RequestBody img_type_id2         = RequestBody.create(MediaType.parse("text/plain"), img_type_id      );
        RequestBody photo_user_id2       = RequestBody.create(MediaType.parse("text/plain"), photo_user_id    );
        RequestBody client_tovar_group2  = RequestBody.create(MediaType.parse("text/plain"), client_tovar_group);
        RequestBody doc_num2             = RequestBody.create(MediaType.parse("text/plain"), doc_num);
        RequestBody theme_id2            = RequestBody.create(MediaType.parse("text/plain"), theme_id         );
        RequestBody comment2             = RequestBody.create(MediaType.parse("text/plain"), comment          );
        RequestBody dvi2                 = RequestBody.create(MediaType.parse("text/plain"), dvi              );
        RequestBody codeDad2             = RequestBody.create(MediaType.parse("text/plain"), code_dad2        );
        RequestBody gp2                  = RequestBody.create(MediaType.parse("text/plain"), gp               );




        //pass it like this
        File file = new File(photoDB.getPhoto_num());

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part photo =
                MultipartBody.Part.createFormData("photos[]", file.getName(), RequestBody.create(MEDIA_TYPE_JPG, file));

        Log.e("TAG_SEND_PHOTO", "Data: \n"
                + "\n mod:" + mod
                + "\n act:"  + act
                + "\n client_id:"  + client_id
                + "\n addr_id:"  + addr_id
                + "\n date:"  + date
                + "\n img_type_id:"  + img_type_id
                + "\n photo_user_id:" +  photo_user_id
                + "\n client_tovar_group:" +  client_tovar_group
                + "\n doc_num:" +  doc_num
                + "\n theme_id:"  + theme_id
                + "\n comment:"  + comment
                + "\n code_dad2:"  + code_dad2
                + "\n gp:"  + null
                + "\n photo:" +  photo);

        String data = "" + "mod:" + mod
                + "\n act:"  + act
                + "\n client_id:"  + client_id
                + "\n addr_id:"  + addr_id
                + "\n date:"  + date
                + "\n img_type_id:"  + img_type_id
                + "\n photo_user_id:" +  photo_user_id
                + "\n client_tovar_group:" +  client_tovar_group
                + "\n doc_num:" +  doc_num
                + "\n theme_id:"  + theme_id
                + "\n comment:"  + comment
                + "\n code_dad2:"  + code_dad2
                + "\n gp:"  + null
                + "\n photo:" +  photo;

        RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "Данные(" + RealmManager.getStackPhotoPhotoToUpload().size() + "): " + data , 1088, null, null, null, null, null, Globals.session, null)));



        // РУЧНАЯ ВЫГРУЗКА
        if (mode==1) {
            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface()
                    .SEND_PHOTO_2_BODY(mod2, act2, client_id2, addr_id2, date2, img_type_id2, photo_user_id2, client_tovar_group2, doc_num2, theme_id2, comment2, dvi2, codeDad2, gp2, photo);

            String finalDate = date;
            String finalDate1 = date;

            try{
                RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "Попытка выгрузки фото." + call, 1088, null, null, null, null, null, Globals.session, null)));

                call.enqueue(new retrofit2.Callback<JsonObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                        Log.e("TAG_REALM_LOG", "SUCCESS: " + response.body());

                        JsonObject jsonR = response.body();
                        Log.e("TAG_SEND_PHOTO", "RESPONSE: " + response.body());
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                if (jsonR != null) {
                                    RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "(Адрес/Пользователь)" + address_id + "/" + user_id + "Ответ от сервера: " + jsonR, 1088, customer_id, null, null, null, null, Globals.session, finalDate)));
                                    if (!jsonR.get("state").isJsonNull() && jsonR.get("state").getAsBoolean()) {
                                        try {
                                            RealmManager.INSTANCE.executeTransaction(realm -> photoDB.setUpload_to_server(System.currentTimeMillis()));
                                            RealmManager.stackPhotoSavePhoto(photoDB);

                                            Toast.makeText(menu_main.this, "Фото " + photoId + " выгружено на сервер.", Toast.LENGTH_SHORT).show();
                                        }catch (Exception e){
                                            String msg = Arrays.toString(e.getStackTrace());
                                            globals.alertDialogMsg(menu_main.this, msg);
                                        }
                                    } else if (!jsonR.get("state").isJsonNull() && !jsonR.get("state").getAsBoolean()){
                                        if (!jsonR.get("error").isJsonNull() || jsonR.get("error") != null) {
                                            String error = jsonR.get("error").getAsString();
                                            globals.alertDialogMsg(menu_main.this, "(Выгрузка фото)Возникла ошибка: " + error);
                                        }else {
                                            globals.alertDialogMsg(menu_main.this, "Фото не выгружено. Сообщите об этом руководителю. Ответ от сервера: " + inputStream);
                                        }
                                    }else {
                                        globals.alertDialogMsg(menu_main.this, "Ошибка: " + jsonR);//Toast toast = Toast.makeText(this, "Данные сохранить не получилось, повторите попытку", Toast.LENGTH_SHORT);toast.show();
                                    }
                                }else {
                                    globals.alertDialogMsg(menu_main.this, "Не удалось получить ответ от сервера. Скорее всего отсутствует интернет. Проверьте связь и повторите попытку или обратитесь к Ващему руководителю.");//Toast toast = Toast.makeText(this, "Не удалось получить ответ от сервера. Скорее всего отсутствует интернет. Проверьте связь и повторите попытку или обратитесь к Ващему руководителю.", Toast.LENGTH_SHORT);toast.show();
                                }
                            }catch (Exception e){
                                RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "(Адрес/Пользователь)" + address_id + "/" + user_id + "Ошибка при разборе ответа с сервера: " + e, 1088, customer_id, null, null, null, null, Globals.session, finalDate1)));
                                globals.alertDialogMsg(menu_main.this, "Ошибка при выгрузке фото - повторите попытку позже или обратитесь к Вашему руководителю. \nОшибка: " + e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {

                        Log.e("TAG_REALM_LOG", "FAILURE");
                        Log.e("TAG_SEND_PHOTO", "FAILURE: " + t.getMessage());

                        try {
                            Log.e("TAG_REALM_LOG", "ЗАПИСЬ 5");
                            RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "Ошибка при выгрузке фото(FAILURE): " + t, 1088, customer_id, Integer.parseInt(address_id), null, Integer.parseInt(user_id), null, Globals.session, finalDate1)));
                        }catch (Exception e){
                            Log.e("TAG_REALM_LOG", "Ошибка(5): " + e);
                        }

                        Log.e("TAG_SEND_PHOTO", "FAILURE: " + t.getMessage());
                        Toast.makeText(menu_main.this, "Проблема с связью: " + t, Toast.LENGTH_SHORT).show();
                    }
                });
            }catch (Exception e){
                RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "Ошибка при выгрузке фото: " + e, 1088, customer_id, null, null, null, null, Globals.session, finalDate)));
            }

        }
    }




    public void showHelpMassage(){
        String msg = getString(R.string.text_help_photo);
        globals.alertDialogMsg(this, msg);
    }

    private void printErrOnFailure(IOException e, String mod){
        String err = String.valueOf(e);
        String msg = String.format("%sВыгрузка не удалась. Повторите попытку или обратитесь к руководителю если ошибка повторяется. Код ошибки: %s", mod, err);
        globals.alertDialogMsg(menu_main.this, msg);
    }





    // ===================================== КРОНЧИК ============== КРОНЧИК =============== КРОНЧИК ==================== КРОНЧИК ================= КРОНЧИК ========
    private Runnable runnableCron10 = new Runnable() {
        public void run() {
            try {
                // Попытка залогиниться, если мы были в оффлайне
                server.sessionCheckAndLogin(menu_main.this, login, password);   // Проверка активности сессии и логин, если сессия протухла

                internetStatus = server.internetStatus();   // Обновление статуса интеренета
//                RealmManager.stackPhotoDeletePhoto();       // Удаление фото < 2 дня
                lightStatus();                              // Обновление статуса Светофоров
                setupBadge(stackPhotoTableCount());         // Подсчёт кол-ва фоток в БД & Установка числа в счётчик
                cronCheckUploadsPhotoOnServer();            // Получение инфы о "загруженности" фоток

                // Если включена Автовыгрузка/Автообмен
                if (Globals.autoSend) {
                    getPhotoAndUpload(1);   // Выгрузка фото
/*                    TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading();
                    tablesLoadingUnloading.updateTables(menu_main.this);*/
                }

            }catch (Exception e){
                globals.alertDialogMsg(menu_main.this, "При возникновении этой ошибки - обратитесь к Вашему руководителю. Ошибка крона: " + e);
            }

            globals.handlerCount.postDelayed(this, 10000);  //повтор раз в 10 секунд
        }
    };




    /**
     * Функция 10сек крона которая будет получать по хэшам выгруженые на Сайт фото и удалять их
     * (Счётчик глобальный)
     * Также запускает автовыгрузку фоток
     * */
    public void cronCheckUploadsPhotoOnServer(){
        try {
            final RealmResults<StackPhotoDB> realmResults = RealmManager.stackPhotoGetHashs();

            if (!realmResults.isEmpty()){
                Toast.makeText(menu_main.this, "Сервер не обработал: " + realmResults.size()+1 + " фоток.", Toast.LENGTH_SHORT);

                String mod = "images_view";
                String act = "list_image";
                String noLimit = "no_limit";
                String date_from = Clock.lastWeek();
                String date_to = Clock.tomorrow;
                ArrayList<String> listHash = new ArrayList<>();

                for (int i=0; i<realmResults.size(); i++){
                    listHash.add(i, realmResults.get(i).getPhoto_hash());
                }

                retrofit2.Call<PhotoHash> call = RetrofitBuilder.getRetrofitInterface()
                        .SEND_PHOTO_HASH(mod, act, noLimit, date_from, date_to, listHash);
                call.enqueue(new retrofit2.Callback<PhotoHash>() {
                    @Override
                    public void onResponse(Call<PhotoHash> call, Response<PhotoHash> response) {

                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                if (response.body().getState()){
                                    if (response.body().getTotalPages() > 0) {
                                        List<PhotoHashList> list = response.body().getList();
                                        for (int i = 0; i < list.size(); i++) {
                                            for (int j=0; j<realmResults.size(); j++){
                                                if (list.get(i).getHash().equals(realmResults.get(j).getPhoto_hash())) {
                                                    int finalJ = j;
                                                    RealmManager.INSTANCE.executeTransaction(realm -> {
                                                        realmResults.get(finalJ).setGet_on_server(System.currentTimeMillis());
                                                    });
                                                }
                                            }
                                        }
                                    }else {
                                        Toast.makeText(menu_main.this, "Фото для проверки есть, но сервером ещё не обработаны.", Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                }
                            }
                        }catch (Exception e){
                            Toast.makeText(menu_main.this, "Ошибка при проверке фото с сервера: " + e, Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Call<PhotoHash> call, Throwable t) {
                    }
                });
            }else{
            }
        }catch (Exception e){
            Toast.makeText(menu_main.this, "Ошибка при проверке фото: " + e, Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Функция которая просто получает кол-во строчек "счётчик локальный" фото готовых на выгрузку
     * с таблички stack_photo
     * */
    private int stackPhotoTableCount(){
        return RealmManager.stackPhotoNotUploadedPhotosCount();
    }


    //Проверки для меню
    /**Отвечает за общий цвет светофора*/
    public void lightStatus(){
        int lightColorInet = 0;
        int lightColorTrec = 0;

//        if (lightsStatusInternet() == 1){lightColorInet = 1;} else if(lightsStatusInternet() > 1){lightColorInet = 2;}
        if (lightsStatusServer() == 1){lightColorInet = 1;} else if(lightsStatusServer() > 1){lightColorInet = 2;}

//        lightsStatusOnline();

        if (lightsStatusProvider() == 1){lightColorTrec = 1;}else if(lightsStatusProvider() == 2){lightColorTrec = 2;} else if(lightsStatusProvider() > 2){lightColorTrec = 3;}
//        if (lightsStatusMP() == 1){lightColorTrec = 1;} else if(lightsStatusMP() > 1){lightColorTrec = 3;}

        if (menu != null) {
            if (lightColorInet == 1 && lightColorTrec < 3) {    // Все отлично
                menu.getItem(0).setIcon(this.getResources().getDrawable(R.mipmap.light_green));
            } else if (lightColorInet == 2 && lightColorTrec < 3) { // Проблема с Связью
                menu.getItem(0).setIcon(this.getResources().getDrawable(R.mipmap.light_yellow));
            } else {
                menu.getItem(0).setIcon(this.getResources().getDrawable(R.mipmap.light_red));
            }
        }
    }

    /**Отвечает за обновление статуса Интернет в меню светофора*/
/*    public int lightsStatusInternet(){
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.check_internet);

            //boolean b = server.pingInternet();
            boolean b = false;

            if (b) {
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.gray2));
                return 1;
            } else if (!b) {
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.gray2));
                return 2;
            } else {
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.gray2));
                return 0;
            }
        }
        return 0;
    }*/

    /**Отвечает за обновление статуса Сервера в меню светофора*/
    public int lightsStatusServer(){
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.check_sever);
//            if (server.pingServer(this)) {
            if ( server.serverIsOn()){
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_green));
                return 1;
            } else {
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_red));
                return 0;
            }
        }
        return 0;
    }


    /**Отвечает за обновление статуса Online/Offline в меню светофора*/
/*    public int lightsStatusOnline(){
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.check_online);
            if (Globals.statusOnline){
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_green));
                item.setTitle("Онлайн");
                return 1;
            } else {
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_red));
                item.setTitle("Офлайн");
                return 0;
            }
        }
        return 0;
    }*/

    /**Обобщённый цвет GPS & NET*/
    public int lightsStatusProvider(){
        int res = 4;
        int statGPS = lightsStatusGPS();
        int statNET = lightsStatusNET();

        if (menu != null) {
            MenuItem item = menu.findItem(R.id.check_Provider);

            if (statGPS == 1 && statNET == 1) {
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_green));
                res = 1;    // Green
            } else if (statGPS != 1 && statNET == 1) {
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_yellow));
                res = 2;    // Yeloow
            } else if (statGPS != 1) {
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_red));
                res = 3;    // Red
            } else {
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_red));
                res = 4;    // Gray
            }
        }

        return res;
    }

    /**Отвечает за обновление статуса Геоданных в меню светофора*/
    @SuppressLint("SimpleDateFormat")
    public int lightsStatusGPS(){
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.check_GPS);
            Coordinates();
            if (trecker.imHereGPS != null) {
                long dv = trecker.imHereGPS.getTime();// its need to be in milisecond
                Date df = new java.util.Date(dv);
                lastGPSData = new SimpleDateFormat("MM-dd").format(df);
                lastGPSTime = new SimpleDateFormat("HH:mm:ss").format(df);

                delayGPS = System.currentTimeMillis() - trecker.imHereGPS.getTime();
                delayGPS = (delayGPS / 1000 - 1800) / 60;
                if (delayGPS > 0) {
                    item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_red));
                    String gps = "GPS( " + lastGPSTime + " ): " + delayGPS;
                    item.setTitle(gps);
                    return 2; // Данные устарели
                } else {
                    item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_green));
                    String gps = "GPS (" + lastGPSTime + ")";
                    item.setTitle(gps);
                    return 1; // Всё окей
                }

            } else {
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_red));
                return 3; // Данные GPS не доступны
            }
        }
        return 0;
    }

    /**Отвечает за обновление статуса NET*/
    @SuppressLint("SimpleDateFormat")
    public int lightsStatusNET(){
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.check_NET);
            Coordinates();
            if (trecker.imHereNET != null) {
                long dv = trecker.imHereNET.getTime();// its need to be in milisecond
                Date df = new java.util.Date(dv);
                lastNETData = new SimpleDateFormat("MM-dd").format(df);
                lastNETTime = new SimpleDateFormat("HH:mm:ss").format(df);

                delayNET = System.currentTimeMillis() - trecker.imHereNET.getTime();
                delayNET = (delayNET / 1000 - 1800) / 60;
                if (delayNET > 0) {
                    item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_red));
                    String net = "NET( " + lastNETTime + " ): " + delayNET;
                    item.setTitle(net);
                    return 2; // Данные устарели
                } else {
                    item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_green));
                    String net = "NET (" + lastNETTime + ")";
                    item.setTitle(net);
                    return 1; // Всё окей
                }

            } else {
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_red));
                return 3; // Данные NET не доступны
            }
        }
        return 0;
    }




    /**Предупреждение что фото выполняется при нарушении МП*/
    private void alertMassage(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle("Не выбрано Посещение");
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(Html.fromHtml("<font color='#000000'>Ок</font>"), (dialog, which) -> {
        });
//        builder.setNegativeButton(Html.fromHtml("<font color='#000000'>Отказаться от изготовления фото</font>"), (dialog, which) -> {
//        });
        builder.create().show();
    }

    /**Схема перевопроса
     *
     * @param title         Заголовок сообщения
     * @param msg           Сообщение для основного окна
     * @param trueButton    Текст позитивной кнопки
     * @param falseButton   Текст негативной кнопки
     *
     * */
    private void alertMassageMP(int mod, String title, String msg, String trueButton, String falseButton, String title2, String msg2, String trueButton2, String falseButton2){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setMessage(msg);
        if (mod == 1) {
            builder.setPositiveButton(Html.fromHtml(trueButton), (dialog, which) -> alertMassageMPPhoto(title2, msg2, trueButton2, falseButton2));
        }
        builder.setNegativeButton(Html.fromHtml(falseButton), (dialog, which) -> {});
        builder.create().show();
    }


    /**
     * @param title2            Заголовок сообщения
     * @param msg2              Сообщение для основного окна
     * @param trueButton2       Текст позитивной кнопки
     * @param falseButton2      Текст негативной кнопки   */
    private void alertMassageMPPhoto(String title2, String msg2, String trueButton2, String falseButton2){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle(title2);
        builder.setCancelable(false);
        builder.setMessage(msg2);
        builder.setPositiveButton(Html.fromHtml(trueButton2), (dialog, which) -> {});
        builder.setNegativeButton(Html.fromHtml(falseButton2), (dialog, which) -> {
            dispatchTakePictureIntent(); // Метод который запускает камеру и создаёт файл фото.
        });
        builder.create().show();
    }







    // ---------------------------------------------------------------------------------------------


    // СПИНЕР (Выпадающий список типов фото в Активости ФОТООТЧЁТА)
    public class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
                                   View view, int pos, long id) {
            try {
                String photoType = parent.getSelectedItem().toString();
                photo_type = globals.getKeyForValue(photoType, mapSpinner);
            }catch(Exception e){
                //Globals.alertDialogMsg("err: " + e, menu_main.this);
            }
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }



    // ПОВОРОТ ФОТОК (проверка сделанная для самсунгов которые разворачивают фотографии)
    /**
     * Поворачивает фото в 0:0
     * */
    private Bitmap checkRotationFromCamera(Bitmap bitmap, String pathToFile, int rotate) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }

    /**
     * Получает ориентацию фотографии.
     * */
    public static int getImageOrientation(String imagePath) {
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }

    /**Перезаписывает повернутое фото в новый файл*/
    private File resaveBitmap(File img, int rotation) { //help for fix landscape photos
        OutputStream outStream = null;
        File file = new File(img.toURI());
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            bitmap = checkRotationFromCamera(bitmap, file.getPath(), rotation);
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) ((float) bitmap.getWidth() * 0.3f), (int) ((float) bitmap.getHeight() * 0.3f), false);
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private boolean serverTimeControl(){
        long currentTime = System.currentTimeMillis();
        if (RetrofitBuilder.getServerTime() != 0){
            if (currentTime - Globals.serverGetTime <= 3600) {
                return (Globals.serverGetTime - RetrofitBuilder.getServerTime())/1000 > 20;
            } else {
                return false;
            }
        }else{return false;}
    }



}//END OF CLASS..


