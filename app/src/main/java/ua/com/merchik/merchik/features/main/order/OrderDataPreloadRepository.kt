package ua.com.merchik.merchik.features.main.order

import android.content.Context
import android.location.Location
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.OrderDataSDB
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.data.RetrofitResponse.tables.AddressResponse
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.retrofit.RetrofitBuilder
import ua.com.merchik.merchik.trecker
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

object OrderDataPreloadRepository {

    private const val TAG = "OrderDataPreload"
    private const val DEFAULT_ORDER_PERIOD_DAYS = 30L

    private val gson = Gson()

    data class VisitAddOneTimePayload(
        val dateYmd: String,
        val timeStart: String,
        val timeStop: String,
        val addrId: Int,
        val userId: Int,
        val docNum1cId: Int
    )

    data class PreloadResult(
        val orderListJson: JsonObject?,
        val addressJson: JsonObject?,
        val savedOrderCount: Int,
        val savedAddressCount: Int,
        val savedTradeMarkCount: Int,
        val missingTradeMarkIds: List<String>,
        val errors: List<String>
    )

    data class CreatedVisitsSyncResult(
        val requestedDad2: List<Long>,
        val foundVisits: List<WpDataDB>,
        val missingDad2: List<Long>,
        val attempts: Int,
        val lastError: String? = null
    )

    data class CreatedVisitDetailsPreloadResult(
        val requestedDad2: List<Long>,
        val savedOptionsCount: Int,
        val savedReportPrepareCount: Int,
        val errors: List<String>
    )

    suspend fun preload(context: Context?): PreloadResult = coroutineScope {
        val orderListDeferred = async(Dispatchers.IO) {
            runCatching { downloadOrderList() }
        }
        val addressDeferred = async(Dispatchers.IO) {
            runCatching { downloadNearbyAddresses(context) }
        }

        val orderResult = orderListDeferred.await()
        val addressResult = addressDeferred.await()
        val errors = mutableListOf<String>()

        orderResult.exceptionOrNull()?.let { errors.add("order_data.list: ${it.message}") }
        addressResult.exceptionOrNull()?.let { errors.add("addr_list: ${it.message}") }
        addressResult.getOrNull()?.errors?.let { errors.addAll(it) }

        PreloadResult(
            orderListJson = orderResult.getOrNull()?.json,
            addressJson = addressResult.getOrNull()?.json,
            savedOrderCount = orderResult.getOrNull()?.savedCount ?: 0,
            savedAddressCount = addressResult.getOrNull()?.savedCount ?: 0,
            savedTradeMarkCount = addressResult.getOrNull()?.savedTradeMarkCount ?: 0,
            missingTradeMarkIds = addressResult.getOrNull()?.missingTradeMarkIds.orEmpty(),
            errors = errors
        ).also { result ->
            logInfo(
                "preload",
                "completed: savedOrderCount=${result.savedOrderCount}, savedAddressCount=${result.savedAddressCount}, savedTradeMarkCount=${result.savedTradeMarkCount}, missingTradeMarkIds=${result.missingTradeMarkIds}, errors=${result.errors}"
            )
        }
    }

    suspend fun downloadOrderList(
        dtChangeFrom: String = defaultDtChangeFrom(),
        dtChangeTo: String = defaultDtChangeTo()
    ): OrderPreloadResult = withContext(Dispatchers.IO) {
        val request = JsonObject().apply {
            addProperty("mod", "order_data")
            addProperty("act", "list")
            addProperty("dt_change_from", dtChangeFrom)
            addProperty("dt_change_to", dtChangeTo)
        }

        val json = executeJsonRequest("order_data.list", request)
        val orders = parseOrderList(json)

        if (orders.isNotEmpty()) {
            RoomManager.SQL_DB.orderDataDao().insertAll(orders)
            logInfo("order_data.list.save", "saved OrderDataSDB count=${orders.size}")
        } else {
            val state = json.get("state")
            val listSize = json.get("list")
                ?.takeIf { it.isJsonArray }
                ?.asJsonArray
                ?.size() ?: 0
            logInfo(
                "order_data.list.save",
                "skip save: state=$state, listSize=$listSize"
            )
        }

        OrderPreloadResult(json = json, savedCount = orders.size)
    }

