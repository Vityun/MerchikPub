package ua.com.merchik.merchik.features.main.DBViewModels

import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB

// Immutable values: detached ReportPrepareDB instances can still be edited by save callbacks.
internal data class TovarReportPrepareSnapshot(
    val id: Long?,
    val price: String?,
    val priceMin: String?,
    val priceMax: String?,
    val face: String?,
    val amount: Int,
    val up: String?,
    val dtExpire: String?,
    val expireLeft: String?,
    val notes: String?,
    val oborotvedNum: String?,
    val oborotvedDate: String?,
    val errorId: String?,
    val errorComment: String?,
    val akciyaId: String?,
    val akciya: String?,
    val uploadStatus: Int
) {
    companion object {
        fun from(row: ReportPrepareDB) = TovarReportPrepareSnapshot(
            id = row.getID(),
            price = row.getPrice(),
            priceMin = row.getPriceMin(),
            priceMax = row.getPriceMax(),
            face = row.getFace(),
            amount = row.getAmount(),
            up = row.getUp(),
            dtExpire = row.getDtExpire(),
            expireLeft = row.getExpireLeft(),
            notes = row.getNotes(),
            oborotvedNum = row.getOborotvedNum(),
            oborotvedDate = row.oborotved_num_date,
            errorId = row.getErrorId(),
            errorComment = row.getErrorComment(),
            akciyaId = row.getAkciyaId(),
            akciya = row.getAkciya(),
            uploadStatus = row.getUploadStatus()
        )
    }
}

internal fun indexReportPrepareByTovar(rows: List<ReportPrepareDB>): Map<String, ReportPrepareDB> {
    val result = linkedMapOf<String, ReportPrepareDB>()
    rows.forEach { row ->
        val id = row.getTovarId()?.takeIf { it.isNotBlank() } ?: return@forEach
        // Match the existing findFirst() lookup if duplicate product rows exist.
        if (id !in result) result[id] = row
    }
    return result
}

internal fun changedReportPrepareTovarIds(
    previous: Map<String, TovarReportPrepareSnapshot>,
    current: Map<String, TovarReportPrepareSnapshot>
): Set<String> = (previous.keys + current.keys).filterTo(linkedSetOf()) {
    previous[it] != current[it]
}
