package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity

import androidx.annotation.MainThread
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.awaitResponse
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.RealmModels.OptionsDB
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB
import ua.com.merchik.merchik.data.RealmModels.TovarDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.database.realm.RealmManager
import ua.com.merchik.merchik.retrofit.RetrofitBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal object TARTovarDataLoader {
    data class Result(val visit: WpDataDB, val warnings: List<String>)

    // RealmManager.INSTANCE belongs to the main thread; only Retrofit performs background work.
    @MainThread
    suspend fun load(sourceDad2: Long): Result {
        require(sourceDad2 > 0) { "У ЗІР не вказано вихідне відвідування." }
        val visit = loadVisit(sourceDad2)
        val warnings = mutableListOf<String>()

        suspend fun loadPart(title: String, action: suspend () -> Unit) {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Globals.writeToMLOG("ERROR", "TARTovars/load", "dad2=$sourceDad2, part=$title, error=$e")
                warnings += "$title: не вдалося отримати всі дані."
            }
        }

        loadPart("Детальний звіт") { loadReportIfMissing(visit) }
        loadPart("Товари") { loadMissingProducts(sourceDad2) }
        loadPart("Опції") { loadOptionsIfMissing(sourceDad2) }
        return Result(checkNotNull(localVisit(sourceDad2)) {
            "Вихідне відвідування більше не доступне локально."
        }, warnings)
    }

    suspend fun loadVisit(dad2: Long): WpDataDB {
        require(dad2 > 0) { "У ЗІР не вказано вихідне відвідування." }
        localVisit(dad2)?.let { return it }
        val request = visitRequest("plan", "list", dad2).apply {
            addProperty("date_from", "")
            addProperty("date_to", "")
        }
        val response = RetrofitBuilder.getRetrofitInterface()
            .WpDataServer_RESPONSE(RetrofitBuilder.contentType, request).bodyOrThrow()
        check(response.state == true) { response.error ?: "plan/list: state != true" }
        val visits = response.list.orEmpty().filter { it.code_dad2 == dad2 }
        check(visits.isNotEmpty()) { "Вихідне відвідування не знайдено на сервері." }

        // Another exchange may have saved this visit while the request was running.
        if (localVisit(dad2) == null) {
            RealmManager.updateWorkPlanFromServer(visits)
        }
        return checkNotNull(localVisit(dad2)) { "Вихідне відвідування недоступне для цього користувача." }
    }

    private fun localVisit(dad2: Long): WpDataDB? =
        RealmManager.getWorkPlanRowByCodeDad2(dad2)?.let {
            RealmManager.INSTANCE.copyFromRealm(it)
        }

    private suspend fun loadReportIfMissing(visit: WpDataDB) {
        val dad2 = visit.code_dad2.toString()
        val realm = RealmManager.INSTANCE
        if (realm.where(ReportPrepareDB::class.java).equalTo("codeDad2", dad2).count() > 0) return

        val date = checkNotNull(visit.dt) { "У вихідного відвідування не вказана дата." }
        val request = visitRequest("report_prepare", "list_data", visit.code_dad2).apply {
            addProperty("date_from", dateWithOffset(date, -30))
            addProperty("date_to", dateWithOffset(date, 5))
        }
        Globals.writeToMLOG("INFO", "TARTovars/report", "request=$request")
        val response = RetrofitBuilder.getRetrofitInterface()
            .DOWNLOAD_REPORT_PREPARE(RetrofitBuilder.contentType, request).bodyOrThrow()
        check(response.state == true) { response.error ?: "report_prepare: state != true" }
        val rows = checkNotNull(response.list) { "report_prepare: list is null" }
            .filter { it.codeDad2 == dad2 && it.iD != null && !it.tovarId.isNullOrBlank() }

        // Fill gaps only: never replace a report row edited locally during the request.
        realm.executeTransaction { db ->
            rows.forEach { row ->
                val sameId = db.where(ReportPrepareDB::class.java).equalTo("iD", row.iD).findFirst()
                val sameProduct = db.where(ReportPrepareDB::class.java)
                    .equalTo("codeDad2", dad2).equalTo("tovarId", row.tovarId).findFirst()
                if (sameId == null && sameProduct == null) db.insert(row)
            }
        }
    }

    private fun missingProductIds(dad2: Long): List<String> {
        val realm = RealmManager.INSTANCE
        val required = realm.where(ReportPrepareDB::class.java)
            .equalTo("codeDad2", dad2.toString()).findAll()
            .mapNotNull { it.tovarId?.takeIf { id -> id.isNotBlank() && id != "0" } }
            .distinct()
        if (required.isEmpty()) return emptyList()
        // Deleted products are present too; the regular product query decides whether to show them.
        val existing = realm.where(TovarDB::class.java).`in`("iD", required.toTypedArray())
            .findAll().map { it.getiD() }.toSet()
        return required.filterNot { it in existing }
    }

    private suspend fun loadMissingProducts(dad2: Long) {
        val missing = missingProductIds(dad2)
        if (missing.isEmpty()) return
        Globals.writeToMLOG("INFO", "TARTovars/products", "dad2=$dad2, missing=${missing.size}, ids=${missing.take(20)}")
        for (ids in missing.chunked(200)) {
            val request = JsonObject().apply {
                addProperty("mod", "data_list")
                addProperty("act", "tovar_list")
                add("id", JsonArray().apply { ids.forEach { add(it) } })
                addProperty("deleted", 2)
            }
            val response = RetrofitBuilder.getRetrofitInterface()
                .GET_TOVAR_TABLE(RetrofitBuilder.contentType, request).bodyOrThrow()
            check(response.state == true) { response.error ?: "tovar_list: state != true" }
            val products = checkNotNull(response.list) { "tovar_list: list is null" }
                .filter { it.getiD() in ids }.distinctBy { it.getiD() }
            RealmManager.INSTANCE.executeTransaction { db ->
                products.forEach { product ->
                    if (db.where(TovarDB::class.java).equalTo("iD", product.getiD()).findFirst() == null) {
                        db.insert(product)
                    }
                }
            }
        }
        val remaining = missingProductIds(dad2)
        check(remaining.isEmpty()) { "tovar_list: missing=${remaining.size}, ids=${remaining.take(20)}" }
    }

    private suspend fun loadOptionsIfMissing(dad2: Long) {
        val realm = RealmManager.INSTANCE
        if (realm.where(OptionsDB::class.java).equalTo("codeDad2", dad2.toString()).count() > 0) return
        val response = RetrofitBuilder.getRetrofitInterface()
            .GET_OPTIONS(RetrofitBuilder.contentType, visitRequest("plan", "options_list", dad2))
            .bodyOrThrow()
        check(response.state == true) { response.error ?: "options_list: state != true" }
        val rows = checkNotNull(response.list) { "options_list: list is null" }
            .filter { it.codeDad2 == dad2.toString() && !it.getID().isNullOrBlank() }
        realm.executeTransaction { db ->
            rows.forEach { row ->
                if (db.where(OptionsDB::class.java).equalTo("iD", row.getID()).findFirst() == null) {
                    db.insert(row)
                }
            }
        }
    }

    private fun visitRequest(mod: String, act: String, dad2: Long) = JsonObject().apply {
        addProperty("mod", mod)
        addProperty("act", act)
        addProperty("code_dad2", dad2.toString())
    }

    private fun dateWithOffset(date: Date, days: Int): String {
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_MONTH, days)
        }
        return SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendar.time)
    }

    private suspend fun <T : Any> Call<T>.bodyOrThrow(): T {
        val response = awaitResponse()
        if (!response.isSuccessful) throw HttpException(response)
        return checkNotNull(response.body()) { "Empty server response" }
    }
}
