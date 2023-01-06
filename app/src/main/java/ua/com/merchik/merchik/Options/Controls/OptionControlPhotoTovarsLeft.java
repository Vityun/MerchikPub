package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;

import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

/**
 * 06.04.2023.
 * Контроль наличия Фото Остатков Товаров (для любого из Клиентов)
 * */
public class OptionControlPhotoTovarsLeft<T> extends OptionControl {
    public int OPTION_CONTROL_PHOTO_TOVARS_LEFT_ID = 1470;

    // option data
    private boolean signal = false;

    // document data
    private long dad2 = 0;

    public OptionControlPhotoTovarsLeft(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
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
            WpDataDB wp = (WpDataDB) document;

            dad2 = wp.getCode_dad2();
        }
    }

    private void executeOption() {

    }

    /**
     * Сохранение данных об опции контроля в БД
     */
    private void saveOptionResultInDB() {
        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                if (signal) {
                    optionDB.setIsSignal("1");
                } else {
                    optionDB.setIsSignal("2");
                }
                realm.insertOrUpdate(optionDB);
            }
        });
    }

}
