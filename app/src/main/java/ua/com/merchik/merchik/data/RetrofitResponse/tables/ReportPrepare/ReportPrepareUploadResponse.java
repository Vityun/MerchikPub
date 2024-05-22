package ua.com.merchik.merchik.data.RetrofitResponse.tables.ReportPrepare;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReportPrepareUploadResponse {
    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("error")
    @Expose
    public String error;

    @SerializedName("data")
    @Expose
    public List<ReportPrepareUploadList> data = null;
}
