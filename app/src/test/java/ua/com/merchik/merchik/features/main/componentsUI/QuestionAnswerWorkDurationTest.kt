package ua.com.merchik.merchik.features.main.componentsUI

import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionAnswerWorkDurationTest {
    private val start = 1_791_800_000L

    @Test fun shortAverageForOneTimeClient9382AddsBaseAndLastReportDigit() {
        assertEquals(49.0, average(listOf(visit(20.0, 9403230133L))), 0.000001)
    }

    @Test fun permanentPaymentKeepsOriginalAverage() {
        assertEquals(20.0, average(listOf(visit(20.0)), oneTime = false), 0.000001)
    }

    @Test fun otherClientsKeepOriginalAverage() {
        for (client in listOf("9381", "19382", null, "")) {
            assertEquals(20.0, average(listOf(visit(20.0)), client = client), 0.000001)
        }
    }

    @Test fun averageOfExactlyThirtyDoesNotAdjustEvenShortVisits() {
        assertEquals(30.0, average(listOf(visit(20.0), visit(40.0))), 0.000001)
    }

    @Test fun averageAboveThirtyDoesNotAdjustEvenShortVisits() {
        assertEquals(30.5, average(listOf(visit(20.0), visit(41.0))), 0.000001)
    }

    @Test fun averageIsCheckedBeforeDisplayRounding() {
        assertEquals(58.6, average(listOf(visit(29.6, 123L))), 0.000001)
    }

    @Test fun visitOfExactlyThirtyIsAdjustedWhenOverallAverageIsBelowThirty() {
        assertEquals(54.0, average(listOf(visit(20.0, 123L), visit(30.0, 123L))), 0.000001)
    }

    @Test fun longVisitsRemainUnchangedWhenShortAverageIsAdjusted() {
        // Initial average is 25; only the ten-minute visit receives 26 + 1 minutes.
        assertEquals(38.5, average(listOf(visit(10.0, 121L), visit(40.0, 129L))), 0.000001)
    }

    @Test fun eachVisitUsesItsOwnLastReportDigit() {
        assertEquals(50.5, average(listOf(visit(20.0, 120L), visit(20.0, 129L))), 0.000001)
        assertEquals(46.0, average(listOf(visit(20.0, 0L))), 0.000001)
    }

    @Test fun clientTimesMustBothBePresentAndOrdered() {
        val sample = visit(20.0)
        val invalid = listOf(
            sample.copy(clientStart = 0),
            sample.copy(clientEnd = 0),
            sample.copy(clientStart = -1),
            sample.copy(clientEnd = -1),
            sample.copy(clientEnd = start - 1)
        )
        for (duration in invalid) {
            assertEquals(20.0, average(listOf(duration)), 0.000001)
        }
    }

    @Test fun eligibilityUsesClientIntervalWithoutRoundingItDown() {
        val duration = visit(20.0).copy(clientEnd = start + 30 * 60 + 1)
        assertEquals(20.0, average(listOf(duration)), 0.000001)
    }

    @Test fun legacyMillisecondClientTimesAreNormalized() {
        val duration = visit(20.0, 123L)
        assertEquals(
            49.0,
            average(listOf(duration.copy(
                clientStart = duration.clientStart * 1000,
                clientEnd = duration.clientEnd * 1000
            ))),
            0.000001
        )
    }

    @Test fun emptyHistoryKeepsZeroAverage() {
        assertEquals(0.0, average(emptyList()), 0.000001)
    }

    @Test fun repeatedCalculationDoesNotAccumulateAdjustmentOrModifySamples() {
        val samples = listOf(visit(20.0, 123L))
        val original = samples.single().copy()
        assertEquals(49.0, average(samples), 0.000001)
        assertEquals(49.0, average(samples), 0.000001)
        assertEquals(original, samples.single())
        assertEquals(20.0, average(samples, oneTime = false), 0.000001)
    }

    private fun visit(minutes: Double, reportId: Long = 123L) =
        QuestionAnswerWorkDuration(minutes, start, start + (minutes * 60).toLong(), reportId)

    private fun average(
        durations: List<QuestionAnswerWorkDuration>,
        client: String? = "9382",
        oneTime: Boolean = true
    ) = questionAnswerAverageWorkDuration(durations, client, oneTime)
}
