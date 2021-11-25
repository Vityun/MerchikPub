package ua.com.merchik.merchik.data.ServerData.TARCommentsData.TARCommentData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TARCommentsServerData {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("list")
    @Expose
    private List<TARCommentsListServerData> list = null;
    @SerializedName("error")
    @Expose
    private String error;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<TARCommentsListServerData> getList() {
        return list;
    }

    public void setList(List<TARCommentsListServerData> list) {
        this.list = list;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
