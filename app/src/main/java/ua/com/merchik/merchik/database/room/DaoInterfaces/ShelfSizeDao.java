package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.ShelfSizeSDB;

@Dao
public interface ShelfSizeDao {
    @Query("SELECT * FROM shelf_size")
    List<ShelfSizeSDB> getAll();

    @Query("SELECT * FROM shelf_size WHERE id = :id")
    ShelfSizeSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<ShelfSizeSDB> data);

    // ----------------------------------------------------

    @Query("SELECT * FROM shelf_size WHERE client_id = :clientId AND addr_id = :addressId ORDER BY dt DESC")
    List<ShelfSizeSDB> getBy(String clientId, Integer addressId);
}
