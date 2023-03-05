package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.Tab3Fragment.TARCommentIndex;
import static ua.com.merchik.merchik.MakePhoto.CAMERA_REQUEST_PROMOTION_TOV_PHOTO;
import static ua.com.merchik.merchik.MakePhoto.CAMERA_REQUEST_TAR_COMMENT_PHOTO;
import static ua.com.merchik.merchik.Options.Controls.OptionControlPhotoPromotion.tovarDBOPTION_CONTROL_PROMOTION_ID;
import static ua.com.merchik.merchik.Options.Controls.OptionControlPhotoPromotion.wpDataDBOPTION_CONTROL_PROMOTION_ID;
import static ua.com.merchik.merchik.PhotoReportActivity.exifPhotoData;
import static ua.com.merchik.merchik.PhotoReportActivity.getImageOrientation;
import static ua.com.merchik.merchik.PhotoReportActivity.resaveBitmap;
import static ua.com.merchik.merchik.PhotoReportActivity.resizeImageFile;
import static ua.com.merchik.merchik.data.RealmModels.StackPhotoDB.PHOTO_PROMOTION_TOV;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Response;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARFragmentHome;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto;
import ua.com.merchik.merchik.PhotoReportActivity;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.Translate;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;
import ua.com.merchik.merchik.toolbar_menus;

public class DetailedReportActivity extends toolbar_menus {

    private Translate translate = new Translate();
    private WorkPlan workPlan = new WorkPlan();
    private WpDataDB wpDataDB;
    private WpDataDB rowWP;

    private WPDataObj wpDataObj;

    private File image;

    public static FloatingActionButton fab;
    public static List<TasksAndReclamationsSDB> tarList;
    private List<TasksAndReclamationsSDB> tasksAndReclamationsSDBList;
    public static boolean additionalRequirementsFilter = true;  // true - по наличию в RP. false - отображаем ВСЁ


    // ----- ПЕРЕМЕННЫЕ ДЛЯ МОДУЛЯ -----
    public static int rpThemeId = 0;    // ID темы Отчёта исполнителя
    public static int rpAmountSum = 0;    // Сумма колонци КОЛИЧЕСТВО RP данного Документа
    public static int rpCount = 0;    // Количество записей в ReportPrepare(дад2 не 0) для данного Документа
    public static int rpAmountSum2 = 0;   // Итоговое количество
    public static double rpTotalSumToRedemptionOfGoods = 0;   // Общая сумма для Выкупа товара
    // ----- ПЛАН ПО АСОРТИМЕНТУ -----
    public static double SKUPlan = 0;
    public static double SKUFact = 0;
    public static double OFS = 0;   // % сколько нет товаров
    public static double OOS = 0;   // Представленность %
    public static RealmResults<TovarDB> detailedReportTovList = null;
    public static RealmResults<ReportPrepareDB> detailedReportRPList = null;
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

        Globals.writeToMLOG("INFO", "DetailedReportActivity/onCreate", "Открыли по новой активность. Смотри Выше лог - после чего именно.");

        setActivityData();

        SKUPlan = 0;
        SKUFact = 0;
        OFS = 0;   // % сколько нет товаров
        OOS = 0;   // Представленность %

        detailedReportTovList = null;   // Обнуляю переменные для свеже открытого отчёта.
        detailedReportRPList = null;    // Обнуляю переменные для свеже открытого отчёта.

        // Задачи для Закладочки "ЗИР"
        tarList = SQL_DB.tarDao().getAllByInfo(1, wpDataDB.getClient_id(), wpDataDB.getAddr_id(), (System.currentTimeMillis() / 1000 - 5184000), 0);
        tasksAndReclamationsSDBList = SQL_DB.tarDao().getAllByInfo(0, wpDataDB.getAddr_id());

        globals.writeToMLOG(Clock.getHumanTime() + "DetailedReportActivity.onCreate: " + "ENTER" + "\n");

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

