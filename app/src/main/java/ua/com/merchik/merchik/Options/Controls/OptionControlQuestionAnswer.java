package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.QuestionAnswerDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;

// Контроль наличия жалоб на условия работ
public class OptionControlQuestionAnswer<T> extends OptionControl {

    public int OPTION_CONTROL_QUESTION_ANSWER_ID = 151121;

    private static final int THEME_OTHER = 6;
    private static final int THEME_IDEA = 607;
    private static final int THEME_PAYMENT_INCREASE = 610;
    private static final int THEME_MANAGER_WRONG = 612;
    private static final int THEME_DISMISSED_SALARY_REVIEW = 421;

    private static final boolean TEMP_IGNORE_UNTIL_2027 = true;

    private WpDataDB wpDataDB;
    private UsersSDB user;

    private int userId;
    private int theme;

    private long documentDateSec;
    private long dateFromSec;
    private long dateToSec;

    private String controlledUserText = "";
    private String periodText = "";

    public boolean signal = false;

    public OptionControlQuestionAnswer(
            Context context,
            T document,
            OptionsDB optionDB,
            OptionMassageType msgType,
            Options.NNKMode nnkMode,
            UnlockCodeResultListener unlockCodeResultListener
    ) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;

        getDocumentVar();
        executeOption();
    }

    /**
     * 1.0. Определим вводные данные
     */
    private void getDocumentVar() {
        try {
            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;

                this.theme = wpDataDB.getTheme_id();
                this.userId = wpDataDB.getUser_id();

                this.documentDateSec = getDocumentDateSec(wpDataDB);
                this.dateFromSec = documentDateSec - 30L * 24L * 60L * 60L;
                this.dateToSec = documentDateSec;

                this.periodText = "За период с "
                        + formatDateSec(dateFromSec)
                        + " по "
                        + formatDateSec(dateToSec);

                this.user = SQL_DB.usersDao().getById(userId);
                this.controlledUserText = user.fio;

            }
        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlQuestionAnswer/getDocumentVar",
                    "Exception e: " + e
            );
        }
    }

    private void executeOption() {
        try {
            if (wpDataDB == null) {
                stringBuilderMsg.append("Документ для проверки наличия жалоб не найден.");
                signal = false;
                setIsBlockOption(signal);
                checkUnlockCode(optionDB);
                return;
            }

            /*
             * 2.0. Получим данные о наличии поданных замечаний/жалоб
             */
            List<QuestionAnswerDB> complaints = getComplaints();

            /*
             * 3.0. Сформируем сообщение и сигнал
             */
            int complaintsCount = complaints != null ? complaints.size() : 0;

            // исполнитель не провел 5-го отчета
            boolean beforeFifthReport;

            try {

                java.util.Date docDate = wpDataDB != null ? wpDataDB.getDt() : null;

                if (user == null) {
                    beforeFifthReport = true;

                } else if (user.reportDate05 == null) {
                    beforeFifthReport = true;

                } else if (docDate == null) {
                    beforeFifthReport = true;

                } else {
                    Calendar report05Calendar = Calendar.getInstance();
                    report05Calendar.setTime(user.reportDate05); // java.sql.Date
                    report05Calendar.set(Calendar.HOUR_OF_DAY, 0);
                    report05Calendar.set(Calendar.MINUTE, 0);
                    report05Calendar.set(Calendar.SECOND, 0);
                    report05Calendar.set(Calendar.MILLISECOND, 0);

                    Calendar docCalendar = Calendar.getInstance();
                    docCalendar.setTime(docDate); // java.util.Date
                    docCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    docCalendar.set(Calendar.MINUTE, 0);
                    docCalendar.set(Calendar.SECOND, 0);
                    docCalendar.set(Calendar.MILLISECOND, 0);

                    beforeFifthReport =
                            report05Calendar.getTimeInMillis() >= docCalendar.getTimeInMillis();
                }

            } catch (Exception e) {
                Globals.writeToMLOG(
                        "ERROR",
                        "OptionControlQuestionAnswer/executeOption/beforeFifthReport",
                        "Exception e: " + e
                );

                beforeFifthReport = true;
            }

            if (complaintsCount == 0 && beforeFifthReport) {

                stringBuilderMsg
                        .append(periodText)
                        .append(" замечаний (жалоб) на условия работы практикант ")
                        .append(controlledUserText)
                        .append(" НЕ подавал, но до 5-го отчета это не обязательно.");

                signal = false;

            } else if (complaintsCount == 0) {

                stringBuilderMsg
                        .append(periodText)
                        .append(" замечаний (жалоб) на условия работы сотрудник ")
                        .append(controlledUserText)
                        .append(" НЕ подавал!");

                signal = true;

            } else {

                stringBuilderMsg
                        .append(periodText)
                        .append(" сотрудник ")
                        .append(controlledUserText)
                        .append(" подал ")
                        .append(complaintsCount)
                        .append(" замечаний (жалоб) на условия работы. Замечаний нет.");

                signal = false;
            }

            /*
             * Исключение из 1С:
             * если это разбор з/п уволенного бойца, то получение жалоб не контролируем.
             * На всякий случай оставил для полного соответствия 1С, хотя это явно лишнее в приложении
             */
            if (signal && theme == THEME_DISMISSED_SALARY_REVIEW && isControlledUserDismissed()) {
                stringBuilderMsg
                        .append(" Но, проверяемый уволен. Поэтому получение замечаний (жалоб) НЕ контролируем!");
                signal = false;
            }

            /*
             * 4.0
             */
            if (signal) {
                if (optionDB.getBlockPns().equals("1") && wpDataDB.getStatus() == 0) {
                    stringBuilderMsg.append("\n\nДокумент проведен не будет!");
                } else {
                    stringBuilderMsg.append(
                            "\n\nВы можете получить Премиальные БОЛЬШЕ, если он подаст замечение (в МВС)."
                    );
                }
            }


            /*
             * 5.0
             */
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

            /*
             * 6.0
             */
            /*
             * Временное исключение до 01.01.2027:
             * замечания/жалобы пока не блокируем.
             */
            if (signal && TEMP_IGNORE_UNTIL_2027 && isBeforeTemporaryExceptionEndDate()) {
                stringBuilderMsg
                        .append("\n\nДо 01.01.2027 діє тимчасове виключення. ")
                        .append("Отримання зауважень/скарг НЕ контролюємо.");

                signal = false;
            }
            setIsBlockOption(signal);

            checkUnlockCode(optionDB);

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlQuestionAnswer/executeOption",
                    "Exception e: " + e
            );
        }
    }

    private List<QuestionAnswerDB> getComplaints() {
        try {
            List<Integer> themeIds = Arrays.asList(
                    THEME_OTHER,
                    THEME_IDEA,
                    THEME_PAYMENT_INCREASE,
                    THEME_MANAGER_WRONG
            );

            return SQL_DB.questionAnswerDao().getComplaintsByUserAndPeriod(
                    userId,
                    dateFromSec,
                    dateToSec,
                    themeIds
            );

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlQuestionAnswer/getComplaints",
                    "Exception e: " + e
            );

            return new ArrayList<>();
        }
    }

    private long getDocumentDateSec(WpDataDB wpDataDB) {
        try {
            if (wpDataDB != null && wpDataDB.getDt() != null) {
                return wpDataDB.getDt().getTime() / 1000L;
            }
        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlQuestionAnswer/getDocumentDateSec",
                    "Exception e: " + e
            );
        }

        return System.currentTimeMillis() / 1000L;
    }


    /**
     * Аналог:
     * Кон.Уволен = 1
     */
    private boolean isControlledUserDismissed() {
        try {
             return user != null && user.fired == 1;
        } catch (Exception e) {
            return false;
        }
    }

    private String formatDateSec(long dateSec) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return format.format(new Date(dateSec * 1000L));
        } catch (Exception e) {
            return String.valueOf(dateSec);
        }
    }

    private boolean isBeforeTemporaryExceptionEndDate() {
        try {
            Calendar now = Calendar.getInstance();

            Calendar endDate = Calendar.getInstance();
            endDate.set(Calendar.YEAR, 2027);
            endDate.set(Calendar.MONTH, Calendar.JANUARY);
            endDate.set(Calendar.DAY_OF_MONTH, 1);
            endDate.set(Calendar.HOUR_OF_DAY, 0);
            endDate.set(Calendar.MINUTE, 0);
            endDate.set(Calendar.SECOND, 0);
            endDate.set(Calendar.MILLISECOND, 0);

            return now.getTimeInMillis() < endDate.getTimeInMillis();

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlQuestionAnswer/isBeforeTemporaryExceptionEndDate",
                    "Exception e: " + e
            );

            return false;
        }
    }
}
