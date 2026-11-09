package ua.com.merchik.merchik.ServerExchange;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static Download active;
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

    /** Called by the startup screen, including after an interrupted login/app restart. */
    public static void retryInitial() {
        onMain(() -> {
            int user = Globals.getCurrentUserId();
            if (user <= 0 || user == 172906 || user == 19653) return;
            if (retryUser != user) { retryUser = user; lastAttempt = -1; }
            if (active != null || (reportReady() && productsReady())
                    || !VisitReportDownloadPolicy.retryDue(SystemClock.elapsedRealtime(), lastAttempt)) return;
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
            Set<String> ids = new HashSet<>();
            for (ReportPrepareDB row : reports()) {
                if (row.getTovarId() == null || row.getTovarId().trim().isEmpty())
                    throw new IllegalStateException("RP product ID missing");
                ids.add(row.getTovarId());
            }
            if (!ids.isEmpty()) {
                for (TovarDB row : realm.where(TovarDB.class).in("iD", ids.toArray(new String[0])).findAll()) ids.remove(row.getiD());
            }
            return ids;
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
                long from = VisitReportDownloadPolicy.requestVpi(receipt(PRODUCTS) != null && missingProducts().isEmpty(), vpi("tovar_list"));
                long started = TrustedTime.nowServerSecOrLocalSec();
                log("request", "tovar_list from=" + from + ", visits=" + visits.size());
                loadProducts(request(false, from), count -> {
                    Set<String> missing = missingProducts();
                    Runnable complete = () -> {
                        Set<String> remaining = missingProducts();
                        if (!remaining.isEmpty()) throw new IllegalStateException("Product cards still missing: " + remaining);
                        saveReceipt(PRODUCTS, reports().size());
                        advance("tovar_list", started, count);
                        TablesLoadingUnloading.readyTovarTable = true;
                        log("saved", "tovar_list rows=" + count + ", vpi=" + vpi("tovar_list"));
                        finish(null);
                    };
                    if (missing.isEmpty()) complete.run();
                    else {
                        JsonObject repair = new JsonObject();
                        repair.addProperty("mod", "data_list");
                        repair.addProperty("act", "tovar_list");
                        JsonArray ids = new JsonArray();
                        for (String id : missing) ids.add(id);
                        repair.add("id", ids);
                        log("repair", "missing product cards=" + missing.size() + ", ids=" + missing);
                        loadProducts(repair, ignored -> complete.run());
                    }
                });
            } catch (RuntimeException e) { finish(e); }
        }

        void loadProducts(JsonObject request, java.util.function.IntConsumer saved) {
            RetrofitBuilder.getRetrofitInterface().GET_TOVAR_TABLE(RetrofitBuilder.contentType, request)
                    .enqueue(new Callback<TovarTableResponse>() {
                        @Override public void onResponse(Call<TovarTableResponse> call, Response<TovarTableResponse> response) {
                            try {
                                requireCurrent();
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
                            } catch (RuntimeException e) { finish(e); }
                        }
                        @Override public void onFailure(Call<TovarTableResponse> call, Throwable error) { finish(error); }
                    });
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
        }
    }
}
