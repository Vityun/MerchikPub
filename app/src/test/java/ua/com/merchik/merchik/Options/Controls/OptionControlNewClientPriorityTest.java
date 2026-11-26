package ua.com.merchik.merchik.Options.Controls;

import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.DateConverter;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import static org.junit.Assert.*;

public class OptionControlNewClientPriorityTest {
    private final LocalDate day = LocalDate.of(2026, 11, 26);
    private final WpDataDB current = visit("current", 10, 100);
    private final Map<String, CustomerSDB> clients = new HashMap<>();

    private static WpDataDB visit(String id, long dad, long start) {
        WpDataDB row = new WpDataDB();
        row.setClient_id(id);
        row.setCodedad(dad);
        row.setVisit_start_dt(start);
        return row;
    }

    private java.sql.Date stored(LocalDate day) {
        java.sql.Date fromServer = new GsonBuilder().setDateFormat("yyyy-MM-dd").create()
                .fromJson("\"" + day + "\"", java.sql.Date.class);
        return DateConverter.timeToDate(DateConverter.dateToTime(fromServer));
    }

    private void customer(String name, int age, Integer restartAge) {
        CustomerSDB c = new CustomerSDB();
        c.nm = c.id = name;
        c.workStartDate = stored(day.minusDays(age));
        c.workRestartDate = restartAge == null ? null : stored(day.minusDays(restartAge));
        clients.put(c.id, c);
    }

    private OptionControlNewClientPriority.Result check(WpDataDB... rows) {
        List<WpDataDB> others = OptionControlNewClientPriority.otherVisits(Arrays.asList(rows), current.getCodedad());
        return OptionControlNewClientPriority.evaluate(day, current, others, clients);
    }

    @Test public void excludesCurrentDadNotDad2AndChecksEmptyPlanFirst() {
        customer("current", 2, null);
        WpDataDB same = visit("other", 10, 100);
        same.setCode_dad2(99);
        assertTrue(check(same).note.startsWith("Взагалі нема планів"));
        assertTrue(check(visit("other", 20, 100)).note.startsWith("Поточний клієнт: current"));
    }

    @Test public void strictlyMoreRecentThan35DaysAndBothLaunchDatesMatter() {
        customer("current", 35, null);
        customer("A", 35, 90);
        WpDataDB other = visit("A", 20, 100);
        assertFalse(check(other).signal);
        customer("A", 90, 34);
        assertTrue(check(other).signal);
        assertTrue(check(other).note.contains("23.10.2026"));
        customer("current", 90, 3);
        assertFalse(check(other).signal);
        assertTrue(check(other).note.contains("23.11.2026"));
    }

    @Test public void comparesActualStartsIncludingZeroAndEqualTimes() {
        customer("current", 100, null);
        customer("A", 7, null);
        assertFalse(check(visit("A", 20, 0)).signal);
        assertFalse(check(visit("A", 20, 99)).signal);
        assertTrue(check(visit("A", 20, 100)).signal);
        assertTrue(check(visit("A", 20, 101)).signal);
        current.setVisit_start_dt(0);
        assertTrue(check(visit("A", 20, 0)).signal);
    }

    @Test public void lastViolatingGroupProvidesTheNote() {
        customer("current", 100, null);
        customer("A", 10, null);
        customer("B", 2, null);
        WpDataDB a = visit("A", 20, 120), b = visit("B", 30, 130);
        assertTrue(check(a, b, a, visit("unknown", 40, 150)).note.startsWith("Клієнт: B "));
    }

    @Test public void violationDoesNotAlwaysBlockPosting() {
        customer("current", 100, null);
        customer("A", 1, null);
        OptionControlNewClientPriority.Result r = check(visit("A", 20, 120));
        assertTrue(r.blocks(true, false, 0));
        assertFalse(r.blocks(false, false, 0));
        assertFalse(r.blocks(true, true, 0));
        assertFalse(r.blocks(true, false, 1));
        assertFalse(check().blocks(true, false, 0));
    }
}
