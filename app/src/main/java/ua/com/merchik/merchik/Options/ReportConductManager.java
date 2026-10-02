package ua.com.merchik.merchik.Options;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.merchik.merchik.Activities.MyApplication;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TestJsonUpload.StratEndWork.StartEndData;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

public final class ReportConductManager {
    private static final String TAG = "ReportConduct";
    private static final Gson GSON = new Gson();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final Set<String> IN_FLIGHT = new HashSet<>();
    private static final long RETRY_MS = 60_000L;

    private ReportConductManager() { }

    private static SharedPreferences prefs() {
        return MyApplication.getAppContext().getSharedPreferences("report_conduct", Context.MODE_PRIVATE);
    }

    private static String key(int userId, long dad2) {
        return userId + ":" + dad2;
    }

    public static ReportConductState get(long dad2) {
        return get(Globals.getCurrentUserId(), dad2);
    }

    public static boolean isAwaitingServer(long dad2) {
        ReportConductState state = get(dad2);
        return state != null && (state.stage == ReportConductState.Stage.READY
                || state.stage == ReportConductState.Stage.ACCEPTED);
    }

    private static ReportConductState get(int userId, long dad2) {
        String json = prefs().getString(key(userId, dad2), null);
        if (json == null) return null;
        try {
            return GSON.fromJson(json, ReportConductState.class);
        } catch (RuntimeException e) {
            log("ERROR", dad2, "invalid saved state: " + e);
            return null;
        }
    }

    private static void save(ReportConductState state) {
        // Persist before sending: process death must not discard an approved command.
        if (!prefs().edit().putString(key(state.userId, state.dad2), GSON.toJson(state)).commit()) {
            throw new IllegalStateException("Cannot persist report conduct state");
        }
    }

    public static boolean begin(long dad2) {
        ReportConductState previous = get(dad2);
        if (previous != null && previous.stage == ReportConductState.Stage.READY) {
            flushPending();
            return false;
        }
        int userId = Globals.getCurrentUserId();
        if (userId <= 0 || dad2 <= 0) throw new IllegalStateException("Missing user or visit");
        ReportConductState state = new ReportConductState();
        state.userId = userId;
        state.dad2 = dad2;
        save(state);
        log("INFO", dad2, "WAITING_CHECK");
        return true;
    }

    public static void awaitingConfirmation(long dad2) {
        ReportConductState state = get(dad2);
        if (state != null && state.awaitingDecision()) {
            state.stage = ReportConductState.Stage.WAITING_CONFIRMATION;
            save(state);
            log("INFO", dad2, "WAITING_CONFIRMATION");
        }
    }

    public static void cancelDecision(long dad2) {
        ReportConductState state = get(dad2);
        if (state != null && state.awaitingDecision()) {
            if (!prefs().edit().remove(key(state.userId, dad2)).commit()) {
                log("ERROR", dad2, "Could not clear dismissed decision");
            }
            log("INFO", dad2, "decision dismissed; no command authorized");
        }
    }

    public static void approve(long dad2) {
        ReportConductState state = get(dad2);
        if (state == null || !state.awaitingDecision()) {
            throw new IllegalStateException("Conduct decision is no longer active");
        }
        WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(dad2);
        if (wp == null) throw new IllegalStateException("Visit is no longer available");
        state.visitStart = wp.getVisit_start_dt();
        state.visitEnd = wp.getVisit_end_dt();
        state.clientStart = wp.getClient_start_dt();
        state.clientEnd = wp.getClient_end_dt();
        state.workDataUploaded = !wp.startUpdate;
        state.approve();
        save(state);
        log("INFO", dad2, "READY: command saved");
    }

