package ua.com.merchik.merchik.data.PhotoGP;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PhotoGP {
    @SerializedName("browser_info")
    @Expose
    public BrowserInfo browserInfo;
    @SerializedName("connection_info")
    @Expose
    public ConnectionInfo connectionInfo;
    @SerializedName("screen_info")
    @Expose
    public ScreenInfo screenInfo;
    @SerializedName("battery")
    @Expose
    public Battery battery;
    @SerializedName("coords")
    @Expose
    public Coords coords;
    @SerializedName("device_time")
    @Expose
    public String deviceTime;
    @SerializedName("timestamp")
    @Expose
    public String timestamp;

}
