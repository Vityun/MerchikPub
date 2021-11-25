package ua.com.merchik.merchik.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LogMPResponce {

    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("geo_id")
    @Expose
    private String geoId;

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
}
