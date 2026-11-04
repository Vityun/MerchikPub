package ua.com.merchik.merchik.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public final class WifiScanAccess {
    public final WifiManager manager;
    public final Boolean enabled;
    private final boolean fine;
    private final boolean coarse;
    private final Boolean nearby;

    public WifiScanAccess(Context ctx) {
        manager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Boolean state = null;
        if (manager != null) {
            try {
                state = manager.isWifiEnabled();
            } catch (SecurityException ignored) {
                // Unknown radio state is not the same as Wi-Fi being off.
            }
        }
        enabled = state;
        fine = granted(ctx, Manifest.permission.ACCESS_FINE_LOCATION);
        coarse = granted(ctx, Manifest.permission.ACCESS_COARSE_LOCATION);
        nearby = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? granted(ctx, Manifest.permission.NEARBY_WIFI_DEVICES) : null;
    }

    private static boolean granted(Context ctx, String permission) {
        return ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasPermissions() {
        return fine && !Boolean.FALSE.equals(nearby);
    }

    public String diagnostics() {
        return "wifiEnabled=" + (enabled == null ? "unknown" : enabled) + ", fine=" + fine
                + ", coarse=" + coarse + ", nearbyWifi=" + (nearby == null ? "not_required" : nearby);
    }
}
