package ua.com.merchik.merchik.ServerExchange;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Activities.MyApplication;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.ReportConductManager;
import ua.com.merchik.merchik.PhotoReports;
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.UploadToServ.LogUploadToServ;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/** Explicit work-end/conduct upload, independent of the periodic full exchange and autoSend. */
public final class VisitUploadSync {
    private static final Gson JSON = new Gson();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final long RETRY_MS = 60_000;
    private static final Runnable WAKE = VisitUploadSync::resume;
    private static boolean running;
    private static Run active;
    private static final java.util.concurrent.atomic.AtomicBoolean TASK_COMMENTS = new java.util.concurrent.atomic.AtomicBoolean();
    private static final java.util.concurrent.atomic.AtomicBoolean ACHIEVEMENTS = new java.util.concurrent.atomic.AtomicBoolean();
    private VisitUploadSync() { }

    static boolean acquireAchievements() { return ACHIEVEMENTS.compareAndSet(false, true); }
    static void releaseAchievements() { ACHIEVEMENTS.set(false); }

    public static boolean acquireTaskComments() { return TASK_COMMENTS.compareAndSet(false, true); }
    public static void releaseTaskComments() { TASK_COMMENTS.set(false); }

    private static SharedPreferences prefs() {
        return MyApplication.getAppContext().getSharedPreferences("visit_upload", Context.MODE_PRIVATE);
    }

    private static String key(int user, long dad2) { return user + ":" + dad2; }

    private static VisitUploadBatch read(int user, long dad2) {
        String value = prefs().getString(key(user, dad2), null);
        return value == null ? null : JSON.fromJson(value, VisitUploadBatch.class);
    }

    static VisitUploadBatch achievementReceipts(int user, long dad2) {
        // The visit journal commits the HTTP receipt before applying it to Room.
        return read(user, dad2);
    }

    private static void save(VisitUploadBatch batch) {
        if (!prefs().edit().putString(key(batch.userId, batch.dad2), JSON.toJson(batch)).commit()) {
            throw new IllegalStateException("Cannot persist visit upload");
        }
    }

