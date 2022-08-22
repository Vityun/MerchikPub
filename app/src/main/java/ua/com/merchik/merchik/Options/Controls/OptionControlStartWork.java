package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;

import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class OptionControlStartWork<T> extends OptionControl {

    private long startWork;

    public OptionControlStartWork(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;

        executeOption();
    }


    public void executeOption(){
        getStartWorkFromDocument();

        if (startWork > 0) {
            saveOption("2");
        } else {
            saveOption("1");
        }
    }

    private void getStartWorkFromDocument() {
        if (document instanceof WpDataDB) {
            startWork = ((WpDataDB) document).getVisit_start_dt();
        } else if (document instanceof TasksAndReclamationsSDB) {
            startWork = ((TasksAndReclamationsSDB) document).dt_start_fact;
        }
    }

    private void saveOption(String signal){
        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                optionDB.setIsSignal(signal);
                realm.insertOrUpdate(optionDB);
            }
        });
    }
}
