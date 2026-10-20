package ua.com.merchik.merchik.features.main.componentsUI

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.util.Date
import java.util.TimeZone

class QuestionAnswerPaymentTextTest {
    private val start = Instant.parse("2026-10-19T12:00:00Z").epochSecond
    private val visitDate = Date.from(Instant.parse("2026-10-18T21:00:00Z"))

    @Test fun oneTimeTemplateDoesNotRequestMissingTranslation() {
        val template = questionAnswerStatementTemplate(true) { _, _ ->
            error("One-time text has no translation ID")
        }
        assertTrue(template.contains("{DURATION_FACT}"))
        assertTrue(template.contains("{UP_FACT}"))
        assertTrue(template.contains("{VISIT_DATE}"))
        assertTrue(template.contains("{CURRENT_VISIT_DURATION}"))
        assertTrue(template.contains("{DESIRED_CASH}"))
        assertFalse(template.contains("{VISITS_PER_WEEK}"))
        assertFalse(template.contains("{ВРЕМЯ"))
        assertFalse(template.contains("{ДОЛЖЕН"))
    }

    @Test fun permanentTemplateKeepsExistingTranslationIdAndFallback() {
        var calls = 0
        val template = questionAnswerStatementTemplate(false) { fallback, id ->
            calls++
            assertEquals(100500L, id)
            assertTrue(fallback.startsWith("Я, {USER},"))
            assertTrue(fallback.contains("{CASH_FACT}"))
            assertTrue(fallback.contains("{CASH_PENALTY_FACT}"))
            assertTrue(fallback.contains("{VISITS_PER_WEEK_FACT}"))
            assertTrue(fallback.contains("{VISITS_PER_WEEK}"))
            assertFalse(fallback.contains("{CURRENT_VISIT_DURATION}"))
            "translated statement"
        }
        assertEquals(1, calls)
        assertEquals("translated statement", template)
    }

    @Test fun switchingModesRestoresOriginalCommentPlaceholder() {
        val original = "Тут додайте усі фактори, котрі суттєво впливають на тривалість виконання робіт. " +
                "Складнощі з: (планограмами, персоналом, складом, фасуванням, і т.д.) та вкажіть свої аргументи щодо збільшення кількості візитів, або вартості."
        assertEquals(original, questionAnswerCommentPlaceholder(false))
        assertTrue(questionAnswerCommentPlaceholder(true).contains("ПОТОЧНОГО"))
        assertTrue(questionAnswerCommentPlaceholder(true).contains("РАЗОВОГО бонусу"))
        assertEquals(original, questionAnswerCommentPlaceholder(false))
    }

    @Test fun oneTimeAmountDoesNotRequireInvisibleWeeklyVisitsField() {
        assertFalse(questionAnswerHasEmptyRequiredFields(true, "150", ""))
        assertFalse(questionAnswerHasEmptyRequiredFields(true, "150", " "))
        assertTrue(questionAnswerHasEmptyRequiredFields(true, "", "3"))
        assertTrue(questionAnswerHasEmptyRequiredFields(true, " ", ""))
    }

    @Test fun permanentIncreaseStillRequiresBothFields() {
        assertTrue(questionAnswerHasEmptyRequiredFields(false, "150", ""))
        assertTrue(questionAnswerHasEmptyRequiredFields(false, "", "3"))
        assertFalse(questionAnswerHasEmptyRequiredFields(false, "150", "3"))
    }

    @Test fun oneTimeSavedTextContainsCalculatedValuesAndEnteredBonus() {
        val template = questionAnswerStatementTemplate(true) { text, _ -> text }
        val result = savedText(template)
        assertTrue(result.contains("40 хвилин"))
        assertTrue(result.contains("12 одиниць товару"))
        assertTrue(result.contains("за 19.10.2026"))
        assertTrue(result.contains("33 хв."))
        assertTrue(result.contains("150.50 грн."))
        assertTrue(result.endsWith("\n\nAdditional work"))
        assertFalse(result.contains("{"))
        assertFalse(result.contains("грн/вiзит"))
        assertFalse(result.contains("тиждень"))
    }

    @Test fun permanentSavedTextRetainsAllOriginalCalculations() {
        val template = questionAnswerStatementTemplate(false) { text, _ -> text }
        val result = savedText(template)
        assertTrue(result.startsWith("Я, Test user,"))
        assertTrue(result.contains("40 хвилин"))
        assertTrue(result.contains("12 одиниць товару"))
        assertTrue(result.contains("100 грн/вiзит"))
        assertTrue(result.contains("5 грн/вiзит"))
        assertTrue(result.contains("150.50 грн/вiзит"))
        assertTrue(result.contains("2 вiзитiв на тиждень"))
        assertTrue(result.contains("3 вiзитiв в тиждень"))
        assertFalse(result.contains("19.10.2026"))
        assertFalse(result.contains("{"))
    }

