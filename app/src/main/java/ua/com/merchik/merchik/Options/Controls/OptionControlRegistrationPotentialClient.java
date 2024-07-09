package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;

import java.util.Arrays;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.PotentialClientSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class OptionControlRegistrationPotentialClient<T> extends OptionControl {
    public int OPTION_CONTROL_REGISTRATION_POTENTIAL_CLIENT_ID = 133381;

    public boolean signal = false;
    private boolean report200 = false;
    private int err = 0;
    private String type = "потенциальных клиентов (ПК)";
    private String dateDiapason = "";
    private int count = 0;
    private StringBuilder potentialClientMsg = new StringBuilder();

    private WpDataDB wpDataDB;

    private int themeId;
    private long dt;  // Дата источника в секундах
    private long dtFrom;
    private long dtTo;

    public OptionControlRegistrationPotentialClient(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
            Globals.writeToMLOG("ERROR", "OptionControlRegistrationPotentialClient", "Exception e: " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            wpDataDB = (WpDataDB) document;

            themeId = wpDataDB.getTheme_id();
            dt = wpDataDB.getDt().getTime() / 1000;
        }

        dtFrom = dt - 2592000;  // -30 дней
        dtTo = dt + 86400;      // +1 день
        dateDiapason = "За период с " + Clock.getHumanTime3(dtFrom) + " по " + Clock.getHumanTime3(dtTo);
    }


    private void executeOption() {
        List<PotentialClientSDB> potentialClients = SQL_DB.potentialClientDao().getByUserThemeDt(wpDataDB.getUser_id(), 834, dtFrom, dtTo);

        if (potentialClients == null || potentialClients.size() == 0) {
            err++;
            count = 0;
            potentialClientMsg.append(dateDiapason).append(", сотрудник ").append(wpDataDB.getUser_txt()).append(" ").append(type).append(" НЕ регистрировал.");
        } else {
            count = potentialClients.size();
        }

        UsersSDB user = SQL_DB.usersDao().getUserById(wpDataDB.getUser_id());
//        if (err > 0 && user != null && user.reportCount < 200) {

        if (err > 0 && user != null && (user.reportDate200 == null || wpDataDB.getDt().getTime() < user.reportDate200.getTime())) {
            count = 0;
            potentialClientMsg.append(", но он еще не провел своего 200-го отчета.");
            report200 = true;
        }

        formatMsg(potentialClients);

        if (signal){
            if (optionDB.getBlockPns().equals("1")){
                stringBuilderMsg.append("\n\nДокумент проведен не будет!");
                setIsBlockOption(signal);    // Установка блокирует ли опция работу приложения или нет
            }else {
                stringBuilderMsg.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если Вы (или ваши подчиненные) будут регистрировать ").append(type);
            }
        }

        // Блокировка
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
        checkUnlockCode(optionDB);

        showOptionMassage("");
    }

    private void formatMsg(List<PotentialClientSDB> potentialClients) {
        if (potentialClients != null && potentialClients.size() == 0) {
            stringBuilderMsg.append("Не могу определить параметры расчета.").append("\n\n").append(potentialClientMsg);
            signal = !report200;
        } else if (err > 0) {
            stringBuilderMsg.append(dateDiapason).append(" ").append(type).append(" не зарегистрировали. Подробности см. ниже.").append("\n\n").append(potentialClientMsg);
            signal = true;
        } else {
            stringBuilderMsg.append(dateDiapason).append(" замечаний по регистрации ").append(type).append(" нет. Зарегистрировано ").append(count).append(" ПК").append("\n\n").append(potentialClientMsg);
            signal = false;
        }
    }

}
