package ua.com.merchik.merchik.Utils;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ua.com.merchik.merchik.data.SynchronizationTimeTable;
import ua.com.merchik.merchik.data.synchronization.DownloadStatus;
import ua.com.merchik.merchik.data.synchronization.TableName;
import ua.com.merchik.merchik.database.room.DaoInterfaces.SynchronizationTimetableDao;

public class DatabaseInitializer {

    private final int SEC_10 = 10;
    private final int MINUTE_1 = 60;
    private final int MINUTE_5 = 300;
    private final int MINUTE_10 = 600;
    private final int MINUTE_30 = 3000;
    private final int HOUR_1 = 6000;


    private final SynchronizationTimetableDao timetableDao;

    public DatabaseInitializer(SynchronizationTimetableDao timetableDao) {
        this.timetableDao = timetableDao;
    }

    public void initializeDefaultData() {
        List<SynchronizationTimeTable> defaultTables = Arrays.asList(
                createDefaultEntry(TableName.WP_DATA, MINUTE_10, true),
                createDefaultEntry(TableName.IMAGE_TP, 600, false),
                createDefaultEntry(TableName.CLIENT_GROUP_TP, 3600, false),
                createDefaultEntry(TableName.LOG_MP, 600, false),
                createDefaultEntry(TableName.CLIENTS, 6000, false),
                createDefaultEntry(TableName.ADDRESS, 6000, false),
                createDefaultEntry(TableName.USERS, 6000, false),
                createDefaultEntry(TableName.PROMO_LIST, 6000, false),
                createDefaultEntry(TableName.ERROR_LIST, 60000, false),
                createDefaultEntry(TableName.STACK_PHOTO, 600, false),
                createDefaultEntry(TableName.TASK_AND_RECLAMATION, SEC_10, true),
                createDefaultEntry(TableName.PLANOGRAMM, MINUTE_30, false),
                createDefaultEntry(TableName.PLANOGRAMM_ADDRESS, MINUTE_30, false),
                createDefaultEntry(TableName.PLANOGRAMM_GROUP, MINUTE_30, false),
                createDefaultEntry(TableName.PLANOGRAMM_TYPE, MINUTE_30, false),
                createDefaultEntry(TableName.PLANOGRAMM_IMAGES, MINUTE_30, false),
                createDefaultEntry(TableName.PLANOGRAMM_VIZIT_SHOWCASE, MINUTE_10, true)
        );

        timetableDao.insertAll(defaultTables)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> Log.d("Database", "Default data initialized"),
                        e -> Log.e("Database", "Init error", e));
    }

    private SynchronizationTimeTable createDefaultEntry(TableName tableName,
                                                        long syncPeriodSeconds,
                                                        boolean isUserGenerated) {
        return new SynchronizationTimeTable(
                tableName.ordinal(), // используем ordinal как ID
                tableName,
                syncPeriodSeconds,
                0, // lastDownloadTime
                0, // lastUploadTime
                0, // downloadedItems
                0, // uploadedItems
                "Auto-generated", // description
                isUserGenerated,
                DownloadStatus.SUCCESS,
                DownloadStatus.SUCCESS
        );
    }
}