package ua.com.merchik.merchik.ServerExchange;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.OptionsServer;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;
import ua.com.merchik.merchik.data.RetrofitResponse.models.TovarTableResponse;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

import static ua.com.merchik.merchik.ServerExchange.RnoVisitDataPolicy.Part;

/** Main-thread coordinator. Cursor rows here are completion receipts, not global VPI. */
public final class RnoVisitDataExchange {
    private static final String REQUESTED = "rno_visit/requested";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final Map<Context, Download> IN_FLIGHT = new HashMap<>();

    private RnoVisitDataExchange() { }

    public interface Result { void complete(String error); }

    public static final class Subscription {
        private Result result;
        Subscription(Result result) { this.result = result; }
        public void cancel() { result = null; }
        void complete(String error) {
            Result listener = result;
            result = null;
            if (listener != null) listener.complete(error);
        }
    }

    public static boolean requiresPreparation(WpDataDB visit) {
        return visit != null && RnoVisitDataPolicy.needsPreparation(visit.getUser_id(), visit.getCode_dad2_hash());
    }

    public static boolean isReady(WpDataDB visit) {
        if (visit == null) return false;
        if (!requiresPreparation(visit)) return true;
        if (!RnoVisitDataPolicy.hasHash(visit.getCode_dad2_hash())) return false;
        try {
            Context context = new Context(visit);
            context.requireCurrent();
            SynchronizationCursorSDB options = context.receipt(Part.OPTIONS);
            return RnoVisitDataPolicy.ready(context.loaded(Part.OPTIONS), options == null ? 0 : options.itemCount,
                    context.loaded(Part.REPORT_PREPARE), context.loaded(Part.PRODUCTS), context.missingProducts().size());
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static Subscription prepare(long dad2, boolean refresh, Result callback) {
        Subscription subscription = new Subscription(callback);
        // All callers, including legacy Java screens, join the same in-flight operation.
        if (Looper.myLooper() == Looper.getMainLooper()) start(dad2, refresh, subscription);
        else MAIN.post(() -> start(dad2, refresh, subscription));
        return subscription;
    }

    /** Existing targeted refresh/reconciliation requests also need the visit's access key. */
    public static void authorizeTargetedRequest(JsonObject request) {
        if (!request.has("code_dad2") || request.has("code_dad2_hash")) return;
        try {
            com.google.gson.JsonElement value = request.get("code_dad2");
            if (value.isJsonArray()) {
                if (value.getAsJsonArray().size() != 1) return;
                value = value.getAsJsonArray().get(0);
            }
            if (value.isJsonNull()) return;
            long dad2 = Long.parseLong(value.getAsString());
            WpDataDB visit = RealmManager.INSTANCE.where(WpDataDB.class).equalTo("code_dad2", dad2).findFirst();
            if (visit == null || !RnoVisitDataPolicy.hasHash(visit.getCode_dad2_hash())) return;
            JsonObject access = RnoVisitDataPolicy.request(Part.REPORT_PREPARE, dad2, visit.getCode_dad2_hash());
            request.add("code_dad2", access.get("code_dad2"));
            request.add("code_dad2_hash", access.get("code_dad2_hash"));
            request.addProperty("dt_change_from", "0");
            request.remove("date_from");
            request.remove("date_to");
        } catch (NumberFormatException ignored) {
            // An unscoped legacy request is not an on-demand visit request.
        }
    }

    private static void start(long dad2, boolean refresh, Subscription subscription) {
        try {
            WpDataDB visit = RealmManager.INSTANCE.where(WpDataDB.class).equalTo("code_dad2", dad2).findFirst();
            if (visit == null) throw new IllegalStateException("Відвідування не знайдено. Виконайте обмін даними.");
            if (!requiresPreparation(visit)) {
                subscription.complete(null);
                return;
            }
            if (!RnoVisitDataPolicy.hasHash(visit.getCode_dad2_hash())) {
                throw new IllegalStateException("Не отримано ключ доступу до РНО. Виконайте обмін даними та повторіть спробу.");
            }
            Context context = new Context(visit);
            context.requireCurrent();
            Download download = IN_FLIGHT.get(context);
            if (download != null) {
                download.listeners.add(subscription);
                log(dad2, "join");
                return;
            }
            if (!refresh && isReady(visit)) {
                subscription.complete(null);
                return;
            }
            context.save(REQUESTED, RnoVisitDataPolicy.visitScope(context.user, dad2), 0);
            download = new Download(context, refresh);
            download.listeners.add(subscription);
            IN_FLIGHT.put(context, download);
            log(dad2, "start, refresh=" + refresh);
            download.options();
        } catch (RuntimeException e) {
            log(dad2, "cannot start: " + e.getClass().getSimpleName());
            subscription.complete(e.getMessage());
        }
    }

    /** Refresh only visits the user has opened; never fan out over the entire RNO list. */
    public static void refreshRequestedVisits() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            MAIN.post(RnoVisitDataExchange::refreshRequestedVisits);
            return;
        }
        try {
            int user = Globals.getCurrentUserId();
            if (user <= 0 || RoomManager.SQL_DB == null) return;
            long now = TrustedTime.nowServerSecOrLocalSec();
            List<Long> selected = new ArrayList<>();
            for (WpDataDB visit : RealmManager.INSTANCE.where(WpDataDB.class)
                    .greaterThanOrEqualTo("dt", new Date((now - 21L * 86400) * 1000)).findAll()) {
                if (!RnoVisitDataPolicy.hasHash(visit.getCode_dad2_hash())) continue;
                SynchronizationCursorSDB receipt = RoomManager.SQL_DB.synchronizationCursorDao()
                        .get(REQUESTED, RnoVisitDataPolicy.visitScope(user, visit.getCode_dad2()));
                if (receipt != null && now - receipt.vpi >= 600) selected.add(visit.getCode_dad2());
            }
            for (long dad2 : selected) prepare(dad2, true, error -> {
                if (error != null) log(dad2, "background refresh failed; retry on next exchange");
            });
        } catch (RuntimeException e) {
            log(0, "refresh failed: " + e.getClass().getSimpleName());
        }
    }

