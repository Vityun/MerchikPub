package ua.com.merchik.merchik.features.main.options

import android.os.Handler
import android.os.Looper
import androidx.room.InvalidationTracker
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.RealmModel
import io.realm.RealmResults
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm
import ua.com.merchik.merchik.database.room.RoomManager

/** Watch saved inputs while the visit has unlocked remarks. Never observe the option results we write. */
internal class VisitCorrectionInputs(visit: WpDataDB, onSaved: () -> Unit) {
    private val main = Handler(Looper.getMainLooper())
    private var closed = false
    private val notify = Runnable { if (!closed) onSaved() }
    private val unsubscribe = mutableListOf<() -> Unit>()
    private val tracker = RoomManager.SQL_DB.invalidationTracker
    private val roomObserver = object : InvalidationTracker.Observer(
        "ekl", "tasks_and_reclamations", "question_answers", "achievements", "votes",
        "shelf_size", "planogram_vizit_showcase", "additional_materials", "potential_client",
        "dossier_sotr", "sms_plan", "sms_log"
    ) {
        override fun onInvalidated(tables: Set<String>) = schedule()
    }

    private fun schedule() {
        main.removeCallbacks(notify)
        main.post(notify)
    }

    init {
        val realm = RealmManager.INSTANCE
        watch(ReportPrepareRealm.getReportPrepareByDad2(visit.code_dad2))
        watch(StackPhotoRealm.getPhotosByDAD2AllTypes(visit.code_dad2))
        watch(realm.where(TARCommentsDB::class.java).equalTo("who", visit.user_id.toString()).findAll())
        watch(realm.where(AdditionalRequirementsMarkDB::class.java).equalTo("userId", visit.user_id.toString()).findAll())
        watch(realm.where(WpDataDB::class.java).equalTo("code_dad2", visit.code_dad2).findAll())
        // 156928 depends on a different visit leaving the executor's set of unfinished jobs.
        watch(realm.where(WpDataDB::class.java).equalTo("user_id", visit.user_id)
            .notEqualTo("code_dad2", visit.code_dad2).greaterThan("visit_start_dt", 0L)
            .equalTo("visit_end_dt", 0L).findAll(), membershipOnly = true)
        tracker.addObserver(roomObserver)
    }

    private fun <T : RealmModel> watch(rows: RealmResults<T>, membershipOnly: Boolean = false) {
        val listener = OrderedRealmCollectionChangeListener<RealmResults<T>> { _, change ->
            if (change.insertions.isNotEmpty() || change.deletions.isNotEmpty()
                    || (!membershipOnly && change.changes.isNotEmpty())) schedule()
        }
        rows.addChangeListener(listener)
        unsubscribe += { if (rows.isValid) rows.removeChangeListener(listener) }
    }

    fun close() {
        closed = true
        tracker.removeObserver(roomObserver)
        unsubscribe.forEach { it() }
        unsubscribe.clear()
        main.removeCallbacks(notify)
    }
}
