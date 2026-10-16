package ua.com.merchik.merchik.features.main.Main

import com.google.gson.Gson
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import java.time.LocalDate

private const val FILTER_LOG_LIMIT = 12

internal fun buildWorkPlanFilterLog(
    context: String,
    before: Int,
    visible: List<DataItemUI>,
    filters: Filters?,
    rangeStart: LocalDate?,
    rangeEnd: LocalDate?,
    error: String? = null
): String {
    val dad2 = visible.flatMap { item ->
        item.rawObj.filterIsInstance<WpDataDB>().map { it.code_dad2 }
    }.sorted()
    val activeFilters = filters?.items.orEmpty()
        .filter { it.rightValuesRaw.isNotEmpty() }
        .map { filter ->
            linkedMapOf(
                "field" to filter.leftField,
                "mode" to if (filter.excludeMode) "exclude" else "include",
                "count" to filter.rightValuesRaw.size,
                "values" to filter.rightValuesRaw.take(FILTER_LOG_LIMIT).map { it?.take(120) },
                "valuesHash" to filter.rightValuesRaw.hashCode()
            )
        }
    return Gson().toJson(linkedMapOf(
        "context" to context,
        "before" to before,
        "after" to visible.size,
        "date" to filters?.rangeDataByKey?.let {
            linkedMapOf("field" to it.key, "from" to rangeStart?.toString(), "to" to rangeEnd?.toString())
        },
        "search" to filters?.searchText.orEmpty().take(120),
        "searchHash" to filters?.searchText.orEmpty().hashCode(),
        "filters" to activeFilters,
        "visibleDad2" to dad2.take(FILTER_LOG_LIMIT),
        "visibleDad2Count" to dad2.size,
        "visibleDad2Hash" to dad2.hashCode(),
        "error" to error
    ))
}
