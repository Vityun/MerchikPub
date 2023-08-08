package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;

public class OptionControlPhotoBeforeStartWork<T> extends OptionControl {
    public int OPTION_CONTROL_PHOTO_BEFORE_START_WORK_ID = 151594;

    private WpDataDB wpDataDB;
    private long dateFrom;
    private long dateTo;
    private Number DVI = 0;

    public boolean signal = false;

    public OptionControlPhotoBeforeStartWork(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
            wpDataDB = (WpDataDB) document;
            dateFrom = wpDataDB.getDt().getTime() - 345600000;  // -4
            dateTo = wpDataDB.getDt().getTime() + 345600000;    // +4
        }
    }

    private void executeOption() {
        try {
            RealmResults<StackPhotoDB> stackPhotoDB = StackPhotoRealm.getPhoto(dateFrom, dateTo, wpDataDB.getCode_dad2(), 14);
            DVI = stackPhotoDB.where().sum("dvi");

            int min = 0;
            if (optionDB.getAmountMin() != null){
                min = Integer.parseInt(optionDB.getAmountMin());
            }

            if (stackPhotoDB.size() > 0 && DVI.intValue() == stackPhotoDB.size() && min > 0){  // //27.07.2022 в данном случае, КолМин обозначает кол-во фото НЕ помеченных на ДВИ (таких, которые видит клиент) Если КолМин=0 то может быть только ОДНА фотка и помечена на ДВИ (используем для контроля операторами СП)
                stringBuilderMsg.append("Фото Витрины ДО начала работ (ФВДОНР) выполнено (").append(stackPhotoDB.size()).append(") но помечено на ДВИ. Для данной опции ДВИ ставить НЕ нужно!");
                signal = true;
            }else if (stackPhotoDB.size() > 0){
                stringBuilderMsg.append("Фото Витрины ДО начала работ (ФВДОНР) выполнено (").append(stackPhotoDB.size()).append(") и будет проверено ОСП.");
                signal = false;
            }else {
                stringBuilderMsg.append("Фото Витрины ДО начала работ (ФВДОНР) отсутствует (или помечено на ДВИ)!");
                signal = true;
            }

            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionDB != null) {
                    if (signal){
                        optionDB.setIsSignal("1");
                    }else {
                        optionDB.setIsSignal("2");
                    }
                    realm.insertOrUpdate(optionDB);
                }
            });

            setIsBlockOption(signal);

        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "OptionControlPhotoBeforeStartWork", "Exception e: " + e);
        }
    }
}
