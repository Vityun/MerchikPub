package ua.com.merchik.merchik.data.RetrofitResponse.tables.update.wpdata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WpDataUpdateResponse {
    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("data")
    @Expose
    public List<WpDataUpdateResponseList> data = null;

    @SerializedName("error")
    @Expose
    public String error;
}
