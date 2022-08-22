package ua.com.merchik.merchik.data.RetrofitResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsSDB;

public class AdditionalMaterialsResponse {
    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("list")
    @Expose
    public List<AdditionalMaterialsSDB> list = null;
}
