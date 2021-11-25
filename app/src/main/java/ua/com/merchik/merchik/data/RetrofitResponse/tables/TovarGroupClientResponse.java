package ua.com.merchik.merchik.data.RetrofitResponse.tables;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.TovarGroupClientSDB;

public class TovarGroupClientResponse {

    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("list")
    @Expose
    public List<TovarGroupClientSDB> list;

    @SerializedName("error")
    @Expose
    public String error;
}
