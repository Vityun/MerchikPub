package ua.com.merchik.merchik.dataLayer.common


import android.util.Log
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.GroupMeta
import ua.com.merchik.merchik.features.main.Main.GroupingField
import ua.com.merchik.merchik.features.main.Main.SortingField
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

data class FilterAndSortResult(
    val items: List<DataItemUI>,
    val groups: List<GroupMeta> = emptyList(),
    val isActiveFiltered: Boolean,
    val isActiveSorted: Boolean,
    val isActiveGrouped: Boolean
)


fun filterAndSortDataItems(
    items: List<DataItemUI>,
    filters: Filters?,                    // твоя модель фильтров
    sortingFields: List<SortingField?>,   // список до 3-х, можно и больше
    groupingFields: List<GroupingField?>, // ← добавили
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
    if (filters?.rangeDataByKey != null && (rangeStart != null || rangeEnd != null)) {
        isActiveFiltered = true
    }

    // есть ли вообще активные инструкции сортировки?
    val hasActiveSorting =
        sortingFields.any { it?.key?.isNotBlank() == true && (it.order == 1 || it.order == -1) }

    // активные инструкции группировки (уже отсортированы по priority)
    val activeGrouping: List<GroupingField> = groupingFields
        .filter { it?.key?.isNotBlank() == true }
        .map { it!! }
        .sortedBy { it.priority }

    val hasActiveGrouping = activeGrouping.isNotEmpty()

    // --- сама фильтрация ---
    val filtered = items.filter { dataItemUI ->

        // 1) Фильтр по диапазону дат (если задан ключ)
        filters?.rangeDataByKey?.let { rangeKey ->
            val tsOk = dataItemUI.fields.all { fv ->
                if (!fv.key.equals(rangeKey.key, ignoreCase = true)) return@all true

                val raw = fv.value.rawValue
                val ts = parseToMillis(raw, zoneId)
                if (ts == null) {
                    Log.d(
                        "DBG_DATE_PARSE",
                        "Failed parse raw for item field. field=${fv.key}, raw=${raw}, class=${raw?.javaClass?.name}"
                    )
                    return@filter false
                }
                ts in startMillis..endMillis
            }
            if (!tsOk) return@filter false
        }

        // 2) Фильтр по поисковой строке
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

    // общий компаратор для сортировки (если есть)
    val baseComparator: Comparator<DataItemUI>? = if (hasActiveSorting) {
        makeComparator(sortingFields.getOrNull(0), zoneId)
            .thenComparing(makeComparator(sortingFields.getOrNull(1), zoneId))
            .thenComparing(makeComparator(sortingFields.getOrNull(2), zoneId))
    } else null

    // --- без группировки: как раньше ---
    if (!hasActiveGrouping) {
        val sorted = if (baseComparator != null) {
            filtered.sortedWith(baseComparator)
        } else {
            filtered
        }

          return FilterAndSortResult(
            items = sorted,
            groups = emptyList(),
            isActiveFiltered = isActiveFiltered,
            isActiveSorted = hasActiveSorting,
            isActiveGrouped = false
        )
    }

    // --- с группировкой ---

    // ✅ для TOP-уровня используем ТОЛЬКО первую группировку
    val topGrouping = activeGrouping.first()
    val groupingForTopLevel = listOf(topGrouping)

    // 1) группируем по значению первого поля группировки
    val grouped: Map<List<Comparable<*>?>, List<DataItemUI>> = filtered.groupBy { item ->
        extractGroupKeyValues(item, groupingForTopLevel, zoneId)
    }

    // 2) компаратор для ключей групп (список из одного элемента, но код универсальный)
    val groupKeyComparator = Comparator<List<Comparable<*>?>> { k1, k2 ->
        val maxSize = maxOf(k1.size, k2.size)
        for (i in 0 until maxSize) {
            val v1 = k1.getOrNull(i)
            val v2 = k2.getOrNull(i)
            val cmp = compareValues(v1, v2)
            if (cmp != 0) return@Comparator cmp
        }
        0
    }

    // 3) сортируем группы по ключам
    val sortedGroupEntries = grouped.entries.sortedWith { e1, e2 ->
        groupKeyComparator.compare(e1.key, e2.key)
    }

    // 4) разворачиваем в плоский список + считаем мету по группам
    val resultItems = mutableListOf<DataItemUI>()
    val groupsMeta = mutableListOf<GroupMeta>()
    var index = 0

    for ((groupKeyValues, groupItems) in sortedGroupEntries) {
        val sortedGroupItems = if (baseComparator != null) {
            groupItems.sortedWith(baseComparator)
        } else {
            groupItems
        }

        val startIndex = index
        resultItems.addAll(sortedGroupItems)
        index += sortedGroupItems.size

        // 👇 тайтл строим тоже только по первой группировке
        val groupTitle = buildGroupTitle(
            sampleItem = sortedGroupItems.firstOrNull(),
            grouping = groupingForTopLevel,
            groupKeyValues = groupKeyValues
        )

        groupsMeta += GroupMeta(
            groupKey = groupTitle,
            title = groupTitle,
            startIndex = startIndex,
            endIndexExclusive = index
        )
    }

    return FilterAndSortResult(
        items = resultItems,
        groups = groupsMeta,
        isActiveFiltered = isActiveFiltered,
        isActiveSorted = hasActiveSorting,
        isActiveGrouped = true
    )
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
            is Date -> return raw.time

            is Long -> return normalizeEpochToMillis(raw) ?: raw
            is Int -> return normalizeEpochToMillis(raw.toLong()) ?: raw.toLong()
            is Double -> return normalizeEpochToMillis(raw.toLong()) ?: raw.toLong()
            is Float -> return normalizeEpochToMillis(raw.toLong()) ?: raw.toLong()
            is Number -> return normalizeEpochToMillis(raw.toLong()) ?: raw.toLong()

            is String -> {
                val s = raw.trim()
                if (s.isEmpty()) return null

                // numeric timestamp string
                if (s.matches(Regex("^\\d+$"))) {
                    val v = s.toLongOrNull() ?: return null
                    return normalizeEpochToMillis(v) ?: v
                }

                // ISO instant / offset / zoned
                try { return Instant.parse(s).toEpochMilli() } catch (_: DateTimeParseException) {}
                try { return OffsetDateTime.parse(s).toInstant().toEpochMilli() } catch (_: DateTimeParseException) {}
                try { return ZonedDateTime.parse(s).toInstant().toEpochMilli() } catch (_: DateTimeParseException) {}

                val patterns = listOf(
                    "MMM d, yyyy HH:mm:ss",
                    "MMM dd, yyyy HH:mm:ss",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "d MMM yyyy HH:mm:ss",
                    "d MMM yyyy",
                    "MMM d, yyyy hh:mm:ss a",
                    "MMM dd, yyyy hh:mm:ss a"
                )

                for (pat in patterns) {
                    for (loc in listOf(Locale.getDefault(), Locale.US)) {
                        try {
                            val fmt = DateTimeFormatter.ofPattern(pat, loc)
                            return if (pat.contains("H") || pat.contains("h")) {
                                LocalDateTime.parse(s, fmt).atZone(zoneId).toInstant().toEpochMilli()
                            } else {
                                LocalDate.parse(s, fmt).atStartOfDay(zoneId).toInstant().toEpochMilli()
                            }
                        } catch (_: DateTimeParseException) {
                        } catch (e: Exception) {
                            Log.d("PARSE_DATE_ERR", "pattern fail pat=$pat locale=$loc s='$s' : ${e.message}")
                        }
                    }
                }

                try {
                    return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .atZone(zoneId).toInstant().toEpochMilli()
                } catch (_: Exception) {}

                return null
            }

            else -> return null
        }
    } catch (e: Throwable) {
        Log.e("PARSE_DATE_ERR", "parseToMillis error for value=$raw class=${raw::class.java.name}", e)
        return null
    }
}
//fun parseToMillis(raw: Any?, zoneId: ZoneId = ZoneId.systemDefault()): Long? {
//    if (raw == null) return null
//    try {
//        when (raw) {
//            is Long -> return raw
//            is Int -> return raw.toLong()
//            is Double -> return raw.toLong()
//            is Float -> return raw.toLong()
//            is Number -> return raw.toLong()
//            is Date -> return raw.time
//            is String -> {
//                val s = raw.trim()
//                if (s.isEmpty()) return null
//
//                // 1) numeric timestamp string (seconds or millis)
//                if (s.matches(Regex("^\\d{10,}$"))) {
//                    val v = s.toLongOrNull() ?: return null
//                    return if (s.length < 12)
//                        v * 1000L else v
//                }
//
//
//                // 2) ISO instant or offset date-time (e.g. 2025-09-17T12:34:56Z / ...+03:00)
//                try {
//                    val inst = Instant.parse(s)
//                    return inst.toEpochMilli()
//                } catch (_: DateTimeParseException) { /* ignore */ }
//
//                // 3) Try ZonedDateTime/OffsetDateTime parsing (common ISO with offset)
//                try {
//                    val odt = OffsetDateTime.parse(s)
//                    return odt.toInstant().toEpochMilli()
//                } catch (_: DateTimeParseException) { /* ignore */ }
//
//                try {
//                    val zdt = ZonedDateTime.parse(s)
//                    return zdt.toInstant().toEpochMilli()
//                } catch (_: DateTimeParseException) { /* ignore */ }
//
//                // 4) Patterns — 24h first (defaults), 12h 'a' as fallback, date-only as last
//                val patterns = listOf(
//                    "MMM d, yyyy HH:mm:ss",    // Sep 11, 2025 00:00:00  <- твой 24h пример
//                    "MMM dd, yyyy HH:mm:ss",
//                    "yyyy-MM-dd HH:mm:ss",
//                    "yyyy-MM-dd'T'HH:mm:ss",   // without offset
//                    "d MMM yyyy HH:mm:ss",
//                    "d MMM yyyy",              // date-only
//                    // fallback 12-hour with AM/PM
//                    "MMM d, yyyy hh:mm:ss a",
//                    "MMM dd, yyyy hh:mm:ss a"
//                )
//
//                // try each pattern with both default locale and US (covers localized month names)
//                for (pat in patterns) {
//                    listOf(Locale.getDefault(), Locale.US).forEach { loc ->
//                        try {
//                            val fmt = DateTimeFormatter.ofPattern(pat, loc)
//                            // For patterns including time
//                            if (pat.contains("H") || pat.contains("h")) {
//                                val ldt = LocalDateTime.parse(s, fmt)
//                                return ldt.atZone(zoneId).toInstant().toEpochMilli()
//                            } else {
//                                // date-only pattern -> treat as start of day
//                                val ld = LocalDate.parse(s, fmt)
//                                return ld.atStartOfDay(zoneId).toInstant().toEpochMilli()
//                            }
//                        } catch (_: DateTimeParseException) {
//                            // try next
//                        } catch (e: Exception) {
//                            // safety net for unexpected
//                            Log.d("PARSE_DATE_ERR", "pattern fail pat=$pat locale=$loc s='$s' : ${e.message}")
//                        }
//                    }
//                }
//
//                // 5) last resort: try parse as LocalDateTime with relaxed ISO formats
//                try {
//                    val ldt = LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
//                    return ldt.atZone(zoneId).toInstant().toEpochMilli()
//                } catch (_: Exception) { /* ignore */ }
//
//                // nothing matched
//                return null
//            }
//            else -> return null
//        }
//    } catch (e: Throwable) {
//        Log.e("PARSE_DATE_ERR", "parseToMillis error for value=$raw class=${raw::class.java.name}", e)
//        return null
//    }
//}

