package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportHomeFrag.DetailedReportHomeFrag_VIDEO_LESSONS;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportOptionsFrag.DetailedReportOptionsFrag_VIDEO_LESSONS;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTARFrag.DETAILED_REPORT_FRAGMENT_TAR_VIDEO_LESSONS;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovarsFrag.DETAILED_REPORT_FRAGMENT_TOVAR_VIDEO_LESSONS;
import static ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.Tab3Fragment.TARCommentIndex;
import static ua.com.merchik.merchik.MakePhoto.MakePhoto.CAMERA_REQUEST_PROMOTION_TOV_PHOTO;
import static ua.com.merchik.merchik.MakePhoto.MakePhoto.CAMERA_REQUEST_TAKE_PHOTO_TEST;
import static ua.com.merchik.merchik.MakePhoto.MakePhoto.CAMERA_REQUEST_TAR_COMMENT_PHOTO;
import static ua.com.merchik.merchik.MakePhoto.MakePhoto.PICK_GALLERY_IMAGE_REQUEST;
import static ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery.MakePhotoFromGaleryWpDataDB;
import static ua.com.merchik.merchik.Options.Controls.OptionControlPhotoPromotion.tovarDBOPTION_CONTROL_PROMOTION_ID;
import static ua.com.merchik.merchik.Options.Controls.OptionControlPhotoPromotion.wpDataDBOPTION_CONTROL_PROMOTION_ID;
import static ua.com.merchik.merchik.PhotoReportActivity.exifPhotoData;
import static ua.com.merchik.merchik.PhotoReportActivity.getImageOrientation;
import static ua.com.merchik.merchik.PhotoReportActivity.resaveBitmap;
import static ua.com.merchik.merchik.PhotoReportActivity.resizeImageFile;
import static ua.com.merchik.merchik.data.RealmModels.StackPhotoDB.PHOTO_PROMOTION_TOV;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.realm.RealmResults;
import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Response;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARFragmentHome;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonUserOpinion;
import ua.com.merchik.merchik.PhotoReportActivity;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.Translate;
import ua.com.merchik.merchik.Utils.CustomString;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;
import ua.com.merchik.merchik.data.Database.Room.ViewListSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogAchievement.DialogCreateAchievement;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;
import ua.com.merchik.merchik.dialogs.EKL.DialogEKL;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;
import ua.com.merchik.merchik.toolbar_menus;

@AndroidEntryPoint
public class DetailedReportActivity extends toolbar_menus {

    public static final int NEED_UPDATE_UI_REQUEST = 333;

    private Translate translate = new Translate();
//    private WorkPlan workPlan = new WorkPlan();
    private WpDataDB wpDataDB;
//    private WpDataDB rowWP;

    private WPDataObj wpDataObj;

    private File image;

    public static FloatingActionButton fab, fabViber;
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
    public static List<TovarDB> detailedReportTovList = null; // список ВСЕХ товаров клиента из этого посещения
    public static List<ReportPrepareDB> detailedReportRPList = null;    // список "ТОВАРОВ" которые исполнитель должен выложить на полки

    public static int userId;
//    public static UsersSDB usersSDB = null;

    //==================================


    TabLayout tabLayout;
    ViewPager2 viewPager;

    // Интерфейс
    TextView activity_title;
    TextView textDRDateV, textDRAddrV, textDRCustV, textDRMercV;
    Button buttonTakeKPSfromDR;
    LinearLayout option_signal_layout2;
    public static ImageView imageView;
    private CommentViewModel commentViewModel;

    private boolean isNavigationBlocked = false;

//    public static ImageView imageViewVideoRedDot;


    //    public Data wp;
//    public ArrayList<Data> list = new ArrayList<Data>();

    //----------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Globals.writeToMLOG("INFO", "DetailedReportActivity/ON CREATE", "onCreate");

