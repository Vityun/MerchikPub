package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReportHint {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("list")
    @Expose
    private List<ReportHintList> list = null;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<ReportHintList> getList() {
        return list;
    }

    public void setList(List<ReportHintList> list) {
        this.list = list;
    }
}
