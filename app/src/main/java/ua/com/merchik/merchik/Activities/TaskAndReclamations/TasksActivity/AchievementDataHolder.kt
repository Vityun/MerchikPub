package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity

import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB
import ua.com.merchik.merchik.dialogs.DialogAchievement.FilteringDialogDataHolder
import ua.com.merchik.merchik.features.main.DBViewModels.ImagesTypeListDBViewModel
import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter
import ua.com.merchik.merchik.dataLayer.ModeUI

class TarPhotoDataHolder private constructor() {

    var photoToId: Int? = null

    fun preparePhotoTypeSelection() {
        val filter = ItemFilter(
            title = "Тип фото",
            clazz = ImagesTypeListDB::class,
            clazzViewModel = ImagesTypeListDBViewModel::class,
            modeUI = ModeUI.ONE_SELECT,
            titleContext = "Типы фото",
            subTitleContext = "Оберіть тип фото для коментаря",
            leftField = "id",
            rightField = "id",
            rightValuesRaw = emptyList(),
            rightValuesUI = emptyList(),
            enabled = false
        )
        FilteringDialogDataHolder.instance().filters = Filters(
            title = "Типы фото",
            subTitle = "Укажите тип фото которое Вы собираетесь сделать",
            items = listOf(filter)
        )
    }

    fun consumeSelectedPhotoTypeId(): Int? {
        val selectedId = FilteringDialogDataHolder.instance().filters
            ?.items
            ?.firstOrNull()
            ?.rightValuesRaw
            ?.firstOrNull()
            ?.toIntOrNull()
        FilteringDialogDataHolder.instance().filters = null
        return selectedId
    }

    companion object {
        private var instance: TarPhotoDataHolder? = null
        fun instance(): TarPhotoDataHolder {
            if (instance == null) {
                instance = TarPhotoDataHolder()
            }
            return instance!!
        }
    }

    fun init() {
        photoToId = null
    }
}
