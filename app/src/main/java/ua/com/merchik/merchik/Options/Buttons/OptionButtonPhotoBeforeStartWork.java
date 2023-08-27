package ua.com.merchik.merchik.Options.Buttons;

import android.app.Activity;
import android.content.Context;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

public class OptionButtonPhotoBeforeStartWork<T> extends OptionControl {
    public int OPTION_BUTTON_PHOTO_BEFORE_START_WORK_ID = 135809;

    private WpDataDB wpDataDB;
    private final WorkPlan workPlan = new WorkPlan();

    public OptionButtonPhotoBeforeStartWork(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
        new Globals().fixMP(wpDataDB);// Фиксация Местоположения в таблице ЛогМп
        try {
            MakePhoto makePhoto = new MakePhoto();
            makePhoto.pressedMakePhoto((Activity) context, wpDataDB, optionDB, "14"); // Фото До начала Работ

        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "OptionButtonPhotoBeforeStartWork/executeOption/Exception", "Exception e: " + e);
        }
    }
}
