package ua.com.merchik.merchik.dialogs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BarcodeCopyLinkTest {
    @Test
    public void findsOnlyBarcodeInWeightRow() {
        String text = "80 ml, 0012345678905";
        int start = BarcodeCopyLink.findBarcodeStart(text, "0012345678905", 0);
        assertEquals(7, start);
        assertEquals("0012345678905", text.substring(start));
    }

    @Test
    public void findsBarcodeInsideDescription() {
        assertEquals(9, BarcodeCopyLink.findBarcodeStart(
                "Barcode: 0012345678905\nStock: 5", "0012345678905", 0));
    }

    @Test
    public void findsStandaloneBarcode() {
        assertEquals(0, BarcodeCopyLink.findBarcodeStart("000123", "000123", 0));
    }

    @Test
    public void acceptsPunctuationAroundBarcode() {
        assertEquals(1, BarcodeCopyLink.findBarcodeStart("(000123),", "000123", 0));
    }

    @Test
    public void doesNotMatchPartOfLongerCode() {
        assertEquals(-1, BarcodeCopyLink.findBarcodeStart("1000123 0001234", "000123", 0));
        assertEquals(-1, BarcodeCopyLink.findBarcodeStart("A000123 000123B", "000123", 0));
    }

    @Test
    public void skipsPartialMatchAndFindsFullBarcode() {
        assertEquals(8, BarcodeCopyLink.findBarcodeStart("1000123 000123", "000123", 0));
    }

    @Test
    public void findsRepeatedBarcodeWithoutLooping() {
        String text = "000123 / 000123";
        assertEquals(0, BarcodeCopyLink.findBarcodeStart(text, "000123", 0));
        assertEquals(9, BarcodeCopyLink.findBarcodeStart(text, "000123", 6));
        assertEquals(-1, BarcodeCopyLink.findBarcodeStart(text, "000123", 15));
    }

    @Test
    public void ignoresMissingBarcodeAndDescription() {
        assertEquals(-1, BarcodeCopyLink.findBarcodeStart(null, "000123", 0));
        assertEquals(-1, BarcodeCopyLink.findBarcodeStart("80 ml, null", null, 0));
        assertEquals(-1, BarcodeCopyLink.findBarcodeStart("80 ml, ", "", 0));
        assertEquals(-1, BarcodeCopyLink.findBarcodeStart("80 ml, ", " ", 0));
        assertEquals(-1, BarcodeCopyLink.findBarcodeStart("80 ml", "000123", 0));
    }
}
