package ua.com.merchik.merchik.retrofit;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

public class FlexibleIntegerTypeAdapterTest {
    @Test public void acceptsExactIntegersAndIntegralDecimalNotation() throws IOException {
        FlexibleIntegerTypeAdapter adapter = new FlexibleIntegerTypeAdapter(0);
        for (String input : new String[]{"17", "17.0", "\"17.00\"", "\" 17 \"", "1.7e1"}) {
            assertEquals(Integer.valueOf(17), adapter.fromJson(input));
        }
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), adapter.fromJson("2147483647"));
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), adapter.fromJson("-2147483648"));
    }

    @Test public void doesNotConvertBadNumbersToZeroOrNull() {
        for (Integer fallback : new Integer[]{0, null}) {
            FlexibleIntegerTypeAdapter adapter = new FlexibleIntegerTypeAdapter(fallback);
            for (String input : new String[]{"17.9", "\"17.9\"", "\"17,9\"", "2147483648", "-2147483649", "\"NaN\"", "\"bad\"", "{}", "[]"}) {
                assertThrows(input, IOException.class, () -> adapter.fromJson(input));
            }
        }
    }

    @Test public void preservesMissingValueContract() throws IOException {
        for (String input : new String[]{"null", "\"\"", "\"null\""}) {
            assertEquals(Integer.valueOf(0), new FlexibleIntegerTypeAdapter(0).fromJson(input));
            assertNull(new FlexibleIntegerTypeAdapter(null).fromJson(input));
        }
    }

    @Test public void preservesBooleanContract() throws IOException {
        assertEquals(Integer.valueOf(1), new FlexibleIntegerTypeAdapter(0).fromJson("true"));
        assertEquals(Integer.valueOf(0), new FlexibleIntegerTypeAdapter(0).fromJson("false"));
    }
}
