package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import org.junit.Test;

import static org.junit.Assert.*;

public class MarketingResearchSurveyTest {
    @Test public void onlyMarketingResearchThemeIsSupported() {
        assertTrue(MarketingResearchSurvey.supportsTheme(1182));
        assertFalse(MarketingResearchSurvey.supportsTheme(998));
        assertFalse(MarketingResearchSurvey.supportsTheme(0));
        assertFalse(MarketingResearchSurvey.supportsTheme(1132));
    }

    @Test public void startsWithFirstQuestionAndNoInventedAnswers() {
        MarketingResearchSurvey survey = new MarketingResearchSurvey();
        assertEquals(MarketingResearchSurvey.Question.REQUIRED, survey.nextQuestion());
        assertNull(survey.getMerchandisingRequired());
        assertNull(survey.getWillMerchandise());
    }

    @Test public void firstNoFinishesWithoutAnsweringSecondQuestion() {
        MarketingResearchSurvey survey = new MarketingResearchSurvey();
        survey.answer(MarketingResearchSurvey.Question.REQUIRED, false);
        assertEquals(Boolean.FALSE, survey.getMerchandisingRequired());
        assertNull(survey.getWillMerchandise());
        assertEquals(MarketingResearchSurvey.Question.NONE, survey.nextQuestion());
    }

    @Test public void firstYesOpensSecondQuestion() {
        MarketingResearchSurvey survey = new MarketingResearchSurvey();
        survey.answer(MarketingResearchSurvey.Question.REQUIRED, true);
        assertEquals(Boolean.TRUE, survey.getMerchandisingRequired());
        assertNull(survey.getWillMerchandise());
        assertEquals(MarketingResearchSurvey.Question.INTENTION, survey.nextQuestion());
    }

    @Test public void secondNoFinishesWithoutWarning() {
        MarketingResearchSurvey survey = new MarketingResearchSurvey();
        survey.answer(MarketingResearchSurvey.Question.REQUIRED, true);
        survey.answer(MarketingResearchSurvey.Question.INTENTION, false);
        assertEquals(Boolean.FALSE, survey.getWillMerchandise());
        assertEquals(MarketingResearchSurvey.Question.NONE, survey.nextQuestion());
    }

    @Test public void twoYesAnswersShowWarningThenRestartWithEmptyAnswers() {
        MarketingResearchSurvey survey = new MarketingResearchSurvey();
        survey.answer(MarketingResearchSurvey.Question.REQUIRED, true);
        survey.answer(MarketingResearchSurvey.Question.INTENTION, true);
        assertEquals(MarketingResearchSurvey.Question.WARNING, survey.nextQuestion());
        survey.answer(MarketingResearchSurvey.Question.WARNING, false);
        assertEquals(Boolean.TRUE, survey.getWillMerchandise());
        assertEquals(MarketingResearchSurvey.Question.WARNING, survey.nextQuestion());
        survey.acknowledgeWarning();
        assertEquals(MarketingResearchSurvey.Question.REQUIRED, survey.nextQuestion());
        assertNull(survey.getMerchandisingRequired());
        assertNull(survey.getWillMerchandise());
    }

    @Test public void prematureAcknowledgementDoesNotSuppressWarning() {
        MarketingResearchSurvey survey = new MarketingResearchSurvey();
        survey.acknowledgeWarning();
        survey.answer(MarketingResearchSurvey.Question.REQUIRED, true);
        survey.acknowledgeWarning();
        survey.answer(MarketingResearchSurvey.Question.INTENTION, true);
        assertEquals(MarketingResearchSurvey.Question.WARNING, survey.nextQuestion());
    }

    @Test public void warningRestartsEveryTimeBothAnswersAreYes() {
        MarketingResearchSurvey survey = new MarketingResearchSurvey();
        for (int i = 0; i < 3; i++) {
            survey.answer(MarketingResearchSurvey.Question.REQUIRED, true);
            survey.answer(MarketingResearchSurvey.Question.INTENTION, true);
            assertEquals(MarketingResearchSurvey.Question.WARNING, survey.nextQuestion());
            survey.acknowledgeWarning();
            assertEquals(MarketingResearchSurvey.Question.REQUIRED, survey.nextQuestion());
            assertNull(survey.getMerchandisingRequired());
            assertNull(survey.getWillMerchandise());
        }
    }

    @Test public void viewModelRetainsPendingWarningAndRestartAfterOk() {
        DetailedReportViewModel model = new DetailedReportViewModel();
        long dad2 = 1041126039410061655L;
        model.getMarketingResearchSurvey(dad2).answer(MarketingResearchSurvey.Question.REQUIRED, true);
        model.getMarketingResearchSurvey(dad2).answer(MarketingResearchSurvey.Question.INTENTION, true);
        assertEquals(MarketingResearchSurvey.Question.WARNING,
                model.getMarketingResearchSurvey(dad2).nextQuestion());
        model.getMarketingResearchSurvey(dad2).acknowledgeWarning();
        assertEquals(MarketingResearchSurvey.Question.REQUIRED,
                model.getMarketingResearchSurvey(dad2).nextQuestion());
    }

    @Test public void firstQuestionRemainsPendingWithoutAnswer() {
        MarketingResearchSurvey survey = new MarketingResearchSurvey();
        assertNull(survey.getMerchandisingRequired());
        assertNull(survey.getWillMerchandise());
        assertEquals(MarketingResearchSurvey.Question.REQUIRED, survey.nextQuestion());
        assertEquals(MarketingResearchSurvey.Question.REQUIRED, survey.nextQuestion());
    }

