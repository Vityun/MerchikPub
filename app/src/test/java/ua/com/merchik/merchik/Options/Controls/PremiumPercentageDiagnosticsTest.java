package ua.com.merchik.merchik.Options.Controls;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PremiumPercentageDiagnosticsTest {
    @Test
    public void recordsSharedAmountsForBonusAndBothReductions() {
        for (int percent : new int[]{5, -5, -10}) {
            JsonObject result = PremiumPercentageDiagnostics.result(273.13, percent, true, "TEST_BRANCH");
            assertEquals(PremiumPercentagePolicy.amount(273.13, percent), result.get("amount").getAsBigDecimal());
            assertEquals(percent == -10 ? "42.02" : "21.01", result.get("amount").getAsString());
            assertEquals(percent, result.get("percent").getAsInt());
            assertEquals(percent == -10 ? "-15.39" : percent < 0 ? "-7.69" : "7.69",
                    result.get("effective_percent").getAsString());
            assertEquals(percent == -10 ? 2 : 1, result.get("multiplier").getAsInt());
            assertEquals(percent == -10 ? "0.15386" : "0.07693", result.get("adjustment_rate").getAsString());
            assertEquals(percent > 0 ? "BONUS" : "REDUCTION", result.get("adjustment").getAsString());
            assertEquals(0, result.getAsJsonArray("zero_reasons").size());
            assertEquals("TEST_BRANCH", result.get("decision").getAsString());
        }
    }

    @Test
    public void zeroPercentageExplainsTheActualDecision() {
        for (String decision : new String[]{"NO_COMPLETED_REPORTS_IN_PERIOD", "BEFORE_20TH_REPORT",
                "NEUTRAL_RECLAMATION_RANGE", "NO_MATCHING_PERCENT_BRANCH"}) {
            JsonObject result = PremiumPercentageDiagnostics.result(273.13, 0, true, decision);
            assertEquals("0.00", result.get("amount").getAsString());
            assertEquals("0.00", result.get("effective_percent").getAsString());
            assertEquals(0, result.get("multiplier").getAsInt());
            assertReasons(result, "ZERO_PERCENT", decision);
        }
    }

    @Test
    public void zeroBaseDoesNotBlameThePercentage() {
        assertReasons(PremiumPercentageDiagnostics.result(0.0, -10, true, "REDUCTION"), "CASH_ZAKAZ_ZERO");
    }

    @Test
    public void reportsBothCausesWhenBaseAndPercentageAreZero() {
        assertReasons(PremiumPercentageDiagnostics.result(0.0, 0, true, "NEUTRAL_RECLAMATION_RANGE"),
                "ZERO_PERCENT", "NEUTRAL_RECLAMATION_RANGE", "CASH_ZAKAZ_ZERO");
    }

    @Test
    public void missingNegativeAndNonFiniteBasesHaveDistinctReasons() {
        assertReasons(PremiumPercentageDiagnostics.result(null, -5, true, "REDUCTION"), "CASH_ZAKAZ_MISSING");
        assertReasons(PremiumPercentageDiagnostics.result(-100.0, -5, true, "REDUCTION"), "CASH_ZAKAZ_NEGATIVE");
        for (double base : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            JsonObject result = PremiumPercentageDiagnostics.result(base, -5, true, "REDUCTION");
            assertReasons(result, "CASH_ZAKAZ_NON_FINITE");
            assertEquals(Double.toString(base), JsonParser.parseString(result.toString())
                    .getAsJsonObject().get("cash_zakaz").getAsString());
        }
    }

    @Test
    public void tinyAmountIdentifiesRoundingNotMissingData() {
        JsonObject result = PremiumPercentageDiagnostics.result(0.01, -5, true, "REDUCTION");
        assertEquals("0.00", result.get("amount").getAsString());
        assertReasons(result, "ROUNDED_TO_ZERO");
    }

    @Test
    public void incompleteCalculationIsNotPresentedAsAValidZero() {
        for (String decision : new String[]{"EXECUTOR_MISSING", "DOCUMENT_DATE_MISSING", "CALCULATION_EXCEPTION"}) {
            JsonObject result = PremiumPercentageDiagnostics.result(273.13, 0, false, decision);
            assertFalse(result.get("calculation_complete").getAsBoolean());
            assertTrue(result.get("amount").isJsonNull());
            assertTrue(result.get("percent").isJsonNull());
            assertTrue(result.get("effective_percent").isJsonNull());
            assertTrue(result.get("adjustment_rate").isJsonNull());
            assertTrue(result.get("multiplier").isJsonNull());
            assertEquals(decision, result.get("unavailable_reason").getAsString());
            assertReasons(result);
        }
    }

    @Test
    public void includesFormulaAndRoundingForReproduction() {
        JsonObject result = PremiumPercentageDiagnostics.result(2.98, -5, true, "REDUCTION");
        assertEquals("0.23", result.get("amount").getAsString());
        assertEquals(0.07693, result.get("rate_for_5_percent").getAsDouble(), 0);
        assertFalse(result.has("rate_for_10_percent"));
        assertEquals(5, result.get("percent_step").getAsInt());
        assertEquals("cash_zakaz * rate_for_5_percent * abs(percent) / 5", result.get("formula").getAsString());
        assertEquals("rate_for_5_percent * percent / 5 * 100", result.get("effective_percent_formula").getAsString());
        assertEquals("HALF_UP, scale=2", result.get("rounding").getAsString());
    }

    @Test
    public void datesPreserveExactBoundsAndUnknownDatesStayUnknown() {
        Date date = new Date(1794565326929L);
        JsonObject value = PremiumPercentageDiagnostics.date(date);
        assertEquals(date.getTime(), value.get("epoch_ms").getAsLong());
        assertEquals(TimeZone.getDefault().getID(), value.get("time_zone").getAsString());
        assertTrue(value.get("local").getAsString().matches(".*\\.929 [+-]\\d{4}"));
        assertTrue(PremiumPercentageDiagnostics.date(null).get("epoch_ms").isJsonNull());
        assertTrue(PremiumPercentageDiagnostics.date(null).get("local").isJsonNull());
    }

    private static void assertReasons(JsonObject result, String... expected) {
        JsonArray reasons = result.getAsJsonArray("zero_reasons");
        assertEquals(expected.length, reasons.size());
        for (int i = 0; i < expected.length; i++) assertEquals(expected[i], reasons.get(i).getAsString());
    }
}
