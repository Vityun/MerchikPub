package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.synchronization.SynchronizationTimetableRepository;

public class SynchronizationTimetableRealm {

    public static List<SynchronizationTimetableDB> getSynchronizationTimetable() {
        return SynchronizationTimetableRepository.getUserGeneratedLegacy();
    }
}
