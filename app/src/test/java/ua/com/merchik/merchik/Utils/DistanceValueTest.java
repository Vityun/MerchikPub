package ua.com.merchik.merchik.Utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class DistanceValueTest {
    @Test public void convertsBeforeRounding() {
        assertEquals(1900, DistanceValue.metres(1.9f, true));
        assertEquals(12, DistanceValue.metres(12.49, false));
        assertEquals(13, DistanceValue.metres(12.5, false));
        assertEquals(0, DistanceValue.metres(0, false));
    }

    @Test public void invalidDistanceIsUnknownNotAtShop() {
        for (double value : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY, Double.MAX_VALUE}) {
            assertEquals(-1, DistanceValue.metres(value, true));
        }
    }
}
