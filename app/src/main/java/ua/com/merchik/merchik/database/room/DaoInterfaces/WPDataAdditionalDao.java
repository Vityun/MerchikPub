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

//    dt хранится в секундах (а не в миллисекундах), то разница 3 дня — это:
//    3 дня — это 3 * 24 * 60 * 60 = 259200 секунд
//    1 ltym — это 24 * 60 * 60 = 86400 секунд
@Query("SELECT * FROM wp_data_additional WHERE confirm_decision = 0 AND ID > 0 AND dt > strftime('%s','now') - 259200")
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

    @Query("SELECT `action` FROM wp_data_additional WHERE code_dad2 = :dad2 ORDER BY dt DESC LIMIT 1")
    Integer getLastActionByDad2(long dad2);

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

    @Transaction
    default void applyServerResultSync(long serverId, boolean accepted, String comment, long nowSec) {
        WPDataAdditional item = getByIdSync(serverId);
        if (item == null) return;

        // 1 = APPROVED, 2 = DECLINED (как у тебя уже используется)
        item.action = accepted ? 1 : 2;

        item.confirmDecision = item.action;

        item.comment = comment;

        // dt в секундах
        item.dt = nowSec;

        insertSync(item);
    }

    @Query("SELECT comment FROM wp_data_additional WHERE code_dad2 = :dad2 ORDER BY dt DESC LIMIT 1")
    String getLastCommentByDad2Sync(long dad2);


}
