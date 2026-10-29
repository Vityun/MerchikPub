package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import ua.com.merchik.merchik.ServerExchange.ExchangeWork;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Objects;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.ServerExchange.SyncResponsePolicy;
import ua.com.merchik.merchik.ServerExchange.SyncExchangeTrace;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatGrpSDB;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ChatResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ChatGrp.ChatGrpResponse;
import ua.com.merchik.merchik.data.SynchronizationTimeTable;
import ua.com.merchik.merchik.data.synchronization.ChatSyncPolicy;
import ua.com.merchik.merchik.data.synchronization.DownloadStatus;
import ua.com.merchik.merchik.data.synchronization.SynchronizationTimetableRepository;
import ua.com.merchik.merchik.data.synchronization.TableName;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public final class ChatExchange {
    private static final Object MESSAGES_LOCK = new Object();
    private static final Object GROUPS_LOCK = new Object();
    private static final Map<String, Completable> IN_FLIGHT = new HashMap<>();
    private volatile Call<?> activeCall;
    private volatile boolean cancelled;

    public static void downloadForExchange(TableName table) {
        int userId = Globals.getCurrentUserId();
        if (userId <= 0) return;
        String key = userId + ":" + table.getCode();
        Completable task;
        synchronized (IN_FLIGHT) {
            task = IN_FLIGHT.get(key);
            if (task == null) {
                task = Completable.fromAction(() -> new ChatExchange().download(table, userId))
                        .subscribeOn(Schedulers.io())
                        .doOnTerminate(() -> { synchronized (IN_FLIGHT) { IN_FLIGHT.remove(key); } })
                        .cache();
                IN_FLIGHT.put(key, task);
            }
        }
        task.compose(ExchangeWork.track())
                .subscribe(() -> { }, error -> Globals.writeToMLOG("ERROR", "ChatExchange/failed",
                        "table=" + table.getCode() + ", VPI unchanged: " + error));
    }

    public void download(TableName table, int userId) throws Exception {
        ChatSyncPolicy.action(table);
        // A notification worker and a full exchange must not commit competing cursor snapshots.
        synchronized (table == TableName.CHAT ? MESSAGES_LOCK : GROUPS_LOCK) {
            downloadLocked(table, userId);
        }
    }

    private void downloadLocked(TableName table, int userId) throws Exception {
        AppDatabase db = RoomManager.SQL_DB;
        String session = Globals.getCurrentSessionId();
        requireCurrentAccount(db, userId, session);
        SynchronizationTimetableRepository.ensureDefaults();
        SynchronizationTimeTable sync = db.synchronizationTimetableDao().getByTableName(table);
        if (sync == null) throw new IllegalStateException("Missing chat sync settings");
        Long started = TrustedTime.nowServerSecOrLocalSec();
        long from = ChatSyncPolicy.requestWatermark(sync.getLastDownloadTime(), started);
        JsonObject request = ChatSyncPolicy.request(table, from);
        Globals.writeToMLOG("INFO", "ChatExchange/start", "userId=" + userId + ", request=" + request);
        SyncExchangeTrace trace = new SyncExchangeTrace("chat/" + ChatSyncPolicy.action(table), from);
        try {
            Runnable saveRows;
            int count;
            if (table == TableName.CHAT) {
                ChatResponse body = execute(RetrofitBuilder.getRetrofitInterface()
                        .GET_TABLE_CHAT(RetrofitBuilder.contentType, request));
                trace.response(body.state, body.error, body.list);
                List<ChatSDB> rows = ChatSyncPolicy.requireSuccessfulList(body.state, body.error, body.list);
                for (ChatSDB row : rows) {
                    if (row.id == null || row.id <= 0 || row.chatId == null || row.chatId <= 0) {
                        throw new IllegalStateException("Invalid chat message ID");
                    }
                }
                count = rows.size();
                saveRows = () -> db.chatDao().insertAll(rows);
            } else {
                ChatGrpResponse body = execute(RetrofitBuilder.getRetrofitInterface()
                        .CHAT_GRP_DOWNLOAD(RetrofitBuilder.contentType, request));
                trace.response(body.state, body.error, body.list);
                List<ChatGrpSDB> rows = ChatSyncPolicy.requireSuccessfulList(body.state, body.error, body.list);
                for (ChatGrpSDB row : rows) {
                    if (row.id == null || row.id <= 0) throw new IllegalStateException("Invalid chat group ID");
                }
                count = rows.size();
                saveRows = () -> db.chatGrpDao().insertAll(rows);
            }
            long watermark = ChatSyncPolicy.savedWatermark(from, started);
            db.runInTransaction(() -> {
                requireCurrentAccount(db, userId, session);
                saveRows.run();
                SynchronizationTimeTable current = db.synchronizationTimetableDao().getByTableName(table);
                if (current == null) throw new IllegalStateException("Missing chat sync settings");
                db.synchronizationTimetableDao().insert(new SynchronizationTimeTable(
                        current.getId(), table, current.getSyncPeriodSeconds(), watermark,
                        current.getLastUploadTime(), count, current.getUploadedItems(),
                        current.getDescription(), current.isUserGenerated(),
                        DownloadStatus.SUCCESS, current.getLastUploadStatus()));
                requireCurrentAccount(db, userId, session);
            });
            Globals.writeToMLOG("INFO", "ChatExchange/saved", "table=" + table.getCode()
                    + ", rows=" + count + ", vpi=" + watermark + ", requestStartedSec=" + started);
            trace.saved(watermark);
        } catch (Exception error) {
            trace.failed(error);
            throw error;
        }
    }

    private <T> T execute(Call<T> call) throws Exception {
        activeCall = call;
        if (cancelled) call.cancel();
        Response<T> response = call.execute();
        if (!response.isSuccessful()) {
            if (response.errorBody() != null) response.errorBody().close();
            throw new SyncResponsePolicy.HttpFailure(response.code());
        }
        if (response.body() == null) throw new IllegalStateException("Empty chat response");
        return response.body();
    }

    private void requireCurrentAccount(AppDatabase db, int userId, String session) {
        if (cancelled || db == null || db != RoomManager.SQL_DB || userId <= 0
                || userId != Globals.getCurrentUserId() || session == null || session.isEmpty()
                || !Objects.equals(session, Globals.getCurrentSessionId())) {
            throw new IllegalStateException("Chat download stopped: account/session/database changed");
        }
    }

    public void cancel() {
        cancelled = true;
        Call<?> call = activeCall;
        if (call != null) call.cancel();
    }
}
