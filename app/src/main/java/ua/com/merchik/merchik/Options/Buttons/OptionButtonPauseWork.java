package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.Log;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.PauseWorkStateHolder;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.toolbar_menus;

public class OptionButtonPauseWork<T> extends OptionControl {
    public static final int OPTION_BUTTON_PAUSE_WORK_ID = 139337;

    private WpDataDB wpDataDB;

    public OptionButtonPauseWork(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
        try {
            DetailedReportActivity activity = findDetailedReportActivity(context);
            if (activity != null) {
                activity.startPauseWorkUi(wpDataDB);
                return;
            }

            if (wpDataDB == null || wpDataDB.getCode_dad2() == 0) {
                Globals.writeToMLOG("ERROR", "OptionButtonPauseWork/executeOption", "WpDataDB is empty or code_dad2 is 0");
                return;
            }

            PauseWorkStateHolder.start(wpDataDB.getCode_dad2(), wpDataDB.getId());
            if (context instanceof toolbar_menus) {
                ((toolbar_menus) context).invalidateOptionsMenu();
            }

            Intent intent = new Intent(context, DetailedReportActivity.class);
            intent.putExtra("WpDataDB_ID", wpDataDB.getId());
            if (!(context instanceof android.app.Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionButtonPauseWork/executeOption/Exception", "Exception e: " + e);
        }
    }

    private DetailedReportActivity findDetailedReportActivity(Context context) {
        Context current = context;
        while (current instanceof ContextWrapper) {
            if (current instanceof DetailedReportActivity) {
                return (DetailedReportActivity) current;
            }
            Context baseContext = ((ContextWrapper) current).getBaseContext();
            if (baseContext == current) {
                break;
            }
            current = baseContext;
        }
        return null;
    }
}
