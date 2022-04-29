package ua.com.merchik.merchik.Activities.WorkPlanActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.RecycleViewWPAdapter;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogFilter.DialogFilter;
import ua.com.merchik.merchik.dialogs.DialogFilter.data.DialogFilterResult;
import ua.com.merchik.merchik.toolbar_menus;

public class WPDataActivity extends toolbar_menus {

    private RealmResults<WpDataDB> workPlan;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private FragmentManager fragmentManager;
    private WPDataFragmentHome homeFrag;
    private WPDataFragmentMap mapFrag;


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
//        activity_title = (TextView) findViewById(R.id.activity_title);
//        activity_title.setText("План работ");

        filter = findViewById(R.id.filter);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

//        activity_title.setBackgroundColor(Color.parseColor("#B1B1B1"));
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //Убирает фокус с полей ввода

/*        workPlan = RealmManager.getAllWorkPlan();
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
        }*/



        // Установка закладок
        setTabs();


        textLesson = 816;
        videoLesson = 817;
        setFab(this, findViewById(R.id.fab));

        initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(129);

    }//------------------------------- /ON CREATE --------------------------------------------------

    private RecyclerView recyclerView;
    private RecycleViewWPAdapter adapter;
    private void visualizeWpData(){

        recyclerView = (RecyclerView) findViewById(R.id.RecyclerViewWorkPlan);

        adapter = new RecycleViewWPAdapter(this, workPlan);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        searchView = (EditText) findViewById(R.id.searchView);
        searchView.setText(Clock.today);
        searchView.clearFocus();

        adapter.getFilter().filter(searchView.getText());

        Log.e("TAG_SEARCH_VIEW", "WP: " + searchView);

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() != 0){
                    adapter.getFilter().filter(s);
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


    private void setTabs(){
        tabLayout.getTabAt(0).setText("План работ");
        tabLayout.getTabAt(1).setText("Карта");

        fragmentManager = getSupportFragmentManager();
        WPDataTab tabAdapter = new WPDataTab(fragmentManager, tabLayout.getTabCount());
        viewPager.setAdapter(tabAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    // 998
    private void setFilter(){
        if (DialogFilter.filtered != null && DialogFilter.filtered == 1){
            filter.setImageDrawable(getResources().getDrawable(R.drawable.ic_filterbold));
        }

        filter.setOnClickListener((v)->{
            DialogFilter dialog = new DialogFilter(v.getContext(), Globals.SourceAct.WP_DATA);

            dialog.setEditText(searchView.getText().toString());
            dialog.setFilterFields();

            dialog.clickApply(new Clicks.click() {
                @Override
                public <T> void click(T data) {
                    DialogFilterResult dialogResult = (DialogFilterResult) data;

                    searchView.setText(dialogResult.searchField);

                    List<WpDataDB> wp = WpDataRealm.wpFiltered(dialogResult.addressId, dialogResult.customerId, dialogResult.executorId, dialogResult.themeId);
                    adapter.updateData(wp);
                    adapter.notifyDataSetChanged();

                    adapter.getFilter().filter(searchView.getText());
                }
            });

            dialog.setClose(dialog::dismiss);
            dialog.show();
        });
    }





}
