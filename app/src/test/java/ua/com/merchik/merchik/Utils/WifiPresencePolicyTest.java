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
}
