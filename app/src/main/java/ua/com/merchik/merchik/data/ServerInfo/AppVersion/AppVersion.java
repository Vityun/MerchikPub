package ua.com.merchik.merchik.data.ServerInfo.AppVersion;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AppVersion {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }
}
