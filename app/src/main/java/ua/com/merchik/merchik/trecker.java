package ua.com.merchik.merchik;

import static android.content.Context.LOCATION_SERVICE;
import static ua.com.merchik.merchik.Globals.dalayMaxTimeGPS;
import static ua.com.merchik.merchik.Globals.provider;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.ContextCompat;

import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;


public class trecker implements LocationListener {

    //private LocationManager locationManager; // FOR VISIBILITY
    public static Location imHereGPS; // GPS coordinates
    public static Location imHereNET; // Network coordinates
    public static boolean enabledGPS;
    static boolean enabledNET;
    static boolean isMockGPS;
    static boolean isMockNET;
    //static boolean isMock;

    public static String locationUniqueStringGPS = "";
    public static String locationUniqueStringGSM = "";

    public static boolean switchedOff = true; // Выключен

    public static LocationListener locationListener;

    public static LocationManager locationManager;

    static void SetUpLocationListener(Context context) {

        try {
            Log.e("GPS_LISTENER", "HERE");

            // Запрашивает доступы если API больше 23
            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            // Создание гео-менеджера
            locationManager = (LocationManager)
                    context.getSystemService(LOCATION_SERVICE);
            locationListener = new trecker();

            // Обновление гео-данных
            if (locationManager != null) {
//            if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))


//            if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
                /*
                06.05.2025 изменил частоту обновления с 0 секунд и 0 метров на 5 сек или 10 метров
                 */
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        4000L, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        4000L, 0, locationListener); // 4 сек или 0 метров
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                        0, 0, locationListener);

                Globals.writeToMLOG("INFO", "trecker/SetUpLocationListener/", "locationManager: " + locationManager);

                // Запись местоположения для дальнейшего использования
                imHereNET = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Globals.writeToMLOG("INFO", "trecker/SetUpLocationListener/", "imHereNET: " + imHereNET);

                imHereGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                Globals.writeToMLOG("INFO", "trecker/SetUpLocationListener/", "imHereGPS: " + imHereGPS);


                // Включены ли службы местоопределения
                enabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                Globals.writeToMLOG("INFO", "trecker/SetUpLocationListener/", "enabledGPS: " + enabledGPS);

                enabledNET = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                Globals.writeToMLOG("INFO", "trecker/SetUpLocationListener/", "enabledNET: " + enabledNET);

                isMockSettingsON();
            }

