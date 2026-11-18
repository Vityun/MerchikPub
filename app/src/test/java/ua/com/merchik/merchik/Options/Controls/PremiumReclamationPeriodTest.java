package ua.com.merchik.merchik.Options.Controls;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ua.com.merchik.merchik.database.room.DaoInterfaces.TarDao;

import static org.junit.Assert.*;

public class PremiumReclamationPeriodTest {
    private static final TimeZone KYIV = TimeZone.getTimeZone("Europe/Kiev");

    @Test public void fourReclamationsUsePostingDateNotVisitDate() throws Exception {
        try (Connection db = database()) {
            insert(db, 2682150, 0, 189722, 1793138400L, 1793550381L);
            insert(db, 2682352, 0, 189722, 1793224800L, 1793606783L);
            insert(db, 2682407, 0, 189722, 1793311200L, 1793636324L);
            insert(db, 2685954, 0, 189722, 1794434400L, 1794474500L);
            PremiumReclamationPeriod period = period("2026-11-16 00:00:00");
            List<Integer> ids = select(db, period);
            assertEquals(Arrays.asList(2682150, 2682352, 2682407, 2685954), ids);
            assertTrue(100.0 * ids.size() / 39 > 3.29 * 1.5);
            assertEquals("42.02", PremiumPercentagePolicy.penalty(273.13, -10));
        }
    }

    @Test public void includesFullLastDayButNotReportDay() throws Exception {
        try (Connection db = database()) {
            PremiumReclamationPeriod period = period("2026-11-16 14:27:13");
            insert(db, 1, 0, 189722, 0, period.fromSeconds - 1);
            insert(db, 2, 0, 189722, 0, period.fromSeconds);
            insert(db, 3, 0, 189722, 0, period.toExclusiveSeconds - 1);
            insert(db, 4, 0, 189722, 0, period.toExclusiveSeconds);
            assertEquals(Arrays.asList(2, 3), select(db, period));
            assertEquals(seconds("2026-11-01 00:00:00"), period.fromSeconds);
            assertEquals(seconds("2026-11-16 00:00:00"), period.toExclusiveSeconds);
        }
    }

    @Test public void missingPostingDateDoesNotFallBackToVisitDate() throws Exception {
        try (Connection db = database()) {
            PremiumReclamationPeriod period = period("2026-11-16 00:00:00");
            insert(db, 1, 0, 189722, period.fromSeconds, null);
            insert(db, 2, 0, 189722, period.fromSeconds, 0L);
            insert(db, 3, 0, 189722, period.fromSeconds, period.fromSeconds - 1);
            insert(db, 4, 0, 189722, period.fromSeconds, period.toExclusiveSeconds);
            assertTrue(select(db, period).isEmpty());
        }
    }

    @Test public void otherUsersAndTasksStayExcluded() throws Exception {
        try (Connection db = database()) {
            PremiumReclamationPeriod period = period("2026-11-16 00:00:00");
            insert(db, 1, 0, 189722, 0, period.fromSeconds);
            insert(db, 2, 1, 189722, 0, period.fromSeconds);
            insert(db, 3, 0, 261007, 0, period.fromSeconds);
            assertEquals(Arrays.asList(1), select(db, period));
        }
    }

    @Test public void twentiethReportFilterUsesPostingTimeAndRejectsMissingDates() throws Exception {
        Date twentieth = new Date(seconds("2026-11-01 12:00:00") * 1000);
        long boundary = twentieth.getTime() / 1000;
        assertTrue(PremiumReclamationPeriod.afterTwentiethReport(boundary + 1, twentieth));
        assertFalse(PremiumReclamationPeriod.afterTwentiethReport(boundary, twentieth));
        assertFalse(PremiumReclamationPeriod.afterTwentiethReport(boundary - 1, twentieth));
        assertFalse(PremiumReclamationPeriod.afterTwentiethReport(null, twentieth));
        assertFalse(PremiumReclamationPeriod.afterTwentiethReport(0L, twentieth));
        assertFalse(PremiumReclamationPeriod.afterTwentiethReport(boundary + 1, null));
    }

    @Test public void calendarPeriodSurvivesDaylightSavingChange() throws Exception {
        PremiumReclamationPeriod period = period("2026-11-02 12:00:00");
        assertEquals(seconds("2026-10-18 00:00:00"), period.fromSeconds);
        assertEquals(seconds("2026-11-02 00:00:00"), period.toExclusiveSeconds);
        assertEquals(15 * 86400 + 3600, period.toExclusiveSeconds - period.fromSeconds);
    }

    private static PremiumReclamationPeriod period(String date) throws Exception {
        return new PremiumReclamationPeriod(new Date(seconds(date) * 1000), KYIV);
    }

    private static long seconds(String date) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        format.setTimeZone(KYIV);
        return format.parse(date).getTime() / 1000;
    }

    private static Connection database() throws Exception {
        Connection db = DriverManager.getConnection("jdbc:sqlite::memory:");
        db.createStatement().execute("CREATE TABLE tasks_and_reclamations (id INTEGER PRIMARY KEY, tp INTEGER, vinovnik INTEGER, dt INTEGER, dt_real_post INTEGER)");
        return db;
    }

    private static void insert(Connection db, int id, int type, int user, long visit, Long posted) throws Exception {
        try (PreparedStatement statement = db.prepareStatement("INSERT INTO tasks_and_reclamations VALUES (?,?,?,?,?)")) {
            statement.setInt(1, id);
            statement.setInt(2, type);
            statement.setInt(3, user);
            statement.setLong(4, visit);
            statement.setObject(5, posted);
            statement.executeUpdate();
        }
    }

    private static List<Integer> select(Connection db, PremiumReclamationPeriod period) throws Exception {
        try (PreparedStatement statement = db.prepareStatement(TarDao.PREMIUM_RECLAMATIONS_QUERY + " ORDER BY id")) {
            statement.setInt(1, 0);
            statement.setInt(2, 189722);
            statement.setLong(3, period.fromSeconds);
            statement.setLong(4, period.toExclusiveSeconds);
            try (ResultSet rows = statement.executeQuery()) {
                List<Integer> ids = new ArrayList<>();
                while (rows.next()) ids.add(rows.getInt("id"));
                return ids;
            }
        }
    }
}
