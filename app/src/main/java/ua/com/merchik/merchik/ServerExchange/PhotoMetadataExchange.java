package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.realm.Realm;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.FilteredSyncPolicy;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ModImagesView;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ModImagesViewList;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/** Downloads metadata only. Files are neither required nor downloaded here. */
public final class PhotoMetadataExchange {
    private static final Map<Context, Single<Result>> IN_FLIGHT = new HashMap<>();

    private PhotoMetadataExchange() { }

    public static final class Result {
        public final List<ModImagesViewList> rows;
        public final List<StackPhotoDB> photos;
        Result(List<ModImagesViewList> rows, List<StackPhotoDB> photos) {
            this.rows = rows;
            this.photos = photos;
        }
    }

    public static void download(PhotoTableRequest request, boolean incremental,
                                Clicks.clickObjectAndStatus<Result> callback) {
        Context context;
        try {
            context = new Context(request, incremental);
        } catch (Exception error) {
            log("ERROR", "invalid metadata request: " + error);
            if (callback != null) {
                try { callback.onFailure(error.toString()); }
                catch (Exception failure) { log("ERROR", "callback failed: " + failure); }
            }
            return;
        }
        Single<Result> pending;
        synchronized (IN_FLIGHT) {
            pending = IN_FLIGHT.get(context);
            if (pending == null) {
                pending = Single.fromCallable(() -> downloadSync(context))
                        .subscribeOn(Schedulers.io())
                        .doFinally(() -> { synchronized (IN_FLIGHT) { IN_FLIGHT.remove(context); } })
                        .cache();
                IN_FLIGHT.put(context, pending);
            } else {
                log("INFO", "join in-flight: " + context.scope);
            }
        }
        pending.observeOn(AndroidSchedulers.mainThread()).compose(ExchangeWork.track()).subscribe(result -> notify(context, callback, result, null), error -> {
            log("ERROR", "VPI unchanged, scope=" + context.scope + ", error=" + error);
            notify(context, callback, null, error);
        });
    }

    private static void notify(Context context, Clicks.clickObjectAndStatus<Result> callback,
                               Result result, Throwable error) {
        if (callback == null) return;
        try {
            if (!context.isCurrent()) callback.onFailure("Photo metadata session changed");
            else if (error != null) callback.onFailure(error.toString());
            else callback.onSuccess(result);
        } catch (Exception failure) {
            log("ERROR", "callback failed after download: " + failure);
        }
    }

    private static Result downloadSync(Context context) throws Exception {
        context.requireCurrent();
        Long started = TrustedTime.nowServerSecOrLocalSec();
        long startedLocal = System.currentTimeMillis() / 1000;
        SynchronizationCursorSDB cursor = context.incremental
                ? context.db.synchronizationCursorDao().get(PhotoMetadataPolicy.RESOURCE, context.scope) : null;
        long count;
        try (Realm realm = Realm.getDefaultInstance()) { count = realm.where(StackPhotoDB.class).count(); }
        long from = context.incremental ? FilteredSyncPolicy.requestWatermark(cursor, started,
                cursor != null && count >= cursor.itemCount) : 0;
        JsonObject request = PhotoMetadataPolicy.request(context.filters, from);
        log("INFO", "request=" + request);
        SyncExchangeTrace trace = new SyncExchangeTrace("images_view/list_image", from);
        try {
            Response<ModImagesView> response = RetrofitBuilder.getRetrofitInterface()
                    .MOD_IMAGES_VIEW_CALL(RetrofitBuilder.contentType, request).execute();
            if (!response.isSuccessful()) {
                if (response.errorBody() != null) response.errorBody().close();
                throw new IllegalStateException("list_image HTTP " + response.code());
            }
            ModImagesView body = response.body();
            trace.response(body == null ? null : body.getState(), body == null ? null : body.getError(),
                    body == null ? null : body.getList());
            List<ModImagesViewList> rows = PhotoMetadataPolicy.rows(body, request);
            context.requireCurrent();
            List<StackPhotoDB> photos;
            try (Realm realm = Realm.getDefaultInstance()) {
                photos = save(realm, rows, startedLocal, context::requireCurrent);
                count = realm.where(StackPhotoDB.class).count();
            }
            long savedVpi = from;
            if (context.incremental) {
                SynchronizationCursorSDB saved = FilteredSyncPolicy.savedCursor(
                        PhotoMetadataPolicy.RESOURCE, context.scope, from, started, count);
                // Realm first. A failure before Room commits leaves the old cursor and replays the delta.
                context.db.runInTransaction(() -> {
                    context.requireCurrent();
                    context.db.synchronizationCursorDao().save(saved);
                    context.requireCurrent();
                });
                savedVpi = saved.vpi;
            }
            trace.saved(savedVpi);
            log("INFO", "saved=" + photos.size() + ", received=" + response.body().getList().size()
                    + ", incremental=" + context.incremental + ", scope=" + context.scope);
            return new Result(rows, photos);
        } catch (Exception error) {
            trace.failed(error);
            throw error;
        }
    }

