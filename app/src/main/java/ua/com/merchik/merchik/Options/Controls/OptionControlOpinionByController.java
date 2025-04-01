package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;

import java.sql.SQLData;
import java.util.Objects;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;

public class OptionControlOpinionByController<T> extends OptionControl {
    public int OPTION_CONTROL_OPINION_BY_CONTROLLER_ID = 141893;

    private WpDataDB wpDataDB;
    private ThemeDB themeDB;

    private String controllerOpinion;
    private String controllerId;
    private int theme;

    private int optionAmountMin;

//    private String massageComment;
    public boolean signal = false;

    private String opinionStatus = "0";

    public OptionControlOpinionByController(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
        try {
            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;
                this.theme = wpDataDB.getTheme_id();
            }

            this.themeDB = ThemeRealm.getThemeById(String.valueOf(theme));

            optionAmountMin = Integer.parseInt(optionDB.getAmountMin());
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAddComment/getDocumentVar", "Exception e: " + e);
        }
    }

    private void executeOption() {
        try {
            if (wpDataDB != null) {
                controllerOpinion = wpDataDB.getController_opinion_id();
                controllerId = wpDataDB.getController_opinion_author_id();
//                massageComment = "Думка виконавця стосовно візиту";
            }


            //2.0. готовим сообщение и сигнал
            if (controllerOpinion != null && !controllerOpinion.isEmpty() && !controllerOpinion.equals("0")) {
                String author;
                String opinion;
                try {
                    opinion = SQL_DB.opinionDao().getOpinionById(Integer.parseInt(controllerOpinion)).nm;
                } catch (Exception e) {
                    opinion = "Помилка при визначенні теми, зверніться до керівника";
                }
                try {
                    author = SQL_DB.usersDao().getUserName(Integer.parseInt(controllerId));
                } catch (Exception e){
                    author = "автор не визначено";
                }
                if (Objects.equals(controllerOpinion, "3")
                        || Objects.equals(controllerOpinion, "8")
                        || Objects.equals(controllerOpinion, "9")
                        || Objects.equals(controllerOpinion, "29")) {  //значит нет мнения
                    stringBuilderMsg.append("По поточному відвідуванню, від контролера ")
                            .append(author)
                            .append(", отримано зауваження: ")
                            .append(opinion);
                    signal = true;
                } else {
                    stringBuilderMsg.append("Думка контролера: ")
                            .append(opinion)
                            .append(". Зауважень нема.");
                    signal = false;
                }
            }

            // 4.0
            if (signal) {
                if (optionDB.getBlockPns().equals("1") && wpDataDB.getStatus() == 0) {    //блокировать проведение, если есть сигнал
                    stringBuilderMsg.append("\n\nДокумент проведено не буде!");
                } else {
                    stringBuilderMsg.append("\n\nВи можете отримати БІЛЬШІ преміальні, якщо по кожному клієнту Оцініть ВСІ затверджені Фото Зразків Викладки (Планограми) БУДЬ-ЯКОЮ оцінкою.");
                }
            }

            // 5.0
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

            // 6.0
            setIsBlockOption(signal);

            checkUnlockCode(optionDB);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAddComment/executeOption", "Exception e: " + e);
        }
    }

    public String currentOpinionStatus() {
        return opinionStatus + "/1";
    }
}
