package ua.com.merchik.merchik.features.main.DBViewModels

import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB
import ua.com.merchik.merchik.data.RealmModels.WpDataDB
import ua.com.merchik.merchik.MakePhoto.PhotoReferenceContext

internal object ShowcasePhotoRows {
    fun build(
        showcases: List<ShowcaseSDB>,
        photos: List<StackPhotoDB>,
        visit: WpDataDB
    ): List<StackPhotoDB> = build(showcases, photos, PhotoReferenceContext.fromVisit(visit))

    fun build(
        showcases: List<ShowcaseSDB>,
        photos: List<StackPhotoDB>,
        context: PhotoReferenceContext
    ): List<StackPhotoDB> {
        // Keep the same first photo row as DialogShowcase.findFirst().
        val photosById = photos.distinctBy { it.photoServerId }.associateBy { it.photoServerId }
        return showcases.distinctBy { it.id }.map { showcase ->
            val showcaseId = showcase.id
            val photoId = showcase.photoId?.takeIf { it > 0 }?.toString().orEmpty()
            val sample = photosById[photoId]

            // UI identity belongs to the showcase, never to its optional/shared sample photo.
            StackPhotoDB().apply {
                id = -showcaseId
                this.showcaseId = showcaseId
                showcase_id = showcaseId.toString()
                showcaseName = showcase.nm?.takeIf { it.isNotBlank() }
                    ?: showcase.selectName.orEmpty()
                statusShowcase = showcase.status ?: 0
                mainOption = showcase.mainOptionId ?: 0
                photoServerId = photoId
                photo_num = sample?.photo_num
                photo_size = sample?.photo_size
                photo_hash = sample?.photo_hash.orEmpty()
                photoServerURL = sample?.photoServerURL?.takeIf { it.isNotBlank() }
                    ?: showcase.photoBig?.takeIf { it.isNotBlank() } ?: showcase.photo
                dt = sample?.dt
                addr_id = context.addressId
                client_id = context.clientId
                code_dad2 = context.codeDad2
                setCode_iza(context.codeIza)
            }
        }
    }

    fun markCompleted(rows: List<StackPhotoDB>, completedPhotos: List<StackPhotoDB>) {
        val completedIds = completedPhotos.mapNotNull {
            it.showcase_id?.toIntOrNull()?.takeIf { id -> id > 0 }
        }.toSet()
        rows.forEach { it.specialCol = if (it.showcaseId in completedIds) 1 else 2 }
    }
}
