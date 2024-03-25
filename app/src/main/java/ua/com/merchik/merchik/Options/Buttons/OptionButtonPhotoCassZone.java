package ua.com.merchik.merchik.Options.Buttons;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

public class OptionButtonPhotoCassZone <T> extends OptionControl {
    public int OPTION_BUTTON_PhotoCassZone_ID = 164351;

    private WpDataDB wpDataDB;
    private final WorkPlan workPlan = new WorkPlan();

    public OptionButtonPhotoCassZone(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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

        if (document instanceof TasksAndReclamationsSDB){
            long codDad2 = ((TasksAndReclamationsSDB) document).codeDad2SrcDoc;
            this.wpDataDB = WpDataRealm.getWpDataRowByDad2Id(codDad2);
        }
    }

    private void executeOption() {
        new Globals().fixMP(wpDataDB, null);// Фиксация Местоположения в таблице ЛогМп
        try {
//            WPDataObj wpDataObj = workPlan.getKPS(wpDataDB.getId());
//            wpDataObj.setPhotoType("45");

//            MakePhoto makePhoto = new MakePhoto();
//            makePhoto.pressedMakePhotoOldStyle((Activity) context, wpDataObj, wpDataDB, optionDB);

            MakePhoto makePhoto = new MakePhoto();
            makePhoto.pressedMakePhoto((Activity) context, wpDataDB, optionDB, "45");
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionButtonPhotoCassZone/executeOption/Exception", "Exception e: " + e);
        }
    }
}
