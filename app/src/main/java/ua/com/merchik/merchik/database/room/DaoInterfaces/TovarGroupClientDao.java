package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupClientSDB;

@Dao
public interface TovarGroupClientDao {
    @Query("SELECT * FROM tovar_grp_client")
    List<TovarGroupClientSDB> getAll();

    @Query("SELECT * FROM tovar_grp_client WHERE id = :id")
    TovarGroupClientSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TovarGroupClientSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<TovarGroupClientSDB> data);

    @Query("SELECT * FROM tovar_grp_client WHERE client_id = :clientId")
    List<TovarGroupClientSDB> getAllBy(String clientId);
}
