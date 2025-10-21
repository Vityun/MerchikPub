package ua.com.merchik.merchik.features.maps.domain.usecases

import ua.com.merchik.merchik.dataLayer.common.FilterAndSortResult
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.SortingField
import ua.com.merchik.merchik.features.maps.domain.PointUi
import ua.com.merchik.merchik.features.maps.domain.StoreCenter
import ua.com.merchik.merchik.features.maps.domain.StorePoint
import ua.com.merchik.merchik.features.maps.domain.repositories.AddressRepository
import ua.com.merchik.merchik.features.maps.domain.repositories.ItemsRepository
import ua.com.merchik.merchik.features.maps.domain.scenarios.MapScenario
import java.time.LocalDate
import java.time.ZoneId

class FilterAndSortItemsUC(private val repo: ItemsRepository) {
    suspend operator fun invoke(
        items: List<DataItemUI>,
        filters: Filters?,
        sorting: List<SortingField?>,
        rangeStart: LocalDate?,
        rangeEnd: LocalDate?,
        search: String?,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): FilterAndSortResult = repo.filterAndSort(
        items, filters, sorting, rangeStart, rangeEnd, search, zoneId
    )
}


class BuildMapPointsUC(private val scenario: MapScenario, private val addrRepo: AddressRepository) {
    suspend operator fun invoke(items: List<DataItemUI>): Triple<StoreCenter?, List<StorePoint>, Map<Int, Pair<Int, Double>>> {
        val center = scenario.deriveStoreCenter(items)
        val points = scenario.buildPoints(items) { id -> addrRepo.getById(id) }
        val badges = scenario.aggregateCash(items)
        return Triple(center, points, badges)
    }
}


class MakePointUiUC(private val scenario: MapScenario) {
    operator fun invoke(center: StoreCenter?, points: List<StorePoint>, badges: Map<Int, Pair<Int, Double>>, radiusMeters: Double): List<PointUi> {
        return points.map { p ->
            val idInt = p.id?.toIntOrNull()
            val (count, sum) = badges[idInt] ?: (0 to 0.0)
            val inside = scenario.isInsideRadius(center, p, radiusMeters)
            PointUi(point = p, count = count, sum = sum, insideRadius = inside)
        }
    }
}