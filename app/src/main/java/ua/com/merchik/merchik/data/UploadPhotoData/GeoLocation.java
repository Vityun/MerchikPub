package ua.com.merchik.merchik.data.UploadPhotoData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GeoLocation {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("geo_id")
    @Expose
    private String geoId;
    @SerializedName("geo_accuracy")
    @Expose
    private Integer geoAccuracy;
    @SerializedName("geo")
    @Expose
    private Geo geo;
    @SerializedName("detect_addr")
    @Expose
    private DetectAddr detectAddr;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public String getGeoId() {
        return geoId;
    }

    public void setGeoId(String geoId) {
        this.geoId = geoId;
    }

    public Integer getGeoAccuracy() {
        return geoAccuracy;
    }

    public void setGeoAccuracy(Integer geoAccuracy) {
        this.geoAccuracy = geoAccuracy;
    }

    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    public DetectAddr getDetectAddr() {
        return detectAddr;
    }

    public void setDetectAddr(DetectAddr detectAddr) {
        this.detectAddr = detectAddr;
    }

}
