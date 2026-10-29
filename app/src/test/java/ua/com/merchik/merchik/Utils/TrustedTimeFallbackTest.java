package ua.com.merchik.merchik.Utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class TrustedTimeFallbackTest {
    @Test public void usesLocalSecondsBeforeFirstServerTime() {
        assertNull(TrustedTime.nowServerSecOrNull());
        long before = System.currentTimeMillis() / 1000L;
        long now = TrustedTime.nowServerSecOrLocalSec();
        assertTrue(now >= before);
        assertTrue(now <= System.currentTimeMillis() / 1000L);
    }

    @Test public void firstSuccessfulExchangeCanAdvanceVpiWithoutServerTime() {
        long before = System.currentTimeMillis() / 1000L - 120;
        long vpi = TrustedTime.syncWatermarkSec(0, 120);
        assertTrue(vpi >= before);
        assertTrue(vpi <= System.currentTimeMillis() / 1000L - 120);
    }
}
