package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;

@Dao
public interface TranslatesDao {

    @Query("SELECT * FROM translates")
    List<TranslatesSDB> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TranslatesSDB> data);

    @Query("SELECT COUNT(*) FROM translates WHERE lang_id = :langId")
    long countForLanguage(String langId);

    @Query("DELETE FROM translates WHERE lang_id = :langId")
    void deleteForLanguage(String langId);
}
