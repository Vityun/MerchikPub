package ua.com.merchik.merchik.database.realm.tables;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.function.Consumer;

import io.realm.RealmObject;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.WorkTimeRules;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TestJsonUpload.StratEndWork.StartEndData;
import ua.com.merchik.merchik.database.realm.RealmManager;

/** Pair normalization and field-level writes. normalize/mergeServer require a transaction for managed rows. */
public final class WpWorkTimes {
    private WpWorkTimes() { }

    private static WorkTimeRules.Times times(WpDataDB wp) {
        return WorkTimeRules.normalize(wp.getVisit_start_dt(), wp.getClient_start_dt(),
                wp.getVisit_end_dt(), wp.getClient_end_dt());
    }

    public static boolean valid(WpDataDB wp) {
        return wp != null && WorkTimeRules.consistent(wp.getVisit_start_dt(), wp.getClient_start_dt(),
                wp.getVisit_end_dt(), wp.getClient_end_dt());
    }

    private static String values(WpDataDB wp) {
        return "start=" + wp.getVisit_start_dt() + "/" + wp.getClient_start_dt()
                + ", end=" + wp.getVisit_end_dt() + "/" + wp.getClient_end_dt()
                + ", duration=" + wp.client_work_duration + ", vpi=" + wp.getDt_update()
                + ", dirty=" + wp.startUpdate;
    }

    private static void apply(WpDataDB wp, WorkTimeRules.Times times) {
        wp.setVisit_start_dt(times.start);
        wp.setClient_start_dt(times.start);
        wp.setVisit_end_dt(times.end);
        wp.setClient_end_dt(times.end);
        wp.client_work_duration = times.duration();
    }

    public static boolean normalize(WpDataDB wp, String source) {
        try {
            WorkTimeRules.Times times = times(wp);
            boolean changed = !times.matches(wp.getVisit_start_dt(), wp.getClient_start_dt(),
                    wp.getVisit_end_dt(), wp.getClient_end_dt())
                    || (wp.client_work_duration != null && wp.client_work_duration != times.duration())
                    || (wp.client_work_duration == null && times.end > 0);
            if (changed) {
                String before = values(wp);
                apply(wp, times);
                markDirty(wp);
                log(wp, source, before, "NORMALIZED");
            }
            return true;
        } catch (IllegalArgumentException e) {
            log(wp, source, values(wp), "INVALID: " + e.getMessage());
            return false;
        }
    }

    public static void markDirty(WpDataDB wp) {
        wp.startUpdate = true;
        wp.setDt_update(Math.max(wp.getDt_update(), System.currentTimeMillis() / 1000));
    }

    public static boolean matchesSentSnapshot(WpDataDB db, StartEndData sent) {
        if (db == null || sent == null || !valid(db)) return false;
        return equal(db.getId(), sent.element_id)
                && equal(db.getCode_dad2(), sent.code_dad2)
                && equal(db.getUser_id(), sent.user_id)
                && db.getDt_update() == sent.dt_update
                && equal(db.getVisit_start_dt(), sent.visit_start_dt)
                && equal(db.getVisit_end_dt(), sent.visit_end_dt)
                && equal(db.getClient_start_dt(), sent.client_start_dt)
                && equal(db.getClient_end_dt(), sent.client_end_dt)
                && equal(String.valueOf(db.client_work_duration), sent.client_work_duration)
                && equal(db.getSetStatus(), sent.status_set)
                && equal(db.user_comment, sent.user_comment)
                && equal(db.user_comment_author_id, sent.user_comment_author_id)
                && equal(db.user_comment_dt_update, sent.user_comment_dt_update)
                && equal(db.getUser_opinion_id(), sent.user_opinion_id)
                && equal(db.getUser_opinion_author_id(), sent.user_opinion_author_id)
                && equal(db.getUser_opinion_dt_update(), sent.user_opinion_dt_update);
    }

    private static boolean equal(Object a, Object b) {
        return java.util.Objects.equals(a == null ? null : String.valueOf(a), b == null ? null : String.valueOf(b));
    }

