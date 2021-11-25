package ua.com.merchik.merchik.data.RetrofitResponse.tables;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.CitySDB;

public class CityResponse {
    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("list")
    @Expose
    public List<CitySDB> list = null;

    @SerializedName("error")
    @Expose
    public String error;

    @SerializedName("server_time")
    @Expose
    public Integer serverTime;
}
