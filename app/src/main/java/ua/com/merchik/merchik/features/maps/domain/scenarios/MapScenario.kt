package ua.com.merchik.merchik.features.maps.domain.scenarios

import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.dataLayer.model.DataItemUI
import ua.com.merchik.merchik.features.maps.domain.StoreCenter
import ua.com.merchik.merchik.features.maps.domain.StorePoint


interface MapScenario {
    fun deriveStoreCenter(items: List<DataItemUI>): StoreCenter?


    suspend fun buildPoints(
        items: List<DataItemUI>,
        addressResolver: suspend (Int) -> AddressSDB?
    ): List<StorePoint>


    fun isInsideRadius(center: StoreCenter?, point: StorePoint, radiusMeters: Double): Boolean


    fun aggregateCash(items: List<DataItemUI>): Map<Int, Pair<Int, Double>>
}