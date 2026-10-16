package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Immutable values shown to the user, also used to scope a one-time approval. */
public final class PromotionPriceCheck {
    public final String price;
    public final String priceBefore;
    public final int discountPercent;
    public final String promotionName;

    public PromotionPriceCheck(String price, String priceBefore, int discountPercent) {
        this(price, priceBefore, discountPercent, "");
    }

    public PromotionPriceCheck(String price, String priceBefore, int discountPercent, String promotionName) {
        this.price = price;
        this.priceBefore = priceBefore;
        this.discountPercent = discountPercent;
        this.promotionName = promotionName == null ? "" : promotionName.trim();
    }

    public boolean outsideRange() {
        BigDecimal current = positive(price);
        BigDecimal before = positive(priceBefore);
        if (current == null || before == null || discountPercent <= 0 || discountPercent > 100
                || PriceDisplay.cents(current).compareTo(PriceDisplay.cents(before)) >= 0) return false;
        current = PriceDisplay.cents(current);
        before = PriceDisplay.cents(before);
        BigDecimal discount = before.multiply(BigDecimal.valueOf(discountPercent)).movePointLeft(2);
        BigDecimal planned = before.subtract(discount);
        BigDecimal tolerance = discount.divide(BigDecimal.valueOf(2));
        return current.compareTo(planned.subtract(tolerance)) <= 0
                || current.compareTo(planned.add(tolerance)) >= 0;
    }

    private BigDecimal expectedDiscount() {
        BigDecimal before = positive(priceBefore);
        if (before == null || discountPercent <= 0 || discountPercent > 100) return BigDecimal.ZERO;
        return before.multiply(BigDecimal.valueOf(discountPercent)).movePointLeft(2);
    }

    public String expectedDiscountText() {
        return expectedDiscount().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public String actualDiscountText() {
        BigDecimal before = positive(priceBefore);
        BigDecimal current = positive(price);
        if (before == null || current == null) return "0";
        return before.subtract(current).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public boolean matches(PromotionPriceCheck other) {
        return other != null && discountPercent == other.discountPercent
                && Objects.equals(price, other.price) && Objects.equals(priceBefore, other.priceBefore);
    }

    private static BigDecimal positive(String value) {
        if (value == null) return null;
        try {
            BigDecimal price = new BigDecimal(value.trim().replace(',', '.'));
            return price.signum() > 0 ? price : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
