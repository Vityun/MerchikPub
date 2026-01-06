package ua.com.merchik.merchik.Activities;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WifiScannerActivity extends AppCompatActivity {

    private static final int REQ_PERMS = 1001;

    private WifiManager wifiManager;
    private BroadcastReceiver scanReceiver;

    private TextView tv;
    private Button btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // простейшая разметка без XML:
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);

        btnScan = new Button(this);
        btnScan.setText("Scan Wi-Fi");
        root.addView(btnScan);

        tv = new TextView(this);
        tv.setTextIsSelectable(true);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(tv);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0, 1f
        ));

        setContentView(root);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        scanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean updated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                append("SCAN_RESULTS_AVAILABLE_ACTION: updated=" + updated);

                dumpConnectedInfo();
                dumpScanResults();
            }
        };

        btnScan.setOnClickListener(v -> {
            if (!hasPerms()) {
                requestPerms();
                return;
            }
            if (!isLocationEnabled()) {
                append("Location выключена. Включи Location (Геолокацию), иначе scanResults могут быть пустыми.");
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }
            startScan();
        });

        // запросим сразу
        if (!hasPerms()) requestPerms();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(scanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(scanReceiver);
    }

    private void startScan() {
        if (wifiManager == null) {
            append("WifiManager == null");
            return;
        }
        if (!wifiManager.isWifiEnabled()) {
            append("Wi-Fi выключен. Открой настройки и включи.");
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            return;
        }

        boolean started;
        try {
            started = wifiManager.startScan();
        } catch (Exception e) {
            append("startScan exception: " + e);
            started = false;
        }

        append("startScan() started=" + started + " (если false — может быть throttling, но scanResults могут быть кешом)");
        // Результаты придут в BroadcastReceiver, но даже если throttling — можно сразу читать кеш:
        dumpConnectedInfo();
        dumpScanResults();
    }

    private void dumpConnectedInfo() {
        try {
            WifiInfo info = wifiManager.getConnectionInfo();
            if (info == null) {
                append("Connected Wi-Fi: info=null");
                return;
            }
            String ssid = normalizeSsid(info.getSSID());
            String bssid = info.getBSSID();
            int rssi = info.getRssi();
            int linkSpeed = info.getLinkSpeed(); // Mbps
            int freq = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? info.getFrequency() : -1;

            append("Connected Wi-Fi:");
            append("  SSID=" + ssid);
            append("  BSSID=" + bssid);
            append("  RSSI=" + rssi + " dBm");
            append("  linkSpeed=" + linkSpeed + " Mbps");
            append("  frequency=" + freq + " MHz");
        } catch (Exception e) {
            append("dumpConnectedInfo exception: " + e);
        }
    }

    private void dumpScanResults() {
        try {
            List<ScanResult> results = wifiManager.getScanResults();
            if (results == null || results.isEmpty()) {
                append("scanResults: empty (часто из-за Location OFF или permissions)");
                return;
            }

            // Отсортируем по силе сигнала (level ближе к 0 — сильнее)
            results.sort((a, b) -> Integer.compare(b.level, a.level));

            append("scanResults count=" + results.size());
            int limit = Math.min(results.size(), 20);
            for (int i = 0; i < limit; i++) {
                ScanResult r = results.get(i);
                append(String.format(Locale.US,
                        "%02d) SSID=%s | BSSID=%s | level=%ddBm | freq=%dMHz | caps=%s",
                        i + 1,
                        safe(r.SSID),
                        safe(r.BSSID),
                        r.level,
                        r.frequency,
                        safe(r.capabilities)
                ));
            }
        } catch (Exception e) {
            append("dumpScanResults exception: " + e);
        }
    }

    private boolean hasPerms() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean nearby = ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES)
                    == PackageManager.PERMISSION_GRANTED;
            return fine && nearby;
        }
        return fine;
    }

    private void requestPerms() {
        ArrayList<String> perms = new ArrayList<>();
        perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        }
        ActivityCompat.requestPermissions(this, perms.toArray(new String[0]), REQ_PERMS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMS && hasPerms()) {
            append("Permissions granted");
        } else {
            append("Permissions denied");
        }
    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return lm.isLocationEnabled();
        } else {
            try {
                int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
                return mode != Settings.Secure.LOCATION_MODE_OFF;
            } catch (Settings.SettingNotFoundException e) {
                return false;
            }
        }
    }

    private void append(String s) {
        Log.d("WIFI_SCAN", s);
        tv.append(s + "\n");
    }

    private static String normalizeSsid(String ssid) {
        if (ssid == null) return null;
        ssid = ssid.trim();
        if (ssid.startsWith("\"") && ssid.endsWith("\"") && ssid.length() >= 2) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