            switchedOff = false;

        } catch (Exception e) {
            // todo запись в лог
        }
    }

    public static void stopTracking(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        switchedOff = true;
    }

    @Override
    public void onLocationChanged(Location loc) {
        System.out.println("TEST.GPS_LOCATION: " + loc.getTime());
        showLocation(loc);
    }

    @Override
    public void onProviderDisabled(String provider) {
//        checkConnection();
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    // Отображение и запись координат
    private void showLocation(Location location) {
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            imHereGPS = location;
            Log.e("TEST.GPS_LOCATION", "GPS: " + imHereGPS + " GPStime: " + imHereGPS.getTime());

        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            imHereNET = location;
            Log.e("TEST.GPS_LOCATION", "NET: " + imHereNET + " NETtime: " + imHereNET.getTime());
        }
    }

    // Проверка: включён ли мокинг
    static void isMockSettingsON() {
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            if (imHereGPS != null) {
                isMockGPS = imHereGPS.isFromMockProvider();
            }
            if (imHereNET != null) {
                isMockNET = imHereNET.isFromMockProvider();
            }
        }
    }

    public boolean isMockGPS(Context context) {
        boolean mock = true;
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            if (imHereGPS != null) {
                mock = imHereGPS.isFromMockProvider();
            }
        } else {
            mock = !Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
        }
        return mock;
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


    /**
     * Метод для получения геоданных сети в случае если GPS-данные не доступны
     * <p>
     * Это дефолтная реализация.
     */
    public static int Coordinates() {
        Globals globals = new Globals();
        Log.e("КООРДИНАТЫ", "tracker/imHereGPS: " + trecker.imHereGPS);
        Log.e("КООРДИНАТЫ", "tracker/imHereNET: " + trecker.imHereNET);

        int provider;// 0=NULL; 1=GPS; 2=NET.

        // Передача параметра с подделкой координат
        if (trecker.isMockGPS || trecker.isMockNET) {
            Globals.mocking = 0;
        } else {
            Globals.mocking = 1;
        }

        globals.writeToMLOG(Clock.getHumanTime() + " Coordinates.trecker.imHereGPS: " + trecker.imHereGPS + "\n");
        globals.writeToMLOG(Clock.getHumanTime() + " Coordinates.trecker.imHereNET: " + trecker.imHereNET + "\n");


        // Сохранение текущего состояния GPS
        if (trecker.imHereGPS != null /*&& outOfDayCoordinates(System.currentTimeMillis(), trecker.imHereGPS.getTime())*/) {
            provider = 1;
            Globals.providerType = 1;
            Globals.locationGPS = imHereGPS;

            Globals.CoordX = imHereGPS.getLatitude();
            Globals.CoordY = imHereGPS.getLongitude();
            Globals.CoordTime = imHereGPS.getTime();
            Globals.CoordSpeed = imHereGPS.getSpeed();
            Globals.CoordAltitude = imHereGPS.getAltitude();
            Globals.CoordAccuracy = imHereGPS.getAccuracy();
            Globals.provider = provider;

            globals.writeToMLOG(Clock.getHumanTime() + " Coordinates.provider: " + provider + "\n");

            return provider;
        } else if (trecker.imHereNET != null) {
            provider = 2;
            Globals.providerType = 2;
//            Globals.provider = 2;
            Globals.locationNET = imHereNET;

            Globals.CoordX = imHereNET.getLatitude();
            Globals.CoordY = imHereNET.getLongitude();
            Globals.CoordTime = imHereNET.getTime();
            Globals.CoordSpeed = imHereNET.getSpeed();
            Globals.CoordAltitude = imHereNET.getAltitude();
            Globals.CoordAccuracy = imHereNET.getAccuracy();

            globals.writeToMLOG(Clock.getHumanTime() + " Coordinates.provider: " + provider + "\n");
            return provider;
        } else {
            // Никаких координат нет
//            Globals.provider = 0;
            provider = 0;

            globals.writeToMLOG(Clock.getHumanTime() + " Coordinates.provider: " + provider + "\n");
            return provider;
        }
    }


    public static boolean outOfDayCoordinates(long deviceTime, long coordTime) {
        try {
            long res = Math.abs(deviceTime - coordTime) / 1000 / 60;    // /1000 - переводим в секунды /60 - Переводим в минуты
            if (res < 30) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 10.08.23.
     * Специальная функция для переписывания координат, если надо для кого-то отредактировать
     * время устаревания
     */
    public static void Coordinates(WpDataDB wpDataDB) {
        try {
            // Передача параметра с подделкой координат
            if (trecker.isMockGPS || trecker.isMockNET) {
                Globals.mocking = 0;
            } else {
                Globals.mocking = 1;
            }

            if (trecker.imHereGPS != null && outOfDayCoordinates(System.currentTimeMillis(), trecker.imHereGPS.getTime(), calculateMinutes(wpDataDB))) {
                provider = 1;
                Globals.providerType = 1;
                Globals.locationGPS = imHereGPS;

                Globals.CoordX = imHereGPS.getLatitude();
                Globals.CoordY = imHereGPS.getLongitude();
                Globals.CoordTime = imHereGPS.getTime();
                Globals.CoordSpeed = imHereGPS.getSpeed();
                Globals.CoordAltitude = imHereGPS.getAltitude();
                Globals.CoordAccuracy = imHereGPS.getAccuracy();

                Globals.writeToMLOG("INFO", "Coordinates/NEW", "imHereGPS: " + imHereGPS);
            }else if (outOfDayCoordinates(System.currentTimeMillis(), trecker.imHereGPS.getTime(), calculateMinutes(wpDataDB))){
                provider = 1;
                Globals.providerType = 1;
                Globals.locationGPS = imHereGPS;

                Globals.CoordX = imHereGPS.getLatitude();
                Globals.CoordY = imHereGPS.getLongitude();
                Globals.CoordTime = imHereGPS.getTime();
                Globals.CoordSpeed = imHereGPS.getSpeed();
                Globals.CoordAltitude = imHereGPS.getAltitude();
                Globals.CoordAccuracy = imHereGPS.getAccuracy();
            }else {
                Globals.writeToMLOG("INFO", "Coordinates/NEW/else", "imHereGPS: " + imHereGPS);

                Globals.CoordX = 0;
                Globals.CoordY = 0;
                Globals.CoordTime = 0;
                Globals.CoordSpeed = 0;
                Globals.CoordAltitude = 0;
                Globals.CoordAccuracy = 0;
                Globals.provider = 0;
            }
        }catch (Exception e){
            Globals.writeToMLOG("INFO", "Coordinates/NEW", "Exception e: " + e);
        }
    }

    /**
     * 10.08.23.
     * То самое место, где будет решаться сколько время будет актуальны координаты. Все условия тут.
     * */
    private static int calculateMinutes(WpDataDB wpDataDB) {
        int res = 30; // Минуты по умолчанию

        UsersSDB usersSDB = SQL_DB.usersDao().getById(wpDataDB.getUser_id());

        // 1. Первое условие.
        if (usersSDB.clientId.equals(wpDataDB.getClient_id()) && wpDataDB.getClient_id().equals("32246")){
            res = 45;   // Для мерчей Ласунки(32246) можно просрочить координаты на 45 минут.
            dalayMaxTimeGPS = res;
        }

        return res;
    }

    /**
     * 10.08.23.
     *
     * @param deviceTime время устройства в миллисекундах. Или время от которого будем расчитывать
     *                   "устарелость" координат.
     * @param coordTime  Время когда получены последние координаты.
     * @param minutes    Минуты которые разрешены для того что б координаты считать устаревшими.
     */
    public static boolean outOfDayCoordinates(long deviceTime, long coordTime, int minutes) {
        try {
            long res = Math.abs(deviceTime - coordTime) / 1000 / 60;    // /1000 - переводим в секунды /60 - Переводим в минуты
            Globals.writeToMLOG("INFO", "outOfDayCoordinates", "res: " + res + " minutes: " + minutes + " coordTime: " + coordTime);
            if (res < minutes) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Globals.writeToMLOG("INFO", "outOfDayCoordinates", "Exception e: " + e);
            return false;
        }
    }

    /**
     * 24.08.23.
     *
     * @param deviceTime время устройства в миллисекундах. Или время от которого будем расчитывать
     *                   "устарелость" координат.
     * @param coordTime  Время когда получены последние координаты.
     * @param minutes    Минуты которые разрешены для того что б координаты считать устаревшими.
     */
    public static long howOldCoordinates(long deviceTime, long coordTime, int minutes){
        try {
            return Math.abs(deviceTime - coordTime) / 1000 / 60;    // /1000 - переводим в секунды /60 - Переводим в минуты
        }catch (Exception e){
            return 0L;
        }
    }

}