            // Регистрация ActivityResultLauncher
            requestPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(), isGranted -> {
                        if (isGranted) {
                            // Разрешение получено, продолжайте удаление файла
                            Log.e("deleteFile", "Разрешение получено, продолжайте удаление файла");
                        } else {
                            // Разрешение не получено, обработайте соответствующим образом
                            Log.e("deleteFile", "Например, выведите сообщение пользователю или выполните альтернативные действия");
                        }
                    }
            );

            Globals.writeToMLOG("INFO", "DetailedReportActivity/onCreate", "Открыли по новой активность. Смотри Выше лог - после чего именно.");

            setActivityData();

            registrationPermission();

            SKUPlan = 0;
            SKUFact = 0;
            OFS = 0;   // % сколько нет товаров
            OOS = 0;   // Представленность %

            detailedReportTovList = null;   // Обнуляю переменные для свеже открытого отчёта.
            detailedReportRPList = null;    // Обнуляю переменные для свеже открытого отчёта.

            // Задачи для Закладочки "ЗИР"
            tarList = SQL_DB.tarDao().getAllByInfo(1, wpDataDB.getClient_id(), wpDataDB.getAddr_id(), (System.currentTimeMillis() / 1000 - 5184000), 0);
            tasksAndReclamationsSDBList = SQL_DB.tarDao().getAllByInfo(0, wpDataDB.getAddr_id());

            globals.writeToMLOG( "DetailedReportActivity.onCreate: " + "ENTER" + "\n");

            setContentView(R.layout.drawler_dr);

            setSupportActionBar(findViewById(R.id.my_toolbar));
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            activity_title = findViewById(R.id.activity_title);

            tabLayout = findViewById(R.id.tabLayout);
            viewPager = findViewById(R.id.viewPager);

            imageView = findViewById(R.id.red_dot);


            Log.e("TRANSLATES_DEBUG", "Globals.langId: " + Globals.langId);

            TranslatesSDB home = translate.getTranslationText("DetailedReportHomeFrag_Title");
            if (home != null) {
                tabLayout.getTabAt(0).setText("home.nm");
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
            tarTabTitle.append(getText(R.string.title_tab_3));
            if (tarList != null && tarList.size() > 0) {
                tarTabTitle.append("(");
                if (tasksAndReclamationsSDBList != null && tasksAndReclamationsSDBList.size() > 0) {
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
                globals.writeToMLOG( "DetailedReportActivity.onCreate.fab: " + "ENTER" + "\n");
                fab = findViewById(R.id.fab);
                fabViber = findViewById(R.id.fab_viber1);

                toolbar_menus.textLesson = 818;
//            toolbar_menus.videoLesson = 819;
//            toolbar_menus.videoLessons = null;
                toolbar_menus.videoLessons = DetailedReportHomeFrag_VIDEO_LESSONS;
                toolbar_menus.setFab(DetailedReportActivity.this, DetailedReportActivity.fab, () -> {
                    checkVideo(new Integer[videoLesson]);
                }); // ГЛАВНАЯ
                checkVideo(new Integer[videoLesson]);

                Log.e("ЧТО_ПРОИСХОДИТ", "DetailedReportActivity");

                fabViber.setOnClickListener(v -> {
                    String format = CustomString.viberLink();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(format));
                    this.startActivity(intent);
                });

                initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));
                NavigationView navigationView;
                navigationView = findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.nav_dr);
            } catch (Exception e) {
                globals.writeToMLOG( "DetailedReportActivity.onCreate.fab.e: " + e + "\n");
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

            checkVideo(new Integer[]{819});
        } catch (Exception e) {
            Globals.writeToMLOG("INFO", "DetailedReportActivity/ON CREATE", "Exception e: " + e);
        }
    }//--------------------------------------------------------------------- /ON CREATE ---------------------------------------------------------------------

    public static List<ViewListSDB> checkVideos(Integer[] ids, Clicks.clickVoid click) {
        List<ViewListSDB> viewListSDB = new ArrayList<>();
        List<SiteObjectsDB> object = RealmManager.getLesson(ids);
        List<SiteHintsDB> data = null;
        List<Integer> objectLessonIds = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        if (object != null && !object.isEmpty()) {
            for (SiteObjectsDB item : object) {
                String lessonId = item.getLessonId();
                if (lessonId != null && !lessonId.isEmpty()) {
                    objectLessonIds.add(Integer.valueOf(lessonId));
                }
            }

            if (!objectLessonIds.isEmpty()) {
                Integer[] lessonIds = objectLessonIds.toArray(new Integer[0]);
                data = RealmManager.getVideoLesson(lessonIds);
            }
        }

        if (data != null) {
            for (SiteHintsDB item : data) {
                ViewListSDB view = SQL_DB.videoViewDao().getOneByLessonId(item.getID());
                if (view != null) {
                    viewListSDB.add(view);
                }
            }
        }

        return viewListSDB;
    }

    ActivityResultLauncher<String> requestPermissionLauncher;

    private void registrationPermission() {
        // Регистрация ActivityResultLauncher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Разрешение получено, продолжайте удаление файла
                        Log.e("deleteFile", "Разрешение получено, продолжайте удаление файла1");
                    } else {
                        // Разрешение не получено, обработайте соответствующим образом
                        // Например, выведите сообщение пользователю или выполните альтернативные действия
                        Log.e("deleteFile", "Разрешение получено, продолжайте удаление файла2");
                    }
                }
        );
    }

    private void checkAndDeleteFile(Uri fileUri) {
        final String filePath = getRealPathFromURI(fileUri); // Переменная filePath объявлена как final

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Log.e("deleteFile", "getContentResolver().takePersistableUriPermission");
            getContentResolver().takePersistableUriPermission(fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        // Проверяем наличие разрешения на запись во внешнее хранилище
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Разрешение уже предоставлено, продолжайте удаление файла
            Log.e("deleteFile", "PERMISSION_GRANTED");
            deleteFileFromMediaStore(filePath);
        } else {
            // Разрешение не предоставлено, запрашиваем его у пользователя
            Log.e("deleteFile", "Разрешение не предоставлено, запрашиваем его у пользователя");
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void deleteFileFromMediaStore(String filePath) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Используем SAF для удаления файла
            Uri uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
            String selection = MediaStore.MediaColumns.DATA + "=?";
            String[] selectionArgs = new String[]{filePath};

            int deletedRows = getContentResolver().delete(uri, selection, selectionArgs);

            if (deletedRows > 0) {
                // Файл успешно удален
                Log.e("deleteFile", "Файл успешно удален");
            } else {
                // Возникла ошибка при удалении файла
                Log.e("deleteFile", "Возникла ошибка при удалении файла");
            }
        } else {
            // Версия Android ниже 10, можно использовать старый метод удаления файла
            File file = new File(filePath);
            boolean deleted = file.delete();

            if (deleted) {
                // Файл успешно удален
                Log.e("deleteFile", "Файл успешно удален1");
            } else {
                // Возникла ошибка при удалении файла
                Log.e("deleteFile", "Возникла ошибка при удалении файла1");
            }
        }
    }

    private void deleteFileFromMediaStore2(String filePath) {
        ContentResolver contentResolver = getContentResolver();
        Uri mediaStoreUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = new String[]{filePath};

        // Получение ID файла в MediaStore
        long fileId = -1;
        try (Cursor cursor = contentResolver.query(mediaStoreUri, null, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                fileId = cursor.getLong(idColumn);
            }
        }

        if (fileId != -1) {
            // Создание Uri файла в MediaStore с использованием ID
            Uri fileContentUri = ContentUris.withAppendedId(mediaStoreUri, fileId);

            // Удаление файла из MediaStore
            int deletedRows = contentResolver.delete(fileContentUri, null, null);
            if (deletedRows > 0) {
                // Файл успешно удален
                Log.e("deleteFile", "Файл успешно удален");
            } else {
                // Не удалось удалить файл
                Log.e("deleteFile", "Не удалось удалить файл");
            }
        } else {
            // Файл не найден в MediaStore
            Log.e("deleteFile", "Файл не найден в MediaStore");
        }
    }


    /*Получаю строчку с Пална работ и храню её для Отчёта*/
