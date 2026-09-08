package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.LogListResponse;
import ua.com.merchik.merchik.data.SynchronizationTimeTable;
import ua.com.merchik.merchik.data.synchronization.DownloadStatus;
import ua.com.merchik.merchik.data.synchronization.SynchronizationTimetableRepository;
import ua.com.merchik.merchik.data.synchronization.TableName;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class LogExchange {

    private static final String TAG = "LogExchange";
    private static final long SYNC_PERIOD_SECONDS = 600L;
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    public void syncInCron() {
        if (SQL_DB == null) {
            writeLog("ERROR", "skip: Room database is not initialized");
            return;
        }
        if (!RUNNING.compareAndSet(false, true)) return;

        Single.fromCallable(this::downloadSync)
                .subscribeOn(Schedulers.io())
                .doFinally(() -> RUNNING.set(false))
                .subscribe(
                        summary -> writeLog("INFO", summary),
                        throwable -> writeLog("ERROR", "failed; VPI unchanged: " + throwable)
                );
    }

    private String downloadSync() throws Exception {
        SynchronizationTimetableRepository.ensureDefaults();
        SynchronizationTimeTable syncInfo = SQL_DB.synchronizationTimetableDao()
                .getByTableName(TableName.LOG);
        if (syncInfo == null) {
            throw new IllegalStateException("log synchronization timetable is missing");
        }

        long previousVpi = Math.max(0L, syncInfo.getLastDownloadTime());
        if (previousVpi > 0 && !TrustedTime.isSyncDue(previousVpi, SYNC_PERIOD_SECONDS)) {
            return "skip: next download in "
                    + TrustedTime.secondsUntilSync(previousVpi, SYNC_PERIOD_SECONDS) + " sec";
        }

        // Advance only after the response is saved, using the time before the request.
        Long requestStartedSec = TrustedTime.nowServerSecOrNull();
        try {
            JsonObject request = new JsonObject();
            request.addProperty("mod", "log");
            request.addProperty("act", "list");
            request.addProperty("dt_change_from", String.valueOf(previousVpi));
            writeLog("INFO", "request=" + request);

            Response<LogListResponse> response = RetrofitBuilder.getRetrofitInterface()
                    .GET_LOG_LIST(RetrofitBuilder.contentType, request)
                    .execute();
            if (!response.isSuccessful()) {
                if (response.errorBody() != null) response.errorBody().close();
                throw new IllegalStateException("HTTP " + response.code() + " " + response.message());
            }

            LogListResponse body = response.body();
            if (body == null || !Boolean.TRUE.equals(body.state)) {
                throw new IllegalStateException(body == null ? "empty response" : "state="
                        + body.state + ", error=" + body.error);
            }
            if (body.error != null && !body.error.trim().isEmpty()) {
                throw new IllegalStateException(body.error);
            }
            if (body.list == null) {
                throw new IllegalStateException("response.list is missing");
            }

            saveRows(body.list);
            int savedRows = body.list.size();
            writeLog("INFO", "response: state=true, savedRows=" + savedRows);

            long nextVpi = requestStartedSec != null ? requestStartedSec : previousVpi;
            saveSyncInfo(syncInfo, nextVpi, savedRows, DownloadStatus.SUCCESS);
            if (requestStartedSec == null) {
                writeLog("WARN", "No trusted server time; keeping VPI=" + previousVpi);
            }
            return "saved=" + savedRows + ", VPI=" + previousVpi + " -> " + nextVpi;
        } catch (Exception e) {
            saveSyncInfo(syncInfo, previousVpi, syncInfo.getDownloadedItems(), DownloadStatus.ERROR);
            throw e;
        }
    }

    private void saveRows(List<LogDB> rows) {
        if (rows.isEmpty()) return;

        long receivedAtSec = TrustedTime.nowServerSecOrLocalSec();
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(transaction -> {
                for (LogDB row : rows) {
                    if (row == null || row.getId() == null || row.getId() <= 0) {
                        throw new IllegalStateException("log row has no valid id/ID");
                    }
                    LogDB local = transaction.where(LogDB.class).equalTo("id", row.getId()).findFirst();
                    if (local != null && local.getDt() == null) {
                        // Keep both the pending local record and the old VPI for a later retry.
                        throw new IllegalStateException("pending local log ID conflict: " + row.getId());
                    }
                    // A null dt puts a log in the upload queue. Downloaded logs are already on the server.
                    if (row.getDt() == null) row.setDt(receivedAtSec);
                    transaction.copyToRealmOrUpdate(row);
                }
            });
        }
    }

    private void saveSyncInfo(SynchronizationTimeTable current, long vpi,
                              int downloadedItems, DownloadStatus status) {
        SQL_DB.synchronizationTimetableDao().insert(new SynchronizationTimeTable(
                current.getId(),
                current.getTableName(),
                SYNC_PERIOD_SECONDS,
                vpi,
                current.getLastUploadTime(),
                downloadedItems,
                current.getUploadedItems(),
                current.getDescription(),
                current.isUserGenerated(),
                status,
                current.getLastUploadStatus()
        ));
    }

    private void writeLog(String level, String message) {
        Log.d(TAG, "log.list: " + message);
        Globals.writeToMLOG(level, TAG + "/log.list", message);
    }
}
