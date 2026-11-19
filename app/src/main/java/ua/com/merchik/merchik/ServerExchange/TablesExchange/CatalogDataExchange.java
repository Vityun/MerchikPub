package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Function;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeWork;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammAddressSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammGroupSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammImagesSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammTypeSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ShowcaseResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammAddressResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammGroupResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammImagesResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammTypeResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm.PlanogrammVizitShowcaseResponse;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

import static ua.com.merchik.merchik.ServerExchange.TablesExchange.CatalogSyncPolicy.Table;
import static ua.com.merchik.merchik.ServerExchange.TablesExchange.CatalogSyncPolicy.rows;

/** All catalog entry points share account guards, in-flight requests and atomic daily receipts. */
public final class CatalogDataExchange {
    private static final Map<RequestContext, Single<?>> IN_FLIGHT = new HashMap<>();
    private CatalogDataExchange() { }

    public static void showcases(List<WpDataDB> visits, Clicks.clickObjectAndStatus callback) {
        if (visits.isEmpty()) { if (callback != null) callback.onSuccess(new ArrayList<ShowcaseSDB>()); return; }
        JsonObject base = Table.SHOWCASES.baseRequest();
        base.addProperty("active_only", "1");
        TreeSet<String> clients = new TreeSet<>();
        TreeSet<Integer> addresses = new TreeSet<>();
        for (WpDataDB visit : visits) {
            if (visit.getClient_id() != null) clients.add(visit.getClient_id());
            addresses.add(visit.getAddr_id());
        }
        JsonArray clientIds = new JsonArray();
        for (String client : clients) clientIds.add(client);
        JsonArray addressIds = new JsonArray();
        for (Integer address : addresses) addressIds.add(address.toString());
        base.add("client_id", clientIds);
        base.add("addr_id", addressIds);
        start(new Dataset<ShowcaseSDB>(Table.SHOWCASES, row -> row.id) {
            List<ShowcaseSDB> load(JsonObject request) throws Exception {
                ShowcaseResponse body = body(RetrofitBuilder.getRetrofitInterface().SHOWCASE_UPLOAD(RetrofitBuilder.contentType, request));
                return rows(body.state, body.error, body.list, id);
            }
            List<ShowcaseSDB> local(AppDatabase db) { return db.showcaseDao().getAll(); }
            List<Long> knownIds(List<ShowcaseSDB> local) { return CatalogSyncPolicy.showcaseIds(local); }
            void save(AppDatabase db, List<ShowcaseSDB> rows) { db.showcaseDao().upsertAll(rows); }
        }, base, callback);
    }

    public static void planograms(Clicks.clickObjectAndStatus callback) {
        start(new Dataset<PlanogrammSDB>(Table.PLANOGRAMS, row -> row.id) {
            List<PlanogrammSDB> load(JsonObject request) throws Exception {
                PlanogrammResponse body = body(RetrofitBuilder.getRetrofitInterface().Planogramm_RESPONSE(RetrofitBuilder.contentType, request));
                List<PlanogrammSDB> result = rows(body.state, body.error, body.list, id);
                for (PlanogrammSDB row : result) {
                    if (row.dtStart != null && row.dtStart.getTime() < 0) row.dtStart = null;
                    if (row.dtEnd != null && row.dtEnd.getTime() < 0) row.dtEnd = null;
                }
                return result;
            }
            List<PlanogrammSDB> local(AppDatabase db) { return db.planogrammDao().getAll(); }
            // Replace existing IDs too: tovar_grp may change or become empty/0.
            void save(AppDatabase db, List<PlanogrammSDB> rows) { db.planogrammDao().upsertAll(rows); }
        }, callback);
    }

    public static void addresses(Clicks.clickObjectAndStatus callback) {
        start(new Dataset<PlanogrammAddressSDB>(Table.ADDRESSES, row -> row.id, row -> row.planogramId) {
            List<PlanogrammAddressSDB> load(JsonObject request) throws Exception {
                PlanogrammAddressResponse body = body(RetrofitBuilder.getRetrofitInterface().Planogramm_ADDRESS_RESPONSE(RetrofitBuilder.contentType, request));
                return rows(body.state, body.error, body.list, id);
            }
            List<PlanogrammAddressSDB> local(AppDatabase db) { return db.planogrammAddressDao().getAll(); }
            void save(AppDatabase db, List<PlanogrammAddressSDB> rows) { db.planogrammAddressDao().upsertAll(rows); }
        }, callback);
    }