    suspend fun createVisitOneTime(payload: VisitAddOneTimePayload): JsonObject =
        withContext(Dispatchers.IO) {
            val item = JsonObject().apply {
                addProperty("date_ymd", payload.dateYmd)
                addProperty("time_start", payload.timeStart)
                addProperty("time_stop", payload.timeStop)
                addProperty("addr_id", payload.addrId)
                addProperty("user_id", payload.userId)
                addProperty("doc_num_1c_id", payload.docNum1cId)
            }

            val request = JsonObject().apply {
                addProperty("mod", "order_data")
                addProperty("act", "visit_add_one_time")
                add("data", item)
            }

            executeJsonRequest("order_data.visit_add_one_time", request)
        }

    suspend fun syncCreatedVisitsByDad2(
        codeDad2List: List<Long>,
        dateYmd: String,
        timeoutMs: Long = CREATED_VISIT_SYNC_TIMEOUT_MS,
        pollIntervalMs: Long = CREATED_VISIT_SYNC_POLL_INTERVAL_MS
    ): CreatedVisitsSyncResult {
        val requestedDad2 = codeDad2List
            .filter { it > 0L }
            .distinct()

        if (requestedDad2.isEmpty()) {
            return CreatedVisitsSyncResult(
                requestedDad2 = emptyList(),
                foundVisits = emptyList(),
                missingDad2 = emptyList(),
                attempts = 0
            )
        }

        val deadline = SystemClock.elapsedRealtime() + timeoutMs.coerceAtLeast(0L)
        var attempts = 0
        var lastFoundVisits = emptyList<WpDataDB>()
        var lastError: String? = null

        do {
            attempts++

            runCatching {
                val serverVisits = downloadWpDataByDad2(requestedDad2, dateYmd)
                if (serverVisits.isNotEmpty()) {
                    saveWpDataPartial(serverVisits)
                }
                loadLocalVisitsByDad2(requestedDad2)
            }.onSuccess { visits ->
                lastFoundVisits = visits
                lastError = null

                val foundDad2 = visits.map { it.code_dad2 }.toSet()
                if (requestedDad2.all { it in foundDad2 }) {
                    return CreatedVisitsSyncResult(
                        requestedDad2 = requestedDad2,
                        foundVisits = visits,
                        missingDad2 = emptyList(),
                        attempts = attempts
                    )
                }
            }.onFailure { throwable ->
                lastError = throwable.message ?: throwable.toString()
                logError("wp_data.created_visits", lastError.orEmpty())
            }

            val remainingMs = deadline - SystemClock.elapsedRealtime()
            if (remainingMs > 0L) {
                delay(pollIntervalMs.coerceAtMost(remainingMs).coerceAtLeast(250L))
            }
        } while (SystemClock.elapsedRealtime() < deadline)

        val foundDad2 = lastFoundVisits.map { it.code_dad2 }.toSet()
        return CreatedVisitsSyncResult(
            requestedDad2 = requestedDad2,
            foundVisits = lastFoundVisits,
            missingDad2 = requestedDad2.filter { it !in foundDad2 },
            attempts = attempts,
            lastError = lastError
        )
    }

