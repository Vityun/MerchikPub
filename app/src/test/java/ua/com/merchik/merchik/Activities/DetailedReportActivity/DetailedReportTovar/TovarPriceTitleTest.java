package ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import ua.com.merchik.merchik.data.TovarOptions;

import static org.junit.Assert.assertEquals;
import static ua.com.merchik.merchik.Globals.OptionControlName.PRICE;
import static ua.com.merchik.merchik.Globals.OptionControlName.PRICE_BEFORE_PROMOTION;

public class TovarPriceTitleTest {
    private final TovarOptions price = new TovarOptions(PRICE, "P", "Price", "price", "main", 579, 174974);
    private final TovarOptions before = new TovarOptions(PRICE_BEFORE_PROMOTION, "@", "Before promotion", "price_before_promotion", "main", 175121);

    @Test public void renamesCurrentPriceWhenBothPricesAreRequired() {
        assertEquals("\u041f\u041e\u0422\u041e\u0427\u041d\u0410 \u0446\u0435\u043d\u0430 \u0442\u043e\u0432\u0430\u0440\u0430",
                TovarRequisites.optionTitle(price, Arrays.asList(price, before)));
    }

    @Test public void keepsTitleWhenOnlyCurrentPriceIsRequired() {
        assertEquals("Price", TovarRequisites.optionTitle(price, Collections.singletonList(price)));
    }

    @Test public void keepsTitleWhenOnlyBeforePromotionPriceIsRequired() {
        assertEquals("Price", TovarRequisites.optionTitle(price, Collections.singletonList(before)));
    }

    @Test public void keepsTitleWithoutRequiredOptions() {
        assertEquals("Price", TovarRequisites.optionTitle(price, Collections.emptyList()));
        assertEquals("Price", TovarRequisites.optionTitle(price, null));
    }

    @Test public void doesNotRenameBeforePromotionPrice() {
        assertEquals("Before promotion", TovarRequisites.optionTitle(before, Arrays.asList(price, before)));
    }

    @Test public void doesNotChangeSharedTemplateOrTitleForNextVisit() {
        TovarRequisites.optionTitle(price, Arrays.asList(before, price));
        assertEquals("Price", price.getOptionLong());
        assertEquals("Price", TovarRequisites.optionTitle(price, Collections.singletonList(price)));
    }

    @Test public void missingVisitKeepsOriginalTitleWithoutDatabaseLookup() {
        assertEquals("Price", TovarRequisites.optionTitleForVisit(price, null));
        assertEquals("Price", TovarRequisites.optionTitleForVisit(price, " "));
    }
}
