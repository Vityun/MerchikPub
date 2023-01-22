package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.sql.Date;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.ReclamationPercentageSDB;

@Dao
public interface ReclamationPercentageDao {
    @Query("SELECT * FROM reclamation_percentage")
    List<ReclamationPercentageSDB> getAll();

    @Query("SELECT * FROM reclamation_percentage WHERE id = :id")
    ReclamationPercentageSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<ReclamationPercentageSDB> data);

    // ----------------------------------------------------

    @Query("SELECT * FROM reclamation_percentage WHERE tp = :tp AND dt BETWEEN :dtF AND :dtT ORDER BY dt DESC")
    List<ReclamationPercentageSDB> getAll(Date dtF, Date dtT, Integer tp);
}
