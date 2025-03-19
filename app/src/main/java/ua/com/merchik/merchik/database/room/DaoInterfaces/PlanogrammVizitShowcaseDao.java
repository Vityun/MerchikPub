package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB;


@Dao
public interface PlanogrammVizitShowcaseDao {
    @Insert
    void insert(PlanogrammVizitShowcaseSDB planogrammVizitShowcase);

    @Query("SELECT * FROM planogram_vizit_showcase")
    List<PlanogrammVizitShowcaseSDB> getAll();

    @Query("SELECT * FROM planogram_vizit_showcase WHERE id = :id")
    PlanogrammVizitShowcaseSDB getById(int id);

    @Update
    void update(PlanogrammVizitShowcaseSDB planogrammVizitShowcase);

    @Delete
    void delete(PlanogrammVizitShowcaseSDB planogrammVizitShowcase);
}