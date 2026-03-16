package ua.com.merchik.merchik.Utils;

import static ua.com.merchik.merchik.features.maps.domain.UtilitiesKt.parseDoubleSafe;

import android.location.Location;

import java.util.List;

import javax.annotation.Nullable;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class GPSUtils {

    @Nullable
    public static double[] parseWpPoint(@Nullable WpDataDB wp) {
        if (wp == null) return null;

        try {
            Double lat = parseDoubleSafe(wp.getAddr_location_xd());
            Double lon = parseDoubleSafe(wp.getAddr_location_yd());

            if (lat == null || lon == null) return null;
            if (lat < -90.0 || lat > 90.0) return null;
            if (lon < -180.0 || lon > 180.0) return null;

            return new double[]{lat, lon};
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "RealmManager.parseWpPoint", "Exception: " + e);
            return null;
        }
    }

    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(Math.toRadians(lat1)) *
                                Math.cos(Math.toRadians(lat2)) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

}
