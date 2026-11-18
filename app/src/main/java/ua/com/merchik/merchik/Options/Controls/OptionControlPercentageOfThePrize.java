package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TimeZone;

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
    public boolean signal = false;
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
    private Double percentReclamation;
    private Double percentReclamation2;
    private float percentReclamationConst;

    private UsersSDB usersSDB;
    private PremiumReclamationPeriod reclamationPeriod;

    private boolean periodResolved;
    private boolean reportsLoaded;
    private boolean calculationComplete;
    private boolean resultSaved;
    private Integer sourceReclamationCount;
    private Boolean reclamationSourceWasNull;
    private Integer reclamationUserId;
    private Integer thresholdType;
    private Integer thresholdRowCount;
    private ReclamationPercentageSDB thresholdRow;
    private String calculationStage = "document";
    private String calculationDecision = "NOT_CALCULATED";

    public OptionControlPercentageOfThePrize(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        try {
            this.context = context;
            this.document = document;
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            this.unlockCodeResultListener = unlockCodeResultListener;
            getDocumentVar();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                executeOption();
            } else {
                calculationDecision = "UNSUPPORTED_SDK";
                writeCalculationLog(null);
                StringBuilder sb = new StringBuilder();
                sb.append("Build.VERSION.SDK_INT: ").append(Build.VERSION.SDK_INT).append("  Build.VERSION_CODES.N: ").append(Build.VERSION_CODES.N);
                Globals.writeToMLOG("INFO", "OptionControlPercentageOfThePrize", "sb: " + sb);
            }
        }catch (Exception e){
            if ("NOT_CALCULATED".equals(calculationDecision)) calculationDecision = "CALCULATION_EXCEPTION";
            writeCalculationLog(e);
            logOptionError("init/executeOption", e);
            Log.e("test_135412", "Exception e: " + e);
            e.printStackTrace();
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            WpDataDB wp = (WpDataDB) document;
            wpDataDB = wp;

            dt = wp.getDt();
            if (dt == null) calculationDecision = "DOCUMENT_DATE_MISSING";
            dateFrom = Clock.getDatePeriodLong(wp.getDt().getTime(), -15);
            dateTo = Clock.getDatePeriodLong(wp.getDt().getTime(), -1);
            reclamationPeriod = new PremiumReclamationPeriod(dt, TimeZone.getDefault());
            periodResolved = true;

            period = " з " + Clock.getHumanTimeSecPattern(dateFrom / 1000, "dd-MM-yy") + " по " + Clock.getHumanTimeSecPattern(dateTo / 1000, "dd-MM-yy") + "";

            calculationStage = "reports";
            List<WpDataDB> wpDataDBList =  WpDataRealm.getWpDataBy(new Date(dateFrom), new Date(dateTo), 1, null, null, wp.getUser_id());
            kps = wpDataDBList.size();
            reportsLoaded = true;

            calculationStage = "executor";
            usersSDB = SQL_DB.usersDao().getById(wp.getUser_id());
        }
    }

    private void executeOption() {
        if (wpDataDB == null || dt == null || usersSDB == null) {
            calculationDecision = wpDataDB == null ? "VISIT_MISSING"
                    : dt == null ? "DOCUMENT_DATE_MISSING" : "EXECUTOR_MISSING";
            Globals.writeToMLOG(
                    "ERROR",
                    "OptionControlPercentageOfThePrize/executeOption",
                    "required data is null, wpDataDB=" + wpDataDB + ", dt=" + dt + ", usersSDB=" + usersSDB
            );
            signal = false;
            saveOptionResultInDB();
            setIsBlockOption(false);
            return;
        }

        calculationStage = "reclamations";
        reclamationUserId = Globals.userId;
        List<TasksAndReclamationsSDB> sourceReclamations = SQL_DB.tarDao().getTARForOptionControl135061(
                0, reclamationPeriod.fromSeconds, reclamationPeriod.toExclusiveSeconds, Globals.userId);
        sourceReclamationCount = sourceReclamations != null ? sourceReclamations.size() : 0;
        reclamationSourceWasNull = sourceReclamations == null;
        reclamations = filterReclamationAuthorNotUserSupervisor(sourceReclamations != null ? sourceReclamations : Collections.emptyList());

        calculationStage = "threshold";
        if (Objects.equals(usersSDB.department, 3) || Objects.equals(usersSDB.department, 8)) {
            thresholdType = 1;
            java.sql.Date dateF = new java.sql.Date(dateFrom);
            java.sql.Date dateT = new java.sql.Date(dateTo);
            List<ReclamationPercentageSDB> percentageSDBS = SQL_DB.reclamationPercentageDao().getAll(dateF, dateT, 1);
            thresholdRowCount = percentageSDBS != null ? percentageSDBS.size() : 0;
            if (percentageSDBS != null && !percentageSDBS.isEmpty()) {
                thresholdRow = percentageSDBS.get(0);
                percentReclamationConst = percentageSDBS.get(0).percent;
            } else {
                percentReclamationConst = 1.6f;
            }

            strReg = "% Киева";
        } else {
            thresholdType = 2;
            java.sql.Date dateF = new java.sql.Date(dateFrom);
            java.sql.Date dateT = new java.sql.Date(dateTo);
            List<ReclamationPercentageSDB> percentageSDBS = SQL_DB.reclamationPercentageDao().getAll(dateF, dateT, 2);
            thresholdRowCount = percentageSDBS != null ? percentageSDBS.size() : 0;
            if (percentageSDBS != null && !percentageSDBS.isEmpty()) {
                thresholdRow = percentageSDBS.get(0);
                percentReclamationConst = percentageSDBS.get(0).percent;
            } else {
                percentReclamationConst = 1.8f;
            }

            strReg = "% Регионов";
        }

        StringBuilder formula = new StringBuilder();

        //4.0. определим для исполнителя процент рекламаций
        if (kps > 0) {
            percentReclamation = 100.0 * reclamations.size() / kps;
            percentReclamation2 = percentReclamation;
        } else {
            percentReclamation = 0.00;
            percentReclamation2 = 0.00;
        }

        formula.append("\n\nВідсоток рекламацій = ( 100 * Кількість рекламацій / Кількість звітів )");

        //5.0. определим процент
        calculationStage = "percentage";
        calculationDecision = "NO_MATCHING_PERCENT_BRANCH";
        if (kps == 0) {  //для "молодых" бойцов
            calculationDecision = "NO_COMPLETED_REPORTS_IN_PERIOD";
            stringBuilderMsg.append("За період ").append(period).append(" Ваші звіти не знайдено! Ви отримаєте Преміальні на базовому рівні.");
            signal = false;
        } else if (usersSDB.reportDate20 != null && usersSDB.reportDate20.getTime() > dt.getTime()) { //до 20-й отчетности
            calculationDecision = "BEFORE_20TH_REPORT";
            stringBuilderMsg.append("Ви отримаєте Преміальні на базовому рівні тому що ще не провели свою 20-ту звітність. В майбутньому у Вас буде можливість збільшити цей вітсоток! (якщо відсутні рекламації).");
            signal = false;
        } else if (percentReclamation < (percentReclamationConst / 2)) { //менее 50% от СРЕДНЕГО
            calculationDecision = "BELOW_HALF_THRESHOLD_BONUS_5";
            rez = rez + 0.05f;

            stringBuilderMsg.append("Ви отримаєте Преміальні на 5% більше інших тому що ").append(period).append(", співвідношення отриманих Вами рекламацій (")
                    .append(reclamations.size()).append("рек) к виконаним роботам (").append(kps).append("кпс) складає: (")
                    .append(percentReclamation).append("%). Це набагато менше середнього ").append(percentReclamationConst)
                    .append(strReg).append(". (менше 50% від середнього)");
            signal = false;

            percent = 5; //это для отображения на кнопке  135412 - ПроцентПремиальных в МВС
        } else if (percentReclamation >= (percentReclamationConst / 2) && percentReclamation <= percentReclamationConst) {
            calculationDecision = "NEUTRAL_RECLAMATION_RANGE";

            stringBuilderMsg.append("Ви можете отримати Преміальні на 5% більше інших якщо за 14-ть ")
                    .append(period).append(" будете отримувати менше рекламацій! Якщо співвідношення отриманих Вами рекламацій до вик. робіт складає меньше: (")
                    .append((percentReclamationConst / 2)).append("). У Вас наразі ").append(percentReclamation).append("%");
            signal = false;

            percent = 0;//это для отображения на кнопке  135412 - ПроцентПремиальных в МВС
        } else if (percentReclamation >= percentReclamationConst && percentReclamation <= (percentReclamationConst * 1.5)) {
            calculationDecision = "ABOVE_THRESHOLD_REDUCTION_5";
            rez = rez - 0.05f;

            stringBuilderMsg.append("Ви можете отримати Преміальні на 5% більше, якщо за 14-ть діб ").append(period)
                    .append(" будете отримувати менше рекламацій! Якщо співвідношення отриманих Вами рекламацій до кількості вик. робіт (кпс) буде складати менше ")
                    .append(percentReclamationConst).append("%. Наразі у Вас (").append(reclamations.size()).append("рек) та співвідношення до вик. робіт (")
                    .append(kps).append("кпс) складає: (").append(percentReclamation).append("%). Це більше середнього ").append(percentReclamationConst)
                    .append(strReg).append(".");
            signal = true;

            percent = -5; //это для отображения на кнопке  135412 - ПроцентПремиальных в МВС
        } else if (percentReclamation > (percentReclamationConst * 1.5) && kps < 20) {  //для ребят у которых МАЛО кпс делаем поблажку
            calculationDecision = "ABOVE_1_5_THRESHOLD_FEWER_20_REPORTS_REDUCTION_5";
            rez = rez - 0.05f;

            stringBuilderMsg.append("Ви можете отримати Преміальні на 5% більше, якщо за 14-ть діб ").append(period)
                    .append(" будете отримувати менше рекламацій! Якщо співвідношення отриманих Вами рекламацій до кількості вик. робіт (кпс) буде складати меньш за ")
                    .append(percentReclamationConst).append("%. Наразі у Вас (").append(reclamations.size()).append("рек) та співвідношення до вик. роботам (")
                    .append(kps).append("кпс) складає: (").append(percentReclamation).append("%). Це на багато більше середнього ")
                    .append(percentReclamationConst).append(strReg).append(". (більше, ніж в півтора рази від середнього)");
            signal = true;

            percent = -5; //это для отображения на кнопке  135412 - ПроцентПремиальных в МВС
        } else if (percentReclamation > (percentReclamationConst * 1.5)) {
            calculationDecision = "ABOVE_1_5_THRESHOLD_REDUCTION_10";
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


        calculationComplete = true;
        calculationStage = "presentation";
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

        calculationStage = "blocking/unlock";
        if (signal) {
            if (optionDB.getBlockPns().equals("1")) {
                setIsBlockOption(signal);
                spannableStringBuilder.append("\n\n").append("Документ проведен не буде!");
            } else {
                spannableStringBuilder.append("\n\n").append("Ви можете отримати Преміальні БІЛЬШЕ, якщо будете отримувати менше рекламацій.");
            }
        }
        checkUnlockCode(optionDB);
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
            if (PremiumReclamationPeriod.afterTwentiethReport(item.dtRealPost, usersSDB.reportDate20)) {
                res.add(item);
            }
        }
        return res;
    }

    /**
     * Сохранение данных об опции контроля в БД
     */
    private void saveOptionResultInDB() {
        calculationStage = "save";
        RealmManager.INSTANCE.executeTransaction(realm -> {
            Log.e("OptionControlPercentageOfThePrize","signal: " + signal);
            if (optionDB != null) {
                if (signal) {
                    String penalty = "0.00";
                    if (wpDataDB != null) {
                        penalty = PremiumPercentagePolicy.penalty(wpDataDB.getCash_zakaz(), percent);
                    } else {
                        Globals.writeToMLOG(
                                "ERROR",
                                "OptionControlPercentageOfThePrize/saveOptionResultInDB",
                                "wpDataDB is null, optionId=" + optionDB.getOptionId() + ", codeDad2=" + optionDB.getCodeDad2()
                        );
                    }
                    optionDB.setIsSignal("1");
                    optionDB.setSumPenalty(penalty);
                } else {
                    optionDB.setIsSignal("2");
                    optionDB.setSumPenalty("0.00");
                }

                optionDB.setPercent(String.valueOf(rez));
                optionDB.setPrice(String.valueOf(percentReclamationConst));
                optionDB.setAmountMin(reclamations != null ? String.valueOf(reclamations.size()) : "0");
                optionDB.setAmountMax(String.valueOf(percent));

                realm.insertOrUpdate(optionDB);
            }
        });
        resultSaved = optionDB != null;
        calculationStage = resultSaved ? "saved" : "option_missing";
        writeCalculationLog(null);
    }

    private void writeCalculationLog(Exception error) {
        try {
            JsonObject log = new JsonObject();
            log.addProperty("control_id", OPTION_CONTROL_PERCENTAGE_OF_THE_PRIZE_ID);
            log.addProperty("stage", calculationStage);
            log.addProperty("mode", nnkMode != null ? nnkMode.name() : null);
            log.addProperty("dad2", wpDataDB != null ? String.valueOf(wpDataDB.getCode_dad2())
                    : optionDB != null ? optionDB.getCodeDad2() : null);
            log.addProperty("document", wpDataDB != null ? wpDataDB.getDoc_num_otchet() : null);
            log.addProperty("executor_id", wpDataDB != null ? wpDataDB.getUser_id() : null);
            log.addProperty("current_user_id", Globals.userId);
            log.add("document_date", PremiumPercentageDiagnostics.date(dt));
            log.addProperty("data_source", "LOCAL_DATABASES");

            JsonObject history = new JsonObject();
            history.addProperty("period_basis", "DOCUMENT_DATE");
            history.addProperty("from_offset_days", -15);
            history.addProperty("to_offset_days", -1);
            history.addProperty("bounds", "BETWEEN_INCLUSIVE");
            history.add("from", PremiumPercentageDiagnostics.date(periodResolved ? new Date(dateFrom) : null));
            history.add("to", PremiumPercentageDiagnostics.date(periodResolved ? new Date(dateTo) : null));
            history.addProperty("reclamations_date_field", "dt_real_post");
            history.addProperty("reclamations_bounds", "FROM_INCLUSIVE_TO_EXCLUSIVE");
            history.addProperty("reclamations_from_seconds", reclamationPeriod != null ? reclamationPeriod.fromSeconds : null);
            history.addProperty("reclamations_to_exclusive_seconds", reclamationPeriod != null ? reclamationPeriod.toExclusiveSeconds : null);
            history.addProperty("reports_status", 1);
            history.addProperty("reports_count", reportsLoaded ? kps : null);
            history.addProperty("reclamations_type", 0);
            history.addProperty("reclamations_vinovnik_id", reclamationUserId);
            history.addProperty("reclamations_source_was_null", reclamationSourceWasNull);
            history.addProperty("reclamations_raw", sourceReclamationCount);
            history.addProperty("reclamations_count", reclamations != null ? reclamations.size() : null);
            history.addProperty("reclamations_excluded", reclamations != null && sourceReclamationCount != null
                    ? sourceReclamationCount - reclamations.size() : null);
            history.add("report_date_20", PremiumPercentageDiagnostics.date(usersSDB != null ? usersSDB.reportDate20 : null));
            history.addProperty("reclamation_filter", "dt_real_post_seconds > report_date_20_seconds; missing report_date_20 excludes all");
            history.addProperty("reclamation_percent", percentReclamation);
            log.add("history", history);

            JsonObject threshold = new JsonObject();
            threshold.addProperty("department", usersSDB != null ? usersSDB.department : null);
            threshold.addProperty("type", thresholdType);
            threshold.addProperty("rows", thresholdRowCount);
            threshold.addProperty("source", thresholdRowCount == null ? "NOT_LOADED" : thresholdRow != null ? "DATABASE" : "DEFAULT");
            threshold.addProperty("row_id", thresholdRow != null ? thresholdRow.id : null);
            threshold.add("row_date", PremiumPercentageDiagnostics.date(thresholdRow != null ? thresholdRow.dt : null));
            threshold.addProperty("row_percent", thresholdRow != null && thresholdRow.percent != null ? thresholdRow.percent.toString() : null);
            threshold.addProperty("percent", percentReclamation != null ? String.valueOf(percentReclamationConst) : null);
            log.add("threshold", threshold);
            log.add("result", PremiumPercentageDiagnostics.result(wpDataDB != null ? wpDataDB.getCash_zakaz() : null,
                    percent, calculationComplete, calculationDecision));
            log.addProperty("signal", signal);
            log.addProperty("coefficient", rez);
            log.addProperty("result_saved", resultSaved);
            if (resultSaved) {
                JsonObject stored = new JsonObject();
                stored.addProperty("amountMax", optionDB.getAmountMax());
                stored.addProperty("sumPenalty", optionDB.getSumPenalty());
                stored.addProperty("percent", optionDB.getPercent());
                stored.addProperty("price", optionDB.getPrice());
                stored.addProperty("amountMin", optionDB.getAmountMin());
                log.add("stored", stored);
            }
            if (error != null) log.addProperty("error_class", error.getClass().getName());
            Globals.writeToMLOG(error != null ? "ERROR" : "INFO", "PremiumPercentage/calculation", log.toString());
        } catch (Exception loggingError) {
            // Diagnostics must not interrupt the option calculation or its persistence.
            Log.e("PremiumPercentage", "Calculation diagnostics failed", loggingError);
        }
    }

}
