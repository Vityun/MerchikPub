package ua.com.merchik.merchik.features.main.order

import android.content.Context
import android.location.Location
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.data.Database.Room.OrderDataSDB
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB
import ua.com.merchik.merchik.data.RetrofitResponse.tables.AddressResponse
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.retrofit.RetrofitBuilder
import ua.com.merchik.merchik.trecker

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
                    missingTradeMarkIds = tradeMarkResult.requestedIds
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
        val missingIds = findMissingTradeMarkIds(addresses)
        if (missingIds.isEmpty()) {
            logInfo("data_list.tovar_manufacturer_list", "skip: no missing trade marks")
            return TradeMarkPreloadResult(requestedIds = emptyList(), savedCount = 0)
        }

        val response = timeRequest("data_list.tovar_manufacturer_list") {
            val call = RetrofitBuilder.getRetrofitInterface()
                .GET_TRADE_MARKS_T(
                    "data_list",
                    "tovar_manufacturer_list",
                    missingIds.toTypedArray()
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
            "requestedIds=$missingIds, saved TradeMarkDB count=${tradeMarks.size}"
        )

        return TradeMarkPreloadResult(
            requestedIds = missingIds,
            savedCount = tradeMarks.size
        )
    }

    private suspend fun findMissingTradeMarkIds(addresses: List<AddressSDB>): List<String> =
        withContext(Dispatchers.Main) {
            addresses
                .asSequence()
                .mapNotNull { it.tpId }
                .filter { it > 0 }
                .map { it.toString() }
                .distinct()
                .filter { id -> TradeMarkRealm.getTradeMarkRowById(id) == null }
                .toList()
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
        val requestedIds: List<String>,
        val savedCount: Int
    )

    private data class Coordinate(
        val lat: Double,
        val lon: Double
    )
}
