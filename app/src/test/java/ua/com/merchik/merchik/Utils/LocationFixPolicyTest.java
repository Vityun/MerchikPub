package ua.com.merchik.merchik.Utils;

import org.junit.Test;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;

import static org.junit.Assert.*;

public class LocationFixPolicyTest {
    private static final long NOW = 1794393502703L;

    private String reason(long measuredAt) {
        return LocationFixPolicy.rejection(50.519516, 30.785419, measuredAt, NOW, false);
    }

    @Test public void actualFutureGpsFromIncidentFallsBackToNetwork() {
        String gps = LocationFixPolicy.rejection(-12.030764180235565, -77.05237668938935,
                1836273819000L, NOW, false);
        assertEquals("future_time", gps);
        assertEquals(2, LocationFixPolicy.preferredProvider(gps, reason(NOW - 2000)));
    }

    @Test public void missingGpsDoesNotBlockNetwork() {
        assertEquals(2, LocationFixPolicy.preferredProvider("missing_location", reason(NOW)));
    }

    @Test public void staleGpsDoesNotBlockNetwork() {
        assertEquals("stale_time", reason(NOW - LocationFixPolicy.MAX_AGE_MS - 1));
        assertEquals(2, LocationFixPolicy.preferredProvider(
                reason(NOW - LocationFixPolicy.MAX_AGE_MS - 1), reason(NOW - 1)));
    }

    @Test public void mockGpsDoesNotBlockRealNetwork() {
        String gps = LocationFixPolicy.rejection(50.52, 30.78, NOW, NOW, true);
        assertEquals("mock_location", gps);
        assertEquals(2, LocationFixPolicy.preferredProvider(gps, reason(NOW)));
    }

    @Test public void validGpsKeepsExistingPriority() {
        assertEquals(1, LocationFixPolicy.preferredProvider(reason(NOW), reason(NOW)));
        assertEquals(1, LocationFixPolicy.preferredProvider(reason(NOW), "missing_location"));
    }

    @Test public void noUsableProviderDoesNotInventMeasurement() {
        assertEquals(0, LocationFixPolicy.preferredProvider(reason(NOW + 1), reason(0)));
    }

    @Test public void acceptsTenMinuteBoundaryButNotOlder() {
        assertNull(reason(NOW));
        assertNull(reason(NOW - LocationFixPolicy.MAX_AGE_MS));
        assertEquals("stale_time", reason(NOW - LocationFixPolicy.MAX_AGE_MS - 1));
    }

    @Test public void rejectsMissingSecondsAndFutureTimestamps() {
        assertEquals("missing_time", reason(0));
        assertEquals("missing_time", reason(-1));
        assertEquals("stale_time", reason(NOW / 1000));
        assertEquals("future_time", reason(NOW + 1));
        assertEquals("future_time", reason(Long.MAX_VALUE));
    }

    @Test public void rejectsInvalidCoordinates() {
        for (double[] point : new double[][]{{0, 30}, {50, 0}, {91, 30}, {50, -181},
                {Double.NaN, 30}, {50, Double.POSITIVE_INFINITY}}) {
            assertEquals("invalid_coordinates", LocationFixPolicy.rejection(point[0], point[1], NOW, NOW, false));
        }
    }

    @Test public void doesNotRejectValidCoordinatesJustBecauseTheyAreFarFromStore() {
        assertNull(LocationFixPolicy.rejection(-12.030764, -77.052377, NOW, NOW, false));
    }

    @Test public void cachedWifiScanCannotBecomeFreshByChangingWallClock() {
        long elapsed = 20 * 60 * 1000;
        assertTrue(LocationFixPolicy.freshWifiScan(elapsed * 1000, elapsed));
        assertTrue(LocationFixPolicy.freshWifiScan((elapsed - LocationFixPolicy.MAX_AGE_MS) * 1000, elapsed));
        assertFalse(LocationFixPolicy.freshWifiScan((elapsed - LocationFixPolicy.MAX_AGE_MS - 1) * 1000, elapsed));
        assertFalse(LocationFixPolicy.freshWifiScan((elapsed + 1) * 1000, elapsed));
        assertFalse(LocationFixPolicy.freshWifiScan(0, elapsed));
        assertFalse(LocationFixPolicy.freshWifiScan(NOW * 1000, elapsed));
    }

    private LogMPDB historyPoint() {
        LogMPDB row = new LogMPDB();
        row.CoordX = 50.52;
        row.CoordY = 30.78;
        row.CoordTime = NOW - 30 * 60 * 1000;
        row.vpi = NOW / 1000;
        row.provider = 2;
        row.distance = 36;
        return row;
    }

    @Test public void historicalPointDoesNotUseTenMinuteFreshFixLimit() {
        LogMPDB row = historyPoint();
        assertTrue(LocationFixPolicy.usableHistory(row, NOW));
        assertEquals("stale_time", reason(row.CoordTime));
    }

    @Test public void downloadedAtNowDoesNotMakeFutureCoordinateUsable() {
        LogMPDB row = historyPoint();
        row.CoordTime = 1836273819000L;
        assertFalse(LocationFixPolicy.usableHistory(row, NOW));
        row.CoordTime = 0;
        assertFalse(LocationFixPolicy.usableHistory(row, NOW));
    }

    @Test public void historyDoesNotAcceptMockOrInvalidCoordinates() {
        LogMPDB row = historyPoint();
        row.mocking = true;
        assertFalse(LocationFixPolicy.usableHistory(row, NOW));
        row.mocking = false;
        row.CoordX = Double.NaN;
        assertFalse(LocationFixPolicy.usableHistory(row, NOW));
    }

    @Test public void networkEvidenceCanBeCheckedWithGpsDisabled() {
        LogMPDB row = historyPoint();
        assertTrue(LocationFixPolicy.canCheckHistoryWithoutGps(row, 28847));
        row.mocking = true;
        assertFalse(LocationFixPolicy.canCheckHistoryWithoutGps(row, 28847));
        row.mocking = false;
        row.provider = 1;
        assertFalse(LocationFixPolicy.canCheckHistoryWithoutGps(row, 28847));
    }

    @Test public void onlyConfirmedWifiAtThisStoreCanBypassDisabledGps() {
        LogMPDB row = WifiPresencePolicy.newObservation(1, 2, 28847, 50.52, 30.78, true, "evidence", NOW);
        assertTrue(LocationFixPolicy.canCheckHistoryWithoutGps(row, 28847));
        assertFalse(LocationFixPolicy.canCheckHistoryWithoutGps(row, 27710));
        row.inPlace = 0;
        assertFalse(LocationFixPolicy.canCheckHistoryWithoutGps(row, 28847));
    }
}