    public static void groups(Clicks.clickObjectAndStatus callback) {
        start(new Dataset<PlanogrammGroupSDB>(Table.GROUPS, row -> row.id, row -> row.planogramId) {
            List<PlanogrammGroupSDB> load(JsonObject request) throws Exception {
                PlanogrammGroupResponse body = body(RetrofitBuilder.getRetrofitInterface().Planogramm_GROUP_RESPONSE(RetrofitBuilder.contentType, request));
                return rows(body.state, body.error, body.list, id);
            }
            List<PlanogrammGroupSDB> local(AppDatabase db) { return db.planogrammGroupDao().getAll(); }
            void save(AppDatabase db, List<PlanogrammGroupSDB> rows) { db.planogrammGroupDao().upsertAll(rows); }
        }, callback);
    }

    public static void images(Clicks.clickObjectAndStatus callback) {
        start(new Dataset<PlanogrammImagesSDB>(Table.IMAGES, row -> row.id, row -> row.planogramId) {
            List<PlanogrammImagesSDB> load(JsonObject request) throws Exception {
                PlanogrammImagesResponse body = body(RetrofitBuilder.getRetrofitInterface().Planogramm_IMAGES_RESPONSE(RetrofitBuilder.contentType, request));
                return rows(body.state, body.error, body.list, id);
            }
            List<PlanogrammImagesSDB> local(AppDatabase db) { return db.planogrammImagesDao().getAll(); }
            void save(AppDatabase db, List<PlanogrammImagesSDB> rows) { db.planogrammImagesDao().upsertAll(rows); }
        }, callback);
    }

    public static void types(Clicks.clickObjectAndStatus callback) {
        start(new Dataset<PlanogrammTypeSDB>(Table.TYPES, PlanogrammTypeSDB::getID, PlanogrammTypeSDB::getPlanogram_id) {
            List<PlanogrammTypeSDB> load(JsonObject request) {
                PlanogrammTypeResponse body = RetrofitBuilder.getRetrofitInterface().Planogramm_TYPE_RESPONSE(RetrofitBuilder.contentType, request).blockingGet();
                return rows(body.state, body.error, body.list, id);
            }
            List<PlanogrammTypeSDB> local(AppDatabase db) { return db.planogrammTypeDao().getAllPlanogramsm(); }
            void save(AppDatabase db, List<PlanogrammTypeSDB> rows) { db.planogrammTypeDao().upsertAll(rows); }
        }, callback);
    }

    public static void visits(Clicks.clickObjectAndStatus callback) {
        start(new Dataset<PlanogrammVizitShowcaseSDB>(Table.VISITS, row -> row.id) {
            List<PlanogrammVizitShowcaseSDB> load(JsonObject request) {
                PlanogrammVizitShowcaseResponse body = RetrofitBuilder.getRetrofitInterface().PLANOGRAMM_VIZIT_SHOWCASE_RESPONSE(RetrofitBuilder.contentType, request).blockingGet();
                return rows(body.state, body.error, body.list, id);
            }
            List<PlanogrammVizitShowcaseSDB> local(AppDatabase db) { return db.planogrammVizitShowcaseDao().getAll(); }
            boolean downloaded(PlanogrammVizitShowcaseSDB row) { return !Integer.valueOf(1).equals(row.uploadStatus); }
            void save(AppDatabase db, List<PlanogrammVizitShowcaseSDB> rows) {
                // Protect both edits made during download and edits acknowledged while it was in flight.
                db.planogrammVizitShowcaseDao().upsertAll(CatalogSyncPolicy.visitRowsToSave(rows, beforeRequest, local(db)));
            }
        }, callback);
    }

    private static <T> void start(Dataset<T> dataset, Clicks.clickObjectAndStatus callback) {
        start(dataset, dataset.table.baseRequest(), callback);
    }

    @SuppressWarnings("unchecked")
    private static <T> void start(Dataset<T> dataset, JsonObject base, Clicks.clickObjectAndStatus callback) {
        RequestContext context = new RequestContext(dataset.table, base);
        Single<List<T>> request;
        synchronized (IN_FLIGHT) {
            request = (Single<List<T>>) IN_FLIGHT.get(context);
            if (request == null) {
                request = Single.fromCallable(() -> download(context, dataset))
                        .subscribeOn(Schedulers.io())
                        .doOnError(error -> log(context, "failed; full-refresh time unchanged: " + error))
                        .doFinally(() -> { synchronized (IN_FLIGHT) { IN_FLIGHT.remove(context); } }).cache();
                IN_FLIGHT.put(context, request);
            }
        }
        request.observeOn(AndroidSchedulers.mainThread()).compose(ExchangeWork.track()).subscribe(
                rows -> notify(context, callback, rows, null), error -> notify(context, callback, null, error));
    }

