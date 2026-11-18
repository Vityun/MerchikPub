package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatSDB;
import ua.com.merchik.merchik.data.synchronization.ChatReadPolicy;

@Dao
public interface ChatDao {
    @Query("SELECT * FROM chat GROUP BY chat_id ORDER BY dt DESC")
    Single<List<ChatSDB>> getAll();

    @Query("SELECT * FROM chat WHERE chat_id = :id ORDER BY dt DESC")
    Single<List<ChatSDB>> getAllById(int id);

    @Query("SELECT * FROM chat WHERE chat_id = :id ORDER BY dt DESC, id DESC")
    Flowable<List<ChatSDB>> observeConversation(int id);

    @Query("SELECT * FROM chat WHERE id IN (:ids)")
    List<ChatSDB> getByIds(List<Integer> ids);

    @Query("UPDATE chat SET dt_read = :readAt WHERE id = :messageId AND chat_id = :chatId " +
            "AND (dt_read IS NULL OR dt_read <= 0)")
    int markRead(int chatId, int messageId, long readAt);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<ChatSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ChatSDB> data);

    // A replayed server row must not turn an already viewed message into unread.
    @Transaction
    default void insertDownloaded(List<ChatSDB> data) {
        for (int start = 0; start < data.size(); start += 500) {
            List<ChatSDB> batch = data.subList(start, Math.min(start + 500, data.size()));
            List<Integer> ids = new ArrayList<>();
            for (ChatSDB row : batch) ids.add(row.id);
            Map<Integer, Long> readTimes = new HashMap<>();
            for (ChatSDB local : getByIds(ids)) readTimes.put(local.id, local.dtRead);
            for (ChatSDB row : batch) {
                row.dtRead = ChatReadPolicy.mergeReadTime(readTimes.get(row.id), row.dtRead);
                readTimes.put(row.id, row.dtRead);
            }
            insertAll(batch);
        }
    }
}
