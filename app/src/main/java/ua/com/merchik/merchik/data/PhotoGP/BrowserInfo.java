package ua.com.merchik.merchik.data.PhotoGP;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BrowserInfo {
    @SerializedName("hardwareConcurrency")
    @Expose
    public String hardwareConcurrency;
    @SerializedName("version_app")
    @Expose
    public String versionApp;
    @SerializedName("maxTouchPoints")
    @Expose
    public String maxTouchPoints;
    @SerializedName("platform")
    @Expose
    public String platform;

}
