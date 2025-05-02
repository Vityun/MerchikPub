package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;

public class SiteObjectsResponse {
    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("object_list")
    @Expose
    public List<SiteObjectsSDB> objectSQLList = null;

    @SerializedName("error")
    @Expose
    public String error;
}
