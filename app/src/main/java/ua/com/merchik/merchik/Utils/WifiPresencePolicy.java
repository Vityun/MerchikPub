package ua.com.merchik.merchik.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.com.merchik.merchik.data.RealmModels.LogMPDB;

public final class WifiPresencePolicy {
    public static final int PROVIDER = 22;
    private static final Pattern LEGACY_MATCH = Pattern.compile(
            "^maps_wifi: countFoundNow=([0-9]+) \\| countMapWiFiDB=([0-9]+) \\| match=([0-9]+) \\| pct=.*$");

    private WifiPresencePolicy() {}

    public static boolean confirmed(int matched, int known) {
        return known > 0 && matched <= known && matched * 10L > known;
    }

    private static boolean legacyConfirmed(String evidence) {
        if (evidence == null) return false;
        Matcher m = LEGACY_MATCH.matcher(evidence);
        if (!m.matches()) return false;
        try {
            int matched = Integer.parseInt(m.group(3));
            return matched <= Integer.parseInt(m.group(1)) && confirmed(matched, Integer.parseInt(m.group(2)));
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public static boolean usableDistance(LogMPDB row, int addrId) {
        if (row.provider == null || row.provider != PROVIDER) return row.distance > 0;
        return addrId > 0 && row.address == addrId && row.distance >= 0 && row.CoordX != 0 && row.CoordY != 0
                && (row.inPlace == 1 || legacyConfirmed(row.locationUniqueString));
    }

    public static boolean onSite(LogMPDB row, int addrId, int radius) {
        return usableDistance(row, addrId) && row.distance < radius;
    }

    public static LogMPDB newObservation(int localId, long dad2, int addrId,
                                         double lat, double lon, boolean confirmed,
                                         String evidence, long observedAtMillis) {
        LogMPDB row = new LogMPDB();
        row.id = localId;
        row.codeDad2 = dad2;
        row.address = addrId;
        row.provider = PROVIDER;
        row.CoordTime = observedAtMillis;
        row.vpi = observedAtMillis / 1000;
        row.CoordAccuracy = 1;
        row.inPlace = confirmed && addrId > 0 && LocationFixPolicy.validCoordinates(lat, lon) ? 1 : 0;
        row.CoordX = row.inPlace == 1 ? lat : 0;
        row.CoordY = row.inPlace == 1 ? lon : 0;
        row.distance = 0;
        row.locationUniqueString = evidence;
        return row;
    }
}
