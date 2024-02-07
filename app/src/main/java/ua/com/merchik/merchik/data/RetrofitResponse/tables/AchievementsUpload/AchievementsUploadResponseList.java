package ua.com.merchik.merchik.data.RetrofitResponse.tables.AchievementsUpload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AchievementsUploadResponseList {
    @SerializedName("state")
    @Expose
    public Boolean state;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("element_id")
    @Expose
    public Integer elementId;
}
