package ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar;


import static ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery.MakePhotoFromGaleryWpDataDB;
import static ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery.tovarId;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Arrays;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportOptionsFrag;
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.PhotoDescriptionText;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;

/**
 * 05.05.23.
 * Пробная идея по разруливанию проблем с реквизитами "Ц, Ф, К.." в Товарах.
 * Новые реквизиты и их обработка должна описываться тут.
 */
public class TovarRequisites {

    private TovarDB tovar;
    private ReportPrepareDB reportPrepareDB;
    private final int photoType;

    public TovarRequisites() {
        this.photoType = 4;
    }

    public TovarRequisites(TovarDB tovar, ReportPrepareDB reportPrepareDB) {
        this.tovar = tovar;
        this.reportPrepareDB = reportPrepareDB;
        this.photoType = 4;
    }

    public TovarRequisites(TovarDB tovar, ReportPrepareDB reportPrepareDB, int photoType) {
        this.tovar = tovar;
        this.reportPrepareDB = reportPrepareDB;
        this.photoType = photoType;
    }

    /**
     * 05.05.23
     * Создание и отображение модального окна для выполнения фото остатков Товаров
     */
    public DialogData createDialog(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, Clicks.clickVoid click) {
        DialogData res = new DialogData(context);

        TovarOptions tovarOptions;
        if (photoType != 4) {
            tovarOptions = new TovarOptions().createTovarOptionPhotoType();
            res.setTitle("Фото вiтрини (наближене)");
        } else {
            tovarOptions = new TovarOptions().createTovarOptionPhoto();
            res.setTitle("");
        }
        res.setText("");
        res.setClose(res::dismiss);
        res.setLesson(context, true, 802);
        res.setVideoLesson(context, true, 803, null, null);

        tovarId = "";
        if (tovar != null) {
            res.setImage(true, getPhotoFromDB(tovar));
            res.setAdditionalText(setPhotoInfo(reportPrepareDB, tovarOptions, tovar, "", ""));

            // Сделано для того что б можно было контролировать какая опция сейчас открыта
            res.tovarOptions = tovarOptions;
            res.reportPrepareDB = reportPrepareDB;
            tovarId = reportPrepareDB.tovarId;
        }

        res.setOperationButtons(
                "Зробити фото",
                () -> {
                    new MakePhoto().pressedMakePhoto((Activity) context, wpDataDB, optionsDB, String.valueOf(photoType), tovarId, click);
                },
                "Вибрати з галереї",
                () -> {
                    try {
                        if (DetailedReportOptionsFrag.PermissionUtils.checkReadExternalStoragePermission(context)) {
                            Globals.writeToMLOG("INFO", "Вибрати з галереї", "DetailedReportOptionsFrag.PermissionUtils.checkReadExternalStoragePermission(context)");
                            MakePhotoFromGaleryWpDataDB = wpDataDB;
                            if (photoType != 4)
                                MakePhotoFromGalery.tovarId = reportPrepareDB.tovarId;
                            MakePhotoFromGalery.photoType = photoType;
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            if (context instanceof DetailedReportActivity) {
                                ((DetailedReportActivity) context).startActivityForResult(Intent.createChooser(intent, "Select Picture"), 500);
                            } else if (context instanceof FeaturesActivity) {
                                ((FeaturesActivity) context).startActivityForResult(Intent.createChooser(intent, "Select Picture"), 500);
                                res.dismiss();
                            }
                        } else {
                            DetailedReportOptionsFrag.PermissionUtils.requestReadExternalStoragePermission((AppCompatActivity) context);
                            Globals.writeToMLOG("INFO", "Вибрати з галереї", "Запрос доступа");
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("INFO", "Вибрати з галереї", "Exception e: " + Arrays.toString(e.getStackTrace()));
                    }

                });

        res.setCancel("Закрити", res::dismiss);

        return res;
    }


    /**
     * !!! Думаю что это должно находиться где-то в StackPhoto
     * <p>
     * 05.05.23.
     * Получение ФАЙЛА фотографии Данного Товара.
     */
    public File getPhotoFromDB(TovarDB tovar) {
        int id = Integer.parseInt(tovar.getiD());
        StackPhotoDB stackPhotoDB = RealmManager.getTovarPhotoByIdAndType(id, tovar.photoId, 18, false);
        if (stackPhotoDB != null) {
            if (stackPhotoDB.getObject_id() == id) {
                if (stackPhotoDB.getPhoto_num() != null && !stackPhotoDB.getPhoto_num().equals("")) {
                    File file = new File(stackPhotoDB.getPhoto_num());
                    return file;
                }
            }
        }
        return null;
    }


    public PhotoDescriptionText setPhotoInfo(ReportPrepareDB reportPrepareDB, TovarOptions tpl, TovarDB tovar, String finalBalanceData1, String finalBalanceDate1) {
        PhotoDescriptionText res = new PhotoDescriptionText();
        try {
            String weightString = String.format("%s, %s", tovar.getWeight(), tovar.getBarcode()); // составление строк веса и штрихкода для того что б выводить в одно поле

            String title = tpl.getOptionLong();

            if (DetailedReportActivity.rpThemeId == 1178) {
                if (tpl.getOptionId().contains(578) || tpl.getOptionId().contains(1465)) {
                    title = "Кол-во выкуп. товара";
                }

                if (tpl.getOptionId().contains(579)) {
                    title = "Цена выкуп. товара";
                }
            }

            if (DetailedReportActivity.rpThemeId == 33) {
                if (tpl.getOptionId().contains(587)) {
                    title = "Кол-во заказанного товара";
                }
            }

            res.row1Text = title;
            res.row1TextValue = "";
            res.row2TextValue = tovar.getNm();
            res.row3TextValue = weightString;
            Log.e("ПРОИЗВОДИТЕЛЬ", "2ШТО ТУТ?:" + RealmManager.getNmById(tovar.getManufacturerId()) != null ? RealmManager.getNmById(tovar.getManufacturerId()).getNm() : "");

            res.row4TextValue = RealmManager.getNmById(tovar.getManufacturerId()) != null ? RealmManager.getNmById(tovar.getManufacturerId()).getNm() : "";

            res.row5Text = "Ост.:";
            res.row5TextValue = finalBalanceData1 + " шт на " + finalBalanceDate1;

            if (reportPrepareDB.facesPlan != null && reportPrepareDB.facesPlan > 0) {
                res.row6Text = "План фейс.:";
                res.row6TextValue = "" + reportPrepareDB.facesPlan;
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.setPhotoInfo", "Exception e: " + e);
        }
        return res;
    }


    // ----------- Setters
    public void setTovar(TovarDB tovar) {
        this.tovar = tovar;
    }

    public void setReportPrepareDB(ReportPrepareDB reportPrepareDB) {
        this.reportPrepareDB = reportPrepareDB;
    }
}
