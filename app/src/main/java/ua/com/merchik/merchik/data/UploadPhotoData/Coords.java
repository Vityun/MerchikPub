package ua.com.merchik.merchik.data.UploadPhotoData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Coords {
    @SerializedName("altitude")
    @Expose
    private String altitude;
    @SerializedName("heading")
    @Expose
    private String heading;
    @SerializedName("latitude")
    @Expose
    private String latitude;
    @SerializedName("trusted_location")
    @Expose
    private String trustedLocation;
    @SerializedName("accuracy")
    @Expose
    private String accuracy;
    @SerializedName("altitudeAccuracy")
    @Expose
    private String altitudeAccuracy;
    @SerializedName("speed")
    @Expose
    private String speed;
    @SerializedName("longitude")
    @Expose
    private String longitude;

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getTrustedLocation() {
        return trustedLocation;
    }

    public void setTrustedLocation(String trustedLocation) {
        this.trustedLocation = trustedLocation;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public String getAltitudeAccuracy() {
        return altitudeAccuracy;
    }

    public void setAltitudeAccuracy(String altitudeAccuracy) {
        this.altitudeAccuracy = altitudeAccuracy;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
