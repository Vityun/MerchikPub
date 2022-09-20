package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;

@Dao
public interface EKLDao {
    @Query("SELECT * FROM ekl")
    Flowable<List<EKL_SDB>> getAll();

    @Query("SELECT * FROM ekl WHERE id = :id")
    Flowable<EKL_SDB> getById(int id);

    @Query("SELECT * FROM ekl WHERE user_id = :id")
    EKL_SDB getByUserId(int id);

    @Query("SELECT * FROM ekl WHERE dad2 = :dad2")
    List<EKL_SDB> getByDad2(long dad2);

    @Query("SELECT * FROM ekl WHERE client_id = :clientId")
    List<EKL_SDB> getByClientId(String clientId);

    @Query("SELECT * FROM ekl WHERE ekl_hash_code = :code")
    Flowable<EKL_SDB> getByHashCode(String code);

    @Query("SELECT count('id') FROM ekl WHERE ekl_hash_code = :code")
    Integer getCountHashCode(String code);

    @Query("SELECT * FROM ekl WHERE ekl_code IS NOT NULL AND upload IS NULL")
    List<EKL_SDB> getEKLToUpload();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EKL_SDB> data);



}
