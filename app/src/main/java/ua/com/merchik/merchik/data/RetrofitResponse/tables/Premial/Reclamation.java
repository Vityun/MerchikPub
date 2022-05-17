package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Reclamation {
    @SerializedName("active")
    @Expose
    public List<String> active = null;
    @SerializedName("total_sum_premiya")
    @Expose
    public List<Object> totalSumPremiya = null;
    @SerializedName("total_sum_penalty")
    @Expose
    public List<Object> totalSumPenalty = null;
    @SerializedName("unrespond")
    @Expose
    public List<String> unrespond = null;
    @SerializedName("unread")
    @Expose
    public List<String> unread = null;
    @SerializedName("finished")
    @Expose
    public List<String> finished = null;
}
