package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;

@Dao
public interface ShowcaseDao {
    @Query("SELECT * FROM showcase")
    List<ShowcaseSDB> getAll();

    @Query("SELECT * FROM showcase WHERE ID = :id")
    ShowcaseSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<ShowcaseSDB> data);

    // ----------------------------------------------------
}