private fun normalizeEpochToMillis(v: Long): Long? {
    val abs = kotlin.math.abs(v)

    return when {
        // seconds (10 digits-ish) -> millis
        abs in 1_000_000_000L..9_999_999_999L -> v * 1000L

        // millis (13 digits-ish)
        abs in 1_000_000_000_000L..9_999_999_999_999L -> v

        // micros (16 digits-ish) -> millis
        abs in 1_000_000_000_000_000L..9_999_999_999_999_999L -> v / 1000L

        // nanos (19 digits-ish) -> millis
        abs >= 1_000_000_000_000_000_000L -> v / 1_000_000L

        else -> {
            // слишком маленькое число: это может быть не timestamp
            // если хочешь — можно вернуть v как есть, но обычно безопаснее null
            null
        }
    }
}

private fun parseNum(raw: Any?): Double? = when (raw) {
    null -> null
    is Number -> raw.toDouble()
    is String -> raw
        .trim()
        .replace(" ", "")
        .replace(",", ".")
        .toDoubleOrNull()
    else -> null
}

// эвристика: по ключу понимаем, что это поле-дата
private fun looksLikeDateKey(key: String): Boolean {
    val k = key.lowercase()
    return k.contains("dt") || k.contains("date") || k.contains("time")
}

private fun getFieldRaw(item: DataItemUI, key: String): Any? =
    item.fields.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value?.rawValue


