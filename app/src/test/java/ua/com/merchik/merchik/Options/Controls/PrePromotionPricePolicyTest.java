package ua.com.merchik.merchik.Options.Controls;

import org.junit.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.*;
import static ua.com.merchik.merchik.Options.Controls.PrePromotionPricePolicy.*;
import static ua.com.merchik.merchik.Options.Controls.PrePromotionPricePolicy.Issue.*;
import static ua.com.merchik.merchik.Options.Controls.PrePromotionPricePolicy.PromotionCheck.*;

public class PrePromotionPricePolicyTest {
    private final Date start = Date.from(Instant.parse("2026-11-01T22:00:00Z"));

    @Test public void promotionStartsOnDocumentDayInKyiv() {
        assertEquals(NOT_APPLICABLE, promotionCheck(new Date(start.getTime() - 1), "186", "0"));
        assertEquals(NOT_APPLICABLE, promotionCheck(null, "186", "0"));
        assertEquals(PRESENCE_NOT_SET, promotionCheck(start, "186", "0"));
    }

    @Test public void onlyConfiguredPromotionRequiresPresenceUnlessExplicitlyAbsent() {
        for (String code : new String[]{null, "", " ", "0"})
            assertEquals(NOT_APPLICABLE, promotionCheck(start, code, "0"));
        for (String presence : new String[]{null, "", " ", "0", " 0 "})
            assertEquals(PRESENCE_NOT_SET, promotionCheck(start, "186", presence));
        assertEquals(CHECK_PRICE, promotionCheck(start, "186", "1"));
        assertEquals(NOT_APPLICABLE, promotionCheck(start, "186", " 2 "));
    }

    @Test public void missingPresenceIsReportedBeforeCorridorEvenWithoutAPercentage() {
        for (int discount : new int[]{0, 20}) {
            Evaluation result = evaluate("95", "100", "100", PRESENCE_NOT_SET, discount);
            assertEquals(MISSING_PROMOTION_PRESENCE, result.issue);
            assertTrue(result.issue.currentPriceFound());
            assertEquals("corridorChecked=false", result.corridorDiagnostic());
        }
        assertEquals(OUTSIDE_CORRIDOR_DELTA, evaluate("95", "100", "100", CHECK_PRICE, 20).issue);
        assertEquals(NONE, evaluate("95", "100", "100", NOT_APPLICABLE, 20).issue);
    }

    @Test public void missingPresenceDoesNotMaskEarlierPriceViolations() {
        assertEquals(MISSING_CURRENT, evaluate("0", "100", "100", PRESENCE_NOT_SET, 20).issue);
        assertEquals(MISSING_BEFORE, evaluate("80", "0", "100", PRESENCE_NOT_SET, 20).issue);
        assertEquals(ABOVE_MIN, evaluate("80", "70", "100", PRESENCE_NOT_SET, 20).issue);
        assertEquals(ABOVE_MAX, evaluate("80", "100", "70", PRESENCE_NOT_SET, 20).issue);
        assertEquals(EQUAL_TO_MAX, evaluate("100", "100", "100", PRESENCE_NOT_SET, 20).issue);
    }

    @Test public void missingCurrentOverridesEarlierViolationEvenWithoutPromotion() {
        assertEquals(MISSING_CURRENT, evaluate("0", "100", "100", NOT_APPLICABLE, 0).issue);
        assertEquals(MISSING_CURRENT, evaluate("0.00", "100", "100", CHECK_PRICE, 20).issue);
        assertEquals(MISSING_CURRENT, evaluate("", null, "0", CHECK_PRICE, 20).issue);
        assertEquals(MISSING_CURRENT, evaluate("0.004", "100", "100", CHECK_PRICE, 20).issue);
    }

    @Test public void bothBeforePricesAreRequiredBeforeComparisons() {
        assertEquals(MISSING_BEFORE, evaluate("100", "", "99", CHECK_PRICE, 20).issue);
        assertEquals(MISSING_BEFORE, evaluate("100", "99", "0", NOT_APPLICABLE, 0).issue);
    }

