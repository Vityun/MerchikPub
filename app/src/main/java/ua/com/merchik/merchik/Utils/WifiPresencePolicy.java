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
}
