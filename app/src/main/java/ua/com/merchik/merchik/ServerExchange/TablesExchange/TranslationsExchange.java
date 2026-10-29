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
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.ServerExchange.SyncExchangeTrace;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.TranslatesResponse;
import ua.com.merchik.merchik.data.Translation.SiteTranslationsList;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class TranslationsExchange {
    private static final Scheduler SERIAL = Schedulers.from(Executors.newSingleThreadExecutor());
    private static final Map<RequestContext, Single<List<TranslatesSDB>>> IN_FLIGHT = new HashMap<>();

    public void downloadTranslations(ExchangeInterface.Translates callback) {
        downloadTranslations(Globals.langId, callback);
    }

    public void downloadTranslations(int langId, ExchangeInterface.Translates callback) {
        RequestContext context = new RequestContext(langId);
        Single<List<TranslatesSDB>> request;
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
                log("INFO", "join: langId=" + langId);
            }
        }
        request.observeOn(AndroidSchedulers.mainThread()).compose(ExchangeWork.track()).subscribe(
                rows -> notifyCallback(context, callback, rows, null),
                error -> notifyCallback(context, callback, null, error));
    }

    private static List<TranslatesSDB> download(RequestContext context) throws Exception {
        SyncExchangeTrace trace = null;
        try {
            context.requireCurrent();
            AppDatabase db = context.db;
            String language = FilteredSyncPolicy.translationScope(context.langId);
            String scope = FilteredSyncPolicy.accountScope(context.userId, language);
            SynchronizationCursorSDB cursor = db.synchronizationCursorDao().get(FilteredSyncPolicy.TRANSLATIONS, scope);
            try (Realm realm = Realm.getDefaultInstance()) {
                Long started = TrustedTime.nowServerSecOrLocalSec();
                long from = FilteredSyncPolicy.requestWatermark(cursor, started, ready(db, realm, language, cursor));
                JsonObject request = FilteredSyncPolicy.translationRequest(context.langId, from);
                log("INFO", "start: " + request);
                trace = new SyncExchangeTrace(FilteredSyncPolicy.TRANSLATIONS, from);
                Response<TranslatesResponse> response = RetrofitBuilder.getRetrofitInterface()
                        .GET_TRANSLATES_ROOM(RetrofitBuilder.contentType, request).execute();
                if (!response.isSuccessful()) {
                    if (response.errorBody() != null) response.errorBody().close();
                    throw new IllegalStateException("translation_list HTTP " + response.code());
                }
                TranslatesResponse body = response.body();
                trace.response(body == null ? null : body.state, body == null ? null : body.error,
                        body == null ? null : body.list);
                List<TranslatesSDB> rows = FilteredSyncPolicy.translationRows(body, context.langId);
                List<SiteTranslationsList> realmRows = FilteredSyncPolicy.realmTranslations(rows);
                context.requireCurrent();
                realm.refresh();
                if (from > 0 && !ready(db, realm, language, cursor)) {
                    throw new IllegalStateException("Translations changed during download; full retry required");
                }
                realm.executeTransaction(transaction -> {
                    context.requireCurrent();
                    if (from == 0) transaction.where(SiteTranslationsList.class).equalTo("langId", language).findAll().deleteAllFromRealm();
                    transaction.copyToRealmOrUpdate(realmRows);
                    context.requireCurrent();
                });
                db.runInTransaction(() -> {
                    context.requireCurrent();
                    if (from == 0) db.translatesDao().deleteForLanguage(language);
                    db.translatesDao().insertAll(rows);
                    db.synchronizationCursorDao().save(FilteredSyncPolicy.savedCursor(
                            FilteredSyncPolicy.TRANSLATIONS, scope, from, started,
                            db.translatesDao().countForLanguage(language)));
                    context.requireCurrent();
                });
                log("INFO", "saved: langId=" + language + ", rows=" + rows.size()
                        + ", room=true, realm=true, vpi="
                        + FilteredSyncPolicy.savedCursor(FilteredSyncPolicy.TRANSLATIONS, scope, from, started, 0).vpi);
                trace.saved(FilteredSyncPolicy.savedCursor(FilteredSyncPolicy.TRANSLATIONS, scope, from, started, 0).vpi);
                return rows;
            }
        } catch (Exception error) {
            if (trace != null) trace.failed(error);
            log("ERROR", "failed: langId=" + context.langId + ", VPI unchanged: " + error);
            throw error;
        }
    }

    private static boolean ready(AppDatabase db, Realm realm, String language, SynchronizationCursorSDB cursor) {
        return FilteredSyncPolicy.translationsReady(cursor, db.translatesDao().countForLanguage(language),
                realm.where(SiteTranslationsList.class).equalTo("langId", language).count());
    }

    private static void notifyCallback(RequestContext context, ExchangeInterface.Translates callback,
                                       List<TranslatesSDB> rows, Throwable error) {
        if (callback == null || !context.isCurrent()) return;
        try {
            if (error == null) callback.onSuccess(rows);
            else callback.onFailure(error.toString());
        } catch (Exception callbackError) {
            log("ERROR", "callback failed: " + callbackError);
        }
    }

    private static void log(String level, String message) {
        Globals.writeToMLOG(level, "TranslationsExchange", message);
    }

    private static final class RequestContext {
        final AppDatabase db = RoomManager.SQL_DB;
        final int userId = Globals.getCurrentUserId();
        final String sessionId = Globals.getCurrentSessionId();
        final int langId;

        RequestContext(int langId) { this.langId = langId; }

        boolean isCurrent() {
            return db != null && db == RoomManager.SQL_DB && userId == Globals.getCurrentUserId()
                    && Objects.equals(sessionId, Globals.getCurrentSessionId());
        }

        void requireCurrent() {
            if (!isCurrent()) throw new IllegalStateException("Account/session/database changed");
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
