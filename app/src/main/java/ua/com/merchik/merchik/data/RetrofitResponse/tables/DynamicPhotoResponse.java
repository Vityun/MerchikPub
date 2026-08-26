package ua.com.merchik.merchik.data.RetrofitResponse.tables;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.DynamicPhotoSDB;

public class DynamicPhotoResponse {

    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("list")
    @Expose
    public List<DynamicPhotoSDB> list;

    @SerializedName("page_total")
    @Expose
    public Integer pageTotal;

    @SerializedName("item_total")
    @Expose
    public String itemTotal;

    @SerializedName("rows_total")
    @Expose
    public Integer rowsTotal;

    @SerializedName("error")
    @Expose
    public String error;
}
