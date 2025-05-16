package ua.com.merchik.merchik.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import androidx.core.content.ContextCompat;
import android.Manifest;

public class LocationUtils {

    // Проверка, есть ли разрешение на геолокацию
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Проверка, включен ли GPS
    public static boolean isGPSEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // Проверка всего: и разрешения, и GPS
    public static boolean canUseLocationServices(Context context) {
        return hasLocationPermission(context) && isGPSEnabled(context);
    }
}