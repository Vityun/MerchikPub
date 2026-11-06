package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import java.math.BigDecimal;

/** Bounds shared by report control 175121 and price entry. Only the plan and delta are rounded. */
public final class PromotionPriceRange {
    public final BigDecimal planned, delta, lower, upper;

    public PromotionPriceRange(BigDecimal before, int percent) {
        before = PriceDisplay.cents(before);
        BigDecimal discount = before.multiply(BigDecimal.valueOf(percent)).movePointLeft(2);
        planned = PriceDisplay.cents(before.subtract(discount));
        delta = PriceDisplay.cents(discount);
        lower = planned.subtract(delta.multiply(BigDecimal.valueOf(2.5)));
        upper = planned.add(delta.divide(BigDecimal.valueOf(2)));
    }

    public boolean excludes(BigDecimal current) {
        current = PriceDisplay.cents(current);
        return current.compareTo(lower) < 0 || current.compareTo(upper) > 0;
    }
}
