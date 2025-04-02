package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.SMS.SMSLogSDB;
import ua.com.merchik.merchik.data.Database.Room.SMS.SMSPlanSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;

/**
 * 04.01.2023
 * <p>
 * Выполняется проверка редактирования ДО, с момента создания. Предназначена для того, что бы
 * заставить исполнителя внести изменения в предварительно сгенерированный шаблон дет. отчётности.
 */
public class OptionControlCheckDetailedReport<T> extends OptionControl {
    public int OPTION_CONTROL_CHECK_DR_ID = 157352;

    // option data
    public boolean signal = false;
    private long time = 0;
    private int correctionPercentage = 0;
    private int min = 10;    // Минимальное значение из опции, по умолчанию 10

    private WpDataDB wpDataDB;
    private AddressSDB addressSDB;

    // document data
    private long dad2 = 0;
    private Date date;
    private long startWorkTime = 0;


    public OptionControlCheckDetailedReport(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
            StringBuilder sb = new StringBuilder();
            sb.append("Build.VERSION.SDK_INT: ").append(Build.VERSION.SDK_INT).append("  Build.VERSION_CODES.N: ").append(Build.VERSION_CODES.N);
            Globals.writeToMLOG("INFO", "OptionControlCheckDetailedReport", "sb: " + sb);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            WpDataDB wp = (WpDataDB) document;

            wpDataDB = wp;
            addressSDB = SQL_DB.addressDao().getById(wp.getAddr_id());

            dad2 = wp.getCode_dad2();
            date = wp.getDt();
// 03.02.2025 изменил на время начала работ
            startWorkTime = wp.getVisit_start_dt();
//            time = Clock.getDatePeriodLong(date.getTime(), -4) / 1000;    // работает в миллисекундах, по этому перевёл в секунды
            time = Clock.getDatePeriodLong(startWorkTime, -4) / 1000;    // работает в миллисекундах, по этому перевёл в секунды

            try {
                if (!optionDB.getAmountMin().equals("0")) {
                    min = Integer.parseInt(optionDB.getAmountMin());
                }
            } catch (Exception e) {
                Globals.writeToMLOG("INFO", "OptionControlCheckDetailedReport", "Exception e: " + e);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        // Получаем RP(товары) для дальнейшего анализа.
        List<ReportPrepareDB> reportPrepare = ReportPrepareRealm.getReportPrepareByDad2(dad2);

        // 5.0
        reportPrepare = prepareOSVData(reportPrepare,
//                adjustStartTime(wpDataDB.getVisit_start_dt())
                wpDataDB.getVisit_start_dt()
        );

        // 6.0 готовим сообение и сигнал.
        int colSKU = reportPrepare.stream().map(table -> table.colSKU).reduce(0, Integer::sum);
        int err = reportPrepare.stream().map(table -> table.errorExist).reduce(0, Integer::sum);
        int fixesNum = reportPrepare.stream().map(table -> table.fixesNum).reduce(0, Integer::sum);

        try {
            correctionPercentage = (int) Math.round((100.0 * (colSKU - err)) / colSKU);
        } catch (Exception e) {
            correctionPercentage = 0;
        }

        if (time < 0) {
            stringBuilderMsg.append("Роботи по поточному кпс (клієнто/відвідуванню) ще не були початі. Почніть роботи, відредагуйте ДЗ (дет. звіт) та повторіть спробу.");
            signal = true;
        } else if (reportPrepare.isEmpty()) {
            stringBuilderMsg.append("Товарів, по котрим треба перевірити виправлені ДЗ, не знайдено.");
            signal = true;
        } else if (colSKU == 0) {
//            stringBuilderMsg.append("Товарів, по котрим треба виконувати ПЛАН по ФЕЙЧАС не знайдено.");
            stringBuilderMsg.append("Товарів, у котрих визначена їх наявність на вітрині (фейси) у деталіз.звітності не знайдено.");
            signal = true;
        } else if (min > 0 && correctionPercentage < min) {
            stringBuilderMsg.append("Данні деталіз.звітності виправлені у ").append(fixesNum)
                    .append(" товарів, що складає ").append(correctionPercentage)
                    .append("% та менше мінімально допустимого ").append(min)
                    .append("% Ви повинні виправити данні деталіз.звітності у більшої кількості товарів.");
            signal = true;
        } else {
            stringBuilderMsg.append("Данні деталіз.звітності виправлені у ").append(fixesNum)
                    .append(" товарів, що складає ").append(correctionPercentage)
                    .append("% та більше мінімально допустимого ").append(min)
                    .append("% Зауважень немає.");
            signal = false;
        }

        //8.0. виключения на випадок, якщо товару на ТТ взагалі нема
        if (signal) {
            if (Objects.equals(wpDataDB.getUser_opinion_id(), "59")) {
                signal = false;
                spannableStringBuilder.append("\n").append("Cповіщення про ВІДСУТНІСТЬ товару на ТТ замовнику відправлено, сигнал знятий!");
            } else {
                Long dtFrom = wpDataDB.getDt().getTime() / 1000 - 604800;   // -7 дней в секундах.. на самом деле должно біть минус 6, но оно  счтиает старт дня
                Long dtTo = wpDataDB.getDt().getTime() / 1000 + 345600;   // +4 дней в секундах.. на самом деле должно біть минус 3, но оно  счтиает старт дня

                List<SMSPlanSDB> smsPlanSDBS = SQL_DB.smsPlanDao().getAll(dtFrom, dtTo, 1172, wpDataDB.getAddr_id(), wpDataDB.getClient_id());
                List<SMSLogSDB> smsLogSDBS = SQL_DB.smsLogDao().getAll(dtFrom, dtTo, 1172, wpDataDB.getAddr_id(), wpDataDB.getClient_id());

                if (smsPlanSDBS != null && smsPlanSDBS.size() > 0) {
                    signal = false;
                    spannableStringBuilder.append("\n").append("Cповіщення про ВІДСУТНІСТЬ товару на ТТ замовнику відправлено, сигнал знятий!");
                } else if (smsLogSDBS != null && smsLogSDBS.size() > 0) {
                    signal = false;
                    spannableStringBuilder.append("\n").append("Cповіщення об ОТСУТСТВИИ товара заказчику отправлено, сигнал отменён!");
                } else if (addressSDB.tpId == 383) {   // Для АШАН-ов(8196 - у петрова такое тут, странно) которые работают через ДОТ ОФС ДЗ НЕ проверяем
                    if (wpDataDB.getDot_user_id() > 0) {
                        signal = false;
                        stringBuilderMsg.append(", але для Ашанів, по котрим праюємо з ДОТ, ОФС ДЗ не перевіряємо.");
                    }
                } else {
                    spannableStringBuilder.append("\n\nВи зможете зняти сигнал, якщо відтправите Cповіщення замовнику про те, що товар на ТТ ВІДСУТЕН." +
                            "У випадку, якщо на вітрині (і на складі) реально немає частини товару, повідомте про це в Думцi щодо відвідування(см. на кнопку \"Думка про відвідування\")");
                }
            }
        }


        saveOptionResultInDB();
        if (signal) {
            if (optionDB.getBlockPns().equals("1")) {
                setIsBlockOption(signal);
                stringBuilderMsg.append("\n\n").append("Документ проведен не будет!");
            } else {
                stringBuilderMsg.append("\n\n").append("Ви можете отримати Преміальні БІЛЬШЕ, якщо будете редагувати (виправляти помилки) у деталізованому звіті.");
            }
        }

        checkUnlockCode(optionDB);

    }

    /**
     * Заполняем данными с ОСВ
     */
//    private List<ReportPrepareDB> prepareOSVData(List<ReportPrepareDB> reportPrepare) {
//        List<ReportPrepareDB> res = null;
//        if (reportPrepare != null && reportPrepare.size() > 0) {
//            res = RealmManager.INSTANCE.copyFromRealm(reportPrepare);
//            for (ReportPrepareDB item : res) {
//                if (calculateSKU(item.getFace()) == 0) {
//                    item.colSKU = 0;
//                    continue;
//                } else {
//                    item.colSKU = 1;
//                }
//
//                long dtChangeTime = item.getDtChange();
//                if (dtChangeTime < time) {
//                    item.errorExist = 1;
//                    item.note = "исправление не внесено";
//                } else {
//                    item.fixesNum = 1;
//                }
//            }
//        }
//        return res;
//    }
    public static long adjustStartTime(long timeStartWork) {
        // Получаем текущую дату в секундах (без миллисекунд)
        long nowInSeconds = System.currentTimeMillis() / 1000;

        // Переводим обе даты в дни
        long todayDays = nowInSeconds / 86400; // 86400 секунд в сутках
        long startDays = timeStartWork / 86400;

        // Разница в днях
        long diffDays = startDays - todayDays;

        // Определяем, сколько дней нужно отнять
        long daysToSubtract = diffDays >= 3 ? 3 : diffDays;

        // Возвращаем скорректированное время
        return timeStartWork - (daysToSubtract * 86400);
    }

    /**
     * Заполняем данными с ОСВ изменены от 18.02.25
     */
    private List<ReportPrepareDB> prepareOSVData(List<ReportPrepareDB> reportPrepare, long dateStart) {
        List<ReportPrepareDB> res = null;
        long testTime = 0L;
//        Calendar calendar = Calendar.getInstance();
//        int currentHour = calendar.get(Calendar.HOUR_OF_DAY); // Час в формате 0-23
//        if (currentHour < 9)
//            dateStart -= 60 * 6;
        if (reportPrepare != null && !reportPrepare.isEmpty()) {
            res = RealmManager.INSTANCE.copyFromRealm(reportPrepare);
            for (ReportPrepareDB item : res) {
                if (calculateSKU(item.getFace()) == 0) {
                    item.colSKU = 0;
                    continue;
                } else {
                    item.colSKU = 1;
                }
                long time = item.getDtChange();
                if (testTime == 0L)
                    testTime = time;

                if (time != testTime)
                    Log.e("testLOg", "++++++++");

                Log.e("!prepareOSVData!", item.tovarId + ": " + item.dtChange + " < " + dateStart + " = " + (item.dtChange < dateStart));

                if (time < dateStart) {
                    item.errorExist = 1;
                    item.note = "исправление не внесено";
                } else {
                    item.fixesNum = 1;
                }
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
                realm.insertOrUpdate(optionDB);
            }
        });
    }

    /*Подсчёт СКЮ. СКЮ == 1, когда кол фейс > 0*/
    private int calculateSKU(String face) {
        int res = 0;
        if (face != null && !face.equals("")) {
            int faceX = Integer.parseInt(face);
            if (faceX > 0) {
                res = 1;
            }
        }
        return res;
    }
}
