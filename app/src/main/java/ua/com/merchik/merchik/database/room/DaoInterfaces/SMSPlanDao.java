package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.SMS.SMSPlanSDB;

@Dao
public interface SMSPlanDao {

    @Query("SELECT * FROM sms_plan")
    List<SMSPlanSDB> getAll();

    @Query("SELECT * FROM sms_plan WHERE id = :id")
    SMSPlanSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SMSPlanSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAllCompletable(List<SMSPlanSDB> data);

    // ---------------------------------------------------------

    @Query("SELECT * FROM sms_plan WHERE addr_id = :addr AND client_to = :client AND tp = :tp AND dt BETWEEN :dtFrom AND :dtTo")
    List<SMSPlanSDB> getAll(Long dtFrom, Long dtTo, Integer tp, Integer addr, String client);
}
