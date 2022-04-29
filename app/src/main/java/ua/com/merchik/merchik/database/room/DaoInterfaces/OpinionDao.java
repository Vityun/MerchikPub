package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;

@Dao
public interface OpinionDao {
    @Query("SELECT * FROM opinions")
    List<OpinionSDB> getAll();

    @Query("SELECT * FROM opinions WHERE id = :id")
    OpinionSDB getOpinionById(int id);

    @Query("SELECT * FROM opinions WHERE id = :id")
    Flowable<OpinionSDB> getOpinionByIdF(int id);

    @Query("SELECT * FROM opinions WHERE nm = :nm")
    OpinionSDB getOpinionByNm(String nm);

    @Query("SELECT * FROM opinions WHERE id IN (:ids)")
    List<OpinionSDB> getOpinionByIds(List<String> ids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OpinionSDB> data);
}
