package ua.com.merchik.merchik;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import ua.com.merchik.merchik.Activities.MenuMainActivity;
import ua.com.merchik.merchik.Activities.MyApplication;
import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Activities.PremiumActivity.PremiumActivity;
import ua.com.merchik.merchik.Activities.ReferencesActivity.ReferencesActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARActivity;
import ua.com.merchik.merchik.Activities.WorkPlanActivity.WPDataActivity;
import ua.com.merchik.merchik.Activities.navigationMenu.MenuHeader;
import ua.com.merchik.merchik.Activities.navigationMenu.MenuHeaderAdapter;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RealmModels.MenuItemFromWebDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RetrofitResponse.Logout;
import ua.com.merchik.merchik.data.RetrofitResponse.PhotoHash;
import ua.com.merchik.merchik.data.RetrofitResponse.PhotoHashList;
import ua.com.merchik.merchik.data.RetrofitResponse.ServerConnection;
import ua.com.merchik.merchik.data.UploadPhotoData.Move;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogMap;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class toolbar_menus extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public Globals globals = new Globals();
    static trecker trecker = new trecker();
    TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading();

    private WPDataObj wpDataObj;

//    public FloatingActionButton fab;

    Menu menu;
    MenuItem light;
    private ImageButton ib;

    boolean logFromOffline; // Залогинились ли мы или нет
    long count = 0;
    long internetSpeed = 0;

    public String msgDialogSystemTxt = "";
    public String msgDialogUserTxt = "";
    String address; // Адрес который приходит откуда надо (сейчас с места где делаются фотки)
    String user_id, login, password; // Переменные между активностями
    private String toolbarMwnuItemServer;

    TextView textCartItemCount; // Текст иконки кол-ва фоток в toolbar
    TextView serverStat;
    public static int internetStatus;


    //---------------------------

    //----------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        try {
        Log.e("MIGRATION_2_3", "TOOLBAR");
        Log.e("MIGRATION_2_3", "SQL_DB" + SQL_DB.oborotVedDao().toString());


        Globals.translatesList = SQL_DB.translatesDao().getAll();

        Log.e("takePhoto", "takePhotoToool: " + ua.com.merchik.merchik.trecker.enabledGPS);
        if (ua.com.merchik.merchik.trecker.switchedOff) {
            ua.com.merchik.merchik.trecker.SetUpLocationListener(this);
        }

        globals.handlerCount.postDelayed(runnableCron10, 100);

        globals.getDate();

        logFromOffline = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("logFromOffline", false);

        address = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("address", "");

        login = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("login", "");

        password = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("password", "");


        Log.e("PreferenceManager", "TOOLBAR" + PreferenceManager.getDefaultSharedPreferences(this)
                .getString("login", ""));


