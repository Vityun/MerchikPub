package ua.com.merchik.merchik.features.main.Main

import org.junit.Assert.assertEquals
import org.junit.Test

class InlineInputValueTest {
    @Test
    fun unchangedInputAcceptsFreshDatabaseValue() {
        assertEquals("20", refreshedInlineInputValue("10", "10", "20"))
        assertEquals("0", refreshedInlineInputValue("20", "20", "0"))
    }

    @Test
    fun partialDecimalSurvivesAnExternalPriceUpdate() {
        assertEquals("10.", refreshedInlineInputValue("10", "10.", "20"))
        assertEquals("", refreshedInlineInputValue("10", "", "20"))
    }

    @Test
    fun rejectedCommentIsNotReplacedByARefresh() {
        assertEquals("draft", refreshedInlineInputValue("old comment", "draft", "new comment"))
    }

    @Test
    fun savedInputCanReceiveTheNextExternalUpdate() {
        val saved = refreshedInlineInputValue("10", "15", "15")
        assertEquals("15", saved)
        assertEquals("20", refreshedInlineInputValue("15", saved, "20"))
    }

    @Test
    fun normalizedSavedNumberIsNotMistakenForAnUnsavedDraft() {
        val saved = refreshedInlineInputValue("0", "02", "2", numeric = true)
        assertEquals("2", saved)
        assertEquals("3", refreshedInlineInputValue("2", saved, "3", numeric = true))
        assertEquals("1.5", refreshedInlineInputValue("0", "1.50", "1.5", numeric = true))
    }

    @Test
    fun numericAcknowledgementDoesNotDiscardAnUnfinishedDecimal() {
        assertEquals("10.", refreshedInlineInputValue("0", "10.", "10", numeric = true))
    }
}
