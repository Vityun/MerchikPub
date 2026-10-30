package ua.com.merchik.merchik.MakePhoto

import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammJOINSDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.database.room.RoomManager.SQL_DB

// The preflight check and the selector must use the same restrictions.
data class PlanogramPhotoSelection(
    val context: PhotoReferenceContext,
    val networkId: Int?,
    val date: String
) {
    fun getPlans(): List<PlanogrammJOINSDB> = SQL_DB.planogrammDao().getByClientAddress(
        context.clientId, context.addressId, networkId, date
    )

    companion object {
        @JvmStatic
        fun forVisit(visit: WpDataDB): PlanogramPhotoSelection = forContext(PhotoReferenceContext.fromVisit(visit))

        fun forContext(context: PhotoReferenceContext): PlanogramPhotoSelection {
            val address = requireNotNull(SQL_DB.addressDao().getById(context.addressId)) {
                "Address ${context.addressId} not found"
            }
            val date = Clock.getHumanTimeSecPattern(System.currentTimeMillis() / 1000, "yyyy-MM-dd")
            return PlanogramPhotoSelection(context, address.tpId, date)
        }
    }
}
