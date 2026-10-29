package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammImagesSDB;

@Dao
public interface PlanogrammImagesDao {
    @Query("SELECT * FROM planogramm_img")
    List<PlanogrammImagesSDB> getAll();

    @Query("SELECT * FROM planogramm_img WHERE ID = :id")
    PlanogrammImagesSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<PlanogrammImagesSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<PlanogrammImagesSDB> data);

    @Query("SELECT COUNT(*) FROM planogramm_img")
    long count();

    // ----------------------------------------------------
}
