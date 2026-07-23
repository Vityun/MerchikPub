package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.Database.Room.WPDataPauseSDB;
import ua.com.merchik.merchik.data.SynchronizationTimeTable;
import ua.com.merchik.merchik.data.synchronization.DownloadStatus;
import ua.com.merchik.merchik.data.synchronization.TableName;
import ua.com.merchik.merchik.database.room.DaoInterfaces.WPDataPauseDao;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class WPDataPauseExchange {

    private static final String TAG = "WPDataPauseExchange";
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
                        throwable -> logError("syncInCron", throwable.getMessage() != null ? throwable.getMessage() : throwable.toString())
                );
    }

    private String syncSync() throws Exception {
        SynchronizationTimeTable syncInfo = ensureSyncInfo();
        WPDataPauseDao dao = SQL_DB.wpDataPauseDao();
        List<WPDataPauseSDB> pendingUpload = dao.getUploadToServer();
        long now = currentTimeSeconds();
        long syncPeriodSeconds = syncInfo.getSyncPeriodSeconds() > 0
                ? syncInfo.getSyncPeriodSeconds()
                : DEFAULT_SYNC_PERIOD_SECONDS;

        if ((pendingUpload == null || pendingUpload.isEmpty())
                && syncInfo.getLastDownloadTime() > 0
                && syncInfo.getLastDownloadTime() + syncPeriodSeconds > now) {
            return "skip: next download in "
                    + ((syncInfo.getLastDownloadTime() + syncPeriodSeconds) - now)
                    + " sec";
        }

        int uploadedItems = 0;
        int downloadedItems = 0;
        boolean uploadOk = false;
        boolean downloadOk = false;
        List<String> errors = new ArrayList<>();

        try {
            uploadedItems = uploadPauseTableSync(pendingUpload, now);
            uploadOk = true;
        } catch (Exception e) {
            errors.add("pause_save: " + messageOf(e));
            logError("pause_save", messageOf(e));
        }

        try {
            downloadedItems = downloadPauseTableSync(syncInfo, now);
            downloadOk = true;
        } catch (Exception e) {
            errors.add("pause_list: " + messageOf(e));
            logError("pause_list", messageOf(e));
        }

        saveSyncInfo(
                syncInfo,
                downloadOk ? now : syncInfo.getLastDownloadTime(),
                uploadOk ? now : syncInfo.getLastUploadTime(),
                downloadOk ? downloadedItems : syncInfo.getDownloadedItems(),
                uploadOk ? uploadedItems : syncInfo.getUploadedItems(),
                downloadOk ? DownloadStatus.SUCCESS : DownloadStatus.ERROR,
                uploadOk ? DownloadStatus.SUCCESS : DownloadStatus.ERROR
        );

        String summary = "uploaded=" + uploadedItems + ", downloaded=" + downloadedItems;
        if (!errors.isEmpty()) {
            throw new IllegalStateException(summary + ", errors=" + errors);
        }

        return summary;
    }

    private int uploadPauseTableSync(List<WPDataPauseSDB> pendingUpload, long nowSeconds) throws Exception {
        if (pendingUpload == null || pendingUpload.isEmpty()) {
            logInfo("pause_save", "skip: no local rows for upload");
            return 0;
        }

        List<WPDataPauseSDB> validUpload = filterValidUploadRows(pendingUpload);
        if (validUpload.isEmpty()) {
            SQL_DB.wpDataPauseDao().markUploadedSync(pendingUpload);
            logError("pause_save", "skip: all local rows are invalid, count=" + pendingUpload.size());
            return 0;
        }

        JsonObject request = new JsonObject();
        request.addProperty("mod", "plan");
        request.addProperty("act", "pause_save");
        request.add("data", buildUploadRows(validUpload, nowSeconds));

        JsonObject response = executeJsonRequest("plan.pause_save", request);
        if (!isStateSuccessful(response)) {
            throw new IllegalStateException(serverError(response));
        }

        SQL_DB.wpDataPauseDao().markUploadedSync(validUpload);
        logInfo("pause_save", "uploaded rows count=" + validUpload.size());

        return validUpload.size();
    }

    private int downloadPauseTableSync(SynchronizationTimeTable syncInfo, long nowSeconds) throws Exception {
        long from = syncInfo.getLastDownloadTime() > DOWNLOAD_OVERLAP_SECONDS
                ? syncInfo.getLastDownloadTime() - DOWNLOAD_OVERLAP_SECONDS
                : 0L;

        JsonObject request = new JsonObject();
        request.addProperty("mod", "plan");
        request.addProperty("act", "pause_list");
        request.addProperty("dt_change_from", String.valueOf(from));
        request.addProperty("dt_change_to", String.valueOf(nowSeconds));

        JsonObject response = executeJsonRequest("plan.pause_list", request);
        if (!isStateSuccessful(response)) {
            throw new IllegalStateException(serverError(response));
        }

        List<WPDataPauseSDB> pauses = parsePauseList(response);
        SQL_DB.wpDataPauseDao().insertDownloadedSync(pauses);
        logInfo("pause_list", "downloaded rows count=" + pauses.size());

        return pauses.size();
    }

    private List<WPDataPauseSDB> filterValidUploadRows(List<WPDataPauseSDB> items) {
        List<WPDataPauseSDB> result = new ArrayList<>();
        for (WPDataPauseSDB item : items) {
            if (item != null && item.codeDad2 > 0L && item.dtStart > 0L) {
                result.add(item);
            }
        }
        return result;
    }

    private JsonArray buildUploadRows(List<WPDataPauseSDB> items, long nowSeconds) {
        JsonArray rows = new JsonArray();
        for (WPDataPauseSDB item : items) {
            JsonObject row = new JsonObject();
            row.addProperty("code_dad2", item.codeDad2);
            row.addProperty("dt_start", item.dtStart);
            row.addProperty("dt_end", item.dtEnd);
            row.addProperty("dt_update_client", item.dtUpdateClient > 0L ? item.dtUpdateClient : nowSeconds);
            rows.add(row);
        }
        return rows;
    }

    private List<WPDataPauseSDB> parsePauseList(JsonObject response) {
        List<WPDataPauseSDB> result = new ArrayList<>();
        JsonElement listElement = firstDataElement(response);
        if (listElement == null || listElement.isJsonNull()) return result;

        if (listElement.isJsonArray()) {
            for (JsonElement element : listElement.getAsJsonArray()) {
                WPDataPauseSDB item = parsePause(element);
                if (item != null) result.add(item);
            }
        } else if (listElement.isJsonObject()) {
            WPDataPauseSDB item = parsePause(listElement);
            if (item != null) result.add(item);
        }

        return result;
    }

    private JsonElement firstDataElement(JsonObject response) {
        if (response == null) return null;

        JsonElement list = response.get("list");
        if (list != null && !list.isJsonNull()) return list;

        JsonElement data = response.get("data");
        if (data != null && !data.isJsonNull()) return data;

        return null;
    }

    private WPDataPauseSDB parsePause(JsonElement element) {
        try {
            WPDataPauseSDB item = gson.fromJson(element, WPDataPauseSDB.class);
            if (item == null || item.codeDad2 <= 0L || item.dtStart <= 0L) return null;
            item.uploadStatus = 0;
            return item;
        } catch (Exception e) {
            logError("pause_list.parse", messageOf(e));
            return null;
        }
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

            logInfo(requestName, "response=" + body);
            return body;
        });
    }

    private SynchronizationTimeTable ensureSyncInfo() {
        SynchronizationTimeTable syncInfo = SQL_DB.synchronizationTimetableDao()
                .getByTableName(TableName.WP_DATA_PAUSE);

        if (syncInfo != null) return syncInfo;

        SynchronizationTimeTable created = new SynchronizationTimeTable(
                TableName.WP_DATA_PAUSE.ordinal(),
                TableName.WP_DATA_PAUSE,
                DEFAULT_SYNC_PERIOD_SECONDS,
                0L,
                0L,
                0,
                0,
                "Auto-generated",
                true,
                DownloadStatus.SUCCESS,
                DownloadStatus.SUCCESS
        );
        SQL_DB.synchronizationTimetableDao().insert(created);

        return created;
    }

    private void saveSyncInfo(SynchronizationTimeTable current,
                              long lastDownloadTime,
                              long lastUploadTime,
                              int downloadedItems,
                              int uploadedItems,
                              DownloadStatus downloadStatus,
                              DownloadStatus uploadStatus) {
        SynchronizationTimeTable updated = new SynchronizationTimeTable(
                current.getId(),
                current.getTableName(),
                current.getSyncPeriodSeconds(),
                lastDownloadTime,
                lastUploadTime,
                downloadedItems,
                uploadedItems,
                current.getDescription(),
                current.isUserGenerated(),
                downloadStatus,
                uploadStatus
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
        if (error != null && !error.trim().isEmpty()) return error;

        String message = optString(response, "message");
        if (message != null && !message.trim().isEmpty()) return message;

        return "server state=false";
    }

    private String optString(JsonObject response, String key) {
        if (response == null || !response.has(key) || response.get(key).isJsonNull()) return null;

        try {
            return response.get(key).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000L;
    }

    private String messageOf(Throwable throwable) {
        return throwable.getMessage() != null ? throwable.getMessage() : throwable.toString();
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

    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
