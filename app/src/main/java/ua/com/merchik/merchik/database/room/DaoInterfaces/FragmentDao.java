package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.FragmentSDB;

@Dao
public interface FragmentDao {
    @Query("SELECT * FROM fragment")
    List<FragmentSDB> getAll();

    @Query("SELECT * FROM fragment WHERE img_id = :id")
    List<FragmentSDB> getAllByPhotoId(int id);

    @Query("SELECT * FROM fragment WHERE id = :id")
    FragmentSDB getById(int id);

    @Query("SELECT MAX(dt_update) FROM fragment")
    Integer getLastDtUpdate();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FragmentSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertData(List<FragmentSDB> data);
}