    @Test public void secondQuestionRemainsPendingWithoutAnswer() {
        MarketingResearchSurvey survey = new MarketingResearchSurvey();
        survey.answer(MarketingResearchSurvey.Question.REQUIRED, true);
        assertEquals(Boolean.TRUE, survey.getMerchandisingRequired());
        assertNull(survey.getWillMerchandise());
        assertEquals(MarketingResearchSurvey.Question.INTENTION, survey.nextQuestion());
        assertEquals(MarketingResearchSurvey.Question.INTENTION, survey.nextQuestion());
    }

    @Test public void staleOrOutOfOrderCallbacksCannotChangeAnswers() {
        MarketingResearchSurvey survey = new MarketingResearchSurvey();
        survey.answer(MarketingResearchSurvey.Question.INTENTION, true);
        assertNull(survey.getWillMerchandise());
        survey.answer(MarketingResearchSurvey.Question.NONE, true);
        assertNull(survey.getMerchandisingRequired());
        survey.answer(MarketingResearchSurvey.Question.REQUIRED, true);
        survey.answer(MarketingResearchSurvey.Question.REQUIRED, false);
        assertEquals(Boolean.TRUE, survey.getMerchandisingRequired());
        survey.answer(MarketingResearchSurvey.Question.INTENTION, false);
        survey.answer(MarketingResearchSurvey.Question.INTENTION, true);
        assertEquals(Boolean.FALSE, survey.getWillMerchandise());
    }

    @Test public void restartedSurveyIgnoresOldSecondQuestionAndRepeatedAcknowledgement() {
        MarketingResearchSurvey survey = new MarketingResearchSurvey();
        survey.answer(MarketingResearchSurvey.Question.REQUIRED, true);
        survey.answer(MarketingResearchSurvey.Question.INTENTION, true);
        survey.acknowledgeWarning();
        survey.answer(MarketingResearchSurvey.Question.INTENTION, true);
        survey.acknowledgeWarning();
        assertEquals(MarketingResearchSurvey.Question.REQUIRED, survey.nextQuestion());
        assertNull(survey.getMerchandisingRequired());
        assertNull(survey.getWillMerchandise());
    }

    @Test public void firstNoAfterRestartFinishesTheSurvey() {
        MarketingResearchSurvey survey = restartedSurvey();
        survey.answer(MarketingResearchSurvey.Question.REQUIRED, false);
        assertEquals(MarketingResearchSurvey.Question.NONE, survey.nextQuestion());
        assertEquals(Boolean.FALSE, survey.getMerchandisingRequired());
        assertNull(survey.getWillMerchandise());
    }

    @Test public void secondNoAfterRestartFinishesWithoutAnotherWarning() {
        MarketingResearchSurvey survey = restartedSurvey();
        survey.answer(MarketingResearchSurvey.Question.REQUIRED, true);
        survey.answer(MarketingResearchSurvey.Question.INTENTION, false);
        assertEquals(MarketingResearchSurvey.Question.NONE, survey.nextQuestion());
        assertEquals(Boolean.TRUE, survey.getMerchandisingRequired());
        assertEquals(Boolean.FALSE, survey.getWillMerchandise());
    }

    private static MarketingResearchSurvey restartedSurvey() {
        MarketingResearchSurvey survey = new MarketingResearchSurvey();
        survey.answer(MarketingResearchSurvey.Question.REQUIRED, true);
        survey.answer(MarketingResearchSurvey.Question.INTENTION, true);
        survey.acknowledgeWarning();
        return survey;
    }

    @Test public void separateSurveysDoNotShareAnswers() {
        MarketingResearchSurvey first = new MarketingResearchSurvey();
        MarketingResearchSurvey second = new MarketingResearchSurvey();
        first.answer(MarketingResearchSurvey.Question.REQUIRED, true);
        first.answer(MarketingResearchSurvey.Question.INTENTION, false);
        assertNull(second.getMerchandisingRequired());
        assertNull(second.getWillMerchandise());
        assertEquals(MarketingResearchSurvey.Question.REQUIRED, second.nextQuestion());
    }

    @Test public void viewModelKeepsAnswersForSameVisitAndSeparatesOtherVisits() {
        DetailedReportViewModel model = new DetailedReportViewModel();
        MarketingResearchSurvey first = model.getMarketingResearchSurvey(1041126039410061655L);
        first.answer(MarketingResearchSurvey.Question.REQUIRED, true);
        assertSame(first, model.getMarketingResearchSurvey(1041126039410061655L));
        assertEquals(MarketingResearchSurvey.Question.INTENTION,
                model.getMarketingResearchSurvey(1041126039410061655L).nextQuestion());
        MarketingResearchSurvey second = model.getMarketingResearchSurvey(1041126039410061639L);
        assertNotSame(first, second);
        assertNull(second.getMerchandisingRequired());
        assertNull(second.getWillMerchandise());
    }

    @Test public void answersAreNotRetainedAfterLeavingReportSession() {
        DetailedReportViewModel firstSession = new DetailedReportViewModel();
        firstSession.getMarketingResearchSurvey(1041126039410061655L)
                .answer(MarketingResearchSurvey.Question.REQUIRED, false);
        DetailedReportViewModel newSession = new DetailedReportViewModel();
        assertEquals(MarketingResearchSurvey.Question.REQUIRED,
                newSession.getMarketingResearchSurvey(1041126039410061655L).nextQuestion());
    }
}
