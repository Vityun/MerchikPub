package ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirementsMarks;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdditionalRequirementsSendMarksServerData {
    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("list")
    @Expose
    private java.util.List<AdditionalRequirementsMarksListServerData> list = null;
    @SerializedName("error")
    @Expose
    private String error;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public java.util.List<AdditionalRequirementsMarksListServerData> getList() {
        return list;
    }

    public void setList(java.util.List<AdditionalRequirementsMarksListServerData> list) {
        this.list = list;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
