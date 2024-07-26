package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Date;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
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

            time = Clock.getDatePeriodLong(date.getTime(), -4) / 1000;    // работает в миллисекундах, по этому перевёл в секунды

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
        reportPrepare = prepareOSVData(reportPrepare);

        // 6.0 готовим сообение и сигнал.
        int colSKU = reportPrepare.stream().map(table -> table.colSKU).reduce(0, Integer::sum);
        int err = reportPrepare.stream().map(table -> table.errorExist).reduce(0, Integer::sum);
        int fixesNum = reportPrepare.stream().map(table -> table.fixesNum).reduce(0, Integer::sum);

        try {
            correctionPercentage = (int) 100 * (colSKU - err) / colSKU;
        } catch (Exception e) {
            correctionPercentage = 0;
        }

        if (reportPrepare.size() == 0) {
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

        if (addressSDB.tpId == 383) {   // Для АШАН-ов(8196 - у петрова такое тут, странно) которые работают через ДОТ и ФОТ виправлення ДЗ НЕ проверяем
            if (wpDataDB.getDot_user_id() > 0 || wpDataDB.getFot_user_id() > 0) {
                signal = false;
                stringBuilderMsg.append(", але для Ашанів, по котрим праюємо з ДОТ чи ФОТ, виправлення ДЗ не перевіряємо.");
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
    private List<ReportPrepareDB> prepareOSVData(List<ReportPrepareDB> reportPrepare) {
        List<ReportPrepareDB> res = null;
        if (reportPrepare != null && reportPrepare.size() > 0) {
            res = RealmManager.INSTANCE.copyFromRealm(reportPrepare);
            for (ReportPrepareDB item : res) {
                if (calculateSKU(item.getFace()) == 0) {
                    item.colSKU = 0;
                    continue;
                } else {
                    item.colSKU = 1;
                }
                long dtChangeTime = item.getDtChange();
                if (dtChangeTime < time) {
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
