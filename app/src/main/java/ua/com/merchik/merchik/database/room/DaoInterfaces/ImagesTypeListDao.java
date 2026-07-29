package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.ImagesTypeListSDB;

@Dao
public interface ImagesTypeListDao {
    @Query("SELECT * FROM images_type_list")
    List<ImagesTypeListSDB> getAll();

    @Query("SELECT COUNT(*) FROM images_type_list")
    int getCount();

    @Query("SELECT * FROM images_type_list WHERE id = :id LIMIT 1")
    ImagesTypeListSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ImagesTypeListSDB> data);
}
