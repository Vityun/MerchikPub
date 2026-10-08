package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PriceSaveGuardTest {
    @Test
    public void higherCurrentPriceIsRejected() {
        assertTrue(PriceSaveGuard.isPriceAboveBeforePromotion("30", "20"));
        assertTrue(PriceSaveGuard.isPriceAboveBeforePromotion("20.01", "20"));
    }

    @Test
    public void equalAndLowerCurrentPricesAreAllowed() {
        assertFalse(PriceSaveGuard.isPriceAboveBeforePromotion("10", "20"));
        assertFalse(PriceSaveGuard.isPriceAboveBeforePromotion("20", "20.00"));
        assertFalse(PriceSaveGuard.isPriceAboveBeforePromotion("10.0100", "10.01"));
    }

    @Test
    public void decimalCommasAndSurroundingSpacesAreSupported() {
        assertTrue(PriceSaveGuard.isPriceAboveBeforePromotion(" 20,01 ", " 20,00 "));
        assertFalse(PriceSaveGuard.isPriceAboveBeforePromotion("19,99", "20.00"));
    }

    @Test
    public void firstPriceCanBeEnteredBeforeTheOtherOne() {
        for (String missing : new String[]{null, "", " ", "0", "0.00", "0,00"}) {
            assertFalse(PriceSaveGuard.isPriceAboveBeforePromotion("10", missing));
            assertFalse(PriceSaveGuard.isPriceAboveBeforePromotion(missing, "20"));
        }
    }

    @Test
    public void malformedValuesAreNotTreatedAsValidPricesForComparison() {
        for (String invalid : new String[]{"abc", "NaN", "Infinity", "-1", "1,2,3"}) {
            assertFalse(PriceSaveGuard.isPriceAboveBeforePromotion("10", invalid));
            assertFalse(PriceSaveGuard.isPriceAboveBeforePromotion(invalid, "20"));
        }
    }
}
