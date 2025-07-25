package ua.com.merchik.merchik.MakePhoto;

import static ua.com.merchik.merchik.Global.UnlockCode.UnlockCodeMode.CODE_DAD_2_AND_OPTION;
import static ua.com.merchik.merchik.Global.UnlockCode.UnlockCodeMode.DATE_AND_USER;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.trecker.enabledGPS;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Global.UnlockCode;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.Controls.OptionControlMP;
import ua.com.merchik.merchik.PhotoReportActivity;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammJOINSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogShowcase.DialogShowcase;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;
import ua.com.merchik.merchik.trecker;

public class MakePhoto {

    static Globals globals = new Globals();
    private static final int CAMERA_REQUEST = 1;
    public static final int REQUEST_TAKE_PHOTO = 101;          // Получение фотки с Журнала Фото

    // Кода 200 я решил использовать для выполнения фото.
    public static final int CAMERA_REQUEST_TAKE_PHOTO = 200;   // Для нового интента выполнения фото
    public static final int CAMERA_REQUEST_TAKE_PHOTO_TEST = 201;   // Тестовый реквест для фото
    public static final int CAMERA_REQUEST_TAR_COMMENT_PHOTO = 203; // ЗИР. Закладка переписки. Список комментариев. Возможность делать фото к коментарию.
    public static final int CAMERA_REQUEST_PROMOTION_TOV_PHOTO = 204; // Опция Фото Акционного Товара + ценника

    // Кода 500 буду использовать для выбора фото из галереи
    public static final int PICK_GALLERY_IMAGE_REQUEST = 500;

    public static File image;
    private static Context mContext;
    public static WPDataObj wp;
    public static Uri contentUri;


    // TODO Устарело
    public static void startToMakePhoto(Context context, WPDataObj wpDataObj) {
        globals.writeToMLOG( "MakePhoto.startToMakePhoto: " + "ENTER" + "\n");
        mContext = context;
        wp = wpDataObj;

        if (wp != null)
            choiceCustomerGroupAndPhoto();
    }

    /*Alert dialog для выбора групы товара клиента*/

