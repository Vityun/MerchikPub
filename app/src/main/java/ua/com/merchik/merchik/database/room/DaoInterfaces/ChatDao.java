package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatSDB;

@Dao
public interface ChatDao {
    @Query("SELECT * FROM chat GROUP BY chat_id ORDER BY dt DESC")
    Single<List<ChatSDB>> getAll();

    @Query("SELECT * FROM chat WHERE chat_id = :id ORDER BY dt DESC")
    Single<List<ChatSDB>> getAllById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<ChatSDB> data);
}
