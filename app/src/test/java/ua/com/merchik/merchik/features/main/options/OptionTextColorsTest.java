package ua.com.merchik.merchik.features.main.options;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OptionTextColorsTest {
    private static final int GREEN = 0xFF00FF00;
    private static final int RED = 0xFFEF5350;
    private static final int YELLOW = 0xFFFFF200;

    @Test public void titleKeepsTheExistingStandardColor() {
        assertEquals(0xFF424242, OptionTextColors.DEFAULT_TEXT);
    }

    @Test public void neutralResultKeepsThePreviousGray() {
        assertEquals(0xFF888888, OptionTextColors.NEUTRAL_COUNTER);
    }

    @Test public void visibleGreenSignalMakesResultGreen() {
        assertEquals(GREEN, OptionTextColors.counterColor(true, GREEN, GREEN, RED));
    }

    @Test public void visibleRedSignalMakesResultRed() {
        assertEquals(RED, OptionTextColors.counterColor(true, RED, GREEN, RED));
    }

    @Test public void yellowSignalUsesLightGrayResultNotTitleColor() {
        assertEquals(OptionTextColors.NEUTRAL_COUNTER, OptionTextColors.counterColor(true, YELLOW, GREEN, RED));
    }

    @Test public void grayAndUnknownSignalsUseLightGrayResult() {
        for (int tint : new int[]{0xFF888888, 0xFFB1B1B1, 0, 0xFFFFFF00}) {
            assertEquals(OptionTextColors.NEUTRAL_COUNTER, OptionTextColors.counterColor(true, tint, GREEN, RED));
        }
    }

    @Test public void hiddenSignalCannotColorTheResult() {
        for (int tint : new int[]{GREEN, RED, YELLOW}) {
            assertEquals(OptionTextColors.NEUTRAL_COUNTER, OptionTextColors.counterColor(false, tint, GREEN, RED));
        }
    }

    @Test public void paletteComesFromPassedResourceColors() {
        int green = 0xFF10BB20, red = 0xFFED5052;
        assertEquals(green, OptionTextColors.counterColor(true, green, green, red));
        assertEquals(red, OptionTextColors.counterColor(true, red, green, red));
        assertEquals(OptionTextColors.NEUTRAL_COUNTER, OptionTextColors.counterColor(true, RED, green, red));
    }
}
