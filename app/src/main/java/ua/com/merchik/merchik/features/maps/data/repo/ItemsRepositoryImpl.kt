package ua.com.merchik.merchik.features.maps.data.repo

import ua.com.merchik.merchik.dataLayer.common.FilterAndSortResult
import ua.com.merchik.merchik.dataLayer.common.filterAndSortDataItems
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.GroupingField
import ua.com.merchik.merchik.features.main.Main.SortingField
import ua.com.merchik.merchik.features.maps.domain.repositories.ItemsRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject


class ItemsRepositoryImpl  @Inject constructor() : ItemsRepository {
    override suspend fun filterAndSort(
        items: List<DataItemUI>,
        filters: Filters?,
        sorting: List<SortingField?>,
        grouping: List<GroupingField?>,
        rangeStart: LocalDate?,
        rangeEnd: LocalDate?,
        searchText: String?,
        zoneId: ZoneId
    ): FilterAndSortResult = filterAndSortDataItems(
        items = items,
        filters = filters,
        sortingFields = sorting,
        groupingFields = grouping,
        rangeStart = rangeStart,
        rangeEnd = rangeEnd,
        searchText = searchText,
        zoneId = zoneId
    )
}
