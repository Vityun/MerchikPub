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
    Flowable<List<EKL_SDB>> getAllF();

    @Query("SELECT * FROM ekl")
    List<EKL_SDB> getAll();

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

    /*пока юзаю только в ЭКЛах*/
    @Query("SELECT * FROM ekl WHERE client_id = :client_id AND address_id = :addr_id AND user_id = :user_id AND (vpi IS NOT NULL AND vpi BETWEEN :l AND :l1 OR dt BETWEEN :l AND :l1)")
    List<EKL_SDB> getBy(long l, long l1, String client_id, int addr_id, int user_id);

    @Query("SELECT * FROM ekl WHERE client_id = :client_id AND address_id = :addr_id AND user_id = :user_id AND sotr_id = :ptt_user_id AND (vpi IS NOT NULL AND vpi BETWEEN :l AND :l1 OR dt BETWEEN :l AND :l1)")
    List<EKL_SDB> getBy(long l, long l1, String client_id, int addr_id, int user_id, int ptt_user_id);

    @Query("SELECT * FROM ekl WHERE department IN (:tovarGroup) AND address_id = :addr_id AND user_id = :user_id AND sotr_id = :ptt_user_id AND (vpi IS NOT NULL AND vpi BETWEEN :l AND :l1 OR dt BETWEEN :l AND :l1)")
    List<EKL_SDB> getBy(long l, long l1, List<Integer> tovarGroup, int addr_id, int user_id, int ptt_user_id);
}
