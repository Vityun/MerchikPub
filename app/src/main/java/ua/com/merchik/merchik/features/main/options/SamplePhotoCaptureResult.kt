package ua.com.merchik.merchik.features.main.options

/** One-shot handoff for photo windows also opened without startActivityForResult. */
internal object SamplePhotoCaptureResult {
    enum class Kind { CAPTURE, CORRECTION }
    data class Result(val visitId: Long, val optionRowId: String, val kind: Kind = Kind.CAPTURE)

    // Accessed on the main thread; retain IDs only, never an Activity or a Realm object.
    private var pending: Result? = null

    fun publish(visitId: Long, optionRowId: String) {
        if (visitId > 0 && optionRowId.isNotBlank()) {
            pending = Result(visitId, optionRowId)
        }
    }

    fun publishCorrection(visitId: Long, optionRowId: String) {
        if (visitId > 0 && optionRowId.isNotBlank()) {
            pending = Result(visitId, optionRowId, Kind.CORRECTION)
        }
    }

    fun takeForVisit(visitId: Long): Result? {
        val result = pending?.takeIf { it.visitId == visitId } ?: return null
        pending = null
        return result
    }
}
