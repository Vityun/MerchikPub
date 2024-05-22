package ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammImagesSDB;

public class PlanogrammImagesResponse {
    @SerializedName("state")
    public Boolean state;

    @SerializedName("list")
    public List<PlanogrammImagesSDB> list;

    @SerializedName("error")
    public String error;
}
