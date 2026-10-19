package ua.com.merchik.merchik.features.main.Main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OneTimePaymentIncreaseLayerTest {
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
