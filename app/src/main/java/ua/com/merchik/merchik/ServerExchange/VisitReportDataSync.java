package ua.com.merchik.merchik.ServerExchange;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import io.realm.Realm;
import io.realm.RealmObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;
import ua.com.merchik.merchik.data.RetrofitResponse.models.TovarTableResponse;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/** Ordinary visits only. Transport/merge/serialization of RP stays in ReportPrepareSync. */
public final class VisitReportDataSync {
    private static final String RP = "visit_report_ready/report_prepare";
    private static final String PRODUCTS = "visit_report_ready/products";
    private static final String PRODUCT_ATTEMPTS = "visit_product_repair/attempts";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final Map<String, List<Runnable>> VISIT_REPAIRS = new HashMap<>();
    private static Download active;
    private static Runnable scheduledRepair;
    private static boolean pendingScopeRefresh;
    private static int retryUser;
    private static long lastAttempt = -1;

    private VisitReportDataSync() { }

    public static boolean reportReady() {
        try { return new Download().reportLoaded(); }
        catch (RuntimeException e) { return false; }
    }

    public static boolean productsReady() {
        try {
            Download data = new Download();
            return data.receipt(PRODUCTS) != null && data.missingProducts().isEmpty();
        } catch (RuntimeException e) { return false; }
    }

    /** The initial batch was processed; individual missing cards must not lock the whole plan. */
    public static boolean productsDownloaded() {
        try { return new Download().receipt(PRODUCTS) != null; }
        catch (RuntimeException e) { return false; }
    }

    /** Called by the startup screen, including after an interrupted login/app restart. */
    public static void retryInitial() {
        onMain(() -> {
            int user = Globals.getCurrentUserId();
            if (user <= 0 || user == 172906 || user == 19653) return;
            if (retryUser != user) { retryUser = user; lastAttempt = -1; }
            if (active != null) return;
            if (reportReady() && productsDownloaded()) {
                scheduleRepair();
                return;
            }
            if (!VisitReportDownloadPolicy.retryDue(SystemClock.elapsedRealtime(), lastAttempt)) return;
            lastAttempt = SystemClock.elapsedRealtime();
            try {
                if (!hasSavedPlan()) {
                    new TablesLoadingUnloading().downloadWPData();
                } else {
                    sync("startup retry", false);
                }
            } catch (RuntimeException e) {
                log("retryFailed", e.toString());
            }
        });
    }

    /** Run after any plan attempt; errors must not prevent refreshing cached visits. */
    public static void sync(String reason, boolean refresh) {
        onMain(() -> {
            Download started = null;
            try {
                if (!hasSavedPlan()) {
                    log("skip", "reason=" + reason + ", no saved plan");
                    return;
                }
                Download data = new Download();
                data.requireCurrent();
                if (active != null) {
                    pendingScopeRefresh |= !active.scope.equals(data.scope);
                    log("join", "reason=" + reason + ", scopeChanged=" + pendingScopeRefresh);
                    return;
                }
                if (!refresh && data.reportLoaded() && data.receipt(PRODUCTS) != null
                        && data.missingProducts().isEmpty()) return;
                retryUser = data.user;
                lastAttempt = SystemClock.elapsedRealtime();
                active = data;
                data.refreshProducts = refresh;
                started = data;
                data.ticket = ExchangeWork.TRACKER.beginWork();
                log("start", "reason=" + reason + ", user=" + data.user + ", visits=" + data.visits.size()
                        + ", rpReady=" + data.reportLoaded() + ", productsReady=" + (data.receipt(PRODUCTS) != null));
                if (data.visits.isEmpty()) {
                    data.saveReceipt(RP, 0);
                    data.saveReceipt(PRODUCTS, 0);
                    data.finish(null);
                    return;
                }
                if (!refresh && data.reportLoaded()) { data.products(); return; }
                // A background refresh must respect the editor's quiet period and auto-send setting.
                ReportPrepareSync.upload(false, null, new ExchangeInterface.ExchangeResponseInterface() {
                    @Override public <T> void onSuccess(List<T> rows) { data.report(); }
                    @Override public void onFailure(String error) { data.report(); }
                });
            } catch (RuntimeException e) {
                if (started != null) started.finish(e);
                else log("failed", e.toString());
            }
        });
    }

