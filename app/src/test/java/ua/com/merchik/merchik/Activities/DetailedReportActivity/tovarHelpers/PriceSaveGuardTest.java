package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PriceSaveGuardTest {
    @Test
    public void manualNoPromotionWinsOverPricesUntilManuallyEnabled() {
        assertFalse(PriceSaveGuard.shouldAutoMarkPromotion("80", "100", "2"));
        assertFalse(PriceSaveGuard.shouldAutoMarkPromotion("100", "100.00", " 2 "));
        for (String initial : new String[]{null, "", "0", "1"}) {
            assertTrue(PriceSaveGuard.shouldAutoMarkPromotion("80", "100", initial));
            assertFalse(PriceSaveGuard.shouldAutoMarkPromotion("0", "100", initial));
        }
        ReportPrepareDB row = new ReportPrepareDB();
        row.setAkciya("2");
        row.setAkciyaId("357");
        assertEquals(0, PriceSaveGuard.getDiscountPercent(row));
        assertFalse(PriceSaveGuard.getPromotionPriceCheck(row, "80", "180").outsideRange());
        assertEquals("357", row.getAkciyaId());
        assertEquals("2", row.getAkciya());
        row.setAkciya("1");
        assertTrue(PriceSaveGuard.shouldAutoMarkPromotion("80", "100", row.getAkciya()));
    }

    @Test
    public void comparesPricesAtDisplayedCentPrecision() {
        assertFalse(PriceSaveGuard.isPriceAboveBeforePromotion("95.90", "95.9"));
        assertFalse(PriceSaveGuard.isPriceAboveBeforePromotion("95.904", "95.899999"));
        assertTrue(PriceSaveGuard.isPriceAboveBeforePromotion("95.905", "95.90"));
        assertTrue(PriceSaveGuard.isPriceAboveBeforePromotion("100.01", "100.00"));
    }

    @Test
    public void productLabelContainsCodeNameAndWeight() {
        TovarDB product = new TovarDB();
        product.setNm(" Juice ");
        product.setWeight(" 0.95 l ");
        assertEquals("(141609) Juice 0.95 l", PriceSaveGuard.productLabel("141609", product));
    }

    @Test
    public void productLabelOmitsMissingPartsWithoutNullText() {
        assertEquals("(141609)", PriceSaveGuard.productLabel("141609", null));
        TovarDB product = new TovarDB();
        assertEquals("(141609)", PriceSaveGuard.productLabel("141609", product));
        product.setNm("Juice");
        product.setWeight(" ");
        assertEquals("(141609) Juice", PriceSaveGuard.productLabel("141609", product));
    }

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