    private static final class Download {
        final Context context;
        final boolean refresh;
        final List<Subscription> listeners = new ArrayList<>();
        boolean finished;
        Part phase;

        Download(Context context, boolean refresh) { this.context = context; this.refresh = refresh; }

        void options() {
            try {
                phase = Part.OPTIONS;
                context.requireCurrent();
                if (!refresh && context.loaded(Part.OPTIONS) && context.receipt(Part.OPTIONS).itemCount > 0) {
                    reportPrepare();
                    return;
                }
                execute(RetrofitBuilder.getRetrofitInterface().GET_OPTIONS(RetrofitBuilder.contentType,
                        context.request(Part.OPTIONS)), body -> {
                    validate(body.getState(), body.getList());
                    for (OptionsDB row : body.getList()) {
                        if (row == null || row.getID() == null || !String.valueOf(context.dad2).equals(row.getCodeDad2())) {
                            throw new IllegalStateException("Некоректна відповідь з опціями відвідування.");
                        }
                    }
                    RealmManager.setOptions2(body.getList());
                    context.saved(Part.OPTIONS, body.getList().size());
                    reportPrepare();
                });
            } catch (RuntimeException e) { fail(e); }
        }

        void reportPrepare() {
            try {
                phase = Part.REPORT_PREPARE;
                context.requireCurrent();
                if (!refresh && context.loaded(Part.REPORT_PREPARE)) { products(); return; }
                ReportPrepareSync.download(context.request(Part.REPORT_PREPARE), false,
                        new Callback<ReportPrepareServer>() {
                            public void onResponse(Call<ReportPrepareServer> call, Response<ReportPrepareServer> response) {
                                try {
                                    context.requireCurrent();
                                    ReportPrepareServer body = requireBody(response);
                                    validate(body.getState(), body.getList());
                                    for (ReportPrepareDB row : body.getList()) {
                                        // A conflicting server ID may have been rejected by the RP merge.
                                        if (context.realm.where(ReportPrepareDB.class)
                                                .equalTo("codeDad2", String.valueOf(context.dad2))
                                                .equalTo("tovarId", row.getTovarId()).count() == 0) {
                                            throw new IllegalStateException("RP row was not saved");
                                        }
                                    }
                                    context.saved(Part.REPORT_PREPARE, context.count(Part.REPORT_PREPARE));
                                    products();
                                } catch (RuntimeException e) { fail(e); }
                            }
                            public void onFailure(Call<ReportPrepareServer> call, Throwable error) { fail(error); }
                        }, context::requireCurrent, rows -> {
                            for (ReportPrepareDB row : rows) {
                                if (row == null || !RnoVisitDataPolicy.validReportRow(row.getID(),
                                        row.getCodeDad2(), row.getTovarId(), context.dad2)) {
                                    throw new IllegalStateException("Invalid RP row in visit response");
                                }
                            }
                        });
            } catch (RuntimeException e) { fail(e); }
        }

