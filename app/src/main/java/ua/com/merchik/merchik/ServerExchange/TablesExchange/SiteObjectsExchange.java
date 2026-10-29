package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import ua.com.merchik.merchik.ServerExchange.ExchangeWork;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.StartupDownloadReuse;
import ua.com.merchik.merchik.ServerExchange.SyncExchangeTrace;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsLocalDefaults;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SiteObjectsResponse;
import ua.com.merchik.merchik.data.SynchronizationTimeTable;
import ua.com.merchik.merchik.data.synchronization.DownloadStatus;
import ua.com.merchik.merchik.data.synchronization.SynchronizationTimetableRepository;
import ua.com.merchik.merchik.data.synchronization.TableName;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class SiteObjectsExchange {
    private static final Scheduler SERIAL = Schedulers.from(Executors.newSingleThreadExecutor());
    private static final Map<RequestContext, Single<String>> IN_FLIGHT = new HashMap<>();
    private static final StartupDownloadReuse<RequestContext> STARTUP = new StartupDownloadReuse<>();

    public static void invalidateStartupReuse() { STARTUP.clear(); }

    public void downloadStartup(int langId, Exchange.ExchangeInt callback) {
        RequestContext context = new RequestContext(langId);
        long ticket = STARTUP.begin(context);
        downloadSiteObjects(langId, new Exchange.ExchangeInt() {
            @Override public void onSuccess(String message) {
                if (context.isCurrent()) STARTUP.succeeded(ticket, System.nanoTime() / 1_000_000L);
                if (callback != null) callback.onSuccess(message);
            }
            @Override public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    public void downloadForRegularExchange(Exchange.ExchangeInt callback) {
        RequestContext context = new RequestContext(Globals.langId);
        if (STARTUP.consume(context, System.nanoTime() / 1_000_000L)
                && context.isCurrent() && roomReady(context.db, Integer.toString(context.langId))
                && SiteObjectsLocalDefaults.hasDownloadedObjects()) {
            log("INFO", "reuse successful startup download: userId=" + context.userId + ", langId=" + context.langId);
            notifyCallback(context, callback, "site_objects: already saved during startup", null);
            return;
        }
        downloadSiteObjects(context.langId, callback);
    }

    public void downloadSiteObjects(Exchange.ExchangeInt callback) {
        downloadSiteObjects(Globals.langId, callback);
    }

    public void downloadSiteObjects(int langId, Exchange.ExchangeInt callback) {
        RequestContext context = new RequestContext(langId);
        Single<String> request;
        synchronized (IN_FLIGHT) {
            request = IN_FLIGHT.get(context);
            if (request == null) {
                request = Single.fromCallable(() -> download(context))
                        .subscribeOn(SERIAL)
                        .doFinally(() -> {
                            synchronized (IN_FLIGHT) {
                                IN_FLIGHT.remove(context);
                            }
                        }).cache();
                IN_FLIGHT.put(context, request);
            } else {
                log("INFO", "join: langId=" + langId + ", userId=" + context.userId);
            }
        }
        request.observeOn(AndroidSchedulers.mainThread()).compose(ExchangeWork.track()).subscribe(
                message -> notifyCallback(context, callback, message, null),
                error -> notifyCallback(context, callback, null, error));
    }

    private static String download(RequestContext context) throws Exception {
        SyncExchangeTrace trace = null;
        try {
            context.requireCurrent();
            SynchronizationTimetableRepository.ensureDefaults();
            AppDatabase db = context.db;
            SynchronizationTimeTable sync = db.synchronizationTimetableDao().getByTableName(TableName.SITE_OBJECTS);
            if (sync == null) throw new IllegalStateException("Missing site_objects sync settings");
            try (Realm realm = Realm.getDefaultInstance()) {
                String language = Integer.toString(context.langId);
                Long started = TrustedTime.nowServerSecOrLocalSec();
                long from = SiteObjectsSyncPolicy.requestWatermark(sync.getLastDownloadTime(), started,
                        roomReady(db, language), SiteObjectsLocalDefaults.hasDownloadedLanguage(realm, language));
                JsonObject request = SiteObjectsSyncPolicy.request(context.langId, from);
                log("INFO", "start: userId=" + context.userId + ", request=" + request);
                trace = new SyncExchangeTrace("site_objects/list", from);
                Response<SiteObjectsResponse> response = RetrofitBuilder.getRetrofitInterface()
                        .GET_SITE_OBJECTS_R(RetrofitBuilder.contentType, request).execute();
                if (!response.isSuccessful()) {
                    if (response.errorBody() != null) response.errorBody().close();
                    throw new IllegalStateException("site_objects HTTP " + response.code());
                }
                SiteObjectsResponse body = response.body();
                trace.response(body == null ? null : body.state, body == null ? null : body.error,
                        body == null ? null : body.objectSQLList);
                List<SiteObjectsSDB> rows = SiteObjectsSyncPolicy.rows(body, context.langId, from == 0);
                List<SiteObjectsDB> realmRows = SiteObjectsSyncPolicy.realmRows(rows);
                context.requireCurrent();
                realm.refresh();
                if (from > 0 && (!roomReady(db, language)
                        || !SiteObjectsLocalDefaults.hasDownloadedLanguage(realm, language))) {
                    throw new IllegalStateException("Dictionary changed during download; full retry required");
                }

                // Realm commits first. If Room fails, VPI stays unchanged and the same delta is replayed.
                realm.executeTransaction(transaction -> {
                    context.requireCurrent();
                    if (from == 0) transaction.delete(SiteObjectsDB.class);
                    transaction.copyToRealmOrUpdate(realmRows);
                    SiteObjectsLocalDefaults.ensureDurationObjects(transaction, context.langId);
                    context.requireCurrent();
                });
                long watermark = SiteObjectsSyncPolicy.savedWatermark(from, started);
                db.runInTransaction(() -> {
                    context.requireCurrent();
                    if (from == 0) db.siteObjectsDao().deleteAll();
                    db.siteObjectsDao().upsertAll(rows);
                    db.initStateDao().markSiteLoaded();
                    saveWatermark(db, watermark, rows.size());
                    context.requireCurrent();
                });
                log("INFO", "saved: langId=" + context.langId + ", rows=" + rows.size()
                        + ", realm=true, room=true, vpi=" + watermark + ", full=" + (from == 0));
                trace.saved(watermark);
                return "site_objects: " + rows.size() + " (Room + Realm)";
            }
        } catch (Exception error) {
            if (trace != null) trace.failed(error);
            log("ERROR", "failed: langId=" + context.langId + ", VPI unchanged: " + error);
            throw error;
        }
    }

    private static boolean roomReady(AppDatabase db, String language) {
        long count = db.siteObjectsDao().count();
        return count > 0 && db.siteObjectsDao().countForLanguage(language) == count;
    }

    private static void saveWatermark(AppDatabase db, long watermark, int count) {
        SynchronizationTimeTable current = db.synchronizationTimetableDao().getByTableName(TableName.SITE_OBJECTS);
        if (current == null) throw new IllegalStateException("Missing site_objects sync settings");
        db.synchronizationTimetableDao().insert(new SynchronizationTimeTable(
                current.getId(), current.getTableName(), current.getSyncPeriodSeconds(), watermark,
                current.getLastUploadTime(), count, current.getUploadedItems(), current.getDescription(),
                current.isUserGenerated(), DownloadStatus.SUCCESS, current.getLastUploadStatus()));
    }

    private static void notifyCallback(RequestContext context, Exchange.ExchangeInt callback,
                                       String message, Throwable error) {
        // A stale language selection must not roll the UI back through its onFailure callback.
        if (callback == null || !context.isCurrent()) return;
        try {
            if (error == null) callback.onSuccess(message);
            else callback.onFailure(error.toString());
        } catch (Exception callbackError) {
            log("ERROR", "callback failed: " + callbackError);
        }
    }

    private static void log(String level, String message) {
        Globals.writeToMLOG(level, "SiteObjectsExchange", message);
    }

    private static final class RequestContext {
        final AppDatabase db = RoomManager.SQL_DB;
        final int userId = Globals.getCurrentUserId();
        final String sessionId = Globals.getCurrentSessionId();
        final int langId;

        RequestContext(int langId) { this.langId = langId; }

        boolean isCurrent() {
            return db != null && db == RoomManager.SQL_DB && userId == Globals.getCurrentUserId()
                    && Objects.equals(sessionId, Globals.getCurrentSessionId()) && langId == Globals.langId;
        }

        void requireCurrent() {
            if (!isCurrent()) throw new IllegalStateException("Account/database/language changed");
        }

        @Override public boolean equals(Object other) {
            if (!(other instanceof RequestContext)) return false;
            RequestContext that = (RequestContext) other;
            return db == that.db && userId == that.userId && langId == that.langId
                    && Objects.equals(sessionId, that.sessionId);
        }

        @Override public int hashCode() {
            return Objects.hash(System.identityHashCode(db), userId, langId, sessionId);
        }
    }
}