    /**
     * 27.08.2020
     * Alert dialog для выбора групы товара клиента
     * Когда группа выбрана - открываем фотоаппарат
     */
    // TODO Устарело
    private static void choiceCustomerGroupAndPhoto() {
        if (wp.getCustomerTypeGrp() != null) {
            final String[] result = wp.getCustomerTypeGrp().values().toArray(new String[0]);
            if (wp.getCustomerTypeGrp().size() > 1 && !wp.getPhotoType().equals("5")) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Выберите группу товара для следующего фото: ")
                        .setItems(result, (dialog, which) -> {
                            Toast t = Toast.makeText(mContext, "Выбрана группа товара: " + result[which], Toast.LENGTH_LONG);
                            t.show();
                            wp.setCustomerTypeGrpS(Globals.getKeyForValue(result[which], wp.getCustomerTypeGrp()));
                            takePhoto();
                        })
                        .show();
            } else if (wp.getCustomerTypeGrp().size() == 1 && !wp.getPhotoType().equals("5")) {
                wp.setCustomerTypeGrpS(Globals.getKeyForValue(result[0], wp.getCustomerTypeGrp()));
                Toast.makeText(mContext, "Выбрана группа товара: " + result[0], Toast.LENGTH_LONG).show();
                takePhoto();
            } else {
                if (!wp.getPhotoType().equals("5")) {
                    globals.alertDialogMsg(mContext, "Не обнаружено ни одной группы товаров по данному клиенту. Сообщите об этом Администратору!");
                }
                wp.setCustomerTypeGrpS("");
                takePhoto();
            }
        } else {
            globals.alertDialogMsg(mContext, "Не выбрано посещение\n\nЗайдите в раздел План работ, выберите посещение и повторите попытку.");
        }
    }


    // Выполнить проверку включённости GPS, МП и запустить фотоаппарат для фотографирования
    // TODO Устарело
    private static void takePhoto() {
        try {
            Log.e("takePhoto", "takePhoto1: " + enabledGPS);
            Globals.writeToMLOG("INFO", "takePhoto", "enabledGPS: " + enabledGPS);
            Globals.writeToMLOG("INFO", "takePhoto", "trecker.imHereGPS: " + trecker.imHereGPS + "/ trecker.imHereNET: " + trecker.imHereNET);
            Globals.writeToMLOG("INFO", "takePhoto", "Globals.CoordX: " + Globals.CoordX + "/ Globals.CoordY: " + Globals.CoordY);

            if (wp.getThemeId() != 998)
                dispatchTakePictureIntent();
            else if (enabledGPS) {
                if (wp != null) {
                    if (wp.getLatitude() > 0 && wp.getLongitude() > 0) {
//                    if (true){
                        if (Globals.CoordX != 0 && Globals.CoordY != 0) {

                            Log.e("takePhoto", "takePhoto2: " + wp.getLatitude() + "/" + wp.getLongitude());
                            Log.e("takePhoto", "takePhoto3: " + Globals.CoordX + "/" + Globals.CoordX);

                            Globals.writeToMLOG("INFO", "takePhoto", "wp.getLatitude(): " + wp.getLatitude() + "/ wp.getLongitude(): " + wp.getLongitude());
                            Globals.writeToMLOG("INFO", "takePhoto", "Globals.CoordX: " + Globals.CoordX + "/ Globals.CoordY: " + Globals.CoordY);

                            double d = ua.com.merchik.merchik.trecker.coordinatesDistanse(wp.getLatitude(), wp.getLongitude(), Globals.CoordX, Globals.CoordY);
                            if (d > 500) {
                                String settext1 = String.format("По данным системы вы находитесь на расстоянии %s метров от ТТ %s, что больше допустимых 500 метров.\n\nВы не сможете выполнить фото пока, Ваше местоположение не определено.\n\nЕсли в действительности Вы находитесь в ТТ - обратитесь за помощью к своему руководителю или в службу поддержки merchik.", (int) d, wp.getAddressIdTxt());
                                String settext2 = "*Система не обнаружила вас в ТТ. \n\nФотографии выполненные в этом режиме могут быть признаны не действительными.\n\nОтказаться от изготовления фото?";

                                DialogData dialog1 = new DialogData(mContext);
                                dialog1.setTitle("Нарушение по Местоположению.");
                                dialog1.setText(settext1);
//                                dialog1.setDialogErrorColor();
                                dialog1.setOk(Html.fromHtml("<font color='#000000'>Всё равно сделать фото</font>"), () -> {
                                    DialogData dialogData2 = new DialogData(mContext);
                                    dialogData2.setTitle("ВНИМАНИЕ!");
                                    dialogData2.setText(settext2);
                                    dialogData2.setOk(Html.fromHtml("<font color='#000000'>Да</font>"), dialogData2::dismiss);
                                    dialogData2.setCancel(Html.fromHtml("<font color='#000000'>Нет</font>"), () -> {
                                        dialog1.dismiss();
                                        dialogData2.dismiss();
                                        dispatchTakePictureIntent(); // Метод который запускает камеру и создаёт файл фото.
                                    });
                                    dialogData2.setClose(dialogData2::dismiss);
                                    dialogData2.show();
                                });
                                dialog1.setCancel2(Html.fromHtml("<font color='#000000'>Отказаться от изготовления фото</font>"), dialog1::dismiss);

                                dialog1.setImgBtnCall(mContext);
                                dialog1.setClose(dialog1::dismiss);
                                dialog1.show();


                                String title = "Нарушение по Местоположению.";
                                String msg = String.format("По данным системы вы находитесь на расстоянии %.1f метров от ТТ %s, что больше допустимых 500 метров.\n\nВы не сможете использовать фото которые выполните в таком состоянии системы.\n\nЕсли в действительности Вы находитесь в ТТ - обратитесь к своему руководителю за помощью.", d, wp.getAddressIdTxt());
                                String trueButton = "<font color='#000000'>Всё равно сделать фото</font>";
                                String falseButton = "<font color='#000000'>Отказаться от изготовления фото</font>";
                                String title2 = "ВНИМАНИЕ!";
                                String msg2 = "Система не обнаружила вас в ТТ. \n\nФотографии выполненные в этом режиме могут быть признаны не действительными.\n\nОтказаться от изготовления фото?";
                                String trueButton2 = "<font color='#000000'>Да</font>";
                                String falseButton2 = "<font color='#000000'>Нет</font>";

//                                alertMassageMP(1, title, msg, trueButton, falseButton, title2, msg2, trueButton2, falseButton2);
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
                                String bt1 = "<font color='#000000'>Всё равно сделать фото</font>";
                                String bf1 = "<font color='#000000'>Отказаться от изготовления фото</font>";
                                String t2 = "ВНИМАНИЕ!";
                                String m2 = "Фотографии выполненные в этом режиме могут быть признаны не действительными.\n\nОтказаться от изготовления фото?";
                                String bt2 = "<font color='#000000'>Да</font>";
                                String bf2 = "<font color='#000000'>Нет</font>";

                                alertMassageMP(mContext, 1, t1, m1, bt1, bf1, t2, m2, bt2, bf2);
                            } else {
                                dispatchTakePictureIntent(); // Метод который запускает камеру и создаёт файл фото.
                            }
                        } else {
                            dispatchTakePictureIntent(); // Метод который запускает камеру и создаёт файл фото.
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

                            alertMassageMP(mContext, 2, t1, m1, bt1, bf1, "", "", "", "");
                        }
                    }
                } else {
                    Toast.makeText(mContext, "Не обнаружены данные посещения, обратитесь к Вашему руководителю.", Toast.LENGTH_SHORT).show();
                }
            } else {
                String title = "Нет сигнала GPS";
                String msg = "Не могу определить Ваше местоположение. Возможно GPS выключен или Вы его только недавно включили.\n\n" +
                        "Вы не сможете выполнить фото пока, Ваше местоположение не определено.\n\n" +
                        "Включите GPS (для этого перейдите []), подождите 10 секунд и повторите попытку сделать фото.\n\n" +
                        "Если в действительности у Вас включён GPS - обратитесь за помощью к своему руководителю или в службу поддержки merchik.";
                String trueButton = "<font color='#000000'>У меня всё работает</font>";
                String falseButton = "<font color='#000000'>Закрыть сообщение</font>";
                String title2 = "ВНИМАНИЕ!";
                String msg2 = "Система не обнаружила GPS. \n\n" +
                        "Фотографии выполненные в этом режиме могут быть признаны не действительными.\n\n" +
                        "Отказаться от изготовления фото?";
                String trueButton2 = "<font color='#000000'>Да</font>";
                String falseButton2 = "<font color='#000000'>Нет</font>";


                DialogData dialogData1 = new DialogData(mContext);
                dialogData1.setTitle(title);
                dialogData1.setText(msg);
                dialogData1.setDialogErrorColor();
                dialogData1.setOk(Html.fromHtml(trueButton), () -> {
                    DialogData dialogData2 = new DialogData(mContext);
                    dialogData2.setTitle(title2);
                    dialogData2.setText(msg2);
                    dialogData2.setOk(Html.fromHtml(trueButton2), dialogData2::dismiss);
                    dialogData2.setCancel2(Html.fromHtml(falseButton2), () -> {
                        dialogData1.dismiss();
                        dialogData2.dismiss();
                        dispatchTakePictureIntent(); // Метод который запускает камеру и создаёт файл фото.
                    });
                    dialogData2.setClose(dialogData2::dismiss);
                    dialogData2.show();
                });
                dialogData1.setCancel2(Html.fromHtml(falseButton), dialogData1::dismiss);
                dialogData1.setClose(dialogData1::dismiss);
                dialogData1.setImgBtnCall(mContext);
                dialogData1.show();


//                alertMassageMP(1, title, msg, trueButton, falseButton, title2, msg2, trueButton2, falseButton2);
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Ошибка при проверке состояния GPS. Повторите попытку или обратитесь к Вашему руководителю. Ошибка: " + e, Toast.LENGTH_LONG).show();
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
    private static void alertMassageMP(Context context, int mod, String title, String msg, String trueButton, String falseButton, String title2, String msg2, String trueButton2, String falseButton2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyDialogTheme);
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setMessage(msg);
        if (mod == 1) {
            builder.setPositiveButton(Html.fromHtml(trueButton), (dialog, which) -> alertMassageMPPhoto(context, title2, msg2, trueButton2, falseButton2));
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
    private static void alertMassageMPPhoto(Context context, String title2, String msg2, String trueButton2, String falseButton2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyDialogTheme);
        builder.setTitle(title2);
        builder.setCancelable(false);
        builder.setMessage(msg2);
        builder.setPositiveButton(Html.fromHtml(trueButton2), (dialog, which) -> {
        });
        builder.setNegativeButton(Html.fromHtml(falseButton2), (dialog, which) -> {
//            dispatchTakePictureIntent(); // Метод который запускает камеру и создаёт файл фото.
            dispatchTakePictureIntent(); // Метод который запускает камеру и создаёт файл фото.
        });
        builder.create().show();
    }


    /**
     * 27.08.2020
     * Создание фото и пути к ней
     */
    public static void dispatchTakePictureIntent() {
        globals.writeToMLOG( "MakePhoto.dispatchTakePictureIntent: " + "ENTER" + "\n");
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPG_" + timeStamp + "_";

                File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);  // Для прилы
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
                try {
                    contentUri = FileProvider.getUriForFile(mContext, "ua.com.merchik.merchik.provider", image);
                    globals.writeToMLOG( " MakePhoto.class.Type1.Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT + "\n");
                } catch (Exception e) {
                    contentUri = Uri.fromFile(image);
                    globals.writeToMLOG( " MakePhoto.class.Type2.Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT + "\n");
                }


                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                globals.writeToMLOG( "MakePhoto.startActivityForResult: " + "ENTER" + "\n");
                ((DetailedReportActivity) mContext).startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }

        } catch (Exception e) {
            globals.alertDialogMsg(mContext, "Ошибка при создании фото: " + e);
            globals.writeToMLOG( "MakePhoto.dispatchTakePictureIntent.Error: " + Arrays.toString(e.getStackTrace()) + "\n");
        }
    }


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
     * 04.11.2021
     */
    private Context cameraContext;

    public void setCameraContext(Context context) {
        cameraContext = context;
    }

    /**
     * 04.11.2021
     */
    public static String openCameraPhotoUri;

    public void openCamera(Activity activity, int requestCode) {
        globals.writeToMLOG( "MakePhoto.openCamera: " + "ENTER" + "\n");
        try {
            File photo = null;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            Globals.writeToMLOG("INFO", "MakePhoto.openCamera.Intent", "intent: " + intent);
            Globals.writeToMLOG("INFO", "MakePhoto.openCamera.Activity", "activity: " + activity);
            Globals.writeToMLOG("INFO", "MakePhoto.openCamera.Activity", "activity.getPackageManager(): " + activity.getPackageManager());
            Globals.writeToMLOG("INFO", "MakePhoto.openCamera.Intent", "intent.resolveActivity(activity.getPackageManager()): " + intent.resolveActivity(activity.getPackageManager()));

            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPG_" + timeStamp + "_";

                File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);  // Для прилы
                try {
                    photo = File.createTempFile(
                            imageFileName,
                            ".jpg",
                            storageDir
                    );
                } catch (IOException e) {
                    globals.alertDialogMsg(activity, "Ошибка при создании фото: " + e);
                    e.printStackTrace();
                    return;
                }

                Uri contentUri;
                try {
                    contentUri = FileProvider.getUriForFile(activity, "ua.com.merchik.merchik.provider", photo);
                    globals.writeToMLOG( " MakePhoto.class.Type1.Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT + "\n");
                } catch (Exception e) {
                    contentUri = Uri.fromFile(photo);
                    globals.writeToMLOG( " MakePhoto.class.Type2.Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT + "\n");
                }

                MakePhoto.openCameraPhotoUri = photo.getAbsolutePath();

                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                intent.putExtra("photo_uri", contentUri);
                globals.writeToMLOG( "MakePhoto.startActivityForResult: " + "ENTER" + "\n");
                activity.startActivityForResult(intent, requestCode);
            }

        } catch (Exception e) {
            globals.alertDialogMsg(activity, "Ошибка при создании фото: " + e);
            globals.writeToMLOG( "MakePhoto.dispatchTakePictureIntent.Error: " + Arrays.toString(e.getStackTrace()) + "\n");
        }
    }


    /**
     * Эксперемент с выполнением фото и моментальным его сохранением в БД
     */
    public static String photoNum; // URI фотографии
    //    public static String codeIza; // URI фотографии
    public static Long dt;
    public static String photoType = "0";
    public static String tovarId = "";
    public static String photoCustomerGroup = "";
    public static String img_src_id = "";
    public static String showcase_id = "";
    public static String planogram_id = "";
    public static String planogram_img_id = "";
    public static String example_id = "";
    public static String example_img_id = "";

    public <T> void makePhoto(Activity activity, T data, Clicks.clickVoid clickVoid) {
        try {
            final WorkPlan workPlan = new WorkPlan();
            WPDataObj wpDataObj;

            if (data instanceof TasksAndReclamationsSDB) {
                wpDataObj = workPlan.getKPS((TasksAndReclamationsSDB) data);
            } else {
                WpDataDB wpDataDB = (WpDataDB) data;
                wpDataObj = workPlan.getKPS(wpDataDB.getId());
                dt = wpDataDB.getDt().getTime() / 1000;
            }
            wpDataObj.setPhotoType(photoType);
            wpDataObj.setCustomerTypeGrpS(photoCustomerGroup);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File photoFile = createPhotoFile(activity);
            Uri uri = getPhotoUri(activity, photoFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            photoNum = photoFile.getAbsolutePath();

            boolean isSavePhoto = PhotoReportActivity.savePhoto(activity, wpDataObj, photoFile, clickVoid);

            Globals.writeToMLOG("INFO", "makePhoto", "photoType: " + photoType);
            Globals.writeToMLOG("INFO", "makePhoto", "photoNum: " + photoNum);
            Globals.writeToMLOG("INFO", "makePhoto", "isSavePhoto: " + isSavePhoto);

            Log.e("!!!!!!", "tovarId: " + tovarId);
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                Globals.writeToMLOG("INFO", "makePhoto", "resolveActivity != null: " + activity.getPackageManager());
                activity.startActivityForResult(intent, CAMERA_REQUEST_TAKE_PHOTO_TEST);
            } else {
                Globals.writeToMLOG("INFO", "makePhoto", "resolveActivity = null: " + activity.getPackageManager());
                activity.startActivityForResult(intent, CAMERA_REQUEST_TAKE_PHOTO_TEST);
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "makePhoto", "Error msg(Exception e): " + e);
            Toast.makeText(activity, "Ошибка при выполнении фото: " + e, Toast.LENGTH_LONG).show();
        }
    }

    private File createPhotoFile(Activity activity) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String imageFileName = "CAMERA_PHOTO_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);  // Для прилы
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private Uri getPhotoUri(Activity activity, File file) {
        Uri contentUri;
        try {
            contentUri = FileProvider.getUriForFile(activity, "ua.com.merchik.merchik.provider", file);
        } catch (Exception e) {
            contentUri = Uri.fromFile(file);
        }
        return contentUri;
    }


    public <T> void pressedMakePhoto(Activity activity, T data, OptionsDB optionsDB, String photoType) {
        try {
            int userId = 0;
            final WorkPlan workPlan = new WorkPlan();
            WPDataObj wpDataObj;
            if (data instanceof TasksAndReclamationsSDB) {
                wpDataObj = workPlan.getKPS((TasksAndReclamationsSDB) data);
                userId = ((TasksAndReclamationsSDB) data).vinovnik;
            } else {
                WpDataDB wpDataDB = (WpDataDB) data;
                wpDataObj = workPlan.getKPS(wpDataDB.getId());
                userId = wpDataDB.getUser_id();
            }
            MakePhoto.photoType = photoType;
            Globals.writeToMLOG("INFO", "pressedMakePhoto", "photoType: " + photoType);


            if (AppUserRealm.getAppUserById(userId).user_work_plan_status.equals("our")) {
                // Тут должна открываться инфа про Витрины
                showDialogSW(activity, wpDataObj, data, optionsDB);
            } else {
//                photoDialogsNEW(activity, wpDataObj, data, optionsDB);
                choiceCustomerGroupAndPhoto2(activity, wpDataObj, data, optionsDB, () -> {
                });
            }

//            choiceCustomerGroupAndPhoto2(activity, wpDataObj, data, optionsDB);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "pressedMakePhoto", "Exception e: " + e);
        }
    }

    public <T> void pressedMakePhoto(Activity activity, T data, OptionsDB optionsDB, String photoType, String tovarId, Clicks.clickVoid click) {
        try {
            final WorkPlan workPlan = new WorkPlan();
            WPDataObj wpDataObj;
            if (data instanceof TasksAndReclamationsSDB) {
                wpDataObj = workPlan.getKPS((TasksAndReclamationsSDB) data);
            } else {
                WpDataDB wpDataDB = (WpDataDB) data;
                wpDataObj = workPlan.getKPS(wpDataDB.getId());
                wpDataObj.setPhotoType(photoType);
            }
            MakePhoto.photoType = photoType;
            MakePhoto.tovarId = tovarId;
            Globals.writeToMLOG("INFO", "pressedMakePhoto", "photoType: " + photoType);
            choiceCustomerGroupAndPhoto2(activity, wpDataObj, data, optionsDB, click);
            click.click();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "pressedMakePhoto", "Exception e: " + e);
        }
    }

    public <T> void pressedMakePhotoOldStyle(Activity activity, WPDataObj wp, T data, OptionsDB optionsDB) {
        photoType = wp.getPhotoType();
        choiceCustomerGroupAndPhoto2(activity, wp, data, optionsDB, () -> {
        });
    }

    public <T> void pressedMakePhotoOldStyle(Activity activity, WPDataObj wp, T data, OptionsDB optionsDB, StackPhotoDB stackPhoto) {
        photoType = wp.getPhotoType();

        choiceCustomerGroupAndPhoto2(activity, wp, data, optionsDB, () -> {
        });
    }

    // TODO ## убрать эту группу
    private <T> void choiceCustomerGroupAndPhoto2(Activity activity, WPDataObj wp, T data, OptionsDB optionsDB, Clicks.clickVoid clickVoid) {
        if (wp.getCustomerTypeGrp() != null) {
            final String[] result = wp.getCustomerTypeGrp().values().toArray(new String[0]);
            if (wp.getCustomerTypeGrp().size() > 1 && !wp.getPhotoType().equals("5")
                    && !wp.getPhotoType().equals("47")) {
                new AlertDialog.Builder(activity)
                        .setTitle("Выберите группу товара для следующего фото: ")
                        .setItems(result, (dialog, which) -> {
                            Toast t = Toast.makeText(activity, "Выбрана группа товара: " + result[which], Toast.LENGTH_LONG);
                            t.show();
                            wp.setCustomerTypeGrpS(Globals.getKeyForValue(result[which], wp.getCustomerTypeGrp()));

                            Log.e("choiceCustomerGroup", "Globals.getKeyForValue(result[which]: " + Globals.getKeyForValue(result[which], wp.getCustomerTypeGrp()));
                            Log.e("choiceCustomerGroup", "result[which]: " + result[which]);
                            Log.e("choiceCustomerGroup", "wp.getCustomerTypeGrp(): " + wp.getCustomerTypeGrp());
                            Log.e("choiceCustomerGroup", "which: " + which);

                            /*07.07.23. Возможно в будущем изза этой Группы Товаров будут проблемы.*/
                            photoCustomerGroup = Globals.getKeyForValue(result[which], wp.getCustomerTypeGrp());

//                            photoDialogs(activity, wp, data, optionsDB);
                            photoDialogsNEW(activity, wp, data, optionsDB, clickVoid);
                        })
                        .show();
            } else if (wp.getCustomerTypeGrp().size() == 1 && !wp.getPhotoType().equals("5")) {
                wp.setCustomerTypeGrpS(Globals.getKeyForValue(result[0], wp.getCustomerTypeGrp()));
                Toast.makeText(activity, "Выбрана группа товара: " + result[0], Toast.LENGTH_LONG).show();
//                photoDialogs(activity, wp, data, optionsDB);
                photoDialogsNEW(activity, wp, data, optionsDB, clickVoid);
            } else {
                if (!wp.getPhotoType().equals("5") && !wp.getPhotoType().equals("47")) {
                    globals.alertDialogMsg(activity, "Не обнаружено ни одной группы товаров по данному клиенту. Сообщите об этом Администратору!");
                }
                wp.setCustomerTypeGrpS("");
//                photoDialogs(activity, wp, data, optionsDB);
                photoDialogsNEW(activity, wp, data, optionsDB, clickVoid);
            }
        } else {
            globals.alertDialogMsg(activity, "Не выбрано посещение\n\nЗайдите в раздел План работ, выберите посещение и повторите попытку.");
        }
    }

//    private <T> void photoDialogsNEW2(Activity activity, WPDataObj wpDataObj, T data, Clicks.clickVoid clickVoid) {
//        makePhoto(activity, data, clickVoid); // Метод который запускает камеру и создаёт файл фото.
//    }

    /**
     * 28.08.23
     */
    boolean isPhotoMake = true;
    private <T> void photoDialogsNEW(Activity activity, WPDataObj wpDataObj, T data, OptionsDB optionsDB, Clicks.clickVoid clickVoid) {
        OptionControlMP optionControlMP = new OptionControlMP(activity.getBaseContext(), (WpDataDB) data, optionsDB, null, null, null);
        isPhotoMake = true;
        optionControlMP.showMassage(false, new Clicks.clickStatusMsg() {
            @Override
            public void onSuccess(String string) {
                if (isPhotoMake) {
                    makePhoto(activity, data, clickVoid); // Метод который запускает камеру и создаёт файл фото.
                    isPhotoMake = false;
                }
            }

            @Override
            public void onFailure(String error) {
                String settext2 = "Система не обнаружила вас в ТТ. \n\nФотографии выполненные в этом режиме могут быть признаны не действительными.\n\nОтказаться от изготовления фото?";

                DialogData dialog1 = new DialogData(activity);
                dialog1.setTitle("Нарушение по Местоположению.");
                dialog1.setText(error);
                dialog1.setDialogIco();
                dialog1.setOk(Html.fromHtml("<font color='#000000'>Всё равно сделать фото</font>"), () -> {
                    DialogData dialogData2 = new DialogData(activity);
                    dialogData2.setTitle("ВНИМАНИЕ!");
                    dialogData2.setText(settext2);
                    dialogData2.setDialogIco();
                    dialogData2.setOk(Html.fromHtml("<font color='#000000'>Да</font>"), dialogData2::dismiss);
                    dialogData2.setCancel(Html.fromHtml("<font color='#000000'>Нет</font>"), () -> {
                        dialog1.dismiss();
                        dialogData2.dismiss();
                        showDialogPass(activity, wpDataObj, optionsDB, () -> {
                            makePhoto(activity, data, clickVoid); // Метод который запускает камеру и создаёт файл фото.
                        });
                    });
                    dialogData2.setClose(dialogData2::dismiss);
                    dialogData2.show();
                });
                dialog1.setCancel2(Html.fromHtml("<font color='#000000'>Отказаться от изготовления фото</font>"), dialog1::dismiss);

                dialog1.setImgBtnCall(activity);
                dialog1.setClose(dialog1::dismiss);
                dialog1.show();
            }
        });
    }

/*
    private <T> void photoDialogs(Activity activity, WPDataObj wpDataObj, T data, OptionsDB optionsDB) {
        if (enabledGPS) {
            if (wpDataObj != null) {
                if (wpDataObj.getLatitude() > 0 && wpDataObj.getLongitude() > 0) {
                    ua.com.merchik.merchik.trecker.Coordinates((WpDataDB) data);
                    if (Globals.CoordX != 0 && Globals.CoordY != 0) {
                        double d = ua.com.merchik.merchik.trecker.coordinatesDistanse(wpDataObj.getLatitude(), wpDataObj.getLongitude(), Globals.CoordX, Globals.CoordY);
                        if (d > 500) {
                            String settext1 = String.format("По данным системы вы находитесь на расстоянии %s метров от ТТ %s, что больше допустимых 500 метров.\n\nВы не сможете выполнить фото пока, Ваше местоположение не определено.\n\nЕсли в действительности Вы находитесь в ТТ - обратитесь за помощью к своему руководителю или в службу поддержки merchik.", (int) d, wpDataObj.getAddressIdTxt());
                            String settext2 = "Система не обнаружила вас в ТТ. \n\nФотографии выполненные в этом режиме могут быть признаны не действительными.\n\nОтказаться от изготовления фото?";

                            DialogData dialog1 = new DialogData(activity);
                            dialog1.setTitle("Нарушение по Местоположению.");
                            dialog1.setText(settext1);
                            dialog1.setDialogIco();
                            dialog1.setOk(Html.fromHtml("<font color='#000000'>Всё равно сделать фото</font>"), () -> {
                                DialogData dialogData2 = new DialogData(activity);
                                dialogData2.setTitle("ВНИМАНИЕ!");
                                dialogData2.setText(settext2);
                                dialogData2.setDialogIco();
                                dialogData2.setOk(Html.fromHtml("<font color='#000000'>Да</font>"), dialogData2::dismiss);
                                dialogData2.setCancel(Html.fromHtml("<font color='#000000'>Нет</font>"), () -> {
                                    dialog1.dismiss();
                                    dialogData2.dismiss();
                                    showDialogPass(activity, wpDataObj, optionsDB, () -> {
                                        makePhoto(activity, data, () -> {
                                        }); // Метод который запускает камеру и создаёт файл фото.
                                    });
                                });
                                dialogData2.setClose(dialogData2::dismiss);
                                dialogData2.show();
                            });
                            dialog1.setCancel2(Html.fromHtml("<font color='#000000'>Отказаться от изготовления фото</font>"), dialog1::dismiss);

                            dialog1.setImgBtnCall(activity);
                            dialog1.setClose(dialog1::dismiss);
                            dialog1.show();

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
                            String bt1 = "<font color='#000000'>Всё равно сделать фото</font>";
                            String bf1 = "<font color='#000000'>Отказаться от изготовления фото</font>";
                            String t2 = "ВНИМАНИЕ!";
                            String m2 = "Фотографии выполненные в этом режиме могут быть признаны не действительными.\n\nОтказаться от изготовления фото?";
                            String bt2 = "<font color='#000000'>Да</font>";
                            String bf2 = "<font color='#000000'>Нет</font>";

                            alertMassageMP(activity, 1, t1, m1, bt1, bf1, t2, m2, bt2, bf2);
                        } else {
                            makePhoto(activity, data, () -> {
                            }); // Метод который запускает камеру и создаёт файл фото.
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
                        String bf1 = "Ок";

                        alertMassageMP(activity, 2, t1, m1, bt1, bf1, "", "", "", "");
//                            makePhoto(activity, data); // Метод который запускает камеру и создаёт файл фото.
                    }
                } else {
                    Toast.makeText(activity, "Координаты магазина не обнаружены. Выполнение фото невозможно, обратитесь к Вашему руководителю.", Toast.LENGTH_SHORT).show();
                    makePhoto(activity, data, () -> {
                    }); // Метод который запускает камеру и создаёт файл фото.
                }
            } else {
                Toast.makeText(activity.getBaseContext(), "Не обнаружены данные посещения, обратитесь к Вашему руководителю.", Toast.LENGTH_SHORT).show();
            }
        } else {
            String title = "Нет сигнала GPS";
            String msg = "Не могу определить Ваше местоположение. Возможно GPS выключен или Вы его только недавно включили.\n\n" +
                    "Вы не сможете выполнить фото пока, Ваше местоположение не определено.\n\n" +
                    "Включите GPS (для этого перейдите []), подождите 10 секунд и повторите попытку сделать фото.\n\n" +
                    "Если в действительности у Вас включён GPS - обратитесь за помощью к своему руководителю или в службу поддержки merchik.";
            String trueButton = "<font color='#000000'>У меня всё работает</font>";
            String falseButton = "<font color='#000000'>Закрыть сообщение</font>";
            String title2 = "ВНИМАНИЕ!";
            String msg2 = "Система не обнаружила GPS. \n\n" +
                    "Фотографии выполненные в этом режиме могут быть признаны не действительными.\n\n" +
                    "Отказаться от изготовления фото?";
            String trueButton2 = "<font color='#000000'>Да</font>";
            String falseButton2 = "<font color='#000000'>Нет</font>";


            DialogData dialogData1 = new DialogData(activity);
            dialogData1.setTitle(title);
            dialogData1.setText(msg);
            dialogData1.setDialogIco();
            dialogData1.setOk(Html.fromHtml(trueButton), () -> {
                DialogData dialogData2 = new DialogData(activity);
                dialogData2.setTitle(title2);
                dialogData2.setText(msg2);
                dialogData2.setDialogIco();
                dialogData2.setOk(Html.fromHtml(trueButton2), dialogData2::dismiss);
                dialogData2.setCancel2(Html.fromHtml(falseButton2), () -> {
                    dialogData1.dismiss();
                    dialogData2.dismiss();
                    showDialogPass(activity, wpDataObj, optionsDB, () -> {
                        makePhoto(activity, data, () -> {
                        }); // Метод который запускает камеру и создаёт файл фото.
                    });
                });
                dialogData2.setClose(dialogData2::dismiss);
                dialogData2.show();
            });
            dialogData1.setCancel2(Html.fromHtml(falseButton), dialogData1::dismiss);
            dialogData1.setClose(dialogData1::dismiss);
            dialogData1.setImgBtnCall(activity);
            dialogData1.show();
        }
    }
*/

    public void showDialogPass(Context context, WPDataObj wpDataObj, OptionsDB option, Clicks.clickVoid click) {

        WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(wpDataObj.dad2);
//        Log.e("!!!!!!!!!","wpDataObj -> user_comment: " + wpDataObj.);

        DialogData dialog = new DialogData(context);
        dialog.setTitle("Внесіть пароль!");
        dialog.setText("Для продовження внесіть пароль: ");
        dialog.setClose(dialog::dismiss);
        dialog.setOperation(DialogData.Operations.TEXT, "", null, () -> {
        });
        dialog.setOkNotClose("Ok", () -> {
//            Toast.makeText(dialog.context, "Внесли: " + dialog.getOperationResult(), Toast.LENGTH_SHORT).show();
            String res = dialog.getOperationResult();

            long date = wp.getDt().getTime() / 1000;
            UsersSDB user = SQL_DB.usersDao().getUserById(wp.getUser_id());
            long dad2 = wp.getCode_dad2();


            Log.e("UnlockCode", "date: " + Clock.getHumanTimeYYYYMMDD(date));
            Log.e("UnlockCode", "user: " + user.id);
            Log.e("UnlockCode", "dad2: " + dad2);
//            Log.e("UnlockCode", "option: " + option.getOptionId());

            String unlockCode = new UnlockCode().unlockCode(date, user, dad2, option, CODE_DAD_2_AND_OPTION);
            String unlockCode2 = new UnlockCode().unlockCode(date, user, dad2, option, DATE_AND_USER);

            Log.e("UnlockCode", "unlockCode: " + unlockCode);
            Log.e("UnlockCode", "unlockCode2: " + unlockCode2);
            Log.e("!!!!!!!!!", "wp -> user_comment: " + wp.user_comment);

            if (unlockCode.equals(res)) {
                int tema_id = 1285;
                String client_id = wp.getClient_id(); // код клиента из плана работ
                Date wpDate = wp.getDt(); // дата работ из плана работ
                int addr_id = wp.getAddr_id(); // код адреса из плана работ
                int user_id = wp.getUser_id(); // код сотрудника из плана работ

                String opt_id = option.getOptionId(); // код опции в виде строки
                int len = opt_id.length();
                String dad2str = String.valueOf(dad2); // код ДАД2 из плана работ в виде строки

                String kodObstr = "1" + opt_id.substring(len - 3, len) + dad2str.substring(1, 5) + dad2str.substring(6, 7) + dad2str.substring(8, 13) + dad2str.substring(14, 19);
                long kodOb = Long.parseLong(kodObstr);

                Toast.makeText(context, "Код прийнято", Toast.LENGTH_LONG).show();
                RealmManager.setRowToLog(Collections.singletonList(
                        new LogDB(
                                RealmManager.getLastIdLogDB() + 1,
                                System.currentTimeMillis() / 1000,
                                "використання коду розблокування " + res,
                                tema_id,
                                client_id,
                                addr_id,
                                kodOb,
                                user_id,
                                null,
                                Globals.session,
                                String.valueOf(wpDate))));

                click.click();
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Код не вірний!", Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
    }


    /**
     * 24.07.23.
     * Открытие модального окна для выбора Витрин
     *
     * @param activity
     */
    public <T> void showDialogSW(Activity activity, WPDataObj wp, T dataT, OptionsDB optionsDB) {
        DialogShowcase dialog = new DialogShowcase(activity);
        dialog.wpDataDB = (WpDataDB) dataT;
        dialog.photoType = Integer.valueOf(MakePhoto.photoType);
        dialog.populateDialogData(new Clicks.click() {
            @Override
            public <T> void click(T data) {
                try {
                    /*
                    25.04.25
                    Добавил обновление, теперь учитываются планограммы из таблицы planogram_vizit_showcase,
                    если их нет оставил старый дизайн
                     */
                    ShowcaseSDB showcase = (ShowcaseSDB) data;
                    Toast.makeText(activity, "Обрана вітрина: " + showcase.nm + " (" + showcase.id + ")", Toast.LENGTH_LONG).show();
                    PlanogrammSDB planogrammSDB = null;
                    PlanogrammVizitShowcaseSDB planogrammVizitShowcaseSDB = null;

                    try {
                        // Проверяем photoId и id
                        MakePhoto.img_src_id = showcase.photoId != null ? String.valueOf(showcase.photoId) : "";
                        MakePhoto.showcase_id = showcase.id != null ? String.valueOf(showcase.id) : "";
                        MakePhoto.example_img_id = showcase.photoId != null ? String.valueOf(showcase.photoId) : "";

                        List<PlanogrammVizitShowcaseSDB> planogrammVizitShowcaseSDBList = SQL_DB
                                .planogrammVizitShowcaseDao()
                                .getByCodeDad2(wp.dad2);
                        for (PlanogrammVizitShowcaseSDB item : planogrammVizitShowcaseSDBList) {
                            if (item != null && Objects.equals(item.showcase_id, showcase.id)) {
                                planogrammVizitShowcaseSDB = item;
                                break;
                            }
                        }
                        if (planogrammVizitShowcaseSDB != null) {
                            MakePhoto.planogram_id = planogrammVizitShowcaseSDB.planogram_id != null
                                    ? String.valueOf(planogrammVizitShowcaseSDB.planogram_id) : "";
                            MakePhoto.planogram_img_id = planogrammVizitShowcaseSDB.planogram_photo_id != null
                                    ? String.valueOf(planogrammVizitShowcaseSDB.planogram_photo_id) : "";
                        } else {
                            MakePhoto.planogram_id = showcase.planogramId != null
                                    ? String.valueOf(showcase.planogramId) : "";
                            planogrammSDB = SQL_DB.planogrammDao().getById(showcase.planogramId);
                            MakePhoto.planogram_img_id = planogrammSDB != null && planogrammSDB.photoId != null
                                    ? String.valueOf(planogrammSDB.photoId) : "";
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "showDialogSW/click/showcase", "Exception e: " + e);
                    }

                    boolean needPlan;
                    if (planogrammSDB != null) {
                        needPlan = true;
                    } else {
                        needPlan = dialog.photoType == 0;
                    }
                    if (planogrammVizitShowcaseSDB != null)
                        needPlan = false;

                    if (showcase.tovarGrp != null && showcase.tovarGrp > 0) {
                        wp.setCustomerTypeGrpS(String.valueOf(showcase.tovarGrp));
                        Toast.makeText(activity, "Обрана група товару: " + showcase.tovarGrpTxt, Toast.LENGTH_LONG).show();

                        if (needPlan) {
                            showDialogPlanogramm(activity, wp, dataT, optionsDB, () -> {
                                photoDialogsNEW(activity, wp, dataT, optionsDB, () -> {
                                });
                            });
                        } else {
                            photoDialogsNEW(activity, wp, dataT, optionsDB, () -> {
                            });
                        }

                    } else {
                        if (needPlan) {
                            showDialogPlanogramm(activity, wp, dataT, optionsDB, () -> {
                                choiceCustomerGroupAndPhoto2(activity, wp, dataT, optionsDB, () -> {
                                });
                            });
                        } else {
                            choiceCustomerGroupAndPhoto2(activity, wp, dataT, optionsDB, () -> {
                            });
                        }
                    }

                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("", "Exception e: " + e);
                    Globals.writeToMLOG("ERROR", "showDialogSW/click", "Exception e: " + e);
                }
            }
        });
        dialog.setClose(dialog::dismiss);
        dialog.show();
    }

    /**
     * 08.05.25.
     * Открытие модального окна для выбора Планограм
     *
     * @param activity
     */
    public <T> void showDialogPlanogramm(Activity activity, WPDataObj wp, T dataT, OptionsDB optionsDB, Clicks.clickVoid clickVoid) {
        DialogShowcase dialog = new DialogShowcase(activity);
        dialog.setCurrTitle("Оберіть планограму по котрій будете викладати товар");
        dialog.wpDataDB = (WpDataDB) dataT;
        dialog.photoType = Integer.valueOf(MakePhoto.photoType);
        dialog.populateDialogDataPlanogramm(new Clicks.click() {
            @Override
            public <T> void click(T data) {
                try {
                    PlanogrammJOINSDB planogramm = (PlanogrammJOINSDB) data;
                    Toast.makeText(activity, "Обрана планограма: " + planogramm.planogrammName + " (" + planogramm.id + ")", Toast.LENGTH_LONG).show();

                    MakePhoto.planogram_id = String.valueOf(planogramm.id);
                    MakePhoto.planogram_img_id = String.valueOf(planogramm.planogrammPhotoId);

//                    choiceCustomerGroupAndPhoto2(activity, wp, dataT, optionsDB, () -> {
//                    });

                    clickVoid.click();

                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("", "Exception e: " + e);
                }
            }
        });
        dialog.setClose(dialog::dismiss);
        dialog.show();
    }
}
