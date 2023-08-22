package ua.com.merchik.merchik;

import static ua.com.merchik.merchik.toolbar_menus.internetStatus;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.merchik.merchik.Activities.MyApplication;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.data.AppData.AppData;
import ua.com.merchik.merchik.data.AppData.Browser;
import ua.com.merchik.merchik.data.AppData.Device;
import ua.com.merchik.merchik.data.AppData.Os;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;

public class Globals {

    /**
     * Код телефонного региона Украина
     */
    public static final String TELEPHONE_REGION_UA = "+38";
    public static final String APP_DIR = "/Merchik"; // Место где приложение в открытом доступе хранит свои данные


    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_REALM = "realm";

    final Handler handlerCount = new Handler();

    boolean viewMassage = true;
    boolean refresh;
    boolean uriNonElementMassage = true;


    // Связь
    static boolean autoSend = true;                 // Автообмен Выкл/Вкл
    static boolean onlineStatus;


    public static boolean isViewClicked = false;    // Нажималась ли кнопка?


    String currentDate, currentDatePlusOneDay;  // Даты

    // Нужны для работы метода Coordinates()
    public static Location locationGPS, locationNET;
    public static int provider; // 0=NULL; 1=GPS; 2=NET.
    public static double CoordX, CoordY, CoordAltitude;
    public static long CoordTime;
    public static float CoordSpeed, CoordAccuracy;
    public static int mocking;
    public static int providerType;

    // Для светофора и МП
    public static String lastGPSData, lastGPSTime, lastNETData, lastNETTime;
    public static String measure = "м";

    public static long delayGPS, delayNET;
    public static int dalayMaxTimeGPS = 30; // 30 минут по умолчанию
    public static int distanceMin = 500;
    public static float distanceAB;
    public static double lat, lon;

    //----------------------------------------------------------------------------------------------
    public static int userId;
    public static boolean userOwnership;    // В этом признаке буду хранить значение "Свой/Чужой"
    public static String token;
    public static Integer userEKLId;
    public static int langId = 2;
    public static boolean statusOnline = false;
    public static String session = null;
    public static int numberOfReports;
    public static long serverGetTime;

    // Данные для прилы TODO (ментор) Нужна помощь в переводах. Так правильно делать?
    public static List<TranslatesSDB> translatesList;

    //----------------------------------------------------------------------------------------------

    // Режимы в которых должно работать приложение.
    public enum AppWorkMode {
        ONLINE,
        OFFLINE
    }

    public enum Triple {
        TRUE, FALSE, NO_DATA
    }

    public enum OptionControlName {
        PRICE, FACE, EXPIRE_LEFT, AMOUNT, OBOROTVED_NUM, UP, DT_EXPIRE, ERROR_ID, AKCIYA_ID, AKCIYA, NOTES, PHOTO,
        LINK_TEXT   // 06.07.23. Добавлен такой тип для выпадающего списка реквизитов. Возможно изза него будут проблемы
    }

    public enum SourceAct {
        PHOTO_LOG,              // Журнал фото
        DETAILED_REPORT,        // Детализированный отчёт
        TASK_AND_RECLAMATION,   // Задачи и Рекламации (ЗИР)
        WP_DATA                 // План работ
    }

    public enum ViewHolderDataType {
        USER, ADDRESS, CUSTOMER, THEME, STATUS
    }

    public enum NewTARDataType {
        ADDRESS,
        CUSTOMER,
        THEME,
        OPINION,
        USERS,
        COMMENT,
        PREMIYA
    }

    public enum ReferencesEnum {
        ADDRESS,
        CUSTOMER,
        USERS,
        CHAT
    }

    // ----- shit data ------


    //------------------------------------- interface ----------------------------------------------


    public interface OperationResult {
        void onSuccess();

        void onFailure(String error);
    }


    public interface TARInterface {
        void onSuccess(TasksAndReclamationsSDB data);

        void onFailure(String error);
    }


    public interface getVersionInterface {
        void onSuccess(Long l);

        void onFailure(String s);
    }

    //----------------------------------------------------------------------------------------------

