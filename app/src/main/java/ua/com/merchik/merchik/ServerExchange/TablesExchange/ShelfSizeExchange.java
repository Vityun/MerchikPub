package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import ua.com.merchik.merchik.ServerExchange.ExchangeWork;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.ServerExchange.SyncExchangeTrace;
import ua.com.merchik.merchik.data.Database.Room.ShelfSizeSDB;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ShelfSizeResponse;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class ShelfSizeExchange {
    private static final AtomicBoolean RUNNING = new AtomicBoolean();

    public void downloadShelfSize() {
        if (!RUNNING.compareAndSet(false, true)) return;
        AppDatabase db = RoomManager.SQL_DB;
        int userId = Globals.getCurrentUserId();
        String session = Globals.getCurrentSessionId();
        Completable.fromAction(() -> download(db, userId, session))
                .subscribeOn(Schedulers.io())
                .doFinally(() -> RUNNING.set(false))
                .compose(ExchangeWork.track())
                .subscribe(() -> { }, error -> Globals.writeToMLOG(
                        "ERROR", "ShelfSizeExchange", "VPI unchanged: " + error));
    }

    private void download(AppDatabase db, int userId, String session) throws Exception {
        requireCurrent(db, userId, session);
        Long started = TrustedTime.nowServerSecOrLocalSec();
        String scope = FilteredSyncPolicy.shelfScope(userId);
        SynchronizationCursorSDB cursor = db.synchronizationCursorDao().get(FilteredSyncPolicy.SHELF_SIZE, scope);
        long from = FilteredSyncPolicy.requestWatermark(cursor, started,
                cursor != null && cursor.itemCount == db.shelfSizeDao().count());
        JsonObject request = FilteredSyncPolicy.shelfRequest(from);
        Globals.writeToMLOG("INFO", "ShelfSizeExchange/start", request.toString());
        SyncExchangeTrace trace = new SyncExchangeTrace(FilteredSyncPolicy.SHELF_SIZE, from);
        try {
            Response<ShelfSizeResponse> response = RetrofitBuilder.getRetrofitInterface()
                    .ShelfSize_DOWNLOAD_TABLE(RetrofitBuilder.contentType, request).execute();
            if (!response.isSuccessful()) {
                if (response.errorBody() != null) response.errorBody().close();
                throw new IllegalStateException("shelf_size HTTP " + response.code());
            }
            ShelfSizeResponse body = response.body();
            trace.response(body == null ? null : body.state, body == null ? null : body.error,
                    body == null ? null : body.list);
            List<ShelfSizeSDB> rows = FilteredSyncPolicy.shelfRows(body);
            SynchronizationCursorSDB saved = FilteredSyncPolicy.savedCursor(
                    FilteredSyncPolicy.SHELF_SIZE, scope, from, started, 0, rows.size());
            if (saved == null) {
                trace.unchanged();
                return;
            }
            db.runInTransaction(() -> {
                requireCurrent(db, userId, session);
                db.shelfSizeDao().upsertAll(rows);
                saved.itemCount = db.shelfSizeDao().count();
                db.synchronizationCursorDao().save(saved);
                requireCurrent(db, userId, session);
            });
            Globals.writeToMLOG("INFO", "ShelfSizeExchange/saved", "scope=" + scope
                    + ", rows=" + rows.size() + ", vpi="
                    + saved.vpi);
            trace.saved(saved.vpi);
        } catch (Exception error) {
            trace.failed(error);
            throw error;
        }
    }

    private void requireCurrent(AppDatabase db, int userId, String session) {
        if (db == null || db != RoomManager.SQL_DB || userId != Globals.getCurrentUserId()
                || !Objects.equals(session, Globals.getCurrentSessionId())) {
            throw new IllegalStateException("Account/session/database changed");
        }
    }
}
