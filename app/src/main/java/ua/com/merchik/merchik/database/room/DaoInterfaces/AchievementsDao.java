package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB;

@Dao
public interface AchievementsDao {
    @Query("SELECT * FROM achievements")
    List<AchievementsSDB> getAll();

    @Query("SELECT * FROM achievements WHERE id = :id")
    AchievementsSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AchievementsSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAllCompletable(List<AchievementsSDB> data);
}
