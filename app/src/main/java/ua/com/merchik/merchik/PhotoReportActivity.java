package ua.com.merchik.merchik;

import static ua.com.merchik.merchik.menu_main.getBatteryPercentage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.realm.RealmResults;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.DataFromServer.PhotoData.AddrId;
import ua.com.merchik.merchik.data.DataFromServer.PhotoData.ClientId;
import ua.com.merchik.merchik.data.DataFromServer.PhotoData.ClientTovarGroup;
import ua.com.merchik.merchik.data.DataFromServer.PhotoData.PhotoData;
import ua.com.merchik.merchik.data.PhotoGP.Battery;
import ua.com.merchik.merchik.data.PhotoGP.BrowserInfo;
import ua.com.merchik.merchik.data.PhotoGP.ConnectionInfo;
import ua.com.merchik.merchik.data.PhotoGP.Coords;
import ua.com.merchik.merchik.data.PhotoGP.PhotoGP;
import ua.com.merchik.merchik.data.PhotoGP.ScreenInfo;
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.URLData.URLData;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;


public class PhotoReportActivity extends toolbar_menus {

    Globals globals = new Globals();

    TextView activity_title;
    ImageView ivPhotoReportImageViewPhotoPreview;
    TextView tvPhotoReportTextViewDateValue, tvPhotoReportTextViewAddrValue, tvPhotoReportTextViewClientValue, tvPhotoReportTextViewTovGroupValue, tvPhotoReportTextViewCommentValue;  // Дата, Адрес, Клиент, Группа товара.
    Button bPhotoReportButtonMakePhoto, bPhotoReportButtonSaveAndClose, bPhotoReportButtonMakeMorePhoto, bPhotoReportButtonAddComent;

    public static WPDataObj wpDataObj;

    @SuppressLint("UseSparseArrays")
    Map<Integer, String> mapSpinner = new HashMap<>();

    private static File image;     // Фото из фотоаппарата
    private Uri imageUri;

    //File photoToDB; // Фото скопировано сюда для сохранения в БД

    private static String comment = "";    // Комментарий к фотографии
    public static Integer user_id;
    private static String currentVersion;

    private static final int CAMERA_REQUEST = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("PHOTO_REPORT", "onCreate");
        super.onCreate(savedInstanceState);

        dataFromPreferenceManager();
        setActivityContent();

        setFab(this, findViewById(R.id.fab), () -> {
        });

        initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), findViewById(R.id.nav_view));
        NavigationView navigationView;
        navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_pr);

    }
    //==============================================================================================

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save file url in bundle as it will be null on scren orientation changes
        try {
            outState.putString("file_uri", image.getAbsolutePath());
        } catch (Exception e) {
            // TODO Если переходить из фотоотчёта не сделав фото - будет краш: java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String java.io.File.getAbsolutePath()' on a null object reference
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // get the file url
        Uri uri = Uri.parse(savedInstanceState.getString("file_uri"));
        if (uri != null) {
            image = new File(uri.getPath());
        }
    }


    /**
     * 27.08.2020
     * <p>
     * Кнопка "Сделать фото"
     * Такая штука находится и в MainMenu
     * Я не знаю как они будут между собой драться, надо проверить
     */
    public void makePhoto(View view) {
        if (wpDataObj != null)
            choiceCustomerGroupAndPhoto();
    }


    /**
     * 27.08.2020
     * <p>
     * Кнопка "Сохранить и закрыть"
     */
    public void saveAndClose(View view) {
        if (!savePhotoToDB()) {
            Toast toast = Toast.makeText(this, "Фото сохранить не удалось! Повторите попытку и обновите страничку.\n\nЕсли ошибка повторяется - обратитесь к руководителю.", Toast.LENGTH_LONG);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, "Фото сохранено и готово к отправке.", Toast.LENGTH_SHORT);
            toast.show();
        }

        bPhotoReportButtonAddComent.setVisibility(View.INVISIBLE);       // Скрываем кнопку Комментария
        bPhotoReportButtonSaveAndClose.setVisibility(View.INVISIBLE);    // Скрываем кнопку Сохранить и Закрыть
        bPhotoReportButtonMakeMorePhoto.setVisibility(View.INVISIBLE);   // Скрываем кнопку Сделать ещё фото

        bPhotoReportButtonMakePhoto.setVisibility(View.VISIBLE);    // Отображаем кнопку "Сделать фото"
        ivPhotoReportImageViewPhotoPreview.setImageResource(R.mipmap.merchik_m);    // Сбрасываем по умолчанию Превью

        //image = null; // Обнуляем фотку


