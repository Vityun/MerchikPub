package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;
import android.util.Log;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.Controls.OptionControlPhotoPromotion;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

public class OptionButtonPhotoAktionTovar<T> extends OptionControl {
    public int OPTION_BUTTON_AKTION_TOVAR_ID = 157277;

    private WpDataDB wpDataDB;
    private final WorkPlan workPlan = new WorkPlan();

    public OptionButtonPhotoAktionTovar(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
        Log.e("OptionControlTask", "here");
        if (document instanceof WpDataDB) {
            this.wpDataDB = (WpDataDB) document;
        }
    }

    private void executeOption() {
        new Globals().fixMP(wpDataDB, null);// Фиксация Местоположения в таблице ЛогМп
        try {
//            WPDataObj wpDataObj = workPlan.getKPS(wpDataDB.getId());
//            wpDataObj.setPhotoType("28");
//
//            MakePhoto makePhoto = new MakePhoto();
//            makePhoto.pressedMakePhotoOldStyle((Activity) context, wpDataObj, wpDataDB);

            new OptionControlPhotoPromotion<>(context, document, optionDB, msgType, nnkMode, unlockCodeResultListener).showOptionMassage("");

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionButtonPhotoAktionTovar/executeOption/Exception", "Exception e: " + e);
        }
    }
}
