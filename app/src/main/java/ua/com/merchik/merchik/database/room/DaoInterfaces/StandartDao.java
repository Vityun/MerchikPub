package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.StandartSDB;

@Dao
public interface StandartDao {
    @Query("SELECT * FROM standart")
    List<StandartSDB> getAll();

    @Query("SELECT * FROM standart WHERE code_dad2 = :dad2")
    List<StandartSDB> getByDad2(long dad2);

    @Query("SELECT * FROM standart WHERE id = :id")
    StandartSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StandartSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<StandartSDB> data);
}
