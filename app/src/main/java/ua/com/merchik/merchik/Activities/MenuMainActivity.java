package ua.com.merchik.merchik.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.SamplePhotoExchange;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;
import ua.com.merchik.merchik.toolbar_menus;


public class MenuMainActivity extends toolbar_menus {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent();


        try {

            findViewById(R.id.fab).setOnClickListener(v -> {
                Toast.makeText(this, "Подсказка к данному разделу не готова", Toast.LENGTH_SHORT).show();
                test(new Click() {
                    @Override
                    public <T> void onSuccess(T data) {
                        Log.e("test", "test" + data);
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("test", "test" + error);
                    }
                });
            });

            findViewById(R.id.fab).setOnLongClickListener(v -> {
                Toast.makeText(this, "Отладочная информация!\nДолгий клик по подсказке.", Toast.LENGTH_SHORT).show();
//                test2(v.getContext());
                return true;
            });


            initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_home);
        } catch (Exception e) {
            globals.alertDialogMsg(this, "ОшибкаMain: " + e);
        }
    }

    // =================================== --- onCreate --- ========================================

    private void setActivityContent() {
        setContentView(R.layout.toolbar_new);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        TextView activity_title = (TextView) findViewById(R.id.activity_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title.setText(getString(R.string.title_activity_menu_main));
    }

    private void test(Click result) {

        SamplePhotoExchange samplePhotoExchange = new SamplePhotoExchange();
        samplePhotoExchange.downloadSamplePhotoTable(new Clicks.clickStatus() {
            @Override
            public void onSuccess(Object data) {
                Globals.writeToMLOG("INFO", "Exchange/SamplePhotoExchange()/onSuccess", "data: " + data);

                List<SamplePhotoSDB> res = (List<SamplePhotoSDB>) data;

                try {
                    RealmManager.INSTANCE.executeTransaction(realm -> {
                        if (samplePhotoExchange.synchronizationTimetableDB != null) {
                            samplePhotoExchange.synchronizationTimetableDB.setVpi_app(System.currentTimeMillis() / 1000);
                            realm.copyToRealmOrUpdate(samplePhotoExchange.synchronizationTimetableDB);
                        }
                    });


                    samplePhotoExchange.downloadSamplePhotos(res, new Clicks.clickStatusMsg() {
                        @Override
                        public void onSuccess(String data) {
                            Globals.writeToMLOG("INFO", "2Exchange/SamplePhotoExchange()/onSuccess", "data: " + data);
                        }

                        @Override
                        public void onFailure(String error) {
                            Globals.writeToMLOG("ERROR", "2Exchange/SamplePhotoExchange()/onFailure", "error: " + error);
                        }
                    });

                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "SamplePhotoExchange/downloadSamplePhotoTable/onResponse/onComplete/synchronizationTimetableDB", "Exception e: " + e);
                }
            }

            @Override
            public void onFailure(String error) {
                Globals.writeToMLOG("ERROR", "Exchange/SamplePhotoExchange()/onFailure", "error: " + error);
            }
        });

//        new Exchange().sendTAR();

//        new Exchange().getGroupTovByClient();


//        StandartData data = new StandartData();
//        data.mod = "potential_clients";
//        data.act = "list";
//
//        Gson gson = new Gson();
//        String json = gson.toJson(data);
//        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
//
//        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
//        call.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                Log.e("test", "test" + response);
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.e("test", "test" + t);
//            }
//        });
    }


    /*        UploadDataSEWork data = new UploadDataSEWork();
        data.mod = "plan";
        data.act = "update_data";
        data.data = RealmManager.getUploadWpData(RealmManager.WpDataUpload.COMMENT);

        JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(data), JsonObject.class);

        Globals.writeToMLOG("INFO", "Exchange.sendWpDataToServer", "convertedObject" + convertedObject);

        if (data != null && data.data.size() > 0) {
            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new retrofit2.Callback<JsonObject>() {
                @Override
                public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    Log.e("test", "test: " + response.body());
                }

                @Override
                public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                    Log.e("test", "test: " + t);
                }
            });
        }*/

    /**
     * Получение на мою сторону данных о выделенных областях на фото
     */
/*
*
        StandartData data = new StandartData();
        data.mod = "images_view";
        data.act = "list_image_region";

        data.dt_change_from = Clock.today_7;
        data.dt_change_to = Clock.tomorrow7;

        /*data.hash_list = "";    // - по хэшу фоток
        data.code_dad2 = "";    // - по дад2
        data.id = "";           // - ID фото
        data.date_from = "";    // - дата с (фотографии) по дефолту 1 месяц назад
        data.date_to = "";      // - дата по (фотографии) по дефолту сегодняшний день

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<JsonObject>()

        {
            @Override
            public void onResponse (Call < JsonObject > call, Response < JsonObject > response){
            Log.e("test", "test" + response);
        }

            @Override
            public void onFailure (Call < JsonObject > call, Throwable t){
            Log.e("test", "test" + t);
        }
        });
**/



    /*
        WebSocket ws;
    public void test(Context context) {
        ws = RetrofitBuilder.testWebSocket(new Clicks.click() {
            @Override
            public <T> void click(T data) {
                if (data instanceof WebSocketData){
                    WebSocketData wsData = (WebSocketData) data;
                    switch (wsData.action){
                        case "chat_message":
                            Toast.makeText(context, "chat_message/\n\nНовое сообщение: " + wsData.chat.msg, Toast.LENGTH_SHORT).show();
                            break;
                        case "global_notice":
                            Toast.makeText(context, "global_notice: " + wsData.text, Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(context, "Web Socket. Не смог определить тип сообщения. Сообщение: " + data, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }else {
                    Toast.makeText(context, "Data: " + data, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void test2(Context context){
        if (ws != null){
            ws.close(0, "Because I wanted");
        }
    }
    * */
}
