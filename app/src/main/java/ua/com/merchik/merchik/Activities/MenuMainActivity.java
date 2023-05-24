package ua.com.merchik.merchik.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;
import ua.com.merchik.merchik.toolbar_menus;


public class MenuMainActivity extends toolbar_menus {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent();


        try {

            findViewById(R.id.fab).setOnClickListener(v -> {
                Toast.makeText(this, "Подсказка к данному разделу не готова", Toast.LENGTH_SHORT).show();
                test();
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

    private void test() {
        planogram();    // PlanogramSDB
        planogramAddr();
        planogramGrp(); // PlanogramGroupListSDB
        planogramImg(); // PlanogramImgListSDB
    }

    private void planogram() {
        // Просто планограммы
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "Просто планограммы convertedObject: " + convertedObject);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("MAIN_test", "Просто планограммы: " + response);
                Log.e("MAIN_test", "Просто планограммы body: " + response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MAIN_test", "Просто планограммы: " + t);
            }
        });
    }

    private void planogramAddr(){
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "addr_list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "Адреса планограммы convertedObject: " + convertedObject);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("MAIN_test", "Адреса планограммы: " + response);
                Log.e("MAIN_test", "Адреса планограммы body: " + response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MAIN_test", "Адреса планограммы: " + t);
            }
        });
    }

    private void planogramGrp(){
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "group_list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "Группы планограммы convertedObject: " + convertedObject);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("MAIN_test", "Группы планограммы: " + response);
                Log.e("MAIN_test", "Группы планограммы body: " + response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MAIN_test", "Группы планограммы: " + t);
            }
        });
    }

    private void planogramImg(){
        StandartData data = new StandartData();
        data.mod = "planogram";
        data.act = "img_list";
        data.nolimit = "1";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
        Log.e("MAIN_test", "Фото планограммы convertedObject: " + convertedObject);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("MAIN_test", "Фото планограммы: " + response);
                Log.e("MAIN_test", "Фото планограммы body: " + response.body());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("MAIN_test", "Фото планограммы: " + t);
            }
        });
    }
    // =================================== --- onCreate --- ========================================

    private void setActivityContent() {
        setContentView(R.layout.toolbar_new);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        TextView activity_title = (TextView) findViewById(R.id.activity_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title.setText(getString(R.string.title_activity_menu_main));
    }


}
