package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.annotation.Nullable;
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





    @Query("SELECT DISTINCT " +
            "pl.id AS id, " +
            "pl.client_id AS planogrammClientId, " +
            "TRIM(pl.nm) AS planogrammName, " +
            "TRIM(pl.comments) AS planogrammComment, " +
            "pl.dt_start AS planogrammDtStart, " +
            "pl.dt_end AS planogrammDtEnd, " +
            "plaa.addr_id AS planogrammAddress, " +
            "plaa.addr_txt AS planogrammAddressTxt, " +
            "plaa.city_txt AS planogrammCityTxt, " +
            "plag.group_id AS planogrammGroupId, " +
            "plag.group_txt AS planogrammGroupTxt, " +
            "pl.img_id AS planogrammPhotoId " +
            "FROM planogramm pl " +
            "LEFT JOIN planogramm_address plaa ON pl.id = plaa.planogram_id " +
            "LEFT JOIN planogramm_group plag ON pl.id = plag.planogram_id AND plag.group_id = 351 " +
            "LEFT JOIN planogramm_type platt ON pl.id = platt.planogram_id " +
            "WHERE pl.client_id = :clientId " +
            "AND ( " +
            "   (plaa.addr_id IS NOT NULL AND plag.group_id IS NOT NULL AND platt.tt_id IS NOT NULL) OR " +
            "   (plaa.addr_id IS NOT NULL AND plag.group_id IS NOT NULL AND (platt.tt_id IS NULL OR platt.tt_id = 0)) OR " +
            "   (plaa.addr_id IS NOT NULL AND (plag.group_id IS NULL OR plag.group_id = 0) AND platt.tt_id IS NOT NULL) OR " +
            "   ((plaa.addr_id IS NULL OR plaa.addr_id = 0) AND plag.group_id IS NOT NULL AND platt.tt_id IS NOT NULL) OR " +
            "   (plaa.addr_id IS NOT NULL AND (plag.group_id IS NULL OR plag.group_id = 0) AND (platt.tt_id IS NULL OR platt.tt_id = 0)) OR " +
            "   ((plaa.addr_id IS NULL OR plaa.addr_id = 0) AND plag.group_id IS NOT NULL AND (platt.tt_id IS NULL OR platt.tt_id = 0)) OR " +
            "   ((plaa.addr_id IS NULL OR plaa.addr_id = 0) AND (plag.group_id IS NULL OR plag.group_id = 0) AND platt.tt_id IS NOT NULL) OR " +
            "   ((plaa.addr_id IS NULL OR plaa.addr_id = 0) AND (plag.group_id IS NULL OR plag.group_id = 0) AND (platt.tt_id IS NULL OR platt.tt_id = 0)) " +
            ")")
    List<PlanogrammJOINSDB> getPlanogrammsByClient(String clientId);


    @Query("SELECT DISTINCT " +
            "pl.id AS id, " +
            "pl.client_id AS planogrammClientId, " +
            "TRIM(pl.nm) AS planogrammName, " +
            "TRIM(pl.comments) AS planogrammComment, " +
            "pl.dt_start AS planogrammDtStart, " +
            "pl.dt_end AS planogrammDtEnd, " +
            "plaa.addr_id AS planogrammAddress, " +
            "plaa.addr_txt AS planogrammAddressTxt, " +
            "plaa.city_txt AS planogrammCityTxt, " +
            "plag.group_id AS planogrammGroupId, " +
            "plag.group_txt AS planogrammGroupTxt, " +
            "pl.img_id AS planogrammPhotoId " +
            "FROM planogramm pl " +
            "LEFT JOIN planogramm_address plaa ON pl.id = plaa.planogram_id " +
            "LEFT JOIN planogramm_group plag ON pl.id = plag.planogram_id AND plag.group_id = 351 " +
            "LEFT JOIN planogramm_type platt ON pl.id = platt.planogram_id " +
            "WHERE pl.client_id = :clientId " +
            "AND (:addressId IS NULL OR plaa.addr_id = :addressId) " + // Фильтр по адресу
            "AND (:ttId IS NULL OR platt.tt_id = :ttId) " + // Фильтр по TT
            "AND ( " +
            "   (plaa.addr_id IS NOT NULL AND plag.group_id IS NOT NULL AND platt.tt_id IS NOT NULL) OR " +
            "   (plaa.addr_id IS NOT NULL AND plag.group_id IS NOT NULL AND (platt.tt_id IS NULL OR platt.tt_id = 0)) OR " +
            "   (plaa.addr_id IS NOT NULL AND (plag.group_id IS NULL OR plag.group_id = 0) AND platt.tt_id IS NOT NULL) OR " +
            "   ((plaa.addr_id IS NULL OR plaa.addr_id = 0) AND plag.group_id IS NOT NULL AND platt.tt_id IS NOT NULL) OR " +
            "   (plaa.addr_id IS NOT NULL AND (plag.group_id IS NULL OR plag.group_id = 0) AND (platt.tt_id IS NULL OR platt.tt_id = 0)) OR " +
            "   ((plaa.addr_id IS NULL OR plaa.addr_id = 0) AND plag.group_id IS NOT NULL AND (platt.tt_id IS NULL OR platt.tt_id = 0)) OR " +
            "   ((plaa.addr_id IS NULL OR plaa.addr_id = 0) AND (plag.group_id IS NULL OR plag.group_id = 0) AND platt.tt_id IS NOT NULL) OR " +
            "   ((plaa.addr_id IS NULL OR plaa.addr_id = 0) AND (plag.group_id IS NULL OR plag.group_id = 0) AND (platt.tt_id IS NULL OR platt.tt_id = 0)) " +
            ")")
    List<PlanogrammJOINSDB> getPlanogrammsByClientAddressTtId(
            String clientId,
            Integer addressId,
            String ttId
    );


