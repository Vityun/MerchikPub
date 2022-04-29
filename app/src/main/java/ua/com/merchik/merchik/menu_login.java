package ua.com.merchik.merchik;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Activities.WorkPlanActivity.WPDataActivity;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.SiteObjectsExchange;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RetrofitResponse.Login;
import ua.com.merchik.merchik.data.RetrofitResponse.Logout;
import ua.com.merchik.merchik.data.ServerLogin.LoginSearch;
import ua.com.merchik.merchik.data.ServerLogin.LoginSearchList;
import ua.com.merchik.merchik.data.ServerLogin.SessionCheck;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.data.Translation.LangListDB;
import ua.com.merchik.merchik.data.Translation.SiteLanguages;
import ua.com.merchik.merchik.data.Translation.SiteTranslations;
import ua.com.merchik.merchik.data.Translation.SiteTranslationsList;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogLoginHelp;
import ua.com.merchik.merchik.dialogs.DialogRetingOperatorSuppr;
import ua.com.merchik.merchik.dialogs.DialogSupport;
import ua.com.merchik.merchik.dialogs.DialogTelephoneRegistration;
import ua.com.merchik.merchik.retrofit.MyCookieJar;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;


public class menu_login extends AppCompatActivity {

    private final TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading();

    private Progress progress;
    private static final int PERMISSION_FINE_LOCATION = 0;
    private static final int PERMISSION_CAMERA = 1;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private String telephoneLogin = "";


    private String obj891 = "";
    private String obj890 = "";
    private String obj893 = "";
    private String obj894 = "";
    private String obj895 = "";


    //    private ImageButton flag;
    private ImageView flag;

    Globals globals;
    URL URL = new URL();

    Intent intent;

    String uri; // query from website
    int wil; // what i login
    String sess_id;
    String login = null, password = null;
    String user_id;

    private AutoCompleteTextView autoText;


    public static JsonObject jsonMVS;


    private TextView textViewVer;


    EditText //editText_login,
            editText_password;

    TextView text1;
    EditText edit1;
    EditText edit2;
    Button but1;
    Button but2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_login);

        flag = findViewById(R.id.flag);
        text1 = findViewById(R.id.text_login);

        edit1 = findViewById(R.id.editText_login);
        edit2 = findViewById(R.id.editText_password);

        but1 = findViewById(R.id.button_login);
        but2 = findViewById(R.id.button_regestration);

        editText_password = findViewById(R.id.editText_password);

        autoText = findViewById(R.id.login);
        autoEditText();

        obj891 = this.getResources().getString(R.string.object_891);
        obj890 = this.getResources().getString(R.string.object_890);
        obj893 = this.getResources().getString(R.string.object_893);
        obj894 = this.getResources().getString(R.string.object_894);
        obj895 = this.getResources().getString(R.string.object_895);

