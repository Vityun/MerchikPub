package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.data.Database.Room.ResultsDynamicSDB;
import ua.com.merchik.merchik.data.SynchronizationTimeTable;
import ua.com.merchik.merchik.data.synchronization.DownloadStatus;
import ua.com.merchik.merchik.data.synchronization.SynchronizationTimetableRepository;
import ua.com.merchik.merchik.data.synchronization.TableName;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class ResultsDynamicExchange {
    private static final String TAG = "ResultsDynamicExchange";
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    public void syncInCron() {
        AppDatabase db = RoomManager.SQL_DB;
        int userId = Globals.getCurrentUserId();
        String sessionId = Globals.getCurrentSessionId();
        if (db == null || userId <= 0 || sessionId == null || sessionId.isEmpty()) return;
        if (!RUNNING.compareAndSet(false, true)) {
            log("INFO", "skip: previous request is still running");
            return;
        }
        try {
            Completable.fromAction(() -> downloadSnapshot(db, userId, sessionId))
                    .subscribeOn(Schedulers.io())
                    .doFinally(() -> RUNNING.set(false))
                    .subscribe(() -> { }, error -> log("ERROR", "snapshot kept: " + error));
        } catch (Exception error) {
            RUNNING.set(false);
            log("ERROR", "cannot start download: " + error);
        }
    }

    private void downloadSnapshot(AppDatabase db, int userId, String sessionId) throws Exception {
        requireCurrentAccount(db, userId, sessionId);
        SynchronizationTimetableRepository.ensureDefaults();
        try {
            log("INFO", "start: userId=" + userId + ", full snapshot, no VPI");
            Response<JsonObject> response = RetrofitBuilder.getRetrofitInterface()
                    .TEST_JSON_UPLOAD(RetrofitBuilder.contentType, ResultsDynamicSnapshot.request())
                    .execute();
            if (!response.isSuccessful()) {
                throw new IllegalStateException("HTTP " + response.code());
            }
            List<ResultsDynamicSDB> rows = ResultsDynamicSnapshot.parse(response.body());
            requireCurrentAccount(db, userId, sessionId);
            db.runInTransaction(() -> {
                requireCurrentAccount(db, userId, sessionId);
                db.resultsDynamicDao().replaceAll(rows);
                // This is a local download timestamp, never a server change watermark.
                saveStatus(db, DownloadStatus.SUCCESS, rows.size());
                requireCurrentAccount(db, userId, sessionId);
            });
            log("INFO", "saved: userId=" + userId + ", rows=" + rows.size());
        } catch (Exception error) {
            if (isCurrentAccount(db, userId, sessionId)) {
                try {
                    db.runInTransaction(() -> {
                        requireCurrentAccount(db, userId, sessionId);
                        saveStatus(db, DownloadStatus.ERROR, 0);
                        requireCurrentAccount(db, userId, sessionId);
                    });
                } catch (Exception statusError) {
                    log("WARNING", "cannot save download status: " + statusError);
                }
            }
            throw error;
        }
    }

    private void saveStatus(AppDatabase db, DownloadStatus status, int rows) {
        SynchronizationTimeTable current = db.synchronizationTimetableDao()
                .getByTableName(TableName.PLAN_RESULTS_DYNAMIC);
        if (current == null) throw new IllegalStateException("Missing results_dynamic sync settings");
        boolean success = status == DownloadStatus.SUCCESS;
        db.synchronizationTimetableDao().insert(new SynchronizationTimeTable(
                current.getId(), current.getTableName(), current.getSyncPeriodSeconds(),
                success ? TrustedTime.nowServerSecOrLocalSec() : current.getLastDownloadTime(),
                current.getLastUploadTime(), success ? rows : current.getDownloadedItems(),
                current.getUploadedItems(), current.getDescription(), current.isUserGenerated(),
                status, current.getLastUploadStatus()));
    }

    private boolean isCurrentAccount(AppDatabase db, int userId, String sessionId) {
        return db == RoomManager.SQL_DB && userId == Globals.getCurrentUserId()
                && Objects.equals(sessionId, Globals.getCurrentSessionId());
    }

    private void requireCurrentAccount(AppDatabase db, int userId, String sessionId) {
        if (!isCurrentAccount(db, userId, sessionId)) {
            throw new IllegalStateException("Discarded response: account/session/database changed");
        }
    }

    private static void log(String level, String message) {
        Globals.writeToMLOG(level, TAG, message);
    }
}
