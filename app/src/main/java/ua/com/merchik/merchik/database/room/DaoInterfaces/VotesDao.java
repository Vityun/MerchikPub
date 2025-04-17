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

    /**
     * 14.06.23.
     * Для Опции Контроля 135708
     * */
    @Query("SELECT * FROM votes WHERE :dtFrom < dt < :dtTo AND score <= :score AND code_dad2 = :codeDad2 ORDER BY score DESC")
    List<VoteSDB> getAll(long dtFrom, long dtTo, int score, long codeDad2);

    /**
     * 14.06.23.
     * Для Опции Контроля 135595
     * */
    @Query("SELECT * FROM votes WHERE :dtFrom < dt < :dtTo AND score <= :score AND code_dad2 = :codeDad2 AND kli = :client AND addr_id = :addr AND vote_class = :voteClass")
    List<VoteSDB> getAll(long dtFrom, long dtTo, int score, long codeDad2, String client, int addr, int voteClass);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAllCompletable(List<VoteSDB> data);

    // ---------------------------------------------------------


    @Query("SELECT * FROM votes WHERE dt_upload = 0")
    List<VoteSDB> getAllToUpload();

    /**
     * 14.04.25.
     * Для планограммы посещения
     * */
    @Query("""
                SELECT * FROM votes WHERE code_dad2 = :codeDad2 AND
                    vote_class = :voteClass AND
                    (:photo_id = 0 OR photo_id = :photo_id)
            ORDER BY dt DESC
                LIMIT 1
            """)
    VoteSDB getVote(
            long codeDad2,
            long photo_id,
            int voteClass
    );
//    @Query("""
//    SELECT * FROM votes WHERE (:codeDad2 IS NULL OR code_dad2 = :codeDad2) AND
//        (:client IS NULL OR kli = :client) AND
//        (:addr = 0 OR addr_id = :addr) AND
//        (:photo_id = 0 OR photo_id = :photo_id) AND
//        (:voteClass = 0 OR vote_class = :voteClass)
//    ORDER BY dt_year DESC, dt_month DESC, dt_day DESC, dt_upload DESC
//    LIMIT 1
//""")    VoteSDB getVote(long codeDad2, String client, int addr, long photo_id, int voteClass);

    @Query("""
    SELECT * FROM votes WHERE code_dad2 = :codeDad2 AND
        vote_class = :voteClass AND
        (:photo_id = 0 OR photo_id = :photo_id)

""")
    List<VoteSDB> getVoteAll(
            long codeDad2,
            long photo_id,
            int voteClass
    );
}
