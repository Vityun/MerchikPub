package ua.com.merchik.merchik.features.main.options;

import org.junit.Test;

import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

import static org.junit.Assert.*;

public class PhotoReportMinimumTest {
    @Test public void counterAndCaptureResolveTheSameMinimum() {
        WpDataDB visit = new WpDataDB();
        for (int sku : new int[]{0, 4, 5, 100}) {
            visit.setSku(sku);
            for (String optionId : new String[]{"158309", "158308", "135809", "164355", "132969",
                    "169108", "174213", "158604", "158605", "172100"}) {
                for (String minimum : new String[]{null, "", "0", "1", "3", "5"}) {
                    OptionsDB option = option(optionId, minimum);
                    assertEquals("option=" + optionId + ", sku=" + sku + ", minimum=" + minimum,
                            PhotoReportMinimum.requiredCount(visit, 10, option, null),
                            PhotoReportMinimum.requiredCount(visit, 10, option, null, true));
                }
            }
        }
    }

    @Test public void configuredWarehouseMinimumIsNotReplacedByDefault() {
        WpDataDB visit = new WpDataDB();
        visit.setSku(100);
        assertEquals(5, PhotoReportMinimum.requiredCount(visit, 10, option("141360", "5"), null));
    }

    @Test public void exactMinimumDoesNotRequireAnyDatabaseOrOption() {
        assertEquals(4, PhotoReportMinimum.requiredCount(null, 0, null, 4));
        assertEquals(0, PhotoReportMinimum.requiredCount(null, 0, null, -1));
    }

    @Test(expected = NumberFormatException.class)
    public void invalidMinimumIsNotTreatedAsCompleted() {
        PhotoReportMinimum.requiredCount(new WpDataDB(), 10, option("158309", "invalid"), null, true);
    }

    private OptionsDB option(String id, String minimum) {
        OptionsDB option = new OptionsDB();
        option.setOptionId(id);
        option.setOptionControlId("158608");
        option.setAmountMin(minimum);
        return option;
    }
}
