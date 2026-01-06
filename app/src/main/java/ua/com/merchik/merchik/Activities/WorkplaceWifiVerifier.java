package ua.com.merchik.merchik.Activities;


import android.Manifest;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.annotation.RequiresPermission;

import java.util.List;
import java.util.Set;

public final class WorkplaceWifiVerifier {

    // Лучше хранить BSSID конкретных AP (MAC точки), а не только SSID
    // Пример: {"AA:BB:CC:DD:EE:01", "AA:BB:CC:DD:EE:02"}
    private final Set<String> allowedBssids;
    private final Set<String> allowedSsids;

    public WorkplaceWifiVerifier(Set<String> allowedBssids, Set<String> allowedSsids) {
        this.allowedBssids = allowedBssids;
        this.allowedSsids = allowedSsids;
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public boolean isAtWorkplace(Context ctx, int minNearbyMatches) {
        WifiManager wifi = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) return false;

        // 1) Проверяем текущее подключение
        WifiInfo info = wifi.getConnectionInfo();
        if (info == null) return false;

        String ssid = normalizeSsid(info.getSSID());
        String bssid = info.getBSSID(); // может быть null на некоторых устройствах/политиках

        boolean connectedMatches =
                (ssid != null && allowedSsids.contains(ssid)) ||
                        (bssid != null && allowedBssids.contains(bssid));

        if (!connectedMatches) return false;

        // 2) Усиливаем проверку: сканируем окружение (fingerprint)
        // Важно: startScan() throttling -> scanResults могут быть кешем, но это ок для отпечатка
        try { wifi.startScan(); } catch (Exception ignore) {}

        List<ScanResult> results = wifi.getScanResults();
        if (results == null || results.isEmpty()) {
            // если скан недоступен — можно принять по факту подключения
            return true;
        }

        int matches = 0;
        for (ScanResult r : results) {
            if (r != null && r.BSSID != null && allowedBssids.contains(r.BSSID)) {
                matches++;
            }
        }

        return matches >= minNearbyMatches;
    }

    private static String normalizeSsid(String ssid) {
        if (ssid == null) return null;
        ssid = ssid.trim();
        // Android часто возвращает SSID в кавычках
        if (ssid.startsWith("\"") && ssid.endsWith("\"") && ssid.length() >= 2) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }
}
