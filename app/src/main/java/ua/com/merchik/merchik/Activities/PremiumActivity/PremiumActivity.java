package ua.com.merchik.merchik.Activities.PremiumActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter.DialogAdapter;
import ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter.ViewHolderTypeList;
import ua.com.merchik.merchik.toolbar_menus;

public class PremiumActivity extends toolbar_menus {

    private RecyclerView recycler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityContent();

        setActivityData();

        try {
            initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_home);
        } catch (Exception e) {
            globals.alertDialogMsg(this, "ОшибкаMain: " + e);
        }
    }


    private void setActivityContent() {
        setContentView(R.layout.drawler_premium);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        TextView activity_title = (TextView) findViewById(R.id.activity_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title.setText(getString(R.string.title_activity_premium));

        recycler = findViewById(R.id.recycler_view);
    }


    private void setActivityData() {
        setFab();
        setRecycler();
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

    private void setRecycler() {
        DialogAdapter adapter = createRecyclerAdapter();

        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    /*
    * Создание адаптера с элементами для активности
    * */
    private DialogAdapter createRecyclerAdapter(){
        List<ViewHolderTypeList> data = new ArrayList<>();

//        data.add(userChoice());
//        data.add(buttonRegistration(data.get(0), click));

        return new DialogAdapter(data);
    }

    private ViewHolderTypeList userChoice(){
        ViewHolderTypeList res = new ViewHolderTypeList();
//        res.type = ;
//        res.block = block;
        return res;

        /*
        ViewHolderTypeList.AutoTextLayoutData autoTextLayoutData = new ViewHolderTypeList.AutoTextLayoutData();
        autoTextLayoutData.dataTextAutoTextHint = "ЕДРПОУ или Название компании";
        autoTextLayoutData.result = "";
        autoTextLayoutData.click = new ViewHolderTypeList.AutoTextLayoutData.Click() {
            @Override
            public <T> void onSuccess(T data) {
                EDRPOUResponse item = (EDRPOUResponse) data;
                autoTextLayoutData.result = item.label;
                autoTextLayoutData.resultData = item;
                Toast.makeText(menu_login.this, "Выбран клиент с ID: " + item.clientId, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(menu_login.this, "onFailure", Toast.LENGTH_LONG).show();
            }
        };

        res.type = 3;
        res.autoTextBlock = autoTextLayoutData;*/
    }



}
