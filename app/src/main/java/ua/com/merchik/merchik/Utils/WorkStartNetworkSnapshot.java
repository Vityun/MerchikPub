package ua.com.merchik.merchik.Utils;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

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
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.LogMPRealm;
import ua.com.merchik.merchik.database.room.DaoInterfaces.LocationDevicesDao;
import ua.com.merchik.merchik.trecker;

/**
 * Слепок Wi-Fi + Bluetooth только в момент клика "Начать работы".
 * Никаких сканов заранее, никаких фоновых продолжений.
 */
public final class WorkStartNetworkSnapshot {

    private static final class WifiLine {
        final String line;
        final String bssid; // hex "AA:BB:.."

        WifiLine(String line, String bssid) {
            this.line = line;
            this.bssid = bssid;
        }
    }


    private WorkStartNetworkSnapshot() {
    }

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

        public boolean isOk() {
            return error == null;
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN})
    public static void captureAndLog(
            Context context,
            WpDataDB wpDataDB,
            int tema_id,
            DistanceFn distanceFn,
            ResultCallback callback
    ) {
        double shopLat = parseDoubleSafe(wpDataDB.getAddr_location_xd());
        double shopLng = parseDoubleSafe(wpDataDB.getAddr_location_yd());

        if (Double.isNaN(shopLat) || Double.isNaN(shopLng)) {
            callback.onDone(new Result(0, 0, "Запись не выполнена: координаты магазина не указаны"));
            return;
        }

        // mocking (как у тебя)
        boolean isMocking = (trecker.isMockGPS || trecker.isMockNET);
        if (isMocking) {
            callback.onDone(new Result(0, 0, "Запись не выполнена: включено фиктивное местоположение"));
            return;
        }

        // perms
        if (!hasWifiPerms(context)) {
            callback.onDone(new Result(0, 0, "У Вашего телефона выключен Wi-Fi и система не может использовать эту " +
                    "технологию, для определения вашего местоположения в ТТ. " +
                    "Включите Wi-Fi и повторите попытку."));
            return;
        }

        // 1) текущие координаты
        double curLat = Globals.CoordX;
        double curLng = Globals.CoordY;

        float distNow = distanceFn.distanceMeters(curLat, curLng, shopLat, shopLng);

        // 2) если текущие не "на месте" — пробуем MP
        double snapX = curLat;
        double snapY = curLng;
        long snapTime = System.currentTimeMillis(); // время “клика/слепка”

        if (distNow > 350f) {
            long endSec = System.currentTimeMillis() / 1000L;
            long startSec = endSec - 10 * 60L;

            LogMPDB nearest = getNearestMpPointSorted(shopLat, shopLng, startSec, endSec);
            if (nearest == null) {
                callback.onDone(new Result(0, 0, "Запись не выполнена: нет координат за последние 10 минут"));
                return;
            }

            if (nearest.distance > 200) {
                callback.onDone(new Result(0, 0, "Запись не выполнена: вы дальше 100м от магазина, расстояние: " + nearest.distance));
                return;
            }

            // ✅ вот эти координаты и время считаем “актуальными”
            snapX = nearest.CoordX;
            snapY = nearest.CoordY;
            snapTime = nearest.CoordTime; // у тебя есть поле в LogMPDB
        }

        // 3) запускаем слепок Wi-Fi/BT
        SnapshotSession session = new SnapshotSession(
                context.getApplicationContext(), wpDataDB, tema_id, callback,
                snapX, snapY, snapTime
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

        private final ArrayList<WifiLine> wifiLines = new ArrayList<>();
        private final LinkedHashMap<String, BtItem> btUnique = new LinkedHashMap<>();

        private boolean wifiDone = false;
        private boolean btDone = false;

        private BroadcastReceiver wifiReceiver;
        private BluetoothLeScanner bleScanner;
        private ScanCallback bleCallback;

        private Runnable wifiTimeout;
        private Runnable btStop;
        private LocationDevicesDao locationDevicesDao;
        private final int addrId;

        private final double snapX;
        private final double snapY;
        private final long snapTime;


        SnapshotSession(Context app, WpDataDB wp, int temaId, ResultCallback callback,
                        double snapX, double snapY, long snapTime){
            this.app = app;
            this.wp = wp;
            this.temaId = temaId;
            this.callback = callback;

            this.snapX = snapX;
            this.snapY = snapY;
            this.snapTime = snapTime;

            this.wifiManager = (WifiManager) app.getSystemService(Context.WIFI_SERVICE);

            BluetoothManager bm = (BluetoothManager) app.getSystemService(Context.BLUETOOTH_SERVICE);
            this.btAdapter = bm != null ? bm.getAdapter() : null;
            locationDevicesDao = SQL_DB.locationDevicesDao();
            this.addrId = wp.getAddr_id();
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
            if (wifiManager == null) {
                wifiDone = true;
                return;
            }

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
                        wifiLines.addAll(buildWifiLinesFromScanResults(wifiManager,
                                WIFI_LIMIT,
                                locationDevicesDao,
                                addrId,
                                snapX,
                                snapY,
                                snapTime));
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
            try {
                started = wifiManager.startScan();
            } catch (Exception e) {
                started = false;
            }

            if (!started) {
                // читаем кеш и завершаем
                wifiLines.addAll(buildWifiLinesFromScanResults(wifiManager,
                        WIFI_LIMIT,
                        locationDevicesDao,
                        addrId,
                        snapX,
                        snapY,
                        snapTime));
                safeUnregisterWifi();
                wifiDone = true;
                return;
            }

            wifiTimeout = () -> {
                wifiLines.addAll(buildWifiLinesFromScanResults(wifiManager,
                        WIFI_LIMIT,
                        locationDevicesDao,
                        addrId,
                        snapX,
                        snapY,
                        snapTime));
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
                try {
                    app.unregisterReceiver(wifiReceiver);
                } catch (Exception ignored) {
                }
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

                    // ✅ фильтр по таблице
                    if (locationDevicesDao != null && locationDevicesDao.existsByMacAndAddrIdNoCase(addr, addrId)) {
                        return;
                    }

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
                try {
                    bleScanner.stopScan(bleCallback);
                } catch (Exception ignored) {
                }
            }
            bleCallback = null;
            bleScanner = null;
        }

        // ---- FINISH ----
        private void tryFinish() {
            if (!wifiDone || !btDone) return;

            // Пишем ТОЛЬКО WiFi/Bluetooth строки
            int wifiAdded = 0;
            for (WifiLine it : wifiLines) {
                if (writeOneLineLog(wp, temaId, it.line, it.bssid)) wifiAdded++;
            }

            int btAdded = 0;
            for (WifiLine it : buildBluetoothLines(btUnique)) {
                if (writeOneLineLog(wp, temaId, it.line, it.bssid)) btAdded++;
            }

            // Если ничего не записали — дадим внятную причину
            String error = null;
            if (wifiAdded == 0 && btAdded == 0) {
                // максимально коротко, без лишних данных
                if (wifiManager != null && !wifiManager.isWifiEnabled()) {
                    error = "У Вашего телефона выключен Wi-Fi и система не может использовать эту " +
                            "технологию, для определения вашего местоположения в ТТ. " +
                            "Включите Wi-Fi ы повторите попытку.";
                } else if (btAdapter != null && !btAdapter.isEnabled()) {
                    error = "У Вашего телефона выключен Bluetooth и система не может использовать эту " +
                            "технологию, для определения вашего местоположения в ТТ. " +
                            "Включите Bluetooth ы повторите попытку.";
                } else {
                    error = "Не найдено ни одного устройства Wi-Fi/Bluetooth. " +
                            "Попробуйте подойти ближе к кассе или другой кассе и повторить попытку.";
                }
            }
            callback.onDone(new Result(wifiAdded, btAdded, error));
        }
    }

    // ---------------- Builders / Format ----------------

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    private static List<WifiLine> buildWifiLinesFromScanResults(
            WifiManager wifiManager,
            int limit,
            LocationDevicesDao dao,
            long addrId,
            double x,
            double y,
            long time
    ) {
        ArrayList<WifiLine> lines = new ArrayList<>();
        List<android.net.wifi.ScanResult> results;

        try {
            results = wifiManager.getScanResults();
        } catch (Exception e) {
            return lines;
        }

        if (results == null || results.isEmpty()) return lines;

        // unique by BSSID внутри одного скана
        HashMap<String, android.net.wifi.ScanResult> uniq = new HashMap<>();
        for (android.net.wifi.ScanResult r : results) {
            if (r == null) continue;
            if (r.BSSID == null || r.BSSID.isEmpty()) continue;

            android.net.wifi.ScanResult prev = uniq.get(r.BSSID);
            if (prev == null || r.level > prev.level) uniq.put(r.BSSID, r);
        }

        ArrayList<android.net.wifi.ScanResult> uniqList = new ArrayList<>(uniq.values());
        uniqList.sort((a, b) -> Integer.compare(b.level, a.level));

        int realLimit = (limit <= 0) ? uniqList.size() : Math.min(limit, uniqList.size());
        for (int i = 0; i < realLimit; i++) {
            android.net.wifi.ScanResult r = uniqList.get(i);

            String mac = r.BSSID; // MAC точки доступа
            // ✅ фильтр по таблице: если уже есть — не добавляем
            if (dao != null && dao.existsByMacAndAddrIdNoCase(mac, addrId)) {
                continue;
            }

            String line =
                    "WiFi | SSID=" + safe(r.SSID) +
                            " | BSSID=" + safe(r.BSSID) +
                            " | level=" + r.level + "dBm" +
                            " | freq=" + r.frequency + "MHz" +
                            " | DX=" + x +
                            " | DY=" + y +
                            " | time=" + time;

            lines.add(new WifiLine(line, mac));
        }


        return lines;
    }


    private static List<WifiLine> buildBluetoothLines(LinkedHashMap<String, BtItem> uniq) {
        ArrayList<WifiLine> out = new ArrayList<>();
        if (uniq == null || uniq.isEmpty()) return out;

        for (BtItem it : uniq.values()) {
            out.add(new WifiLine("Bluetooth | name=" + safe(it.name) +
                    " | addr=" + safe(it.addr) +
                    " | level=" + it.rssi + "dBm",it.addr));
        }
        return out;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

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
    private static boolean writeOneLineLog(WpDataDB wpDataDB, int tema_id, String commentLine, String macWiFi16f) {
        if (commentLine == null || commentLine.trim().isEmpty()) return false;

        int addr_id = wpDataDB.getAddr_id();
        int user_id = wpDataDB.getUser_id();
        long dad2 = wpDataDB.getCode_dad2();
        String client_id = wpDataDB.getClient_id();
        java.util.Date wpDate = wpDataDB.getDt();
        String macWiFiNumeric = macToFixedDecimalKey(macWiFi16f);

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
                        String.valueOf(wpDate),
                        macWiFiNumeric,
                        String.valueOf(tema_id)
                )
        ));
        return true;
    }

    // ---------------- Permissions / Utils ----------------

    public static boolean hasWifiPerms(Context ctx) {
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
        try {
            return Double.parseDouble(s.trim().replace(',', '.'));
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private static LogMPDB getNearestMpPointSorted(
            double coordAddrX, double coordAddrY,
            long startTimeSec, long endTimeSec
    ) {
        List<LogMPDB> logMPList = LogMPRealm.getLogMPTime(startTimeSec * 1000, endTimeSec * 1000);

        if (logMPList != null && logMPList.size() > 0) {
            for (LogMPDB item : logMPList) {
                // как у тебя: считаем distance и пишем в item.distance
                double distance = coordinatesDistanse(coordAddrX, coordAddrY, item.CoordX, item.CoordY);
                item.distance = (int) distance;
            }

            LogMPRealm.setLogMP(logMPList);

            // сортируем по distance
            Collections.sort(logMPList, new Comparator<LogMPDB>() {
                @Override
                public int compare(LogMPDB o1, LogMPDB o2) {
                    return Integer.compare(o1.distance, o2.distance);
                }
            });

            return logMPList.get(0); // ближайший
        }

        return null;
    }

    public static float coordinatesDistanse(double latA, double lngA, double latB, double lngB) {

        float res;

        Location locationA = new Location("point A");

        locationA.setLatitude(latA);
        locationA.setLongitude(lngA);

        Location locationB = new Location("point B");

        locationB.setLatitude(latB);
        locationB.setLongitude(lngB);

        res = locationA.distanceTo(locationB);

        //System.out.println("TEST.DISTANCE: " + res);

        return res;
    }

    public static String macToFixedDecimalKey(String mac) {
        if (mac == null) return null;

        String[] parts = mac.trim().toUpperCase().split(":");
        if (parts.length != 6) return "1000000000000000000";

        StringBuilder sb = new StringBuilder(19);
        sb.append('1');
        for (String p : parts) {
            if (p.length() != 2) return "1000000000000000000";
            int v;
            try {
                v = Integer.parseInt(p, 16);
            } catch (NumberFormatException e) {
                return "1000000000000000000";
            }
            sb.append(String.format(java.util.Locale.US, "%03d", v));
        }
        return sb.toString();
    }


}


