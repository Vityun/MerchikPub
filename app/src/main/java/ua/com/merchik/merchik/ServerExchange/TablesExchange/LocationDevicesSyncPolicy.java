package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import java.util.List;
import ua.com.merchik.merchik.ServerExchange.SyncResponsePolicy;
import ua.com.merchik.merchik.data.Database.Room.LocationDevices;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.LocationDevicesResponse;

public final class LocationDevicesSyncPolicy {
    private LocationDevicesSyncPolicy() { }

    public static List<LocationDevices> rows(LocationDevicesResponse response) {
        if (response == null) throw new IllegalStateException("Empty location/device_list response");
        List<LocationDevices> rows = SyncResponsePolicy.requireList(response.state, response.error, response.list);
        for (LocationDevices row : rows) {
            if (row.id == null || row.id <= 0) throw new IllegalStateException("Invalid Wi-Fi row ID");
        }
        return rows;
    }

    public static long requestWatermark(long previous, Long started) {
        return started == null ? Math.max(0, previous)
                : Math.min(Math.max(0, previous), Math.max(0, started - 120));
    }

    public static long savedWatermark(long from, Long started) {
        return started == null ? Math.max(0, from) : Math.max(from, Math.max(0, started - 120));
    }
}
