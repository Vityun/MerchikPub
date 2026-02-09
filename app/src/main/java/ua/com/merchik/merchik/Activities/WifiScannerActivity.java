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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import java.util.Set;

import android.os.Handler;
import android.os.Looper;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonStartWork;
import ua.com.merchik.merchik.Utils.WorkStartNetworkSnapshot;

public class WifiScannerActivity extends AppCompatActivity {

    private static final int REQ_PERMS = 1001;

    private WifiManager wifiManager;
    private BroadcastReceiver wifiScanReceiver;

    // ---- BT ----
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner bleScanner;
    private ScanCallback bleCallback;
    private boolean bleScanning = false;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable bleStopRunnable;

    private TextView tv;
    private Button btnScanWifi;
    private Button btnScanBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI без XML
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);

        tv = new TextView(this);
        tv.setTextIsSelectable(true);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(tv);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0, 1f
        ));

        // Кнопки внизу
        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);

        btnScanWifi = new Button(this);
        btnScanWifi.setText("Scan Wi-Fi");

        btnScanBt = new Button(this);
        btnScanBt.setText("Scan Bluetooth");

        bottom.addView(btnScanWifi);
        bottom.addView(btnScanBt);
        root.addView(bottom);

        setContentView(root);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bm != null) btAdapter = bm.getAdapter();

        // WIFI receiver: выводим результаты ТОЛЬКО когда scan действительно запущен кнопкой
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean updated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                w("SCAN_RESULTS_AVAILABLE_ACTION: updated=" + updated);
                dumpWifiConnectedInfo();
                dumpWifiScanResults();
            }
        };

        btnScanWifi.setOnClickListener(v -> {
            if (!hasWifiPerms()) {
                requestPerms();
                return;
            }
            if (!isLocationEnabled()) {
                w("Location выключена. Включи Location (Геолокацию), иначе scanResults могут быть пустыми.");
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }
            startWifiScan();
        });

        btnScanBt.setOnClickListener(v -> {
            if (!hasBtPerms()) {
                requestPerms();
                return;
            }
            startBluetoothScanOnce(10_000); // скан 10 секунд и стоп
        });

        // ❌ Никаких авто-сканов/дампов тут
        // ❌ Не вызываем requestPerms() автоматически — только по кнопке (если надо)
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wifiScanReceiver);
        stopBleScanIfNeeded(); // на всякий случай
    }

    // ---------------- WIFI ----------------

    private void startWifiScan() {
        w("----- WIFI: SCAN -----");

        if (wifiManager == null) {
            w("WifiManager == null");
            return;
        }
        if (!wifiManager.isWifiEnabled()) {
            w("Wi-Fi выключен. Открой настройки и включи.");
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            return;
        }

        boolean started;
        try {
            started = wifiManager.startScan();
        } catch (Exception e) {
            w("startScan exception: " + e);
            started = false;
        }

        w("startScan() started=" + started + " (если false — может быть throttling, но scanResults могут быть кешом)");
        // По твоему желанию — можно НЕ дампить кеш сразу, а ждать broadcast.
        // Но ты раньше дампил сразу — оставлю как было:
        dumpWifiConnectedInfo();
        dumpWifiScanResults();
    }

    private void dumpWifiConnectedInfo() {
        try {
            WifiInfo info = wifiManager != null ? wifiManager.getConnectionInfo() : null;
            if (info == null) {
                w("Connected Wi-Fi: info=null");
                return;
            }
            String ssid = normalizeSsid(info.getSSID());
            String bssid = info.getBSSID();
            int rssi = info.getRssi();
            int linkSpeed = info.getLinkSpeed(); // Mbps
            int freq = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? info.getFrequency() : -1;

            w("Connected Wi-Fi:");
            w("  SSID=" + ssid);
            w("  BSSID=" + bssid);
            w("  RSSI=" + rssi + " dBm");
            w("  linkSpeed=" + linkSpeed + " Mbps");
            w("  frequency=" + freq + " MHz");
        } catch (Exception e) {
            w("dumpWifiConnectedInfo exception: " + e);
        }
    }

    private void dumpWifiScanResults() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            List<android.net.wifi.ScanResult> results = wifiManager != null ? wifiManager.getScanResults() : null;
            if (results == null || results.isEmpty()) {
                w("scanResults: empty (часто из-за Location OFF или permissions)");
                return;
            }

            results.sort((a, b) -> Integer.compare(b.level, a.level));

            w("scanResults count=" + results.size());
            int limit = Math.min(results.size(), 20);
            for (int i = 0; i < limit; i++) {
                android.net.wifi.ScanResult r = results.get(i);
                w(String.format(Locale.US,
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
            w("dumpWifiScanResults exception: " + e);
        }
    }

    // ---------------- BLUETOOTH ----------------

    private void startBluetoothScanOnce(long durationMs) {
        b("----- BLUETOOTH: SCAN -----");

        if (btAdapter == null) {
            b("BluetoothAdapter == null");
            return;
        }

        if (!btAdapter.isEnabled()) {
            b("Bluetooth выключен. Открой настройки и включи.");
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            return;
        }

        // Сначала выведем инфу (по клику — это ок)
        dumpBluetoothStateAndPaired();

        bleScanner = btAdapter.getBluetoothLeScanner();
        if (bleScanner == null) {
            b("BluetoothLeScanner == null (BLE может быть недоступен)");
            return;
        }

        if (bleCallback == null) {
            bleCallback = new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice d = result.getDevice();
                    String name = null;
                    String addr = null;
                    try {
                        if (d != null) {
                            name = d.getName();
                            addr = d.getAddress();
                        }
                    } catch (SecurityException ignored) {}

                    b(String.format(Locale.US,
                            "BLE: name=%s | addr=%s | rssi=%ddBm",
                            safe(name),
                            safe(addr),
                            result.getRssi()
                    ));

                    try {
                        if (d != null) {
                            name = d.getName();
                            addr = d.getAddress();
                        }
                    } catch (SecurityException ignored) {}

                    int rssi = result.getRssi();
//                    btItems.add(new WorkStartNetworkSnapshot.BtItem(name, addr, rssi, null));
                }

                @Override
                public void onScanFailed(int errorCode) {
                    b("BLE scan failed: errorCode=" + errorCode);
                }
            };
        }

        stopBleScanIfNeeded(); // если вдруг был активен

        try {
            bleScanner.startScan(bleCallback);
            bleScanning = true;
            b("bleScanner.startScan() started=true");

            // Авто-стоп через durationMs, чтобы не работало в фоне
            if (bleStopRunnable != null) mainHandler.removeCallbacks(bleStopRunnable);
            bleStopRunnable = () -> {
                b("BLE: auto stop after " + durationMs + "ms");
                stopBleScanIfNeeded();
            };
            mainHandler.postDelayed(bleStopRunnable, durationMs);

        } catch (SecurityException se) {
            b("startScan SecurityException: " + se);
        } catch (Exception e) {
            b("startScan exception: " + e);
        }
    }

    private void stopBleScanIfNeeded() {
        if (!bleScanning) return;

        if (bleStopRunnable != null) {
            mainHandler.removeCallbacks(bleStopRunnable);
            bleStopRunnable = null;
        }

        if (bleScanner != null && bleCallback != null) {
            try {
                bleScanner.stopScan(bleCallback);
            } catch (Exception ignored) {}
        }
        bleScanning = false;
    }

    private void dumpBluetoothStateAndPaired() {
        try {
            if (btAdapter == null) {
                b("Bluetooth: adapter=null");
                return;
            }

            b("Bluetooth state:");
            b("  enabled=" + btAdapter.isEnabled());
            b("  name=" + safe(btAdapter.getName()));
            b("  address=" + safe(btAdapter.getAddress()));

            Set<BluetoothDevice> bonded = btAdapter.getBondedDevices();
            if (bonded == null || bonded.isEmpty()) {
                b("bondedDevices: empty");
                return;
            }

            b("bondedDevices count=" + bonded.size());
            int i = 1;
            for (BluetoothDevice d : bonded) {
                String name = null;
                String addr = null;
                int bond = -1;
                try {
                    name = d.getName();
                    addr = d.getAddress();
                    bond = d.getBondState();
                } catch (SecurityException ignored) {}

                b(String.format(Locale.US,
                        "%02d) name=%s | addr=%s | bondState=%d",
                        i++,
                        safe(name),
                        safe(addr),
                        bond
                ));
            }
        } catch (Exception e) {
            b("dumpBluetoothStateAndPaired exception: " + e);
        }
    }

    // ---------------- PERMISSIONS ----------------

    private boolean hasWifiPerms() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean nearby = ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES)
                    == PackageManager.PERMISSION_GRANTED;
            return fine && nearby;
        }
        return fine;
    }

    private boolean hasBtPerms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean scan = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_GRANTED;
            boolean connect = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
            return scan && connect;
        } else {
            // До Android 12: для скана часто нужен location
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPerms() {
        ArrayList<String> perms = new ArrayList<>();
        perms.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_SCAN);
            perms.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        ActivityCompat.requestPermissions(this, perms.toArray(new String[0]), REQ_PERMS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMS) {
            w("Permissions Wi-Fi = " + hasWifiPerms());
            b("Permissions BT    = " + hasBtPerms());
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

    // ---------------- LOG HELPERS ----------------

    private void w(String s) {
        Log.d("WIFI_SCAN", s);
        tv.append("[WIFI] " + s + "\n");
    }

    private void b(String s) {
        Log.d("BT_SCAN", s);
        tv.append("[BT] " + s + "\n");
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


