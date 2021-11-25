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

//    @Query("SELECT * FROM tasks_and_reclamations WHERE tp = :tp")
//    List<TasksAndReclamationsSDB> getAllByTp(int tp);

    @Query("SELECT tar.*, addr.nm AS addr_nm, cl.nm AS client_nm, sot.fio AS sotr_nm FROM tasks_and_reclamations tar \n" +
            "LEFT JOIN address addr ON tar.addr = addr.id\n" +
            "LEFT JOIN client cl ON tar.client = cl.id\n" +
            "LEFT JOIN sotr sot ON tar.vinovnik = sot.id\n" +
            "WHERE tp = :tp AND dt >:dt\n" +
            "ORDER BY dt DESC;")
    List<TasksAndReclamationsSDB> getAllByTp(int tp, long dt);


    @Query("SELECT tar.*, addr.nm AS addr_nm, cl.nm AS client_nm, sot.fio AS sotr_nm FROM tasks_and_reclamations tar \n" +
            "LEFT JOIN address addr ON tar.addr = addr.id\n" +
            "LEFT JOIN client cl ON tar.client = cl.id\n" +
            "LEFT JOIN sotr sot ON tar.vinovnik = sot.id\n" +
            "WHERE tp = :tp AND dt BETWEEN :dtFrom AND :dtTo AND (:theme IS NULL OR theme_id = :theme) AND (:status IS NULL OR state = :status)\n" +
            "ORDER BY dt DESC;")
    List<TasksAndReclamationsSDB> getTaRBy(int tp, Long dtFrom, Long dtTo, Integer theme, Integer status);

    @Query("SELECT tar.*, addr.nm AS addr_nm, cl.nm AS client_nm, sot.fio AS sotr_nm FROM tasks_and_reclamations tar \n" +
            "LEFT JOIN address addr ON tar.addr = addr.id\n" +
            "LEFT JOIN client cl ON tar.client = cl.id\n" +
            "LEFT JOIN sotr sot ON tar.vinovnik = sot.id\n" +
            "WHERE tp = :tp AND addr = :address AND client = :client AND dt >= :dt\n" +
            "ORDER BY dt DESC;")
    List<TasksAndReclamationsSDB> getAllByInfo(int tp, String client, int address, long dt);


    @Query("SELECT * FROM tasks_and_reclamations WHERE tp = :tp")
    Flowable<List<TasksAndReclamationsSDB>> getAllByTpRx(int tp);

    @Query("SELECT * FROM tasks_and_reclamations WHERE id = :id")
    TasksAndReclamationsSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertData(List<TasksAndReclamationsSDB> data);

}

/*SELECT tar.*, addr.nm FROM tasks_and_reclamations tar
LEFT JOIN address addr ON tar.addr = addr.id
LEFT JOIN client cl ON tar.client = cl.nm
LEFT JOIN sotr sot ON tar.vinovnik = sot.id
WHERE tp = :tp
ORDER BY dt;*/
