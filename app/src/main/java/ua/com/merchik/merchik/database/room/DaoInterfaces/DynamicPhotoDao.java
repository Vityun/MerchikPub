package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.DynamicPhotoSDB;

@Dao
public interface DynamicPhotoDao {

    @Query("SELECT * FROM dynamic_photo")
    List<DynamicPhotoSDB> getAll();

    @Query("SELECT * FROM dynamic_photo WHERE ID = :id LIMIT 1")
    DynamicPhotoSDB getById(String id);

    @Query("SELECT * FROM dynamic_photo WHERE achieve_dynamics_id = :dynamicAchievementId")
    List<DynamicPhotoSDB> getByDynamicAchievementId(String dynamicAchievementId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DynamicPhotoSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAllCompletable(List<DynamicPhotoSDB> data);
}
