package ua.com.merchik.merchik.data.synchronization;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.SynchronizationTimeTable;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.room.DaoInterfaces.SynchronizationTimetableDao;
import ua.com.merchik.merchik.database.room.RoomManager;

public final class SynchronizationTimetableRepository {

    private static final String TAG = "SyncTimetableRoom";
    private static boolean roomSchemaMigrated;

    private SynchronizationTimetableRepository() {
    }

    public static void markRoomSchemaMigrated() {
        roomSchemaMigrated = true;
    }

    public static List<SynchronizationTimetableDB> getAllLegacy() {
        ensureDefaults();
        SynchronizationTimetableDao dao = dao();
        if (dao == null) {
            return new ArrayList<>();
        }

        Map<String, SynchronizationTimetableDB> uniqueRows = new LinkedHashMap<>();
        for (SynchronizationTimeTable entity : dao.getAll()) {
            SynchronizationTimetableDB row = toLegacy(entity);
            if (row.getTable_name() != null && !uniqueRows.containsKey(row.getTable_name())) {
                uniqueRows.put(row.getTable_name(), row);
            }
        }
        return new ArrayList<>(uniqueRows.values());
    }

    public static List<SynchronizationTimetableDB> getUserGeneratedLegacy() {
        ensureDefaults();
        SynchronizationTimetableDao dao = dao();
        if (dao == null) {
            return new ArrayList<>();
        }

        List<SynchronizationTimetableDB> result = new ArrayList<>();
        for (SynchronizationTimeTable entity : dao.getUserGenerated()) {
            result.add(toLegacy(entity));
        }
        return result;
    }

    public static SynchronizationTimetableDB getByTableName(String tableName) {
        ensureDefaults();
        SynchronizationTimetableDao dao = dao();
        TableName table = resolveTableName(tableName);
        if (dao == null || table == null) {
            return null;
        }

        SynchronizationTimeTable entity = dao.getByTableName(table);
        if (entity == null) {
            SynchronizationTimetableDB defaultRow = findDefaultLegacy(tableName);
            if (defaultRow != null) {
                upsertLegacy(defaultRow);
                entity = dao.getByTableName(table);
            }
        }
        return entity == null ? null : toLegacy(entity);
    }

    public static void upsertLegacy(SynchronizationTimetableDB row) {
        SynchronizationTimetableDao dao = dao();
        if (dao == null || row == null || row.getTable_name() == null) {
            return;
        }

        TableName table = resolveTableName(row.getTable_name());
        if (table == null) {
            Log.w(TAG, "Unknown synchronization table: " + row.getTable_name());
            return;
        }

        SynchronizationTimeTable existing = dao.getByTableName(table);
        int id = existing != null ? existing.getId() : resolveInsertId(dao, row.getId(), table);
        long lastDownloadTime = maxPositive(row.getVpi_app(), row.getVpi_server());
        long lastUploadTime = maxPositive(row.getVpo_app(), row.getVpo_export());
        if (existing != null) {
            lastDownloadTime = Math.max(lastDownloadTime, existing.getLastDownloadTime());
            lastUploadTime = Math.max(lastUploadTime, existing.getLastUploadTime());
        }

        SynchronizationTimeTable entity = new SynchronizationTimeTable(
                id,
                table,
                row.getUpdate_frequency() > 0
                        ? row.getUpdate_frequency()
                        : existing != null ? existing.getSyncPeriodSeconds() : 0L,
                lastDownloadTime,
                lastUploadTime,
                existing != null ? existing.getDownloadedItems() : 0,
                existing != null ? existing.getUploadedItems() : 0,
                row.tableTxt != null ? row.tableTxt : table.getCode(),
                row.update == 1,
                existing != null ? existing.getLastDownloadStatus() : DownloadStatus.SUCCESS,
                existing != null ? existing.getLastUploadStatus() : DownloadStatus.SUCCESS
        );
        dao.insert(entity);
    }

