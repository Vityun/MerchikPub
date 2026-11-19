package ua.com.merchik.merchik.database.room;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import ua.com.merchik.merchik.database.room.DaoInterfaces.TarDao;

import static org.junit.Assert.assertEquals;

public class TarActiveCountTest {
    private Connection db;

    @Before public void setUp() throws Exception {
        db = DriverManager.getConnection("jdbc:sqlite::memory:");
        execute("CREATE TABLE tasks_and_reclamations (id INTEGER PRIMARY KEY, tp INTEGER, "
                + "state INTEGER, dt INTEGER, vinovnik INTEGER, vinovnik2 INTEGER, zamena_user_id INTEGER)");
    }

    @After public void tearDown() throws Exception {
        if (db != null) db.close();
    }

    @Test public void emptyTableReturnsZeroForBothTypes() throws Exception {
        assertEquals(0, count(1));
        assertEquals(0, count(0));
    }

    @Test public void countsOnlyActiveAndSeparatesTasksFromReclamations() throws Exception {
        execute("INSERT INTO tasks_and_reclamations VALUES "
                + "(1,1,0,101,7,0,0),(2,1,0,101,7,0,0),(3,0,0,101,7,0,0),"
                + "(4,1,1,101,7,0,0),(5,1,2,101,7,0,0),(6,1,3,101,7,0,0),"
                + "(7,0,1,101,7,0,0),(8,0,2,101,7,0,0),(9,0,3,101,7,0,0)");
        assertEquals(2, count(1));
        assertEquals(1, count(0));
    }

    @Test public void matchesAllAssignmentRolesWithoutDoubleCounting() throws Exception {
        execute("INSERT INTO tasks_and_reclamations VALUES "
                + "(1,1,0,101,7,0,0),(2,1,0,101,0,7,0),(3,1,0,101,0,0,7),"
                + "(4,1,0,101,7,7,7),(5,1,0,101,8,8,8)");
        assertEquals(4, count(1));
    }

    @Test public void preservesPeriodBoundaryAndUserScope() throws Exception {
        execute("INSERT INTO tasks_and_reclamations VALUES "
                + "(1,0,0,99,7,0,0),(2,0,0,100,7,0,0),(3,0,0,101,7,0,0),"
                + "(4,0,0,101,8,8,8)");
        assertEquals(1, count(0));
    }

    @Test public void completedCancelledAndDeletedRecordsReduceCountToZero() throws Exception {
        execute("INSERT INTO tasks_and_reclamations VALUES "
                + "(1,1,0,101,7,0,0),(2,1,0,101,7,0,0),(3,1,0,101,7,0,0)");
        assertEquals(3, count(1));
        execute("UPDATE tasks_and_reclamations SET state=1 WHERE id=1");
        assertEquals(2, count(1));
        execute("UPDATE tasks_and_reclamations SET state=3 WHERE id=2");
        assertEquals(1, count(1));
        execute("DELETE FROM tasks_and_reclamations WHERE id=3");
        assertEquals(0, count(1));
    }

    @Test public void unknownOrMissingStatusIsNotActive() throws Exception {
        execute("INSERT INTO tasks_and_reclamations VALUES "
                + "(1,0,NULL,101,7,0,0),(2,0,99,101,7,0,0),(3,0,-1,101,7,0,0)");
        assertEquals(0, count(0));
    }

    private long count(int type) throws Exception {
        String sql = TarDao.ACTIVE_COUNT_QUERY.replace(":tp", "?")
                .replace(":dt", "?").replace(":userId", "?");
        try (PreparedStatement query = db.prepareStatement(sql)) {
            query.setInt(1, type);
            query.setLong(2, 100);
            query.setInt(3, 7);
            query.setInt(4, 7);
            query.setInt(5, 7);
            try (ResultSet result = query.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    private void execute(String sql) throws Exception {
        try (Statement statement = db.createStatement()) {
            statement.execute(sql);
        }
    }
}
