package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.DossierSotrSDB;

@Dao
public interface DossierSotrDao {
    @Query("SELECT * FROM dossier_sotr")
    List<DossierSotrSDB> getAll();

    @Query("SELECT * FROM dossier_sotr WHERE id = :id")
    DossierSotrSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DossierSotrSDB> itemList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DossierSotrSDB item);

    @Query("SELECT * FROM dossier_sotr " +
            "WHERE (:id IS NULL OR id = :id) " +
            "AND (:themeId IS NULL OR theme_id = :themeId) " +
            "AND (:codeIZA IS NULL OR (exam_id = '103693' || SUBSTR(:codeIZA,7, 16)) OR (exam_id = :codeIZA)) " + // в exam_id код ИЗА с бека
            "ORDER BY priznak DESC")
    List<DossierSotrSDB> getData(Long id, Long themeId, String codeIZA);
}
