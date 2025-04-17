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

    @Insert
    Completable insertWithOutReplace(List<PlanogrammVizitShowcaseSDB> data);

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

    @Update
    void update(PlanogrammVizitShowcaseSDB planogrammVizitShowcase);

    @Delete
    void delete(PlanogrammVizitShowcaseSDB planogrammVizitShowcase);

    @Query("""
    SELECT p1.* FROM planogram_vizit_showcase p1
    INNER JOIN (
        SELECT planogram_id, MAX(dt_update) as max_dt 
        FROM planogram_vizit_showcase 
        WHERE client_id = :client_id 
          AND addr_id = :addr_id 
          AND (:code_dad2 IS NULL OR code_dad2 = :code_dad2)
        GROUP BY planogram_id
    ) p2 ON p1.planogram_id = p2.planogram_id AND p1.dt_update = p2.max_dt
    INNER JOIN (
        SELECT planogram_id, dt_update, MAX(id) as max_id
        FROM planogram_vizit_showcase
        GROUP BY planogram_id, dt_update
    ) p3 ON p1.planogram_id = p3.planogram_id 
          AND p1.dt_update = p3.dt_update 
          AND p1.id = p3.max_id
    WHERE p1.client_id = :client_id 
      AND p1.addr_id = :addr_id
      AND (:code_dad2 IS NULL OR p1.code_dad2 = :code_dad2)
    ORDER BY p1.planogram_id
""")
    List<PlanogrammVizitShowcaseSDB> getByClientIdAddressIdAndDad2(
            String client_id,
            int addr_id,
            Long code_dad2
    );

    @Query("""
    SELECT p1.* FROM planogram_vizit_showcase p1
    INNER JOIN (
        SELECT planogram_id, MAX(id) as max_id
        FROM planogram_vizit_showcase
        WHERE client_id = :client_id AND addr_id = :addr_id
        GROUP BY planogram_id
    ) p2 ON p1.planogram_id = p2.planogram_id AND p1.id = p2.max_id
    WHERE p1.client_id = :client_id AND p1.addr_id = :addr_id
""")
    List<PlanogrammVizitShowcaseSDB> getByClientIdAdressIdUnique(String client_id, int addr_id);

    @Query("SELECT * FROM planogram_vizit_showcase WHERE uploadStatus = 1")
    List<PlanogrammVizitShowcaseSDB> getAllUploadedPlanograms();


}