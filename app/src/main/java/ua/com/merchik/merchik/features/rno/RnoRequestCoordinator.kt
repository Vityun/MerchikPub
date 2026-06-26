package ua.com.merchik.merchik.features.rno

import android.app.Activity
import android.util.Log
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading
import ua.com.merchik.merchik.data.Database.Room.WPDataAdditional
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData
import ua.com.merchik.merchik.database.room.DaoInterfaces.WPDataAdditionalDao
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.database.room.factory.WPDataAdditionalFactory
import ua.com.merchik.merchik.retrofit.RetrofitBuilder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

enum class RnoRequestStatus {
    SUBMITTED,
    APPROVED,
    DECLINED,
    TIMEOUT,
    SYNC_FAILED
}

data class RnoRequestResult(
    val status: RnoRequestStatus,
    val createdCount: Int = 0,
    val alreadySubmittedCount: Int = 0,
    val trackedRows: List<WPDataAdditional> = emptyList(),
    val requestedDad2List: List<Long> = emptyList(),
    val comment: String? = null,
    val notice: String? = null,
    val errorMessage: String? = null
) {
    fun approvedRows(): List<WPDataAdditional> =
        trackedRows.filter { it.confirmDecision == 1 && it.action == 1 && it.ID > 0 }
}

class RnoRequestCoordinator @JvmOverloads constructor(
    private val tablesLoadingUnloading: TablesLoadingUnloading = TablesLoadingUnloading()
) {

    @JvmOverloads
    fun submit(
        wpDataList: List<WpDataDB>,
        forever: Boolean = false,
        decisionTimeoutMs: Long = DEFAULT_DECISION_TIMEOUT_MS
    ): Single<RnoRequestResult> {
        if (wpDataList.isEmpty()) {
            return Single.just(RnoRequestResult(status = RnoRequestStatus.SUBMITTED))
        }

        val signature = requestSignature(wpDataList, forever)
        inFlightSubmissions[signature]?.let { return it }

        val submission = Single.fromCallable { createBatch(wpDataList, forever) }
            .subscribeOn(Schedulers.io())
            .flatMap { batch ->
                if (batch.toInsert.isEmpty()) {
                    Single.just(
                        resultForExistingRows(batch)
                    )
                } else {
                    tablesLoadingUnloading.uploadPlanBudgetRx()
                        .timeout(UPLOAD_TIMEOUT_SEC, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .flatMap { upload ->
                            Single.fromCallable {
                                waitForDecisionBlocking(
                                    batch = batch,
                                    timeoutMs = decisionTimeoutMs,
                                    notice = upload.notice
                                )
                            }.subscribeOn(Schedulers.io())
                        }
                        .onErrorReturn { error ->
                            batch.toResult(
                                status = RnoRequestStatus.SUBMITTED,
                                rows = loadRowsForKeys(RoomManager.SQL_DB.wpDataAdditionalDao(), batch.keys),
                                errorMessage = error.message
                            )
                        }
                }
            }
            .onErrorReturn { error ->
                RnoRequestResult(
                    status = RnoRequestStatus.SYNC_FAILED,
                    errorMessage = error.message
                )
            }
            .doFinally { inFlightSubmissions.remove(signature) }
            .cache()

        return inFlightSubmissions.putIfAbsent(signature, submission) ?: submission
    }

    @JvmOverloads
    fun submit(
        wpData: WpDataDB,
        forever: Boolean = false,
        decisionTimeoutMs: Long = DEFAULT_DECISION_TIMEOUT_MS
    ): Single<RnoRequestResult> = submit(listOf(wpData), forever, decisionTimeoutMs)

    @JvmOverloads
    fun startConfirmedExchange(
        activity: Activity,
        result: RnoRequestResult,
        onFinish: Runnable? = null
    ): Boolean {
        val rows = result.approvedRows()
        if (rows.isEmpty()) return false

        tablesLoadingUnloading.donwloadPlanBudgetForConfirmDecision(activity, rows, onFinish)
        return true
    }

    private fun createBatch(wpDataList: List<WpDataDB>, forever: Boolean): RequestBatch {
        val db = RoomManager.SQL_DB
        val dao = db.wpDataAdditionalDao()
        val unique = LinkedHashMap<RequestKey, WpDataDB>()

        wpDataList.forEach { wp ->
            unique.putIfAbsent(keyFor(wp, forever), wp)
        }

        var result: RequestBatch? = null
        db.runInTransaction {
            val existingRows = mutableListOf<WPDataAdditional>()
            val toInsert = mutableListOf<WPDataAdditional>()
            val keysToTrack = mutableListOf<RequestKey>()
            val requestedDad2 = unique.values.map { it.code_dad2 }

            unique.forEach { (key, wp) ->
                val existingForKey = loadRowsForKey(dao, key)
                if (existingForKey.isNotEmpty()) {
                    existingRows.addAll(existingForKey)
                } else {
                    val row = if (forever) {
                        WPDataAdditionalFactory.blankWithDad2Forever(wp)
                    } else {
                        WPDataAdditionalFactory.blankWithDad2Now(wp)
                    }
                    toInsert.add(row)
                    keysToTrack.add(key)
                }
            }

            toInsert.forEach { dao.insertSync(it) }

            result = RequestBatch(
                keys = keysToTrack,
                toInsert = toInsert,
                existingRows = existingRows,
                requestedDad2List = requestedDad2,
                alreadySubmittedCount = existingRows.distinctBy { it.ID }.size
            )
        }

        return requireNotNull(result)
    }

    private fun resultForExistingRows(batch: RequestBatch): RnoRequestResult {
        val rows = batch.existingRows
        val declined = rows.firstOrNull { it.action == 2 }
        if (declined != null) {
            return batch.toResult(
                status = RnoRequestStatus.DECLINED,
                rows = rows,
                comment = declined.comment
            )
        }

        if (rows.isNotEmpty() && rows.all { it.action == 1 && it.confirmDecision == 1 }) {
            return batch.toResult(
                status = RnoRequestStatus.APPROVED,
                rows = rows
            )
        }

        return batch.toResult(
            status = RnoRequestStatus.SUBMITTED,
            rows = rows
        )
    }

    private fun waitForDecisionBlocking(
        batch: RequestBatch,
        timeoutMs: Long,
        notice: String?
    ): RnoRequestResult {
        val dao = RoomManager.SQL_DB.wpDataAdditionalDao()
        val deadline = System.currentTimeMillis() + timeoutMs
        var lastRows = loadRowsForKeys(dao, batch.keys)
        var lastError: Throwable? = null

        while (System.currentTimeMillis() < deadline) {
            try {
                refreshPlanBudgetSnapshotBlocking(dao)
                lastRows = loadRowsForKeys(dao, batch.keys)

                val declined = lastRows.firstOrNull { it.action == 2 }
                if (declined != null) {
                    return batch.toResult(
                        status = RnoRequestStatus.DECLINED,
                        rows = lastRows,
                        comment = declined.comment,
                        notice = notice
                    )
                }

                val approvedCount = lastRows.count { it.action == 1 && it.confirmDecision == 1 }
                if (batch.keys.isNotEmpty() && approvedCount >= batch.keys.size) {
                    return batch.toResult(
                        status = RnoRequestStatus.APPROVED,
                        rows = lastRows,
                        notice = notice
                    )
                }
            } catch (t: Throwable) {
                lastError = t
                Log.e(TAG, "waitForDecision refresh failed", t)
            }

            Thread.sleep(POLL_INTERVAL_MS)
        }

        return batch.toResult(
            status = RnoRequestStatus.TIMEOUT,
            rows = lastRows,
            notice = notice,
            errorMessage = lastError?.message
        )
    }

    private fun refreshPlanBudgetSnapshotBlocking(dao: WPDataAdditionalDao): List<WPDataAdditional> {
        val data = StandartData<Any?>().apply {
            mod = "plan_budget"
            act = "wp_data_request_list"
        }
        val body = Gson().toJsonTree(data).asJsonObject
        val response = RetrofitBuilder.getRetrofitInterface()
            .GET_WP_DATA_ADDITIONAL(RetrofitBuilder.contentType, body)
            .blockingGet()

        if (response == null || response.state != true || response.list == null) {
            throw IllegalStateException(response?.error ?: "Invalid wp_data_request_list response")
        }

        val list = response.list
        if (list.isNotEmpty()) {
            dao.insertAll(list).blockingAwait()
        }
        return list
    }

    private fun keyFor(wpData: WpDataDB, forever: Boolean): RequestKey {
        val clientId = Integer.parseInt(wpData.client_id)
        return if (forever) {
            RequestKey(clientId = clientId, addrId = wpData.addr_id, codeDad2 = 0L)
        } else {
            RequestKey(clientId = clientId, addrId = wpData.addr_id, codeDad2 = wpData.code_dad2)
        }
    }

    private fun loadRowsForKey(dao: WPDataAdditionalDao, key: RequestKey): List<WPDataAdditional> {
        return if (key.codeDad2 == 0L) {
            dao.getByClientAddrCodeDad2Sync(key.clientId, key.addrId, 0L)
        } else {
            dao.getByCodeDad2Sync(key.codeDad2)
        }
    }

    private fun loadRowsForKeys(
        dao: WPDataAdditionalDao,
        keys: List<RequestKey>
    ): List<WPDataAdditional> {
        val rowsById = LinkedHashMap<Long, WPDataAdditional>()
        keys.forEach { key ->
            loadRowsForKey(dao, key).forEach { row ->
                rowsById[row.ID] = row
            }
        }
        return rowsById.values.toList()
    }

    private fun requestSignature(wpDataList: List<WpDataDB>, forever: Boolean): String {
        val keys = wpDataList
            .map { keyFor(it, forever) }
            .distinct()
            .sortedWith(compareBy<RequestKey> { it.clientId }
                .thenBy { it.addrId }
                .thenBy { it.codeDad2 })
            .joinToString(separator = "|") { "${it.clientId}:${it.addrId}:${it.codeDad2}" }
        return "${if (forever) "forever" else "once"}:$keys"
    }

    private data class RequestKey(
        val clientId: Int,
        val addrId: Int,
        val codeDad2: Long
    )

    private data class RequestBatch(
        val keys: List<RequestKey>,
        val toInsert: List<WPDataAdditional>,
        val existingRows: List<WPDataAdditional>,
        val requestedDad2List: List<Long>,
        val alreadySubmittedCount: Int
    ) {
        fun toResult(
            status: RnoRequestStatus,
            rows: List<WPDataAdditional> = emptyList(),
            comment: String? = null,
            notice: String? = null,
            errorMessage: String? = null
        ): RnoRequestResult {
            return RnoRequestResult(
                status = status,
                createdCount = toInsert.size,
                alreadySubmittedCount = alreadySubmittedCount,
                trackedRows = rows,
                requestedDad2List = requestedDad2List,
                comment = comment,
                notice = notice,
                errorMessage = errorMessage
            )
        }
    }

    companion object {
        private const val TAG = "RnoRequestCoordinator"
        private const val DEFAULT_DECISION_TIMEOUT_MS = 28_700L
        private const val UPLOAD_TIMEOUT_SEC = 35L
        private const val POLL_INTERVAL_MS = 2_000L

        private val resumeRunning = AtomicBoolean(false)
        private val inFlightSubmissions = ConcurrentHashMap<String, Single<RnoRequestResult>>()

        @JvmStatic
        @JvmOverloads
        fun resumePendingAndSync(activity: Activity, onFinish: Runnable? = null) {
            if (!resumeRunning.compareAndSet(false, true)) return

            val coordinator = RnoRequestCoordinator()
            coordinator.tablesLoadingUnloading.uploadPlanBudgetRx()
                .onErrorReturn { error ->
                    Log.e(TAG, "resume upload failed", error)
                    TablesLoadingUnloading.UploadPlanBudgetResult(0, 0, emptyList(), null)
                }
                .flatMap { upload ->
                    Single.fromCallable {
                        val dao = RoomManager.SQL_DB.wpDataAdditionalDao()
                        val notConfirmed = dao.getNotConfirmDecision()
                        if (!notConfirmed.isNullOrEmpty()) {
                            notConfirmed
                        } else {
                            val dad2List = upload.dad2List?.distinct().orEmpty()
                            if (dad2List.isEmpty()) {
                                emptyList()
                            } else {
                                dao.getByCodeDad2ListSync(dad2List)
                                    .filter { it.ID > 0 && it.confirmDecision == 1 && it.action == 1 }
                            }
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { resumeRunning.set(false) }
                .subscribe(
                    { rows ->
                        if (rows.isNullOrEmpty()) return@subscribe
                        if (activity.isFinishing || activity.isDestroyed) return@subscribe
                        coordinator.tablesLoadingUnloading
                            .donwloadPlanBudgetForConfirmDecision(activity, rows, onFinish)
                    },
                    { error -> Log.e(TAG, "resumePendingAndSync failed", error) }
                )
        }
    }
}
