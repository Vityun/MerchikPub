package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;
import android.util.Log;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;

public class OptionControlOverTimelinessOfReporting<T> extends OptionControl {
    public int OPTION_CONTROL_OverTimelinessOfReporting_ID = 1474; // "Контроль своевременности внесения отчетности"

    // 1С: Если Дат < '29.12.2025' Тогда ... исключение (сигнал сбрасываем)
    private static final LocalDate EXCEPTION_BEFORE_DATE = LocalDate.of(2025, 12, 29);

    private long startWorkDocSec;   // ВремРНФДок (то, что записано в документе)
    private long startWorkFactSec;  // ВремРНФакт (docSec или nowSec)
    private Date docDate;           // ДокИст.ДатаДок (плановая дата)
    public boolean signal = false;

    public OptionControlOverTimelinessOfReporting(Context context,
                                                  T document,
                                                  OptionsDB optionDB,
                                                  OptionMassageType msgType,
                                                  Options.NNKMode nnkMode,
                                                  UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;

        if (document instanceof WpDataDB)
            this.wpDataDB = (WpDataDB) document;

        executeOption();
    }

    public void executeOption() {
        spannableStringBuilder.clear();

        // ===== 2.0. читаем документ =====
        docDate = getDocDateFromDocument();   // ДокИст.ДатаДок
        getStartWorkFromDocument();           // заполняет startWorkDocSec (секунды)

        long nowSec = Instant.now().getEpochSecond();

        // 1С: ВремРНФакт = ?(ДокИст.ВРНФакт=0,ТекВрем,ДокИст.ВРНФакт)
        startWorkFactSec = (startWorkDocSec == 0L) ? nowSec : startWorkDocSec;

        // ===== 3.0. считаем окно допустимых дат/времени =====
        ZoneId zone = ZoneId.systemDefault();

        if (docDate == null) {
            // в 1С это маловероятно, но в приложении не падаем
            boolean signal = true;
            massageToUser = "Немає даних про планову дату робіт.";
            finish(signal);
            return;
        }

        LocalDate planDay = docDate.toInstant().atZone(zone).toLocalDate();

        // 1С: НачДня/КонДня — юникс секунды начала/конца планового дня
        long planDayStartSec = planDay.atStartOfDay(zone).toEpochSecond();
        long planDayEndSec = planDay.plusDays(1).atStartOfDay(zone).toEpochSecond() - 1;

        // 1С: ДельтаА = 3600 * КолМИН, ДельтаБ = 3600 * КолМАКС
        long deltaA = 3600L * getColMinHours(); // часы ДО начала дня
        long deltaB = 3600L * getColMaxHours(); // часы ПОСЛЕ конца дня

        // 1С: override из ОСВ (если есть) — оставляю хук, чтобы ты подключил свою БД
        long[] deltas = applyOsvOverrides(deltaA, deltaB, wpDataDB.getDt());
        deltaA = deltas[0];
        deltaB = deltas[1];

        long minAllowedSec = planDayStartSec - deltaA;
        long maxAllowedSec = planDayEndSec + deltaB;

        // ===== 4.0. логика как в 1С =====
        if (startWorkFactSec == 0L) {
            // 1С: ВремРНФакт=0
            massageToUser = "Немає даних про час ПОЧАТКУ робіт.";
            signal = true;

        } else if (startWorkDocSec > 0L && startWorkDocSec < minAllowedSec) {
            // 1С: (ВремРНФДок>0) и (ВремРНФДок<НачДня-ДельтаА)
            massageToUser = "Робота була (фактично) почата: " + fmtDdMmHm(startWorkDocSec) +
                    " що МЕНШЕ: " + fmtDdMmHm(minAllowedSec) +
                    " (мінімально припустимого), це заборонено для поточного клієнта.";
            signal = true;

        } else if (startWorkDocSec > 0L && startWorkDocSec > maxAllowedSec) {
            // 1С: (ВремРНФДок>0) и (ВремРНФДок>КонДня+ДельтаБ)
            massageToUser = "Робота була (фактично) почата: " + fmtDdMmHm(startWorkDocSec) +
                    " що БІЛЬШЕ: " + fmtDdMmHm(maxAllowedSec) +
                    " (максимально припустимого), це заборонено для поточного клієнта.";
            signal = true;

        } else if (startWorkDocSec == 0L && startWorkFactSec < minAllowedSec) {
            // 1С: (ВремРНФДок=0) и (ВремРНФакт<НачДня-ДельтаА)
            massageToUser = "На жаль, Ви не можете почати роботу раніше ніж: " +
                    fmtDdMmHm(minAllowedSec) +
                    " (мінімально припустимого), що заборонено для поточного клієнта.";
            signal = true;

        } else if (startWorkDocSec == 0L && startWorkFactSec > maxAllowedSec) {
            // 1С: (ВремРНФДок=0) и (ВремРНФакт>КонДня+ДельтаБ)
            massageToUser = "На жаль, Ви не можете почати роботу після " +
                    fmtDdMmHm(maxAllowedSec) +
                    " (максимально припустимого), що заборонено для поточного клієнта.";
            signal = true;

        } else {
            // 1С: OK
            massageToUser = "Роботи початі своєчасно " + fmtDdMmHm(startWorkFactSec) +
                    ". (Дозволений період " + fmtDdMmHm(minAllowedSec) +
                    " - " + fmtDdMmHm(maxAllowedSec) + "). Зауважень нема.";
            signal = false;
        }

        // ===== 5.0. исключение по дате (как в 1С) =====
        if (signal && planDay.isBefore(EXCEPTION_BEFORE_DATE)) {
            massageToUser = massageToUser + " Але, до '29.12.2025' надане виключення.";
            signal = false;
        }

        spannableStringBuilder.append(massageToUser);

        // ===== 5.1. код разблокировки (в 1С он проверяется только если сигнал=1) =====
        if (signal) {
            checkUnlockCode(optionDB);
        }

        // ===== 5.2. блокировать только если сигнал и включена блокировка опцией =====
        boolean shouldBlock = signal && isBlockPnsEnabled();
        setIsBlockOption(shouldBlock);

        // ===== 6.0. фиксация сигнала в опции =====
        saveOption(signal ? "1" : "2");

        Log.d("test", "massageToUser: " + massageToUser);
        Log.d("test", "spannableStringBuilder: " + spannableStringBuilder);
    }

