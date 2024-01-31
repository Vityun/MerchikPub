package ua.com.merchik.merchik.database.realm.tables;

import ua.com.merchik.merchik.data.RealmModels.LogDB;
import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.List;

public class LogRealm {

    public static void setLogTable(List<LogDB> data) {
        INSTANCE.beginTransaction();
        INSTANCE.delete(LogDB.class);
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    public static LogDB getLogDbById(Integer id) {
        return INSTANCE.where(LogDB.class)
                .equalTo("id", id)
                .findFirst();
    }

    public static LogDB getLogDbByKodOb(Long kodOb) {
        return INSTANCE.where(LogDB.class)
                .equalTo("obj_id", kodOb)
                .findFirst();
    }

}
