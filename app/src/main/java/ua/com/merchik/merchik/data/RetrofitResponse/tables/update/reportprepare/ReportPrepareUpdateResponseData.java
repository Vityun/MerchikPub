package ua.com.merchik.merchik.data.RetrofitResponse.tables.update.reportprepare;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReportPrepareUpdateResponseData {
    @SerializedName("client_end_dt")
    @Expose
    public boolean clientEndDt;

    @SerializedName("client_start_dt")
    @Expose
    public boolean clientStartDt;

    @SerializedName("visit_end_dt")
    @Expose
    public boolean visitEndDt;

    @SerializedName("visit_start_dt")
    @Expose
    public boolean visitStartDt;

}