    suspend fun preloadCreatedVisitDetailsByDad2(
        codeDad2List: List<Long>
    ): CreatedVisitDetailsPreloadResult = coroutineScope {
        val requestedDad2 = codeDad2List
            .filter { it > 0L }
            .distinct()

        if (requestedDad2.isEmpty()) {
            return@coroutineScope CreatedVisitDetailsPreloadResult(
                requestedDad2 = emptyList(),
                savedOptionsCount = 0,
                savedReportPrepareCount = 0,
                errors = emptyList()
            )
        }

        val optionsDeferred = async(Dispatchers.IO) {
            runCatching { downloadCreatedVisitOptionsByDad2(requestedDad2) }
        }
        val reportPrepareDeferred = async(Dispatchers.IO) {
            runCatching { downloadCreatedVisitReportPrepareByDad2(requestedDad2) }
        }

        val errors = mutableListOf<String>()
        val options = optionsDeferred.await()
            .onFailure { throwable ->
                errors.add("options_list: ${throwable.message ?: throwable}")
                logError("plan.options_list.created_visits", throwable.message ?: throwable.toString())
            }
            .getOrDefault(emptyList())
        val reportPrepare = reportPrepareDeferred.await()
            .onFailure { throwable ->
                errors.add("report_prepare.list_data: ${throwable.message ?: throwable}")
                logError(
                    "report_prepare.list_data.created_visits",
                    throwable.message ?: throwable.toString()
                )
            }
            .getOrDefault(emptyList())

        if (options.isNotEmpty()) {
            saveOptionsPartial(options)
        }
        if (reportPrepare.isNotEmpty()) {
            saveReportPreparePartial(reportPrepare)
        }

        CreatedVisitDetailsPreloadResult(
            requestedDad2 = requestedDad2,
            savedOptionsCount = options.size,
            savedReportPrepareCount = reportPrepare.size,
            errors = errors
        ).also { result ->
            logInfo(
                "created_visit_details.preload",
                "dad2=${result.requestedDad2.previewLongIds()}, options=${result.savedOptionsCount}, reportPrepare=${result.savedReportPrepareCount}, errors=${result.errors}"
            )
        }
    }

    suspend fun findExistingVisit(
        dateYmd: String,
        addrId: Int,
        clientId: String
    ): WpDataDB? = withContext(Dispatchers.IO) {
        val normalizedClientId = clientId.trim()
        val day = runCatching { LocalDate.parse(dateYmd) }.getOrNull()

        if (normalizedClientId.isBlank() || day == null) {
            logInfo(
                "wp_data.duplicate_check",
                "skip: dateYmd=$dateYmd, addrId=$addrId, clientId=$clientId"
            )
            return@withContext null
        }

        val zone = ZoneId.systemDefault()
        val dayStart = Date.from(day.atStartOfDay(zone).toInstant())
        val nextDayStart = Date.from(day.plusDays(1).atStartOfDay(zone).toInstant())
        val realm = Realm.getDefaultInstance()

        try {
            val existing = realm.where(WpDataDB::class.java)
                .equalTo("client_id", normalizedClientId)
                .equalTo("addr_id", addrId)
                .greaterThanOrEqualTo("dt", dayStart)
                .lessThan("dt", nextDayStart)
                .findFirst()

            existing?.let { realm.copyFromRealm(it) }.also { result ->
                logInfo(
                    "wp_data.duplicate_check",
                    "dateYmd=$dateYmd, addrId=$addrId, clientId=$normalizedClientId, existingId=${result?.id}"
                )
            }
        } finally {
            realm.close()
        }
    }

    private fun downloadCreatedVisitOptionsByDad2(
        codeDad2List: List<Long>
    ): List<OptionsDB> {
        val request = buildCreatedVisitOptionsRequest(codeDad2List)
        val response = timeRequest("plan.options_list.created_visits") {
            logInfo("plan.options_list.created_visits", "request=$request")
            RetrofitBuilder.getRetrofitInterface()
                .GET_OPTIONS(RetrofitBuilder.contentType, request)
                .execute()
        }

        if (!response.isSuccessful) {
            val message = "HTTP ${response.code()} ${response.message()}"
            logError("plan.options_list.created_visits", message)
            throw IllegalStateException(message)
        }

        val body = response.body()
            ?: throw IllegalStateException("empty response body")

        if (body.state != true) {
            throw IllegalStateException(body.error ?: "state=${body.state}")
        }

        return body.list.orEmpty().also { options ->
            logInfo(
                "plan.options_list.created_visits",
                "response options=${options.size}, requestedDad2=${codeDad2List.previewLongIds()}"
            )
        }
    }

