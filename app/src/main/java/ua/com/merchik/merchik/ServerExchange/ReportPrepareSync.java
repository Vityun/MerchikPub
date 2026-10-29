package ua.com.merchik.merchik.ServerExchange;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.ReportConductManager;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ReportPrepare.ReportPrepareUploadList;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.ReportPrepare.ReportPrepareUploadResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.update.reportprepare.ReportPrepareUpdateResponseList;
import ua.com.merchik.merchik.data.UploadToServ.ReportPrepareServ;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/** All RP requests and their Realm commits run sequentially on the main thread. */
public final class ReportPrepareSync {
    private static final String TAG = "ReportPrepareSync";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final Gson GSON = new Gson();
    private static final ReportPrepareUploadPolicy POLICY = new ReportPrepareUploadPolicy();
    private static final ReportPrepareRequestQueue QUEUE = new ReportPrepareRequestQueue(MAIN::post);
    private static final Set<String> DEFERRED_VISITS = new HashSet<>();
    private static final Set<String> FORCED_VISITS = new HashSet<>();
    private static final Runnable TIMER = () -> {
        nextTickAt = Long.MAX_VALUE;
        upload(false, null, null);
    };
    private static boolean automaticQueued;
    private static int policyUser;
    private static long retryAfter;
    private static long nextTickAt = Long.MAX_VALUE;

    private ReportPrepareSync() { }

    public static void localEdit(Long id) {
        if (id == null) return;
        int user = Globals.getCurrentUserId();
        long now = SystemClock.elapsedRealtime();
        onMain(() -> {
            if (user != Globals.getCurrentUserId()) return;
            ensureUser();
            POLICY.edited(String.valueOf(id), now);
            // Read only after the caller's transaction. No full-table scan on every keystroke.
            MAIN.post(() -> {
                if (user != Globals.getCurrentUserId()) return;
                ReportPrepareDB row = RealmManager.INSTANCE.where(ReportPrepareDB.class)
                        .equalTo("iD", id).findFirst();
                if (row != null) POLICY.remaining(ReportPrepareRealm.uploadSnapshot(row), now);
                if (Globals.autoSend) armTimer(ReportPrepareUploadPolicy.QUIET_MS);
            });
        });
    }

    public static void deferredDownload(String dad2) {
        if (dad2 != null && !dad2.isEmpty()) DEFERRED_VISITS.add(dad2);
    }

    public static boolean hasPendingVisit(long dad2) {
        return RealmManager.INSTANCE.where(ReportPrepareDB.class)
                .equalTo("codeDad2", String.valueOf(dad2)).equalTo("uploadStatus", 1).count() > 0;
    }

    public static void flushVisit(long dad2) {
        onMain(() -> {
            ensureUser();
            String key = String.valueOf(dad2);
            if (SystemClock.elapsedRealtime() < retryAfter || !FORCED_VISITS.add(key)) return;
            upload(true, key, new ExchangeInterface.ExchangeResponseInterface() {
                @Override public <T> void onSuccess(List<T> data) { FORCED_VISITS.remove(key); }
                @Override public void onFailure(String error) { FORCED_VISITS.remove(key); }
            });
        });
    }

    public static void upload(boolean force, String dad2,
                              ExchangeInterface.ExchangeResponseInterface callback) {
        onMain(() -> {
            ensureUser();
            if (!force && callback == null && automaticQueued) return;
            boolean automatic = !force && callback == null;
            if (automatic) automaticQueued = true;
            int user = Globals.getCurrentUserId();
            enqueue(done -> {
                if (automatic) automaticQueued = false;
                if (user != Globals.getCurrentUserId()) {
                    fail(callback, "User changed");
                    done.run();
                    return;
                }
                startUpload(user, force, dad2, callback, done);
            }, error -> fail(callback, "Cannot start upload: " + error));
        });
    }