        // Установка Заголовка в закладочку "ЗИР". В скобочках нужно написать кол-во самих задач
        StringBuffer tarTabTitle = new StringBuffer();
        tarTabTitle.append("ЗИР");
        if (tarList != null && tarList.size() > 0) {
            tarTabTitle.append("(");
            if (tasksAndReclamationsSDBList != null && tasksAndReclamationsSDBList.size() > 0){
                tarTabTitle.append("<font color='red'>").append(tasksAndReclamationsSDBList.size()).append("</font>").append("/");
            }
            tarTabTitle.append("<font color='red'>").append(tarList.size()).append("</font>");
            tarTabTitle.append(")");
        }
        tabLayout.getTabAt(3).setText(Html.fromHtml(String.valueOf(tarTabTitle)));


        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000).substring(5)).append(".. ").append(wpDataDB.getAddr_txt().substring(0, 25)).append(".. ").append("\n");    //+TODO CHANGE DATE
            stringBuilder.append(wpDataDB.getClient_txt().substring(0, 12)).append(".. ").append(wpDataDB.getUser_txt().substring(0, 12)).append(".. ");
        } catch (Exception e) {
            stringBuilder.append("Дет. Отчёт №: ").append(wpDataDB.getCode_dad2());
        }


        activity_title.setText(stringBuilder);
        activity_title.setBackgroundColor(Color.parseColor("#B1B1B1"));

        setTab();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //Убирает фокус с полей ввода


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

        // Если у Магазина пустые координаты - запускаем
        if (wpDataDB.getAddr_location_xd() == null || wpDataDB.getAddr_location_xd().isEmpty() || wpDataDB.getAddr_location_xd().equals("0")) {
            setDialogAddCoordinate();
        } else {
            Log.e("test", "test");
        }

    }//--------------------------------------------------------------------- /ON CREATE ---------------------------------------------------------------------

    /*Получаю строчку с Пална работ и храню её для Отчёта*/
    private void setActivityData() {
        Intent i = getIntent();
        rowWP = (WpDataDB) i.getSerializableExtra("rowWP");
        Data wp = (Data) i.getSerializableExtra("dataFromWP");

        list.addAll(Collections.singleton(wp));

        wpDataDB = RealmManager.getWorkPlanRowById(list.get(0).getId());
    }

    // Основное сообщение
    private void setDialogAddCoordinate() {
        DialogData dialog = new DialogData(this);
        dialog.setTitle("Не определены координаты адреса");
        dialog.setDialogIco();
        dialog.setText("Внимание. У адреса " + wpDataDB.getAddr_txt() + " не определены гео координаты местоположения! \nДля того что бы определить эти координаты: \n 1. Приедьте по указанному адресу; \n 2. Убедитесь что приложение ПОДКЛЮЧЕНО к серверу; \n 3. Откройте текущую форму и нажмите кнопку \"Определить координаты адреса\"");
        dialog.setOk("Определить координаты адреса", () -> {
            setDialogAddCoordinateQuestion();
            dialog.dismiss();
        });
        dialog.setClose(dialog::dismiss);
        dialog.show();
    }

    // Вопрос
    private void setDialogAddCoordinateQuestion() {
        DialogData dialog = new DialogData(this);
        dialog.setDialogIco();
        dialog.setText("Вы сейчас НЕ находитесь по адресу " + wpDataDB.getAddr_txt() + " ?");
        dialog.setOk("Да", () -> {
            DialogData dialogData = new DialogData(dialog.context);
            dialogData.setDialogIco();
            dialogData.setText("Тогда приедьте, пожалуйста, по адресу и повторите процедуру.");
            dialogData.setClose(dialogData::dismiss);
            dialogData.show();
            dialog.dismiss();
        });
        dialog.setCancel("Нет", () -> {
            setDialogAddCoordinateReQuestion();
            dialog.dismiss();
        });
        dialog.setClose(dialog::dismiss);
        dialog.show();
    }

    // Перевопрос
    private void setDialogAddCoordinateReQuestion() {
        DialogData dialog = new DialogData(this);
        dialog.setDialogIco();
        dialog.setText("Вы находитесь по адресу " + wpDataDB.getAddr_txt() + " ?");
        dialog.setOk("Да", () -> {
            sendNewAddressCoordinate();
            dialog.dismiss();
        });
        dialog.setCancel("Нет", () -> {
            setDialogAddCoordinateQuestion();
            dialog.dismiss();
        });
        dialog.setClose(dialog::dismiss);
        dialog.show();
    }

    public void sendNewAddressCoordinate() {

        if (ua.com.merchik.merchik.trecker.enabledGPS) {
            if (wpDataDB != null) {
                if (Globals.CoordX != 0 && Globals.CoordY != 0) {
                    StandartData standartData = new StandartData();
                    standartData.mod = "data_list";
                    standartData.act = "set_addr_geo";

                    standartData.addr_id = wpDataDB.getAddr_id();
                    standartData.x = Globals.CoordX;
                    standartData.y = Globals.CoordY;

                    Gson gson = new Gson();
                    String json = gson.toJson(standartData);
                    JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                    retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
                    call.enqueue(new retrofit2.Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            Log.e("test", "response: " + response);
//                            Toast.makeText(DetailedReportActivity.this, "OK", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                            Log.e("test", "Throwable: " + t);
                            Toast.makeText(DetailedReportActivity.this, "ERR" + t, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(DetailedReportActivity.this, "Перезагрузите GPS модуль или выйдите на улицу и повторите попытку.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(DetailedReportActivity.this, "Перезадите в детализированный отчёт и повторите попытку.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(DetailedReportActivity.this, "Включите GPS и повторите попытку.", Toast.LENGTH_LONG).show();
        }


    }

    DetailedReportTab adapter;

    private void setTab() {
        List<TovarDB> dataTovar = RealmManager.getTovarListFromReportPrepareByDad2(wpDataDB.getCode_dad2());
        if (dataTovar != null){
            List<TovarDB> dataTovarDownloadList = RealmManager.getTovarListPhotoToDownload(dataTovar, "small");
            TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading();
            tablesLoadingUnloading.getTovarImg(dataTovar, "small");
        }

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


    public void refreshAdapterFragmentB() {
        DetailedReportTab.refreshAdapter();
    }


    // Размещение фотки по URI адрессу для пользователя, отображение фотографии загрузки
    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult", "requestCode / resultCode / data: " + requestCode + "/" + resultCode + "/" + data);

        switch (requestCode) {
            case 101:
                try {
                    globals.writeToMLOG(Clock.getHumanTime() + "DETAILED_REPORT_ACT.onActivityResult: " + "ENTER" + "\n");
                    image = MakePhoto.image;
                    globals.writeToMLOG(Clock.getHumanTime() + "DETAILED_REPORT_ACT.onActivityResult.image: " + image + "\n");

                    Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/101", "Image: " + image);

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


        if (requestCode == 201 && resultCode == RESULT_OK) {
            Toast.makeText(this, "Фото сохранено", Toast.LENGTH_SHORT).show();
            try {
                Globals.writeToMLOG("INFO", "requestCode == 201 && resultCode == RESULT_OK/MakePhoto_photoNum", "MakePhoto.photoNum: " + MakePhoto.photoNum);

                StackPhotoDB photo = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getByPhotoNum(MakePhoto.photoNum));
                File photoFile = new File(MakePhoto.photoNum);

                JsonObject jsonObject = new Gson().fromJson(new Gson().toJson(photo), JsonObject.class);

                Globals.writeToMLOG("INFO", "requestCode == 201 && resultCode == RESULT_OK/photo", "photo: " + jsonObject);
                Globals.writeToMLOG("INFO", "requestCode == 201 && resultCode == RESULT_OK/photoFile", "photoFile: " + photoFile);

                final int rotation = getImageOrientation(photoFile.getPath()); //Проверка на сколько градусов повёрнуто изображение
                if (rotation > 0) {
                    photoFile = resaveBitmap(photoFile, rotation);  // ДляСамсунгов и тп.. Разворачиваем как надо.
                }

                try {
                    photoFile = resizeImageFile(this, photoFile);
                } catch (Exception e) {
                    globals.alertDialogMsg(this, "Ошибка В ужатии: " + e);
                }

                exifPhotoData(photoFile);

                String hash;
                hash = globals.getHashMD5FromFile2(photoFile, this);

                if (hash == null || hash.equals("")) {
                    hash = globals.getHashMD5FromFile(photoFile, this);
                }

                photo.setPhoto_hash(hash);
                photo.setPhoto_num(photoFile.getAbsolutePath());
                photo.setPhoto_type(Integer.valueOf(MakePhoto.photoType));

                JsonObject jsonObject2 = new Gson().fromJson(new Gson().toJson(photo), JsonObject.class);

                Globals.writeToMLOG("INFO", "requestCode == 201 && resultCode == RESULT_OK/photo_save", "photoSave: " + jsonObject2);

//                Globals.writeToMLOG("INFO", "requestCode == 201 && resultCode == RESULT_OK/photo_save", "photoSave: " + photo);

                StackPhotoRealm.setAll(Collections.singletonList(photo));
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "requestCode == 201 && resultCode == RESULT_OK", "Exception e: " + e);
            }
        } else if (requestCode == 201 && resultCode == RESULT_CANCELED) {
            StackPhotoRealm.deleteByPhotoNum(MakePhoto.photoNum);
        }

        if (requestCode == CAMERA_REQUEST_TAR_COMMENT_PHOTO) {
            try {
                FragmentManager fragmentManager = getSupportFragmentManager();
                List<Fragment> fragments = fragmentManager.getFragments();
                TARFragmentHome fragmentHome = (TARFragmentHome) fragments.get(0);

                AddressSDB addr = SQL_DB.addressDao().getById(fragmentHome.secondFrag.data.addr);
                CustomerSDB client = SQL_DB.customerDao().getById(fragmentHome.secondFrag.data.client);

                StackPhotoDB stackPhotoDB = saveTestPhoto(new File(MakePhoto.openCameraPhotoUri), addr, client);
                MakePhoto.openCameraPhotoUri = null;

                fragmentHome.secondFrag.setPhotoComment(stackPhotoDB.getId(), TARCommentIndex);
            }catch (Exception e){
                Globals.writeToMLOG("ERROR", "DR/CAMERA_REQUEST_TAR_COMMENT_PHOTO", "Exception e: " + e);
            }
        }else if (requestCode == CAMERA_REQUEST_PROMOTION_TOV_PHOTO && resultCode == RESULT_OK){
            try {
                savePhotoPromotionTov(new File(MakePhoto.openCameraPhotoUri), wpDataDBOPTION_CONTROL_PROMOTION_ID, tovarDBOPTION_CONTROL_PROMOTION_ID);
                // Концептуально тут нужно эту фотку как-то обработать.
            }catch (Exception e){
                Globals.writeToMLOG("ERROR", "DR/CAMERA_REQUEST_PROMOTION_TOV_PHOTO", "Exception e: " + e);
            }
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
                        } catch (Exception e) {
                            e.printStackTrace();

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
     */
    public void RENAME(WpDataDB wp) {
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
        } catch (Exception e) {
            e.printStackTrace();
            // TODO =__=
        }

        Log.e("DetailedReportActivity", "rpAmountSum: " + rpAmountSum);
        Log.e("DetailedReportActivity", "rpCount: " + rpCount);
        Log.e("DetailedReportActivity", "rpAmountSum2: " + rpAmountSum2);
        Log.e("DetailedReportActivity", "rpTotalSumToRedemptionOfGoods: " + rpTotalSumToRedemptionOfGoods);
    }


    /*Это ***, такое же в TARAct*/
    private StackPhotoDB saveTestPhoto(File photoFile, AddressSDB addr, CustomerSDB client) {
        try {
            int id = RealmManager.stackPhotoGetLastId();
            id++;
            StackPhotoDB stackPhotoDB = new StackPhotoDB();
            stackPhotoDB.setId(id);
            stackPhotoDB.setDt(System.currentTimeMillis() / 1000);
            stackPhotoDB.setTime_event(Clock.getHumanTime2(System.currentTimeMillis() / 1000));

            stackPhotoDB.setAddr_id(addr.id);
            stackPhotoDB.setAddressTxt(addr.nm);

            stackPhotoDB.setClient_id(client.id);
            stackPhotoDB.setCustomerTxt(client.nm);

            stackPhotoDB.setUser_id(Globals.userId);
            stackPhotoDB.setPhoto_type(0);

            stackPhotoDB.setDvi(1);

            stackPhotoDB.setCreate_time(System.currentTimeMillis());

            stackPhotoDB.setPhoto_hash(globals.getHashMD5FromFile2(photoFile, null));
            stackPhotoDB.setPhoto_num(photoFile.getAbsolutePath());


            RealmManager.stackPhotoSavePhoto(stackPhotoDB);
            return stackPhotoDB;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "TARActivity.onActivityResult.savePhoto", "Exception e: " + e);
            return null;
        }
    }


    /**
     * 05.03.23.
     * Сохранение фото Акционного товара + акции
     * */
    private StackPhotoDB savePhotoPromotionTov(File photoFile, WpDataDB wpDataDB, TovarDB tovarDB){
        try {

            Globals.writeToMLOG("INFO", "OptionControlPhotoPromotion/savePhotoPromotionTov", "wp_dad2: " + wpDataDB.getCode_dad2());
            Globals.writeToMLOG("INFO", "OptionControlPhotoPromotion/savePhotoPromotionTov", "tov.id: " + tovarDB.getiD());

            int id = RealmManager.stackPhotoGetLastId();
            id++;
            StackPhotoDB stackPhotoDB = new StackPhotoDB();
            stackPhotoDB.setId(id);
            stackPhotoDB.setDt(System.currentTimeMillis() / 1000);
            stackPhotoDB.setTime_event(Clock.getHumanTime2(System.currentTimeMillis() / 1000));

            stackPhotoDB.setAddr_id(wpDataDB.getAddr_id());
            stackPhotoDB.setAddressTxt(wpDataDB.getAddr_txt());

            stackPhotoDB.setClient_id(wpDataDB.getClient_id());
            stackPhotoDB.setCustomerTxt(wpDataDB.getClient_txt());

            stackPhotoDB.code_dad2 = wpDataDB.getCode_dad2();

            stackPhotoDB.setUser_id(Globals.userId);
            stackPhotoDB.setPhoto_type(PHOTO_PROMOTION_TOV);

            stackPhotoDB.tovar_id = tovarDB.getiD();

            stackPhotoDB.setDvi(1);

            stackPhotoDB.setCreate_time(System.currentTimeMillis());

            stackPhotoDB.setPhoto_hash(globals.getHashMD5FromFile2(photoFile, null));
            stackPhotoDB.setPhoto_num(photoFile.getAbsolutePath());


            RealmManager.stackPhotoSavePhoto(stackPhotoDB);
            return stackPhotoDB;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DRActivity.onActivityResult.savePhotoPromotionTov", "Exception e: " + e);
            return null;
        }
    }
}



