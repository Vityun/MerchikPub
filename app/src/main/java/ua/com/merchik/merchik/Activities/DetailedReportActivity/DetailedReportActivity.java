package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto;
import ua.com.merchik.merchik.PhotoReportActivity;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.Translate;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.toolbar_menus;

import static ua.com.merchik.merchik.PhotoReportActivity.getImageOrientation;
import static ua.com.merchik.merchik.PhotoReportActivity.resaveBitmap;

public class DetailedReportActivity extends toolbar_menus {

    private Translate translate = new Translate();
    private WorkPlan workPlan = new WorkPlan();
    private WpDataDB wpDataDB;
    private WpDataDB rowWP;

    private WPDataObj wpDataObj;

    private File image;

    public static FloatingActionButton fab;
    
    // ----- ПЕРЕМЕННЫЕ ДЛЯ МОДУЛЯ -----
    public static int rpThemeId = 0;    // ID темы Отчёта исполнителя
    public static int rpAmountSum = 0;    // Сумма колонци КОЛИЧЕСТВО RP данного Документа
    public static int rpCount = 0;    // Количество записей в ReportPrepare(дад2 не 0) для данного Документа
    public static int rpAmountSum2 = 0;   // Итоговое количество
    public static double rpTotalSumToRedemptionOfGoods = 0;   // Общая сумма для Выкупа товара
    //==================================
    

    TabLayout tabLayout;
    ViewPager viewPager;

    // Интерфейс
    TextView activity_title;
    TextView textDRDateV, textDRAddrV, textDRCustV, textDRMercV;
    Button buttonTakeKPSfromDR;
    LinearLayout option_signal_layout2;

    public ArrayList<Data> list = new ArrayList<Data>();

    //----------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        globals.writeToMLOG(Clock.getHumanTime() + "DetailedReportActivity.onCreate: " + "ENTER" + "\n");
//        try {
        setContentView(R.layout.drawler_dr);

        setSupportActionBar(findViewById(R.id.my_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title = findViewById(R.id.activity_title);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);


        Log.e("TRANSLATES_DEBUG", "Globals.langId: " + Globals.langId);

        TranslatesSDB home = translate.getTranslationText("DetailedReportHomeFrag_Title");
        if (home != null) {
            tabLayout.getTabAt(0).setText(home.nm);
        }

        TranslatesSDB option = translate.getTranslationText("DetailedReportOptionsFrag_Title");
        if (option != null) {
            tabLayout.getTabAt(1).setText(option.nm);
        }

        TranslatesSDB tovars = translate.getTranslationText("DetailedReportTovarsFrag_Title");
        if (tovars != null) {
            tabLayout.getTabAt(2).setText(tovars.nm);
        }

        tabLayout.getTabAt(3).setText("ЗиР");


        Intent i = getIntent();
        rowWP = (WpDataDB) i.getSerializableExtra("rowWP");
        Data wp = (Data) i.getSerializableExtra("dataFromWP");

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

//            try {
        list.addAll(Collections.singleton(wp));
        Log.e("DetailedReportA", "list: " + list);

        wpDataDB = RealmManager.getWorkPlanRowById(list.get(0).getId());
        Log.e("DetailedReportA", "wpDataDB: " + wpDataDB.getId());

//        String s = getResources().getString(R.string.title_detailed_report);
//        String titleDr = String.format("%s%s", s, wpDataDB.getCode_dad2());

        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(wpDataDB.getDt().substring(5)).append(".. ").append(wpDataDB.getAddr_txt().substring(0, 25)).append(".. ").append("\n");
            stringBuilder.append(wpDataDB.getClient_txt().substring(0, 15)).append(".. ").append(wpDataDB.getUser_txt().substring(0, 15)).append(".. ");
        }catch (Exception e){
            stringBuilder.append("Дет. Отчёт №: ").append(wpDataDB.getCode_dad2());
        }

//        activity_title.setTextSize(10);

