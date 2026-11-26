package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.CoordOnMapSDB;

@Dao
public interface CoordOnMapDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveAll(List<CoordOnMapSDB> rows);

    @Query("SELECT * FROM coord_on_map WHERE ID = :id")
    CoordOnMapSDB getById(long id);
}
