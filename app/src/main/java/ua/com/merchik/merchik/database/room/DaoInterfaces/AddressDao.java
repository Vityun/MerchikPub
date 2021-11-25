package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;

@Dao
public interface AddressDao {
    @Query("SELECT * FROM address")
    List<AddressSDB> getAll();

    @Query("SELECT * FROM address WHERE id = :id")
    AddressSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AddressSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<AddressSDB> data);
}
