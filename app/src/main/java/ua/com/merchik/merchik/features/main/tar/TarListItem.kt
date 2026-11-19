package ua.com.merchik.merchik.features.main.tar

import ua.com.merchik.merchik.Clock
import ua.com.merchik.merchik.R
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB
import ua.com.merchik.merchik.dataLayer.DataObjectUI
import ua.com.merchik.merchik.database.realm.tables.TasksAndReclamationsRealm

// Presentation only: the Room entity and its upload fields stay unchanged.
data class TarListItem(
    val id: Int = 0,
    val dt: Long = 0,
    val id1c: String = "",
    val state: Int = 0,
    val tp: Int = 0,
    val addr: Int = 0,
    val address: String = "",
    val client: String = "",
    val customer: String = "",
    val responsible: String = "",
    val amount: String = "",
    val comment: String = "",
    val photo: Int = 0,
    val photoHash: String = ""
) : DataObjectUI {
    override fun getHidedFieldsOnUI() = "tp,addr,client,photo,photoHash"
    override fun getIdResImage() = R.mipmap.merchik
    override fun getFieldsImageOnUI() = "photo"
    override fun getPreferredFieldOrder() =
        listOf("dt", "id1c", "id", "state", "address", "customer", "responsible", "amount", "comment")

    fun fieldTitle(key: String): String = when (key) {
        "id" -> "ID"
        "dt" -> "Дата"
        "id1c" -> "Документ"
        "state" -> "Статус"
        "address" -> "Адреса"
        "customer" -> "Клієнт"
        "responsible" -> "Відповідальний"
        "amount" -> "Сума (план/факт)"
        "comment" -> "Коментар"
        "id_res_image" -> "Фото"
        else -> key
    }

    override fun getValueUI(key: String, value: Any): String = when (key) {
        "dt" -> value.toString().toLongOrNull()?.takeIf { it > 0 }
            ?.let { Clock.getHumanTimeDDMMYYYY(it) }.orEmpty()
        "state" -> TasksAndReclamationsRealm.getStatusTxt(tp, state)
        else -> value.toString()
    }

    companion object {
        fun from(row: TasksAndReclamationsSDB) = TarListItem(
            id = row.id ?: 0, dt = row.dt ?: 0, id1c = row.id1c.orEmpty(),
            state = row.state ?: 0, tp = row.tp ?: 0, addr = row.addr ?: 0,
            address = row.addrNm?.takeIf { it.isNotBlank() } ?: row.addr?.toString().orEmpty(),
            client = row.client.orEmpty(),
            customer = row.clientNm?.takeIf { it.isNotBlank() } ?: row.client.orEmpty(),
            responsible = row.sortNm?.takeIf { it.isNotBlank() } ?: row.vinovnik?.toString().orEmpty(),
            amount = amount(row), comment = row.comment.orEmpty(),
            photo = row.photo ?: 0, photoHash = row.photoHash.orEmpty()
        )

        private fun amount(row: TasksAndReclamationsSDB): String {
            val penalty = row.sumPenalty.orEmpty().ifBlank { "0" }
            val premium = row.sumPremiya.orEmpty().ifBlank { "0" }
            return when {
                row.tp == 1 -> "$penalty / ${if (row.state == 0) premium else "0"}"
                row.state == 0 -> "$penalty / -$penalty"
                row.state == 1 -> "$penalty / 0"
                else -> ""
            }
        }
    }
}
