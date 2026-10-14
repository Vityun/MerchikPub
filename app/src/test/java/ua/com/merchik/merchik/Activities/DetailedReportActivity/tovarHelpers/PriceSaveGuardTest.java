package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PriceSaveGuardTest {
    @Test
    public void onlyMissingPromotionTypeCanBeFilledAutomatically() {
        for (String missing : new String[]{null, "", " ", "0", " 0 "}) {
            assertTrue(PriceSaveGuard.isPromotionTypeMissing(missing));
        }
        assertFalse(PriceSaveGuard.isPromotionTypeMissing("357"));
        assertFalse(PriceSaveGuard.isPromotionTypeMissing(" 358 "));
    }

    @Test
    public void promotionTypeMustBelongToTheSameProduct() {
        assertEquals("357", PriceSaveGuard.findPromotionTypeId("141609", Arrays.asList(
                requirement("141610", 358),
                requirement("141609", 0),
                requirement("141609", 357))));
        assertNull(PriceSaveGuard.findPromotionTypeId("141611",
                Collections.singletonList(requirement("141609", 357))));
    }

    @Test
    public void missingOrZeroPromotionTypeDoesNotSuggestAValue() {
        assertNull(PriceSaveGuard.findPromotionTypeId("141609", null));
        assertNull(PriceSaveGuard.findPromotionTypeId("141609", Collections.emptyList()));
        assertNull(PriceSaveGuard.findPromotionTypeId("141609", Arrays.asList(
                null, requirement("141609", 0), requirement("141609", -1))));
        for (String invalid : new String[]{null, "", "0"}) {
            assertNull(PriceSaveGuard.findPromotionTypeId(invalid,
                    Collections.singletonList(requirement(invalid, 357))));
        }
    }

    private static AdditionalRequirementsDB requirement(String tovarId, int promoTpId) {
        AdditionalRequirementsDB requirement = new AdditionalRequirementsDB();
        requirement.setTovarId(tovarId);
        requirement.setPromoTpId(promoTpId);
        return requirement;
    }

    @Test
    public void bothPricesMustBePositiveToMarkPromotionPresent() {
        assertTrue(PriceSaveGuard.hasBothPositivePrices("10", "20"));
        assertTrue(PriceSaveGuard.hasBothPositivePrices("20", "20.00"));
        assertTrue(PriceSaveGuard.hasBothPositivePrices(" 19,99 ", " 20,00 "));
    }

    @Test
    public void missingOrInvalidPriceDoesNotTriggerPromotionMark() {
        for (String missing : new String[]{null, "", " ", "0", "0.00", "0,00", "-1", "abc", "NaN", "Infinity", "1,2,3"}) {
            assertFalse(PriceSaveGuard.hasBothPositivePrices("10", missing));
            assertFalse(PriceSaveGuard.hasBothPositivePrices(missing, "20"));
        }
    }

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
