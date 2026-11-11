package ua.com.merchik.merchik.Options.Controls;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers.PromotionPriceRange;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers.PriceDisplay.cents;

/** Report control 175121; unlike the price editor, the report accepts both corridor endpoints. */
final class PrePromotionPricePolicy {
    enum Issue {
        NONE, MISSING_CURRENT, MISSING_BEFORE, ABOVE_MIN, ABOVE_MAX, EQUAL_TO_MAX,
        MISSING_PROMOTION_PRESENCE, OUTSIDE_CORRIDOR_DELTA;

        boolean currentPriceFound() {
            return this != MISSING_CURRENT;
        }
    }

    static final class Evaluation {
        final Issue issue;
        final BigDecimal planned, delta, lower, upper;
        final boolean photoExceptionApplied;

        private Evaluation(Issue issue) {
            this(issue, null, null, null, null, false);
        }

        private Evaluation(Issue issue, BigDecimal planned, BigDecimal delta, BigDecimal lower, BigDecimal upper,
                           boolean photoExceptionApplied) {
            this.issue = issue;
            this.planned = planned;
            this.delta = delta;
            this.lower = lower;
            this.upper = upper;
            this.photoExceptionApplied = photoExceptionApplied;
        }

        String corridorDiagnostic() {
            if (planned == null) return "corridorChecked=false";
            return "corridorChecked=true, planned=" + planned.toPlainString() + ", delta=" + delta.toPlainString()
                    + ", lower=" + lower.toPlainString() + ", upper=" + upper.toPlainString();
        }
    }

    private static final long START = LocalDate.of(2026, 11, 2)
            .atStartOfDay(ZoneId.of("Europe/Kiev")).toInstant().toEpochMilli();
    private static final long PRESENCE_START = LocalDate.of(2026, 11, 7)
            .atStartOfDay(ZoneId.of("Europe/Kiev")).toInstant().toEpochMilli();

    enum PromotionCheck { NOT_APPLICABLE, PRESENCE_NOT_SET, CHECK_PRICE }

    static PromotionCheck promotionCheck(Date documentDate, String typeId, String presence) {
        String mark = presence == null ? "" : presence.trim();
        if (documentDate == null || documentDate.getTime() < START || typeId == null
                || typeId.trim().isEmpty() || "0".equals(typeId.trim()) || "2".equals(mark))
            return PromotionCheck.NOT_APPLICABLE;
        return documentDate.getTime() >= PRESENCE_START && (mark.isEmpty() || "0".equals(mark))
                ? PromotionCheck.PRESENCE_NOT_SET : PromotionCheck.CHECK_PRICE;
    }

    static Evaluation evaluate(String price, String priceMin, String priceMax, PromotionCheck promotion, int percent) {
        return evaluate(price, priceMin, priceMax, promotion, percent, false);
    }

    static Evaluation evaluate(String price, String priceMin, String priceMax, PromotionCheck promotion, int percent,
                               boolean hasProductPhoto) {
        BigDecimal current = money(price);
        // The final independent price=0 check in 1C overrides any earlier violation.
        if (current.signum() <= 0) return new Evaluation(Issue.MISSING_CURRENT);
        BigDecimal min = money(priceMin);
        BigDecimal max = money(priceMax);
        if (min.signum() <= 0 || max.signum() <= 0) return new Evaluation(Issue.MISSING_BEFORE);
        if (current.compareTo(min) > 0) return new Evaluation(Issue.ABOVE_MIN);
        if (current.compareTo(max) > 0) return new Evaluation(Issue.ABOVE_MAX);
        if (promotion == PromotionCheck.NOT_APPLICABLE) return new Evaluation(Issue.NONE);
        if (current.compareTo(max) == 0) return new Evaluation(Issue.EQUAL_TO_MAX);
        if (promotion == PromotionCheck.PRESENCE_NOT_SET) return new Evaluation(Issue.MISSING_PROMOTION_PRESENCE);
        if (percent > 0) {
            PromotionPriceRange range = new PromotionPriceRange(max, percent);
            boolean outside = range.excludes(current);
            // Type 28 evidence is an exception only for the corridor check, not earlier violations.
            return new Evaluation(outside && !hasProductPhoto ? Issue.OUTSIDE_CORRIDOR_DELTA : Issue.NONE,
                    range.planned, range.delta, range.lower, range.upper, outside && hasProductPhoto);
        }
        return new Evaluation(Issue.NONE);
    }

    static String plannedPriceText(String priceMax, int percent) {
        return new PromotionPriceRange(money(priceMax), percent).planned.toPlainString();
    }

    private static BigDecimal money(String value) {
        try {
            return value == null ? BigDecimal.ZERO : cents(new BigDecimal(value.trim().replace(',', '.')));
        } catch (NumberFormatException ignored) {
            return BigDecimal.ZERO;
        }
    }

    private PrePromotionPricePolicy() {}
}
