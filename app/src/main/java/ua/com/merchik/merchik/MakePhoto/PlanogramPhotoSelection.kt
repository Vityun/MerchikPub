package ua.com.merchik.merchik.MakePhoto

import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammJOINSDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.database.room.RoomManager.SQL_DB

// The preflight check and the selector must use the same restrictions.
data class PlanogramPhotoSelection(
    val visit: WpDataDB,
    val networkId: Int?,
    val date: String
) {
    fun getPlans(): List<PlanogrammJOINSDB> = SQL_DB.planogrammDao().getByClientAddress(
        visit.client_id, visit.addr_id, networkId, date
    )

    companion object {
        @JvmStatic
        fun forVisit(visit: WpDataDB): PlanogramPhotoSelection {
            val address = requireNotNull(SQL_DB.addressDao().getById(visit.addr_id)) {
                "Address ${visit.addr_id} not found"
            }
            val date = Clock.getHumanTimeSecPattern(System.currentTimeMillis() / 1000, "yyyy-MM-dd")
            return PlanogramPhotoSelection(visit, address.tpId, date)
        }
    }
}
