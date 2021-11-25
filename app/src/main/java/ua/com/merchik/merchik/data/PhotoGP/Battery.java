package ua.com.merchik.merchik.data.PhotoGP;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Battery {
    @SerializedName("battery_level")
    @Expose
    public String batteryLevel;
}
