package ua.com.merchik.merchik.data.ServerData.TARCommentsData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;

public class AdditionalRequirementsMarksServerData {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("list")
    @Expose
    private List<AdditionalRequirementsMarkDB> list = null;
    @SerializedName("error")
    @Expose
    private String error;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<AdditionalRequirementsMarkDB> getList() {
        return list;
    }

    public void setList(List<AdditionalRequirementsMarkDB> list) {
        this.list = list;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