private fun makeComparator(
    sf: SortingField?,
    zoneId: ZoneId
): Comparator<DataItemUI> {
    if (sf?.key == null || sf.key.isBlank() || (sf.order != 1 && sf.order != -1))
        return Comparator { _, _ -> 0 }

    val key = sf.key
    val asc = sf.order == 1

    // готовим селектор "сначала пробуем дату, потом число, потом строку"
    val selectorDate: (DataItemUI) -> Long? = { item ->
        val raw = key?.let { getFieldRaw(item, it) }
        // если ключ похож на дату — пробуем распарсить в любом случае
        val ts = parseToMillis(raw, zoneId)
        if (ts != null) ts
        else if (key?.let { looksLikeDateKey(it) } == true) parseToMillis(
            // иногда value.value хранит нормализованную строку
            item.fields.firstOrNull { it.key.equals(key, true) }?.value?.value,
            zoneId
        )
        else null
    }
    val selectorNum: (DataItemUI) -> Double? = { item ->
        parseNum(key?.let { getFieldRaw(item, it) })
            ?: parseNum(item.fields.firstOrNull { it.key.equals(key, true) }?.value?.value)
    }
    val selectorStr: (DataItemUI) -> String? = { item ->
        item.fields.firstOrNull { it.key.equals(key, true) }?.value?.value
    }

    // Приоритет: дата > число > строка
    val cmpDate = compareBy<DataItemUI, Long?>(nullsLast(naturalOrder()), selectorDate)
    val cmpNum  = compareBy<DataItemUI, Double?>(nullsLast(naturalOrder()), selectorNum)
    val cmpStr  = compareBy<DataItemUI, String?>(nullsLast(naturalOrder()), selectorStr)

    val base = cmpDate.thenComparing(cmpNum).thenComparing(cmpStr)
    return if (asc) base else base.reversed()
}


