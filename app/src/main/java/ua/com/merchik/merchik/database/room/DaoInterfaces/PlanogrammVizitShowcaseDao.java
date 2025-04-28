package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
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

    @Query("SELECT * FROM planogram_vizit_showcase WHERE planogram_photo_id = :planogram_photo_id " +
            "AND client_id = :client_id " +
            "AND addr_id = :addr_id")
    PlanogrammVizitShowcaseSDB getByPlanogrammIdClientIdAdressId(int planogram_photo_id, String client_id, int addr_id);


    @Query("SELECT * FROM planogram_vizit_showcase WHERE client_id = :client_id AND addr_id = :addr_id")
    List<PlanogrammVizitShowcaseSDB> getByClientIdAdressId(String client_id, int addr_id);

    @Query("SELECT * FROM planogram_vizit_showcase WHERE client_id = :client_id")
    List<PlanogrammVizitShowcaseSDB> getByClient(String client_id);

    @Query("SELECT * FROM planogram_vizit_showcase WHERE code_dad2 = :codeDad2")
    List<PlanogrammVizitShowcaseSDB> getByCodeDad2(Long codeDad2);

    @Update
    void update(PlanogrammVizitShowcaseSDB planogrammVizitShowcase);

    @Delete
    void delete(PlanogrammVizitShowcaseSDB planogrammVizitShowcase);



    @Query("SELECT * FROM planogram_vizit_showcase WHERE uploadStatus = 1")
    List<PlanogrammVizitShowcaseSDB> getAllUploadedPlanograms();


    @Query("UPDATE planogram_vizit_showcase SET uploadStatus = 0 WHERE ID IN (:ids)")
    void markUploaded(List<Integer> ids);
}