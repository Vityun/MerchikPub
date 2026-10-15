package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import org.junit.Test;
import static org.junit.Assert.*;

public class PromotionPriceCheckTest {
    @Test public void expectedDiscountUsesBeforePriceNotEnteredCurrentPrice() {
        assertEquals("20", new PromotionPriceCheck("83", "100", 20).expectedDiscountText());
        assertEquals("36", new PromotionPriceCheck("80", "180", 20).expectedDiscountText());
        assertEquals("6.67", new PromotionPriceCheck("1", "33,35", 20).expectedDiscountText());
        assertEquals("2.01", new PromotionPriceCheck("1", "10.025", 20).expectedDiscountText());
    }

    @Test public void missingDiscountOrBeforePriceHasNoExpectedAmount() {
        for (String missing : new String[]{null, "", "0", "bad", "-1"}) {
            assertEquals("0", new PromotionPriceCheck("80", missing, 20).expectedDiscountText());
        }
        for (int percent : new int[]{0, -1, 101}) {
            assertEquals("0", new PromotionPriceCheck("80", "100", percent).expectedDiscountText());
        }
    }

    @Test public void promotionNameIsAnImmutableDisplayValue() {
        assertEquals("Summer 20%", new PromotionPriceCheck("80", "100", 20, " Summer 20% ").promotionName);
        assertEquals("", new PromotionPriceCheck("80", "100", 20, null).promotionName);
        assertEquals("", new PromotionPriceCheck("80", "100", 20).promotionName);
    }

    @Test public void acceptsPricesStrictlyInsideTolerance() {
        for (String price : new String[]{"70.01", "80", "83", "89.99"}) {
            assertFalse(price, new PromotionPriceCheck(price, "100", 20).outsideRange());
        }
    }

    @Test public void rejectsBothBoundariesAndOutsidePrices() {
        for (String price : new String[]{"69.99", "70", "90", "90.01", "100"}) {
            assertTrue(price, new PromotionPriceCheck(price, "100", 20).outsideRange());
        }
        assertTrue(new PromotionPriceCheck("80", "180", 20).outsideRange());
    }

    @Test public void oldPriceOrderErrorTakesPriority() {
        assertFalse(new PromotionPriceCheck("101", "100", 20).outsideRange());
    }

    @Test public void missingPricesAndMissingDiscountDoNotBlockClearingOrFirstEntry() {
        for (String missing : new String[]{null, "", "0", "0.00", "bad", "-1"}) {
            assertFalse(new PromotionPriceCheck(missing, "100", 20).outsideRange());
            assertFalse(new PromotionPriceCheck("80", missing, 20).outsideRange());
        }
        for (int percent : new int[]{0, -1, 101}) {
            assertFalse(new PromotionPriceCheck("10", "100", percent).outsideRange());
        }
    }

    @Test public void usesDecimalArithmeticWithoutRoundingBounds() {
        assertFalse(new PromotionPriceCheck(" 83,00 ", "100,00", 20).outsideRange());
        assertTrue(new PromotionPriceCheck("0.07", "0.10", 20).outsideRange());
        assertFalse(new PromotionPriceCheck("0.08", "0.10", 20).outsideRange());
        assertTrue(new PromotionPriceCheck("0.09", "0.10", 20).outsideRange());
    }

    @Test public void approvalAppliesOnlyToThePricesAndPercentageShown() {
        PromotionPriceCheck approved = new PromotionPriceCheck("80", "180", 20);
        assertTrue(approved.matches(new PromotionPriceCheck("80", "180", 20)));
        assertFalse(approved.matches(new PromotionPriceCheck("81", "180", 20)));
        assertFalse(approved.matches(new PromotionPriceCheck("80", "181", 20)));
        assertFalse(approved.matches(new PromotionPriceCheck("80", "180", 30)));
        assertFalse(approved.matches(null));
    }
}
