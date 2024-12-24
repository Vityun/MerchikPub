package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;

@Dao
public interface SiteObjectsDao {

    @Query("SELECT * FROM site_objects")
    List<SiteObjectsSDB> getAll();

    @Query("SELECT * FROM site_objects WHERE additional_id IN (:additionalId)")
    List<SiteObjectsSDB> getObjectsById(List<Integer> additionalId);

    @Query("SELECT * FROM site_objects WHERE id = :id")
    SiteObjectsSDB getObjectsByRealId(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SiteObjectsSDB> data);

}