//        MenuMainActivity.test();

        try {
            globals = new Globals();

//            intent = new Intent(menu_login.this, MenuMainActivity.class);
            intent = new Intent(menu_login.this, WPDataActivity.class);
            progress = new Progress(this, "Вход", "Вход в систему", false);


            Log.e("ПОСЛЕДОВАТЕЛЬНОСТЬ", "3_obj893: " + obj893);


            execute();

//            editText_login = findViewById(R.id.editText_login);


            textViewVer = findViewById(R.id.textViewVer);
            SpannableString content = new SpannableString(textViewVer.getText());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            textViewVer.setText(content);


//            toolbar_menus.textLesson = 812;
//            toolbar_menus.videoLesson = 813;
//            toolbar_menus.setFab(this, findViewById(R.id.fab));

            setFab(findViewById(R.id.fab));


            if (checkPermission()) {
                // all right
            } else {
                requestPermission();
            }


        } catch (Exception e) {
            globals.alertDialogMsg(this, "Ошибка при входе(0): " + e);
        }

        try {
            new MyCookieJar();
            String debugStatus = getResources().getString(R.string.debug_status);
            String msg = getResources().getString(R.string.start_app_test);
            String instructions = getResources().getString(R.string.start_app_test_instructions);
            instructions = "Требуется работа в штатном режиме. Прокликать ВСЕ кнопки и попробовать зайти во все меню.";

            // Получение двнных с нажатой ссылки на сайте
            Uri data = this.getIntent().getData();
            Log.e("MVS_JSON", "data: " + data);

            if (data != null && data.isHierarchical()) /*Если данные получены с МВС*/ {

                if (debugStatus.equals("1")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCancelable(false);
                    builder.setMessage(msg + instructions);
                    builder.setPositiveButton("Ок", (dialog, which) -> loginOnServer());
                    builder.create().show();
                } else {
                    loginOnServer();
                }

            } else/*При обычном входе*/ {
                Log.e("MVS_JSON", "Когда данных с МВС нет");

//                intent = new Intent(menu_login.this, MenuMainActivity.class);
                intent = new Intent(menu_login.this, WPDataActivity.class);
                if (debugStatus.equals("1")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCancelable(false);
                    builder.setMessage(msg + instructions);
                    builder.setPositiveButton("Ок", (dialog, which) -> loginOnServer());
                    builder.create().show();
                } else {
                    loginOnServer();
                }
            }

        } catch (Exception e) {
            globals.alertDialogMsg(this, "Ошибка при входе: " + e);
        }


    }//---------------------------------------------------------------------------------------------


    @Override
    protected void onResume() {
        super.onResume();
//        Globals.restartActivity(this);
    }

    /**
     * 05.02.2021
     * Должно запустить все "важные" методы при старте программы
     */
    private void execute() {

        /*Скачивание Языков и Переводов*/
        getTableTranslate(new ExchangeInterface.LanguagesResponce() {
            @Override
            public void onSuccess(List<LangListDB> data, String s) {
                saveLanguages(data);
            }

            @Override
            public void onFailure(List<LangListDB> data, String error) {

            }
        });


        /*15.05.2021
         * Скачивание нормальных Переводов для приложения:
         * */
        new Translate().getTranslates();


        /*Отправка на сервер переводов*/
        //uploadNewTranslate();

        /*Установка переводов в Активность*/
        setLangToActivity();

        // Получение минимальной версии приложения
        checkVersion();

        /*Установка клика на Флаг смены языка*/
        setFlagClick();
    }


    private void setLangToActivity() {
        String lang = PreferenceManager.getDefaultSharedPreferences(this).getString("lang", "UA");
        switch (lang) {
            case "UA":
                setData(lang);
                flag.setImageDrawable(getResources().getDrawable(R.drawable.ua));
                break;

            case "RU":
                setData(lang);
                flag.setImageDrawable(getResources().getDrawable(R.drawable.ru));
                break;

            case "GB":
                setData(lang);
                flag.setImageDrawable(getResources().getDrawable(R.drawable.gb));
                break;

            case "PL":
                setData(lang);
                flag.setImageDrawable(getResources().getDrawable(R.drawable.pl));
                break;
        }
    }


    private void setData(String shortLanguage) {
        try {
            LangListDB langListDB = RealmManager.getLangList(shortLanguage);
            if (langListDB != null) {
                Log.e("setLangToActivity", "setData.langList: " + langListDB.getNm());
                List<SiteTranslationsList> data = RealmManager.getSiteTranslationsList(langListDB.getID());

                for (SiteTranslationsList item : data) {
                    Log.e("setLangToActivity", "setData.item.getID: " + item.getID());
                    Log.e("setLangToActivity", "setData.item.getNm: " + item.getNm());
                    Log.e("setLangToActivity", "setData.item.getLangId: " + item.getLangId());
                    Log.e("setLangToActivity", "setData.item.getTitle: " + item.getTitle());
                    switch (item.getTitle()) {
                        case "объект 869":
                            but2.setText(item.getNm());
                            break;
                        case "объект 868":
                            but1.setText(item.getNm());
                            break;
                        case "объект 887":
                            autoText.setHint(item.getNm());
                            break;
                        case "объект 888":
                            editText_password.setHint(item.getNm());
                            break;
                        case "объект 889":
                            text1.setText(item.getNm());
                            break;


                        case "объект 891": // Подсказка
                            obj891 = item.getNm();
                            break;
                        case "объект 890": // Помощь
                            obj890 = item.getNm();
                            break;
                        case "объект 893": // Текущая версия приложения
                            Log.e("ПОСЛЕДОВАТЕЛЬНОСТЬ", "2_obj893: " + obj893);
                            obj893 = item.getNm();
                            Log.e("ПОСЛЕДОВАТЕЛЬНОСТЬ", "2.1_obj893: " + obj893);

                            break;
                        case "объект 894": // У Вас последняя версия приложения
                            obj894 = item.getNm();
                            break;
                        case "объект 895": // Рекомендуем установить новую версию приложения:
                            obj895 = item.getNm();
                            break;
                    }
                }
            } else {
                // Данных переводов пока нет
            }

        } catch (Exception e) {

        }


    }


    private void checkVersion() {
        VersionApp versionApp = new VersionApp();

        Log.e("getVer", "1vA.VERSION_APP: " + VersionApp.VERSION_APP);
        versionApp.getMinVer(new Globals.getVersionInterface() {
            @Override
            public void onSuccess(Long l) {
                Long currentVer = Long.valueOf(BuildConfig.VERSION_NAME);
                Long minimalVer = VersionApp.VERSION_APP;
                if (currentVer < minimalVer) {
                    textViewVer.setTextColor(Color.RED);
                }
            }

            @Override
            public void onFailure(String s) {
            }
        });
    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result4 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE);

        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED &&
                result2 == PackageManager.PERMISSION_GRANTED &&
                result3 == PackageManager.PERMISSION_GRANTED &&
                result4 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            switch (requestCode) {
                case PERMISSION_REQUEST_CODE:
                    if (grantResults.length > 0) {

                        boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                        boolean readStorage = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                        boolean writeStorage = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                        boolean call = grantResults[4] == PackageManager.PERMISSION_GRANTED;


                        if (locationAccepted && cameraAccepted && readStorage && writeStorage) {
                            if (trecker.switchedOff) {
                                trecker.SetUpLocationListener(this);
                            }

                        } else {
                            if (Build.VERSION.SDK_INT >= 23) {
                                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) &&
                                        shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) &&
                                        shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                                        shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                                        shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)
                                ) {
                                    requestPermissions(new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.CALL_PHONE
                                    }, PERMISSION_REQUEST_CODE);
                                    if (trecker.switchedOff) {
                                        trecker.SetUpLocationListener(this);
                                    }
                                    return;
                                }
                            }
                        }
                    }
                    break;
            }

        } catch (Exception e) {
        }
    }

    //---------------------------------------------------------------------------------------------

    // BUTTON_1 Login in system
    public void LogIn(View view) {
        try {
//            intent = new Intent(menu_login.this, MenuMainActivity.class);
            intent = new Intent(menu_login.this, WPDataActivity.class);
            appLogin();
        } catch (Exception e) {
            globals.alertDialogMsg(this, "(0)Ошибка при входе: " + e);
        }
    }

    // BUTTON_2 Logout
    public void logOut(View view) {
        String mod = "logout";
        retrofit2.Call<Logout> call = RetrofitBuilder.getRetrofitInterface().logoutInfo(mod);
        call.enqueue(new retrofit2.Callback<Logout>() {
            @Override
            public void onResponse(retrofit2.Call<Logout> call, retrofit2.Response<Logout> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getState()) {
                        Toast.makeText(menu_login.this, "Вы разлогинились.", Toast.LENGTH_SHORT).show();
                        Globals.session = null;
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Logout> call, Throwable t) {
                Toast.makeText(menu_login.this, "Разлогиниться не получилось: " + t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // BUTTON_3 REGESTRATION
    // TODO Заменить все текстовки на Обьекты Сайта
    public void regestration(View view) {
        DialogTelephoneRegistration dialog = new DialogTelephoneRegistration(this);
        dialog.setTitle("Какой-то милый заголовок");
        dialog.setText("Тут буду делать красивый интерфейс для того что-б люди могли вносить телефоны и регистрироваться в нашем богоподобном ресурсе.");
        dialog.setTelephone();
        dialog.setButtonOk("Зарегестрироваться", () -> {
            DialogData dialogReg = new DialogData(this);
            dialogReg.setTitle("Регистрация");
            dialogReg.setMerchikIco(this);
            dialogReg.setText("После подтверждения регистрации Вам прийдёт сообщение с паролем для авторизации. Продолжить регистрацию?");
            dialogReg.setClose(dialogReg::dismiss);
            dialogReg.setCancel("Отменить", dialogReg::dismiss);
            dialogReg.setOk("Зарегистрироваться", () -> {
                autoText.setText(dialog.getTelephone());
                dialog.dismiss();

                Toast.makeText(this, "Внесли телефон: " + dialog.getTelephone() , Toast.LENGTH_SHORT).show();

                String telReplace;
                telReplace = dialog.getTelephone().replaceAll("\\(", "");
                telReplace = telReplace.replaceAll("\\)", "");
                telReplace = telReplace.replaceAll("-", "");

                StandartData standartData = new StandartData();
                standartData.mod = "auth";
                standartData.act = "register";
                standartData.login = telReplace;

                Gson gson = new Gson();
                String json = gson.toJson(standartData);
                JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                Log.e("regestration", "convertedObject: " + convertedObject);

                retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        Log.e("regestration", "response: " + response.body());
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                Toast.makeText(menu_login.this, "response.body(): " + response.body(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(menu_login.this, "response.body(): NULL", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(menu_login.this, "response.code: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e("regestration", "response: " + t);    // java.io.EOFException: End of input at line 1 column 1 path $
                        Toast.makeText(menu_login.this, "Проверьте связь: " + t, Toast.LENGTH_SHORT).show();
                    }
                });
            });
            dialogReg.show();
        });
        dialog.close(dialog::dismiss);
        dialog.show();
    }


    // мусор
    private void regestrationDialog() {
        DialogData dialogRegistration = new DialogData(this);

        dialogRegistration.setLesson(this, true, 633);
        dialogRegistration.setVideoLesson(this, true, 630, () -> {
        });

        dialogRegistration.setTitle("Регистрация");
        dialogRegistration.setMerchikIco(this);
        dialogRegistration.setText("Телефон");
        dialogRegistration.setEditTextHint("+38(___)___-____");

        dialogRegistration.setTelephoneEditText("", (data) -> {
            if (data == null || data.equals("")) {
                data = "Внесите корректно номер телефона!";
            } else {
                telephoneLogin = data;

                DialogData dialog = new DialogData(this);
                dialog.setTitle("Регистрация");
                dialog.setMerchikIco(this);
                dialog.setText("После подтверждения регистрации Вам прийдёт сообщение с паролем для авторизации. Продолжить регистрацию?");
                dialog.setClose(dialog::dismiss);
                dialog.setCancel("Отменить", dialog::dismiss);
                dialog.setOk("Зарегистрироваться", () -> {
                    autoText.setText(telephoneLogin);
                    dialogRegistration.dismiss();

                    StandartData standartData = new StandartData();
                    standartData.mod = "auth";
                    standartData.act = "register";
                    standartData.login = "+380" + telephoneLogin;

                    Gson gson = new Gson();
                    String json = gson.toJson(standartData);
                    JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                    Log.e("regestration", "convertedObject: " + convertedObject);

                    retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            Log.e("regestration", "response: " + response.body());
                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    Toast.makeText(menu_login.this, "response.body(): " + response.body(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(menu_login.this, "response.body(): NULL", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(menu_login.this, "response.code: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Log.e("regestration", "response: " + t);    // java.io.EOFException: End of input at line 1 column 1 path $
                            Toast.makeText(menu_login.this, "Проверьте связь: " + t, Toast.LENGTH_SHORT).show();
                        }
                    });

                    Toast.makeText(this, "Тут, в данный момент, должно сработать отправка на сервер телефона для регистрации, закрытие этого окошка и ожидание СМС-ки с паролем", Toast.LENGTH_LONG).show();
                });
                dialog.show();
            }

            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
        });


        dialogRegistration.setClose(dialogRegistration::dismiss);
        dialogRegistration.show();


        Toast.makeText(this, "Для входа в приложение укажите свой логин и пароль в соответствующих полях и нажмите кнопку \"Вход\". Логин и пароль Вы можете получить связавшись с представителем нашей компании позвонив по телефону +380674491922 +380674061394", Toast.LENGTH_LONG).show();

        /*Для входа в приложение укажите свой логин и пароль в соответствующих полях и нажмите кнопку "Вход". Логин и пароль Вы можете получить связавшись с представителем нашей компании позвонив по телефону +380674491922 +380674061394*/

    }

    // TEXT_BUTTON VersionApp
    public void version(View view) {
        Long currentVer = Long.valueOf(BuildConfig.VERSION_NAME);
        Long minimalVer = VersionApp.VERSION_APP;

        Log.e("ПОСЛЕДОВАТЕЛЬНОСТЬ", "1_obj893: " + obj893);

        StringBuilder msg = new StringBuilder();
        msg.append(obj893 + ": " + BuildConfig.VERSION_NAME + "\n\n");

        if (minimalVer != null) {
            if (currentVer < minimalVer) {
                msg.append(obj895 + ": ").append(minimalVer);
            } else {
                msg.append(obj894);
            }
            showDialogVer(msg);
        } else {
            VersionApp versionApp = new VersionApp();
            versionApp.getMinVer(new Globals.getVersionInterface() {
                @Override
                public void onSuccess(Long l) {
                    if (currentVer < l) {
                        msg.append(obj895 + ": ").append(minimalVer);
                    } else {
                        msg.append(obj894);
                    }
                    showDialogVer(msg);
                }

                @Override
                public void onFailure(String s) {
                    Toast.makeText(menu_login.this, s, Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    private void showDialogVer(StringBuilder msg) {
        DialogData dialog = new DialogData(this);
        dialog.setTitle("Версия приложения");
        dialog.setText(String.valueOf(msg));
        dialog.setMerchikIco(this);
        dialog.show();
    }


    //---------------------------------------------------------------------------------------------

    private void loginOnServer() {
        try {
            String mod = "auth";
            String sessId = "";
            if (sess_id != null) {
                sessId = sess_id;
            }

            retrofit2.Call<Login> call = RetrofitBuilder.getRetrofitInterface().loginInfo(mod, sessId);
            call.enqueue(new retrofit2.Callback<Login>() {
                @Override
                public void onResponse(retrofit2.Call<Login> call, retrofit2.Response<Login> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.code() == 200) {
                            Log.e("loginOnServer", "response.body(): " + response.body().getState());
                            wil = 0;
                            appLogin();
                        } else {
                            withoutLogin();
                        }
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<Login> call, Throwable t) {
                    withoutLogin();
                }
            });
        } catch (Exception e) {
            globals.alertDialogMsg(this, "Ошибка_1: " + e);
        }

    }


    /**
     * Функция логина в приложении.
     * <p>
     * Проверка данных на сервере.
     **/
    private void appLogin() {
        // https://merchik.net/mobile_app.php?mod=auth&app_data=435235235

        try {
            String mod = "auth";
            String act = "sotr_auth";

            Log.e("APP_LOGIN", "LOGIN(0): " + login + " pass: " + password);

            progress = new Progress(this, "Вход", "Вход в систему", false);
            progress.show();

            // Проверка - есть ли сессия на сервере(залогинились ли мы)
            retrofit2.Call<SessionCheck> call = RetrofitBuilder.getRetrofitInterface().CHECK_SESSION(mod, Globals.getAppInfoToSession(this));
            call.enqueue(new retrofit2.Callback<SessionCheck>() {
                @Override
                public void onResponse(retrofit2.Call<SessionCheck> call, retrofit2.Response<SessionCheck> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            SessionCheck resp = response.body();
                            Log.e("APP_LOGIN", "AUTH: " + resp.getAuth());

                            JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(response.body()), JsonObject.class);
                            Log.e("APP_LOGIN", "convertedObject: " + convertedObject);


                            // Сохраняем id сессии
                            if (resp.getSessionId() != null && resp.getSessionId().equals("")) {
                                Globals.session = resp.getSessionId();
                                Log.e("APP_LOGIN", "AUTH SESSION: " + resp.getSessionId());
                            }

                            if (resp.getState()) {
                                // Если сессия активна - заходим в приложение
                                if (resp.getAuth()) {
                                    Log.e("APP_LOGIN", "AUTH: " + resp.getUserInfo());

                                    AppUsersDB appUsersDB = RealmManager.getAppUserById(resp.getUserInfo().getUserId());
                                    if (appUsersDB != null) {

                                        Log.e("PreferenceManager", "BLOC_1");
                                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                                                .putString("user_id", String.valueOf(appUsersDB.getUserId())).apply();

                                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                                                .putString("login", appUsersDB.getLogin()).apply();

                                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                                                .putString("password", appUsersDB.getPassword()).apply();
                                        Log.e("PreferenceManager", "BLOC_1" + PreferenceManager.getDefaultSharedPreferences(menu_login.this)
                                                .getString("login", ""));

                                        // Запись в лог инфы
                                        try {
                                            RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Вход в приложение. Сессия активна.", 1084, null, null, null, null, null, null, null)));
                                        } catch (Exception e) {
                                        }

                                        progress.dissmiss();
                                        new TablesLoadingUnloading().downloadMenu();
                                        Toast.makeText(getApplicationContext(), "Вы зашли как " + resp.getUserInfo().getFio(), Toast.LENGTH_SHORT).show();
                                        Globals.userId = Integer.parseInt(resp.getUserInfo().getUserId());
                                        Globals.token = resp.websocketParam.token;
                                        startActivity(intent); // ++
                                    } else {
//                                    Toast.makeText(menu_login.this, "Внесите Логин/Пароль и повторите попытку", Toast.LENGTH_SHORT).show();
//                                    progress.dissmiss();
                                        AUTH();
                                    }
                                } else if (!resp.getAuth()) {
                                    // login
                                    AUTH(); // Вход через Бд или через Логин/Пароль
                                } else {
                                    // Что-то пошло не по плану.
                                    withoutLogin(); // Оффлайн режим
                                }
                            }

                        }
                    } catch (Exception e) {
                        globals.alertDialogMsg(menu_login.this, "Ошибка во время логина(1). Обратитесь к Вашему руководителю. Ошибка: " + e);
                        progress.dissmiss();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<SessionCheck> call, Throwable t) {
                    withoutLogin();
                }
            });
        } catch (Exception e) {
            globals.alertDialogMsg(this, "Ошибка_2: " + e);

        }
    }

    private void AUTH() {
        try {
            String sEditTextLogin = autoText.getText().toString();
            String sEditTextPassword = editText_password.getText().toString();
            String login = "";
            String password = "";

            AppUsersDB appUsersDB = RealmManager.getAppUser();

            if (appUsersDB != null || !sEditTextLogin.equals("") && !sEditTextPassword.equals("")) {

                // Костыль что б можно было логиниться
                if (appUsersDB != null) {
                    login = appUsersDB.getLogin();
                    password = appUsersDB.getPassword();
                    wil = 1;    // Человек залогинится с помощью БД
                } else {
                    login = sEditTextLogin;
                    password = sEditTextPassword;
                    wil = 2;    // Человек залогинится с помощью введеных данных
                }

                // Логинимся на сервере
                String mod = "auth";
                String act = "sotr_auth";

                Log.e("APP_LOGIN", "LOGIN: " + login + " pass: " + password);

                retrofit2.Call<Login> callLogin = RetrofitBuilder.getRetrofitInterface().LOGIN(mod, act, login, password, Globals.getAppInfoToSession(this));
                String finalLogin = login;
                String finalPassword = password;
                callLogin.enqueue(new retrofit2.Callback<Login>() {
                    @Override
                    public void onResponse(retrofit2.Call<Login> callLogin, retrofit2.Response<Login> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(response.body()), JsonObject.class);
                            Log.e("APP_LOGIN", "convertedObject: " + convertedObject);

                            // Разбираем ответ на логин
                            Log.e("APP_LOGIN", "LOGIN_callLogin: " + response.body().getState());
                            if (response.body().getState()) {

                                // =================================================
                                retrofit2.Call<JsonObject> TEST_SESSION_CALL = RetrofitBuilder.getRetrofitInterface().CHECK_SESSION2(mod);
                                TEST_SESSION_CALL.enqueue(new retrofit2.Callback<JsonObject>() {

                                    @Override
                                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                        Log.e("APP_LOGIN", "(2)TEST_SESSION_CALL: " + response.body());
                                    }

                                    @Override
                                    public void onFailure(Call<JsonObject> call, Throwable t) {
                                        Log.e("APP_LOGIN", "(2)TEST_SESSION_CALL_ERROR: " + t);
                                    }
                                });

                                Call<SessionCheck> callAUTH = RetrofitBuilder.getRetrofitInterface().CHECK_SESSION(mod, Globals.getAppInfoToSession(menu_login.this));
                                callAUTH.enqueue(new retrofit2.Callback<SessionCheck>() {
                                    @Override
                                    public void onResponse(retrofit2.Call<SessionCheck> callAUTH, retrofit2.Response<SessionCheck> RESPONSE) {
//                                        Log.e("APP_LOGIN", "AUTH2: " + RESPONSE.body());
//                                        Log.e("APP_LOGIN", "AUTH2: " + RESPONSE.body().getUserInfo());
//                                        Log.e("APP_LOGIN", "AUTH2: " + RESPONSE.body().getUserInfo().getUserId());
                                        if (RESPONSE.isSuccessful() && RESPONSE.body() != null) {
                                            JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(RESPONSE.body()), JsonObject.class);

                                            SessionCheck resp = RESPONSE.body();

                                            Globals.session = resp.getSessionId();

                                            if (resp.getAuth()) {
                                                // Если залогинились - запись в БД
                                                RealmManager.setAppUser(new AppUsersDB(Integer.parseInt(resp.getUserInfo().getUserId()), resp.getUserInfo().getFio(), finalLogin, finalPassword));

                                                AppUsersDB appUsersDB = RealmManager.getAppUserById(resp.getUserInfo().getUserId());

                                                Log.e("PreferenceManager", "BLOC_2");
                                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                                                        .putString("user_id", String.valueOf(appUsersDB.getUserId())).apply();

                                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                                                        .putString("login", appUsersDB.getLogin()).apply();

                                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                                                        .putString("password", appUsersDB.getPassword()).apply();
                                                Log.e("PreferenceManager", "BLOC_2" + PreferenceManager.getDefaultSharedPreferences(menu_login.this)
                                                        .getString("login", ""));

                                                // Запись в лог инфы
                                                try {
                                                    RealmManager.setRowToLog(Collections.singletonList(new LogDB(RealmManager.getLastIdLogDB() + 1, System.currentTimeMillis() / 1000, "Вход в приложение. (" + wil + ")", 1084, null, null, null, null, null, null, null)));
                                                } catch (Exception e) {
                                                }


                                                {
                                                    tablesLoadingUnloading.downloadAllTables(menu_login.this);
                                                    new TablesLoadingUnloading().downloadMenu();
                                                    Toast.makeText(getApplicationContext(), "Вы зашли как " + resp.getUserInfo().getFio(), Toast.LENGTH_SHORT).show();
                                                    Globals.userId = Integer.parseInt(resp.getUserInfo().getUserId());
                                                    Globals.token = resp.websocketParam.token;
                                                }
                                                progress.dissmiss();
                                                startActivity(intent);  //++
                                            } else {
                                                appLogin();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(retrofit2.Call<SessionCheck> callAUTH, Throwable t) {
                                        withoutLogin();
                                    }
                                });
                            } else {
                                globals.alertDialogMsg(menu_login.this, response.body().getError());
                                progress.dissmiss();
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Login> callLogin, Throwable t) {
                        withoutLogin();
                    }
                });


            } else {
                Toast.makeText(getApplicationContext(), "Проверьте внесённые данные.", Toast.LENGTH_SHORT).show();
                progress.dissmiss();
            }

        } catch (Exception e) {
            globals.alertDialogMsg(this, "Ошибка_3: " + e);
        }

    }

    private void withoutLogin() {
        try {
            AppUsersDB appUsersDB = RealmManager.getAppUser();

            if (appUsersDB != null) {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putString("user_id", String.valueOf(appUsersDB.getUserId())).apply();

                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putString("login", appUsersDB.getLogin()).apply();

                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putString("password", appUsersDB.getPassword()).apply();
                Log.e("PreferenceManager", "BLOC_3" + PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("login", ""));


                new TablesLoadingUnloading().downloadMenu();
                Globals.userId = appUsersDB.getUserId();

                progress.dissmiss();
                startActivity(intent);  //++
            } else {
                // Не получилось залогиниться БЕЗ инета или при ошибке. БД пустая.
                progress.dissmiss();
                globals.alertDialogMsg(menu_login.this, "Не удалось войти. \n\nПроверьте состояние интернета и повторите попытку входа.");
            }

        } catch (Exception e) {
            globals.alertDialogMsg(this, "Ошибка_4: " + e);
        }

    }


    /**
     * 19.01.2021
     * Вывод подсказок с сайта
     * <p>
     * Получаем с сервера примеры логинов. Сделано для того что б человек не мог допустить ошибку
     * в своем ФИО, если он зарегестрирован на сервере. Механика работы 1:1 как работает подсказка
     * на самом сервере. Начинает работать после внесения 3го символа.
     */
    private void getUserLogin(String text) {
        String mod = "auth";
        String act = "login_search";

        String term = text;

        Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().GET_LOGIN_HINT(mod, act, term);
        call.enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("getUserLogin", "onResponse: " + response.body());

                LoginSearch obj = new Gson().fromJson(response.body(), LoginSearch.class);
                if (obj != null && obj.getState()) {
                    List<String> list = new ArrayList<>();
                    for (LoginSearchList item : obj.getList()) {
                        list.add(item.getValue());
                    }

                    LoginArrayAdapter adapter = new LoginArrayAdapter(menu_login.this, android.R.layout.simple_list_item_1, list);
                    autoText.setAdapter(adapter);
                    Log.e("getUserLogin", "onResponse.true.setAdapter: " + adapter);
                } else {
                    Log.e("getUserLogin", "onResponse.false.setAdapter: " + null);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("getUserLogin", "onFailure: " + t.toString());
            }
        });

    }


    /**
     * 20.01.2021
     * Автозаполение текста.
     */
    private void autoEditText() {
        autoText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String string = autoText.getText().toString().toLowerCase(Locale.getDefault());
                Log.e("getUserLogin", "beforeTextChanged: " + string);
                getUserLogin(string);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    /**
     * 02.02.2021
     * Установка подсказки
     * <p>
     * Устанавливает подсказку по нажатию на "?" в правом нижнем углу.
     */
    private void setFab(FloatingActionButton fab) {

        getHelpFromServer();

        try {
            fab.setOnClickListener(view -> {
                DialogLoginHelp dialog = new DialogLoginHelp(this);
//                dialog.setHelpMsg(SpannableString.valueOf(help));
                dialog.setTitle(obj890);
                dialog.setHelpMsg(help2);
                dialog.setClose(dialog::dismiss);
                dialog.setLesson(this, true, 812);
                dialog.setVideoLesson(this, true, 813, () -> {
//                    Toast.makeText(this, "НУ КАК ТЫ НОЛЬ ЕСЛИ НЕ НОЛЬ", Toast.LENGTH_LONG).show();
                });
//                dialog.setImgBtnCall(this);
                dialog.show();
            });
        } catch (Exception e) {
            Log.e("setFab", "e: " + e);
            e.printStackTrace();
        }
    }


    /**
     * 02.02.2021
     * Получение с сервера подсказки для новых пользователей.
     * <p>
     * С сервера приходит "подсказка для новых пользователей". Она может меняться. Она взята с
     * сервера, но для приложение она видоизменена. Её надо солучать при старте приложения и
     * сохранять в "кэше" для того что б отображать, если нет интернета. Каждый раз при старте
     * приложения получать Обновлять данное сообщение.
     */
    private String help = "";
    private SpannableStringBuilder help2;

    private void getHelpFromServer() {
        String mod = "help";
        String act = "get";

        Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().GET_LOGIN_HELP(mod, act);
        call.enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("getHelpFromServer", "onResponse.response.body(): " + response.body());

                // TODO Нужно сохранить обьект подсказки где-то в телефоне. Убрать парс отсюда.

                help2 = parseText(response.body());
                Log.e("getHelpFromServer", "parseText.help.: " + help);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("getHelpFromServer", "onFailure.t: " + t);
            }
        });
    }


    /**
     * 02.02.2021
     * Парсинг ответа подсказки. Сбор данных в кликабельный текст.
     * <p>
     * С сервера приходит функциональный текст, нужно его распарсить и использовать соответствующий
     * функционал в зависимости от того что надо делать.
     */
    private SpannableStringBuilder parseText(JsonObject object) {
        String res = "";
//        StringBuilder builder = new StringBuilder();
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (object != null) {
            if (object.get("state") != null) {
                boolean b = object.get("state").getAsBoolean(); // Прошел ли запрос успешно?
                if (b) {
                    // Если запрос прошел успешно - получаем список с текстом.
                    JsonArray arr = object.get("list").getAsJsonArray();

                    int i = 1;
                    for (JsonElement list : arr) {
                        JsonObject obj = list.getAsJsonObject();
                        String s = obj.get("text").getAsString();

//                        String test = setString(obj);
                        CharSequence test = setString(obj);
//                        test = test.replace("&quot;", "\"");
                        Log.e("parseText", "test: " + test);

//                        builder.append(test);

                        //tv.setText(builder);

//                        builder.append(i).append(". ").append(s).append("\n");
                        builder.append(String.valueOf(i)).append(". ").append(test).append("\n");
                        i++;
                    }

                    return builder;
//                    return String.valueOf(builder);

                } else {
                    // todo Разобрать исключение. STATE FALSE - запрос не удачный
                }
            } else {
                // todo Разобрать исключение. STATE NULL - такого быть не должно, странно что это вылетит
            }
        } else {
            // todo Разобрать исключение. От сервера пришла пустота - очень странная ситуация
        }

        return builder;
    }


    /**
     * 08.02.2021
     * Функционал для звонка
     */
    private class CallClickableSpan extends ClickableSpan {

        private Context mContext;
        private String phone;

        public CallClickableSpan(Context context, String phone) {
            this.mContext = context;
            this.phone = phone;
        }

        @Override
        public void onClick(@NonNull View widget) {
            Globals.telephoneCall(mContext, phone);
        }
    }


    /**
     * 08.02.2021
     * Функционал для диалога
     */
    private class DialogClickableSpan extends ClickableSpan {

        private Context mContext;
        private String title;
        private String text;
        private String mod;

        public DialogClickableSpan(Context context, String mod) {
            this.mContext = context;
            this.mod = mod;
        }

        public DialogClickableSpan(Context context, String title, String text) {
            this.mContext = context;
            this.title = title;
            this.text = text;
        }

        @Override
        public void onClick(@NonNull View widget) {
//            DialogData dialog = new DialogData(mContext);
//            dialog.setTitle(title);
//            dialog.setText(text);
//            dialog.show();

            switch (mod) {
                case "dialog1":
                    Log.e("DIALOG_MOD", "DIALOG_MOD/dialog1: " + mod);
                    DialogRetingOperatorSuppr dialog = new DialogRetingOperatorSuppr(mContext);
                    dialog.setLesson(mContext, true, 0);
                    dialog.setVideoLesson(mContext, true, 0, null);
                    dialog.setMerchikIco(mContext);
                    dialog.show();
                    break;

                case "dialog2":
                    Log.e("DIALOG_MOD", "DIALOG_MOD/dialog2: " + mod);
                    DialogSupport dialog2 = new DialogSupport(mContext);
                    dialog2.setLesson(mContext, true, 0);
                    dialog2.setVideoLesson(mContext, true, 0, null);
                    dialog2.setMerchikIco(mContext);
                    dialog2.show();
                    break;
            }


        }
    }

    /**
     * 08.02.2021
     * Установка ClickableSpan
     */
    private ClickableSpan setClickableSpan(String mode, String modDialog, CharSequence text) {
        ClickableSpan clickableSpan;
        Log.e("setClickableSpan", "mode: " + mode + ", text: " + text);
        switch (mode) {
            case "call":
                clickableSpan = new CallClickableSpan(this, text.toString());
                return clickableSpan;
            case "modal_form":
                Log.e("DIALOG_MOD", "DIALOG_MOD/setClickableSpan: " + modDialog);
                clickableSpan = new DialogClickableSpan(this, modDialog);
                return clickableSpan;

            default:
                return null;
        }
    }


    /**
     * 02.02.2021
     * Установка строки.
     * <p>
     * Разбор строки в JsonObject что б подставить нужный текст или функционал.
     */
    private CharSequence setString(JsonObject object) {
        Log.e("setString", "=========================================");

        String text = object.get("text").getAsString();

        Log.e("setString", "START_OBJ: " + object);
        Log.e("setString", "START_STRING: " + text);

        SpannableStringBuilder spannableText = new SpannableStringBuilder(text);
        int index = 0;

        String[] res = StringUtils.substringsBetween(text, "{", "}");
        // [{help_sign}, {rate_quality}]
        Log.e("setString", "res: " + Arrays.toString(res));

        if (res != null) {
            for (String s : res) {
                Log.e("setString", "s: " + s);

                try {
                    JsonObject obj = getJsonObjParam(object, s);

                    Log.e("setString", "obj: " + obj);
                    Log.e("setString", "obj.get(\"text\").getAsString(): " + obj.get("text").getAsString());

                    CharSequence replacement = "{" + s + "}";   // Что заменяем
                    CharSequence substitute = obj.get("text").getAsString(); // На что заменяем (замена)

                    Log.e("setString", "Что заменяем: " + replacement);
                    Log.e("setString", "НА что заменяем: " + substitute);

                    if (replacement.equals(substitute)) {
                        Log.e("setString", "---------------HERE");
                        substitute = setString(obj);
                        Log.e("setString", "---------------HERE_s2: " + substitute);
                    }

                    String replaced = text.replace(replacement, substitute);    // Итоговая строка после замещения
                    Log.e("setString", "Результат: " + replaced);


                    // ----- Поиск в строке с замещением телефонов.
                    List<String> tel = Globals.findTelephones(substitute);

                    String mode;
                    if (tel != null && tel.size() > 0) {
                        mode = "call";
                    } else {
                        mode = obj.get("type").getAsString();
                    }


                    // ----- Определение страрта/конца "кликабельной" строки
                    int index1 = replaced.indexOf(substitute.toString());// start of substitute in replaced
                    int index2 = index1 + substitute.length();//end of substitute in replaced

                    Log.e("setString", "spannableText" + "Я ДОЛЖЕН ТУТ БЫТЬ СТАРТ");
                    spannableText.delete(index, spannableText.length());
                    Log.e("setString", "spannableText.delete: " + spannableText);
                    spannableText.append(replaced.substring(index));
                    Log.e("setString", "spannableText.append: " + spannableText);


                    Log.e("setStringClick", "_______________________________________");
                    Log.e("setStringClick", "substitute: " + substitute);
                    Log.e("setStringClick", "mode: " + mode);

                    if (!mode.equals("text")) {
                        spannableText.setSpan(new ForegroundColorSpan(Color.BLUE), index1, index2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        Log.e("setString", "spannableText.setSpan: " + spannableText);

                        Log.e("setStringClick", "==========================================");
                        Log.e("setStringClick", "substitute: " + substitute);
                        Log.e("setStringClick", "mode: " + mode);


                        String dialogMod = "";
                        try {
                            String DIALOG_MOD = obj.get("param").getAsJsonObject().get("mod").getAsString();
                            Log.e("DIALOG_MOD", "DIALOG_MOD: " + DIALOG_MOD);
                            if (DIALOG_MOD.equals("suppr_rate_opinion_form")) {
                                dialogMod = "dialog1";
                            } else if (DIALOG_MOD.equals("ticket")) {
                                dialogMod = "dialog2";
                            }
                        } catch (Exception e) {
                            Log.e("DIALOG_MOD", "DIALOG_MOD: " + "NULL");
                        }

                        Log.e("DIALOG_MOD", "dialogMod: " + dialogMod);
                        spannableText.setSpan(setClickableSpan(mode, dialogMod, substitute), index1, index2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        index = index2;
                        Log.e("setString", "index: " + index);
                    }


                    text = replaced;
                    Log.e("setString", "Результат изначального текста: " + text);

                    Log.e("setString", "spannableText" + "Я ДОЛЖЕН ТУТ БЫТЬ КОНЕЦ");


                } catch (Exception e) {
                    try {
                        Log.e("setString", "object: " + object);
                        String TEST = object.get("param").getAsJsonObject().get(s).getAsString();
                        Log.e("setString", "objectTEST: " + TEST);
                        return TEST;
                    } catch (Exception ex) {
                        Log.e("setString", "ExceptionException: " + text);
                        return text;
                    }
                }
            }
        } else {
            Log.e("setString", "res == null");
        }
//        return text;
        return spannableText;
    }


    private JsonObject getJsonObjParam(JsonObject object, String str) {
        return object.get("param").getAsJsonObject().get(str).getAsJsonObject();
    }

    private boolean isSubstring(String s) {
        return StringUtils.substringsBetween(s, "{", "}") != null;
    }

    private SpannableString getSpanned() {
        return null;
    }


    /**
     * 09.02.2021
     * <p>
     * Получение языков с сервера.
     * (Если тип = true -- скачиваем переводы)
     * <p>
     * mod=translation
     * <p>
     * act=lang_list -список языков
     * <p>
     * act=translation_list - список переводов элементов
     * dt - unixtime времени последнего изменения элементов
     * lang_id - код языка, для которого требуется загрузить переводы
     * <p>
     * todo нужно эыто будет перенести в соответствующий класс
     *
     * @param languagesResponce
     */
    private void getTableTranslate(ExchangeInterface.LanguagesResponce languagesResponce) {
        try {
            Log.e("getTableTranslate", "+");
            String l = Translate.getAppLanguage(this);
            LangListDB id_lang = RealmManager.getLangList(l);

            Log.e("getTableTranslate", "id_lang: " + id_lang);
            TablesLoadingUnloading.downloadSiteHints(id_lang.getID());
        } catch (Exception e) {
            Log.e("getTableTranslate", "-");
        }

        String mod = "translation";
        String act = "lang_list";
        retrofit2.Call<SiteLanguages> call = RetrofitBuilder.getRetrofitInterface().GET_LANGUAGES(mod, act);
        call.enqueue(new retrofit2.Callback<SiteLanguages>() {
            @Override
            public void onResponse(retrofit2.Call<SiteLanguages> call, retrofit2.Response<SiteLanguages> response) {
                Log.e("getTableTranslate", "lang_list.response: " + response.body());
                try {
                    if (response.body() != null) {
                        if (response.body().getList() != null) {
                            languagesResponce.onSuccess(response.body().getList(), "");
                        }
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(retrofit2.Call<SiteLanguages> call, Throwable t) {
                Log.e("getTableTranslate", "t:" + t);
            }
        });


        try {
            List<LangListDB> list = RealmManager.getAllLangList();
            for (LangListDB item : list) {
                Log.e("getTableTranslate", "item: " + item.getID());
                String mod2 = "translation";
                String act2 = "translation_list";
                String lang = item.getID();
                retrofit2.Call<SiteTranslations> call2 = RetrofitBuilder.getRetrofitInterface().GET_TRANSLATES(mod2, act2, lang);
                call2.enqueue(new retrofit2.Callback<SiteTranslations>() {
                    @Override
                    public void onResponse(retrofit2.Call<SiteTranslations> call, retrofit2.Response<SiteTranslations> response) {
                        try {
                            if (response.body() != null) {
                                if (response.body().getList() != null) {
                                    saveTranslates(response.body().getList());
                                }
                            }

                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<SiteTranslations> call, Throwable t) {
                        Log.e("getTableTranslate", "t2: " + t);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("getTableTranslate", "Exception: " + e);
        }

    }


    /**
     * 10.02.2021
     * Сохранение в БД переводов
     */
    private void saveLanguages(List<LangListDB> data) {

        Log.e("saveLanguages", "data: " + data);
        for (LangListDB item : data) {
            Log.e("saveLanguages", "item.getID(): " + item.getID());
            Log.e("saveLanguages", "item.getNm(): " + item.getNm());
        }


        if (data != null) {
            INSTANCE.executeTransaction(realm -> {
                INSTANCE.delete(LangListDB.class);
                INSTANCE.copyToRealmOrUpdate(data);
            });
        }
    }

    /**
     * 10.02.2021
     * Сохранение в БД переводов
     */
    private void saveTranslates(List<SiteTranslationsList> data) {
        Log.e("saveTranslates", "data: " + data);
        for (SiteTranslationsList item : data) {
            Log.e("saveTranslates", "item.getID(): " + item.getID());
            Log.e("saveTranslates", "item.getNm(): " + item.getNm());
            Log.e("saveTranslates", "item.getTitle(): " + item.getTitle());
        }

        if (data != null) {
            INSTANCE.executeTransaction(realm -> {
                for (SiteTranslationsList item : data) {
                    INSTANCE.copyToRealmOrUpdate(item);
                }
            });
        }

    }


    private void setFlagClick() {
        flag.setOnClickListener(v -> {
            showPopupMenu(v);
        });
    }


    @SuppressLint("ResourceType")
    private void showPopupMenu(View view) {

        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.popup_language);

        try {
            LangListDB langUa = RealmManager.getLangList("UA");
            popupMenu.getMenu().findItem(R.id.lang1).setTitle(langUa.getNm());
            LangListDB langRu = RealmManager.getLangList("RU");
            popupMenu.getMenu().findItem(R.id.lang2).setTitle(langRu.getNm());
            LangListDB langGb = RealmManager.getLangList("GB");
            popupMenu.getMenu().findItem(R.id.lang3).setTitle(langGb.getNm());
            LangListDB langPl = RealmManager.getLangList("PL");
            popupMenu.getMenu().findItem(R.id.lang4).setTitle(langPl.getNm());
        } catch (Exception e) {
        }


        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.lang1:
                    Toast.makeText(this, "Вы сменили язык. Дождитесь сообщения о загрузке нового языка", Toast.LENGTH_SHORT).show();
                    Translate.setLanguage(getApplicationContext(), 2);

                    new SiteObjectsExchange().downloadSiteObjects(new Exchange.ExchangeInt() {
                        @Override
                        public void onSuccess(String msg) {
                            Toast.makeText(menu_login.this, "Новый язык загружен.", Toast.LENGTH_SHORT).show();
                            flag.setImageDrawable(getResources().getDrawable(R.drawable.ua));
                            PreferenceManager.getDefaultSharedPreferences(menu_login.this).edit().putString("lang", "UA").apply();
                            restartActivity(menu_login.this);   //++
                        }

                        @Override
                        public void onFailure(String error) {
                            Translate.setLanguage(getApplicationContext(), 1);

                            Toast.makeText(menu_login.this, "Произошла ошибка при загрузке нового языка, повторите попытку позже.", Toast.LENGTH_SHORT).show();
                            Log.e("СМЕНА_ЯЗЫКА", error);
                        }
                    });
                    return true;

                case R.id.lang2:
                    Toast.makeText(this, "Вы сменили язык. Дождитесь сообщения о загрузке нового языка", Toast.LENGTH_SHORT).show();
                    Translate.setLanguage(getApplicationContext(), 1);

                    new SiteObjectsExchange().downloadSiteObjects(new Exchange.ExchangeInt() {
                        @Override
                        public void onSuccess(String msg) {
                            Toast.makeText(menu_login.this, "Новый язык загружен.", Toast.LENGTH_SHORT).show();
                            flag.setImageDrawable(getResources().getDrawable(R.drawable.ru));
                            PreferenceManager.getDefaultSharedPreferences(menu_login.this).edit().putString("lang", "RU").apply();
                            restartActivity(menu_login.this);   //++
                        }

                        @Override
                        public void onFailure(String error) {
                            Translate.setLanguage(getApplicationContext(), 1);

                            Toast.makeText(menu_login.this, "Произошла ошибка при загрузке нового языка, повторите попытку позже.", Toast.LENGTH_SHORT).show();
                            Log.e("СМЕНА_ЯЗЫКА", error);
                        }
                    });
                    return true;

                case R.id.lang3:
                    Toast.makeText(this, "Вы сменили язык. Дождитесь сообщения о загрузке нового языка", Toast.LENGTH_SHORT).show();
                    Translate.setLanguage(getApplicationContext(), 3);

                    new SiteObjectsExchange().downloadSiteObjects(new Exchange.ExchangeInt() {
                        @Override
                        public void onSuccess(String msg) {
                            Toast.makeText(menu_login.this, "Новый язык загружен.", Toast.LENGTH_SHORT).show();
                            flag.setImageDrawable(getResources().getDrawable(R.drawable.gb));
                            PreferenceManager.getDefaultSharedPreferences(menu_login.this).edit().putString("lang", "GB").apply();
                            restartActivity(menu_login.this);   //++
                        }

                        @Override
                        public void onFailure(String error) {
                            Translate.setLanguage(getApplicationContext(), 1);

                            Toast.makeText(menu_login.this, "Произошла ошибка при загрузке нового языка, повторите попытку позже.", Toast.LENGTH_SHORT).show();
                            Log.e("СМЕНА_ЯЗЫКА", error);
                        }
                    });
                    return true;

                case R.id.lang4:
                    Toast.makeText(this, "Вы сменили язык. Дождитесь сообщения о загрузке нового языка", Toast.LENGTH_SHORT).show();
                    Translate.setLanguage(getApplicationContext(), 7);

                    new SiteObjectsExchange().downloadSiteObjects(new Exchange.ExchangeInt() {
                        @Override
                        public void onSuccess(String msg) {
                            Toast.makeText(menu_login.this, "Новый язык загружен.", Toast.LENGTH_SHORT).show();
                            flag.setImageDrawable(getResources().getDrawable(R.drawable.pl));
                            PreferenceManager.getDefaultSharedPreferences(menu_login.this).edit().putString("lang", "PL").apply();
                            restartActivity(menu_login.this);   //++
                        }

                        @Override
                        public void onFailure(String error) {
                            Translate.setLanguage(getApplicationContext(), 1);

                            Toast.makeText(menu_login.this, "Произошла ошибка при загрузке нового языка, повторите попытку позже.", Toast.LENGTH_SHORT).show();
                            Log.e("СМЕНА_ЯЗЫКА", error);
                        }
                    });
                    return true;

                default:
                    return false;
            }

        });


        Object menuHelper;
        Class[] argTypes;
        try {
            Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
            fMenuHelper.setAccessible(true);
            menuHelper = fMenuHelper.get(popupMenu);
            argTypes = new Class[]{boolean.class};
            menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
        } catch (Exception e) {
            // Possible exceptions are NoSuchMethodError and NoSuchFieldError
            //
            // In either case, an exception indicates something is wrong with the reflection code, or the
            // structure of the PopupMenu class or its dependencies has changed.
            //
            // These exceptions should never happen since we're shipping the AppCompat library in our own apk,
            // but in the case that they do, we simply can't force icons to display, so log the error and
            // show the menu normally.
        }

        popupMenu.show();
    }


    private void restartActivity(Activity act) {
        Intent intent = new Intent();
        intent.setClass(act, act.getClass());
        act.startActivity(intent);
        act.finish();
    }


}// END CLASS..
