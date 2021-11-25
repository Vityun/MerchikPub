package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;

@Dao
public interface OpinionDao {
    @Query("SELECT * FROM opinions")
    List<OpinionSDB> getAll();

    @Query("SELECT * FROM opinions WHERE id = :id")
    OpinionSDB getOpinionById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OpinionSDB> data);
}
