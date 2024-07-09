package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.LogDB;

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

    public static LogDB getLogByODADandTheme(Long kodOb, int themeId) {

        LogDB res = INSTANCE.where(LogDB.class)
                .equalTo("obj_id", kodOb)
                .equalTo("tp", themeId)
                .findFirst();

        if (res != null) {
            return INSTANCE.copyFromRealm(res);
        }else {
            return null;
        }
    }

}