    private static <T> List<T> download(RequestContext context, Dataset<T> dataset) throws Exception {
        context.requireCurrent();
        AppDatabase db = context.db;
        dataset.beforeRequest = dataset.local(db);
        List<Long> ids = dataset.knownIds(dataset.beforeRequest);
        SynchronizationCursorSDB receipt = db.synchronizationCursorDao().get(context.table.receiptResource(), context.scope);
        int knownRows = dataset.downloadedCount(dataset.beforeRequest);
        boolean full = CatalogSyncPolicy.fullDue(TrustedTime.nowServerSecOrLocalSec(), receipt, knownRows);
        JsonObject request = CatalogSyncPolicy.request(context.table, context.base, ids, full);
        log(context, "request: full=" + full + ", knownRows=" + knownRows + ", excludedIds="
                + (full ? 0 : ids.size()) + ", exclusion=" + context.table.exclusion
                + ", idField=" + context.table.exclusionIdField() + ", scope=" + context.scope + ", VPI=none");
        List<T> rows = dataset.load(request);
        context.requireCurrent();
        if (rows.isEmpty()) {
            log(context, "empty; local rows and full-refresh time unchanged");
            return rows;
        }
        db.runInTransaction(() -> {
            context.requireCurrent();
            dataset.save(db, rows);
            SynchronizationCursorSDB saved = CatalogSyncPolicy.fullReceipt(context.table, context.scope, full,
                    TrustedTime.nowServerSecOrLocalSec(), rows.size(), dataset.downloadedCount(dataset.local(db)));
            if (saved != null) db.synchronizationCursorDao().save(saved);
            context.requireCurrent();
        });
        log(context, "saved: full=" + full + ", rows=" + rows.size());
        return rows;
    }

    private static <B> B body(Call<B> call) throws Exception {
        Response<B> response = call.execute();
        if (!response.isSuccessful() || response.body() == null) {
            if (response.errorBody() != null) response.errorBody().close();
            throw new IllegalStateException("HTTP " + response.code() + ", emptyBody=" + (response.body() == null));
        }
        return response.body();
    }

    private static void notify(RequestContext context, Clicks.clickObjectAndStatus callback, Object rows, Throwable error) {
        if (callback == null || !context.isCurrent()) return;
        try {
            if (error == null) callback.onSuccess(rows);
            else callback.onFailure(error.toString());
        } catch (Exception failure) { log(context, "callback failed: " + failure); }
    }

    private static void log(RequestContext context, String message) {
        Globals.writeToMLOG("INFO", "CatalogDataExchange/" + context.table.resource(), message);
    }

    private abstract static class Dataset<T> {
        final Table table;
        final Function<T, ? extends Number> id;
        final Function<T, ?> exclusionId;
        List<T> beforeRequest;
        Dataset(Table table, Function<T, ? extends Number> id) { this(table, id, id); }
        Dataset(Table table, Function<T, ? extends Number> id, Function<T, ?> exclusionId) {
            this.table = table;
            this.id = id;
            this.exclusionId = exclusionId;
        }
        abstract List<T> load(JsonObject request) throws Exception;
        abstract List<T> local(AppDatabase db);
        abstract void save(AppDatabase db, List<T> rows);
        boolean downloaded(T row) { return true; }
        List<Long> knownIds(List<T> local) {
            // Related catalog methods interpret id_exclude_old as parent planogram IDs.
            return CatalogSyncPolicy.exclusionIds(local, exclusionId, this::downloaded);
        }
        int downloadedCount(List<T> local) {
            int count = 0;
            for (T row : local) {
                Number key = id.apply(row);
                if (key != null && key.longValue() > 0 && downloaded(row)) count++;
            }
            return count;
        }
    }

    private static final class RequestContext {
        final AppDatabase db = RoomManager.SQL_DB;
        final int user = Globals.getCurrentUserId();
        final String session = Globals.getCurrentSessionId();
        final Table table;
        final JsonObject base;
        final String scope;
        RequestContext(Table table, JsonObject base) {
            this.table = table;
            this.base = base.deepCopy();
            scope = CatalogSyncPolicy.scope(user, this.base);
        }
        boolean isCurrent() {
            return db != null && db == RoomManager.SQL_DB && user == Globals.getCurrentUserId()
                    && Objects.equals(session, Globals.getCurrentSessionId());
        }
        void requireCurrent() {
            if (!isCurrent()) throw new IllegalStateException("Account/session/database changed");
        }
        @Override public boolean equals(Object other) {
            if (!(other instanceof RequestContext)) return false;
            RequestContext that = (RequestContext) other;
            return db == that.db && table == that.table && scope.equals(that.scope) && Objects.equals(session, that.session);
        }
        @Override public int hashCode() { return Objects.hash(System.identityHashCode(db), table, scope, session); }
    }
}
