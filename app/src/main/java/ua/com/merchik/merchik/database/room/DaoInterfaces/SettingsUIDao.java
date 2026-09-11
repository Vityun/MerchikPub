package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.SettingsUISDB;

@Dao
public interface SettingsUIDao {
    @Query("SELECT * FROM settings_ui")
    List<SettingsUISDB> getAll();

    @Query("SELECT * FROM settings_ui WHERE table_db = :tableDB AND context_tag = :contextTag")
    SettingsUISDB getTableByContext(String tableDB, String contextTag);

    @Query("SELECT * FROM settings_ui WHERE id = :id")
    SettingsUISDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SettingsUISDB> itemList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SettingsUISDB item);

}
