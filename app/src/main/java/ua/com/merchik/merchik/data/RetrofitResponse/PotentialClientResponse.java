package ua.com.merchik.merchik.data.RetrofitResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.PotentialClientSDB;

public class PotentialClientResponse {
    @SerializedName("state")
    @Expose
    public boolean state;
    @SerializedName("list")
    @Expose
    public List<PotentialClientSDB> list = null;
}
