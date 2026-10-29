package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import ua.com.merchik.merchik.ServerExchange.ExchangeWork;

import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RetrofitResponse.Location.LocationList;
import ua.com.merchik.merchik.data.RetrofitResponse.Location.LocationResponse;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class LocationExchange {
    private static final ReferenceSyncPolicy.Table TABLE = ReferenceSyncPolicy.Table.LOCATIONS;
    // Accessed on the main thread, like the existing local LogMP writers.
    private static boolean inFlight;

    public void downloadLocationTable(ExchangeInterface.ExchangeResponseInterface exchange) {
        ExchangeWork.dispatch(
                action -> AndroidSchedulers.mainThread().scheduleDirect(action), () -> start(exchange));
    }

    private void start(ExchangeInterface.ExchangeResponseInterface exchange) {
        if (inFlight) {
            log("INFO", "skip: download already in flight");
            return;
        }
        inFlight = true;
        RequestContext context = new RequestContext();
        try {
            context.requireCurrent();
            String scope = FilteredSyncPolicy.accountScope(context.userId, "all");
            SynchronizationCursorSDB cursor = context.db.synchronizationCursorDao().get(TABLE.resource(), scope);
            Long started = TrustedTime.nowServerSecOrLocalSec();
            long count;
            try (Realm realm = Realm.getDefaultInstance()) {
                count = serverRowCount(realm);
            }
            long from = FilteredSyncPolicy.requestWatermark(cursor, started,
                    LocationDownloadPolicy.cacheReady(cursor, count));
            JsonObject request = ReferenceSyncPolicy.request(TABLE, from);
            log("INFO", "start: " + request);
            RetrofitBuilder.getRetrofitInterface().LOCATION_RESPONSE_CALL(RetrofitBuilder.contentType, request)
                    .enqueue(new Callback<LocationResponse>() {
                        @Override public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                            try {
                                context.requireCurrent();
                                if (!response.isSuccessful() || response.body() == null) {
                                    if (response.errorBody() != null) response.errorBody().close();
                                    throw new IllegalStateException("location/list HTTP " + response.code());
                                }
                                LocationResponse body = response.body();
                                List<LocationList> rows = ReferenceSyncPolicy.rows(body.state, body.error, body.list, row -> row.id);
                                persist(context, rows, scope, from, started);
                                notifyCallback(context, exchange, rows, null);
                            } catch (Exception error) {
                                fail(context, exchange, error);
                            } finally {
                                inFlight = false;
                            }
                        }

                        @Override public void onFailure(Call<LocationResponse> call, Throwable error) {
                            try {
                                fail(context, exchange, error);
                            } finally {
                                inFlight = false;
                            }
                        }
                    });
        } catch (Exception error) {
            inFlight = false;
            fail(context, exchange, error);
        }
    }

    private static void persist(RequestContext context, List<LocationList> rows, String scope,
                                long from, Long started) {
        SynchronizationCursorSDB saved = FilteredSyncPolicy.savedCursor(TABLE.resource(), scope, from, started, 0);
        try (Realm realm = Realm.getDefaultInstance()) {
            if (!rows.isEmpty()) {
                realm.executeTransaction(transaction -> {
                    context.requireCurrent();
                    Set<Long> serverIds = new HashSet<>();
                    for (LogMPDB row : transaction.where(LogMPDB.class).greaterThan("serverId", 0L).findAll()) {
                        serverIds.add(row.serverId);
                    }
                    Number lastId = transaction.where(LogMPDB.class).max("id");
                    List<LogMPDB> additions = LocationDownloadPolicy.missingRows(rows, serverIds,
                            lastId == null ? 0 : lastId.intValue(), System.currentTimeMillis() / 1000L);
                    // Do not replace existing records: their visit links and local coordinates take precedence.
                    transaction.copyToRealm(additions);
                    context.requireCurrent();
                });
            }
            saved.itemCount = serverRowCount(realm);
        }
        // Realm is committed first. If saving the cursor fails, the delta is replayed safely.
        context.db.runInTransaction(() -> {
            context.requireCurrent();
            context.db.synchronizationCursorDao().save(saved);
            context.requireCurrent();
        });
        log("INFO", "saved: received=" + rows.size() + ", serverRows=" + saved.itemCount + ", vpi=" + saved.vpi);
    }

    private static long serverRowCount(Realm realm) {
        return realm.where(LogMPDB.class).greaterThan("serverId", 0L).count();
    }

    private static void fail(RequestContext context, ExchangeInterface.ExchangeResponseInterface exchange, Throwable error) {
        log("ERROR", "VPI unchanged: " + error);
        notifyCallback(context, exchange, null, error);
    }

    private static void notifyCallback(RequestContext context, ExchangeInterface.ExchangeResponseInterface exchange,
                               List<LocationList> rows, Throwable error) {
        if (exchange == null || !context.isCurrent()) return;
        try {
            if (error == null) exchange.onSuccess(rows);
            else exchange.onFailure(error.toString());
        } catch (Exception callbackError) {
            log("ERROR", "callback failed: " + callbackError);
        }
    }

    private static void log(String level, String message) {
        Globals.writeToMLOG(level, "LocationExchange", message);
    }

    private static final class RequestContext {
        final AppDatabase db = RoomManager.SQL_DB;
        final int userId = Globals.getCurrentUserId();
        final String sessionId = Globals.getCurrentSessionId();

        boolean isCurrent() {
            return db != null && db == RoomManager.SQL_DB && userId == Globals.getCurrentUserId()
                    && Objects.equals(sessionId, Globals.getCurrentSessionId());
        }

        void requireCurrent() {
            if (!isCurrent()) throw new IllegalStateException("Account/session/database changed");
        }
    }
}
