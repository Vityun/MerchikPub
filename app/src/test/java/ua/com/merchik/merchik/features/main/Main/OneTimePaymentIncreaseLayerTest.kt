package ua.com.merchik.merchik.features.main.Main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OneTimePaymentIncreaseLayerTest {
    private val start = 1_791_800_000L
    private val end = start + 33 * 60

    @Test fun unfinishedVisitCannotRequestOneTimePaymentIncrease() {
        assertFalse(canRequestOneTimePaymentIncrease(start, start, 0, 0))
    }

    @Test fun notStartedVisitCannotRequestOneTimePaymentIncrease() {
        assertFalse(canRequestOneTimePaymentIncrease(0, 0, 0, 0))
    }

    @Test fun completedVisitCanRequestOneTimePaymentIncrease() {
        assertTrue(canRequestOneTimePaymentIncrease(start, start, end, end))
    }

    @Test fun legacyClientOnlyCompletionIsRecognized() {
        assertTrue(canRequestOneTimePaymentIncrease(0, start, 0, end))
    }

    @Test fun legacyVisitOnlyCompletionIsRecognized() {
        assertTrue(canRequestOneTimePaymentIncrease(start, 0, end, 0))
    }

    @Test fun endWithoutValidStartIsNotCompletion() {
        assertFalse(canRequestOneTimePaymentIncrease(0, 0, end, end))
        assertFalse(canRequestOneTimePaymentIncrease(start, start, start - 1, start - 1))
    }

    @Test fun legacyMillisecondsAreNormalizedBeforeCheckingCompletion() {
        assertTrue(canRequestOneTimePaymentIncrease(start * 1000, start, end, end * 1000))
    }

    @Test fun invalidTimesDoNotOpenFormOrThrow() {
        assertFalse(canRequestOneTimePaymentIncrease(-1, start, end, end))
        assertFalse(canRequestOneTimePaymentIncrease(start, start, -1, end))
        assertFalse(canRequestOneTimePaymentIncrease(start, start, Long.MAX_VALUE, end))
    }

    @Test fun questionAnswerHasAchievementUnderneath() {
        assertTrue(OneTimePaymentIncreaseLayer.QUESTION_ANSWER.hasAchievementDialog)
        assertEquals(1378, ONE_TIME_PAYMENT_ACHIEVEMENT_THEME)
    }

    @Test fun closingQuestionAnswerLeavesAchievementOpen() {
        val next = OneTimePaymentIncreaseLayer.QUESTION_ANSWER.dismissQuestionAnswer()
        assertEquals(OneTimePaymentIncreaseLayer.CREATE_ACHIEVEMENT, next)
        assertTrue(next.hasAchievementDialog)
    }

    @Test fun repeatedCloseDoesNotCloseUnderlyingAchievement() {
        val next = OneTimePaymentIncreaseLayer.QUESTION_ANSWER
            .dismissQuestionAnswer().dismissQuestionAnswer()
        assertEquals(OneTimePaymentIncreaseLayer.CREATE_ACHIEVEMENT, next)
    }

    @Test fun listDoesNotReopenDialogsAfterRefresh() {
        val next = OneTimePaymentIncreaseLayer.ACHIEVEMENTS.dismissQuestionAnswer()
        assertEquals(OneTimePaymentIncreaseLayer.ACHIEVEMENTS, next)
        assertFalse(next.hasAchievementDialog)
    }

    @Test fun layerCanBeRestoredWithoutReopeningUpperForm() {
        val saved = OneTimePaymentIncreaseLayer.QUESTION_ANSWER.dismissQuestionAnswer().name
        assertEquals(OneTimePaymentIncreaseLayer.CREATE_ACHIEVEMENT,
            OneTimePaymentIncreaseLayer.valueOf(saved))
    }
}
