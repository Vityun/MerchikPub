package ua.com.merchik.merchik.Options.Controls;

import org.junit.Test;
import static org.junit.Assert.*;

public class StockBalancePolicyTest {
    @Test public void cutoffIsTwentyDaysNotMinutes() {
        long now = 1794394800123L;
        assertEquals(20L * 86400, now / 1000 - StockBalancePolicy.cutoffSeconds(now));
        assertTrue(now / 1000 - 86400 > StockBalancePolicy.cutoffSeconds(now));
        assertTrue(now / 1000 - 21L * 86400 < StockBalancePolicy.cutoffSeconds(now));
    }

    @Test public void averagePreservesFractionAndLargeSum() {
        assertEquals(2.5, StockBalancePolicy.average(5, 2), 0.0);
        assertEquals(2147483647.0, StockBalancePolicy.average(4294967294L, 2), 0.0);
        assertEquals(0.0, StockBalancePolicy.average(0, 0), 0.0);
    }

    @Test public void actualStockIsComparedToMinimumNotPresenceFlag() {
        assertFalse(StockBalancePolicy.belowMinimum(107, 20, 1000));
        assertTrue(StockBalancePolicy.belowMinimum(19, 20, 1000));
        assertFalse(StockBalancePolicy.belowMinimum(20, 20, 1000));
    }

    @Test public void tenthOfAverageIsNotTruncated() {
        assertTrue(StockBalancePolicy.belowMinimum(1, 0, 19.5));
        assertFalse(StockBalancePolicy.belowMinimum(2, 0, 19.5));
    }
}
