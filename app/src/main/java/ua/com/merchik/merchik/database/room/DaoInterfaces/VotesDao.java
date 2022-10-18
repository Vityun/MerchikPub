package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.VoteSDB;

@Dao
public interface VotesDao {
    @Query("SELECT * FROM votes")
    List<VoteSDB> getAll();

    @Query("SELECT * FROM votes WHERE serverId = :id")
    VoteSDB getById(int id);

    @Query("SELECT * FROM votes WHERE photo_id IN (:ids)")
    List<VoteSDB> getByIds(List<Integer> ids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<VoteSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAllCompletable(List<VoteSDB> data);

    // ---------------------------------------------------------

}
