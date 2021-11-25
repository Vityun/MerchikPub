package ua.com.merchik.merchik.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.PPADB;

public class PPAonResponse {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("list")
    @Expose
    private List<PPADB> list = null;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<PPADB> getList() {
        return list;
    }

    public void setList(List<PPADB> list) {
        this.list = list;
    }
}
