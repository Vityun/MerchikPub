package ua.com.merchik.merchik.database.room.DaoInterfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB;

@Dao
public interface AchievementsDao {
    @Query("SELECT * FROM achievements")
    List<AchievementsSDB> getAll();

    @Query("SELECT * FROM achievements WHERE id = :id")
    AchievementsSDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AchievementsSDB> data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAllCompletable(List<AchievementsSDB> data);

    // ---------------------------------------------------------

    @Query("SELECT * FROM achievements WHERE code_dad2 = :dad2")
    List<AchievementsSDB> getByDad2(long dad2);

    /*Получение Достижений для Опции контроля Достижений*/
    @Query("SELECT * FROM achievements WHERE theme_id = :themeId AND client_id = :clientId AND addr_id = :addressId AND (dt_ut IS NOT NULL AND dt_ut BETWEEN :dtFrom AND :dtTo) ORDER BY dt_ut DESC")
    List<AchievementsSDB> getForOptionControl(Long dtFrom, Long dtTo, String clientId, Integer addressId, Integer themeId);

    @Query("SELECT * FROM achievements WHERE user_id = :userId AND (dt_ut IS NOT NULL AND dt_ut BETWEEN :dtFrom AND :dtTo) ORDER BY dt_ut DESC")
    List<AchievementsSDB> getList(Long dtFrom, Long dtTo, Integer userId);


    /**
     * Віктор
     * 12.01.24.
     * Это попытка - эксперемент собрать универсальный запрос в котором будут учитываться только те реквизиты,
     * которые не равны пустоте (NULL).
     * Реквизиты без этого учёта (обязательные):
     * @param dtFrom    Дата С;
     * @param dtTo      Дата По
     * @param clientId  Клиент
     * @param addressId Адрес
     *
     * Реквизиты НЕ обязательные, что б они не учитывались нужно их передавать как null
     * @param themeId   Тема Достижения.
     * */
    @Query("SELECT * FROM achievements " +
            "WHERE client_id = :clientId " +
            "AND addr_id = :addressId " +
            "AND (dt_ut IS NOT NULL AND dt_ut BETWEEN :dtFrom AND :dtTo) " +
            "AND (:themeId IS NULL OR theme_id = :themeId) " +  // Не обязательный реквизит Тема, мы берём для запроса темы только со значениями
            "AND theme_id IS NOT NULL " +   // Если у темы в БД тоже нет значения - мы не учитываем её и не берём такие данные
            "ORDER BY dt_ut DESC")
    List<AchievementsSDB> getAchievementsList(Long dtFrom, Long dtTo, String clientId, Integer addressId, Integer themeId);


    @Query("SELECT * FROM achievements WHERE serverId = 0")
    List<AchievementsSDB> getAllToDownload();
}
