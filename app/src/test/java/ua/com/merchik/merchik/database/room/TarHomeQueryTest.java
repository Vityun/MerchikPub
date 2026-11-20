package ua.com.merchik.merchik.database.room;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.Test;
import ua.com.merchik.merchik.database.room.DaoInterfaces.TarDao;

import static org.junit.Assert.*;

public class TarHomeQueryTest {
    @Test public void themeJoinKeepsRowsWithoutDictionaryEntriesAndPreservesVisitScope() throws Exception {
        try (Connection db = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement setup = db.createStatement()) {
            setup.execute("CREATE TABLE tasks_and_reclamations (id INTEGER PRIMARY KEY, tp INTEGER, dt INTEGER, "
                    + "theme_id INTEGER, addr INTEGER, client TEXT, vinovnik INTEGER, vinovnik2 INTEGER, zamena_user_id INTEGER)");
            setup.execute("CREATE TABLE address (id INTEGER, nm TEXT)");
            setup.execute("CREATE TABLE client (id TEXT, nm TEXT)");
            setup.execute("CREATE TABLE sotr (id INTEGER, fio TEXT)");
            setup.execute("CREATE TABLE theme_list (id TEXT PRIMARY KEY, nm TEXT)");
            setup.execute("INSERT INTO theme_list VALUES ('998', 'Shelf layout')");
            setup.execute("INSERT INTO tasks_and_reclamations VALUES "
                    + "(1,1,103,998,null,null,7,0,0),(2,1,102,999,null,null,0,7,0),"
                    + "(3,0,101,998,null,null,0,0,7),(4,1,104,998,null,null,8,0,0),"
                    + "(5,1,90,998,null,null,7,0,0)");

            try (PreparedStatement query = db.prepareStatement(TarDao.HOME_QUERY)) {
                bind(query, 0, 1);
                try (ResultSet tasks = query.executeQuery()) {
                    assertTrue(tasks.next());
                    assertEquals(1, tasks.getInt("id"));
                    assertEquals("Shelf layout", tasks.getString("themeName"));
                    assertTrue(tasks.next());
                    assertEquals(2, tasks.getInt("id"));
                    assertEquals(999, tasks.getInt("theme_id"));
                    assertNull(tasks.getString("themeName"));
                    assertFalse(tasks.next());
                }

                bind(query, 0, 0);
                try (ResultSet reclamations = query.executeQuery()) {
                    assertTrue(reclamations.next());
                    assertEquals(3, reclamations.getInt("id"));
                    assertEquals("Shelf layout", reclamations.getString("themeName"));
                    assertFalse(reclamations.next());
                }

                bind(query, 5, 1);
                try (ResultSet linked = query.executeQuery()) {
                    assertTrue(linked.next());
                    assertEquals(5, linked.getInt("id"));
                    assertEquals("Shelf layout", linked.getString("themeName"));
                    assertFalse(linked.next());
                }
            }
        }
    }

    private static void bind(PreparedStatement query, int tarId, int type) throws Exception {
        query.setInt(1, tarId);
        query.setInt(2, type);
        query.setLong(3, 100);
        query.setInt(4, 7);
    }
}
