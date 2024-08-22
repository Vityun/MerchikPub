package ua.com.merchik.merchik.dialogs.DialogAchievement

import ua.com.merchik.merchik.features.main.Main.Filters
import ua.com.merchik.merchik.features.main.Main.ItemFilter

class FilteringDialogDataHolder private constructor() {
    var filters: Filters? = null

    companion object {
        private var instance: FilteringDialogDataHolder? = null
        fun instance(): FilteringDialogDataHolder {
            if (instance == null) {
                instance = FilteringDialogDataHolder()
            }
            return instance!!
        }
    }

    fun init() {
        filters = null
    }
}