        activity_title.setText(stringBuilder);
        activity_title.setBackgroundColor(Color.parseColor("#B1B1B1"));

        setTab();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //Убирает фокус с полей ввода

//            }catch (Exception e){
//                Log.e("ERROR", "WRITE_TO_LOG_TABLE: " + e);
//            }
//        }catch (Exception e){
//            globals.alertDialogMsg(this, "Ошибка в детализированном отчёте: " + e);
//        }


        try {
            globals.writeToMLOG(Clock.getHumanTime() + "DetailedReportActivity.onCreate.fab: " + "ENTER" + "\n");
            fab = findViewById(R.id.fab);

            toolbar_menus.textLesson = 818;
            toolbar_menus.videoLesson = 819;
            toolbar_menus.setFab(DetailedReportActivity.this, DetailedReportActivity.fab); // ГЛАВНАЯ

            Log.e("ЧТО_ПРОИСХОДИТ", "DetailedReportActivity");

            initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));
            NavigationView navigationView;
            navigationView = findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_dr);
        } catch (Exception e) {
            globals.writeToMLOG(Clock.getHumanTime() + "DetailedReportActivity.onCreate.fab.e: " + e + "\n");
            e.printStackTrace();
        }
        
        
        // функционал для работы модуля
        RENAME(wpDataDB);   // подсчёт данных для Выкупа и заказа товаров
        rpThemeId = wpDataDB.getTheme_id();

    }//--------------------------------------------------------------------- /ON CREATE ---------------------------------------------------------------------


