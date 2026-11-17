package ua.com.merchik.merchik.database.room;

import androidx.sqlite.db.SupportSQLiteDatabase;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PlanogramProductGroupMigrationTest {
    @Test public void migrationAddsNullableGroupWithoutDeletingRowsOrChangingWatermarks() {
        List<String> statements = new ArrayList<>();
        SupportSQLiteDatabase database = (SupportSQLiteDatabase) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{SupportSQLiteDatabase.class}, (proxy, method, args) -> {
                    if (!method.getName().equals("execSQL")) throw new UnsupportedOperationException(method.getName());
                    statements.add((String) args[0]);
                    return null;
                });
        assertEquals(87, RoomManager.MIGRATION_87_88.startVersion);
        assertEquals(88, RoomManager.MIGRATION_87_88.endVersion);
        RoomManager.MIGRATION_87_88.migrate(database);
        assertEquals(Collections.singletonList("ALTER TABLE planogramm ADD COLUMN tovar_grp INTEGER"), statements);
    }
}
