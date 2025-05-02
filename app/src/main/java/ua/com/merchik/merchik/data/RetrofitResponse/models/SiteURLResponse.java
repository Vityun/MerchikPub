package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SiteURLResponse {
    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("page_total")
    @Expose
    public Integer pageTotal;

    @SerializedName("item_total")
    @Expose
    public Integer itemTotal;

    @SerializedName("list")
    @Expose
    public List<SiteURLItemResponse> list = null;
}
