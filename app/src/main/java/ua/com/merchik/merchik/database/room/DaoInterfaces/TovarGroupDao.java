package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB;

@Dao
public interface TovarGroupDao {
    @Query("SELECT * FROM tovar_group")
    List<TovarGroupSDB> getAll();

    @Query("SELECT * FROM tovar_group WHERE id = :id")
    TovarGroupSDB getById(int id);

    @Query("SELECT * FROM tovar_group WHERE id IN (:listIds)")
    List<TovarGroupSDB> getAllByIds(List<Integer> listIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TovarGroupSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<TovarGroupSDB> data);
}