    // ======== твой рабочий метод, только заполняем startWorkDocSec ========
    private void getStartWorkFromDocument() {
        startWorkDocSec = 0L;
        if (document instanceof WpDataDB) {
            startWorkDocSec = ((WpDataDB) document).getVisit_start_dt(); // СЕКУНДЫ
        } else if (document instanceof TasksAndReclamationsSDB) {
            startWorkDocSec = ((TasksAndReclamationsSDB) document).dt_start_fact; // если там тоже СЕКУНДЫ
        }
    }

    // ======== плановая дата ДокИст.ДатаДок ========
    private Date getDocDateFromDocument() {
        if (document instanceof WpDataDB) {
            return ((WpDataDB) document).getDt(); // плановая дата ПР
        }
        // если для Tasks есть плановая дата — подставь тут
        return null;
    }

    // ======== чтение КолМИН / КолМАКС (подставь свои геттеры) ========
    private int getColMinHours() {
        if (optionDB == null) return 0;
        return Integer.parseInt(optionDB.getAmountMin()); // например
    }

    private int getColMaxHours() {
        if (optionDB == null) return 0;
        return Integer.parseInt(optionDB.getAmountMax()); // например
    }

    /**
     * ОСВ/доп.требования: берём записи по твоему рабочему методу и выбираем “самую приоритетную”
     * (в 1С: сортировка -ВПИ и берем первую).
     * КолМИН/КолМАКС из ОСВ заменяют дельты опции.
     */
    private long[] applyOsvOverrides(long deltaA, long deltaB, Date planDate) {
        try {
            List<AdditionalRequirementsDB> additionalRequirements =
                    AdditionalRequirementsRealm.getDocumentAdditionalRequirements(
                            document,
                            true,
                            OPTION_CONTROL_OverTimelinessOfReporting_ID,
                            null,
                            planDate,
                            planDate,
                            null, null, null, null
                    );

            if (additionalRequirements == null || additionalRequirements.isEmpty()) {
                return new long[]{deltaA, deltaB};
            }

            // аналог: Сортировать("-ВПИ"); ПолучитьСтроку();
            AdditionalRequirementsDB best = null;
            for (AdditionalRequirementsDB r : additionalRequirements) {
                if (best == null) {
                    best = r;
                } else {
                    // подставь точное имя поля приоритета (часто VPI / vpi / getVpi())
                    Long v1 = safeInt(getVpi(best));
                    Long v2 = safeInt(getVpi(r));
                    if (v2 > v1) best = r;
                }
            }

            if (best == null) return new long[]{deltaA, deltaB};

            Integer osvMinHours = getOsvColMinHours(best); // КолМИН (часы)
            Integer osvMaxHours = getOsvColMaxHours(best); // КолМАКС (часы)

            if (osvMinHours != null) deltaA = 3600L * osvMinHours;
            if (osvMaxHours != null) deltaB = 3600L * osvMaxHours;

            return new long[]{deltaA, deltaB};

        } catch (Throwable t) {
            // не ломаем выполнение опции
            return new long[]{deltaA, deltaB};
        }
    }

    private Integer getOsvColMinHours(AdditionalRequirementsDB r) {
        // подставь реальное поле КолМИН из ОСВ/доп. требования
        return Integer.valueOf(r.getAmountMin()); // <-- если иначе
    }

    private Integer getOsvColMaxHours(AdditionalRequirementsDB r) {
        // подставь реальное поле КолМАКС из ОСВ/доп. требования
        return Integer.valueOf(r.getAmountMax()); // <-- если иначе
    }

    private Long safeInt(Long v) {
        return v == null ? 0 : v;
    }

    private Long getVpi(AdditionalRequirementsDB r) {
        // подставь реальное поле ВПИ
        return r.getDtChange(); // <-- если иначе: getVpi()
    }

    // ======== включена ли блокировка (БлокПНС) ========
    private boolean isBlockPnsEnabled() {
        if (optionDB == null) return false;
        return optionDB.getBlockPns().equals("1");
    }

    // ======== формат как в 1С: "ДД.ММ ЧЧ:ММ" ========
    private static String fmtDdMmHm(long unixSec) {
        try {
            if (unixSec <= 0) return "";
            return Instant.ofEpochSecond(unixSec)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd.MM HH:mm"));
        } catch (Exception e) {
            return "";
        }
    }

    // ======== твой рабочий метод сохранения сигнала ========
    private void saveOption(String signal) {
        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                optionDB.setIsSignal(signal);
                realm.insertOrUpdate(optionDB);
            }
        });
    }

    private void finish(boolean signal) {
        spannableStringBuilder.clear();
        spannableStringBuilder.append(massageToUser);

        if (signal) checkUnlockCode(optionDB);

        boolean shouldBlock = signal && isBlockPnsEnabled();
        setIsBlockOption(shouldBlock);

        saveOption(signal ? "1" : "2");
    }
}
