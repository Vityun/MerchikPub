package ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirementsMarks;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdditionalRequirementsMarksListServerData {
    @SerializedName("state")
    @Expose
    private Boolean state;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

}
