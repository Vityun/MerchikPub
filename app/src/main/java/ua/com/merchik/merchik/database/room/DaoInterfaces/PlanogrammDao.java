package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.PlanogrammSDB;

@Dao
public interface PlanogrammDao {
    @Query("SELECT * FROM planogramm")
    List<PlanogrammSDB> getAll();

    @Query("SELECT * FROM planogramm WHERE ID = :id")
    PlanogrammSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<PlanogrammSDB> data);

    // ----------------------------------------------------

    @Query("SELECT * FROM planogramm WHERE client_id = :clientId")
    List<PlanogrammSDB> getByDocTP(String clientId);

}
