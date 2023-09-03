package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.Adapters.LogMPAdapter;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.LogMPRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

// 138773
public class OptionButtonHistoryMP<T> extends OptionControl {
    public int OPTION_BUTTON_ADD_COMMENT_ID = 132623;

    private WpDataDB wpDataDB;
    private List<LogMPDB> logMPDBList;

    private int validTime = 1800;   // 30 (1800сек) минут допустимого времени.
    private long startTime;
    private long endTime;

    public OptionButtonHistoryMP(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        try {
            this.context = context;
            this.document = document;
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            this.unlockCodeResultListener = unlockCodeResultListener;
            getDocumentVar();
            executeOption();
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "OptionButtonHistoryMP", "Exception e: " + e);
        }
    }

    private void getDocumentVar(){
        try {
            wpDataDB = (WpDataDB) document;
            startTime = (wpDataDB.getVisit_start_dt() > 0)
                    ? wpDataDB.getVisit_start_dt() - validTime
                    : (System.currentTimeMillis() / 1000) - validTime;

//            long endTime = (wpDataDB.getVisit_end_dt() > 0)
//                    ? wpDataDB.getVisit_end_dt()
//                    : (System.currentTimeMillis() / 1000);


            endTime = System.currentTimeMillis() / 1000;

            logMPDBList = LogMPRealm.getLogMPTimeDad2(startTime, endTime, wpDataDB.getCode_dad2());
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "OptionButtonHistoryMP/getDocumentVar", "Exception e: " + e);
        }
    }

    private void executeOption(){
        try {
            DialogData dialog = new DialogData(context);
            dialog.setTitle("Історія місцеположення");
            dialog.setText("Нижче зазначені данні із місцезнаходженням вашого пристрою за період: з " + Clock.getHumanTimeSecPattern(startTime, "dd.MM HH:mm:ss") + " по " + Clock.getHumanTimeSecPattern(endTime, "dd.MM HH:mm:ss"));
            dialog.setRecycler(createAdapter(dialog.context, logMPDBList), new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            dialog.setClose(dialog::dismiss);
            dialog.show();
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "OptionButtonHistoryMP/executeOption", "Exception e: " + e);
        }
    }

    /**
     * 01.09.23.
     * Создание адаптера для отображения списка Истории местоположений
     *
     * @return*/
    private RecyclerView.Adapter createAdapter(Context context, List<LogMPDB> logMPDBList){
        LogMPAdapter adapter = new LogMPAdapter(context, logMPDBList);
        return adapter;
    }
}