private fun extractGroupKeyValues(
    item: DataItemUI,
    grouping: List<GroupingField>,
    zoneId: ZoneId
): List<Comparable<*>?> {
    return grouping.map { g ->
        val fv = item.fields.firstOrNull { it.key.equals(g.key, ignoreCase = true) }
        val raw = fv?.value?.rawValue

        // пробуем как дату/время
        val ts = parseToMillis(raw, zoneId)
        if (ts != null) {
            ts as Comparable<*> // Long
        } else {
            // fallback – UI-строка, чтобы хотя бы как-то сравнивалось
            fv?.value?.value ?: raw?.toString() ?: ""
        }
    }
}

private fun buildGroupTitle(
    sampleItem: DataItemUI?,
    grouping: List<GroupingField>,
    groupKeyValues: List<Comparable<*>?>
): String {
    if (sampleItem == null) return groupKeyValues.joinToString(" • ") { it?.toString().orEmpty() }

    val parts = grouping.mapIndexed { index, g ->
        val fv = sampleItem.fields.firstOrNull { it.key.equals(g.key, ignoreCase = true) }
        val txt = fv?.value?.value
        if (!txt.isNullOrBlank()) {
            txt
        } else {
            groupKeyValues.getOrNull(index)?.toString().orEmpty()
        }
    }.filter { it.isNotBlank() }

    return parts.joinToString(" • ").ifBlank {
        groupKeyValues.joinToString(" • ") { it?.toString().orEmpty() }
    }
}

