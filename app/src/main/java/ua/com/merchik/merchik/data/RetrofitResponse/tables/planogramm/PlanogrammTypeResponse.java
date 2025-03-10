package ua.com.merchik.merchik.data.RetrofitResponse.tables.planogramm;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammAddressSDB;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammTypeSDB;

public class PlanogrammTypeResponse {
    @SerializedName("state")
    public Boolean state;

    @SerializedName("list")
    public List<PlanogrammTypeSDB> list;

    @SerializedName("error")
    public String error;
}
