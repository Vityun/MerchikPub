package ua.com.merchik.merchik.Options.Controls;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class PremiumPercentagePolicyTest {
    private static final double CASH_ZAKAZ = 273.13;

    @Test
    public void tenPercentReductionIsNotDoubled() {
        assertReduction(-10, "21.01", "21,01");
    }

    @Test
    public void fivePercentReductionUsesHalfTheUnroundedAmount() {
        assertReduction(-5, "10.51", "10,51");
    }

    @Test
    public void reductionsScaleWithTheVisitBase() {
        assertEquals(new BigDecimal("7.69"), PremiumPercentagePolicy.amount(100, -10));
        assertEquals(new BigDecimal("3.85"), PremiumPercentagePolicy.amount(100, -5));
        assertEquals("3.85", PremiumPercentagePolicy.penalty(100, -5));
    }

    @Test
    public void roundingHappensOnlyAfterApplyingThePercentage() {
        // Rounding the 10% amount (0.23) first would incorrectly give 0.12 for 5%.
        assertEquals(new BigDecimal("0.23"), PremiumPercentagePolicy.amount(2.98, -10));
        assertEquals(new BigDecimal("0.11"), PremiumPercentagePolicy.amount(2.98, -5));
        assertEquals(new BigDecimal("0.23"), PremiumPercentagePolicy.amount(2.99, -10));
        assertEquals(new BigDecimal("0.12"), PremiumPercentagePolicy.amount(2.99, -5));
    }

    @Test
    public void bonusAndUnchangedPercentageAreNotStoredAsPenalties() {
        assertEquals(new BigDecimal("10.51"), PremiumPercentagePolicy.amount(CASH_ZAKAZ, 5));
        assertEquals("0.00", PremiumPercentagePolicy.penalty(CASH_ZAKAZ, 5));
        assertEquals(new BigDecimal("0.00"), PremiumPercentagePolicy.amount(CASH_ZAKAZ, 0));
        assertEquals("0.00", PremiumPercentagePolicy.penalty(CASH_ZAKAZ, 0));
    }

    @Test
    public void storedPercentageAcceptsIntegerAndDecimalRepresentations() {
        for (String percent : new String[]{"-5", " -5 ", "-5.0", "-5,00"}) {
            assertEquals(percent, new BigDecimal("10.51"),
                    PremiumPercentagePolicy.amount(CASH_ZAKAZ, percent));
        }
        assertEquals(new BigDecimal("21.01"), PremiumPercentagePolicy.amount(CASH_ZAKAZ, "-10.0"));
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
        assertEquals("green", PremiumPercentagePolicy.counterColor("5"));
        assertEquals("~+10,51", PremiumPercentagePolicy.counterText(
                CASH_ZAKAZ, "5", Locale.forLanguageTag("uk-UA")));
        assertEquals("0.00", PremiumPercentagePolicy.penalty(CASH_ZAKAZ, 5));
    }

    @Test
    public void negativeCountersAreRedForBothReductionLevels() {
        assertEquals("red", PremiumPercentagePolicy.counterColor("-5"));
        assertEquals("red", PremiumPercentagePolicy.counterColor("-10"));
        assertEquals("~-10,51", PremiumPercentagePolicy.counterText(
                CASH_ZAKAZ, "-5", Locale.forLanguageTag("uk-UA")));
        assertEquals("~-21,01", PremiumPercentagePolicy.counterText(
                CASH_ZAKAZ, "-10", Locale.forLanguageTag("uk-UA")));
    }

    @Test
    public void zeroCounterIsNeutralWithoutASign() {
        assertEquals("gray", PremiumPercentagePolicy.counterColor("0"));
        assertEquals("~0,00", PremiumPercentagePolicy.counterText(
                CASH_ZAKAZ, "0", Locale.forLanguageTag("uk-UA")));
    }

    @Test
    public void counterFormattingHandlesStoredDecimalsAndMissingResults() {
        assertEquals("green", PremiumPercentagePolicy.counterColor(" +5.0 "));
        assertEquals("~+10.51", PremiumPercentagePolicy.counterText(CASH_ZAKAZ, " +5.0 ", Locale.US));
        for (String percent : new String[]{null, "", "bad"}) {
            assertEquals("gray", PremiumPercentagePolicy.counterColor(percent));
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
}
