package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.List;

import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;

public class LogMPRealm {

    public static void setLogMP(List<LogMPDB> list){
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(list);
        INSTANCE.commitTransaction();
    }

    public static List<LogMPDB> getAllLogMP() {
        return INSTANCE.copyFromRealm(INSTANCE.where(LogMPDB.class)
                .findAll());
    }

    public static List<LogMPDB> getLogMPTime(long startTime, long endTime) {
        RealmQuery<LogMPDB> query = INSTANCE.where(LogMPDB.class);
        query = query.greaterThanOrEqualTo("vpi", startTime);
        query = query.and().lessThanOrEqualTo("vpi", endTime);
        RealmResults<LogMPDB> results = query
                .sort("vpi", Sort.DESCENDING) // Сортировка по убыванию
                .notEqualTo("CoordX", 0d)
                .notEqualTo("CoordY", 0d)
                .findAll();
        return INSTANCE.copyFromRealm(results);
    }


}
