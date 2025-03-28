package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;

import java.util.Objects;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;

public class OptionControlAddOpinion<T> extends OptionControl {
    public int OPTION_CONTROL_OPINION_ID = 84001;

    private WpDataDB wpDataDB;
    private ThemeDB themeDB;

    private String opinion;
    private int userId;
    private int theme;

    private int optionAmountMin;

    private String massageComment;
    public boolean signal = false;

    private String opinionStatus = "0";

    public OptionControlAddOpinion(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
                opinion = wpDataDB.getUser_opinion_id();
                userId = wpDataDB.getUser_id();
                massageComment = "Думка виконавця стосовно візиту";
            }

            //2.0. готовим сообщение и сигнал
            if (Objects.equals(opinion, "0")) {  //значит нет мнения
                stringBuilderMsg.append("Не зазначена ").append(massageComment);
                opinionStatus ="0";
                signal = true;
            } else {
                stringBuilderMsg.append(massageComment).append(" прийнята.");
                opinionStatus ="1";
                signal = false;
            }

            // 4.0
            if (signal) {
                if (optionDB.getBlockPns().equals("1") && wpDataDB.getStatus() == 0) {    //блокировать проведение, если есть сигнал
                    stringBuilderMsg.append("\n\nДокумент проведен не будет!");
                } else {
                    stringBuilderMsg.append("\n\nВы можете получить Премиальные БОЛЬШЕ, если будете заполнять комментарий корректно.");
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