    public static boolean mergeServer(WpDataDB incoming, WpDataDB local) {
        String before = values(incoming);
        WorkTimeRules.Times localTimes = null;
        if (local != null) {
            try { localTimes = times(local); }
            catch (IllegalArgumentException e) { log(local, "server.local", values(local), "INVALID_LOCAL"); }
        }
        WorkTimeRules.Times serverTimes;
        try { serverTimes = times(incoming); }
        catch (IllegalArgumentException e) {
            log(incoming, "server", before, "INVALID_SERVER: " + e.getMessage());
            if (localTimes == null) return false;
            serverTimes = localTimes;
        }
        WorkTimeRules.Times chosen = WorkTimeRules.merge(localTimes, serverTimes,
                local != null && local.startUpdate,
                local != null && incoming.getDt_update() < local.getDt_update(),
                incoming.getVisit_start_dt() != incoming.getClient_start_dt(),
                incoming.getVisit_end_dt() != incoming.getClient_end_dt());
        boolean repair = !chosen.matches(incoming.getVisit_start_dt(), incoming.getClient_start_dt(),
                incoming.getVisit_end_dt(), incoming.getClient_end_dt())
                || (incoming.client_work_duration != null && incoming.client_work_duration != chosen.duration())
                || (incoming.client_work_duration == null && chosen.end > 0);
        apply(incoming, chosen);
        incoming.startUpdate = local != null && local.startUpdate;
        if (local != null) {
            incoming.setId(local.getId());
            incoming.setSetStatus(local.getSetStatus());
            if (local.startUpdate || incoming.getDt_update() < local.getDt_update()) {
                incoming.user_comment = local.user_comment;
                incoming.user_comment_author_id = local.user_comment_author_id;
                incoming.user_comment_dt_update = local.user_comment_dt_update;
                incoming.setUser_opinion_id(local.getUser_opinion_id());
                incoming.setUser_opinion_author_id(local.getUser_opinion_author_id());
                incoming.setUser_opinion_dt_update(local.getUser_opinion_dt_update());
                incoming.setDt_update(Math.max(incoming.getDt_update(), local.getDt_update()));
            }
        }
        if (repair) {
            markDirty(incoming);
            log(incoming, "server", before + "; local=" + (local == null ? "none" : values(local)), "MERGED");
        }
        return true;
    }

    public static WpDataDB updateFields(WpDataDB source, boolean upload, Consumer<WpDataDB> change) {
        if (source == null) return null;
        if (RealmObject.isManaged(source) && !RealmObject.isValid(source)) return null;
        if (source.getCode_dad2() <= 0) return null;
        WpDataDB[] saved = {null};
        RealmManager.INSTANCE.executeTransaction(realm -> {
            WpDataDB current = realm.where(WpDataDB.class).equalTo("code_dad2", source.getCode_dad2())
                    .equalTo("user_id", source.getUser_id()).findFirst();
            if (current == null || !normalize(current, "local.edit")) return;
            change.accept(current);
            if (!normalize(current, "local.edit.result")) throw new IllegalArgumentException("Invalid work times");
            if (upload) markDirty(current);
            saved[0] = realm.copyFromRealm(current);
        });
        if (saved[0] != null && !RealmObject.isManaged(source)) {
            // Callers supply setter-only edits; keep the detached UI copy in sync after the commit.
            change.accept(source);
            apply(source, times(saved[0]));
            source.startUpdate = saved[0].startUpdate;
            source.setDt_update(saved[0].getDt_update());
        }
        return saved[0];
    }

    public static WpDataDB start(WpDataDB source, long start) {
        long seconds = WorkTimeRules.seconds(start);
        if (seconds == 0) throw new IllegalArgumentException("Missing start time");
        return updateFields(source, true, current -> {
            if (current.getVisit_start_dt() == 0 && current.getVisit_end_dt() == 0) {
                current.setVisit_start_dt(seconds);
                current.setClient_start_dt(seconds);
            }
        });
    }

    public static void repairExisting(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("wp_work_times", Context.MODE_PRIVATE);
        if (prefs.getBoolean("pairs_v1", false)) return;
        try {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                for (WpDataDB wp : realm.where(WpDataDB.class).findAll()) normalize(wp, "existing");
            });
            prefs.edit().putBoolean("pairs_v1", true).apply();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "WpWorkTimes", "repairExisting failed; retry next launch: " + e);
        }
    }

    private static void log(WpDataDB wp, String source, String before, String reason) {
        Globals.writeToMLOG("WARNING", "WpWorkTimes", "dad2=" + wp.getCode_dad2() + ", source=" + source
                + ", reason=" + reason + ", before={" + before + "}, after={" + values(wp) + "}");
    }
}
