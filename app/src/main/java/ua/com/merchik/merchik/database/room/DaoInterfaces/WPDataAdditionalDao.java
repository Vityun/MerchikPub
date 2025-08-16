package ua.com.merchik.merchik.database.room.DaoInterfaces;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import ua.com.merchik.merchik.data.Database.Room.WPDataAdditional;
import ua.com.merchik.merchik.data.UploadToServ.WPDataAdditionalServ;


@Dao
public interface WPDataAdditionalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<WPDataAdditional> items);


    @Query("SELECT * FROM wp_data_additional")
    List<WPDataAdditional> getAll();

    @Query("SELECT * FROM wp_data_additional WHERE uploadStatus = 1")
    List<WPDataAdditional> getUploadToServer();

    @Query("UPDATE wp_data_additional SET uploadStatus = 0 WHERE ID IN (:ids)")
    Completable markUploadedByIds(List<Long> ids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(WPDataAdditional item);

    @Query("SELECT * FROM wp_data_additional WHERE code_dad2 = :codeDad2")
    Single<List<WPDataAdditional>> getByCodeDad2(long codeDad2);

    @Query("SELECT * FROM wp_data_additional WHERE client_id = 0 AND addr_id = :addrId")
    Single<List<WPDataAdditional>> getByAddr(int addrId);


    @Query("SELECT * FROM wp_data_additional WHERE client_id = :clientId AND addr_id = :addrId")
    Single<List<WPDataAdditional>> getByClientAndAddr(int clientId, int addrId);


}
