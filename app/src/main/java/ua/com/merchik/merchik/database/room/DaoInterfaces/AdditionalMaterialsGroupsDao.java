package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsGroupsSDB;

@Dao
public interface AdditionalMaterialsGroupsDao {
    @Query("SELECT * FROM additional_materials_groups")
    List<AdditionalMaterialsGroupsSDB> getAll();

    @Query("SELECT * FROM additional_materials_groups WHERE id = :id")
    AdditionalMaterialsGroupsSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AdditionalMaterialsGroupsSDB> data);
}
