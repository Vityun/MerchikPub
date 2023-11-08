package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB;

@Dao
public interface SamplePhotoDao {
    @Query("SELECT * FROM sample_photo")
    List<SamplePhotoSDB> getAll();

    @Query("SELECT photoId FROM sample_photo")
    List<Integer> getAllPhotosIds();

    @Query("SELECT * FROM sample_photo WHERE active = :active")
    List<SamplePhotoSDB> getPhotoLogActive(Integer active);

    @Query("SELECT * FROM sample_photo WHERE active = :active AND photoTp = :photoTp")
    List<SamplePhotoSDB> getPhotoLogActiveAndTp(Integer active, Integer photoTp);

    @Query("SELECT * FROM sample_photo WHERE id = :id")
    SamplePhotoSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<SamplePhotoSDB> data);

    // ----------------------------------------------------
}
