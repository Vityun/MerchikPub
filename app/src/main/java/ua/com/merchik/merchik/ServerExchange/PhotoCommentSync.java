package ua.com.merchik.merchik.ServerExchange;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.ReportConductManager;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.PhotoInfoResponseList;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoInformation;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoInformationData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;

/** Serializes comment uploads without holding an Activity or a managed Realm snapshot. */
public final class PhotoCommentSync {
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final PhotoCommentUploadPolicy POLICY = new PhotoCommentUploadPolicy();
    private static boolean forced;
    private static long nextTickAt = Long.MAX_VALUE;
    private static final Runnable TIMER = () -> {
        nextTickAt = Long.MAX_VALUE;
        send();
    };

    private PhotoCommentSync() { }

    public static void changed() {
        request(false, true);
    }

    public static void flush() {
        request(true, false);
    }

    public static boolean hasPendingVisit(long dad2) {
        // Include comments awaiting a server ID, not just rows eligible for set_comment.
        return RealmManager.INSTANCE.where(StackPhotoDB.class)
                .equalTo("code_dad2", dad2).equalTo("commentUpload", true).count() > 0;
    }

    private static void request(boolean force, boolean recheckConduct) {
        int user = Globals.getCurrentUserId();
        MAIN.post(() -> {
            if (user <= 0 || user != Globals.getCurrentUserId()) return;
            ensureUser();
            forced |= force;
            schedule();
            if (recheckConduct) ReportConductManager.flushPending();
        });
    }

    private static void ensureUser() {
        if (POLICY.useUser(Globals.getCurrentUserId())) {
            MAIN.removeCallbacks(TIMER);
            nextTickAt = Long.MAX_VALUE;
            forced = false;
        }
    }

    private static void schedule() {
        if ((!forced && !Globals.autoSend) || POLICY.isRunning()) return;
        long delay = POLICY.remaining(SystemClock.elapsedRealtime());
        long at = SystemClock.elapsedRealtime() + delay;
        if (at >= nextTickAt) return;
        MAIN.removeCallbacks(TIMER);
        nextTickAt = at;
        MAIN.postDelayed(TIMER, delay);
    }

    private static void send() {
        ensureUser();
        if (!forced && !Globals.autoSend) return;
        int user = Globals.getCurrentUserId();
        long token = POLICY.start(SystemClock.elapsedRealtime());
        if (token == 0) return;
        try {
            Exchange exchange = new Exchange();
            final PhotoInformation request = exchange.getPhotoInfoToUpload(Exchange.UploadPhotoInfo.COMMENT);
            if (request == null || request.data == null || request.data.isEmpty()) {
                POLICY.finish(token, true, SystemClock.elapsedRealtime());
                forced = false;
                return;
            }
            exchange.sendPhotoInformation(request, new ExchangeInterface.ExchangeResponseInterface() {
                @Override public <T> void onSuccess(List<T> data) {
                    MAIN.post(() -> acknowledge(user, token, request, (List<PhotoInfoResponseList>) data));
                }

                @Override public void onFailure(String error) {
                    MAIN.post(() -> failed(user, token, error));
                }
            });
        } catch (Exception e) {
            failed(user, token, e.toString());
        }
    }

    private static boolean owns(int user, long token) {
        ensureUser();
        return user == Globals.getCurrentUserId() && POLICY.owns(token);
    }

    private static void acknowledge(int user, long token, PhotoInformation request,
                                    List<PhotoInfoResponseList> response) {
        if (!owns(user, token)) return;
        try {
            if (response != null) {
                for (PhotoInfoResponseList item : response) {
                    if (item != null) Globals.writeToMLOG("INFO", "StackPhotoSync",
                            "commentResponse: localId=" + item.elementId + ", state=" + item.state
                                    + ", error=" + item.error);
                }
            }
            List<PhotoInformationData> accepted = new ArrayList<>();
            for (PhotoInformationData sent : request.data) {
                if (StackPhotoSyncPolicy.commentAccepted(sent, response)) {
                    accepted.add(sent);
                } else {
                    Globals.writeToMLOG("ERROR", "StackPhotoSync", "commentNotAccepted: localId="
                            + sent.element_id + ", serverId=" + sent.id + ", keptPending=true");
                }
            }
            if (!accepted.isEmpty()) StackPhotoRealm.acknowledgeComments(accepted);
            finish(token, accepted.size() == request.data.size());
            // Re-read persisted flags: a newer edit must still hold back document_complete.
            MAIN.post(ReportConductManager::flushPending);
        } catch (Exception e) {
            failed(user, token, "commentAck: " + e);
        }
    }

    private static void failed(int user, long token, String error) {
        if (!owns(user, token)) return;
        Globals.writeToMLOG("ERROR", "StackPhotoSync", "commentUpload: " + error
                + ", keptPending=true, retryMs=" + PhotoCommentUploadPolicy.RETRY_MS);
        finish(token, false);
    }

    private static void finish(long token, boolean accepted) {
        if (POLICY.finish(token, accepted, SystemClock.elapsedRealtime())) schedule();
    }
}
