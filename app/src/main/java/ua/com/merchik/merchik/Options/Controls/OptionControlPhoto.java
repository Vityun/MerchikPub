package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;

public class OptionControlPhoto<T> extends OptionControl {
    public int OPTION_CONTROL_PHOTO_ID = 84932;
    private boolean signal = true;
    private StringBuilder optionResultStr = new StringBuilder();

    private WpDataDB wpDataDB;

    public OptionControlPhoto(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
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
        int m = Integer.parseInt(optionDB.getAmountMin());
        if (m == 0) {
            m = 3;
        }
        int photoType = 0;

        switch (optionDB.getOptionControlId()){
            case "141361":
                photoType = 31; // Фото товара на скалде
                m = 1;
                break;

            case "158606":  // Корпоративный блок
                photoType = 40;
                break;

            case "158607":  // Наполненность полки
                photoType = 41;
                break;

            case "158608":  // Приближенная фото
                photoType = 39;
                break;

            case "158609":  // Дополнительное место продаж
                photoType = 42;
                break;

            case "159726":  // Фото торговой точки
                photoType = 37;
                break;

        }

        RealmResults<StackPhotoDB> stackPhotoDB = StackPhotoRealm.getPhotosByDAD2(wpDataDB.getCode_dad2(), photoType);
        if (stackPhotoDB != null && stackPhotoDB.size() < m){
            stringBuilderMsg.append("Вы должны сделать: ").append(m).append(" фото, а сделали: ").append(stackPhotoDB.size()).append(" - доделайте фотографии.");
            signal = true;
        }else {
            stringBuilderMsg.append("Жалоб по фыполнению фото нет. Сделано: ").append(stackPhotoDB.size()).append(" фото.");
            signal = false;
        }

        //7.0. сохраним сигнал
        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                if (signal) {
                    optionDB.setIsSignal("1");
                    setIsBlockOption(signal);
                } else {
                    optionDB.setIsSignal("2");
                }
                realm.insertOrUpdate(optionDB);
            }
        });

        //8.0. блокировка проведения
        // Установка блокирует ли опция работу приложения или нет
        if (signal) {
            if (optionDB.getBlockPns().equals("1")) {
                setIsBlockOption(signal);
                stringBuilderMsg.append("\n\n").append("Документ проведен не будет!");
            } else {
                stringBuilderMsg.append("\n\n").append("Вы можете отримати Преміальні БІЛЬШЕ, якщо будете збільшувати кількість фейсів товарів замовника на полиці.");
            }
        }
    }
}
