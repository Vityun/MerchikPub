package ua.com.merchik.merchik.Utils;

import ua.com.merchik.merchik.data.RealmModels.LogMPDB;

public final class LocationFixPolicy {
    public static final long MAX_AGE_MS = 10 * 60 * 1000L;

    private LocationFixPolicy() { }

    public static String rejection(double lat, double lon, long measuredAt, long now, boolean mock) {
        if (!validCoordinates(lat, lon)) return "invalid_coordinates";
        if (mock) return "mock_location";
        if (measuredAt <= 0) return "missing_time";
        if (measuredAt > now) return "future_time";
        if (now - measuredAt > MAX_AGE_MS) return "stale_time";
        return null;
    }

    public static boolean validCoordinates(double lat, double lon) {
        return Double.isFinite(lat) && Double.isFinite(lon)
                && lat != 0 && lon != 0 && Math.abs(lat) <= 90 && Math.abs(lon) <= 180;
    }

    public static int preferredProvider(String gpsRejection, String netRejection) {
        if (gpsRejection == null) return 1;
        return netRejection == null ? 2 : 0;
    }

    public static boolean usableHistory(LogMPDB row, long now) {
        // The caller supplies the visit's history interval, not the fresh-fix interval.
        return !row.mocking && validCoordinates(row.CoordX, row.CoordY)
                && row.CoordTime > 0 && row.CoordTime <= now;
    }

    public static boolean canCheckHistoryWithoutGps(LogMPDB row, int addrId) {
        return !row.mocking && validCoordinates(row.CoordX, row.CoordY)
                && (Integer.valueOf(2).equals(row.provider)
                || Integer.valueOf(WifiPresencePolicy.PROVIDER).equals(row.provider))
                && WifiPresencePolicy.usableDistance(row, addrId);
    }

    public static boolean freshWifiScan(long timestampMicros, long elapsedRealtimeMs) {
        long measuredAt = timestampMicros / 1000;
        return measuredAt > 0 && measuredAt <= elapsedRealtimeMs
                && elapsedRealtimeMs - measuredAt <= MAX_AGE_MS;
    }
}
