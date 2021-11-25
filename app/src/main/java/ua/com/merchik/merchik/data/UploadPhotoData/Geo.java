package ua.com.merchik.merchik.data.UploadPhotoData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Geo {
    @SerializedName("coords")
    @Expose
    private Coords coords;
    @SerializedName("device_time")
    @Expose
    private String deviceTime;

    public Coords getCoords() {
        return coords;
    }

    public void setCoords(Coords coords) {
        this.coords = coords;
    }

    public String getDeviceTime() {
        return deviceTime;
    }

    public void setDeviceTime(String deviceTime) {
        this.deviceTime = deviceTime;
    }
}
