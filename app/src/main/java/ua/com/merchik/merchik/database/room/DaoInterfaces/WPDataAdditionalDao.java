package ua.com.merchik.merchik.database.room.DaoInterfaces;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.WPDataAdditional;


@Dao
public interface WPDataAdditionalDao {

    @Insert
    void insertAll(List<WPDataAdditional> items);

    @Query("SELECT * FROM wp_data_additional")
    List<WPDataAdditional> getAll();

}
