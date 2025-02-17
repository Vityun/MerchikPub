package ua.com.merchik.merchik.Activities.DetailedReportActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ua.com.merchik.merchik.dialogs.DialogAchievement.AchievementDataHolder

class OpinionDataHolder private constructor() {
    var opinionID: Int? by mutableStateOf(null)
    var opinionName:  String? by mutableStateOf(null)
    var comment: String by mutableStateOf("")


    companion object {
        private var instance: OpinionDataHolder? = null
        fun instance(): OpinionDataHolder {
            if (instance == null) {
                instance = OpinionDataHolder()
            }
            return instance!!
        }
    }

    fun init() {
        opinionID = null
        opinionName = null
        comment = ""

    }
}