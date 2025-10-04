package ua.com.merchik.merchik.database.room.DaoInterfaces;


import android.util.Pair;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
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

    @Query("SELECT * FROM wp_data_additional WHERE confirm_decision = 0")
    List<WPDataAdditional> getNotConfirmDecision();

    @Query("SELECT * FROM wp_data_additional WHERE ID IN (:ids)")
    List<WPDataAdditional> getByIds(List<Long> ids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(WPDataAdditional item);

    @Query("SELECT * FROM wp_data_additional WHERE code_dad2 = :codeDad2")
    Single<List<WPDataAdditional>> getByCodeDad2(long codeDad2);

    @Query("SELECT * FROM wp_data_additional WHERE client_id = 0 AND addr_id = :addrId")
    Single<List<WPDataAdditional>> getByAddr(int addrId);


    @Query("SELECT * FROM wp_data_additional WHERE client_id = :clientId AND addr_id = :addrId")
    Single<List<WPDataAdditional>> getByClientAndAddr(int clientId, int addrId);

    @Query("SELECT * FROM wp_data_additional WHERE ID = :id LIMIT 1")
    WPDataAdditional getByIdSync(long id); // вызывается внутри транзакции / в bg-потоке

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSync(WPDataAdditional item);

    @Query("DELETE FROM wp_data_additional WHERE ID = :id")
    void deleteByIdSync(long id);

    // синхронная транзакционная операция — возвращает void (не Completable)
    @Transaction
    default void replaceLocalIdWithServerIdSync(long localId, long serverId) {
        WPDataAdditional item = getByIdSync(localId);
        if (item == null) return;

        // если serverId совпадает с localId — можно просто пометить загруженным
        if (localId == serverId) {
            item.uploadStatus = 0;
            insertSync(item);
            return;
        }

        // присваиваем новый ID (serverId) и помечаем загруженным
        item.ID = serverId;
        item.uploadStatus = 0;

        insertSync(item);       // вставит/перезапишет запись с новым ID (REPLACE)
        deleteByIdSync(localId); // удаляем старую
    }

    // массовая синхронная транзакционная версия (не обязательна, можно делать снаружи)
    @Transaction
    default void replaceLocalIdsWithServerIdsSync(List<Pair<Long, Long>> mapping) {
        if (mapping == null || mapping.isEmpty()) return;
        for (Pair<Long, Long> p : mapping) {
            replaceLocalIdWithServerIdSync(p.first, p.second);
        }
    }

}