    private fun downloadCreatedVisitReportPrepareByDad2(
        codeDad2List: List<Long>
    ): List<ReportPrepareDB> {
        val request = buildCreatedVisitReportPrepareRequest(codeDad2List)
        val response = timeRequest("report_prepare.list_data.created_visits") {
            logInfo("report_prepare.list_data.created_visits", "request=$request")
            RetrofitBuilder.getRetrofitInterface()
                .ReportPrepareServer_RESPONSE(RetrofitBuilder.contentType, request)
                .execute()
        }

        if (!response.isSuccessful) {
            val message = "HTTP ${response.code()} ${response.message()}"
            logError("report_prepare.list_data.created_visits", message)
            throw IllegalStateException(message)
        }

        val body = response.body()
            ?: throw IllegalStateException("empty response body")

        if (body.state != true) {
            throw IllegalStateException(body.error ?: "state=${body.state}")
        }

        return body.list.orEmpty().also { reportPrepare ->
            logInfo(
                "report_prepare.list_data.created_visits",
                "response reportPrepare=${reportPrepare.size}, requestedDad2=${codeDad2List.previewLongIds()}"
            )
        }
    }

    private suspend fun saveOptionsPartial(options: List<OptionsDB>) {
        withContext(Dispatchers.Main) {
            RealmManager.setOptions2(options)
        }
    }

    private suspend fun saveReportPreparePartial(reportPrepare: List<ReportPrepareDB>) {
        withContext(Dispatchers.Main) {
            RealmManager.setReportPrepare(reportPrepare)
        }
    }

    private fun buildCreatedVisitOptionsRequest(codeDad2List: List<Long>): JsonObject {
        return JsonObject().apply {
            addProperty("mod", "plan")
            addProperty("act", "options_list")
            addCodeDad2Value(codeDad2List)
        }
    }

    private fun buildCreatedVisitReportPrepareRequest(codeDad2List: List<Long>): JsonObject {
        return JsonObject().apply {
            addProperty("mod", "report_prepare")
            addProperty("act", "list_data")
            addCodeDad2Value(codeDad2List)
        }
    }

    private suspend fun downloadWpDataByDad2(
        codeDad2List: List<Long>,
        dateYmd: String
    ): List<WpDataDB> = withContext(Dispatchers.IO) {
        val request = buildWpDataDad2Request(codeDad2List, dateYmd)
        val response = timeRequest("plan.list.created_visits") {
            logInfo("plan.list.created_visits", "request=$request")
            RetrofitBuilder.getRetrofitInterface()
                .WpDataServer_RESPONSE(RetrofitBuilder.contentType, request)
                .execute()
        }

        if (!response.isSuccessful) {
            val message = "HTTP ${response.code()} ${response.message()}"
            logError("plan.list.created_visits", message)
            throw IllegalStateException(message)
        }

        val body = response.body()
            ?: throw IllegalStateException("empty response body")

        if (body.state != true) {
            throw IllegalStateException(body.error ?: "state=${body.state}")
        }

        body.list.orEmpty().also { visits ->
            logInfo(
                "plan.list.created_visits",
                "response visits=${visits.size}, requestedDad2=${codeDad2List.previewLongIds()}"
            )
        }
    }

    private suspend fun saveWpDataPartial(wpData: List<WpDataDB>) {
        withContext(Dispatchers.Main) {
            RealmManager.updateWorkPlanFromServer(wpData)
        }
    }

    private suspend fun loadLocalVisitsByDad2(codeDad2List: List<Long>): List<WpDataDB> =
        withContext(Dispatchers.IO) {
            val requestedDad2 = codeDad2List
                .filter { it > 0L }
                .distinct()
            if (requestedDad2.isEmpty()) return@withContext emptyList()

            val realm = Realm.getDefaultInstance()
            try {
                val orderByDad2 = requestedDad2
                    .withIndex()
                    .associate { it.value to it.index }
                realm.where(WpDataDB::class.java)
                    .apply {
                        requestedDad2.forEachIndexed { index, dad2 ->
                            if (index > 0) or()
                            equalTo("code_dad2", dad2)
                        }
                    }
                    .findAll()
                    .let { realm.copyFromRealm(it) }
                    .sortedBy { orderByDad2[it.code_dad2] ?: Int.MAX_VALUE }
            } finally {
                realm.close()
            }
        }

