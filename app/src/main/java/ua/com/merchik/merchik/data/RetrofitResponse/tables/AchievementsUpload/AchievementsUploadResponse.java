package ua.com.merchik.merchik.data.RetrofitResponse.tables.AchievementsUpload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AchievementsUploadResponse {

    @SerializedName("state")
    @Expose
    public Boolean state;
    @SerializedName("list")
    @Expose
    public List<AchievementsUploadResponseList> list;
}
