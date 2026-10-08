package ua.com.merchik.merchik.dialogs.features.dialogMessage

import java.util.Locale

enum class DialogStatus {
    NORMAL, ALERT, ERROR, EMPTY, LOADING, INFO;

    companion object {
        fun fromString(value: String?): DialogStatus = when (value?.lowercase(Locale.ROOT)) {
            "info" -> INFO
            "alert" -> ALERT
            "danger" -> ERROR
            "ok" -> NORMAL
            else -> EMPTY
        }
    }
}
