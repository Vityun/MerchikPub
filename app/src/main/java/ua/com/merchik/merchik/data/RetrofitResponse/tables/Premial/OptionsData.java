package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OptionsData {
    @SerializedName("sum_penalty")
    @Expose
    public Object sumPenalty;
    @SerializedName("penalty_date_from")
    @Expose
    public String penaltyDateFrom;
    @SerializedName("penalty_date_to")
    @Expose
    public String penaltyDateTo;
}
