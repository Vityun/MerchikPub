package ua.com.merchik.merchik.Utils;

import org.junit.Test;

import ua.com.merchik.merchik.data.RealmModels.LogMPDB;

import static org.junit.Assert.*;

public class WifiPresencePolicyTest {
    private static final int STORE = 45069;

    private static LogMPDB wifi() {
        LogMPDB row = new LogMPDB();
        row.address = STORE;
        row.provider = WifiPresencePolicy.PROVIDER;
        row.CoordX = 51.540333;
        row.CoordY = 31.266134;
        row.inPlace = 1;
        return row;
    }

    @Test public void confirmedWifiAtThisStoreCanBeExactlyZeroMetres() {
        assertTrue(WifiPresencePolicy.onSite(wifi(), STORE, 500));
    }

    @Test public void wifiAtAnotherStoreNeverConfirmsEvenNearby() {
        LogMPDB row = wifi();
        assertFalse(WifiPresencePolicy.onSite(row, STORE + 1, 500));
        row.distance = 12;
        assertFalse(WifiPresencePolicy.usableDistance(row, STORE + 1));
        row.address = 0;
        assertFalse(WifiPresencePolicy.usableDistance(row, 0));
    }

    @Test public void zeroGpsOrNetworkDistanceIsNotEvidence() {
        LogMPDB row = wifi();
        for (Integer provider : new Integer[]{1, 2, null}) {
            row.provider = provider;
            assertFalse(WifiPresencePolicy.usableDistance(row, STORE));
        }
    }

    @Test public void ordinaryPositiveDistanceStillUsesTheRadius() {
        LogMPDB row = wifi();
        row.provider = 1;
        row.address = 0;
        row.distance = 499;
        assertTrue(WifiPresencePolicy.onSite(row, STORE, 500));
        row.distance = 500;
        assertFalse(WifiPresencePolicy.onSite(row, STORE, 500));
        row.distance = -1;
        assertFalse(WifiPresencePolicy.onSite(row, STORE, 500));
    }

    @Test public void failedOrInvalidEvidenceDoesNotConfirm() {
        LogMPDB row = wifi();
        row.inPlace = 0;
        for (String text : new String[]{null, "", "WiFi is OFF", "No address in MAPS_WIFI_DB",
                "maps_wifi: countFoundNow=0 | countMapWiFiDB=8 | match=2 | pct=25.0"}) {
            row.locationUniqueString = text;
            assertFalse(WifiPresencePolicy.usableDistance(row, STORE));
        }
        row.inPlace = 1;
        row.CoordY = 0;
        assertFalse(WifiPresencePolicy.onSite(row, STORE, 500));
    }

    @Test public void oldRowsUseActualCountsInsteadOfRoundedPercentage() {
        LogMPDB row = wifi();
        row.inPlace = 0;
        row.locationUniqueString = "maps_wifi: countFoundNow=31 | countMapWiFiDB=300 | match=30 | pct=10.0";
        assertFalse(WifiPresencePolicy.onSite(row, STORE, 500));
        row.locationUniqueString = "maps_wifi: countFoundNow=31 | countMapWiFiDB=299 | match=30 | pct=10.0";
        assertTrue(WifiPresencePolicy.onSite(row, STORE, 500));
    }

    @Test public void matchRequiresStrictlyMoreThanTenPercentOfTheReference() {
        assertFalse(WifiPresencePolicy.confirmed(0, 0));
        assertFalse(WifiPresencePolicy.confirmed(1, 10));
        assertFalse(WifiPresencePolicy.confirmed(1, 11));
        assertFalse(WifiPresencePolicy.confirmed(3, 2));
        assertTrue(WifiPresencePolicy.confirmed(1, 9));
        assertTrue(WifiPresencePolicy.confirmed(3, 3));
    }

    @Test public void observationHasItsOwnTimeAndNoGpsOrUploadState() {
        long now = 1794393502703L;
        LogMPDB gps = wifi();
        gps.id = 1411;
        gps.CoordTime = 1836273819000L;
        gps.mocking = true;
        gps.serverId = 123L;
        gps.upload = 1;
        LogMPDB row = WifiPresencePolicy.newObservation(1412, 1101126028847061639L,
                STORE, gps.CoordX, gps.CoordY, true, "evidence", now);
        assertEquals(now, row.CoordTime);
        assertEquals(now / 1000, row.vpi);
        assertEquals(1412, row.id);
        assertEquals(1101126028847061639L, row.codeDad2);
        assertEquals(Integer.valueOf(WifiPresencePolicy.PROVIDER), row.provider);
        assertEquals(0, row.serverId);
        assertEquals(0, row.upload);
        assertFalse(row.mocking);
        assertTrue(WifiPresencePolicy.onSite(row, STORE, 500));
        assertEquals(1836273819000L, gps.CoordTime);
        assertNull(LocationFixPolicy.rejection(row.CoordX, row.CoordY, row.CoordTime, now, row.mocking));
    }

    @Test public void failedMatchNeverCreatesPresenceEvidence() {
        LogMPDB row = WifiPresencePolicy.newObservation(1, 2L, STORE, 51.54, 31.26,
                false, "no match", 1794393502703L);
        assertEquals(0, row.inPlace);
        assertEquals(0, row.CoordX, 0);
        assertEquals(0, row.CoordY, 0);
        assertFalse(WifiPresencePolicy.onSite(row, STORE, 500));
    }

    @Test public void matchingNetworksRequireStoreAndValidStoreCoordinates() {
        for (double[] coordinates : new double[][]{{0, 31}, {51, 0}, {91, 31}, {51, Double.NaN}}) {
            assertEquals(0, WifiPresencePolicy.newObservation(1, 2, STORE,
                    coordinates[0], coordinates[1], true, "evidence", 1000).inPlace);
        }
        assertEquals(0, WifiPresencePolicy.newObservation(1, 2, 0, 51, 31, true, "evidence", 1000).inPlace);
    }
}
