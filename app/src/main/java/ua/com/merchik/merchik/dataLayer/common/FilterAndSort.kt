package ua.com.merchik.merchik.dataLayer.common


import android.util.Log
import com.google.gson.Gson
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.features.main.Main.SortingField
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

data class FilterAndSortResult(
    val items: List<DataItemUI>,
    val isActiveFiltered: Boolean,
    val isActiveSorted: Boolean
)

fun filterAndSortDataItems(
    items: List<DataItemUI>,
    filters: Filters?,                    // твоя модель фильтров
    sortingFields: List<SortingField?>,   // список до 3-х, можно и больше
    rangeStart: LocalDate?,               // viewModel.rangeDataStart.value
    rangeEnd: LocalDate?,                 // viewModel.rangeDataEnd.value
    searchText: String?,                  // uiState.filters?.searchText
    zoneId: ZoneId = ZoneId.systemDefault()
): FilterAndSortResult {

    // --- подготовка входных данных ---
    val searchTerms: List<String> = searchText
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.split("\\s+".toRegex())
        ?: emptyList()

    // заранее считаем границы диапазона (если заданы)
    val startMillis: Long = rangeStart
        ?.atStartOfDay(zoneId)
        ?.toInstant()
        ?.toEpochMilli()
        ?: Long.MIN_VALUE

    val endMillis: Long = rangeEnd
        ?.atTime(LocalTime.MAX)
        ?.atZone(zoneId)
        ?.toInstant()
        ?.toEpochMilli()
        ?: Long.MAX_VALUE


    var isActiveFiltered = false
    if (searchTerms.isNotEmpty()) isActiveFiltered = true
    if (filters?.items?.any { it.rightValuesRaw.isNotEmpty() } == true) isActiveFiltered = true
    if (filters?.rangeDataByKey != null && (rangeStart != null || rangeEnd != null)) isActiveFiltered =
        true

    // есть ли вообще активные инструкции сортировки?
    val hasActiveSorting =
        sortingFields.any { it?.key?.isNotBlank() == true && (it.order == 1 || it.order == -1) }

    // --- утилиты для сортировки ---
    fun getSortValue(item: DataItemUI, sortingField: SortingField): String? =
        item.fields.firstOrNull { fv -> fv.key.equals(sortingField.key, ignoreCase = true) }
            ?.value?.value

    fun comparator(sf: SortingField?): Comparator<DataItemUI> =
        when (sf?.order) {
            1 -> compareBy<DataItemUI, String?>(nullsLast(naturalOrder())) { getSortValue(it, sf) }
            -1 -> compareByDescending<DataItemUI, String?>(nullsLast(naturalOrder())) {
                getSortValue(
                    it,
                    sf
                )
            }

            else -> Comparator { _, _ -> 0 }
        }

    // --- сама фильтрация ---
    val filtered = items.filter { dataItemUI ->

        // 1) Фильтр по диапазону дат (если задан ключ)
        filters?.rangeDataByKey?.let { rangeKey ->
            // ищем нужное поле и приводим к timestamp
            val tsOk = dataItemUI.fields.all { fv ->
                if (!fv.key.equals(rangeKey.key, ignoreCase = true)) return@all true

                val raw = fv.value.rawValue
//                Log.d("DBG_DATE", "DBG_DATE field=${fv.key}, raw=${raw?.toString()?.take(200)} class=${raw?.javaClass?.name}")
                val ts = parseToMillis(raw, zoneId)
                if (ts == null) {
                    Log.d("DBG_DATE_PARSE", "Failed parse raw for item field. field=${fv.key}, raw=${raw}, class=${raw?.javaClass?.name}")
                    return@filter false
                }
                ts in startMillis..endMillis
            }
            if (!tsOk) return@filter false
        }

        // 2) Фильтр по поисковой строке (каждый терм должен встретиться хотя бы в одном поле)
        if (searchTerms.isNotEmpty()) {
            val allTermsFound = searchTerms.all { term ->
                dataItemUI.fields.any { fv -> fv.value.value.contains(term, ignoreCase = true) }
            }
            if (!allTermsFound) return@filter false
        }

        // 3) Фильтры по значениям rawFields (правые значения – список допустимых)
        filters?.items?.forEach { flt ->
            if (flt.rightValuesRaw.isNotEmpty()) {
                val matched = dataItemUI.rawFields.any { fv ->
                    fv.key.equals(flt.leftField, ignoreCase = true) &&
                            flt.rightValuesRaw.contains(fv.value.rawValue.toString())
                }
                if (!matched) return@filter false
            }
        }

        true
    }

    // --- сортировка (до трёх уровней, как у тебя) ---
    val sorted = if (hasActiveSorting) {
        filtered.sortedWith(
            comparator(sortingFields.getOrNull(0))
                .thenComparing(comparator(sortingFields.getOrNull(1)))
                .thenComparing(comparator(sortingFields.getOrNull(2)))
        )
    } else filtered

//    Log.e("DBG_FILTERS", "DBG_FILTERS filterAndSortDataItems will complete")

    return FilterAndSortResult(sorted, isActiveFiltered, hasActiveSorting)
}


