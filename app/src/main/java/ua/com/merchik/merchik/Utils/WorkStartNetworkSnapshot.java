package ua.com.merchik.merchik.Utils;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.trecker;

/**
 * Слепок Wi-Fi + Bluetooth только в момент клика "Начать работы".
 * Никаких сканов заранее, никаких фоновых продолжений.
 */
public final class WorkStartNetworkSnapshot {

    private WorkStartNetworkSnapshot() {}

    private static final long WIFI_TIMEOUT_MS = 6_000;
    private static final long BT_SCAN_MS = 6_000;
    private static final int WIFI_LIMIT = 0; // 0 = без лимита

    public interface DistanceFn {
        float distanceMeters(double latA, double lngA, double latB, double lngB);
    }

    public interface ResultCallback {
        /** Всегда вызывается один раз в конце */
        void onDone(Result result);
    }

    public static final class Result {
        public final int wifiAdded;
        public final int btAdded;
        public final String error; // null если всё ок

        public Result(int wifiAdded, int btAdded, String error) {
            this.wifiAdded = wifiAdded;
            this.btAdded = btAdded;
            this.error = error;
        }

        public boolean isOk() { return error == null; }
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN})
    public static void captureAndLog(
            Context context,
            WpDataDB wpDataDB,
            int tema_id,
            DistanceFn distanceFn,
            ResultCallback callback
    ) {
        // 1) coords
        double curLat = Globals.CoordX;
        double curLng = Globals.CoordY;

        double shopLat = parseDoubleSafe(wpDataDB.getAddr_location_xd());
        double shopLng = parseDoubleSafe(wpDataDB.getAddr_location_yd());

        if (Double.isNaN(shopLat) || Double.isNaN(shopLng)) {
            callback.onDone(new Result(0, 0, "Ошибка: координаты магазина невалидны"));
            return;
        }

        float dist = distanceFn.distanceMeters(curLat, curLng, shopLat, shopLng);

        // ✅ если дальше 100м — НИ ОДНОЙ записи, но диалог покажем причину
        if (dist > 100f) {
            callback.onDone(new Result(0, 0, "Запись не выполнена: вы дальше 100м от магазина"));
            return;
        }
        boolean isMocking = (trecker.isMockGPS || trecker.isMockNET);
        if (isMocking) {
            callback.onDone(new Result(0, 0, "Запись не выполнена: включенно фиктивное местоположение"));
            return;
        }

        // perms
        if (!hasWifiPerms(context) || !hasBtPerms(context)) {
            callback.onDone(new Result(0, 0, "Ошибка: нет разрешений (Wi-Fi/Bluetooth/Location)"));
            return;
        }

        SnapshotSession session = new SnapshotSession(
                context.getApplicationContext(), wpDataDB, tema_id, callback
        );
        session.start();
    }

    // ---------------- Session ----------------

    private static final class SnapshotSession {
        private final Context app;
        private final WpDataDB wp;
        private final int temaId;
        private final ResultCallback callback;

        private final Handler main = new Handler(Looper.getMainLooper());

        private final WifiManager wifiManager;
        private final BluetoothAdapter btAdapter;

        private final ArrayList<String> wifiLines = new ArrayList<>();
        private final LinkedHashMap<String, BtItem> btUnique = new LinkedHashMap<>();

        private boolean wifiDone = false;
        private boolean btDone = false;

        private BroadcastReceiver wifiReceiver;
        private BluetoothLeScanner bleScanner;
        private ScanCallback bleCallback;

        private Runnable wifiTimeout;
        private Runnable btStop;

        SnapshotSession(Context app, WpDataDB wp, int temaId, ResultCallback callback) {
            this.app = app;
            this.wp = wp;
            this.temaId = temaId;
            this.callback = callback;

            this.wifiManager = (WifiManager) app.getSystemService(Context.WIFI_SERVICE);

            BluetoothManager bm = (BluetoothManager) app.getSystemService(Context.BLUETOOTH_SERVICE);
            this.btAdapter = bm != null ? bm.getAdapter() : null;
        }

        @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION})
        void start() {
            startWifiOnce();
            startBleOnce();
            tryFinish();
        }

        // ---- WIFI ----
        @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        private void startWifiOnce() {
            if (wifiManager == null) { wifiDone = true; return; }
            if (!wifiManager.isWifiEnabled()) {
                // ничего в лог не пишем, просто причина в диалог
                wifiLines.clear();
                wifiDone = true;
                return;
            }

            wifiReceiver = new BroadcastReceiver() {
                @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        wifiLines.addAll(buildWifiLinesFromScanResults(wifiManager, WIFI_LIMIT));
                    } finally {
                        safeUnregisterWifi();
                        wifiDone = true;
                        tryFinish();
                    }
                }
            };

            try {
                app.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            } catch (Exception e) {
                wifiDone = true;
                return;
            }

            boolean started;
            try { started = wifiManager.startScan(); }
            catch (Exception e) { started = false; }

            if (!started) {
                // читаем кеш и завершаем
                wifiLines.addAll(buildWifiLinesFromScanResults(wifiManager, WIFI_LIMIT));
                safeUnregisterWifi();
                wifiDone = true;
                return;
            }

            wifiTimeout = () -> {
                wifiLines.addAll(buildWifiLinesFromScanResults(wifiManager, WIFI_LIMIT));
                safeUnregisterWifi();
                wifiDone = true;
                tryFinish();
            };
            main.postDelayed(wifiTimeout, WIFI_TIMEOUT_MS);
        }

        private void safeUnregisterWifi() {
            if (wifiTimeout != null) {
                main.removeCallbacks(wifiTimeout);
                wifiTimeout = null;
            }
            if (wifiReceiver != null) {
                try { app.unregisterReceiver(wifiReceiver); } catch (Exception ignored) {}
                wifiReceiver = null;
            }
        }

        // ---- BLE ----
        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        private void startBleOnce() {
            if (btAdapter == null || !btAdapter.isEnabled()) {
                btDone = true;
                return;
            }

            bleScanner = btAdapter.getBluetoothLeScanner();
            if (bleScanner == null) {
                btDone = true;
                return;
            }

            bleCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    BluetoothDevice d = result.getDevice();
                    if (d == null) return;

                    String addr;
                    String name;
                    try {
                        addr = d.getAddress();
                        name = d.getName();
                    } catch (SecurityException se) {
                        return;
                    }
                    if (addr == null || addr.isEmpty()) return;

                    btUnique.put(addr, new BtItem(name, addr, result.getRssi()));
                }
            };

            try {
                bleScanner.startScan(bleCallback);
            } catch (Exception e) {
                btDone = true;
                return;
            }

            btStop = () -> {
                stopBle();
                btDone = true;
                tryFinish();
            };
            main.postDelayed(btStop, BT_SCAN_MS);
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        private void stopBle() {
            if (btStop != null) {
                main.removeCallbacks(btStop);
                btStop = null;
            }
            if (bleScanner != null && bleCallback != null) {
                try { bleScanner.stopScan(bleCallback); } catch (Exception ignored) {}
            }
            bleCallback = null;
            bleScanner = null;
        }

        // ---- FINISH ----
        private void tryFinish() {
            if (!wifiDone || !btDone) return;

            // Пишем ТОЛЬКО WiFi/Bluetooth строки
            int wifiAdded = 0;
            for (String line : wifiLines) {
                if (writeOneLineLog(wp, temaId, line)) wifiAdded++;
            }

            int btAdded = 0;
            for (String line : buildBluetoothLines(btUnique)) {
                if (writeOneLineLog(wp, temaId, line)) btAdded++;
            }

            // Если ничего не записали — дадим внятную причину
            String error = null;
            if (wifiAdded == 0 && btAdded == 0) {
                // максимально коротко, без лишних данных
                if (wifiManager != null && !wifiManager.isWifiEnabled()) {
                    error = "Wi-Fi выключен";
                } else if (btAdapter != null && !btAdapter.isEnabled()) {
                    error = "Bluetooth выключен";
                } else {
                    error = "Не удалось получить данные Wi-Fi/Bluetooth";
                }
            }

            callback.onDone(new Result(wifiAdded, btAdded, error));
        }
    }

    // ---------------- Builders / Format ----------------

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    private static List<String> buildWifiLinesFromScanResults(WifiManager wifiManager, int limit) {
        ArrayList<String> lines = new ArrayList<>();
        List<android.net.wifi.ScanResult> results;
        try { results = wifiManager.getScanResults(); }
        catch (Exception e) { return lines; }
        if (results == null || results.isEmpty()) return lines;

        HashMap<String, android.net.wifi.ScanResult> uniq = new HashMap<>();
        for (android.net.wifi.ScanResult r : results) {
            if (r == null) continue;
            if (r.BSSID == null || r.BSSID.isEmpty()) continue;

            android.net.wifi.ScanResult prev = uniq.get(r.BSSID);
            if (prev == null || r.level > prev.level) uniq.put(r.BSSID, r);
        }

        ArrayList<android.net.wifi.ScanResult> uniqList = new ArrayList<>(uniq.values());
        Collections.sort(uniqList, (a, b) -> Integer.compare(b.level, a.level));

        int realLimit = (limit <= 0) ? uniqList.size() : Math.min(limit, uniqList.size());
        for (int i = 0; i < realLimit; i++) {
            android.net.wifi.ScanResult r = uniqList.get(i);
            lines.add("WiFi | SSID=" + safe(r.SSID) +
                    " | BSSID=" + safe(r.BSSID) +
                    " | level=" + r.level + "dBm" +
                    " | freq=" + r.frequency + "MHz");
        }
        return lines;
    }

    private static List<String> buildBluetoothLines(LinkedHashMap<String, BtItem> uniq) {
        ArrayList<String> out = new ArrayList<>();
        if (uniq == null || uniq.isEmpty()) return out;

        for (BtItem it : uniq.values()) {
            out.add("Bluetooth | name=" + safe(it.name) +
                    " | addr=" + safe(it.addr) +
                    " | level=" + it.rssi + "dBm");
        }
        return out;
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static final class BtItem {
        final String name;
        final String addr;
        final int rssi;
        BtItem(String name, String addr, int rssi) {
            this.name = name;
            this.addr = addr;
            this.rssi = rssi;
        }
    }

    // ---------------- Realm log writer ----------------

    /** @return true если реально добавили запись */
    private static boolean writeOneLineLog(WpDataDB wpDataDB, int tema_id, String commentLine) {
        if (commentLine == null || commentLine.trim().isEmpty()) return false;

        int addr_id = wpDataDB.getAddr_id();
        int user_id = wpDataDB.getUser_id();
        long dad2 = wpDataDB.getCode_dad2();
        String client_id = wpDataDB.getClient_id();
        java.util.Date wpDate = wpDataDB.getDt();

        RealmManager.setRowToLog(Collections.singletonList(
                new LogDB(
                        RealmManager.getLastIdLogDB() + 1,
                        System.currentTimeMillis() / 1000,
                        commentLine,
                        tema_id,
                        client_id,
                        addr_id,
                        dad2,
                        user_id,
                        null,
                        Globals.session,
                        String.valueOf(wpDate)
                )
        ));
        return true;
    }

    // ---------------- Permissions / Utils ----------------

    private static boolean hasWifiPerms(Context ctx) {
        boolean fine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean nearby = ContextCompat.checkSelfPermission(ctx, Manifest.permission.NEARBY_WIFI_DEVICES)
                    == PackageManager.PERMISSION_GRANTED;
            return fine && nearby;
        }
        return fine;
    }

    private static boolean hasBtPerms(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean scan = ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_GRANTED;
            boolean connect = ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
            return scan && connect;
        } else {
            return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private static double parseDoubleSafe(String s) {
        if (s == null) return Double.NaN;
        try { return Double.parseDouble(s.trim().replace(',', '.')); }
        catch (Exception e) { return Double.NaN; }
    }
}


