package ua.com.merchik.merchik.Utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class FaceValueTest {
    @Test public void inputLimitIncludes999AndDoesNotAffectSmallerCounts() {
        for (String value : new String[]{null, "", " ", "0", "17.9", "998.9", "999", "999.0", "999,00", " 0999.000 "}) {
            assertFalse(value, FaceValue.exceedsInputLimit(value));
        }
        assertEquals("999", FaceValue.input("998.9"));
    }

    @Test public void inputLimitRejectsExcessBeforeRounding() {
        for (String value : new String[]{"1000", "5000", "999.01", "999,49", "999.5", "999.999", "1e3"}) {
            assertTrue(value, FaceValue.exceedsInputLimit(value));
        }
    }

    @Test public void oversizedPastedNumbersAreRejectedWithoutIntegerOverflow() {
        assertTrue(FaceValue.exceedsInputLimit("2147483648"));
        assertTrue(FaceValue.exceedsInputLimit("9999999999999999999999999999999999999999"));
    }

    @Test public void plusAtLimitIsRejectedRatherThanClamped() {
        assertFalse(FaceValue.exceedsInputLimit(FaceValue.step("998", 1)));
        assertTrue(FaceValue.exceedsInputLimit(FaceValue.step("999", 1)));
        assertEquals("998", FaceValue.step("999", -1));
    }

    @Test public void existingServerValuesAreStillReadableAndOtherCountsAreNotLimited() {
        assertEquals(5000, FaceValue.rounded("5000.0"));
        assertEquals("5000", FaceValue.display("5000"));
        assertEquals("5000", CountValue.input("5000"));
    }

    @Test public void inputBoundDoesNotReplaceMalformedValueValidation() {
        for (String value : new String[]{"bad", "999..9", "NaN", "Infinity"}) {
            assertFalse(FaceValue.exceedsInputLimit(value));
            assertThrows(NumberFormatException.class, () -> FaceValue.input(value));
        }
    }

    @Test public void negativeFaceCannotBypassCompletedVisitGuard() {
        assertThrows(NumberFormatException.class, () -> FaceValue.input("-1"));
        assertThrows(NumberFormatException.class, () -> FaceValue.draft("-1"));
        assertThrows(NumberFormatException.class, () -> FaceValue.resetsToZero("5", "-1"));
        assertThrows(NumberFormatException.class, () -> FaceValue.resetsToZero("5", "-0.1"));
        assertTrue(FaceValue.resetsToZero("-1", "0"));
    }

    @Test public void roundsHalfUpInsteadOfTruncating() {
        assertEquals(18, FaceValue.rounded("17.9"));
        assertEquals(18, FaceValue.rounded("17.5"));
        assertEquals(17, FaceValue.rounded("17.499"));
        assertEquals(18, FaceValue.rounded("17,9"));
        assertEquals(18, FaceValue.rounded(" 17.90 "));
        assertEquals(18, FaceValue.rounded("18.0"));
    }

    @Test public void roundsSmallAndNegativeValuesMathematically() {
        assertEquals(0, FaceValue.rounded("0.49"));
        assertEquals(1, FaceValue.rounded("0.5"));
        assertEquals(1, FaceValue.rounded("0.9"));
        assertEquals(-18, FaceValue.rounded("-17.5"));
        assertEquals(-17, FaceValue.rounded("-17.4"));
    }

    @Test public void missingValueIsNotTheSameAsMalformedValue() {
        assertEquals(0, FaceValue.rounded(null));
        assertEquals(0, FaceValue.rounded(" "));
        assertEquals("", FaceValue.input(null));
        assertEquals("", FaceValue.input(" "));
        for (String value : new String[]{"bad", "17..9", "17,9.1", "NaN", "Infinity", "2147483647.5"}) {
            assertThrows(value, NumberFormatException.class, () -> FaceValue.rounded(value));
        }
    }

    @Test public void editorDoesNotRemoveTheDecimalSeparator() {
        assertEquals("18", FaceValue.input("17.9"));
        assertEquals("18", FaceValue.input("17,9"));
        assertEquals("17", FaceValue.input("17."));
        assertEquals("18", FaceValue.display("17.9"));
        assertEquals("bad", FaceValue.display("bad"));
    }

    @Test public void steppingStartsFromRoundedCount() {
        assertEquals("19", FaceValue.step("17.9", 1));
        assertEquals("17", FaceValue.step("17.9", -1));
        assertEquals("18", FaceValue.step("17.4", 1));
        assertEquals("0", FaceValue.step("0.4", -1));
        assertEquals("1", FaceValue.step("", 1));
        assertEquals("1", FaceValue.step(",", 1));
        assertThrows(NumberFormatException.class, () -> FaceValue.step("bad", 1));
        assertThrows(ArithmeticException.class, () -> FaceValue.step("2147483647", 1));
    }

    @Test public void partialInputKeepsSeparatorUntilUserFinishesTyping() {
        assertEquals("17.", FaceValue.draft("17."));
        assertEquals("17.9", FaceValue.draft("17,9"));
        assertEquals("0.", FaceValue.draft(","));
        assertEquals("0", FaceValue.input(FaceValue.draft(",")));
        assertEquals("18", FaceValue.input(FaceValue.draft("17.9")));
        assertThrows(NumberFormatException.class, () -> FaceValue.draft("17..9"));
    }

    @Test public void decimalOldValueCannotBypassZeroResetProtection() {
        assertTrue(FaceValue.resetsToZero("17.9", "0"));
        assertTrue(FaceValue.resetsToZero("17,9", "0.4"));
        assertTrue(FaceValue.resetsToZero("17.9", ""));
        assertFalse(FaceValue.resetsToZero("17.9", "0.5"));
        assertFalse(FaceValue.resetsToZero("17.9", "18"));
        assertFalse(FaceValue.resetsToZero(null, "0"));
        assertThrows(NumberFormatException.class, () -> FaceValue.resetsToZero("bad", "0"));
    }
}
