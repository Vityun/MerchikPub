package ua.com.merchik.merchik.ServerExchange;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import ua.com.merchik.merchik.Activities.MyApplication;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.ReportConductManager;
import ua.com.merchik.merchik.data.Database.Room.Chat.ChatSDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;

/** Durable inbox shared by WebSocket and incremental chat downloads. No conduct authorization. */
public final class ReportConductRecovery {
    private static final Gson JSON = new Gson();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final Runnable WAKE = ReportConductManager::flushPending;

    private ReportConductRecovery() { }

    private static SharedPreferences prefs() {
        return MyApplication.getAppContext().getSharedPreferences("report_conduct_recovery", Context.MODE_PRIVATE);
    }

    public static void requireAccount(int user, AppDatabase db, String session) {
        if (user <= 0 || user != Globals.getCurrentUserId() || db == null || db != RoomManager.SQL_DB
                || session == null || session.isEmpty() || !Objects.equals(session, Globals.getCurrentSessionId())) {
            throw new IllegalStateException("Chat recovery account changed");
        }
    }

    // Commit the intent before advancing the chat cursor. Replayed messages retain their receipt.
    public static synchronized void stage(int user, AppDatabase db, String session, List<ChatSDB> messages) {
        requireAccount(user, db, session);
        SharedPreferences preferences = prefs();
        SharedPreferences.Editor editor = preferences.edit();
        boolean hasFailure = false;
        for (ChatSDB row : messages) {
            ReportConductFailure failure = ReportConductFailure.parse(user, row.id, row.userId,
                    row.userIdTo, row.dt, row.msg);
            if (failure == null) continue;
            hasFailure = true;
            if (preferences.contains(failure.key())) continue;
            editor.putString(failure.key(), JSON.toJson(failure));
        }
        // A failed commit may already have updated the in-memory map. Retry its disk write too.
        if (hasFailure && !editor.commit()) throw new IllegalStateException("Cannot persist chat recovery");
    }

    public static void wake() { MAIN.post(WAKE); }

    /** Called on the main thread before any queued document command can be sent. */
    public static void resume() {
        if (Looper.myLooper() != Looper.getMainLooper()) throw new IllegalStateException("Main thread required");
        int user = Globals.getCurrentUserId();
        if (user <= 0 || RealmManager.INSTANCE == null || RealmManager.INSTANCE.isClosed()
                || RoomManager.SQL_DB == null) return;
        boolean pending = false;
        for (Map.Entry<String, ?> entry : prefs().getAll().entrySet()) {
            if (!entry.getKey().startsWith(user + ":") || !(entry.getValue() instanceof String)) continue;
            pending = true;
            try {
                ReportConductFailure failure = JSON.fromJson((String) entry.getValue(), ReportConductFailure.class);
                if (failure == null || failure.userId != user || !entry.getKey().equals(failure.key())) {
                    throw new IllegalStateException("Invalid recovery journal");
                }
                WpDataDB wp = resolve(failure);
                if (wp == null) continue;
                ReportConductManager.onChatConductFailure(wp.getCode_dad2(), failure.messageTime);
                if (!VisitUploadSync.retryAfterConductFailure(user, wp.getCode_dad2(), failure.messageId)) continue;
                acknowledge(failure);
                Globals.writeToMLOG("INFO", "ReportConductRecovery", "queued message=" + failure.messageId
                        + ", dad2=" + wp.getCode_dad2() + ", upload_only=true");
            } catch (Exception error) {
                Globals.writeToMLOG("ERROR", "ReportConductRecovery", "Recovery retained: " + error.getClass().getSimpleName());
            }
        }
        MAIN.removeCallbacks(WAKE);
        if (pending) MAIN.postDelayed(WAKE, 60_000);
    }

    public static boolean blocksConduct(WpDataDB wp) {
        int user = Globals.getCurrentUserId();
        for (Map.Entry<String, ?> entry : prefs().getAll().entrySet()) {
            if (!entry.getKey().startsWith(user + ":") || !(entry.getValue() instanceof String)) continue;
            try {
                ReportConductFailure failure = JSON.fromJson((String) entry.getValue(), ReportConductFailure.class);
                if (failure != null && failure.userId == user
                        && failure.matchesVisit(wp.getDoc_num_otchet(), wp.getCode_dad2())) return true;
            } catch (RuntimeException ignored) {
                // resume logs corrupt entries; an unknown target must not block unrelated reports.
            }
        }
        return false;
    }

    private static WpDataDB resolve(ReportConductFailure failure) {
        List<WpDataDB> matches = RealmManager.INSTANCE.where(WpDataDB.class)
                .equalTo("doc_num_otchet", failure.document).findAll();
        WpDataDB result = null;
        for (WpDataDB wp : matches) {
            if (!failure.matchesVisit(wp.getDoc_num_otchet(), wp.getCode_dad2())) continue;
            if (result != null && result.getCode_dad2() != wp.getCode_dad2()) {
                throw new IllegalStateException("Ambiguous report visit");
            }
            result = wp;
        }
        return result == null ? null : RealmManager.INSTANCE.copyFromRealm(result);
    }

    private static synchronized void acknowledge(ReportConductFailure failure) {
        // Keep just the receipt, not the message body; overlapping downloads must not requeue it.
        if (!prefs().edit().putBoolean(failure.key(), true).commit()) {
            throw new IllegalStateException("Cannot persist chat recovery receipt");
        }
    }
}
