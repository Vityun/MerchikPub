package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.PotentialClientSDB;

@Dao
public interface PotentialClientDao {
    @Query("SELECT * FROM potential_client")
    List<PotentialClientSDB> getAll();

    @Query("SELECT * FROM potential_client WHERE id = :id")
    PotentialClientSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<PotentialClientSDB> data);
}
