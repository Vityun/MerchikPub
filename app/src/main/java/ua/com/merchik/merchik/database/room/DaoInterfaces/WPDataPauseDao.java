package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.WPDataPauseSDB;

@Dao
public interface WPDataPauseDao {

    @Query("SELECT * FROM wp_data_pause")
    List<WPDataPauseSDB> getAll();

    @Query("SELECT * FROM wp_data_pause WHERE code_dad2 = :codeDad2 AND dt_start = :dtStart LIMIT 1")
    WPDataPauseSDB getByIdSync(long codeDad2, long dtStart);

    @Query("SELECT * FROM wp_data_pause WHERE code_dad2 = :codeDad2")
    List<WPDataPauseSDB> getAllByDad2(long codeDad2);

    @Query("SELECT * FROM wp_data_pause WHERE code_dad2 = :codeDad2 AND dt_end = 0 ORDER BY dt_start DESC LIMIT 1")
    WPDataPauseSDB getActiveByCodeDad2Sync(long codeDad2);

    @Query("SELECT * FROM wp_data_pause WHERE dt_end = 0 ORDER BY dt_start DESC")
    List<WPDataPauseSDB> getActivePausesSync();

    @Query("SELECT COUNT(DISTINCT code_dad2) FROM wp_data_pause WHERE dt_end = 0")
    int getActivePauseVisitCountSync();

    @Query("SELECT code_dad2 FROM wp_data_pause WHERE dt_end = 0 GROUP BY code_dad2 ORDER BY MAX(dt_start) DESC")
    List<Long> getActivePauseCodeDad2ListSync();

    @Query("SELECT * FROM wp_data_pause WHERE uploadStatus = 1")
    List<WPDataPauseSDB> getUploadToServer();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSync(WPDataPauseSDB item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WPDataPauseSDB> items);

    @Query("UPDATE wp_data_pause SET uploadStatus = 0 WHERE code_dad2 = :codeDad2 AND dt_start = :dtStart")
    void markUploadedSync(long codeDad2, long dtStart);

    @Query("UPDATE wp_data_pause SET dt_end = :dtEnd, dt_update_client = :dtUpdateClient, uploadStatus = 1 WHERE code_dad2 = :codeDad2 AND dt_end = 0")
    int finishActivePauseSync(long codeDad2, long dtEnd, long dtUpdateClient);

    @Query("DELETE FROM wp_data_pause WHERE code_dad2 = :codeDad2")
    void deleteByCodeDad2Sync(long codeDad2);

    @Transaction
    default void markUploadedSync(List<WPDataPauseSDB> items) {
        if (items == null || items.isEmpty()) return;

        for (WPDataPauseSDB item : items) {
            markUploadedSync(item.codeDad2, item.dtStart);
        }
    }

    @Transaction
    default void insertDownloadedSync(List<WPDataPauseSDB> items) {
        if (items == null || items.isEmpty()) return;

        for (WPDataPauseSDB item : items) {
            if (item == null) continue;

            WPDataPauseSDB local = getByIdSync(item.codeDad2, item.dtStart);
            if (local != null && local.uploadStatus == 1) {
                continue;
            }

            item.uploadStatus = 0;
            insertSync(item);
        }
    }
}
