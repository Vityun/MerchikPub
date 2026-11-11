package ua.com.merchik.merchik.Options.Controls;

import org.junit.Test;

import static org.junit.Assert.*;
import static ua.com.merchik.merchik.Options.Controls.PrePromotionPricePolicy.*;

public class PrePromotionPriceMessageTest {
    @Test public void translation10624UsesCalculatedBoundsNotStoredBeforePrices() {
        Evaluation result = evaluate("95", "99", "100", PromotionCheck.CHECK_PRICE, 20);
        String text = PrePromotionPriceMessage.corridor(null, "95", 20, result);
        assertEquals(10624, PrePromotionPriceMessage.TRANSLATION_ID);
        assertEquals("Для товара є акція зі знижкою 20% Ви вказали АКЦІЙНУ ціну 95.00 грн. що СУТТЄВО відрізняється "
                + "від ПЛАНОВАНОЇ ціни акції: 80.00 грн. Або правильно зазначте ціни, "
                + "або зробіть ФОТО акційного товару у якості ПІДТВЕРДЖЕННЯ того, що акційна ціна "
                + "виходить за межі цінового диапазону: (30.00 - 90.00 грн).", text);
        assertFalse(text.contains("{"));
    }

    @Test public void keepsUpdatedTranslationAndSubstitutesEveryParameter() {
        Evaluation result = evaluate("95", "100", "100", PromotionCheck.CHECK_PRICE, 20);
        String template = "{price_min}|{price_max}|{price_plan}|{price}|{discount_percent}|{price}";
        assertEquals("30.00|90.00|80.00|95.00|20|95.00",
                PrePromotionPriceMessage.corridor(template, "95,0", 20, result));
    }

    @Test public void staleIncompleteOrEmptyTranslationFallsBackToNewMessage() {
        Evaluation result = evaluate("95", "100", "100", PromotionCheck.CHECK_PRICE, 20);
        String expected = PrePromotionPriceMessage.corridor(PrePromotionPriceMessage.DEFAULT_TEMPLATE, "95", 20, result);
        for (String old : new String[]{null, "", " ", "{price} / {discount_percent}% / {price_plan}"}) {
            assertEquals(expected, PrePromotionPriceMessage.corridor(old, "95", 20, result));
        }
    }

    @Test public void doesNotRoundUpperHalfCentIntoAnInvalidPrice() {
        Evaluation result = evaluate("35.07", "37.90", "37.90", PromotionCheck.CHECK_PRICE, 15);
        String text = PrePromotionPriceMessage.corridor(null, "35.07", 15, result);
        assertTrue(text.contains("ціни акції: 32.22 грн."));
        assertTrue(text.contains("(17.995 - 35.065 грн)"));
    }
}