//        setupBadge(stackPhotoTableCount()); // Обновляем счётчик
    }


    /**
     * 27.08.2020
     * <p>
     * Кнопка "Сделать ещё фото"
     */
    public void makeMorePhoto(View view) {
        if (!savePhotoToDB()) {
            Toast toast = Toast.makeText(this, "Фото сохранить не удалось! Повторите попытку и обновите страничку.\n\nЕсли ошибка повторяется - обратитесь к руководителю.", Toast.LENGTH_LONG);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, "Фото сохранено и готово к отправке.", Toast.LENGTH_SHORT);
            toast.show();
        }
        image = null; // Обнуляем фотку
        choiceCustomerGroupAndPhoto();
    }


    /**
     * 27.08.2020
     * <p>
     * Кнопка "Добавить комментарий"
     */
    public void addComent(View view) {
        if (tvPhotoReportTextViewCommentValue != null && !comment.equals("")) {
            bPhotoReportButtonAddComent.setText("Изменить комментарий");
        }
        addComment();
    }

    //==============================================================================================


    //==============================================================================================

    /**
     * 18.10.2020
     * Заполняем Активность
     */
    private void dataFromPreferenceManager() {
        user_id = Integer.valueOf(Objects.requireNonNull(PreferenceManager.getDefaultSharedPreferences(this)
                .getString("user_id", null)));
    }


    /**
     * 25.08.2020
     * Заполняем Активность
     */
    private void setActivityContent() {
        setContentView(R.layout.drawler_photo);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        activity_title = (TextView) findViewById(R.id.activity_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity_title.setText(getString(R.string.title_photo_report) + ".");

        ivPhotoReportImageViewPhotoPreview = (ImageView) findViewById(R.id.photoReportImageViewPhotoPreview);

        tvPhotoReportTextViewDateValue = (TextView) findViewById(R.id.photoReportTextViewDateValue);
        tvPhotoReportTextViewAddrValue = (TextView) findViewById(R.id.photoReportTextViewAddrValue);
        tvPhotoReportTextViewClientValue = (TextView) findViewById(R.id.photoReportTextViewClientValue);
        tvPhotoReportTextViewTovGroupValue = (TextView) findViewById(R.id.photoReportTextViewTovGroupValue);
        tvPhotoReportTextViewCommentValue = (TextView) findViewById(R.id.photoReportTextViewCommentValue);

        bPhotoReportButtonMakePhoto = (Button) findViewById(R.id.photoReportButtonMakePhoto);
        bPhotoReportButtonSaveAndClose = (Button) findViewById(R.id.photoReportButtonSaveAndClose);
        bPhotoReportButtonMakeMorePhoto = (Button) findViewById(R.id.photoReportButtonMakeMorePhoto);
        bPhotoReportButtonAddComent = (Button) findViewById(R.id.photoReportButtonAddComent);


        // ПОПРАВИТЬ!!!!!!!!!!!!!!!!!!!!!!
        try {
            currentVersion = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        currentVersion = currentVersion.replaceAll("\\.", "");


        //------------------------------------------------------------------------------------------

        String dateValue = "Не определено";
        String addrValue = "Не определено";
        String clientValue = "Не определено";
        String tovGroupValue = "Не определено";


        //getUrlWpData(); // Получение данных с МВС
        //getDataFromIntent(); // Получение данных для активности

        wpDataObj = new WPDataObj();
        if (!getUrlWpData()) {
            wpDataObj = new WPDataObj();
            getDataFromIntent();
            try {
                if (wpDataObj.getDate() != null && !wpDataObj.getDate().equals(""))
                    dateValue = wpDataObj.getDate();

                if (wpDataObj.getAddressIdTxt() != null && !wpDataObj.getAddressIdTxt().equals(""))
                    addrValue = wpDataObj.getAddressIdTxt();

                if (wpDataObj.getCustomerIdTxt() != null && !wpDataObj.getCustomerIdTxt().equals(""))
                    clientValue = wpDataObj.getCustomerIdTxt();

                if (wpDataObj.getCustomerTypeGrp().size() == 1)
                    tovGroupValue = wpDataObj.getCustomerTypeGrp().values().toArray(new String[0])[0];
                else if (wpDataObj.getCustomerTypeGrp().size() > 1)
                    tovGroupValue = "" + wpDataObj.getCustomerTypeGrp().size() + " групп";
            } catch (Exception e) {
            }

            tvPhotoReportTextViewDateValue.setText(dateValue);
            tvPhotoReportTextViewAddrValue.setText(addrValue);
            tvPhotoReportTextViewClientValue.setText(clientValue);
            tvPhotoReportTextViewTovGroupValue.setText(tovGroupValue);
        }


        Spinner sPT = photoTypeSpinner();   // Получаем спиннер Типа фото
        try {
            sPT.setOnItemSelectedListener(new PhotoReportActivity.MyOnItemSelectedListener());
        } catch (Exception e) {
            //Toast.makeText(menu_main.this, "Err", Toast.LENGTH_LONG).show();
        }

    }


    /**
     * 17.10.2020
     * Получение данных с другой активности.
     */
    private boolean getUrlWpData() {
        JsonObject JSON;
        URLData mvsData;

        if (menu_login.jsonMVS != null) {
            Log.e("MVS_DATA", "Данные о фото пришли через МВС");
            JSON = menu_login.jsonMVS;
            menu_login.jsonMVS = null;
            mvsData = new Gson().fromJson(JSON, URLData.class);
        } else {
            Log.e("MVS_DATA", "Данных о фото с МВС не пришло");
            return false;
        }
        Log.e("MVS_DATA", "Должно отобразиться если данные пришли с МВС");


        Log.e("MVS_DATA", "UriToParseFromSite: " + JSON);
        Log.e("MVS_DATA", "URLData: " + mvsData.getParams().getDate());

        getTxtInfoAboutPhoto(mvsData);
        getTxtInfoAboutAddress(mvsData);

        try {
            wpDataObj.setId(0L);
            wpDataObj.setDate(mvsData.getParams().getDate());
            wpDataObj.setCustomerId(mvsData.getParams().getClientId());
            wpDataObj.setAddressId(Integer.parseInt(mvsData.getParams().getAddrId()));
            wpDataObj.setPhotoType(mvsData.getParams().getImgTypeId());
            wpDataObj.setDocNum(String.valueOf(mvsData.getParams().getDocNum()));
            wpDataObj.setThemeId(mvsData.getParams().getThemeId());
            wpDataObj.setPhotoUserId(mvsData.getParams().getPhotoUserId());
            wpDataObj.setDad2(Long.parseLong(mvsData.getParams().getCodeDad2()));
        } catch (Exception e) {
            Log.e("MVS_DATA", "Разве чо-то поменяется?");
        }

        tvPhotoReportTextViewDateValue.setText(mvsData.getParams().getDate());

        return true;
    }


    /**
     * 25.08.2020
     * Получение данных с другой активности.
     */
    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent.getSerializableExtra("dataFromWPObj") != null) {
            WPDataObj wp = (WPDataObj) intent.getSerializableExtra("dataFromWPObj");
            if (wp != null) {
                Log.e("PHOTO_REPORT", "wp.wpId:" + wp.getId());
                Log.e("PHOTO_REPORT", "wp.wpPhotoType:" + wp.getPhotoType());

                // МЕГАКОСТЫЛЬ, НАДО ФИКСИТЬ КАК БУДЕТ ВРЕМЯ
                WpDataDB wpRow = RealmManager.getWorkPlanRowById(wp.getId());
                user_id = wpRow.getUser_id();

                wpDataObj = wp;
            }
        }
    }


    private void getTxtInfoAboutPhoto(URLData mvsData) {
        Map<Integer, String> map = new HashMap<>();

        String mod = "filter_list";
        String act = "menu_list";
        String client_id = mvsData.getParams().getClientId();
        String addr_id = "";
        String client_tovar_group = "";
        String images_type_list = "";
        String only_selected = "";

        retrofit2.Call<PhotoData> call = RetrofitBuilder.getRetrofitInterface().MVS_DATA_CLI(mod, act, client_id, addr_id, client_tovar_group, images_type_list, only_selected);
        call.enqueue(new retrofit2.Callback<PhotoData>() {
            @Override
            public void onResponse(retrofit2.Call<PhotoData> call, retrofit2.Response<PhotoData> response) {
                Log.e("MVSAboutPhotoC", "SUCCESS: " + response.body().getMenuList().getClientId());

                Log.e("MVSAboutPhotoC", "ClientId: " + mvsData.getParams().getClientId());
                for (ClientId list : response.body().getMenuList().getClientId()) {
                    Log.e("MVSAboutPhotoC", "SUCCESS ListC: " + list.getId() + " |" + list.getNm());
                    if (list.getId().equals(mvsData.getParams().getClientId())) {
                        wpDataObj.setCustomerIdTxt(list.getNm());
                        tvPhotoReportTextViewClientValue.setText(list.getNm());
                    }
                }


                Log.e("MVSAboutPhotoC", "AddrId: " + mvsData.getParams().getAddrId());
                for (AddrId list : response.body().getMenuList().getAddrId()) {
                    Log.e("MVSAboutPhotoC", "SUCCESS ListA: " + list.getId() + " |" + list.getNm());
                    if (list.getId().equals(mvsData.getParams().getAddrId())) {
                        wpDataObj.setAddressIdTxt(list.getNm());
                        tvPhotoReportTextViewAddrValue.setText(list.getNm());
                    }
                }


                Log.e("MVSAboutPhotoC", "ClientTovarGroup: " + mvsData.getParams().getClientTovarGroup());
                for (ClientTovarGroup list : response.body().getMenuList().getClientTovarGroup()) {
                    Log.e("MVSAboutPhotoC", "SUCCESS ListG: " + list.getId() + " |" + list.getNm());
                    if (list.getId().equals(mvsData.getParams().getClientTovarGroup())) {
                        map.put(Integer.valueOf(mvsData.getParams().getClientTovarGroup()), list.getNm());
                        wpDataObj.setCustomerTypeGrp(map);
                        tvPhotoReportTextViewTovGroupValue.setText(list.getNm());
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<PhotoData> call, Throwable t) {
                Log.e("MVSAboutPhotoC", "FAILURE_C_E: " + t.getMessage());
                Log.e("MVSAboutPhotoC", "FAILURE_C_E2: " + t);
            }
        });
    }

    private void getTxtInfoAboutAddress(URLData mvsData) {
        String mod2 = "filter_list";
        String act2 = "menu_list";
        String addr_id2 = mvsData.getParams().getAddrId();
        Log.e("MVSAboutPhotoA", "address_id: " + mvsData.getParams().getAddrId());

        retrofit2.Call<PhotoData> call2 = RetrofitBuilder.getRetrofitInterface().MVS_DATA_ADD(mod2, act2, addr_id2);
        call2.enqueue(new retrofit2.Callback<PhotoData>() {
            @Override
            public void onResponse(retrofit2.Call<PhotoData> call, retrofit2.Response<PhotoData> response) {
                Log.e("MVSAboutPhotoA", "SUCCESS_A: " + response.body());

                for (AddrId list : response.body().getMenuList().getAddrId()) {
                    if (list.getId().equals(mvsData.getParams().getAddrId())) {
                        wpDataObj.setLatitude(Float.valueOf(list.getLat()));
                        wpDataObj.setLongitude(Float.valueOf(list.getLon()));
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<PhotoData> call, Throwable t) {
                Log.e("MVSAboutPhotoA", "FAILURE_A_E: " + t.getMessage());
                Log.e("MVSAboutPhotoA", "FAILURE_A_E2: " + t);
            }
        });
    }


    /**
     * 26.08.2020
     * <p>
     * Заполняем спинер типов фото
     */
    private Spinner photoTypeSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.photoReportSpinnerPhotoType);

        try {
            // Получаем из БД все типы фото что б отобразить их в спинере
            RealmResults<ImagesTypeListDB> imagesTypeList = RealmManager.getAllImagesTypeList();

            // Добавляем в карту ключь:значение данные из БД
            for (ImagesTypeListDB imgType : imagesTypeList) {
                Log.e("SPINER", "type: " + imgType.getNm());
                // Проверка что б поле NM(тип фото.txt) было заполнено (с сервера пустые типы приходят, не мои дела типо)
                if (imgType.getNm() != null && !imgType.getNm().equals(""))
                    mapSpinner.put(imgType.getId(), imgType.getNm());
            }

            String[] result = mapSpinner.values().toArray(new String[0]);   // Конвертация MAP в массив. Карта нужна ради ID из бд, в данном случае ID не нужны

            // Не знаю как правильно работает. Грубо говоря - помещаю список Типов фото в сам спинер.
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, result);

            // Выставляю значение по умолчанию
            try {
                int spinnerPosition = adapter.getPosition(mapSpinner.get(Integer.parseInt(wpDataObj.getPhotoType())));
                adapter.setDropDownViewResource(R.layout.spinner_text);
                spinner.setAdapter(adapter);
                spinner.setSelection(spinnerPosition);
            } catch (Exception e) {
                spinner.setAdapter(adapter);
            }


        } catch (Exception e) {
            globals.alertDialogMsg(this, "Список типов фото получить не удалось, попробуйте нажать в меню 3х точек на \"Перейти на главную\". Если после этого ошибка повторится - обратитесь к Вашему руководителю." + e);
        }
        return spinner;
    }


    /*Alert dialog для выбора групы товара клиента*/

    /**
     * 27.08.2020
     * Alert dialog для выбора групы товара клиента
     * Когда группа выбрана - открываем фотоаппарат
     */
    private void choiceCustomerGroupAndPhoto() {
        if (wpDataObj.getCustomerTypeGrp() != null) {
            final String[] result = wpDataObj.getCustomerTypeGrp().values().toArray(new String[0]);
            if (wpDataObj.getCustomerTypeGrp().size() > 1) {
                new AlertDialog.Builder(this)
                        .setTitle("Выберите группу товара для следующего фото: ")
                        .setItems(result, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast t = Toast.makeText(PhotoReportActivity.this, "Вибрано групу товару: " + result[which], Toast.LENGTH_LONG);
                                t.show();
                                wpDataObj.setCustomerTypeGrpS(globals.getKeyForValue(result[which], wpDataObj.getCustomerTypeGrp()));
                                //customerTypeGrp = globals.getKeyForValue(result[which], wpDataObj.getCustomerTypeGrp());
                                takePhoto();
                            }
                        })
                        .show();
            } else if (wpDataObj.getCustomerTypeGrp().size() == 1) {
                wpDataObj.setCustomerTypeGrpS(globals.getKeyForValue(result[0], wpDataObj.getCustomerTypeGrp()));
                //customerTypeGrp = globals.getKeyForValue(result[0], wpDataObj.getCustomerTypeGrp());
                Toast.makeText(this, "Вибрано групу товару: " + result[0], Toast.LENGTH_LONG).show();
                takePhoto();
            } else {
                globals.alertDialogMsg(this, "Не обнаружено ни одной группы товаров по данному клиенту. Сообщите об этом Администратору!");
                wpDataObj.setCustomerTypeGrpS("");
                //customerTypeGrp = "";
                takePhoto();
            }
        } else {
            globals.alertDialogMsg(this, "Не выбрано посещение\n\nЗайдите в раздел План работ, выберите посещение и повторите попытку.");
        }
    }


    // Выполнить проверку включённости GPS, МП и запустить фотоаппарат для фотографирования
    private void takePhoto() {
        try {
            if (ua.com.merchik.merchik.trecker.enabledGPS) {
                if (wpDataObj != null) {
                    if (wpDataObj.getLatitude() > 0 && wpDataObj.getLongitude() > 0) {
//                    if (true){
                        if (Globals.CoordX > 0 && Globals.CoordY > 0) {
                            double d = ua.com.merchik.merchik.trecker.coordinatesDistanse(wpDataObj.getLatitude(), wpDataObj.getLongitude(), Globals.CoordX, Globals.CoordY);
                            if (d > 500) {
                                String title = "Порушення за місцезнаходженням.";
                                String msg = String.format("По данным системы вы находитесь на расстоянии %.1f метров от ТТ %s, что больше допустимых 500 метров.\n\nВы не сможете использовать фото которые выполните в таком состоянии системы.\n\nЕсли в действительности Вы находитесь в ТТ - обратитесь к своему руководителю за помощью.", d, wpDataObj.getAddressIdTxt());
                                String trueButton = "<font color='#000000'>Все одно зробити фото</font>";
                                String falseButton = "<font color='#000000'>Пропустити створення фото</font>";
                                String title2 = "ВНИМАНИЕ!";
                                String msg2 = "Система не обнаружила вас в ТТ. \n\nФотографии выполненные в этом режиме могут быть признаны не действительными.\n\nОтказаться от изготовления фото?";
                                String trueButton2 = "<font color='#000000'>Да</font>";
                                String falseButton2 = "<font color='#000000'>Нет</font>";

                                alertMassageMP(1, title, msg, trueButton, falseButton, title2, msg2, trueButton2, falseButton2);
                            } else if (serverTimeControl()) {
                                String timeStamp = new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis());
                                String timeStamp2 = new SimpleDateFormat("HH:mm:ss").format(RetrofitBuilder.getServerTime());
                                String timeDifference = "" + (Globals.serverGetTime - RetrofitBuilder.getServerTime()) / 1000;

                                String t1 = "Ошибка синхронизации времени.";
                                String m1 = String.format("Время установленное на: \n" +
                                        "Вашем телефоне: %s \n" +
                                        "Нашем сервере:\t\t %s \n\n" +
                                        "Разница во времени больше %s секунд\n\n" +
                                        "Установите на своём телефоне время аналогичное с сервером и повторите попытку.", timeStamp, timeStamp2, timeDifference);
                                String bt1 = "<font color='#000000'>Все одно зробити фото</font>";
                                String bf1 = "<font color='#000000'>Пропустити створення фото</font>";
                                String t2 = "ВНИМАНИЕ!";
                                String m2 = "Фотографии выполненные в этом режиме могут быть признаны не действительными.\n\nОтказаться от изготовления фото?";
                                String bt2 = "<font color='#000000'>Да</font>";
                                String bf2 = "<font color='#000000'>Нет</font>";

                                alertMassageMP(1, t1, m1, bt1, bf1, t2, m2, bt2, bf2);
                            } else {
                                dispatchTakePictureIntent(); // Метод который запускает камеру и создаёт файл фото.
                            }
                        } else {
                            String t1 = "Координаты не определены";
                            String m1 = "GPS на Вашем телефоне включен, но по какой-то причине не смог определить Ваши координаты.\n" +
                                    "1. Выйдите на улицу\n" +
                                    "2. Перезагрузите (Выключите/Включите) GPS\n" +
                                    "3. В меню приложения нажмите на \"Перейти на главную\"\n" +
                                    "4. Повторите попытку \n" +
                                    "\n" +
                                    "Если ошибка повторится - обратитесь за помощью к Вашему руководителю.";
                            String bt1 = "";
                            String bf1 = "Ок" +
                                    "" +
                                    "";

                            alertMassageMP(2, t1, m1, bt1, bf1, "", "", "", "");
                        }
                    }
                } else {
                    Toast.makeText(this, "Не обнаружены данные посещения, обратитесь к Вашему руководителю.", Toast.LENGTH_SHORT).show();
                }
            } else {
                String title = "Выключеный GPS";
                String msg = "Не могу определить Ваше местоположение, возможно выключен GPS.\n\n" +
                        "Вы не сможете использовать фото которые выполните в таком состоянии системы.\n\n" +
                        "Если в действительности у Вас включён GPS - обратитесь к своему руководителю за помощью.";
                String trueButton = "<font color='#000000'>У меня всё работает</font>";
                String falseButton = "<font color='#000000'>Закрыть сообщение</font>";
                String title2 = "ВНИМАНИЕ!";
                String msg2 = "Система не обнаружила GPS. \n\n" +
                        "Фотографии выполненные в этом режиме могут быть признаны не действительными.\n\n" +
                        "Отказаться от изготовления фото?";
                String trueButton2 = "<font color='#000000'>Да</font>";
                String falseButton2 = "<font color='#000000'>Нет</font>";

                alertMassageMP(1, title, msg, trueButton, falseButton, title2, msg2, trueButton2, falseButton2);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при проверке состояния GPS. Повторите попытку или обратитесь к Вашему руководителю. Ошибка: " + e, Toast.LENGTH_LONG).show();
        }

    }


    /**
     * 27.08.2020
     * Проверка серверного времени с временем на телефоне
     */
    public static boolean serverTimeControl() {
        long currentTime = System.currentTimeMillis();
        if (RetrofitBuilder.getServerTime() != 0) {
            if (currentTime - Globals.serverGetTime <= 3600) {
                return (Globals.serverGetTime - RetrofitBuilder.getServerTime()) / 1000 > 20;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    /**
     * Схема перевопроса
     *
     * @param title       Заголовок сообщения
     * @param msg         Сообщение для основного окна
     * @param trueButton  Текст позитивной кнопки
     * @param falseButton Текст негативной кнопки
     */
    private void alertMassageMP(int mod, String title, String msg, String trueButton, String falseButton, String title2, String msg2, String trueButton2, String falseButton2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setMessage(msg);
        if (mod == 1) {
            builder.setPositiveButton(Html.fromHtml(trueButton), (dialog, which) -> alertMassageMPPhoto(title2, msg2, trueButton2, falseButton2));
        }
        builder.setNegativeButton(Html.fromHtml(falseButton), (dialog, which) -> {
        });
        builder.create().show();
    }


    /**
     * @param title2       Заголовок сообщения
     * @param msg2         Сообщение для основного окна
     * @param trueButton2  Текст позитивной кнопки
     * @param falseButton2 Текст негативной кнопки
     */
    private void alertMassageMPPhoto(String title2, String msg2, String trueButton2, String falseButton2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle(title2);
        builder.setCancelable(false);
        builder.setMessage(msg2);
        builder.setPositiveButton(Html.fromHtml(trueButton2), (dialog, which) -> {
        });
        builder.setNegativeButton(Html.fromHtml(falseButton2), (dialog, which) -> {
            dispatchTakePictureIntent(); // Метод который запускает камеру и создаёт файл фото.
        });
        builder.create().show();
    }


    /**
     * 27.08.2020
     * Создание фото и пути к ней
     */
    public void dispatchTakePictureIntent() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPG_" + timeStamp + "_";

                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);  // Для прилы
                try {
                    image = File.createTempFile(
                            imageFileName,
                            ".jpg",
                            storageDir
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Uri contentUri;
//                if (Build.VERSION_CODES.N < android.os.Build.VERSION.SDK_INT) {
//                    contentUri = FileProvider.getUriForFile(this, "ua.com.merchik.merchik.provider", image);
//                } else {
//                    contentUri = Uri.fromFile(image);
//                }
                try {
                    contentUri = FileProvider.getUriForFile(this, "ua.com.merchik.merchik.provider", image);
                    globals.writeToMLOG(" MakePhoto.class.Type1.Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT + "\n");
                } catch (Exception e) {
                    contentUri = Uri.fromFile(image);
                    globals.writeToMLOG(" MakePhoto.class.Type2.Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT + "\n");
                }

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }

        } catch (Exception e) {
            globals.alertDialogMsg(this, "Ошибка при создании фото: " + e);
        }
    }


    // Размещение фотки по URI адрессу для пользователя, отображение фотографии загрузки
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        try {
            // Если отменили сьемку:
            if (resultCode == Activity.RESULT_CANCELED && requestCode == 1) image.delete();

            // Если сьемка успешная:
            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

                if (image != null && image.exists()) {
                    if (image.length() > 0) {       //

                        final int rotation = getImageOrientation(image.getPath()); //Проверка на сколько градусов повёрнуто изображение
                        if (rotation > 0) {
                            image = resaveBitmap(image, rotation);  // ДляСамсунгов и тп.. Разворачиваем как надо.
                        } else {
                            // photoToDB = image;  // Для нормальных телефонов - оставляем как есть
                        }


                        tvPhotoReportTextViewCommentValue.setText(null);
                        comment = "";  // Обнуление коментария для новой фото
                        bPhotoReportButtonAddComent.setText("Добавить комментарий"); // "Возвращение" к исходной кнопки "Добавить комментарий"

                        bPhotoReportButtonMakePhoto.setVisibility(View.INVISIBLE); // Скрытие кнопки "Сделать фото"
                        bPhotoReportButtonAddComent.setVisibility(View.VISIBLE);    // Отображение кнопки "Добавить комментарий"
                        bPhotoReportButtonMakeMorePhoto.setVisibility(View.VISIBLE);    // Отображение кнопки "Сделать ещё фото"
                        bPhotoReportButtonSaveAndClose.setVisibility(View.VISIBLE); // Отображение кнопки "Сохранить и Закрыть"

                        Bitmap b = decodeSampledBitmapFromResource(image, 500, 500);
                        if (b != null) {
                            ivPhotoReportImageViewPhotoPreview.setImageBitmap(b);
                        } else {
                            globals.alertDialogMsg(this, "Фото ужать не получилось");
                        }


                        Toast.makeText(this, "Фото сделано, не забудьте его сохранить!", Toast.LENGTH_LONG).show();
                    } else { // Если фото получилось нулевым - файлик удаляется
                        deleteFile(image);
                    }
                } else {
                    globals.alertDialogMsg(this, "Фото не было создано, повторите попытку");
                }
            }
        } catch (Exception e) {
            globals.alertDialogMsg(this, "Ошибка при выполнении фото: " + e);
        }
    }


    /**
     * 27.08.2020
     * Удаление файла (фото)
     */
    private void deleteFile(File img) {
        img.delete();
        if (img.exists()) {
            try {
                img.getCanonicalFile().delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (img.exists()) {
                getApplicationContext().deleteFile(img.getName());
            }
        }
    }


    /**
     * 27.08.2020
     * Получает ориентацию фотографии.
     */
    public static int getImageOrientation(String imagePath) {
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }


    /** 27.08.2020
     *  Перезаписывает повернутое фото в новый файл
     * */
    /***/
    public static File resaveBitmap(File img, int rotation) { //help for fix landscape photos
        OutputStream outStream = null;
        File file = new File(img.toURI());
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            bitmap = checkRotationFromCamera(bitmap, file.getPath(), rotation);
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) ((float) bitmap.getWidth() * 0.3f), (int) ((float) bitmap.getHeight() * 0.3f), false);
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }


    /**
     * Поворачивает фото в 0:0
     */
    private static Bitmap checkRotationFromCamera(Bitmap bitmap, String pathToFile, int rotate) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }


    // ПЕРЕПИСАТЬ
    // Создание уменьшеного файла
    public static File resizeImageFile(Context context, File image) {
        File f = null;
        Bitmap res = null;
        int origWidth, origHeight;
        Bitmap readyToDecode;

        readyToDecode = decodeSampledBitmapFromResource(image, 1500, 1500);

        try {
            origWidth = readyToDecode.getWidth();
            origHeight = readyToDecode.getHeight();
        } catch (NullPointerException e) {
//            globals.alertDialogMsg("Фото ужать не вышло. Обратитесь к руководителю. (Ошибка №1)", this);
            return image;
        }

        final int destWidth = 1500;//width you need

        if (origWidth > destWidth) {
            int destHeight = origHeight / (origWidth / destWidth);
            Bitmap b2 = Bitmap.createScaledBitmap(readyToDecode, destWidth, destHeight, false);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            b2.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
            res = b2;

            try {
                f = createImageFile(context);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //write the bytes in file
            FileOutputStream fo = null;
            try {
                fo = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                if (fo != null) {
                    fo.write(outStream.toByteArray());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // remember close de FileOutput
            try {
                if (fo != null) {
                    fo.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //Globals.alertDialogMsg("Фото ужать не вышло. Оно слишком маленькое. (Ошибка №0)", this);
            return image;
        }

        image.delete(); //Удаление полноразмерной фотографии
        return f;
    }

    // ПЕРЕПИСАТЬ
    // Получение информации о фотографии
    public static File exifPhotoData(File file) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif != null) {
            exif.setAttribute(ExifInterface.TAG_DATETIME, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));


//            exif.setAttribute(ExifInterface.TAG_DATETIME, new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").format(new Date()));
        }

        try {
            if (exif != null) {
                exif.saveAttributes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    // ПЕРЕПИСАТЬ
    // Получение даты создания фото
    public static String getExifCreateData(File file) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif != null) {
            return exif.getAttribute(ExifInterface.TAG_DATETIME);
        } else {
            return "null";
        }
    }

    // ПЕРЕПИСАТЬ
    // Создание файла для фотографии
    private static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);  // Для прилы
        image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    /**
     * Метод с документации.
     * Расчёт оптимального размера файла фотографии для загрузки в память
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    /**
     * РАЗОБРАТЬСЯ
     * Метод с документации.
     * Декодирует обьект(фото) что б не вылетала ошибка OutOfMemoryError
     * <p>
     * зачем и на что влияет resId
     * разобраться с методами: BitmapFactory.decodeResource, мне вроде нужен был декодирование файла.
     */
    private static Bitmap decodeSampledBitmapFromResource(File res, int reqWidth, int reqHeight) {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(res.getAbsolutePath(), options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(res.getAbsolutePath(), options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 27.08.2020
     * Вызов диалогового окна для комментария
     */
    private void addComment() {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_comment, null);

        final EditText editTextComment = (EditText) dialogView.findViewById(R.id.edt_comment);
        Button button1 = (Button) dialogView.findViewById(R.id.buttonSubmit);

        if (!comment.equals("")) {
            editTextComment.append(comment);
        }

        button1.setOnClickListener(view -> {
            comment = String.valueOf(editTextComment.getText());
            tvPhotoReportTextViewCommentValue.setText(comment);

            if (tvPhotoReportTextViewCommentValue != null) {
                bPhotoReportButtonAddComent.setText("Изменить комментарий");
            }
            dialogBuilder.dismiss();
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }


    /**
     * Сохраняет данные о фото и само фото в БД
     *
     * @return true    - если данные сохранить удалось
     * false   - есил произошла какая-то ошибка
     */
    public boolean savePhotoToDB() {
        try {
            int coord = trecker.Coordinates();// Получение кординат и типа данных

            // Вывод сообщения что координаты получить не вышло
            if (coord == 0) {
                globals.alertDialogMsg(this, "Определить координаты вашего местоположения не удалось. Фотоотчёт будет отправлен на сервер без координат. И может быть не принят. За помощью обратитесь к своему руководителю.");
            }

            // Предупреждение: Как давно получены данные GPS, если они устарели.
            if (coord == 6) {
                long sec = System.currentTimeMillis() - globals.CoordTime;
                sec = (sec / 1000 - 1800) / 60;
                if (sec < 0) {
                    globals.alertDialogMsg(this, "Данные о местоположении устарели. Они получены " + sec + " минут назад. Обновите эти данные и продолжите работу.\n\n" +
                            "Что б обновить актуальность данных МП следует:\n" +
                            "1. Иметь включённый GPS\n" +
                            "2. Выйти с ТТ на улицу или подойти к окну с включённым приложением\n" +
                            "3. Подождать минуту пока данные не обновятся \n\n" +
                            "Если Вам что-то не понятно - обратитесь к своему руководителю.");
                    Toast toast = Toast.makeText(this, "Данные о местоположении устарели. Они получены " + sec + " минут назад. Обновите эти данные и продолжите работу.\n\nЕсли Вам что-то не понятно - обратитесь к своему руководителю.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }

            // Предупреждение: Как далеко от ТТ.
            if (wpDataObj.getLatitude() > 0 && wpDataObj.getLongitude() > 0) {
                double d = trecker.coordinatesDistanse(wpDataObj.getLatitude(), wpDataObj.getLongitude(), globals.CoordX, globals.CoordY);
//                double d = trecker.coordinatesDistanse(lat, lon, CoordX, CoordY);
                if (d > 500) {
                    globals.alertDialogMsg(this, "По данным системы вы находитесь на расстоянии " + String.format("%.1f", d) + "MEASURE" + " метров от ТТ " + wpDataObj.getAddressIdTxt() + ", что больше допустимых 500 метров.\n\nЕсли в действительности Вы находитесь на ТТ - проверьте актуальность гео-данных в меню светофора \"Provider\". Если данные получены недавно и актуальны - обратитесь к своему руководителю за помощью.");
                }
            }

            // mod 1 = "Устаревший" вариант для сбора информации об устройстве и координатах
            String GP = POST_10(this, 1);// Запись пост данных в переменную для БД для последущей отправки на сервер
//            String GP = POST_10_JSON(this);// Запись пост данных в переменную для БД для последущей отправки на сервер


            if (RealmManager.chechPhotoExist(image.getAbsolutePath())) {
                //if (!SQL.checkIsDataAlreadyInDBorNot("stack_photo", "photo_num", photoToDB.getAbsolutePath(), 1)) {   // Проверка на наличие фото в таблице

                File file = null;

                try {
                    file = resizeImageFile(this, image);
                } catch (Exception e) {
                    globals.alertDialogMsg(this, "Ошибка В ужатии " + e);
                }

                if (file == null) {
                    globals.alertDialogMsg(this, "Не удалось ужать фото - оно будет сохранено и отправлено в полном объеме. Сообщите об этом своему руководителю.\n(Это влияет на скорость отправки фото и трафик)");
                    file = image;
                }

                /*Сохранение даты фотки*/
                file = exifPhotoData(file);

                System.out.println("PHOTOCHKA: " + getExifCreateData(file));

                String hash = "";
                String path = file.getAbsolutePath();

                try {
                    hash = globals.getHashMD5FromFilePath(path, this);
                } catch (Exception e) {
                    globals.alertDialogMsg(this, "Ошибка в расчёте hash(тип 1) фото: " + e);
                }

                if (hash == null || hash.equals("")) {
                    try {
                        hash = globals.getHashMD5FromFile(file, this);
                    } catch (Exception e) {
                        globals.alertDialogMsg(this, "Ошибка в расчёте hash(тип 2) фото: " + e);
                    }
                }

                try {
                    int id = RealmManager.stackPhotoGetLastId();
                    id++;

                    Integer photoType = Integer.valueOf(wpDataObj.getPhotoType());

                    String userNmText = "";
                    String customerNmText = "";
                    String addressNmText = "";


                    try {
                        if (RealmManager.getUsersNm(user_id) != null)
                            userNmText = RealmManager.getUsersNm(user_id);

                        if (RealmManager.getCustomerNm(wpDataObj.getCustomerId()) != null)
                            customerNmText = RealmManager.getCustomerNm(wpDataObj.getCustomerId());

                        if (RealmManager.getAddressNm(wpDataObj.getAddressId()) != null)
                            addressNmText = RealmManager.getAddressNm(wpDataObj.getAddressId());
                    } catch (Exception e) {
                        // Ошибка NPE при получении имени пользователя с БД
                        globals.alertDialogMsg(this, "Фото сохранено, но возникли некоторые проблемы: " + e);
                    }

                    String iza = Globals.generateIzaCode(
                            userNmText,
                            wpDataObj.getCustomerId(),
                            wpDataObj.getAddressId()
                    );

                    StackPhotoDB stackPhotoDB = new StackPhotoDB(
                            id,
                            "",
                            null,
                            user_id,
                            wpDataObj.getAddressId(),
                            wpDataObj.getCustomerId(),
                            wpDataObj.getThemeId(),
                            wpDataObj.getDate(),
                            System.currentTimeMillis(),
                            System.currentTimeMillis(),
                            0,
                            0,
                            wpDataObj.getDad2(),
                            path,
                            hash,
                            photoType,
                            wpDataObj.getPhotoUserId(),
                            wpDataObj.getCustomerTypeGrpS(),
                            wpDataObj.getDocNum(),
                            comment,
                            GP,
                            0,
                            0,
                            false,
                            userNmText,
                            customerNmText,
                            addressNmText);

                    stackPhotoDB.setCode_iza(iza);

                    // Проверка - есть ли что-то NULL для сохранения в БД
                    RealmManager.stackPhotoSavePhoto(stackPhotoDB);
                    try {
                        Log.e("TAG_REALM_LOG", "ЗАПИСЬ 3");
//                        RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Успешное сохранение фото в БД", 1087, null, null, null, user_id, null, Globals.session, null)));
                    } catch (Exception e) {
                        Log.e("TAG_REALM_LOG", "Ошибка(3): " + e);
                    }
                    return true;
                } catch (Exception e) {
                    Globals.alertDialogMsg(this,
                            DialogStatus.ERROR,
                            "Виникла помилка",
                            "Ошибка сохранения в БД: " + e);
                }

            } else {
                Globals.alertDialogMsg(this,
                        DialogStatus.ALERT,
                        "Увага",
                        "Такое фото уже существует. Если ошибка повторяется - обратитесь к Вашему руководителю");
                return false;
            }

        } catch (Exception e) {
            Globals.alertDialogMsg(this,
                    DialogStatus.ERROR,
                    "Виникла помилка",
                    "Ошибка при сохранении фото. При возникновении этой ошибки - обратитесь к руководителю. Код ошибки: " + e);
        }
        return false;
    }


    /**
     * Сохраняет данные о фото и само фото в БД
     *
     * @return true    - если данные сохранить удалось
     * false   - есил произошла какая-то ошибка
     */
    public static boolean savePhoto(Context context, WPDataObj wpDataObj, File image, Clicks.clickVoid clickVoid) {

        Globals globals = new Globals();
        try {
            Globals.writeToMLOG("INFO", "savePhoto", "wpDataObj 1: " + wpDataObj);
            Globals.writeToMLOG("INFO", "savePhoto", "wpDataObj 2: " + new Gson().toJson(wpDataObj));
            WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(wpDataObj.dad2);

            Globals.writeToMLOG("INFO", "savePhoto", "wp 1: " + wp);
            Globals.writeToMLOG("INFO", "savePhoto", "wp 2: " + new Gson().toJson(wp));
            LogMPDB log = Globals.fixMP(wp, context);

            Gson gson = new Gson();

            try {
                // Преобразуем log в JsonObject
                JsonElement jsonElement = gson.toJsonTree(log);
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    // Удаляем поле "gp", если есть
                    jsonObject.remove("gp");
                    // Преобразуем обратно в JSON-строку
                    String json = gson.toJson(jsonObject);
                    Log.d("JSON", json);
                    Globals.writeToMLOG("INFO", "savePhoto", "log 2: " + json);

                } else {
                    // Если log — не объект, просто сериализуем
                    String json = gson.toJson(log);
                    Log.d("JSON", json);
                    Globals.writeToMLOG("INFO", "savePhoto", "log 2: " + json);
                }
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "savePhoto", "log 2 Exception: " + e.getMessage());

            }

            String GP = log != null ? log.gp : "";

//            String GP = Objects.requireNonNull(Globals.fixMP(WpDataRealm.getWpDataRowByDad2Id(wpDataObj.dad2))).gp;

            if (RealmManager.chechPhotoExist(image.getAbsolutePath())) {
                File file = null;

                try {
                    file = resizeImageFile(context, image);
                } catch (Exception e) {
                    globals.alertDialogMsg(context, "Ошибка В ужатии " + e);
                }

                if (file == null) {
                    globals.alertDialogMsg(context, "Не удалось ужать фото - оно будет сохранено и отправлено в полном объеме. Сообщите об этом своему руководителю.\n(Это влияет на скорость отправки фото и трафик)");
                    file = image;
                }

                /*Сохранение даты фотки*/
//                file = exifPhotoData(file);

//                System.out.println("PHOTOCHKA: " + getExifCreateData(file));
//
                String hash = "";
                String path = file.getAbsolutePath();

//                try {
//                    hash = globals.getHashMD5FromFilePath(path, context);
//                } catch (Exception e) {
//                    globals.alertDialogMsg(context, "Ошибка в расчёте hash(тип 1) фото: " + e);
//                }

//                if (hash == null || hash.equals("")) {
//                    try {
//                        hash = globals.getHashMD5FromFile(file, context);
//                    } catch (Exception e) {
//                        globals.alertDialogMsg(context, "Ошибка в расчёте hash(тип 2) фото: " + e);
//                    }
//                }

                try {
                    int id = RealmManager.stackPhotoGetLastId();
                    id++;

                    Globals.writeToMLOG("INFO", "savePhoto", "new photo id: " + id);

                    Integer photoType = Integer.valueOf(wpDataObj.getPhotoType());

                    String userNmText = "";
                    String customerNmText = "";
                    String addressNmText = "";

                    try {
                        if (RealmManager.getUsersNm(Integer.valueOf(wpDataObj.getPhotoUserId())) != null)
                            userNmText = RealmManager.getUsersNm(Integer.valueOf(wpDataObj.getPhotoUserId()));

                        if (RealmManager.getCustomerNm(wpDataObj.getCustomerId()) != null)
                            customerNmText = RealmManager.getCustomerNm(wpDataObj.getCustomerId());

                        if (RealmManager.getAddressNm(wpDataObj.getAddressId()) != null)
                            addressNmText = RealmManager.getAddressNm(wpDataObj.getAddressId());
                    } catch (Exception e) {
                        // Ошибка NPE при получении имени пользователя с БД
                        Globals.writeToMLOG("ERROR", "savePhoto", "Ошибка NPE при получении имени пользователя с БД Exception e: " + e);
                    }

                    StackPhotoDB stackPhotoDB = new StackPhotoDB(
                            id,
                            "",
                            null,
                            Integer.valueOf(wpDataObj.getPhotoUserId()),
                            wpDataObj.getAddressId(),
                            wpDataObj.getCustomerId(),
                            wpDataObj.getThemeId(),
                            wpDataObj.getDate(),
                            System.currentTimeMillis(),
                            System.currentTimeMillis(),
                            0,
                            0,
                            wpDataObj.getDad2(),
                            path,
                            hash,
                            photoType,
                            wpDataObj.getPhotoUserId(),
                            wpDataObj.getCustomerTypeGrpS(),
                            wpDataObj.getDocNum(),
                            comment,
                            GP,
                            0,
                            0,
                            false,
                            userNmText,
                            customerNmText,
                            addressNmText);

                    stackPhotoDB.setPhoto_type(Integer.valueOf(MakePhoto.photoType));
                    stackPhotoDB.img_src_id = MakePhoto.img_src_id;
                    stackPhotoDB.showcase_id = MakePhoto.showcase_id;
                    stackPhotoDB.planogram_id = MakePhoto.planogram_id;
                    stackPhotoDB.planogram_img_id = MakePhoto.planogram_img_id;
                    stackPhotoDB.example_id = MakePhoto.example_id;
                    stackPhotoDB.example_img_id = MakePhoto.example_img_id;
                    stackPhotoDB.setDvi(0); // #### уточнить

                    stackPhotoDB.setCode_iza(wp.getCode_iza());   // Вмазал код ИЗА что б не крашился тип 31 фото в своей опции контроля (и всегда был с фоткой)

                    if (MakePhoto.photoType.equals("4") || MakePhoto.photoType.equals("39")) {
                        stackPhotoDB.tovar_id = MakePhoto.tovarId;
                        Globals.writeToMLOG("INFO", "requestCode == 201 && resultCode == RESULT_OK/photo_save", "MakePhoto.tovarId: " + MakePhoto.tovarId);
                    }

                    Globals.writeToMLOG("INFO", "savePhoto", "stackPhotoDB_1: " + stackPhotoDB);

                    // Проверка - есть ли что-то NULL для сохранения в БД
                    RealmManager.stackPhotoSavePhoto(stackPhotoDB); // Сохранение фотографии в БД

                    clickVoid.click();

                    Globals.writeToMLOG("INFO", "savePhoto", "stackPhotoDB_2: " + stackPhotoDB);
                    return true;
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "savePhoto", "Ошибка сохранения в БД: " + e);
                    globals.alertDialogMsg(context, "Ошибка сохранения в БД: " + e);
                }

            } else {
                Globals.writeToMLOG("ERROR", "savePhoto", "Такое фото уже существует. Если ошибка повторяется - обратитесь к Вашему руководителю");
                globals.alertDialogMsg(context, "Такое фото уже существует. Если ошибка повторяется - обратитесь к Вашему руководителю");
                return false;
            }

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "savePhoto", "Exception e: " + e);
            globals.alertDialogMsg(context, "Ошибка при сохранении фото. При возникновении этой ошибки - обратитесь к руководителю. Код ошибки: " + e);
        }
        return false;
    }


    // ЭТО *** НАДО МЕНЯТЬ!!!!!!!!!!!!!!!

    /**
     * POST.. для 10 минутной отправки на сервер данных
     * Формирование данных для отправки на сервер
     * <p>
     * return - POST String in base64
     */
    public static String POST_10(Context context, int mod) {

        if (mod == 1) {
            //Coordinates();

            String M_to_URL; //
            String sURL;
            String base64 = null; // Строка для гранения закодированного пакета переменных на сервер
            int width = 0;
            int height = 0;


            Map<String, Object> DataMap = new HashMap<String, Object>();                                // Общая hashmap. Второе значение Obj, потому что вторым параметром может быть hashmap.
            Map<String, Object> battery = new HashMap<String, Object>();
            Map<String, Object> connection_info = new HashMap<String, Object>();
            Map<String, Object> browser_info = new HashMap<String, Object>();
            Map<String, Object> screen_info = new HashMap<String, Object>();
            Map<String, Object> coords = new HashMap<String, Object>();

            battery.put("battery_level", getBatteryPercentage(context));                           // Уровень зар¤да батареи в процентах или -1 если данных нет
            DataMap.put("battery", battery);

            DataMap.put("device_time", Globals.CoordTime / 1000);            // unixtime момента получения данных геокоординат

            connection_info.put("downlink", "");                                                  // скорость соединени¤ в мегабитах
            connection_info.put("downlinkMax", "");
            connection_info.put("effectiveType", "");                                                  // тип сети
            connection_info.put("rtt", "");                                                  // задержка
            connection_info.put("type", "");
            DataMap.put("connection_info", connection_info);                                            // информаци¤ о подключении к сети

            browser_info.put("hardwareConcurrency", Runtime.getRuntime().availableProcessors());        // количество ¤дер
            browser_info.put("maxTouchPoints", "");                                                // количество одновременно обрабатываемых точек касания
            browser_info.put("platform", Build.VERSION.SDK_INT);                             // платформа
//            browser_info.put("version_app", currentVersion);
            browser_info.put("version_app", "");
            DataMap.put("browser_info", browser_info);                                      // информаци¤ о браузере и железе

            screen_info.put("availHeight", "");
            screen_info.put("availWidth", "");
            screen_info.put("height", height);
            screen_info.put("width", width);
            screen_info.put("keepAwake", "");
            screen_info.put("orientation_angle", "");
            screen_info.put("orientation_type", "");
            DataMap.put("screen_info", screen_info);                                                    // информаци¤ об экране

            coords.put("latitude", Globals.CoordX);
            coords.put("longitude", Globals.CoordY);
            coords.put("altitude", Globals.CoordAltitude);                                             // высота над уровнем моря
            coords.put("accuracy", Globals.CoordAccuracy);                                             // точность обязательно
            coords.put("altitudeAccuracy", "");
            coords.put("heading", "");
            coords.put("speed", Globals.CoordSpeed);
            coords.put("trusted_location", Globals.mocking);
            DataMap.put("coords", coords);                                                              // географические координаты

            DataMap.put("timestamp", System.currentTimeMillis()); // unixtime текущего времени, когда был отправлен запрос с данными с точностью до тысячных (если не сможешь настолько точное врем¤ получить, бери текущий unixtime и умножай на 1000)

            M_to_URL = URL.httpBuildQuery(DataMap, "UTF-8");

            try {
                sURL = URLEncoder.encode(M_to_URL, "UTF-8"); //Кодирование URLData в конечную.
                base64 = Base64.encodeToString(sURL.getBytes(), 0);  //Кодирование конечной URLData в base64 для качества передачи.
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return base64;
        } else {   //--- mod 2

            return "mod2";
        }
    }


    public static String POST_10_JSON(Context context) {
        String res = "";

        PhotoGP GP = new PhotoGP();

        Battery battery = new Battery();
        battery.batteryLevel = "test1";
        GP.battery = battery;


        BrowserInfo browserInfo = new BrowserInfo();
        browserInfo.hardwareConcurrency = "test2";
        browserInfo.maxTouchPoints = "test3";
        browserInfo.platform = "test4";
        browserInfo.versionApp = "test5";
        GP.browserInfo = browserInfo;


        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.downlink = "test6";
        connectionInfo.downlinkMax = "test7";
        connectionInfo.effectiveType = "test8";
        connectionInfo.rtt = "test9";
        connectionInfo.type = "test";
        GP.connectionInfo = connectionInfo;


        Coords coords = new Coords();
        coords.latitude = "2347238416238468";
        coords.longitude = "3875617561756865";
        coords.accuracy = "test";
        coords.altitude = "test";
        coords.altitudeAccuracy = "test";
        coords.heading = "test";
        coords.speed = "test";
        coords.trustedLocation = "test";
        GP.coords = coords;


        ScreenInfo screenInfo = new ScreenInfo();
        screenInfo.availHeight = "3875617561756865";
        screenInfo.availWidth = "3875617561756865";
        screenInfo.height = "3875617561756865";
        screenInfo.keepAwake = "3875617561756865";
        screenInfo.orientationAngle = "3875617561756865";
        screenInfo.orientationType = "3875617561756865";
        screenInfo.width = "3875617561756865";
        GP.screenInfo = screenInfo;


        GP.deviceTime = "111111111111";
        GP.timestamp = "222222222222";

        res = new Gson().fromJson(new Gson().toJson(GP), JsonObject.class).toString();
        return res;
    }

    // СПИНЕР (Выпадающий список типов фото в Активости ФОТООТЧЁТА)
    private class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            try {
                String photoType = parent.getSelectedItem().toString();
                wpDataObj.setPhotoType(Globals.getKeyForValue(photoType, mapSpinner));
            } catch (Exception e) {
                //
            }
        }

        public void onNothingSelected(AdapterView parent) {

        }
    }
}