    // Вызов диалогового окна
    public boolean alertMassage(String msg, boolean state, final Context context) {

        final boolean[] TEST = {state};

        if (TEST[0]) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setMessage(msg);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TEST[0] = true;
//                    refreshActivity(context);
                        }
                    })
                    .setNegativeButton("Больше не показывать", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TEST[0] = false;
//                            refreshActivity(context);
//                            System.out.println("PRESSED: " + TEST[0]);
                        }
                    });

            builder.create().show();
        } else {
            TEST[0] = false;
            return TEST[0];
        }


//        System.out.println("PRESSED_1: " + state);
        return TEST[0];
    }


    // Вызов диалогового окна(просто Предупреждение)
    public void alertDialogMsg(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton("Ок", (dialog, which) -> {
        });
        builder.create().show();
    }


    public void alertDialogHTML(Context context, Spanned msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton("Ок", (dialog, which) -> {
        });
        builder.create().show();
    }

    public String convertToHtml(String htmlString) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<![CDATA[");
        stringBuilder.append(htmlString);
        stringBuilder.append("]]>");
        return stringBuilder.toString();
    }


    public static void refreshActivity(Context context) {
        Intent i = new Intent(context, context.getClass());
        ((Activity) context).finish();
        context.startActivity(i);
    }

    public static void restartActivity(Activity act) {
        Intent intent = new Intent();
        intent.setClass(act, act.getClass());
        act.startActivity(intent);
        act.finish();
    }


    public String getHashMD5FromFilePath(String filePath, Context context) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            if (context != null) {
                alertDialogMsg(context, "photo: " + inputStream + "\nОшибка в подсчёте MD5: " + e);
            }
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    if (context != null) {
                        alertDialogMsg(context, "Не вышло изза фото: " + e);
                    }
                }
            }
        }
    }

    // ===============================================================================================================
    public String getHashMD5FromFileTEST(Uri uri, Context context) {
        InputStream inputStream = null;
        try {

            Globals.writeToMLOG("INFO",
                    "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2",
                    "uri: " + uri);

            Globals.writeToMLOG("INFO",
                    "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2",
                    "uri.getPath(): " + uri.getPath());

//            File file = new File(uri.getPath());
            File file = new File(getRealPathFromURI(uri, context));

//            Uri uriFileProvider = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
//            Globals.writeToMLOG("INFO",
//                    "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2",
//                    "uriFileProvider: " + uriFileProvider);

            Globals.writeToMLOG("INFO",
                    "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2",
                    "file.length(): " + file.length());

            // 1
            boolean fileExists = file.exists();
            Globals.writeToMLOG("INFO",
                    "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2",
                    "fileExists: " + fileExists);

            // 2
            boolean isReadable = file.canRead();
            if (isReadable) {
                // У тебя есть права на чтение файла
                Globals.writeToMLOG("INFO",
                        "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2",
                        "У тебя есть права на чтение файла isReadable: " + isReadable);
            } else {
                // У тебя нет прав на чтение файла
                Globals.writeToMLOG("INFO",
                        "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2",
                        "У тебя нет прав на чтение файла isReadable: " + isReadable);
            }

            try {
                inputStream = context.getContentResolver().openInputStream(uri);
            } catch (Exception e) {
                Globals.writeToMLOG("INFO",
                        "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2",
                        "inputStream Exception(2) e: " + e);
            }


            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            Globals.writeToMLOG("INFO",
                    "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2",
                    "Exception(1) e: " + e);
//            if (context != null) {
//                alertDialogMsg(context, "photo: " + inputStream + "\nОшибка в подсчёте MD5: " + e);
//            }
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    Globals.writeToMLOG("INFO",
                            "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2",
                            "Exception(2) e: " + e);
//                    if (context != null) {
//                        alertDialogMsg(context, "Не вышло изза фото: " + e);
//                    }
                }
            }
        }
    }

    public static String getRealPathFromURI(Uri contentUri, Context context) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getApplicationContext().getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor == null) {
            return null;
        }
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }

    public static String getRealPathFromURITEST(Uri uri, Context context) {
        String filePath = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
        } else if (uri.getScheme().equals("file")) {
            filePath = uri.getPath();
        }
        return filePath;
    }


    public String getHashMD5FromFile2(Uri uri, Context context) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);

            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2", "Exception(1) e: " + e);
            if (context != null) {
                alertDialogMsg(context, "photo: " + inputStream + "\nОшибка в подсчёте MD5: " + e);
            }
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2", "Exception(2) e: " + e);
                    if (context != null) {
                        alertDialogMsg(context, "Не вышло изза фото: " + e);
                    }
                }
            }
        }
    }

    public String getHashMD5FromFile2(File file, Context context) {
        InputStream inputStream = null;
        try {

/*            // 1
            File file1 = new File(file.getAbsolutePath());
            boolean fileExists = file1.exists();
            Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2", "fileExists: " + fileExists);

            // 2
            boolean isReadable = file.canRead();
            if (isReadable) {
                // У тебя есть права на чтение файла
                Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2", "У тебя есть права на чтение файла isReadable: " + isReadable);
            } else {
                // У тебя нет прав на чтение файла
                Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2", "У тебя нет прав на чтение файла isReadable: " + isReadable);
            }

            //3
            int readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (readPermission == PackageManager.PERMISSION_GRANTED) {
                // Разрешение на чтение внешнего хранилища есть
                Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2", "Разрешение на чтение внешнего хранилища есть");
            } else {
                // Разрешение на чтение внешнего хранилища отсутствует
                Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2", "Разрешение на чтение внешнего хранилища отсутствует");
            }*/


//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && context != null) {
//                // Используем новую систему Scoped Storage
//                // Используй getContentResolver().openInputStream(uri) для получения InputStream
//            Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
//            Uri fileUri2 = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file1);
//
//            Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2", "fileUri: " + fileUri);
//            Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2", "fileUri2: " + fileUri2);
//
//            try {
//                inputStream = context.getContentResolver().openInputStream(fileUri);
//            } catch (Exception e) {
//                Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2", "inputStream Exception(1) e: " + e);
//            }
//
//            try {
//                inputStream = context.getContentResolver().openInputStream(fileUri2);
//            } catch (Exception e) {
//                Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2", "inputStream Exception(2) e: " + e);
//            }


            inputStream = context.getContentResolver().openInputStream(getFileUri(context, file));
//            } else {
            // Используем старый подход
            // Используй new FileInputStream(file) для получения InputStream
//                inputStream = new FileInputStream(file);
//            }

            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2", "Exception(1) e: " + e);
            if (context != null) {
                alertDialogMsg(context, "photo: " + inputStream + "\nОшибка в подсчёте MD5: " + e);
            }
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2", "Exception(2) e: " + e);
                    if (context != null) {
                        alertDialogMsg(context, "Не вышло изза фото: " + e);
                    }
                }
            }
        }
    }

    public Uri getFileUri(Context context, File file) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            } else {
                return Uri.fromFile(file);
            }
        } catch (Exception e) {
            Globals.writeToMLOG("INFO", "DetailedReportActivity/onActivityResult/PICK_GALLERY_IMAGE_REQUEST/getHashMD5FromFile2/getFileUri", "Exception e: " + e);
            return null;
        }
    }

    private static String convertHashToString(byte[] md5Bytes) {
        String returnVal = "";
        for (int i = 0; i < md5Bytes.length; i++) {
            returnVal += Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1);
        }
        return returnVal;
    }


    /**
     * Проверяет карту на наличие нулей в ней
     */
    public boolean isMapNull(Map<String, String> map, Context context) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null) {
                alertDialogMsg(context, "Значение " + key + " не удалось получить. Нажмите \"На главную\" в меню \"трёх точек\". Или попробуйте перезагрузить приложение. \n\nЕсли ничего из этого не помогло и ошибка повторяется - обратитесь к Вашему руководителю.");
                return true;
            }
        }
        return false;
    }

    public String getHashMD5FromFile(File updateFile, Context context) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            alertDialogMsg(context, "Не вышло получить файл: " + e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            alertDialogMsg(context, "Ошибка при расчёте МД5: " + e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                alertDialogMsg(context, "Изза ошибки с файлом - МД5 не вышло подсчитать: " + e);
            }
        }
        return null;
    }

    public static String getKeyForValue(Object desiredObject, Map<Integer, String> map) {
        //HashMap<String,Object> map=new HashMap<String,Object>();
        Set<Map.Entry<Integer, String>> entrySet = map.entrySet();

        //Object desiredObject=new Object();//что хотим найти
        for (Map.Entry<Integer, String> pair : entrySet) {
            if (desiredObject.equals(pair.getValue())) {
                return pair.getKey().toString();// нашли наше значение и возвращаем  ключ
            }
        }
        return null;
    }

