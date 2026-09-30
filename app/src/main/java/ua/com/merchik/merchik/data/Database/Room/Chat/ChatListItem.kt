package ua.com.merchik.merchik.data.Database.Room.Chat

import androidx.room.Ignore
import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.dataLayer.DataObjectUI

// Room query projection, not a new database table.
data class ChatListItem(
    val id: Int,
    val nm: String,
    val dt: Long,
    val lastUpdate: Long,
    val lastMsg: String,
    val kolRead: Int,
    val kolUnread: Int,
    val kolVsego: Int,
    val readState: Int
) : DataObjectUI {
    @Ignore
    constructor() : this(0, "", 0, 0, "", 0, 0, 0, 0)

    override fun getHidedFieldsOnUI() = "readState,kolRead,kolVsego"

    override fun getPreferredFieldOrder() =
        listOf("nm", "lastMsg", "lastUpdate", "kolRead", "kolUnread", "kolVsego", "id", "dt")

    fun fieldTitle(key: String): String = when (key) {
        "id" -> "ID чату"
        "nm" -> "Назва чату"
        "dt" -> "Дата створення"
        "lastUpdate" -> "Останнє повідомлення"
        "lastMsg" -> "Текст повідомлення"
        "kolRead" -> "Прочитані"
        "kolUnread" -> "Непрочитані"
        "kolVsego" -> "Усього повідомлень"
        "readState" -> "Стан повідомлень"
        else -> key
    }

    override fun getValueUI(key: String, value: Any): String = when (key) {
        "dt", "lastUpdate" -> value.toString().toLongOrNull()?.takeIf { it > 0 }
            ?.let { Clock.getHumanTimeSecPattern(it, "HH:mm dd.MM.yyyy") }.orEmpty()
        else -> value.toString()
    }
}
