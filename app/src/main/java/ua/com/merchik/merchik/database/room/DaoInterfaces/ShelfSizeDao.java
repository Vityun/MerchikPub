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

    class HistoryMeasurement {
        public String sourceIds;
        public String sourceNetworkIds;
        public int groupId;
        public Integer addrId;
        public String addressName;
        public String clientId;
        public String clientName;
        public Integer authorId;
        public String authorName;
        public Float width;
        public Float planValue;
        public long dt;
        public String dtDay;
        public Long dtChange;
    }

    // A zero-network row is a fallback for the same measurement, not another vote in the mean.
    // The physical ID and network variant are not part of the measurement's identity.
    // An explicit server network must not depend on the local address directory's coverage.
    String SHELF_LENGTH_HISTORY_QUERY = "SELECT h.*, a.nm AS addressName, c.nm AS clientName, u.fio AS authorName FROM (" +
            "SELECT GROUP_CONCAT(s.id) AS sourceIds, GROUP_CONCAT(DISTINCT s.gruadr_id) AS sourceNetworkIds, " +
            "s.grp_id AS groupId, s.addr_id AS addrId, s.client_id AS clientId, s.author_id AS authorId, " +
            "s.width, s.planzn AS planValue, s.dt, s.dt_day AS dtDay, s.dt_change AS dtChange " +
            "FROM shelf_size s LEFT JOIN address a ON a.id = s.addr_id " +
            "WHERE (s.gruadr_id = :networkId OR (s.gruadr_id = 0 AND a.tp_id = :networkId)) " +
            "AND s.grp_id IN (:groupIds) " +
            "AND s.dt > 0 AND s.dt < :endExclusive " +
            "AND (s.author_id IS NULL OR s.author_id NOT IN (:executorId, :robotId)) " +
            "GROUP BY s.grp_id, s.addr_id, s.client_id, s.width, s.planzn, s.author_id, s.dt, s.dt_day, s.dt_change" +
            ") h LEFT JOIN address a ON a.id = h.addrId " +
            "LEFT JOIN client c ON c.id = h.clientId LEFT JOIN sotr u ON u.id = h.authorId " +
            "ORDER BY h.groupId, h.dt, h.sourceIds";

    @Query(SHELF_LENGTH_HISTORY_QUERY)
    List<HistoryMeasurement> getHistoryForShelfLength(int networkId, List<Integer> groupIds, long endExclusive,
                                                    int executorId, int robotId);
}
