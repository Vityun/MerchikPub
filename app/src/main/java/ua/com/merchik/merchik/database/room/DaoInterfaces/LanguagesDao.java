package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.LanguagesSDB;

@Dao
public interface LanguagesDao {
    @Query("SELECT * FROM languages")
    List<LanguagesSDB> getAll();

    @Query("SELECT * FROM languages WHERE id = :id")
    LanguagesSDB getLanguageById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LanguagesSDB> data);
}