//    @Query("""
//            SELECT DISTINCT
//              p.id AS id,
//              p.client_id AS planogrammClientId,
//              p.nm AS planogrammName,
//              p.comments AS planogrammComment,
//              p.dt_start AS planogrammDtStart,
//              p.dt_end AS planogrammDtEnd,
//              pa.addr_id AS planogrammAddress,
//              pa.addr_txt AS planogrammAddressTxt,
//              pa.city_txt AS planogrammCityTxt,
//              pg.group_id AS planogrammGroupId,
//              pg.group_txt AS planogrammGroupTxt,
//              p.photo_id AS planogrammPhotoId
//            FROM planogramm p
//            LEFT JOIN planogramm_address pa ON pa.planogram_id = p.id
//            LEFT JOIN planogramm_group pg ON pg.planogram_id = p.id
//            LEFT JOIN planogramm_type pt ON pt.planogram_id = p.id
//            WHERE (pa.addr_id IS NOT NULL AND pa.addr_id > 0)
//              AND (pg.group_id IS NOT NULL AND pg.group_id > 0)
//              AND (pt.tt_id IS NOT NULL AND pt.tt_id > 0)
//            """)
//    List<PlanogrammJOINSDB> getPlanogrammsByClientAddressTtId2(
//            String clientId,
//            Integer addressId,
//            String ttId
//    );

    @Query("""
    SELECT DISTINCT
    p.id as id,
    p.client_id as planogrammClientId,
    p.client_txt as planogrammClientTxt,
    p.nm as planogrammName,
    p.comments as planogrammComment,
    p.dt_start as planogrammDtStart,
    p.dt_end as planogrammDtEnd,
    pa.addr_id as planogrammAddress,
    pa.addr_txt as planogrammAddressTxt,
    pa.city_txt as planogrammCityTxt,
    pg.group_id as planogrammGroupId,
    pg.group_txt as planogrammGroupTxt,
    p.photo_id as planogrammPhotoId
    FROM planogramm as p
    LEFT JOIN planogramm_address as pa ON pa.planogram_id = p.id
    LEFT JOIN planogramm_group as pg ON pg.planogram_id = p.id
    LEFT JOIN planogramm_type as pt ON pt.planogram_id = p.id
    WHERE p.client_id = :clientId
    AND (:addressId IS NULL OR pa.addr_id = :addressId)
    AND (:ttId IS NULL OR pt.tt_id = :ttId)
    ORDER BY p.dt_start DESC
""")
    List<PlanogrammJOINSDB> getPlanogrammsByClientAddressTtId3(
            String clientId,
            Integer addressId,
            String ttId
    );
}
