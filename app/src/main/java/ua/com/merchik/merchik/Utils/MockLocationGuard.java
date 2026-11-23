package ua.com.merchik.merchik.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;

import androidx.core.location.LocationCompat;

import ua.com.merchik.merchik.trecker;

public final class MockLocationGuard {
    public static final String MESSAGE = "Отримано фіктивні дані місцеположення. Перебування в торговій точці не підтверджено.";
    public static final String INSTRUCTIONS = "У налаштуваннях розробника відкрийте «Додаток для фіктивного місцеположення» та виберіть «Немає».";

    private MockLocationGuard() {}

    public static boolean isActive(Context context) {
        LocationManager manager = context == null ? null : context.getSystemService(LocationManager.class);
        return mocked(manager, LocationManager.GPS_PROVIDER, trecker.imHereGPS)
                || mocked(manager, LocationManager.NETWORK_PROVIDER, trecker.imHereNET)
                || mocked(manager, "fused", null)
                || mocked(manager, LocationManager.PASSIVE_PROVIDER, null);
    }

    @SuppressLint("MissingPermission")
    private static boolean mocked(LocationManager manager, String provider, Location delivered) {
        Location cached = null;
        try {
            if (manager != null) cached = manager.getLastKnownLocation(provider);
        } catch (SecurityException | IllegalArgumentException ignored) {
            // Permission or provider may be unavailable. Still check a recently delivered fix.
        }
        return LocationFixPolicy.latestProviderFixMock(
                cached == null ? -1 : cached.getElapsedRealtimeNanos(), cached != null && LocationCompat.isMock(cached),
                delivered == null ? -1 : delivered.getElapsedRealtimeNanos(), delivered != null && LocationCompat.isMock(delivered),
                SystemClock.elapsedRealtimeNanos());
    }
}
