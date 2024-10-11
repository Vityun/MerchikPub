package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.VacancySDB;

@Dao
public interface VacancyDao {
    @Query("SELECT * FROM vacancy")
    List<VacancySDB> getAll();

    @Query("SELECT * FROM vacancy WHERE id = :id")
    VacancySDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<VacancySDB> itemList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(VacancySDB item);
}
