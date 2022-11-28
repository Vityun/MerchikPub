package ua.com.merchik.merchik.database.realm.tables;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dialogs.DialogFilter.data.DFWpResult;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;


/**
 * 03.03.2021 (новый день, новые эксперементы)
 * Класс в котором описываются запросы к Плану
 */
public class WpDataRealm {


    public enum UserPostRes {
        EMPTY, MANAGER, SUBORDINATE
    }


    /**
     * 03.03.2021
     * Переписанное сохранение плана работ
     */
    public static void setWpData(List<WpDataDB> wpData) {
        INSTANCE.beginTransaction();
//        INSTANCE.delete(WpDataDB.class);
        INSTANCE.copyToRealmOrUpdate(wpData);
        INSTANCE.commitTransaction();
    }

    /**
     * 03.03.2021
     * Получение Всего плана работ
     */
    public static RealmResults<WpDataDB> getWpData() {
        return INSTANCE.where(WpDataDB.class).findAll();
    }

    /**
     * 03.03.2021
     * Получение строки с Плана работ по её id
     */
    public static WpDataDB getWpDataRowById(int id) {
        return INSTANCE.where(WpDataDB.class)
                .equalTo("ID", id)
                .findFirst();
    }

    public static WpDataDB getWpDataRowByDad2Id(long dad2) {
        return INSTANCE.where(WpDataDB.class)
                .equalTo("code_dad2", dad2)
                .findFirst();
    }

    public static List<WpDataDB> getWpDataRowByIds(Long[] id) {
        return INSTANCE.where(WpDataDB.class)
                .in("ID", id)
                .findAll();
    }

    /**
     * 03.03.2021
     * Получение инфы - руководитель это или нет
     */
    public static UserPostRes userPost(int userId) {
        try {
            List<WpDataDB> list = getWpData();
            Log.e("userPost", "WP DATA SIZE: " + list.size());
            if (list != null) {
                // Данные есть, нужно разобраться - руководитель это или пользователь.

                List<WpDataDB> result = new ArrayList<>();
                for (WpDataDB item : list) {
                    if (item.getSuper_id() == userId || item.getTerritorial_id() == userId ||
                            item.getRegional_id() == userId || item.getNop_id() == userId) {
                        result.add(item);
                    }
                }


                Log.e("userPost", "result.size(): " + result.size());

                if (result.size() > 1) {
                    return UserPostRes.MANAGER;
                } else {
                    return UserPostRes.SUBORDINATE;
                }

            } else {
                // Данных нет вообще - надо обновитсья.
                return UserPostRes.EMPTY;
            }
        } catch (Exception e) {
            return UserPostRes.EMPTY;
        }

    }


    /**
     * 05.03.2021
     * Получение списка ИЗА
     */
    public static List<String> getIZAList() {
        List<String> res = new ArrayList<>();
        try {
            List<WpDataDB> list = getWpData();
            for (WpDataDB item : list) {
                res.add(item.getCode_iza());
            }
        } catch (Exception e) {
            return null;    // todo обработать нормально
        }

        Log.e("getIZAList", "res: " + res);
        return res;
    }


    /**
     * 29.03.2021
     * Получение всех данных обновлённых ПОСЛЕ обмена
     */
    public static RealmResults<WpDataDB> getAppUpdatedWpData(long vpi) {
        return INSTANCE.where(WpDataDB.class)
                .greaterThan("dt_update", vpi)
                .findAll();
    }


    /**
     * 18.08.2021
     * Фильтр по плану работ
     */
    public static RealmResults<WpDataDB> wpFilterQuery(DFWpResult wp) {

        RealmResults<WpDataDB> result;

        if (wp.status == null) {
            result = INSTANCE.where(WpDataDB.class)
                    .sort("dt_start", Sort.ASCENDING)
                    .findAll();
        } else {
            result = INSTANCE.where(WpDataDB.class)
                    .equalTo("status", wp.status)
                    .sort("dt_start", Sort.ASCENDING)
                    .findAll();
        }


        return result;
    }


    /**
     * запрос к БД из Фильтра
     */
    public static RealmResults<WpDataDB> wpFiltered(Integer addressId, Integer customerId, Integer userId, Integer themeId) {
        RealmResults<WpDataDB> result = INSTANCE.where(WpDataDB.class)
                .findAll();

        if (addressId != null && addressId != 0) {
            result = result.where()
                    .equalTo("addr_id", addressId)
                    .findAll();
        }

        if (customerId != null && customerId != 0){
            result = result.where()
                    .equalTo("client_id", String.valueOf(customerId))
                    .findAll();
        }

        if (userId != null && userId != 0){
            result = result.where()
                    .equalTo("user_id", userId)
                    .findAll();
        }

        if (themeId != null && themeId != 0){
            result = result.where()
                    .equalTo("theme_id", themeId)
                    .findAll();
        }

        return result;
    }

}