    public static void ensureDefaults() {
        SynchronizationTimetableDao dao = dao();
        if (dao == null) {
            return;
        }

        for (SynchronizationTimetableDB row : defaultLegacyRows()) {
            TableName table = resolveTableName(row.getTable_name());
            if (table != null && dao.getByTableName(table) == null) {
                upsertLegacy(row);
            }
        }
    }

    public static void resetToDefaults() {
        SynchronizationTimetableDao dao = dao();
        if (dao == null) {
            return;
        }

        dao.clear();
        for (SynchronizationTimetableDB row : defaultLegacyRows()) {
            upsertLegacy(row);
        }
    }

    public static void migrateFromRealmIfNeeded() {
        SynchronizationTimetableDao dao = dao();
        if (dao == null) {
            return;
        }
        if (consumeRoomSchemaMigrated() || RealmManager.consumeRealmSchemaMigrated()) {
            resetToDefaults();
            Log.i(TAG, "Synchronization timetable was reset after schema migration");
            return;
        }
        if (RealmManager.INSTANCE == null) {
            ensureDefaults();
            return;
        }

        try {
            RealmResults<SynchronizationTimetableDB> realmRows =
                    RealmManager.INSTANCE.where(SynchronizationTimetableDB.class).findAll();
            List<SynchronizationTimetableDB> rows = RealmManager.INSTANCE.copyFromRealm(realmRows);
            for (SynchronizationTimetableDB row : rows) {
                TableName table = resolveTableName(row.getTable_name());
                if (table == null) {
                    continue;
                }

                upsertLegacy(row);
            }
        } catch (Exception e) {
            Log.e(TAG, "Realm to Room migration failed", e);
        }
        ensureDefaults();
    }

    public static List<SynchronizationTimetableDB> defaultLegacyRows() {
        return Arrays.asList(
                row(1, "wp_data", 600, 0, 0, "wp_data", 0),
                row(2, "image_tp", 36000, 0, 0, "image_tp", 0),
                row(3, "client_group_tp", 36000, 0, 0, "client_group_tp", 0),
                row(4, "log_mp", 600, 0, 0, "log_mp", 0),
                row(5, "clients", 36000, 0, 0, "clients", 0),
                row(6, "address", 36000, 0, 0, "address", 0),
                row(7, "users", 36000, 0, 0, "users", 0),
                row(8, "promoList", 3600000, 0, 0, "promoList", 0),
                row(9, "errorsList", 3600000, 0, 0, "errorsList", 0),
                row(10, "stack_photo", 36000, 0, 0, "stack_photo", 0),
                row(11, "task_and_reclamations", 600, 0, 0, "task_and_reclamations", 0),
                row(12, "planogram", 36000, 0, 0, "planogram", 0),
                row(13, "address_sql", 36000, 0, 0, "address_sql", 0),
                row(14, "clients_sql", 36000, 0, 0, "clients_sql", 0),
                row(15, "users_sql", 36000, 0, 0, "users_sql", 0),
                row(16, "city_sql", 36000, 0, 0, "city_sql", 0),
                row(17, "oblast_sql", 36000, 0, 0, "oblast_sql", 0),
                row(18, "sample_photo", 604800, 0, 0, "sample_photo", 0),
                row(19, "ekl_sql", 36000, 0, 0, "ekl_sql", 0),
                row(20, "location", 86400, 0, 0, "location", 0),
                row(21, "photo_tovar", 86400, 0, 0, "photo_tovar", 1),
                row(22, "photo_sample", 86400, 0, 0, "photo_sample", 1),
                row(23, "photo_planogram", 86400, 0, 0, "photo_planogram", 1),
                row(24, "photo_showcase", 86400, 0, 0, "photo_showcase", 1),
                row(25, "coments_to_photo", 86400, 0, 0, "coments_to_photo", 1),
                row(26, "photo_user_from_serv", 86400, 0, 0, "photo_user_from_serv", 1),
                row(27, "upload_ekl", 86400, 0, 0, "upload_ekl", 1),
                row(28, "photo_tar", 86400, 0, 0, "photo_tar", 1),
                row(29, "dossier_sotr", 86400, 0, 0, "dossier_sotr", 1),
                row(30, "vacancy", 86400, 0, 0, "vacancy", 1),
                row(31, "bonus", 86400, 0, 0, "bonus", 1),
                row(32, "site_url", 86400, 0, 0, "site_url", 1),
                row(33, "site_account", 86400, 0, 0, "site_account", 1),
                row(34, "sms_log", 86400, 0, 0, "sms_log", 0),
                row(35, "achievements", 600, 0, 0, "achievements", 0),
                row(37, "standart_table", 86400, 0, 0, "standart_table", 0),
                row(38, "content_table", 86400, 0, 0, "content_table", 0),
                row(39, "report_prepare", 86400, 0, 0, "report_prepare", 0),
                row(40, "vizit_showcase_list", 86400, 0, 0, "vizit_showcase_list", 0),
                row(41, "images_vote", 86400, 0, 0, "images_vote", 0),
                row(42, "theme_list", 86400, 0, 0, "theme_list", 0),
                row(43, "options_list", 86400, 0, 0, "options_list", 0),
                row(44, "additional_requirements", 86400, 0, 0, "additional_requirements", 0),
                row(45, "tovar_list", 86400, 0, 0, "tovar_list", 0),
                row(46, "wifi_mac_location", 86400, 0, 0, "wifi_mac_location", 0),
                row(47, "planogramm_vizit_showcase", 86400, 0, 0, "planogramm_vizit_showcase", 0),
                row(48, "question_answer", 86400, 0, 10000000, "question_answer", 0),
                row(49, "wp_data_pause", 600, 0, 0, "wp_data_pause", 1)
        );
    }

