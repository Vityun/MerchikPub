package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.TradeMarkSDB;

@Dao
public interface TradeMarkDao {
    @Query("SELECT * FROM trade_mark")
    List<TradeMarkSDB> getAll();

    @Query("SELECT COUNT(*) FROM trade_mark")
    int getCount();

    @Query("SELECT * FROM trade_mark WHERE id = :id LIMIT 1")
    TradeMarkSDB getById(String id);

    @Query("SELECT * FROM trade_mark WHERE id IN (:ids)")
    List<TradeMarkSDB> getByIds(String[] ids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TradeMarkSDB> data);
}