    private fun buildWpDataDad2Request(
        codeDad2List: List<Long>,
        dateYmd: String
    ): JsonObject {
        val (dateFrom, dateTo) = dateRangeForCreatedVisit(dateYmd)
        val dad2Array = JsonArray().apply {
            codeDad2List.forEach { add(it) }
        }

        return JsonObject().apply {
            addProperty("mod", "plan")
            addProperty("act", "list")
            addProperty("date_from", dateFrom)
            addProperty("date_to", dateTo)
            add("code_dad2", dad2Array)
        }
    }

    private fun JsonObject.addCodeDad2Value(codeDad2List: List<Long>) {
        val requestedDad2 = codeDad2List
            .filter { it > 0L }
            .distinct()

        if (requestedDad2.size == 1) {
            addProperty("code_dad2", requestedDad2.single().toString())
        } else {
            add("code_dad2", JsonArray().apply {
                requestedDad2.forEach { add(it.toString()) }
            })
        }
    }

    private fun dateRangeForCreatedVisit(dateYmd: String): Pair<String, String> {
        val day = runCatching { LocalDate.parse(dateYmd) }.getOrNull()
        return if (day != null) {
            day.minusDays(1).toString() to day.plusDays(1).toString()
        } else {
            Clock.getDatePeriod(-21) to Clock.getDatePeriod(5)
        }
    }

    suspend fun downloadNearbyAddresses(context: Context?): AddressPreloadResult =
        withContext(Dispatchers.IO) {
            val request = JsonObject().apply {
                addProperty("mod", "data_list")
                addProperty("act", "addr_list")

                resolveCurrentCoordinates(context)?.let { coordinate ->
                    addProperty("location_lat", coordinate.lat)
                    addProperty("location_lon", coordinate.lon)
                }
            }

            if (!request.has("location_lat") || !request.has("location_lon")) {
                logInfo(
                    "addr_list.coordinates",
                    "real coordinates not found, request will use server location fallback"
                )
            }

            val json = executeJsonRequest("data_list.addr_list", request)
            val response = gson.fromJson(json, AddressResponse::class.java)
            val addresses = response?.list.orEmpty()
            var savedTradeMarkCount = 0
            var missingTradeMarkIds = emptyList<String>()
            val errors = mutableListOf<String>()

            if (response?.state == true && addresses.isNotEmpty()) {
                RoomManager.SQL_DB.addressDao().insertAll(addresses)
                logInfo("data_list.addr_list.save", "saved AddressSDB count=${addresses.size}")

                runCatching {
                    downloadMissingTradeMarksForAddresses(addresses)
                }.onSuccess { tradeMarkResult ->
                    savedTradeMarkCount = tradeMarkResult.savedCount
                    missingTradeMarkIds = tradeMarkResult.missingIds
                }.onFailure { throwable ->
                    val message = throwable.message ?: throwable.toString()
                    errors.add("trade_marks: $message")
                    logError("data_list.tovar_manufacturer_list", message)
                }
            } else {
                logInfo(
                    "data_list.addr_list.save",
                    "skip save: state=${response?.state}, count=${addresses.size}, error=${response?.error}"
                )
            }

            AddressPreloadResult(
                json = json,
                savedCount = addresses.size,
                savedTradeMarkCount = savedTradeMarkCount,
                missingTradeMarkIds = missingTradeMarkIds,
                errors = errors
            )
        }

