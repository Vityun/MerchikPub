package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammJOINSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB;

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

    @Query("SELECT " +
            "planogramm.id AS id, " +
            "planogramm.client_id AS planogrammClientId, " +
            "planogramm.client_txt AS planogrammClientTxt, " +
            "planogramm.nm AS planogrammName," +
            "planogramm.comments AS planogrammComment," +
            "planogramm.dt_start AS planogrammDtStart," +
            "planogramm.dt_end AS planogrammDtEnd," +
            "pa.addr_id AS planogrammAddress," +
            "pa.addr_txt AS planogrammAddressTxt," +
            "pa.city_txt AS planogrammCityTxt," +
            "pg.group_id AS planogrammGroupId," +
            "pg.group_txt AS planogrammGroupTxt," +
            "pi.photo_id AS planogrammPhotoId " +
            "FROM planogramm " +
            "LEFT JOIN (" +
            "  SELECT planogram_id, MAX(addr_id) AS addr_id, MAX(addr_txt) AS addr_txt, MAX(city_txt) AS city_txt " +
            "  FROM planogramm_address " +
            "  GROUP BY planogram_id" +
            ") pa ON planogramm.id = pa.planogram_id " +
            "LEFT JOIN (" +
            "  SELECT planogram_id, MAX(group_id) AS group_id, MAX(group_txt) AS group_txt " +
            "  FROM planogramm_group " +
            "  GROUP BY planogram_id" +
            ") pg ON planogramm.id = pg.planogram_id " +
            "LEFT JOIN (" +
            "  SELECT planogram_id, MAX(photo_id) AS photo_id " +
            "  FROM planogramm_img " +
            "  GROUP BY planogram_id" +
            ") pi ON planogramm.id = pi.planogram_id " +
            "WHERE " +
//            "(planogramm.client_id = :clientId OR :clientId IS NULL) " +
//            "AND (pa.addr_id = :addressId OR :addressId IS NULL OR pa.addr_id IS NULL) " +
//            "AND (pg.group_id = :groupId OR :groupId IS NULL OR pg.group_id IS NULL)")
            "(planogramm.client_id = :clientId OR :clientId IS NULL) " +
            "AND (pa.addr_id = :addressId OR :addressId IS NULL OR pa.addr_id IS NULL) " +
            "AND (pg.group_id = :groupId OR :groupId IS NULL OR pg.group_id IS NULL) " +
            "AND ((planogramm.dt_start <= :currentDate OR planogramm.dt_start = '0000-00-00' OR planogramm.dt_start IS NULL) " +
            "AND (planogramm.dt_end >= :currentDate OR planogramm.dt_end = '0000-00-00' OR planogramm.dt_end IS NULL))")
    List<PlanogrammJOINSDB> getByClientAddress(String clientId, Integer addressId, Integer groupId, String currentDate);    // yyyy-MM-dd

    @Query("SELECT " +
            "planogramm.id AS id, " +
            "planogramm.client_id AS planogrammClientId, " +
            "planogramm.client_txt AS planogrammClientTxt, " +
            "planogramm.nm AS planogrammName," +
            "planogramm.comments AS planogrammComment," +
            "planogramm.dt_start AS planogrammDtStart," +
            "planogramm.dt_end AS planogrammDtEnd," +
            "planAddr.addr_id AS planogrammAddress," +
            "planAddr.addr_txt AS planogrammAddressTxt," +
            "planAddr.city_txt AS planogrammCityTxt," +
            "planGrp.group_id AS planogrammGroupId," +
            "planGrp.group_txt AS planogrammGroupTxt," +
            "planImg.photo_id AS planogrammPhotoId " +
            "FROM planogramm " +
            "LEFT JOIN planogramm_address AS planAddr ON planogramm.id=planAddr.planogram_id " +
            "LEFT JOIN planogramm_group AS planGrp ON planogramm.id=planGrp.planogram_id " +
            "LEFT JOIN planogramm_img AS planImg ON planogramm.id=planImg.planogram_id " +
            "WHERE " +
            "(planogramm.id = :planogrammId OR :planogrammId IS NULL) " +
            "AND (planogramm.client_id = :clientId OR :clientId IS NULL) " +
            "AND (planAddr.addr_id = :addressId OR :addressId IS NULL)")
    PlanogrammJOINSDB getSoloBy(Integer planogrammId, String clientId, Integer addressId);

}
