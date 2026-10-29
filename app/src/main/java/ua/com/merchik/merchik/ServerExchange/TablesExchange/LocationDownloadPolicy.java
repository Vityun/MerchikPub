package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.merchik.merchik.data.Database.Room.SynchronizationCursorSDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RetrofitResponse.Location.LocationList;

public final class LocationDownloadPolicy {
    private LocationDownloadPolicy() { }

    public static boolean cacheReady(SynchronizationCursorSDB cursor, long serverRowCount) {
        // Upload confirmations add server IDs between downloads; they must not reset the VPI.
        return cursor != null && serverRowCount >= cursor.itemCount;
    }

    public static List<LogMPDB> missingRows(List<LocationList> rows, Set<Long> existingServerIds,
                                           int lastLocalId, long recordedAtSeconds) {
        Set<Long> known = new HashSet<>(existingServerIds);
        List<LogMPDB> result = new ArrayList<>();
        for (LocationList location : rows) {
            if (location == null || location.id == null || location.id <= 0) {
                throw new IllegalStateException("Missing location server ID");
            }
            if (!known.add(location.id)) continue;
            if (location.dtDevice == null || location.lat == null || location.lon == null
                    || location.altitude == null || location.speed == null || location.accuracy == null) {
                throw new IllegalStateException("Incomplete location: serverId=" + location.id);
            }
            LogMPDB row = new LogMPDB();
            row.id = lastLocalId = Math.incrementExact(lastLocalId);
            row.serverId = location.id;
            row.provider = location.sourceId;
            row.CoordX = location.lat;
            row.CoordY = location.lon;
            row.CoordAltitude = location.altitude;
            row.CoordTime = Math.multiplyExact(location.dtDevice, 1000L);
            row.CoordSpeed = location.speed;
            row.CoordAccuracy = location.accuracy;
            row.mocking = location.locationIsFake != null && location.locationIsFake != 0;
            row.vpi = recordedAtSeconds;
            result.add(row);
        }
        return result;
    }
}
