package ua.com.merchik.merchik.Options.Buttons;

import static ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery.MakePhotoFromGaleryWpDataDB;

import android.content.Context;
import android.content.Intent;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportOptionsFrag;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

public class OptionButtonPhotoEFFIE<T> extends OptionControl {
    public int OPTION_BUTTON_PHOTO_EFFIE_ID = 165481;

    private WpDataDB wpDataDB;
    private final WorkPlan workPlan = new WorkPlan();

    public OptionButtonPhotoEFFIE(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;
        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            this.wpDataDB = (WpDataDB) document;
        }
    }

    private void executeOption() {
        new Globals().fixMP(wpDataDB, null);// Фиксация Местоположения в таблице ЛогМп
        try {
//            WPDataObj wpDataObj = workPlan.getKPS(wpDataDB.getId());
//            wpDataObj.setPhotoType("46");
//
//            MakePhoto makePhoto = new MakePhoto();
//            makePhoto.pressedMakePhotoOldStyle((Activity) context, wpDataObj, wpDataDB, optionDB);

            if (DetailedReportOptionsFrag.PermissionUtils.checkReadExternalStoragePermission(context)) {
                MakePhotoFromGaleryWpDataDB = wpDataDB;
                MakePhotoFromGalery.tovarId = "0";
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                ((DetailedReportActivity) context).startActivityForResult(Intent.createChooser(intent, "Select Picture"), 500);
            } else {
                DetailedReportOptionsFrag.PermissionUtils.requestReadExternalStoragePermission(context, (DetailedReportActivity) context);
            }

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionButtonPhotoFOT/executeOption/Exception", "Exception e: " + e);
        }
    }
}
