package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.BonusSDB;
import ua.com.merchik.merchik.data.Database.Room.SiteUrlSDB;

@Dao
public interface SiteUrlDao {
    @Query("SELECT * FROM site_url")
    List<SiteUrlSDB> getAll();

    @Query("SELECT * FROM site_url WHERE id = :id")
    SiteUrlSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SiteUrlSDB> itemList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SiteUrlSDB item);
}
