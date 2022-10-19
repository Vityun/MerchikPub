package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.ArticleSDB;

@Dao
public interface ArticleDao {
    @Query("SELECT * FROM article")
    List<ArticleSDB> getAll();

    @Query("SELECT * FROM article WHERE id = :id")
    ArticleSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ArticleSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAllCompletable(List<ArticleSDB> data);

    // ---------------------------------------------------------

    @Query("SELECT * FROM article WHERE tovar_id = :id")
    ArticleSDB getByTovId(int id);
}
