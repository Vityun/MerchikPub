package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import ua.com.merchik.merchik.data.Database.Room.ChatGrpSDB;

@Dao
public interface ChatGrpDao {
    @Query("SELECT * FROM chat_grp ORDER BY dt_last_update DESC")
    Single<List<ChatGrpSDB>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Completable insertData(List<ChatGrpSDB> data);
}
