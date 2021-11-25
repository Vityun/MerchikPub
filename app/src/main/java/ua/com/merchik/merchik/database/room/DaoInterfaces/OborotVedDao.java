package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.OborotVedSDB;

@Dao
public interface OborotVedDao {
    @Query("SELECT * FROM oborot_ved")
    List<OborotVedSDB> getAll();

    @Query("SELECT * FROM oborot_ved WHERE id = :id")
    OborotVedSDB getLanguageById(int id);

    @Query("SELECT * FROM oborot_ved WHERE date(dat) > date(:endDate) AND date(dat) < date(:startDate) AND tov_id = :tovId AND adr_id = :adrId ORDER BY adr_id")
    List<OborotVedSDB> getOborotData(String startDate, String endDate, int tovId, int adrId);

    @Query("SELECT * FROM oborot_ved WHERE date(dat) > date(:s)")
    List<OborotVedSDB> getTest(String s);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OborotVedSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<OborotVedSDB> data);
}
