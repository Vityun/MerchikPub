package ua.com.merchik.merchik.data.RetrofitResponse.tables;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.LocationDevices;
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;

public class LocationDevicesResponse {
    @SerializedName("state")
    @Expose
    public Boolean state;
    @SerializedName("list")
    @Expose
    public List<LocationDevices> list = null;

    @SerializedName("error")
    @Expose
    public String error;
}
