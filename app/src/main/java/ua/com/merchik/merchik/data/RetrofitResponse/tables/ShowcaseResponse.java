package ua.com.merchik.merchik.data.RetrofitResponse.tables;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;

public class ShowcaseResponse {
    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("list")
    @Expose
    public List<ShowcaseSDB> list;

    @SerializedName("rows_total")
    @Expose
    public Integer rowsTotal;

    @SerializedName("item_total")
    @Expose
    public String itemTotal;

    @SerializedName("page_total")
    @Expose
    public Integer pageTotal;
}
