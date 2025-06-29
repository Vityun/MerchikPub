package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogFilter.data.DFWpResult;


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
        Globals.writeToMLOG("INFO", "WpDataRealm/setWpData/", " List<WpDataDB> wpData.size: " + wpData.size());
        INSTANCE.beginTransaction();
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
     * 18.02.2025
     * Получение плана работ для апдейта
     */
    public static RealmResults<WpDataDB> getWpDataToUpdate() {
        return INSTANCE.where(WpDataDB.class)
                .equalTo("startUpdate", true)
                .findAll();
    }

    /**
     * 03.03.2021
     * Получение строки с Плана работ по её id
     */
    public static WpDataDB getWpDataRowById(long id) {
        return INSTANCE.where(WpDataDB.class)
                .equalTo("ID", id)
                .findFirst();
    }

    public static WpDataDB getWpDataRowByDad2Id(long dad2) {
        WpDataDB result = INSTANCE.where(WpDataDB.class)
                .equalTo("code_dad2", dad2)
                .findFirst();
        if (result != null )
            result = INSTANCE.copyFromRealm(result);
        return result;
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

        if (customerId != null && customerId != 0) {
            result = result.where()
                    .equalTo("client_id", String.valueOf(customerId))
                    .findAll();
        }

        if (userId != null && userId != 0) {
            result = result.where()
                    .equalTo("user_id", userId)
                    .findAll();
        }

        if (themeId != null && themeId != 0) {
            result = result.where()
                    .equalTo("theme_id", themeId)
                    .findAll();
        }

        return result;
    }


    public static List<WpDataDB> getWpDataBy(Date dateFrom, Date dateTo, Integer status, Integer addressId, String customerId, Integer userId) {
        RealmResults<WpDataDB> result = INSTANCE.where(WpDataDB.class)
                .findAll();

        if (dateFrom != null && dateTo != null) {
            result = result.where()
                    .between("dt", dateFrom, dateTo)
                    .findAll();
        }

        if (status != null) {
            result = result.where()
                    .equalTo("status", status)
                    .findAll();
        }

        if (addressId != null) {
            result = result.where()
                    .equalTo("addr_id", addressId)
                    .findAll();
        }

        if (customerId != null) {
            result = result.where()
                    .equalTo("client_id", customerId)
                    .findAll();
        }

        if (userId != null) {
            result = result.where()
                    .equalTo("user_id", userId)
                    .findAll();
        }

        return RealmManager.INSTANCE.copyFromRealm(result);
    }


    /**
     * 22.08.23.
     * Получаю список адресов из Плана работ.
     * <p>
     * На данный момент надо для того что б по этим адресам получать отфильтрованный ReportPrepare
     */
    public static List<Integer> getWpDataAddresses() {
        try {
            // Получение списка всех WpDataDB объектов
            RealmResults<WpDataDB> wpData = INSTANCE.where(WpDataDB.class).findAll();

            // Создание множества для хранения уникальных идентификаторов
            HashSet<Integer> uniqueAddrIds = new HashSet<>();

            // Перебор всех объектов и добавление их идентификаторов в множество
            for (WpDataDB wpDataItem : wpData) {
                uniqueAddrIds.add(wpDataItem.getAddr_id());
            }

            // Создание списка из множества уникальных идентификаторов
            return new ArrayList<>(uniqueAddrIds);
        } catch (Exception e) {
            return null;
        }

    }
}
