package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;

@Dao
public interface SynchronizationCursorDao {
    @Query("SELECT * FROM synchronization_cursor WHERE resource = :resource AND scope = :scope LIMIT 1")
    SynchronizationCursorSDB get(String resource, String scope);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(SynchronizationCursorSDB cursor);

    @Query("DELETE FROM synchronization_cursor WHERE resource = :resource AND scope != :scope")
    void deleteOtherScopes(String resource, String scope);

    @Query("DELETE FROM synchronization_cursor")
    void clear();
}
