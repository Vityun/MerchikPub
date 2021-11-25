package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.ContentSDB;

@Dao
public interface ContentDao {
    @Query("SELECT * FROM content")
    List<ContentSDB> getAll();

    @Query("SELECT * FROM content WHERE id = :id")
    ContentSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ContentSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<ContentSDB> data);
}
