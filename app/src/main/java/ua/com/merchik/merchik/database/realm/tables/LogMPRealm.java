package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

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
        RealmQuery<LogMPDB> query = INSTANCE.where(LogMPDB.class);
        query = query.greaterThanOrEqualTo("CoordTime", startTime);
        query = query.and().lessThanOrEqualTo("CoordTime", endTime);
        RealmResults<LogMPDB> results = query
                .sort("CoordTime", Sort.DESCENDING) // Сортировка по убыванию
                .notEqualTo("CoordX", 0d)
                .notEqualTo("CoordY", 0d)
                .findAll();
        return INSTANCE.copyFromRealm(results);
    }

    public static List<LogMPDB> getLogMPTimeDad2(long startTime, long endTime, long codeDad2) {
        RealmQuery<LogMPDB> query = INSTANCE.where(LogMPDB.class);
        query = query.greaterThanOrEqualTo("CoordTime", startTime);
        query = query.and().lessThanOrEqualTo("CoordTime", endTime);
        RealmResults<LogMPDB> results = query
                .sort("CoordTime", Sort.DESCENDING) // Сортировка по убыванию
                .equalTo("codeDad2", codeDad2)
                .notEqualTo("CoordX", 0d)
                .notEqualTo("CoordY", 0d)
                .findAll();
        return INSTANCE.copyFromRealm(results);
    }


}
