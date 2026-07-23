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

    @Query("SELECT * FROM wp_data_pause WHERE uploadStatus = 1")
    List<WPDataPauseSDB> getUploadToServer();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSync(WPDataPauseSDB item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WPDataPauseSDB> items);

    @Query("UPDATE wp_data_pause SET uploadStatus = 0 WHERE code_dad2 = :codeDad2 AND dt_start = :dtStart")
    void markUploadedSync(long codeDad2, long dtStart);

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
