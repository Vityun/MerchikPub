package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.LanguagesSDB;
import ua.com.merchik.merchik.data.Database.Room.OpinionThemeSDB;

@Dao
public interface OpinionThemeDao {
    @Query("SELECT * FROM opinions_theme")
    List<OpinionThemeSDB> getAll();

    @Query("SELECT * FROM opinions_theme WHERE id = :id")
    LanguagesSDB getOpinionThemeById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OpinionThemeSDB> data);
}