        void products() {
            try {
                phase = Part.PRODUCTS;
                context.requireCurrent();
                if (context.missingProducts().isEmpty()) {
                    context.saved(Part.PRODUCTS, context.productIds().size());
                    complete();
                    return;
                }
                execute(RetrofitBuilder.getRetrofitInterface().GET_TOVAR_TABLE(RetrofitBuilder.contentType,
                        context.request(Part.PRODUCTS)), body -> {
                    validate(body.getState(), body.getList());
                    for (TovarDB row : body.getList()) {
                        if (row == null || row.getiD() == null || row.getiD().trim().isEmpty()) {
                            throw new IllegalStateException("Некоректна відповідь довідника товарів.");
                        }
                    }
                    // Upsert only: setTovar() deletes the whole shared product directory.
                    context.realm.executeTransaction(realm -> realm.insertOrUpdate(body.getList()));
                    Set<String> missing = context.missingProducts();
                    if (!missing.isEmpty()) throw new IllegalStateException("Не завантажено картки товарів: " + missing.size());
                    context.saved(Part.PRODUCTS, context.productIds().size());
                    complete();
                });
            } catch (RuntimeException e) { fail(e); }
        }

        void complete() {
            if (context.receipt(Part.OPTIONS).itemCount == 0) {
                finish("Сервер не повернув опції цього відвідування. Роботу поки не можна розпочати.");
                return;
            }
            finish(null);
        }

        <T> void execute(Call<T> call, Save<T> save) {
            call.enqueue(new Callback<T>() {
                public void onResponse(Call<T> call, Response<T> response) {
                    try {
                        context.requireCurrent();
                        save.accept(requireBody(response));
                    } catch (RuntimeException e) { fail(e); }
                }
                public void onFailure(Call<T> call, Throwable error) { fail(error); }
            });
        }

        void fail(Throwable error) {
            log(context.dad2, "failed " + phase + ": " + error.getClass().getSimpleName()
                    + ", " + String.valueOf(error.getMessage()).replace(context.hash, "[redacted]"));
            finish("Не вдалося підготувати дані відвідування. Перевірте з'єднання та повторіть спробу.");
        }

        void finish(String error) {
            if (finished) return;
            finished = true;
            IN_FLIGHT.remove(context);
            log(context.dad2, error == null ? "ready" : "not ready");
            for (Subscription listener : listeners) {
                try { listener.complete(error); }
                catch (RuntimeException e) { log(context.dad2, "listener failed"); }
            }
            listeners.clear();
        }
    }

    private static final class Context {
        final AppDatabase db = RoomManager.SQL_DB;
        final Realm realm = RealmManager.INSTANCE;
        final int user = Globals.getCurrentUserId();
        final String session = Globals.getCurrentSessionId();
        final long dad2;
        final String hash;
        final String scope;