    private static void onMain(Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) action.run();
        else ExchangeWork.dispatch(MAIN::post, action);
    }

    private static boolean hasSavedPlan() {
        boolean loaded = RoomManager.SQL_DB.initStateDao().getState() != null
                && RoomManager.SQL_DB.initStateDao().getState().getWpLoaded();
        return VisitReportDownloadPolicy.canUseSavedPlan(loaded,
                RealmManager.INSTANCE.where(WpDataDB.class).count());
    }

    private static Map<Long, LocalDate> ordinaryVisitDates() {
        Map<Long, LocalDate> dates = new TreeMap<>();
        for (WpDataDB row : RealmManager.INSTANCE.where(WpDataDB.class).findAll()) {
            if (row.getCode_dad2() > 0 && !RnoVisitDataExchange.requiresPreparation(row))
                dates.put(row.getCode_dad2(), VisitReportDownloadPolicy.visitDate(row.getDt()));
        }
        return dates;
    }

    private static void log(String stage, String message) {
        Globals.writeToMLOG("INFO", "VisitReportDataSync/" + stage, message);
    }

    private static void scheduleRepair() {
        if (scheduledRepair != null) return;
        try {
            Download data = new Download();
            data.requireCurrent();
            if (!data.reportLoaded() || data.receipt(PRODUCTS) == null || data.repairableProducts().isEmpty()) return;
            scheduledRepair = () -> {
                scheduledRepair = null;
                // Re-read the current account/plan; never retain a Realm object across the delay.
                sync("missing product retry", false);
            };
            MAIN.postDelayed(scheduledRepair, VisitReportDownloadPolicy.RETRY_MS);
        } catch (RuntimeException e) { log("repairScheduleFailed", e.toString()); }
    }

    public static boolean hasMissingProducts(long dad2) {
        return !missingProducts(RealmManager.INSTANCE, new String[]{String.valueOf(dad2)}).isEmpty();
    }

    private static Set<String> missingProducts(Realm realm, String[] visits) {
        Set<String> ids = new HashSet<>();
        if (visits.length == 0) return ids;
        for (ReportPrepareDB row : realm.where(ReportPrepareDB.class).in("codeDad2", visits).findAll()) {
            if (row.getTovarId() == null || row.getTovarId().trim().isEmpty())
                throw new IllegalStateException("RP product ID missing");
            ids.add(row.getTovarId());
        }
        if (!ids.isEmpty()) {
            for (TovarDB row : realm.where(TovarDB.class).in("iD", ids.toArray(new String[0])).findAll()) ids.remove(row.getiD());
        }
        return ids;
    }

    /** One explicit retry when opening a visit, including products whose automatic budget expired. */
    public static void repairForVisit(long dad2, Runnable completed) {
        onMain(() -> {
            if (active != null) {
                active.afterFinish.add(() -> repairForVisit(dad2, completed));
                return;
            }
            int user = Globals.getCurrentUserId();
            String session = Globals.getCurrentSessionId();
            Realm realm = RealmManager.INSTANCE;
            AppDatabase db = RoomManager.SQL_DB;
            String key = ProductCardRetryPolicy.scope(user, String.valueOf(dad2));
            if (VISIT_REPAIRS.containsKey(key)) {
                VISIT_REPAIRS.get(key).add(completed);
                return;
            }
            try {
                WpDataDB visit = realm.where(WpDataDB.class).equalTo("code_dad2", dad2).findFirst();
                if (user <= 0 || visit == null || RnoVisitDataExchange.requiresPreparation(visit)) {
                    completed.run();
                    return;
                }
                Set<String> missing = missingProducts(realm, new String[]{String.valueOf(dad2)});
                if (missing.isEmpty()) { completed.run(); return; }
                List<Runnable> listeners = new ArrayList<>();
                listeners.add(completed);
                VISIT_REPAIRS.put(key, listeners);
                log("visitRepair", "dad2=" + dad2 + ", ids=" + missing);
                loadProducts(ProductCardRetryPolicy.request(missing), realm, () -> {
                    if (user != Globals.getCurrentUserId() || !Objects.equals(session, Globals.getCurrentSessionId())
                            || realm != RealmManager.INSTANCE || realm.isClosed() || db != RoomManager.SQL_DB)
                        throw new IllegalStateException("Account/database changed");
                }, count -> finishVisitRepair(key, null), error -> finishVisitRepair(key, error));
            } catch (RuntimeException e) {
                if (VISIT_REPAIRS.containsKey(key)) finishVisitRepair(key, e);
                else { log("visitRepairFailed", e.toString()); completed.run(); }
            }
        });
    }

    private static void finishVisitRepair(String key, Throwable error) {
        log("visitRepairFinished", "scope=" + key + (error == null ? "" : ", error=" + error));
        List<Runnable> listeners = VISIT_REPAIRS.remove(key);
        if (listeners != null) for (Runnable listener : listeners) listener.run();
    }

    private static void loadProducts(JsonObject request, Realm realm, Runnable requireCurrent,
                                     java.util.function.IntConsumer saved, java.util.function.Consumer<Throwable> failed) {
        RetrofitBuilder.getRetrofitInterface().GET_TOVAR_TABLE(RetrofitBuilder.contentType, request)
                .enqueue(new Callback<TovarTableResponse>() {
                    @Override public void onResponse(Call<TovarTableResponse> call, Response<TovarTableResponse> response) {
                        try {
                            requireCurrent.run();
                            TovarTableResponse body = response.body();
                            if (!response.isSuccessful() || body == null || !Boolean.TRUE.equals(body.getState()) || body.getList() == null)
                                throw new IllegalStateException("Products response invalid: HTTP " + response.code()
                                        + ", error=" + (body == null ? "empty body" : body.getError()));
                            for (TovarDB row : body.getList()) {
                                if (row == null || row.getiD() == null || row.getiD().trim().isEmpty())
                                    throw new IllegalStateException("Invalid product row");
                            }
                            if (!body.getList().isEmpty()) realm.executeTransaction(r -> r.insertOrUpdate(body.getList()));
                            saved.accept(body.getList().size());
                        } catch (RuntimeException e) { failed.accept(e); }
                    }
                    @Override public void onFailure(Call<TovarTableResponse> call, Throwable error) { failed.accept(error); }
                });
    }

    private static final class Download {
        final int user = Globals.getCurrentUserId();
        final String session = Globals.getCurrentSessionId();
        final Realm realm = RealmManager.INSTANCE;
        final AppDatabase db = RoomManager.SQL_DB;
        final Map<Long, LocalDate> visitDates = ordinaryVisitDates();
        final Set<Long> visits = visitDates.keySet();
        final String scope = VisitReportDownloadPolicy.scope(user, visitDates);
        final String[] dad2 = visits.stream().map(String::valueOf).toArray(String[]::new);
        ExchangeRunTracker.Ticket ticket;
        boolean finished;
        boolean productsStarted;
        boolean refreshProducts;
        final List<Runnable> afterFinish = new ArrayList<>();
        Throwable reportError;

        void requireCurrent() {
            if (finished || user <= 0 || user != Globals.getCurrentUserId() || !Objects.equals(session, Globals.getCurrentSessionId())
                    || db != RoomManager.SQL_DB || realm != RealmManager.INSTANCE || realm.isClosed()
                    || !visitDates.equals(ordinaryVisitDates())) throw new IllegalStateException("Account/database/visit scope changed");
        }

        SynchronizationCursorSDB receipt(String part) { return db.synchronizationCursorDao().get(part, scope); }

        List<ReportPrepareDB> reports() {
            return visits.isEmpty() ? new ArrayList<>()
                    : realm.where(ReportPrepareDB.class).in("codeDad2", dad2).findAll();
        }

        boolean reportLoaded() {
            SynchronizationCursorSDB saved = receipt(RP);
            return VisitReportDownloadPolicy.loaded(saved != null, saved == null ? -1 : saved.itemCount, reports().size());
        }

        Set<String> missingProducts() {
            return VisitReportDataSync.missingProducts(realm, dad2);
        }

        Map<String, Long> productAttempts(Set<String> ids) {
            Map<String, Long> attempts = new HashMap<>();
            for (String id : ids) {
                SynchronizationCursorSDB saved = db.synchronizationCursorDao().get(PRODUCT_ATTEMPTS,
                        ProductCardRetryPolicy.scope(user, id));
                attempts.put(id, saved == null ? 0L : saved.itemCount);
            }
            return attempts;
        }

        Set<String> repairableProducts() {
            Set<String> missing = missingProducts();
            return ProductCardRetryPolicy.eligible(missing, productAttempts(missing));
        }

        void saveReceipt(String part, long count) {
            requireCurrent();
            SynchronizationCursorSDB row = new SynchronizationCursorSDB();
            row.resource = part;
            row.scope = scope;
            row.itemCount = count;
            // vpi deliberately stays 0: even an empty successful response can complete startup.
            db.runInTransaction(() -> {
                db.synchronizationCursorDao().save(row);
                db.synchronizationCursorDao().deleteOtherScopes(part, scope);
            });
        }

        JsonObject request(boolean rp, long vpi) {
            return VisitReportDownloadPolicy.request(rp, visitDates, vpi);
        }

        long vpi(String table) {
            SynchronizationTimetableDB row = RealmManager.getSynchronizationTimetableRowByTable(table);
            return row == null ? 0 : row.getVpi_app();
        }

        void advance(String table, long started, int count) {
            requireCurrent();
            SynchronizationTimetableDB row = RealmManager.getSynchronizationTimetableRowByTable(table);
            if (row == null || count == 0) return;
            row.setVpi_app(VisitReportDownloadPolicy.nextVpi(row.getVpi_app(), started, count, true));
            RealmManager.setToSynchronizationTimetableDB(row);
        }

        void report() {
            try {
                requireCurrent();
                long from = VisitReportDownloadPolicy.requestVpi(reportLoaded(), vpi("report_prepare"));
                long started = TrustedTime.nowServerSecOrLocalSec();
                boolean[] fullyMerged = {false};
                JsonObject request = request(true, from);
                log("request", "report_prepare from=" + from + ", visits=" + visits.size()
                        + ", date_from=" + request.get("date_from").getAsString()
                        + ", date_to=" + request.get("date_to").getAsString());
                ReportPrepareSync.download(request, false, new Callback<ReportPrepareServer>() {
                    @Override public void onResponse(Call<ReportPrepareServer> call, Response<ReportPrepareServer> response) {
                        try {
                            requireCurrent();
                            ReportPrepareServer body = response.body();
                            if (!response.isSuccessful() || body == null || !Boolean.TRUE.equals(body.getState())
                                    || body.getList() == null)
                                throw new IllegalStateException("RP response invalid: HTTP " + response.code()
                                        + ", error=" + (body == null ? "empty body" : body.getError()));
                            // Deferred rows already exist locally. They do not block offline readiness,
                            // but must be downloaded again after their local edits are acknowledged.
                            saveReceipt(RP, reports().size());
                            if (fullyMerged[0]) advance("report_prepare", started, body.getList().size());
                            TablesLoadingUnloading.readyReportPrepare = true;
                            log("saved", "report_prepare rows=" + body.getList().size()
                                    + ", fullyMerged=" + fullyMerged[0] + ", vpi=" + vpi("report_prepare"));
                            products();
                        } catch (RuntimeException e) { reportFailed(e); }
                    }
                    @Override public void onFailure(Call<ReportPrepareServer> call, Throwable error) { reportFailed(error); }
                }, this::requireCurrent, rows -> {
                    for (ReportPrepareDB row : rows) {
                        if (row == null || !RealmObject.isValid(row) || row.getID() == null || row.getID() <= 0 || row.getTovarId() == null
                                || row.getTovarId().trim().isEmpty() || !visits.contains(Long.parseLong(row.getCodeDad2())))
                            throw new IllegalStateException("Invalid/out-of-scope RP row");
                    }
                }, merged -> fullyMerged[0] = merged);
            } catch (RuntimeException e) { reportFailed(e); }
        }

        void reportFailed(Throwable error) {
            try {
                requireCurrent();
                reportError = error;
                log("reportFailed", "vpi=" + vpi("report_prepare")
                        + ", continuing products, error=" + error);
                products();
            } catch (RuntimeException e) { finish(e); }
        }

        void products() {
            try {
                requireCurrent();
                if (productsStarted) return;
                productsStarted = true;
                if (!ProductCardRetryPolicy.needsBatch(refreshProducts, receipt(PRODUCTS) != null)) { repairProducts(); return; }
                long from = VisitReportDownloadPolicy.requestVpi(receipt(PRODUCTS) != null, vpi("tovar_list"));
                long started = TrustedTime.nowServerSecOrLocalSec();
                log("request", "tovar_list from=" + from + ", visits=" + visits.size());
                loadProducts(request(false, from), count -> {
                    saveReceipt(PRODUCTS, reports().size());
                    advance("tovar_list", started, count);
                    TablesLoadingUnloading.readyTovarTable = true;
                    log("saved", "tovar_list rows=" + count + ", vpi=" + vpi("tovar_list")
                            + ", missing=" + missingProducts());
                    repairProducts();
                });
            } catch (RuntimeException e) { finish(e); }
        }

        void repairProducts() {
            requireCurrent();
            if (!VISIT_REPAIRS.isEmpty()) { finish(null); return; }
            Set<String> missing = missingProducts();
            Map<String, Long> attempts = productAttempts(missing);
            Set<String> eligible = ProductCardRetryPolicy.eligible(missing, attempts);
            if (eligible.isEmpty()) {
                if (!missing.isEmpty()) log("repairExhausted", "user=" + user + ", ids=" + missing + ", retry on visit open only");
                finish(null);
                return;
            }
            // Persist before dispatch: network errors/process restarts also consume an attempt.
            db.runInTransaction(() -> {
                for (String id : eligible) {
                    SynchronizationCursorSDB row = new SynchronizationCursorSDB();
                    row.resource = PRODUCT_ATTEMPTS;
                    row.scope = ProductCardRetryPolicy.scope(user, id);
                    row.itemCount = ProductCardRetryPolicy.nextAttempt(attempts.get(id));
                    db.synchronizationCursorDao().save(row);
                    attempts.put(id, row.itemCount);
                }
            });
            log("repair", "ids=" + eligible + ", attempts=" + attempts);
            // A targeted response cannot advance the watermark of the entire product directory.
            loadProducts(ProductCardRetryPolicy.request(eligible), count -> {
                log("repairResult", "rows=" + count + ", remaining=" + missingProducts());
                finish(null);
            });
        }

        void loadProducts(JsonObject request, java.util.function.IntConsumer saved) {
            VisitReportDataSync.loadProducts(request, realm, this::requireCurrent, saved, this::finish);
        }

        void finish(Throwable error) {
            if (finished) return;
            if (error == null) error = reportError;
            finished = true;
            if (active == this) active = null;
            lastAttempt = SystemClock.elapsedRealtime();
            log(error == null ? "ready" : "failed", "user=" + user + ", visits=" + visits.size()
                    + (error == null ? "" : ", error=" + error));
            if (pendingScopeRefresh) {
                pendingScopeRefresh = false;
                ExchangeWork.dispatch(MAIN::post, () -> sync("plan changed during download", true));
            }
            if (ticket != null) ticket.close();
            for (Runnable listener : afterFinish) listener.run();
            scheduleRepair();
        }
    }
}
