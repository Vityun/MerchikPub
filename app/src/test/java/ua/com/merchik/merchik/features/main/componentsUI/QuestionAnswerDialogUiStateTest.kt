package ua.com.merchik.merchik.features.main.componentsUI

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionAnswerDialogUiStateTest {
    @Test fun oneTimeSelectionIsKeptWhenFormFieldsChange() {
        val state = QuestionAnswerDialogUiState(isOneTimePaymentIncrease = true)

        assertTrue(state.copy(wantReceivePlan = "150", comment = "comment").isOneTimePaymentIncrease)
    }

    @Test fun permanentSelectionIsKeptWhenFormFieldsChange() {
        val state = QuestionAnswerDialogUiState(isOneTimePaymentIncrease = false)

        assertFalse(state.copy(wantReceivePlan = "150", comment = "comment").isOneTimePaymentIncrease)
    }

    @Test fun previousFormDoesNotChangeNewFormDefault() {
        QuestionAnswerDialogUiState(isOneTimePaymentIncrease = true)

        assertFalse(QuestionAnswerDialogUiState().isOneTimePaymentIncrease)
    }
}
