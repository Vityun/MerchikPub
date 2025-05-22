package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.*;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import ua.com.merchik.merchik.data.SynchronizationTimeTable;

@Dao
public interface SynchronizationTimetableDao {

    @Query("SELECT * FROM synchronization_timetable")
    List<SynchronizationTimeTable> getAll();

    @Query("SELECT * FROM synchronization_timetable WHERE id = :id LIMIT 1")
    SynchronizationTimeTable getById(int id);

    @Query("SELECT * FROM synchronization_timetable WHERE tableName = :name LIMIT 1")
    SynchronizationTimeTable getByTableName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SynchronizationTimeTable entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<SynchronizationTimeTable> items);

    @Update
    void update(SynchronizationTimeTable entity);

    @Delete
    void delete(SynchronizationTimeTable entity);

    @Query("SELECT COUNT(*) FROM synchronization_timetable")
    Single<Integer> getCount();
}
