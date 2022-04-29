package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;

@Dao
public interface AddressDao {
    @Query("SELECT * FROM address")
    List<AddressSDB> getAll();

    @Query("SELECT * FROM address")
    Flowable<List<UsersSDB>> getAllFlow();

    @Query("SELECT * FROM address WHERE id = :id")
    AddressSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AddressSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<AddressSDB> data);
}
