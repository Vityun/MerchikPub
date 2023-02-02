package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;

// Задачи и рекламации
@Dao
public interface TarDao {
    @Query("SELECT * FROM tasks_and_reclamations")
    List<TasksAndReclamationsSDB> getAll();

//    @Query("SELECT * FROM tasks_and_reclamations")
    @Query("SELECT tar.*, addr.location_xd AS coord_X , addr.location_yd AS coord_Y FROM tasks_and_reclamations tar LEFT JOIN address addr ON tar.addr = addr.id GROUP BY tar.addr ORDER BY dt ;")
    List<TasksAndReclamationsSDB> getAllJoinAddressSDB();

//    @Query("SELECT * FROM tasks_and_reclamations WHERE tp = :tp")
//    List<TasksAndReclamationsSDB> getAllByTp(int tp);

    @Query("SELECT tar.*, addr.nm AS addr_nm, cl.nm AS client_nm, sot.fio AS sotr_nm FROM tasks_and_reclamations tar \n" +
            "LEFT JOIN address addr ON tar.addr = addr.id\n" +
            "LEFT JOIN client cl ON tar.client = cl.id\n" +
            "LEFT JOIN sotr sot ON tar.vinovnik = sot.id\n" +
            "WHERE tp = :tp AND dt >:dt AND (vinovnik = :userId OR vinovnik2 = :userId OR zamena_user_id = :userId)\n" +
            "ORDER BY dt DESC;")
    List<TasksAndReclamationsSDB> getAllByTp(int userId, int tp, long dt);


    @Query("SELECT tar.*, addr.nm AS addr_nm, cl.nm AS client_nm, sot.fio AS sotr_nm FROM tasks_and_reclamations tar \n" +
            "LEFT JOIN address addr ON tar.addr = addr.id\n" +
            "LEFT JOIN client cl ON tar.client = cl.id\n" +
            "LEFT JOIN sotr sot ON tar.vinovnik = sot.id\n" +
            "WHERE tp = :tp AND dt BETWEEN :dtFrom AND :dtTo AND (:theme IS NULL OR theme_id = :theme) AND (:status IS NULL OR state = :status)\n" +
            "ORDER BY dt DESC;")
    List<TasksAndReclamationsSDB> getTaRBy(int tp, Long dtFrom, Long dtTo, Integer theme, Integer status);
//    AND (:theme IS NULL OR theme_id = :theme) AND (:status IS NULL OR state = :status)

    @Query("SELECT tar.*, addr.nm AS addr_nm, cl.nm AS client_nm, sot.fio AS sotr_nm FROM tasks_and_reclamations tar \n" +
            "LEFT JOIN address addr ON tar.addr = addr.id\n" +
            "LEFT JOIN client cl ON tar.client = cl.id\n" +
            "LEFT JOIN sotr sot ON tar.vinovnik = sot.id\n" +
            "WHERE tp = :tp AND addr = :address AND client = :client AND dt >= :dt\n" +
            "ORDER BY dt DESC;")
    List<TasksAndReclamationsSDB> getAllByInfo(int tp, String client, int address, long dt);

    @Query("SELECT tar.*, addr.nm AS addr_nm, cl.nm AS client_nm, sot.fio AS sotr_nm FROM tasks_and_reclamations tar \n" +
            "LEFT JOIN address addr ON tar.addr = addr.id\n" +
            "LEFT JOIN client cl ON tar.client = cl.id\n" +
            "LEFT JOIN sotr sot ON tar.vinovnik = sot.id\n" +
            "WHERE tp = :tp AND state = :state AND addr = :address AND client = :client AND dt >= :dt\n" +
            "ORDER BY dt DESC;")
    List<TasksAndReclamationsSDB> getAllByInfo(int tp, String client, int address, long dt, int state);


    @Query("SELECT tar.*, addr.nm AS addr_nm FROM tasks_and_reclamations tar LEFT JOIN address addr ON tar.addr = addr.id WHERE state = :state AND addr = :address ORDER BY client")
    List<TasksAndReclamationsSDB> getAllByInfo(int state, int address);


    /**
     * 0 - выгружен
     * 1 - надо выгрузить
     * NULL - получено с сервера
     * */
    @Query("SELECT * FROM tasks_and_reclamations WHERE uploadStatus = :uploadStatus")
    List<TasksAndReclamationsSDB> getByUploadStatus(Integer uploadStatus);

    @Query("SELECT * FROM tasks_and_reclamations WHERE uploadStatus = 1 AND vote_score > 0 AND vinovnik_score > 0")
    TasksAndReclamationsSDB getByUploadStatusVotes();

    @Query("SELECT * FROM tasks_and_reclamations WHERE tp = :tp")
    Flowable<List<TasksAndReclamationsSDB>> getAllByTpRx(int tp);

    @Query("SELECT * FROM tasks_and_reclamations WHERE id = :id")
    TasksAndReclamationsSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertData(List<TasksAndReclamationsSDB> data);

    /**
     * Для опции контроля Задач 135329(
     * */
    @Query("SELECT * FROM tasks_and_reclamations WHERE tp = :tarType AND addr = :addressId AND (vinovnik = :userId OR vinovnik2 = :userId OR zamena_user_id = :userId) AND state = :state AND dt BETWEEN :dtFrom AND :dtTo")
    List<TasksAndReclamationsSDB> getTARForOptionControl(Integer tarType, int addressId, int userId, int state, Long dtFrom, Long dtTo);

    @Query("SELECT * FROM tasks_and_reclamations WHERE (vinovnik = :userId OR vinovnik2 = :userId OR zamena_user_id = :userId) AND tp = :tarType AND addr = :addressId AND client = :clientId  AND state = :state AND dt BETWEEN :dtFrom AND :dtTo")
//    @Query("SELECT * FROM tasks_and_reclamations WHERE tp = :tarType AND addr = :addressId AND client = :clientId AND (vinovnik = :userId OR vinovnik2 = :userId OR zamena_user_id = :userId) AND state = :state AND dt BETWEEN :dtFrom AND :dtTo")
    List<TasksAndReclamationsSDB> getTARForOptionControl150822(Integer tarType, int addressId, String clientId, int userId, int state, Long dtFrom, Long dtTo);

    @Query("SELECT * FROM tasks_and_reclamations WHERE tp = :tarType AND dt BETWEEN :dtFrom AND :dtTo")
    List<TasksAndReclamationsSDB> getTARForOptionControl135061(Integer tarType, Long dtFrom, Long dtTo);
}

/*SELECT tar.*, addr.nm FROM tasks_and_reclamations tar
LEFT JOIN address addr ON tar.addr = addr.id
LEFT JOIN client cl ON tar.client = cl.nm
LEFT JOIN sotr sot ON tar.vinovnik = sot.id
WHERE tp = :tp
ORDER BY dt;*/
