package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammGroupSDB;

@Dao
public interface PlanogrammGroupDao {
    @Query("SELECT * FROM planogramm_group")
    List<PlanogrammGroupSDB> getAll();

    @Query("SELECT * FROM planogramm_group WHERE ID = :id")
    PlanogrammGroupSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<PlanogrammGroupSDB> data);

    // ----------------------------------------------------
}
