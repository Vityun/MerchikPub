package ua.com.merchik.merchik.data.RetrofitResponse.tables.update.wpdata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WpDataUpdateResponseList {
    @SerializedName("element_id")
    @Expose
    public Long elementId;

    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("data")
    @Expose
    public WpDataUpdateResponseListData data;

    @SerializedName("error")
    @Expose
    public String error;
}
