package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class SiteObjectsRealm {


    public static void setToDB(List<SiteObjectsDB> list) {
        INSTANCE.beginTransaction();
        INSTANCE.delete(SiteObjectsDB.class);
        INSTANCE.copyToRealmOrUpdate(list);
        INSTANCE.commitTransaction();
    }

}
