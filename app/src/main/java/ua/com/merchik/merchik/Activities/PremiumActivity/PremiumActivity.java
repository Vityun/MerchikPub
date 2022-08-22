package ua.com.merchik.merchik.Activities.PremiumActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium.PremiumPremium;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter.DialogAdapter;
import ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter.ViewHolderTypeList;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;
import ua.com.merchik.merchik.toolbar_menus;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class PremiumActivity extends toolbar_menus {

    private RecyclerView recycler;
    private TextView text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        try {
            setActivityContent();
            setActivityData();
//        } catch (Exception e) {
//            globals.alertDialogMsg(this, "Ошибка при открытии странички: " + e);
//        }
    }

    private void setActivityContent() {
        setContentView(R.layout.drawler_premium);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        TextView activity_title = (TextView) findViewById(R.id.activity_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title.setText(getString(R.string.title_activity_premium));

        recycler = findViewById(R.id.recycler_view);
        text = findViewById(R.id.text);
    }

    private void setActivityData() {
        setFab();
        setTextHintVisualise();
        setNavigation();
        setRecycler();
    }

    private void setTextHintVisualise() {
        text.setOnClickListener(view -> Toast.makeText(view.getContext(), text.getText(), Toast.LENGTH_LONG).show());
    }

    private void setFab() {
        findViewById(R.id.fab).setOnClickListener(v -> {
            Toast.makeText(this, "Подсказка к данному разделу не готова", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.fab).setOnLongClickListener(v -> {
            Toast.makeText(this, "Отладочная информация!\nДолгий клик по подсказке.", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void setNavigation() {
        initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));
        NavigationView navigationView = findViewById(R.id.nav_view);
//        navigationView.setCheckedItem();
    }

    private void setRecycler() {
        recycler.setAdapter(createAdapter());
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private DialogAdapter createAdapter() {
        List<ViewHolderTypeList> data = new ArrayList<>();
        data.add(createChoiceUserBlockRV()); // Спиннер, выбор сотрудника
//        data.add(createChoiceDateBlockRV()); // Блок с выбором дат.
//        data.add(createTextBlockRV()); // Текстовое поле с формулой
        data.add(createTableBlockRV()); // Табличка с "Периодом"
//        data.add(null); // Табличка с "Подотчётом"
//        data.add(createButtonRefreshBlockRV()); // Кнопка "Обновить"
        return new DialogAdapter(data);
    }

    private ViewHolderTypeList createChoiceUserBlockRV(){
        ViewHolderTypeList res = new ViewHolderTypeList();

        ViewHolderTypeList.ChoiceSpinnerLayoutData block = new ViewHolderTypeList.ChoiceSpinnerLayoutData();
        block.dataTextTitle = "Сотрудник: ";

        // Тут надо будет получать список пользователей
        UsersSDB usersSDB = SQL_DB.usersDao().getById(Globals.userId);

        // В данном контексте получаю лишь одного пользователя (текущего, под кем залогинился)
        String[] userList = new String[1];
        if (usersSDB == null){
            userList[0] = "тест";
        }else {
            userList[0] = usersSDB.fio;
        }

//        userList[1] = "Тестовый пользователь";

        // В принципе тут должен получать пользователей списком, тех кто доступен руководителю.
        // Если пользователь один - отображаем ТОЛЬКО его без возможности выбора, если их много -
        // можно выбирать (ТОЛЛЬКО для руководителей)
//        userList = new String[]{"Пользователь 1", "Пользователь 2", "Пользователь 3", "Пользователь 4", "Пользователь N"};

        block.dataSpinner = userList;
        block.click = new ViewHolderTypeList.ClickData() {
            @Override
            public <T> void onSuccess(T data) {
                Toast.makeText(PremiumActivity.this, "" + data, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(PremiumActivity.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        };

        res.type = 5;
        res.choiceSpinnerLayoutData = block;

        return res;
    }

    private ViewHolderTypeList createChoiceDateBlockRV(){
        ViewHolderTypeList res = new ViewHolderTypeList();

        ViewHolderTypeList.ChoiceDateLayoutData block = new ViewHolderTypeList.ChoiceDateLayoutData();
        block.dataTextTitle = "Дата с: ";
        block.dataTextTitle2 = "дата по: ";
        block.dateFrom = Calendar.getInstance().getTime();
        block.dateTo = Calendar.getInstance().getTime();
        block.click = new ViewHolderTypeList.ClickData() {
            @Override
            public <T> void onSuccess(T data) {
                Toast.makeText(PremiumActivity.this, "" + data, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(PremiumActivity.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        };

        res.type = 6;
        res.choiceDateLayoutData = block;

        return res;
    }

    private ViewHolderTypeList createTextBlockRV(){
        ViewHolderTypeList res = new ViewHolderTypeList();

        ViewHolderTypeList.TextLayoutData block = new ViewHolderTypeList.TextLayoutData();
        block.data = "Формула для расчёта зарплаты мерчандайзера";
        block.click = new ViewHolderTypeList.TextLayoutData.Click() {
            @Override
            public <T> void onSuccess(T data) {
                Toast.makeText(PremiumActivity.this, "" + data, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {

            }
        };

        res.type = 0;
        res.textBlock = block;

        return res;
    }

    private ViewHolderTypeList createTableBlockRV(){
        ViewHolderTypeList res = new ViewHolderTypeList();

        ViewHolderTypeList.TablePremiumLayoutData block = new ViewHolderTypeList.TablePremiumLayoutData();

        block.title = "Период";
        block.titleColumn1 = "Нач. Ост.";
        block.titleColumn2 = "Прих.";
        block.titleColumn3 = "Расх.";
        block.titleColumn4 = "Кон. Ост.";

        // ----------------------------

        List<ViewHolderTypeList.TablePremiumLayoutData.PremiumTableRow> table = new ArrayList();

        table.add(createRow(Clock.getLastMonday(), Clock.getLastSunday()));
        table.add(createRow(Clock.getCurrentMonday(), Calendar.getInstance()));

        // ----------------------------

        block.table = table;


        res.type = 7;
        res.tablePremiumLayoutData = block;

        return res;
    }

    private ViewHolderTypeList.TablePremiumLayoutData.PremiumTableRow createRow(Calendar dateFrom, Calendar dateTo){
        ViewHolderTypeList.TablePremiumLayoutData.PremiumTableRow row = new ViewHolderTypeList.TablePremiumLayoutData.PremiumTableRow();

        row.click = new ViewHolderTypeList.ClickData() {
            @Override
            public <T> void onSuccess(T data) {
                Toast.makeText(PremiumActivity.this, "" + data, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(PremiumActivity.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        };

        DateFormat serverDF = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat userDF = new SimpleDateFormat("dd-MM");

        StandartData data = new StandartData();
        data.mod = "premium";
        data.act = "premium";
        data.date_from = serverDF.format(dateFrom.getTime());
        data.date_to = serverDF.format(dateTo.getTime());

//        data.date_from = "2022-05-09";
//        data.date_to = "2022-05-15";

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<PremiumPremium> call = RetrofitBuilder.getRetrofitInterface().GET_PREMIUM_PREMIUM(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<PremiumPremium>() {
            @Override
            public void onResponse(Call<PremiumPremium> call, Response<PremiumPremium> response) {
                Log.e("test", "onResponse: " + response);

                int nachOst = (int) response.body().list.total.nachOst;
                int prihod = (int) response.body().list.total.prihod;
                int rashod = (int) response.body().list.total.rashod;
                int konOst = (int) response.body().list.total.konOst;

                row.titleColumn = userDF.format(dateFrom.getTime()) + " - " + userDF.format(dateTo.getTime());
                row.column1 = "" + nachOst;
                row.column2 = "" + prihod;
                row.column3 = "" + rashod;
                row.column4 = "" + konOst;

                row.data = response.body().list;

                row.clicks.click();
            }

            @Override
            public void onFailure(Call<PremiumPremium> call, Throwable t) {
                Log.e("test", "onFailure: " + t);
            }
        });

        return row;
    }

    private ViewHolderTypeList createButtonRefreshBlockRV(){
        ViewHolderTypeList res = new ViewHolderTypeList();

        ViewHolderTypeList.ButtonLayoutData block = new ViewHolderTypeList.ButtonLayoutData();

        block.data = "Обновить";
        block.click = new ViewHolderTypeList.ButtonLayoutData.Click() {
            @Override
            public <T> void onSuccess(T data) {
                Toast.makeText(PremiumActivity.this, "" + data, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(PremiumActivity.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        };

        res.type = 2;
        res.buttonBlock = block;

        return res;
    }
}