    public static void request(long dad2) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            int user = Globals.getCurrentUserId();
            MAIN.post(() -> { if (user == Globals.getCurrentUserId()) request(dad2); });
            return;
        }
        int user = Globals.getCurrentUserId();
        if (user <= 0 || dad2 <= 0) throw new IllegalStateException("Missing upload user/visit");
        VisitUploadBatch batch = active != null && active.current() && active.batch.dad2 == dad2
                ? active.batch : read(user, dad2);
        if (batch == null) {
            batch = new VisitUploadBatch();
            batch.userId = user;
            batch.dad2 = dad2;
        }
        WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(dad2);
        if (wp == null) throw new IllegalStateException("Visit no longer available");
        batch.merge(VisitUploadRows.collect(wp, batch));
        batch.complete = false;
        save(batch);
        MAIN.post(WAKE);
    }

    public static void ensureRequested(long dad2) {
        if (read(Globals.getCurrentUserId(), dad2) == null) request(dad2);
        else MAIN.post(WAKE);
    }

    static boolean retryAfterConductFailure(int user, long dad2, int messageId) {
        if (Looper.myLooper() != Looper.getMainLooper() || user != Globals.getCurrentUserId()) {
            throw new IllegalStateException("Recovery account/thread changed");
        }
        // Finish an in-flight request first so its old ACK cannot acknowledge the replayed rows.
        if (running) return false;
        VisitUploadBatch batch = read(user, dad2);
        if (batch == null) {
            batch = new VisitUploadBatch();
            batch.userId = user;
            batch.dad2 = dad2;
        }
        if (batch.recoveryMessages.contains(messageId)) {
            save(batch);
            return true;
        }
        WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(dad2);
        if (wp == null) return false;
        applyAcknowledgements(batch);
        batch.merge(VisitUploadRows.collect(wp, batch));
        batch.retryAfterConductFailure(messageId);
        // Snapshot and event receipt share one durable write, closing the replay-after-crash gap.
        save(batch);
        MAIN.post(WAKE);
        return true;
    }

    public static boolean ready(long dad2) {
        VisitUploadBatch batch = read(Globals.getCurrentUserId(), dad2);
        if (batch == null || !batch.complete) return false;
        WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(dad2);
        if (wp == null) return false;
        List<StackPhotoDB> photos = VisitUploadRows.photos(wp);
        if (batch.merge(VisitUploadRows.collect(wp, batch)) || !VisitPhotoUploadPolicy.allReady(batch, photos)
                || !batch.allAcknowledged(VisitPhotoUploadPolicy.pendingServerIds(batch, photos))) {
            batch.complete = false;
            save(batch);
            MAIN.post(WAKE);
            return false;
        }
        return true;
    }

    public static void resume() {
        if (Looper.myLooper() != Looper.getMainLooper()) { MAIN.post(WAKE); return; }
        ReportConductRecovery.resume();
        if (running || Globals.getCurrentUserId() <= 0) return;
        MAIN.removeCallbacks(WAKE);
        boolean pending = false;
        for (Map.Entry<String, ?> entry : prefs().getAll().entrySet()) {
            if (!entry.getKey().startsWith(Globals.getCurrentUserId() + ":")) continue;
            try {
                VisitUploadBatch batch = JSON.fromJson((String) entry.getValue(), VisitUploadBatch.class);
                if (batch.complete) continue;
                pending = true;
                if (batch.retryAfter > System.currentTimeMillis()) continue;
                Run next = new Run(batch);
                running = true;
                active = next;
                active.start();
                return;
            } catch (Exception error) {
                Globals.writeToMLOG("ERROR", "VisitUpload", "Cannot resume upload; kept queued: " + error.getClass().getSimpleName());
            }
        }
        if (pending) MAIN.postDelayed(WAKE, RETRY_MS);
    }

    private static void applyAcknowledgements(VisitUploadBatch batch) {
        for (VisitUploadBatch.Row row : batch.rows.values()) {
            if (row.acknowledgement != null && !row.applied) {
                VisitUploadRows.acknowledge(row, row.acknowledgement);
                row.applied = true;
                save(batch);
            }
        }
    }

    private static final class Run {
        final VisitUploadBatch batch;
        final Object room = RoomManager.SQL_DB;
        final Object realm = RealmManager.INSTANCE;
        final String session = Globals.getCurrentSessionId();
        boolean finished;
        boolean photosBlocked;
        boolean ownsTaskComments;
        boolean ownsAchievements;
        final Set<Integer> attemptedPhotoLookups = new HashSet<>();
        final Set<VisitUploadBatch.Kind> deferred = new HashSet<>();

        Run(VisitUploadBatch batch) { this.batch = batch; }

        boolean current() {
            return !finished && batch.userId == Globals.getCurrentUserId() && room == RoomManager.SQL_DB
                    && realm == RealmManager.INSTANCE && RealmManager.INSTANCE != null && !RealmManager.INSTANCE.isClosed()
                    && Objects.equals(session, Globals.getCurrentSessionId());
        }

        void start() {
            try {
                if (!current()) { end(); return; }
                applyAcknowledgements(batch);
                WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(batch.dad2);
                if (wp == null) { fail("visit_missing"); return; }
                batch.merge(VisitUploadRows.collect(wp, batch));
                VisitUploadRows.resolvePhotoReferences(batch);
                save(batch);
                List<StackPhotoDB> photos = VisitUploadRows.photos(wp);
                Set<Integer> files = new HashSet<>();
                for (StackPhotoDB photo : photos) {
                    if (!VisitPhotoUploadPolicy.ready(batch, photo)) files.add(photo.getId());
                }
                if (!files.isEmpty()) {
                    log("SEND photos=" + files.size());
                    new PhotoReports(MyApplication.getAppContext()).uploadVisit(files, (photo, response) -> guarded(() -> {
                        boolean received = VisitPhotoUploadPolicy.record(batch, photo, response.isSuccessful(), response.body());
                        if (received) save(batch);
                        log("PHOTO_HTTP id=" + photo.getId() + ", http=" + response.code()
                                + ", conductReceipt=" + received + "; normal photo state unchanged");
                    }), retry -> guarded(() -> {
                        if (!current()) { end(); return; }
                        WpDataDB latest = WpDataRealm.getWpDataRowByDad2Id(batch.dad2);
                        if (latest == null) { fail("visit_missing"); return; }
                        for (StackPhotoDB photo : VisitUploadRows.photos(latest)) {
                            if (!VisitPhotoUploadPolicy.ready(batch, photo)) {
                                deferPhotos("photo_http_not_confirmed id=" + photo.getId());
                                return;
                            }
                        }
                        MAIN.post(this::start);
                    }));
                    return;
                }
                sendNext();
            } catch (Exception error) { fail("prepare_" + error.getClass().getSimpleName()); }
        }

        void sendNext() {
            if (!current()) { end(); return; }
            try {
                applyAcknowledgements(batch);
                WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(batch.dad2);
                if (wp == null) { fail("visit_missing"); return; }
                // Capture edits made during the preceding HTTP request before deciding to conduct.
                batch.merge(VisitUploadRows.collect(wp, batch));
                VisitUploadRows.resolvePhotoReferences(batch);
                save(batch);
                List<StackPhotoDB> photos = VisitUploadRows.photos(wp);
                Set<String> pendingPhotoIds = VisitPhotoUploadPolicy.pendingServerIds(batch, photos);
                if (!photosBlocked && !VisitPhotoUploadPolicy.allReady(batch, photos)) { MAIN.post(this::start); return; }
                List<VisitUploadBatch.Row> sent = new ArrayList<>(batch.next(deferred, pendingPhotoIds));
                if (sent.isEmpty()) {
                    Set<Integer> lookup = new HashSet<>();
                    for (StackPhotoDB photo : photos) {
                        if (photo.getCode_dad2() == batch.dad2 && VisitPhotoUploadPolicy.requiresWarehouseComment(photo)
                                && pendingPhotoIds.contains(String.valueOf(photo.getId()))
                                && attemptedPhotoLookups.add(photo.getId())) lookup.add(photo.getId());
                    }
                    if (!lookup.isEmpty()) {
                        log("RESOLVE FTS photo IDs local=" + lookup);
                        PhotoHashExchange.refreshVisit(lookup, () -> guarded(() -> MAIN.post(this::sendNext)),
                                () -> guarded(() -> fail("fts_photo_id_lookup_failed local=" + lookup)));
                        return;
                    }
                    if (!batch.allAcknowledged(pendingPhotoIds) || !VisitPhotoUploadPolicy.allReady(batch, photos)) {
                        for (VisitUploadBatch.Row row : batch.rows.values()) {
                            if (row.requiredWarehouseComment && (row.acknowledgement == null || !row.applied)) {
                                log("WAIT FTS comment local=" + row.id + ", reason="
                                        + (row.missingCommentField() == null ? "set_comment_unconfirmed" : row.missingCommentField()));
                            }
                        }
                        fail("unconfirmed_queues=" + deferred + ", photos=" + photosBlocked);
                        return;
                    }
                    VisitUploadRows.acknowledgeAnswers(batch);
                    batch.complete = true;
                    batch.retryAfter = 0;
                    save(batch);
                    log("CONDUCT_READY: required data confirmed, photos awaiting server ID=" + pendingPhotoIds.size()
                            + "; photo metadata stays in normal queues; conduct requires a separate authorized command");
                    end();
                    MAIN.post(ReportConductManager::flushPending);
                    return;
                }
                VisitUploadBatch.Kind kind = sent.get(0).kind;
                if (kind == VisitUploadBatch.Kind.TAR
                        && !VisitUploadBatch.positiveId(sent.get(0).data.get("code_dad2"))) {
                    defer(kind, "task_server_identity_missing");
                    return;
                }
                if (kind == VisitUploadBatch.Kind.ACHIEVEMENT) {
                    ownsAchievements = acquireAchievements();
                    if (!ownsAchievements) { defer(kind, "another_uploader_running"); return; }
                    // Check Room after acquiring ownership: the periodic uploader may just have committed.
                    Map<String, Integer> storedIds = new HashMap<>();
                    for (AchievementsSDB row : RoomManager.SQL_DB.achievementsDao().getByDad2(batch.dad2)) {
                        storedIds.put(String.valueOf(row.id), row.serverId);
                    }
                    sent.clear();
                    sent.addAll(batch.pendingAchievementCreates(storedIds));
                    save(batch);
                    if (sent.isEmpty()) {
                        releaseAchievementLease();
                        for (VisitUploadBatch.Row row : batch.rows.values()) {
                            if (row.kind == kind && row.acknowledgement == null) {
                                defer(kind, "missing_local_row element_id=" + row.id);
                                return;
                            }
                        }
                        MAIN.post(this::sendNext);
                        return;
                    }
                }
                Call<JsonObject> call;
                if (kind == VisitUploadBatch.Kind.LOCATION) {
                    HashMap<String, String> map = new HashMap<>();
                    for (VisitUploadBatch.Row row : sent) {
                        String gp = VisitUploadBatch.string(row.data, "gp");
                        if (gp.isEmpty()) { defer(kind, "location_payload_missing id=" + row.id); return; }
                        map.put("gp[" + row.id + "]", gp);
                    }
                    call = RetrofitBuilder.getRetrofitInterface().UPLOAD_LOG_MP("location", "track", map);
                } else if (kind == VisitUploadBatch.Kind.LOG) {
                    ArrayList<LogUploadToServ> logs = new ArrayList<>();
                    for (VisitUploadBatch.Row row : sent) {
                        LogUploadToServ log = JSON.fromJson(row.data, LogUploadToServ.class);
                        log.setUpload_date(String.valueOf(System.currentTimeMillis() / 1000));
                        logs.add(log);
                    }
                    call = RetrofitBuilder.getRetrofitInterface().LOG("log", "save", logs);
                } else {
                    call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, VisitUploadBatch.request(sent));
                }
                log("SEND " + kind + ", rows=" + sent.size());
                if (kind == VisitUploadBatch.Kind.TAR_COMMENT) {
                    ownsTaskComments = acquireTaskComments();
                    if (!ownsTaskComments) { defer(kind, "another_uploader_running"); return; }
                }
                call.enqueue(new Callback<JsonObject>() {
                    @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        releaseCommentLease();
                        if (!current()) { end(); return; }
                        try {
                            int accepted = 0;
                            if (response.isSuccessful()) {
                                for (VisitUploadBatch.Row row : sent) {
                                    JsonObject ack = VisitUploadBatch.accepted(row, response.body());
                                    if (ack == null) continue;
                                    if (batch.acknowledge(row, ack)) {
                                        accepted++;
                                        if (kind == VisitUploadBatch.Kind.ACHIEVEMENT) {
                                            log("ACK ACHIEVEMENT element_id=" + row.id + " id=" + VisitUploadBatch.string(ack, "id"));
                                        }
                                    }
                                }
                            }
                            save(batch);
                            applyAcknowledgements(batch);
                            if (accepted != sent.size()) {
                                defer(kind, "http=" + response.code() + " accepted=" + accepted + "/" + sent.size());
                            } else MAIN.post(Run.this::sendNext);
                        } catch (Exception error) { fail(kind + " ack_" + error.getClass().getSimpleName()); }
                        finally { releaseAchievementLease(); }
                    }
                    @Override public void onFailure(Call<JsonObject> call, Throwable error) {
                        releaseCommentLease();
                        releaseAchievementLease();
                        defer(kind, "transport_" + error.getClass().getSimpleName());
                    }
                });
            } catch (Exception error) { fail("send_" + error.getClass().getSimpleName()); }
        }

        void deferPhotos(String reason) {
            if (!current()) { end(); return; }
            photosBlocked = true;
            deferred.add(VisitUploadBatch.Kind.PHOTO_COMMENT);
            deferred.add(VisitUploadBatch.Kind.PHOTO_DVI);
            deferred.add(VisitUploadBatch.Kind.PHOTO_SCORE);
            deferred.add(VisitUploadBatch.Kind.ACHIEVEMENT);
            deferred.add(VisitUploadBatch.Kind.TAR_CREATE);
            deferred.add(VisitUploadBatch.Kind.TAR_COMMENT);
            deferred.add(VisitUploadBatch.Kind.PLANOGRAM);
            log("WAIT " + reason);
            MAIN.post(this::sendNext);
        }

        void defer(VisitUploadBatch.Kind kind, String reason) {
            if (!current()) { end(); return; }
            deferred.add(kind);
            if (kind == VisitUploadBatch.Kind.TAR_CREATE) {
                deferred.add(VisitUploadBatch.Kind.TAR);
                deferred.add(VisitUploadBatch.Kind.TAR_COMMENT);
            }
            log("WAIT " + kind + " " + reason);
            MAIN.post(this::sendNext);
        }

        void guarded(Runnable action) {
            if (!current()) { end(); return; }
            try { action.run(); }
            catch (Exception error) { fail("callback_" + error.getClass().getSimpleName()); }
        }

        void fail(String reason) {
            if (finished) return;
            if (current()) {
                batch.complete = false;
                batch.retryAfter = System.currentTimeMillis() + RETRY_MS;
                try { save(batch); }
                catch (Exception error) { log("PERSIST_ERROR; original journal retained"); }
                log("RETRY " + reason + "; document_complete not sent");
            }
            end();
        }

        void releaseAchievementLease() {
            if (ownsAchievements) { ownsAchievements = false; releaseAchievements(); }
        }

        void releaseCommentLease() {
            if (ownsTaskComments) { ownsTaskComments = false; releaseTaskComments(); }
        }

        void log(String message) { ReportConductManager.log("INFO", batch.dad2, "VisitUpload " + message); }

        void end() {
            if (finished) return;
            finished = true;
            releaseCommentLease();
            releaseAchievementLease();
            running = false;
            if (active == this) active = null;
            MAIN.post(WAKE);
        }
    }
}
