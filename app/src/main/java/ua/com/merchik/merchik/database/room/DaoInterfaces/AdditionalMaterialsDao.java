package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsJOIN.AdditionalMaterialsJOINAdditionalMaterialsAddressSDB;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsSDB;

@Dao
public interface AdditionalMaterialsDao {
    @Query("SELECT * FROM additional_materials")
    List<AdditionalMaterialsSDB> getAll();

    @Query("SELECT * FROM additional_materials WHERE id = :id")
    AdditionalMaterialsSDB getById(int id);

    @Query("SELECT * FROM additional_materials WHERE client = :clientId")
    List<AdditionalMaterialsSDB> getAllByClientId(String clientId);

    @Query("SELECT * FROM additional_materials WHERE client = :clientId AND approve = :approve AND state = :state AND expire >= :expire")
    List<AdditionalMaterialsSDB> getAllForOption(String clientId, String approve, String state, String expire);
    // approve, state, expire,

    // TEST
    @Query("SELECT * FROM additional_materials " +
            "INNER JOIN additional_materials_address ON additional_materials.id = additional_materials_address.file_id " +
            "WHERE client = :clientId AND approve = :approve AND state = :state")
    List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> getAllForOptionTEST(String clientId, String approve, String state);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AdditionalMaterialsSDB> data);
}
