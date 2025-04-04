package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammImagesSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammTypeSDB;


@Dao
public interface PlanogrammTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<PlanogrammTypeSDB> planograms);

    @Query("SELECT * FROM planogramm_type")
    List<PlanogrammTypeSDB> getAllPlanogramsm();

    @Query("SELECT * FROM planogramm_type WHERE ID = :id")
    PlanogrammTypeSDB getById(int id);
}