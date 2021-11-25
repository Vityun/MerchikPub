package ua.com.merchik.merchik.data.PhotoGP;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Coords {
    @SerializedName("altitude")
    @Expose
    public String altitude;
    @SerializedName("heading")
    @Expose
    public String heading;
    @SerializedName("latitude")
    @Expose
    public String latitude;
    @SerializedName("trusted_location")
    @Expose
    public String trustedLocation;
    @SerializedName("accuracy")
    @Expose
    public String accuracy;
    @SerializedName("altitudeAccuracy")
    @Expose
    public String altitudeAccuracy;
    @SerializedName("speed")
    @Expose
    public String speed;
    @SerializedName("longitude")
    @Expose
    public String longitude;
}
