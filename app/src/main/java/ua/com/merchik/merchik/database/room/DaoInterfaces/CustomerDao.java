package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;

@Dao
public interface CustomerDao {
    @Query("SELECT * FROM client")
    List<CustomerSDB> getAll();

    @Query("SELECT * FROM client ORDER BY nm DESC")
    List<CustomerSDB> getAllSortedByNm();

    @Query("SELECT * FROM client WHERE id = :id")
    CustomerSDB getById(int id);

    @Query("SELECT * FROM client WHERE nm = :nm")
    CustomerSDB getByNm(int nm);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CustomerSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<CustomerSDB> data);
}
