package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.SMS.SMSLogSDB;

@Dao
public interface SMSLogDao {
    @Query("SELECT * FROM sms_log")
    List<SMSLogSDB> getAll();

    @Query("SELECT * FROM sms_log WHERE id = :id")
    SMSLogSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SMSLogSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAllCompletable(List<SMSLogSDB> data);

    // ---------------------------------------------------------

    @Query("SELECT * FROM sms_log WHERE addr_id = :addr AND client_to = :client AND tp = :tp AND dt BETWEEN :dtFrom AND :dtTo")
    List<SMSLogSDB> getAll(Long dtFrom, Long dtTo, Integer tp, Integer addr, String client);
}
