package ua.com.merchik.merchik.Utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class CountValueTest {
    @Test public void fractionsAreRoundedNotConcatenated() {
        assertEquals("18", CountValue.input("17.9"));
        assertEquals("18", CountValue.input("17,5"));
        assertEquals("17", CountValue.input("17.49"));
        assertEquals("19", CountValue.step("17.9", 1));
        assertEquals("17", CountValue.step("17.9", -1));
    }

    @Test public void missingAndMalformedAreDifferent() {
        assertEquals(0, CountValue.rounded(null));
        assertEquals(0, CountValue.rounded(" "));
        assertEquals("", CountValue.input(null));
        for (String value : new String[]{"bad", "NaN", "Infinity", "17..9", "17,9.1"}) {
            assertThrows(NumberFormatException.class, () -> CountValue.rounded(value));
        }
    }

    @Test public void negativeInputIncludingSubUnitIsRejected() {
        for (String value : new String[]{"-1", "-0.1", "-17,9"}) {
            assertThrows(NumberFormatException.class, () -> CountValue.input(value));
            assertThrows(NumberFormatException.class, () -> CountValue.draft(value));
            assertThrows(NumberFormatException.class, () -> CountValue.nonNegative(value));
        }
    }

    @Test public void draftKeepsDecimalPoint() {
        assertEquals("17.", CountValue.draft("17."));
        assertEquals("17.9", CountValue.draft("17,9"));
        assertEquals("0.", CountValue.draft(","));
        for (String value : new String[]{"1x2", "1e2", "1 2", "+12"}) {
            assertThrows(NumberFormatException.class, () -> CountValue.draft(value));
        }
    }

    @Test public void checksBoundsBeforeSavingOrStepping() {
        assertEquals(2147483647, CountValue.nonNegative("2147483647.49"));
        assertThrows(NumberFormatException.class, () -> CountValue.nonNegative("2147483647.5"));
        assertThrows(NumberFormatException.class, () -> CountValue.nonNegative("2147483648"));
        assertThrows(ArithmeticException.class, () -> CountValue.step("2147483647", 1));
        assertEquals("0", CountValue.step("0", -1));
    }
}
