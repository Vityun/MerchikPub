package ua.com.merchik.merchik.data.RetrofitResponse.tables.ReportPrepare;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReportPrepareUploadList {
    @SerializedName("element_id")
    @Expose
    public String elementId;

    @SerializedName("state")
    @Expose
    public boolean state;
}
