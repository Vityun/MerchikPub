package ua.com.merchik.merchik.MakePhoto

import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammSDB
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB
import ua.com.merchik.merchik.database.room.RoomManager.SQL_DB

data class ShowcasePhotoMetadata(
    val showcaseId: String,
    val imageId: String,
    val planogramId: String,
    val planogramImageId: String,
    val needsPlanogram: Boolean
) {
    companion object {
        @JvmStatic
        fun load(dad2: Long, photoType: String, showcase: ShowcaseSDB): ShowcasePhotoMetadata {
            val link = SQL_DB.planogrammVizitShowcaseDao().getByCodeDad2(dad2)
                .orEmpty().firstOrNull { it != null && it.showcase_id == showcase.id }
            val plan = if (link == null) showcase.planogramId?.let { SQL_DB.planogrammDao().getById(it) } else null
            return resolve(photoType, showcase, link, plan)
        }

        internal fun resolve(
            photoType: String, showcase: ShowcaseSDB,
            link: PlanogrammVizitShowcaseSDB?, plan: PlanogrammSDB?
        ) = ShowcasePhotoMetadata(
            showcase.id?.toString().orEmpty(),
            showcase.photoId?.toString().orEmpty(),
            if (link != null) link.planogram_id?.toString().orEmpty() else showcase.planogramId?.toString().orEmpty(),
            if (link != null) link.planogram_photo_id?.toString().orEmpty() else plan?.photoId?.toString().orEmpty(),
            link == null && (plan != null || photoType == "0")
        )
    }
}
