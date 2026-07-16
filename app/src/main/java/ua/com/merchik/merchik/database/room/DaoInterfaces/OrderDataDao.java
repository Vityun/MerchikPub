package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.OrderDataSDB;

@Dao
public interface OrderDataDao {

    @Query("SELECT * FROM order_data ORDER BY CAST(order_id AS INTEGER) DESC")
    List<OrderDataSDB> getAll();

    @Query("SELECT * FROM order_data WHERE id = :id LIMIT 1")
    OrderDataSDB getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OrderDataSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertData(List<OrderDataSDB> data);
}
