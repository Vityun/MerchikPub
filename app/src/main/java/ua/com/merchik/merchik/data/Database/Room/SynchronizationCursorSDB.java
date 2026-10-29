package ua.com.merchik.merchik.data.Database.Room;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "synchronization_cursor", primaryKeys = {"resource", "scope"})
public class SynchronizationCursorSDB {
    @NonNull public String resource = "";
    @NonNull public String scope = "";
    public long vpi;
    public long itemCount;

    public SynchronizationCursorSDB() { }
}
