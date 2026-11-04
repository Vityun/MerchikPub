package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.ShelfSizeSDB;

@Dao
public interface ShelfSizeDao {
    @Query("SELECT * FROM shelf_size")
    List<ShelfSizeSDB> getAll();

    @Query("SELECT * FROM shelf_size WHERE id = :id")
    ShelfSizeSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<ShelfSizeSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ShelfSizeSDB> data);

    @Query("SELECT COUNT(*) FROM shelf_size")
    long count();

    // ----------------------------------------------------

    @Query("SELECT * FROM shelf_size WHERE client_id = :clientId AND addr_id = :addressId ORDER BY dt DESC")
    List<ShelfSizeSDB> getBy(String clientId, Integer addressId);

    class WidthCount {
        public int groupId;
        public Float width;
        public long measurements;
    }

    // A zero-network row is a fallback for the same measurement, not another vote in the mean.
    // The physical ID and network variant are not part of the measurement's identity.
    @Query("SELECT grp_id AS groupId, width, COUNT(*) AS measurements FROM (" +
            "SELECT DISTINCT s.grp_id, s.addr_id, s.client_id, s.width, s.planzn, s.author_id, s.dt, s.dt_day, s.dt_change " +
            "FROM shelf_size s JOIN address a ON a.id = s.addr_id " +
            "WHERE a.tp_id = :networkId AND s.gruadr_id IN (:networkId, 0) AND s.grp_id IN (:groupIds) " +
            "AND s.dt > 0 AND s.dt < :endExclusive " +
            "AND (s.author_id IS NULL OR s.author_id NOT IN (:executorId, :robotId))" +
            ") GROUP BY grp_id, width")
    List<WidthCount> getHistoryForShelfLength(int networkId, List<Integer> groupIds, long endExclusive,
                                              int executorId, int robotId);
}