    private suspend fun downloadMissingTradeMarksForAddresses(
        addresses: List<AddressSDB>
    ): TradeMarkPreloadResult {
        val idsCheck = checkTradeMarkIds(addresses)
        val idsToRequest = idsCheck.requiredIds
        if (idsToRequest.isEmpty()) {
            logInfo("data_list.tovar_manufacturer_list", "skip: no tp_id in addresses")
            return TradeMarkPreloadResult(missingIds = emptyList(), savedCount = 0)
        }

        val response = timeRequest("data_list.tovar_manufacturer_list") {
            val call = RetrofitBuilder.getRetrofitInterface()
                .GET_TRADE_MARKS_T(
                    "data_list",
                    "tovar_manufacturer_list",
                    idsToRequest.toTypedArray()
                )
            logInfo(
                "data_list.tovar_manufacturer_list",
                "request=${call.request().method} ${call.request().url}"
            )
            call.execute()
        }

        if (!response.isSuccessful) {
            val message = "HTTP ${response.code()} ${response.message()}"
            logError("data_list.tovar_manufacturer_list", message)
            throw IllegalStateException(message)
        }

        val body = response.body()
            ?: throw IllegalStateException("empty response body")

        if (body.state != true) {
            throw IllegalStateException("state=${body.state}")
        }

        val tradeMarks = body.list.orEmpty()
        if (tradeMarks.isNotEmpty()) {
            saveTradeMarksPartial(tradeMarks)
        }

        logInfo(
            "data_list.tovar_manufacturer_list.save",
            "requestedIds=${idsToRequest.previewIds()}, localMissingIds=${idsCheck.missingIds.previewIds()}, saved TradeMarkDB count=${tradeMarks.size}"
        )

        return TradeMarkPreloadResult(
            missingIds = idsCheck.missingIds,
            savedCount = tradeMarks.size
        )
    }

    private suspend fun checkTradeMarkIds(addresses: List<AddressSDB>): TradeMarkIdsCheck =
        withContext(Dispatchers.IO) {
            val uniqueIds = addresses
                .asSequence()
                .mapNotNull { address -> address.tpId }
                .filter { id -> id > 0 }
                .map { id -> id.toString() }
                .distinct()
                .toList()

            if (uniqueIds.isEmpty()) {
                logInfo(
                    "data_list.tovar_manufacturer_list.check",
                    "addresses=${addresses.size}, uniqueTpIds=0, missing=0"
                )
                return@withContext TradeMarkIdsCheck(
                    requiredIds = emptyList(),
                    missingIds = emptyList()
                )
            }

            val realm = Realm.getDefaultInstance()
            try {
                val localNamesById = realm.where(TradeMarkDB::class.java)
                    .`in`("iD", uniqueIds.toTypedArray())
                    .findAll()
                    .mapNotNull { tradeMark ->
                        val id = tradeMark.id ?: return@mapNotNull null
                        id to tradeMark.nm
                    }
                    .toMap()

                val missingIds = uniqueIds
                    .filter { id -> localNamesById[id].isNullOrBlank() }
                val absentCount = uniqueIds.count { id -> !localNamesById.containsKey(id) }
                val incompleteCount = missingIds.size - absentCount
                val completeCount = uniqueIds.size - missingIds.size

                logInfo(
                    "data_list.tovar_manufacturer_list.check",
                    "addresses=${addresses.size}, uniqueTpIds=${uniqueIds.size}, complete=$completeCount, absent=$absentCount, incomplete=$incompleteCount, missing=${missingIds.size}, missingIds=${missingIds.previewIds()}, willRefresh=${uniqueIds.size}"
                )

                TradeMarkIdsCheck(
                    requiredIds = uniqueIds,
                    missingIds = missingIds
                )
            } finally {
                realm.close()
            }
        }

    private suspend fun saveTradeMarksPartial(tradeMarks: List<TradeMarkDB>) {
        withContext(Dispatchers.Main) {
            val realm = RealmManager.INSTANCE
                ?: throw IllegalStateException("Realm is not initialized")

            realm.beginTransaction()
            try {
                realm.copyToRealmOrUpdate(tradeMarks)
                realm.commitTransaction()
            } catch (throwable: Throwable) {
                if (realm.isInTransaction) {
                    realm.cancelTransaction()
                }
                throw throwable
            }
        }
    }

    private fun defaultDtChangeFrom(): String {
        val nowSeconds = System.currentTimeMillis() / 1000L
        return (nowSeconds - DEFAULT_ORDER_PERIOD_DAYS * 24L * 60L * 60L).toString()
    }

    private fun defaultDtChangeTo(): String {
        return (System.currentTimeMillis() / 1000L).toString()
    }

