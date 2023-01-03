package ua.com.merchik.merchik.Activities.PremiumActivity;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Activities.PremiumActivity.PremiumTable.PremiumTableHeader;
import ua.com.merchik.merchik.Activities.PremiumActivity.PremiumTable.PremiumTableHeaderAdapter;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium.Detailed;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium.PremiumPremium;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium.PremiumPremiumList;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter.DialogAdapter;
import ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter.ViewHolderTypeList;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;
import ua.com.merchik.merchik.toolbar_menus;

public class PremiumActivity extends toolbar_menus {

    private RecyclerView recycler, table;
    private TextView text, userTV;
    private TextView date, col1, col5, col2, col3, col4;
    private Spinner spinner;

    private List<PremiumTableHeader> headerArrayList = new ArrayList<>();
    private PremiumTableHeaderAdapter adapter = createTableAdapter();

    private interface PremiumRespListener {
        void onSuccess(PremiumPremiumList res);

        void onFailure(String err);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            RealmManager.setRowToLog(Collections.singletonList(
                    new LogDB(
                            RealmManager.getLastIdLogDB() + 1,
                            System.currentTimeMillis() / 1000,
                            "Факт відкриття Преміальних на стороні додатку. Успіх.",
                            387,
                            null,
                            null,
                            null,
                            null,
                            System.currentTimeMillis() / 1000,
                            Globals.session,
                            null)));
            setActivityContent();
            setActivityData();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "PremiumActivity", "Exception e: " + e);
        }
    }

    private void setActivityContent() {
        setContentView(R.layout.drawler_premium);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        TextView activity_title = (TextView) findViewById(R.id.activity_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title.setText(getString(R.string.title_activity_premium));

        recycler = findViewById(R.id.recycler_view);
        text = findViewById(R.id.text);
        date = findViewById(R.id.name);
        col1 = findViewById(R.id.col1);
        col5 = findViewById(R.id.col5);
        col2 = findViewById(R.id.col2);
        col3 = findViewById(R.id.col3);
        col4 = findViewById(R.id.col4);
        userTV = findViewById(R.id.userTV);
        spinner = findViewById(R.id.spinner);

        table = findViewById(R.id.table);
    }

    private void setActivityData() {
        setFab();
        setTextHintVisualise();
        setNavigation();
//        setRecycler();

        date.setText("Дата");
        col1.setText("Поч. Зал.");
        col5.setText("Дохід План");
        col2.setText("Дохід Факт");
        col3.setText("Витрати");
        col4.setText("Кін. Зал.");

        setUser();
        makeTableDataFirstWeek();
        setNewRecycler();
    }

    /*Судя по всему это "низ" */
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
    }


    private void setUser() {
        String textViewUserVal = "Сотрудник: ";

        userTV.setText(textViewUserVal);

        // Тут надо будет получать список пользователей
        UsersSDB usersSDB = SQL_DB.usersDao().getById(Globals.userId);

        // В данном контексте получаю лишь одного пользователя (текущего, под кем залогинился)
        String[] userList = new String[1];
        if (usersSDB == null) {
            userList[0] = "тест";
        } else {
            userList[0] = usersSDB.fio;
        }


        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, userList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String data = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(adapterView.getContext(), "Выбрали: " + data, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(adapterView.getContext(), "Ничего не выбрано", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setNewRecycler() {
        table.setAdapter(adapter);
        table.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    protected PremiumTableHeaderAdapter createTableAdapter() {
        return new PremiumTableHeaderAdapter(headerArrayList, (View view, Detailed item) -> {
            Toast.makeText(view.getContext(), "Ви клікнули, поки нічого не виконається", Toast.LENGTH_SHORT).show();
        });
    }

    private void prepareTableData(String period, PremiumPremiumList res) {
        // Должны получить список Премиальных который хотим отобразить мерчам
        // Пробегаем по списку ПРЕМИАЛЬНЫХ
        // Определяем сначала ЗАГОЛОВКИ
        // Относительно каждого ЗАГОЛОВКА находим его пункты и записываем соответственно в заголовки
        // Создаём новый экземпляр PremiumTableHeader в который запихиваем значение заголовка и значений
        // Добавляем данные в "headerArrayList"

        PremiumTableHeader.DetailedHeader header = new PremiumTableHeader.DetailedHeader();
        header.date = period;
        header.sumInitialBalance = res.total.nachOst;
//        header.sumComing = res.total.prihod;
//        header.sumConsumption = res.total.rashod;
        header.sumEndBalance = res.total.konOst;

        ArrayList<PremiumTableHeader.DetailedSubHeader> subHeaderArrayList = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        for (Detailed item : res.detailed) {
            // Если у нас в большом нашем списке нет такого подзаголовка - добавляем его
            if (subHeaderArrayList.size() == 0 || !titles.contains(item.docDefName)) {
                titles.add(item.docDefName);    // список в котором мы храним подзаголовки которые уже есть
                PremiumTableHeader.DetailedSubHeader subHeader = new PremiumTableHeader.DetailedSubHeader();
                PremiumTableHeader.DetailedHeader detailedHeader = new PremiumTableHeader.DetailedHeader();

                detailedHeader.date = item.docDefName;
                detailedHeader.sumInitialBalance = 0.0;
                detailedHeader.sumEndBalance = 0.0;
                for (Detailed currentItem : res.detailed) {
                    if (currentItem.docDefName.equals(item.docDefName)){
                        detailedHeader.sumPlan += currentItem.sumPlan;
                        detailedHeader.sumComing += currentItem.prihod;
                        detailedHeader.sumConsumption += currentItem.rashod;
                        subHeader.items.add(currentItem);
                    }
                }
                subHeader.header = detailedHeader;
                subHeaderArrayList.add(subHeader);

                header.sumPlan += detailedHeader.sumPlan;
                header.sumComing += detailedHeader.sumComing;
                header.sumConsumption += detailedHeader.sumConsumption;
            }
        }

        headerArrayList.add(new PremiumTableHeader(header, subHeaderArrayList));
        adapter.setNewData(headerArrayList);
        adapter.notifyDataSetChanged();
    }

    private void makeTableDataFirstWeek() {
        // Clock.getLastMonday(), Clock.getLastSunday();
        // Clock.getCurrentMonday(), Calendar.getInstance();

        ProgressDialog progressDialog = ProgressDialog.show(this, "Преміальні", "Завантажую минулий тиждень.", true, true);

        downloadPremium(Clock.getLastMonday(), Clock.getLastSunday(), new PremiumRespListener() {
            @Override
            public void onSuccess(PremiumPremiumList res) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                prepareTableData(getPeriodString(Clock.getLastMonday(), Clock.getLastSunday()), res);
                makeTableDataSecondWeek();
            }

            @Override
            public void onFailure(String err) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                DialogData dialog = new DialogData(PremiumActivity.this);
                dialog.setTitle("Помилка!");
                dialog.setText(err);
                dialog.setClose(dialog::dismiss);
                dialog.show();
            }
        });
    }

    private void makeTableDataSecondWeek() {
        ProgressDialog progressDialog = ProgressDialog.show(this, "Преміальні", "Завантажую поточний тиждень.", true, true);

        downloadPremium(Clock.getCurrentMonday(), Calendar.getInstance(), new PremiumRespListener() {
            @Override
            public void onSuccess(PremiumPremiumList res) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                prepareTableData(getPeriodString(Clock.getCurrentMonday(), Calendar.getInstance()), res);
            }

            @Override
            public void onFailure(String err) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                DialogData dialog = new DialogData(PremiumActivity.this);
                dialog.setTitle("Помилка!");
                dialog.setText(err);
                dialog.setClose(dialog::dismiss);
                dialog.show();
            }
        });
    }

    private void downloadPremium(Calendar dateFrom, Calendar dateTo, PremiumRespListener listener) {
        DateFormat serverDF = new SimpleDateFormat("yyyy-MM-dd");

        StandartData data = new StandartData();
        data.mod = "premium";
        data.act = "premium";
        data.date_from = serverDF.format(dateFrom.getTime());
        data.date_to = serverDF.format(dateTo.getTime());

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<JsonObject> test = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        test.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("test", "onResponse: " + response);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("test", "Throwable t: " + t);
            }
        });

        retrofit2.Call<PremiumPremium> call = RetrofitBuilder.getRetrofitInterface().GET_PREMIUM_PREMIUM(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<PremiumPremium>() {
            @Override
            public void onResponse(Call<PremiumPremium> call, Response<PremiumPremium> response) {
                Log.e("test", "onResponse: " + response);
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().state) {
                        if (response.body().list.state) {
                            if (response.body().list.detailed != null && response.body().list.detailed.size() > 0) {
                                listener.onSuccess(response.body().list);
                            } else {
                                listener.onFailure("Вибачаємось. Не змогли отримати данні о Преміальних. Зверніться до Вашго керівника.");
                            }
                        } else {
                            listener.onFailure("Виникла помилка. Запит не опрацьовано. Зверніться до Вашого керівника.");
                        }
                    } else {
                        listener.onFailure("Виникла помилка. Запит не опрацьовано або він пустий. Зверніться до Вашого керівника.");
                    }
                } else {
                    listener.onFailure("Перевірте з'єднання з інтернетом та повторіть спробу пізніше.");
                }
            }

            @Override
            public void onFailure(Call<PremiumPremium> call, Throwable t) {
                Log.e("test", "onFailure: " + t);
                listener.onFailure("Щось пішло не так: " + t);
            }
        });
    }

    private String getPeriodString(Calendar start, Calendar end) {
        DateFormat userDF = new SimpleDateFormat("dd.MM");
        return userDF.format(start.getTime()) + " - " + userDF.format(end.getTime());
    }

    //------------------------------------------------------------------------------------------------

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

    private ViewHolderTypeList createChoiceUserBlockRV() {
        ViewHolderTypeList res = new ViewHolderTypeList();

        ViewHolderTypeList.ChoiceSpinnerLayoutData block = new ViewHolderTypeList.ChoiceSpinnerLayoutData();
        block.dataTextTitle = "Сотрудник: ";

        // Тут надо будет получать список пользователей
        UsersSDB usersSDB = SQL_DB.usersDao().getById(Globals.userId);

        // В данном контексте получаю лишь одного пользователя (текущего, под кем залогинился)
        String[] userList = new String[1];
        if (usersSDB == null) {
            userList[0] = "тест";
        } else {
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

    private ViewHolderTypeList createChoiceDateBlockRV() {
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

    private ViewHolderTypeList createTextBlockRV() {
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

    private ViewHolderTypeList createTableBlockRV() {
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

    private ViewHolderTypeList.TablePremiumLayoutData.PremiumTableRow createRow(Calendar dateFrom, Calendar dateTo) {
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

    private ViewHolderTypeList createButtonRefreshBlockRV() {
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
