package ua.com.merchik.merchik.Options;

import io.realm.Realm;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.Utils.WorkTimeRules;

/** Applies only an authorized work end, without overwriting the rest of a stale visit snapshot. */
final class ReportWorkEndPersistence {
    private ReportWorkEndPersistence() { }

    static boolean save(Realm realm, ReportConductState state, long nowSeconds) {
        if (state.visitStart <= 0 || state.visitEnd <= 0
                || !WorkTimeRules.consistent(state.visitStart, state.clientStart, state.visitEnd, state.clientEnd)) return false;
        boolean[] saved = {false};
        realm.executeTransaction(owner -> {
            WpDataDB wp = owner.where(WpDataDB.class).equalTo("code_dad2", state.dad2).findFirst();
            if (wp == null || !state.canApplyWorkEnd(wp.getVisit_start_dt(), wp.getVisit_end_dt(),
                    wp.getClient_start_dt(), wp.getClient_end_dt())) return;
            // A restart after the Realm commit must not change the timestamp or re-dirty an uploaded visit.
            if (!state.matchesWorkTimes(wp.getVisit_start_dt(), wp.getVisit_end_dt(),
                    wp.getClient_start_dt(), wp.getClient_end_dt())) {
                wp.setVisit_end_dt(state.visitEnd);
                wp.setClient_end_dt(state.clientEnd);
                wp.client_work_duration = state.clientEnd - state.clientStart;
                wp.setDt_update(nowSeconds);
                wp.startUpdate = true;
            }
            for (OptionsDB option : owner.where(OptionsDB.class).equalTo("codeDad2", String.valueOf(state.dad2))
                    .beginGroup().equalTo("optionControlId", "138521").or().equalTo("optionId", "138521")
                    .endGroup().findAll()) {
                option.setIsSignal("2");
            }
            saved[0] = true;
        });
        return saved[0];
    }
}
