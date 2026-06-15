package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
    private static final int THEME_NO_COMPLAINTS = 600;

    private static final long DAY_SEC = 24L * 60L * 60L;

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
                this.dateToSec = documentDateSec + 24L * 60L * 60L;

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
             * 3.0. Сформируем сообщение и базовый сигнал
             */
            int complaintsCount = complaints != null ? complaints.size() : 0;

            if (complaintsCount == 0) {

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
             * 4.0. Исключения из 1С
             */

            /*
             * 4.1. Исключение по количеству отчетов у исполнителя.
             *
             * Аналог 1С:
             * Если сигнал есть, документ = ОтчетИсполнителя,
             * и Кон.ДатаОМ20 пустая или Кон.ДатаОМ20 >= ДатаДок,
             * значит до 20-го отчета подача замечаний не обязательна.
             */
            if (signal && isExecutorReportDocument() && isBeforeTwentiethReport()) {
                stringBuilderMsg
                        .append(" Але, до 20-го звіту це не обов'язково.");

                signal = false;
            }

            /*
             * 4.2. Исключение:
             * если это разбор з/п уволенного бойца,
             * то получение жалоб не контролируем.
             */
            if (signal && theme == THEME_DISMISSED_SALARY_REVIEW && isControlledUserDismissed()) {
                stringBuilderMsg
                        .append(" Но, проверяемый уволен. Поэтому получение замечаний (жалоб) НЕ контролируем!");

                signal = false;
            }

            /*
             * 4.3. Новое исключение из 1С от 14.06.2026:
             * на протяжении примерно двух недель исполнитель может выбрать вариант "Замечаний нет".
             *
             * Логика:
             * - делим контролируемый период пополам;
             * - ищем тему 600 в первой половине;
             * - ищем тему 600 во второй половине;
             * - если в первой половине нет, а во второй есть,
             *   значит делаем исключение до рассчитанной даты.
             */
            if (signal && isExecutorReportDocument()) {
                applyNoComplaintsTemporaryWindowException();
            }

            /*
             * 4.4. Временное исключение до 01.07.2026.
             *
             * Важно:
             * в 1С проверяется дата документа:
             * Если Дат < '01.07.2026'
             */
            if (signal && isExecutorReportDocument() && isDocumentBeforeTemporaryExceptionEndDate()) {
                stringBuilderMsg
                        .append("\nАле до 01.07.2026 зроблено виключення");

                signal = false;
            }

            /*
             * 5.0. КАНОН.
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
                    THEME_OTHER,              // 6
                    THEME_IDEA,               // 607
                    THEME_PAYMENT_INCREASE,   // 610
                    THEME_MANAGER_WRONG       // 612
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

    private List<QuestionAnswerDB> getNoComplaintsAnswers(long fromSec, long toSec) {
        try {
            return SQL_DB.questionAnswerDao().getComplaintsByUserAndPeriod(
                    userId,
                    fromSec,
                    toSec,
                    Collections.singletonList(THEME_NO_COMPLAINTS)
            );

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlQuestionAnswer/getNoComplaintsAnswers",
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

    private void applyNoComplaintsTemporaryWindowException() {
        try {
            long periodFromSec = normalizeDateSecToDay(dateFromSec);
            long periodToSec = normalizeDateSecToDay(dateToSec);

            if (periodToSec < periodFromSec) {
                return;
            }

            long periodDays = Math.round((periodToSec - periodFromSec) / (double) DAY_SEC);
            long deltaDays = Math.round(periodDays / 2.0);

            long firstPartFromSec = periodFromSec;
            long firstPartToSec = periodFromSec + deltaDays * DAY_SEC;

            long secondPartFromSec = periodFromSec + (deltaDays + 1L) * DAY_SEC;
            long secondPartToSec = periodToSec;

            List<QuestionAnswerDB> firstPartAnswers =
                    getNoComplaintsAnswers(firstPartFromSec, firstPartToSec);

            List<QuestionAnswerDB> secondPartAnswers =
                    getNoComplaintsAnswers(secondPartFromSec, secondPartToSec);

            int firstCount = firstPartAnswers != null ? firstPartAnswers.size() : 0;
            int secondCount = secondPartAnswers != null ? secondPartAnswers.size() : 0;

            if (firstCount == 0 && secondCount > 0) {
                long answerDateSec = getLatestQuestionAnswerDateSec(secondPartAnswers);

                if (answerDateSec <= 0L) {
                    return;
                }

                long documentDateSec = normalizeDateSecToDay(getDocumentDateSec(wpDataDB));

                /*
                 * Аналог 1С:
                 * ДатИск = Дат + (ДатЗап - (ДатС + Дельта + 1));
                 */
                long exceptionDateSec = documentDateSec + (answerDateSec - secondPartFromSec);

                stringBuilderMsg
                        .append(", але до ")
                        .append(formatDateSec(exceptionDateSec))
                        .append(", зроблено виключення.");

                signal = false;
            }

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlQuestionAnswer/applyNoComplaintsTemporaryWindowException",
                    "Exception e: " + e
            );
        }
    }

    private long getLatestQuestionAnswerDateSec(List<QuestionAnswerDB> answers) {
        try {
            if (answers == null || answers.isEmpty()) {
                return 0L;
            }

            long latestDateSec = 0L;

            for (QuestionAnswerDB answer : answers) {
                long answerDateSec = getQuestionAnswerDateSec(answer);

                if (answerDateSec > latestDateSec) {
                    latestDateSec = answerDateSec;
                }
            }

            return normalizeDateSecToDay(latestDateSec);

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlQuestionAnswer/getLatestQuestionAnswerDateSec",
                    "Exception e: " + e
            );

            return 0L;
        }
    }

    private long getQuestionAnswerDateSec(QuestionAnswerDB answer) {
        try {
            if (answer == null) {
                return 0L;
            }

            if (answer.getDt() != null) {
                return answer.getDt() / 1000L;
            }

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlQuestionAnswer/getQuestionAnswerDateSec",
                    "Exception e: " + e
            );
        }

        return 0L;
    }

    private boolean isBeforeTwentiethReport() {
        try {
            java.util.Date docDate = wpDataDB != null ? wpDataDB.getDt() : null;

            if (user == null) {
                return true;
            }

            /*
             * В 1С:
             * Кон.ДатаОМ20
             *
             * Здесь нужно использовать твое Android-поле,
             * которое соответствует ДатаОМ20.
             *
             * Старое user.reportDate05 нужно заменить на новое поле.
             */
            if (user.reportDate20 == null) {
                return true;
            }

            if (docDate == null) {
                return true;
            }

            Calendar report20Calendar = Calendar.getInstance();
            report20Calendar.setTime(user.reportDate20);
            clearTime(report20Calendar);

            Calendar docCalendar = Calendar.getInstance();
            docCalendar.setTime(docDate);
            clearTime(docCalendar);

            return report20Calendar.getTimeInMillis() >= docCalendar.getTimeInMillis();

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlQuestionAnswer/isBeforeTwentiethReport",
                    "Exception e: " + e
            );

            return true;
        }
    }

    private boolean isExecutorReportDocument() {
        /*
         * В 1С условие:
         * ДокИст.Вид() = "ОтчетИсполнителя"
         *
         * Если эта Android-опция вызывается только из ОтчетаИсполнителя,
         * можно оставить true.
         *
         * Если опция общая для разных документов —
         * сюда надо поставить реальную проверку типа документа.
         */
        return true;
    }



    private boolean isDocumentBeforeTemporaryExceptionEndDate() {
        try {
            long documentDateSec = normalizeDateSecToDay(getDocumentDateSec(wpDataDB));

            Calendar endDate = Calendar.getInstance();
            endDate.set(Calendar.YEAR, 2026);
            endDate.set(Calendar.MONTH, Calendar.JULY);
            endDate.set(Calendar.DAY_OF_MONTH, 1);
            endDate.set(Calendar.HOUR_OF_DAY, 0);
            endDate.set(Calendar.MINUTE, 0);
            endDate.set(Calendar.SECOND, 0);
            endDate.set(Calendar.MILLISECOND, 0);

            long endDateSec = endDate.getTimeInMillis() / 1000L;

            return documentDateSec < endDateSec;

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlQuestionAnswer/isDocumentBeforeTemporaryExceptionEndDate",
                    "Exception e: " + e
            );

            return false;
        }
    }

    private long normalizeDateSecToDay(long dateSec) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dateSec * 1000L);
            clearTime(calendar);
            return calendar.getTimeInMillis() / 1000L;

        } catch (Exception e) {
            return dateSec;
        }
    }

    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

}
