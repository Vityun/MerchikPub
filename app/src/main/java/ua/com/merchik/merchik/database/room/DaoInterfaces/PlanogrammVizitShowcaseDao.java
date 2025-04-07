package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB;


@Dao
public interface PlanogrammVizitShowcaseDao {
    @Insert
    void insert(PlanogrammVizitShowcaseSDB planogrammVizitShowcase);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<PlanogrammVizitShowcaseSDB> data);

    @Query("SELECT * FROM planogram_vizit_showcase")
    List<PlanogrammVizitShowcaseSDB> getAll();

    @Query("SELECT * FROM planogram_vizit_showcase WHERE id = :id")
    PlanogrammVizitShowcaseSDB getById(int id);

    @Update
    void update(PlanogrammVizitShowcaseSDB planogrammVizitShowcase);

    @Delete
    void delete(PlanogrammVizitShowcaseSDB planogrammVizitShowcase);
}