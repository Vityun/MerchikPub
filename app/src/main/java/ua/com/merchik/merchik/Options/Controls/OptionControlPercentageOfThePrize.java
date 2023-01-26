package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPercentageOfThePrize;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.ReclamationPercentageSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

/**
 * 17.01.23.
 * Опция Контроля: Процент Премии (135061)
 * <p>
 * Функция рассчитывает процент премии исполнителю (от стоимости, которую платит клиент) в
 * зависимости от качества работы (количества рекламаций за 30-ь дней от Дат)
 */
public class OptionControlPercentageOfThePrize<T> extends OptionControl {
    public int OPTION_CONTROL_PERCENTAGE_OF_THE_PRIZE_ID = 135061;

    // option data
    private boolean signal = false;
    private String strReg = "";
    private float rez = 0.75f;
    private int percent;
    private String period = "";

    private List<TasksAndReclamationsSDB> reclamations;

    // document data
    private long dateFrom;
    private long dateTo;
    private Date dt;
    private int kps;
    private int percentReclamation;
    private Double percentReclamation2;
    private float percentReclamationConst;

    private UsersSDB usersSDB;

    public OptionControlPercentageOfThePrize(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        getDocumentVar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executeOption();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Build.VERSION.SDK_INT: ").append(Build.VERSION.SDK_INT).append("  Build.VERSION_CODES.N: ").append(Build.VERSION_CODES.N);
            Globals.writeToMLOG("INFO", "OptionControlPercentageOfThePrize", "sb: " + sb);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            WpDataDB wp = (WpDataDB) document;

            dt = wp.getDt();
            dateFrom = Clock.getDatePeriodLong(wp.getDt().getTime(), -15);
            dateTo = Clock.getDatePeriodLong(wp.getDt().getTime(), -1);

            period = " з " + Clock.getHumanTimeSecPattern(dateFrom / 1000, "dd-MM-yy") + " по " + Clock.getHumanTimeSecPattern(dateTo / 1000, "dd-MM-yy") + "";

            kps = WpDataRealm.getWpDataBy(new Date(dateFrom), new Date(dateTo), 1).size();

            usersSDB = SQL_DB.usersDao().getById(wp.getUser_id());
        }
    }

    private void executeOption() {
        reclamations = filterReclamationAuthorNotUserSupervisor(SQL_DB.tarDao().getTARForOptionControl135061(0, dateFrom / 1000, dateTo / 1000));

        if (usersSDB.department == 3 || usersSDB.department == 8) {
            java.sql.Date dateF = new java.sql.Date(dateFrom);
            java.sql.Date dateT = new java.sql.Date(dateTo);
            List<ReclamationPercentageSDB> percentageSDBS = SQL_DB.reclamationPercentageDao().getAll(dateF, dateT, 1);
            if (percentageSDBS != null) {
                percentReclamationConst = percentageSDBS.get(0).percent;
            } else {
                percentReclamationConst = 1.6f;
            }

            strReg = "% Киева";
        } else {
            java.sql.Date dateF = new java.sql.Date(dateFrom);
            java.sql.Date dateT = new java.sql.Date(dateTo);
            List<ReclamationPercentageSDB> percentageSDBS = SQL_DB.reclamationPercentageDao().getAll(dateF, dateT, 2);
            if (percentageSDBS != null) {
                percentReclamationConst = percentageSDBS.get(0).percent;
            } else {
                percentReclamationConst = 1.8f;
            }

            strReg = "% Регионов";
        }

        StringBuilder formula = new StringBuilder();

        //4.0. определим для исполнителя процент рекламаций
        if (kps > 0) {
            percentReclamation = 100 * reclamations.size() / kps;
            percentReclamation2 = Double.valueOf(100 * reclamations.size() / kps);
        }

        formula.append("\n\nВідсоток рекламацій = ( 100 * Кількість рекламацій / Кількість звітів )");

        //5.0. определим процент
        if (kps == 0) {  //для "молодых" бойцов
            stringBuilderMsg.append("За період ").append(period).append(" Ваші звіти не знайдено! Ви отримаєте Преміальні на базовому рівні.");
            signal = false;
        } else if (usersSDB.reportDate20 != null && usersSDB.reportDate20.getTime() > dt.getTime()) { //до 20-й отчетности
            stringBuilderMsg.append("Ви отримаєте Преміальні на базовому рівні тому що ще не провели свою 20-ту звітність. В майбутньому у Вас буде можливість збільшити цей вітсоток! (якщо відсутні рекламації).");
            signal = false;
        } else if (percentReclamation < (percentReclamationConst / 2)) { //менее 50% от СРЕДНЕГО
            rez = rez + 0.05f;

            stringBuilderMsg.append("Ви отримаєте Преміальні на 5% більше інших тому що ").append(period).append(", співвідношення отриманих Вами рекламацій (")
                    .append(reclamations.size()).append("рек) к виконаним роботам (").append(kps).append("кпс) складає: (")
                    .append(percentReclamation).append("%). Це набагато менше середнього ").append(percentReclamationConst)
                    .append(strReg).append(". (менше 50% від середнього)");
            signal = false;

            percent = 5; //это для отображения на кнопке  135412 - ПроцентПремиальных в МВС
        } else if (percentReclamation >= (percentReclamationConst / 2) && percentReclamation <= percentReclamationConst) {

            stringBuilderMsg.append("Ви можете отримати Преміальні на 5% більше інших якщо за 14-ть ")
                    .append(period).append(" будете отримувати менше рекламацій! Якщо співвідношення отриманих Вами рекламацій до вик. робіт складає меньше: (")
                    .append((percentReclamationConst / 2)).append("). У Вас наразі ").append(percentReclamation).append("%");
            signal = false;

            percent = 0;//это для отображения на кнопке  135412 - ПроцентПремиальных в МВС
        } else if (percentReclamation >= percentReclamationConst && percentReclamation <= (percentReclamationConst * 1.5)) {
            rez = rez - 0.05f;

            stringBuilderMsg.append("Ви можете отримати Преміальні на 5% більше, якщо за 14-ть діб ").append(period)
                    .append(" будете отримувати менше рекламацій! Якщо співвідношення отриманих Вами рекламацій до кількості вик. робіт (кпс) буде складати менше ")
                    .append(percentReclamationConst).append("%. Наразі у Вас (").append(reclamations.size()).append("рек) та співвідношення до вик. робіт (")
                    .append(kps).append("кпс) складає: (").append(percentReclamation).append("%). Це більше середнього ").append(percentReclamationConst)
                    .append(strReg).append(".");
            signal = true;

            percent = -5; //это для отображения на кнопке  135412 - ПроцентПремиальных в МВС
        } else if (percentReclamation > (percentReclamationConst * 1.5) && kps < 20) {  //для ребят у которых МАЛО кпс делаем поблажку
            rez = rez - 0.05f;

            stringBuilderMsg.append("Ви можете отримати Преміальні на 5% більше, якщо за 14-ть діб ").append(period)
                    .append(" будете отримувати менше рекламацій! Якщо співвідношення отриманих Вами рекламацій до кількості вик. робіт (кпс) буде складати меньш за ")
                    .append(percentReclamationConst).append("%. Наразі у Вас (").append(reclamations.size()).append("рек) та співвідношення до вик. роботам (")
                    .append(kps).append("кпс) складає: (").append(percentReclamation).append("%). Це на багато більше середнього ")
                    .append(percentReclamationConst).append(strReg).append(". (більше, ніж в півтора рази від середнього)");
            signal = true;

            percent = -5; //это для отображения на кнопке  135412 - ПроцентПремиальных в МВС
        } else if (percentReclamation > (percentReclamationConst * 1.5)) {
            rez = rez - 0.1f;

            stringBuilderMsg.append("Ви можете отримати Преміальні на 10% більше, якщо за 14-ть діб ").append(period)
                    .append(" будете отримувати менше рекламацій! Якщо співвідношення отриманих Вами рекламацій до кількості вик. робіт (кпс) буде складати меньш за ")
                    .append(percentReclamationConst).append("%. З ").append(Clock.getHumanTimeSecPattern(dateFrom / 1000, "dd-MM-yy")).append(" по ")
                    .append(Clock.getHumanTimeSecPattern(dateTo / 1000, "dd-MM-yy")).append(" у Вас (").append(reclamations.size()).append("рек) та співвідношення до вик. роботам (")
                    .append(kps).append("кпс) складає: (").append(percentReclamation).append("%). Це на багато більше середнього ").append(percentReclamationConst).append(strReg)
                    .append(". (більше, ніж в півтора рази від середнього)");
            signal = true;

            percent = -10; //это для отображения на кнопке  135412 - ПроцентПремиальных в МВС
        }


        stringBuilderMsg.append(formula);
        stringBuilderMsg = new StringBuilder();    // Потому что я могу

        OptionButtonPercentageOfThePrize buttonOption = new OptionButtonPercentageOfThePrize<>(context, document, optionDB, msgType, nnkMode);
        buttonOption.date = period;
        buttonOption.kps = kps;
        buttonOption.reclam = reclamations.size();
        buttonOption.reclamPer = percentReclamation2;
        buttonOption.maxPer = percentReclamationConst;
        buttonOption.bonus = percent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            buttonOption.executeOption();
            spannableStringBuilder = buttonOption.getMsg();
        }


        saveOptionResultInDB();
        if (signal) {
            if (optionDB.getBlockPns().equals("1")) {
                setIsBlockOption(signal);
                spannableStringBuilder.append("\n\n").append("Документ проведен не буде!");
            } else {
                spannableStringBuilder.append("\n\n").append("Ви можете отримати Преміальні БІЛЬШЕ, якщо будете отримувати менше рекламацій.");
            }
        }
    }


    /**
     * 18.01.23
     * Эта штука должна отсеять рекламации которые поставили мерчику не его руководители
     * и поставленные после 20го отчёта мерчандайзера
     */
    private List<TasksAndReclamationsSDB> filterReclamationAuthorNotUserSupervisor(List<TasksAndReclamationsSDB> reclamations) {
        List<TasksAndReclamationsSDB> res = new ArrayList<>();
        for (TasksAndReclamationsSDB item : reclamations) {

            // Рекламацию оставляем на месте, если у мерчандайзера рекламацию поставил НЕ его руковдитель.
            // TODO дописать функционал когда будут данные

            // Рекламацию оставляем на месте, если она добавлена ПОСЛЕ 20го отчёта мерчандайзера
            if (usersSDB.reportDate20 != null && item.dt > usersSDB.reportDate20.getTime() / 1000) {
                res.add(item);
            }
        }
        return res;
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

                optionDB.setPercent(String.valueOf(rez));
                optionDB.setPrice(String.valueOf(percentReclamationConst));
                optionDB.setAmountMin(reclamations != null ? String.valueOf(reclamations.size()) : "0");
                optionDB.setAmountMax(String.valueOf(percent));

                realm.insertOrUpdate(optionDB);
            }
        });
    }


}