    public static void workDataUploaded(int userId, List<StartEndData> snapshot) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            MAIN.post(() -> workDataUploaded(userId, snapshot));
            return;
        }
        for (StartEndData sent : snapshot) {
            try {
                ReportConductState state = get(userId, Long.parseLong(sent.code_dad2));
                if (state == null || state.stage != ReportConductState.Stage.READY || state.workDataUploaded) continue;
                if (String.valueOf(state.visitStart).equals(sent.visit_start_dt)
                        && String.valueOf(state.visitEnd).equals(sent.visit_end_dt)
                        && String.valueOf(state.clientStart).equals(sent.client_start_dt)
                        && String.valueOf(state.clientEnd).equals(sent.client_end_dt)) {
                    state.workDataUploaded = true;
                    save(state);
                    log("INFO", state.dad2, "work-time upload succeeded");
                }
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", TAG + "/workDataUploaded", e.toString());
            }
        }
    }

    public static void resultShown(long dad2, String revision) {
        try {
            ReportConductState state = get(dad2);
            if (state != null && state.resultPending && revision.equals(state.revision)) {
                state.resultPending = false;
                save(state);
            }
        } catch (Exception e) {
            log("ERROR", dad2, "saving result acknowledgement: " + e);
        }
    }

    public static void flushPending() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            MAIN.post(ReportConductManager::flushPending);
            return;
        }
        int userId = Globals.getCurrentUserId();
        if (userId <= 0) return;
        for (Map.Entry<String, ?> entry : new ArrayList<>(prefs().getAll().entrySet())) {
            if (!entry.getKey().startsWith(userId + ":") || !(entry.getValue() instanceof String)) continue;
            try {
                ReportConductState state = GSON.fromJson((String) entry.getValue(), ReportConductState.class);
                if (state == null || state.userId != userId || !state.canSend(System.currentTimeMillis())) continue;
                String key = key(userId, state.dad2);
                if (IN_FLIGHT.contains(key)) continue;
                WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(state.dad2);
                if (wp == null) continue;
                if (wp.getStatus() == 1) {
                    state.complete(true, "Звіт уже проведено.");
                    save(state);
                    ReportConductUi.showResult(state.dad2);
                    continue;
                }
                if (!state.matchesWorkTimes(wp.getVisit_start_dt(), wp.getVisit_end_dt(),
                        wp.getClient_start_dt(), wp.getClient_end_dt())) {
                    state.stage = ReportConductState.Stage.WAITING_CHECK;
                    save(state);
                    log("INFO", state.dad2, "work times changed; recheck required");
                    continue;
                }
                // A successful repeat can report no changed fields: times may already be on the server.
                if (!state.workDataUploaded && wp.startUpdate) continue;
                state.retryAfter = System.currentTimeMillis() + RETRY_MS;
                save(state);
                IN_FLIGHT.add(key);
                log("INFO", state.dad2, "SEND document_complete");
                try {
                    Exchange.conductingOnServerWpData(state.dad2, new Exchange.ConductCallback() {
                        @Override public void onAccepted(String notice) { finish(state, true, false, notice); }
                        @Override public void onRejected(String error) { finish(state, false, false, error); }
                        @Override public void onRetry(String error) { finish(state, false, true, error); }
                    });
                } catch (Exception e) {
                    finish(state, false, true, e.toString());
                }
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", TAG + "/flush", e.toString());
            }
        }
    }

    private static void finish(ReportConductState sent, boolean accepted, boolean retry, String message) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            MAIN.post(() -> finish(sent, accepted, retry, message));
            return;
        }
        IN_FLIGHT.remove(key(sent.userId, sent.dad2));
        try {
            ReportConductState current = get(sent.userId, sent.dad2);
            if (current == null || !sent.revision.equals(current.revision)) return;
            if (retry) {
                current.retryAfter = System.currentTimeMillis() + RETRY_MS;
                current.message = message;
            } else {
                current.complete(accepted, message);
            }
            save(current);
            log(retry || !accepted ? "ERROR" : "INFO", sent.dad2,
                    (retry ? "RETRY" : current.stage.name()) + ": " + message);
            if (!retry && sent.userId == Globals.getCurrentUserId()) ReportConductUi.showResult(sent.dad2);
        } catch (Exception e) {
            log("ERROR", sent.dad2, "saving response: " + e);
        }
    }

    public static void log(String level, long dad2, String message) {
        Globals.writeToMLOG(level, TAG, "dad2=" + dad2 + ", " + message);
    }
}
