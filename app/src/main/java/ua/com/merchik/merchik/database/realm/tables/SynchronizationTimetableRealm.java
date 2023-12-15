package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;

public class SynchronizationTimetableRealm {

    public static List<SynchronizationTimetableDB> getSynchronizationTimetable(){
        return INSTANCE.copyFromRealm(INSTANCE.where(SynchronizationTimetableDB.class)
                .equalTo("update", 1)
                .findAll());
    }


}
