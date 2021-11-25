package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.OblastSDB;

@Dao
public interface OblastDao {
    @Query("SELECT * FROM oblast")
    List<OblastSDB> getAll();

    @Query("SELECT * FROM oblast WHERE id = :id")
    OblastSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OblastSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<OblastSDB> data);
}
