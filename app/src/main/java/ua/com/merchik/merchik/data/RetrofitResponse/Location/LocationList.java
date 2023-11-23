package ua.com.merchik.merchik.data.RetrofitResponse.Location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationList {
    @SerializedName("ID")
    @Expose
    public Long id;

    @SerializedName("dt")
    @Expose
    public Long dt;

    @SerializedName("dt_user")
    @Expose
    public Long dtUser;

    @SerializedName("dt_diff")
    @Expose
    public Long dtDiff;

    @SerializedName("dt_device")
    @Expose
    public Long dtDevice;

    @SerializedName("offline_data")
    @Expose
    public Integer offlineData;

    @SerializedName("location_is_fake")
    @Expose
    public Integer locationIsFake;

    @SerializedName("source_id")
    @Expose
    public Integer sourceId;

    @SerializedName("ip")
    @Expose
    public String ip;

    @SerializedName("user_id")
    @Expose
    public Integer userId;

    @SerializedName("lat")
    @Expose
    public Float lat;

    @SerializedName("lon")
    @Expose
    public Float lon;

    @SerializedName("accuracy")
    @Expose
    public Integer accuracy;

    @SerializedName("altitude")
    @Expose
    public Integer altitude;

    @SerializedName("altitude_accuracy")
    @Expose
    public Integer altitudeAccuracy;

    @SerializedName("heading")
    @Expose
    public Float heading;

    @SerializedName("speed")
    @Expose
    public Float speed;

    @SerializedName("device_id")
    @Expose
    public String deviceId;

    @SerializedName("battery_level")
    @Expose
    public Integer batteryLevel;

    @SerializedName("battery_chargingtime")
    @Expose
    public Integer batteryChargingtime;

    @SerializedName("battery_dischargingtime")
    @Expose
    public Integer batteryDischargingtime;

    @SerializedName("battery_charging")
    @Expose
    public Integer batteryCharging;

    @SerializedName("connection_type")
    @Expose
    public String connectionType;

    @SerializedName("platform_id")
    @Expose
    public Integer platformId;

    @SerializedName("session_id")
    @Expose
    public Long sessionId;
}