        Context(WpDataDB visit) {
            dad2 = visit.getCode_dad2();
            hash = visit.getCode_dad2_hash();
            scope = RnoVisitDataPolicy.dataScope(user, dad2, hash);
        }

        void requireCurrent() {
            if (db == null || db != RoomManager.SQL_DB || realm != RealmManager.INSTANCE || realm.isClosed()
                    || user != Globals.getCurrentUserId() || !Objects.equals(session, Globals.getCurrentSessionId())) {
                throw new IllegalStateException("Account/session/database changed");
            }
            WpDataDB visit = realm.where(WpDataDB.class).equalTo("code_dad2", dad2).findFirst();
            if (visit == null || !Objects.equals(hash, visit.getCode_dad2_hash())) {
                throw new IllegalStateException("Visit/access changed");
            }
        }

        JsonObject request(Part part) { return RnoVisitDataPolicy.request(part, dad2, hash); }

        SynchronizationCursorSDB receipt(Part part) {
            return db.synchronizationCursorDao().get("rno_visit/" + part.name(), scope);
        }

        boolean loaded(Part part) {
            SynchronizationCursorSDB receipt = receipt(part);
            return RnoVisitDataPolicy.loaded(receipt != null, receipt == null ? -1 : receipt.itemCount, count(part));
        }

        long count(Part part) {
            if (part == Part.PRODUCTS) return productIds().size();
            if (part == Part.OPTIONS) return realm.where(OptionsDB.class).equalTo("codeDad2", String.valueOf(dad2)).count();
            return realm.where(ReportPrepareDB.class).equalTo("codeDad2", String.valueOf(dad2)).count();
        }

        Set<String> productIds() {
            Set<String> ids = new HashSet<>();
            for (ReportPrepareDB row : realm.where(ReportPrepareDB.class).equalTo("codeDad2", String.valueOf(dad2)).findAll()) {
                String id = row.getTovarId();
                if (id == null || id.trim().isEmpty()) throw new IllegalStateException("RP product ID missing");
                ids.add(id);
            }
            return ids;
        }

        Set<String> missingProducts() {
            Set<String> missing = productIds();
            if (!missing.isEmpty()) {
                for (TovarDB row : realm.where(TovarDB.class).in("iD", missing.toArray(new String[0])).findAll()) {
                    missing.remove(row.getiD());
                }
            }
            return missing;
        }

        void saved(Part part, long count) {
            save("rno_visit/" + part.name(), scope, count);
            log(dad2, "saved " + part + ", rows=" + count);
        }

        void save(String resource, String scope, long count) {
            requireCurrent();
            SynchronizationCursorSDB receipt = new SynchronizationCursorSDB();
            receipt.resource = resource;
            receipt.scope = scope;
            receipt.vpi = Math.max(1, TrustedTime.nowServerSecOrLocalSec());
            receipt.itemCount = count;
            db.synchronizationCursorDao().save(receipt);
        }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Context)) return false;
            Context that = (Context) other;
            return db == that.db && realm == that.realm && user == that.user && dad2 == that.dad2
                    && Objects.equals(session, that.session) && Objects.equals(hash, that.hash);
        }
        @Override public int hashCode() {
            return Objects.hash(System.identityHashCode(db), System.identityHashCode(realm), user, dad2, session, hash);
        }
    }

    private interface Save<T> { void accept(T value); }

    private static void validate(Boolean state, List<?> rows) {
        if (!Boolean.TRUE.equals(state) || rows == null) throw new IllegalStateException("Invalid server response");
    }

    private static <T> T requireBody(Response<T> response) {
        if (!response.isSuccessful() || response.body() == null) throw new IllegalStateException("HTTP " + response.code());
        return response.body();
    }

    private static void log(long dad2, String message) {
        Globals.writeToMLOG("INFO", "RnoVisitData", "dad2=" + dad2 + ", " + message);
    }
}
