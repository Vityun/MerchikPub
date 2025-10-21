package ua.com.merchik.merchik.features.maps.domain.repositories

import ua.com.merchik.merchik.data.Database.Room.AddressSDB

interface AddressRepository {
    suspend fun getById(id: Int): AddressSDB?
}