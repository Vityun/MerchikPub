package ua.com.merchik.merchik.Options.Controls;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class PremiumPercentagePolicyTest {
    private static final String ERROR_COLOR = "#EF5350";
    private static final double CASH_ZAKAZ = 273.13;

    @Test
    public void tenPercentReductionUsesTwoFivePercentSteps() {
        assertReduction(-10, "42.02", "42,02");
    }

    @Test
    public void fivePercentReductionUsesOneStep() {
        assertReduction(-5, "21.01", "21,01");
    }

    @Test
    public void reductionsScaleWithTheVisitBase() {
        assertEquals(new BigDecimal("15.39"), PremiumPercentagePolicy.amount(100, -10));
        assertEquals(new BigDecimal("7.69"), PremiumPercentagePolicy.amount(100, -5));
        assertEquals("7.69", PremiumPercentagePolicy.penalty(100, -5));
    }

    @Test
    public void roundingHappensOnlyAfterApplyingThePercentage() {
        // Doubling the rounded one-step amount would produce 0.02/0.04 instead of 0.03.
        assertEquals(new BigDecimal("0.03"), PremiumPercentagePolicy.amount(0.19, -10));
        assertEquals(new BigDecimal("0.01"), PremiumPercentagePolicy.amount(0.19, -5));
        assertEquals(new BigDecimal("0.03"), PremiumPercentagePolicy.amount(0.20, -10));
        assertEquals(new BigDecimal("0.02"), PremiumPercentagePolicy.amount(0.20, -5));
    }

    @Test
    public void bonusAndUnchangedPercentageAreNotStoredAsPenalties() {
        assertEquals(new BigDecimal("21.01"), PremiumPercentagePolicy.amount(CASH_ZAKAZ, 5));
        assertEquals("0.00", PremiumPercentagePolicy.penalty(CASH_ZAKAZ, 5));
        assertEquals(new BigDecimal("0.00"), PremiumPercentagePolicy.amount(CASH_ZAKAZ, 0));
        assertEquals("0.00", PremiumPercentagePolicy.penalty(CASH_ZAKAZ, 0));
    }

    @Test
    public void storedPercentageAcceptsIntegerAndDecimalRepresentations() {
        for (String percent : new String[]{"-5", " -5 ", "-5.0", "-5,00"}) {
            assertEquals(percent, new BigDecimal("21.01"),
                    PremiumPercentagePolicy.amount(CASH_ZAKAZ, percent));
        }
        assertEquals(new BigDecimal("42.02"), PremiumPercentagePolicy.amount(CASH_ZAKAZ, "-10.0"));
    }

    @Test
    public void absentOrInvalidStoredPercentageDoesNotInventAPenalty() {
        for (String percent : new String[]{null, "", " ", "null", "bad", "-5.5", "2147483648"}) {
            assertEquals(percent, new BigDecimal("0.00"),
                    PremiumPercentagePolicy.amount(CASH_ZAKAZ, percent));
        }
    }

    @Test
    public void missingOrInvalidVisitBaseHasZeroAmount() {
        for (double base : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(new BigDecimal("0.00"), PremiumPercentagePolicy.amount(base, -10));
            assertEquals("0.00", PremiumPercentagePolicy.penalty(base, -5));
        }
    }

    @Test
    public void positiveCounterIsGreenAndUsesTheSharedAmount() {
        assertEquals("green", PremiumPercentagePolicy.counterColor("5", ERROR_COLOR));
        assertEquals("~+21,01", PremiumPercentagePolicy.counterText(
                CASH_ZAKAZ, "5", Locale.forLanguageTag("uk-UA")));
        assertEquals("0.00", PremiumPercentagePolicy.penalty(CASH_ZAKAZ, 5));
    }

    @Test
    public void negativeCountersAreRedForBothReductionLevels() {
        assertEquals(ERROR_COLOR, PremiumPercentagePolicy.counterColor("-5", ERROR_COLOR));
        assertEquals(ERROR_COLOR, PremiumPercentagePolicy.counterColor("-10", ERROR_COLOR));
        assertEquals("~-21,01", PremiumPercentagePolicy.counterText(
                CASH_ZAKAZ, "-5", Locale.forLanguageTag("uk-UA")));
        assertEquals("~-42,02", PremiumPercentagePolicy.counterText(
                CASH_ZAKAZ, "-10", Locale.forLanguageTag("uk-UA")));
    }

    @Test
    public void negativeCounterUsesTheColorResolvedByTheUi() {
        for (String percent : new String[]{"-5", "-10", "-5.0", "-10,00"}) {
            assertEquals("#123456", PremiumPercentagePolicy.counterColor(percent, "#123456"));
        }
        assertEquals("green", PremiumPercentagePolicy.counterColor("5", "#123456"));
        assertEquals("gray", PremiumPercentagePolicy.counterColor("0", "#123456"));
    }

    @Test
    public void zeroCounterIsNeutralWithoutASign() {
        assertEquals("gray", PremiumPercentagePolicy.counterColor("0", ERROR_COLOR));
        assertEquals("~0,00", PremiumPercentagePolicy.counterText(
                CASH_ZAKAZ, "0", Locale.forLanguageTag("uk-UA")));
    }

    @Test
    public void counterFormattingHandlesStoredDecimalsAndMissingResults() {
        assertEquals("green", PremiumPercentagePolicy.counterColor(" +5.0 ", ERROR_COLOR));
        assertEquals("~+21.01", PremiumPercentagePolicy.counterText(CASH_ZAKAZ, " +5.0 ", Locale.US));
        for (String percent : new String[]{null, "", "bad"}) {
            assertEquals("gray", PremiumPercentagePolicy.counterColor(percent, ERROR_COLOR));
            assertEquals("~0.00", PremiumPercentagePolicy.counterText(CASH_ZAKAZ, percent, Locale.US));
        }
    }

    private static void assertReduction(int percent, String stored, String displayed) {
        BigDecimal detailAmount = PremiumPercentagePolicy.amount(CASH_ZAKAZ, percent);
        BigDecimal buttonAmount = PremiumPercentagePolicy.amount(CASH_ZAKAZ, String.valueOf(percent));
        assertEquals(new BigDecimal(stored), detailAmount);
        assertEquals(detailAmount, buttonAmount);
        assertEquals(stored, PremiumPercentagePolicy.penalty(CASH_ZAKAZ, percent));
        assertEquals(displayed, String.format(Locale.forLanguageTag("uk-UA"), "%.2f", buttonAmount));
        assertEquals(displayed, String.format(Locale.forLanguageTag("uk-UA"), "%.2f", detailAmount));
    }

    @Test
    public void effectivePercentAccountsForPerformerShareOnlyOnce() {
        assertEquals(new BigDecimal("-7.69"), PremiumPercentagePolicy.effectivePercent(-5));
        assertEquals(new BigDecimal("-15.39"), PremiumPercentagePolicy.effectivePercent(-10));
        assertEquals(new BigDecimal("7.69"), PremiumPercentagePolicy.effectivePercent(5));
        assertEquals(new BigDecimal("0.00"), PremiumPercentagePolicy.effectivePercent(0));
    }

    @Test
    public void detailUsesEffectivePercentWhilePersistenceUsesTechnicalPercent() {
        Locale locale = Locale.forLanguageTag("uk-UA");
        assertEquals("-7,69%", PremiumPercentagePolicy.detailPercentText(-5, locale));
        assertEquals("-15,39%", PremiumPercentagePolicy.detailPercentText(-10, locale));
        assertEquals("7,69%", PremiumPercentagePolicy.detailPercentText(5, locale));
        assertEquals("0,00%", PremiumPercentagePolicy.detailPercentText(0, locale));
        assertEquals("-15.39%", PremiumPercentagePolicy.detailPercentText(-10, Locale.US));
        assertEquals("42.02", PremiumPercentagePolicy.penalty(CASH_ZAKAZ, -10));
        assertEquals(new BigDecimal("42.02"), PremiumPercentagePolicy.amount(CASH_ZAKAZ, "-10"));
    }

    @Test
    public void moneyUsesExactRateNotRoundedDisplayPercentage() {
        assertEquals(new BigDecimal("0.07693"), PremiumPercentagePolicy.adjustmentRate(-5));
        assertEquals(new BigDecimal("0.15386"), PremiumPercentagePolicy.adjustmentRate(-10));
        // Using the displayed 15.39% would give 42.03, not the agreed 42.02.
        assertEquals(new BigDecimal("42.02"), PremiumPercentagePolicy.amount(CASH_ZAKAZ, -10));
        assertEquals(new BigDecimal("153.86"), PremiumPercentagePolicy.amount(1000, -10));
        assertEquals(new BigDecimal("76.93"), PremiumPercentagePolicy.amount(1000, 5));
    }
}
