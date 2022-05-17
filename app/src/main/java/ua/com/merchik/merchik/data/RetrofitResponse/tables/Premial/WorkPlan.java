package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WorkPlan {
    @SerializedName("total_plan")
    @Expose
    public long totalPlan;
    @SerializedName("total_done")
    @Expose
    public long totalDone;
    @SerializedName("total_undone")
    @Expose
    public long totalUndone;
    @SerializedName("total_status_done")
    @Expose
    public long totalStatusDone;
}
