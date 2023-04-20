package ua.com.merchik.merchik.Options.Buttons;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.WPDataObj;

public class OptionButtonPhotoShowcaseFullness<T> extends OptionControl {
    public static int OPTION_BUTTON_PhotoShowcaseFullness_ID = 158605;

    private WpDataDB wpDataDB;
    private final WorkPlan workPlan = new WorkPlan();

    public OptionButtonPhotoShowcaseFullness(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        Log.e("OptionControlTask", "here");
        if (document instanceof WpDataDB) {
            this.wpDataDB = (WpDataDB) document;
        }
    }

    private void executeOption() {
        new Globals().fixMP();// Фиксация Местоположения в таблице ЛогМп
        try {
            WPDataObj wpDataObj = workPlan.getKPS(wpDataDB.getId());
            wpDataObj.setPhotoType("40");

            MakePhoto makePhoto = new MakePhoto();
            makePhoto.pressedMakePhotoOldStyle((Activity) context, wpDataObj, wpDataDB);
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "OptionButPhotoPlanogramm/executeOption/Exception", "Exception e: " + e);
        }
    }
}
