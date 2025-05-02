package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsGroupsSDB;

public class AdditionalMaterialsGroupsResponse {
    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("list")
    @Expose
    public List<AdditionalMaterialsGroupsSDB> list = null;
}
