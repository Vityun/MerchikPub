package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PriceDisplayTest {
    @Test public void showsExactlyTwoDecimalPlaces() {
        assertEquals("10.00", PriceDisplay.format("10"));
        assertEquals("5.60", PriceDisplay.format("5.6"));
        assertEquals("95.90", PriceDisplay.format("95.90"));
        assertEquals("95.90", PriceDisplay.format(" 95,9 "));
        assertEquals("0.00", PriceDisplay.format("0"));
    }

    @Test public void roundsToCentsWithoutBinaryFloatingPoint() {
        assertEquals("10.03", PriceDisplay.format("10.025"));
        assertEquals("95.90", PriceDisplay.format("95.899999"));
        assertEquals("95.90", PriceDisplay.format("95.904"));
        assertEquals("95.91", PriceDisplay.format("95.905"));
    }

    @Test public void handlesMissingAndInvalidValues() {
        assertEquals("0.00", PriceDisplay.format(null));
        assertEquals("0.00", PriceDisplay.format(" "));
        assertEquals("bad", PriceDisplay.format("bad"));
    }

    @Test public void untouchedEditorPreservesOriginalStorageValue() {
        assertEquals("10", PriceDisplay.editorResult("10.00", "10"));
        assertEquals("5.6", PriceDisplay.editorResult("5.60", "5.6"));
        assertEquals("95.899999", PriceDisplay.editorResult("95.90", "95.899999"));
        assertEquals("12", PriceDisplay.editorResult("12", "10"));
        assertEquals("", PriceDisplay.editorResult("", "10"));
        assertEquals("0.00", PriceDisplay.editorResult("0.00", null));
    }
}