    private static void startUpload(int user, boolean force, String dad2,
                                    ExchangeInterface.ExchangeResponseInterface callback, Runnable done) {
        List<ReportPrepareServ> selected = new ArrayList<>();
        long now = SystemClock.elapsedRealtime();
        try {
            for (ReportPrepareServ row : RealmManager.getReportPrepareToUpload()) {
                long remaining = POLICY.remaining(row, now);
                if (dad2 != null && !dad2.equals(row.getCode_dad2())) continue;
                if (force || (Globals.autoSend && now >= retryAfter && remaining == 0)) selected.add(row);
            }
            if (selected.isEmpty()) {
                succeed(callback, Collections.emptyList());
                done.run();
                return;
            }
            Map<String, JsonElement> sent = new HashMap<>();
            for (ReportPrepareServ row : selected) {
                sent.put(row.getElement_id(), ReportPrepareUploadPolicy.snapshot(row));
            }
            JsonObject request = new JsonObject();
            request.addProperty("mod", "report_prepare");
            request.addProperty("act", "set_report_data");
            request.add("data", GSON.toJsonTree(selected));
            log("SEND force=" + force + ", ids=" + sent.keySet());
            Log.e("UPLOAD_DATA", "set_report_data: " + request);
            RetrofitBuilder.getRetrofitInterface().SEND_RP(RetrofitBuilder.contentType, request)
                    .enqueue(new Callback<ReportPrepareUploadResponse>() {
                        @Override public void onResponse(Call<ReportPrepareUploadResponse> call,
                                                         Response<ReportPrepareUploadResponse> response) {
                            try {
                                if (user != Globals.getCurrentUserId()) {
                                    fail(callback, "User changed; response ignored");
                                    return;
                                }
                                ReportPrepareUploadResponse body = response.body();
                                if (!response.isSuccessful() || body == null || !body.state || body.data == null) {
                                    retryAfter = SystemClock.elapsedRealtime() + 30_000L;
                                    fail(callback, "Upload failed: HTTP " + response.code()
                                            + ", error=" + (body == null ? "empty body" : body.error));
                                    return;
                                }
                                List<ReportPrepareUpdateResponseList> acknowledged = new ArrayList<>();
                                Set<String> acceptedIds = new HashSet<>();
                                for (ReportPrepareUploadList item : body.data) {
                                    if (item != null && item.state && sent.containsKey(item.elementId)) {
                                        acceptedIds.add(item.elementId);
                                    }
                                }
                                Set<Long> cleared = new HashSet<>(ReportPrepareRealm.acknowledgeUpload(sent, acceptedIds));
                                for (String id : acceptedIds) {
                                    boolean same = cleared.contains(Long.parseLong(id));
                                    if (same) {
                                        ReportPrepareUpdateResponseList result = new ReportPrepareUpdateResponseList();
                                        result.elementId = Long.parseLong(id);
                                        result.state = true;
                                        acknowledged.add(result);
                                    }
                                    log("ACK id=" + id + ", action="
                                            + (same ? "CLEAN" : "KEEP_PENDING_CHANGED_OR_DELETED"));
                                }
                                if (acceptedIds.size() != sent.size()) {
                                    retryAfter = SystemClock.elapsedRealtime() + 30_000L;
                                    log("PARTIAL sent=" + sent.keySet() + ", accepted=" + acceptedIds);
                                    fail(callback, "Some RP rows were not accepted");
                                } else {
                                    retryAfter = 0;
                                    succeed(callback, acknowledged);
                                }
                                reconcileDeferredVisits();
                                MAIN.post(ReportConductManager::flushPending);
                            } catch (RuntimeException e) {
                                retryAfter = SystemClock.elapsedRealtime() + 30_000L;
                                fail(callback, "Cannot acknowledge RP: " + e);
                            } finally {
                                done.run();
                            }
                        }

                        @Override public void onFailure(Call<ReportPrepareUploadResponse> call, Throwable t) {
                            try {
                                retryAfter = SystemClock.elapsedRealtime() + 30_000L;
                                fail(callback, "Upload failed: " + t);
                            } finally {
                                done.run();
                            }
                        }
                    });
        } catch (RuntimeException e) {
            retryAfter = SystemClock.elapsedRealtime() + 30_000L;
            fail(callback, "Cannot send RP: " + e);
            done.run();
        }
    }

    public static void download(JsonObject request, boolean advanceWatermark,
                                Callback<ReportPrepareServer> callback) {
        download(request, advanceWatermark, callback, () -> { }, rows -> { });
    }

    public static void download(JsonObject request, boolean advanceWatermark,
                                Callback<ReportPrepareServer> callback, Runnable requireCurrent,
                                Consumer<List<ReportPrepareDB>> beforeSave) {
        int user = Globals.getCurrentUserId();
        JsonObject snapshot = request.deepCopy();
        onMain(() -> enqueue(done -> {
            ensureUser();
            RnoVisitDataExchange.authorizeTargetedRequest(snapshot);
            Call<ReportPrepareServer> call = RetrofitBuilder.getRetrofitInterface()
                    .ReportPrepareServer_RESPONSE(RetrofitBuilder.contentType, snapshot);
            if (user != Globals.getCurrentUserId()) {
                try { callback.onFailure(call, new IllegalStateException("User changed")); }
                finally { done.run(); }
                return;
            }
            try {
                requireCurrent.run();
                call.enqueue(new Callback<ReportPrepareServer>() {
                    @Override public void onResponse(Call<ReportPrepareServer> call,
                                                     Response<ReportPrepareServer> response) {
                        try {
                            if (user != Globals.getCurrentUserId()) {
                                callback.onFailure(call, new IllegalStateException("User changed; response ignored"));
                                return;
                            }
                            requireCurrent.run();
                            ReportPrepareServer body = response.body();
                            if (response.isSuccessful() && body != null && Boolean.TRUE.equals(body.getState())
                                    && body.getList() != null) {
                                beforeSave.accept(body.getList());
                                boolean merged = ReportPrepareRealm.mergeServerRows(body.getList());
                                log("DOWNLOAD rows=" + body.getList().size() + ", fullyMerged=" + merged);
                                // Do not advance past rows deferred because of local edits.
                                if (advanceWatermark && merged) {
                                    SynchronizationTimetableDB table = RealmManager
                                            .getSynchronizationTimetableRowByTable("report_prepare");
                                    if (table != null) {
                                        table.setVpi_app(TrustedTime.syncWatermarkSec(table.getVpi_app(), 120));
                                        RealmManager.setToSynchronizationTimetableDB(table);
                                    }
                                }
                            }
                            callback.onResponse(call, response);
                        } catch (RuntimeException e) {
                            callback.onFailure(call, e);
                        } finally {
                            done.run();
                        }
                    }

                    @Override public void onFailure(Call<ReportPrepareServer> call, Throwable t) {
                        try { callback.onFailure(call, t); }
                        finally { done.run(); }
                    }
                });
            } catch (RuntimeException e) {
                try { callback.onFailure(call, e); }
                finally { done.run(); }
            }
        }, error -> callback.onFailure(null, error)));
    }