/**
 * Универсальный парсер дат/времён в millis.
 * Пытается:
 *  1) numeric timestamp (seconds или millis)
 *  2) ISO instant/parsers
 *  3) набор шаблонов (24h сначала), пробует с Locale.getDefault() и Locale.US
 *  4) если шаблон без времени — ставит начало дня (00:00) в указанной zoneId
 *
 * Возвращает null, если не удалось распарсить.
 */
fun parseToMillis(raw: Any?, zoneId: ZoneId = ZoneId.systemDefault()): Long? {
    if (raw == null) return null
    try {
        when (raw) {
            is Long -> return raw
            is Int -> return raw.toLong()
            is Double -> return raw.toLong()
            is Float -> return raw.toLong()
            is Number -> return raw.toLong()
            is Date -> return raw.time
            is String -> {
                val s = raw.trim()
                if (s.isEmpty()) return null

                // 1) numeric timestamp string (seconds or millis)
                if (s.matches(Regex("^\\d{10,}$"))) {
                    val v = s.toLongOrNull() ?: return null
                    return if (s.length == 10) v * 1000L else v
                }

                // 2) ISO instant or offset date-time (e.g. 2025-09-17T12:34:56Z / ...+03:00)
                try {
                    val inst = Instant.parse(s)
                    return inst.toEpochMilli()
                } catch (_: DateTimeParseException) { /* ignore */ }

                // 3) Try ZonedDateTime/OffsetDateTime parsing (common ISO with offset)
                try {
                    val odt = OffsetDateTime.parse(s)
                    return odt.toInstant().toEpochMilli()
                } catch (_: DateTimeParseException) { /* ignore */ }

                try {
                    val zdt = ZonedDateTime.parse(s)
                    return zdt.toInstant().toEpochMilli()
                } catch (_: DateTimeParseException) { /* ignore */ }

                // 4) Patterns — 24h first (defaults), 12h 'a' as fallback, date-only as last
                val patterns = listOf(
                    "MMM d, yyyy HH:mm:ss",    // Sep 11, 2025 00:00:00  <- твой 24h пример
                    "MMM dd, yyyy HH:mm:ss",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ss",   // without offset
                    "d MMM yyyy HH:mm:ss",
                    "d MMM yyyy",              // date-only
                    // fallback 12-hour with AM/PM
                    "MMM d, yyyy hh:mm:ss a",
                    "MMM dd, yyyy hh:mm:ss a"
                )

                // try each pattern with both default locale and US (covers localized month names)
                for (pat in patterns) {
                    listOf(Locale.getDefault(), Locale.US).forEach { loc ->
                        try {
                            val fmt = DateTimeFormatter.ofPattern(pat, loc)
                            // For patterns including time
                            if (pat.contains("H") || pat.contains("h")) {
                                val ldt = LocalDateTime.parse(s, fmt)
                                return ldt.atZone(zoneId).toInstant().toEpochMilli()
                            } else {
                                // date-only pattern -> treat as start of day
                                val ld = LocalDate.parse(s, fmt)
                                return ld.atStartOfDay(zoneId).toInstant().toEpochMilli()
                            }
                        } catch (_: DateTimeParseException) {
                            // try next
                        } catch (e: Exception) {
                            // safety net for unexpected
                            Log.d("PARSE_DATE_ERR", "pattern fail pat=$pat locale=$loc s='$s' : ${e.message}")
                        }
                    }
                }

                // 5) last resort: try parse as LocalDateTime with relaxed ISO formats
                try {
                    val ldt = LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    return ldt.atZone(zoneId).toInstant().toEpochMilli()
                } catch (_: Exception) { /* ignore */ }

                // nothing matched
                return null
            }
            else -> return null
        }
    } catch (e: Throwable) {
        Log.e("PARSE_DATE_ERR", "parseToMillis error for value=$raw class=${raw::class.java.name}", e)
        return null
    }
}
