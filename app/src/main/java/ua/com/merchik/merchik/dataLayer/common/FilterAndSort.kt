package ua.com.merchik.merchik.dataLayer.common


import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.SortingField
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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

    val dateFmt = SimpleDateFormat("MMM d, yyyy hh:mm:ss a", Locale.US)

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
                val ts: Long = when (raw) {
                    is Long -> raw
                    is Date -> raw.time
                    is String -> try {
                        dateFmt.parse(raw)?.time ?: return@filter false
                    } catch (_: Exception) {
                        return@filter false
                    }

                    else -> return@filter false
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

    return FilterAndSortResult(sorted, isActiveFiltered, hasActiveSorting)
}