/*    @Override
    protected void onStop() {
        finish();
        super.onStop();
    }*/


    DetailedReportTab adapter;
    private void setTab() {
        List<TovarDB> dataTovar = RealmManager.getTovarListFromReportPrepareByDad2(wpDataDB.getCode_dad2());
        List<TovarDB> dataTovarDownloadList = RealmManager.getTovarListPhotoToDownload(dataTovar, "small");

        TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading();
        tablesLoadingUnloading.getTovarImg(dataTovar, "small");

        adapter = new DetailedReportTab(this, getSupportFragmentManager(), tabLayout.getTabCount(), list, rowWP);
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                Log.e("onTabSelected", "tabLayout.getTabCount(): " + tabLayout.getTabCount());
                Log.e("onTabSelected", "tab.getPosition(): " + tab.getPosition());

                if (tab.getPosition() == 0) {
                    Log.e("onTabSelected", "ГЛАВНАЯ");

                    toolbar_menus.textLesson = 818;
                    toolbar_menus.videoLesson = 819;
                    toolbar_menus.setFab(DetailedReportActivity.this, DetailedReportActivity.fab); // ГЛАВНАЯ

                } else if (tab.getPosition() == 1) {
                    Log.e("onTabSelected", "ОПЦИИ");

                    toolbar_menus.textLesson = 820;
                    toolbar_menus.videoLesson = 821;
                    toolbar_menus.setFab(DetailedReportActivity.this, DetailedReportActivity.fab); // ОПЦИИ

                } else if (tab.getPosition() == 2) {
                    Log.e("onTabSelected", "ТОВАРЫ");

                    toolbar_menus.textLesson = 822;
                    toolbar_menus.videoLesson = 823;
                    toolbar_menus.setFab(DetailedReportActivity.this, DetailedReportActivity.fab); // ТОВАР

                }

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


    public void refreshAdapterFragmentB(){
        DetailedReportTab.refreshAdapter();
    }


    // Размещение фотки по URI адрессу для пользователя, отображение фотографии загрузки
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Log.d("test", "" + requestCode + resultCode + intent);

        switch (requestCode){
            case 101:
                try {
                    globals.writeToMLOG(Clock.getHumanTime() + "DETAILED_REPORT_ACT.onActivityResult: " + "ENTER" + "\n");
                    image = MakePhoto.image;
                    globals.writeToMLOG(Clock.getHumanTime() + "DETAILED_REPORT_ACT.onActivityResult.image: " + image + "\n");

                    Log.e("dispatchTakePicture", "image: " + image);
                    Log.e("dispatchTakePicture", "imagelength: " + image.length());
                    Log.e("dispatchTakePicture", "imagegetAbsolutePath: " + image.getAbsolutePath());

                    String msg = "";
                    msg = String.format("image: %s \nimagelength: %s\nimagegetAbsolutePath: %s\nresultCode: %s\nrequestCode: %s", image, image.length(), image.getAbsolutePath(), resultCode, requestCode);

                    globals.writeToMLOG(Clock.getHumanTime() + "DETAILED_REPORT_ACT.onActivityResult.image.data: " + msg + "\n");
                } catch (Exception e) {
                    e.printStackTrace();
                    Globals.writeToMLOG("ERROR", ".DetailedReportActivity.DetailedReportActivity.onActivityResult (DetailedReportActivity.java:238)", "Exception e: " + e);

                    DialogData dialog = new DialogData(DetailedReportActivity.this);
                    dialog.setTitle("Ошибка при выполнении фото.");
                    dialog.setText("Возникла ошибка при выполнении фото, передайте это сообщение своему руководителю и повторите попытку выполнения фото. \nОшибка: " + e);
                    dialog.setClose(dialog::dismiss);
                    dialog.show();
                }
                break;
        }



        try {
            // Если отменили сьемку:
            if (resultCode == Activity.RESULT_CANCELED && requestCode == 101) image.delete();

            // Если сьемка успешная:
            if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
                if (image != null && image.exists()) {
                    if (image.length() > 0) {       //

                        final int rotation = getImageOrientation(image.getPath()); //Проверка на сколько градусов повёрнуто изображение
                        if (rotation > 0) {
                            image = resaveBitmap(image, rotation);  // ДляСамсунгов и тп.. Разворачиваем как надо.
                        }

                        wpDataObj = MakePhoto.wp;
                        Log.e("getPhotoType", "wpDataObj.getPhotoType(): " + wpDataObj.getPhotoType());

                        PhotoReportActivity.savePhoto(this, wpDataObj, image);

                        refreshAdapterFragmentB();

                        Toast.makeText(this, "Фото сделано, и сохранено", Toast.LENGTH_LONG).show();


                    } else { // Если фото получилось нулевым - файлик удаляется
                        try {
                            image.delete();
                        } catch (Exception e) {e.printStackTrace();

                        }
                    }
                } else {
                    globals.alertDialogMsg(this, "Фото не было создано, повторите попытку");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            globals.alertDialogMsg(this, "Ошибка при выполнении фото: " + e);
        }
    }



    /**
     * 29.06.2021
     * */
    public void RENAME(WpDataDB wp){
        try {
            RealmResults<ReportPrepareDB> reportPrepareDB = ReportPrepareRealm.getReportPrepareByDad2(wp.getCode_dad2());
            List<ReportPrepareDB> rows = reportPrepareDB.where().notEqualTo("codeDad2", "0").findAll();

            rpAmountSum = reportPrepareDB.sum("amount").intValue();
            rpCount = rows.size();

            for (ReportPrepareDB item : rows) {
                int curAmount = item.getAmount();
                double price = Double.parseDouble(item.getPrice());
                rpAmountSum2 += curAmount;
                rpTotalSumToRedemptionOfGoods += curAmount * price;
            }
        }catch (Exception e){
            e.printStackTrace();
            // TODO =__=
        }

        Log.e("DetailedReportActivity", "rpAmountSum: " + rpAmountSum);
        Log.e("DetailedReportActivity", "rpCount: " + rpCount);
        Log.e("DetailedReportActivity", "rpAmountSum2: " + rpAmountSum2);
        Log.e("DetailedReportActivity", "rpTotalSumToRedemptionOfGoods: " + rpTotalSumToRedemptionOfGoods);
    }


    public static void makeRealPhoto(){

    }
}



