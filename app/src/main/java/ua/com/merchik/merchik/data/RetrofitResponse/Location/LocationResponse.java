package ua.com.merchik.merchik.data.RetrofitResponse.Location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LocationResponse {
    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("list")
    @Expose
    public List<LocationList> list;

    @SerializedName("error")
    @Expose
    public String error;
}
