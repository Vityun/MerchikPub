package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;

@Dao
public interface ShowcaseDao {
    @Query("SELECT * FROM showcase")
    List<ShowcaseSDB> getAll();

    @Query("SELECT * FROM showcase WHERE ID = :id")
    ShowcaseSDB getById(int id);

    @Query("SELECT * FROM showcase WHERE client_id = :clientId AND addr_id = :addrId")
    List<ShowcaseSDB> getByDocTP(String clientId, int addrId);

    @Query("SELECT * FROM showcase WHERE client_id = :clientId AND addr_id = :addrId AND tp_id IN (:tpId) ")
    List<ShowcaseSDB> getByDocTP(String clientId, int addrId, List<Integer> tpId);

    @Query("SELECT * FROM showcase WHERE client_id = :clientId AND addr_id = :addrId")
    List<ShowcaseSDB> getByDoc(String clientId, int addrId);

    @Query("SELECT * FROM showcase WHERE client_id = :clientId AND addr_id = :addrId AND id IN (:shwAR)")
    List<ShowcaseSDB> getByDoc(String clientId, int addrId, List<Integer> shwAR);

//    @Query("SELECT * FROM showcase WHERE client_id = :clientId AND addr_id = :addrId")
//    List<ShowcaseSDB> getByDocAndDate(String clientId, int addrId, );

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<ShowcaseSDB> data);

    // ----------------------------------------------------
    @Query("SELECT photo_id FROM showcase")
    List<Integer> getAllPhotosIds();

    @Query("SELECT * FROM showcase WHERE photo_id IN (:photoIds)")
    List<ShowcaseSDB> getAllByPhotosIds(List<Integer> photoIds);

}