    @Test public void aboveMinAndMaxTakePrecedence() {
        assertEquals(ABOVE_MIN, evaluate("100", "90", "80", CHECK_PRICE, 20).issue);
        assertEquals(ABOVE_MIN, evaluate("100", "90", "100", CHECK_PRICE, 20).issue);
        assertEquals(ABOVE_MAX, evaluate("100", "101", "90", NOT_APPLICABLE, 0).issue);
    }

    @Test public void equalityUsesMaxAtCentPrecisionAndNeedsNoDiscountPercent() {
        assertEquals(EQUAL_TO_MAX, evaluate("95,9", "100", "95.904", CHECK_PRICE, 0).issue);
        assertEquals(NONE, evaluate("95.90", "100", "95.90", NOT_APPLICABLE, 20).issue);
        assertEquals(NONE, evaluate("80", "80", "100", CHECK_PRICE, 20).issue);
    }

    @Test public void corridorBasedOnMaxIncludesBothEndpoints() {
        assertEquals(NONE, evaluate("30", "95", "100", CHECK_PRICE, 20).issue);
        assertEquals(NONE, evaluate("90", "95", "100", CHECK_PRICE, 20).issue);
        assertEquals(OUTSIDE_CORRIDOR_DELTA, evaluate("29.99", "95", "100", CHECK_PRICE, 20).issue);
        assertEquals(OUTSIDE_CORRIDOR_DELTA, evaluate("90.01", "95", "100", CHECK_PRICE, 20).issue);
        assertEquals(NONE, evaluate("95", "95", "100", NOT_APPLICABLE, 20).issue);
        assertEquals(NONE, evaluate("95", "95", "100", CHECK_PRICE, 0).issue);
    }

    @Test public void plannedAndFullDiscountAreRoundedBeforeCalculatingBounds() {
        assertEquals("9.05", plannedPriceText("10.05", 10));
        assertEquals(NONE, evaluate("6.53", "10.05", "10.05", CHECK_PRICE, 10).issue);
        assertEquals(OUTSIDE_CORRIDOR_DELTA, evaluate("6.52", "10.05", "10.05", CHECK_PRICE, 10).issue);
    }

    @Test public void upperBoundAtHalfCentIsNotRoundedUp() {
        // 9.05 + 1.01 / 2 = 9.555
        assertEquals(NONE, evaluate("9.55", "10.05", "10.05", CHECK_PRICE, 10).issue);
        assertEquals(OUTSIDE_CORRIDOR_DELTA, evaluate("9.56", "10.05", "10.05", CHECK_PRICE, 10).issue);
    }

    @Test public void foundCountsCurrentPriceIndependentlyOfViolation() {
        for (Issue issue : Issue.values()) assertEquals(issue != MISSING_CURRENT, issue.currentPriceFound());
    }

    @Test public void diagnosticsUseActualUnroundedBoundsForRejectedAndAcceptedPrices() {
        Evaluation rejected = evaluate("17.99", "37.90", "37.90", CHECK_PRICE, 15);
        assertEquals(OUTSIDE_CORRIDOR_DELTA, rejected.issue);
        assertEquals("corridorChecked=true, planned=32.22, delta=5.69, lower=17.995, upper=35.065",
                rejected.corridorDiagnostic());
        Evaluation accepted = evaluate("35.06", "37.90", "37.90", CHECK_PRICE, 15);
        assertEquals(NONE, accepted.issue);
        assertEquals(rejected.corridorDiagnostic(), accepted.corridorDiagnostic());
    }

    @Test public void diagnosticsDoNotInventBoundsWhenCorridorWasNotEvaluated() {
        assertEquals("corridorChecked=false", evaluate("0", "37.90", "37.90", CHECK_PRICE, 15).corridorDiagnostic());
        assertEquals("corridorChecked=false", evaluate("18", "37.90", "37.90", NOT_APPLICABLE, 0).corridorDiagnostic());
    }
}
