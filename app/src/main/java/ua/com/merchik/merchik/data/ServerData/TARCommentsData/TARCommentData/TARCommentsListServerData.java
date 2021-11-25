package ua.com.merchik.merchik.data.ServerData.TARCommentsData.TARCommentData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TARCommentsListServerData {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("info")
    @Expose
    private TARCommentsInfoServerData info;

    @SerializedName("error")
    @Expose
    public String error;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public TARCommentsInfoServerData getInfo() {
        return info;
    }

    public void setInfo(TARCommentsInfoServerData info) {
        this.info = info;
    }
}
