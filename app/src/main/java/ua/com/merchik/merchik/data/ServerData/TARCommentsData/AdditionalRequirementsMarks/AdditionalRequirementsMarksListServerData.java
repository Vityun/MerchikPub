package ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirementsMarks;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdditionalRequirementsMarksListServerData {
    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("notice")
    @Expose
    public String notice;

    @SerializedName("total_scores_text")
    @Expose
    public String totalScoresText;

    @SerializedName("log_title")
    @Expose
    public String logTitle;

    @SerializedName("element_id")
    @Expose
    public Integer elementId;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

}
