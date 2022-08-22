package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsAddressSDB;

@Dao
public interface AdditionalMaterialsAddressDao {
    @Query("SELECT * FROM additional_materials_address")
    List<AdditionalMaterialsAddressSDB> getAll();

    @Query("SELECT * FROM additional_materials_address WHERE id = :id")
    AdditionalMaterialsAddressSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AdditionalMaterialsAddressSDB> data);
}
