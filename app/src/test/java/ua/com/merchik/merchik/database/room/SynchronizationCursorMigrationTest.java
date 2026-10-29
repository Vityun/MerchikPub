package ua.com.merchik.merchik.database.room;

import androidx.sqlite.db.SupportSQLiteDatabase;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SynchronizationCursorMigrationTest {
    @Test public void migrationOnlyCreatesScopedCursorTable() {
        List<String> statements = new ArrayList<>();
        SupportSQLiteDatabase bridge = (SupportSQLiteDatabase) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{SupportSQLiteDatabase.class}, (proxy, method, args) -> {
                    if (!method.getName().equals("execSQL")) throw new UnsupportedOperationException(method.getName());
                    statements.add((String) args[0]);
                    return null;
                });
        assertEquals(85, RoomManager.MIGRATION_85_86.startVersion);
        assertEquals(86, RoomManager.MIGRATION_85_86.endVersion);
        RoomManager.MIGRATION_85_86.migrate(bridge);
        assertEquals(1, statements.size());
        assertEquals("CREATE TABLE IF NOT EXISTS `synchronization_cursor` ("
                + "`resource` TEXT NOT NULL, `scope` TEXT NOT NULL, `vpi` INTEGER NOT NULL, "
                + "`itemCount` INTEGER NOT NULL, PRIMARY KEY(`resource`, `scope`))", statements.get(0));
    }
}
