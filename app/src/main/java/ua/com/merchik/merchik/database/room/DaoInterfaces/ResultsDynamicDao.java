package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.ResultsDynamicSDB;

@Dao
public interface ResultsDynamicDao {
    String WITH_ADDRESS_QUERY = "SELECT r.*, a.tp_id AS tpId, a.tt_id AS ttId, a.kol_kass AS kolKass "
            + "FROM plan_results_dynamic r LEFT JOIN address a ON a.id = r.addr_id "
            + "WHERE r.yearmonth = :yearmonth ORDER BY r.client_id, r.addr_id";
    String FACE_COUNT_QUERY = "SELECT r.* FROM plan_results_dynamic r JOIN address a ON a.id = r.addr_id "
            + "WHERE r.yearmonth BETWEEN :monthFrom AND :monthTo "
            + "AND r.client_id = :clientId AND a.tp_id = :networkId "
            + "ORDER BY r.yearmonth, r.addr_id";

    @Query("SELECT * FROM plan_results_dynamic ORDER BY yearmonth, client_id, addr_id")
    List<ResultsDynamicSDB> getAll();

    @Query("SELECT * FROM plan_results_dynamic WHERE yearmonth = :yearmonth "
            + "AND client_id = :clientId AND addr_id = :addrId LIMIT 1")
    ResultsDynamicSDB getByKey(String yearmonth, String clientId, String addrId);

    @Query(WITH_ADDRESS_QUERY)
    List<WithAddress> getWithAddress(String yearmonth);

    @Query(FACE_COUNT_QUERY)
    List<ResultsDynamicSDB> getForFaceCount(String monthFrom, String monthTo, String clientId, int networkId);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertAll(List<ResultsDynamicSDB> rows);

    @Query("DELETE FROM plan_results_dynamic")
    void clear();

    @Transaction
    default void replaceAll(List<ResultsDynamicSDB> rows) {
        if (rows == null) throw new IllegalArgumentException("Snapshot must not be null");
        clear();
        insertAll(rows);
    }

    class WithAddress {
        @Embedded
        public ResultsDynamicSDB result;
        public Integer tpId;
        public Integer ttId;
        public Integer kolKass;
    }
}
