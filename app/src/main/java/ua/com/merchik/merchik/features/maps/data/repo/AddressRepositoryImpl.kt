package ua.com.merchik.merchik.features.maps.data.repo

import ua.com.merchik.merchik.data.Database.Room.AddressSDB
import ua.com.merchik.merchik.database.room.RoomManager
import ua.com.merchik.merchik.features.maps.domain.repositories.AddressRepository
import javax.inject.Inject

class AddressRepositoryImpl @Inject constructor() :  AddressRepository {
    override suspend fun getById(id: Int): AddressSDB? = RoomManager.SQL_DB.addressDao().getById(id)
}