    private static SynchronizationTimetableDB toLegacy(SynchronizationTimeTable entity) {
        SynchronizationTimetableDB row = new SynchronizationTimetableDB(
                entity.getId(),
                entity.getTableName().getCode(),
                (int) entity.getSyncPeriodSeconds(),
                entity.getLastDownloadTime(),
                entity.getLastDownloadTime(),
                entity.getLastUploadTime(),
                entity.getLastUploadTime(),
                entity.getDescription(),
                entity.isUserGenerated() ? 1 : 0
        );
        if (row.tableTxt == null || row.tableTxt.trim().isEmpty()) {
            row.tableTxt = entity.getTableName().getCode();
        }
        return row;
    }

    private static SynchronizationTimetableDB row(int id,
                                                  String tableName,
                                                  int frequency,
                                                  long vpiServer,
                                                  long vpiApp,
                                                  String text,
                                                  int update) {
        return new SynchronizationTimetableDB(id, tableName, frequency, vpiServer, vpiApp, 0, 0, text, update);
    }

    private static SynchronizationTimetableDB findDefaultLegacy(String tableName) {
        for (SynchronizationTimetableDB row : defaultLegacyRows()) {
            if (row.getTable_name().equals(tableName)) {
                return row;
            }
        }
        return null;
    }

    private static int resolveInsertId(SynchronizationTimetableDao dao, int preferredId, TableName table) {
        if (preferredId > 0) {
            SynchronizationTimeTable byId = dao.getById(preferredId);
            if (byId == null || byId.getTableName() == table) {
                return preferredId;
            }
        }

        Integer maxId = dao.getMaxId();
        return maxId == null ? table.ordinal() : maxId + 1;
    }

    private static long maxPositive(long first, long second) {
        return Math.max(Math.max(first, second), 0L);
    }

    private static boolean consumeRoomSchemaMigrated() {
        boolean result = roomSchemaMigrated;
        roomSchemaMigrated = false;
        return result;
    }

    private static TableName resolveTableName(String tableName) {
        for (TableName item : TableName.values()) {
            if (item.getCode().equals(tableName)) {
                return item;
            }
        }
        return null;
    }

    private static SynchronizationTimetableDao dao() {
        if (RoomManager.SQL_DB == null) {
            return null;
        }
        return RoomManager.SQL_DB.synchronizationTimetableDao();
    }
}
