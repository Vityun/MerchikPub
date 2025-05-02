package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.ViewListSDB;

public class ViewListResponse {

    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("ID")
    @Expose
    public List<ViewListSDB> list;

    @SerializedName("error")
    @Expose
    public String error;

}