    private fun executeJsonRequest(requestName: String, request: JsonObject): JsonObject {
        return timeRequest(requestName) {
            logInfo(requestName, "request=$request")
            val response = RetrofitBuilder.getRetrofitInterface()
                .TEST_JSON_UPLOAD(RetrofitBuilder.contentType, request)
                .execute()

            if (!response.isSuccessful) {
                val message = "HTTP ${response.code()} ${response.message()}"
                logError(requestName, message)
                throw IllegalStateException(message)
            }

            (response.body() ?: JsonObject()).also { json ->
                logInfo(requestName, "response=$json")
            }
        }
    }

    private fun parseOrderList(json: JsonObject): List<OrderDataSDB> {
        val listElement = json.get("list") ?: return emptyList()
        if (!listElement.isJsonArray) return emptyList()

        return listElement.asJsonArray.mapNotNull { element ->
            runCatching {
                gson.fromJson(element, OrderDataSDB::class.java)
                    ?.also { it.normalizeId() }
                    ?.takeIf { it.id.isNotBlank() }
            }.onFailure { throwable ->
                logError("order_data.list.parse", throwable.message ?: throwable.toString())
            }.getOrNull()
        }
    }

    private inline fun <T> timeRequest(requestName: String, block: () -> T): T {
        val startedAt = SystemClock.elapsedRealtime()
        return try {
            block()
        } finally {
            val durationMs = SystemClock.elapsedRealtime() - startedAt
            logInfo(requestName, "durationMs=$durationMs")
        }
    }

    private suspend fun resolveCurrentCoordinates(context: Context?): Coordinate? {
        currentGlobalCoordinate()?.let { return it }
        trecker.imHereGPS?.toCoordinate()?.let { return it }
        trecker.imHereNET?.toCoordinate()?.let { return it }

        val appContext = context?.applicationContext ?: return null
        return runCatching {
            LocationServices.getFusedLocationProviderClient(appContext)
                .lastLocation
                .await()
                ?.toCoordinate()
        }.onFailure { throwable ->
            logError("resolveCurrentCoordinates", throwable.message ?: throwable.toString())
        }.getOrNull()
    }

    private fun currentGlobalCoordinate(): Coordinate? {
        return Coordinate(Globals.CoordX, Globals.CoordY)
            .takeIf { it.isValid() }
    }

    private fun Location.toCoordinate(): Coordinate? {
        return Coordinate(latitude, longitude).takeIf { it.isValid() }
    }

    private fun Coordinate.isValid(): Boolean {
        return lat != 0.0 &&
                lon != 0.0 &&
                lat in -90.0..90.0 &&
                lon in -180.0..180.0
    }

    private fun logInfo(place: String, message: String) {
        Log.e(TAG, "$place: $message")
        Globals.writeToMLOG("INFO", "$TAG/$place", message)
    }

    private fun logError(place: String, message: String) {
        Log.e(TAG, "$place: $message")
        Globals.writeToMLOG("ERROR", "$TAG/$place", message)
    }

    private fun List<String>.previewIds(limit: Int = 50): String {
        if (isEmpty()) return "[]"
        val ids = take(limit).joinToString(",")
        return if (size > limit) "[$ids,... +${size - limit}]" else "[$ids]"
    }

    private fun List<Long>.previewLongIds(limit: Int = 50): String {
        if (isEmpty()) return "[]"
        val ids = take(limit).joinToString(",")
        return if (size > limit) "[$ids,... +${size - limit}]" else "[$ids]"
    }

    data class AddressPreloadResult(
        val json: JsonObject,
        val savedCount: Int,
        val savedTradeMarkCount: Int,
        val missingTradeMarkIds: List<String>,
        val errors: List<String>
    )

    data class OrderPreloadResult(
        val json: JsonObject,
        val savedCount: Int
    )

    private data class TradeMarkPreloadResult(
        val missingIds: List<String>,
        val savedCount: Int
    )

    private data class TradeMarkIdsCheck(
        val requiredIds: List<String>,
        val missingIds: List<String>
    )

    private data class Coordinate(
        val lat: Double,
        val lon: Double
    )

    private const val CREATED_VISIT_SYNC_TIMEOUT_MS = 20_000L
    private const val CREATED_VISIT_SYNC_POLL_INTERVAL_MS = 2_000L
}
