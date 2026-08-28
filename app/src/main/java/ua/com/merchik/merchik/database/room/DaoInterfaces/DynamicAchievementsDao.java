package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.DynamicAchievementSDB;

@Dao
public interface DynamicAchievementsDao {

    @Query("SELECT * FROM dynamic_achievements")
    List<DynamicAchievementSDB> getAll();

    @Query("SELECT * FROM dynamic_achievements WHERE ID = :id LIMIT 1")
    DynamicAchievementSDB getById(String id);

    @Query("SELECT * FROM dynamic_achievements " +
            "WHERE (:clientId IS NULL OR :clientId = '' OR client_id = :clientId) " +
            "AND (:addrId IS NULL OR :addrId = '' OR addr_id = :addrId) " +
            "ORDER BY dt_update DESC")
    List<DynamicAchievementSDB> getByClientAndAddress(String clientId, String addrId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DynamicAchievementSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAllCompletable(List<DynamicAchievementSDB> data);
}
