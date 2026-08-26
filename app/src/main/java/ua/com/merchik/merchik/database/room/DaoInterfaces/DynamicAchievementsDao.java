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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DynamicAchievementSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAllCompletable(List<DynamicAchievementSDB> data);
}
