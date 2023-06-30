package ua.com.merchik.merchik.Options.Buttons;

import android.app.Activity;
import android.content.Context;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.Options.Controls.OptionControlAvailabilityControlPhotoRemainingGoods;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.WPDataObj;

public class OptionButtonPhotoFOT<T> extends OptionControl {
    public int OPTION_BUTTON_PHOTO_PhotoFOT_ID = 135158;


    private WpDataDB wpDataDB;
    private final WorkPlan workPlan = new WorkPlan();

    public OptionButtonPhotoFOT(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            this.wpDataDB = (WpDataDB) document;
        }
    }

    private void executeOption() {
        new Globals().fixMP();// Фиксация Местоположения в таблице ЛогМп
        try {
            if (optionDB.getOptionControlId().equals("159707")) {
                OptionControlAvailabilityControlPhotoRemainingGoods<?> optionControlAvailabilityControlPhotoRemainingGoods = new OptionControlAvailabilityControlPhotoRemainingGoods<>(context, document, optionDB, msgType, nnkMode);
                optionControlAvailabilityControlPhotoRemainingGoods.showOptionMassage("");
            } else {
                WPDataObj wpDataObj = workPlan.getKPS(wpDataDB.getId());
                wpDataObj.setPhotoType("4");

                MakePhoto makePhoto = new MakePhoto();
                makePhoto.pressedMakePhotoOldStyle((Activity) context, wpDataObj, wpDataDB, optionDB);
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionButtonPhotoFOT/executeOption/Exception", "Exception e: " + e);
        }
    }
}