    @Test fun oneTimeSubmissionTransfersFullStatementWithoutAccessingComplaintJournal() {
        val template = questionAnswerStatementTemplate(true) { text, _ -> text }
        var receivedText: String? = null
        var transferCount = 0
        submitQuestionAnswer(
            isOneTimePaymentIncrease = true,
            prepareStatement = { savedText(template) },
            onOneTimeSave = { text ->
                transferCount++
                receivedText = text
            },
            onPermanentSave = { error("One-time requests must not check or save complaints") }
        )
        assertEquals(1, transferCount)
        assertEquals(savedText(template), receivedText)
        assertTrue(receivedText!!.endsWith("\n\nAdditional work"))
    }

    @Test fun permanentSubmissionKeepsComplaintFlowWithoutAchievementTransfer() {
        var complaintSaveCount = 0
        submitQuestionAnswer(
            isOneTimePaymentIncrease = false,
            prepareStatement = { error("The permanent flow prepares its own journal entry") },
            onOneTimeSave = { error("Permanent requests must not fill an achievement") },
            onPermanentSave = { complaintSaveCount++ }
        )
        assertEquals(1, complaintSaveCount)
    }

    @Test fun failedOneTimeTransferDoesNotFallBackToComplaintSave() {
        val failure = IllegalStateException("Achievement dialog unavailable")
        try {
            submitQuestionAnswer(
                isOneTimePaymentIncrease = true,
                prepareStatement = { "Prepared statement" },
                onOneTimeSave = { throw failure },
                onPermanentSave = { error("Failed transfer must not create a complaint") }
            )
            fail("The caller must receive the transfer error")
        } catch (error: IllegalStateException) {
            assertSame(failure, error)
        }
    }

    @Test fun emptyCommentDoesNotAppendBlankParagraph() {
        assertFalse(savedText("Statement {DESIRED_CASH}", comment = " ").endsWith("\n\n "))
        assertEquals("Statement 150.50", savedText("Statement {DESIRED_CASH}", comment = ""))
    }

    @Test fun completedVisitUsesRecordedEndNotCurrentTime() {
        assertEquals("33", details(end = start + 33 * 60).durationMinutes)
        assertEquals("19.10.2026", details(end = start + 33 * 60).date)
    }

    @Test fun unfinishedVisitUsesOpeningTime() {
        assertEquals("33", details(now = start + 33 * 60).durationMinutes)
    }

    @Test fun millisecondsAreNormalizedBeforeDurationCalculation() {
        val result = questionAnswerVisitDetails(
            visitDate, start * 1000, start * 1000,
            (start + 33 * 60) * 1000, (start + 33 * 60) * 1000, start + 10_000
        )
        assertEquals("33", result.durationMinutes)
    }

    @Test fun legacyClientOnlyTimesAreSupported() {
        val result = questionAnswerVisitDetails(visitDate, 0, start, 0, start + 33 * 60, start + 10_000)
        assertEquals("33", result.durationMinutes)
    }

    @Test fun missingStartDoesNotInventDuration() {
        val result = questionAnswerVisitDetails(visitDate, 0, 0, 0, 0, start + 33 * 60)
        assertEquals("не визначено", result.durationMinutes)
    }

    @Test fun endBeforeStartDoesNotInventDuration() {
        assertEquals("не визначено", details(end = start - 1).durationMinutes)
        assertEquals("не визначено", details(now = start - 1).durationMinutes)
    }

    @Test fun currentDurationUsesSameWholeMinuteRoundingAsAverages() {
        assertEquals("33", details(end = start + 33 * 60 + 29).durationMinutes)
        assertEquals("34", details(end = start + 33 * 60 + 30).durationMinutes)
    }

    @Test fun visitDateUsesKyivRegardlessOfDeviceTimezone() {
        val previous = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"))
            assertEquals("19.10.2026", details().date)
        } finally {
            TimeZone.setDefault(previous)
        }
    }

    @Test fun missingVisitDateIsNotReplacedByToday() {
        val result = questionAnswerVisitDetails(null, start, start, 0, 0, start + 33 * 60)
        assertEquals("не вказано", result.date)
    }

    private fun details(end: Long = 0, now: Long = start + 10_000) =
        questionAnswerVisitDetails(visitDate, start, start, end, end, now)

    private fun savedText(template: String, comment: String = "Additional work") = buildQuestionAnswerSavedText(
        template = template,
        userName = "Test user",
        durationFact = "40",
        upFact = "12",
        cashFact = "100",
        cashPenaltyFact = "5",
        visitsPerWeekFact = "2",
        desiredCash = "150.50",
        desiredVisitsPerWeek = "3",
        comment = comment,
        currentVisit = QuestionAnswerVisitDetails("19.10.2026", "33")
    )
}
