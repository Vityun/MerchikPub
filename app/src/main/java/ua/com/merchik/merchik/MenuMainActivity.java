package ua.com.merchik.merchik;

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
import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.Premial;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;


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

    // =================================== --- onCreate --- ========================================

    private void setActivityContent() {
        setContentView(R.layout.toolbar_new);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        TextView activity_title = (TextView) findViewById(R.id.activity_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title.setText(getString(R.string.title_activity_menu_main));
    }

    private void test(){
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "short_stats";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

/*        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("test", "onResponse: " + response);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("test", "onFailure: " + t);
            }
        });*/

        retrofit2.Call<Premial> call2 = RetrofitBuilder.getRetrofitInterface().GET_PREMIAL(RetrofitBuilder.contentType, convertedObject);
        call2.enqueue(new Callback<Premial>() {
            @Override
            public void onResponse(Call<Premial> call, Response<Premial> response) {
                Log.e("test", "onResponse: " + response);
            }

            @Override
            public void onFailure(Call<Premial> call, Throwable t) {
                Log.e("test", "onFailure: " + t);
            }
        });
    }







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
