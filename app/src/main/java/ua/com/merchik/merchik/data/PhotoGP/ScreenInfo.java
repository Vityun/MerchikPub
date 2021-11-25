package ua.com.merchik.merchik.data.PhotoGP;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ScreenInfo {
    @SerializedName("keepAwake")
    @Expose
    public String keepAwake;
    @SerializedName("availWidth")
    @Expose
    public String availWidth;
    @SerializedName("orientation_type")
    @Expose
    public String orientationType;
    @SerializedName("width")
    @Expose
    public String width;
    @SerializedName("orientation_angle")
    @Expose
    public String orientationAngle;
    @SerializedName("availHeight")
    @Expose
    public String availHeight;
    @SerializedName("height")
    @Expose
    public String height;
}
