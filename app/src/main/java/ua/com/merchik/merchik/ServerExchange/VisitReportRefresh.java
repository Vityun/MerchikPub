package ua.com.merchik.merchik.ServerExchange;

import android.os.Handler;
import android.os.Looper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.TrustedTime;
import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.room.AppDatabase;
import ua.com.merchik.merchik.database.room.RoomManager;

/** Refresh on open, not a periodic task. Failures leave the cached visit available. */
public final class VisitReportRefresh {
    private static final String RECEIPT = "visit_report_refresh/success";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final Map<String, Refresh> IN_FLIGHT = new HashMap<>();

    private VisitReportRefresh() { }

    public static boolean isDue(WpDataDB visit) {
        if (!notStarted(visit) || Globals.getCurrentUserId() <= 0) return false;
        try {
            String scope = VisitReportRefreshPolicy.scope(Globals.getCurrentUserId(), visit.getCode_dad2());
            SynchronizationCursorSDB saved = RoomManager.SQL_DB.synchronizationCursorDao().get(RECEIPT, scope);
            boolean hasRows = RealmManager.INSTANCE.where(ReportPrepareDB.class)
                    .equalTo("codeDad2", String.valueOf(visit.getCode_dad2())).count() > 0;
            return VisitReportRefreshPolicy.due(TrustedTime.nowServerSecOrLocalSec(),
                    saved == null ? 0 : saved.vpi, hasRows);
        } catch (RuntimeException e) {
            log("receiptFailed", visit.getCode_dad2(), e.toString());
            return true;
        }
    }

    public static void refreshForVisit(long dad2, Runnable completed) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            MAIN.post(() -> refreshForVisit(dad2, completed));
            return;
        }
        Refresh refresh = null;
        try {
            WpDataDB visit = RealmManager.INSTANCE.where(WpDataDB.class).equalTo("code_dad2", dad2).findFirst();
            if (!isDue(visit)) {
                log("skip", dad2, "started/completed or refreshed within one hour");
                completed.run();
                return;
            }
            Refresh candidate = new Refresh(visit);
            Refresh running = IN_FLIGHT.get(candidate.scope);
            if (running != null && running.sameContext(candidate)) {
                running.listeners.add(completed);
                log("join", dad2, "user=" + candidate.user);
                return;
            }
            refresh = candidate;
            refresh.listeners.add(completed);
            IN_FLIGHT.put(refresh.scope, refresh);
            refresh.start();
        } catch (RuntimeException e) {
            if (refresh != null) refresh.finish(e);
            else {
                log("failed", dad2, e.toString());
                completed.run();
            }
        }
    }

    private static boolean notStarted(WpDataDB visit) {
        return visit != null && visit.getCode_dad2() > 0
                && VisitReportRefreshPolicy.notStarted(visit.getClient_start_dt(), visit.getVisit_start_dt(),
                visit.getClient_end_dt(), visit.getVisit_end_dt());
    }

    private static void log(String stage, long dad2, String detail) {
        Globals.writeToMLOG("INFO", "VisitReportRefresh/" + stage, "dad2=" + dad2 + ", " + detail);
    }

    private static final class Refresh {
        final int user = Globals.getCurrentUserId();
        final String session = Globals.getCurrentSessionId();
        final Realm realm = RealmManager.INSTANCE;
        final AppDatabase db = RoomManager.SQL_DB;
        final long dad2;
        final int assignedUser;
        final String hash;
        final LocalDate date;
        final String scope;
        final List<Runnable> listeners = new ArrayList<>();
        boolean applied;
        boolean finished;

        Refresh(WpDataDB visit) {
            dad2 = visit.getCode_dad2();
            assignedUser = visit.getUser_id();
            hash = visit.getCode_dad2_hash();
            date = VisitReportDownloadPolicy.visitDate(visit.getDt());
            scope = VisitReportRefreshPolicy.scope(user, dad2);
        }

        boolean sameContext(Refresh other) {
            return realm == other.realm && db == other.db && Objects.equals(session, other.session)
                    && assignedUser == other.assignedUser && Objects.equals(hash, other.hash) && date.equals(other.date);
        }

        void requireCurrent() {
            if (user != Globals.getCurrentUserId() || !Objects.equals(session, Globals.getCurrentSessionId())
                    || realm != RealmManager.INSTANCE || realm.isClosed() || db != RoomManager.SQL_DB)
                throw new IllegalStateException("Account/database changed");
            WpDataDB visit = realm.where(WpDataDB.class).equalTo("code_dad2", dad2).findFirst();
            if (!notStarted(visit)) throw new IllegalStateException("Visit missing or already started");
            if (assignedUser != visit.getUser_id() || !Objects.equals(hash, visit.getCode_dad2_hash())
                    || !date.equals(VisitReportDownloadPolicy.visitDate(visit.getDt())))
                throw new IllegalStateException("Visit assignment/date changed");
        }

        void start() {
            requireCurrent();
            log("request", dad2, "user=" + user + ", dt_change_from=0");
            ReportPrepareSync.refreshUnstartedVisit(dad2, VisitReportRefreshPolicy.request(dad2, date),
                    new Callback<ReportPrepareServer>() {
                        @Override public void onResponse(Call<ReportPrepareServer> call,
                                                         Response<ReportPrepareServer> response) {
                            try {
                                requireCurrent();
                                ReportPrepareServer body = response.body();
                                if (!response.isSuccessful() || body == null || !Boolean.TRUE.equals(body.getState())
                                        || body.getList() == null)
                                    throw new IllegalStateException("RP response failed: HTTP " + response.code());
                                int count = body.getList().size();
                                if (applied && count > 0) {
                                    SynchronizationCursorSDB row = new SynchronizationCursorSDB();
                                    row.resource = RECEIPT;
                                    row.scope = scope;
                                    row.itemCount = count;
                                    row.vpi = VisitReportRefreshPolicy.completedAt(0,
                                            TrustedTime.nowServerSecOrLocalSec(), count, applied);
                                    db.synchronizationCursorDao().save(row);
                                    log("saved", dad2, "user=" + user + ", rows=" + count + ", refreshedAt=" + row.vpi);
                                } else {
                                    log("empty", dad2, "refresh time unchanged");
                                }
                                finish(null);
                            } catch (RuntimeException e) { finish(e); }
                        }

                        @Override public void onFailure(Call<ReportPrepareServer> call, Throwable error) {
                            finish(error);
                        }
                    }, this::requireCurrent, merged -> applied = merged);
        }

        void finish(Throwable error) {
            if (finished) return;
            finished = true;
            IN_FLIGHT.remove(scope, this);
            if (error != null) log("failed", dad2, "refresh time unchanged, error=" + error);
            for (Runnable listener : listeners) {
                try { listener.run(); }
                catch (RuntimeException e) { log("listenerFailed", dad2, e.toString()); }
            }
        }
    }
}
