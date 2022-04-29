package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDBDat.UserSDBJoin;

@Dao
public interface UsersDao {
    @Query("SELECT * FROM sotr")
    Flowable<List<UsersSDB>> getAll();

    @Query("SELECT * FROM sotr")
    List<UsersSDB> getAll2();

    @Query("SELECT s.fio FROM sotr s WHERE id = :id")
    String getUserName(int id);

    @Query("SELECT * FROM sotr WHERE work_addr_id = :addrId")
    List<UsersSDB> getPTT(int addrId);

    @Query("SELECT * FROM sotr WHERE id = :id")
    UsersSDB getUserById(int id);

    @Query("SELECT * FROM sotr limit 50")
    Flowable<List<UsersSDB>> getAllLim50();

    @Query("SELECT * FROM sotr WHERE id = :id")
    UsersSDB getById(int id);

    @Query("SELECT * FROM sotr WHERE id = :id")
    Flowable<UsersSDB> getByIdF(int id);

    @Query("SELECT s.*, t.nm FROM sotr s LEFT JOIN tovar_group t ON s.otdel_id = t.id WHERE work_addr_id = :addrId ORDER BY fio")
    List<UserSDBJoin> getAllUsersLJoinTovGrps(int addrId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UsersSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<UsersSDB> data);
}
