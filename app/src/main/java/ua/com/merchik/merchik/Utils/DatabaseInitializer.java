package ua.com.merchik.merchik.Utils;

import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.SynchronizationTimeTable;
import ua.com.merchik.merchik.data.synchronization.DownloadStatus;
import ua.com.merchik.merchik.data.synchronization.SynchronizationTimetableRepository;
import ua.com.merchik.merchik.data.synchronization.TableName;
import ua.com.merchik.merchik.database.room.DaoInterfaces.SynchronizationTimetableDao;

public class DatabaseInitializer {

    private final SynchronizationTimetableDao timetableDao;

    public DatabaseInitializer(SynchronizationTimetableDao timetableDao) {
        this.timetableDao = timetableDao;
    }

    public void initializeDefaultData() {
        for (SynchronizationTimetableDB row : SynchronizationTimetableRepository.defaultLegacyRows()) {
            TableName tableName = TableName.fromCode(row.getTable_name());
            timetableDao.insert(new SynchronizationTimeTable(
                    row.getId(),
                    tableName,
                    row.getUpdate_frequency(),
                    row.getVpi_app(),
                    0,
                    0,
                    0,
                    row.tableTxt,
                    row.update == 1,
                    DownloadStatus.SUCCESS,
                    DownloadStatus.SUCCESS
            ));
        }
    }
}
