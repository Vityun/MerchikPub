package ua.com.merchik.merchik.data.PhotoGP;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConnectionInfo {
    @SerializedName("effectiveType")
    @Expose
    public String effectiveType;
    @SerializedName("rtt")
    @Expose
    public String rtt;
    @SerializedName("downlinkMax")
    @Expose
    public String downlinkMax;
    @SerializedName("downlink")
    @Expose
    public String downlink;
    @SerializedName("type")
    @Expose
    public String type;
}
