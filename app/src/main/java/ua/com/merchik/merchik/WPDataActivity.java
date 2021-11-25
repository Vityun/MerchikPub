package ua.com.merchik.merchik;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;
import ua.com.merchik.merchik.dialogs.DialogFilter.DialogFilter;
import ua.com.merchik.merchik.dialogs.DialogFilter.data.DFWpResult;

public class WPDataActivity extends toolbar_menus{

    private RealmResults<WpDataDB> workPlan;

    Globals globals = new Globals();

    TextView activity_title;
    EditText editsearch;
    EditText searchView;
    private ImageButton filter;

    //----------------------------------------------------------------------------------------------
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawler_wp);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title = (TextView) findViewById(R.id.activity_title);
        activity_title.setText("План работ");

        filter = findViewById(R.id.filter);

//        activity_title.setBackgroundColor(Color.parseColor("#B1B1B1"));
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //Убирает фокус с полей ввода

        workPlan = RealmManager.getAllWorkPlan();
        if (workPlan == null || workPlan.size() == 0){
//            globals.alertDialogMsg(this, "План работ пуст.\nВыполните Синхронизацию таблиц для получения Плана работ.");

            DialogData dialogData = new DialogData(this);
            dialogData.setTitle("План работ пуст.");
            dialogData.setText("Выполните Синхронизацию таблиц для получения Плана работ.");
            dialogData.setClose(dialogData::dismiss);
            dialogData.show();
        }else {
            try {
                visualizeWpData();
            }catch (Exception e) {
                globals.alertDialogMsg(this, "Возникла ошибка. Сообщите о ней своему администратору. Ошибка1: " + e);
            }
        }





        textLesson = 816;
        videoLesson = 817;
        setFab(this, findViewById(R.id.fab));

        initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(129);

    }//------------------------------- /ON CREATE --------------------------------------------------

    private RecyclerView recyclerView;
    private RecycleViewWPAdapter recycleViewWPData;
    private void visualizeWpData(){

        recyclerView = (RecyclerView) findViewById(R.id.RecyclerViewWorkPlan);

        recycleViewWPData = new RecycleViewWPAdapter(this, workPlan);
        recyclerView.setAdapter(recycleViewWPData);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        searchView = (EditText) findViewById(R.id.searchView);
        searchView.setText(Clock.today);
        searchView.clearFocus();

        recycleViewWPData.getFilter().filter(searchView.getText());

        Log.e("TAG_SEARCH_VIEW", "WP: " + searchView);

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() != 0){
                    recycleViewWPData.getFilter().filter(s);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });


        setFilter();
    }


    private void setFilter(){
        if (DialogFilter.filtered != null && DialogFilter.filtered == 1){
            filter.setImageDrawable(getResources().getDrawable(R.drawable.ic_filterbold));
        }

        filter.setOnClickListener((v)->{
            DialogFilter dialog = new DialogFilter(v.getContext(), Globals.SourceAct.WP_DATA);

            dialog.setEditText(searchView.getText().toString());
            dialog.apply(new Click() {
                @Override
                public <T> void onSuccess(T data) {
                    DFWpResult wpFilter = (DFWpResult) data;

                    if (DialogFilter.filtered == 1){
                        filter.setImageDrawable(getResources().getDrawable(R.drawable.ic_filterbold));
                    }else {
                        filter.setImageDrawable(getResources().getDrawable(R.drawable.ic_filter));
                    }

                    // Получаю данные для запроса в БД
                    workPlan = WpDataRealm.wpFilterQuery(wpFilter);
                    Toast.makeText(v.getContext(), "Отобрано: " + workPlan.size(), Toast.LENGTH_SHORT).show();
                    recycleViewWPData.updateData(workPlan);

                    // Установка новых данных в search
                    searchView.setText(wpFilter.editText);

                    // Производим поиск по новым данным
                    recycleViewWPData.getFilter().filter(searchView.getText());
//                    recycleViewWPData.notifyDataSetChanged();
                }

                @Override
                public void onFailure(String error) {
                }
            });

            dialog.setClose(dialog::dismiss);
            dialog.show();
        });
    }





}
