package ua.com.merchik.merchik.data.RetrofitResponse.tables.update.reportprepare;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReportPrepareUpdateResponse {
    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("data")
    @Expose
    public List<ReportPrepareUpdateResponseList> data = null;

    @SerializedName("error")
    @Expose
    public String error;
}
