package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import android.util.Log;

import java.util.List;

import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;

public class LogMPRealm {

    public static void setLogMP(List<LogMPDB> list) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(list);
        INSTANCE.commitTransaction();
    }

    public static List<LogMPDB> getAllLogMP() {
        return INSTANCE.copyFromRealm(INSTANCE.where(LogMPDB.class)
                .findAll());
    }

    public static long getLogMPCount() {
        return INSTANCE.where(LogMPDB.class)
                .count();
    }

    public static List<LogMPDB> getLogMPByDad2(long codeDad2) {
        return INSTANCE.copyFromRealm(INSTANCE.where(LogMPDB.class)
                .equalTo("codeDad2", codeDad2)
                .findAll());
    }

    public static List<LogMPDB> getLogMPByDad2Distance(long codeDad2, int distance) {
        return INSTANCE.copyFromRealm(INSTANCE.where(LogMPDB.class)
                .equalTo("codeDad2", codeDad2)
                .between("distance", 1, distance)
                .findAll());
    }

    public static List<LogMPDB> getLogMPByDtAndDistance(Long dtFrom, Long dtTo, Integer distance) {
        return INSTANCE.copyFromRealm(INSTANCE.where(LogMPDB.class)
                .between("CoordTime", dtFrom, dtTo)
//                .between("distance", 1, distance)
                .findAll());
    }

    public static List<LogMPDB> getLogMPTime(long startTime, long endTime) {
        startTime = normalizeToMillis(startTime);
        endTime = normalizeToMillis(endTime);

        RealmQuery<LogMPDB> query = INSTANCE.where(LogMPDB.class);
        query = query.greaterThanOrEqualTo("CoordTime", startTime);
        query = query.and().lessThanOrEqualTo("CoordTime", endTime);

        RealmResults<LogMPDB> results = query
                .sort("CoordTime", Sort.DESCENDING)
                .notEqualTo("CoordX", 0d)
                .notEqualTo("CoordY", 0d)
                .findAll();

        return INSTANCE.copyFromRealm(results);
    }

    /**
     * CoordTime в LogMPDB хранится в МИЛЛИСЕКУНДАХ, не тупить в будущем с этим, передавать В МИЛЛИСЕКУНДАХ!!
     * */
    public static List<LogMPDB> getLogMPTimeDad2(long startTime, long endTime, long codeDad2) {
        RealmQuery<LogMPDB> query = INSTANCE.where(LogMPDB.class);
        query = query.greaterThanOrEqualTo("CoordTime", startTime);

        RealmResults<LogMPDB> res1 = query.findAll();
        Log.e("getLogMPTimeDad2", "res1: " + res1.size());

        query = query.lessThan("CoordTime", endTime);

        RealmResults<LogMPDB> res2 = query.findAll();
        Log.e("getLogMPTimeDad2", "res2: " + res2.size());


        RealmResults<LogMPDB> res3 = query.equalTo("codeDad2", codeDad2).findAll();
        Log.e("getLogMPTimeDad2", "res3: " + res3.size());

        RealmResults<LogMPDB> results = query
                .sort("CoordTime", Sort.DESCENDING) // Сортировка по убыванию
                .equalTo("codeDad2", codeDad2)
                .notEqualTo("CoordX", 0d)
                .notEqualTo("CoordY", 0d)
                .findAll();
        return INSTANCE.copyFromRealm(results);
    }

    public static LogMPDB getLatestLogMP() {
        return INSTANCE.where(LogMPDB.class)
                .sort("CoordTime", Sort.DESCENDING)
                .findFirst();
    }

    /**
     * Нормализует время к миллисекундам.
     * Если значение слишком большое для миллисекунд (микросекунды), делим на 1000.
     * Если наоборот слишком маленькое (секунды), умножаем на 1000.
     */
    public static long normalizeToMillis(long time) {
        long currentMillis = System.currentTimeMillis();

        if (time > currentMillis * 10) { // микросекунды
            return time / 1000L;
        } else if (time < 10_000_000_000L) { // секунды (примерно до 2286 года)
            return time * 1000L;
        } else {
            return time; // уже миллисекунды
        }
    }


}
