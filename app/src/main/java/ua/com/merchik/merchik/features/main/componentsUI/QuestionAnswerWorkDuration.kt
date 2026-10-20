package ua.com.merchik.merchik.features.main.componentsUI

import ua.com.merchik.merchik.Utils.WorkTimeRules
import kotlin.math.abs

internal data class QuestionAnswerWorkDuration(
    val minutes: Double,
    val clientStart: Long,
    val clientEnd: Long,
    val reportId: Long
)

internal fun questionAnswerAverageWorkDuration(
    durations: List<QuestionAnswerWorkDuration>,
    clientId: String?,
    isOneTimePaymentIncrease: Boolean
): Double {
    if (durations.isEmpty()) return 0.0

    val average = durations.map { it.minutes }.average()
    if (!isOneTimePaymentIncrease || clientId != "9382" || average >= 30.0) {
        return average
    }

    // Test the original average once; adjust only eligible visits, without changing their times.
    return durations.map { visit ->
        val clientMinutes = runCatching {
            val start = WorkTimeRules.seconds(visit.clientStart)
            val end = WorkTimeRules.seconds(visit.clientEnd)
            if (start <= 0L || end <= 0L || end < start) null else (end - start) / 60.0
        }.getOrNull()

        if (clientMinutes != null && clientMinutes <= 30.0) {
            visit.minutes + 16 + abs(visit.reportId % 10)
        } else {
            visit.minutes
        }
    }.average()
}
