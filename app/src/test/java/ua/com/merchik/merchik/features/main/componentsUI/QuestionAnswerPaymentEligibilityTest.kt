package ua.com.merchik.merchik.features.main.componentsUI

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionAnswerPaymentEligibilityTest {
    @Test fun shorterOneTimeVisitBlocksSavingAndAchievementEntry() {
        assertTrue(snapshot(33.0, 40.0).isDurationBelowAverage)
    }

    @Test fun equalDurationIsAllowed() {
        assertFalse(snapshot(40.0, 40.0).isDurationBelowAverage)
    }

    @Test fun longerDurationIsAllowed() {
        assertFalse(snapshot(41.0, 40.0).isDurationBelowAverage)
    }

    @Test fun permanentPaymentIsNotBlockedByDurationComparison() {
        assertFalse(snapshot(20.0, 40.0, oneTime = false).isDurationBelowAverage)
    }

    @Test fun comparisonUsesPreciseValuesEvenWhenDisplayValuesAreEqual() {
        val data = snapshot(30.1, 30.4).copy(
            calculatedData = QuestionAnswerCalculatedData(
                avgWorkDurationFact = "30", avgWorkDurationMinutes = 30.4
            ),
            currentVisit = QuestionAnswerVisitDetails(
                durationMinutes = "30", preciseDurationMinutes = 30.1
            )
        )
        assertEquals(data.calculatedData.avgWorkDurationFact, data.currentVisit.durationMinutes)
        assertTrue(data.isDurationBelowAverage)
    }

    @Test fun comparisonUsesAdjustedAverageRatherThanOriginalHistory() {
        val start = 1_791_800_000L
        val adjustedAverage = questionAnswerAverageWorkDuration(
            listOf(QuestionAnswerWorkDuration(20.0, start, start + 20 * 60, 123L)),
            "9382",
            true
        )
        assertFalse(snapshot(33.0, 20.0).isDurationBelowAverage)
        assertTrue(snapshot(33.0, adjustedAverage).isDurationBelowAverage)
    }

    @Test fun emptyHistoryDoesNotInventAnAverage() {
        assertFalse(snapshot(33.0, 0.0).isDurationBelowAverage)
    }

    @Test fun unknownOrInvalidDurationDoesNotInventAShortVisit() {
        assertFalse(snapshot(null, 40.0).isDurationBelowAverage)
        assertFalse(snapshot(-1.0, 40.0).isDurationBelowAverage)
        assertFalse(snapshot(Double.NaN, 40.0).isDurationBelowAverage)
        assertFalse(snapshot(Double.POSITIVE_INFINITY, 40.0).isDurationBelowAverage)
        assertFalse(snapshot(33.0, Double.NaN).isDurationBelowAverage)
        assertFalse(snapshot(33.0, Double.POSITIVE_INFINITY).isDurationBelowAverage)
    }

    @Test fun currentVisitRetainsSecondsForComparisonAndWholeMinutesForDisplay() {
        val start = 1_791_800_000L
        val details = questionAnswerVisitDetails(
            null, start, start, start + 33 * 60 + 20, start + 33 * 60 + 20, start + 10_000
        )
        assertEquals("33", details.durationMinutes)
        assertEquals(33.0 + 20.0 / 60.0, details.preciseDurationMinutes!!, 0.000001)
    }

    @Test fun repeatedCheckKeepsTheSameSnapshotAndDoesNotEnableSaving() {
        val data = snapshot(33.0, 40.0)
        repeat(3) {
            assertTrue(data.isDurationBelowAverage)
            assertEquals(40.0, data.calculatedData.avgWorkDurationMinutes, 0.000001)
        }
    }

    private fun snapshot(current: Double?, average: Double, oneTime: Boolean = true) =
        QuestionAnswerPaymentSnapshot(
            calculatedData = QuestionAnswerCalculatedData(avgWorkDurationMinutes = average),
            currentVisit = QuestionAnswerVisitDetails(preciseDurationMinutes = current),
            isOneTimePaymentIncrease = oneTime
        )
}
