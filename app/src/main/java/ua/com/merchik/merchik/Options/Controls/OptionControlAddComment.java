package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;

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

public class OptionControlAddComment<T> extends OptionControl {
    public int OPTION_CONTROL_ADD_COMMENT_ID = 132624;

    private WpDataDB wpDataDB;
    private ThemeDB themeDB;

    private String comment;
    private int userId;
    private int theme;

    private int optionAmountMin;

    private String massageComment;
    public boolean signal = false;

    public OptionControlAddComment(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
                comment = wpDataDB.user_comment; // TODO Нужно написать функцию которая будет вырезать 'мусор' с строк и использовать тут
                userId = wpDataDB.getUser_id();
                massageComment = "Комментарий ";
            }

            //3.0. готовим сообщение и сигнал
            if (comment.length() == 0) {  //значит в комментарии один мусор
                stringBuilderMsg.append("За период с ").append(Clock.today).append(" по ")
                        .append(Clock.today).append(" Вы НЕ заполнили поле (").append(massageComment).append(") ");
                signal = true;
            } else if (optionAmountMin == 0) {    //значит не установлен минимальный размер комментария
                stringBuilderMsg.append("Для темы: ").append(themeDB.getNm()).append(" не установлена минимальная длина для поля (")
                        .append(massageComment).append("). Обратитесь к руководителю.");
                signal = true;
            } else if (comment.length() < optionAmountMin) {    //значит комментарий есть но он слишком короткий
                stringBuilderMsg.append("Ваш(е) ").append(massageComment).append(" слишком короткий(ое). ")
                        .append(" Он(о) должен(но) быть не менее ").append(optionAmountMin).append("символов.");
                signal = true;
            }else {
                stringBuilderMsg.append(massageComment).append(" (").append(comment).append(") принят.");
                signal = false;
            }

            // 4.0
            if (signal){
                if (optionDB.getBlockPns().equals("1") && wpDataDB.getStatus() == 0) {    //блокировать проведение, если есть сигнал
                    stringBuilderMsg.append("\n\nДокумент проведен не будет!");
                }else {
                    stringBuilderMsg.append("\n\nВы можете получить Премиальные БОЛЬШЕ, если будете заполнять комментарий корректно.");
                }
            }

            // 5.0
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

            // 6.0
            setIsBlockOption(signal);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAddComment/executeOption", "Exception e: " + e);
        }
    }
}
