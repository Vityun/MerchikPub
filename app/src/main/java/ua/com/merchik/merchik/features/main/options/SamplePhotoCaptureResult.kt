package ua.com.merchik.merchik.features.main.options

import ua.com.merchik.merchik.Global.OptionUnlockPolicy

/** One-shot handoff for photo windows also opened without startActivityForResult. */
internal object SamplePhotoCaptureResult {
    enum class Kind { CAPTURE, CORRECTION }
    data class Result(val visitId: Long, val optionRowId: String, val kind: Kind = Kind.CAPTURE) {
        fun shouldShowFeedback(signal: String?): Boolean = kind == Kind.CAPTURE || signal == "2"
    }

    fun hasOutstandingRemarks(signal: String?): Boolean =
        OptionUnlockPolicy.hasRemark(signal)

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
