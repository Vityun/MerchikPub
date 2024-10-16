package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.SiteAccountSDB;
import ua.com.merchik.merchik.data.Database.Room.SiteUrlSDB;

@Dao
public interface SiteAccountDao {
    @Query("SELECT * FROM site_account")
    List<SiteAccountSDB> getAll();

    @Query("SELECT * FROM site_account WHERE id = :id")
    SiteAccountSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SiteAccountSDB> itemList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SiteAccountSDB item);
}
