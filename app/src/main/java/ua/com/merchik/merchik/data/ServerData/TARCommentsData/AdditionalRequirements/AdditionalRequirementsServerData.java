package ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirements;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;

public class AdditionalRequirementsServerData {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("list")
    @Expose
    private List<AdditionalRequirementsDB> list = null;
    @SerializedName("error")
    @Expose
    private String error;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<AdditionalRequirementsDB> getList() {
        return list;
    }

    public void setList(List<AdditionalRequirementsDB> list) {
        this.list = list;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
