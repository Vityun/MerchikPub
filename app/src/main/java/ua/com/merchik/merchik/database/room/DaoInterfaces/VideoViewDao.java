package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.ViewListSDB;

@Dao
public interface VideoViewDao {
    @Query("SELECT * FROM view_list")
    List<ViewListSDB> getAll();

    @Query("SELECT * FROM view_list WHERE id = :id")
    ViewListSDB getById(int id);

    @Query("SELECT * FROM view_list WHERE id IN (:ids)")
    List<ViewListSDB> getByIds(List<Integer> ids);

    @Query("SELECT * FROM view_list WHERE lessonId = :id")
    List<ViewListSDB> getByLessonId(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ViewListSDB> data);
}