/*    private void setActivityData() {
        Intent i = getIntent();
        rowWP = (WpDataDB) i.getSerializableExtra("rowWP");
        Data wp = (Data) i.getSerializableExtra("dataFromWP");

        list.addAll(Collections.singleton(wp));

        wpDataDB = RealmManager.getWorkPlanRowById(list.get(0).getId());
    }*/

    private void setActivityData() {
        Intent i = getIntent();

        wpDataDB = RealmManager.INSTANCE.copyFromRealm(WpDataRealm.getWpDataRowById(i.getLongExtra("WpDataDB_ID", 0)));

        // Получаем ViewModel и передаем туда данные
        commentViewModel = new ViewModelProvider(this).get(CommentViewModel.class);
        commentViewModel.setWpDataDB(wpDataDB);

        DetailedReportViewModel detailedReportViewModel = new ViewModelProvider(this).get(DetailedReportViewModel.class);
        detailedReportViewModel.setWpDataDB(wpDataDB);


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

                    standartData.addr_id = Collections.singletonList(wpDataDB.getAddr_id());
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

    public void checkVideo(Integer[] ids) {
        try {
            List<ViewListSDB> viewListSDB = null;
            List<SiteObjectsDB> object = RealmManager.getLesson(ids);
            List<SiteHintsDB> data = null;
            List<Integer> objectLessonIds = new ArrayList<>();
            StringBuilder sb = new StringBuilder();

            if (object != null && !object.isEmpty()) {
                for (SiteObjectsDB item : object) {
                    String lessonId = item.getLessonId();
                    if (lessonId != null && !lessonId.isEmpty()) {
                        objectLessonIds.add(Integer.valueOf(lessonId));
                    }
                }

                if (!objectLessonIds.isEmpty()) {
                    Integer[] lessonIds = objectLessonIds.toArray(new Integer[0]);
                    data = RealmManager.getVideoLesson(lessonIds);
                }
            }

            if (data != null && data.size() > 0) {
                for (SiteHintsDB item : data) {
                    sb.append(item.getNm()).append("\n");
                }
                viewListSDB = SQL_DB.videoViewDao().getByLessonId(data.get(0).getID());
            }


            if (viewListSDB != null && viewListSDB.size() != 0) {
                imageView.setVisibility(View.GONE);
            } else {
                imageView.setVisibility(View.VISIBLE);
                Snackbar.make(imageView.getRootView(), "Вы просмотрели ещё не все ролики: " + sb, Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DetailedReportActivity/checkVideo", "Exception e: " + e);
        }
    }

    private void setTab() {
        try {
            Globals.writeToMLOG("INFO", "DetailedReportActivity/setTab", "setTab");
            // 02.10.23. Убрал вечную проверку фоток Товаров при входе в отчёт. У некоторых мерчей это занимает сильно много ресурсов.
//            List<TovarDB> dataTovar = RealmManager.INSTANCE.copyFromRealm(RealmManager.getTovarListFromReportPrepareByDad2(wpDataDB.getCode_dad2()));
//            Log.e("getTovarImg", "dataTovar: " + dataTovar);
//            if (dataTovar != null) {
//                List<TovarDB> dataTovarDownloadList = RealmManager.getTovarListPhotoToDownload(dataTovar, "small");
//                TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading();
//                tablesLoadingUnloading.getTovarImg(dataTovar, "small");
//            }

            Globals.writeToMLOG("INFO", "DetailedReportTab/0", "setTab create");
            adapter = new DetailedReportTab(this, getSupportFragmentManager(), getLifecycle(), tabLayout.getTabCount(), wpDataDB);
            viewPager.setAdapter(adapter);


            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {

//
                    if (commentViewModel.isSaved().getValue()) {
                        // Показываем диалог
                        showSaveDialog();
                        // Отменяем перелистывание, возвращаясь на предыдущую страницу
//                        viewPager.setCurrentItem(0, false);
                    } else {
                        TabLayout.Tab tab = tabLayout.getTabAt(position);
                        if (tab != null && !tab.isSelected()) {
                            tab.select();
                        }
                    }
                }
            });

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    Log.e("onTabSelected", "tabLayout.getTabCount(): " + tabLayout.getTabCount());
                    Log.e("onTabSelected", "tab.getPosition(): " + tab.getPosition());


                    if (tab.getPosition() == 0) {
                        Log.e("onTabSelected", "ГЛАВНАЯ");
                        toolbar_menus.textLesson = 818;
//                    toolbar_menus.videoLesson = 819;
//                    toolbar_menus.videoLessons = null;
                        toolbar_menus.videoLessons = DetailedReportHomeFrag_VIDEO_LESSONS;
                        toolbar_menus.setFab(DetailedReportActivity.this, DetailedReportActivity.fab, () -> {
                            checkVideo(new Integer[]{videoLesson});
                        }); // ГЛАВНАЯ
                        checkVideo(new Integer[]{videoLesson});

                    } else if (tab.getPosition() == 1) {
                        Log.e("onTabSelected", "ОПЦИИ");

                        toolbar_menus.textLesson = 820;
//                    toolbar_menus.videoLesson = 821;
                        toolbar_menus.videoLessons = DetailedReportOptionsFrag_VIDEO_LESSONS;
                        toolbar_menus.setFab(DetailedReportActivity.this, DetailedReportActivity.fab, () -> {
                            checkVideo(new Integer[]{videoLesson});
                        }); // ОПЦИИ
                        checkVideo(new Integer[]{videoLesson});

                    } else if (tab.getPosition() == 2) {
                        Log.e("onTabSelected", "ТОВАРЫ");

                        toolbar_menus.textLesson = 822;
//                    toolbar_menus.videoLesson = 823;
                        toolbar_menus.videoLessons = DETAILED_REPORT_FRAGMENT_TOVAR_VIDEO_LESSONS;
                        toolbar_menus.setFab(DetailedReportActivity.this, DetailedReportActivity.fab, () -> {
                            checkVideo(new Integer[]{videoLesson});
                        }); // ТОВАР
                        checkVideo(new Integer[]{videoLesson});

                    } else if (tab.getPosition() == 3) {
                        Log.e("onTabSelected", "ЗИР");

                        toolbar_menus.textLesson = 822;
                        toolbar_menus.textLesson = 4225;
//                    toolbar_menus.videoLesson = 3527;
                        toolbar_menus.videoLessons = DETAILED_REPORT_FRAGMENT_TAR_VIDEO_LESSONS;
                        toolbar_menus.setFab(DetailedReportActivity.this, DetailedReportActivity.fab, () -> {
                            checkVideo(videoLessons);
                        }); // ЗИР
                        checkVideo(videoLessons);
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
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DetailedReportActivity/setTab", "Exception e: " + e);
        }
    }


    private void showSaveDialog() {
        new MessageDialogBuilder(this)
                .setTitle("Вы не сохранили комментарий")
                .setSubTitle("Текст комментария к текущему визиту:")
                .setMessage(commentViewModel.getComment().getValue())
                .setStatus(DialogStatus.ALERT)
                .setOnCancelAction(() -> {
                    commentViewModel.setSave(false);
                    commentViewModel.updateComment("");
                    return Unit.INSTANCE;
                })
                .setOnConfirmAction("Сохранить", () -> {
                    commentViewModel.setSave(false);

                    long startTime = System.currentTimeMillis() / 1000;
                    RealmManager.INSTANCE.executeTransaction(realm -> {
                        wpDataDB.setDt_update(startTime);
                        wpDataDB.user_comment = commentViewModel.getComment().getValue();
                        wpDataDB.user_comment_author_id = wpDataDB.getUser_id();
                        wpDataDB.user_comment_dt_update = startTime;
                        wpDataDB.startUpdate = true;
                        realm.insertOrUpdate(wpDataDB);
                    });

                    // Это жосткие костыли
                    Exchange exchange = new Exchange();
                    exchange.sendWpDataToServer(new Click() {
                        @Override
                        public <T> void onSuccess(T data) {
                            String msg = (String) data;
                            Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork.onSuccess", "msg: " + msg);
                        }

                        @Override
                        public void onFailure(String error) {
                            Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork.onFailure", "error: " + error);
                        }
                    });

                    return Unit.INSTANCE;
                })
                .show();
    }


    public void refreshAdapterFragmentB() {
        DetailedReportTab.refreshAdapter();
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getApplicationContext().getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor == null) {
            return null;
        }
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }


    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 123;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение на чтение внешнего хранилища получено
                // Вы можете продолжить выполнение действий, требующих разрешения
                Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST.onRequestPermissionsResult", "Разрешение на чтение внешнего хранилища получено");
            } else {
                // Разрешение на чтение внешнего хранилища отклонено
                // Вы можете выполнить действия, когда разрешение не было предоставлено
                Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST.onRequestPermissionsResult", "Разрешение на чтение внешнего хранилища отклонено");
            }
        }
    }

    private boolean checkManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return true;
        }
    }


    // Размещение фотки по URI адрессу для пользователя, отображение фотографии загрузки
    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult", "requestCode / resultCode / data: " + requestCode + "/" + resultCode + "/" + data);

            if (requestCode == NEED_UPDATE_UI_REQUEST && resultCode == RESULT_OK) {

                if (DialogCreateAchievement.onUpdateUI != null)
                    DialogCreateAchievement.onUpdateUI.update();

                if (DialogEKL.onUpdateUI != null)
                    DialogEKL.onUpdateUI.update();

                if (OptionButtonUserOpinion.onUpdateUI != null)
                    OptionButtonUserOpinion.onUpdateUI.update();

            }

            if (requestCode == PICK_GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                try {
//                    int photoType = data.getIntExtra("photo_type", 4); // Получаем тип фотографии из Intent
                    int photoType = MakePhotoFromGalery.photoType; // Получаем тип фотографии из Intent


                    Uri uri = data.getData();
                    File file = new File(Globals.FileUtils.getRealPathFromUri(getApplicationContext(), uri));
                    StackPhotoDB stackPhotoDB = savePhoto(file, MakePhotoFromGaleryWpDataDB, photoType, MakePhotoFromGalery.tovarId, getApplicationContext());

                    if (stackPhotoDB != null) {
                        // Сохраняем результат что фото сохранено
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("photo_saved", true); // Передайте информацию о сохраненном фото
                        setResult(Activity.RESULT_OK, resultIntent);
                    }
                } catch (Exception e) {
                    Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST", "Exception e: " + e);
                }
            }

            switch (requestCode) {
                case 101:
                    try {
                        globals.writeToMLOG( "DETAILED_REPORT_ACT.onActivityResult: " + "ENTER" + "\n");
                        image = MakePhoto.image;
                        globals.writeToMLOG( "DETAILED_REPORT_ACT.onActivityResult.image: " + image + "\n");

                        Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/101", "Image: " + image);

                        Log.e("dispatchTakePicture", "image: " + image);
                        Log.e("dispatchTakePicture", "imagelength: " + image.length());
                        Log.e("dispatchTakePicture", "imagegetAbsolutePath: " + image.getAbsolutePath());

                        String msg = "";
                        msg = String.format("image: %s \nimagelength: %s\nimagegetAbsolutePath: %s\nresultCode: %s\nrequestCode: %s", image, image.length(), image.getAbsolutePath(), resultCode, requestCode);

                        globals.writeToMLOG( "DETAILED_REPORT_ACT.onActivityResult.image.data: " + msg + "\n");
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

            if (requestCode == CAMERA_REQUEST_TAKE_PHOTO_TEST && resultCode == RESULT_OK) {
                Toast.makeText(this, "Фото сохранено", Toast.LENGTH_SHORT).show();
                savePhoto(globals, this);
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
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "DR/CAMERA_REQUEST_TAR_COMMENT_PHOTO", "Exception e: " + e);
                }
            } else if (requestCode == CAMERA_REQUEST_PROMOTION_TOV_PHOTO && resultCode == RESULT_OK) {
                try {
                    savePhotoPromotionTov(new File(MakePhoto.openCameraPhotoUri), wpDataDBOPTION_CONTROL_PROMOTION_ID, tovarDBOPTION_CONTROL_PROMOTION_ID);
                    // Концептуально тут нужно эту фотку как-то обработать.
                } catch (Exception e) {
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

                            PhotoReportActivity.savePhoto(this, wpDataObj, image, () -> {
                            });

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
                        Globals.alertDialogMsg(this,
                                "Фото не было создано, повторите попытку");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Globals.alertDialogMsg(this,
                        DialogStatus.ERROR,
                        "Произошла ошибка",
                        "Ошибка при выполнении фото: " + e);
            }

            refreshAdapterFragmentB();
        } catch (Exception e) {
            Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult", "Exception e: " + e);
        }
    }

    public static void savePhoto(Globals globals, Activity activity) {
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
                photoFile = resizeImageFile(activity, photoFile);
            } catch (Exception e) {
                globals.alertDialogMsg(activity, "Ошибка В ужатии: " + e);
            }

            exifPhotoData(photoFile);

            Log.e("2222", "hash1 = " + photo.getPhoto_hash());

            if (TextUtils.isEmpty(photo.getPhoto_hash())) {
                String hash;
                hash = globals.getHashMD5FromFile2(photoFile, activity);
                if (hash == null || hash.equals("")) {
                    hash = globals.getHashMD5FromFile(photoFile, activity);
                }
                photo.setPhoto_hash(hash);
            }

            Log.e("2222", "hash2 = " + photo.getPhoto_hash());
            Globals.writeToMLOG("INFO", "A_YA_GOVORIL", "HASH: " + photo.getPhoto_hash());

            photo.setPhoto_num(photoFile.getAbsolutePath());
            photo.setPhoto_type(Integer.valueOf(MakePhoto.photoType));

            photo.dt = MakePhoto.dt;

            photo.img_src_id = MakePhoto.img_src_id;
            photo.showcase_id = MakePhoto.showcase_id;
            photo.planogram_id = MakePhoto.planogram_id;
            photo.planogram_img_id = MakePhoto.planogram_img_id;
            photo.example_id = MakePhoto.example_id;
            photo.example_img_id = MakePhoto.example_img_id;

            if (MakePhoto.photoType.equals("4")) {
                photo.tovar_id = MakePhoto.tovarId;
                Globals.writeToMLOG("INFO", "requestCode == 201 && resultCode == RESULT_OK/photo_save", "MakePhoto.tovarId: " + MakePhoto.tovarId);
            }

            JsonObject jsonObject2 = new Gson().fromJson(new Gson().toJson(photo), JsonObject.class);

            Globals.writeToMLOG("INFO", "requestCode == 201 && resultCode == RESULT_OK/photo_save", "photoSave: " + jsonObject2);

            StackPhotoRealm.setAll(Collections.singletonList(photo));
        } catch (Exception e) {
            Log.i("2222", "Exception ", e);
            Globals.writeToMLOG("ERROR", "requestCode == 201 && resultCode == RESULT_OK", "Exception e: " + e);
        }
    }


    private void requestManageExternalStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 123);
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

            stackPhotoDB.setPhoto_hash(globals.getHashMD5FromFile2(photoFile, this));
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
     */
    private StackPhotoDB savePhotoPromotionTov(File photoFile, WpDataDB wpDataDB, TovarDB tovarDB) {
        try {

            Globals.writeToMLOG("INFO", "OptionControlPhotoPromotion/savePhotoPromotionTov", "wp_dad2: " + wpDataDB.getCode_dad2());
            Globals.writeToMLOG("INFO", "OptionControlPhotoPromotion/savePhotoPromotionTov", "tov.id: " + tovarDB.getiD());

            int id = RealmManager.stackPhotoGetLastId();
            id++;
            StackPhotoDB stackPhotoDB = new StackPhotoDB();
            stackPhotoDB.setId(id);
            stackPhotoDB.setDt(wpDataDB.getDt().getTime() / 1000);
            stackPhotoDB.setTime_event(Clock.getHumanTimeSecPattern(wpDataDB.getDt().getTime() / 1000, "yyyy-MM-dd"));

            stackPhotoDB.setAddr_id(wpDataDB.getAddr_id());
            stackPhotoDB.setAddressTxt(wpDataDB.getAddr_txt());

            stackPhotoDB.setClient_id(wpDataDB.getClient_id());
            stackPhotoDB.setCustomerTxt(wpDataDB.getClient_txt());

            stackPhotoDB.code_dad2 = wpDataDB.getCode_dad2();

            LogMPDB log = Globals.fixMP(wpDataDB, null);
            String GP = log != null ? log.gp : "";
            stackPhotoDB.gp = GP;

            stackPhotoDB.setUser_id(Globals.userId);
            stackPhotoDB.setUserTxt(SQL_DB.usersDao().getUserName(Globals.userId));
            stackPhotoDB.setPhoto_type(PHOTO_PROMOTION_TOV);
            stackPhotoDB.tovar_id = tovarDB.getiD();


            stackPhotoDB.setCreate_time(System.currentTimeMillis());

            String hash = globals.getHashMD5FromFile2(photoFile, this);
            if (hash == null || hash.equals("")) hash = globals.getHashMD5FromFile(photoFile, this);
            stackPhotoDB.setPhoto_hash(hash);

            stackPhotoDB.setPhoto_num(photoFile.getAbsolutePath());

            RealmManager.stackPhotoSavePhoto(stackPhotoDB);
            return stackPhotoDB;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DRActivity.onActivityResult.savePhotoPromotionTov", "Exception e: " + e);
            return null;
        }
    }


    public static StackPhotoDB savePhoto(File file, WpDataDB wpDataDB, int photoType, String tovarId, Context context) {
        try {
            file.exists();
            int id = RealmManager.stackPhotoGetLastId();
            id++;
            StackPhotoDB stackPhotoDB = new StackPhotoDB();
            stackPhotoDB.setId(id);
            stackPhotoDB.setDt(wpDataDB.getDt().getTime() / 1000);
            stackPhotoDB.setTime_event(Clock.getHumanTimeSecPattern(wpDataDB.getDt().getTime() / 1000, "yyyy-MM-dd"));

            stackPhotoDB.setAddr_id(wpDataDB.getAddr_id());
            stackPhotoDB.setAddressTxt(wpDataDB.getAddr_txt());

            stackPhotoDB.setClient_id(wpDataDB.getClient_id());
            stackPhotoDB.setCustomerTxt(wpDataDB.getClient_txt());

            stackPhotoDB.code_dad2 = wpDataDB.getCode_dad2();

            // 10.09.23. Походу Сервер без этого не воспринимает фотографии (сейчас касается это // Тип фото Остатков из ГАЛЕРЕИ)
            LogMPDB log = Globals.fixMP(wpDataDB, null);
            String GP = log != null ? log.gp : "";
            stackPhotoDB.gp = GP;

            stackPhotoDB.setUser_id(Globals.userId);
            stackPhotoDB.setUserTxt(SQL_DB.usersDao().getUserName(Globals.userId));

            stackPhotoDB.setPhoto_type(photoType);
            stackPhotoDB.tovar_id = tovarId;

            stackPhotoDB.setCreate_time(System.currentTimeMillis());

            Globals globals1 = new Globals();
//            String hash = globals.getHashMD5FromFileTEST(uri, context);

//            String hash = Globals.FileHashCalculator.calculateHash(file.getPath());

//            String hash = globals1.getHashMD5FromFile2(file, context);
            String hash = "";
            if (hash == null || hash.equals("")) hash = globals1.getHashMD5FromFile(file, context);
            Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST", "hash: " + hash);

            if (hash == null || hash.equals("")) {
                hash = globals1.getHashMD5FromFile(file, context);
            }

            stackPhotoDB.setPhoto_hash(hash);
            stackPhotoDB.setPhoto_num(file.getAbsolutePath());

            String jo = new Gson().toJson(stackPhotoDB);
            Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST", "stackPhotoDB: " + jo);

            RealmManager.stackPhotoSavePhoto(stackPhotoDB);
            Toast.makeText(context, "Фото (" + id + ") з Галереї успішно збережено!", Toast.LENGTH_LONG).show();
            return stackPhotoDB;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST", "Exception e: " + e);
            return null;
        }
    }
}



