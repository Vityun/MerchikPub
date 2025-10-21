package ua.com.merchik.merchik.features.maps.domain.repositories

import ua.com.merchik.merchik.dataLayer.common.FilterAndSortResult
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.SortingField
import java.time.LocalDate
import java.time.ZoneId



interface ItemsRepository {
    suspend fun filterAndSort(
        items: List<DataItemUI>,
        filters: Filters?,
        sorting: List<SortingField?>,
        rangeStart: java.time.LocalDate?,
        rangeEnd: java.time.LocalDate?,
        searchText: String?,
        zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault()
    ): FilterAndSortResult
}
