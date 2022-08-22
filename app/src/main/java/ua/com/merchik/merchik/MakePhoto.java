package ua.com.merchik.merchik;

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
import java.util.Date;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class MakePhoto {

    static Globals globals = new Globals();
    private static final int CAMERA_REQUEST = 1;
    private static final int REQUEST_TAKE_PHOTO = 101;
    private static final int CAMERA_REQUEST_TAKE_PHOTO = 200;   // Для нового интента выполнения фото
    private static final int CAMERA_REQUEST_TAKE_PHOTO_TEST = 201;   // Тестовый реквест для фото
    public static File image;
    private static Context mContext;
    public static WPDataObj wp;
    public static Uri contentUri;


    public static void startToMakePhoto(Context context, WPDataObj wpDataObj) {
        globals.writeToMLOG(Clock.getHumanTime() + "MakePhoto.startToMakePhoto: " + "ENTER" + "\n");
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
    private static void choiceCustomerGroupAndPhoto() {
        if (wp.getCustomerTypeGrp() != null) {
            final String[] result = wp.getCustomerTypeGrp().values().toArray(new String[0]);
            if (wp.getCustomerTypeGrp().size() > 1 && !wp.getPhotoType().equals("5")) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Выберите группу товара для следующего фото: ")
                        .setItems(result, (dialog, which) -> {
                            Toast t = Toast.makeText(mContext, "Выбрана группа товара: " + result[which], Toast.LENGTH_LONG);
                            t.show();
                            wp.setCustomerTypeGrpS(globals.getKeyForValue(result[which], wp.getCustomerTypeGrp()));
                            takePhoto();
                        })
                        .show();
            } else if (wp.getCustomerTypeGrp().size() == 1 && !wp.getPhotoType().equals("5")) {
                wp.setCustomerTypeGrpS(globals.getKeyForValue(result[0], wp.getCustomerTypeGrp()));
                Toast.makeText(mContext, "Выбрана группа товара: " + result[0], Toast.LENGTH_LONG).show();
                takePhoto();
            } else {
                if (!wp.getPhotoType().equals("5")){
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
    private static void takePhoto() {
        try {
            Log.e("takePhoto", "takePhoto1: " + ua.com.merchik.merchik.trecker.enabledGPS);
            if (ua.com.merchik.merchik.trecker.enabledGPS) {
                if (wp != null) {
                    if (wp.getLatitude() > 0 && wp.getLongitude() > 0) {
//                    if (true){
                        if (Globals.CoordX != 0 && Globals.CoordY != 0) {

                            Log.e("takePhoto", "takePhoto2: " + wp.getLatitude() + "/" + wp.getLongitude());
                            Log.e("takePhoto", "takePhoto3: " + Globals.CoordX + "/" + Globals.CoordX);


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
        globals.writeToMLOG(Clock.getHumanTime() + "MakePhoto.dispatchTakePictureIntent: " + "ENTER" + "\n");
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
                    globals.writeToMLOG(Clock.getHumanTime() + " MakePhoto.class.Type1.Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT + "\n");
                } catch (Exception e) {
                    contentUri = Uri.fromFile(image);
                    globals.writeToMLOG(Clock.getHumanTime() + " MakePhoto.class.Type2.Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT + "\n");
                }


                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                globals.writeToMLOG(Clock.getHumanTime() + "MakePhoto.startActivityForResult: " + "ENTER" + "\n");
                ((DetailedReportActivity) mContext).startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }

        } catch (Exception e) {
            globals.alertDialogMsg(mContext, "Ошибка при создании фото: " + e);
            globals.writeToMLOG(Clock.getHumanTime() + "MakePhoto.dispatchTakePictureIntent.Error: " + Arrays.toString(e.getStackTrace()) + "\n");
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

    public void openCamera(Activity activity) {
        globals.writeToMLOG(Clock.getHumanTime() + "MakePhoto.openCamera: " + "ENTER" + "\n");
        try {
            File photo = null;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

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
                    globals.writeToMLOG(Clock.getHumanTime() + " MakePhoto.class.Type1.Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT + "\n");
                } catch (Exception e) {
                    contentUri = Uri.fromFile(photo);
                    globals.writeToMLOG(Clock.getHumanTime() + " MakePhoto.class.Type2.Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT + "\n");
                }

                MakePhoto.openCameraPhotoUri = photo.getAbsolutePath();

                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
                intent.putExtra("photo_uri", contentUri);
                globals.writeToMLOG(Clock.getHumanTime() + "MakePhoto.startActivityForResult: " + "ENTER" + "\n");
                activity.startActivityForResult(intent, CAMERA_REQUEST_TAKE_PHOTO);
            }

        } catch (Exception e) {
            globals.alertDialogMsg(activity, "Ошибка при создании фото: " + e);
            globals.writeToMLOG(Clock.getHumanTime() + "MakePhoto.dispatchTakePictureIntent.Error: " + Arrays.toString(e.getStackTrace()) + "\n");
        }
    }


    /**
     * Эксперемент с выполнением фото и моментальным его сохранением в БД
     */
    public static String photoNum; // URI фотографии
    public static String photoType;
    public <T> void makePhoto(Activity activity, T data) {
        try {
            final WorkPlan workPlan = new WorkPlan();
            WPDataObj wpDataObj;

            if (data instanceof TasksAndReclamationsSDB){
                wpDataObj = workPlan.getKPS((TasksAndReclamationsSDB) data);
            }else {
                WpDataDB wpDataDB = (WpDataDB) data;
                wpDataObj = workPlan.getKPS(wpDataDB.getId());
            }

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File photoFile = createPhotoFile(activity);
            Uri uri = getPhotoUri(activity, photoFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            photoNum = photoFile.getAbsolutePath();
            PhotoReportActivity.savePhoto(activity, wpDataObj, photoFile);

            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(intent, CAMERA_REQUEST_TAKE_PHOTO_TEST);
            }else {
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


    public <T> void pressedMakePhoto(Activity activity, T data) {
        try {
            final WorkPlan workPlan = new WorkPlan();
            WPDataObj wpDataObj;
            if (data instanceof TasksAndReclamationsSDB){
                wpDataObj = workPlan.getKPS((TasksAndReclamationsSDB) data);
            }else {
                WpDataDB wpDataDB = (WpDataDB) data;
                wpDataObj = workPlan.getKPS(wpDataDB.getId());
            }
            photoType = "0";
            choiceCustomerGroupAndPhoto2(activity, wpDataObj, data);


        } catch (Exception e) {
            Toast.makeText(activity, "Ошибка при проверке состояния GPS. Повторите попытку или обратитесь к Вашему руководителю. Ошибка: " + e, Toast.LENGTH_LONG).show();
        }
    }

    public <T> void pressedMakePhotoOldStyle(Activity activity, WPDataObj wp, T data){
        photoType = wp.getPhotoType();
        choiceCustomerGroupAndPhoto2(activity, wp, data);
    }

    private <T> void choiceCustomerGroupAndPhoto2(Activity activity, WPDataObj wp, T data) {
        if (wp.getCustomerTypeGrp() != null) {
            final String[] result = wp.getCustomerTypeGrp().values().toArray(new String[0]);
            if (wp.getCustomerTypeGrp().size() > 1 && !wp.getPhotoType().equals("5")) {
                new AlertDialog.Builder(activity)
                        .setTitle("Выберите группу товара для следующего фото: ")
                        .setItems(result, (dialog, which) -> {
                            Toast t = Toast.makeText(activity, "Выбрана группа товара: " + result[which], Toast.LENGTH_LONG);
                            t.show();
                            wp.setCustomerTypeGrpS(globals.getKeyForValue(result[which], wp.getCustomerTypeGrp()));
                            photoDialogs(activity, wp, data);
                        })
                        .show();
            } else if (wp.getCustomerTypeGrp().size() == 1 && !wp.getPhotoType().equals("5")) {
                wp.setCustomerTypeGrpS(globals.getKeyForValue(result[0], wp.getCustomerTypeGrp()));
                Toast.makeText(activity, "Выбрана группа товара: " + result[0], Toast.LENGTH_LONG).show();
                photoDialogs(activity, wp, data);
            } else {
                if (!wp.getPhotoType().equals("5")){
                    globals.alertDialogMsg(activity, "Не обнаружено ни одной группы товаров по данному клиенту. Сообщите об этом Администратору!");
                }
                wp.setCustomerTypeGrpS("");
                photoDialogs(activity, wp, data);
            }
        } else {
            globals.alertDialogMsg(activity, "Не выбрано посещение\n\nЗайдите в раздел План работ, выберите посещение и повторите попытку.");
        }
    }

    private <T> void photoDialogs(Activity activity, WPDataObj wpDataObj, T data){
        if (ua.com.merchik.merchik.trecker.enabledGPS) {
            if (wpDataObj != null) {
                if (wpDataObj.getLatitude() > 0 && wpDataObj.getLongitude() > 0) {
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
                                    makePhoto(activity, data); // Метод который запускает камеру и создаёт файл фото.
                                });
                                dialogData2.setClose(dialogData2::dismiss);
                                dialogData2.show();
                            });
                            dialog1.setCancel2(Html.fromHtml("<font color='#000000'>Отказаться от изготовления фото</font>"), dialog1::dismiss);

                            dialog1.setImgBtnCall(activity);
                            dialog1.setClose(dialog1::dismiss);
                            dialog1.show();


                            String title = "Нарушение по Местоположению.";
                            String msg = String.format("По данным системы вы находитесь на расстоянии %.1f метров от ТТ %s, что больше допустимых 500 метров.\n\nВы не сможете использовать фото которые выполните в таком состоянии системы.\n\nЕсли в действительности Вы находитесь в ТТ - обратитесь к своему руководителю за помощью.", d, wpDataObj.getAddressIdTxt());
                            String trueButton = "<font color='#000000'>Всё равно сделать фото</font>";
                            String falseButton = "<font color='#000000'>Отказаться от изготовления фото</font>";
                            String title2 = "ВНИМАНИЕ!";
                            String msg2 = "Система не обнаружила вас в ТТ. \n\nФотографии выполненные в этом режиме могут быть признаны не действительными.\n\nОтказаться от изготовления фото?";
                            String trueButton2 = "<font color='#000000'>Да</font>";
                            String falseButton2 = "<font color='#000000'>Нет</font>";

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
                            makePhoto(activity, data); // Метод который запускает камеру и создаёт файл фото.
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
                }else {
                    Toast.makeText(activity, "Координаты магазина не обнаружены. Выполнение фото невозможно, обратитесь к Вашему руководителю.", Toast.LENGTH_SHORT).show();
                    makePhoto(activity, data); // Метод который запускает камеру и создаёт файл фото.
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
                    makePhoto(activity, data); // Метод который запускает камеру и создаёт файл фото.
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
}
