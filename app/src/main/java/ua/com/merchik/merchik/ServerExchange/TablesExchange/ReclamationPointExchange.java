package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.ExchangeWork;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.TasksAndReclamationsSDBResponce;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class ReclamationPointExchange {
    private static final AtomicBoolean RUNNING = new AtomicBoolean();

    /** The callback observes already persisted rows; it must not insert them again. */
    public void downloadTaR(ExchangeInterface.ExchangeResponseInterface exchange) {
        if (!RUNNING.compareAndSet(false, true)) return;
        RequestContext context = new RequestContext();
        Single.fromCallable(() -> download(context))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> RUNNING.set(false))
                .compose(ExchangeWork.track())
                .subscribe(rows -> notifyCallback(context, exchange, rows, null), error -> {
                    log("ERROR", "download failed; VPI unchanged: " + error);
                    notifyCallback(context, exchange, null, error);
                });
    }

    private List<TasksAndReclamationsSDB> download(RequestContext context) throws Exception {
        context.requireCurrent();
        Long started = TrustedTime.nowServerSecOrLocalSec();
        String scope = FilteredSyncPolicy.accountScope(context.userId, "all");
        SynchronizationCursorSDB cursor = context.db.synchronizationCursorDao().get(ReclamationSyncPolicy.RESOURCE, scope);
        long from = FilteredSyncPolicy.requestWatermark(cursor, started,
                cursor != null && cursor.itemCount == context.db.tarDao().count());
        JsonObject request = ReclamationSyncPolicy.request(from);
        log("INFO", "request: " + request + ", userId=" + context.userId);
        Response<TasksAndReclamationsSDBResponce> response = RetrofitBuilder.getRetrofitInterface()
                .GET_TABLE_TasksAndReclamationsSDB(RetrofitBuilder.contentType, request).execute();
        if (!response.isSuccessful() || response.body() == null) {
            if (response.errorBody() != null) response.errorBody().close();
            throw new IllegalStateException("reclamation/list HTTP " + response.code());
        }
        TasksAndReclamationsSDBResponce body = response.body();
        List<TasksAndReclamationsSDB> rows = ReferenceSyncPolicy.rows(body.state, body.error, body.list, row -> row.id);
        SynchronizationCursorSDB saved = FilteredSyncPolicy.savedCursor(
                ReclamationSyncPolicy.RESOURCE, scope, from, started, 0, rows.size());
        context.requireCurrent();
        if (saved == null) {
            log("INFO", "empty delta; VPI unchanged, userId=" + context.userId);
            return rows;
        }
        List<TasksAndReclamationsSDB> merged = new ArrayList<>(rows.size());
        context.db.runInTransaction(() -> {
            context.requireCurrent();
            for (TasksAndReclamationsSDB row : rows) {
                merged.add(ReclamationSyncPolicy.merge(row, context.db.tarDao().getById(row.id), started));
            }
            context.db.tarDao().insertSync(merged);
            saved.itemCount = context.db.tarDao().count();
            context.db.synchronizationCursorDao().save(saved);
            context.requireCurrent();
        });
        log("INFO", "saved: received=" + rows.size() + ", scope=" + scope + ", vpi=" + saved.vpi);
        return merged;
    }

    private static void notifyCallback(RequestContext context, ExchangeInterface.ExchangeResponseInterface exchange,
                                       List<TasksAndReclamationsSDB> rows, Throwable error) {
        if (exchange == null || !context.isCurrent()) return;
        try {
            if (error == null) exchange.onSuccess(rows);
            else exchange.onFailure(error.toString());
        } catch (Exception callbackError) {
            log("ERROR", "callback failed: " + callbackError);
        }
    }

    private static void log(String level, String message) {
        Globals.writeToMLOG(level, "ReclamationPointExchange/postingHistory", message);
    }

    private static final class RequestContext {
        final AppDatabase db = RoomManager.SQL_DB;
        final int userId = Globals.getCurrentUserId();
        final String session = Globals.getCurrentSessionId();

        boolean isCurrent() {
            return db != null && db == RoomManager.SQL_DB && userId > 0
                    && userId == Globals.getCurrentUserId() && Objects.equals(session, Globals.getCurrentSessionId());
        }

        void requireCurrent() {
            if (!isCurrent()) throw new IllegalStateException("Account/session/database changed");
        }
    }
}
