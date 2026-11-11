package ua.com.merchik.merchik.Options.Controls;

import java.math.BigDecimal;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers.PriceDisplay;

final class PrePromotionPriceMessage {
    static final int TRANSLATION_ID = 10624;
    static final String DEFAULT_TEMPLATE = "Для товара є акція зі знижкою {discount_percent}% Ви вказали АКЦІЙНУ ціну {price} грн. "
            + "що СУТТЄВО відрізняється від ПЛАНОВАНОЇ ціни акції: {price_plan} грн. "
            + "Або правильно зазначте ціни, або зробіть ФОТО акційного товару "
            + "у якості ПІДТВЕРДЖЕННЯ того, що акційна ціна виходить за межі цінового диапазону: "
            + "({price_min} - {price_max} грн).";
    private static final String[] PARAMETERS = {
            "{discount_percent}", "{price}", "{price_plan}", "{price_min}", "{price_max}"
    };

    private PrePromotionPriceMessage() {}

    static String corridor(String template, String price, int percent, PrePromotionPricePolicy.Evaluation result) {
        // A cached older translation must not hide the new photo alternative and range parameters.
        for (String parameter : PARAMETERS) {
            if (template == null || !template.contains(parameter)) {
                template = DEFAULT_TEMPLATE;
                break;
            }
        }
        return template.replace("{discount_percent}", String.valueOf(percent))
                .replace("{price}", PriceDisplay.format(price))
                .replace("{price_plan}", result.planned.toPlainString())
                .replace("{price_min}", bound(result.lower))
                .replace("{price_max}", bound(result.upper));
    }

    private static String bound(BigDecimal value) {
        // Preserve a half-cent boundary instead of advertising a rounded but invalid price.
        BigDecimal exact = value.stripTrailingZeros();
        return (exact.scale() < 2 ? exact.setScale(2) : exact).toPlainString();
    }
}
