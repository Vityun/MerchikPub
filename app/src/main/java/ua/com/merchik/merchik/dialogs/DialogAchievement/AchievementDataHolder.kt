package ua.com.merchik.merchik.dialogs.DialogAchievement

class AchievementDataHolder private constructor() {
    var tovarId: Int? = null
    var tovarName: String? = null
    var requirementClientId: Int? = null
    var requirementClientName: String? = null
    var manufactureId: Int? = null
    var manufactureName: String? = null
    var themeId: Int? = null
    var themeName: String? = null
    var photoToId: Int? = null
    var photoToURI: String? = null
    var photoHashTo: String? = null
    var photoAfterId: Int? = null
    var photoAfterURI: String? = null
    var photoHashAfter: String? = null


    companion object {
        private var instance: AchievementDataHolder? = null
        fun instance(): AchievementDataHolder {
            if (instance == null) {
                instance = AchievementDataHolder()
            }
            return instance!!
        }
    }

    fun init() {
        tovarId = null
        tovarName = null
        requirementClientId = null
        requirementClientName = null
        manufactureId = null
        manufactureName = null
        themeId = null
        themeName = null
        photoToId = null
        photoToURI = null
        photoHashTo = null
        photoAfterId = null
        photoAfterURI = null
        photoHashAfter = null
    }
}