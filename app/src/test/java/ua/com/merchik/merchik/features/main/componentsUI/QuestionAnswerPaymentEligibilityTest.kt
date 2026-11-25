package ua.com.merchik.merchik.features.main.componentsUI

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionAnswerPaymentEligibilityTest {
    @Test fun warningShowsCurrentAndAverageMinutes() {
        assertEquals(
            "Нельзя запрашивать РАЗОВОЕ повышение оплаты, если продолжительность работы (33 мин.) " +
                    "по текущему визиту меньше среднего времени, которое вы обычно тратите на визит в этой ТТ (40 мин.).",
            oneTimeShortDurationMessage(33.0, 40.0, planned = false)
        )
    }

    @Test fun plannedWarningNamesThePlanAndKeepsCloseDurationsDistinct() {
        val message = oneTimeShortDurationMessage(30.0, 30.002, planned = true)
        assertTrue(message.contains("работы (30 мин.)"))
        assertTrue(message.endsWith("меньше планового времени этого визита (30,002 мин.)."))
    }

    @Test fun oneTimeCommentNeedsTenLettersInTheExecutorsOwnText() {
        assertFalse(hasEnoughOneTimeCommentLetters(""))
        assertFalse(hasEnoughOneTimeCommentLetters("123 !? 🙂"))
        assertFalse(hasEnoughOneTimeCommentLetters("АБВГҐ ДЕЄЖ'’ʼ 45!"))
        assertTrue(hasEnoughOneTimeCommentLetters("АБВГҐ ДЕЄЖ'’ʼ 45!ї"))
        assertTrue(hasEnoughOneTimeCommentLetters("abcdefghij"))
    }

    @Test fun supplementaryLettersCountOnceAndEmojiDoNotCount() {
        val letter = String(Character.toChars(0x10400))
        assertFalse(hasEnoughOneTimeCommentLetters(letter.repeat(9) + "🙂"))
        assertTrue(hasEnoughOneTimeCommentLetters(letter.repeat(10)))
    }

    @Test fun shorterOneTimeVisitBlocksSavingAndAchievementEntry() {
        assertTrue(snapshot(33.0, 40.0).isDurationBelowAverage)
    }

    @Test fun validatedAutomaticOfferDoesNotApplyManualAverageCheck() {
        assertFalse(snapshot(40.0, 50.0).copy(isServerDuration = true).isDurationBelowAverage)
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