    private static void reconcileDeferredVisits() {
        for (String dad2 : new HashSet<>(DEFERRED_VISITS)) {
            if (RealmManager.INSTANCE.where(ReportPrepareDB.class).equalTo("codeDad2", dad2)
                    .equalTo("uploadStatus", 1).count() != 0) continue;
            DEFERRED_VISITS.remove(dad2);
            JsonObject request = new JsonObject();
            request.addProperty("mod", "report_prepare");
            request.addProperty("act", "list_data");
            request.addProperty("code_dad2", dad2);
            download(request, false, new Callback<ReportPrepareServer>() {
                @Override public void onResponse(Call<ReportPrepareServer> call, Response<ReportPrepareServer> response) {
                    if (!response.isSuccessful() || response.body() == null
                            || !Boolean.TRUE.equals(response.body().getState())) DEFERRED_VISITS.add(dad2);
                }
                @Override public void onFailure(Call<ReportPrepareServer> call, Throwable t) {
                    DEFERRED_VISITS.add(dad2);
                    log("RECONCILE dad2=" + dad2 + ", error=" + t);
                }
            });
        }
    }

    private static void scheduleNext() {
        MAIN.removeCallbacks(TIMER);
        nextTickAt = Long.MAX_VALUE;
        if (!Globals.autoSend || Globals.getCurrentUserId() <= 0) return;
        try {
            long now = SystemClock.elapsedRealtime();
            long delay = Long.MAX_VALUE;
            Set<String> ids = new HashSet<>();
            for (ReportPrepareServ row : RealmManager.getReportPrepareToUpload()) {
                ids.add(row.getElement_id());
                delay = Math.min(delay, POLICY.remaining(row, now));
            }
            POLICY.retain(ids);
            if (delay != Long.MAX_VALUE && !automaticQueued) {
                armTimer(Math.max(delay, retryAfter - now));
            }
        } catch (RuntimeException e) {
            log("SCHEDULE failed; cron will retry: " + e);
        }
    }

    private static void armTimer(long delay) {
        long at = SystemClock.elapsedRealtime() + Math.max(0, delay);
        if (at >= nextTickAt) return;
        MAIN.removeCallbacks(TIMER);
        nextTickAt = at;
        MAIN.postDelayed(TIMER, Math.max(0, delay));
    }

    private static void ensureUser() {
        int user = Globals.getCurrentUserId();
        if (policyUser != user) {
            policyUser = user;
            POLICY.clear();
            DEFERRED_VISITS.clear();
            FORCED_VISITS.clear();
            retryAfter = 0;
        }
    }

    private static void onMain(Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) action.run();
        else ExchangeWork.dispatch(MAIN::post, action);
    }

    private static void enqueue(Consumer<Runnable> action, Consumer<RuntimeException> onError) {
        ExchangeRunTracker.Ticket ticket = ExchangeWork.TRACKER.beginWork();
        QUEUE.submit(done -> action.accept(() -> {
            try {
                done.run();
                scheduleNext();
            } finally {
                ticket.close();
            }
        }), error -> {
            try { onError.accept(error); }
            finally {
                try { scheduleNext(); }
                finally { ticket.close(); }
            }
        });
    }

    private static void fail(ExchangeInterface.ExchangeResponseInterface callback, String error) {
        log(error);
        if (callback != null) {
            try { callback.onFailure(error); }
            catch (RuntimeException e) { log("Failure callback failed: " + e); }
        }
    }

    private static void succeed(ExchangeInterface.ExchangeResponseInterface callback,
                                List<ReportPrepareUpdateResponseList> rows) {
        if (callback != null) {
            try { callback.onSuccess(rows); }
            catch (RuntimeException e) { log("Success callback failed: " + e); }
        }
    }

    private static void log(String message) {
        Globals.writeToMLOG("INFO", TAG, message);
    }
}
