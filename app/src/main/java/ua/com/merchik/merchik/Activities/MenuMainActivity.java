package ua.com.merchik.merchik.Activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.navigation.NavigationView;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.toolbar_menus;


public class MenuMainActivity extends toolbar_menus {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent();


        try {

            findViewById(R.id.fab).setOnClickListener(v -> {
                Toast.makeText(this, "Подсказка к данному разделу не готова", Toast.LENGTH_SHORT).show();
//                test(new Click() {
//                    @Override
//                    public <T> void onSuccess(T data) {
//                        Log.e("test", "test" + data);
//                    }
//
//                    @Override
//                    public void onFailure(String error) {
//                        Log.e("test", "test" + error);
//                    }
//                });

                test(this);
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

/*    private void test(Click result) {
        new Exchange().downloadAchievements();
        new Exchange().downloadVoteTable();

        new Exchange().downloadArticleTable();
    }*/

/*        StandartData data = new StandartData();
        data.mod = "sms_verification";
        data.act = "list";
//        data.dt_change_from = Clock.today_7;
//        data.dt_change_to = Clock.tomorrow;

        JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(data), JsonObject.class);

        retrofit2.Call<EKLResponse> call = RetrofitBuilder.getRetrofitInterface().GET_EKL(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new retrofit2.Callback<EKLResponse>() {
            @Override
            public void onResponse(retrofit2.Call<EKLResponse> call, retrofit2.Response<EKLResponse> response) {
                Log.e("test", "test: " + response.body());
                try {
                    SQL_DB.eklDao().insertAll(response.body().list);
                } catch (Exception e) {
                    Log.e("test", "Exception e: " + e);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<EKLResponse> call, Throwable t) {
                Log.e("test", "test: " + t);
            }
        });
    }*/


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
    public void test(Context context) {
/*        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "2")
                .setSmallIcon(R.mipmap.merchik)
                .setContentTitle("Нове повідомлення")
                .setContentText("Тестовое оповещение которое должно отобразиться при нажати на этот знак вопроса, да я так тестирую оповещения, не осуждайте меня.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(2, builder.build());*/


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = null;   // for heads-up notifications
            channel = new NotificationChannel("channel01", "name",
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription("description");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, "channel01")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Заголовок")
                .setContentText("Тестове повідомлення!")
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)   // heads-up
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, notification);

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("2", "name", importance);
            channel.setDescription("description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}
