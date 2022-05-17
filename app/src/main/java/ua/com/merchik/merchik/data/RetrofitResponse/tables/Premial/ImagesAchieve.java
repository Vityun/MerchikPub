package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ImagesAchieve {
    @SerializedName("total_cnt")
    @Expose
    public Object totalCnt;
    @SerializedName("total_confirm")
    @Expose
    public Object totalConfirm;
    @SerializedName("total_good_score")
    @Expose
    public Object totalGoodScore;
}