//        } catch (Exception e) {
//            globals.alertDialogMsg(this, "ОшибкаToolbars: " + e);
//        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("test", "" + requestCode + resultCode + data);
    }


    public static int textLesson;
    public static int videoLesson;

    public static void setFab(Context context, FloatingActionButton fab) {
        Log.e("setFab", "textLesson: " + textLesson);
        Log.e("setFab", "videoLesson: " + videoLesson);

        try {
            SiteObjectsDB object = RealmManager.INSTANCE.copyFromRealm(RealmManager.getLesson(textLesson));

            Log.e("setFab", "REALtextLesson: " + textLesson);
            Log.e("setFab", "REALvideoLesson: " + videoLesson);

            String str = object.getComments();
            str = str.replace("&quot;", "\"");

            String finalStr = str;
            fab.setOnClickListener(view -> {
                DialogData dialog = new DialogData(context);
                dialog.setTitle("Подсказка");
                dialog.setText(finalStr);
                dialog.setMerchikIco(context);
                dialog.setImgBtnCall(context);
                dialog.setVideoLesson(context, true, videoLesson, null);
                dialog.show();
            });
        } catch (Exception e) {
            Log.e("setFab", "e: " + e);
            e.printStackTrace();
        }
    }

    private List<MenuHeader> headerList_2 = new ArrayList<>();

    private HashMap<MenuModel, List<MenuModel>> childList = new HashMap<>();

    public void initDrawerStuff(DrawerLayout drawerLayout, Toolbar toolbar, NavigationView navigationView) {
        prepareMenuData();

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_header_subtext);
//        navUsername.setText("Версия: " + BuildConfig.VERSION_NAME);
        navUsername.setText("Версия: " + getResources().getString(R.string.ver));

        RecyclerView recyclerView = navigationView.findViewById(R.id.navigationRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MenuHeaderAdapter(headerList_2, (View view, MenuItemFromWebDB item) -> {
            itemClick(view, item.getID());
        }));
        recyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                Log.e("setOnFlingListener", "0velocityX: " + velocityX + "    0velocityY: " + velocityY);
                recyclerView.fling(velocityX / 2, velocityY / 2);
                return false;
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemBackground(this.getResources().getDrawable(R.drawable.hamburger_menu_selector));
    }


    /**
     * 07.06.2021
     * ТОТ САМЫЙ ВЫБОР ПУНКТОВ МЕНЮ
     */
    private void itemClick(View view, int id) {

        Log.e("NWitemClick", "id: " + id);

        Intent intentRef = new Intent(this, ReferencesActivity.class);

        switch (id) {
            case 154:   // Справочники ГРУППА
                Toast.makeText(this, "Справочники", Toast.LENGTH_SHORT).show();
                break;

            case 155:   // Адреса
                Toast.makeText(this, "Адреса", Toast.LENGTH_SHORT).show();
                intentRef.putExtra("ReferencesEnum", Globals.ReferencesEnum.ADDRESS);
                startActivity(intentRef);
                break;

            case 156:   // Клиенты
                Toast.makeText(this, "Клиенты", Toast.LENGTH_SHORT).show();
                intentRef.putExtra("ReferencesEnum", Globals.ReferencesEnum.CUSTOMER);
                startActivity(intentRef);
                break;

            case 157:   // Сотрудники
                Toast.makeText(this, "Сотрудники", Toast.LENGTH_SHORT).show();
                Log.e("navigationmenuclick", "Сотрудники");
                intentRef.putExtra("ReferencesEnum", Globals.ReferencesEnum.USERS);
                startActivity(intentRef);
                break;

            case 160:   // Чаты
                Toast.makeText(this, "Чат", Toast.LENGTH_SHORT).show();
                Log.e("navigationmenuclick", "Чат");
                intentRef.putExtra("ReferencesEnum", Globals.ReferencesEnum.CHAT);
                startActivity(intentRef);
                break;

            case 162:
                Toast.makeText(this, "Образцы ФотоОтчетов", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, PhotoLogActivity.class);
                intent.putExtra("SamplePhoto", true);
                startActivity(intent);
                break;

            case 164:
                MenuItemFromWebDB menuItem164 = RealmManager.getSiteMenuItem(164);
                String menuItem164format = menuItem164.getUrl();
                Intent menuItem164browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(menuItem164format));
                this.startActivity(menuItem164browserIntent);
                break;

            case 165:
                MenuItemFromWebDB menuItem165 = RealmManager.getSiteMenuItem(165);
                String menuItem165format = menuItem165.getUrl();
                Intent menuItem165browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(menuItem165format));
                this.startActivity(menuItem165browserIntent);
                break;

        }


        if (id == 135) {
            Log.e("NVitemClick", "WPid: " + 135);
            Intent intent = new Intent(this, WPDataActivity.class);
            startActivity(intent);
        } else if (id == 134) {
            Toast.makeText(this, "План работ/Список заявок", Toast.LENGTH_LONG).show();
        } else if (id == 129) {
            Toast.makeText(this, "План работ", Toast.LENGTH_LONG).show();
        } else if (id == 143) {
//            Intent intent = new Intent(this, TasksActivity.class);
            Intent intent = new Intent(this, TARActivity.class);
            intent.putExtra("TAR_type", 1);

            startActivity(intent);
        } else if (id == 144) {
//            Intent intent = new Intent(this, ReclamationsActivity.class);
            Intent intent = new Intent(this, TARActivity.class);
            intent.putExtra("TAR_type", 0);

            startActivity(intent);
        } else if (id == 133) {
            Toast.makeText(this, "Премиальные", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, PremiumActivity.class);
            startActivity(intent);
        } else if (id == 132) {
            Toast.makeText(this, "Светофор", Toast.LENGTH_LONG).show();
        } else if (id == 131) {
            Toast.makeText(this, "Язык", Toast.LENGTH_LONG).show();
            Translate.showPopupMenu(this, view);
        } else if (id == 130) {
            Toast.makeText(this, "Выход", Toast.LENGTH_LONG).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                finishAffinity();
            }
            System.exit(0);
        }
    }

    private void prepareMenuData() {
        List<MenuItemFromWebDB> menuItemFromWebDBList = RealmManager.INSTANCE.copyFromRealm(RealmManager.getSiteMenu());
        for (MenuItemFromWebDB list : menuItemFromWebDBList) {
            if (list.getParent().equals(0)) {
                if (list.getSubmenu().size() > 0) {
                    Integer[] integers = list.getSubmenu().toArray(new Integer[0]);
                    List<MenuItemFromWebDB> menuItemFromWebDBList1 = RealmManager.INSTANCE.copyFromRealm(RealmManager.getSiteMenuItems(integers));
                    MenuHeader header = new MenuHeader(list, menuItemFromWebDBList1);
                    headerList_2.add(header);
                } else {
                    MenuHeader header = new MenuHeader(list, new ArrayList<>());
                    headerList_2.add(header);
                }
            }
        }

    }


    // DON`T USE
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        globals.handlerCount.removeCallbacks(runnableCron10);
        globals.handlerCount.postDelayed(runnableCron10, 100);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        globals.handlerCount.removeCallbacks(runnableCron10);
    }

    @Override
    protected void onStop() {
        super.onStop();
        globals.handlerCount.removeCallbacks(runnableCron10);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        globals.handlerCount.removeCallbacks(runnableCron10);
    }


    /**
     * 25.08.2020
     * Получение данных с другой активности.
     */
    private void getDataFromIntent() {
        try {
            Intent intent = getIntent();
            if ((WPDataObj) intent.getSerializableExtra("dataFromWPObj") != null) {
                WPDataObj wp = (WPDataObj) intent.getSerializableExtra("dataFromWPObj");

                Log.e("MANAGER_COORD", "План работ с Активности Плана работ: " + wp);

                if (wp != null) {
                    wpDataObj = wp;
                    Log.e("MANAGER_COORD", "План работ с Активности Плана работ: " + wpDataObj);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Произошла ошибка. Сообщите о ней своему руководителю. Ошибка: " + e, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Создание меню в toolbox-е
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        this.menu = menu;

        light = menu.findItem(R.id.action_check);

        MenuItem item = menu.findItem(R.id.menu_toolbar_exchange);
        View actionView = MenuItemCompat.getActionView(item);
        textCartItemCount = actionView.findViewById(R.id.cart_badge1);
        setupBadge(0);

        ib = actionView.findViewById(R.id.imageViewExchange);

        pingServer(1);

        item.getActionView().setOnLongClickListener(v -> {
            Toast.makeText(toolbar_menus.this, "Начинаю Обмен. (Выгрузка фото и Синхронизация таблиц)", Toast.LENGTH_SHORT).show();

//            photoUpload();  // Выгрузка фото

            tablesLoadingUnloading.uploadAllTables(toolbar_menus.this);     // Выгрузка таблиц
            tablesLoadingUnloading.downloadAllTables(toolbar_menus.this);   // Скачивание таблиц
            return false;
        });

        item.getActionView().setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(toolbar_menus.this, v);
            MenuInflater inflater1 = popup.getMenuInflater();
            inflater1.inflate(R.menu.actions, popup.getMenu());
            popup.getMenu().findItem(R.id.exchange_serv).setTitle(toolbarMwnuItemServer); // Обновляем состояние сервера (пишем ему текст)

            popup.show();

            popup.setOnMenuItemClickListener((MenuItem i) -> {
                // Сервер
                if (i.getItemId() == R.id.exchange_serv) {
                    pingServer(2);
                }

                // Выгрузка фото
                if (i.getItemId() == R.id.exchange_photo_action) {
                    Globals.writeToMLOG("INFO", "toolbar_menus.act/click/exchange_photo_action", "Нажали на кнопку 'Выгрузка фото'");
                    new PhotoReports(this).uploadPhotoReports(PhotoReports.UploadType.MULTIPLE);
                }

                // Синхронизовать Таблици
                if (i.getItemId() == R.id.exchange_db_action) {
                    Toast.makeText(toolbar_menus.this, "Синхронизаци таблиц", Toast.LENGTH_SHORT).show();
                    if (!TablesLoadingUnloading.sync) {
                        Exchange.sendWpData2();
                        Exchange.chatExchange();
                        Exchange.chatGroupExchange();
                        tablesLoadingUnloading.uploadAllTables(toolbar_menus.this);     // Выгрузка таблиц
                        tablesLoadingUnloading.downloadAllTables(toolbar_menus.this);   // Скачивание таблиц
                    } else {
                        globals.alertDialogMsg(toolbar_menus.this, "Синхронизация уже запущена! Подождите сообщения об окончании.");
                    }

                    try {
                        // Новый обмен. Нужно ещё донастроить для нормальной работы.
                        Exchange exchange = new Exchange();
                        exchange.context = toolbar_menus.this;
                        exchange.startExchange();

                        exchange.uploadTARComments(null);
                    } catch (Exception e) {
                        Log.d("test", "test" + e);
                    }

                }
                return true;
            });

        });

        MenuItem autoSendCheck = menu.findItem(R.id.action_autosend);
        autoSendCheck.setChecked(Globals.autoSend);

        return true;
    }


    /**
     * Устанавливается в счётчик число фоток
     */
    void setupBadge(int countPhoto) {
        Log.e("setupBadge", "countPhoto: " + countPhoto);
        if (textCartItemCount != null) {
            if (countPhoto == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {
                textCartItemCount.setText(String.valueOf(Math.min(countPhoto, 99)));
                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    /**
     * Обработка функцтонала в ToolBox-е
     */

    @SuppressLint("DefaultLocale")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // ... "На главную"
        if (id == R.id.action_to_main) {
            Intent intent = new Intent(this, MenuMainActivity.class);
            startActivity(intent);
        }

        // ... "План работ"
        if (id == R.id.action_to_wpdata) {
            Intent intent = new Intent(this, WPDataActivity.class);
            startActivity(intent);
        }

        // ... Отобразить подсказку
        if (id == R.id.action_info) {
            Toast.makeText(this, "Подсказка не доступна в этом меню. Попробуйте вызвать её с активности Фотоотчётов.", Toast.LENGTH_LONG).show();
        }

        // ... Журнал фото
        if (id == R.id.action_photo_log) {
//            PhotoLog photoLog = new PhotoLog();
//            photoLog.viewPhotoLog(this);

            try {
                Intent intent = new Intent(this, PhotoLogActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                DialogData dialog = new DialogData(this);
                dialog.setTitle("Ошибка !");
                dialog.setText("Журнал фото: " + e);
                dialog.setClose(dialog::dismiss);
                dialog.show();
            }
        }

        // ... Настройки
        if (id == R.id.action_settings) {
            DialogData dialog = new DialogData(this);
            dialog.setTitle("Настройки");
            dialog.setText("Отправить служебный файл?");
            dialog.setOk("Отправить", () -> {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@merchik.com.ua"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Приложение. M_LOG.");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Отправка отладочного файла");
                File root = MyApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_NOTIFICATIONS);
                String fName = "M_LOG.txt";
                File file = new File(root, fName);
                if (!file.exists() || !file.canRead()) {
                    return;
                }
                Uri contentUri;
                try {
                    contentUri = FileProvider.getUriForFile(this, "ua.com.merchik.merchik.provider", file);
                } catch (Exception e) {
                    contentUri = Uri.fromFile(file);
                }

                emailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(emailIntent, "Как отправить файл?"));
            });
            dialog.setClose(dialog::dismiss);
            dialog.show();
        }

        // ... Автовыгрузка
        if (id == R.id.action_autosend) {

            boolean change = !Globals.autoSend;
            item.setChecked(change);
            Globals.autoSend = change;

            String stat = "включен";    // stat = статус в который перевели Автовыгрузку
            if (!Globals.autoSend) {
                stat = "выключен";
            }
            globals.alertDialogMsg(this, "Обмен данными с сервером в автоматическом режиме " + stat);
        }


        // Временно для манагеров
        // ...


        // ... "Выход"
        if (id == R.id.action_exit) {
            String mod = "logout";
            retrofit2.Call<Logout> call = RetrofitBuilder.getRetrofitInterface().logoutInfo(mod);
            call.enqueue(new retrofit2.Callback<Logout>() {
                @Override
                public void onResponse(retrofit2.Call<Logout> call, retrofit2.Response<Logout> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().getState()) {
                            Toast.makeText(toolbar_menus.this, "Вы разлогинились.", Toast.LENGTH_SHORT).show();
                            Globals.session = null;
                        }
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<Logout> call, Throwable t) {
                    Toast.makeText(toolbar_menus.this, "Разлогиниться не получилось: " + t.toString(), Toast.LENGTH_SHORT).show();
                }
            });

            String msg = "Для экономии заряда батареи рекомендую выключить GPS в настройках телефона.\n\nСпасибо за сотрудничество )";
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage(msg);
            builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        finishAffinity();
                    }
                    System.exit(0);
                }
            });
            builder.create().show();
            return true;
        }


        // Светофор
        if (id == R.id.action_check) {
            dialogMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * 09.07.2020
     * Отображение сообщения и выгрузка фото
     */

    private void photoUpload() {
        globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.photoUpload.: " + "ENTER" + "\n");

        Log.e("ОБМЕН", "(Кнопка)Выгрузка фото");

        try {
            globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.photoUpload.: " + "TRY" + "\n");
            int countPhoto = RealmManager.stackPhotoNotUploadedPhotosCount();
            if (countPhoto > 0) {
                String msg = "Сейчас будет выгружено " + countPhoto + " фото на сервер. Дождитесь сообщения об окончании работы.";
                AlertDialog.Builder builder = new AlertDialog.Builder(toolbar_menus.this);
                builder.setCancelable(false);
                builder.setMessage(msg);
                builder.setPositiveButton("Ок", (dialog, which) -> /*getPhotoAndUpload(1)*/getPhotosAndUpload(1));
                builder.setNegativeButton("Отменить", (dialog, which) -> {
                });
                builder.create().show();
            } else {
                Toast.makeText(toolbar_menus.this, "Нет фото для выгрузки.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.photoUpload.catch: " + e + "\n");
        }


    }


    //----------------------------------------------------------------------------------------------
    //service -- жосткий аналог крона
    // ===== КРОНЧИК ===== КРОНЧИК ===== КРОНЧИК ===== КРОНЧИК ===== КРОНЧИК ===== КРОНЧИК =====
    private Runnable runnableCron10 = new Runnable() {
        public void run() {
            try {
                Log.e("КРОНЧИК", "Time: " + Clock.getHumanTime());

//            Log.e("КРОНЧИК", "stackPhotoDBAll: " + StackPhotoRealm.getAll().size());

                // 22.04.2021 Ужасная хрень. Если нет данных от GPS -- оно начинает его слушать.
                ua.com.merchik.merchik.trecker.switchedOff = !ua.com.merchik.merchik.trecker.enabledGPS;

                if (ua.com.merchik.merchik.trecker.switchedOff) {
                    Log.e("КРОНЧИК", "Запускаю слушатель GPS-а");
                    ua.com.merchik.merchik.trecker.SetUpLocationListener(toolbar_menus.this);
                }

                Log.e("КРОНЧИК", "SESSION: " + Globals.session);
                Log.e("КРОНЧИК", "login: " + login);
                Log.e("КРОНЧИК", "password: " + password);

                server.sessionCheckAndLogin(toolbar_menus.this, login, password);   // Проверка активности сессии и логин, если сессия протухла
                internetStatus = server.internetStatus();       // Обновление статуса интеренета
//            pingServer(1);                            // ОБМЕН ЦВЕТ
                RealmManager.stackPhotoDeletePhoto();           // Удаление фото < 2 дня
                lightStatus();                                  // Обновление статуса Светофоров
                setupBadge(RealmManager.stackPhotoNotUploadedPhotosCount()); // Подсчёт кол-ва фоток в БД & Установка числа в счётчик

                Log.e("КРОНЧИК", "stackPhotoNotUploadedPhotosCount(): " + RealmManager.stackPhotoNotUploadedPhotosCount());

                globals.testMSG(toolbar_menus.this);

                Log.e("КРОНЧИК", "internetStatus: " + internetStatus);

                globals.writeToMLOG(Clock.getHumanTime() + " CRON.internetStatus: " + internetStatus + "\n");

                cronCheckUploadsPhotoOnServer();                // Получение инфы о "загруженности" фоток


                // Если включена Автовыгрузка/Автообмен
                if (Globals.autoSend && internetStatus == 1) {
//                getPhotoAndUpload(1);   // Выгрузка фото


                    try {
                        Globals.writeToMLOG("INFO", "CRON LOG MP", "Попытка выгрузить ЛОГ_Местоположения");
                        tablesLoadingUnloading.uploadLodMp(new ExchangeInterface.ExchangeRes() {
                            @Override
                            public void onSuccess(String ok) {
                                // todo log
//                                Toast.makeText(toolbar_menus.this, ok, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(String error) {
                                // todo log
//                                Toast.makeText(toolbar_menus.this, error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "CRON LOG MP", "Exception e: " + e);
                    }

                    tablesLoadingUnloading.cronUpdateTables();

                    try {
                        Globals.writeToMLOG("INFO", "CRON uploadReportPrepare", "Start");
                        tablesLoadingUnloading.uploadReportPrepareToServer();
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "CRON uploadReportPrepare", "Exception e: " + e);
                    }

                    try {
//                    tablesLoadingUnloading.updateWpData();
                    } catch (Exception e) {
                    }


                    // Новый обмен. Нужно ещё донастроить для нормальной работы.
                    Exchange exchange = new Exchange();
                    exchange.context = toolbar_menus.this;
                    exchange.startExchange();


                    PhotoReports photoReports = new PhotoReports(toolbar_menus.this);
                    if (photoReports.permission) {
                        Globals.writeToMLOG("INFO", "CRON/PhotoReports", "Start upload photo reports. upload permission: true");
                        photoReports.uploadPhotoReports(PhotoReports.UploadType.AUTO);
                    } else {
                        Globals.writeToMLOG("INFO", "CRON/PhotoReports", "Start upload photo reports. upload permission: false");
                    }

                }


                // Пишет статус логина. Или режим работы приложения
                if (Globals.onlineStatus) {
                    toolbarMwnuItemServer = getResources().getString(R.string.txt_sever) + "(" + getResources().getString(R.string.txt_online) + ")";
                } else {
                    toolbarMwnuItemServer = getResources().getString(R.string.txt_sever) + "(" + getResources().getString(R.string.txt_offline) + ")";
                }
                Log.e("КРОНЧИК", "Globals.onlineStatus: " + toolbarMwnuItemServer);
            } catch (Exception e) {
                Log.e("КРОНЧИК", "Exception" + e);
            }


            globals.handlerCount.postDelayed(this, 10000);  //повтор раз в 10 секунд
        }
    };

    // ===== КРОНЧИК ===== КРОНЧИК ===== КРОНЧИК ===== КРОНЧИК ===== КРОНЧИК ===== КРОНЧИК =====
    //----------------------------------------------------------------------------------------------


    // НОВАЯ ВЫГРУЗКА ФОТО
    // ---------------------------------------------------------------------------------------------


    /**
     * Получение и выгрузка фоток.
     * <p>
     * mod = 1 -- Выгрузка в ручном режиме
     * mod = 2 -- Выгрузка вызвана из крона (Автовыгрузка)
     */
    private List<StackPhotoDB> realmResults = new ArrayList<>();

    public interface UploadCallback {
        void onSuccess();

        void onFailure(String s);
    }

    private void startUploading(int mod) {

        try {
            int finalId = 0;
            if (!realmResults.isEmpty()) {
                Log.e("startUploading", "start");

                StackPhotoDB current = realmResults.get(0);

                try {
                    if (current != null) {
                        finalId = current.getId();
                        globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.startUploading. current: " + "not null" + "\n");
                    } else {
                        globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.startUploading. current: " + "NULL!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + "\n");
                    }
                } catch (Exception e) {
                    globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.startUploading.current.exception: " + e + "\n");
                }


                globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.перед самим вызовом выгрузки. id фото выгрузки: " + finalId + "\n");
                int finalId1 = finalId;
                photoUploadToServer(mod, current, new UploadCallback() {
                    @Override
                    public void onSuccess() {
                        try {
                            globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.UploadCallback.onSuccess. id фото выгрузки: " + finalId1 + "\n");

                            Log.e("startUploading", "onSuccess");
                            Toast.makeText(toolbar_menus.this, "Фото: " + finalId1 + " успешно выгружено", Toast.LENGTH_LONG).show();
                            RealmManager.INSTANCE.executeTransaction(realm -> {
                                current.setError(null);
                                RealmManager.INSTANCE.insertOrUpdate(current);
                            });
                            realmResults.remove(current);
                            startUploading(mod);
                        } catch (Exception e) {
                            globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.UploadCallback.onSuccess.catch.e: " + e + "\n");
                        }
                    }

                    @Override
                    public void onFailure(String s) {
                        try {
                            globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.UploadCallback.onFailure. id фото выгрузки: " + finalId1 + " ОШИБКА: " + s + "\n");

                            Log.e("startUploading", "onFailure " + s + ".");
//                            Toast.makeText(toolbar_menus.this, "При выгрузке фото: " + current.getPhoto_num() + " возникла ошибка: " + s, Toast.LENGTH_LONG).show();
                            RealmManager.INSTANCE.executeTransaction(realm -> {
                                current.setError(1);
                                current.setErrorTime(System.currentTimeMillis());
                                current.setErrorTxt(s);
                                RealmManager.INSTANCE.insertOrUpdate(current);
                            });
                            realmResults.remove(current);
                            startUploading(mod);
                        } catch (Exception e) {
                            globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.UploadCallback.onFailure.catch.e: " + e + "\n");
                        }
                    }
                });


            } else {
                // ВСЕ ФОТО БЫЛИ ВЫГРУЖЕНЫ
                DialogData dialogData = new DialogData(this);
                dialogData.setTitle("Выгрузка фото");
                dialogData.setText("Выгрузка окончена");
                dialogData.show();
                uploadPermission = true;
            }
        } catch (Exception e) {
            globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.startUploading.Ошибка: " + e + "\n");
        }

    }


    private boolean uploadPermission = true; // По умолчанию можно выгружать

    private void getPhotosAndUpload(int mod) {
        if (uploadPermission) {
            uploadPermission = false; // Запрещаю повторно начинать выгрузку
            realmResults.clear();
            List<StackPhotoDB> res = RealmManager.INSTANCE.copyFromRealm(RealmManager.getStackPhotoPhotoToUpload());    // todo У меня были обращения к удалённым фоткам. Может быть ЭТО поправит
            for (StackPhotoDB photo : res) {
                if (photo != null) realmResults.add(photo);
            }
            globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.выгрузка.сбор данных перед выгрузкой. количество фоток к выгрузке на текущий момент: " + realmResults.size() + "\n");
            startUploading(mod);
        } else {
            // ВЫ УЖЕ НАЧАЛИ ВЫГРУЗКУ
            DialogData dialogData = new DialogData(this);
            dialogData.setTitle("Выгрузка фото");
            dialogData.setText("Вы уже начали выгрузку. Осталось выгрузить: " + realmResults.size() + " фото. \nДождитесь пока все фото выгрузятся.");
            dialogData.show();
        }

    }

    private void getPhotoAndUpload(int mod) {
        if (internetStatus == 1) {// inet+
            RealmResults<StackPhotoDB> results = RealmManager.getStackPhotoPhotoToUpload();

            Log.e("TIME_TO_UPLOAD", "results.size(): " + results.size());
            for (int i = 0; i < results.size(); i++) {
                Log.e("TIME_TO_UPLOAD", "i: " + i);
                if (results.get(i) != null) {
                    Log.e("TIME_TO_UPLOAD", "photoUploaded: " + photoUploaded);
                }
            }

        } else if (internetStatus == 2) {// inet-
            if (!Globals.autoSend)
                globals.alertDialogMsg(this, "Ошибка при выгрузке фото: нет данных о сервере. Перезапустите интернет и повторите попытку. Если проблема повторится - обратитесь к Вашему руководителю.");
        } else {
            if (!Globals.autoSend)
                globals.alertDialogMsg(this, "Ошибка при выгрузке фото: проверьте состояние интернета. Перезапустите интернет и повторите попытку. Если проблема повторится - обратитесь к Вашему руководителю.");
        }
    }


    // Флажок для выгрузки
    private boolean photoUploaded = true;

    // Выгрузка фоток
    private void photoUploadToServer(int mode, StackPhotoDB photoDB, UploadCallback callback) {
        final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");

        int photoId = photoDB.getId();
        // Запрос
        String mod = "images_prepare";
//        String act = "upload_image";
        String act = "upload_photo";

        String client_id = "";
        String addr_id = "";
        String date = "";
        String img_type_id = "";
        String photo_user_id = "";
        String client_tovar_group = "";
        String doc_num = "";
        String theme_id = "";
        String comment = "";
        String dvi = "";
        String code_dad2 = "";
        String gp = "";

        // Распаковка данных с БД
        if (photoDB.getClient_id() != null) {
            client_id = photoDB.getClient_id();
        }

        if (photoDB.getAddr_id() != null) {
            addr_id = String.valueOf(photoDB.getAddr_id());
        }

        if (photoDB.getTime_event() != null) {
            date = photoDB.getTime_event();
        }

        if (photoDB.getPhoto_type() != null) {
            img_type_id = String.valueOf(photoDB.getPhoto_type());
        }

        if (photoDB.getPhoto_user_id() != null) {
            photo_user_id = photoDB.getPhoto_user_id();
        }

        if (photoDB.getPhoto_group_id() != null) {
            client_tovar_group = photoDB.getPhoto_group_id();
        }

        if (photoDB.getDoc_id() != null) {
            doc_num = photoDB.getDoc_id();
        }

        if (photoDB.getTheme_id() != null) {
            theme_id = String.valueOf(photoDB.getTheme_id());
        }

        if (photoDB.getComment() != null) {
            comment = photoDB.getComment();
        }

        if (photoDB.getDvi() != null) {
            dvi = String.valueOf(photoDB.getDvi());
        }

        try {
            code_dad2 = String.valueOf(photoDB.getCode_dad2());
        } catch (Exception e) {
            // Запись ошибки
            code_dad2 = "";
        }

        if (photoDB.getGp() != null) {
            gp = photoDB.getGp();
        }

        // Запаковка данных для сервера
        RequestBody mod2 = RequestBody.create(MediaType.parse("text/plain"), mod);
        RequestBody act2 = RequestBody.create(MediaType.parse("text/plain"), act);
        RequestBody client_id2 = RequestBody.create(MediaType.parse("text/plain"), client_id);
        RequestBody addr_id2 = RequestBody.create(MediaType.parse("text/plain"), addr_id);
        RequestBody date2 = RequestBody.create(MediaType.parse("text/plain"), date);
        RequestBody img_type_id2 = RequestBody.create(MediaType.parse("text/plain"), img_type_id);
        RequestBody photo_user_id2 = RequestBody.create(MediaType.parse("text/plain"), photo_user_id);
        RequestBody client_tovar_group2 = RequestBody.create(MediaType.parse("text/plain"), client_tovar_group);
        RequestBody doc_num2 = RequestBody.create(MediaType.parse("text/plain"), doc_num);
        RequestBody theme_id2 = RequestBody.create(MediaType.parse("text/plain"), theme_id);
        RequestBody comment2 = RequestBody.create(MediaType.parse("text/plain"), comment);
        RequestBody dvi2 = RequestBody.create(MediaType.parse("text/plain"), dvi);
        RequestBody codeDad2 = RequestBody.create(MediaType.parse("text/plain"), code_dad2);
        RequestBody gp2 = RequestBody.create(MediaType.parse("text/plain"), gp);

        File file = new File(photoDB.getPhoto_num());

        // УДАЛИТЬ, КОСТЫЛЯКА
        final Realm.Transaction transaction = realm -> {
            photoDB.setUpload_to_server(System.currentTimeMillis());
//            RealmManager.INSTANCE.copyToRealmOrUpdate(photoDB);
        };

        if (file.length() == 0) {
            RealmManager.INSTANCE.executeTransaction(transaction);
//            RealmManager.stackPhotoSavePhoto(photoDB);
            callback.onFailure("ЭТО ПУСТОЕ ФОТО, ЕГО НЕЛЬЗЯ ВЫГРУЗИТЬ. Скрин этой ошибки руководителю.");
            return;
        }

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part photo =
                MultipartBody.Part.createFormData("photos[]", file.getName(), RequestBody.create(MEDIA_TYPE_JPG, file));

        // Данные для отладки
        Log.e("TAG_SEND_PHOTO", "Data: \n"
                + "\n mod:" + mod
                + "\n act:" + act
                + "\n client_id:" + client_id
                + "\n addr_id:" + addr_id
                + "\n date:" + date
                + "\n img_type_id:" + img_type_id
                + "\n photo_user_id:" + photo_user_id
                + "\n client_tovar_group:" + client_tovar_group
                + "\n doc_num:" + doc_num
                + "\n theme_id:" + theme_id
                + "\n comment:" + comment
                + "\n code_dad2:" + code_dad2
                + "\n gp:" + gp
                + "\n photo:" + file.toString());

        String data = "" + "mod:" + mod + " act:" + act + " client_id:" + client_id + " addr_id:" + addr_id + " date:" + date + " img_type_id:" + img_type_id + " photo_user_id:" + photo_user_id + " client_tovar_group:" + client_tovar_group + " doc_num:" + doc_num + " theme_id:" + theme_id + " comment:" + comment + " code_dad2:" + code_dad2 + " gp:" + "" + " photo:" + file.toString();


        String info = " UPLOAD.PHOTO.TOOLBAR.PHOTODATA: photoId: " + photoId;
        globals.writeToMLOG(Clock.getHumanTime() + info + " " + data + "\n");

        if (mode == 1) {
            retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface()
                    .SEND_PHOTO_2_BODY(mod2, act2, client_id2, addr_id2, date2, img_type_id2, photo_user_id2, client_tovar_group2, doc_num2, theme_id2, comment2, dvi2, codeDad2, gp2, photo);
//            call.cancel();

            try {
                Log.e("TAG_REALM_LOG", "ПОПЫТКА ВЫГРУЗИТЬ ФОТО с ID: " + photoDB.getId());

                call.enqueue(new retrofit2.Callback<JsonObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                        globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.onResponse.Успешный ответ: id фото выгрузки: " + photoId + " Ответ с сервера: " + response.body() + "\n");


                        Log.e("TAG_REALM_LOG", "SUCCESS: " + response.body());

                        JsonObject jsonR = response.body();
                        Log.e("TAG_SEND_PHOTO", "RESPONSE: " + response.body());
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                if (jsonR != null) {
                                    if (!jsonR.get("state").isJsonNull() && jsonR.get("state").getAsBoolean()) {
                                        if (!jsonR.get("move").isJsonNull()) {
                                            try {
                                                Log.e("TAG_REALM_LOG", "ФОТО ВЫГРУЖЕНО с ID: " + photoDB.getId());

                                                JSONObject j = new JSONObject(jsonR.get("move").toString());
                                                Iterator keys = j.keys();
                                                Move obj = new Gson().fromJson(jsonR.get("move").getAsJsonObject().get(keys.next().toString()), Move.class);

                                                Log.e("photoUploadToServer", "jsonR.get(\"move\"): " + jsonR.get("move"));
                                                Log.e("photoUploadToServer", "obj.getRes(): " + obj.getRes());

                                                if (obj.getRes().equals("true") || obj.getRes().equals("1")) {
                                                    RealmManager.INSTANCE.executeTransaction(transaction);
                                                    RealmManager.stackPhotoSavePhoto(photoDB);

                                                    Toast.makeText(toolbar_menus.this, "Фото " + photoId + " выгружено на сервер.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    callback.onFailure(response.body().toString());
                                                }

                                            } catch (Exception e) {
                                                String msg = Arrays.toString(e.getStackTrace());
                                                callback.onFailure(msg + response.body().toString());
                                            }
                                        } else {
                                            callback.onFailure(response.body().toString());
                                        }


                                    } else if (!jsonR.get("state").isJsonNull() && !jsonR.get("state").getAsBoolean()) {
                                        try {
                                            if (!jsonR.get("error").isJsonNull() || jsonR.get("error") != null) {
                                                String error = jsonR.get("error").getAsString();


                                                globals.writeToMLOG(Clock.getHumanTime() + " TOOLBAR.photoUploadToServer.onResponse.state-false.error: " + error + "\n");

                                                // Такое фото уже было загружено ранее: JPG_20210216_091646_-1173842094.jpg
                                                String crutch = error.substring(0, 35);

                                                globals.writeToMLOG(Clock.getHumanTime() + " TOOLBAR.photoUploadToServer.onResponse.state-false.crutch: " + error + "\n");

                                                if (crutch.equals("Такое фото уже было загружено ранее:")) {
                                                    globals.writeToMLOG(Clock.getHumanTime() + " TOOLBAR.photoUploadToServer.onResponse.state-false.error&crutch: " + error + " |||crutch: " + crutch + "\n");

                                                    try {
                                                        RealmManager.INSTANCE.executeTransaction(transaction);
                                                        RealmManager.stackPhotoSavePhoto(photoDB);
                                                    } catch (Exception e) {
                                                    }

                                                } else {
                                                    globals.writeToMLOG(Clock.getHumanTime() + " TOOLBAR.photoUploadToServer.onResponse.SECOND.state-false.error&crutch: " + error + " |||crutch: " + crutch + "\n");
                                                    try {
                                                        RealmManager.INSTANCE.executeTransaction(transaction);
                                                        RealmManager.stackPhotoSavePhoto(photoDB);
                                                    } catch (Exception e) {
                                                    }
                                                }


                                                callback.onFailure("(Выгрузка фото)Возникла ошибка: " + error + response.body().toString());
                                            } else {
                                                callback.onFailure("Фото не выгружено. Сообщите об этом руководителю. Ответ от сервера: " + jsonR);
                                            }
                                        } catch (Exception e) {
                                            callback.onFailure("Фото не выгружено." + e + response.body().toString());
                                        }
                                    } else {
                                        callback.onFailure("Ошибка: " + jsonR + response.body().toString());
                                    }
                                } else {
                                    callback.onFailure("Пустой ответ от сервера: " + response);
                                }
                            } catch (Exception e) {
                                callback.onFailure("Ошибка при выгрузке фото - повторите попытку позже или обратитесь к Вашему руководителю. \nОшибка: " + e + response.body().toString());
                            }
                        }

                        Log.e("TAG_REALM_LOG", "ОКОНЧЕНА ПОПЫТКА ВЫГРУЗКИ ФОТО с ID: " + photoDB.getId());
                        photoUploaded = true;
                        callback.onSuccess();
                    }

                    @Override
                    public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {

                        globals.writeToMLOG(Clock.getHumanTime() + "TOOLBAR.onFailure.НЕ Успешный ответ: id фото выгрузки: " + photoId + " Код ошибки: " + t.toString() + "\n");


                        Log.e("TAG_REALM_LOG", "ОШИБКА ПРИ ВЫГРУЗКЕ ФОТО с ID: " + photoDB.getId());
                        Log.e("TAG_REALM_LOG", "FAILURE");
                        Log.e("TAG_SEND_PHOTO", "FAILURE: " + t.getMessage());

                        try {
                            Log.e("TAG_REALM_LOG", "ЗАПИСЬ 5");
//                            RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "Ошибка при выгрузке фото(FAILURE): " + t, 1088, null, null, null, Integer.parseInt(user_id), null, Globals.session, finalDate1)));
                        } catch (Exception e) {
                            Log.e("TAG_REALM_LOG", "Ошибка(5): " + e);
                        }

                        Log.e("TAG_SEND_PHOTO", "FAILURE: " + t.getMessage());
                        Toast.makeText(toolbar_menus.this, "Проблема с связью: " + t, Toast.LENGTH_SHORT).show();
                        photoUploaded = true;
                        callback.onFailure(t.toString());
                    }
                });
            } catch (Exception e) {
//                RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB()+1, System.currentTimeMillis()/1000, "Ошибка при выгрузке фото: " + e, 1088, null, null, null, null, null, Globals.session, finalDate)));
            }
        }
    }


    /**
     * b647dc0294942f02356b6366bcdef29d
     * b647dc0294942f02356b6366bcdef29d
     * <p>
     * b647dc0294942f02356b6366bcdef29d
     * 912b29e9e9ea53fefb35c7238e9df88c
     * c7a96d61a66702e0e00bdf1adbdc4fe4
     * <p>
     * !!! Возможно тут будет проблема с ДВИ.
     * <p>
     * ужс
     * Функция 10сек крона которая будет получать по хэшам выгруженые на Сайт фото и удалять их
     * (Счётчик глобальный)
     * Также запускает автовыгрузку фоток
     */
    public void cronCheckUploadsPhotoOnServer() {
        try {
            final RealmResults<StackPhotoDB> realmResults = RealmManager.stackPhotoGetHashs();

            Log.e("CHECK_HASH", "realmResults: " + realmResults.size());

            Globals.writeToMLOG("INFO", "cronCheckUploadsPhotoOnServer", " START realmResults/size: " + realmResults.size());

            if (!realmResults.isEmpty()) {
                Toast.makeText(toolbar_menus.this, "Сервер не обработал: " + realmResults.size() + 1 + " фоток.", Toast.LENGTH_SHORT);

                ArrayList<String> listHash = new ArrayList<>();
                for (int i = 0; i < realmResults.size(); i++) {
                    listHash.add(i, realmResults.get(i).getPhoto_hash());
                }

                // TODO нормально оформить этот запрос
/*                StandartData standartData = new StandartData();
                standartData.mod = "images_view";
                standartData.act = "list_image";
                standartData.nolimit = "no_limit";
                standartData.date_from = Clock.lastWeek();
                standartData.date_to = Clock.tomorrow;
                standartData.hash_list = listHash;

                JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(standartData), JsonObject.class);

                retrofit2.Call<JsonObject> callJS = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
//                retrofit2.Call<PhotoHash> callPH = RetrofitBuilder.getRetrofitInterface().SEND_PHOTO_HASH_NEW(RetrofitBuilder.contentType, convertedObject);

                callJS.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        Log.e("test", "test: " + response);
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e("test", "test t: " + t);
                    }
                });*/

                String mod = "images_view";
                String act = "list_image";
                String noLimit = "no_limit";
                String date_from = Clock.lastWeek();
                String date_to = Clock.tomorrow;


                retrofit2.Call<PhotoHash> call = RetrofitBuilder.getRetrofitInterface()
                        .SEND_PHOTO_HASH(mod, act, noLimit, date_from, date_to, listHash);
                call.enqueue(new retrofit2.Callback<PhotoHash>() {
                    @Override
                    public void onResponse(Call<PhotoHash> call, Response<PhotoHash> response) {

                        try {
                            Gson gson = new Gson();
                            String json = gson.toJson(response.body());
                            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                            Log.e("CHECK_HASH", "Response convertedObject: " + convertedObject);

                            Globals.writeToMLOG("INFO", "cronCheckUploadsPhotoOnServer", "size: " + response.body().getList().size());
                            Globals.writeToMLOG("INFO", "cronCheckUploadsPhotoOnServer", "convertedObject: " + convertedObject);
                        } catch (Exception e) {
                            Globals.writeToMLOG("ERROR", "cronCheckUploadsPhotoOnServer", "Exception e: " + e);
                            Log.e("CHECK_HASH", "Exception e: " + e);
                        }


                        try {

                            for (PhotoHashList item : response.body().getList()) {
                                Log.e("CHECK_HASH", "item.getID(): " + item.getID());
                                Log.e("CHECK_HASH", "item.getHash(): " + item.getHash());
                            }

                            if (response.isSuccessful() && response.body() != null) {
                                if (response.body().getState()) {
                                    if (response.body().getList() != null && response.body().getList().size() > 0) {
                                        List<PhotoHashList> list = response.body().getList();

                                        for (PhotoHashList itemSite : list) {
                                            for (StackPhotoDB itemApp : realmResults) {

                                                String hashSite = itemSite.imgHash;
                                                String hashApp = itemApp.getPhoto_hash();

                                                if (hashSite.equals(hashApp)) {
                                                    Log.e("CHECK_HASH", "Данные найдены. HashS: " + hashSite + " \tHashS: " + hashApp);
                                                    try {
                                                        Log.e("CHECK_HASH", "Запись в БД");
                                                        RealmManager.INSTANCE.executeTransaction(realm -> {
                                                            itemApp.setGet_on_server(System.currentTimeMillis());
                                                            itemApp.setPhotoServerId(itemSite.getID());
                                                        });
                                                        RealmManager.stackPhotoSavePhoto(itemApp);
                                                    } catch (Exception e) {
                                                        Log.e("CHECK_HASH", "Exception e: " + e);
                                                    }
                                                } else {
                                                    Log.e("CHECK_HASH", "Данные НЕ найдены. HashS: " + hashSite + " \tHashS: " + hashApp);
                                                }

                                            }
                                        }

                                    } else {
                                        Log.d("test", "Нет данных1");
                                    }
                                } else {
                                    Log.d("test", "Нет данных2");
                                }
                            }
                        } catch (Exception e) {
//                            Toast.makeText(toolbar_menus.this, "Ошибка при проверке фото с сервера: " + e, Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Call<PhotoHash> call, Throwable t) {
                        Log.d("test", "Нет данных3");
                    }
                });
            } else {
                Log.d("test", "Нет данных4");
            }
        } catch (Exception e) {
            Toast.makeText(toolbar_menus.this, "Ошибка при проверке фото: " + e, Toast.LENGTH_SHORT).show();
        }

    }


    //Проверки для меню

    /**
     * Отвечает за общий цвет светофора
     */

    private void lightStatus() {

        // Получение данных о посещении
        try {
            if (wpDataObj != null) {
                wpDataObj = PhotoReportActivity.wpDataObj;
            }
        } catch (Exception e) {

        }

        // Новая реализация.
        // Определение местоположения
        //  К - нет определения (выкл/нули возвращает)
        //  Ж - определил, не на месте/срок определения истёк
        //  З - Определился не более пол часа назад

//        Log.e("КООРДИНАТЫ", "lightsStatusGPS():" + lightsStatusGPS());

        switch (lightsStatusGPS()) {
            case (1):
                if (menu != null)
                    light.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_green));
                break;

            case (2):
                if (menu != null)
                    light.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_yellow));
                break;

            case (3):

            case (4):
                if (menu != null)
                    light.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_red));
                break;

            default:
                if (menu != null)
                    light.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_gray));
        }


    }


    /**
     * Отвечает за обновление статуса Геоданных в меню светофора
     */

    @SuppressLint("SimpleDateFormat")
    public int lightsStatusGPS() {
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.action_check);
            Globals.provider = ua.com.merchik.merchik.trecker.Coordinates();

            if (Globals.locationGPS != null || Globals.locationNET != null) {
                long dv = Globals.CoordTime;// its need to be in milisecond
                Date df = new java.util.Date(dv);
                Globals.lastGPSData = new SimpleDateFormat("MM-dd").format(df);
                Globals.lastGPSTime = new SimpleDateFormat("HH:mm:ss").format(df);

                Globals.delayGPS = System.currentTimeMillis() - Globals.CoordTime;
                Globals.delayGPS = (Globals.delayGPS / 1000 - 1800) / 60;

                // нет определения (выкл/нули возвращает)
                if (Globals.CoordX == 0 || Globals.CoordY == 0) {
                    item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_red));
                    return 3; // Не на месте/Данные устарели
                } else if (!onTT() || Globals.delayGPS > Globals.dalayMaxTimeGPS) {
                    item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_yellow));
                    return 2; // Опознан не на месте/срок определения истёк
                } else {
                    item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_green));
                    return 1; // Всё окей
                }

            } else {
                item.setIcon(ContextCompat.getDrawable(this, R.mipmap.light_red));
                return 4; // Данные GPS не доступны в принципе
            }
        }
        return 5;
    }


    /**
     * 09.07.2020
     * Проверка дистанции ДО рабочего места. (ТТ - Торговая Точка)
     *
     * @return true -- опознан на рабочем месте; false -- нет.
     */

    private boolean onTT() {
        if (wpDataObj != null) {
            if (wpDataObj.getLongitude() != null && wpDataObj.getLatitude() != null && wpDataObj.getLatitude() > 0 && wpDataObj.getLongitude() > 0) {
                Globals.distanceAB = ua.com.merchik.merchik.trecker.coordinatesDistanse(wpDataObj.getLatitude(), wpDataObj.getLongitude(), Globals.CoordX, Globals.CoordY);
                if (Globals.distanceAB <= Globals.distanceMin) {
                    return true;
                } else if (Globals.distanceAB > Globals.distanceMin) {
                    return false;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private void calcGPSData() {
        int status = lightsStatusGPS();

        String provider = "С помощью ";
        if (Globals.provider == 1) {
            provider = provider + "GPS\n\n";
        } else if (Globals.provider == 2) {
            provider = provider + "NET\n\n";
        } else {
            provider = "Не удалось определить координаты\n\n";
        }

        if (status == 1) {
            msgDialogSystemTxt = String.format(provider + "Координаты GPS актуальны по состоянию на:\nДата: %s \nВремя: %s\n\nКоординаты: \nlat: %s\nlon: %s", Globals.lastGPSData, Globals.lastGPSTime, Globals.CoordX, Globals.CoordY);
            msgDialogUserTxt = String.format("Данные получены %s минут назад. \nЗамечаний по определению местоположения(МП) нет", Globals.delayGPS);
        } else if (status == 2) {
            if (Globals.delayGPS > Globals.dalayMaxTimeGPS) {
                msgDialogSystemTxt = String.format(provider + "Координаты GPS актуальны по состоянию на:\nДата: %s \nВремя: %s\n\nКоординаты: \nlat: %s\nlon: %s \n\nДанные просрочены на %s минут", Globals.lastGPSData, Globals.lastGPSTime, Globals.CoordX, Globals.CoordY, Globals.delayGPS);
                msgDialogUserTxt = String.format("Данные о местоположении получены %s минут назад (что больше 30минут). \n\nВыйдите из помещения и подождите для того что б получить новые данные о своём местоположении.", Globals.delayGPS);
            } else if (wpDataObj != null) {
                Float lat = wpDataObj.getLatitude();
                Float lon = wpDataObj.getLongitude();
                if (lat >= 0 && lon >= 0 && Globals.distanceAB > Globals.distanceMin) {
                    if (Globals.distanceAB > 1000) {
                        Globals.distanceAB = Globals.distanceAB / 1000;
                        Globals.measure = "км";
                    }
                    msgDialogSystemTxt = String.format(provider + "Координаты GPS актуальны по состоянию на:\nДата: %s \nВремя: %s\n\nКоординаты: \nlat: %s\nlon: %s", Globals.lastGPSData, Globals.lastGPSTime, Globals.CoordX, Globals.CoordY);
                    msgDialogUserTxt = String.format("Расстояние от места выполнения работ %.1f %s (что больше 500 метров). \n\nВыйдите из помещения и подождите для того что б получить новые данные о своём местоположении.", Globals.distanceAB, Globals.measure);
                }
            } else {
                msgDialogSystemTxt = String.format(provider + "Координаты GPS актуальны по состоянию на:\nДата: %s \nВремя: %s\n\nКоординаты: \nlat: %s\nlon: %s", Globals.lastGPSData, Globals.lastGPSTime, Globals.CoordX, Globals.CoordY);
                msgDialogUserTxt = "Выберите посещение";
            }
        } else if (status == 3) {

            String s = provider + "Координаты МП не определены.";

            String ig = "";
            String in = "";

            if (Globals.locationGPS != null) {
                ig = Globals.locationGPS.toString();
            }

            if (Globals.locationNET != null) {
                in = Globals.locationNET.toString();
            }

            msgDialogSystemTxt = s + "\n---GPS---\n" + ig + "\n\n---NET---\n" + in;
            msgDialogUserTxt = "Включите/Перезапустите GPS на данном устройстве, выйдите из помещения и подождите пока сигнал поменяет свой цвет.";
        } else {

            String s = provider + "Координаты МП не определены.";

            String ig = "";
            String in = "";

            if (Globals.locationGPS != null) {
                ig = Globals.locationGPS.toString();
            }

            if (Globals.locationNET != null) {
                in = Globals.locationNET.toString();
            }


            msgDialogSystemTxt = s + "\n---GPS---\n" + ig + "\n\n---NET---\n" + in;
            msgDialogUserTxt = "Не удалось определить Метоположение. Попробуйте перезагрузить GPS, выйти на улицу и повторить попытку.";
        }
    }


    /**
     * Диалог с Картой
     */

    private DialogMap dialogMap;

    public void dialogMap() {
        getDataFromIntent();
        calcGPSData();

        Log.e("MANAGER_COORD", "wpDataObj: ERROR?");
        Log.e("MANAGER_COORD", "wpDataObj: " + wpDataObj);

        if (dialogMap == null) dialogMap = new DialogMap(this);
        if (wpDataObj != null) {
            if (wpDataObj.getLatitude() != null && wpDataObj.getLongitude() != null) {

                Log.e("MANAGER_COORD", "wpDataObj.getLatitude()" + wpDataObj.getLatitude());

                dialogMap.setSpot(wpDataObj.getLatitude(), wpDataObj.getLongitude());
                dialogMap.updateMap();
            }
        }
        dialogMap.setData(msgDialogSystemTxt, msgDialogUserTxt);
        dialogMap.show();
    }

    public void pingServer(int mode) {
        // Если Меню создано и нашло нужный Элемент
        if (ib != null) {
            Drawable background = ib.getBackground();
            RequestBody mod = RequestBody.create(MediaType.parse("text/plain"), "ping");
            RequestBody time = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(System.currentTimeMillis()));

            Log.e("PING_SERVER", "Here: " + System.currentTimeMillis() / 1000);
            Log.e("PING_SERVER", "mode: " + mode);

            MultipartBody.Part body = null;
            if (mode == 2) {
                try {
                    Log.e("pingServer", "Создание фотки. try. mode: " + mode);
                    String imageFileName = "TEST_PHOTO_SERV";
                    String root = Environment.getExternalStorageDirectory().toString() + "/Merchik/Test";
                    File myDir = new File(root);

                    String exists = String.valueOf(myDir.exists());
                    String isDirectory = String.valueOf(myDir.isDirectory());
                    String canRead = String.valueOf(myDir.canRead());
                    String canWrite = String.valueOf(myDir.canWrite());

                    Log.e("pingServer", "myDir.exists(): " + exists);
                    Log.e("pingServer", "myDir.isDirectory(): " + isDirectory);
                    Log.e("pingServer", "myDir.canRead(): " + canRead);
                    Log.e("pingServer", "myDir.canWrite(): " + canWrite);


                    boolean b = myDir.mkdirs();

                    Log.e("pingServer", "Создание папки: " + b);
                    Log.e("pingServer", "root: " + root);

                    String fname = imageFileName + ".jpg";
                    File image = new File(myDir, fname);
                    @SuppressLint("ResourceType") InputStream inputStream = getResources().openRawResource(R.drawable.test_server_photo);
                    OutputStream out = new FileOutputStream(image);
                    byte buf[] = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0)
                        out.write(buf, 0, len);
                    out.close();
                    inputStream.close();
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), image);
                    body = MultipartBody.Part.createFormData("image", image.getName(), requestFile);
                } catch (IOException e) {
                    Log.e("pingServer", "Создание фотки. catch. mode: " + mode);

                    Log.e("PING_SERVER", "FILE: E: " + e);
                }
            }

            retrofit2.Call<ServerConnection> call = RetrofitBuilder.getRetrofitInterface().PING_SERVER(mod, time, body);
            Globals.writeToMLOG("INFO", "pingServer/pingServer", "call: " + call);
            call.enqueue(new retrofit2.Callback<ServerConnection>() {
                @Override
                public void onResponse(retrofit2.Call<ServerConnection> call, retrofit2.Response<ServerConnection> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getState()) {

                                String msg = String.format(": \nServReply: %s\nServTime: %s\nServState: %s\nServT: %s", response.body().getReply(),
                                        response.body().getServer_time(),
                                        response.body().getState(),
                                        response.body().getT());
                                Log.e("PING_SERVER", msg);
                                long currentTime = System.currentTimeMillis();
                                long difference = currentTime - response.body().getT();
                                internetSpeed = difference;
                                Log.e("PING_SERVER", "StartTime: " + response.body().getT() + " CurrentTime: " + currentTime + " difference: " + difference);

                                if (mode == 2) {
                                    Toast.makeText(toolbar_menus.this, "Задержка составляет: " + difference + "млс.", Toast.LENGTH_LONG).show();
                                }

                                if (background instanceof ShapeDrawable) {
                                    ((ShapeDrawable) background).getPaint().setColor(ContextCompat.getColor(toolbar_menus.this, R.color.greenCol));
                                } else if (background instanceof GradientDrawable) {
                                    ((GradientDrawable) background).setColor(ContextCompat.getColor(toolbar_menus.this, R.color.greenCol));
                                } else if (background instanceof ColorDrawable) {
                                    ((ColorDrawable) background).setColor(ContextCompat.getColor(toolbar_menus.this, R.color.greenCol));
                                }
                            }
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "pingServer/onResponse", "Exception e: " + e);
                        Globals.writeToMLOG("ERROR", "pingServer/onResponse", "response: " + response);
                        if (background instanceof ShapeDrawable) {
                            ((ShapeDrawable) background).getPaint().setColor(ContextCompat.getColor(toolbar_menus.this, R.color.red_error));
                        } else if (background instanceof GradientDrawable) {
                            ((GradientDrawable) background).setColor(ContextCompat.getColor(toolbar_menus.this, R.color.red_error));
                        } else if (background instanceof ColorDrawable) {
                            ((ColorDrawable) background).setColor(ContextCompat.getColor(toolbar_menus.this, R.color.red_error));
                        }
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ServerConnection> call, Throwable t) {
                    Log.e("PING_SERVER", "err");
                    Globals.writeToMLOG("ERROR", "pingServer/onFailure", "Throwable t: " + t);

                    if (background instanceof ShapeDrawable) {
                        ((ShapeDrawable) background).getPaint().setColor(ContextCompat.getColor(toolbar_menus.this, R.color.red_error));
                    } else if (background instanceof GradientDrawable) {
                        ((GradientDrawable) background).setColor(ContextCompat.getColor(toolbar_menus.this, R.color.red_error));
                    } else if (background instanceof ColorDrawable) {
                        ((ColorDrawable) background).setColor(ContextCompat.getColor(toolbar_menus.this, R.color.red_error));
                    }
                }
            });
        }
    }


}//END OF CLASS..
