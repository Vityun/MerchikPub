package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.ThemeSDB;

@Dao
public interface ThemeDao {
    @Query("SELECT * FROM theme_list")
    List<ThemeSDB> getAll();

    @Query("SELECT COUNT(*) FROM theme_list")
    int getCount();

    @Query("SELECT * FROM theme_list WHERE opros_theme IS NOT NULL AND opros_theme > 0")
    List<ThemeSDB> getAllOpros();

    @Query("SELECT * FROM theme_list WHERE id = :id LIMIT 1")
    ThemeSDB getById(String id);

    @Query("SELECT * FROM theme_list WHERE id IN (:ids)")
    List<ThemeSDB> getByIds(String[] ids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ThemeSDB> data);
}
