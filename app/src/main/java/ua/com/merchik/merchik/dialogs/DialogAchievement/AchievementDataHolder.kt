package ua.com.merchik.merchik.dialogs.DialogAchievement

class AchievementDataHolder private constructor() {
    var tovarId: Int = 0
    var tovarName: String = ""

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
        tovarId = 0
        tovarName = ""
    }
}