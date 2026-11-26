package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ua.com.merchik.merchik.Global.UnlockCode;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.database.room.RoomManager;

// 175265. ПровИспНовКли: роботи з новим клієнтом виконуються у першу чергу.
public class OptionControlNewClientPriority<T> extends OptionControl<T> {
    public static final int ID = 175265;
    private static final ZoneId VISIT_ZONE = ZoneId.of("Europe/Kiev");
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private LocalDate day;

    public OptionControlNewClientPriority(Context context, T document, OptionsDB optionDB,
                                          OptionMassageType msgType, Options.NNKMode nnkMode,
                                          UnlockCodeResultListener listener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = listener;
        getDocumentVar();
        if (day != null && optionDB != null) executeOption();
    }

    private void getDocumentVar() {
        // 1.0. Определим переменные. ДатПо, КолМин и Сум в условии 1С не участвуют.
        // 2.0. ОтчетИсполнителя; для остальных видов документов пока не реализован.
        if (document instanceof WpDataDB) {
            wpDataDB = (WpDataDB) document;
            if (wpDataDB.getDt() != null) {
                day = Instant.ofEpochMilli(wpDataDB.getDt().getTime()).atZone(VISIT_ZONE).toLocalDate();
            }
        }
    }

    // 3.0. Перевіримо наявність початку робіт по НОВИМ клієнтам.
    private Result checkPlan() {
        // 3.1. Перелік усіх робіт за Дат, Исп, Адр, виключаючи ДАД поточного відвідування.
        List<WpDataDB> rows = WpDataRealm.getWpDataBy(Date.from(day.atStartOfDay(VISIT_ZONE).toInstant()),
                Date.from(day.plusDays(1).atStartOfDay(VISIT_ZONE).toInstant().minusMillis(1)),
                null, wpDataDB.getAddr_id(), null, wpDataDB.getUser_id());
        List<WpDataDB> others = otherVisits(rows, wpDataDB.getCodedad());
        Map<String, CustomerSDB> customers = new HashMap<>();
        if (!others.isEmpty()) {
            Set<String> ids = new HashSet<>();
            ids.add(wpDataDB.getClient_id());
            for (WpDataDB row : others) ids.add(row.getClient_id());
            for (CustomerSDB customer : RoomManager.SQL_DB.customerDao().getByIds(new ArrayList<>(ids))) {
                customers.put(customer.id, customer);
            }
        }
        return evaluate(day, wpDataDB, others, customers);
    }

    static List<WpDataDB> otherVisits(List<WpDataDB> rows, long currentDad) {
        // Тзн.Свернуть: дата, адрес и исполнитель уже зафиксированы запросом.
        Set<List<?>> seen = new HashSet<>();
        List<WpDataDB> result = new ArrayList<>();
        for (WpDataDB row : rows) {
            if (row.getCodedad() != currentDad && seen.add(Arrays.asList(row.getClient_id(), row.getCodedad(),
                    row.getVisit_start_dt(), row.getVisit_end_dt(), row.getDt_update()))) result.add(row);
        }
        return result;
    }

    static Result evaluate(LocalDate day, WpDataDB visit, List<WpDataDB> others, Map<String, CustomerSDB> customers) {
        // 3.2. Позначимо клієнтів, з котрими почали працювати нещодавно.
        LocalDate cutoff = day.minusDays(35);
        CustomerSDB current = customers.get(visit.getClient_id());
        LocalDate currentLaunch = launch(current);
        boolean currentIsNew = currentLaunch != null && currentLaunch.isAfter(cutoff);
        String note = null;

        // 3.3. ДатНач = максимум дати запуску та перезапуску; Прим від останнього порушення.
        for (WpDataDB row : others) {
            CustomerSDB customer = customers.get(row.getClient_id());
            LocalDate launch = launch(customer);
            if (!currentIsNew && launch != null && launch.isAfter(cutoff)
                    && visit.getVisit_start_dt() <= row.getVisit_start_dt()) {
                note = "Клієнт: " + name(customer, row) + " є новим (запущеним " + DISPLAY.format(launch)
                        + "), і роботи з ним потрібно виконувати у першу чергу.";
            }
        }

        // 4.0. Готовим сообщение и сигнал.
        if (others.isEmpty()) return new Result(false, "Взагалі нема планів проведення робіт (крім поточного). Зауважень нема.");
        if (currentIsNew) return new Result(false, "Поточний клієнт: " + name(current, visit)
                + " був запущеним (відновленим) нещодавно (" + DISPLAY.format(currentLaunch)
                + "). Він є приорітетним. Зауважень нема.");
        if (note != null) return new Result(true, note);
        return new Result(false, "Зауважень по порядку виконання робіт з новими клієнтами нема.");
    }

    private static LocalDate launch(CustomerSDB client) {
        if (client == null) return null;
        Date start = client.workStartDate, restart = client.workRestartDate;
        Date latest = restart != null && (start == null || restart.after(start)) ? restart : start;
        if (latest == null) return null;
        // CustomerSDB: локальная полночь из JSON сохраняется DateConverter как календарная дата GMT.
        LocalDate stored = Instant.ofEpochMilli(latest.getTime()).atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate next = stored.plusDays(1);
        return next.atStartOfDay(ZoneId.systemDefault()).toInstant().atZone(ZoneOffset.UTC).toLocalDate().equals(stored)
                ? next : stored;
    }

    private static String name(CustomerSDB client, WpDataDB visit) {
        return client != null && client.nm != null ? client.nm : visit.getClient_txt();
    }

    static final class Result {
        final boolean signal;
        final String note;

        Result(boolean signal, String note) {
            this.signal = signal;
            this.note = note;
        }

        boolean blocks(boolean blockPns, boolean unlocked, int documentStatus) {
            return signal && blockPns && !unlocked && documentStatus == 0;
        }
    }

    private void executeOption() {
        Result result = checkPlan();
        // 5.0. ДокОпц.Примечание и СигналОИ. Штраф за порядок работ в 1С не начисляется.
        String signal = result.signal ? "1" : "2";
        if (!signal.equals(optionDB.getIsSignal()) || !Objects.equals(result.note, optionDB.getNotes())) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                optionDB.setIsSignal(signal);
                optionDB.setNotes(result.note);
                realm.insertOrUpdate(optionDB);
            });
        }
        stringBuilderMsg.append(result.note);

        // 03.11.2026. Проверяем факт использования кода разблокировки (тема 1285, код ОДАД).
        boolean blockPns = "1".equals(optionDB.getBlockPns());
        boolean unlocked = result.signal && blockPns && new UnlockCode().applyStoredUnlockCode(optionDB);
        if (unlocked) stringBuilderMsg.append(", Але виконавцю виданий разовий код розблокування.");

        // 6.0. Блокировка проведения.
        setIsBlockOption(result.blocks(blockPns, unlocked, wpDataDB.getStatus()));
        if (result.signal) {
            stringBuilderMsg.append(isBlockOption() ? " Документ проведен не буде!"
                    : " Ви можете отримати БІЛЬШЕ преміальних, якщо будете проводити презентаційні роботи після презентації");
        }
        // 7.0. Вывод по режиму выполняет диспетчер опций через showOptionMassage.
        massageToUser = stringBuilderMsg.toString();
    }
}
