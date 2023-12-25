package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;
import android.util.Log;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ImagesTypeListRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;

public class OptionControlPhoto<T> extends OptionControl {
    public int OPTION_CONTROL_PHOTO_ID = 84932;
    public boolean signal = true;
    private StringBuilder optionResultStr = new StringBuilder();

    private WpDataDB wpDataDB;

    public OptionControlPhoto(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
            Log.e("OptionControlPhoto", "Exception e: " + e);
            Globals.writeToMLOG("ERR", "OptionControlPhoto", "Exception e: " + e);
        }
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

        String optionId;
        if (nnkMode.equals(Options.NNKMode.BLOCK)){
            optionId = optionDB.getOptionId();
        }else {
            optionId = optionDB.getOptionControlId();
        }

        switch (optionId){
            case "164352":  // Контроль наявності світлини прикасової зони
                photoType = 45;
                m = 1;
                break;

            case "84932":
                photoType = 0;
                m = 3;
                break;

            case "132971":
                photoType = 10; // Проверка наличия Фото тележка с товаром (тип 10)
                m = 1;
                break;

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
            case "159725":  // Кнопка "Фото Торговой Точки (ФТТ)"
                photoType = 37;
                break;

        }

        RealmResults<StackPhotoDB> stackPhotoDB = StackPhotoRealm.getPhotosByDAD2(wpDataDB.getCode_dad2(), photoType);
        if (stackPhotoDB != null && stackPhotoDB.size() < m){
            stringBuilderMsg.append("Вы должны сделать: ").append(m).append(" фото с типом: ").append(ImagesTypeListRealm.getByID(photoType).getNm()).append(", а сделали: ").append(stackPhotoDB.size()).append(" - доделайте фотографии.");
            signal = true;
            unlockCodeResultListener.onUnlockCodeFailure();
        }else {
            stringBuilderMsg.append("Жалоб по фыполнению фото нет. Сделано: ").append(stackPhotoDB.size()).append(" фото.");
            signal = false;
            unlockCodeResultListener.onUnlockCodeSuccess();
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
