package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.data.Database.Room.DynamicAchievementSDB;
import ua.com.merchik.merchik.data.Database.Room.DynamicPhotoSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.DynamicAchievementsResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.DynamicPhotoResponse;
import ua.com.merchik.merchik.data.SynchronizationTimeTable;
import ua.com.merchik.merchik.data.synchronization.DownloadStatus;
import ua.com.merchik.merchik.data.synchronization.SynchronizationTimetableRepository;
import ua.com.merchik.merchik.data.synchronization.TableName;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class DynamicAchievementsExchange {

    private static final String TAG = "DynamicAchievementsExchange";
    private static final long DEFAULT_SYNC_PERIOD_SECONDS = 600L;
    private static final long DOWNLOAD_OVERLAP_SECONDS = 300L;
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    private final Gson gson = new Gson();

    public void syncInCron() {
        if (SQL_DB == null) {
            logError("syncInCron", "skip: Room database is not initialized");
            return;
        }

        if (!RUNNING.compareAndSet(false, true)) {
            logInfo("syncInCron", "skip: previous sync is still running");
            return;
        }

        Single.fromCallable(this::syncSync)
                .subscribeOn(Schedulers.io())
                .doFinally(() -> RUNNING.set(false))
                .subscribe(
                        summary -> logInfo("syncInCron", summary),
                        throwable -> logError("syncInCron", messageOf(throwable))
                );
    }

    private String syncSync() throws Exception {
        List<String> summary = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            summary.add(downloadDynamicAchievementsSync());
        } catch (Exception e) {
            errors.add("dynamic: " + messageOf(e));
            logError("images_achieve.dynamic", messageOf(e));
        }

        try {
            summary.add(downloadDynamicPhotoSync());
        } catch (Exception e) {
            errors.add("dynamic_photo: " + messageOf(e));
            logError("images_achieve.dynamic_photo", messageOf(e));
        }

        String result = joinSummary(summary);
        if (!errors.isEmpty()) {
            throw new IllegalStateException(result + "; errors=" + errors);
        }

        return result;
    }

    private String downloadDynamicAchievementsSync() throws Exception {
        SynchronizationTimeTable syncInfo = ensureSyncInfo(TableName.DYNAMIC_ACHIEVEMENTS);
        SyncWindow window = prepareSyncWindow(syncInfo);
        if (!window.isDue) {
            return "dynamic skip: next download in " + window.nextDownloadInSec + " sec";
        }

        try {
            JsonObject response = executeJsonRequest(
                    "images_achieve.dynamic",
                    buildDownloadRequest("dynamic", window.fromSec, window.toSec)
            );

            if (!isStateSuccessful(response)) {
                throw new IllegalStateException(serverError(response));
            }

            DynamicAchievementsResponse parsed = gson.fromJson(response, DynamicAchievementsResponse.class);
            List<DynamicAchievementSDB> rows = sanitizeDynamicAchievements(parsed != null ? parsed.list : null);
            if (!rows.isEmpty()) {
                SQL_DB.dynamicAchievementsDao().insertAll(rows);
            }

            saveDownloadSyncInfo(syncInfo, successfulWatermark(syncInfo, window.toSec), rows.size(), DownloadStatus.SUCCESS);
            logInfo("images_achieve.dynamic", responseSummary(response, rows.size()));
            return "dynamic downloaded=" + rows.size();
        } catch (Exception e) {
            saveDownloadSyncInfo(syncInfo, syncInfo.getLastDownloadTime(), syncInfo.getDownloadedItems(), DownloadStatus.ERROR);
            throw e;
        }
    }

    private String downloadDynamicPhotoSync() throws Exception {
        SynchronizationTimeTable syncInfo = ensureSyncInfo(TableName.DYNAMIC_PHOTO);
        SyncWindow window = prepareSyncWindow(syncInfo);
        if (!window.isDue) {
            return "dynamic_photo skip: next download in " + window.nextDownloadInSec + " sec";
        }

        try {
            JsonObject response = executeJsonRequest(
                    "images_achieve.dynamic_photo",
                    buildDownloadRequest("dynamic_photo", window.fromSec, window.toSec)
            );

            if (!isStateSuccessful(response)) {
                throw new IllegalStateException(serverError(response));
            }

            DynamicPhotoResponse parsed = gson.fromJson(response, DynamicPhotoResponse.class);
            List<DynamicPhotoSDB> rows = sanitizeDynamicPhotos(parsed != null ? parsed.list : null);
            if (!rows.isEmpty()) {
                SQL_DB.dynamicPhotoDao().insertAll(rows);
            }

            saveDownloadSyncInfo(syncInfo, successfulWatermark(syncInfo, window.toSec), rows.size(), DownloadStatus.SUCCESS);
            logInfo("images_achieve.dynamic_photo", responseSummary(response, rows.size()));
            boolean stackPhotoDownloadStarted = new DynamicPhotoStackPhotoExchange()
                    .downloadMissingFromDynamicPhotoTableAsync();

            return "dynamic_photo downloaded=" + rows.size()
                    + ", stack_photo_missing_check=" + (stackPhotoDownloadStarted ? "started" : "skipped");
        } catch (Exception e) {
            saveDownloadSyncInfo(syncInfo, syncInfo.getLastDownloadTime(), syncInfo.getDownloadedItems(), DownloadStatus.ERROR);
            throw e;
        }
    }

    public int downloadDynamicAchievementsForVisitSync(String clientId, String addrId) throws Exception {
        if (isBlank(clientId) || isBlank(addrId)) {
            logWarn(
                    "images_achieve.dynamic.visit",
                    "skip: clientId or addrId is empty, clientId=" + clientId + ", addrId=" + addrId
            );
            return 0;
        }

        JsonObject response = executeJsonRequest(
                "images_achieve.dynamic.visit",
                buildDownloadRequest("dynamic", 0L, currentTimeSeconds(), clientId, addrId)
        );

        if (!isStateSuccessful(response)) {
            throw new IllegalStateException(serverError(response));
        }

        DynamicAchievementsResponse parsed = gson.fromJson(response, DynamicAchievementsResponse.class);
        List<DynamicAchievementSDB> rows = sanitizeDynamicAchievements(parsed != null ? parsed.list : null);
        if (!rows.isEmpty()) {
            SQL_DB.dynamicAchievementsDao().insertAll(rows);
        }

        logInfo(
                "images_achieve.dynamic.visit",
                "clientId=" + clientId + ", addrId=" + addrId + ", " + responseSummary(response, rows.size())
        );
        return rows.size();
    }

    private JsonObject buildDownloadRequest(String act, long dtChangeFrom, long dtChangeTo) {
        return buildDownloadRequest(act, dtChangeFrom, dtChangeTo, null, null);
    }

    private JsonObject buildDownloadRequest(
            String act,
            long dtChangeFrom,
            long dtChangeTo,
            String clientId,
            String addrId
    ) {
        JsonObject request = new JsonObject();
        request.addProperty("mod", "images_achieve");
        request.addProperty("act", act);

        JsonObject filter = new JsonObject();
        filter.addProperty("dt_change_from", String.valueOf(dtChangeFrom));
        filter.addProperty("dt_change_to", String.valueOf(dtChangeTo));
        if (!isBlank(clientId)) {
            filter.addProperty("client_id", clientId);
        }
        if (!isBlank(addrId)) {
            filter.addProperty("addr_id", addrId);
        }
        request.add("filter", filter);

        return request;
    }

    private List<DynamicAchievementSDB> sanitizeDynamicAchievements(List<DynamicAchievementSDB> rows) {
        List<DynamicAchievementSDB> result = new ArrayList<>();
        if (rows == null || rows.isEmpty()) return result;

        for (DynamicAchievementSDB row : rows) {
            if (row == null || isBlank(row.id)) continue;
            result.add(row);
        }
        return result;
    }

    private List<DynamicPhotoSDB> sanitizeDynamicPhotos(List<DynamicPhotoSDB> rows) {
        List<DynamicPhotoSDB> result = new ArrayList<>();
        if (rows == null || rows.isEmpty()) return result;

        for (DynamicPhotoSDB row : rows) {
            if (row == null || isBlank(row.id)) continue;
            result.add(row);
        }
        return result;
    }

    private SyncWindow prepareSyncWindow(SynchronizationTimeTable syncInfo) {
        long syncPeriodSeconds = syncInfo.getSyncPeriodSeconds() > 0
                ? syncInfo.getSyncPeriodSeconds()
                : DEFAULT_SYNC_PERIOD_SECONDS;

        if (syncInfo.getLastDownloadTime() > 0
                && !TrustedTime.isSyncDue(syncInfo.getLastDownloadTime(), syncPeriodSeconds)) {
            return SyncWindow.skip(TrustedTime.secondsUntilSync(syncInfo.getLastDownloadTime(), syncPeriodSeconds));
        }

        long from = syncInfo.getLastDownloadTime() > DOWNLOAD_OVERLAP_SECONDS
                ? syncInfo.getLastDownloadTime() - DOWNLOAD_OVERLAP_SECONDS
                : 0L;
        long to = currentTimeSeconds();

        return SyncWindow.due(from, Math.max(to, syncInfo.getLastDownloadTime()));
    }

    private JsonObject executeJsonRequest(String requestName, JsonObject request) throws Exception {
        return timeRequest(requestName, () -> {
            logInfo(requestName, "request=" + request);

            Response<JsonObject> response = RetrofitBuilder.getRetrofitInterface()
                    .TEST_JSON_UPLOAD(RetrofitBuilder.contentType, request)
                    .execute();

            if (!response.isSuccessful()) {
                throw new IllegalStateException("HTTP " + response.code() + " " + response.message());
            }

            JsonObject body = response.body();
            if (body == null) {
                throw new IllegalStateException("empty response body");
            }

            return body;
        });
    }

    private SynchronizationTimeTable ensureSyncInfo(TableName tableName) {
        SynchronizationTimetableRepository.ensureDefaults();

        SynchronizationTimeTable syncInfo = SQL_DB.synchronizationTimetableDao()
                .getByTableName(tableName);

        if (syncInfo != null) return syncInfo;

        SynchronizationTimeTable created = new SynchronizationTimeTable(
                tableName == TableName.DYNAMIC_ACHIEVEMENTS ? 50 : 51,
                tableName,
                DEFAULT_SYNC_PERIOD_SECONDS,
                0L,
                0L,
                0,
                0,
                tableName.getCode(),
                false,
                DownloadStatus.SUCCESS,
                DownloadStatus.SUCCESS
        );
        SQL_DB.synchronizationTimetableDao().insert(created);

        return created;
    }

    private void saveDownloadSyncInfo(SynchronizationTimeTable current,
                                      long lastDownloadTime,
                                      int downloadedItems,
                                      DownloadStatus downloadStatus) {
        SynchronizationTimeTable updated = new SynchronizationTimeTable(
                current.getId(),
                current.getTableName(),
                current.getSyncPeriodSeconds(),
                lastDownloadTime,
                current.getLastUploadTime(),
                downloadedItems,
                current.getUploadedItems(),
                current.getDescription(),
                current.isUserGenerated(),
                downloadStatus,
                current.getLastUploadStatus()
        );
        SQL_DB.synchronizationTimetableDao().insert(updated);
    }

    private boolean isStateSuccessful(JsonObject response) {
        if (response == null) return false;

        if (!response.has("state") || response.get("state").isJsonNull()) {
            String error = optString(response, "error");
            return error == null || error.trim().isEmpty();
        }

        try {
            String state = response.get("state").getAsString();
            if ("1".equals(state)) return true;
            if ("0".equals(state)) return false;
            return Boolean.parseBoolean(state);
        } catch (Exception e) {
            return false;
        }
    }

    private String serverError(JsonObject response) {
        String error = optString(response, "error");
        if (!isBlank(error)) return error;

        String message = optString(response, "message");
        if (!isBlank(message)) return message;

        return "server state=false";
    }

    private String responseSummary(JsonObject response, int savedRows) {
        return "state=" + optString(response, "state")
                + ", savedRows=" + savedRows
                + ", page_total=" + optString(response, "page_total")
                + ", item_total=" + optString(response, "item_total")
                + ", rows_total=" + optString(response, "rows_total")
                + ", error=" + optString(response, "error");
    }

    private String optString(JsonObject response, String key) {
        if (response == null || !response.has(key) || response.get(key).isJsonNull()) return null;

        try {
            return response.get(key).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private long currentTimeSeconds() {
        return TrustedTime.nowServerSecOrLocalSec();
    }

    private long successfulWatermark(SynchronizationTimeTable syncInfo, long requestToSec) {
        if (TrustedTime.nowServerSecOrNull() == null) {
            logWarn(
                    "watermark",
                    "trusted server time is empty. Keep previous watermark=" + syncInfo.getLastDownloadTime()
            );
            return syncInfo.getLastDownloadTime();
        }

        return Math.max(syncInfo.getLastDownloadTime(), requestToSec);
    }

    private String messageOf(Throwable throwable) {
        return throwable.getMessage() != null ? throwable.getMessage() : throwable.toString();
    }

    private String joinSummary(List<String> summary) {
        if (summary == null || summary.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        for (String item : summary) {
            if (isBlank(item)) continue;
            if (builder.length() > 0) builder.append("; ");
            builder.append(item);
        }
        return builder.toString();
    }

    private <T> T timeRequest(String requestName, ThrowingSupplier<T> supplier) throws Exception {
        long startedAt = SystemClock.elapsedRealtime();
        try {
            return supplier.get();
        } finally {
            long durationMs = SystemClock.elapsedRealtime() - startedAt;
            logInfo(requestName, "durationMs=" + durationMs);
        }
    }

    private void logInfo(String place, String message) {
        Log.e(TAG, place + ": " + message);
        Globals.writeToMLOG("INFO", TAG + "/" + place, message);
    }

    private void logError(String place, String message) {
        Log.e(TAG, place + ": " + message);
        Globals.writeToMLOG("ERROR", TAG + "/" + place, message);
    }

    private void logWarn(String place, String message) {
        Log.w(TAG, place + ": " + message);
        Globals.writeToMLOG("WARN", TAG + "/" + place, message);
    }

    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private static class SyncWindow {
        final boolean isDue;
        final long fromSec;
        final long toSec;
        final long nextDownloadInSec;

        private SyncWindow(boolean isDue, long fromSec, long toSec, long nextDownloadInSec) {
            this.isDue = isDue;
            this.fromSec = fromSec;
            this.toSec = toSec;
            this.nextDownloadInSec = nextDownloadInSec;
        }

        static SyncWindow due(long fromSec, long toSec) {
            return new SyncWindow(true, fromSec, toSec, 0L);
        }

        static SyncWindow skip(long nextDownloadInSec) {
            return new SyncWindow(false, 0L, 0L, nextDownloadInSec);
        }
    }
}