    public static List<StackPhotoDB> save(Realm realm, List<ModImagesViewList> incoming,
                                         long startedLocalSec, Runnable requireCurrent) {
        List<ModImagesViewList> rows = PhotoMetadataPolicy.uniqueRows(incoming);
        List<StackPhotoDB> saved = new ArrayList<>();
        boolean[] commentPending = {false};
        realm.executeTransaction(transaction -> {
            requireCurrent.run();
            long nextId = RealmManager.stackPhotoGetLastId(transaction) + 1;
            for (ModImagesViewList row : rows) {
                StackPhotoDB current = transaction.where(StackPhotoDB.class)
                        .equalTo("photoServerId", row.getID()).findFirst();
                if (current == null && row.imgHash != null && !row.imgHash.trim().isEmpty()) {
                    List<StackPhotoDB> candidates = new ArrayList<>();
                    for (StackPhotoDB candidate : transaction.where(StackPhotoDB.class)
                            .equalTo("photo_hash", row.imgHash).findAll()) {
                        if (PhotoMetadataPolicy.matchesUnassignedLocal(candidate, row)) candidates.add(candidate);
                    }
                    if (candidates.size() == 1) current = candidates.get(0);
                    if (candidates.size() > 1) throw new IllegalStateException("Ambiguous local photo hash for " + row.getID());
                }
                boolean isNew = current == null;
                if (isNew) current = transaction.createObject(StackPhotoDB.class, Math.toIntExact(nextId++));
                PhotoMetadataPolicy.merge(current, row, isNew, startedLocalSec);
                if (current.getTime_event() == null && row.getDt() > 0) {
                    current.setTime_event(Clock.getHumanTime3(row.getDt()));
                }
                commentPending[0] |= current.isCommentUpload();
                saved.add(current);
            }
            requireCurrent.run();
        });
        List<StackPhotoDB> copies = realm.copyFromRealm(saved);
        if (commentPending[0]) PhotoCommentSync.changed();
        return copies;
    }

    private static void log(String level, String text) {
        Globals.writeToMLOG(level, "PhotoMetadataExchange", text);
    }

    private static final class Context {
        final AppDatabase db = RoomManager.SQL_DB;
        final int userId = Globals.getCurrentUserId();
        final String sessionId = Globals.getCurrentSessionId();
        final JsonObject filters;
        final String scope;
        final boolean incremental;

        Context(PhotoTableRequest request, boolean incremental) {
            filters = PhotoMetadataPolicy.filters(new Gson().toJsonTree(request).getAsJsonObject());
            scope = PhotoMetadataPolicy.scope(userId, filters);
            this.incremental = incremental && !filters.has("id_list");
        }
        boolean isCurrent() {
            return db != null && db == RoomManager.SQL_DB && userId == Globals.getCurrentUserId()
                    && Objects.equals(sessionId, Globals.getCurrentSessionId());
        }
        void requireCurrent() {
            if (!isCurrent()) throw new IllegalStateException("Account/session/database changed");
        }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Context)) return false;
            Context that = (Context) other;
            return db == that.db && userId == that.userId && incremental == that.incremental
                    && Objects.equals(sessionId, that.sessionId) && scope.equals(that.scope);
        }
        @Override public int hashCode() {
            return Objects.hash(System.identityHashCode(db), userId, sessionId, scope, incremental);
        }
    }
}
