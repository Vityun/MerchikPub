package ua.com.merchik.merchik.data.RetrofitResponse.tables.update.wpdata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WpDataUpdateResponseList {
    @SerializedName("element_id")
    @Expose
    public Integer elementId;

    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("error")
    @Expose
    public String error;
}
