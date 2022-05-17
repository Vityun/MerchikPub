package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestApproveData {
    @SerializedName("approve_need_plan")
    @Expose
    public long approveNeedPlan;
    @SerializedName("approve_need_task")
    @Expose
    public long approveNeedTask;
    @SerializedName("approve_need")
    @Expose
    public long approveNeed;
}