//    public static String getKeyForValueStr(Object desiredObject, Map<String, String> map){
//        Set<Map.Entry<String,String>> entrySet=map.entrySet();
//
//        //Object desiredObject=new Object();//что хотим найти
//        for (Map.Entry<String,String> pair : entrySet) {
//            if (desiredObject.equals(pair.getValue())) {
//                return pair.getKey().toString();// нашли наше значение и возвращаем  ключ
//            }
//        }
//        return null;
//    }


    public static String getKeyForValueS(Object desiredObject, Map<Integer, String> map) {
        //HashMap<String,Object> map=new HashMap<String,Object>();
        Set<Map.Entry<Integer, String>> entrySet = map.entrySet();

        //Object desiredObject=new Object();//что хотим найти
        for (Map.Entry<Integer, String> pair : entrySet) {
            if (desiredObject.equals(pair.getValue())) {
                return String.valueOf(pair.getKey());// нашли наше значение и возвращаем  ключ
            }
        }
        return null;
    }


    /**
     * Получение дат для работы
     */
    public void getDate() {
        currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(currentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.DATE, 1);  // number of days to add
        currentDatePlusOneDay = sdf.format(calendar.getTime());  // dt is now the new date
    }


    public void startTimer() {
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                isViewClicked = false;
            }
        }, 1000);

    }


    // Long начала текущего дня. Нужно проверить ибо может быть не то что надо изза часового пояса
    public static long startOfDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        cal.set(Calendar.MINUTE, 0); // set minutes to zero
        cal.set(Calendar.SECOND, 0); //set seconds to zero
        return cal.getTimeInMillis();
    }


    // =========================


    // =========================

    /**
     * 09.09.2022
     * Сохранение фото в память телефона.
     * <p>
     * imageDir - "/Tovar" (например)
     */
    public static String saveImageHD(Bitmap finalBitmap, String imageDir, String image_name) {
        File myDir = MyApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES + imageDir);
        myDir.mkdirs();

        String fname = image_name + ".jpg";
        File file = new File(myDir, fname);

        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();

        }

        return file.getAbsolutePath();
    }


    public static String saveImage1(Bitmap finalBitmap, String image_name) {

        Log.e("TAG_TABLE", "PHOTO_TOVAR_URL_photo_start: " + finalBitmap.getByteCount());

//        String root = Environment.getExternalStorageDirectory().toString() + "/Merchik/Tovar";
//        File myDir = new File(root);


        File myDir = MyApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/Tovar");

        myDir.mkdirs();


        String fname = "/Tovar-" + image_name + ".jpg";
        File file = new File(myDir, fname);

        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();

        }


        return file.getAbsolutePath();
    }


    /**
     * 02.03.2021
     * Сохранение фотографии в память телефона доступную пользователю.
     * <p>
     * Аналог устаревшей функции "saveImage1" которая была заточека сохранять Только в один каталог
     * и только фото товаров. Возвращает путь фотки если она успешно смогла записаться в файлик или
     * пустоту при ошибке.
     *
     * @param folderPath Путь куда надо сохранить фото. Например: /Tovar
     * @param image_name Имя файла.
     * @param bitmap     Сама фотка для сохранения
     */
    public static String savePhotoToPhoneMemory(String folderPath, String image_name, Bitmap bitmap) {

        Log.e("savePhotoToPhoneMemory", "Размер фотографии для сохранения. (bitmap.getByteCount()): " + bitmap.getByteCount());

//        String root = Environment.getExternalStorageDirectory().toString() + APP_DIR + folderPath;
//        File myDir = new File(root);


        File myDir = MyApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES + folderPath);
        myDir.mkdirs(); // Если такой папки не создано - создать

        String fname = image_name + ".jpg"; // Создание имени файлика

        File file = new File(myDir, fname); // Создание файла

        if (file.exists())
            file.delete();   // Если такой файл уже есть - удаляем его (это вообще тут надо?)

        // Запись в файл фотки.
        // Если удачно сохранило фотку - возвращает её путь, иначе - возвразает пустоту.
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Добавляет запись в ЛОГ
     */
    public static void addLog() {

    }


    public void testMSG(Context context) {
        if (TablesLoadingUnloading.readyWPData &&
                TablesLoadingUnloading.readyImagesTp &&
                TablesLoadingUnloading.readyTypeGrp &&
                TablesLoadingUnloading.readyOptions &&
                TablesLoadingUnloading.readyReportPrepare &&
                TablesLoadingUnloading.readyCustomerTable &&
                TablesLoadingUnloading.readyAddressTable &&
                TablesLoadingUnloading.readySotrTable &&
                TablesLoadingUnloading.readyTovarTable &&
                TablesLoadingUnloading.readyErrorTable &&
                TablesLoadingUnloading.readyAkciyTable &&
                TablesLoadingUnloading.readyTradeMarksTable) {
            TablesLoadingUnloading.sync = false;
            TablesLoadingUnloading.readyWPData = false;
            TablesLoadingUnloading.readyImagesTp = false;
            TablesLoadingUnloading.readyTypeGrp = false;
            TablesLoadingUnloading.readyOptions = false;
            TablesLoadingUnloading.readyReportPrepare = false;
            TablesLoadingUnloading.readyCustomerTable = false;
            TablesLoadingUnloading.readyAddressTable = false;
            TablesLoadingUnloading.readySotrTable = false;
            TablesLoadingUnloading.readyTovarTable = false;
            TablesLoadingUnloading.readyErrorTable = false;
            TablesLoadingUnloading.readyAkciyTable = false;
            TablesLoadingUnloading.readyTradeMarksTable = false;

            alertDialogMsg(context, "Синхронизация окончена");
        }

        if (TablesLoadingUnloading.syncInternetError) {
            TablesLoadingUnloading.syncInternetError = false;
            TablesLoadingUnloading.sync = false;

            String msg;

            if (internetStatus == 1) {
                msg = "При выполнении Синхронизации возникла ошибка связи с сервером. Проверьте интернет " +
                        "соединение или повторите попытку позже.\n\nВнимание! Если отсутствует связь с " +
                        "сервером НЕ переустанавливайте приложение потому, что при переустановке приложения " +
                        "будут удалены все внесённые вами данные (фото, дет. отчёт, доступы, справочники " +
                        "и тп..) которые, на текущий момент, не выгружены на сервер.\n\nПри необходимости " +
                        "установки НОВОЙ ВЕРСИИ приложения из Плэй Маркета, используйте кнопку 'Обновить'.";
            } else {
                msg = "Проверьте состояние Вашего интернета. Подключитесь к Wifi или выйдите с помещения," +
                        " для лучшей связи и повторите попытку";
            }


            DialogData dialog = new DialogData(context);
            dialog.setTitle("Внимание!");
            dialog.setDialogIco();
            dialog.setText(msg);
            dialog.setClose(dialog::dismiss);
            dialog.show();


//            alertDialogMsg(context, "При выполнении Синхронизации возникла ошибка связи. Проверьте интернет соединение или повторите попытку позже.\n\nВнимание, если отсутствует связь с сервером не переустанавливайте приложение! Потому что при переустановке приложения будут удалены все внесённые вами данные(фото, дет. отчёт, доступы, справочники и тп..)\n\nпри необходимости установки новой версии приложения в Плэй Маркете используйте кнопку 'Обновить'.");
        }
    }


    // Костыль для выгрузки МП
    public String POST_10() {
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

        battery.put("battery_level", "");                           // Уровень зар¤да батареи в процентах или -1 если данных нет
        DataMap.put("battery", battery);

        DataMap.put("device_time", (CoordTime + 86400000) / 1000);                           // unixtime момента получения данных геокоординат

        connection_info.put("downlink", "");                                                  // скорость соединени¤ в мегабитах
        connection_info.put("downlinkMax", "");
        connection_info.put("effectiveType", "");                                                  // тип сети
        connection_info.put("rtt", "");                                                  // задержка
        connection_info.put("type", "");
        DataMap.put("connection_info", connection_info);                                            // информаци¤ о подключении к сети

        browser_info.put("hardwareConcurrency", Runtime.getRuntime().availableProcessors());        // количество ¤дер
        browser_info.put("maxTouchPoints", "");                                                // количество одновременно обрабатываемых точек касания
        browser_info.put("platform", Build.VERSION.SDK_INT);                             // платформа
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

        coords.put("latitude", CoordX);
        coords.put("longitude", CoordY);
        coords.put("altitude", CoordAltitude);                                             // высота над уровнем моря
        coords.put("accuracy", CoordAccuracy);                                             // точность обязательно
        coords.put("altitudeAccuracy", "");
        coords.put("heading", "");
        coords.put("speed", CoordSpeed);
        coords.put("trusted_location", mocking);
        coords.put("source_id", providerType);
        DataMap.put("coords", coords);                                                              // географические координаты

        DataMap.put("timestamp", System.currentTimeMillis() + 86400000); // unixtime текущего времени, когда был отправлен запрос с данными с точностью до тысячных (если не сможешь настолько точное врем¤ получить, бери текущий unixtime и умножай на 1000)

        M_to_URL = URL.httpBuildQuery(DataMap, "UTF-8");

        try {
            sURL = URLEncoder.encode(M_to_URL, "UTF-8"); //Кодирование URLData в конечную.
            base64 = Base64.encodeToString(sURL.getBytes(), 0);  //Кодирование конечной URLData в base64 для качества передачи.
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return base64;
    }


    /**
     * 22.10.21.
     * Запись в лог местоположений
     */
    public void fixMP() {
        try {
            int id = RealmManager.logMPGetLastId() + 1;
            Globals.writeToMLOG("INFO", "fixMP", "create new logMP id: " + id);
            LogMPDB log = new LogMPDB(id, POST_10());
            RealmManager.setLogMpRow(log);
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "fixMP", "Exception e: " + e);
        }
    }

    public static String getAppInfoToSession(Context context) {

        String date = context.getResources().getString(R.string.debug_date);
        String appDebugVersion = context.getResources().getString(R.string.debug_status);


        String osVerApi = String.valueOf(Build.VERSION.SDK_INT);
        Log.e("getAppInfoToSession", "Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT);
        String brovVer = "";
        try {
            brovVer = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            brovVer = brovVer.replaceAll("\\.", "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String getRadioVersion = Build.getRadioVersion();

//        DialogData dialog = new DialogData(context);
//        try {
//            dialog.setText(Build.VERSION.RELEASE + "\n" + BuildConfig.VERSION_NAME + "\n" + Build.BRAND + "\n" + Build.MODEL);
//        }catch (Exception e){
//            dialog.setText(e.toString());
//        }
//        dialog.show();


        Os os = new Os("Android", Build.VERSION.RELEASE, osVerApi);
        Browser browser = new Browser("MerchikApp", BuildConfig.VERSION_NAME, "mobile_app", date, appDebugVersion); // 1 = TestVer
        Device device = new Device("smartphone", Build.BRAND, Build.MODEL, getRadioVersion);
        AppData appData = new AppData(os, browser, device);

        Gson gson = new Gson();
        String json = gson.toJson(appData);

        Log.e("testApiData", "appData: " + json);

        return json;
    }


    public interface MyAwesomeClickListener {
        void clicked();
    }

    public class MyAwesomeDialog extends Dialog {

        MyAwesomeClickListener awesomeClickListener;

        public MyAwesomeDialog(@NonNull Context context, MyAwesomeClickListener awesomeClickListener) {
            super(context);
            this.awesomeClickListener = awesomeClickListener;
        }

        @Override
        public void dismiss() {
            super.dismiss();
            if (awesomeClickListener != null) awesomeClickListener.clicked();
        }
    }


    /**
     * 19.01.2021
     * Запись данных в файлик
     */
    public void writeToMLOG(String logRow) {
        try {
//            File root = new File(Environment.getExternalStorageDirectory(), "/Merchik");
            File root = MyApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_NOTIFICATIONS);

            if (!root.exists()) {
                root.mkdirs();
            }

            String fname = "M_LOG.txt";
            File file = new File(root, fname);

            FileOutputStream stream = new FileOutputStream(file, true);
            try {
                stream.write(logRow.getBytes());
            } finally {
                stream.close();
            }
            Log.e("writeToMLOG", "ENTER_DATA");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void testingMediaStore() {
        Log.e("testingMediaStore", "++");

        try {
            File root = MyApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_NOTIFICATIONS);
            Log.e("testingMediaStore", "root: " + root);


            if (!root.exists()) {
                Log.e("testingMediaStore", "exists");
                root.mkdirs();
            } else {
                Log.e("testingMediaStore", "!exists");
            }

            String fname = "M_LOG.txt";
            File file = new File(root, fname);

            String logRow = "qwerty";

            FileOutputStream stream = new FileOutputStream(file, true);
            try {
                stream.write(logRow.getBytes());
            } finally {
                stream.close();
            }
            Log.e("testingMediaStore", "ENTER_DATA");
        } catch (Exception e) {
            Log.e("testingMediaStore", "ERROR");
            e.printStackTrace();
        }

    }


    /**
     * Запись в лог
     */
    public static void writeToMLOG(String type, String place, String msg) {
        try {
            File root = MyApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_NOTIFICATIONS);
            if (!root.exists()) {
                root.mkdirs();
            }

            String fname = "M_LOG.txt";
            File file = new File(root, fname);
/*
            // Проверка количества записей
            int maxEntries = 100000; // Максимальное количество записей
            List<String> lines = new ArrayList<>();

            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                reader.close();

                if (lines.size() >= maxEntries) {
                    lines.subList(0, lines.size() - maxEntries).clear(); // Удалить старые записи
                }
            }*/

            FileOutputStream stream = new FileOutputStream(file, true);
            try {
                String time = Clock.getHumanTime();
                String space = " ";
                String delimiter = "\n";

                String data = time + space + type + space + place + space + msg + delimiter;

                stream.write(data.getBytes());
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 08.02.2021
     * Звонок по номеру.
     */
    public static void telephoneCall(Context context, String tel) {
        DialogData d = new DialogData(context);
        d.setTitle("Звонок оператору");
        d.setText("Сейчас будет набран номер: " + tel);
        d.setOk("Позвонить", () -> {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + tel));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO запросить разрешение
                    return;
                }
            }
            context.startActivity(intent);
        });
        d.show();
    }

    /**
     * 08.02.2021
     * Звонок по номеру.
     */
    public static void telephoneCall(Context context, String tel, String title) {
        DialogData d = new DialogData(context);
        d.setTitle(title);
        d.setText("Сейчас будет набран номер: " + tel);
        d.setOk("Позвонить", () -> {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + tel));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO запросить разрешение
                    return;
                }
            }
            context.startActivity(intent);
        });
        d.show();
    }

    public static List<String> findTelephones(CharSequence telephoneString) {
        Log.e("findTelephones", "================================================================");
        Log.e("findTelephones", "telephoneString: " + telephoneString);
        try {
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            Iterable<PhoneNumberMatch> iterable = phoneNumberUtil.findNumbers(telephoneString, TELEPHONE_REGION_UA);

            ArrayList<String> numbers = new ArrayList<>();

            for (PhoneNumberMatch number : iterable) {
                numbers.add(phoneNumberUtil.format(number.number(), PhoneNumberUtil.PhoneNumberFormat.NATIONAL));
            }

            Log.e("findTelephones", "numbers: " + numbers);
            return numbers;
        } catch (Exception e) {
            Log.e("findTelephones", "Exception: " + e);
            return null;
        }

    }


    /**
     * 14.07.2021
     * Декодирование ЭКЛ-а
     */
    public static String decodeEKL(String code) {
        String res = code;
        String key = "ylnRzl8P6RL7O3jD2v4GxezsBXRJOFaPW0PlAOSqt4VKOnC3FYasUWIMXWlX";
        return res + key;
    }


    public static String getSha1Hex(String clearString) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(clearString.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes) {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    public static void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    // 09.11.21
    // Профайлер. Делаем записи в него что б их можно было контролировать на сервере
    public static void setProfiler() {
    }


    /**
     * 27.11.2022
     * Реверс строки.
     */
    public static String reverseString(String str) {
        return new StringBuilder(str).reverse().toString();
    }

    ;


}//--------------


/*                    String msg = String.valueOf(response.body());
                    int maxLogSize = 1000;
                    for(int i = 0; i <= msg.length() / maxLogSize; i++) {
                        int start = i * maxLogSize;
                        int end = (i+1) * maxLogSize;
                        end = end > msg.length() ? msg.length() : end;
                        Log.e("MY_TAG", msg.substring(start, end));
                    }*/