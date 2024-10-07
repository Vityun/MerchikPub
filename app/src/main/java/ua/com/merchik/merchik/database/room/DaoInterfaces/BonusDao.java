package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.BonusSDB;

@Dao
public interface BonusDao {
    @Query("SELECT * FROM bonus")
    List<BonusSDB> getAll();

    @Query("SELECT * FROM bonus WHERE id = :id")
    BonusSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BonusSDB> itemList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BonusSDB item);

    @Query("SELECT * FROM bonus " +
            "WHERE (:id IS NULL OR id = :id) " +
            "AND (:themeId IS NULL OR theme_id = :themeId) " +
            "AND (:optionId IS NULL OR option_id = :optionId) ")
    List<BonusSDB> getData(Long id, Long themeId, Long optionId);
}
