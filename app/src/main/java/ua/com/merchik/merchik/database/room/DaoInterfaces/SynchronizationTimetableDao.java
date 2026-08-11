package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.*;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import ua.com.merchik.merchik.data.SynchronizationTimeTable;
import ua.com.merchik.merchik.data.synchronization.TableName;

@Dao
public interface SynchronizationTimetableDao {

    @Query("SELECT * FROM synchronization_timetable ORDER BY id")
    List<SynchronizationTimeTable> getAll();

    @Query("SELECT * FROM synchronization_timetable WHERE tableName = :name LIMIT 1")
    SynchronizationTimeTable getByTableName(TableName name);

    @Query("SELECT * FROM synchronization_timetable WHERE id = :id LIMIT 1")
    SynchronizationTimeTable getById(int id);

    @Query("SELECT * FROM synchronization_timetable WHERE isUserGenerated = 1 ORDER BY id")
    List<SynchronizationTimeTable> getUserGenerated();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SynchronizationTimeTable entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<SynchronizationTimeTable> items);

    @Update
    void update(SynchronizationTimeTable entity);

    @Query("DELETE FROM synchronization_timetable")
    void clear();

    @Query("DELETE FROM synchronization_timetable WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT MAX(id) FROM synchronization_timetable")
    Integer getMaxId();

    @Query("SELECT COUNT(*) FROM synchronization_timetable")
    Single<Integer> getCount();
}
